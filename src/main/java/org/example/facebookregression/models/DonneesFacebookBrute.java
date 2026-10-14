package org.example.facebookregression.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="donnees_facebook_brutes")
public class DonneesFacebookBrute  {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique = true, nullable = false)
	private Integer id;
	@Column(name="longueur_message", unique = false, nullable = true)
	private Integer longueurMessage;
	@Column(name="nb_hashtags", unique = false, nullable = true)
	private Integer nbHashtags;
	@Column(name="nb_tags", unique = false, nullable = true)
	private Integer nbTags;
	@Column(name="contient_lien_externe", unique = false, nullable = true)
	private Boolean contientLienExterne;
	@Column(name="contient_emoji", unique = false, nullable = true)
	private Boolean contientEmoji;
	@Column(name="contient_question", unique = false, nullable = true)
	private Boolean contientQuestion;
	@Column(name="type_post", unique = false, nullable = true)
	@Size(max = 2147483647)
	private String typePost;
	@Column(name="ratio_image", unique = false, nullable = true)
	@Size(max = 2147483647)
	private String ratioImage;
	@Column(name="nb_photos_album", unique = false, nullable = true)
	private Integer nbPhotosAlbum;
	@Column(name="duree_video", unique = false, nullable = true)
	private Double dureeVideo;
	@Column(name="longueur_titre_video", unique = false, nullable = true)
	private Integer longueurTitreVideo;
	@Column(name="jour_semaine", unique = false, nullable = true)
	@Size(max = 2147483647)
	private String jourSemaine;
	@Column(name="heure_publication", unique = false, nullable = true)
	private Integer heurePublication;
	@Column(name="jours_depuis_dernier_post", unique = false, nullable = true)
	private Integer joursDepuisDernierPost;
	@Column(name="nb_posts_7j", unique = false, nullable = true)
	private Integer nbPosts7j;
	@Column(name="nb_posts_30j", unique = false, nullable = true)
	private Integer nbPosts30j;
	@Column(name="vues", unique = false, nullable = true)
	private Double vues;
	@Column(name="reactions", unique = false, nullable = true)
	private Double reactions;
	@Column(name="commentaires", unique = false, nullable = true)
	private Integer commentaires;
	@Column(name="date_insertion", unique = false, nullable = true)
	private java.time.LocalDateTime dateInsertion;
	

}
