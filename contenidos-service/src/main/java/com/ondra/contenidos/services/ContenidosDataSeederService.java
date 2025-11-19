package com.ondra.contenidos.services;

import com.ondra.contenidos.data.ArtistasContenidoData;
import com.ondra.contenidos.data.ArtistasContenidoData.*;
import com.ondra.contenidos.models.dao.Album;
import com.ondra.contenidos.models.dao.AlbumCancion;
import com.ondra.contenidos.models.dao.Cancion;
import com.ondra.contenidos.models.enums.GeneroMusical;
import com.ondra.contenidos.repositories.AlbumCancionRepository;
import com.ondra.contenidos.repositories.AlbumRepository;
import com.ondra.contenidos.repositories.CancionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * Servicio para poblar la base de datos con contenido musical predefinido.
 *
 * <p>Este servicio implementa {@link CommandLineRunner} para ejecutarse automáticamente
 * al iniciar la aplicación en perfil de desarrollo. Genera datos de prueba incluyendo:</p>
 * <ul>
 *   <li>Álbumes con sus canciones asociadas</li>
 *   <li>Singles independientes</li>
 *   <li>Relaciones álbum-canción con orden de pistas</li>
 *   <li>Archivos de audio subidos a Cloudinary</li>
 *   <li>Imágenes de portada para álbumes y singles</li>
 *   <li>Reproducciones aleatorias con distribución realista</li>
 * </ul>
 *
 * <p><b>Configuración:</b> El servicio se activa mediante la propiedad {@code seed.enabled=true} en application.properties.
 * Solo se ejecuta en el perfil {@code dev} de Spring.</p>
 *
 * <p><b>Estructura de archivos:</b> Los archivos deben ubicarse en {@code src/main/resources/seed-data/artists/}
 * organizados por carpeta de artista, con subcarpetas {@code albums} y {@code singles}.</p>
 *
 * @see CommandLineRunner
 * @see CloudinaryService
 * @see ArtistasContenidoData
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Profile("dev")
public class ContenidosDataSeederService implements CommandLineRunner {

    private final CancionRepository cancionRepository;
    private final AlbumRepository albumRepository;
    private final AlbumCancionRepository albumCancionRepository;
    private final CloudinaryService cloudinaryService;

    /**
     * Indica si el servicio de seeding está habilitado.
     * Se configura mediante la propiedad {@code seed.enabled} en application.properties.
     */
    @Value("${seed.enabled:false}")
    private boolean seedEnabled;

    private final Random random = new Random();

    /**
     * Ruta base en el classpath donde se encuentran las carpetas de artistas.
     * Estructura esperada: seed-data/artists/{carpeta-artista}/albums|singles
     */
    private static final String SEED_DATA_CLASSPATH = "seed-data/artists";

