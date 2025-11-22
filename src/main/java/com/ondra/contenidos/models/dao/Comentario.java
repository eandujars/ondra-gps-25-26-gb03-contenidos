package com.ondra.contenidos.models.dao;

import com.ondra.contenidos.models.enums.TipoContenido;
import com.ondra.contenidos.models.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un comentario sobre una canción o álbum.
 *
 * <p>Los comentarios pueden ser realizados por usuarios normales o artistas,
 * y cada comentario está asociado a un único contenido.</p>
 */
@Entity
@Table(name = "comentarios",
        indexes = {
                @Index(name = "idx_comentario_usuario", columnList = "id_usuario"),
                @Index(name = "idx_comentario_cancion", columnList = "id_cancion"),
                @Index(name = "idx_comentario_album", columnList = "id_album"),
                @Index(name = "idx_comentario_tipo_contenido", columnList = "tipo_contenido"),
                @Index(name = "idx_comentario_fecha", columnList = "fecha_publicacion")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comentario {

    /**
     * Identificador único del comentario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comentario")
    private Long idComentario;

    /**
     * Identificador del usuario que realizó el comentario.
     */
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    /**
     * Tipo de usuario que realizó el comentario.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false, length = 20)
    private TipoUsuario tipoUsuario;

    /**
     * Nombre del usuario o nombre artístico del artista.
     */
    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombreUsuario;

    /**
     * Canción comentada.
     * Null si el comentario es para un álbum.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cancion")
    private Cancion cancion;

    /**
     * Álbum comentado.
     * Null si el comentario es para una canción.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_album")
    private Album album;

    /**
     * Tipo de contenido comentado.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", nullable = false, length = 20)
    private TipoContenido tipoContenido;

    /**
     * Contenido del comentario.
     */
    @Column(name = "contenido", nullable = false, length = 1000, columnDefinition = "TEXT")
    private String contenido;

    /**
     * Fecha y hora de publicación del comentario.
     */
    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    /**
     * Fecha y hora de la última edición del comentario.
     */
    @Column(name = "fecha_ultima_edicion")
    private LocalDateTime fechaUltimaEdicion;

    /**
     * Establece la fecha de publicación al crear el comentario.
     */
    @PrePersist
    protected void onCreate() {
        fechaPublicacion = LocalDateTime.now();
    }

    /**
     * Actualiza la fecha de última edición al modificar el comentario.
     */
    @PreUpdate
    protected void onUpdate() {
        fechaUltimaEdicion = LocalDateTime.now();
    }

    /**
     * Valida que el comentario tenga exactamente un contenido.
     *
     * @return true si el comentario tiene solo canción o solo álbum
     */
    public boolean esValido() {
        return (cancion != null && album == null && tipoContenido == TipoContenido.CANCION) ||
                (album != null && cancion == null && tipoContenido == TipoContenido.ALBUM);
    }

    /**
     * Verifica si el comentario fue editado.
     *
     * @return true si existe fecha de última edición
     */
    public boolean fueEditado() {
        return fechaUltimaEdicion != null;
    }
}