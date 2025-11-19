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
 * asociados a canciones y álbumes. Requiere autenticación JWT para todos los endpoints.</p>
 *
 * <p>Validaciones aplicadas:</p>
 * <ul>
 *   <li>Audio: Formatos MP3, WAV, FLAC, M4A, OGG. Tamaño máximo 50MB</li>
 *   <li>Imágenes: Formatos JPG, PNG, WEBP. Tamaño máximo 5MB. Transformación a 1000x1000px</li>
 * </ul>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/multimedia")
public class MultimediaController {

    private final CloudinaryService cloudinaryService;

    /**
     * Sube un archivo de audio para una canción.
     *
     * <p>El audio se almacena en la carpeta media/canciones/audio y se convierte
     * automáticamente al formato MP3.</p>
     *
     * @param file archivo de audio multipart
     * @param authentication contexto de autenticación del usuario
     * @return respuesta con URL, duración y formato del audio subido
     * @throws NoFileProvidedException si no se proporciona archivo
     * @throws InvalidAudioFormatException si el formato no es válido
     * @throws AudioSizeExceededException si el tamaño excede el límite permitido
     * @throws AudioUploadFailedException si ocurre un error durante la subida
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

    /**
     * Sube una imagen de portada para una canción.
     *
     * <p>La imagen se almacena en la carpeta media/canciones/portadas y se redimensiona
     * automáticamente a 1000x1000 píxeles.</p>
     *
     * @param file archivo de imagen multipart
     * @param authentication contexto de autenticación del usuario
     * @return respuesta con URL y dimensiones de la imagen subida
     * @throws NoFileProvidedException si no se proporciona archivo
     * @throws InvalidImageFormatException si el formato no es válido
     * @throws ImageSizeExceededException si el tamaño excede el límite permitido
     * @throws ImageUploadFailedException si ocurre un error durante la subida
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
     * <p>La imagen se almacena en la carpeta media/albumes/portadas y se redimensiona
     * automáticamente a 1000x1000 píxeles.</p>
     *
     * @param file archivo de imagen multipart
     * @param authentication contexto de autenticación del usuario
     * @return respuesta con URL y dimensiones de la imagen subida
     * @throws NoFileProvidedException si no se proporciona archivo
     * @throws InvalidImageFormatException si el formato no es válido
     * @throws ImageSizeExceededException si el tamaño excede el límite permitido
     * @throws ImageUploadFailedException si ocurre un error durante la subida
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

    /**
     * Elimina un archivo multimedia de Cloudinary.
     *
     * <p>Elimina el archivo del servicio de almacenamiento. La actualización de las referencias
     * en la base de datos debe realizarse en los endpoints específicos de canción o álbum.</p>
     *
     * @param fileUrl URL completa del archivo a eliminar
     * @param authentication contexto de autenticación del usuario
     * @return respuesta de confirmación de la operación
     * @throws FileDeletionFailedException si ocurre un error durante la eliminación
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