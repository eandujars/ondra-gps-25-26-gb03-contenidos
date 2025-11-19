package com.ondra.contenidos.models.dao;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tabla intermedia para la relación muchos-a-muchos entre Album y Cancion.
 *
 * <p>Permite que una canción aparezca en múltiples álbumes con diferentes
 * números de pista. Cada combinación álbum-canción es única.</p>
 */
@Entity
@Table(name = "album_canciones",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_album_cancion",
                        columnNames = {"id_album", "id_cancion"}
                ),
                @UniqueConstraint(
                        name = "uk_album_pista",
                        columnNames = {"id_album", "numero_pista"}
                )
        },
        indexes = {
                @Index(name = "idx_album_cancion_album", columnList = "id_album"),
                @Index(name = "idx_album_cancion_cancion", columnList = "id_cancion"),
                @Index(name = "idx_album_cancion_pista", columnList = "numero_pista")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumCancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Álbum al que pertenece esta relación.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_album", nullable = false)
    private Album album;

    /**
     * Canción incluida en el álbum.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cancion", nullable = false)
    private Cancion cancion;

    /**
     * Número de pista dentro del álbum (1-indexed).
     * Debe ser único por álbum.
     */
    @Column(name = "numero_pista", nullable = false)
    private Integer numeroPista;

    /**
     * Fecha en que se agregó la canción al álbum.
     */
    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado;

    @PrePersist
    protected void onCreate() {
        if (fechaAgregado == null) {
            fechaAgregado = LocalDateTime.now();
        }
    }
}