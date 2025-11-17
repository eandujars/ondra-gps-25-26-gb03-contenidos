package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.AudioResponseDTO;
import com.ondra.contenidos.dto.PortadaResponseDTO;
import com.ondra.contenidos.dto.SuccessfulResponseDTO;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * Controlador REST para la gestión de archivos multimedia en Cloudinary.
 *
 * <p>Proporciona endpoints para subida y eliminación de archivos de audio y portadas
 * asociados a canciones y álbumes. Todos los endpoints requieren autenticación JWT.</p>
 *
 * <p><strong>Validaciones de audio:</strong> Formatos MP3, WAV, FLAC, M4A, OGG. Máximo 50MB.</p>
 * <p><strong>Validaciones de portadas:</strong> Formatos JPG, PNG, WEBP. Máximo 5MB.
 * Transformación a 1000x1000px.</p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/multimedia")
public class MultimediaController {

    private final CloudinaryService cloudinaryService;

    // ==================== ENDPOINTS DE AUDIO ====================

    /**
     * Sube un archivo de audio para una canción.
     *
     * <p>El audio se almacena en <code>media/canciones/audio</code> y se convierte a MP3.</p>
     *
     * @param file Archivo de audio (máximo 50MB, formatos: MP3, WAV, FLAC, M4A, OGG)
     * @param authentication Autenticación del usuario
     * @return AudioResponseDTO con URL, duración y formato del audio
     * @throws NoFileProvidedException Si no se proporciona archivo
     * @throws InvalidAudioFormatException Si el formato es inválido
     * @throws AudioSizeExceededException Si excede el tamaño permitido
     * @throws AudioUploadFailedException Si falla la subida
     */
    @PostMapping(value = "/cancion/audio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AudioResponseDTO> subirAudioCancion(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        CloudinaryService.AudioUploadResult resultado =
                cloudinaryService.subirAudio(file, "canciones/audio");

        AudioResponseDTO response = AudioResponseDTO.builder()
                .url(resultado.getUrl())
                .duracion(resultado.getDuracion())
                .formato(resultado.getFormato())
                .mensaje("Audio de canción subido correctamente")
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== ENDPOINTS DE PORTADAS ====================

    /**
     * Sube una imagen de portada para una canción.
     *
     * <p>La imagen se almacena en <code>media/canciones/portadas</code>
     * con transformación a 1000x1000px.</p>
     *
     * @param file Archivo de imagen (máximo 5MB, formatos: JPG, PNG, WEBP)
     * @param authentication Autenticación del usuario
     * @return PortadaResponseDTO con URL de la imagen
     * @throws NoFileProvidedException Si no se proporciona archivo
     * @throws InvalidImageFormatException Si el formato es inválido
     * @throws ImageSizeExceededException Si excede el tamaño permitido
     * @throws ImageUploadFailedException Si falla la subida
     */
    @PostMapping(value = "/cancion/portada",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PortadaResponseDTO> subirPortadaCancion(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        String portadaUrl = cloudinaryService.subirPortada(file, "canciones/portadas");

        PortadaResponseDTO response = PortadaResponseDTO.builder()
                .url(portadaUrl)
                .mensaje("Portada de canción subida correctamente")
                .dimensiones("1000x1000")
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Sube una imagen de portada para un álbum.
     *
     * <p>La imagen se almacena en <code>media/albumes/portadas</code>
     * con transformación a 1000x1000px.</p>
     *
     * @param file Archivo de imagen (máximo 5MB, formatos: JPG, PNG, WEBP)
     * @param authentication Autenticación del usuario
     * @return PortadaResponseDTO con URL de la imagen
     * @throws NoFileProvidedException Si no se proporciona archivo
     * @throws InvalidImageFormatException Si el formato es inválido
     * @throws ImageSizeExceededException Si excede el tamaño permitido
     * @throws ImageUploadFailedException Si falla la subida
     */
    @PostMapping(value = "/album/portada",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PortadaResponseDTO> subirPortadaAlbum(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        String portadaUrl = cloudinaryService.subirPortada(file, "albumes/portadas");

        PortadaResponseDTO response = PortadaResponseDTO.builder()
                .url(portadaUrl)
                .mensaje("Portada de álbum subida correctamente")
                .dimensiones("1000x1000")
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== ENDPOINTS DE ELIMINACIÓN ====================

    /**
     * Elimina un archivo multimedia de Cloudinary.
     *
     * <p>Elimina el archivo del servicio de almacenamiento. La actualización de
     * referencias en base de datos debe realizarse en los endpoints específicos
     * de canción o álbum.</p>
     *
     * @param fileUrl URL completa del archivo a eliminar
     * @param authentication Autenticación del usuario
     * @return SuccessfulResponseDTO con resultado de la operación
     * @throws FileDeletionFailedException Si falla la eliminación
     */
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuccessfulResponseDTO> eliminarArchivo(
            @RequestParam("url") String fileUrl,
            Authentication authentication) {

        Long authenticatedUserId = Long.parseLong(authentication.getName());

        cloudinaryService.eliminarArchivo(fileUrl);

        SuccessfulResponseDTO response = SuccessfulResponseDTO.builder()
                .successful("Eliminación de archivo exitosa")
                .message("El archivo ha sido eliminado correctamente de Cloudinary")
                .statusCode(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now().toString())
                .build();

        return ResponseEntity.ok(response);
    }
}