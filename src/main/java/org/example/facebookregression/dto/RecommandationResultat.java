package org.example.facebookregression.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Résultat de la recommandation : profils observés qui maximisent les classes cibles.
 */
@Schema(description = "Résultat de la recommandation de profils de publication Facebook")
public class RecommandationResultat {

    @Schema(description = "Message récapitulatif")
    private String message;

    @Schema(description = "Message d'erreur éventuel (null si succès)")
    private String erreur;

    @Schema(description = "Classes cibles utilisées pour la recherche")
    private Map<String, String> cibles;

    @Schema(description = "Profils observés les mieux classés (maximum 3)")
    private List<ProfilRecommande> profils;

    @Schema(description = "Note explicative sur l'interprétation des résultats")
    private String note;

    public RecommandationResultat() {
    }

    /**
     * Un profil de publication recommandé avec son score et ses confidences par cible.
     */
    @Schema(description = "Profil de publication observé recommandé")
    public static class ProfilRecommande {

        @Schema(description = "Rang du profil (1 = meilleur)")
        private int rang;

        @Schema(description = "Score conjoint (moyenne géométrique des probabilités, en %)", example = "72.5")
        private double scorePourcent;

        @Schema(description = "Caractéristiques brutes du profil observé")
        private Map<String, String> profil;

        @Schema(description = "Probabilité de chaque cible (en %) pour ce profil")
        private Map<String, Double> confidencesPourcent;

        public ProfilRecommande() {
        }

        public int getRang() {
            return rang;
        }

        public void setRang(int rang) {
            this.rang = rang;
        }

        public double getScorePourcent() {
            return scorePourcent;
        }

        public void setScorePourcent(double scorePourcent) {
            this.scorePourcent = scorePourcent;
        }

        public Map<String, String> getProfil() {
            return profil;
        }

        public void setProfil(Map<String, String> profil) {
            this.profil = profil;
        }

        public Map<String, Double> getConfidencesPourcent() {
            return confidencesPourcent;
        }

        public void setConfidencesPourcent(Map<String, Double> confidencesPourcent) {
            this.confidencesPourcent = confidencesPourcent;
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

    public Map<String, String> getCibles() {
        return cibles;
    }

    public void setCibles(Map<String, String> cibles) {
        this.cibles = cibles;
    }

    public List<ProfilRecommande> getProfils() {
        return profils;
    }

    public void setProfils(List<ProfilRecommande> profils) {
        this.profils = profils;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
