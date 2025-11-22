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

import java.time.LocalDateTime;

/**
 * Controlador REST para gestión de favoritos de usuarios.
 *
 * <p>Permite a usuarios agregar canciones y álbumes a su lista de favoritos,
 * consultar su colección y verificar el estado de favorito de contenidos específicos.</p>
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
     * Agrega una canción o álbum a la lista de favoritos del usuario.
     *
     * @param dto datos del contenido a agregar a favoritos
     * @param authentication contexto de autenticación del usuario
     * @return favorito creado
     */
    @PostMapping
    public ResponseEntity<FavoritoDTO> agregarFavorito(
            @Valid @RequestBody AgregarFavoritoDTO dto,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("➕⭐ POST /favoritos - Usuario: {}, Tipo: {}", idUsuario, dto.getTipoContenido());

        FavoritoDTO favorito = favoritoService.agregarFavorito(idUsuario, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(favorito);
    }

    /**
     * Lista los favoritos de un usuario con filtros opcionales y paginación.
     *
     * @param idUsuario identificador del usuario (opcional si está autenticado)
     * @param tipo filtro por tipo de contenido (CANCION o ALBUM)
     * @param page número de página (default: 1)
     * @param limit elementos por página (default: 20)
     * @param authentication contexto de autenticación del usuario (puede ser null)
     * @return página de favoritos
     */
    @GetMapping
    public ResponseEntity<FavoritosPaginadosDTO> listarFavoritos(
            @RequestParam(required = false) Long idUsuario,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            Authentication authentication) {

        Long userId = idUsuario;
        if (userId == null && authentication != null) {
            userId = obtenerIdUsuario(authentication);
        }

        if (userId == null) {
            log.warn("⚠️ GET /favoritos - No se proporcionó ID de usuario");
            return ResponseEntity.badRequest().build();
        }

        log.info("📋⭐ GET /favoritos - Usuario: {}, Tipo: {}, Página: {}", userId, tipo, page);

        FavoritosPaginadosDTO favoritos = favoritoService.listarFavoritos(userId, tipo, page, limit);
        return ResponseEntity.ok(favoritos);
    }

    /**
     * Elimina una canción de la lista de favoritos.
     *
     * @param idCancion identificador de la canción
     * @param authentication contexto de autenticación del usuario
     * @return respuesta de éxito
     */
    @DeleteMapping("/canciones/{idCancion}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarCancionDeFavoritos(
            @PathVariable Long idCancion,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("🗑️⭐🎵 DELETE /favoritos/canciones/{} - Usuario: {}", idCancion, idUsuario);

        favoritoService.eliminarFavoritoCancion(idUsuario, idCancion);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Canción eliminada de favoritos")
                .statusCode(200)
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    /**
     * Elimina un álbum de la lista de favoritos.
     *
     * @param idAlbum identificador del álbum
     * @param authentication contexto de autenticación del usuario
     * @return respuesta de éxito
     */
    @DeleteMapping("/albumes/{idAlbum}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarAlbumDeFavoritos(
            @PathVariable Long idAlbum,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("🗑️⭐💿 DELETE /favoritos/albumes/{} - Usuario: {}", idAlbum, idUsuario);

        favoritoService.eliminarFavoritoAlbum(idUsuario, idAlbum);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Álbum eliminado de favoritos")
                .statusCode(200)
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    /**
     * Verifica si una canción está en la lista de favoritos del usuario.
     *
     * @param idCancion identificador de la canción
     * @param authentication contexto de autenticación del usuario
     * @return true si está en favoritos, false en caso contrario
     */
    @GetMapping("/canciones/{idCancion}/check")
    public ResponseEntity<Boolean> verificarCancionFavorita(
            @PathVariable Long idCancion,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("✅⭐🎵 GET /favoritos/canciones/{}/check - Usuario: {}", idCancion, idUsuario);

        boolean esFavorita = favoritoService.esCancionFavorita(idUsuario, idCancion);
        return ResponseEntity.ok(esFavorita);
    }

    /**
     * Verifica si un álbum está en la lista de favoritos del usuario.
     *
     * @param idAlbum identificador del álbum
     * @param authentication contexto de autenticación del usuario
     * @return true si está en favoritos, false en caso contrario
     */
    @GetMapping("/albumes/{idAlbum}/check")
    public ResponseEntity<Boolean> verificarAlbumFavorito(
            @PathVariable Long idAlbum,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("✅⭐💿 GET /favoritos/albumes/{}/check - Usuario: {}", idAlbum, idUsuario);

        boolean esFavorito = favoritoService.esAlbumFavorito(idUsuario, idAlbum);
        return ResponseEntity.ok(esFavorito);
    }

    /**
     * Elimina todos los favoritos de un usuario.
     * Endpoint interno utilizado por el microservicio de Usuarios al eliminar un usuario.
     *
     * @param idUsuario identificador del usuario
     * @param serviceToken token de autenticación entre servicios
     * @return respuesta de éxito
     */
    @DeleteMapping("/usuarios/{idUsuario}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarFavoritosUsuario(
            @PathVariable Long idUsuario,
            @RequestHeader("X-Service-Token") String serviceToken) {

        log.info("🗑️📚 DELETE /favoritos/usuarios/{} - Eliminación por servicio", idUsuario);

        favoritoService.eliminarTodosLosFavoritos(idUsuario);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Todos los favoritos del usuario eliminados")
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
}