    /**
     * Método principal que se ejecuta al iniciar la aplicación.
     * Verifica si el seeding está habilitado y procede a poblar el contenido musical.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("⏭️  Data seeding deshabilitado. Para habilitar: seed.enabled=true en application.properties");
            return;
        }

        log.info("🚀 Iniciando población de contenido musical...");

        try {
            limpiarCloudinary();

            int totalCanciones = 0;
            int totalAlbumes = 0;
            int totalSingles = 0;

            for (ArtistaContenido artista : ArtistasContenidoData.ARTISTAS) {
                log.info("🎤 Procesando artista ID {}: {}", artista.getIdArtista(), artista.getCarpeta());

                String artistPath = SEED_DATA_CLASSPATH + "/" + artista.getCarpeta();

                if (!existeEnClasspath(artistPath)) {
                    log.warn("⚠️  Carpeta no encontrada en classpath: {}", artistPath);
                    continue;
                }

                for (AlbumInfo albumInfo : artista.getAlbumes()) {
                    try {
                        Album album = crearAlbum(artista, albumInfo);
                        totalAlbumes++;
                        totalCanciones += albumInfo.getCanciones().size();
                        log.info("✅ Álbum '{}' creado con {} canciones",
                                album.getTituloAlbum(), albumInfo.getCanciones().size());
                    } catch (Exception e) {
                        log.error("❌ Error en álbum '{}': {}", albumInfo.getTitulo(), e.getMessage());
                    }
                }

                for (SingleInfo singleInfo : artista.getSingles()) {
                    try {
                        crearSingle(artista, singleInfo);
                        totalSingles++;
                        totalCanciones++;
                        log.info("✅ Single '{}' creado", singleInfo.getTitulo());
                    } catch (Exception e) {
                        log.error("❌ Error en single '{}': {}", singleInfo.getTitulo(), e.getMessage());
                    }
                }

                log.info("✅ Artista {} completado", artista.getCarpeta());
            }

            log.info("✅ Población completada exitosamente");
            log.info("📊 RESUMEN DE POBLACIÓN:");
            log.info("   • Artistas procesados: {}", ArtistasContenidoData.ARTISTAS.size());
            log.info("   • Álbumes creados: {}", totalAlbumes);
            log.info("   • Singles creados: {}", totalSingles);
            log.info("   • Canciones totales: {}", totalCanciones);

        } catch (Exception e) {
            log.error("❌ Error durante la población de datos: {}", e.getMessage(), e);
        }
    }

    /**
     * Limpia las carpetas de contenido musical en Cloudinary antes de iniciar el seeding.
     * Elimina audios de canciones, portadas de canciones y portadas de álbumes.
     */
    private void limpiarCloudinary() {
        log.info("🧹 Limpiando carpetas de contenido en Cloudinary...");

        try {
            int audio = cloudinaryService.limpiarCarpeta("canciones/audio", "video");
            int portadas = cloudinaryService.limpiarCarpeta("canciones/portadas", "image");
            int albumPortadas = cloudinaryService.limpiarCarpeta("albumes/portadas", "image");

            log.info("✅ Carpetas limpiadas: {} audios, {} portadas de canciones, {} portadas de álbumes",
                    audio, portadas, albumPortadas);
        } catch (Exception e) {
            log.warn("⚠️  Error limpiando Cloudinary: {}", e.getMessage());
        }
    }

    /**
     * Verifica si una ruta existe en el classpath del proyecto.
     *
     * @param path ruta relativa al classpath a verificar
     * @return true si la ruta contiene al menos un recurso, false en caso contrario
     */
    private boolean existeEnClasspath(String path) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:" + path + "/**");
            return resources.length > 0;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Crea un álbum completo con todas sus canciones y relaciones.
     * Sube la portada del álbum y procesa cada canción individualmente.
     *
     * @param artista información del artista propietario
     * @param albumInfo información del álbum a crear
     * @return entidad {@link Album} persistida con sus canciones
     * @throws IOException si ocurre error al leer o subir archivos
     */
    private Album crearAlbum(ArtistaContenido artista, AlbumInfo albumInfo) throws IOException {
        String albumFolder = normalizarNombre(albumInfo.getTitulo());
        String albumPath = SEED_DATA_CLASSPATH + "/" + artista.getCarpeta() + "/albums/" + albumFolder;

        Resource coverResource = buscarArchivoEnClasspath(albumPath, "cover");
        String urlPortada = subirImagen(coverResource, "albumes/portadas");

        Album album = Album.builder()
                .tituloAlbum(albumInfo.getTitulo())
                .idArtista(artista.getIdArtista())
                .genero(GeneroMusical.fromId(albumInfo.getIdGenero()))
                .precioAlbum(albumInfo.getPrecio())
                .urlPortada(urlPortada)
                .descripcion(albumInfo.getDescripcion())
                .fechaPublicacion(LocalDateTime.of(albumInfo.getAnio(), 1, 1, 0, 0))
                .build();

        album = albumRepository.save(album);
        log.debug("  ✓ Álbum '{}' guardado en base de datos", album.getTituloAlbum());

        for (CancionInfo cancionInfo : albumInfo.getCanciones()) {
            try {
                Cancion cancion = crearCancion(artista, cancionInfo, albumPath, urlPortada);

                AlbumCancion albumCancion = AlbumCancion.builder()
                        .album(album)
                        .cancion(cancion)
                        .numeroPista(cancionInfo.getNumeroPista())
                        .build();

                albumCancionRepository.save(albumCancion);
                log.debug("  ✓ Pista {}: {}", cancionInfo.getNumeroPista(), cancion.getTituloCancion());

            } catch (Exception e) {
                log.warn("  ⚠️  Error en canción '{}': {}", cancionInfo.getTitulo(), e.getMessage());
            }
        }

        return album;
    }

