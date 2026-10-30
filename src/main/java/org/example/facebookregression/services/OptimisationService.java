package org.example.facebookregression.services;

import org.example.facebookregression.dto.RecommandationRequest;
import org.example.facebookregression.dto.RecommandationResultat;
import org.example.facebookregression.dto.SimulationRequest;
import org.example.facebookregression.dto.SuggestionResultat;
import org.example.facebookregression.weka.FacebookEncoder;
import org.example.facebookregression.weka.ModelQuality;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service pour la recommandation de profils de publication et la suggestion
 * d'améliorations basées sur les modèles Weka entraînés.
 *
 * <ul>
 *   <li><b>recommander</b> : étant donné des classes cibles (ex: réactions "7-18"),
 *       retourne les profils observés (bruts) qui maximisent la probabilité conjointe
 *       d'atteindre ces classes.</li>
 *   <li><b>suggerer</b> : étant donné un brouillon de publication et des classes cibles,
 *       propose des modifications concrètes qui améliorent le score conjoint.</li>
 * </ul>
 */
@Service
public class OptimisationService {

    /* ------------------------------------------------------------------ config */

    @Value("${app.prediction.db-url:jdbc:postgresql://localhost:5433/facebook}")
    private String dbUrl;

    @Value("${app.prediction.db-user:postgres}")
    private String dbUser;

    @Value("${app.prediction.db-password:}")
    private String dbPassword;

    @Value("${app.collecte.csv-output-dir:output}/features_facebook.csv")
    private String fichierBrut;

    /** Nombre maximal de profils retournés par recommander. */
    private static final int MAX_PROFILS = 3;

    /** Nombre maximal de publications de référence pour les suggestions. */
    private static final int NB_REFERENCES = 5;

    /** Score minimal d'une référence par rapport au meilleur (proportion). */
    private static final double RATIO_REFERENCE = 0.5;

    /** Gain minimal (points de probabilité) pour qu'une suggestion soit "utile". */
    private static final double GAIN_MIN = 0.03;

    /** Plancher pour éviter log(0) dans la moyenne géométrique. */
    private static final double FLOOR = 0.01;

    private static final String[] CIBLES_NOMS = {
            "reactions_classe", "commentaires_classe", "vues_classe"
    };

    private static final Set<String> BOOLEENS = Set.of(
            "contient_lien_externe", "contient_emoji", "contient_question");

    private static final Set<String> NUMERIQUES = Set.of(
            "longueur_message", "nb_hashtags", "nb_tags", "nb_photos_album", "duree_video",
            "longueur_titre_video", "heure_publication", "jours_depuis_dernier_post",
            "nb_posts_7j", "nb_posts_30j");

    private static final List<String> MODIFIABLES = List.of(
            "longueur_message", "nb_hashtags", "nb_tags",
            "contient_lien_externe", "contient_emoji", "contient_question",
            "ratio_image", "nb_photos_album", "duree_video", "longueur_titre_video",
            "jour_semaine", "heure_publication",
            "jours_depuis_dernier_post", "nb_posts_7j", "nb_posts_30j");

    /* ================================================================== RECOMMANDER ========= */

