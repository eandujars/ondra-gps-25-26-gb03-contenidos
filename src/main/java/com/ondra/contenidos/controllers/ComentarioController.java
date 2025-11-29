package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.ComentarioDTO;
import com.ondra.contenidos.dto.ComentariosPaginadosDTO;
import com.ondra.contenidos.dto.CrearComentarioDTO;
import com.ondra.contenidos.dto.EditarComentarioDTO;
import com.ondra.contenidos.dto.SuccessfulResponseDTO;
import com.ondra.contenidos.services.ComentarioService;
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
 * Controlador REST para gestión de comentarios de usuarios y artistas.
 *
 * <p>Permite a usuarios y artistas crear, editar y eliminar comentarios en canciones y álbumes.
 * Incluye endpoints públicos de consulta y endpoints protegidos de modificación.</p>
 *
 * <p>Base URL: /api/comentarios</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    /**
     * Crea un nuevo comentario en una canción o álbum.
     *
     * @param dto datos del comentario a crear
     * @param authentication contexto de autenticación del usuario
     * @return comentario creado
     */
    @PostMapping
    public ResponseEntity<ComentarioDTO> crearComentario(
            @Valid @RequestBody CrearComentarioDTO dto,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        Long idArtista = obtenerIdArtista(authentication);
        String tipoUsuario = obtenerTipoUsuario(authentication);

        log.info("➕💬 POST /comentarios - Usuario: {}, Artista: {}, Tipo: {}",
                idUsuario, idArtista, dto.getTipoContenido());

        ComentarioDTO comentario = comentarioService.crearComentario(
                idUsuario, idArtista, tipoUsuario, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(comentario);
    }

    /**
     * Extrae el identificador del artista desde el token JWT si existe.
     *
     * @param authentication contexto de autenticación del usuario
     * @return identificador del artista o null si no es artista
     */
    @SuppressWarnings("unchecked")
    private Long obtenerIdArtista(Authentication authentication) {
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Object artistId = details.get("artistId");
        return artistId != null ? Long.valueOf(artistId.toString()) : null;
    }

    /**
     * Edita un comentario existente del usuario autenticado.
     *
     * @param idComentario identificador del comentario
     * @param dto datos actualizados del comentario
     * @param authentication contexto de autenticación del usuario
     * @return comentario actualizado
     */
    @PutMapping("/{idComentario}")
    public ResponseEntity<ComentarioDTO> editarComentario(
            @PathVariable Long idComentario,
            @Valid @RequestBody EditarComentarioDTO dto,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("✏️💬 PUT /comentarios/{} - Usuario: {}", idComentario, idUsuario);

        ComentarioDTO comentario = comentarioService.editarComentario(idComentario, idUsuario, dto);
        return ResponseEntity.ok(comentario);
    }

    /**
     * Lista los comentarios de una canción con paginación.
     *
     * @param idCancion identificador de la canción
     * @param page número de página (default: 1)
     * @param limit elementos por página (default: 20)
     * @return página de comentarios
     */
    @GetMapping("/canciones/{idCancion}")
    public ResponseEntity<ComentariosPaginadosDTO> listarComentariosCancion(
            @PathVariable Long idCancion,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        log.info("📋💬 GET /comentarios/canciones/{} - Página: {}", idCancion, page);

        ComentariosPaginadosDTO comentarios = comentarioService.listarComentariosCancion(idCancion, page, limit);
        return ResponseEntity.ok(comentarios);
    }

    /**
     * Lista los comentarios de un álbum con paginación.
     *
     * @param idAlbum identificador del álbum
     * @param page número de página (default: 1)
     * @param limit elementos por página (default: 20)
     * @return página de comentarios
     */
    @GetMapping("/albumes/{idAlbum}")
    public ResponseEntity<ComentariosPaginadosDTO> listarComentariosAlbum(
            @PathVariable Long idAlbum,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        log.info("📋💬 GET /comentarios/albumes/{} - Página: {}", idAlbum, page);

        ComentariosPaginadosDTO comentarios = comentarioService.listarComentariosAlbum(idAlbum, page, limit);
        return ResponseEntity.ok(comentarios);
    }

    /**
     * Lista los comentarios publicados por un usuario específico.
     *
     * @param idUsuario identificador del usuario
     * @param page número de página (default: 1)
     * @param limit elementos por página (default: 20)
     * @param authentication contexto de autenticación del usuario (puede ser null)
     * @return página de comentarios
     */
    @GetMapping("/usuarios/{idUsuario}")
    public ResponseEntity<ComentariosPaginadosDTO> listarComentariosUsuario(
            @PathVariable Long idUsuario,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            Authentication authentication) {

        log.info("📋💬 GET /comentarios/usuarios/{} - Página: {}", idUsuario, page);

        ComentariosPaginadosDTO comentarios = comentarioService.listarComentariosUsuario(idUsuario, page, limit);
        return ResponseEntity.ok(comentarios);
    }

    /**
     * Lista los comentarios del usuario autenticado.
     *
     * @param page número de página (default: 1)
     * @param limit elementos por página (default: 20)
     * @param authentication contexto de autenticación del usuario
     * @return página de comentarios
     */
    @GetMapping("/mis-comentarios")
    public ResponseEntity<ComentariosPaginadosDTO> listarMisComentarios(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("📋💬 GET /comentarios/mis-comentarios - Usuario: {}, Página: {}", idUsuario, page);

        ComentariosPaginadosDTO comentarios = comentarioService.listarComentariosUsuario(idUsuario, page, limit);
        return ResponseEntity.ok(comentarios);
    }

    /**
     * Elimina un comentario.
     * Puede eliminarlo el autor del comentario o el propietario del contenido.
     *
     * @param idComentario identificador del comentario
     * @param authentication contexto de autenticación del usuario
     * @return respuesta de éxito
     */
    @DeleteMapping("/{idComentario}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarComentario(
            @PathVariable Long idComentario,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        String tipoUsuario = obtenerTipoUsuario(authentication);
        log.info("🗑️💬 DELETE /comentarios/{} - Usuario: {}", idComentario, idUsuario);

        comentarioService.eliminarComentario(idComentario, idUsuario, tipoUsuario);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Comentario eliminado correctamente")
                .statusCode(200)
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    /**
     * Elimina todos los comentarios de un usuario.
     * Endpoint interno utilizado por el microservicio de Usuarios al eliminar un usuario.
     *
     * @param idUsuario identificador del usuario
     * @param serviceToken token de autenticación entre servicios
     * @return respuesta de éxito
     */
    @DeleteMapping("/usuarios/{idUsuario}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarComentariosUsuario(
            @PathVariable Long idUsuario,
            @RequestHeader("X-Service-Token") String serviceToken) {

        log.info("🗑️📚 DELETE /comentarios/usuarios/{} - Eliminación por servicio", idUsuario);

        comentarioService.eliminarTodosLosComentarios(idUsuario);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Todos los comentarios del usuario eliminados")
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