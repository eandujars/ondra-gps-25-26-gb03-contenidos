package com.ondra.contenidos.models.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorito")
    private Long idFavorito;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cancion")
    private Cancion cancion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_album")
    private Album album;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", nullable = false, length = 20)
    private TipoContenido tipoContenido;

    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado;

    @PrePersist
    protected void onCreate() {
        fechaAgregado = LocalDateTime.now();
    }

    public enum TipoContenido {
        CANCION,
        ALBUM
    }

    /**
     * Valida que el favorito tenga exactamente un contenido (canción o álbum)
     */
    public boolean esValido() {
        return (cancion != null && album == null && tipoContenido == TipoContenido.CANCION) ||
               (album != null && cancion == null && tipoContenido == TipoContenido.ALBUM);
    }
}