package com.ondra.contenidos.services;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.mappers.AlbumMapper;
import com.ondra.contenidos.models.dao.Album;
import com.ondra.contenidos.models.dao.AlbumCancion;
import com.ondra.contenidos.models.dao.Cancion;
import com.ondra.contenidos.models.enums.GeneroMusical;
import com.ondra.contenidos.repositories.AlbumCancionRepository;
import com.ondra.contenidos.repositories.AlbumRepository;
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
 * Servicio para gestión de álbumes musicales.
 *
 * <p>Proporciona operaciones de consulta, creación, actualización y eliminación de álbumes,
 * así como la gestión de canciones asociadas a cada álbum.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final CancionRepository cancionRepository;
    private final AlbumCancionRepository albumCancionRepository;
    private final AlbumMapper albumMapper;
    private final CloudinaryService cloudinaryService;

    /**
     * Obtiene álbumes con filtros opcionales y paginación.
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
     * @return álbumes paginados con metadatos de paginación
     * @throws GeneroNotFoundException si el género especificado no existe
     */
    @Transactional(readOnly = true)
    public AlbumesPaginadosDTO listarAlbumes(
            Long idArtista,
            Long idGenero,
            String busqueda,
            String ordenar,
            Integer pagina,
            Integer limite) {

        log.debug("📋 Listando álbumes - Artista: {}, Género: {}, Búsqueda: {}, Orden: {}, Página: {}, Límite: {}",
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

        Page<Album> paginaAlbumes = albumRepository.buscarConFiltros(
                idArtista,
                genero,
                busqueda,
                pageable
        );

        List<AlbumDTO> albumes = albumMapper.toDTOList(paginaAlbumes.getContent());

        return AlbumesPaginadosDTO.builder()
                .albumes(albumes)
                .paginaActual(paginaAlbumes.getNumber() + 1)
                .totalPaginas(paginaAlbumes.getTotalPages())
                .totalElementos(paginaAlbumes.getTotalElements())
                .elementosPorPagina(paginaAlbumes.getSize())
                .build();
    }

    /**
     * Obtiene un álbum por su identificador con información detallada.
     *
     * @param idAlbum identificador del álbum
     * @return álbum con lista completa de canciones
     * @throws AlbumNotFoundException si el álbum no existe
     */
    @Transactional(readOnly = true)
    public AlbumDetalleDTO obtenerAlbumPorId(Long idAlbum) {
        log.debug("🔍 Obteniendo álbum con ID: {}", idAlbum);

        Album album = albumRepository.findById(idAlbum)
                .orElseThrow(() -> new AlbumNotFoundException(idAlbum));

        return albumMapper.toDetalleDTO(album);
    }

    /**
     * Obtiene todos los álbumes de un artista ordenados por fecha de publicación descendente.
     *
     * @param idArtista identificador del artista
     * @return lista de álbumes del artista
     */
    @Transactional(readOnly = true)
    public List<AlbumDTO> listarAlbumesPorArtista(Long idArtista) {
        log.debug("👤 Listando álbumes del artista: {}", idArtista);

        List<Album> albumes = albumRepository.findByIdArtistaOrderByFechaPublicacionDesc(idArtista);
        return albumMapper.toDTOList(albumes);
    }

    /**
     * Busca álbumes por término de búsqueda en título o descripción.
     *
     * @param query término de búsqueda
     * @return lista de álbumes que coinciden con la búsqueda
     */
    @Transactional(readOnly = true)
    public List<AlbumDTO> buscarAlbumes(String query) {
        log.debug("🔎 Buscando álbumes con query: {}", query);

        Pageable pageable = PageRequest.of(0, 50);
        Page<Album> albumes = albumRepository.buscarPorTexto(query, pageable);

        return albumMapper.toDTOList(albumes.getContent());
    }

    /**
     * Obtiene las canciones de un álbum ordenadas por número de pista.
     *
     * @param idAlbum identificador del álbum
     * @return lista de canciones con información de pista
     * @throws AlbumNotFoundException si el álbum no existe
     */
    @Transactional(readOnly = true)
    public List<CancionAlbumDTO> obtenerCancionesAlbum(Long idAlbum) {
        log.debug("🎵 Obteniendo canciones del álbum: {}", idAlbum);

        Album album = albumRepository.findById(idAlbum)
                .orElseThrow(() -> new AlbumNotFoundException(idAlbum));

        return album.getAlbumCanciones().stream()
                .map(albumMapper::toCancionAlbumDTO)
                .toList();
    }

    /**
     * Obtiene álbumes con precio cero.
     *
     * @return lista de álbumes gratuitos
     */
    @Transactional(readOnly = true)
    public List<AlbumDTO> listarAlbumesGratuitos() {
        log.debug("💰 Listando álbumes gratuitos");

        Pageable pageable = PageRequest.of(0, 100);
        Page<Album> albumes = albumRepository.findAlbumesGratuitos(pageable);

        return albumMapper.toDTOList(albumes.getContent());
    }

    /**
     * Obtiene álbumes de un género musical específico ordenados por fecha de publicación.
     *
     * @param idGenero identificador del género musical
     * @return lista de álbumes del género especificado
     * @throws GeneroNotFoundException si el género no existe
     */
    @Transactional(readOnly = true)
    public List<AlbumDTO> listarAlbumesPorGenero(Long idGenero) {
        log.debug("🎸 Listando álbumes del género: {}", idGenero);

        GeneroMusical genero;
        try {
            genero = GeneroMusical.fromId(idGenero);
        } catch (IllegalArgumentException e) {
            throw new GeneroNotFoundException(idGenero);
        }

        List<Album> albumes = albumRepository.findByGeneroOrderByFechaPublicacionDesc(genero);
        return albumMapper.toDTOList(albumes);
    }

    /**
     * Obtiene álbumes ordenados por valoración media descendente.
     *
     * @param limite cantidad máxima de álbumes a devolver
     * @return lista de álbumes mejor valorados
     */
    @Transactional(readOnly = true)
    public List<AlbumDTO> listarAlbumesMejorValorados(Integer limite) {
        log.debug("⭐ Listando álbumes mejor valorados (límite: {})", limite);

        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 10;

        Pageable pageable = PageRequest.of(0, limite,
                Sort.by(Sort.Direction.DESC, "valoracionMedia"));

        Page<Album> albumes = albumRepository.findAll(pageable);
        return albumMapper.toDTOList(albumes.getContent());
    }

    /**
     * Obtiene álbumes ordenados por fecha de publicación descendente.
     *
     * @param limite cantidad máxima de álbumes a devolver
     * @return lista de álbumes más recientes
     */
    @Transactional(readOnly = true)
    public List<AlbumDTO> listarAlbumesRecientes(Integer limite) {
        log.debug("📅 Listando álbumes recientes (límite: {})", limite);

        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 10;

        Pageable pageable = PageRequest.of(0, limite,
                Sort.by(Sort.Direction.DESC, "fechaPublicacion"));

        Page<Album> albumes = albumRepository.findAll(pageable);
        return albumMapper.toDTOList(albumes.getContent());
    }

    /**
     * Crea un nuevo álbum para un artista.
     *
     * @param dto datos del álbum a crear
     * @param idArtista identificador del artista propietario
     * @return álbum creado
     * @throws GeneroNotFoundException si el género especificado no existe
     */
    @Transactional
    public AlbumDTO crearAlbum(CrearAlbumDTO dto, Long idArtista) {
        log.info("➕ Creando nuevo álbum '{}' para artista {}", dto.getTituloAlbum(), idArtista);

        try {
            GeneroMusical.fromId(dto.getIdGenero());
        } catch (IllegalArgumentException e) {
            throw new GeneroNotFoundException(dto.getIdGenero());
        }

        Album album = albumMapper.toEntity(dto, idArtista);
        Album albumGuardado = albumRepository.save(album);

        log.info("✅ Álbum creado con ID: {}", albumGuardado.getIdAlbum());
        return albumMapper.toDTO(albumGuardado);
    }

    /**
     * Actualiza un álbum existente.
     *
     * <p>Solo el artista propietario del álbum puede realizar la actualización.</p>
     *
     * @param idAlbum identificador del álbum a actualizar
     * @param dto datos actualizados del álbum
     * @param idArtistaAutenticado identificador del artista autenticado
     * @return álbum actualizado
     * @throws AlbumNotFoundException si el álbum no existe
     * @throws AccesoDenegadoException si el artista no es el propietario
     * @throws GeneroNotFoundException si el género especificado no existe
     */
    @Transactional
    public AlbumDTO actualizarAlbum(Long idAlbum, EditarAlbumDTO dto, Long idArtistaAutenticado) {
        log.info("✏️ Actualizando álbum {} por artista {}", idAlbum, idArtistaAutenticado);

        Album album = albumRepository.findById(idAlbum)
                .orElseThrow(() -> new AlbumNotFoundException(idAlbum));

        if (!album.perteneceArtista(idArtistaAutenticado)) {
            log.warn("⚠️ Artista {} intentó modificar álbum {} que no le pertenece",
                    idArtistaAutenticado, idAlbum);
            throw new AccesoDenegadoException("álbum", idAlbum);
        }

        if (dto.getIdGenero() != null) {
            try {
                GeneroMusical.fromId(dto.getIdGenero());
            } catch (IllegalArgumentException e) {
                throw new GeneroNotFoundException(dto.getIdGenero());
            }
        }

        albumMapper.updateEntity(album, dto);
        Album albumActualizado = albumRepository.save(album);

        log.info("✅ Álbum {} actualizado", idAlbum);
        return albumMapper.toDTO(albumActualizado);
    }

    /**
     * Elimina un álbum y sus recursos asociados.
     *
     * <p>Solo el artista propietario puede eliminar el álbum. Elimina la portada de Cloudinary
     * y las relaciones con canciones, pero no elimina las canciones mismas.</p>
     *
     * @param idAlbum identificador del álbum a eliminar
     * @param idArtistaAutenticado identificador del artista autenticado
     * @throws AlbumNotFoundException si el álbum no existe
     * @throws AccesoDenegadoException si el artista no es el propietario
     */
    @Transactional
    public void eliminarAlbum(Long idAlbum, Long idArtistaAutenticado) {
        log.info("🗑️ Eliminando álbum {} por artista {}", idAlbum, idArtistaAutenticado);

        Album album = albumRepository.findById(idAlbum)
                .orElseThrow(() -> new AlbumNotFoundException(idAlbum));

        if (!album.perteneceArtista(idArtistaAutenticado)) {
            log.warn("⚠️ Artista {} intentó eliminar álbum {} que no le pertenece",
                    idArtistaAutenticado, idAlbum);
            throw new AccesoDenegadoException("álbum", idAlbum);
        }

        try {
            cloudinaryService.eliminarArchivo(album.getUrlPortada());
        } catch (Exception e) {
            log.error("❌ Error al eliminar portada de Cloudinary para álbum {}: {}",
                    idAlbum, e.getMessage());
        }

        albumCancionRepository.deleteByAlbumIdAlbum(idAlbum);
        albumRepository.delete(album);

        log.info("✅ Álbum {} eliminado", idAlbum);
    }

    /**
     * Añade una canción a un álbum con un número de pista específico.
     *
     * <p>Verifica que el artista sea propietario tanto del álbum como de la canción,
     * que la canción no esté ya en el álbum y que el número de pista no esté ocupado.</p>
     *
     * @param idAlbum identificador del álbum
     * @param dto datos de la canción y número de pista
     * @param idArtistaAutenticado identificador del artista autenticado
     * @throws AlbumNotFoundException si el álbum no existe
     * @throws CancionNotFoundException si la canción no existe
     * @throws AccesoDenegadoException si el artista no es propietario
     * @throws CancionYaEnAlbumException si la canción ya está en el álbum
     * @throws NumeroPistaYaExisteException si el número de pista ya está ocupado
     */
    @Transactional
    public void agregarCancionAlAlbum(Long idAlbum, AgregarCancionAlbumDTO dto, Long idArtistaAutenticado) {
        log.info("➕ Agregando canción {} al álbum {} con pista {}",
                dto.getIdCancion(), idAlbum, dto.getNumeroPista());

        Album album = albumRepository.findById(idAlbum)
                .orElseThrow(() -> new AlbumNotFoundException(idAlbum));

        if (!album.perteneceArtista(idArtistaAutenticado)) {
            throw new AccesoDenegadoException("álbum", idAlbum);
        }

        Cancion cancion = cancionRepository.findById(dto.getIdCancion())
                .orElseThrow(() -> new CancionNotFoundException(dto.getIdCancion()));

        if (!cancion.perteneceArtista(idArtistaAutenticado)) {
            throw new AccesoDenegadoException(
                    "No puedes añadir canciones de otros artistas a tu álbum");
        }

        if (album.contieneCancion(dto.getIdCancion())) {
            throw new CancionYaEnAlbumException(dto.getIdCancion(), idAlbum);
        }

        if (album.existeNumeroPista(dto.getNumeroPista())) {
            throw new NumeroPistaYaExisteException(dto.getNumeroPista(), idAlbum);
        }

        AlbumCancion albumCancion = AlbumCancion.builder()
                .album(album)
                .cancion(cancion)
                .numeroPista(dto.getNumeroPista())
                .build();

        albumCancionRepository.save(albumCancion);

        log.info("✅ Canción {} añadida al álbum {} en pista {}",
                dto.getIdCancion(), idAlbum, dto.getNumeroPista());
    }

    /**
     * Elimina una canción de un álbum.
     *
     * <p>Solo elimina la relación entre el álbum y la canción, no la canción misma.</p>
     *
     * @param idAlbum identificador del álbum
     * @param idCancion identificador de la canción
     * @param idArtistaAutenticado identificador del artista autenticado
     * @throws AlbumNotFoundException si el álbum no existe
     * @throws AccesoDenegadoException si el artista no es el propietario
     * @throws CancionNoEnAlbumException si la canción no está en el álbum
     */
    @Transactional
    public void eliminarCancionDeAlbum(Long idAlbum, Long idCancion, Long idArtistaAutenticado) {
        log.info("➖ Eliminando canción {} del álbum {}", idCancion, idAlbum);

        Album album = albumRepository.findById(idAlbum)
                .orElseThrow(() -> new AlbumNotFoundException(idAlbum));

        if (!album.perteneceArtista(idArtistaAutenticado)) {
            throw new AccesoDenegadoException("álbum", idAlbum);
        }

        if (!album.contieneCancion(idCancion)) {
            throw new CancionNoEnAlbumException(idCancion, idAlbum);
        }

        albumCancionRepository.eliminarCancionDeAlbum(idAlbum, idCancion);

        log.info("✅ Canción {} eliminada del álbum {}", idCancion, idAlbum);
    }

    /**
     * Elimina todos los álbumes de un artista junto con sus recursos asociados.
     *
     * <p>Utilizado cuando se elimina un artista del sistema. Elimina portadas de Cloudinary
     * y relaciones con canciones para cada álbum.</p>
     *
     * @param idArtista identificador del artista
     */
    @Transactional
    public void eliminarTodosAlbumesArtista(Long idArtista) {
        log.info("🗑️ Eliminando todos los álbumes del artista {}", idArtista);

        List<Album> albumes = albumRepository.findByIdArtistaOrderByFechaPublicacionDesc(idArtista);

        for (Album album : albumes) {
            try {
                cloudinaryService.eliminarArchivo(album.getUrlPortada());
            } catch (Exception e) {
                log.error("❌ Error al eliminar portada de Cloudinary para álbum {}: {}",
                        album.getIdAlbum(), e.getMessage());
            }

            albumCancionRepository.deleteByAlbumIdAlbum(album.getIdAlbum());
        }

        albumRepository.deleteByIdArtista(idArtista);

        log.info("✅ Eliminados {} álbumes del artista {}", albumes.size(), idArtista);
    }

    /**
     * Calcula las estadísticas totales de reproducciones de un artista.
     *
     * <p>Suma las reproducciones de todas las canciones en todos los álbumes del artista.</p>
     *
     * @param idArtista identificador del artista
     * @return estadísticas con total de reproducciones
     */
    @Transactional(readOnly = true)
    public EstadisticasArtistaDTO obtenerEstadisticasArtista(Long idArtista) {
        log.debug("📊 Calculando estadísticas del artista: {}", idArtista);

        List<Album> albumes = albumRepository.findByIdArtistaOrderByFechaPublicacionDesc(idArtista);

        Long totalReproducciones = albumes.stream()
                .mapToLong(Album::getTotalPlayCount)
                .sum();

        return EstadisticasArtistaDTO.builder()
                .idArtista(idArtista)
                .totalReproducciones(totalReproducciones)
                .build();
    }

    /**
     * Obtiene la ordenación para las consultas según el criterio especificado.
     *
     * <p>Criterios soportados: most_recent, oldest, best_rated, price_asc, price_desc.</p>
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
            case "best_rated", "mejor_valorados" -> Sort.by(Sort.Direction.DESC, "valoracionMedia");
            case "price_asc", "precio_asc" -> Sort.by(Sort.Direction.ASC, "precioAlbum");
            case "price_desc", "precio_desc" -> Sort.by(Sort.Direction.DESC, "precioAlbum");
            default -> Sort.by(Sort.Direction.DESC, "fechaPublicacion");
        };
    }
}