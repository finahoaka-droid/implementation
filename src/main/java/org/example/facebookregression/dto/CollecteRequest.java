package org.example.facebookregression.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Paramètres de la collecte Facebook envoyés depuis le formulaire frontend.
 * Les dates doivent être au format ISO-8601 : yyyy-MM-dd.
 */
public class CollecteRequest {

    @NotBlank(message = "La date de début est obligatoire")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Format attendu : yyyy-MM-dd")
    private String since;

    @NotBlank(message = "La date de fin est obligatoire")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Format attendu : yyyy-MM-dd")
    private String until;

    public CollecteRequest() {}

    public CollecteRequest(String since, String until) {
        this.since = since;
        this.until = until;
    }

    public String getSince() { return since; }
    public void setSince(String since) { this.since = since; }

    public String getUntil() { return until; }
    public void setUntil(String until) { this.until = until; }
}
