package com.exam.todoapp.controller;

import com.exam.todoapp.model.Shoe;
import com.exam.todoapp.repository.ShoeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST CRUD pour le catalogue de chaussures.
 * Données stockées en mémoire (pas de base de données requise).
 * Documentation Swagger disponible sur /swagger-ui.html
 */
@RestController
@RequestMapping("/api/shoes")
@Tag(name = "Chaussures", description = "API CRUD pour le catalogue de chaussures - TP DevOps Exam")
public class ShoeController {

    private final ShoeRepository repository;

    public ShoeController(ShoeRepository repository) {
        this.repository = repository;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/shoes — Liste toutes les chaussures
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "Lister toutes les chaussures", description = "Retourne le catalogue complet des chaussures disponibles.")
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    public ResponseEntity<List<Shoe>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/shoes/{id} — Trouver par ID
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une chaussure par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Chaussure trouvée"),
        @ApiResponse(responseCode = "404", description = "Chaussure non trouvée")
    })
    public ResponseEntity<Shoe> getById(
            @Parameter(description = "ID de la chaussure", required = true) @PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/shoes — Créer une chaussure
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping
    @Operation(summary = "Créer une nouvelle chaussure dans le catalogue")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Chaussure créée"),
        @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<Shoe> create(@RequestBody Shoe shoe) {
        shoe.setId(null); // Force la génération d'un nouvel ID
        Shoe saved = repository.save(shoe);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PUT /api/shoes/{id} — Mettre à jour complètement
    // ──────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une chaussure existante (remplacement complet)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Chaussure mise à jour"),
        @ApiResponse(responseCode = "404", description = "Chaussure non trouvée")
    })
    public ResponseEntity<Shoe> update(
            @Parameter(description = "ID de la chaussure à modifier") @PathVariable Long id,
            @RequestBody Shoe shoe) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        shoe.setId(id);
        return ResponseEntity.ok(repository.save(shoe));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE /api/shoes/{id} — Supprimer
    // ──────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une chaussure du catalogue")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Chaussure supprimée"),
        @ApiResponse(responseCode = "404", description = "Chaussure non trouvée")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la chaussure à supprimer") @PathVariable Long id) {
        if (!repository.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
