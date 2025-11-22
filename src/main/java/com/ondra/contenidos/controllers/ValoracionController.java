package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.CrearValoracionDTO;
import com.ondra.contenidos.dto.EditarValoracionDTO;
import com.ondra.contenidos.dto.SuccessfulResponseDTO;
import com.ondra.contenidos.dto.ValoracionDTO;
import com.ondra.contenidos.dto.ValoracionesPaginadasDTO;
import com.ondra.contenidos.services.ValoracionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controlador REST para gestión de valoraciones de usuarios y artistas.
 *
 * <p>Permite crear, consultar, editar y eliminar valoraciones de canciones y álbumes.
 * Las valoraciones incluyen puntuación de 1 a 5 estrellas y comentario opcional.</p>
 *
 * <p>Base URL: /api/valoraciones</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/valoraciones")
@RequiredArgsConstructor
public class ValoracionController {

    private final ValoracionService valoracionService;

    /**
     * Crea una nueva valoración para una canción o álbum.
     *
     * @param dto datos de la valoración a crear
     * @param authentication contexto de autenticación del usuario
     * @return valoración creada
     */
    @PostMapping
    public ResponseEntity<ValoracionDTO> crearValoracion(
            @Valid @RequestBody CrearValoracionDTO dto,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        String tipoUsuario = obtenerTipoUsuario(authentication);
        log.info("➕⭐ POST /valoraciones - Usuario: {}, Tipo: {}", idUsuario, dto.getTipoContenido());

        ValoracionDTO valoracion = valoracionService.crearValoracion(idUsuario, tipoUsuario, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(valoracion);
    }

    /**
     * Edita una valoración existente del usuario autenticado.
     *
     * @param idValoracion identificador de la valoración
     * @param dto datos actualizados de la valoración
     * @param authentication contexto de autenticación del usuario
     * @return valoración actualizada
     */
    @PutMapping("/{idValoracion}")
    public ResponseEntity<ValoracionDTO> editarValoracion(
            @PathVariable Long idValoracion,
            @Valid @RequestBody EditarValoracionDTO dto,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("✏️⭐ PUT /valoraciones/{} - Usuario: {}", idValoracion, idUsuario);

        ValoracionDTO valoracion = valoracionService.editarValoracion(idValoracion, idUsuario, dto);
        return ResponseEntity.ok(valoracion);
    }

    /**
     * Obtiene la valoración del usuario autenticado para una canción específica.
     *
     * @param idCancion identificador de la canción
     * @param authentication contexto de autenticación del usuario
     * @return valoración del usuario o 204 si no existe
     */
    @GetMapping("/canciones/{idCancion}/mi-valoracion")
    public ResponseEntity<ValoracionDTO> obtenerMiValoracionCancion(
            @PathVariable Long idCancion,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("🔍⭐ GET /valoraciones/canciones/{}/mi-valoracion - Usuario: {}", idCancion, idUsuario);

        ValoracionDTO valoracion = valoracionService.obtenerValoracionUsuarioCancion(idUsuario, idCancion);
        if (valoracion == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(valoracion);
    }

    /**
     * Obtiene la valoración del usuario autenticado para un álbum específico.
     *
     * @param idAlbum identificador del álbum
     * @param authentication contexto de autenticación del usuario
     * @return valoración del usuario o 204 si no existe
     */
    @GetMapping("/albumes/{idAlbum}/mi-valoracion")
    public ResponseEntity<ValoracionDTO> obtenerMiValoracionAlbum(
            @PathVariable Long idAlbum,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("🔍⭐ GET /valoraciones/albumes/{}/mi-valoracion - Usuario: {}", idAlbum, idUsuario);

        ValoracionDTO valoracion = valoracionService.obtenerValoracionUsuarioAlbum(idUsuario, idAlbum);
        if (valoracion == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(valoracion);
    }

    /**
     * Lista las valoraciones de una canción con paginación.
     *
     * @param idCancion identificador de la canción
     * @param page número de página (default: 1)
     * @param limit elementos por página (default: 20)
     * @return página de valoraciones con promedio
     */
    @GetMapping("/canciones/{idCancion}")
    public ResponseEntity<ValoracionesPaginadasDTO> listarValoracionesCancion(
            @PathVariable Long idCancion,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        log.info("📋⭐ GET /valoraciones/canciones/{} - Página: {}", idCancion, page);

        ValoracionesPaginadasDTO valoraciones = valoracionService.listarValoracionesCancion(idCancion, page, limit);
        return ResponseEntity.ok(valoraciones);
    }

    /**
     * Lista las valoraciones de un álbum con paginación.
     *
     * @param idAlbum identificador del álbum
     * @param page número de página (default: 1)
     * @param limit elementos por página (default: 20)
     * @return página de valoraciones con promedio
     */
    @GetMapping("/albumes/{idAlbum}")
    public ResponseEntity<ValoracionesPaginadasDTO> listarValoracionesAlbum(
            @PathVariable Long idAlbum,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        log.info("📋⭐ GET /valoraciones/albumes/{} - Página: {}", idAlbum, page);

        ValoracionesPaginadasDTO valoraciones = valoracionService.listarValoracionesAlbum(idAlbum, page, limit);
        return ResponseEntity.ok(valoraciones);
    }

    /**
     * Obtiene la valoración promedio de una canción.
     *
     * @param idCancion identificador de la canción
     * @return objeto con valoración promedio y metadatos
     */
    @GetMapping("/canciones/{idCancion}/promedio")
    public ResponseEntity<Map<String, Object>> obtenerPromedioCancion(@PathVariable Long idCancion) {
        log.info("📊⭐ GET /valoraciones/canciones/{}/promedio", idCancion);

        Double promedio = valoracionService.obtenerPromedioCancion(idCancion);
        return ResponseEntity.ok(Map.of(
                "idCancion", idCancion,
                "valoracionPromedio", promedio != null ? promedio : 0.0,
                "tieneValoraciones", promedio != null
        ));
    }

    /**
     * Obtiene la valoración promedio de un álbum.
     *
     * @param idAlbum identificador del álbum
     * @return objeto con valoración promedio y metadatos
     */
    @GetMapping("/albumes/{idAlbum}/promedio")
    public ResponseEntity<Map<String, Object>> obtenerPromedioAlbum(@PathVariable Long idAlbum) {
        log.info("📊⭐ GET /valoraciones/albumes/{}/promedio", idAlbum);

        Double promedio = valoracionService.obtenerPromedioAlbum(idAlbum);
        return ResponseEntity.ok(Map.of(
                "idAlbum", idAlbum,
                "valoracionPromedio", promedio != null ? promedio : 0.0,
                "tieneValoraciones", promedio != null
        ));
    }

    /**
     * Lista las valoraciones realizadas por un usuario específico.
     *
     * @param idUsuario identificador del usuario
     * @param page número de página (default: 1)
     * @param limit elementos por página (default: 20)
     * @param authentication contexto de autenticación del usuario (puede ser null)
     * @return página de valoraciones
     */
    @GetMapping("/usuarios/{idUsuario}")
    public ResponseEntity<ValoracionesPaginadasDTO> listarValoracionesUsuario(
            @PathVariable Long idUsuario,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            Authentication authentication) {

        log.info("📋⭐ GET /valoraciones/usuarios/{} - Página: {}", idUsuario, page);

        ValoracionesPaginadasDTO valoraciones = valoracionService.listarValoracionesUsuario(idUsuario, page, limit);
        return ResponseEntity.ok(valoraciones);
    }

    /**
     * Lista las valoraciones del usuario autenticado.
     *
     * @param page número de página (default: 1)
     * @param limit elementos por página (default: 20)
     * @param authentication contexto de autenticación del usuario
     * @return página de valoraciones
     */
    @GetMapping("/mis-valoraciones")
    public ResponseEntity<ValoracionesPaginadasDTO> listarMisValoraciones(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("📋⭐ GET /valoraciones/mis-valoraciones - Usuario: {}, Página: {}", idUsuario, page);

        ValoracionesPaginadasDTO valoraciones = valoracionService.listarValoracionesUsuario(idUsuario, page, limit);
        return ResponseEntity.ok(valoraciones);
    }

    /**
     * Elimina una valoración.
     * Puede eliminarlo el autor de la valoración o el propietario del contenido.
     *
     * @param idValoracion identificador de la valoración
     * @param authentication contexto de autenticación del usuario
     * @return respuesta de éxito
     */
    @DeleteMapping("/{idValoracion}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarValoracion(
            @PathVariable Long idValoracion,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        String tipoUsuario = obtenerTipoUsuario(authentication);
        log.info("🗑️⭐ DELETE /valoraciones/{} - Usuario: {}", idValoracion, idUsuario);

        valoracionService.eliminarValoracion(idValoracion, idUsuario, tipoUsuario);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Valoración eliminada correctamente")
                .statusCode(200)
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    /**
     * Elimina todas las valoraciones de un usuario.
     * Endpoint interno utilizado por el microservicio de Usuarios al eliminar un usuario.
     *
     * @param idUsuario identificador del usuario
     * @param serviceToken token de autenticación entre servicios
     * @return respuesta de éxito
     */
    @DeleteMapping("/usuarios/{idUsuario}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarValoracionesUsuario(
            @PathVariable Long idUsuario,
            @RequestHeader("X-Service-Token") String serviceToken) {

        log.info("🗑️📚 DELETE /valoraciones/usuarios/{} - Eliminación por servicio", idUsuario);

        valoracionService.eliminarTodasLasValoraciones(idUsuario);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Todas las valoraciones del usuario eliminadas")
                .statusCode(200)
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    /**
     * Extrae el identificador del usuario desde el token JWT.
     *
     * @param authentication contexto de autenticación del usuario
     * @return identificador del usuario
     */
    private Long obtenerIdUsuario(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    /**
     * Extrae el tipo de usuario desde el token JWT.
     *
     * @param authentication contexto de autenticación del usuario
     * @return tipo de usuario (USUARIO o ARTISTA)
     */
    @SuppressWarnings("unchecked")
    private String obtenerTipoUsuario(Authentication authentication) {
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        return (String) details.get("tipoUsuario");
    }
}