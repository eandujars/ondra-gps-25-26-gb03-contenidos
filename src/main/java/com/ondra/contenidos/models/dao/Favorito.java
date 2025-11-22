package com.ondra.contenidos.models.dao;

import com.ondra.contenidos.models.enums.TipoContenido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un favorito de un usuario.
 *
 * <p>Los usuarios pueden marcar canciones o álbumes como favoritos.
 * Cada combinación usuario-contenido es única.</p>
 */
@Entity
@Table(name = "favoritos",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"id_usuario", "id_cancion"}),
                @UniqueConstraint(columnNames = {"id_usuario", "id_album"})
        },
        indexes = {
                @Index(name = "idx_favorito_usuario", columnList = "id_usuario"),
                @Index(name = "idx_favorito_cancion", columnList = "id_cancion"),
                @Index(name = "idx_favorito_album", columnList = "id_album"),
                @Index(name = "idx_favorito_tipo", columnList = "tipo_contenido")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorito {

    /**
     * Identificador único del favorito.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorito")
    private Long idFavorito;

    /**
     * Identificador del usuario propietario del favorito.
     */
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    /**
     * Canción marcada como favorita.
     * Null si el favorito es un álbum.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cancion")
    private Cancion cancion;

    /**
     * Álbum marcado como favorito.
     * Null si el favorito es una canción.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_album")
    private Album album;

    /**
     * Tipo de contenido favorito.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", nullable = false, length = 20)
    private TipoContenido tipoContenido;

    /**
     * Fecha en que se agregó el favorito.
     */
    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado;

    /**
     * Establece la fecha de agregación al crear el favorito.
     */
    @PrePersist
    protected void onCreate() {
        fechaAgregado = LocalDateTime.now();
    }

    /**
     * Valida que el favorito tenga exactamente un contenido.
     *
     * @return true si el favorito tiene solo canción o solo álbum
     */
    public boolean esValido() {
        return (cancion != null && album == null && tipoContenido == TipoContenido.CANCION) ||
                (album != null && cancion == null && tipoContenido == TipoContenido.ALBUM);
    }
}