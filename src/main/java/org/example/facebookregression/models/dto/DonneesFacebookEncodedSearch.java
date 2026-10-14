package org.example.facebookregression.models.dto;

import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class DonneesFacebookEncodedSearch {

	private Integer id;

	private Double longueurMessage;
	private Double longueurMessageMin;
	private Double longueurMessageMax;

	private Double nbHashtags;
	private Double nbHashtagsMin;
	private Double nbHashtagsMax;

	private Double nbTags;
	private Double nbTagsMin;
	private Double nbTagsMax;

	private Double contientLienExterne;
	private Double contientLienExterneMin;
	private Double contientLienExterneMax;

	private Double contientEmoji;
	private Double contientEmojiMin;
	private Double contientEmojiMax;

	private Double contientQuestion;
	private Double contientQuestionMin;
	private Double contientQuestionMax;

	private Double typePostPhoto;
	private Double typePostPhotoMin;
	private Double typePostPhotoMax;

	private Double typePostAlbum;
	private Double typePostAlbumMin;
	private Double typePostAlbumMax;

	private Double typePostVideo;
	private Double typePostVideoMin;
	private Double typePostVideoMax;

	private Double typePostLien;
	private Double typePostLienMin;
	private Double typePostLienMax;

	private Double typePostEvenement;
	private Double typePostEvenementMin;
	private Double typePostEvenementMax;

	private Double ratioImage;
	private Double ratioImageMin;
	private Double ratioImageMax;

	private Double nbPhotosAlbum;
	private Double nbPhotosAlbumMin;
	private Double nbPhotosAlbumMax;

	private Double dureeVideo;
	private Double dureeVideoMin;
	private Double dureeVideoMax;

	private Double longueurTitreVideo;
	private Double longueurTitreVideoMin;
	private Double longueurTitreVideoMax;

	private Double jourLundi;
	private Double jourLundiMin;
	private Double jourLundiMax;

	private Double jourMardi;
	private Double jourMardiMin;
	private Double jourMardiMax;

	private Double jourMercredi;
	private Double jourMercrediMin;
	private Double jourMercrediMax;

	private Double jourJeudi;
	private Double jourJeudiMin;
	private Double jourJeudiMax;

	private Double jourVendredi;
	private Double jourVendrediMin;
	private Double jourVendrediMax;

	private Double jourSamedi;
	private Double jourSamediMin;
	private Double jourSamediMax;

	private Double heurePublication;
	private Double heurePublicationMin;
	private Double heurePublicationMax;

	private Double joursDepuisDernierPost;
	private Double joursDepuisDernierPostMin;
	private Double joursDepuisDernierPostMax;

	private Double nbPosts7j;
	private Double nbPosts7jMin;
	private Double nbPosts7jMax;

	private Double nbPosts30j;
	private Double nbPosts30jMin;
	private Double nbPosts30jMax;

	private Double vues;
	private Double vuesMin;
	private Double vuesMax;

	private Double reactions;
	private Double reactionsMin;
	private Double reactionsMax;

	private Double commentaires;
	private Double commentairesMin;
	private Double commentairesMax;

	private java.time.LocalDateTime dateInsertion;
	private java.time.LocalDateTime dateInsertionMin;
	private java.time.LocalDateTime dateInsertionMax;

	
}
