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
 * Controlador REST para gestión de géneros musicales.
 *
 * <p>Proporciona endpoints públicos para consultar el catálogo de géneros musicales
 * disponibles en el sistema. Utilizado por el frontend y otros microservicios
 * para consultas de catálogo y validaciones.</p>
 *
 * <p>Base URL: /api/generos</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/generos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GeneroController {

    /**
     * Obtiene la lista completa de géneros musicales disponibles.
     *
     * @return lista de todos los géneros con identificador y nombre
     */
    @GetMapping
    public ResponseEntity<List<GeneroDTO>> obtenerTodosLosGeneros() {
        log.info("GET /api/generos - Obteniendo lista completa de géneros");

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
     * Obtiene un género musical específico por identificador.
     *
     * @param id identificador del género
     * @return género encontrado o 404 si no existe
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
     * Utilizado por el microservicio de Recomendaciones para validar preferencias musicales.
     *
     * @param id identificador del género a verificar
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
     * @param id identificador del género
     * @return nombre del género o 404 si no existe
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
     * Utilizado para validaciones rápidas y verificaciones de integridad referencial.
     *
     * @return lista de identificadores de géneros
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
     * @return lista de nombres de géneros
     */
    @GetMapping("/nombres")
    public ResponseEntity<List<String>> obtenerNombresGeneros() {
        log.debug("GET /generos/nombres");

        List<String> nombres = GeneroMusical.getAllNombres();

        return ResponseEntity.ok(nombres);
    }

    /**
     * Busca géneros por coincidencia parcial en el nombre.
     * Búsqueda insensible a mayúsculas. Si no se proporciona término, devuelve todos los géneros.
     *
     * @param query término de búsqueda (opcional)
     * @return lista de géneros coincidentes
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