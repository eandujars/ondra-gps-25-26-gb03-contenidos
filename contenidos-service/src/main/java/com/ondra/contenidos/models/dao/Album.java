package com.ondra.contenidos.models.dao;

import com.ondra.contenidos.models.enums.GeneroMusical;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un álbum musical.
 *
 * <p>Un álbum pertenece a un artista y contiene múltiples canciones
 * ordenadas por número de pista.</p>
 */
@Entity
@Table(name = "albumes", indexes = {
        @Index(name = "idx_album_artista", columnList = "id_artista"),
        @Index(name = "idx_album_genero", columnList = "genero"),
        @Index(name = "idx_album_precio", columnList = "precio_album"),
        @Index(name = "idx_album_fecha", columnList = "fecha_publicacion")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Album {

    /**
     * Id del álbum.
     * La información completa del artista se obtiene del microservicio Usuarios.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_album")
    private Long idAlbum;

    /**
     * Título del álbum.
     * La información completa del artista se obtiene del microservicio Usuarios.
     */
    @Column(name = "titulo_album", nullable = false, length = 200)
    private String tituloAlbum;

    /**
     * ID del artista propietario.
     * La información completa del artista se obtiene del microservicio Usuarios.
     */
    @Column(name = "id_artista", nullable = false)
    private Long idArtista;

    /**
     * Género musical predominante del álbum.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 50)
    private GeneroMusical genero;

    /**
     * Precio del álbum completo en euros.
     * 0.00 indica que el álbum es gratuito.
     */
    @Column(name = "precio_album", nullable = false, columnDefinition = "NUMERIC(10,2)")
    private Double precioAlbum;

    /**
     * URL de la portada del álbum almacenada en Cloudinary.
     */
    @Column(name = "url_portada", columnDefinition = "TEXT")
    private String urlPortada;

    /**
     * Fecha y hora de publicación del álbum.
     */
    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    /**
     * Descripción opcional del álbum.
     */
    @Column(name = "descripcion", length = 2000, columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Relación con canciones a través de la tabla intermedia.
     * Las canciones se ordenan automáticamente por número de pista.
     */
    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroPista ASC")
    @Builder.Default
    private List<AlbumCancion> albumCanciones = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (fechaPublicacion == null) {
            fechaPublicacion = LocalDateTime.now();
        }
    }

    /**
     * Calcula la duración total del álbum sumando las duraciones de todas sus canciones.
     *
     * @return duración total en segundos
     */
    public Integer getDuracionTotalSegundos() {
        return albumCanciones.stream()
                .map(ac -> ac.getCancion().getDuracionSegundos())
                .reduce(0, Integer::sum);
    }

    /**
     * Obtiene el número total de canciones en el álbum.
     *
     * @return número de canciones
     */
    public Integer getTotalCanciones() {
        return albumCanciones.size();
    }

    /**
     * Calcula el total de reproducciones del álbum sumando las reproducciones
     * de todas sus canciones.
     *
     * @return total de reproducciones
     */
    public Long getTotalPlayCount() {
        return albumCanciones.stream()
                .map(ac -> ac.getCancion().getReproducciones())
                .reduce(0L, Long::sum);
    }

    /**
     * Verifica si el álbum es gratuito.
     *
     * @return true si el precio es 0.00, false en caso contrario
     */
    public boolean esGratuito() {
        return this.precioAlbum != null && this.precioAlbum == 0.0;
    }

    /**
     * Verifica si una canción específica ya está en el álbum.
     *
     * @param idCancion ID de la canción a verificar
     * @return true si la canción ya está en el álbum
     */
    public boolean contieneCancion(Long idCancion) {
        return albumCanciones.stream()
                .anyMatch(ac -> ac.getCancion().getIdCancion().equals(idCancion));
    }

    /**
     * Verifica si un número de pista ya está ocupado en el álbum.
     *
     * @param numeroPista número de pista a verificar
     * @return true si el número de pista ya existe
     */
    public boolean existeNumeroPista(Integer numeroPista) {
        return albumCanciones.stream()
                .anyMatch(ac -> ac.getNumeroPista().equals(numeroPista));
    }

    /**
     * Añade una canción al álbum con un número de pista específico.
     *
     * @param cancion canción a añadir
     * @param numeroPista número de pista
     */
    public void agregarCancion(Cancion cancion, Integer numeroPista) {
        AlbumCancion albumCancion = AlbumCancion.builder()
                .album(this)
                .cancion(cancion)
                .numeroPista(numeroPista)
                .build();
        this.albumCanciones.add(albumCancion);
    }

    /**
     * Verifica si el álbum pertenece a un artista específico.
     *
     * @param idArtista ID del artista a verificar
     * @return true si el álbum pertenece al artista
     */
    public boolean perteneceArtista(Long idArtista) {
        return this.idArtista != null && this.idArtista.equals(idArtista);
    }
}