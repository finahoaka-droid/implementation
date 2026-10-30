package org.example.facebookregression.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Données d'une publication candidate à simuler.
 */
@Schema(description = "Caractéristiques d'une publication Facebook pour simuler son impact")
public class SimulationRequest {

    @Schema(description = "Type de publication : photo, album, video, texte, lien, evenement", example = "photo")
    private String typePost = "photo";

    @Schema(description = "Longueur du texte de la publication (nombre de caractères)", example = "150")
    private Integer longueurMessage = 0;

    @Schema(description = "Nombre de hashtags inclus dans le message", example = "2")
    private Integer nbHashtags = 0;

    @Schema(description = "Nombre de personnes ou pages taguées", example = "0")
    private Integer nbTags = 0;

    @Schema(description = "Indique si la publication contient un lien externe", example = "false")
    private Boolean contientLienExterne = false;

    @Schema(description = "Indique si le message contient au moins un emoji", example = "true")
    private Boolean contientEmoji = false;

    @Schema(description = "Indique si le message contient une question (?)", example = "false")
    private Boolean contientQuestion = false;

    @Schema(description = "Ratio de l'image (largeur:hauteur, ex: 1:1, 16:9, 4:3) - non requis pour texte/evenement", example = "1:1")
    private String ratioImage = "1:1";

    @Schema(description = "Nombre de photos dans l'album (si type = album)", example = "2")
    private Integer nbPhotosAlbum;

    @Schema(description = "Durée de la vidéo en secondes (si type = video)", example = "30.0")
    private Double dureeVideo;

    @Schema(description = "Longueur du titre de la vidéo en caractères (si type = video)", example = "0")
    private Integer longueurTitreVideo;

    @Schema(description = "Jour de publication : lundi, mardi, mercredi, jeudi, vendredi, samedi, dimanche", example = "lundi")
    private String jourSemaine = "lundi";

    @Schema(description = "Heure de publication (0 à 23)", example = "14")
    private Integer heurePublication = 12;

    @Schema(description = "Nombre de jours écoulés depuis le post précédent", example = "1")
    private Integer joursDepuisDernierPost = 1;

    @Schema(description = "Nombre de publications publiées sur les 7 derniers jours", example = "3")
    private Integer nbPosts7j = 0;

    @Schema(description = "Nombre de publications publiées sur les 30 derniers jours", example = "10")
    private Integer nbPosts30j = 0;

    public SimulationRequest() {
    }

    /**
     * Convertit cette requête en Map brute compatible avec {@code FacebookEncoder.encodeRow}.
     */
    public Map<String, String> toRawMap() {
        Map<String, String> p = new LinkedHashMap<>();
        String type = typePost != null && !typePost.isBlank() ? typePost.trim().toLowerCase() : "photo";
        p.put("type_post", type);
        p.put("longueur_message", String.valueOf(longueurMessage != null ? longueurMessage : 0));
        p.put("nb_hashtags", String.valueOf(nbHashtags != null ? nbHashtags : 0));
        p.put("nb_tags", String.valueOf(nbTags != null ? nbTags : 0));
        p.put("contient_lien_externe", Boolean.TRUE.equals(contientLienExterne) ? "true" : "false");
        p.put("contient_emoji", Boolean.TRUE.equals(contientEmoji) ? "true" : "false");
        p.put("contient_question", Boolean.TRUE.equals(contientQuestion) ? "true" : "false");

        boolean sansImage = type.equals("texte") || type.equals("evenement");
        p.put("ratio_image", sansImage ? "" : (ratioImage != null ? ratioImage : ""));

        p.put("nb_photos_album", type.equals("album") ? String.valueOf(nbPhotosAlbum != null ? nbPhotosAlbum : 2) : "");
        p.put("duree_video", type.equals("video") ? String.valueOf(dureeVideo != null ? dureeVideo : 30) : "");
        p.put("longueur_titre_video", type.equals("video") ? String.valueOf(longueurTitreVideo != null ? longueurTitreVideo : 0) : "");

        p.put("jour_semaine", jourSemaine != null && !jourSemaine.isBlank() ? jourSemaine.trim().toLowerCase() : "lundi");
        p.put("heure_publication", String.valueOf(heurePublication != null ? heurePublication : 12));
        p.put("jours_depuis_dernier_post", String.valueOf(joursDepuisDernierPost != null ? joursDepuisDernierPost : 1));
        p.put("nb_posts_7j", String.valueOf(nbPosts7j != null ? nbPosts7j : 0));
        p.put("nb_posts_30j", String.valueOf(nbPosts30j != null ? nbPosts30j : 0));
        return p;
    }

    // Getters et Setters

    public String getTypePost() {
        return typePost;
    }

    public void setTypePost(String typePost) {
        this.typePost = typePost;
    }

    public Integer getLongueurMessage() {
        return longueurMessage;
    }

    public void setLongueurMessage(Integer longueurMessage) {
        this.longueurMessage = longueurMessage;
    }

    public Integer getNbHashtags() {
        return nbHashtags;
    }

    public void setNbHashtags(Integer nbHashtags) {
        this.nbHashtags = nbHashtags;
    }

    public Integer getNbTags() {
        return nbTags;
    }

    public void setNbTags(Integer nbTags) {
        this.nbTags = nbTags;
    }

    public Boolean getContientLienExterne() {
        return contientLienExterne;
    }

    public void setContientLienExterne(Boolean contientLienExterne) {
        this.contientLienExterne = contientLienExterne;
    }

    public Boolean getContientEmoji() {
        return contientEmoji;
    }

    public void setContientEmoji(Boolean contientEmoji) {
        this.contientEmoji = contientEmoji;
    }

    public Boolean getContientQuestion() {
        return contientQuestion;
    }

    public void setContientQuestion(Boolean contientQuestion) {
        this.contientQuestion = contientQuestion;
    }

    public String getRatioImage() {
        return ratioImage;
    }

    public void setRatioImage(String ratioImage) {
        this.ratioImage = ratioImage;
    }

    public Integer getNbPhotosAlbum() {
        return nbPhotosAlbum;
    }

    public void setNbPhotosAlbum(Integer nbPhotosAlbum) {
        this.nbPhotosAlbum = nbPhotosAlbum;
    }

    public Double getDureeVideo() {
        return dureeVideo;
    }

    public void setDureeVideo(Double dureeVideo) {
        this.dureeVideo = dureeVideo;
    }

    public Integer getLongueurTitreVideo() {
        return longueurTitreVideo;
    }

    public void setLongueurTitreVideo(Integer longueurTitreVideo) {
        this.longueurTitreVideo = longueurTitreVideo;
    }

    public String getJourSemaine() {
        return jourSemaine;
    }

    public void setJourSemaine(String jourSemaine) {
        this.jourSemaine = jourSemaine;
    }

    public Integer getHeurePublication() {
        return heurePublication;
    }

    public void setHeurePublication(Integer heurePublication) {
        this.heurePublication = heurePublication;
    }

    public Integer getJoursDepuisDernierPost() {
        return joursDepuisDernierPost;
    }

    public void setJoursDepuisDernierPost(Integer joursDepuisDernierPost) {
        this.joursDepuisDernierPost = joursDepuisDernierPost;
    }

    public Integer getNbPosts7j() {
        return nbPosts7j;
    }

    public void setNbPosts7j(Integer nbPosts7j) {
        this.nbPosts7j = nbPosts7j;
    }

    public Integer getNbPosts30j() {
        return nbPosts30j;
    }

    public void setNbPosts30j(Integer nbPosts30j) {
        this.nbPosts30j = nbPosts30j;
    }
}
