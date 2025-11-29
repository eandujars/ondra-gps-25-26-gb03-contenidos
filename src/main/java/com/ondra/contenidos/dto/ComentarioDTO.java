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
     * Identificador del artista autor del comentario.
     */
    private Long idArtista;

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
     * Slug del usuario o artista para URLs amigables.
     */
    private String slug;

    /**
     * URL de la foto de perfil del usuario en Cloudinary.
     */
    private String urlFotoPerfil;

    /**
     * Tipo de contenido comentado.
     * Valores válidos: CANCION, ÁLBUM
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