package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.ForbiddenAccessException;
import com.ondra.contenidos.services.CancionService;
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
 * Controlador REST para gestión de canciones.
 *
 * <p>Proporciona endpoints públicos para consulta y reproducción de canciones,
 * endpoints protegidos para artistas que permiten crear, modificar y eliminar canciones,
 * y endpoints internos para comunicación entre microservicios.</p>
 *
 * <p>Base URL: /api/canciones</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/canciones")
@RequiredArgsConstructor
public class CancionController {

    private final CancionService cancionService;

    /**
     * Lista canciones con filtros opcionales y paginación.
     *
     * @param artistId filtro por artista
     * @param genreId filtro por género
     * @param search búsqueda por título o descripción
     * @param orderBy criterio de ordenación (most_recent, oldest, most_played, best_rated, price_asc, price_desc)
     * @param page número de página (1-indexed)
     * @param limit elementos por página (default: 20, max: 100)
     * @return página de canciones con metadatos de paginación
     */
    @GetMapping
    public ResponseEntity<CancionesPaginadasDTO> listarCanciones(
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        log.info("📋 GET /canciones - artistId: {}, genreId: {}, search: {}, orderBy: {}, page: {}, limit: {}",
                artistId, genreId, search, orderBy, page, limit);

        CancionesPaginadasDTO resultado = cancionService.listarCanciones(
                artistId, genreId, search, orderBy, page, limit);

        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtiene el detalle completo de una canción.
     *
     * @param id identificador de la canción
     * @return detalle de la canción
     */
    @GetMapping("/{id}")
    public ResponseEntity<CancionDetalleDTO> obtenerCancion(@PathVariable Long id) {
        log.info("🔍 GET /canciones/{}", id);

        CancionDetalleDTO cancion = cancionService.obtenerCancionPorId(id);
        return ResponseEntity.ok(cancion);
    }

    /**
     * Obtiene todas las canciones publicadas por un artista específico.
     *
     * @param artistId identificador del artista
     * @return lista de canciones del artista
     */
    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<CancionDTO>> obtenerCancionesPorArtista(@PathVariable Long artistId) {
        log.info("🎤 GET /canciones/artist/{}", artistId);

        List<CancionDTO> canciones = cancionService.listarCancionesPorArtista(artistId);
        return ResponseEntity.ok(canciones);
    }

    /**
     * Obtiene las canciones que pertenecen a un álbum específico.
     *
     * @param albumId identificador del álbum
     * @return lista de canciones del álbum
     */
    @GetMapping("/album/{albumId}")
    public ResponseEntity<List<CancionDTO>> obtenerCancionesPorAlbum(@PathVariable Long albumId) {
        log.info("💿 GET /canciones/album/{}", albumId);

        List<CancionDTO> canciones = cancionService.listarCancionesPorAlbum(albumId);
        return ResponseEntity.ok(canciones);
    }

    /**
     * Busca canciones por término de búsqueda en título y descripción.
     *
     * @param q término de búsqueda
     * @return lista de canciones coincidentes
     */
    @GetMapping("/search")
    public ResponseEntity<List<CancionDTO>> buscarCanciones(@RequestParam String q) {
        log.info("🔎 GET /canciones/search?q={}", q);

        List<CancionDTO> canciones = cancionService.buscarCanciones(q);
        return ResponseEntity.ok(canciones);
    }

    /**
     * Obtiene canciones disponibles de forma gratuita.
     *
     * @return lista de canciones con precio 0.00
     */
    @GetMapping("/gratuitas")
    public ResponseEntity<List<CancionDTO>> obtenerCancionesGratuitas() {
        log.info("💸 GET /canciones/gratuitas");

        List<CancionDTO> canciones = cancionService.listarCancionesGratuitas();
        return ResponseEntity.ok(canciones);
    }

    /**
     * Obtiene estadísticas globales del sistema de canciones.
     *
     * @return estadísticas agregadas del catálogo
     */
    @GetMapping("/stats")
    public ResponseEntity<CancionesStatsDTO> obtenerEstadisticas() {
        log.info("📊 GET /canciones/stats");

        CancionesStatsDTO stats = cancionService.obtenerEstadisticas();
        return ResponseEntity.ok(stats);
    }

    /**
     * Registra una reproducción de canción e incrementa el contador.
     *
     * @param id identificador de la canción
     * @return datos de la reproducción registrada con total actualizado
     */
    @PostMapping("/{id}/reproducir")
    public ResponseEntity<ReproduccionResponseDTO> registrarReproduccion(@PathVariable Long id) {
        log.info("▶️ POST /canciones/{}/reproducir", id);

        ReproduccionResponseDTO response = cancionService.registrarReproduccion(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Crea una nueva canción para el artista autenticado.
     *
     * @param dto datos de la canción a crear
     * @param authentication contexto de autenticación del usuario
     * @return canción creada
     */
    @PostMapping
    public ResponseEntity<CancionDTO> crearCancion(
            @Valid @RequestBody CrearCancionDTO dto,
            Authentication authentication) {

        Long idArtista = extraerIdArtista(authentication);
        log.info("➕ POST /canciones - Artista: {}, Título: {}", idArtista, dto.getTituloCancion());

        CancionDTO cancion = cancionService.crearCancion(dto, idArtista);
        return ResponseEntity.status(HttpStatus.CREATED).body(cancion);
    }

    /**
     * Actualiza una canción existente del artista autenticado.
     *
     * @param id identificador de la canción
     * @param dto datos actualizados de la canción
     * @param authentication contexto de autenticación del usuario
     * @return canción actualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<CancionDTO> actualizarCancion(
            @PathVariable Long id,
            @Valid @RequestBody EditarCancionDTO dto,
            Authentication authentication) {

        Long idArtista = extraerIdArtista(authentication);
        log.info("✏️ PUT /canciones/{} - Artista: {}", id, idArtista);

        CancionDTO cancion = cancionService.actualizarCancion(id, dto, idArtista);
        return ResponseEntity.ok(cancion);
    }

    /**
     * Elimina una canción y sus recursos asociados en Cloudinary.
     *
     * @param id identificador de la canción
     * @param authentication contexto de autenticación del usuario
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCancion(
            @PathVariable Long id,
            Authentication authentication) {

        Long idArtista = extraerIdArtista(authentication);
        log.info("🗑️ DELETE /canciones/{} - Artista: {}", id, idArtista);

        cancionService.eliminarCancion(id, idArtista);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina todas las canciones de un artista.
     * Endpoint interno utilizado por el microservicio de Usuarios al eliminar un artista.
     *
     * @param artistId identificador del artista
     * @return respuesta sin contenido
     */
    @DeleteMapping("/artist/{artistId}")
    public ResponseEntity<Void> eliminarCancionesArtista(@PathVariable Long artistId) {
        log.info("🗑️📚 DELETE /canciones/artist/{} - Eliminación masiva", artistId);

        cancionService.eliminarTodasCancionesArtista(artistId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene estadísticas de reproducciones de un artista.
     * Endpoint interno utilizado por el microservicio de Usuarios.
     *
     * @param artistId identificador del artista
     * @return estadísticas agregadas de reproducciones
     */
    @GetMapping("/artist/{artistId}/stats")
    public ResponseEntity<EstadisticasArtistaDTO> obtenerEstadisticasArtista(@PathVariable Long artistId) {
        log.info("📊 GET /canciones/artist/{}/stats", artistId);

        Long totalReproducciones = cancionService.obtenerTotalReproduccionesArtista(artistId);

        EstadisticasArtistaDTO stats = EstadisticasArtistaDTO.builder()
                .idArtista(artistId)
                .totalReproducciones(totalReproducciones)
                .build();

        return ResponseEntity.ok(stats);
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