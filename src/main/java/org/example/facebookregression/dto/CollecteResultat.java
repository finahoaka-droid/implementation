package org.example.facebookregression.dto;

/**
 * Résultat renvoyé au frontend après une collecte.
 */
public class CollecteResultat {

    private int nbPublicationsCollectees;
    private int nbLignesBrutes;
    private int nbLignesEncoded;
    private String cheminCsvTrace;
    private String since;
    private String until;
    private String message;
    private String erreurApi;

    public CollecteResultat() {}

    public int getNbPublicationsCollectees() { return nbPublicationsCollectees; }
    public void setNbPublicationsCollectees(int nbPublicationsCollectees) {
        this.nbPublicationsCollectees = nbPublicationsCollectees;
    }

    public int getNbLignesBrutes() { return nbLignesBrutes; }
    public void setNbLignesBrutes(int nbLignesBrutes) { this.nbLignesBrutes = nbLignesBrutes; }

    public int getNbLignesEncoded() { return nbLignesEncoded; }
    public void setNbLignesEncoded(int nbLignesEncoded) { this.nbLignesEncoded = nbLignesEncoded; }

    public String getCheminCsvTrace() { return cheminCsvTrace; }
    public void setCheminCsvTrace(String cheminCsvTrace) { this.cheminCsvTrace = cheminCsvTrace; }

    public String getSince() { return since; }
    public void setSince(String since) { this.since = since; }

    public String getUntil() { return until; }
    public void setUntil(String until) { this.until = until; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErreurApi() { return erreurApi; }
    public void setErreurApi(String erreurApi) { this.erreurApi = erreurApi; }
}
