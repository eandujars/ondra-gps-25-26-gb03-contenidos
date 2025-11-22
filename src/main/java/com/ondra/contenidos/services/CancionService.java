package com.ondra.contenidos.services;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.mappers.CancionMapper;
import com.ondra.contenidos.models.dao.Cancion;
import com.ondra.contenidos.models.enums.GeneroMusical;
import com.ondra.contenidos.repositories.AlbumCancionRepository;
import com.ondra.contenidos.repositories.CancionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de canciones.
 *
 * <p>Proporciona operaciones de consulta, creación, actualización y eliminación de canciones,
 * registro de reproducciones y obtención de estadísticas.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CancionService {

    private final CancionRepository cancionRepository;
    private final AlbumCancionRepository albumCancionRepository;
    private final CancionMapper cancionMapper;
    private final CloudinaryService cloudinaryService;

    /**
     * Obtiene canciones con filtros opcionales y paginación.
     *
     * <p>Permite filtrar por artista, género y búsqueda de texto, con soporte para
     * ordenación personalizada y paginación de resultados.</p>
     *
     * @param idArtista identificador del artista para filtrar
     * @param idGenero identificador del género musical para filtrar
     * @param busqueda término de búsqueda en título o descripción
     * @param ordenar criterio de ordenación
     * @param pagina número de página (base 1)
     * @param limite cantidad de elementos por página
     * @return canciones paginadas con metadatos de paginación
     * @throws GeneroNotFoundException si el género especificado no existe
     */
    @Transactional(readOnly = true)
    public CancionesPaginadasDTO listarCanciones(
            Long idArtista,
            Long idGenero,
            String busqueda,
            String ordenar,
            Integer pagina,
            Integer limite) {

        log.debug("📋 Listando canciones - Artista: {}, Género: {}, Búsqueda: {}, Orden: {}, Página: {}, Límite: {}",
                idArtista, idGenero, busqueda, ordenar, pagina, limite);

        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite, obtenerOrdenacion(ordenar));

        GeneroMusical genero = null;
        if (idGenero != null) {
            try {
                genero = GeneroMusical.fromId(idGenero);
            } catch (IllegalArgumentException e) {
                throw new GeneroNotFoundException(idGenero);
            }
        }

        Page<Cancion> paginaCanciones = cancionRepository.buscarConFiltros(
                idArtista,
                genero,
                busqueda,
                pageable
        );

        List<CancionDTO> canciones = cancionMapper.toDTOList(paginaCanciones.getContent());

        return CancionesPaginadasDTO.builder()
                .canciones(canciones)
                .paginaActual(paginaCanciones.getNumber() + 1)
                .totalPaginas(paginaCanciones.getTotalPages())
                .totalElementos(paginaCanciones.getTotalElements())
                .elementosPorPagina(paginaCanciones.getSize())
                .build();
    }

    /**
     * Obtiene una canción por su identificador con información detallada.
     *
     * @param idCancion identificador de la canción
     * @return canción con información completa
     * @throws CancionNotFoundException si la canción no existe
     */
    @Transactional(readOnly = true)
    public CancionDetalleDTO obtenerCancionPorId(Long idCancion) {
        log.debug("🔍 Obteniendo canción con ID: {}", idCancion);

        Cancion cancion = cancionRepository.findById(idCancion)
                .orElseThrow(() -> new CancionNotFoundException(idCancion));

        return cancionMapper.toDetalleDTO(cancion);
    }

    /**
     * Obtiene todas las canciones de un artista ordenadas por fecha de publicación descendente.
     *
     * @param idArtista identificador del artista
     * @return lista de canciones del artista
     */
    @Transactional(readOnly = true)
    public List<CancionDTO> listarCancionesPorArtista(Long idArtista) {
        log.debug("👤 Listando canciones del artista: {}", idArtista);

        List<Cancion> canciones = cancionRepository.findByIdArtistaOrderByFechaPublicacionDesc(idArtista);
        return cancionMapper.toDTOList(canciones);
    }

    /**
     * Obtiene todas las canciones de un álbum ordenadas por número de pista.
     *
     * @param idAlbum identificador del álbum
     * @return lista de canciones del álbum
     */
    @Transactional(readOnly = true)
    public List<CancionDTO> listarCancionesPorAlbum(Long idAlbum) {
        log.debug("💿 Listando canciones del álbum: {}", idAlbum);

        List<Cancion> canciones = albumCancionRepository.findByAlbumIdAlbumOrderByNumeroPistaAsc(idAlbum)
                .stream()
                .map(ac -> ac.getCancion())
                .toList();

        return cancionMapper.toDTOList(canciones);
    }

    /**
     * Busca canciones por término de búsqueda en título o descripción.
     *
     * @param query término de búsqueda
     * @return lista de canciones que coinciden con la búsqueda
     */
    @Transactional(readOnly = true)
    public List<CancionDTO> buscarCanciones(String query) {
        log.debug("🔎 Buscando canciones con query: {}", query);

        Pageable pageable = PageRequest.of(0, 50);
        Page<Cancion> canciones = cancionRepository.buscarPorTexto(query, pageable);

        return cancionMapper.toDTOList(canciones.getContent());
    }

    /**
     * Obtiene canciones con precio cero.
     *
     * @return lista de canciones gratuitas
     */
    @Transactional(readOnly = true)
    public List<CancionDTO> listarCancionesGratuitas() {
        log.debug("💰 Listando canciones gratuitas");

        Pageable pageable = PageRequest.of(0, 100);
        Page<Cancion> canciones = cancionRepository.findCancionesGratuitas(pageable);

        return cancionMapper.toDTOList(canciones.getContent());
    }

    /**
     * Obtiene estadísticas globales del sistema de canciones.
     *
     * @return estadísticas con total de canciones y reproducciones
     */
    @Transactional(readOnly = true)
    public CancionesStatsDTO obtenerEstadisticas() {
        log.debug("📊 Obteniendo estadísticas globales de canciones");

        long totalCanciones = cancionRepository.count();
        long totalReproducciones = cancionRepository.getTotalReproducciones();

        return CancionesStatsDTO.builder()
                .totalCanciones(totalCanciones)
                .totalReproducciones(totalReproducciones)
                .build();
    }

    /**
     * Registra una reproducción de canción incrementando su contador.
     *
     * @param idCancion identificador de la canción
     * @return respuesta con identificador y total de reproducciones
     * @throws CancionNotFoundException si la canción no existe
     */
    @Transactional
    public ReproduccionResponseDTO registrarReproduccion(Long idCancion) {
        log.debug("▶️ Registrando reproducción para canción {}", idCancion);

        Cancion cancion = cancionRepository.findById(idCancion)
                .orElseThrow(() -> new CancionNotFoundException(idCancion));

        cancion.incrementarReproducciones();
        cancionRepository.save(cancion);

        return ReproduccionResponseDTO.builder()
                .id(idCancion.toString())
                .totalPlays(cancion.getReproducciones())
                .build();
    }

    /**
     * Crea una nueva canción para un artista.
     *
     * @param dto datos de la canción a crear
     * @param idArtista identificador del artista propietario
     * @return canción creada
     * @throws GeneroNotFoundException si el género especificado no existe
     */
    @Transactional
    public CancionDTO crearCancion(CrearCancionDTO dto, Long idArtista) {
        log.info("➕ Creando nueva canción '{}' para artista {}", dto.getTituloCancion(), idArtista);

        try {
            GeneroMusical.fromId(dto.getIdGenero());
        } catch (IllegalArgumentException e) {
            throw new GeneroNotFoundException(dto.getIdGenero());
        }

        Cancion cancion = cancionMapper.toEntity(dto, idArtista);
        Cancion cancionGuardada = cancionRepository.save(cancion);

        log.info("✅ Canción creada con ID: {}", cancionGuardada.getIdCancion());
        return cancionMapper.toDTO(cancionGuardada);
    }

    /**
     * Actualiza una canción existente.
     *
     * <p>Solo el artista propietario de la canción puede realizar la actualización.</p>
     *
     * @param idCancion identificador de la canción a actualizar
     * @param dto datos actualizados de la canción
     * @param idArtistaAutenticado identificador del artista autenticado
     * @return canción actualizada
     * @throws CancionNotFoundException si la canción no existe
     * @throws AccesoDenegadoException si el artista no es el propietario
     * @throws GeneroNotFoundException si el género especificado no existe
     */
    @Transactional
    public CancionDTO actualizarCancion(Long idCancion, EditarCancionDTO dto, Long idArtistaAutenticado) {
        log.info("✏️ Actualizando canción {} por artista {}", idCancion, idArtistaAutenticado);

        Cancion cancion = cancionRepository.findById(idCancion)
                .orElseThrow(() -> new CancionNotFoundException(idCancion));

        if (!cancion.perteneceArtista(idArtistaAutenticado)) {
            log.warn("⚠️ Artista {} intentó modificar canción {} que no le pertenece",
                    idArtistaAutenticado, idCancion);
            throw new AccesoDenegadoException("canción", idCancion);
        }

        if (dto.getIdGenero() != null) {
            try {
                GeneroMusical.fromId(dto.getIdGenero());
            } catch (IllegalArgumentException e) {
                throw new GeneroNotFoundException(dto.getIdGenero());
            }
        }

        cancionMapper.updateEntity(cancion, dto);
        Cancion cancionActualizada = cancionRepository.save(cancion);

        log.info("✅ Canción {} actualizada", idCancion);
        return cancionMapper.toDTO(cancionActualizada);
    }

    /**
     * Elimina una canción y sus recursos asociados.
     *
     * <p>Solo el artista propietario puede eliminar la canción. Elimina los archivos de audio
     * y portada de Cloudinary, así como las relaciones con álbumes.</p>
     *
     * @param idCancion identificador de la canción a eliminar
     * @param idArtistaAutenticado identificador del artista autenticado
     * @throws CancionNotFoundException si la canción no existe
     * @throws AccesoDenegadoException si el artista no es el propietario
     */
    @Transactional
    public void eliminarCancion(Long idCancion, Long idArtistaAutenticado) {
        log.info("🗑️ Eliminando canción {} por artista {}", idCancion, idArtistaAutenticado);

        Cancion cancion = cancionRepository.findById(idCancion)
                .orElseThrow(() -> new CancionNotFoundException(idCancion));

        if (!cancion.perteneceArtista(idArtistaAutenticado)) {
            log.warn("⚠️ Artista {} intentó eliminar canción {} que no le pertenece",
                    idArtistaAutenticado, idCancion);
            throw new AccesoDenegadoException("canción", idCancion);
        }

        try {
            cloudinaryService.eliminarArchivo(cancion.getUrlAudio());
            cloudinaryService.eliminarArchivo(cancion.getUrlPortada());
        } catch (Exception e) {
            log.error("❌ Error al eliminar archivos de Cloudinary para canción {}: {}",
                    idCancion, e.getMessage());
        }

        albumCancionRepository.deleteByCancionIdCancion(idCancion);
        cancionRepository.delete(cancion);

        log.info("✅ Canción {} eliminada", idCancion);
    }

    /**
     * Elimina todas las canciones de un artista junto con sus recursos asociados.
     *
     * <p>Utilizado cuando se elimina un artista del sistema. Elimina archivos de Cloudinary
     * y relaciones con álbumes para cada canción.</p>
     *
     * @param idArtista identificador del artista
     */
    @Transactional
    public void eliminarTodasCancionesArtista(Long idArtista) {
        log.info("🗑️ Eliminando todas las canciones del artista {}", idArtista);

        List<Cancion> canciones = cancionRepository.findByIdArtistaOrderByFechaPublicacionDesc(idArtista);

        for (Cancion cancion : canciones) {
            try {
                cloudinaryService.eliminarArchivo(cancion.getUrlAudio());
                cloudinaryService.eliminarArchivo(cancion.getUrlPortada());
            } catch (Exception e) {
                log.error("❌ Error al eliminar archivos de Cloudinary para canción {}: {}",
                        cancion.getIdCancion(), e.getMessage());
            }

            albumCancionRepository.deleteByCancionIdCancion(cancion.getIdCancion());
        }

        cancionRepository.deleteByIdArtista(idArtista);

        log.info("✅ Eliminadas {} canciones del artista {}", canciones.size(), idArtista);
    }

    /**
     * Obtiene el total de reproducciones acumuladas de todas las canciones de un artista.
     *
     * @param idArtista identificador del artista
     * @return total de reproducciones del artista
     */
    @Transactional(readOnly = true)
    public Long obtenerTotalReproduccionesArtista(Long idArtista) {
        return cancionRepository.getTotalReproduccionesByArtista(idArtista);
    }

    /**
     * Obtiene la ordenación para las consultas según el criterio especificado.
     *
     * <p>Criterios soportados: most_recent, oldest, most_played, best_rated, price_asc, price_desc.</p>
     *
     * @param ordenar criterio de ordenación
     * @return objeto Sort con la ordenación configurada
     */
    private Sort obtenerOrdenacion(String ordenar) {
        if (ordenar == null) {
            return Sort.by(Sort.Direction.DESC, "fechaPublicacion");
        }

        return switch (ordenar.toLowerCase()) {
            case "most_recent", "mas_recientes" -> Sort.by(Sort.Direction.DESC, "fechaPublicacion");
            case "oldest", "mas_antiguas" -> Sort.by(Sort.Direction.ASC, "fechaPublicacion");
            case "most_played", "mas_reproducidas" -> Sort.by(Sort.Direction.DESC, "reproducciones");
            case "best_rated", "mejor_valoradas" -> Sort.by(Sort.Direction.DESC, "valoracionMedia");
            case "price_asc", "precio_asc" -> Sort.by(Sort.Direction.ASC, "precioCancion");
            case "price_desc", "precio_desc" -> Sort.by(Sort.Direction.DESC, "precioCancion");
            default -> Sort.by(Sort.Direction.DESC, "fechaPublicacion");
        };
    }
}