package com.ondra.contenidos.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import com.ondra.contenidos.exceptions.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para la gestión de archivos multimedia en Cloudinary.
 *
 * <p>Proporciona funcionalidades de subida, validación y eliminación de archivos
 * de audio y portadas para canciones y álbumes.</p>
 *
 * <p>Configuración de audio: formatos MP3, WAV, FLAC, M4A, OGG con tamaño máximo de 50MB.
 * Configuración de imágenes: formatos JPG, PNG, WEBP con tamaño máximo de 5MB y
 * transformación automática a 1000x1000px.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    @Value("${cloudinary.audio.max-size:52428800}")
    private long maxAudioSize;

    @Value("${cloudinary.image.max-size:5242880}")
    private long maxImageSize;

    /**
     * Resultado de subida de audio con metadata extraída.
     */
    @Data
    @AllArgsConstructor
    public static class AudioUploadResult {
        private String url;
        private Integer duracion;
        private String formato;
    }

    /**
     * Sube un archivo de audio a Cloudinary con validación y extracción de metadata.
     *
     * <p>Valida formato y tamaño, extrae la duración del audio y lo convierte automáticamente
     * a formato MP3 durante la subida.</p>
     *
     * @param file archivo de audio a subir
     * @param carpeta subcarpeta de destino dentro del folder principal
     * @return resultado con URL, duración en segundos y formato original
     * @throws NoFileProvidedException si no se proporciona archivo
     * @throws InvalidAudioFormatException si el formato no es válido
     * @throws AudioSizeExceededException si excede el tamaño máximo
     * @throws AudioUploadFailedException si falla la subida
     */
    public AudioUploadResult subirAudio(MultipartFile file, String carpeta) {
        log.debug("🎵 Iniciando subida de audio a carpeta: {}", carpeta);

        if (file == null || file.isEmpty()) {
            log.warn("⚠️ Intento de subir audio sin proporcionar archivo");
            throw new NoFileProvidedException("No se ha proporcionado ningún archivo");
        }

        if (!esAudioValido(file)) {
            log.warn("⚠️ Intento de subir archivo con formato de audio inválido: {}", file.getContentType());
            throw new InvalidAudioFormatException(
                    "El archivo debe ser un audio válido (MP3, WAV, FLAC, M4A, OGG)"
            );
        }

        if (!esTamanoAudioValido(file)) {
            log.warn("⚠️ Intento de subir audio que excede el tamaño máximo: {} bytes", file.getSize());
            throw new AudioSizeExceededException("El archivo de audio no puede superar los 50MB");
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("❌ Error al leer el archivo: {}", e.getMessage(), e);
            throw new AudioUploadFailedException("Error al leer el archivo de audio", e);
        }

        Integer duracion = calcularDuracionAudio(fileBytes, file.getOriginalFilename());
        String formato = extraerFormato(file);

        try {
            String publicId = generarPublicId();
            String folderPath = folder + "/" + carpeta;

            log.debug("📤 Subiendo audio con public_id: {} a carpeta: {} - Duración: {}s - Formato: {}",
                    publicId, folderPath, duracion, formato);

            Map uploadResult = cloudinary.uploader().upload(fileBytes,
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", folderPath,
                            "resource_type", "video",
                            "overwrite", true,
                            "format", "mp3"
                    ));

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("✅ Audio subido exitosamente a Cloudinary: {} - Duración: {}s",
                    secureUrl, duracion);

            return new AudioUploadResult(secureUrl, duracion, formato);

        } catch (IOException e) {
            log.error("❌ Error al subir audio a Cloudinary: {}", e.getMessage(), e);
            throw new AudioUploadFailedException("Error al subir el audio a Cloudinary", e);
        }
    }

    /**
     * Extrae el formato del archivo de audio basándose en su content type.
     *
     * @param file archivo multipart
     * @return formato del audio (mp3, wav, flac, m4a, ogg) o "unknown"
     */
    private String extraerFormato(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return "unknown";
        }

        if (contentType.contains("mpeg") || contentType.contains("mp3")) return "mp3";
        if (contentType.contains("wav")) return "wav";
        if (contentType.contains("flac")) return "flac";
        if (contentType.contains("m4a")) return "m4a";
        if (contentType.contains("ogg")) return "ogg";

        return "unknown";
    }

    /**
     * Valida que el archivo sea de audio con formato permitido.
     *
     * <p>Formatos permitidos: MP3, WAV, FLAC, M4A, OGG.</p>
     *
     * @param file archivo a validar
     * @return true si es un audio válido, false en caso contrario
     */
    public boolean esAudioValido(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.equals("audio/mpeg") ||
                contentType.equals("audio/mp3") ||
                contentType.equals("audio/wav") ||
                contentType.equals("audio/x-wav") ||
                contentType.equals("audio/flac") ||
                contentType.equals("audio/x-flac") ||
                contentType.equals("audio/m4a") ||
                contentType.equals("audio/x-m4a") ||
                contentType.equals("audio/ogg");
    }

    /**
     * Valida que el tamaño del archivo de audio no exceda el límite configurado.
     *
     * @param file archivo a validar
     * @return true si el tamaño es válido, false en caso contrario
     */
    public boolean esTamanoAudioValido(MultipartFile file) {
        return file != null && file.getSize() <= maxAudioSize;
    }

    /**
     * Sube una imagen de portada a Cloudinary con transformación automática.
     *
     * <p>Valida formato y tamaño, y aplica transformación automática a 1000x1000px
     * con recorte tipo fill y calidad automática.</p>
     *
     * @param file archivo de imagen a subir
     * @param carpeta subcarpeta de destino dentro del folder principal
     * @return URL pública de la imagen subida
     * @throws NoFileProvidedException si no se proporciona archivo
     * @throws InvalidImageFormatException si el formato no es válido
     * @throws ImageSizeExceededException si excede el tamaño máximo
     * @throws ImageUploadFailedException si falla la subida
     */
    public String subirPortada(MultipartFile file, String carpeta) {
        log.debug("🖼️ Iniciando subida de portada a carpeta: {}", carpeta);

        if (file == null || file.isEmpty()) {
            log.warn("⚠️ Intento de subir portada sin proporcionar archivo");
            throw new NoFileProvidedException("No se ha proporcionado ningún archivo");
        }

        if (!esImagenValida(file)) {
            log.warn("⚠️ Intento de subir archivo con formato inválido: {}", file.getContentType());
            throw new InvalidImageFormatException(
                    "El archivo debe ser una imagen válida (JPG, PNG, WEBP)"
            );
        }

        if (!esTamanoImagenValido(file)) {
            log.warn("⚠️ Intento de subir imagen que excede el tamaño máximo: {} bytes", file.getSize());
            throw new ImageSizeExceededException("La imagen no puede superar los 5MB");
        }

        try {
            String publicId = generarPublicId();
            String folderPath = folder + "/" + carpeta;

            log.debug("📤 Subiendo portada con public_id: {} a carpeta: {}", publicId, folderPath);

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", folderPath,
                            "resource_type", "image",
                            "overwrite", true,
                            "transformation", new com.cloudinary.Transformation()
                                    .width(1000).height(1000)
                                    .crop("fill")
                                    .quality("auto")
                    ));

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("✅ Portada subida exitosamente a Cloudinary: {}", secureUrl);
            return secureUrl;

        } catch (IOException e) {
            log.error("❌ Error al subir portada a Cloudinary: {}", e.getMessage(), e);
            throw new ImageUploadFailedException("Error al subir la portada a Cloudinary", e);
        }
    }

    /**
     * Valida que el archivo sea una imagen con formato permitido.
     *
     * <p>Formatos permitidos: JPG, PNG, WEBP.</p>
     *
     * @param file archivo a validar
     * @return true si es una imagen válida, false en caso contrario
     */
    public boolean esImagenValida(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/webp");
    }

    /**
     * Valida que el tamaño del archivo de imagen no exceda el límite configurado.
     *
     * @param file archivo a validar
     * @return true si el tamaño es válido, false en caso contrario
     */
    public boolean esTamanoImagenValido(MultipartFile file) {
        return file != null && file.getSize() <= maxImageSize;
    }

    /**
     * Elimina un archivo multimedia de Cloudinary mediante su URL.
     *
     * <p>Detecta automáticamente el tipo de recurso (audio como video o imagen)
     * basándose en la ruta del archivo.</p>
     *
     * @param fileUrl URL completa del archivo a eliminar
     * @throws FileDeletionFailedException si falla la eliminación
     */
    public void eliminarArchivo(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("⚠️ Se intentó eliminar un archivo con URL nula o vacía");
            return;
        }

        String publicId = extraerPublicId(fileUrl);
        if (publicId == null) {
            log.warn("⚠️ No se pudo extraer el public_id de la URL: {}", fileUrl);
            return;
        }

        try {
            log.debug("🗑️ Eliminando archivo de Cloudinary con public_id: {}", publicId);

            String resourceType = publicId.contains("/audio/") ? "video" : "image";

            Map result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType));
            String resultStatus = (String) result.get("result");

            if ("ok".equals(resultStatus)) {
                log.info("✅ Archivo eliminado de Cloudinary: {}", publicId);
            } else {
                log.warn("⚠️ Resultado inesperado al eliminar archivo: {} - Status: {}",
                        publicId, resultStatus);
            }
        } catch (IOException e) {
            log.error("❌ Error al eliminar archivo de Cloudinary: {}", e.getMessage(), e);
            throw new FileDeletionFailedException("Error al eliminar el archivo de Cloudinary", e);
        }
    }

    /**
     * Extrae el public_id de una URL de Cloudinary.
     *
     * <p>Ejemplo de URL: https://res.cloudinary.com/demo/image/upload/v1234567890/media/canciones/audio/abc123.mp3
     * retorna "media/canciones/audio/abc123".</p>
     *
     * @param fileUrl URL completa del archivo
     * @return public_id extraído o null si no se puede extraer
     */
    private String extraerPublicId(String fileUrl) {
        try {
            int uploadIndex = fileUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                log.warn("⚠️ URL no contiene '/upload/': {}", fileUrl);
                return null;
            }

            String afterUpload = fileUrl.substring(uploadIndex + 8);

            int versionEnd = afterUpload.indexOf("/");
            if (versionEnd == -1) {
                log.warn("⚠️ URL no tiene formato de versión correcto: {}", fileUrl);
                return null;
            }

            String pathWithExtension = afterUpload.substring(versionEnd + 1);

            int lastDot = pathWithExtension.lastIndexOf(".");
            String publicId = lastDot != -1
                    ? pathWithExtension.substring(0, lastDot)
                    : pathWithExtension;

            log.debug("🔍 Public ID extraído: {}", publicId);
            return publicId;

        } catch (Exception e) {
            log.error("❌ Error al extraer public_id de la URL: {}", fileUrl, e);
            return null;
        }
    }

    /**
     * Genera un identificador único para el archivo usando UUID.
     *
     * @return identificador único en formato UUID
     */
    private String generarPublicId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Elimina todos los archivos de una carpeta específica en Cloudinary.
     *
     * <p>Utilizado principalmente para limpieza de datos de seeding.
     * Esta operación es irreversible.</p>
     *
     * @param carpeta subcarpeta dentro del folder principal a limpiar
     * @param resourceType tipo de recurso: "image" o "video"
     * @return número de archivos eliminados
     */
    public int limpiarCarpeta(String carpeta, String resourceType) {
        String folderPath = folder + "/" + carpeta;
        int archivosEliminados = 0;

        try {
            log.info("🧹 Iniciando limpieza de la carpeta: {} (tipo: {})", folderPath, resourceType);

            ApiResponse result = cloudinary.api().resources(
                    ObjectUtils.asMap(
                            "type", "upload",
                            "prefix", folderPath,
                            "resource_type", resourceType,
                            "max_results", 500
                    ));

            List<Map> resources = (List<Map>) result.get("resources");

            if (resources == null || resources.isEmpty()) {
                log.info("ℹ️ No se encontraron archivos en la carpeta: {}", folderPath);
                return 0;
            }

            log.info("📂 Se encontraron {} archivos para eliminar", resources.size());

            for (Map resource : resources) {
                String publicId = (String) resource.get("public_id");
                try {
                    cloudinary.uploader().destroy(publicId,
                            ObjectUtils.asMap("resource_type", resourceType));
                    archivosEliminados++;
                    log.debug("🗑️ Archivo eliminado: {}", publicId);
                } catch (Exception e) {
                    log.warn("⚠️ No se pudo eliminar el archivo: {} - Error: {}",
                            publicId, e.getMessage());
                }
            }

            log.info("✅ Limpieza completada: {} archivos eliminados de {}",
                    archivosEliminados, folderPath);

        } catch (Exception e) {
            log.error("❌ Error durante la limpieza de la carpeta {}: {}",
                    folderPath, e.getMessage(), e);
        }

        return archivosEliminados;
    }

    /**
     * Calcula la duración de un archivo de audio en segundos usando JAudioTagger.
     *
     * <p>Crea un archivo temporal para procesar el audio y lo elimina tras la lectura.</p>
     *
     * @param fileBytes bytes del archivo de audio
     * @param filename nombre original del archivo
     * @return duración en segundos, o null si no se puede calcular
     */
    private Integer calcularDuracionAudio(byte[] fileBytes, String filename) {
        Path tempFile = null;
        try {
            String extension = getExtensionFromFilename(filename);
            tempFile = Files.createTempFile("audio_", extension);

            Files.write(tempFile, fileBytes);

            AudioFile audioFile = AudioFileIO.read(tempFile.toFile());
            AudioHeader audioHeader = audioFile.getAudioHeader();

            int duracion = audioHeader.getTrackLength();

            log.debug("⏱️ Duración del audio calculada con JAudioTagger: {} segundos", duracion);
            return duracion;

        } catch (Exception e) {
            log.warn("⚠️ No se pudo calcular la duración del audio: {}", e.getMessage());
            return null;
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("⚠️ No se pudo eliminar el archivo temporal: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Extrae la extensión del nombre del archivo.
     *
     * @param filename nombre del archivo
     * @return extensión con punto (ej: ".mp3") o ".tmp" si no se encuentra
     */
    private String getExtensionFromFilename(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return ".tmp";
    }
}