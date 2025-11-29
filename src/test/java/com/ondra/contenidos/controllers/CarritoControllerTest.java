package com.ondra.contenidos.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.security.*;
import com.ondra.contenidos.services.CarritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarritoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
@TestPropertySource(properties = {"service.token=e3e27aa2f289a9686be1d3c45659f308748be2d8751c4911b1cecb09fdd228fa"})
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockitoBean
    private CarritoService carritoService;

    @Value("${service.token}")
    private String serviceToken;

    private CarritoDTO carritoDTO;
    private CarritoItemDTO itemCancionDTO;
    private CarritoItemDTO itemAlbumDTO;

    @BeforeEach
    void setUp() {
        itemCancionDTO = CarritoItemDTO.builder()
                .idCarritoItem(1L)
                .tipoProducto("CANCION")
                .idCancion(1L)
                .precio(BigDecimal.valueOf(0.99))
                .urlPortada("http://example.com/cancion.jpg")
                .nombreArtistico("Artista Test")
                .titulo("Canción Test")
                .slugArtista("artista-test")
                .fechaAgregado(LocalDateTime.now())
                .build();

        itemAlbumDTO = CarritoItemDTO.builder()
                .idCarritoItem(2L)
                .tipoProducto("ALBUM")
                .idAlbum(1L)
                .precio(BigDecimal.valueOf(9.99))
                .urlPortada("http://example.com/album.jpg")
                .nombreArtistico("Artista Test")
                .titulo("Álbum Test")
                .slugArtista("artista-test")
                .fechaAgregado(LocalDateTime.now())
                .build();

        carritoDTO = CarritoDTO.builder()
                .idCarrito(1L)
                .idUsuario(1L)
                .items(Arrays.asList(itemCancionDTO, itemAlbumDTO))
                .cantidadItems(2)
                .precioTotal(BigDecimal.valueOf(10.98))
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Obtener carrito del usuario autenticado")
    void obtenerCarrito_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(carritoService.obtenerCarrito(1L)).thenReturn(carritoDTO);

        mockMvc.perform(get("/api/carrito")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrito").value(1))
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.cantidadItems").value(2))
                .andExpect(jsonPath("$.precioTotal").value(10.98));
    }

    @Test
    @DisplayName("Obtener carrito vacío")
    void obtenerCarrito_Vacio_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        CarritoDTO carritoVacio = CarritoDTO.builder()
                .idCarrito(1L)
                .idUsuario(1L)
                .items(Collections.emptyList())
                .cantidadItems(0)
                .precioTotal(BigDecimal.ZERO)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        when(carritoService.obtenerCarrito(1L)).thenReturn(carritoVacio);

        mockMvc.perform(get("/api/carrito")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.cantidadItems").value(0))
                .andExpect(jsonPath("$.precioTotal").value(0));
    }

    @Test
    @DisplayName("Agregar canción al carrito")
    void agregarItem_Cancion_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        AgregarAlCarritoDTO dto = AgregarAlCarritoDTO.builder()
                .tipoProducto("CANCION")
                .idCancion(1L)
                .build();

        CarritoDTO carritoActualizado = CarritoDTO.builder()
                .idCarrito(1L)
                .idUsuario(1L)
                .items(Arrays.asList(itemCancionDTO))
                .cantidadItems(1)
                .precioTotal(BigDecimal.valueOf(0.99))
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        when(carritoService.agregarItem(eq(1L), any(AgregarAlCarritoDTO.class)))
                .thenReturn(carritoActualizado);

        mockMvc.perform(post("/api/carrito/items")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidadItems").value(1))
                .andExpect(jsonPath("$.items[0].tipoProducto").value("CANCION"));
    }

    @Test
    @DisplayName("Agregar álbum al carrito")
    void agregarItem_Album_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        AgregarAlCarritoDTO dto = AgregarAlCarritoDTO.builder()
                .tipoProducto("ALBUM")
                .idAlbum(1L)
                .build();

        CarritoDTO carritoActualizado = CarritoDTO.builder()
                .idCarrito(1L)
                .idUsuario(1L)
                .items(Arrays.asList(itemAlbumDTO))
                .cantidadItems(1)
                .precioTotal(BigDecimal.valueOf(9.99))
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        when(carritoService.agregarItem(eq(1L), any(AgregarAlCarritoDTO.class)))
                .thenReturn(carritoActualizado);

        mockMvc.perform(post("/api/carrito/items")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidadItems").value(1))
                .andExpect(jsonPath("$.items[0].tipoProducto").value("ALBUM"));
    }

    @Test
    @DisplayName("Agregar item duplicado - Conflict")
    void agregarItem_Duplicado_Conflict() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        AgregarAlCarritoDTO dto = AgregarAlCarritoDTO.builder()
                .tipoProducto("CANCION")
                .idCancion(1L)
                .build();

        when(carritoService.agregarItem(eq(1L), any(AgregarAlCarritoDTO.class)))
                .thenThrow(new ItemYaEnCarritoException("La canción ya está en el carrito"));

        mockMvc.perform(post("/api/carrito/items")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Agregar item con canción inexistente - Not Found")
    void agregarItem_CancionNoExiste_NotFound() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        AgregarAlCarritoDTO dto = AgregarAlCarritoDTO.builder()
                .tipoProducto("CANCION")
                .idCancion(999L)
                .build();

        when(carritoService.agregarItem(eq(1L), any(AgregarAlCarritoDTO.class)))
                .thenThrow(new CancionNotFoundException(999L));

        mockMvc.perform(post("/api/carrito/items")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Agregar item con álbum inexistente - Not Found")
    void agregarItem_AlbumNoExiste_NotFound() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        AgregarAlCarritoDTO dto = AgregarAlCarritoDTO.builder()
                .tipoProducto("ALBUM")
                .idAlbum(999L)
                .build();

        when(carritoService.agregarItem(eq(1L), any(AgregarAlCarritoDTO.class)))
                .thenThrow(new AlbumNotFoundException(999L));

        mockMvc.perform(post("/api/carrito/items")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Agregar item con tipo de producto inválido - Bad Request")
    void agregarItem_TipoInvalido_BadRequest() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        AgregarAlCarritoDTO dto = AgregarAlCarritoDTO.builder()
                .tipoProducto("INVALIDO")
                .idCancion(1L)
                .build();

        when(carritoService.agregarItem(eq(1L), any(AgregarAlCarritoDTO.class)))
                .thenThrow(new IllegalArgumentException("Tipo de producto inválido: INVALIDO"));

        mockMvc.perform(post("/api/carrito/items")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Eliminar item del carrito")
    void eliminarItem_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        CarritoDTO carritoActualizado = CarritoDTO.builder()
                .idCarrito(1L)
                .idUsuario(1L)
                .items(Arrays.asList(itemAlbumDTO))
                .cantidadItems(1)
                .precioTotal(BigDecimal.valueOf(9.99))
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        when(carritoService.eliminarItem(1L, 1L)).thenReturn(carritoActualizado);

        mockMvc.perform(delete("/api/carrito/items/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadItems").value(1))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    @DisplayName("Eliminar item inexistente - Not Found")
    void eliminarItem_NoExiste_NotFound() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(carritoService.eliminarItem(1L, 999L))
                .thenThrow(new CarritoItemNotFoundException("Item no encontrado"));

        mockMvc.perform(delete("/api/carrito/items/999")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Eliminar item de otro usuario - Forbidden")
    void eliminarItem_OtroUsuario_Forbidden() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(carritoService.eliminarItem(1L, 1L))
                .thenThrow(new ForbiddenAccessException("Este item no pertenece a tu carrito"));

        mockMvc.perform(delete("/api/carrito/items/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Vaciar carrito")
    void vaciarCarrito_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doNothing().when(carritoService).vaciarCarrito(1L);

        mockMvc.perform(delete("/api/carrito")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Carrito vaciado exitosamente"));
    }

    @Test
    @DisplayName("Vaciar carrito inexistente - Not Found")
    void vaciarCarrito_NoExiste_NotFound() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doThrow(new CarritoNotFoundException("Carrito no encontrado"))
                .when(carritoService).vaciarCarrito(1L);

        mockMvc.perform(delete("/api/carrito")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Finalizar compra con método de pago")
    void finalizarCompra_ConMetodoPago_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doNothing().when(carritoService).finalizarCompra(1L, 1L);

        mockMvc.perform(post("/api/carrito/checkout")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .param("idMetodoPago", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Compra finalizada exitosamente"));
    }

    @Test
    @DisplayName("Finalizar compra sin método de pago (contenido gratuito)")
    void finalizarCompra_SinMetodoPago_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doNothing().when(carritoService).finalizarCompra(1L, null);

        mockMvc.perform(post("/api/carrito/checkout")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Compra finalizada exitosamente"));
    }

    @Test
    @DisplayName("Finalizar compra con carrito vacío - Bad Request")
    void finalizarCompra_CarritoVacio_BadRequest() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doThrow(new CarritoVacioException("El carrito está vacío"))
                .when(carritoService).finalizarCompra(1L, null);

        mockMvc.perform(post("/api/carrito/checkout")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Finalizar compra sin método de pago para contenido de pago - Bad Request")
    void finalizarCompra_SinMetodoPagoRequerido_BadRequest() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doThrow(new IllegalArgumentException("Se requiere método de pago para contenido de pago"))
                .when(carritoService).finalizarCompra(1L, null);

        mockMvc.perform(post("/api/carrito/checkout")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Eliminar carrito de usuario (endpoint interno)")
    void eliminarCarritoUsuario_Success() throws Exception {
        doNothing().when(carritoService).eliminarCarrito(1L);

        mockMvc.perform(delete("/api/carrito/usuarios/1")
                        .header("X-Service-Token", serviceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Carrito del usuario eliminado"));
    }

    @Test
    @DisplayName("Agregar item sin autenticación - Forbidden")
    void agregarItem_SinAuth_Forbidden() throws Exception {
        AgregarAlCarritoDTO dto = AgregarAlCarritoDTO.builder()
                .tipoProducto("CANCION")
                .idCancion(1L)
                .build();

        mockMvc.perform(post("/api/carrito/items")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Obtener carrito sin autenticación - Forbidden")
    void obtenerCarrito_SinAuth_Forbidden() throws Exception {
        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Finalizar compra sin autenticación - Forbidden")
    void finalizarCompra_SinAuth_Forbidden() throws Exception {
        mockMvc.perform(post("/api/carrito/checkout")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}