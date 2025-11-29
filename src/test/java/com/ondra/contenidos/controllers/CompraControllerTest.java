package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.security.*;
import com.ondra.contenidos.services.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para {@link CompraController}
 *
 * <p>
 * Esta clase cubre pruebas de:
 * </p>
 *
 * <ul>
 *   <li>Listado de compras con paginación</li>
 *   <li>Filtrado de compras por tipo de contenido (CANCION, ÁLBUM)</li>
 *   <li>Verificación de compra de canciones</li>
 *   <li>Verificación de compra de álbumes</li>
 *   <li>Obtención de total gastado por usuario</li>
 *   <li>Eliminación masiva de compras por usuario (endpoint interno)</li>
 *   <li>Autenticación JWT requerida</li>
 *   <li>Manejo de parámetros opcionales</li>
 * </ul>
 */
@WebMvcTest(CompraController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
@TestPropertySource(properties = {"service.token=e3e27aa2f289a9686be1d3c45659f308748be2d8751c4911b1cecb09fdd228fa"})
class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockitoBean
    private CompraService compraService;

    @Value("${service.token}")
    private String serviceToken;

    private CompraDTO compraCancionDTO;
    private CompraDTO compraAlbumDTO;

    @BeforeEach
    void setUp() {
        CancionDTO cancion = CancionDTO.builder()
                .idCancion(1L)
                .tituloCancion("Canción Test")
                .precioCancion(1.99)
                .build();

        compraCancionDTO = CompraDTO.builder()
                .idCompra(1L)
                .idUsuario(1L)
                .tipoContenido("CANCIÓN")
                .cancion(cancion)
                .precioPagado(new BigDecimal("1.99"))
                .fechaCompra(LocalDateTime.now())
                .idMetodoPago(1L)
                .idTransaccion("TXN-123")
                .nombreArtista("Artista Test")
                .slugArtista("artista-test")
                .build();

        AlbumDTO album = AlbumDTO.builder()
                .idAlbum(1L)
                .tituloAlbum("Álbum Test")
                .precioAlbum(9.99)
                .build();

        compraAlbumDTO = CompraDTO.builder()
                .idCompra(2L)
                .idUsuario(1L)
                .tipoContenido("ÁLBUM")
                .album(album)
                .precioPagado(new BigDecimal("9.99"))
                .fechaCompra(LocalDateTime.now())
                .idMetodoPago(1L)
                .idTransaccion("TXN-456")
                .nombreArtista("Artista Test")
                .slugArtista("artista-test")
                .build();
    }

    // ==================== TESTS LISTAR COMPRAS ====================

    @Test
    @DisplayName("Listar compras del usuario autenticado")
    void listarCompras_UsuarioAutenticado_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        ComprasPaginadasDTO paginaDTO = ComprasPaginadasDTO.builder()
                .compras(Arrays.asList(compraCancionDTO, compraAlbumDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(2L)
                .elementosPorPagina(20)
                .build();

        when(compraService.listarCompras(1L, null, 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/compras")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compras", hasSize(2)))
                .andExpect(jsonPath("$.compras[0].tipoContenido").value("CANCIÓN"))
                .andExpect(jsonPath("$.compras[1].tipoContenido").value("ÁLBUM"))
                .andExpect(jsonPath("$.totalElementos").value(2));

        verify(compraService, times(1)).listarCompras(1L, null, 1, 20);
    }

    @Test
    @DisplayName("Listar compras con ID de usuario específico")
    void listarCompras_ConIdUsuario_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(2L, "usuario2@example.com");

        ComprasPaginadasDTO paginaDTO = ComprasPaginadasDTO.builder()
                .compras(Arrays.asList(compraCancionDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(compraService.listarCompras(2L, null, 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/compras")
                        .param("idUsuario", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compras", hasSize(1)));

        verify(compraService, times(1)).listarCompras(2L, null, 1, 20);
    }

    @Test
    @DisplayName("Listar solo compras de canciones")
    void listarCompras_FiltroCanciones_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        ComprasPaginadasDTO paginaDTO = ComprasPaginadasDTO.builder()
                .compras(Arrays.asList(compraCancionDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(compraService.listarCompras(1L, "CANCION", 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/compras")
                        .param("tipo", "CANCION")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compras", hasSize(1)))
                .andExpect(jsonPath("$.compras[0].tipoContenido").value("CANCIÓN"));

        verify(compraService, times(1)).listarCompras(1L, "CANCION", 1, 20);
    }

    @Test
    @DisplayName("Listar solo compras de álbumes")
    void listarCompras_FiltroAlbumes_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        ComprasPaginadasDTO paginaDTO = ComprasPaginadasDTO.builder()
                .compras(Arrays.asList(compraAlbumDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(compraService.listarCompras(1L, "ÁLBUM", 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/compras")
                        .param("tipo", "ÁLBUM")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compras", hasSize(1)))
                .andExpect(jsonPath("$.compras[0].tipoContenido").value("ÁLBUM"));

        verify(compraService, times(1)).listarCompras(1L, "ÁLBUM", 1, 20);
    }

    @Test
    @DisplayName("Listar compras con paginación personalizada")
    void listarCompras_PaginacionPersonalizada_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        ComprasPaginadasDTO paginaDTO = ComprasPaginadasDTO.builder()
                .compras(Arrays.asList(compraCancionDTO))
                .paginaActual(2)
                .totalPaginas(3)
                .totalElementos(50L)
                .elementosPorPagina(25)
                .build();

        when(compraService.listarCompras(1L, null, 2, 25))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/compras")
                        .param("page", "2")
                        .param("limit", "25")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paginaActual").value(2))
                .andExpect(jsonPath("$.elementosPorPagina").value(25));

        verify(compraService, times(1)).listarCompras(1L, null, 2, 25);
    }

    @Test
    @DisplayName("Listar compras sin usuario ni autenticación - Forbidden")
    void listarCompras_SinUsuario_Forbidden() throws Exception {
        mockMvc.perform(get("/api/compras"))
                .andExpect(status().is(403));

        verify(compraService, never()).listarCompras(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Listar compras con tipo inválido - Bad Request")
    void listarCompras_TipoInvalido_BadRequest() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(compraService.listarCompras(1L, "INVALIDO", 1, 20))
                .thenThrow(new IllegalArgumentException("Tipo de contenido inválido: INVALIDO"));

        mockMvc.perform(get("/api/compras")
                        .param("tipo", "INVALIDO")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ==================== TESTS VERIFICAR COMPRA DE CANCIÓN ====================

    @Test
    @DisplayName("Verificar compra de canción - usuario ha comprado")
    void verificarCompraCancion_HaComprado_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(compraService.haCompradoCancion(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/compras/canciones/1/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(compraService, times(1)).haCompradoCancion(1L, 1L);
    }

    @Test
    @DisplayName("Verificar compra de canción - usuario no ha comprado")
    void verificarCompraCancion_NoHaComprado_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(compraService.haCompradoCancion(1L, 1L)).thenReturn(false);

        mockMvc.perform(get("/api/compras/canciones/1/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(compraService, times(1)).haCompradoCancion(1L, 1L);
    }

    @Test
    @DisplayName("Verificar compra de canción sin autenticación - Forbidden")
    void verificarCompraCancion_SinAutenticacion_Forbidden() throws Exception {
        mockMvc.perform(get("/api/compras/canciones/1/check"))
                .andExpect(status().is(403));

        verify(compraService, never()).haCompradoCancion(any(), any());
    }

    // ==================== TESTS VERIFICAR COMPRA DE ÁLBUM ====================

    @Test
    @DisplayName("Verificar compra de álbum - usuario ha comprado")
    void verificarCompraAlbum_HaComprado_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(compraService.haCompradoAlbum(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/compras/albumes/1/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(compraService, times(1)).haCompradoAlbum(1L, 1L);
    }

    @Test
    @DisplayName("Verificar compra de álbum - usuario no ha comprado")
    void verificarCompraAlbum_NoHaComprado_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(compraService.haCompradoAlbum(1L, 1L)).thenReturn(false);

        mockMvc.perform(get("/api/compras/albumes/1/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(compraService, times(1)).haCompradoAlbum(1L, 1L);
    }

    @Test
    @DisplayName("Verificar compra de álbum sin autenticación - Forbidden")
    void verificarCompraAlbum_SinAutenticacion_Forbidden() throws Exception {
        mockMvc.perform(get("/api/compras/albumes/1/check"))
                .andExpect(status().is(403));

        verify(compraService, never()).haCompradoAlbum(any(), any());
    }

    // ==================== TESTS OBTENER TOTAL GASTADO ====================

    @Test
    @DisplayName("Obtener total gastado por usuario")
    void obtenerTotalGastado_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        BigDecimal totalGastado = new BigDecimal("49.99");
        when(compraService.obtenerTotalGastado(1L)).thenReturn(totalGastado);

        mockMvc.perform(get("/api/compras/total-gastado")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("49.99"));

        verify(compraService, times(1)).obtenerTotalGastado(1L);
    }

    @Test
    @DisplayName("Obtener total gastado - usuario sin compras")
    void obtenerTotalGastado_SinCompras_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        BigDecimal totalGastado = BigDecimal.ZERO;
        when(compraService.obtenerTotalGastado(1L)).thenReturn(totalGastado);

        mockMvc.perform(get("/api/compras/total-gastado")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(compraService, times(1)).obtenerTotalGastado(1L);
    }

    @Test
    @DisplayName("Obtener total gastado sin autenticación - Forbidden")
    void obtenerTotalGastado_SinAutenticacion_Forbidden() throws Exception {
        mockMvc.perform(get("/api/compras/total-gastado"))
                .andExpect(status().is(403));

        verify(compraService, never()).obtenerTotalGastado(any());
    }

    // ==================== TESTS ELIMINAR COMPRAS DE USUARIO ====================

    @Test
    @DisplayName("Eliminar todas las compras de usuario - endpoint interno")
    void eliminarComprasUsuario_Success() throws Exception {
        doNothing().when(compraService).eliminarTodasLasCompras(1L);

        mockMvc.perform(delete("/api/compras/usuarios/1")
                        .header("X-Service-Token", serviceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Todas las compras del usuario eliminadas"))
                .andExpect(jsonPath("$.statusCode").value(200));

        verify(compraService, times(1)).eliminarTodasLasCompras(1L);
    }

    @Test
    @DisplayName("Eliminar compras de usuario sin service token - Forbidden")
    void eliminarComprasUsuario_SinServiceToken_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/compras/usuarios/1"))
                .andExpect(status().is(403));

        verify(compraService, never()).eliminarTodasLasCompras(any());
    }
}