package org.example.facebookregression.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="modeles_regression_facebook")
public class ModelesRegressionFacebook  {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique = true, nullable = false)
	private Integer id;
	@Column(name="nom_modele", unique = false, nullable = false)
	@NotNull(message = "NomModele cannot be null")
	@Size(max = 2147483647)
	private String nomModele;
	@Column(name="type_modele", unique = false, nullable = false)
	@NotNull(message = "TypeModele cannot be null")
	@Size(max = 2147483647)
	private String typeModele;
	@Column(name="variable_cible", unique = false, nullable = false)
	@NotNull(message = "VariableCible cannot be null")
	@Size(max = 2147483647)
	private String variableCible;
	@Column(name="mse", unique = false, nullable = true)
	private Double mse;
	@Column(name="r2", unique = false, nullable = true)
	private Double r2;
	@Column(name="date_entrainement", unique = false, nullable = true)
	private java.time.LocalDateTime dateEntrainement;
	@Column(name="modele_binaire", unique = false, nullable = false)
	@NotNull(message = "ModeleBinaire cannot be null")
	private byte[] modeleBinaire;
	

}
