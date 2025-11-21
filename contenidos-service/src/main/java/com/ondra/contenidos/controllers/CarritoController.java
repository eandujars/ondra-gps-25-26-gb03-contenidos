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

/**
 * Controlador REST para gestión del carrito de compra.
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
     * Obtener carrito del usuario autenticado
     *
     * @param authentication información del usuario autenticado
     * @return carrito del usuario
     */
    @GetMapping
    public ResponseEntity<CarritoDTO> obtenerCarrito(Authentication authentication) {
        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("GET /carrito - Usuario: {}", idUsuario);

        CarritoDTO carrito = carritoService.obtenerCarrito(idUsuario);
        return ResponseEntity.ok(carrito);
    }

    /**
     * Agregar item al carrito
     *
     * @param dto datos del item a agregar
     * @param authentication información del usuario autenticado
     * @return carrito actualizado
     */
    @PostMapping("/items")
    public ResponseEntity<CarritoDTO> agregarItem(
            @Valid @RequestBody AgregarAlCarritoDTO dto,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("POST /carrito/items - Usuario: {}, Tipo: {}", idUsuario, dto.getTipoProducto());

        CarritoDTO carrito = carritoService.agregarItem(idUsuario, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(carrito);
    }

    /**
     * Eliminar item del carrito
     *
     * @param idCarritoItem ID del item a eliminar
     * @param authentication información del usuario autenticado
     * @return carrito actualizado
     */
    @DeleteMapping("/items/{idCarritoItem}")
    public ResponseEntity<CarritoDTO> eliminarItem(
            @PathVariable Long idCarritoItem,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("DELETE /carrito/items/{} - Usuario: {}", idCarritoItem, idUsuario);

        CarritoDTO carrito = carritoService.eliminarItem(idUsuario, idCarritoItem);
        return ResponseEntity.ok(carrito);
    }

    /**
     * Vaciar el carrito
     *
     * @param authentication información del usuario autenticado
     * @return respuesta exitosa
     */
    @DeleteMapping
    public ResponseEntity<SuccessfulResponseDTO> vaciarCarrito(Authentication authentication) {
        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("DELETE /carrito - Usuario: {}", idUsuario);

        carritoService.vaciarCarrito(idUsuario);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Carrito vaciado exitosamente")
                .statusCode(200)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build());
    }

    /**
     * Finalizar compra
     *
     * @param idMetodoPago ID del método de pago del usuario (desde microservicio usuarios)
     * @param authentication información del usuario autenticado
     * @return respuesta exitosa
     */
    @PostMapping("/checkout")
    public ResponseEntity<SuccessfulResponseDTO> finalizarCompra(
            @RequestParam Long idMetodoPago,
            Authentication authentication) {

        Long idUsuario = obtenerIdUsuario(authentication);
        log.info("POST /carrito/checkout - Usuario: {}, Método de pago: {}", idUsuario, idMetodoPago);

        carritoService.finalizarCompra(idUsuario, idMetodoPago);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Compra finalizada exitosamente")
                .statusCode(200)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build());
    }

    /**
     * Eliminar el carrito de un usuario (endpoint de servicio)
     *
     * @param idUsuario ID del usuario
     * @return respuesta exitosa
     */
    @DeleteMapping("/usuarios/{idUsuario}")
    public ResponseEntity<SuccessfulResponseDTO> eliminarCarritoUsuario(
            @PathVariable Long idUsuario,
            @RequestHeader("X-Service-Token") String serviceToken) {

        log.info("DELETE /carrito/usuarios/{} - Eliminación por servicio", idUsuario);

        carritoService.eliminarCarrito(idUsuario);
        return ResponseEntity.ok(SuccessfulResponseDTO.builder()
                .successful("SUCCESS")
                .message("Carrito del usuario eliminado")
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