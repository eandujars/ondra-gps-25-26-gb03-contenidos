package com.ondra.contenidos.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.models.enums.GeneroMusical;
import com.ondra.contenidos.security.*;
import com.ondra.contenidos.services.AlbumService;
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

@WebMvcTest(AlbumController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
@TestPropertySource(properties = {"service.token=e3e27aa2f289a9686be1d3c45659f308748be2d8751c4911b1cecb09fdd228fa"})
class AlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockitoBean
    private AlbumService albumService;

    @Value("${service.token}")
    private String serviceToken;

    private AlbumDTO albumDTO;
    private AlbumDetalleDTO albumDetalleDTO;

    @BeforeEach
    void setUp() {
        albumDTO = AlbumDTO.builder()
                .idAlbum(1L)
                .tituloAlbum("Mi Álbum")
                .idArtista(1L)
                .genero(String.valueOf(GeneroMusical.POP))
                .precioAlbum(9.99)
                .urlPortada("http://example.com/portada.jpg")
                .valoracionMedia(4.5)
                .totalComentarios(25L)
                .totalCanciones(10)
                .duracionTotalSegundos(3600)
                .totalPlayCount(1000L)
                .fechaPublicacion(LocalDateTime.now())
                .descripcion("Descripción del álbum")
                .build();

        albumDetalleDTO = AlbumDetalleDTO.builder()
                .idAlbum(1L)
                .tituloAlbum("Mi Álbum")
                .idArtista(1L)
                .genero(String.valueOf(GeneroMusical.POP))
                .precioAlbum(9.99)
                .urlPortada("http://example.com/portada.jpg")
                .totalCanciones(10)
                .duracionTotalSegundos(3600)
                .totalPlayCount(1000L)
                .valoracionMedia(4.5)
                .totalComentarios(25L)
                .fechaPublicacion(LocalDateTime.now())
                .descripcion("Descripción del álbum")
                .trackList(Arrays.asList())
                .build();
    }

    @Test
    @DisplayName("Listar álbumes sin filtros")
    void listarAlbumes_SinFiltros_Success() throws Exception {
        AlbumesPaginadosDTO paginaDTO = AlbumesPaginadosDTO.builder()
                .albumes(Arrays.asList(albumDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(albumService.listarAlbumes(null, null, null, null, 1, 20, null, null))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/albumes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.albumes", hasSize(1)))
                .andExpect(jsonPath("$.albumes[0].idAlbum").value(1));
    }

    @Test
    @DisplayName("Obtener álbum por ID")
    void obtenerAlbum_Success() throws Exception {
        when(albumService.obtenerAlbumPorId(1L)).thenReturn(albumDetalleDTO);

        mockMvc.perform(get("/api/albumes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlbum").value(1))
                .andExpect(jsonPath("$.tituloAlbum").value("Mi Álbum"));
    }

    @Test
    @DisplayName("Obtener álbum inexistente")
    void obtenerAlbum_NoExiste() throws Exception {
        when(albumService.obtenerAlbumPorId(999L))
                .thenThrow(new AlbumNotFoundException(999L));

        mockMvc.perform(get("/api/albumes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Obtener álbumes por artista")
    void obtenerAlbumesPorArtista_Success() throws Exception {
        List<AlbumDTO> albumes = Arrays.asList(albumDTO);
        when(albumService.listarAlbumesPorArtista(1L)).thenReturn(albumes);

        mockMvc.perform(get("/api/albumes/artist/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Buscar álbumes")
    void buscarAlbumes_Success() throws Exception {
        List<AlbumDTO> albumes = Arrays.asList(albumDTO);
        when(albumService.buscarAlbumes("álbum")).thenReturn(albumes);

        mockMvc.perform(get("/api/albumes/search").param("q", "álbum"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Obtener canciones de álbum")
    void obtenerCancionesAlbum_Success() throws Exception {
        List<CancionAlbumDTO> canciones = Arrays.asList();
        when(albumService.obtenerCancionesAlbum(1L)).thenReturn(canciones);

        mockMvc.perform(get("/api/albumes/1/tracks"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Crear álbum")
    void crearAlbum_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        CrearAlbumDTO dto = CrearAlbumDTO.builder()
                .tituloAlbum("Nuevo Álbum")
                .idGenero(1L)
                .precioAlbum(9.99)
                .urlPortada("https://res.cloudinary.com/demo/image/upload/v1234567890/portada.jpg")
                .build();

        when(albumService.crearAlbum(any(CrearAlbumDTO.class), eq(1L)))
                .thenReturn(albumDTO);

        mockMvc.perform(post("/api/albumes")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAlbum").value(1));
    }

    @Test
    @DisplayName("Actualizar álbum")
    void actualizarAlbum_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        EditarAlbumDTO dto = EditarAlbumDTO.builder()
                .tituloAlbum("Álbum Actualizado")
                .build();

        AlbumDTO albumActualizado = AlbumDTO.builder()
                .idAlbum(1L)
                .tituloAlbum("Álbum Actualizado")
                .build();

        when(albumService.actualizarAlbum(eq(1L), any(EditarAlbumDTO.class), eq(1L)))
                .thenReturn(albumActualizado);

        mockMvc.perform(put("/api/albumes/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tituloAlbum").value("Álbum Actualizado"));
    }

    @Test
    @DisplayName("Eliminar álbum")
    void eliminarAlbum_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        doNothing().when(albumService).eliminarAlbum(1L, 1L);

        mockMvc.perform(delete("/api/albumes/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Agregar canción al álbum")
    void agregarCancionAlAlbum_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        AgregarCancionAlbumDTO dto = AgregarCancionAlbumDTO.builder()
                .idCancion(1L)
                .numeroPista(1)
                .build();

        doNothing().when(albumService).agregarCancionAlAlbum(eq(1L), any(AgregarCancionAlbumDTO.class), eq(1L));

        mockMvc.perform(post("/api/albumes/1/tracks")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Eliminar canción del álbum")
    void eliminarCancionDeAlbum_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        doNothing().when(albumService).eliminarCancionDeAlbum(1L, 1L, 1L);

        mockMvc.perform(delete("/api/albumes/1/tracks/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Eliminar álbumes de artista")
    void eliminarAlbumesArtista_Success() throws Exception {
        doNothing().when(albumService).eliminarTodosAlbumesArtista(1L);

        mockMvc.perform(delete("/api/albumes/artist/1")
                        .header("X-Service-Token", serviceToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Actualizar álbum de otro artista - Forbidden")
    void actualizarAlbum_OtroArtista_Forbidden() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(2L, 2L, "otro@example.com");

        EditarAlbumDTO dto = EditarAlbumDTO.builder()
                .tituloAlbum("Intento Hackeo")
                .build();

        when(albumService.actualizarAlbum(eq(1L), any(EditarAlbumDTO.class), eq(2L)))
                .thenThrow(new AccesoDenegadoException("álbum", 1L));

        mockMvc.perform(put("/api/albumes/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Obtener estadísticas de artista")
    void obtenerEstadisticasArtista_Success() throws Exception {
        EstadisticasArtistaDTO stats = EstadisticasArtistaDTO.builder()
                .idArtista(1L)
                .totalReproducciones(5000L)
                .build();

        when(albumService.obtenerEstadisticasArtista(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/albumes/artist/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idArtista").value(1))
                .andExpect(jsonPath("$.totalReproducciones").value(5000));
    }
}