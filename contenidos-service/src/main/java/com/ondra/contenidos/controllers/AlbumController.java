package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.ForbiddenAccessException;
import com.ondra.contenidos.services.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de álbumes.
 *
 * <p>Proporciona endpoints públicos para consulta de álbumes y endpoints
 * protegidos para artistas que permiten crear, modificar y eliminar álbumes.</p>
 *
 * <p>Base URL: /api/albumes</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/albumes")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    /**
     * Lista álbumes con filtros opcionales y paginación.
     *
     * @param artistId filtro por artista
     * @param genreId filtro por género
     * @param search búsqueda por título o descripción
     * @param orderBy criterio de ordenación (most_recent, oldest, best_rated, price_asc, price_desc)
     * @param page número de página (1-indexed)
     * @param limit elementos por página (default: 20, max: 100)
     * @return página de álbumes con metadatos de paginación
     */
    @GetMapping
    public ResponseEntity<AlbumesPaginadosDTO> listarAlbumes(
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        log.info("GET /albumes - artistId: {}, genreId: {}, search: {}, orderBy: {}, page: {}, limit: {}",
                artistId, genreId, search, orderBy, page, limit);

        AlbumesPaginadosDTO resultado = albumService.listarAlbumes(
                artistId, genreId, search, orderBy, page, limit);

        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtiene el detalle completo de un álbum incluyendo su lista de canciones.
     *
     * @param id identificador del álbum
     * @return detalle del álbum con tracklist
     */
    @GetMapping("/{id}")
    public ResponseEntity<AlbumDetalleDTO> obtenerAlbum(@PathVariable Long id) {
        log.info("GET /albumes/{}", id);

        AlbumDetalleDTO album = albumService.obtenerAlbumPorId(id);
        return ResponseEntity.ok(album);
    }

    /**
     * Obtiene todos los álbumes publicados por un artista específico.
     *
     * @param artistId identificador del artista
     * @return lista de álbumes del artista
     */
    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<AlbumDTO>> obtenerAlbumesPorArtista(@PathVariable Long artistId) {
        log.info("GET /albumes/artist/{}", artistId);

        List<AlbumDTO> albumes = albumService.listarAlbumesPorArtista(artistId);
        return ResponseEntity.ok(albumes);
    }

    /**
     * Busca álbumes por término de búsqueda en título y descripción.
     *
     * @param q término de búsqueda
     * @return lista de álbumes coincidentes
     */
    @GetMapping("/search")
    public ResponseEntity<List<AlbumDTO>> buscarAlbumes(@RequestParam String q) {
        log.info("GET /albumes/search?q={}", q);

        List<AlbumDTO> albumes = albumService.buscarAlbumes(q);
        return ResponseEntity.ok(albumes);
    }

    /**
     * Obtiene las canciones de un álbum ordenadas por número de pista.
     *
     * @param id identificador del álbum
     * @return lista de canciones del álbum
     */
    @GetMapping("/{id}/tracks")
    public ResponseEntity<List<CancionAlbumDTO>> obtenerCancionesAlbum(@PathVariable Long id) {
        log.info("GET /albumes/{}/tracks", id);

        List<CancionAlbumDTO> canciones = albumService.obtenerCancionesAlbum(id);
        return ResponseEntity.ok(canciones);
    }

    /**
     * Obtiene álbumes disponibles de forma gratuita.
     *
     * @return lista de álbumes con precio 0.00
     */
    @GetMapping("/gratuitos")
    public ResponseEntity<List<AlbumDTO>> obtenerAlbumesGratuitos() {
        log.info("GET /albumes/gratuitos");

        List<AlbumDTO> albumes = albumService.listarAlbumesGratuitos();
        return ResponseEntity.ok(albumes);
    }

    /**
     * Obtiene álbumes filtrados por género musical.
     *
     * @param genreId identificador del género
     * @return lista de álbumes del género especificado
     */
    @GetMapping("/genre/{genreId}")
    public ResponseEntity<List<AlbumDTO>> obtenerAlbumesPorGenero(@PathVariable Long genreId) {
        log.info("GET /albumes/genre/{}", genreId);

        List<AlbumDTO> albumes = albumService.listarAlbumesPorGenero(genreId);
        return ResponseEntity.ok(albumes);
    }

    /**
     * Obtiene los álbumes mejor valorados por los usuarios.
     *
     * @param limit número máximo de resultados (default: 10)
     * @return lista de álbumes ordenados por valoración descendente
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<AlbumDTO>> obtenerAlbumesMejorValorados(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {

        log.info("GET /albumes/top-rated?limit={}", limit);

        List<AlbumDTO> albumes = albumService.listarAlbumesMejorValorados(limit);
        return ResponseEntity.ok(albumes);
    }

    /**
     * Obtiene los álbumes publicados más recientemente.
     *
     * @param limit número máximo de resultados (default: 10)
     * @return lista de álbumes ordenados por fecha de publicación descendente
     */
    @GetMapping("/recent")
    public ResponseEntity<List<AlbumDTO>> obtenerAlbumesRecientes(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {

        log.info("GET /albumes/recent?limit={}", limit);

        List<AlbumDTO> albumes = albumService.listarAlbumesRecientes(limit);
        return ResponseEntity.ok(albumes);
    }

    /**
     * Obtiene estadísticas de reproducciones totales de un artista.
     *
     * @param artistId identificador del artista
     * @return estadísticas agregadas de reproducciones
     */
    @GetMapping("/artist/{artistId}/stats")
    public ResponseEntity<EstadisticasArtistaDTO> obtenerEstadisticasArtista(@PathVariable Long artistId) {
        log.info("GET /albumes/artist/{}/stats", artistId);

        EstadisticasArtistaDTO estadisticas = albumService.obtenerEstadisticasArtista(artistId);
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Crea un nuevo álbum para el artista autenticado.
     *
     * @param dto datos del álbum a crear
     * @param authentication contexto de autenticación del usuario
     * @return álbum creado
     */
    @PostMapping
    public ResponseEntity<AlbumDTO> crearAlbum(
            @Valid @RequestBody CrearAlbumDTO dto,
            Authentication authentication) {

        Long idArtista = extraerIdArtista(authentication);
        log.info("POST /albumes - Artista: {}, Título: {}", idArtista, dto.getTituloAlbum());

        AlbumDTO album = albumService.crearAlbum(dto, idArtista);
        return ResponseEntity.status(HttpStatus.CREATED).body(album);
    }

    /**
     * Actualiza un álbum existente del artista autenticado.
     *
     * @param id identificador del álbum
     * @param dto datos actualizados del álbum
     * @param authentication contexto de autenticación del usuario
     * @return álbum actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<AlbumDTO> actualizarAlbum(
            @PathVariable Long id,
            @Valid @RequestBody EditarAlbumDTO dto,
            Authentication authentication) {

        Long idArtista = extraerIdArtista(authentication);
        log.info("PUT /albumes/{} - Artista: {}", id, idArtista);

        AlbumDTO album = albumService.actualizarAlbum(id, dto, idArtista);
        return ResponseEntity.ok(album);
    }

    /**
     * Elimina un álbum y sus recursos asociados en Cloudinary.
     *
     * @param id identificador del álbum
     * @param authentication contexto de autenticación del usuario
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAlbum(
            @PathVariable Long id,
            Authentication authentication) {

        Long idArtista = extraerIdArtista(authentication);
        log.info("DELETE /albumes/{} - Artista: {}", id, idArtista);

        albumService.eliminarAlbum(id, idArtista);
        return ResponseEntity.noContent().build();
    }

    /**
     * Añade una canción existente a un álbum con el número de pista especificado.
     *
     * @param id identificador del álbum
     * @param dto datos de la canción y número de pista
     * @param authentication contexto de autenticación del usuario
     * @return respuesta de creación exitosa
     */
    @PostMapping("/{id}/tracks")
    public ResponseEntity<Void> agregarCancionAlAlbum(
            @PathVariable Long id,
            @Valid @RequestBody AgregarCancionAlbumDTO dto,
            Authentication authentication) {

        Long idArtista = extraerIdArtista(authentication);
        log.info("POST /albumes/{}/tracks - Artista: {}, Canción: {}, Pista: {}",
                id, idArtista, dto.getIdCancion(), dto.getNumeroPista());

        albumService.agregarCancionAlAlbum(id, dto, idArtista);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Elimina una canción de un álbum sin eliminar la canción de la base de datos.
     *
     * @param id identificador del álbum
     * @param songId identificador de la canción
     * @param authentication contexto de autenticación del usuario
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}/tracks/{songId}")
    public ResponseEntity<Void> eliminarCancionDeAlbum(
            @PathVariable Long id,
            @PathVariable Long songId,
            Authentication authentication) {

        Long idArtista = extraerIdArtista(authentication);
        log.info("DELETE /albumes/{}/tracks/{} - Artista: {}", id, songId, idArtista);

        albumService.eliminarCancionDeAlbum(id, songId, idArtista);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina todos los álbumes de un artista.
     * Endpoint interno utilizado por el microservicio de Usuarios al eliminar un artista.
     *
     * @param artistId identificador del artista
     * @return respuesta sin contenido
     */
    @DeleteMapping("/artist/{artistId}")
    public ResponseEntity<Void> eliminarAlbumesArtista(@PathVariable Long artistId) {
        log.info("DELETE /albumes/artist/{} - Eliminación masiva", artistId);

        albumService.eliminarTodosAlbumesArtista(artistId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extrae el identificador del artista desde el token JWT.
     *
     * @param authentication contexto de autenticación del usuario
     * @return identificador del artista
     * @throws ForbiddenAccessException si el usuario no tiene rol de artista o falta el campo artistId
     */
    private Long extraerIdArtista(Authentication authentication) {
        Object details = authentication.getDetails();

        if (details instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> detailsMap = (Map<String, Object>) details;

            Object idArtistaObj = detailsMap.get("artistId");

            if (idArtistaObj == null) {
                log.warn("❌ Usuario sin idArtista intentó acceder a endpoint de artista");
                throw new ForbiddenAccessException("No tienes permisos de artista");
            }

            Long idArtista = Long.parseLong(String.valueOf(idArtistaObj));
            log.debug("✅ ID Artista extraído: {}", idArtista);
            return idArtista;
        }

        log.error("❌ No se pudo extraer idArtista - Details inválidos");
        throw new ForbiddenAccessException("Token inválido o sin información de artista");
    }
}