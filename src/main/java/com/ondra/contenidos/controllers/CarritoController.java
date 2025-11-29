package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.AgregarAlCarritoDTO;
import com.ondra.contenidos.dto.CarritoDTO;
import com.ondra.contenidos.dto.SuccessfulResponseDTO;
import com.ondra.contenidos.services.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controlador REST para gestión del carrito de compra.
 *
 * <p>Permite a usuarios autenticados gestionar items en su carrito,
 * visualizar su contenido y proceder al checkout de la compra.</p>
 *
 * <p>Base URL: /api/carrito</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    /**
     * Obtiene el carrito del usuario autenticado con sus items y total.
     *
     * @param authentication contexto de autenticación del usuario
     * @return carrito con items y precio total
     */
    @GetMapping
    public ResponseEntity<CarritoDTO> obtenerCarrito(Authentication authentication) {
        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("🛒 GET /carrito - Usuario: {}", idUsuario);

        CarritoDTO carrito = carritoService.obtenerCarrito(idUsuario);
        return ResponseEntity.ok(carrito);
    }

    /**
     * Agrega un item al carrito del usuario.
     *
     * @param dto datos del item a agregar (canción o álbum)
     * @param authentication contexto de autenticación del usuario
     * @return carrito actualizado
     */
    @PostMapping("/items")
    public ResponseEntity<CarritoDTO> agregarItem(
            @Valid @RequestBody AgregarAlCarritoDTO dto,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("➕🛒 POST /carrito/items - Usuario: {}, Tipo: {}", idUsuario, dto.getTipoProducto());

        CarritoDTO carrito = carritoService.agregarItem(idUsuario, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(carrito);
    }

    /**
     * Elimina un item específico del carrito.
     *
     * @param idCarritoItem identificador del item en el carrito
     * @param authentication contexto de autenticación del usuario
     * @return carrito actualizado sin el item eliminado
     */
    @DeleteMapping("/items/{idCarritoItem}")
    public ResponseEntity<CarritoDTO> eliminarItem(
            @PathVariable Long idCarritoItem,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("🗑️🛒 DELETE /carrito/items/{} - Usuario: {}", idCarritoItem, idUsuario);

        CarritoDTO carrito = carritoService.eliminarItem(idUsuario, idCarritoItem);
        return ResponseEntity.ok(carrito);
    }

    /**
     * Vacía completamente el carrito del usuario.
     *
     * @param authentication contexto de autenticación del usuario
     * @return respuesta de éxito
     */
    @DeleteMapping
    public ResponseEntity<SuccessfulResponseDTO> vaciarCarrito(Authentication authentication) {
        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("🗑️🛒 DELETE /carrito - Usuario: {}", idUsuario);

        carritoService.vaciarCarrito(idUsuario);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Carrito vaciado exitosamente")
                .statusCode(200)
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    /**
     * Finaliza la compra procesando el pago y vaciando el carrito.
     *
     * @param idMetodoPago identificador del método de pago del usuario
     * @param authentication contexto de autenticación del usuario
     * @return respuesta de éxito
     */
    @PostMapping("/checkout")
    public ResponseEntity<SuccessfulResponseDTO> finalizarCompra(
            @RequestParam(required = false) Long idMetodoPago,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("💳 POST /carrito/checkout - Usuario: {}, Método de pago: {}", idUsuario, idMetodoPago);

        carritoService.finalizarCompra(idUsuario, idMetodoPago);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Compra finalizada exitosamente")
                .statusCode(200)
                .timestamp(LocalDateTime.now().toString())
                .build());
    }

    /**
     * Elimina el carrito de un usuario.
     * Endpoint interno utilizado por el microservicio de Usuarios al eliminar un usuario.
     *
     * @param idUsuario identificador del usuario
     * @param serviceToken token de autenticación entre servicios
     * @return respuesta de éxito
     */
    @DeleteMapping("/usuarios/{idUsuario}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarCarritoUsuario(
            @PathVariable Long idUsuario,
            @RequestHeader("X-Service-Token") String serviceToken) {

        log.info("🗑️🛒 DELETE /carrito/usuarios/{} - Eliminación por servicio", idUsuario);

        carritoService.eliminarCarrito(idUsuario);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Carrito del usuario eliminado")
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