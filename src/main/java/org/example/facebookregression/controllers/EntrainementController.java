package org.example.facebookregression.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.facebookregression.dto.EntrainementResultat;
import org.example.facebookregression.services.WekaEntrainementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour déclencher l'entraînement des modèles Weka.
 *
 * <pre>
 * POST /entrainement/lancer
 *   → Lit output/features_facebook_encoded.csv
 *   → Évalue chaque cible par validation croisée répétée
 *   → Entraîne le meilleur candidat (J48 / Random Forest)
 *   → Stocke les modèles dans modeles_classification_facebook (PostgreSQL)
 *   → Retourne le rapport complet au format JSON
 * </pre>
 */
@RestController
@RequestMapping("/entrainement")
@Tag(
    name = "Entraînement Weka",
    description = "Déclenchement de l'entraînement des modèles de classification Facebook"
)
public class EntrainementController {

    private final WekaEntrainementService entrainementService;

    public EntrainementController(WekaEntrainementService entrainementService) {
        this.entrainementService = entrainementService;
    }

    /**
     * Lance le pipeline d'entraînement complet.
     *
     * <p>L'opération peut prendre plusieurs secondes (validation croisée répétée 5-plis × 10).
     * En cas d'erreur globale (CSV absent, BD inaccessible), le champ {@code erreur} du JSON
     * est renseigné ; en cas d'erreur partielle sur une cible, c'est le champ
     * {@code cibles[].resume} qui porte le message d'erreur avec le verdict {@code ERREUR}.</p>
     *
     * @return 200 OK avec {@link EntrainementResultat} ; le champ {@code erreur} signale
     *         un échec global si présent.
     */
    @Operation(
        summary = "Lancer l'entraînement des modèles Weka",
        description = """
            Lit le fichier output/features_facebook_encoded.csv produit par la collecte,
            évalue chaque modèle candidat (J48 contraint, Random Forest) par validation
            croisée stratifiée répétée (5 plis × 10 répétitions), compare à la classe
            majoritaire, sélectionne le meilleur, l'entraîne sur toutes les données et
            le stocke dans la table modeles_classification_facebook.

            Retourne pour chaque variable cible (reactions_classe, commentaires_classe,
            vues_classe) : le verdict FIABLE / NON_FIABLE / INSUFFISANT, l'accuracy,
            la baseline, le nombre d'instances et un indicateur de stockage en base.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Pipeline exécuté — vérifier le champ 'erreur' pour un échec global "
                        + "et 'cibles[].verdict' pour un échec par cible",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EntrainementResultat.class)
            )
        )
    })
    @PostMapping("/lancer")
    public ResponseEntity<EntrainementResultat> lancer() {
        EntrainementResultat resultat = entrainementService.lancer();
        return ResponseEntity.ok(resultat);
    }
}
