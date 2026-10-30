package org.example.facebookregression.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.facebookregression.dto.SimulationRequest;
import org.example.facebookregression.dto.SimulationResultat;
import org.example.facebookregression.services.SimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la simulation de publications Facebook et la prédiction
 * du niveau de réactions, de commentaires et de vues.
 */
@RestController
@RequestMapping("/simulation")
@Tag(
    name = "Simulation et Prédiction",
    description = "Simuler une publication Facebook et prédire les réactions, commentaires et vues associés"
)
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    /**
     * Simule une publication et prédit les classes de réactions, commentaires et vues.
     *
     * @param request Caractéristiques de la publication simulée
     * @return Résultat de prédiction par cible avec probabilités et verdict de fiabilité
     */
    @Operation(
        summary = "Simuler une publication et prédire son impact",
        description = """
            Prend en entrée les caractéristiques d'une publication Facebook simulée
            (type de post, longueur du message, hashtags, tags, emojis, ratio d'image, jour, heure...),
            encode les données selon les variables du modèle, et utilise les modèles Weka
            préalablement entraînés pour prédire la classe attendue pour :
            - les réactions (ex: 0-6, 7-18, 19+)
            - les commentaires (0 vs 1+)
            - les vues (pour les vidéos et albums uniquement)
            avec les probabilités associées et l'indication de fiabilité.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Simulation réalisée avec succès",
            content = @Content(schema = @Schema(implementation = SimulationResultat.class))
        )
    })
    @PostMapping("/predire")
    public ResponseEntity<SimulationResultat> predire(@RequestBody(required = false) SimulationRequest request) {
        SimulationResultat resultat = simulationService.simuler(request);
        return ResponseEntity.ok(resultat);
    }

    @Operation(
        summary = "Alias pour /simulation/predire",
        description = "Même fonctionnement que /simulation/predire."
    )
    @PostMapping("/simuler")
    public ResponseEntity<SimulationResultat> simuler(@RequestBody(required = false) SimulationRequest request) {
        return predire(request);
    }
}
