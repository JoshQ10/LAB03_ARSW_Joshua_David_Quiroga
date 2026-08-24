package edu.eci.arsw.blueprints.dto;

/**
 * Envoltorio uniforme para todas las respuestas de la API, tal como lo pide
 * el laboratorio: { "code": ..., "message": ..., "data": ... }.
 */
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> of(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}
