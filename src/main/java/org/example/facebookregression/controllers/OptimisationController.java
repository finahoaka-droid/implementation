package org.example.facebookregression.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.facebookregression.dto.RecommandationRequest;
import org.example.facebookregression.dto.RecommandationResultat;
import org.example.facebookregression.dto.SimulationRequest;
import org.example.facebookregression.dto.SuggestionResultat;
import org.example.facebookregression.services.OptimisationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour la recommandation de profils de publication et
 * la suggestion d'améliorations basées sur les modèles Weka.
 *
 * <pre>
 * POST /optimisation/recommander
 *   → Reçoit les classes cibles voulues (réactions, commentaires, vues)
 *   → Retourne les profils de publication observés les mieux classés
 *
 * POST /optimisation/suggerer
 *   → Reçoit un brouillon de publication + les classes cibles voulues
 *   → Retourne des suggestions d'améliorations concrètes
 * </pre>
 */
@RestController
@RequestMapping("/optimisation")
@Tag(
    name = "Optimisation et Recommandation",
    description = "Recommander des profils de publication ou suggérer des améliorations pour atteindre des classes cibles"
)
public class OptimisationController {

    private final OptimisationService optimisationService;

    public OptimisationController(OptimisationService optimisationService) {
        this.optimisationService = optimisationService;
    }

    /* ====================================================================== RECOMMANDER ===== */

    @Operation(
        summary = "Recommander des profils de publication pour atteindre des classes cibles",
        description = """
            Étant donné des classes cibles souhaitées (ex: réactions '7-18', commentaires '1+'),
            cherche parmi toutes les publications réellement observées (CSV brut) celles dont
            le profil maximise la probabilité conjointe d'atteindre ces classes.

            Retourne les 3 meilleurs profils observés avec :
            - leurs caractéristiques brutes (type, jour, heure, hashtags...)
            - le score conjoint (moyenne géométrique des probabilités)
            - la probabilité de chaque cible

            Prérequis :
            - Avoir lancé la collecte (POST /collecte/lancer) pour avoir le CSV de données observées
            - Avoir lancé l'entraînement (POST /entrainement/lancer) pour avoir des modèles disponibles
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Recommandation calculée avec succès",
            content = @Content(schema = @Schema(implementation = RecommandationResultat.class))
        )
    })
    @PostMapping("/recommander")
    public ResponseEntity<RecommandationResultat> recommander(
            @RequestBody(required = false) RecommandationRequest request) {
        if (request == null) request = new RecommandationRequest();
        RecommandationResultat resultat = optimisationService.recommander(request);
        return ResponseEntity.ok(resultat);
    }

    /* ====================================================================== SUGGERER ========= */

    @Operation(
        summary = "Suggérer des améliorations pour rapprocher un brouillon de l'objectif",
        description = """
            Étant donné un brouillon de publication (type, longueur du message, hashtags, heure...)
            et des classes cibles souhaitées (ex: réactions '19+', commentaires '1+'),
            compare le brouillon aux publications observées les plus proches de l'objectif,
            puis propose des modifications concrètes qui améliorent le score conjoint des modèles.

            Pour chaque suggestion :
            - texte lisible décrivant le changement proposé
            - score avant / après la modification (en %)
            - gain de score (en points de pourcentage)

            Retourne aussi l'effet cumulé si toutes les suggestions sont appliquées.

            Prérequis :
            - Avoir lancé la collecte (POST /collecte/lancer)
            - Avoir lancé l'entraînement (POST /entrainement/lancer)
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Suggestions calculées avec succès",
            content = @Content(schema = @Schema(implementation = SuggestionResultat.class))
        )
    })
    @PostMapping("/suggerer")
    public ResponseEntity<SuggestionResultat> suggerer(
            @RequestBody(required = false) SuggestionRequest request) {
        if (request == null) request = new SuggestionRequest();
        SuggestionResultat resultat = optimisationService.suggerer(
                request.getBrouillon(), request.getCibles());
        return ResponseEntity.ok(resultat);
    }

    /* ====================================================================== INNER DTO ======== */

    /**
     * Corps de la requête pour /suggerer :
     * contient le brouillon de publication et les classes cibles.
     */
    @Schema(description = "Requête de suggestion : brouillon de publication + classes cibles")
    public static class SuggestionRequest {

        @Schema(description = "Caractéristiques du brouillon de publication à améliorer")
        private SimulationRequest brouillon;

        @Schema(description = "Classes cibles souhaitées (réactions, commentaires, vues)")
        private RecommandationRequest cibles;

        public SuggestionRequest() {
        }

        public SimulationRequest getBrouillon() {
            return brouillon;
        }

        public void setBrouillon(SimulationRequest brouillon) {
            this.brouillon = brouillon;
        }

        public RecommandationRequest getCibles() {
            return cibles;
        }

        public void setCibles(RecommandationRequest cibles) {
            this.cibles = cibles;
        }
    }
}
