package com.ondra.contenidos.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.security.*;
import com.ondra.contenidos.services.CobroListService;
import com.ondra.contenidos.services.CobroService;
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
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CobroController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
@TestPropertySource(properties = {"service.token=e3e27aa2f289a9686be1d3c45659f308748be2d8751c4911b1cecb09fdd228fa"})
class CobroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockitoBean
    private CobroService cobroService;

    @MockitoBean
    private CobroListService cobroListService;

    @Value("${service.token}")
    private String serviceToken;

    private CobroDTO cobroCompraDTO;
    private CobroDTO cobroReproduccionDTO;
    private CobrosPaginadosDTO cobrosPaginadosDTO;
    private ResumenCobrosDTO resumenCobrosDTO;

    @BeforeEach
    void setUp() {
        cobroCompraDTO = CobroDTO.builder()
                .idCobro(1L)
                .idArtista(1L)
                .tipoCobro("COMPRA")
                .monto(BigDecimal.valueOf(7.99))
                .fechaCobro(LocalDateTime.now().minusDays(5))
                .tipoContenido("ÁLBUM")
                .idAlbum(1L)
                .tituloContenido("Álbum Test")
                .estado("PENDIENTE")
                .descripcion("Compra de álbum (80% de 9.99€)")
                .idCompra(1L)
                .build();

        cobroReproduccionDTO = CobroDTO.builder()
                .idCobro(2L)
                .idArtista(1L)
                .tipoCobro("REPRODUCCION")
                .monto(BigDecimal.valueOf(5.00))
                .fechaCobro(LocalDateTime.now().minusDays(2))
                .tipoContenido("CANCIÓN")
                .idCancion(1L)
                .tituloContenido("Canción Test")
                .reproduccionesAcumuladas(1000L)
                .estado("PENDIENTE")
                .descripcion("Cobro por alcanzar 1000 reproducciones")
                .build();

        cobrosPaginadosDTO = CobrosPaginadosDTO.builder()
                .cobros(Arrays.asList(cobroCompraDTO, cobroReproduccionDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(2L)
                .elementosPorPagina(20)
                .totalMonto(BigDecimal.valueOf(12.99))
                .montoPendiente(BigDecimal.valueOf(12.99))
                .montoPagado(BigDecimal.ZERO)
                .build();

        resumenCobrosDTO = ResumenCobrosDTO.builder()
                .mes(11)
                .anio(2024)
                .totalCobros(BigDecimal.valueOf(100.00))
                .cantidadCobros(10L)
                .montoPendiente(BigDecimal.valueOf(50.00))
                .montoPagado(BigDecimal.valueOf(50.00))
                .build();
    }

    @Test
    @DisplayName("Listar cobros sin filtros")
    void listarCobros_SinFiltros_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroListService.listarCobrosConFiltros(any(FiltrosCobrosDTO.class)))
                .thenReturn(cobrosPaginadosDTO);

        mockMvc.perform(get("/api/cobros")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobros", hasSize(2)))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalMonto").value(12.99))
                .andExpect(jsonPath("$.montoPendiente").value(12.99))
                .andExpect(jsonPath("$.montoPagado").value(0));
    }

    @Test
    @DisplayName("Listar cobros con filtro por artista")
    void listarCobros_ConFiltroArtista_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroListService.listarCobrosConFiltros(any(FiltrosCobrosDTO.class)))
                .thenReturn(cobrosPaginadosDTO);

        mockMvc.perform(get("/api/cobros")
                        .header("Authorization", "Bearer " + token)
                        .param("idArtista", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobros", hasSize(2)));
    }

    @Test
    @DisplayName("Listar cobros con filtro por estado")
    void listarCobros_ConFiltroEstado_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        CobrosPaginadosDTO cobrosPendientes = CobrosPaginadosDTO.builder()
                .cobros(Arrays.asList(cobroCompraDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .totalMonto(BigDecimal.valueOf(7.99))
                .montoPendiente(BigDecimal.valueOf(7.99))
                .montoPagado(BigDecimal.ZERO)
                .build();

        when(cobroListService.listarCobrosConFiltros(any(FiltrosCobrosDTO.class)))
                .thenReturn(cobrosPendientes);

        mockMvc.perform(get("/api/cobros")
                        .header("Authorization", "Bearer " + token)
                        .param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobros", hasSize(1)))
                .andExpect(jsonPath("$.cobros[0].estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("Listar cobros con filtro por tipo de cobro")
    void listarCobros_ConFiltroTipoCobro_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        CobrosPaginadosDTO cobrosCompra = CobrosPaginadosDTO.builder()
                .cobros(Arrays.asList(cobroCompraDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .totalMonto(BigDecimal.valueOf(7.99))
                .montoPendiente(BigDecimal.valueOf(7.99))
                .montoPagado(BigDecimal.ZERO)
                .build();

        when(cobroListService.listarCobrosConFiltros(any(FiltrosCobrosDTO.class)))
                .thenReturn(cobrosCompra);

        mockMvc.perform(get("/api/cobros")
                        .header("Authorization", "Bearer " + token)
                        .param("tipoCobro", "COMPRA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobros", hasSize(1)))
                .andExpect(jsonPath("$.cobros[0].tipoCobro").value("COMPRA"));
    }

    @Test
    @DisplayName("Listar cobros con filtro por tipo de contenido")
    void listarCobros_ConFiltroTipoContenido_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroListService.listarCobrosConFiltros(any(FiltrosCobrosDTO.class)))
                .thenReturn(cobrosPaginadosDTO);

        mockMvc.perform(get("/api/cobros")
                        .header("Authorization", "Bearer " + token)
                        .param("tipoContenido", "CANCIÓN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobros").isArray());
    }

    @Test
    @DisplayName("Listar cobros con filtro por rango de fechas")
    void listarCobros_ConFiltroFechas_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroListService.listarCobrosConFiltros(any(FiltrosCobrosDTO.class)))
                .thenReturn(cobrosPaginadosDTO);

        mockMvc.perform(get("/api/cobros")
                        .header("Authorization", "Bearer " + token)
                        .param("fechaDesde", "2024-01-01T00:00:00")
                        .param("fechaHasta", "2024-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobros", hasSize(2)));
    }

    @Test
    @DisplayName("Listar cobros con filtro por mes y año")
    void listarCobros_ConFiltroMesAnio_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroListService.listarCobrosConFiltros(any(FiltrosCobrosDTO.class)))
                .thenReturn(cobrosPaginadosDTO);

        mockMvc.perform(get("/api/cobros")
                        .header("Authorization", "Bearer " + token)
                        .param("mes", "11")
                        .param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobros", hasSize(2)));
    }

    @Test
    @DisplayName("Listar cobros con filtro por rango de montos")
    void listarCobros_ConFiltroMontos_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroListService.listarCobrosConFiltros(any(FiltrosCobrosDTO.class)))
                .thenReturn(cobrosPaginadosDTO);

        mockMvc.perform(get("/api/cobros")
                        .header("Authorization", "Bearer " + token)
                        .param("montoMinimo", "5.00")
                        .param("montoMaximo", "10.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobros").isArray());
    }

    @Test
    @DisplayName("Listar cobros con ordenamiento y paginación")
    void listarCobros_ConOrdenamientoPaginacion_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroListService.listarCobrosConFiltros(any(FiltrosCobrosDTO.class)))
                .thenReturn(cobrosPaginadosDTO);

        mockMvc.perform(get("/api/cobros")
                        .header("Authorization", "Bearer " + token)
                        .param("ordenarPor", "MONTO")
                        .param("direccion", "ASC")
                        .param("pagina", "1")
                        .param("limite", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paginaActual").value(1))
                .andExpect(jsonPath("$.elementosPorPagina").value(20));
    }

    @Test
    @DisplayName("Listar cobros por mes específico")
    void listarCobrosPorMes_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroListService.listarCobrosPorMes(1L, 11, 2024, 1, 20))
                .thenReturn(cobrosPaginadosDTO);

        mockMvc.perform(get("/api/cobros/mes")
                        .header("Authorization", "Bearer " + token)
                        .param("idArtista", "1")
                        .param("mes", "11")
                        .param("anio", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobros", hasSize(2)))
                .andExpect(jsonPath("$.totalMonto").value(12.99));
    }

    @Test
    @DisplayName("Listar cobros por mes con paginación personalizada")
    void listarCobrosPorMes_ConPaginacion_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroListService.listarCobrosPorMes(1L, 11, 2024, 2, 5))
                .thenReturn(cobrosPaginadosDTO);

        mockMvc.perform(get("/api/cobros/mes")
                        .header("Authorization", "Bearer " + token)
                        .param("idArtista", "1")
                        .param("mes", "11")
                        .param("anio", "2024")
                        .param("pagina", "2")
                        .param("limite", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobros").isArray());
    }

    @Test
    @DisplayName("Obtener resumen mensual de cobros")
    void obtenerResumenMensual_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        List<ResumenCobrosDTO> resumen = Arrays.asList(resumenCobrosDTO);

        when(cobroListService.obtenerResumenPorMes(1L)).thenReturn(resumen);

        mockMvc.perform(get("/api/cobros/resumen-mensual")
                        .header("Authorization", "Bearer " + token)
                        .param("idArtista", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].mes").value(11))
                .andExpect(jsonPath("$[0].anio").value(2024))
                .andExpect(jsonPath("$[0].totalCobros").value(100.00))
                .andExpect(jsonPath("$[0].cantidadCobros").value(10))
                .andExpect(jsonPath("$[0].montoPendiente").value(50.00))
                .andExpect(jsonPath("$[0].montoPagado").value(50.00));
    }

    @Test
    @DisplayName("Obtener totales de ingresos de un artista")
    void obtenerTotales_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroService.obtenerTotalIngresos(1L))
                .thenReturn(BigDecimal.valueOf(1000.00));
        when(cobroService.obtenerTotalPendiente(1L))
                .thenReturn(BigDecimal.valueOf(300.00));

        mockMvc.perform(get("/api/cobros/totales")
                        .header("Authorization", "Bearer " + token)
                        .param("idArtista", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIngresos").value(1000.00))
                .andExpect(jsonPath("$.totalPendiente").value(300.00))
                .andExpect(jsonPath("$.totalPagado").value(700.00));
    }

    @Test
    @DisplayName("Marcar cobros como pagados")
    void marcarComoPagados_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroService.marcarComoPagados(1L, 1L)).thenReturn(5);

        Map<String, Object> request = Map.of(
                "idArtista", 1L,
                "idMetodoCobro", 1L
        );

        mockMvc.perform(post("/api/cobros/marcar-pagados")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cobros marcados como pagados"))
                .andExpect(jsonPath("$.cantidadProcesada").value(5));
    }

    @Test
    @DisplayName("Marcar cobros específicos como pagados")
    void marcarCobrosEspecificosComoPagados_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroService.marcarCobrosEspecificosComoPagados(anyList(), eq(1L)))
                .thenReturn(3);

        Map<String, Object> request = Map.of(
                "idsCobros", Arrays.asList(1, 2, 3),
                "idMetodoCobro", 1L
        );

        mockMvc.perform(post("/api/cobros/marcar-pagados-especificos")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Cobros específicos marcados como pagados"))
                .andExpect(jsonPath("$.cantidadProcesada").value(3));
    }

    @Test
    @DisplayName("Procesar pagos mensuales con método de cobro")
    void procesarPagosMensuales_ConMetodoCobro_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        CobroService.ResumenProcesamientoPagos resumen =
                new CobroService.ResumenProcesamientoPagos(
                        25,
                        BigDecimal.valueOf(500.00),
                        LocalDateTime.now()
                );

        when(cobroService.procesarPagosMensuales(1L)).thenReturn(resumen);

        Map<String, Object> request = Map.of("idMetodoCobro", 1L);

        mockMvc.perform(post("/api/cobros/procesar-pagos-mensuales")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Procesamiento mensual ejecutado correctamente"))
                .andExpect(jsonPath("$.cobrosProcessados").value(25))
                .andExpect(jsonPath("$.montoTotal").value(500.00))
                .andExpect(jsonPath("$.fechaProcesamiento").exists());
    }

    @Test
    @DisplayName("Procesar pagos mensuales sin método de cobro")
    void procesarPagosMensuales_SinMetodoCobro_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        CobroService.ResumenProcesamientoPagos resumen =
                new CobroService.ResumenProcesamientoPagos(
                        15,
                        BigDecimal.valueOf(250.00),
                        LocalDateTime.now()
                );

        when(cobroService.procesarPagosMensuales(null)).thenReturn(resumen);

        mockMvc.perform(post("/api/cobros/procesar-pagos-mensuales")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Procesamiento mensual ejecutado correctamente"))
                .andExpect(jsonPath("$.cobrosProcessados").value(15))
                .andExpect(jsonPath("$.montoTotal").value(250.00));
    }

    @Test
    @DisplayName("Procesar pagos mensuales sin body")
    void procesarPagosMensuales_SinBody_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        CobroService.ResumenProcesamientoPagos resumen =
                new CobroService.ResumenProcesamientoPagos(
                        10,
                        BigDecimal.valueOf(100.00),
                        LocalDateTime.now()
                );

        when(cobroService.procesarPagosMensuales(null)).thenReturn(resumen);

        mockMvc.perform(post("/api/cobros/procesar-pagos-mensuales")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobrosProcessados").value(10));
    }

    @Test
    @DisplayName("Listar cobros con múltiples filtros combinados")
    void listarCobros_ConMultiplesFiltros_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroListService.listarCobrosConFiltros(any(FiltrosCobrosDTO.class)))
                .thenReturn(cobrosPaginadosDTO);

        mockMvc.perform(get("/api/cobros")
                        .header("Authorization", "Bearer " + token)
                        .param("idArtista", "1")
                        .param("estado", "PENDIENTE")
                        .param("tipoCobro", "COMPRA")
                        .param("tipoContenido", "ÁLBUM")
                        .param("mes", "11")
                        .param("anio", "2024")
                        .param("montoMinimo", "5.00")
                        .param("montoMaximo", "20.00")
                        .param("ordenarPor", "MONTO")
                        .param("direccion", "DESC")
                        .param("pagina", "1")
                        .param("limite", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobros").isArray());
    }

    @Test
    @DisplayName("Obtener resumen mensual sin cobros")
    void obtenerResumenMensual_SinCobros_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroListService.obtenerResumenPorMes(1L))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/cobros/resumen-mensual")
                        .header("Authorization", "Bearer " + token)
                        .param("idArtista", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Obtener totales con cero ingresos")
    void obtenerTotales_CeroIngresos_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroService.obtenerTotalIngresos(1L))
                .thenReturn(BigDecimal.ZERO);
        when(cobroService.obtenerTotalPendiente(1L))
                .thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/api/cobros/totales")
                        .header("Authorization", "Bearer " + token)
                        .param("idArtista", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIngresos").value(0))
                .andExpect(jsonPath("$.totalPendiente").value(0))
                .andExpect(jsonPath("$.totalPagado").value(0));
    }

    @Test
    @DisplayName("Marcar cobros como pagados sin cobros pendientes")
    void marcarComoPagados_SinCobrosPendientes_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroService.marcarComoPagados(1L, 1L)).thenReturn(0);

        Map<String, Object> request = Map.of(
                "idArtista", 1L,
                "idMetodoCobro", 1L
        );

        mockMvc.perform(post("/api/cobros/marcar-pagados")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadProcesada").value(0));
    }

    @Test
    @DisplayName("Marcar cobros específicos como pagados con lista vacía")
    void marcarCobrosEspecificosComoPagados_ListaVacia_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        when(cobroService.marcarCobrosEspecificosComoPagados(anyList(), eq(1L)))
                .thenReturn(0);

        Map<String, Object> request = Map.of(
                "idsCobros", Arrays.asList(),
                "idMetodoCobro", 1L
        );

        mockMvc.perform(post("/api/cobros/marcar-pagados-especificos")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadProcesada").value(0));
    }

    @Test
    @DisplayName("Procesar pagos mensuales sin cobros pendientes")
    void procesarPagosMensuales_SinCobrosPendientes_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        CobroService.ResumenProcesamientoPagos resumen =
                new CobroService.ResumenProcesamientoPagos(
                        0,
                        BigDecimal.ZERO,
                        LocalDateTime.now()
                );

        when(cobroService.procesarPagosMensuales(null)).thenReturn(resumen);

        mockMvc.perform(post("/api/cobros/procesar-pagos-mensuales")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cobrosProcessados").value(0))
                .andExpect(jsonPath("$.montoTotal").value(0));
    }
}