package com.ondra.contenidos.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Manejador global de excepciones para el microservicio de contenidos.
 *
 * <p>Captura excepciones específicas y las convierte en respuestas HTTP
 * estructuradas con códigos de estado apropiados.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación en DTOs.
     *
     * @param ex excepción de validación capturada
     * @return ResponseEntity con detalles de los errores de validación
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

    /**
     * Maneja excepciones cuando no se encuentra una canción.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 404
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
     * Maneja excepciones cuando no se encuentra un álbum.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 404
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
     * Maneja excepciones cuando no se encuentra un género musical.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 400
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
     * Maneja excepciones cuando una canción ya pertenece a un álbum.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 409
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
     * Maneja excepciones cuando una canción no pertenece al álbum especificado.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 404
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
     * Maneja excepciones de acceso denegado.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 403
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
     * Maneja excepciones cuando el número de pista ya existe en un álbum.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 409
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

    /**
     * Maneja excepciones cuando un favorito ya existe.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 409
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
     * Maneja excepciones cuando no se encuentra un favorito.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 404
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

    /**
     * Maneja excepciones cuando no se encuentra un comentario.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 404
     */
    @ExceptionHandler(ComentarioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleComentarioNotFound(ComentarioNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("COMENTARIO_NOT_FOUND")
                .message(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja excepciones cuando no se encuentra una valoración.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 404
     */
    @ExceptionHandler(ValoracionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleValoracionNotFound(ValoracionNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("VALORACION_NOT_FOUND")
                .message(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja excepciones cuando una valoración ya existe.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 409
     */
    @ExceptionHandler(ValoracionYaExisteException.class)
    public ResponseEntity<ErrorResponse> handleValoracionYaExiste(ValoracionYaExisteException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("VALORACION_YA_EXISTE")
                .message(ex.getMessage())
                .statusCode(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja excepciones cuando un item ya existe en el carrito.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 409
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
     * Maneja excepciones cuando no se encuentra un carrito.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 404
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
     * Maneja excepciones cuando no se encuentra un item en el carrito.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 404
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
     * Maneja excepciones cuando el carrito está vacío.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 400
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

    /**
     * Maneja excepciones cuando no se proporciona un archivo.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 400
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
     * Maneja excepciones cuando el formato de audio no es válido.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 400
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
     * Maneja excepciones cuando el formato de imagen no es válido.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 400
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
     * Maneja excepciones cuando el tamaño del audio excede el límite.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 413
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
     * Maneja excepciones cuando el tamaño de la imagen excede el límite.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 413
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
     * Maneja excepciones cuando falla la subida de audio.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 502
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
     * Maneja excepciones cuando falla la subida de imagen.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 502
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
     * Maneja excepciones cuando falla la eliminación de archivo.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 502
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
     * Maneja excepciones cuando el tamaño del archivo excede el límite de Spring.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 413
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

    /**
     * Maneja excepciones de acceso prohibido.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 403
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
     * Maneja excepciones de argumentos ilegales.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 400
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
     * Maneja cualquier excepción no controlada específicamente.
     *
     * @param ex excepción capturada
     * @return ResponseEntity con código 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("🚨 Error inesperado capturado: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("INTERNAL_SERVER_ERROR")
                .message("Ocurrió un error inesperado en el servidor")
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * DTO para respuestas de error estructuradas.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String error;
        private String message;
        private Integer statusCode;
        private String timestamp;
        private Map<String, String> detalles;
    }
}