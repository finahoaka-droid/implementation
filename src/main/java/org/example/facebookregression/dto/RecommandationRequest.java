package org.example.facebookregression.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Requête pour obtenir des recommandations de profils de publication
 * correspondant aux classes cibles souhaitées.
 */
@Schema(description = "Classes cibles voulues pour la recommandation de profils de publication")
public class RecommandationRequest {

    @Schema(
        description = "Classe cible pour les réactions. Exemples : '0-6', '7-18', '19+'",
        example = "7-18"
    )
    private String reactionsClasse;

    @Schema(
        description = "Classe cible pour les commentaires. Valeurs : '0' ou '1+'",
        example = "1+"
    )
    private String commentairesClasse;

    @Schema(
        description = "Classe cible pour les vues (uniquement pour vidéo et album). Laisser vide pour ignorer.",
        example = null
    )
    private String vuesClasse;

    public RecommandationRequest() {
    }

    public String getReactionsClasse() {
        return reactionsClasse;
    }

    public void setReactionsClasse(String reactionsClasse) {
        this.reactionsClasse = reactionsClasse;
    }

    public String getCommentairesClasse() {
        return commentairesClasse;
    }

    public void setCommentairesClasse(String commentairesClasse) {
        this.commentairesClasse = commentairesClasse;
    }

    public String getVuesClasse() {
        return vuesClasse;
    }

    public void setVuesClasse(String vuesClasse) {
        this.vuesClasse = vuesClasse;
    }
}
