package edu.eci.arsw.blueprints.web;

import edu.eci.arsw.blueprints.dto.ApiResponse;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Traduce las excepciones de negocio/validacion a respuestas ApiResponse con
 * el codigo HTTP correcto, para no repetir try/catch en cada endpoint del controller.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BlueprintNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(BlueprintNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.of(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(BlueprintPersistenceException.class)
    public ResponseEntity<ApiResponse<Object>> handlePersistence(BlueprintPersistenceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.of(HttpStatus.BAD_REQUEST.value(), message, null));
    }
}
