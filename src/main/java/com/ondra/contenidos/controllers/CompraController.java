package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.ComprasPaginadasDTO;
import com.ondra.contenidos.dto.SuccessfulResponseDTO;
import com.ondra.contenidos.services.CompraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Controlador REST para gestión del historial de compras.
 *
 * <p>Permite consultar el historial de compras de usuarios,
 * verificar propiedad de contenido y obtener estadísticas de gasto.</p>
 *
 * <p>Base URL: /api/compras</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    /**
     * Lista el historial de compras con filtros opcionales y paginación.
     *
     * @param idUsuario identificador del usuario (opcional si está autenticado)
     * @param tipo filtro por tipo de contenido (CANCION o ÁLBUM)
     * @param page número de página (default: 1)
     * @param limit elementos por página (default: 20)
     * @param authentication contexto de autenticación del usuario (puede ser null)
     * @return página de compras
     */
    @GetMapping
    public ResponseEntity<ComprasPaginadasDTO> listarCompras(
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
            log.warn("⚠️ GET /compras - No se proporcionó ID de usuario");
            return ResponseEntity.badRequest().build();
        }

        log.info("📋💰 GET /compras - Usuario: {}, Tipo: {}, Página: {}", userId, tipo, page);

        ComprasPaginadasDTO compras = compraService.listarCompras(userId, tipo, page, limit);
        return ResponseEntity.ok(compras);
    }

    /**
     * Verifica si el usuario ha comprado una canción específica.
     *
     * @param idCancion identificador de la canción
     * @param authentication contexto de autenticación del usuario
     * @return true si ha comprado la canción, false en caso contrario
     */
    @GetMapping("/canciones/{idCancion}/check")
    public ResponseEntity<Boolean> verificarCompraCancion(
            @PathVariable Long idCancion,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("✅🎵 GET /compras/canciones/{}/check - Usuario: {}", idCancion, idUsuario);

        boolean haComprado = compraService.haCompradoCancion(idUsuario, idCancion);
        return ResponseEntity.ok(haComprado);
    }

    /**
     * Verifica si el usuario ha comprado un álbum específico.
     *
     * @param idAlbum identificador del álbum
     * @param authentication contexto de autenticación del usuario
     * @return true si ha comprado el álbum, false en caso contrario
     */
    @GetMapping("/albumes/{idAlbum}/check")
    public ResponseEntity<Boolean> verificarCompraAlbum(
            @PathVariable Long idAlbum,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("✅💿 GET /compras/albumes/{}/check - Usuario: {}", idAlbum, idUsuario);

        boolean haComprado = compraService.haCompradoAlbum(idUsuario, idAlbum);
        return ResponseEntity.ok(haComprado);
    }

    /**
     * Obtiene el total gastado por el usuario en todas sus compras.
     *
     * @param authentication contexto de autenticación del usuario
     * @return monto total gastado
     */
    @GetMapping("/total-gastado")
    public ResponseEntity<BigDecimal> obtenerTotalGastado(Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("💰 GET /compras/total-gastado - Usuario: {}", idUsuario);

        BigDecimal totalGastado = compraService.obtenerTotalGastado(idUsuario);
        return ResponseEntity.ok(totalGastado);
    }

    /**
     * Elimina todas las compras de un usuario.
     * Endpoint interno utilizado por el microservicio de Usuarios al eliminar un usuario.
     *
     * @param idUsuario identificador del usuario
     * @param serviceToken token de autenticación entre servicios
     * @return respuesta de éxito
     */
    @DeleteMapping("/usuarios/{idUsuario}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarComprasUsuario(
            @PathVariable Long idUsuario,
            @RequestHeader("X-Service-Token") String serviceToken) {

        log.info("🗑️📚 DELETE /compras/usuarios/{} - Eliminación por servicio", idUsuario);

        compraService.eliminarTodasLasCompras(idUsuario);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Todas las compras del usuario eliminadas")
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