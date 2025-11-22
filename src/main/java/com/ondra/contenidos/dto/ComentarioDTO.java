package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que representa un comentario en una canción o álbum.
 *
 * <p>Contiene la información del comentario, datos del autor
 * e información resumida del contenido comentado.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComentarioDTO {

    /**
     * Identificador único del comentario.
     */
    private Long idComentario;

    /**
     * Identificador del usuario autor del comentario.
     */
    private Long idUsuario;

    /**
     * Tipo de usuario autor.
     * Valores válidos: USUARIO, ARTISTA
     */
    private String tipoUsuario;

    /**
     * Nombre del usuario autor del comentario.
     */
    private String nombreUsuario;

    /**
     * Tipo de contenido comentado.
     * Valores válidos: CANCION, ALBUM
     */
    private String tipoContenido;

    /**
     * Identificador del contenido comentado.
     */
    private Long idContenido;

    /**
     * Texto del comentario.
     */
    private String contenido;

    /**
     * Fecha y hora de publicación del comentario.
     */
    private LocalDateTime fechaPublicacion;

    /**
     * Fecha y hora de última edición del comentario.
     */
    private LocalDateTime fechaUltimaEdicion;

    /**
     * Indica si el comentario ha sido editado.
     */
    private Boolean editado;

    /**
     * Título del contenido comentado.
     */
    private String tituloContenido;

    /**
     * URL de la portada del contenido en Cloudinary.
     */
    private String urlPortada;
}