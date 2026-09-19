package com.exam.todoapp.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Modèle représentant une chaussure dans le catalogue.
 * Stocké en mémoire via ConcurrentHashMap (pas de base de données).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modèle d'une chaussure du catalogue")
public class Shoe {

    @Schema(description = "Identifiant unique", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nom du modèle", example = "Air Max 90")
    private String nom;

    @Schema(description = "Marque fabricant", example = "Nike")
    private String marque;

    @Schema(description = "Pointure (taille EU)", example = "42")
    private Integer pointure;

    @Schema(description = "Prix en euros", example = "129.99")
    private BigDecimal prix;

    @Schema(description = "Quantité en stock", example = "50")
    private Integer stock;

    @Schema(description = "Couleur principale", example = "Blanc/Rouge")
    private String couleur;
}
