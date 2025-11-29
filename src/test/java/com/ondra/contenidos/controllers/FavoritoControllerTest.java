package com.ondra.contenidos.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.security.*;
import com.ondra.contenidos.services.FavoritoService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FavoritoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
@TestPropertySource(properties = {"service.token=e3e27aa2f289a9686be1d3c45659f308748be2d8751c4911b1cecb09fdd228fa"})
class FavoritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockitoBean
    private FavoritoService favoritoService;

    @Value("${service.token}")
    private String serviceToken;

    private FavoritoDTO favoritoDTO;

    @BeforeEach
    void setUp() {
        favoritoDTO = FavoritoDTO.builder()
                .idFavorito(1L)
                .idUsuario(1L)
                .tipoContenido("CANCIÓN")
                .fechaAgregado(LocalDateTime.now())
                .nombreArtista("Artista Test")
                .slugArtista("artista-test")
                .build();
    }

    @Test
    @DisplayName("Agregar canción a favoritos")
    void agregarFavorito_Cancion_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        AgregarFavoritoDTO dto = AgregarFavoritoDTO.builder()
                .tipoContenido("CANCIÓN")
                .idCancion(1L)
                .build();

        when(favoritoService.agregarFavorito(eq(1L), any(AgregarFavoritoDTO.class)))
                .thenReturn(favoritoDTO);

        mockMvc.perform(post("/api/favoritos")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idFavorito").value(1))
                .andExpect(jsonPath("$.tipoContenido").value("CANCIÓN"));
    }

    @Test
    @DisplayName("Agregar álbum a favoritos")
    void agregarFavorito_Album_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        AgregarFavoritoDTO dto = AgregarFavoritoDTO.builder()
                .tipoContenido("ÁLBUM")
                .idAlbum(1L)
                .build();

        FavoritoDTO favoritoAlbum = FavoritoDTO.builder()
                .idFavorito(2L)
                .idUsuario(1L)
                .tipoContenido("ÁLBUM")
                .fechaAgregado(LocalDateTime.now())
                .build();

        when(favoritoService.agregarFavorito(eq(1L), any(AgregarFavoritoDTO.class)))
                .thenReturn(favoritoAlbum);

        mockMvc.perform(post("/api/favoritos")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoContenido").value("ÁLBUM"));
    }

    @Test
    @DisplayName("Agregar favorito duplicado - Conflict")
    void agregarFavorito_Duplicado_Conflict() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        AgregarFavoritoDTO dto = AgregarFavoritoDTO.builder()
                .tipoContenido("CANCIÓN")
                .idCancion(1L)
                .build();

        when(favoritoService.agregarFavorito(eq(1L), any(AgregarFavoritoDTO.class)))
                .thenThrow(new FavoritoYaExisteException("La canción ya está en favoritos"));

        mockMvc.perform(post("/api/favoritos")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Listar favoritos del usuario autenticado")
    void listarFavoritos_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        FavoritosPaginadosDTO paginaDTO = FavoritosPaginadosDTO.builder()
                .favoritos(Arrays.asList(favoritoDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(favoritoService.listarFavoritos(1L, null, 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/favoritos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favoritos", hasSize(1)))
                .andExpect(jsonPath("$.favoritos[0].idFavorito").value(1));
    }

    @Test
    @DisplayName("Listar favoritos con filtro por tipo")
    void listarFavoritos_ConFiltroTipo_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        FavoritosPaginadosDTO paginaDTO = FavoritosPaginadosDTO.builder()
                .favoritos(Arrays.asList(favoritoDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(favoritoService.listarFavoritos(1L, "CANCIÓN", 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/favoritos")
                        .header("Authorization", "Bearer " + token)
                        .param("tipo", "CANCIÓN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favoritos", hasSize(1)));
    }

    @Test
    @DisplayName("Eliminar canción de favoritos")
    void eliminarCancionDeFavoritos_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doNothing().when(favoritoService).eliminarFavoritoCancion(1L, 1L);

        mockMvc.perform(delete("/api/favoritos/canciones/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Canción eliminada de favoritos"));
    }

    @Test
    @DisplayName("Eliminar canción no favorita - Not Found")
    void eliminarCancionDeFavoritos_NoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doThrow(new FavoritoNotFoundException("La canción no está en favoritos"))
                .when(favoritoService).eliminarFavoritoCancion(1L, 1L);

        mockMvc.perform(delete("/api/favoritos/canciones/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Eliminar álbum de favoritos")
    void eliminarAlbumDeFavoritos_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doNothing().when(favoritoService).eliminarFavoritoAlbum(1L, 1L);

        mockMvc.perform(delete("/api/favoritos/albumes/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Álbum eliminado de favoritos"));
    }

    @Test
    @DisplayName("Verificar si canción es favorita - true")
    void verificarCancionFavorita_EsFavorita_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(favoritoService.esCancionFavorita(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/favoritos/canciones/1/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("Verificar si canción es favorita - false")
    void verificarCancionFavorita_NoEsFavorita_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(favoritoService.esCancionFavorita(1L, 1L)).thenReturn(false);

        mockMvc.perform(get("/api/favoritos/canciones/1/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    @DisplayName("Verificar si álbum es favorito - true")
    void verificarAlbumFavorito_EsFavorito_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(favoritoService.esAlbumFavorito(1L, 1L)).thenReturn(true);

        mockMvc.perform(get("/api/favoritos/albumes/1/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("Eliminar todos los favoritos de un usuario")
    void eliminarFavoritosUsuario_Success() throws Exception {
        doNothing().when(favoritoService).eliminarTodosLosFavoritos(1L);

        mockMvc.perform(delete("/api/favoritos/usuarios/1")
                        .header("X-Service-Token", serviceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Todos los favoritos del usuario eliminados"));
    }

    @Test
    @DisplayName("Agregar favorito sin autenticación - Forbidden")
    void agregarFavorito_SinAuth_Forbidden() throws Exception {
        AgregarFavoritoDTO dto = AgregarFavoritoDTO.builder()
                .tipoContenido("CANCIÓN")
                .idCancion(1L)
                .build();

        mockMvc.perform(post("/api/favoritos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}