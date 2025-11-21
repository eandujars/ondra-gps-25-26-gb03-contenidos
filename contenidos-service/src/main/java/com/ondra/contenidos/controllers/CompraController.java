package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.ComprasPaginadasDTO;
import com.ondra.contenidos.dto.SuccessfulResponseDTO;
import com.ondra.contenidos.services.CompraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controlador REST para gestión del historial de compras.
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
     * Listar historial de compras del usuario autenticado o por ID de usuario
     *
     * @param idUsuario ID del usuario (opcional si está autenticado)
     * @param tipo filtro por tipo de contenido (CANCION o ALBUM)
     * @param page número de página
     * @param limit elementos por página
     * @param authentication información del usuario autenticado (puede ser null)
     * @return lista paginada de compras
     */
    @GetMapping
    public ResponseEntity<ComprasPaginadasDTO> listarCompras(
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
            log.warn("GET /compras - No se proporcionó ID de usuario");
            return ResponseEntity.badRequest().build();
        }

        log.info("GET /compras - Usuario: {}, Tipo: {}, Página: {}", userId, tipo, page);

        ComprasPaginadasDTO compras = compraService.listarCompras(userId, tipo, page, limit);
        return ResponseEntity.ok(compras);
    }

    /**
     * Verificar si el usuario ha comprado una canción
     *
     * @param idCancion ID de la canción
     * @param authentication información del usuario autenticado
     * @return true si ha comprado la canción, false en caso contrario
     */
    @GetMapping("/canciones/{idCancion}/check")
    public ResponseEntity<Boolean> verificarCompraCancion(
            @PathVariable Long idCancion,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("GET /compras/canciones/{}/check - Usuario: {}", idCancion, idUsuario);

        boolean haComprado = compraService.haCompradoCancion(idUsuario, idCancion);
        return ResponseEntity.ok(haComprado);
    }

    /**
     * Verificar si el usuario ha comprado un álbum
     *
     * @param idAlbum ID del álbum
     * @param authentication información del usuario autenticado
     * @return true si ha comprado el álbum, false en caso contrario
     */
    @GetMapping("/albumes/{idAlbum}/check")
    public ResponseEntity<Boolean> verificarCompraAlbum(
            @PathVariable Long idAlbum,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("GET /compras/albumes/{}/check - Usuario: {}", idAlbum, idUsuario);

        boolean haComprado = compraService.haCompradoAlbum(idUsuario, idAlbum);
        return ResponseEntity.ok(haComprado);
    }

    /**
     * Obtener total gastado por el usuario
     *
     * @param authentication información del usuario autenticado
     * @return total gastado
     */
    @GetMapping("/total-gastado")
    public ResponseEntity<BigDecimal> obtenerTotalGastado(Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("GET /compras/total-gastado - Usuario: {}", idUsuario);

        BigDecimal totalGastado = compraService.obtenerTotalGastado(idUsuario);
        return ResponseEntity.ok(totalGastado);
    }

    /**
     * Eliminar todas las compras de un usuario (endpoint de servicio)
     *
     * @param idUsuario ID del usuario
     * @return respuesta exitosa
     */
    @DeleteMapping("/usuarios/{idUsuario}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarComprasUsuario(
            @PathVariable Long idUsuario,
            @RequestHeader("X-Service-Token") String serviceToken) {

        log.info("DELETE /compras/usuarios/{} - Eliminación por servicio", idUsuario);

        compraService.eliminarTodasLasCompras(idUsuario);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Todas las compras del usuario eliminadas")
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