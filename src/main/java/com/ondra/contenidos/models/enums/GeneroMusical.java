package com.ondra.contenidos.models.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enumeración de géneros musicales disponibles en la plataforma.
 *
 * <p>Cada género tiene un identificador único y un nombre asociado.
 * Los IDs son estables y corresponden con los registros en la base de datos.</p>
 */
@Getter
public enum GeneroMusical {
    ROCK(1L, "Rock"),
    POP(2L, "Pop"),
    JAZZ(3L, "Jazz"),
    BLUES(4L, "Blues"),
    CLASICA(5L, "Clásica"),
    REGGAE(6L, "Reggae"),
    COUNTRY(7L, "Country"),
    ELECTRONICA(8L, "Electrónica"),
    HIP_HOP(9L, "Hip Hop"),
    RNB(10L, "R&B"),
    SOUL(11L, "Soul"),
    FUNK(12L, "Funk"),
    METAL(13L, "Metal"),
    PUNK(14L, "Punk"),
    INDIE(15L, "Indie"),
    FOLK(16L, "Folk"),
    LATINA(17L, "Latina"),
    SALSA(18L, "Salsa"),
    REGGAETON(19L, "Reggaeton"),
    FLAMENCO(20L, "Flamenco"),
    TANGO(21L, "Tango"),
    BACHATA(22L, "Bachata"),
    MERENGUE(23L, "Merengue"),
    CUMBIA(24L, "Cumbia"),
    DUBSTEP(25L, "Dubstep"),
    HOUSE(26L, "House"),
    TECHNO(27L, "Techno"),
    TRAP(28L, "Trap"),
    KPOP(29L, "K-Pop"),
    ANIME(30L, "Anime");

    private final Long id;
    private final String nombre;

    GeneroMusical(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    /**
     * Obtiene un género por su identificador.
     *
     * @param id identificador del género
     * @return género musical encontrado
     * @throws IllegalArgumentException si no existe el género
     */
    public static GeneroMusical fromId(Long id) {
        return Arrays.stream(values())
                .filter(genero -> genero.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Género no encontrado con ID: " + id
                ));
    }

    /**
     * Verifica si existe un género con el identificador dado.
     *
     * @param id identificador a verificar
     * @return true si existe el género
     */
    public static boolean existe(Long id) {
        return Arrays.stream(values())
                .anyMatch(genero -> genero.getId().equals(id));
    }

    /**
     * Obtiene un género por su nombre.
     *
     * @param nombre nombre del género
     * @return género musical encontrado
     * @throws IllegalArgumentException si no existe el género
     */
    public static GeneroMusical fromNombre(String nombre) {
        return Arrays.stream(values())
                .filter(genero -> genero.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Género no encontrado con nombre: " + nombre
                ));
    }

    /**
     * Obtiene todos los identificadores de géneros disponibles.
     *
     * @return lista de identificadores
     */
    public static List<Long> getAllIds() {
        return Arrays.stream(values())
                .map(GeneroMusical::getId)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los nombres de géneros disponibles.
     *
     * @return lista de nombres
     */
    public static List<String> getAllNombres() {
        return Arrays.stream(values())
                .map(GeneroMusical::getNombre)
                .collect(Collectors.toList());
    }
}