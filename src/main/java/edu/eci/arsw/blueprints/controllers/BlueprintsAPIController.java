package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.dto.ApiResponse;
import edu.eci.arsw.blueprints.dto.NewBlueprintRequest;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Tag(name = "Blueprints", description = "Operaciones sobre planos (blueprints) y sus puntos")
@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    @Operation(summary = "Lista todos los blueprints registrados")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa")
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> data = services.getAllBlueprints();
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "execute ok", data));
    }

    @Operation(summary = "Lista los blueprints de un autor")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "El autor no tiene blueprints")
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> byAuthor(
            @Parameter(description = "Autor de los blueprints") @PathVariable String author) throws BlueprintNotFoundException {
        Set<Blueprint> data = services.getBlueprintsByAuthor(author);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "execute ok", data));
    }

    @Operation(summary = "Obtiene un blueprint por autor y nombre")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<Blueprint>> byAuthorAndName(
            @Parameter(description = "Autor del blueprint") @PathVariable String author,
            @Parameter(description = "Nombre del blueprint") @PathVariable String bpname) throws BlueprintNotFoundException {
        Blueprint data = services.getBlueprint(author, bpname);
        return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK.value(), "execute ok", data));
    }

    @Operation(summary = "Crea un nuevo blueprint")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint creado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos invalidos o blueprint ya existente")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> add(@Valid @RequestBody NewBlueprintRequest req) throws BlueprintPersistenceException {
        Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
        services.addNewBlueprint(bp);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED.value(), "blueprint created", null));
    }

    @Operation(summary = "Agrega un punto a un blueprint existente")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Punto agregado")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<Void>> addPoint(
            @Parameter(description = "Autor del blueprint") @PathVariable String author,
            @Parameter(description = "Nombre del blueprint") @PathVariable String bpname,
            @Valid @RequestBody Point p) throws BlueprintNotFoundException {
        services.addPoint(author, bpname, p.x(), p.y());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.of(HttpStatus.ACCEPTED.value(), "point added", null));
    }
}