    /**
     * Recommande les profils observés (dans le CSV brut) qui maximisent la probabilité conjointe
     * d'atteindre les classes cibles demandées.
     */
    public RecommandationResultat recommander(RecommandationRequest request) {
        RecommandationResultat resultat = new RecommandationResultat();

        // Construire la map des cibles
        Map<String, String> cibles = new LinkedHashMap<>();
        if (request.getReactionsClasse() != null && !request.getReactionsClasse().isBlank())
            cibles.put("reactions_classe", request.getReactionsClasse().trim());
        if (request.getCommentairesClasse() != null && !request.getCommentairesClasse().isBlank())
            cibles.put("commentaires_classe", request.getCommentairesClasse().trim());
        if (request.getVuesClasse() != null && !request.getVuesClasse().isBlank())
            cibles.put("vues_classe", request.getVuesClasse().trim());

        if (cibles.isEmpty()) {
            resultat.setErreur("Aucune classe cible fournie. Veuillez renseigner au moins reactionsClasse ou commentairesClasse.");
            resultat.setMessage("Requête invalide.");
            return resultat;
        }

        resultat.setCibles(cibles);

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            // Charger les modèles
            Map<String, Classifier> modeles = new LinkedHashMap<>();
            Map<String, Instances> entetes = new LinkedHashMap<>();
            for (String nomCible : cibles.keySet()) {
                ModelQuality.Lot lot = chargerDernierModele(conn, nomCible);
                if (lot == null || lot.modele == null || lot.entete == null) {
                    resultat.setErreur("Modèle introuvable pour '" + nomCible + "'. Lancez d'abord l'entraînement (POST /entrainement/lancer).");
                    resultat.setMessage("Échec de la recommandation.");
                    return resultat;
                }
                modeles.put(nomCible, lot.modele);
                entetes.put(nomCible, lot.entete);
            }

            // Lire les candidats bruts depuis le CSV
            List<Map<String, String>> candidatsBruts = lireCandidatsBruts();
            if (candidatsBruts.isEmpty()) {
                resultat.setErreur("Le fichier CSV brut est introuvable ou vide : " + fichierBrut
                        + ". Lancez d'abord la collecte (POST /collecte/lancer).");
                resultat.setMessage("Aucune donnée observée disponible.");
                return resultat;
            }

            // Évaluer et trier les candidats
            List<CandidatScore> scores = new ArrayList<>();
            for (Map<String, String> brut : candidatsBruts) {
                CandidatScore cs = evaluerCandidat(brut, modeles, entetes, cibles);
                if (cs != null) scores.add(cs);
            }

            if (scores.isEmpty()) {
                resultat.setMessage("Aucun profil observé ne satisfait la combinaison de classes demandée.");
                resultat.setProfils(List.of());
                resultat.setNote("Essayez des classes moins restrictives, ou collectez plus de données (POST /collecte/lancer).");
                return resultat;
            }

            scores.sort(Comparator.comparingDouble((CandidatScore c) -> c.score).reversed());

            // Construire les profils recommandés
            int limite = Math.min(MAX_PROFILS, scores.size());
            List<RecommandationResultat.ProfilRecommande> profils = new ArrayList<>();
            for (int i = 0; i < limite; i++) {
                CandidatScore cs = scores.get(i);
                RecommandationResultat.ProfilRecommande pr = new RecommandationResultat.ProfilRecommande();
                pr.setRang(i + 1);
                pr.setScorePourcent(arrondir(cs.score * 100));
                pr.setProfil(cs.raw);

                Map<String, Double> confPourcent = new LinkedHashMap<>();
                for (Map.Entry<String, Double> e : cs.confidences.entrySet()) {
                    confPourcent.put(e.getKey(), arrondir(e.getValue() * 100));
                }
                pr.setConfidencesPourcent(confPourcent);
                profils.add(pr);
            }

            resultat.setProfils(profils);
            resultat.setMessage(limite + " profil(s) recommandé(s) sur " + scores.size() + " candidats compatibles.");
            resultat.setNote("Le score est la moyenne géométrique des probabilités par cible. "
                    + "Ces profils sont corrélés aux données observées et n'offrent pas de garantie de résultat.");

        } catch (Exception e) {
            resultat.setErreur("Erreur lors de la recommandation : " + e.getMessage());
            resultat.setMessage("Échec de la recommandation.");
        }

