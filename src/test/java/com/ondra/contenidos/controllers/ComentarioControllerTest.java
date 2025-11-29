package com.ondra.contenidos.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.security.*;
import com.ondra.contenidos.services.ComentarioService;
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

/**
 * Tests unitarios para {@link ComentarioController}
 *
 * <p>
 * Esta clase cubre pruebas de:
 * </p>
 *
 * <ul>
 *   <li>Creación de comentarios en canciones y álbumes</li>
 *   <li>Edición de comentarios (con control de propiedad)</li>
 *   <li>Listado de comentarios por canción con paginación</li>
 *   <li>Listado de comentarios por álbum con paginación</li>
 *   <li>Listado de comentarios por usuario con paginación</li>
 *   <li>Listado de comentarios propios del usuario autenticado</li>
 *   <li>Eliminación de comentarios (autor y propietario del contenido)</li>
 *   <li>Eliminación masiva de comentarios por usuario</li>
 *   <li>Validación de permisos y manejo de errores</li>
 * </ul>
 */
@WebMvcTest(ComentarioController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
@TestPropertySource(properties = {"service.token=e3e27aa2f289a9686be1d3c45659f308748be2d8751c4911b1cecb09fdd228fa"})
class ComentarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockitoBean
    private ComentarioService comentarioService;

    @Value("${service.token}")
    private String serviceToken;

    private ComentarioDTO comentarioDTO;

    @BeforeEach
    void setUp() {
        comentarioDTO = ComentarioDTO.builder()
                .idComentario(1L)
                .idUsuario(1L)
                .tipoUsuario("USUARIO")
                .nombreUsuario("Usuario Test")
                .slug("usuario-test")
                .urlFotoPerfil("https://res.cloudinary.com/foto.jpg")
                .tipoContenido("CANCIÓN")
                .idContenido(1L)
                .contenido("Excelente canción!")
                .fechaPublicacion(LocalDateTime.now())
                .editado(false)
                .tituloContenido("Canción Test")
                .urlPortada("https://res.cloudinary.com/portada.jpg")
                .build();
    }

    // ==================== TESTS CREAR COMENTARIO ====================

    @Test
    @DisplayName("Crear comentario en canción como usuario")
    void crearComentario_Cancion_Usuario_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        CrearComentarioDTO dto = CrearComentarioDTO.builder()
                .tipoContenido("CANCIÓN")
                .idCancion(1L)
                .contenido("Excelente canción!")
                .build();

        when(comentarioService.crearComentario(eq(1L), isNull(), eq("NORMAL"), any(CrearComentarioDTO.class)))
                .thenReturn(comentarioDTO);

        mockMvc.perform(post("/api/comentarios")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idComentario").value(1))
                .andExpect(jsonPath("$.contenido").value("Excelente canción!"))
                .andExpect(jsonPath("$.tipoContenido").value("CANCIÓN"));
    }

    @Test
    @DisplayName("Crear comentario en álbum como artista")
    void crearComentario_Album_Artista_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(1L, 1L, "artista@example.com");

        CrearComentarioDTO dto = CrearComentarioDTO.builder()
                .tipoContenido("ÁLBUM")
                .idAlbum(1L)
                .contenido("Gran trabajo!")
                .build();

        ComentarioDTO comentarioArtista = ComentarioDTO.builder()
                .idComentario(1L)
                .idUsuario(1L)
                .idArtista(1L)
                .tipoUsuario("ARTISTA")
                .nombreUsuario("Artista Test")
                .tipoContenido("ÁLBUM")
                .contenido("Gran trabajo!")
                .build();

        when(comentarioService.crearComentario(eq(1L), eq(1L), eq("ARTISTA"), any(CrearComentarioDTO.class)))
                .thenReturn(comentarioArtista);

        mockMvc.perform(post("/api/comentarios")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoUsuario").value("ARTISTA"))
                .andExpect(jsonPath("$.idArtista").value(1));
    }

    @Test
    @DisplayName("Crear comentario en canción inexistente - Not Found")
    void crearComentario_CancionNoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        CrearComentarioDTO dto = CrearComentarioDTO.builder()
                .tipoContenido("CANCIÓN")
                .idCancion(999L)
                .contenido("Test")
                .build();

        when(comentarioService.crearComentario(eq(1L), isNull(), eq("NORMAL"), any(CrearComentarioDTO.class)))
                .thenThrow(new CancionNotFoundException(999L));

        mockMvc.perform(post("/api/comentarios")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Crear comentario en álbum inexistente - Not Found")
    void crearComentario_AlbumNoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        CrearComentarioDTO dto = CrearComentarioDTO.builder()
                .tipoContenido("ÁLBUM")
                .idAlbum(999L)
                .contenido("Test")
                .build();

        when(comentarioService.crearComentario(eq(1L), isNull(), eq("NORMAL"), any(CrearComentarioDTO.class)))
                .thenThrow(new AlbumNotFoundException(999L));

        mockMvc.perform(post("/api/comentarios")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTS EDITAR COMENTARIO ====================

    @Test
    @DisplayName("Editar comentario propio")
    void editarComentario_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        EditarComentarioDTO dto = EditarComentarioDTO.builder()
                .contenido("Comentario editado")
                .build();

        ComentarioDTO comentarioEditado = ComentarioDTO.builder()
                .idComentario(1L)
                .contenido("Comentario editado")
                .editado(true)
                .fechaUltimaEdicion(LocalDateTime.now())
                .build();

        when(comentarioService.editarComentario(eq(1L), eq(1L), any(EditarComentarioDTO.class)))
                .thenReturn(comentarioEditado);

        mockMvc.perform(put("/api/comentarios/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").value("Comentario editado"))
                .andExpect(jsonPath("$.editado").value(true));
    }

    @Test
    @DisplayName("Editar comentario de otro usuario - Forbidden")
    void editarComentario_OtroUsuario_Forbidden() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(2L, "usuario2@example.com");

        EditarComentarioDTO dto = EditarComentarioDTO.builder()
                .contenido("Intento editar")
                .build();

        when(comentarioService.editarComentario(eq(1L), eq(2L), any(EditarComentarioDTO.class)))
                .thenThrow(new AccesoDenegadoException("No tienes permiso para editar este comentario"));

        mockMvc.perform(put("/api/comentarios/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Editar comentario inexistente - Not Found")
    void editarComentario_NoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        EditarComentarioDTO dto = EditarComentarioDTO.builder()
                .contenido("Comentario editado")
                .build();

        when(comentarioService.editarComentario(eq(999L), eq(1L), any(EditarComentarioDTO.class)))
                .thenThrow(new ComentarioNotFoundException(999L));

        mockMvc.perform(put("/api/comentarios/999")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTS LISTAR COMENTARIOS DE CANCIÓN ====================

    @Test
    @DisplayName("Listar comentarios de canción")
    void listarComentariosCancion_Success() throws Exception {
        ComentariosPaginadosDTO paginaDTO = ComentariosPaginadosDTO.builder()
                .comentarios(Arrays.asList(comentarioDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(comentarioService.listarComentariosCancion(1L, 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/comentarios/canciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarios", hasSize(1)))
                .andExpect(jsonPath("$.comentarios[0].contenido").value("Excelente canción!"))
                .andExpect(jsonPath("$.paginaActual").value(1));
    }

    @Test
    @DisplayName("Listar comentarios de canción con paginación")
    void listarComentariosCancion_ConPaginacion() throws Exception {
        ComentariosPaginadosDTO paginaDTO = ComentariosPaginadosDTO.builder()
                .comentarios(Arrays.asList(comentarioDTO))
                .paginaActual(2)
                .totalPaginas(3)
                .totalElementos(50L)
                .elementosPorPagina(25)
                .build();

        when(comentarioService.listarComentariosCancion(1L, 2, 25))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/comentarios/canciones/1")
                        .param("page", "2")
                        .param("limit", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paginaActual").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(3))
                .andExpect(jsonPath("$.elementosPorPagina").value(25));
    }

    @Test
    @DisplayName("Listar comentarios de canción inexistente - Not Found")
    void listarComentariosCancion_CancionNoExiste() throws Exception {
        when(comentarioService.listarComentariosCancion(999L, 1, 20))
                .thenThrow(new CancionNotFoundException(999L));

        mockMvc.perform(get("/api/comentarios/canciones/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTS LISTAR COMENTARIOS DE ÁLBUM ====================

    @Test
    @DisplayName("Listar comentarios de álbum")
    void listarComentariosAlbum_Success() throws Exception {
        ComentarioDTO comentarioAlbum = ComentarioDTO.builder()
                .idComentario(2L)
                .tipoContenido("ÁLBUM")
                .contenido("Gran álbum!")
                .build();

        ComentariosPaginadosDTO paginaDTO = ComentariosPaginadosDTO.builder()
                .comentarios(Arrays.asList(comentarioAlbum))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(comentarioService.listarComentariosAlbum(1L, 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/comentarios/albumes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarios", hasSize(1)))
                .andExpect(jsonPath("$.comentarios[0].tipoContenido").value("ÁLBUM"));
    }

    @Test
    @DisplayName("Listar comentarios de álbum inexistente - Not Found")
    void listarComentariosAlbum_AlbumNoExiste() throws Exception {
        when(comentarioService.listarComentariosAlbum(999L, 1, 20))
                .thenThrow(new AlbumNotFoundException(999L));

        mockMvc.perform(get("/api/comentarios/albumes/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTS LISTAR COMENTARIOS DE USUARIO ====================

    @Test
    @DisplayName("Listar comentarios de usuario")
    void listarComentariosUsuario_Success() throws Exception {
        ComentariosPaginadosDTO paginaDTO = ComentariosPaginadosDTO.builder()
                .comentarios(Arrays.asList(comentarioDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(comentarioService.listarComentariosUsuario(1L, 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/comentarios/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarios", hasSize(1)))
                .andExpect(jsonPath("$.comentarios[0].idUsuario").value(1));
    }

    @Test
    @DisplayName("Listar mis comentarios")
    void listarMisComentarios_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        ComentariosPaginadosDTO paginaDTO = ComentariosPaginadosDTO.builder()
                .comentarios(Arrays.asList(comentarioDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(comentarioService.listarComentariosUsuario(1L, 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/comentarios/mis-comentarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarios", hasSize(1)));
    }

    // ==================== TESTS ELIMINAR COMENTARIO ====================

    @Test
    @DisplayName("Eliminar comentario propio - autor")
    void eliminarComentario_Autor_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doNothing().when(comentarioService).eliminarComentario(1L, 1L, "NORMAL");

        mockMvc.perform(delete("/api/comentarios/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comentario eliminado correctamente"));
    }

    @Test
    @DisplayName("Eliminar comentario - artista propietario del contenido")
    void eliminarComentario_ArtistaPropietario_Success() throws Exception {
        String token = testJwtHelper.generarTokenPruebaArtista(2L, 2L, "artista@example.com");

        doNothing().when(comentarioService).eliminarComentario(1L, 2L, "ARTISTA");

        mockMvc.perform(delete("/api/comentarios/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comentario eliminado correctamente"));
    }

    @Test
    @DisplayName("Eliminar comentario sin permiso - Forbidden")
    void eliminarComentario_SinPermiso_Forbidden() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(2L, "usuario2@example.com");

        doThrow(new AccesoDenegadoException("No tienes permiso para eliminar este comentario"))
                .when(comentarioService).eliminarComentario(1L, 2L, "NORMAL");

        mockMvc.perform(delete("/api/comentarios/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Eliminar comentario inexistente - Not Found")
    void eliminarComentario_NoExiste() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doThrow(new ComentarioNotFoundException(999L))
                .when(comentarioService).eliminarComentario(999L, 1L, "NORMAL");

        mockMvc.perform(delete("/api/comentarios/999")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTS ELIMINAR COMENTARIOS POR USUARIO ====================

    @Test
    @DisplayName("Eliminar todos los comentarios de usuario")
    void eliminarComentariosUsuario_Success() throws Exception {
        doNothing().when(comentarioService).eliminarTodosLosComentarios(1L);

        mockMvc.perform(delete("/api/comentarios/usuarios/1")
                        .header("X-Service-Token", serviceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Todos los comentarios del usuario eliminados"));
    }
}