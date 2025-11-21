package com.ondra.contenidos.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la aplicación.
 * Captura excepciones específicas y las convierte en respuestas HTTP apropiadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación de DTOs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("VALIDATION_ERROR")
                .message("Errores de validación en los datos proporcionados")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .detalles(errores)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // ========== EXCEPCIONES DE CONTENIDOS ==========

    /**
     * Maneja CancionNotFoundException.
     */
    @ExceptionHandler(CancionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCancionNotFound(CancionNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("CANCION_NOT_FOUND")
                .message(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja AlbumNotFoundException.
     */
    @ExceptionHandler(AlbumNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAlbumNotFound(AlbumNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("ALBUM_NOT_FOUND")
                .message(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja GeneroNotFoundException.
     */
    @ExceptionHandler(GeneroNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGeneroNotFound(GeneroNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("GENERO_NOT_FOUND")
                .message(ex.getMessage())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja CancionYaEnAlbumException.
     */
    @ExceptionHandler(CancionYaEnAlbumException.class)
    public ResponseEntity<ErrorResponse> handleCancionYaEnAlbum(CancionYaEnAlbumException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("CANCION_YA_EN_ALBUM")
                .message(ex.getMessage())
                .statusCode(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja CancionNoEnAlbumException.
     */
    @ExceptionHandler(CancionNoEnAlbumException.class)
    public ResponseEntity<ErrorResponse> handleCancionNoEnAlbum(CancionNoEnAlbumException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("CANCION_NO_EN_ALBUM")
                .message(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja AccesoDenegadoException.
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> handleAccesoDenegado(AccesoDenegadoException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("ACCESO_DENEGADO")
                .message(ex.getMessage())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Maneja NumeroPistaYaExisteException.
     */
    @ExceptionHandler(NumeroPistaYaExisteException.class)
    public ResponseEntity<ErrorResponse> handleNumeroPistaYaExiste(NumeroPistaYaExisteException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("NUMERO_PISTA_YA_EXISTE")
                .message(ex.getMessage())
                .statusCode(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    // ========== EXCEPCIONES DE FAVORITOS ==========

    /**
     * Maneja FavoritoYaExisteException.
     */
    @ExceptionHandler(FavoritoYaExisteException.class)
    public ResponseEntity<ErrorResponse> handleFavoritoYaExiste(FavoritoYaExisteException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("FAVORITO_YA_EXISTE")
                .message(ex.getMessage())
                .statusCode(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja FavoritoNotFoundException.
     */
    @ExceptionHandler(FavoritoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFavoritoNotFound(FavoritoNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("FAVORITO_NOT_FOUND")
                .message(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // ========== EXCEPCIONES DE CARRITO ==========

    /**
     * Maneja ItemYaEnCarritoException.
     */
    @ExceptionHandler(ItemYaEnCarritoException.class)
    public ResponseEntity<ErrorResponse> handleItemYaEnCarrito(ItemYaEnCarritoException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("ITEM_YA_EN_CARRITO")
                .message(ex.getMessage())
                .statusCode(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja CarritoNotFoundException.
     */
    @ExceptionHandler(CarritoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCarritoNotFound(CarritoNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("CARRITO_NOT_FOUND")
                .message(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja CarritoItemNotFoundException.
     */
    @ExceptionHandler(CarritoItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCarritoItemNotFound(CarritoItemNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("CARRITO_ITEM_NOT_FOUND")
                .message(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja CarritoVacioException.
     */
    @ExceptionHandler(CarritoVacioException.class)
    public ResponseEntity<ErrorResponse> handleCarritoVacio(CarritoVacioException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("CARRITO_VACIO")
                .message(ex.getMessage())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // ========== EXCEPCIONES DE ARCHIVOS (YA EXISTENTES) ==========

    /**
     * Maneja NoFileProvidedException.
     */
    @ExceptionHandler(NoFileProvidedException.class)
    public ResponseEntity<ErrorResponse> handleNoFileProvided(NoFileProvidedException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("NO_FILE_PROVIDED")
                .message(ex.getMessage())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja InvalidAudioFormatException.
     */
    @ExceptionHandler(InvalidAudioFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAudioFormat(InvalidAudioFormatException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("INVALID_AUDIO_FORMAT")
                .message(ex.getMessage())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja InvalidImageFormatException.
     */
    @ExceptionHandler(InvalidImageFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidImageFormat(InvalidImageFormatException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("INVALID_IMAGE_FORMAT")
                .message(ex.getMessage())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja AudioSizeExceededException.
     */
    @ExceptionHandler(AudioSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleAudioSizeExceeded(AudioSizeExceededException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("AUDIO_SIZE_EXCEEDED")
                .message(ex.getMessage())
                .statusCode(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    /**
     * Maneja ImageSizeExceededException.
     */
    @ExceptionHandler(ImageSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleImageSizeExceeded(ImageSizeExceededException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("IMAGE_SIZE_EXCEEDED")
                .message(ex.getMessage())
                .statusCode(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    /**
     * Maneja AudioUploadFailedException.
     */
    @ExceptionHandler(AudioUploadFailedException.class)
    public ResponseEntity<ErrorResponse> handleAudioUploadFailed(AudioUploadFailedException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("AUDIO_UPLOAD_FAILED")
                .message(ex.getMessage())
                .statusCode(HttpStatus.BAD_GATEWAY.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Maneja ImageUploadFailedException.
     */
    @ExceptionHandler(ImageUploadFailedException.class)
    public ResponseEntity<ErrorResponse> handleImageUploadFailed(ImageUploadFailedException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("IMAGE_UPLOAD_FAILED")
                .message(ex.getMessage())
                .statusCode(HttpStatus.BAD_GATEWAY.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Maneja FileDeletionFailedException.
     */
    @ExceptionHandler(FileDeletionFailedException.class)
    public ResponseEntity<ErrorResponse> handleFileDeletionFailed(FileDeletionFailedException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("FILE_DELETION_FAILED")
                .message(ex.getMessage())
                .statusCode(HttpStatus.BAD_GATEWAY.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Maneja MaxUploadSizeExceededException (Spring).
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("FILE_SIZE_EXCEEDED")
                .message("El archivo excede el tamaño máximo permitido")
                .statusCode(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    // ========== EXCEPCIONES GENÉRICAS ==========

    /**
     * Maneja ForbiddenAccessException.
     * Se lanza cuando un usuario intenta acceder a recursos sin los permisos adecuados.
     */
    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenAccess(ForbiddenAccessException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("FORBIDDEN_ACCESS")
                .message(ex.getMessage())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Maneja IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("INVALID_ARGUMENT")
                .message(ex.getMessage())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja cualquier otra excepción no controlada.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("INTERNAL_SERVER_ERROR")
                .message("Ocurrió un error inesperado en el servidor")
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        // Log del error real para debugging
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Clase interna para estructurar respuestas de error.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ErrorResponse {
        private String error;
        private String message;
        private Integer statusCode;
        private String timestamp;
        private Map<String, String> detalles;
    }
}