        return resultat;
    }

    /* ================================================================== SUGGERER ============ */

    /**
     * Suggère des améliorations à apporter à un brouillon de publication pour rapprocher
     * son score conjoint des classes cibles demandées.
     */
    public SuggestionResultat suggerer(SimulationRequest brouillonRequest, RecommandationRequest ciblesRequest) {
        SuggestionResultat resultat = new SuggestionResultat();

        if (brouillonRequest == null) brouillonRequest = new SimulationRequest();
        Map<String, String> brouillon = brouillonRequest.toRawMap();

        Map<String, String> cibles = new LinkedHashMap<>();
        if (ciblesRequest.getReactionsClasse() != null && !ciblesRequest.getReactionsClasse().isBlank())
            cibles.put("reactions_classe", ciblesRequest.getReactionsClasse().trim());
        if (ciblesRequest.getCommentairesClasse() != null && !ciblesRequest.getCommentairesClasse().isBlank())
            cibles.put("commentaires_classe", ciblesRequest.getCommentairesClasse().trim());
        if (ciblesRequest.getVuesClasse() != null && !ciblesRequest.getVuesClasse().isBlank())
            cibles.put("vues_classe", ciblesRequest.getVuesClasse().trim());

        if (cibles.isEmpty()) {
            resultat.setErreur("Aucune classe cible fournie.");
            resultat.setMessage("Requête invalide.");
            return resultat;
        }

        resultat.setCibles(cibles);

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            // Charger modèles
            Map<String, Classifier> modeles = new LinkedHashMap<>();
            Map<String, Instances> entetes = new LinkedHashMap<>();
            for (String nomCible : cibles.keySet()) {
                ModelQuality.Lot lot = chargerDernierModele(conn, nomCible);
                if (lot == null || lot.modele == null || lot.entete == null) {
                    resultat.setErreur("Modèle introuvable pour '" + nomCible + "'. Lancez d'abord l'entraînement (POST /entrainement/lancer).");
                    resultat.setMessage("Échec de la suggestion.");
                    return resultat;
                }
                modeles.put(nomCible, lot.modele);
                entetes.put(nomCible, lot.entete);
            }

            // Score actuel du brouillon
            Map<String, Double> probasActuelles = calculerProbabilites(brouillon, modeles, entetes, cibles);
            double scoreActuel = score(probasActuelles);
            resultat.setScoreActuelPourcent(arrondir(scoreActuel * 100));
            resultat.setProbasActuellesPourcent(enPourcent(probasActuelles));

            // Lire les publications observées
            List<Map<String, String>> observes = lireCandidatsBruts();
            if (observes.isEmpty()) {
                resultat.setErreur("Aucune donnée observée disponible (CSV introuvable : " + fichierBrut + ").");
                resultat.setMessage("Impossible de suggérer sans données de référence.");
                return resultat;
            }

            // Trouver les meilleures références
            List<Map<String, String>> references = trouverReferences(observes, modeles, entetes, cibles);
            if (references.isEmpty()) {
                resultat.setMessage("Aucune publication observée ne s'approche de cet objectif : impossible de proposer des suggestions.");
                resultat.setScoreReferencePourcent(0);
                resultat.setSuggestionsUtiles(List.of());
                resultat.setAutresDifferences(List.of());
                return resultat;
            }

            double scoreRef = score(calculerProbabilites(references.get(0), modeles, entetes, cibles));
            resultat.setScoreReferencePourcent(arrondir(scoreRef * 100));

            // Générer les suggestions
            String typeActuel = brouillon.getOrDefault("type_post", "photo");
            List<Suggestion> touteSuggestions = new ArrayList<>();

            // Suggestion sur le type de post
            String typeRef = mode(references, "type_post");
            if (!typeRef.isEmpty() && !typeRef.equals(typeActuel)) {
                Map<String, String> modif = avecType(brouillon, typeRef, references);
                double apres = score(calculerProbabilites(modif, modeles, entetes, cibles));
                touteSuggestions.add(new Suggestion("type_post", typeRef,
                        "Publier plutôt en " + typeRef + " (actuellement " + typeActuel + ")",
                        apres, apres - scoreActuel));
            }

            // Suggestions sur chaque caractéristique modifiable
            for (String feature : MODIFIABLES) {
                if (!applicable(feature, typeActuel)) continue;
                String propose = valeurReference(feature, references);
                String actuel = brouillon.getOrDefault(feature, "");
                if (propose.isEmpty() || memeValeur(feature, actuel, propose)) continue;

                Map<String, String> modif = new LinkedHashMap<>(brouillon);
                modif.put(feature, propose);
                double apres = score(calculerProbabilites(modif, modeles, entetes, cibles));
                touteSuggestions.add(new Suggestion(feature, propose,
                        texte(feature, actuel, propose), apres, apres - scoreActuel));
            }

            touteSuggestions.sort(Comparator.comparingDouble((Suggestion s) -> s.gain).reversed());

            // Séparer utiles / autres
            List<SuggestionResultat.SuggestionItem> suggestionsUtiles = new ArrayList<>();
            List<String> autresDiff = new ArrayList<>();
            int rang = 1;
            for (Suggestion s : touteSuggestions) {
                if (s.gain > GAIN_MIN) {
                    SuggestionResultat.SuggestionItem item = new SuggestionResultat.SuggestionItem();
                    item.setRang(rang++);
                    item.setCaracteristique(s.feature);
                    item.setValeurProposee(s.valeur);
                    item.setTexte(s.texte);
                    item.setScoreAvantPourcent(arrondir(scoreActuel * 100));
                    item.setScoreApresPourcent(arrondir(s.scoreApres * 100));
                    item.setGainPoints(arrondir(s.gain * 100));
                    suggestionsUtiles.add(item);
                } else {
                    autresDiff.add(s.texte);
                }
            }

            resultat.setSuggestionsUtiles(suggestionsUtiles);
            resultat.setAutresDifferences(autresDiff);

            // Effet cumulé
            if (!suggestionsUtiles.isEmpty()) {
                Map<String, String> cumul = new LinkedHashMap<>(brouillon);
                for (SuggestionResultat.SuggestionItem si : suggestionsUtiles) {
                    if (si.getCaracteristique().equals("type_post")) {
                        cumul = avecType(cumul, si.getValeurProposee(), references);
                    } else if (applicable(si.getCaracteristique(), cumul.getOrDefault("type_post", ""))) {
                        cumul.put(si.getCaracteristique(), si.getValeurProposee());
                    }
                }
                Map<String, Double> probasCumul = calculerProbabilites(cumul, modeles, entetes, cibles);
                double scoreCumul = score(probasCumul);
                if (scoreCumul > scoreActuel) {
                    resultat.setScoreAmeliorePourcent(arrondir(scoreCumul * 100));
                    resultat.setProbasAmelioreesPourcent(enPourcent(probasCumul));
                }
            }

            if (suggestionsUtiles.isEmpty()) {
                if (scoreActuel >= scoreRef) {
                    resultat.setMessage("Votre publication est déjà au niveau des meilleures publications observées pour cet objectif.");
                } else {
                    resultat.setMessage("Aucun changement isolé ne fait progresser le score selon les modèles.");
                }
            } else {
                resultat.setMessage(suggestionsUtiles.size() + " suggestion(s) identifiée(s) pour améliorer le score conjoint.");
            }
            resultat.setNote("Les suggestions sont corrélatives : elles viennent des publications déjà observées et vérifiées "
                    + "avec les modèles, sans garantie de résultat.");

        } catch (Exception e) {
            resultat.setErreur("Erreur lors de la suggestion : " + e.getMessage());
            resultat.setMessage("Échec de la suggestion.");
        }

        return resultat;
    }

    /* ================================================================== HELPERS ============= */

    /** Charge le dernier modèle entraîné pour une cible. */
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
                if (!rs.next()) return null;
                byte[] bytes = rs.getBytes("modele_binaire");
                if (bytes == null || bytes.length == 0) return null;
                return ModelQuality.deserialiser(bytes);
            }
        }
    }

    /** Lit les candidats bruts uniques depuis le CSV brut (dédupliqués sur les features). */
    private List<Map<String, String>> lireCandidatsBruts() throws IOException {
        List<String> FEATURES = List.of(
                "longueur_message", "nb_hashtags", "nb_tags",
                "contient_lien_externe", "contient_emoji", "contient_question",
                "type_post", "ratio_image", "nb_photos_album", "duree_video",
                "longueur_titre_video", "jour_semaine", "heure_publication",
                "jours_depuis_dernier_post", "nb_posts_7j", "nb_posts_30j"
        );
        try {
            List<Map<String, String>> rows = FacebookEncoder.readCsv(fichierBrut);
            List<Map<String, String>> candidates = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            for (Map<String, String> row : rows) {
                Map<String, String> raw = new LinkedHashMap<>();
                for (String f : FEATURES) raw.put(f, row.getOrDefault(f, ""));
                String key = String.join("\u001f", raw.values());
                if (seen.add(key)) candidates.add(raw);
            }
            return candidates;
        } catch (IOException e) {
            return List.of();
        }
    }

    /** Construit une Instance Weka depuis un profil encodé. */
    private Instance instanceFrom(Instances entete, Map<String, String> encoded) {
        Instance inst = new DenseInstance(entete.numAttributes());
        inst.setDataset(entete);
        for (int i = 0; i < entete.numAttributes(); i++) {
            if (i == entete.classIndex()) {
                inst.setMissing(i);
            } else {
                String val = encoded.get(entete.attribute(i).name());
                inst.setValue(i, val == null || val.isEmpty() ? 0 : Double.parseDouble(val));
            }
        }
        return inst;
    }

    /** Calcule les probabilités des classes voulues pour un profil brut. */
    private Map<String, Double> calculerProbabilites(Map<String, String> raw,
                                                     Map<String, Classifier> modeles,
                                                     Map<String, Instances> entetes,
                                                     Map<String, String> cibles) throws Exception {
        Map<String, String> encoded = FacebookEncoder.encodeRow(raw);
        String type = raw.getOrDefault("type_post", "");
        Map<String, Double> result = new LinkedHashMap<>();

        for (Map.Entry<String, String> cible : cibles.entrySet()) {
            String nomCible = cible.getKey();
            Instances entete = entetes.get(nomCible);
            Classifier modele = modeles.get(nomCible);
            if (entete == null || modele == null) continue;

            boolean applicable = !nomCible.equals("vues_classe") || type.equals("video") || type.equals("album");
            int classIndex = entete.classAttribute().indexOfValue(cible.getValue());
            double proba = 0;
            if (applicable && classIndex >= 0) {
                double[] dist = modele.distributionForInstance(instanceFrom(entete, encoded));
                proba = classIndex < dist.length ? dist[classIndex] : 0;
            }
            result.put(nomCible, proba);
        }
        return result;
    }

    /** Évalue un candidat brut : retourne null si non applicable (vues sur non-video/album) ou score nul. */
    private CandidatScore evaluerCandidat(Map<String, String> raw,
                                          Map<String, Classifier> modeles,
                                          Map<String, Instances> entetes,
                                          Map<String, String> cibles) throws Exception {
        Map<String, String> encoded = FacebookEncoder.encodeRow(raw);
        Map<String, Double> confidences = new LinkedHashMap<>();
        double logScore = 0;
        int nb = 0;

        for (Map.Entry<String, String> cible : cibles.entrySet()) {
            String nomCible = cible.getKey();
            Instances entete = entetes.get(nomCible);
            Classifier modele = modeles.get(nomCible);
            if (entete == null || modele == null) return null;

            if (nomCible.equals("vues_classe")
                    && !raw.getOrDefault("type_post", "").equals("video")
                    && !raw.getOrDefault("type_post", "").equals("album")) {
                return null; // profil non applicable pour la cible vues
            }

            int classIndex = entete.classAttribute().indexOfValue(cible.getValue());
            if (classIndex < 0) return null;

            double[] dist = modele.distributionForInstance(instanceFrom(entete, encoded));
            double conf = classIndex < dist.length ? dist[classIndex] : 0;
            if (conf <= 0) return null;

            confidences.put(nomCible, conf);
            logScore += Math.log(conf);
            nb++;
        }

        if (nb == 0) return null;
        return new CandidatScore(raw, Math.exp(logScore / nb), confidences);
    }

    /** Trouve les publications observées les plus proches de l'objectif. */
    private List<Map<String, String>> trouverReferences(List<Map<String, String>> observes,
                                                         Map<String, Classifier> modeles,
                                                         Map<String, Instances> entetes,
                                                         Map<String, String> cibles) throws Exception {
        List<double[]> scored = new ArrayList<>(); // [score, index]
        for (int i = 0; i < observes.size(); i++) {
            Map<String, Double> probas = calculerProbabilites(observes.get(i), modeles, entetes, cibles);
            scored.add(new double[]{score(probas), i});
        }
        scored.sort((a, b) -> Double.compare(b[0], a[0]));

        if (scored.isEmpty() || scored.get(0)[0] <= FLOOR + 1e-9) return List.of();

        double meilleurScore = scored.get(0)[0];
        List<Map<String, String>> refs = new ArrayList<>();
        for (double[] entry : scored) {
            if (refs.size() >= NB_REFERENCES || entry[0] < meilleurScore * RATIO_REFERENCE) break;
            refs.add(observes.get((int) entry[1]));
        }
        return refs;
    }

    /** Moyenne géométrique avec plancher. */
    private double score(Map<String, Double> probas) {
        if (probas.isEmpty()) return 0;
        double somme = 0;
        for (double p : probas.values()) somme += Math.log(Math.max(p, FLOOR));
        return Math.exp(somme / probas.size());
    }

    private Map<String, Double> enPourcent(Map<String, Double> probas) {
        Map<String, Double> r = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : probas.entrySet()) r.put(e.getKey(), arrondir(e.getValue() * 100));
        return r;
    }

    private double arrondir(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    /* ---- Valeurs de référence ---- */

    private boolean applicable(String feature, String type) {
        return switch (feature) {
            case "nb_photos_album" -> type.equals("album");
            case "duree_video", "longueur_titre_video" -> type.equals("video");
            case "ratio_image" -> !type.equals("texte") && !type.equals("evenement");
            default -> true;
        };
    }

    private String valeurReference(String feature, List<Map<String, String>> refs) {
        if (BOOLEENS.contains(feature)) {
            long vrais = refs.stream().filter(m -> estVrai(m.getOrDefault(feature, ""))).count();
            return vrais * 2 > refs.size() ? "true" : "false";
        }
        if (NUMERIQUES.contains(feature)) {
            List<Double> vals = new ArrayList<>();
            for (Map<String, String> m : refs) {
                String v = m.getOrDefault(feature, "");
                if (v.isBlank()) continue;
                try { vals.add(Double.parseDouble(v.replace(',', '.'))); } catch (NumberFormatException ignored) { }
            }
            if (vals.isEmpty()) return "";
            vals.sort(Double::compare);
            int n = vals.size();
            double med = n % 2 == 1 ? vals.get(n / 2) : (vals.get(n / 2 - 1) + vals.get(n / 2)) / 2;
            return String.valueOf(Math.round(med));
        }
        return mode(refs, feature);
    }

    private String mode(List<Map<String, String>> refs, String feature) {
        Map<String, Integer> compte = new LinkedHashMap<>();
        for (Map<String, String> m : refs) {
            String v = m.getOrDefault(feature, "");
            if (!v.isBlank()) compte.merge(v, 1, Integer::sum);
        }
        String meilleur = "";
        int max = 0;
        for (Map.Entry<String, Integer> e : compte.entrySet()) {
            if (e.getValue() > max) { max = e.getValue(); meilleur = e.getKey(); }
        }
        return meilleur;
    }

    private Map<String, String> avecType(Map<String, String> base, String type, List<Map<String, String>> refs) {
        Map<String, String> m = new LinkedHashMap<>(base);
        Map<String, String> modele = null;
        for (Map<String, String> ref : refs) {
            if (ref.getOrDefault("type_post", "").equals(type)) { modele = ref; break; }
        }
        m.put("type_post", type);
        m.put("ratio_image", !applicable("ratio_image", type) ? ""
                : modele != null ? modele.getOrDefault("ratio_image", "") : base.getOrDefault("ratio_image", ""));
        m.put("nb_photos_album", !applicable("nb_photos_album", type) ? ""
                : (modele != null && !modele.getOrDefault("nb_photos_album", "").isBlank()
                ? modele.get("nb_photos_album") : "2"));
        m.put("duree_video", !applicable("duree_video", type) ? ""
                : (modele != null && !modele.getOrDefault("duree_video", "").isBlank()
                ? modele.get("duree_video") : "30"));
        m.put("longueur_titre_video", !applicable("longueur_titre_video", type) ? ""
                : (modele != null && !modele.getOrDefault("longueur_titre_video", "").isBlank()
                ? modele.get("longueur_titre_video") : "0"));
        return m;
    }

    private boolean estVrai(String s) {
        String v = s == null ? "" : s.trim().toLowerCase();
        return v.equals("true") || v.equals("oui") || v.equals("1") || v.equals("yes");
    }

    private double nombre(String s) {
        try { return s == null || s.isBlank() ? 0 : Double.parseDouble(s.trim().replace(',', '.')); }
        catch (NumberFormatException e) { return 0; }
    }

    private boolean memeValeur(String feature, String a, String b) {
        if (BOOLEENS.contains(feature)) return estVrai(a) == estVrai(b);
        if (NUMERIQUES.contains(feature)) return Math.round(nombre(a)) == Math.round(nombre(b));
        return a != null && a.trim().equalsIgnoreCase(b != null ? b.trim() : "");
    }

    private String texte(String feature, String actuel, String propose) {
        long a = Math.round(nombre(actuel));
        long n = Math.round(nombre(propose));
        return switch (feature) {
            case "longueur_message" -> (n > a ? "Allonger" : "Raccourcir") + " la légende à environ " + n
                    + " caractères (actuellement " + a + ")";
            case "nb_hashtags" -> a == 0 ? "Ajouter " + n + " hashtag(s)"
                    : "Utiliser " + n + " hashtag(s) (actuellement " + a + ")";
            case "nb_tags" -> a == 0 ? "Taguer " + n + " personne(s) ou page(s)"
                    : "Taguer " + n + " personne(s) ou page(s) (actuellement " + a + ")";
            case "contient_lien_externe" -> estVrai(propose) ? "Ajouter un lien externe" : "Retirer le lien externe";
            case "contient_emoji" -> estVrai(propose) ? "Ajouter un emoji" : "Retirer les emojis";
            case "contient_question" -> estVrai(propose) ? "Poser une question dans la légende" : "Éviter la question dans la légende";
            case "ratio_image" -> "Utiliser une image au ratio " + propose
                    + " (actuellement " + (actuel == null || actuel.isBlank() ? "non précisé" : actuel) + ")";
            case "nb_photos_album" -> "Mettre environ " + n + " photos dans l'album (actuellement " + a + ")";
            case "duree_video" -> "Viser une vidéo d'environ " + n + " secondes (actuellement " + a + ")";
            case "longueur_titre_video" -> "Viser un titre de vidéo d'environ " + n + " caractères (actuellement " + a + ")";
            case "jour_semaine" -> "Publier plutôt le " + propose + " (actuellement " + actuel + ")";
            case "heure_publication" -> "Publier vers " + n + "h (actuellement " + a + "h)";
            case "jours_depuis_dernier_post" -> "Attendre environ " + n + " jour(s) depuis le dernier post (actuellement " + a + ")";
            case "nb_posts_7j" -> "Viser environ " + n + " post(s) sur les 7 derniers jours (actuellement " + a + ")";
            case "nb_posts_30j" -> "Viser environ " + n + " post(s) sur les 30 derniers jours (actuellement " + a + ")";
            default -> feature + " : " + propose + " (actuellement " + actuel + ")";
        };
    }

    /* ================================================================== INNER CLASSES ======= */

    private static class CandidatScore {
        final Map<String, String> raw;
        final double score;
        final Map<String, Double> confidences;

        CandidatScore(Map<String, String> raw, double score, Map<String, Double> confidences) {
            this.raw = raw;
            this.score = score;
            this.confidences = confidences;
        }
    }

    private static class Suggestion {
        final String feature;
        final String valeur;
        final String texte;
        final double scoreApres;
        final double gain;

        Suggestion(String feature, String valeur, String texte, double scoreApres, double gain) {
            this.feature = feature;
            this.valeur = valeur;
            this.texte = texte;
            this.scoreApres = scoreApres;
            this.gain = gain;
        }
    }
}
