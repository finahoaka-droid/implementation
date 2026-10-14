package org.example.facebookregression.controllers;

import jakarta.validation.Valid;
import org.example.facebookregression.dto.CollecteRequest;
import org.example.facebookregression.dto.CollecteResultat;
import org.example.facebookregression.services.CollecteService;
import org.example.facebookregression.services.FacebookGraphApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur REST pour déclencher la collecte Facebook.
 *
 * GET  /collecte/test-connexion  → vérifie que le token est valide
 * POST /collecte/lancer          → lance la collecte complète
 *
 * Ces endpoints sont appelables depuis Postman ou le formulaire du frontend web.
 */
@RestController
@RequestMapping("/collecte")
public class CollecteController {

    private final CollecteService        collecteService;
    private final FacebookGraphApiService graphApi;

    public CollecteController(CollecteService collecteService, FacebookGraphApiService graphApi) {
        this.collecteService = collecteService;
        this.graphApi        = graphApi;
    }

    /**
     * Diagnostic rapide : vérifie que le token Facebook est valide.
     * Retourne {id, name} de l'utilisateur/page associé au token, ou le message d'erreur.
     */
    @GetMapping("/test-connexion")
    public ResponseEntity<Map<String, Object>> testConnexion() {
        return ResponseEntity.ok(graphApi.testConnexion());
    }

    /**
     * Lance la collecte, l'encodage et la sauvegarde en base pour la période donnée.
     *
     * @param request  Corps JSON avec les champs {@code since} et {@code until} (format yyyy-MM-dd)
     * @return         Résumé de la collecte (nb publications, nb lignes en base, chemin du CSV de trace)
     */
    @PostMapping("/lancer")
    public ResponseEntity<CollecteResultat> lancer(@Valid @RequestBody CollecteRequest request) {
        CollecteResultat resultat = collecteService.lancer(request);
        return ResponseEntity.ok(resultat);
    }
}
