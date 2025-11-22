package com.ondra.contenidos.services;

import com.ondra.contenidos.data.ArtistasContenidoData;
import com.ondra.contenidos.models.dao.*;
import com.ondra.contenidos.models.enums.GeneroMusical;
import com.ondra.contenidos.models.enums.TipoContenido;
import com.ondra.contenidos.models.enums.TipoUsuario;
import com.ondra.contenidos.repositories.*;
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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de población inicial de la base de datos con contenido musical y datos de usuarios.
 *
 * <p>Crea canciones, álbumes, favoritos, compras, valoraciones y comentarios para usuarios
 * y artistas. Mantiene coherencia con los IDs del microservicio de usuarios.</p>
 *
 * <p>Mapeo de usuarios:</p>
 * <ul>
 *   <li>Usuarios normales: IDs de usuario 1-5</li>
 *   <li>Artistas: IDs de artista 1-6, IDs de usuario 6-11</li>
 * </ul>
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
    private final FavoritoRepository favoritoRepository;
    private final CompraRepository compraRepository;
    private final ComentarioRepository comentarioRepository;
    private final ValoracionRepository valoracionRepository;
    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;

    @Value("${seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${seed.user-data.enabled:true}")
    private boolean seedUserDataEnabled;

    @Value("${seed.artist-interactions.enabled:false}")
    private boolean seedArtistInteractionsEnabled;

    private final Random random = new Random();
    private static final String SEED_DATA_CLASSPATH = "seed-data/artists";

    private static final List<Long> USUARIOS_NORMALES = Arrays.asList(1L, 2L, 3L, 4L, 5L);
    private static final List<Long> USUARIOS_ARTISTAS = Arrays.asList(6L, 7L, 8L, 9L, 10L, 11L);

    private static final Map<Long, String> NOMBRES_USUARIOS = Map.of(
            1L, "Ana García",
            2L, "Carlos Martínez",
            3L, "Laura Rodríguez",
            4L, "Miguel Fernández",
            5L, "Sara González"
    );

    private static final Map<String, Long> CARPETA_A_ID_ARTISTA = Map.of(
            "duki", 1L,
            "aitana", 2L,
            "avicii", 3L,
            "daddyyankee", 4L,
            "rosalia", 5L,
            "sanguijuelasdelguadiana", 6L
    );

    private static final Map<Long, Long> ID_ARTISTA_A_ID_USUARIO = Map.of(
            1L, 6L,   // Duki
            2L, 7L,   // Aitana
            3L, 8L,   // Avicii
            4L, 9L,   // Daddy Yankee
            5L, 10L,  // Rosalía
            6L, 11L   // Sanguijuelas del Guadiana
    );

    private static final Map<Long, String> NOMBRES_ARTISTAS = Map.of(
            6L, "Duki",
            7L, "Aitana",
            8L, "Avicii",
            9L, "Daddy Yankee",
            10L, "Rosalía",
            11L, "Sanguijuelas del Guadiana"
    );

    private final List<Long> cancionesCreadas = new ArrayList<>();
    private final List<Long> albumesCreados = new ArrayList<>();
    private final Map<Long, List<Long>> cancionesPorArtista = new HashMap<>();

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("⏭️  Data seeding deshabilitado");
            return;
        }

        log.info("🚀 Iniciando población de contenido musical...");
        log.info("📋 Usuarios normales: IDs 1-5 | Artistas: IDs artista 1-6, IDs usuario 6-11");

        try {
            limpiarCloudinary();

            int totalCanciones = 0;
            int totalAlbumes = 0;
            int totalSingles = 0;

            for (ArtistasContenidoData.ArtistaContenido artista : ArtistasContenidoData.ARTISTAS) {
                Long idArtista = CARPETA_A_ID_ARTISTA.get(artista.getCarpeta());

                if (idArtista == null) {
                    log.warn("⚠️  Artista sin mapeo: {}", artista.getCarpeta());
                    continue;
                }

                Long idUsuarioArtista = ID_ARTISTA_A_ID_USUARIO.get(idArtista);
                cancionesPorArtista.put(idArtista, new ArrayList<>());

                log.info("🎤 Procesando '{}' - ID Artista: {}, ID Usuario: {}",
                        artista.getCarpeta(), idArtista, idUsuarioArtista);

                String artistPath = SEED_DATA_CLASSPATH + "/" + artista.getCarpeta();

                if (!existeEnClasspath(artistPath)) {
                    log.warn("⚠️  Carpeta no encontrada: {}", artistPath);
                    continue;
                }

                for (ArtistasContenidoData.AlbumInfo albumInfo : artista.getAlbumes()) {
                    try {
                        Album album = crearAlbum(idArtista, artista.getCarpeta(), albumInfo);
                        albumesCreados.add(album.getIdAlbum());
                        totalAlbumes++;
                        totalCanciones += albumInfo.getCanciones().size();
                        log.info("✅ Álbum '{}' con {} canciones", album.getTituloAlbum(),
                                albumInfo.getCanciones().size());
                    } catch (Exception e) {
                        log.error("❌ Error en álbum '{}': {}", albumInfo.getTitulo(), e.getMessage());
                    }
                }

                for (ArtistasContenidoData.SingleInfo singleInfo : artista.getSingles()) {
                    try {
                        Cancion cancion = crearSingle(idArtista, artista.getCarpeta(), singleInfo);
                        cancionesCreadas.add(cancion.getIdCancion());
                        cancionesPorArtista.get(idArtista).add(cancion.getIdCancion());
                        totalSingles++;
                        totalCanciones++;
                        log.info("✅ Single '{}'", singleInfo.getTitulo());
                    } catch (Exception e) {
                        log.error("❌ Error en single '{}': {}", singleInfo.getTitulo(), e.getMessage());
                    }
                }
            }

            if (seedUserDataEnabled) {
                log.info("👥 Poblando datos de usuarios normales...");
                poblarDatosUsuarios();
            }

            if (seedArtistInteractionsEnabled) {
                log.info("🎤 Poblando interacciones de artistas...");
                poblarInteraccionesArtistas();
            }

            log.info("✅ Población completada");
            imprimirResumen(totalAlbumes, totalSingles, totalCanciones);

        } catch (Exception e) {
            log.error("❌ Error durante población: {}", e.getMessage(), e);
        }
    }

    /**
     * Crea datos de interacción para usuarios normales.
     */
    private void poblarDatosUsuarios() {
        int totalFavoritos = 0;
        int totalCompras = 0;
        int totalComentarios = 0;
        int totalValoraciones = 0;
        int totalCarrito = 0;

        for (Long idUsuario : USUARIOS_NORMALES) {
            try {
                Set<Long> cancionesFavoritas = seleccionarAleatorio(cancionesCreadas, 3, 8);
                Set<Long> albumesFavoritos = seleccionarAleatorio(albumesCreados, 2, 5);
                Set<Long> cancionesCompradas = seleccionarAleatorio(cancionesCreadas, 5, 15);
                Set<Long> albumesComprados = seleccionarAleatorio(albumesCreados, 1, 4);
                Set<Long> cancionesComentadas = seleccionarAleatorio(cancionesCreadas, 2, 6);
                Set<Long> albumesComentados = seleccionarAleatorio(albumesCreados, 1, 3);
                Set<Long> cancionesValoradas = seleccionarAleatorio(cancionesCreadas, 4, 10);
                Set<Long> albumesValorados = seleccionarAleatorio(albumesCreados, 2, 4);

                Set<Long> cancionesCarrito = seleccionarAleatorio(
                        cancionesCreadas.stream()
                                .filter(id -> !cancionesCompradas.contains(id))
                                .collect(Collectors.toList()),
                        0, 3
                );
                Set<Long> albumesCarrito = seleccionarAleatorio(
                        albumesCreados.stream()
                                .filter(id -> !albumesComprados.contains(id))
                                .collect(Collectors.toList()),
                        0, 2
                );

                totalFavoritos += crearFavoritos(idUsuario, cancionesFavoritas, albumesFavoritos);
                totalCompras += crearCompras(idUsuario, cancionesCompradas, albumesComprados);
                totalComentarios += crearComentarios(idUsuario, TipoUsuario.USUARIO,
                        NOMBRES_USUARIOS.get(idUsuario), cancionesComentadas, albumesComentados);
                totalValoraciones += crearValoraciones(idUsuario, TipoUsuario.USUARIO,
                        NOMBRES_USUARIOS.get(idUsuario), cancionesValoradas, albumesValorados);
                totalCarrito += crearCarrito(idUsuario, cancionesCarrito, albumesCarrito);

                log.info("✅ Usuario {} completado", NOMBRES_USUARIOS.get(idUsuario));

            } catch (Exception e) {
                log.error("❌ Error en usuario {}: {}", idUsuario, e.getMessage());
            }
        }

        log.info("📊 Usuarios: {} favoritos, {} compras, {} comentarios, {} valoraciones, {} items carrito",
                totalFavoritos, totalCompras, totalComentarios, totalValoraciones, totalCarrito);
    }

    /**
     * Crea interacciones de artistas sobre contenido de otros artistas.
     */
    private void poblarInteraccionesArtistas() {
        int totalComentarios = 0;
        int totalValoraciones = 0;

        for (Long idUsuarioArtista : USUARIOS_ARTISTAS) {
            try {
                Long idArtista = ID_ARTISTA_A_ID_USUARIO.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(idUsuarioArtista))
                        .findFirst()
                        .map(Map.Entry::getKey)
                        .orElse(null);

                if (idArtista == null) continue;

                List<Long> cancionesOtrosArtistas = cancionesCreadas.stream()
                        .filter(id -> !cancionesPorArtista.getOrDefault(idArtista, Collections.emptyList()).contains(id))
                        .collect(Collectors.toList());

                Set<Long> cancionesComentadas = seleccionarAleatorio(cancionesOtrosArtistas, 1, 3);
                Set<Long> albumesComentados = seleccionarAleatorio(albumesCreados, 0, 2);
                Set<Long> cancionesValoradas = seleccionarAleatorio(cancionesOtrosArtistas, 2, 5);
                Set<Long> albumesValorados = seleccionarAleatorio(albumesCreados, 1, 3);

                String nombreArtista = NOMBRES_ARTISTAS.get(idUsuarioArtista);

                totalComentarios += crearComentarios(idUsuarioArtista, TipoUsuario.ARTISTA,
                        nombreArtista, cancionesComentadas, albumesComentados);
                totalValoraciones += crearValoraciones(idUsuarioArtista, TipoUsuario.ARTISTA,
                        nombreArtista, cancionesValoradas, albumesValorados);

                log.info("✅ Artista {} completado", nombreArtista);

            } catch (Exception e) {
                log.error("❌ Error en artista {}: {}", idUsuarioArtista, e.getMessage());
            }
        }

        log.info("📊 Artistas: {} comentarios, {} valoraciones", totalComentarios, totalValoraciones);
    }

    /**
     * Crea favoritos para un usuario.
     *
     * @param idUsuario ID del usuario
     * @param cancionesIds IDs de canciones favoritas
     * @param albumesIds IDs de álbumes favoritos
     * @return número de favoritos creados
     */
    private int crearFavoritos(Long idUsuario, Set<Long> cancionesIds, Set<Long> albumesIds) {
        int count = 0;

        for (Long idCancion : cancionesIds) {
            Optional<Cancion> cancionOpt = cancionRepository.findById(idCancion);
            if (cancionOpt.isPresent()) {
                Favorito favorito = Favorito.builder()
                        .idUsuario(idUsuario)
                        .tipoContenido(TipoContenido.CANCION)
                        .cancion(cancionOpt.get())
                        .fechaAgregado(generarFechaAleatoria())
                        .build();
                favoritoRepository.save(favorito);
                count++;
            }
        }

        for (Long idAlbum : albumesIds) {
            Optional<Album> albumOpt = albumRepository.findById(idAlbum);
            if (albumOpt.isPresent()) {
                Favorito favorito = Favorito.builder()
                        .idUsuario(idUsuario)
                        .tipoContenido(TipoContenido.ALBUM)
                        .album(albumOpt.get())
                        .fechaAgregado(generarFechaAleatoria())
                        .build();
                favoritoRepository.save(favorito);
                count++;
            }
        }

        return count;
    }

    /**
     * Crea compras para un usuario.
     *
     * @param idUsuario ID del usuario
     * @param cancionesIds IDs de canciones compradas
     * @param albumesIds IDs de álbumes comprados
     * @return número de compras creadas
     */
    private int crearCompras(Long idUsuario, Set<Long> cancionesIds, Set<Long> albumesIds) {
        int count = 0;

        for (Long idCancion : cancionesIds) {
            Optional<Cancion> cancionOpt = cancionRepository.findById(idCancion);
            if (cancionOpt.isPresent()) {
                Cancion cancion = cancionOpt.get();
                Compra compra = Compra.builder()
                        .idUsuario(idUsuario)
                        .tipoContenido(TipoContenido.CANCION)
                        .cancion(cancion)
                        .precioPagado(BigDecimal.valueOf(cancion.getPrecioCancion()))
                        .fechaCompra(generarFechaAleatoria())
                        .build();
                compraRepository.save(compra);
                count++;
            }
        }

        for (Long idAlbum : albumesIds) {
            Optional<Album> albumOpt = albumRepository.findById(idAlbum);
            if (albumOpt.isPresent()) {
                Album album = albumOpt.get();
                Compra compra = Compra.builder()
                        .idUsuario(idUsuario)
                        .tipoContenido(TipoContenido.ALBUM)
                        .album(album)
                        .precioPagado(BigDecimal.valueOf(album.getPrecioAlbum()))
                        .fechaCompra(generarFechaAleatoria())
                        .build();
                compraRepository.save(compra);
                count++;
            }
        }

        return count;
    }

    /**
     * Crea comentarios de usuarios o artistas sobre contenido musical.
     *
     * @param idUsuario ID del usuario o artista
     * @param tipoUsuario tipo de usuario que comenta
     * @param nombreUsuario nombre para mostrar en el comentario
     * @param cancionesIds IDs de canciones a comentar
     * @param albumesIds IDs de álbumes a comentar
     * @return número de comentarios creados
     */
    private int crearComentarios(Long idUsuario, TipoUsuario tipoUsuario, String nombreUsuario,
                                 Set<Long> cancionesIds, Set<Long> albumesIds) {
        int count = 0;

        String[] comentariosUsuarios = {
                "¡Me encanta esta canción! La escucho todos los días.",
                "Increíble trabajo, definitivamente uno de mis favoritos.",
                "La mejor del álbum sin duda.",
                "No me canso de escucharla, brutal.",
                "Tiene un ritmo increíble, perfecta para el gym.",
                "Las letras son muy profundas, me identifico mucho.",
                "Obra maestra, nada más que decir.",
                "Esperaba más, pero está bien.",
                "No es mi estilo pero respeto el trabajo.",
                "Muy buena producción, se nota la calidad."
        };

        String[] comentariosArtistas = {
                "Gran trabajo, se nota el esfuerzo en la producción.",
                "Respeto mucho tu arte, sigue así.",
                "Me inspira tu forma de crear música.",
                "Colaboraríamos bien juntos, excelente track.",
                "La mezcla está increíble, ¿qué setup usaste?",
                "Brutal hermano, esto es de otro nivel.",
                "Se nota la evolución, felicidades.",
                "Esto es lo que necesita la escena, fuego."
        };

        String[] comentarios = tipoUsuario == TipoUsuario.ARTISTA ? comentariosArtistas : comentariosUsuarios;

        for (Long idCancion : cancionesIds) {
            Optional<Cancion> cancionOpt = cancionRepository.findById(idCancion);
            if (cancionOpt.isPresent()) {
                String texto = comentarios[random.nextInt(comentarios.length)];
                Comentario comentario = Comentario.builder()
                        .idUsuario(idUsuario)
                        .tipoUsuario(tipoUsuario)
                        .nombreUsuario(nombreUsuario)
                        .tipoContenido(TipoContenido.CANCION)
                        .cancion(cancionOpt.get())
                        .contenido(texto)
                        .fechaPublicacion(generarFechaAleatoria())
                        .build();

                if (random.nextDouble() < 0.2) {
                    comentario.setFechaUltimaEdicion(comentario.getFechaPublicacion().plusHours(random.nextInt(48)));
                }

                comentarioRepository.save(comentario);
                count++;
            }
        }

        for (Long idAlbum : albumesIds) {
            Optional<Album> albumOpt = albumRepository.findById(idAlbum);
            if (albumOpt.isPresent()) {
                String texto = comentarios[random.nextInt(comentarios.length)];
                Comentario comentario = Comentario.builder()
                        .idUsuario(idUsuario)
                        .tipoUsuario(tipoUsuario)
                        .nombreUsuario(nombreUsuario)
                        .tipoContenido(TipoContenido.ALBUM)
                        .album(albumOpt.get())
                        .contenido(texto)
                        .fechaPublicacion(generarFechaAleatoria())
                        .build();

                if (random.nextDouble() < 0.2) {
                    comentario.setFechaUltimaEdicion(comentario.getFechaPublicacion().plusHours(random.nextInt(48)));
                }

                comentarioRepository.save(comentario);
                count++;
            }
        }

        return count;
    }

    /**
     * Crea valoraciones de usuarios o artistas sobre contenido musical.
     *
     * <p>Los artistas tienden a dar valoraciones más altas que los usuarios normales.</p>
     *
     * @param idUsuario ID del usuario o artista
     * @param tipoUsuario tipo de usuario que valora
     * @param nombreUsuario nombre para mostrar en la valoración
     * @param cancionesIds IDs de canciones a valorar
     * @param albumesIds IDs de álbumes a valorar
     * @return número de valoraciones creadas
     */
    private int crearValoraciones(Long idUsuario, TipoUsuario tipoUsuario, String nombreUsuario,
                                  Set<Long> cancionesIds, Set<Long> albumesIds) {
        int count = 0;

        for (Long idCancion : cancionesIds) {
            Optional<Cancion> cancionOpt = cancionRepository.findById(idCancion);
            if (cancionOpt.isPresent()) {
                int puntuacion = generarPuntuacion(tipoUsuario);

                Valoracion valoracion = Valoracion.builder()
                        .idUsuario(idUsuario)
                        .tipoUsuario(tipoUsuario)
                        .nombreUsuario(nombreUsuario)
                        .tipoContenido(TipoContenido.CANCION)
                        .cancion(cancionOpt.get())
                        .valor(puntuacion)
                        .fechaValoracion(generarFechaAleatoria())
                        .build();
                valoracionRepository.save(valoracion);
                count++;
            }
        }

        for (Long idAlbum : albumesIds) {
            Optional<Album> albumOpt = albumRepository.findById(idAlbum);
            if (albumOpt.isPresent()) {
                int puntuacion = generarPuntuacion(tipoUsuario);

                Valoracion valoracion = Valoracion.builder()
                        .idUsuario(idUsuario)
                        .tipoUsuario(tipoUsuario)
                        .nombreUsuario(nombreUsuario)
                        .tipoContenido(TipoContenido.ALBUM)
                        .album(albumOpt.get())
                        .valor(puntuacion)
                        .fechaValoracion(generarFechaAleatoria())
                        .build();
                valoracionRepository.save(valoracion);
                count++;
            }
        }

        return count;
    }

    /**
     * Genera una puntuación según el tipo de usuario.
     *
     * @param tipoUsuario tipo de usuario
     * @return puntuación de 1 a 5
     */
    private int generarPuntuacion(TipoUsuario tipoUsuario) {
        double rand = random.nextDouble();

        if (tipoUsuario == TipoUsuario.ARTISTA) {
            if (rand < 0.60) return 5;
            if (rand < 0.90) return 4;
            return 3;
        } else {
            if (rand < 0.50) return 5;
            if (rand < 0.80) return 4;
            if (rand < 0.95) return 3;
            if (rand < 0.98) return 2;
            return 1;
        }
    }

    /**
     * Crea items en el carrito para un usuario.
     *
     * @param idUsuario ID del usuario
     * @param cancionesIds IDs de canciones en el carrito
     * @param albumesIds IDs de álbumes en el carrito
     * @return número de items creados
     */
    private int crearCarrito(Long idUsuario, Set<Long> cancionesIds, Set<Long> albumesIds) {
        if (cancionesIds.isEmpty() && albumesIds.isEmpty()) {
            return 0;
        }

        int count = 0;

        Optional<Carrito> carritoOpt = carritoRepository.findByIdUsuario(idUsuario);
        Carrito carrito = carritoOpt.orElseGet(() -> carritoRepository.save(
                Carrito.builder().idUsuario(idUsuario).build()
        ));

        for (Long idCancion : cancionesIds) {
            Optional<Cancion> cancionOpt = cancionRepository.findById(idCancion);
            if (cancionOpt.isPresent()) {
                Cancion cancion = cancionOpt.get();

                CarritoItem item = CarritoItem.builder()
                        .carrito(carrito)
                        .cancion(cancion)
                        .tipoProducto(CarritoItem.TipoProducto.CANCION)
                        .precio(BigDecimal.valueOf(cancion.getPrecioCancion()))
                        .titulo(cancion.getTituloCancion())
                        .urlPortada(cancion.getUrlPortada())
                        .nombreArtistico("Artista")
                        .fechaAgregado(generarFechaAleatoria())
                        .build();

                carritoItemRepository.save(item);
                carrito.agregarItem(item);
                count++;
            }
        }

        for (Long idAlbum : albumesIds) {
            Optional<Album> albumOpt = albumRepository.findById(idAlbum);
            if (albumOpt.isPresent()) {
                Album album = albumOpt.get();

                CarritoItem item = CarritoItem.builder()
                        .carrito(carrito)
                        .album(album)
                        .tipoProducto(CarritoItem.TipoProducto.ALBUM)
                        .precio(BigDecimal.valueOf(album.getPrecioAlbum()))
                        .titulo(album.getTituloAlbum())
                        .urlPortada(album.getUrlPortada())
                        .nombreArtistico("Artista")
                        .fechaAgregado(generarFechaAleatoria())
                        .build();

                carritoItemRepository.save(item);
                carrito.agregarItem(item);
                count++;
            }
        }

        if (count > 0) {
            carritoRepository.save(carrito);
        }

        return count;
    }

    /**
     * Selecciona aleatoriamente elementos de una lista.
     *
     * @param lista lista de elementos
     * @param min cantidad mínima a seleccionar
     * @param max cantidad máxima a seleccionar
     * @return conjunto de elementos seleccionados
     */
    private Set<Long> seleccionarAleatorio(List<Long> lista, int min, int max) {
        if (lista.isEmpty()) {
            return new HashSet<>();
        }

        int cantidad = min + random.nextInt(Math.min(max - min + 1, lista.size()));
        Set<Long> seleccionados = new HashSet<>();
        List<Long> copia = new ArrayList<>(lista);
        Collections.shuffle(copia);

        for (int i = 0; i < Math.min(cantidad, copia.size()); i++) {
            seleccionados.add(copia.get(i));
        }

        return seleccionados;
    }

    /**
     * Genera una fecha aleatoria dentro del último año.
     *
     * @return fecha aleatoria
     */
    private LocalDateTime generarFechaAleatoria() {
        return LocalDateTime.now().minusDays(random.nextInt(365));
    }

    /**
     * Imprime resumen de contenido creado.
     */
    private void imprimirResumen(int totalAlbumes, int totalSingles, int totalCanciones) {
        log.info("📊 RESUMEN:");
        log.info("   • Artistas: {}", ArtistasContenidoData.ARTISTAS.size());
        log.info("   • Álbumes: {}", totalAlbumes);
        log.info("   • Singles: {}", totalSingles);
        log.info("   • Canciones totales: {}", totalCanciones);
    }

    /**
     * Limpia carpetas de contenido en Cloudinary.
     */
    private void limpiarCloudinary() {
        log.info("🧹 Limpiando carpetas en Cloudinary...");
        try {
            int audio = cloudinaryService.limpiarCarpeta("canciones/audio", "video");
            int portadas = cloudinaryService.limpiarCarpeta("canciones/portadas", "image");
            int albumPortadas = cloudinaryService.limpiarCarpeta("albumes/portadas", "image");
            log.info("✅ Limpiadas: {} audios, {} portadas canciones, {} portadas álbumes",
                    audio, portadas, albumPortadas);
        } catch (Exception e) {
            log.warn("⚠️  Error limpiando Cloudinary: {}", e.getMessage());
        }
    }

    /**
     * Verifica si existe un path en el classpath.
     *
     * @param path ruta a verificar
     * @return true si existe
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
     * Crea un álbum con sus canciones asociadas.
     *
     * @param idArtista ID del artista
     * @param carpetaArtista carpeta del artista en recursos
     * @param albumInfo información del álbum
     * @return álbum creado
     * @throws IOException si hay error al procesar archivos
     */
    private Album crearAlbum(Long idArtista, String carpetaArtista,
                             ArtistasContenidoData.AlbumInfo albumInfo) throws IOException {
        String albumFolder = normalizarNombre(albumInfo.getTitulo());
        String albumPath = SEED_DATA_CLASSPATH + "/" + carpetaArtista + "/albums/" + albumFolder;

        Resource coverResource = buscarArchivoEnClasspath(albumPath, "cover");
        String urlPortada = subirImagen(coverResource, "albumes/portadas");

        Album album = Album.builder()
                .tituloAlbum(albumInfo.getTitulo())
                .idArtista(idArtista)
                .genero(GeneroMusical.fromId(albumInfo.getIdGenero()))
                .precioAlbum(albumInfo.getPrecio())
                .urlPortada(urlPortada)
                .descripcion(albumInfo.getDescripcion())
                .fechaPublicacion(LocalDateTime.of(albumInfo.getAnio(), 1, 1, 0, 0))
                .build();

        album = albumRepository.save(album);
        log.debug("  ✓ Álbum '{}' guardado", album.getTituloAlbum());

        for (ArtistasContenidoData.CancionInfo cancionInfo : albumInfo.getCanciones()) {
            try {
                Cancion cancion = crearCancion(idArtista, cancionInfo, albumPath, urlPortada);
                cancionesCreadas.add(cancion.getIdCancion());
                cancionesPorArtista.get(idArtista).add(cancion.getIdCancion());

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
     * Crea un single independiente.
     *
     * @param idArtista ID del artista
     * @param carpetaArtista carpeta del artista en recursos
     * @param singleInfo información del single
     * @return canción creada
     * @throws IOException si hay error al procesar archivos
     */
    private Cancion crearSingle(Long idArtista, String carpetaArtista,
                                ArtistasContenidoData.SingleInfo singleInfo) throws IOException {
        String singleFolder = normalizarNombre(singleInfo.getTitulo());
        String singlePath = SEED_DATA_CLASSPATH + "/" + carpetaArtista + "/singles/" + singleFolder;

        Resource audioResource = buscarArchivoEnClasspath(singlePath, normalizarNombre(singleInfo.getTitulo()));
        Resource coverResource = buscarArchivoEnClasspath(singlePath, "cover");

        CloudinaryService.AudioUploadResult audioResult = subirAudio(audioResource, "canciones/audio");
        String urlPortada = subirImagen(coverResource, "canciones/portadas");

        Cancion cancion = Cancion.builder()
                .tituloCancion(singleInfo.getTitulo())
                .idArtista(idArtista)
                .genero(GeneroMusical.fromId(singleInfo.getIdGenero()))
                .precioCancion(singleInfo.getPrecio())
                .duracionSegundos(audioResult.getDuracion() != null ? audioResult.getDuracion() : 180)
                .urlPortada(urlPortada)
                .urlAudio(audioResult.getUrl())
                .descripcion(singleInfo.getDescripcion())
                .reproducciones(generarReproduccionesAleatorias())
                .fechaPublicacion(LocalDateTime.now().minusDays(random.nextInt(365)))
                .build();

        return cancionRepository.save(cancion);
    }

    /**
     * Crea una canción perteneciente a un álbum.
     *
     * @param idArtista ID del artista
     * @param cancionInfo información de la canción
     * @param albumPath ruta del álbum en recursos
     * @param urlPortadaAlbum URL de la portada del álbum
     * @return canción creada
     * @throws IOException si hay error al procesar archivos
     */
    private Cancion crearCancion(Long idArtista,
                                 ArtistasContenidoData.CancionInfo cancionInfo,
                                 String albumPath, String urlPortadaAlbum) throws IOException {
        String audioFileName = String.format("%02d_%s",
                cancionInfo.getNumeroPista(),
                normalizarNombre(cancionInfo.getTitulo()));

        Resource audioResource = buscarArchivoEnClasspath(albumPath, audioFileName);
        CloudinaryService.AudioUploadResult audioResult = subirAudio(audioResource, "canciones/audio");

        Cancion cancion = Cancion.builder()
                .tituloCancion(cancionInfo.getTitulo())
                .idArtista(idArtista)
                .genero(GeneroMusical.fromId(cancionInfo.getIdGenero()))
                .precioCancion(cancionInfo.getPrecio())
                .duracionSegundos(audioResult.getDuracion() != null ? audioResult.getDuracion() : 180)
                .urlPortada(urlPortadaAlbum)
                .urlAudio(audioResult.getUrl())
                .descripcion("Pista " + cancionInfo.getNumeroPista())
                .reproducciones(generarReproduccionesAleatorias())
                .fechaPublicacion(LocalDateTime.now().minusDays(random.nextInt(365)))
                .build();

        return cancionRepository.save(cancion);
    }

    /**
     * Busca un archivo en el classpath por su nombre base.
     *
     * @param directory directorio donde buscar
     * @param baseName nombre base del archivo
     * @return recurso encontrado
     * @throws IOException si no se encuentra el archivo
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
     * Normaliza un nombre eliminando acentos y caracteres especiales.
     *
     * @param nombre nombre a normalizar
     * @return nombre normalizado
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
     * Sube un archivo de audio a Cloudinary.
     *
     * @param resource recurso de audio
     * @param folder carpeta destino
     * @return resultado con URL y duración
     * @throws IOException si hay error al subir
     */
    private CloudinaryService.AudioUploadResult subirAudio(Resource resource, String folder) throws IOException {
        return cloudinaryService.subirAudio(convertirAMultipartFile(resource), folder);
    }

    /**
     * Sube una imagen a Cloudinary.
     *
     * @param resource recurso de imagen
     * @param folder carpeta destino
     * @return URL de la imagen
     * @throws IOException si hay error al subir
     */
    private String subirImagen(Resource resource, String folder) throws IOException {
        return cloudinaryService.subirPortada(convertirAMultipartFile(resource), folder);
    }

    /**
     * Genera un número aleatorio de reproducciones siguiendo una distribución realista.
     *
     * @return número de reproducciones
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
     * Convierte un Resource de Spring en MultipartFile.
     *
     * @param resource recurso a convertir
     * @return archivo multipart
     * @throws IOException si hay error al leer el recurso
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