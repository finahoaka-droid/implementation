package org.example.facebookregression.services;

import org.example.facebookregression.dto.EntrainementResultat;
import org.example.facebookregression.models.DonneesFacebookEncoded;
import org.example.facebookregression.repositories.DonneesFacebookEncodedRepository;
import org.example.facebookregression.weka.FacebookEncoder;
import org.example.facebookregression.weka.ModelQuality;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Service Spring qui encapsule la logique de WekaFacebookClassification :
 * évalue, entraîne et stocke les modèles de classification Facebook.
 *
 * <ul>
 *   <li>Lit le CSV encodé {@code output/features_facebook_encoded.csv} (ou le génère depuis la BD)</li>
 *   <li>Lance une validation croisée répétée (ModelQuality) pour chaque cible</li>
 *   <li>Entraîne le meilleur candidat sur tout le jeu de données</li>
 *   <li>Stocke le modèle sérialisé dans la table {@code modeles_classification_facebook}</li>
 * </ul>
 *
 * Endpoint exposé : {@code POST /entrainement/lancer}
 */
@Service
public class WekaEntrainementService {

    private static final String[] CIBLES = {
        "reactions_classe", "commentaires_classe", "vues_classe"
    };

    private final DonneesFacebookEncodedRepository encodedRepo;

    public WekaEntrainementService(DonneesFacebookEncodedRepository encodedRepo) {
        this.encodedRepo = encodedRepo;
    }

    // Credentials PostgreSQL pour le pipeline Weka
    @Value("${app.prediction.db-url:jdbc:postgresql://localhost:5433/facebook}")
    private String dbUrl;

    @Value("${app.prediction.db-user:postgres}")
    private String dbUser;

    @Value("${app.prediction.db-password:}")
    private String dbPassword;

    // Répertoire contenant les CSV générés par la collecte
    @Value("${app.collecte.csv-output-dir:output}")
    private String csvOutputDir;

    // ------------------------------------------------------------------
    // Point d'entrée principal
    // ------------------------------------------------------------------

    /**
     * Lance le pipeline complet : lecture/génération CSV → évaluation → entraînement → stockage en base.
     *
     * @return {@link EntrainementResultat} avec le détail par cible
     */
    public EntrainementResultat lancer() {
        // Désactiver les logs verbeux de Weka pendant le traitement
        Logger.getLogger("").setLevel(Level.SEVERE);

        EntrainementResultat resultat = new EntrainementResultat();
        List<EntrainementResultat.CibleResultat> details = new ArrayList<>();

        String fichier = csvOutputDir + "/features_facebook_encoded.csv";
        File file = new File(fichier);

        // Si la base contient des données encodées, synchroniser/générer le CSV automatiquement
        List<DonneesFacebookEncoded> instancesDb = encodedRepo.findAll();
        if (!instancesDb.isEmpty()) {
            System.out.printf("[WekaEntrainement] Génération automatique du CSV %s depuis %d lignes en base...%n",
                    fichier, instancesDb.size());
            try {
                genererCsvEncoded(instancesDb, fichier);
            } catch (Exception e) {
                System.err.println("[WekaEntrainement] Attention, échec de la génération automatique du CSV : " + e.getMessage());
            }
        } else if (!file.exists()) {
            resultat.setErreur("Aucune donnée disponible pour l'entraînement. La base est vide et le fichier "
                    + fichier + " est introuvable. Veuillez d'abord lancer une collecte (/collecte/lancer).");
            resultat.setMessage("Échec du pipeline d'entraînement.");
            resultat.setCibles(details);
            return resultat;
        }

        System.out.printf("[WekaEntrainement] Démarrage pipeline – CSV : %s%n", fichier);

        // 1. Charger le CSV encodé
        Instances data;
        try {
            CSVLoader loader = new CSVLoader();
            loader.setNominalAttributes(indicesColonnesClasse(fichier));
            loader.setSource(new File(fichier));
            data = loader.getDataSet();
            System.out.printf("[WekaEntrainement] %d instances chargées.%n", data.numInstances());
        } catch (Exception e) {
            resultat.setErreur("Impossible de lire le CSV encodé (" + fichier + ") : " + e.getMessage());
            resultat.setMessage("Échec du pipeline d'entraînement.");
            resultat.setCibles(details);
            return resultat;
        }

        // 2. Traiter chaque cible et stocker en base
        int nbStockes = 0;
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            for (String cible : CIBLES) {
                System.out.printf("[WekaEntrainement] Traitement cible : %s%n", cible);
                EntrainementResultat.CibleResultat cr = traiterCible(data, cible, conn);
                details.add(cr);
                if (cr.isStocke()) nbStockes++;
                System.out.printf("[WekaEntrainement]   → %s (stocké=%s)%n", cr.getVerdict(), cr.isStocke());
            }
        } catch (SQLException e) {
            resultat.setErreur("Connexion PostgreSQL impossible : " + e.getMessage());
            resultat.setMessage("Échec du pipeline d'entraînement.");
            resultat.setCibles(details);
            return resultat;
        } catch (Exception e) {
            resultat.setErreur("Erreur inattendue pendant le pipeline : " + e.getMessage());
            resultat.setMessage("Échec du pipeline d'entraînement.");
            resultat.setCibles(details);
            return resultat;
        }

