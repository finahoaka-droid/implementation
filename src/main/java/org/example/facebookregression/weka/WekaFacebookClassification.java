package org.example.facebookregression.weka;

import weka.core.Instances;
import weka.core.Attribute;
import weka.core.converters.CSVLoader;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

public class WekaFacebookClassification {

    /**
     * Évalue (validation croisée répétée contre la classe majoritaire), entraîne sur toutes les
     * données le meilleur candidat et le conserve en base avec son entête et son rapport.
     */
    static final String DB_URL = configOrDefault(
            "PREDICTION_DB_URL", "jdbc:postgresql://localhost:5433/vinany");
    static final String DB_USER = configOrDefault("PREDICTION_DB_USER", "postgres");
    static final String DB_PASSWORD = PredictionConfig.get("PREDICTION_DB_PASSWORD");

    static final String FICHIER = "output/features_facebook_encoded.csv";
    static final String[] CIBLES = {"reactions_classe", "commentaires_classe", "vues_classe"};

    static final boolean AFFICHER_REGLES = true;

    static final List<String> COLONNES_RESULTATS = List.of(
            "vues", "reactions", "commentaires",
            "vues_classe", "reactions_classe", "commentaires_classe"
    );

    static String configOrDefault(String name, String defaultValue) {
        String value = PredictionConfig.get(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static void main(String[] args) throws Exception {
        Logger.getLogger("").setLevel(Level.SEVERE);

        CSVLoader loader = new CSVLoader();
        loader.setNominalAttributes(indicesColonnesClasse(FICHIER));
        loader.setSource(new File(FICHIER));
        Instances data = loader.getDataSet();

        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

        for (String cible : CIBLES) {
            Instances prepare = ModelQuality.preparer(data, cible);
            ModelQuality.Rapport rapport = ModelQuality.evaluer(prepare, cible);
            Classifier modele = rapport.modele.equals("aucun") ? null
                    : ModelQuality.entrainer(prepare, cible, rapport.modele);
            if (modele == null) {
                System.out.println("\n" + rapport.resume() + " : modèle non entraîné.");
                continue;
            }
            entrainerEtStocker("facebook_" + cible, cible, modele,
                    ModelQuality.retirerNum(prepare, cible), rapport, conn);
        }

        conn.close();
        System.out.println("\nModèles stockés.");
    }

    static String indicesColonnesClasse(String fichier) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fichier, StandardCharsets.UTF_8))) {
            String[] colonnes = reader.readLine().split(",");
            StringBuilder indices = new StringBuilder();
            for (int i = 0; i < colonnes.length; i++) {
                if (colonnes[i].trim().endsWith("_classe")) {
                    if (indices.length() > 0) indices.append(",");
                    indices.append(i + 1);
                }
            }
            return indices.toString();
        }
    }

    static Instances retirerNonApplicable(Instances data, String cible) throws Exception {
        RemoveWithValues filtre = new RemoveWithValues();
        int index = data.attribute(cible).index() + 1;
        int valeur = data.attribute(cible).indexOfValue("non_applicable") + 1;
        filtre.setOptions(weka.core.Utils.splitOptions("-C " + index + " -L " + valeur));
        filtre.setInputFormat(data);
        return Filter.useFilter(data, filtre);
    }

    static Instances retirerAutresCibles(Instances data, String cibleAGarder) throws Exception {
        StringBuilder indices = new StringBuilder();
        for (String col : COLONNES_RESULTATS) {
            if (col.equals(cibleAGarder)) continue;
            Attribute attr = data.attribute(col);
            if (attr != null) {
                if (indices.length() > 0) indices.append(",");
                indices.append(attr.index() + 1);
            }
        }
        if (indices.length() == 0) return data;

        Remove filtre = new Remove();
        filtre.setOptions(weka.core.Utils.splitOptions("-R " + indices));
        filtre.setInputFormat(data);
        return Filter.useFilter(data, filtre);
    }

    static String extraireRegles(String arbre) {
        int debut = arbre.indexOf("------------------");
        int fin = arbre.indexOf("Number of Leaves");
        if (debut < 0 || fin < 0) return arbre;
        return arbre.substring(arbre.indexOf('\n', debut) + 1, fin).trim();
    }

    static void entrainerEtStocker(String nom, String cible, Classifier modele, Instances entete,
                                    ModelQuality.Rapport rapport, Connection conn) throws Exception {
        System.out.println("\n" + rapport.resume());
        if (!rapport.fiable()) {
            System.out.println("  [!] Ce modèle est stocké mais le programme refusera de s'appuyer dessus "
                    + "pour recommander (voir FacebookPredictor).");
        }
        if (AFFICHER_REGLES && modele instanceof J48) {
            System.out.println(extraireRegles(modele.toString()));
        }

        String sql = "INSERT INTO modeles_classification_facebook " +
                "(nom_modele, variable_cible, accuracy, modele_binaire) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nom + "_" + rapport.verdict.toLowerCase());
            stmt.setString(2, cible);
            stmt.setDouble(3, Double.isNaN(rapport.accuracy) ? 0 : rapport.accuracy * 100);
            stmt.setBytes(4, ModelQuality.serialiser(modele, entete, rapport));
            stmt.executeUpdate();
        }
    }
}
