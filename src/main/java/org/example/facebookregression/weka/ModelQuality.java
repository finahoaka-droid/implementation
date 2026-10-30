package org.example.facebookregression.weka;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Évaluation honnête et choix du modèle.
 *
 * <ul>
 *   <li>validation croisée stratifiée répétée (et non un seul découpage 80/20 de ~20 posts) ;</li>
 *   <li>comparaison systématique avec le modèle « classe majoritaire » ;</li>
 *   <li>seuils des classes (33 % / 66 %) recalculés dans chaque pli sur l'apprentissage seul,
 *       pour qu'aucune information du test ne serve à définir les classes ;</li>
 *   <li>test temporel (apprendre sur le passé, tester sur les posts les plus récents)
 *       pour détecter une dérive ;</li>
 *   <li>verdict explicite : FIABLE, NON_FIABLE ou INSUFFISANT.</li>
 * </ul>
 */
public class ModelQuality {

    static final int FOLDS = 5;
    static final int REPEATS = 10;
    static final long SEED = 42;
    /** En dessous, aucune estimation n'est assez stable pour parler de fiabilité. */
    static final int MIN_INSTANCES = 60;
    /** Gain minimal, après marge d'erreur, sur la classe majoritaire. */
    static final double MIN_LIFT = 0.03;

    static final String COL_NUM = "__num";
    public static final String FIABLE = "FIABLE";
    public static final String NON_FIABLE = "NON_FIABLE";
    public static final String INSUFFISANT = "INSUFFISANT";

    /** Modèle candidat : arbre contraint (probabilités lissées) ou forêt aléatoire. */
    static class Candidat {
        final String nom;
        final Supplier<Classifier> usine;

        Candidat(String nom, Supplier<Classifier> usine) {
            this.nom = nom;
            this.usine = usine;
        }
    }

    static final List<Candidat> CANDIDATS = List.of(
            new Candidat("J48 contraint", () -> {
                J48 arbre = new J48();
                arbre.setMinNumObj(10);
                arbre.setConfidenceFactor(0.1f);
                arbre.setUseLaplace(true);
                return arbre;
            }),
            new Candidat("Random Forest", () -> {
                RandomForest foret = new RandomForest();
                foret.setNumIterations(100);
                foret.setSeed(1);
                foret.setNumExecutionSlots(1);
                return foret;
            }));

    /** Résultat d'évaluation conservé avec le modèle. */
    public static class Rapport implements Serializable {
        private static final long serialVersionUID = 1L;
        public String cible;
        public String modele;
        public String verdict;
        public int n;
        public double accuracy;          // validation croisée, 0..1
        public double baseline;          // classe majoritaire
        public double ecartType;         // entre répétitions
        public double erreurStandard;    // binomiale, sqrt(p(1-p)/n)
        public double brier;             // plus bas = mieux
        public double brierBaseline;
        public double temporel = Double.NaN;
        public double temporelBaseline = Double.NaN;

        public boolean fiable() {
            return FIABLE.equals(verdict);
        }

        public String resume() {
            if (INSUFFISANT.equals(verdict) && Double.isNaN(accuracy)) {
                return String.format("%s : %s (%d posts, minimum %d)", cible, verdict, n, MIN_INSTANCES);
            }
            StringBuilder s = new StringBuilder(String.format(
                    "%s : %s - %s, exactitude %.1f%% ± %.1f (hasard %.1f%%, %d posts), Brier %.3f (hasard %.3f)",
                    cible, verdict, modele, accuracy * 100, erreurStandard * 100,
                    baseline * 100, n, brier, brierBaseline));
            if (!Double.isNaN(temporel)) {
                s.append(String.format(", test temporel %.0f%% (hasard %.0f%%)",
                        temporel * 100, temporelBaseline * 100));
                if (temporel < temporelBaseline || temporelBaseline < 0.15) {
                    s.append(" [!] dérive possible (la répartition des classes a changé entre l'ancien et le récent)");
                }
            }
            return s.toString();
        }
    }

