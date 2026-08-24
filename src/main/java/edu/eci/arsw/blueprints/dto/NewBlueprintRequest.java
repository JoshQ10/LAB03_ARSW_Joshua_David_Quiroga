package edu.eci.arsw.blueprints.dto;

import edu.eci.arsw.blueprints.model.Point;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/** DTO de entrada para POST /api/v1/blueprints. */
public record NewBlueprintRequest(
        @NotBlank String author,
        @NotBlank String name,
        @Valid List<Point> points
) { }
