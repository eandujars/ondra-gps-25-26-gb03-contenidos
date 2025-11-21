package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.AgregarFavoritoDTO;
import com.ondra.contenidos.dto.FavoritoDTO;
import com.ondra.contenidos.dto.FavoritosPaginadosDTO;
import com.ondra.contenidos.dto.SuccessfulResponseDTO;
import com.ondra.contenidos.services.FavoritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de favoritos de usuarios.
 *
 * <p>Base URL: /api/favoritos</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/favoritos")
@RequiredArgsConstructor
public class FavoritoController {

    private final FavoritoService favoritoService;

    /**
     * Agregar una canción o álbum a favoritos
     *
     * @param dto datos del favorito a agregar
     * @param authentication información del usuario autenticado
     * @return favorito creado
     */
    @PostMapping
    public ResponseEntity<FavoritoDTO> agregarFavorito(
            @Valid @RequestBody AgregarFavoritoDTO dto,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("POST /favoritos - Usuario: {}, Tipo: {}", idUsuario, dto.getTipoContenido());

        FavoritoDTO favorito = favoritoService.agregarFavorito(idUsuario, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(favorito);
    }

    /**
     * Listar favoritos del usuario autenticado o por ID de usuario (público)
     *
     * @param idUsuario ID del usuario (opcional si está autenticado)
     * @param tipo filtro por tipo de contenido (CANCION o ALBUM)
     * @param page número de página
     * @param limit elementos por página
     * @param authentication información del usuario autenticado (puede ser null)
     * @return lista paginada de favoritos
     */
    @GetMapping
    public ResponseEntity<FavoritosPaginadosDTO> listarFavoritos(
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            Authentication authentication) {

        // Si no se proporciona idUsuario como parámetro, intentar obtenerlo de la autenticación
        Long userId = idUsuario;
        if (userId == null && authentication != null) {
            userId = obtenerIdUsuario(authentication);
        }

        // Si aún no hay userId, retornar error
        if (userId == null) {
            log.warn("GET /favoritos - No se proporcionó ID de usuario");
            return ResponseEntity.badRequest().build();
        }

        log.info("GET /favoritos - Usuario: {}, Tipo: {}, Página: {}", userId, tipo, page);

        FavoritosPaginadosDTO favoritos = favoritoService.listarFavoritos(userId, tipo, page, limit);
        return ResponseEntity.ok(favoritos);
    }

    /**
     * Eliminar una canción de favoritos
     *
     * @param idCancion ID de la canción
     * @param authentication información del usuario autenticado
     * @return respuesta exitosa
     */
    @DeleteMapping("/canciones/{idCancion}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarCancionDeFavoritos(
            @PathVariable Long idCancion,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("DELETE /favoritos/canciones/{} - Usuario: {}", idCancion, idUsuario);

        favoritoService.eliminarFavoritoCancion(idUsuario, idCancion);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Canción eliminada de favoritos")
                .statusCode(200)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build());
    }

    /**
     * Eliminar un álbum de favoritos
     *
     * @param idAlbum ID del álbum
     * @param authentication información del usuario autenticado
     * @return respuesta exitosa
     */
    @DeleteMapping("/albumes/{idAlbum}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarAlbumDeFavoritos(
            @PathVariable Long idAlbum,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("DELETE /favoritos/albumes/{} - Usuario: {}", idAlbum, idUsuario);

        favoritoService.eliminarFavoritoAlbum(idUsuario, idAlbum);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Álbum eliminado de favoritos")
                .statusCode(200)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build());
    }

    /**
     * Verificar si una canción está en favoritos
     *
     * @param idCancion ID de la canción
     * @param authentication información del usuario autenticado
     * @return true si está en favoritos, false en caso contrario
     */
    @GetMapping("/canciones/{idCancion}/check")
    public ResponseEntity<Boolean> verificarCancionFavorita(
            @PathVariable Long idCancion,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("GET /favoritos/canciones/{}/check - Usuario: {}", idCancion, idUsuario);

        boolean esFavorita = favoritoService.esCancionFavorita(idUsuario, idCancion);
        return ResponseEntity.ok(esFavorita);
    }

    /**
     * Verificar si un álbum está en favoritos
     *
     * @param idAlbum ID del álbum
     * @param authentication información del usuario autenticado
     * @return true si está en favoritos, false en caso contrario
     */
    @GetMapping("/albumes/{idAlbum}/check")
    public ResponseEntity<Boolean> verificarAlbumFavorito(
            @PathVariable Long idAlbum,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("GET /favoritos/albumes/{}/check - Usuario: {}", idAlbum, idUsuario);

        boolean esFavorito = favoritoService.esAlbumFavorito(idUsuario, idAlbum);
        return ResponseEntity.ok(esFavorito);
    }

    /**
     * Eliminar todos los favoritos de un usuario (endpoint de servicio)
     *
     * @param idUsuario ID del usuario
     * @return respuesta exitosa
     */
    @DeleteMapping("/usuarios/{idUsuario}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarFavoritosUsuario(
            @PathVariable Long idUsuario,
            @RequestHeader("X-Service-Token") String serviceToken) {

        log.info("DELETE /favoritos/usuarios/{} - Eliminación por servicio", idUsuario);

        favoritoService.eliminarTodosLosFavoritos(idUsuario);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Todos los favoritos del usuario eliminados")
                .statusCode(200)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build());
    }

    /**
     * Obtener ID del usuario desde el contexto de seguridad
     */
    private Long obtenerIdUsuario(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}