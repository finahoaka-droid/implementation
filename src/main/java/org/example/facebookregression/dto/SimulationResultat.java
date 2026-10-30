package org.example.facebookregression.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Résultat de la simulation d'une publication :
 * prédiction des réactions, des commentaires et des vues.
 */
@Schema(description = "Résultat de la prédiction des réactions, commentaires et vues pour une publication simulée")
public class SimulationResultat {

    @Schema(description = "Message récapitulatif")
    private String message;

    @Schema(description = "Message d'erreur éventuel (null si succès)", example = "null")
    private String erreur;

    @Schema(description = "Prédiction pour les réactions")
    private PredictionDetail reactions;

    @Schema(description = "Prédiction pour les commentaires")
    private PredictionDetail commentaires;

    @Schema(description = "Prédiction pour les vues (applicable uniquement aux vidéos et albums)")
    private PredictionDetail vues;

    public SimulationResultat() {
    }

    /**
     * Détail d'une prédiction pour une variable cible.
     */
    @Schema(description = "Détail de prédiction pour une variable cible")
    public static class PredictionDetail {

        @Schema(description = "Nom de la variable cible", example = "reactions_classe")
        private String cible;

        @Schema(description = "Indique si la cible est applicable pour ce type de publication", example = "true")
        private boolean applicable = true;

        @Schema(description = "Classe prédite par le modèle", example = "20-50")
        private String classePredite;

        @Schema(description = "Probabilité de la classe prédite (en %)", example = "68.4")
        private double probabilite;

        @Schema(description = "Verdict de fiabilité du modèle (FIABLE, NON_FIABLE, INSUFFISANT)", example = "FIABLE")
        private String fiabilite;

        @Schema(description = "Mise en garde sur la fiabilité du résultat", example = "")
        private String avertissement;

        @Schema(description = "Distribution de probabilités sur toutes les classes possibles (en %)")
        private Map<String, Double> probabilitesParClasse;

        public PredictionDetail() {
        }

        public String getCible() {
            return cible;
        }

        public void setCible(String cible) {
            this.cible = cible;
        }

        public boolean isApplicable() {
            return applicable;
        }

        public void setApplicable(boolean applicable) {
            this.applicable = applicable;
        }

        public String getClassePredite() {
            return classePredite;
        }

        public void setClassePredite(String classePredite) {
            this.classePredite = classePredite;
        }

        public double getProbabilite() {
            return probabilite;
        }

        public void setProbabilite(double probabilite) {
            this.probabilite = probabilite;
        }

        public String getFiabilite() {
            return fiabilite;
        }

        public void setFiabilite(String fiabilite) {
            this.fiabilite = fiabilite;
        }

        public String getAvertissement() {
            return avertissement;
        }

        public void setAvertissement(String avertissement) {
            this.avertissement = avertissement;
        }

        public Map<String, Double> getProbabilitesParClasse() {
            return probabilitesParClasse;
        }

        public void setProbabilitesParClasse(Map<String, Double> probabilitesParClasse) {
            this.probabilitesParClasse = probabilitesParClasse;
        }
    }

    // Getters et Setters

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErreur() {
        return erreur;
    }

    public void setErreur(String erreur) {
        this.erreur = erreur;
    }

    public PredictionDetail getReactions() {
        return reactions;
    }

    public void setReactions(PredictionDetail reactions) {
        this.reactions = reactions;
    }

    public PredictionDetail getCommentaires() {
        return commentaires;
    }

    public void setCommentaires(PredictionDetail commentaires) {
        this.commentaires = commentaires;
    }

    public PredictionDetail getVues() {
        return vues;
    }

    public void setVues(PredictionDetail vues) {
        this.vues = vues;
    }
}
