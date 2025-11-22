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
 * Entidad que representa una valoración sobre una canción o álbum.
 *
 * <p>Las valoraciones son puntuaciones de 1 a 5 estrellas realizadas por usuarios
 * o artistas. Cada usuario puede valorar un contenido una única vez.</p>
 */
@Entity
@Table(name = "valoraciones",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"id_usuario", "id_cancion"}),
                @UniqueConstraint(columnNames = {"id_usuario", "id_album"})
        },
        indexes = {
                @Index(name = "idx_valoracion_usuario", columnList = "id_usuario"),
                @Index(name = "idx_valoracion_cancion", columnList = "id_cancion"),
                @Index(name = "idx_valoracion_album", columnList = "id_album"),
                @Index(name = "idx_valoracion_tipo_contenido", columnList = "tipo_contenido"),
                @Index(name = "idx_valoracion_fecha", columnList = "fecha_valoracion")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Valoracion {

    /**
     * Identificador único de la valoración.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_valoracion")
    private Long idValoracion;

    /**
     * Identificador del usuario que realizó la valoración.
     */
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    /**
     * Tipo de usuario que realizó la valoración.
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
     * Canción valorada.
     * Null si la valoración es para un álbum.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cancion")
    private Cancion cancion;

    /**
     * Álbum valorado.
     * Null si la valoración es para una canción.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_album")
    private Album album;

    /**
     * Tipo de contenido valorado.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", nullable = false, length = 20)
    private TipoContenido tipoContenido;

    /**
     * Puntuación de la valoración.
     * Rango válido: 1 a 5 estrellas.
     */
    @Column(name = "valor", nullable = false)
    private Integer valor;

    /**
     * Fecha y hora de la valoración.
     */
    @Column(name = "fecha_valoracion", nullable = false)
    private LocalDateTime fechaValoracion;

    /**
     * Fecha y hora de la última edición de la valoración.
     */
    @Column(name = "fecha_ultima_edicion")
    private LocalDateTime fechaUltimaEdicion;

    /**
     * Establece la fecha de valoración al crear el registro.
     */
    @PrePersist
    protected void onCreate() {
        fechaValoracion = LocalDateTime.now();
    }

    /**
     * Actualiza la fecha de última edición al modificar la valoración.
     */
    @PreUpdate
    protected void onUpdate() {
        fechaUltimaEdicion = LocalDateTime.now();
    }

    /**
     * Valida la integridad de la valoración.
     *
     * @return true si tiene exactamente un contenido y valor válido
     */
    public boolean esValida() {
        boolean tieneContenidoValido = (cancion != null && album == null && tipoContenido == TipoContenido.CANCION) ||
                (album != null && cancion == null && tipoContenido == TipoContenido.ALBUM);
        boolean tieneValorValido = valor != null && valor >= 1 && valor <= 5;
        return tieneContenidoValido && tieneValorValido;
    }

    /**
     * Verifica si la valoración fue editada.
     *
     * @return true si existe fecha de última edición
     */
    public boolean fueEditada() {
        return fechaUltimaEdicion != null;
    }
}