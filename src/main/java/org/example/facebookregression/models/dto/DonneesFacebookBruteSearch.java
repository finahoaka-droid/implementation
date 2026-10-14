package org.example.facebookregression.models.dto;

import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class DonneesFacebookBruteSearch {

	private Integer id;

	private Integer longueurMessage;
	private Integer longueurMessageMin;
	private Integer longueurMessageMax;

	private Integer nbHashtags;
	private Integer nbHashtagsMin;
	private Integer nbHashtagsMax;

	private Integer nbTags;
	private Integer nbTagsMin;
	private Integer nbTagsMax;

	private Boolean contientLienExterne;
	private Boolean contientEmoji;
	private Boolean contientQuestion;
	private String typePost;
	private String ratioImage;
	private Integer nbPhotosAlbum;
	private Integer nbPhotosAlbumMin;
	private Integer nbPhotosAlbumMax;

	private Double dureeVideo;
	private Double dureeVideoMin;
	private Double dureeVideoMax;

	private Integer longueurTitreVideo;
	private Integer longueurTitreVideoMin;
	private Integer longueurTitreVideoMax;

	private String jourSemaine;
	private Integer heurePublication;
	private Integer heurePublicationMin;
	private Integer heurePublicationMax;

	private Integer joursDepuisDernierPost;
	private Integer joursDepuisDernierPostMin;
	private Integer joursDepuisDernierPostMax;

	private Integer nbPosts7j;
	private Integer nbPosts7jMin;
	private Integer nbPosts7jMax;

	private Integer nbPosts30j;
	private Integer nbPosts30jMin;
	private Integer nbPosts30jMax;

	private Double vues;
	private Double vuesMin;
	private Double vuesMax;

	private Double reactions;
	private Double reactionsMin;
	private Double reactionsMax;

	private Integer commentaires;
	private Integer commentairesMin;
	private Integer commentairesMax;

	private java.time.LocalDateTime dateInsertion;
	private java.time.LocalDateTime dateInsertionMin;
	private java.time.LocalDateTime dateInsertionMax;

	
}
