package org.example.facebookregression.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="donnees_facebook_encoded")
public class DonneesFacebookEncoded  {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique = true, nullable = false)
	private Integer id;
	@Column(name="longueur_message", unique = false, nullable = true)
	private Double longueurMessage;
	@Column(name="nb_hashtags", unique = false, nullable = true)
	private Double nbHashtags;
	@Column(name="nb_tags", unique = false, nullable = true)
	private Double nbTags;
	@Column(name="contient_lien_externe", unique = false, nullable = true)
	private Double contientLienExterne;
	@Column(name="contient_emoji", unique = false, nullable = true)
	private Double contientEmoji;
	@Column(name="contient_question", unique = false, nullable = true)
	private Double contientQuestion;
	@Column(name="type_post_photo", unique = false, nullable = true)
	private Double typePostPhoto;
	@Column(name="type_post_album", unique = false, nullable = true)
	private Double typePostAlbum;
	@Column(name="type_post_video", unique = false, nullable = true)
	private Double typePostVideo;
	@Column(name="type_post_lien", unique = false, nullable = true)
	private Double typePostLien;
	@Column(name="type_post_evenement", unique = false, nullable = true)
	private Double typePostEvenement;
	@Column(name="ratio_image", unique = false, nullable = true)
	private Double ratioImage;
	@Column(name="nb_photos_album", unique = false, nullable = true)
	private Double nbPhotosAlbum;
	@Column(name="duree_video", unique = false, nullable = true)
	private Double dureeVideo;
	@Column(name="longueur_titre_video", unique = false, nullable = true)
	private Double longueurTitreVideo;
	@Column(name="jour_lundi", unique = false, nullable = true)
	private Double jourLundi;
	@Column(name="jour_mardi", unique = false, nullable = true)
	private Double jourMardi;
	@Column(name="jour_mercredi", unique = false, nullable = true)
	private Double jourMercredi;
	@Column(name="jour_jeudi", unique = false, nullable = true)
	private Double jourJeudi;
	@Column(name="jour_vendredi", unique = false, nullable = true)
	private Double jourVendredi;
	@Column(name="jour_samedi", unique = false, nullable = true)
	private Double jourSamedi;
	@Column(name="heure_publication", unique = false, nullable = true)
	private Double heurePublication;
	@Column(name="jours_depuis_dernier_post", unique = false, nullable = true)
	private Double joursDepuisDernierPost;
	@Column(name="nb_posts_7j", unique = false, nullable = true)
	private Double nbPosts7j;
	@Column(name="nb_posts_30j", unique = false, nullable = true)
	private Double nbPosts30j;
	@Column(name="vues", unique = false, nullable = true)
	private Double vues;
	@Column(name="reactions", unique = false, nullable = true)
	private Double reactions;
	@Column(name="commentaires", unique = false, nullable = true)
	private Double commentaires;
	@Column(name="date_insertion", unique = false, nullable = true)
	private java.time.LocalDateTime dateInsertion;
	

}
