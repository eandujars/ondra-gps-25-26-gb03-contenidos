package com.ondra.contenidos.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para el microservicio de Contenidos.
 *
 * <p>Captura todas las excepciones personalizadas relacionadas con
 * operaciones de archivos multimedia y las transforma en respuestas
 * HTTP apropiadas con mensajes de error estructurados.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== EXCEPCIONES GENERALES ====================

    /**
     * Maneja el caso cuando no se proporciona ningún archivo en la petición.
     *
     * @param ex Excepción capturada
     * @return ResponseEntity con código 400 (Bad Request)
     */
    @ExceptionHandler(NoFileProvidedException.class)
    public ResponseEntity<Map<String, Object>> handleNoFileProvided(NoFileProvidedException ex) {
        log.warn("NoFileProvidedException: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Archivo no proporcionado",
                ex.getMessage()
        );
    }

    // ==================== EXCEPCIONES DE AUDIO ====================

    /**
     * Maneja errores de formato de audio inválido.
     *
     * @param ex Excepción capturada
     * @return ResponseEntity con código 400 (Bad Request)
     */
    @ExceptionHandler(InvalidAudioFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAudioFormat(InvalidAudioFormatException ex) {
        log.warn("InvalidAudioFormatException: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Formato de audio inválido",
                ex.getMessage()
        );
    }

    /**
     * Maneja errores cuando el archivo de audio excede el tamaño máximo permitido.
     *
     * @param ex Excepción capturada
     * @return ResponseEntity con código 413 (Payload Too Large)
     */
    @ExceptionHandler(AudioSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleAudioSizeExceeded(AudioSizeExceededException ex) {
        log.warn("AudioSizeExceededException: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Tamaño de audio excedido",
                ex.getMessage()
        );
    }

    /**
     * Maneja errores durante la subida de archivos de audio a Cloudinary.
     *
     * @param ex Excepción capturada
     * @return ResponseEntity con código 500 (Internal Server Error)
     */
    @ExceptionHandler(AudioUploadFailedException.class)
    public ResponseEntity<Map<String, Object>> handleAudioUploadFailed(AudioUploadFailedException ex) {
        log.error("AudioUploadFailedException: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al subir audio",
                "Ocurrió un error al intentar subir el archivo de audio. Por favor, inténtelo de nuevo."
        );
    }

    // ==================== EXCEPCIONES DE IMAGEN ====================

    /**
     * Maneja errores de formato de imagen inválido.
     *
     * @param ex Excepción capturada
     * @return ResponseEntity con código 400 (Bad Request)
     */
    @ExceptionHandler(InvalidImageFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidImageFormat(InvalidImageFormatException ex) {
        log.warn("InvalidImageFormatException: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Formato de imagen inválido",
                ex.getMessage()
        );
    }

    /**
     * Maneja errores cuando la imagen excede el tamaño máximo permitido.
     *
     * @param ex Excepción capturada
     * @return ResponseEntity con código 413 (Payload Too Large)
     */
    @ExceptionHandler(ImageSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleImageSizeExceeded(ImageSizeExceededException ex) {
        log.warn("ImageSizeExceededException: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Tamaño de imagen excedido",
                ex.getMessage()
        );
    }

    /**
     * Maneja errores durante la subida de imágenes a Cloudinary.
     *
     * @param ex Excepción capturada
     * @return ResponseEntity con código 500 (Internal Server Error)
     */
    @ExceptionHandler(ImageUploadFailedException.class)
    public ResponseEntity<Map<String, Object>> handleImageUploadFailed(ImageUploadFailedException ex) {
        log.error("ImageUploadFailedException: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al subir imagen",
                "Ocurrió un error al intentar subir la imagen. Por favor, inténtelo de nuevo."
        );
    }

    // ==================== EXCEPCIONES DE ELIMINACIÓN ====================

    /**
     * Maneja errores durante la eliminación de archivos de Cloudinary.
     *
     * @param ex Excepción capturada
     * @return ResponseEntity con código 500 (Internal Server Error)
     */
    @ExceptionHandler(FileDeletionFailedException.class)
    public ResponseEntity<Map<String, Object>> handleFileDeletionFailed(FileDeletionFailedException ex) {
        log.error("FileDeletionFailedException: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al eliminar archivo",
                "Ocurrió un error al intentar eliminar el archivo. Por favor, inténtelo de nuevo."
        );
    }

    // ==================== EXCEPCIÓN GENÉRICA ====================

    /**
     * Maneja cualquier otra excepción no controlada específicamente.
     *
     * @param ex Excepción capturada
     * @return ResponseEntity con código 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Excepción no controlada: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                "Ha ocurrido un error inesperado. Por favor, contacte al administrador."
        );
    }

    // ==================== MÉTODO AUXILIAR ====================

    /**
     * Construye una respuesta de error estructurada.
     *
     * @param status Código de estado HTTP
     * @param error Título del error
     * @param message Mensaje descriptivo del error
     * @return ResponseEntity con el mapa de error
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String error, String message) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);

        return new ResponseEntity<>(errorResponse, status);
    }
}