    /** Modèle, entête exacte d'apprentissage et rapport : stockés ensemble. */
    public static class Lot {
        public Classifier modele;
        public Instances entete;
        public Rapport rapport;
    }

    // ------------------------------------------------------------------ étiquettes

    /**
     * Fonction valeur -> classe, avec des seuils appris sur {@code appris} uniquement.
     * Réutilise les fonctions de {@link FacebookEncoder}, source de vérité des classes.
     */
    static Function<Double, String> etiqueteur(String cible, double[] appris) {
        if (cible.equals("commentaires_classe")) return FacebookEncoder::classifierCommentaires;
        if (appris.length == 0) throw new IllegalArgumentException("Aucune valeur pour apprendre les seuils de " + cible);

        List<Double> triees = new ArrayList<>();
        for (double v : appris) triees.add(v);
        triees.sort(Double::compareTo);

        if (cible.equals("reactions_classe")) {
            double s1 = FacebookEncoder.percentile(triees, 0.33);
            double s2 = FacebookEncoder.percentile(triees, 0.66);
            return v -> FacebookEncoder.classifierReactions(v, s1, s2);
        }
        if (cible.equals("vues_classe")) {
            List<Long> seuils = FacebookEncoder.seuilsVues(triees);
            return v -> FacebookEncoder.classifierVues(v, seuils);
        }
        throw new IllegalArgumentException("Cible inconnue : " + cible);
    }

    // ------------------------------------------------------------------ préparation

    /**
     * Jeu d'une cible : caractéristiques, classe (par son nom) et colonne numérique
     * {@value #COL_NUM} (valeur brute servant à recalculer les classes dans chaque pli).
     * L'ordre des lignes est conservé : le CSV est chronologique.
     */
    public static Instances preparer(Instances data, String cible) throws Exception {
        Instances d = new Instances(data);
        if (cible.equals("vues_classe")) {
            d = WekaFacebookClassification.retirerNonApplicable(d, cible);
        }
        Attribute source = d.attribute(cible.replace("_classe", ""));
        if (source == null) throw new IllegalStateException("Colonne numérique absente pour " + cible);
        d.insertAttributeAt(new Attribute(COL_NUM), d.numAttributes());
        int iNum = d.attribute(COL_NUM).index();
        for (int i = 0; i < d.numInstances(); i++) d.instance(i).setValue(iNum, d.instance(i).value(source));
        d = WekaFacebookClassification.retirerAutresCibles(d, cible);
        d.setClass(d.attribute(cible));
        return d;
    }

    public static Instances retirerNum(Instances d, String cible) throws Exception {
        Remove filtre = new Remove();
        filtre.setAttributeIndicesArray(new int[]{d.attribute(COL_NUM).index()});
        filtre.setInputFormat(d);
        Instances sortie = Filter.useFilter(d, filtre);
        sortie.setClass(sortie.attribute(cible));
        return sortie;
    }

    /** Recalcule la classe de l'apprentissage ET du test avec les seuils de l'apprentissage seul. */
    static Instances[] reetiqueter(Instances train, Instances test, String cible) throws Exception {
        int iNum = train.attribute(COL_NUM).index();
        double[] appris = new double[train.numInstances()];
        for (int i = 0; i < appris.length; i++) appris[i] = train.instance(i).value(iNum);
        Function<Double, String> etiquette = etiqueteur(cible, appris);

        Instances tout = new Instances(train);
        for (int i = 0; i < test.numInstances(); i++) tout.add(test.instance(i));

        String[] labels = new String[tout.numInstances()];
        LinkedHashSet<String> valeurs = new LinkedHashSet<>();
        for (int i = 0; i < labels.length; i++) {
            labels[i] = etiquette.apply(tout.instance(i).value(iNum));
            valeurs.add(labels[i]);
        }

        Remove filtre = new Remove();
        filtre.setAttributeIndicesArray(new int[]{tout.attribute(cible).index(), iNum});
        filtre.setInputFormat(tout);
        Instances traite = Filter.useFilter(tout, filtre);
        traite.insertAttributeAt(new Attribute(cible, new ArrayList<>(valeurs)), traite.numAttributes());
        Attribute classe = traite.attribute(cible);
        for (int i = 0; i < labels.length; i++) traite.instance(i).setValue(classe, labels[i]);
        traite.setClass(classe);

        int nTrain = train.numInstances();
        return new Instances[]{
                new Instances(traite, 0, nTrain),
                new Instances(traite, nTrain, traite.numInstances() - nTrain)};
    }

