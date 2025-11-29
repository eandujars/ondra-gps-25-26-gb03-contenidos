package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.security.*;
import com.ondra.contenidos.services.CloudinaryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para {@link MultimediaController}
 *
 * <p>
 * Esta clase cubre pruebas de:
 * </p>
 *
 * <ul>
 *   <li>Subida de archivos de audio para canciones</li>
 *   <li>Subida de portadas para canciones</li>
 *   <li>Subida de portadas para álbumes</li>
 *   <li>Eliminación de archivos multimedia</li>
 *   <li>Validación de formatos de audio (MP3, WAV, FLAC, M4A, OGG)</li>
 *   <li>Validación de formatos de imagen (JPG, PNG, WEBP)</li>
 *   <li>Validación de tamaños máximos (50MB audio, 5MB imagen)</li>
 *   <li>Manejo de errores de subida y eliminación</li>
 *   <li>Autenticación JWT requerida</li>
 * </ul>
 */
@WebMvcTest(MultimediaController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
@TestPropertySource(properties = {"service.token=e3e27aa2f289a9686be1d3c45659f308748be2d8751c4911b1cecb09fdd228fa"})
class MultimediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    // ==================== TESTS SUBIR AUDIO DE CANCIÓN ====================

    @Test
    @DisplayName("Subir audio de canción MP3 - exitoso")
    void subirAudioCancion_MP3_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile audioFile = new MockMultipartFile(
                "file",
                "cancion.mp3",
                "audio/mpeg",
                "fake audio content".getBytes()
        );

        CloudinaryService.AudioUploadResult resultado =
                new CloudinaryService.AudioUploadResult(
                        "https://res.cloudinary.com/demo/video/upload/v1234/media/canciones/audio/abc123.mp3",
                        180,
                        "mp3"
                );

        when(cloudinaryService.subirAudio(any(), eq("canciones/audio")))
                .thenReturn(resultado);

        mockMvc.perform(multipart("/api/multimedia/cancion/audio")
                        .file(audioFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(resultado.getUrl()))
                .andExpect(jsonPath("$.duracion").value(180))
                .andExpect(jsonPath("$.formato").value("mp3"))
                .andExpect(jsonPath("$.mensaje").value("Audio de canción subido correctamente"));

        verify(cloudinaryService, times(1)).subirAudio(any(), eq("canciones/audio"));
    }

    @Test
    @DisplayName("Subir audio de canción WAV - exitoso")
    void subirAudioCancion_WAV_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile audioFile = new MockMultipartFile(
                "file",
                "cancion.wav",
                "audio/wav",
                "fake audio content".getBytes()
        );

        CloudinaryService.AudioUploadResult resultado =
                new CloudinaryService.AudioUploadResult(
                        "https://res.cloudinary.com/demo/video/upload/v1234/media/canciones/audio/abc123.mp3",
                        200,
                        "wav"
                );

        when(cloudinaryService.subirAudio(any(), eq("canciones/audio")))
                .thenReturn(resultado);

        mockMvc.perform(multipart("/api/multimedia/cancion/audio")
                        .file(audioFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.formato").value("wav"));
    }

    @Test
    @DisplayName("Subir audio sin archivo - Bad Request")
    void subirAudioCancion_SinArchivo() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cloudinaryService.subirAudio(any(), eq("canciones/audio")))
                .thenThrow(new NoFileProvidedException("No se ha proporcionado ningún archivo"));

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "",
                "audio/mpeg",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/multimedia/cancion/audio")
                        .file(emptyFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Subir audio con formato inválido - Bad Request")
    void subirAudioCancion_FormatoInvalido() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "fake content".getBytes()
        );

        when(cloudinaryService.subirAudio(any(), eq("canciones/audio")))
                .thenThrow(new InvalidAudioFormatException(
                        "El archivo debe ser un audio válido (MP3, WAV, FLAC, M4A, OGG)"
                ));

        mockMvc.perform(multipart("/api/multimedia/cancion/audio")
                        .file(invalidFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Subir audio que excede tamaño máximo - Payload Too Large")
    void subirAudioCancion_ExcedeTamano() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "cancion.mp3",
                "audio/mpeg",
                new byte[51 * 1024 * 1024] // 51 MB
        );

        when(cloudinaryService.subirAudio(any(), eq("canciones/audio")))
                .thenThrow(new AudioSizeExceededException("El archivo de audio no puede superar los 50MB"));

        mockMvc.perform(multipart("/api/multimedia/cancion/audio")
                        .file(largeFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(413)); // 413
    }

    @Test
    @DisplayName("Subir audio - error de Cloudinary")
    void subirAudioCancion_ErrorCloudinary() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile audioFile = new MockMultipartFile(
                "file",
                "cancion.mp3",
                "audio/mpeg",
                "fake audio content".getBytes()
        );

        when(cloudinaryService.subirAudio(any(), eq("canciones/audio")))
                .thenThrow(new AudioUploadFailedException("Error al subir el audio a Cloudinary", new RuntimeException()));

        mockMvc.perform(multipart("/api/multimedia/cancion/audio")
                        .file(audioFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(502)); // 502
    }

    @Test
    @DisplayName("Subir audio sin autenticación - Forbidden")
    void subirAudioCancion_SinAutenticacion() throws Exception {
        MockMultipartFile audioFile = new MockMultipartFile(
                "file",
                "cancion.mp3",
                "audio/mpeg",
                "fake audio content".getBytes()
        );

        mockMvc.perform(multipart("/api/multimedia/cancion/audio")
                        .file(audioFile)
                        .with(csrf()))
                .andExpect(status().is(403)); // 403

        verify(cloudinaryService, never()).subirAudio(any(), any());
    }

    // ==================== TESTS SUBIR PORTADA DE CANCIÓN ====================

    @Test
    @DisplayName("Subir portada de canción JPG - exitoso")
    void subirPortadaCancion_JPG_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "portada.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        String cloudinaryUrl = "https://res.cloudinary.com/demo/image/upload/v1234/media/canciones/portadas/xyz789.jpg";

        when(cloudinaryService.subirPortada(any(), eq("canciones/portadas")))
                .thenReturn(cloudinaryUrl);

        mockMvc.perform(multipart("/api/multimedia/cancion/portada")
                        .file(imageFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(cloudinaryUrl))
                .andExpect(jsonPath("$.dimensiones").value("1000x1000"))
                .andExpect(jsonPath("$.mensaje").value("Portada de canción subida correctamente"));

        verify(cloudinaryService, times(1)).subirPortada(any(), eq("canciones/portadas"));
    }

    @Test
    @DisplayName("Subir portada de canción PNG - exitoso")
    void subirPortadaCancion_PNG_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "portada.png",
                "image/png",
                "fake image content".getBytes()
        );

        String cloudinaryUrl = "https://res.cloudinary.com/demo/image/upload/v1234/media/canciones/portadas/xyz789.png";

        when(cloudinaryService.subirPortada(any(), eq("canciones/portadas")))
                .thenReturn(cloudinaryUrl);

        mockMvc.perform(multipart("/api/multimedia/cancion/portada")
                        .file(imageFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(cloudinaryUrl));
    }

    @Test
    @DisplayName("Subir portada de canción sin archivo - Bad Request")
    void subirPortadaCancion_SinArchivo() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cloudinaryService.subirPortada(any(), eq("canciones/portadas")))
                .thenThrow(new NoFileProvidedException("No se ha proporcionado ningún archivo"));

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "",
                "image/jpeg",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/multimedia/cancion/portada")
                        .file(emptyFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Subir portada con formato inválido - Bad Request")
    void subirPortadaCancion_FormatoInvalido() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                "fake content".getBytes()
        );

        when(cloudinaryService.subirPortada(any(), eq("canciones/portadas")))
                .thenThrow(new InvalidImageFormatException(
                        "El archivo debe ser una imagen válida (JPG, PNG, WEBP)"
                ));

        mockMvc.perform(multipart("/api/multimedia/cancion/portada")
                        .file(invalidFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Subir portada que excede tamaño máximo - Payload Too Large")
    void subirPortadaCancion_ExcedeTamano() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "portada.jpg",
                "image/jpeg",
                new byte[6 * 1024 * 1024] // 6 MB
        );

        when(cloudinaryService.subirPortada(any(), eq("canciones/portadas")))
                .thenThrow(new ImageSizeExceededException("La imagen no puede superar los 5MB"));

        mockMvc.perform(multipart("/api/multimedia/cancion/portada")
                        .file(largeFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(413)); // 413
    }

    @Test
    @DisplayName("Subir portada - error de Cloudinary")
    void subirPortadaCancion_ErrorCloudinary() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "portada.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        when(cloudinaryService.subirPortada(any(), eq("canciones/portadas")))
                .thenThrow(new ImageUploadFailedException("Error al subir la portada a Cloudinary", new RuntimeException()));

        mockMvc.perform(multipart("/api/multimedia/cancion/portada")
                        .file(imageFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(502)); // 502
    }

    // ==================== TESTS SUBIR PORTADA DE ÁLBUM ====================

    @Test
    @DisplayName("Subir portada de álbum - exitoso")
    void subirPortadaAlbum_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "portada.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        String cloudinaryUrl = "https://res.cloudinary.com/demo/image/upload/v1234/media/albumes/portadas/xyz789.jpg";

        when(cloudinaryService.subirPortada(any(), eq("albumes/portadas")))
                .thenReturn(cloudinaryUrl);

        mockMvc.perform(multipart("/api/multimedia/album/portada")
                        .file(imageFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(cloudinaryUrl))
                .andExpect(jsonPath("$.dimensiones").value("1000x1000"))
                .andExpect(jsonPath("$.mensaje").value("Portada de álbum subida correctamente"));

        verify(cloudinaryService, times(1)).subirPortada(any(), eq("albumes/portadas"));
    }

    @Test
    @DisplayName("Subir portada de álbum WEBP - exitoso")
    void subirPortadaAlbum_WEBP_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "portada.webp",
                "image/webp",
                "fake image content".getBytes()
        );

        String cloudinaryUrl = "https://res.cloudinary.com/demo/image/upload/v1234/media/albumes/portadas/xyz789.webp";

        when(cloudinaryService.subirPortada(any(), eq("albumes/portadas")))
                .thenReturn(cloudinaryUrl);

        mockMvc.perform(multipart("/api/multimedia/album/portada")
                        .file(imageFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(cloudinaryUrl));
    }

    @Test
    @DisplayName("Subir portada de álbum con formato inválido - Bad Request")
    void subirPortadaAlbum_FormatoInvalido() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "fake content".getBytes()
        );

        when(cloudinaryService.subirPortada(any(), eq("albumes/portadas")))
                .thenThrow(new InvalidImageFormatException(
                        "El archivo debe ser una imagen válida (JPG, PNG, WEBP)"
                ));

        mockMvc.perform(multipart("/api/multimedia/album/portada")
                        .file(invalidFile)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ==================== TESTS ELIMINAR ARCHIVO ====================

    @Test
    @DisplayName("Eliminar archivo de audio - exitoso")
    void eliminarArchivo_Audio_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        String fileUrl = "https://res.cloudinary.com/demo/video/upload/v1234/media/canciones/audio/abc123.mp3";

        doNothing().when(cloudinaryService).eliminarArchivo(fileUrl);

        mockMvc.perform(delete("/api/multimedia")
                        .param("url", fileUrl)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("El archivo ha sido eliminado correctamente de Cloudinary"))
                .andExpect(jsonPath("$.statusCode").value(200));

        verify(cloudinaryService, times(1)).eliminarArchivo(fileUrl);
    }

    @Test
    @DisplayName("Eliminar archivo de imagen - exitoso")
    void eliminarArchivo_Imagen_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        String fileUrl = "https://res.cloudinary.com/demo/image/upload/v1234/media/canciones/portadas/xyz789.jpg";

        doNothing().when(cloudinaryService).eliminarArchivo(fileUrl);

        mockMvc.perform(delete("/api/multimedia")
                        .param("url", fileUrl)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("El archivo ha sido eliminado correctamente de Cloudinary"));

        verify(cloudinaryService, times(1)).eliminarArchivo(fileUrl);
    }

    @Test
    @DisplayName("Eliminar archivo - error de Cloudinary")
    void eliminarArchivo_ErrorCloudinary() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        String fileUrl = "https://res.cloudinary.com/demo/image/upload/v1234/media/canciones/portadas/xyz789.jpg";

        doThrow(new FileDeletionFailedException("Error al eliminar el archivo de Cloudinary", new RuntimeException()))
                .when(cloudinaryService).eliminarArchivo(fileUrl);

        mockMvc.perform(delete("/api/multimedia")
                        .param("url", fileUrl)
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(502)); // 502

        verify(cloudinaryService, times(1)).eliminarArchivo(fileUrl);
    }

    @Test
    @DisplayName("Eliminar archivo sin autenticación - Forbidden")
    void eliminarArchivo_SinAutenticacion() throws Exception {
        String fileUrl = "https://res.cloudinary.com/demo/image/upload/v1234/media/canciones/portadas/xyz789.jpg";

        mockMvc.perform(delete("/api/multimedia")
                        .param("url", fileUrl)
                        .with(csrf()))
                .andExpect(status().is(403)); // 403

        verify(cloudinaryService, never()).eliminarArchivo(any());
    }
}