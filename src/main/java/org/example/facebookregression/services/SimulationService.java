package org.example.facebookregression.services;

import org.example.facebookregression.dto.SimulationRequest;
import org.example.facebookregression.dto.SimulationResultat;
import org.example.facebookregression.weka.FacebookEncoder;
import org.example.facebookregression.weka.ModelQuality;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service pour la simulation de publications Facebook et la prédiction
 * du nombre de réactions, de commentaires et de vues.
 */
@Service
public class SimulationService {

    @Value("${app.prediction.db-url:jdbc:postgresql://localhost:5433/facebook}")
    private String dbUrl;

    @Value("${app.prediction.db-user:postgres}")
    private String dbUser;

    @Value("${app.prediction.db-password:}")
    private String dbPassword;

    public SimulationResultat simuler(SimulationRequest request) {
        SimulationResultat resultat = new SimulationResultat();

        if (request == null) {
            request = new SimulationRequest();
        }

        Map<String, String> raw = request.toRawMap();
        Map<String, String> encode = FacebookEncoder.encodeRow(raw);
        String typePost = raw.getOrDefault("type_post", "photo");

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {

            // 1. Charger et prédire les réactions
            ModelQuality.Lot lotReactions = chargerDernierModele(conn, "reactions_classe");
            if (lotReactions == null) {
                resultat.setErreur("Aucun modèle trouvé pour 'reactions_classe'. Veuillez d'abord exécuter l'entraînement (POST /entrainement/lancer).");
                resultat.setMessage("Échec de la simulation : modèle manquant.");
                return resultat;
            }
            resultat.setReactions(predire("reactions_classe", lotReactions, encode));

            // 2. Charger et prédire les commentaires
            ModelQuality.Lot lotCommentaires = chargerDernierModele(conn, "commentaires_classe");
            if (lotCommentaires == null) {
                resultat.setErreur("Aucun modèle trouvé pour 'commentaires_classe'. Veuillez d'abord exécuter l'entraînement (POST /entrainement/lancer).");
                resultat.setMessage("Échec de la simulation : modèle manquant.");
                return resultat;
            }
            resultat.setCommentaires(predire("commentaires_classe", lotCommentaires, encode));

            // 3. Charger et prédire les vues (seulement si vidéo ou album)
            SimulationResultat.PredictionDetail detailVues = new SimulationResultat.PredictionDetail();
            detailVues.setCible("vues_classe");

            boolean vuesApplicables = typePost.equals("video") || typePost.equals("album");
            detailVues.setApplicable(vuesApplicables);

            if (vuesApplicables) {
                ModelQuality.Lot lotVues = chargerDernierModele(conn, "vues_classe");
                if (lotVues != null) {
                    detailVues = predire("vues_classe", lotVues, encode);
                    detailVues.setApplicable(true);
                } else {
                    detailVues.setClassePredite("inconnue");
                    detailVues.setAvertissement("Modèle des vues non encore entraîné.");
                }
            } else {
                detailVues.setClassePredite("non_applicable");
                detailVues.setProbabilite(0.0);
                detailVues.setFiabilite("NON_APPLICABLE");
                detailVues.setAvertissement("Les vues ne sont mesurées et prédictibles que pour les publications de type 'video' ou 'album'.");
            }
            resultat.setVues(detailVues);

            resultat.setMessage("Simulation calculée avec succès pour une publication de type '" + typePost + "'.");

        } catch (Exception e) {
            resultat.setErreur("Erreur lors de la simulation : " + e.getMessage());
            resultat.setMessage("Échec de la simulation.");
        }

        return resultat;
    }

    private SimulationResultat.PredictionDetail predire(
            String cible, ModelQuality.Lot lot, Map<String, String> encode) throws Exception {

        SimulationResultat.PredictionDetail detail = new SimulationResultat.PredictionDetail();
        detail.setCible(cible);
        detail.setApplicable(true);

        if (lot == null || lot.modele == null || lot.entete == null) {
            detail.setClassePredite("inconnue");
            detail.setAvertissement("Modèle ou entête indisponible pour " + cible);
            return detail;
        }

        Instances entete = lot.entete;
        Instance inst = new DenseInstance(entete.numAttributes());
        inst.setDataset(entete);

        for (int i = 0; i < entete.numAttributes(); i++) {
            if (i == entete.classIndex()) {
                inst.setMissing(i);
            } else {
                String nomAttr = entete.attribute(i).name();
                String val = encode.get(nomAttr);
                inst.setValue(i, val == null || val.isEmpty() ? 0 : Double.parseDouble(val));
            }
        }

        double[] dist = lot.modele.distributionForInstance(inst);
        int best = 0;
        for (int i = 1; i < dist.length; i++) {
            if (dist[i] > dist[best]) best = i;
        }

        String classe = entete.classAttribute().value(best);
        double proba = dist[best] * 100;

        detail.setClassePredite(classe);
        detail.setProbabilite(Math.round(proba * 10.0) / 10.0);

        ModelQuality.Rapport rapport = lot.rapport;
        if (rapport != null) {
            detail.setFiabilite(rapport.verdict);
            if (rapport.fiable()) {
                detail.setAvertissement("");
            } else if (ModelQuality.INSUFFISANT.equals(rapport.verdict)) {
                detail.setAvertissement("[!] Données insuffisantes à l'entraînement : résultat purement indicatif.");
            } else {
                detail.setAvertissement("[!] Modèle pas meilleur que le hasard : ne pas s'y fier.");
            }
        } else {
            detail.setFiabilite("INCONNUE");
            detail.setAvertissement("(fiabilité inconnue : réentraîner)");
        }

        Map<String, Double> distMap = new LinkedHashMap<>();
        for (int i = 0; i < dist.length; i++) {
            String c = entete.classAttribute().value(i);
            if (!c.equals("non_applicable")) {
                distMap.put(c, Math.round(dist[i] * 1000.0) / 10.0);
            }
        }
        detail.setProbabilitesParClasse(distMap);

        return detail;
    }

    private ModelQuality.Lot chargerDernierModele(Connection conn, String cible) throws Exception {
        String sql = """
                SELECT modele_binaire FROM modeles_classification_facebook
                WHERE variable_cible = ?
                ORDER BY date_entrainement DESC, id DESC
                LIMIT 1
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cible);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                byte[] bytes = rs.getBytes("modele_binaire");
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return ModelQuality.deserialiser(bytes);
            }
        }
    }
}
