package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que representa una valoración de una canción o álbum.
 *
 * <p>Contiene la puntuación otorgada, información del autor
 * e información resumida del contenido valorado.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValoracionDTO {

    /**
     * Identificador único de la valoración.
     */
    private Long idValoracion;

    /**
     * Identificador del usuario autor de la valoración.
     */
    private Long idUsuario;

    /**
     * Tipo de usuario autor.
     * Valores válidos: USUARIO, ARTISTA
     */
    private String tipoUsuario;

    /**
     * Nombre del usuario autor de la valoración.
     */
    private String nombreUsuario;

    /**
     * Tipo de contenido valorado.
     * Valores válidos: CANCION, ALBUM
     */
    private String tipoContenido;

    /**
     * Identificador del contenido valorado.
     */
    private Long idContenido;

    /**
     * Valor de la valoración en escala de 1 a 5 estrellas.
     */
    private Integer valor;

    /**
     * Fecha y hora de creación de la valoración.
     */
    private LocalDateTime fechaValoracion;

    /**
     * Fecha y hora de última edición de la valoración.
     */
    private LocalDateTime fechaUltimaEdicion;

    /**
     * Indica si la valoración ha sido editada.
     */
    private Boolean editada;

    /**
     * Título del contenido valorado.
     */
    private String tituloContenido;

    /**
     * URL de la portada del contenido en Cloudinary.
     */
    private String urlPortada;
}