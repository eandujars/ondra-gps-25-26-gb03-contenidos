package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.GeneroDTO;
import com.ondra.contenidos.models.enums.GeneroMusical;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de géneros musicales.
 *
 * <p>Proporciona endpoints para consultar el catálogo de géneros musicales.
 * Todos los endpoints son públicos y no requieren autenticación.</p>
 *
 * <p>Utilizado principalmente por el frontend, el microservicio de Recomendaciones
 * y otros servicios para consultas de catálogo.</p>
 */
@Slf4j
@RestController
@RequestMapping("/generos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GeneroController {

    /**
     * Obtiene la lista completa de géneros musicales.
     *
     * @return Lista de todos los géneros con ID y nombre
     */
    @GetMapping
    public ResponseEntity<List<GeneroDTO>> obtenerTodosLosGeneros() {
        log.info("GET /generos - Obteniendo lista completa de géneros");

        List<GeneroDTO> generos = Arrays.stream(GeneroMusical.values())
                .map(genero -> GeneroDTO.builder()
                        .idGenero(genero.getId())
                        .nombreGenero(genero.getNombre())
                        .build())
                .collect(Collectors.toList());

        log.info("Devolviendo {} géneros", generos.size());
        return ResponseEntity.ok(generos);
    }

    /**
     * Obtiene un género musical específico por su identificador.
     *
     * @param id Identificador del género
     * @return Género encontrado o HTTP 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<GeneroDTO> obtenerGeneroPorId(@PathVariable Long id) {
        log.info("GET /generos/{} - Buscando género", id);

        try {
            GeneroMusical genero = GeneroMusical.fromId(id);

            GeneroDTO dto = GeneroDTO.builder()
                    .idGenero(genero.getId())
                    .nombreGenero(genero.getNombre())
                    .build();

            log.info("Género encontrado: {}", genero.getNombre());
            return ResponseEntity.ok(dto);

        } catch (IllegalArgumentException e) {
            log.warn("Género no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Verifica si existe un género con el identificador especificado.
     *
     * <p>Utilizado principalmente por el microservicio de Recomendaciones para
     * validar géneros antes de procesar preferencias musicales.</p>
     *
     * @param id Identificador del género a verificar
     * @return true si el género existe, false en caso contrario
     */
    @GetMapping("/{id}/existe")
    public ResponseEntity<Boolean> existeGenero(@PathVariable Long id) {
        boolean existe = GeneroMusical.existe(id);

        log.debug("GET /generos/{}/existe - Resultado: {}", id, existe);

        return ResponseEntity.ok(existe);
    }

    /**
     * Obtiene el nombre de un género musical específico.
     *
     * <p>Endpoint optimizado para obtener solo el nombre sin el objeto completo.</p>
     *
     * @param id Identificador del género
     * @return Nombre del género o HTTP 404 si no existe
     */
    @GetMapping("/{id}/nombre")
    public ResponseEntity<String> obtenerNombreGenero(@PathVariable Long id) {
        log.debug("GET /generos/{}/nombre", id);

        try {
            GeneroMusical genero = GeneroMusical.fromId(id);
            String nombre = genero.getNombre();

            log.debug("Nombre del género {}: {}", id, nombre);
            return ResponseEntity.ok(nombre);

        } catch (IllegalArgumentException e) {
            log.warn("Género no encontrado con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene la lista de identificadores de todos los géneros musicales.
     *
     * <p>Útil para validaciones rápidas y verificaciones de integridad referencial.</p>
     *
     * @return Lista de identificadores de géneros
     */
    @GetMapping("/ids")
    public ResponseEntity<List<Long>> obtenerIdsGeneros() {
        log.debug("GET /generos/ids");

        List<Long> ids = GeneroMusical.getAllIds();

        return ResponseEntity.ok(ids);
    }

    /**
     * Obtiene la lista de nombres de todos los géneros musicales.
     *
     * @return Lista de nombres de géneros
     */
    @GetMapping("/nombres")
    public ResponseEntity<List<String>> obtenerNombresGeneros() {
        log.debug("GET /generos/nombres");

        List<String> nombres = GeneroMusical.getAllNombres();

        return ResponseEntity.ok(nombres);
    }

    /**
     * Busca géneros por coincidencia parcial en el nombre.
     *
     * <p>Búsqueda insensible a mayúsculas/minúsculas. Si no se proporciona
     * término de búsqueda, devuelve todos los géneros.</p>
     *
     * @param query Término de búsqueda (opcional, por defecto: cadena vacía)
     * @return Lista de géneros que coinciden con la búsqueda
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<GeneroDTO>> buscarGeneros(
            @RequestParam(required = false, defaultValue = "") String query
    ) {
        log.info("GET /generos/buscar?query={}", query);

        if (query.isBlank()) {
            return obtenerTodosLosGeneros();
        }

        List<GeneroDTO> resultados = Arrays.stream(GeneroMusical.values())
                .filter(genero -> genero.getNombre()
                        .toLowerCase()
                        .contains(query.toLowerCase()))
                .map(genero -> GeneroDTO.builder()
                        .idGenero(genero.getId())
                        .nombreGenero(genero.getNombre())
                        .build())
                .collect(Collectors.toList());

        log.info("Encontrados {} géneros para la consulta: '{}'", resultados.size(), query);
        return ResponseEntity.ok(resultados);
    }
}