    /**
     * Crea una canción individual (single) con su audio y portada propios.
     *
     * @param artista información del artista propietario
     * @param singleInfo información del single a crear
     * @return entidad {@link Cancion} persistida
     * @throws IOException si ocurre error al leer o subir archivos
     */
    private Cancion crearSingle(ArtistaContenido artista, SingleInfo singleInfo) throws IOException {
        String singleFolder = normalizarNombre(singleInfo.getTitulo());
        String singlePath = SEED_DATA_CLASSPATH + "/" + artista.getCarpeta() + "/singles/" + singleFolder;

        Resource audioResource = buscarArchivoEnClasspath(singlePath, normalizarNombre(singleInfo.getTitulo()));
        Resource coverResource = buscarArchivoEnClasspath(singlePath, "cover");

        CloudinaryService.AudioUploadResult audioResult = subirAudio(audioResource, "canciones/audio");
        String urlPortada = subirImagen(coverResource, "canciones/portadas");

        Cancion cancion = Cancion.builder()
                .tituloCancion(singleInfo.getTitulo())
                .idArtista(artista.getIdArtista())
                .genero(GeneroMusical.fromId(singleInfo.getIdGenero()))
                .precioCancion(singleInfo.getPrecio())
                .duracionSegundos(audioResult.getDuracion() != null ? audioResult.getDuracion() : 180)
                .urlPortada(urlPortada)
                .urlAudio(audioResult.getUrl())
                .descripcion(singleInfo.getDescripcion())
                .reproducciones(generarReproduccionesAleatorias())
                .fechaPublicacion(LocalDateTime.now().minusDays(new Random().nextInt(365)))
                .build();

        return cancionRepository.save(cancion);
    }

    /**
     * Crea una canción perteneciente a un álbum.
     * Utiliza la portada del álbum y busca el archivo de audio por número de pista.
     *
     * @param artista información del artista propietario
     * @param cancionInfo información de la canción a crear
     * @param albumPath ruta del directorio del álbum en el classpath
     * @param urlPortadaAlbum URL de la portada del álbum ya subida
     * @return entidad {@link Cancion} persistida
     * @throws IOException si ocurre error al leer o subir el archivo de audio
     */
    private Cancion crearCancion(ArtistaContenido artista, CancionInfo cancionInfo,
                                 String albumPath, String urlPortadaAlbum) throws IOException {

        String audioFileName = String.format("%02d_%s",
                cancionInfo.getNumeroPista(),
                normalizarNombre(cancionInfo.getTitulo()));

        Resource audioResource = buscarArchivoEnClasspath(albumPath, audioFileName);
        CloudinaryService.AudioUploadResult audioResult = subirAudio(audioResource, "canciones/audio");

        Cancion cancion = Cancion.builder()
                .tituloCancion(cancionInfo.getTitulo())
                .idArtista(artista.getIdArtista())
                .genero(GeneroMusical.fromId(cancionInfo.getIdGenero()))
                .precioCancion(cancionInfo.getPrecio())
                .duracionSegundos(audioResult.getDuracion() != null ? audioResult.getDuracion() : 180)
                .urlPortada(urlPortadaAlbum)
                .urlAudio(audioResult.getUrl())
                .descripcion("Pista " + cancionInfo.getNumeroPista())
                .reproducciones(generarReproduccionesAleatorias())
                .fechaPublicacion(LocalDateTime.now().minusDays(new Random().nextInt(365)))
                .build();

        return cancionRepository.save(cancion);
    }

    /**
     * Busca un archivo en el classpath basándose en el nombre base.
     * Realiza búsqueda flexible que permite coincidencias parciales.
     *
     * @param directory directorio donde buscar en el classpath
     * @param baseName nombre base del archivo a buscar (sin extensión)
     * @return recurso encontrado
     * @throws IOException si no se encuentra ningún archivo coincidente
     */
    private Resource buscarArchivoEnClasspath(String directory, String baseName) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String baseNameLower = baseName.toLowerCase();

