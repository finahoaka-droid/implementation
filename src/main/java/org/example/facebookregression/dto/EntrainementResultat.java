package org.example.facebookregression.dto;

import java.util.List;

/**
 * Résultat renvoyé au frontend après un lancement d'entraînement Weka.
 */
public class EntrainementResultat {

    /** Résumé textuel global (ex. "3 modèles traités"). */
    private String message;

    /** Erreur globale si le pipeline n'a pas pu démarrer (ex. CSV absent). */
    private String erreur;

    /** Résultat par cible (reactions_classe, commentaires_classe, vues_classe). */
    private List<CibleResultat> cibles;

    public EntrainementResultat() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErreur() { return erreur; }
    public void setErreur(String erreur) { this.erreur = erreur; }

    public List<CibleResultat> getCibles() { return cibles; }
    public void setCibles(List<CibleResultat> cibles) { this.cibles = cibles; }

    // ------------------------------------------------------------------

    public static class CibleResultat {

        /** Nom de la variable cible (ex. "reactions_classe"). */
        private String cible;

        /** Verdict d'évaluation : FIABLE | NON_FIABLE | INSUFFISANT | ERREUR. */
        private String verdict;

        /** Nom du meilleur candidat sélectionné : "J48 contraint" | "Random Forest" | "aucun". */
        private String modele;

        /** Exactitude en validation croisée répétée, 0–100 (null si non calculée). */
        private Double accuracy;

        /** Exactitude du modèle classe majoritaire, 0–100 (null si non calculée). */
        private Double baseline;

        /** Nombre d'instances (publications) utilisées pour cette cible. */
        private Integer nbInstances;

        /** Texte complet du rapport ModelQuality.Rapport.resume(). */
        private String resume;

        /** Vrai si le modèle a été enregistré dans la table modeles_classification_facebook. */
        private boolean stocke;

        /** Valeur de nom_modele telle qu'insérée en base, null si non stocké. */
        private String nomModeleDb;

        public CibleResultat() {}

        public String getCible() { return cible; }
        public void setCible(String cible) { this.cible = cible; }

        public String getVerdict() { return verdict; }
        public void setVerdict(String verdict) { this.verdict = verdict; }

        public String getModele() { return modele; }
        public void setModele(String modele) { this.modele = modele; }

        public Double getAccuracy() { return accuracy; }
        public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }

        public Double getBaseline() { return baseline; }
        public void setBaseline(Double baseline) { this.baseline = baseline; }

        public Integer getNbInstances() { return nbInstances; }
        public void setNbInstances(Integer nbInstances) { this.nbInstances = nbInstances; }

        public String getResume() { return resume; }
        public void setResume(String resume) { this.resume = resume; }

        public boolean isStocke() { return stocke; }
        public void setStocke(boolean stocke) { this.stocke = stocke; }

        public String getNomModeleDb() { return nomModeleDb; }
        public void setNomModeleDb(String nomModeleDb) { this.nomModeleDb = nomModeleDb; }
    }
}
