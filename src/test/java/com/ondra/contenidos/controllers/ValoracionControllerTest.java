package com.ondra.contenidos.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.security.*;
import com.ondra.contenidos.services.ValoracionService;
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

@WebMvcTest(ValoracionController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
@TestPropertySource(properties = {"service.token=e3e27aa2f289a9686be1d3c45659f308748be2d8751c4911b1cecb09fdd228fa"})
class ValoracionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtHelper testJwtHelper;

    @MockitoBean
    private ValoracionService valoracionService;

    @Value("${service.token}")
    private String serviceToken;

    private ValoracionDTO valoracionDTO;

    @BeforeEach
    void setUp() {
        valoracionDTO = ValoracionDTO.builder()
                .idValoracion(1L)
                .idUsuario(1L)
                .tipoUsuario("USUARIO")
                .nombreUsuario("Usuario Test")
                .tipoContenido("CANCIÓN")
                .idContenido(1L)
                .valor(5)
                .fechaValoracion(LocalDateTime.now())
                .editada(false)
                .tituloContenido("Canción Test")
                .urlPortada("https://res.cloudinary.com/portada.jpg")
                .build();
    }

    @Test
    @DisplayName("Crear valoración de canción")
    void crearValoracion_Cancion_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        CrearValoracionDTO dto = CrearValoracionDTO.builder()
                .tipoContenido("CANCIÓN")
                .idCancion(1L)
                .valor(5)
                .build();

        when(valoracionService.crearValoracion(eq(1L), eq("NORMAL"), any(CrearValoracionDTO.class)))
                .thenReturn(valoracionDTO);

        mockMvc.perform(post("/api/valoraciones")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idValoracion").value(1))
                .andExpect(jsonPath("$.valor").value(5));
    }

    @Test
    @DisplayName("Crear valoración duplicada - Conflict")
    void crearValoracion_Duplicada_Conflict() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        CrearValoracionDTO dto = CrearValoracionDTO.builder()
                .tipoContenido("CANCIÓN")
                .idCancion(1L)
                .valor(5)
                .build();

        when(valoracionService.crearValoracion(eq(1L), eq("NORMAL"), any(CrearValoracionDTO.class)))
                .thenThrow(new ValoracionYaExisteException("Ya has valorado esta canción"));

        mockMvc.perform(post("/api/valoraciones")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Editar valoración")
    void editarValoracion_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        EditarValoracionDTO dto = EditarValoracionDTO.builder()
                .valor(4)
                .build();

        ValoracionDTO valoracionEditada = ValoracionDTO.builder()
                .idValoracion(1L)
                .valor(4)
                .editada(true)
                .fechaUltimaEdicion(LocalDateTime.now())
                .build();

        when(valoracionService.editarValoracion(eq(1L), eq(1L), any(EditarValoracionDTO.class)))
                .thenReturn(valoracionEditada);

        mockMvc.perform(put("/api/valoraciones/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(4))
                .andExpect(jsonPath("$.editada").value(true));
    }

    @Test
    @DisplayName("Editar valoración de otro usuario - Forbidden")
    void editarValoracion_OtroUsuario_Forbidden() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(2L, "usuario2@example.com");

        EditarValoracionDTO dto = EditarValoracionDTO.builder()
                .valor(4)
                .build();

        when(valoracionService.editarValoracion(eq(1L), eq(2L), any(EditarValoracionDTO.class)))
                .thenThrow(new AccesoDenegadoException("No tienes permiso para editar esta valoración"));

        mockMvc.perform(put("/api/valoraciones/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Obtener mi valoración de canción - existente")
    void obtenerMiValoracionCancion_Existe_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(valoracionService.obtenerValoracionUsuarioCancion(1L, 1L))
                .thenReturn(valoracionDTO);

        mockMvc.perform(get("/api/valoraciones/canciones/1/mi-valoracion")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idValoracion").value(1))
                .andExpect(jsonPath("$.valor").value(5));
    }

    @Test
    @DisplayName("Obtener mi valoración de canción - no existe")
    void obtenerMiValoracionCancion_NoExiste_NoContent() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        when(valoracionService.obtenerValoracionUsuarioCancion(1L, 1L))
                .thenReturn(null);

        mockMvc.perform(get("/api/valoraciones/canciones/1/mi-valoracion")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Listar valoraciones de canción")
    void listarValoracionesCancion_Success() throws Exception {
        ValoracionesPaginadasDTO paginaDTO = ValoracionesPaginadasDTO.builder()
                .valoraciones(Arrays.asList(valoracionDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .valoracionPromedio(5.0)
                .build();

        when(valoracionService.listarValoracionesCancion(1L, 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/valoraciones/canciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valoraciones", hasSize(1)))
                .andExpect(jsonPath("$.valoracionPromedio").value(5.0));
    }

    @Test
    @DisplayName("Listar valoraciones de álbum")
    void listarValoracionesAlbum_Success() throws Exception {
        ValoracionesPaginadasDTO paginaDTO = ValoracionesPaginadasDTO.builder()
                .valoraciones(Arrays.asList(valoracionDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .valoracionPromedio(4.5)
                .build();

        when(valoracionService.listarValoracionesAlbum(1L, 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/valoraciones/albumes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valoraciones", hasSize(1)))
                .andExpect(jsonPath("$.valoracionPromedio").value(4.5));
    }

    @Test
    @DisplayName("Obtener promedio de canción")
    void obtenerPromedioCancion_Success() throws Exception {
        when(valoracionService.obtenerPromedioCancion(1L)).thenReturn(4.5);

        mockMvc.perform(get("/api/valoraciones/canciones/1/promedio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCancion").value(1))
                .andExpect(jsonPath("$.valoracionPromedio").value(4.5))
                .andExpect(jsonPath("$.tieneValoraciones").value(true));
    }

    @Test
    @DisplayName("Obtener promedio de canción sin valoraciones")
    void obtenerPromedioCancion_SinValoraciones_Success() throws Exception {
        when(valoracionService.obtenerPromedioCancion(1L)).thenReturn(null);

        mockMvc.perform(get("/api/valoraciones/canciones/1/promedio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valoracionPromedio").value(0.0))
                .andExpect(jsonPath("$.tieneValoraciones").value(false));
    }

    @Test
    @DisplayName("Listar valoraciones de usuario")
    void listarValoracionesUsuario_Success() throws Exception {
        ValoracionesPaginadasDTO paginaDTO = ValoracionesPaginadasDTO.builder()
                .valoraciones(Arrays.asList(valoracionDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(valoracionService.listarValoracionesUsuario(1L, 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/valoraciones/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valoraciones", hasSize(1)));
    }

    @Test
    @DisplayName("Listar mis valoraciones")
    void listarMisValoraciones_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        ValoracionesPaginadasDTO paginaDTO = ValoracionesPaginadasDTO.builder()
                .valoraciones(Arrays.asList(valoracionDTO))
                .paginaActual(1)
                .totalPaginas(1)
                .totalElementos(1L)
                .elementosPorPagina(20)
                .build();

        when(valoracionService.listarValoracionesUsuario(1L, 1, 20))
                .thenReturn(paginaDTO);

        mockMvc.perform(get("/api/valoraciones/mis-valoraciones")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valoraciones", hasSize(1)));
    }

    @Test
    @DisplayName("Eliminar valoración - autor")
    void eliminarValoracion_Autor_Success() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doNothing().when(valoracionService).eliminarValoracion(1L, 1L, "NORMAL");

        mockMvc.perform(delete("/api/valoraciones/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Valoración eliminada correctamente"));
    }

    @Test
    @DisplayName("Eliminar valoración - sin permiso")
    void eliminarValoracion_SinPermiso_Forbidden() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(2L, "usuario2@example.com");

        doThrow(new AccesoDenegadoException("No tienes permiso para eliminar esta valoración"))
                .when(valoracionService).eliminarValoracion(1L, 2L, "NORMAL");

        mockMvc.perform(delete("/api/valoraciones/1")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Eliminar valoración inexistente")
    void eliminarValoracion_NoExiste_NotFound() throws Exception {
        String token = testJwtHelper.generarTokenPrueba(1L, "usuario@example.com");

        doThrow(new ValoracionNotFoundException(999L))
                .when(valoracionService).eliminarValoracion(999L, 1L, "NORMAL");

        mockMvc.perform(delete("/api/valoraciones/999")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Eliminar todas las valoraciones de usuario")
    void eliminarValoracionesUsuario_Success() throws Exception {
        doNothing().when(valoracionService).eliminarTodasLasValoraciones(1L);

        mockMvc.perform(delete("/api/valoraciones/usuarios/1")
                        .header("X-Service-Token", serviceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Todas las valoraciones del usuario eliminadas"));
    }

    @Test
    @DisplayName("Obtener promedio de álbum")
    void obtenerPromedioAlbum_Success() throws Exception {
        when(valoracionService.obtenerPromedioAlbum(1L)).thenReturn(4.8);

        mockMvc.perform(get("/api/valoraciones/albumes/1/promedio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlbum").value(1))
                .andExpect(jsonPath("$.valoracionPromedio").value(4.8))
                .andExpect(jsonPath("$.tieneValoraciones").value(true));
    }
}