package com.ondra.contenidos.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.models.enums.GeneroMusical;
import com.ondra.contenidos.security.*;
import com.ondra.contenidos.services.CancionService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para {@link CancionController}
 *
 * <p>
 * Esta clase cubre pruebas de:
 * </p>
 *
 * <ul>
 *   <li>Listado de canciones con filtros y paginación</li>
 *   <li>Obtención de detalle de canciones</li>
 *   <li>Listado de canciones por artista y álbum</li>
 *   <li>Búsqueda de canciones</li>
 *   <li>Listado de canciones gratuitas</li>
 *   <li>Obtención de estadísticas</li>
 *   <li>Registro de reproducciones</li>
 *   <li>Creación de canciones (con permisos de artista)</li>
 *   <li>Actualización de canciones (con control de propiedad)</li>
 *   <li>Eliminación de canciones (con control de propiedad)</li>
 *   <li>Eliminación masiva de canciones por artista</li>
 *   <li>Obtención de estadísticas por artista</li>
 * </ul>
 */
@WebMvcTest(CancionController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
@TestPropertySource(properties = {"service.token=e3e27aa2f289a9686be1d3c45659f308748be2d8751c4911b1cecb09fdd228fa"})
class CancionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockitoBean
    private CancionService cancionService;

    @Value("${service.token}")
    private String serviceToken;

    private CancionDTO cancionDTO;
    private CancionDetalleDTO cancionDetalleDTO;

    @BeforeEach
    void setUp() {
        cancionDTO = CancionDTO.builder()
                .idCancion(1L)
                .tituloCancion("Mi Canción")
                .idArtista(1L)
                .genero(String.valueOf(GeneroMusical.POP))
                .precioCancion(1.99)
                .duracionSegundos(180)
                .urlPortada("http://example.com/portada.jpg")
                .urlAudio("http://example.com/audio.mp3")
                .reproducciones(100L)
                .fechaPublicacion(LocalDateTime.now())
                .build();

        cancionDetalleDTO = CancionDetalleDTO.builder()
                .idCancion(1L)
                .tituloCancion("Mi Canción")
                .idArtista(1L)
                .genero(String.valueOf(GeneroMusical.POP))
                .precioCancion(1.99)
                .duracionSegundos(180)
                .urlPortada("http://example.com/portada.jpg")
                .urlAudio("http://example.com/audio.mp3")
                .reproducciones(100L)
                .fechaPublicacion(LocalDateTime.now())
                .descripcion("Descripción de la canción")
                .build();
    }

    // ==================== TESTS LISTADO DE CANCIONES ====================

    @Test
    @DisplayName("Listar canciones sin filtros - debe retornar página por defecto")
    void listarCanciones_SinFiltros_Success() throws Exception {
        CancionesPaginadasDTO paginaDTO = CancionesPaginadasDTO.builder()
                .canciones(Arrays.asList(cancionDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(cancionService.listarCanciones(null, null, null, null, 1, 20, null, null))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/canciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canciones", hasSize(1)))
                .andExpect(jsonPath("$.canciones[0].idCancion").value(1))
                .andExpect(jsonPath("$.canciones[0].tituloCancion").value("Mi Canción"))
                .andExpect(jsonPath("$.paginaActual").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1));

        verify(cancionService, times(1))
                .listarCanciones(null, null, null, null, 1, 20, null, null);
    }

    @Test
    @DisplayName("Listar canciones con filtros de artista y género")
    void listarCanciones_ConFiltros_Success() throws Exception {
        CancionesPaginadasDTO paginaDTO = CancionesPaginadasDTO.builder()
                .canciones(Arrays.asList(cancionDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(cancionService.listarCanciones(1L, 1L, null, "most_recent", 1, 20, null, null))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/canciones")
                        .param("artistId", "1")
                        .param("genreId", "1")
                        .param("orderBy", "most_recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canciones", hasSize(1)));

        verify(cancionService, times(1))
                .listarCanciones(1L, 1L, null, "most_recent", 1, 20, null, null);
    }

    @Test
    @DisplayName("Listar canciones con filtros de precio")
    void listarCanciones_ConFiltrosPrecio_Success() throws Exception {
        CancionesPaginadasDTO paginaDTO = CancionesPaginadasDTO.builder()
                .canciones(Arrays.asList(cancionDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(cancionService.listarCanciones(null, null, null, null, 1, 20, 0.99, 2.99))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/canciones")
                        .param("minPrice", "0.99")
                        .param("maxPrice", "2.99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canciones", hasSize(1)));

        verify(cancionService, times(1))
                .listarCanciones(null, null, null, null, 1, 20, 0.99, 2.99);
    }

    @Test
    @DisplayName("Listar canciones con género no existente - Bad Request")
    void listarCanciones_GeneroNoExiste() throws Exception {
        when(cancionService.listarCanciones(null, 999L, null, null, 1, 20, null, null))
                .thenThrow(new GeneroNotFoundException(999L));

        mockMvc.perform(get("/api/canciones")
                        .param("genreId", "999"))
                .andExpect(status().isBadRequest());

        verify(cancionService, times(1))
                .listarCanciones(null, 999L, null, null, 1, 20, null, null);
    }

    // ==================== TESTS OBTENER CANCIÓN ====================

    @Test
    @DisplayName("Obtener canción por ID - exitoso")
    void obtenerCancion_Success() throws Exception {
        when(cancionService.obtenerCancionPorId(1L)).thenReturn(cancionDetalleDTO);

        mockMvc.perform(get("/api/canciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCancion").value(1))
                .andExpect(jsonPath("$.tituloCancion").value("Mi Canción"))
                .andExpect(jsonPath("$.descripcion").value("Descripción de la canción"));

        verify(cancionService, times(1)).obtenerCancionPorId(1L);
    }

    @Test
    @DisplayName("Obtener canción inexistente - Not Found")
    void obtenerCancion_NoExiste() throws Exception {
        when(cancionService.obtenerCancionPorId(999L))
                .thenThrow(new CancionNotFoundException(999L));

        mockMvc.perform(get("/api/canciones/999"))
                .andExpect(status().isNotFound());

        verify(cancionService, times(1)).obtenerCancionPorId(999L);
    }

    // ==================== TESTS CANCIONES POR ARTISTA ====================

    @Test
    @DisplayName("Obtener canciones por artista - exitoso")
    void obtenerCancionesPorArtista_Success() throws Exception {
        List<CancionDTO> canciones = Arrays.asList(cancionDTO);
        when(cancionService.listarCancionesPorArtista(1L)).thenReturn(canciones);

        mockMvc.perform(get("/api/canciones/artist/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idCancion").value(1))
                .andExpect(jsonPath("$[0].tituloCancion").value("Mi Canción"));

        verify(cancionService, times(1)).listarCancionesPorArtista(1L);
    }

    // ==================== TESTS CANCIONES POR ÁLBUM ====================

    @Test
    @DisplayName("Obtener canciones por álbum - exitoso")
    void obtenerCancionesPorAlbum_Success() throws Exception {
        List<CancionDTO> canciones = Arrays.asList(cancionDTO);
        when(cancionService.listarCancionesPorAlbum(1L)).thenReturn(canciones);

        mockMvc.perform(get("/api/canciones/album/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idCancion").value(1));

        verify(cancionService, times(1)).listarCancionesPorAlbum(1L);
    }

    // ==================== TESTS BÚSQUEDA ====================

    @Test
    @DisplayName("Buscar canciones por término - exitoso")
    void buscarCanciones_Success() throws Exception {
        List<CancionDTO> canciones = Arrays.asList(cancionDTO);
        when(cancionService.buscarCanciones("canción")).thenReturn(canciones);

        mockMvc.perform(get("/api/canciones/search")
                        .param("q", "canción"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tituloCancion").value("Mi Canción"));

        verify(cancionService, times(1)).buscarCanciones("canción");
    }

    // ==================== TESTS CANCIONES GRATUITAS ====================

    @Test
    @DisplayName("Obtener canciones gratuitas - exitoso")
    void obtenerCancionesGratuitas_Success() throws Exception {
        CancionDTO cancionGratuita = CancionDTO.builder()
                .idCancion(2L)
                .tituloCancion("Canción Gratis")
                .precioCancion(0.0)
                .build();

        List<CancionDTO> canciones = Arrays.asList(cancionGratuita);
        when(cancionService.listarCancionesGratuitas()).thenReturn(canciones);

        mockMvc.perform(get("/api/canciones/gratuitas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].precioCancion").value(0.0));

        verify(cancionService, times(1)).listarCancionesGratuitas();
    }

    // ==================== TESTS ESTADÍSTICAS ====================

    @Test
    @DisplayName("Obtener estadísticas globales - exitoso")
    void obtenerEstadisticas_Success() throws Exception {
        CancionesStatsDTO stats = CancionesStatsDTO.builder()
                .totalCanciones(100L)
                .totalReproducciones(5000L)
                .build();

        when(cancionService.obtenerEstadisticas()).thenReturn(stats);

        mockMvc.perform(get("/api/canciones/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCanciones").value(100))
                .andExpect(jsonPath("$.totalReproducciones").value(5000));

        verify(cancionService, times(1)).obtenerEstadisticas();
    }

    // ==================== TESTS REPRODUCCIÓN ====================

    @Test
    @DisplayName("Registrar reproducción - exitoso")
    void registrarReproduccion_Success() throws Exception {
        ReproduccionResponseDTO response = ReproduccionResponseDTO.builder()
                .id("1")
                .totalPlays(101L)
                .build();

        when(cancionService.registrarReproduccion(1L)).thenReturn(response);

        mockMvc.perform(post("/api/canciones/1/reproducir")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.totalPlays").value(101));

        verify(cancionService, times(1)).registrarReproduccion(1L);
    }

    @Test
    @DisplayName("Registrar reproducción de canción inexistente - Not Found")
    void registrarReproduccion_CancionNoExiste() throws Exception {
        when(cancionService.registrarReproduccion(999L))
                .thenThrow(new CancionNotFoundException(999L));

        mockMvc.perform(post("/api/canciones/999/reproducir")
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(cancionService, times(1)).registrarReproduccion(999L);
    }

    // ==================== TESTS CREAR CANCIÓN ====================

    @Test
    @DisplayName("Crear canción - exitoso")
    void crearCancion_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        CrearCancionDTO dto = CrearCancionDTO.builder()
                .tituloCancion("Nueva Canción")
                .idGenero(1L)
                .precioCancion(1.99)
                .duracionSegundos(200)
                .urlPortada("https://res.cloudinary.com/portada.jpg")
                .urlAudio("https://res.cloudinary.com/audio.mp3")
                .build();

        when(cancionService.crearCancion(any(CrearCancionDTO.class), eq(1L)))
                .thenReturn(cancionDTO);

        mockMvc.perform(post("/api/canciones")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCancion").value(1))
                .andExpect(jsonPath("$.tituloCancion").value("Mi Canción"));

        verify(cancionService, times(1)).crearCancion(any(CrearCancionDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Crear canción con género no existente - Bad Request")
    void crearCancion_GeneroNoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        CrearCancionDTO dto = CrearCancionDTO.builder()
                .tituloCancion("Nueva Canción")
                .idGenero(999L)
                .precioCancion(1.99)
                .duracionSegundos(200)
                .urlPortada("https://res.cloudinary.com/portada.jpg")
                .urlAudio("https://res.cloudinary.com/audio.mp3")
                .build();

        when(cancionService.crearCancion(any(CrearCancionDTO.class), eq(1L)))
                .thenThrow(new GeneroNotFoundException(999L));

        mockMvc.perform(post("/api/canciones")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(cancionService, times(1)).crearCancion(any(CrearCancionDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Crear canción sin rol de artista - Forbidden")
    void crearCancion_SinRolArtista() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        CrearCancionDTO dto = CrearCancionDTO.builder()
                .tituloCancion("Nueva Canción")
                .idGenero(1L)
                .precioCancion(1.99)
                .duracionSegundos(200)
                .urlPortada("https://res.cloudinary.com/portada.jpg")
                .urlAudio("https://res.cloudinary.com/audio.mp3")
                .build();

        mockMvc.perform(post("/api/canciones")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(cancionService, never()).crearCancion(any(), any());
    }

    // ==================== TESTS ACTUALIZAR CANCIÓN ====================

    @Test
    @DisplayName("Actualizar canción - exitoso")
    void actualizarCancion_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        EditarCancionDTO dto = EditarCancionDTO.builder()
                .tituloCancion("Canción Actualizada")
                .precioCancion(2.99)
                .build();

        CancionDTO cancionActualizada = CancionDTO.builder()
                .idCancion(1L)
                .tituloCancion("Canción Actualizada")
                .precioCancion(2.99)
                .build();

        when(cancionService.actualizarCancion(eq(1L), any(EditarCancionDTO.class), eq(1L)))
                .thenReturn(cancionActualizada);

        mockMvc.perform(put("/api/canciones/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloCancion").value("Canción Actualizada"))
                .andExpect(jsonPath("$.precioCancion").value(2.99));

        verify(cancionService, times(1))
                .actualizarCancion(eq(1L), any(EditarCancionDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Actualizar canción de otro artista - Forbidden")
    void actualizarCancion_OtroArtista_Forbidden() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(2L, 2L, "otro@example.com");

        EditarCancionDTO dto = EditarCancionDTO.builder()
                .tituloCancion("Intento Hackeo")
                .build();

        when(cancionService.actualizarCancion(eq(1L), any(EditarCancionDTO.class), eq(2L)))
                .thenThrow(new AccesoDenegadoException("canción", 1L));

        mockMvc.perform(put("/api/canciones/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        verify(cancionService, times(1))
                .actualizarCancion(eq(1L), any(EditarCancionDTO.class), eq(2L));
    }

    @Test
    @DisplayName("Actualizar canción inexistente - Not Found")
    void actualizarCancion_NoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        EditarCancionDTO dto = EditarCancionDTO.builder()
                .tituloCancion("Canción Actualizada")
                .build();

        when(cancionService.actualizarCancion(eq(999L), any(EditarCancionDTO.class), eq(1L)))
                .thenThrow(new CancionNotFoundException(999L));

        mockMvc.perform(put("/api/canciones/999")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());

        verify(cancionService, times(1))
                .actualizarCancion(eq(999L), any(EditarCancionDTO.class), eq(1L));
    }

    // ==================== TESTS ELIMINAR CANCIÓN ====================

    @Test
    @DisplayName("Eliminar canción - exitoso")
    void eliminarCancion_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        doNothing().when(cancionService).eliminarCancion(1L, 1L);

        mockMvc.perform(delete("/api/canciones/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(cancionService, times(1)).eliminarCancion(1L, 1L);
    }

    @Test
    @DisplayName("Eliminar canción de otro artista - Forbidden")
    void eliminarCancion_OtroArtista_Forbidden() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(2L, 2L, "otro@example.com");

        doThrow(new AccesoDenegadoException("canción", 1L))
                .when(cancionService).eliminarCancion(1L, 2L);

        mockMvc.perform(delete("/api/canciones/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verify(cancionService, times(1)).eliminarCancion(1L, 2L);
    }

    @Test
    @DisplayName("Eliminar canción inexistente - Not Found")
    void eliminarCancion_NoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        doThrow(new CancionNotFoundException(999L))
                .when(cancionService).eliminarCancion(999L, 1L);

        mockMvc.perform(delete("/api/canciones/999")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        verify(cancionService, times(1)).eliminarCancion(999L, 1L);
    }

    // ==================== TESTS ELIMINAR CANCIONES POR ARTISTA ====================

    @Test
    @DisplayName("Eliminar todas las canciones de un artista - exitoso")
    void eliminarCancionesArtista_Success() throws Exception {
        // Este endpoint es interno y requiere el service token configurado
        doNothing().when(cancionService).eliminarTodasCancionesArtista(1L);

        mockMvc.perform(delete("/api/canciones/artist/1")
                        .header("X-Service-Token", serviceToken))
                .andExpect(status().isNoContent());

        verify(cancionService, times(1)).eliminarTodasCancionesArtista(1L);
    }

    // ==================== TESTS ESTADÍSTICAS POR ARTISTA ====================

    @Test
    @DisplayName("Obtener estadísticas de artista - exitoso")
    void obtenerEstadisticasArtista_Success() throws Exception {
        when(cancionService.obtenerTotalReproduccionesArtista(1L)).thenReturn(1000L);

        mockMvc.perform(get("/api/canciones/artist/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idArtista").value(1))
                .andExpect(jsonPath("$.totalReproducciones").value(1000));

        verify(cancionService, times(1)).obtenerTotalReproduccionesArtista(1L);
    }
}