    // ------------------------------------------------------------------ évaluation

    private static class Mesure {
        int correct;
        int correctBase;
        double brier;
        double brierBase;
        int total;
    }

    private static void mesurer(Candidat candidat, Instances train, Instances test, Mesure m) throws Exception {
        int nc = train.numClasses();
        double[] effectifs = new double[nc];
        for (Instance inst : train) effectifs[(int) inst.classValue()]++;
        int majoritaire = 0;
        double[] priors = new double[nc];
        for (int k = 0; k < nc; k++) {
            if (effectifs[k] > effectifs[majoritaire]) majoritaire = k;
            priors[k] = effectifs[k] / train.numInstances();
        }
        boolean degenere = Arrays.stream(effectifs).filter(e -> e > 0).count() < 2;
        Classifier modele = null;
        if (!degenere) {
            modele = candidat.usine.get();
            modele.buildClassifier(train);
        }

        for (Instance inst : test) {
            int reel = (int) inst.classValue();
            double[] dist = degenere ? priors : modele.distributionForInstance(inst);
            int pred = 0;
            for (int k = 1; k < dist.length; k++) if (dist[k] > dist[pred]) pred = k;
            if (pred == reel) m.correct++;
            if (majoritaire == reel) m.correctBase++;
            for (int k = 0; k < nc; k++) {
                double y = k == reel ? 1 : 0;
                m.brier += (dist[k] - y) * (dist[k] - y);
                m.brierBase += (priors[k] - y) * (priors[k] - y);
            }
            m.total++;
        }
    }

    static Rapport validationCroisee(Instances prepare, String cible, Candidat candidat) throws Exception {
        double[] acc = new double[REPEATS];
        double sommeBase = 0, sommeBrier = 0, sommeBrierBase = 0;
        for (int r = 0; r < REPEATS; r++) {
            Instances d = new Instances(prepare);
            d.setClass(d.attribute(cible));
            d.randomize(new Random(SEED + r));
            d.stratify(FOLDS);
            Mesure m = new Mesure();
            for (int f = 0; f < FOLDS; f++) {
                Instances[] parts = reetiqueter(d.trainCV(FOLDS, f), d.testCV(FOLDS, f), cible);
                mesurer(candidat, parts[0], parts[1], m);
            }
            acc[r] = (double) m.correct / m.total;
            sommeBase += (double) m.correctBase / m.total;
            sommeBrier += m.brier / m.total;
            sommeBrierBase += m.brierBase / m.total;
        }
        Rapport rapport = new Rapport();
        rapport.cible = cible;
        rapport.modele = candidat.nom;
        rapport.n = prepare.numInstances();
        rapport.accuracy = Arrays.stream(acc).average().orElse(0);
        double variance = Arrays.stream(acc).map(a -> (a - rapport.accuracy) * (a - rapport.accuracy)).sum() / REPEATS;
        rapport.ecartType = Math.sqrt(variance);
        rapport.erreurStandard = Math.sqrt(rapport.accuracy * (1 - rapport.accuracy) / rapport.n);
        rapport.baseline = sommeBase / REPEATS;
        rapport.brier = sommeBrier / REPEATS;
        rapport.brierBaseline = sommeBrierBase / REPEATS;
        return rapport;
    }

