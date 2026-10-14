package org.example.facebookregression.models.dto;

import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class ModelesRegressionFacebookSearch {

	private Integer id;

	private String nomModele;
	private String typeModele;
	private String variableCible;
	private Double mse;
	private Double mseMin;
	private Double mseMax;

	private Double r2;
	private Double r2Min;
	private Double r2Max;

	private java.time.LocalDateTime dateEntrainement;
	private java.time.LocalDateTime dateEntrainementMin;
	private java.time.LocalDateTime dateEntrainementMax;

	private byte[] modeleBinaire;
	
}