        Resource[] resources = resolver.getResources("classpath:" + directory + "/*");

        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            if (fileName != null) {
                String fileNameLower = fileName.toLowerCase();
                if (fileNameLower.startsWith(baseNameLower) || fileNameLower.contains(baseNameLower)) {
                    return resource;
                }
            }
        }

        throw new IOException("Archivo no encontrado: " + baseName + " en " + directory);
    }

    /**
     * Normaliza un nombre eliminando acentos, caracteres especiales y espacios.
     * Convierte a minúsculas y mantiene solo caracteres alfanuméricos.
     *
     * @param nombre nombre a normalizar
     * @return nombre normalizado en minúsculas sin caracteres especiales
     */
    private String normalizarNombre(String nombre) {
        return nombre.toLowerCase()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("[ñ]", "n")
                .replaceAll("[^a-z0-9]", "");
    }

    /**
     * Sube un archivo de audio a Cloudinary en la carpeta especificada.
     *
     * @param resource recurso de audio a subir
     * @param folder carpeta destino en Cloudinary
     * @return resultado con URL y duración del audio
     * @throws IOException si ocurre error durante la subida
     */
    private CloudinaryService.AudioUploadResult subirAudio(Resource resource, String folder) throws IOException {
        return cloudinaryService.subirAudio(convertirAMultipartFile(resource), folder);
    }

    /**
     * Sube una imagen de portada a Cloudinary en la carpeta especificada.
     *
     * @param resource recurso de imagen a subir
     * @param folder carpeta destino en Cloudinary
     * @return URL pública de la imagen subida
     * @throws IOException si ocurre error durante la subida
     */
    private String subirImagen(Resource resource, String folder) throws IOException {
        return cloudinaryService.subirPortada(convertirAMultipartFile(resource), folder);
    }

    /**
     * Genera un número aleatorio de reproducciones con distribución realista.
     * La distribución simula patrones reales de popularidad musical:
     * <ul>
     *   <li>40% entre 1.000 y 10.000 reproducciones (canciones nuevas o menos populares)</li>
     *   <li>35% entre 10.000 y 100.000 reproducciones (popularidad media)</li>
     *   <li>20% entre 100.000 y 1.000.000 reproducciones (éxitos)</li>
     *   <li>5% entre 1.000.000 y 10.000.000 reproducciones (megahits)</li>
     * </ul>
     *
     * @return número de reproducciones generado aleatoriamente
     */
    private Long generarReproduccionesAleatorias() {
        double probabilidad = random.nextDouble();

        if (probabilidad < 0.40) {
            return 1_000L + random.nextInt(9_000);
        } else if (probabilidad < 0.75) {
            return 10_000L + random.nextInt(90_000);
        } else if (probabilidad < 0.95) {
            return 100_000L + random.nextInt(900_000);
        } else {
            return 1_000_000L + random.nextInt(9_000_000);
        }
    }

    /**
     * Convierte un recurso de Spring a MultipartFile para poder ser procesado por servicios externos.
     * Detecta automáticamente el tipo de contenido basándose en la extensión del archivo.
     *
     * @param resource recurso a convertir
     * @return implementación de MultipartFile con el contenido del recurso
     * @throws IOException si ocurre error al leer el recurso
     */
    private MultipartFile convertirAMultipartFile(Resource resource) throws IOException {
        byte[] content = resource.getContentAsByteArray();
        String fileName = resource.getFilename();

        String contentType = "application/octet-stream";
        if (fileName != null) {
            String fileNameLower = fileName.toLowerCase();
            if (fileNameLower.endsWith(".mp3")) {
                contentType = "audio/mpeg";
            } else if (fileNameLower.endsWith(".jpeg") || fileNameLower.endsWith(".jpg")) {
                contentType = "image/jpeg";
            } else if (fileNameLower.endsWith(".png")) {
                contentType = "image/png";
            }
        }

        final String finalContentType = contentType;
        final String finalFileName = fileName;

        return new MultipartFile() {
            @Override
            public String getName() {
                return finalFileName;
            }

            @Override
            public String getOriginalFilename() {
                return finalFileName;
            }

            @Override
            public String getContentType() {
                return finalContentType;
            }

            @Override
            public boolean isEmpty() {
                return content.length == 0;
            }

            @Override
            public long getSize() {
                return content.length;
            }

            @Override
            public byte[] getBytes() {
                return content;
            }

            @Override
            public java.io.InputStream getInputStream() throws IOException {
                return resource.getInputStream();
            }

            @Override
            public void transferTo(File dest) throws IOException {
                Files.write(dest.toPath(), content);
            }
        };
    }
}