        resultat.setMessage(nbStockes + " modèle(s) entraîné(s) et stocké(s) sur "
                + CIBLES.length + " cibles.");
        resultat.setCibles(details);
        System.out.println("[WekaEntrainement] Pipeline terminé : " + resultat.getMessage());
        return resultat;
    }

    // ------------------------------------------------------------------
    // Traitement d'une seule cible
    // ------------------------------------------------------------------

    private EntrainementResultat.CibleResultat traiterCible(
            Instances data, String cible, Connection conn) {

        EntrainementResultat.CibleResultat cr = new EntrainementResultat.CibleResultat();
        cr.setCible(cible);

        try {
            // Préparer le jeu de données pour cette cible
            Instances prepare = ModelQuality.preparer(data, cible);

            // Évaluer par validation croisée répétée
            ModelQuality.Rapport rapport = ModelQuality.evaluer(prepare, cible);

            cr.setVerdict(rapport.verdict);
            cr.setModele(rapport.modele);
            cr.setNbInstances(rapport.n);
            cr.setResume(rapport.resume());
            if (!Double.isNaN(rapport.accuracy))  cr.setAccuracy(rapport.accuracy  * 100);
            if (!Double.isNaN(rapport.baseline))  cr.setBaseline(rapport.baseline  * 100);

            // Si pas assez de données, on ne peut pas entraîner
            if ("aucun".equals(rapport.modele)) {
                cr.setStocke(false);
                return cr;
            }

            // Entraîner le meilleur candidat sur toutes les données
            Classifier modele = ModelQuality.entrainer(prepare, cible, rapport.modele);
            Instances entete  = ModelQuality.retirerNum(prepare, cible);

            // Construire le nom d'enregistrement et insérer en base
            String nomDb = "facebook_" + cible + "_" + rapport.verdict.toLowerCase();
            stocker(nomDb, cible, modele, entete, rapport, conn);

            cr.setStocke(true);
            cr.setNomModeleDb(nomDb);

        } catch (Exception e) {
            cr.setVerdict("ERREUR");
            cr.setResume("Erreur lors du traitement de " + cible + " : " + e.getMessage());
            cr.setStocke(false);
        }
        return cr;
    }

    // ------------------------------------------------------------------
    // Stockage dans modeles_classification_facebook
    // ------------------------------------------------------------------

    /**
     * Insère le modèle sérialisé (modèle + entête Weka + rapport) dans la table PostgreSQL.
     * La clé de déduplication est laissée à la table (pas d'UPSERT : chaque appel crée
     * un nouvel enregistrement horodaté, conformément au comportement de
     * {@code WekaFacebookClassification}).
     */
    private void stocker(String nom, String cible,
                         Classifier modele, Instances entete,
                         ModelQuality.Rapport rapport, Connection conn) throws Exception {
        String createTableSql = """
                CREATE TABLE IF NOT EXISTS modeles_classification_facebook (
                    id SERIAL PRIMARY KEY,
                    nom_modele VARCHAR(255),
                    variable_cible VARCHAR(100),
                    accuracy DOUBLE PRECISION,
                    date_entrainement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    modele_binaire BYTEA
                );
                """;
        try (Statement stmtCreate = conn.createStatement()) {
            stmtCreate.execute(createTableSql);
        }

        String sql = """
                INSERT INTO modeles_classification_facebook
                    (nom_modele, variable_cible, accuracy, modele_binaire)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nom);
            stmt.setString(2, cible);
            stmt.setDouble(3, Double.isNaN(rapport.accuracy) ? 0 : rapport.accuracy * 100);
            stmt.setBytes(4, ModelQuality.serialiser(modele, entete, rapport));
            stmt.executeUpdate();
        }
    }

    // ------------------------------------------------------------------
    // Utilitaire : indices (1-based) des colonnes _classe dans le CSV
    // ------------------------------------------------------------------

    private static String indicesColonnesClasse(String fichier) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(fichier, StandardCharsets.UTF_8))) {
            String[] colonnes = reader.readLine().split(",");
            StringBuilder indices = new StringBuilder();
            for (int i = 0; i < colonnes.length; i++) {
                if (colonnes[i].trim().endsWith("_classe")) {
                    if (!indices.isEmpty()) indices.append(",");
                    indices.append(i + 1);
                }
            }
            return indices.toString();
        }
    }

    // ------------------------------------------------------------------
    // Génération automatique du CSV encodé à partir des entités de la BD
    // ------------------------------------------------------------------

    public static void genererCsvEncoded(List<DonneesFacebookEncoded> instances, String cheminFichier) throws IOException {
        if (instances == null || instances.isEmpty()) {
            return;
        }

        // 1. Calculer les seuils pour reactions
        List<Double> reactionsTriees = new ArrayList<>();
        for (DonneesFacebookEncoded e : instances) {
            reactionsTriees.add(e.getReactions() != null ? e.getReactions() : 0.0);
        }
        reactionsTriees.sort(Double::compareTo);
        double seuilReactions1 = FacebookEncoder.percentile(reactionsTriees, 0.33);
        double seuilReactions2 = FacebookEncoder.percentile(reactionsTriees, 0.66);

        // 2. Calculer les seuils pour vues
        List<Double> vuesTriees = new ArrayList<>();
        for (DonneesFacebookEncoded e : instances) {
            if (e.getVues() != null) {
                vuesTriees.add(e.getVues());
            }
        }
        vuesTriees.sort(Double::compareTo);
        List<Long> seuilsVues = FacebookEncoder.seuilsVues(vuesTriees);

        File f = new File(cheminFichier);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(f), StandardCharsets.UTF_8))) {

            writer.println(String.join(",",
                    "longueur_message", "nb_hashtags", "nb_tags",
                    "contient_lien_externe", "contient_emoji", "contient_question",
                    "type_post_photo", "type_post_album", "type_post_video", "type_post_lien", "type_post_evenement",
                    "ratio_image", "nb_photos_album", "duree_video", "longueur_titre_video",
                    "jour_lundi", "jour_mardi", "jour_mercredi", "jour_jeudi", "jour_vendredi", "jour_samedi",
                    "heure_publication", "jours_depuis_dernier_post", "nb_posts_7j", "nb_posts_30j",
                    "vues", "reactions", "commentaires",
                    "reactions_classe", "commentaires_classe", "vues_classe"
            ));

            for (DonneesFacebookEncoded e : instances) {
                double r = e.getReactions() != null ? e.getReactions() : 0.0;
                double c = e.getCommentaires() != null ? e.getCommentaires() : 0.0;

                String rClasse = FacebookEncoder.classifierReactions(r, seuilReactions1, seuilReactions2);
                String cClasse = FacebookEncoder.classifierCommentaires(c);
                String vClasse = (e.getVues() != null)
                        ? FacebookEncoder.classifierVues(e.getVues(), seuilsVues)
                        : "non_applicable";

                List<String> line = new ArrayList<>();
                line.add(fmt(e.getLongueurMessage()));
                line.add(fmt(e.getNbHashtags()));
                line.add(fmt(e.getNbTags()));
                line.add(fmt(e.getContientLienExterne()));
                line.add(fmt(e.getContientEmoji()));
                line.add(fmt(e.getContientQuestion()));
                line.add(fmt(e.getTypePostPhoto()));
                line.add(fmt(e.getTypePostAlbum()));
                line.add(fmt(e.getTypePostVideo()));
                line.add(fmt(e.getTypePostLien()));
                line.add(fmt(e.getTypePostEvenement()));
                line.add(fmt(e.getRatioImage()));
                line.add(fmt(e.getNbPhotosAlbum()));
                line.add(fmt(e.getDureeVideo()));
                line.add(fmt(e.getLongueurTitreVideo()));
                line.add(fmt(e.getJourLundi()));
                line.add(fmt(e.getJourMardi()));
                line.add(fmt(e.getJourMercredi()));
                line.add(fmt(e.getJourJeudi()));
                line.add(fmt(e.getJourVendredi()));
                line.add(fmt(e.getJourSamedi()));
                line.add(fmt(e.getHeurePublication()));
                line.add(fmt(e.getJoursDepuisDernierPost()));
                line.add(fmt(e.getNbPosts7j()));
                line.add(fmt(e.getNbPosts30j()));
                line.add(fmt(e.getVues()));
                line.add(fmt(e.getReactions()));
                line.add(fmt(e.getCommentaires()));
                line.add(rClasse);
                line.add(cClasse);
                line.add(vClasse);

                writer.println(String.join(",", line));
            }
        }
    }

    private static String fmt(Double val) {
        if (val == null) return "";
        if (val == Math.floor(val) && !Double.isInfinite(val)) {
            return String.valueOf((long) val.doubleValue());
        }
        return String.valueOf(val);
    }
}
