package org.example.facebookregression.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Résultat de la suggestion : améliorations proposées pour rapprocher
 * une publication simulée des classes cibles voulues.
 */
@Schema(description = "Résultat de la suggestion d'améliorations pour une publication Facebook")
public class SuggestionResultat {

    @Schema(description = "Message récapitulatif")
    private String message;

    @Schema(description = "Message d'erreur éventuel (null si succès)")
    private String erreur;

    @Schema(description = "Classes cibles utilisées")
    private Map<String, String> cibles;

    @Schema(description = "Score conjoint actuel du brouillon (en %)")
    private double scoreActuelPourcent;

    @Schema(description = "Probabilités actuelles par cible (en %) pour le brouillon soumis")
    private Map<String, Double> probasActuellesPourcent;

    @Schema(description = "Suggestions qui améliorent le score")
    private List<SuggestionItem> suggestionsUtiles;

    @Schema(description = "Autres différences observées sans effet mesuré par les modèles")
    private List<String> autresDifferences;

    @Schema(description = "Score après application de toutes les suggestions utiles (en %)")
    private Double scoreAmeliorePourcent;

    @Schema(description = "Probabilités après application de toutes les suggestions utiles (en %)")
    private Map<String, Double> probasAmelioreesPourcent;

    @Schema(description = "Score de la meilleure publication de référence (en %)")
    private double scoreReferencePourcent;

    @Schema(description = "Note explicative sur la méthode de suggestion")
    private String note;

    public SuggestionResultat() {
    }

    /**
     * Une suggestion individuelle d'amélioration.
     */
    @Schema(description = "Suggestion d'amélioration d'une caractéristique de la publication")
    public static class SuggestionItem {

        @Schema(description = "Rang de la suggestion (1 = gain le plus élevé)")
        private int rang;

        @Schema(description = "Caractéristique concernée", example = "nb_hashtags")
        private String caracteristique;

        @Schema(description = "Valeur proposée", example = "3")
        private String valeurProposee;

        @Schema(description = "Texte lisible décrivant la suggestion", example = "Ajouter 3 hashtag(s)")
        private String texte;

        @Schema(description = "Score avant la modification (en %)")
        private double scoreAvantPourcent;

        @Schema(description = "Score après la modification (en %)")
        private double scoreApresPourcent;

        @Schema(description = "Gain de score (en points de pourcentage)")
        private double gainPoints;

        public SuggestionItem() {
        }

        public int getRang() {
            return rang;
        }

        public void setRang(int rang) {
            this.rang = rang;
        }

        public String getCaracteristique() {
            return caracteristique;
        }

        public void setCaracteristique(String caracteristique) {
            this.caracteristique = caracteristique;
        }

        public String getValeurProposee() {
            return valeurProposee;
        }

        public void setValeurProposee(String valeurProposee) {
            this.valeurProposee = valeurProposee;
        }

        public String getTexte() {
            return texte;
        }

        public void setTexte(String texte) {
            this.texte = texte;
        }

        public double getScoreAvantPourcent() {
            return scoreAvantPourcent;
        }

        public void setScoreAvantPourcent(double scoreAvantPourcent) {
            this.scoreAvantPourcent = scoreAvantPourcent;
        }

        public double getScoreApresPourcent() {
            return scoreApresPourcent;
        }

        public void setScoreApresPourcent(double scoreApresPourcent) {
            this.scoreApresPourcent = scoreApresPourcent;
        }

        public double getGainPoints() {
            return gainPoints;
        }

        public void setGainPoints(double gainPoints) {
            this.gainPoints = gainPoints;
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

    public double getScoreActuelPourcent() {
        return scoreActuelPourcent;
    }

    public void setScoreActuelPourcent(double scoreActuelPourcent) {
        this.scoreActuelPourcent = scoreActuelPourcent;
    }

    public Map<String, Double> getProbasActuellesPourcent() {
        return probasActuellesPourcent;
    }

    public void setProbasActuellesPourcent(Map<String, Double> probasActuellesPourcent) {
        this.probasActuellesPourcent = probasActuellesPourcent;
    }

    public List<SuggestionItem> getSuggestionsUtiles() {
        return suggestionsUtiles;
    }

    public void setSuggestionsUtiles(List<SuggestionItem> suggestionsUtiles) {
        this.suggestionsUtiles = suggestionsUtiles;
    }

    public List<String> getAutresDifferences() {
        return autresDifferences;
    }

    public void setAutresDifferences(List<String> autresDifferences) {
        this.autresDifferences = autresDifferences;
    }

    public Double getScoreAmeliorePourcent() {
        return scoreAmeliorePourcent;
    }

    public void setScoreAmeliorePourcent(Double scoreAmeliorePourcent) {
        this.scoreAmeliorePourcent = scoreAmeliorePourcent;
    }

    public Map<String, Double> getProbasAmelioreesPourcent() {
        return probasAmelioreesPourcent;
    }

    public void setProbasAmelioreesPourcent(Map<String, Double> probasAmelioreesPourcent) {
        this.probasAmelioreesPourcent = probasAmelioreesPourcent;
    }

    public double getScoreReferencePourcent() {
        return scoreReferencePourcent;
    }

    public void setScoreReferencePourcent(double scoreReferencePourcent) {
        this.scoreReferencePourcent = scoreReferencePourcent;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
