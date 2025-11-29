package com.ondra.contenidos.controllers;

import com.ondra.contenidos.models.enums.GeneroMusical;
import com.ondra.contenidos.security.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GeneroController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, ServiceTokenFilter.class, TestJwtHelper.class})
@TestPropertySource(properties = {"service.token=e3e27aa2f289a9686be1d3c45659f308748be2d8751c4911b1cecb09fdd228fa"})
class GeneroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Obtener todos los géneros")
    void obtenerTodosLosGeneros_Success() throws Exception {
        mockMvc.perform(get("/api/generos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(30)))
                .andExpect(jsonPath("$[0].idGenero").exists())
                .andExpect(jsonPath("$[0].nombreGenero").exists());
    }

    @Test
    @DisplayName("Obtener género por ID - existente")
    void obtenerGeneroPorId_Existe_Success() throws Exception {
        mockMvc.perform(get("/api/generos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idGenero").value(1))
                .andExpect(jsonPath("$.nombreGenero").value("Rock"));
    }

    @Test
    @DisplayName("Obtener género por ID - no existente")
    void obtenerGeneroPorId_NoExiste() throws Exception {
        mockMvc.perform(get("/api/generos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Verificar si existe un género - existente")
    void existeGenero_Existe_Success() throws Exception {
        mockMvc.perform(get("/api/generos/1/existe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("Verificar si existe un género - no existente")
    void existeGenero_NoExiste_Success() throws Exception {
        mockMvc.perform(get("/api/generos/999/existe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    @DisplayName("Obtener nombre de género - existente")
    void obtenerNombreGenero_Existe_Success() throws Exception {
        mockMvc.perform(get("/api/generos/2/nombre"))
                .andExpect(status().isOk())
                .andExpect(content().string("Pop"));
    }

    @Test
    @DisplayName("Obtener nombre de género - no existente")
    void obtenerNombreGenero_NoExiste() throws Exception {
        mockMvc.perform(get("/api/generos/999/nombre"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Obtener todos los IDs de géneros")
    void obtenerIdsGeneros_Success() throws Exception {
        mockMvc.perform(get("/api/generos/ids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(30)))
                .andExpect(jsonPath("$[0]").isNumber());
    }

    @Test
    @DisplayName("Obtener todos los nombres de géneros")
    void obtenerNombresGeneros_Success() throws Exception {
        mockMvc.perform(get("/api/generos/nombres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(30)))
                .andExpect(jsonPath("$[0]").isString());
    }

    @Test
    @DisplayName("Buscar géneros - con término de búsqueda")
    void buscarGeneros_ConQuery_Success() throws Exception {
        mockMvc.perform(get("/api/generos/buscar").param("query", "rock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].nombreGenero", containsStringIgnoringCase("rock")));
    }

    @Test
    @DisplayName("Buscar géneros - sin término de búsqueda")
    void buscarGeneros_SinQuery_Success() throws Exception {
        mockMvc.perform(get("/api/generos/buscar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(30)));
    }

    @Test
    @DisplayName("Buscar géneros - término vacío")
    void buscarGeneros_QueryVacio_Success() throws Exception {
        mockMvc.perform(get("/api/generos/buscar").param("query", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(30)));
    }

    @Test
    @DisplayName("Buscar géneros - sin resultados")
    void buscarGeneros_SinResultados_Success() throws Exception {
        mockMvc.perform(get("/api/generos/buscar").param("query", "xyz123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Obtener género específico - Pop")
    void obtenerGenero_Pop_Success() throws Exception {
        mockMvc.perform(get("/api/generos/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idGenero").value(2))
                .andExpect(jsonPath("$.nombreGenero").value("Pop"));
    }

    @Test
    @DisplayName("Obtener género específico - Jazz")
    void obtenerGenero_Jazz_Success() throws Exception {
        mockMvc.perform(get("/api/generos/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idGenero").value(3))
                .andExpect(jsonPath("$.nombreGenero").value("Jazz"));
    }

    @Test
    @DisplayName("Buscar géneros - case insensitive")
    void buscarGeneros_CaseInsensitive_Success() throws Exception {
        mockMvc.perform(get("/api/generos/buscar").param("query", "ROCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].nombreGenero", containsStringIgnoringCase("rock")));
    }
}