    /**
     * Compare les candidats en validation croisée, garde le meilleur et rend le verdict.
     * Le choix entre deux candidats rend l'estimation très légèrement optimiste : la marge
     * d'erreur et le gain minimal du verdict absorbent ce biais.
     */
    public static Rapport evaluer(Instances prepare, String cible) throws Exception {
        if (prepare.numInstances() < 2 * FOLDS) {
            Rapport r = new Rapport();
            r.cible = cible;
            r.modele = "aucun";
            r.n = prepare.numInstances();
            r.accuracy = r.baseline = r.ecartType = r.erreurStandard = r.brier = r.brierBaseline = Double.NaN;
            r.verdict = INSUFFISANT;
            return r;
        }

        Rapport meilleur = null;
        Candidat choisi = null;
        for (Candidat c : CANDIDATS) {
            Rapport r = validationCroisee(prepare, cible, c);
            boolean mieux = meilleur == null || r.accuracy > meilleur.accuracy + 1e-9
                    || (Math.abs(r.accuracy - meilleur.accuracy) <= 1e-9 && r.brier < meilleur.brier);
            if (mieux) {
                meilleur = r;
                choisi = c;
            }
        }

        if (prepare.numInstances() >= MIN_INSTANCES) {
            int nTrain = (int) Math.round(prepare.numInstances() * 0.8);
            Instances d = new Instances(prepare);
            d.setClass(d.attribute(cible));
            Instances[] parts = reetiqueter(new Instances(d, 0, nTrain),
                    new Instances(d, nTrain, d.numInstances() - nTrain), cible);
            Mesure m = new Mesure();
            mesurer(choisi, parts[0], parts[1], m);
            meilleur.temporel = (double) m.correct / m.total;
            meilleur.temporelBaseline = (double) m.correctBase / m.total;
        }

        if (meilleur.n < MIN_INSTANCES) {
            meilleur.verdict = INSUFFISANT;
        } else if (meilleur.accuracy - meilleur.erreurStandard > meilleur.baseline + MIN_LIFT) {
            meilleur.verdict = FIABLE;
        } else {
            meilleur.verdict = NON_FIABLE;
        }
        return meilleur;
    }

    /** Entraîne le modèle final sur toutes les données (la validation croisée est l'estimation). */
    public static Classifier entrainer(Instances prepare, String cible, String nomModele) throws Exception {
        Candidat candidat = CANDIDATS.stream().filter(c -> c.nom.equals(nomModele)).findFirst()
                .orElse(CANDIDATS.get(0));
        Instances d = retirerNum(prepare, cible);
        Classifier modele = candidat.usine.get();
        modele.buildClassifier(d);
        return modele;
    }

    // ------------------------------------------------------------------ stockage

    public static byte[] serialiser(Classifier modele, Instances entete, Rapport rapport) throws IOException {
        ByteArrayOutputStream octets = new ByteArrayOutputStream();
        try (ObjectOutputStream sortie = new ObjectOutputStream(octets)) {
            sortie.writeObject(modele);
            sortie.writeObject(new Instances(entete, 0));
            sortie.writeObject(rapport);
        }
        return octets.toByteArray();
    }

    /** Relit un lot ; un ancien enregistrement (modèle seul) rend entête et rapport nuls. */
    public static Lot deserialiser(byte[] donnees) throws IOException, ClassNotFoundException {
        Lot lot = new Lot();
        try (ObjectInputStream entree = new ObjectInputStream(new ByteArrayInputStream(donnees))) {
            lot.modele = (Classifier) entree.readObject();
            try {
                lot.entete = (Instances) entree.readObject();
                lot.rapport = (Rapport) entree.readObject();
            } catch (EOFException ancienFormat) {
                lot.entete = null;
                lot.rapport = null;
            }
        }
        return lot;
    }
}
