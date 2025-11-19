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
 * Entidad que representa una canción en el catálogo de la plataforma.
 *
 * <p>Una canción pertenece a un artista y puede estar asociada a múltiples álbumes
 * mediante la tabla intermedia AlbumCancion. Incluye información sobre el archivo
 * de audio, portada, duración y estadísticas de reproducción.</p>
 */
@Entity
@Table(name = "canciones", indexes = {
        @Index(name = "idx_cancion_artista", columnList = "id_artista"),
        @Index(name = "idx_cancion_genero", columnList = "genero"),
        @Index(name = "idx_cancion_precio", columnList = "precio_cancion"),
        @Index(name = "idx_cancion_fecha", columnList = "fecha_publicacion"),
        @Index(name = "idx_cancion_reproducciones", columnList = "reproducciones")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cancion")
    private Long idCancion;

    @Column(name = "titulo_cancion", nullable = false, length = 200)
    private String tituloCancion;

    /**
     * Identificador del artista propietario de la canción.
     * Los datos completos del artista se obtienen del microservicio de usuarios.
     */
    @Column(name = "id_artista", nullable = false)
    private Long idArtista;

    /**
     * Género musical de la canción.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 50)
    private GeneroMusical genero;

    /**
     * Precio de la canción en euros. El valor 0.00 indica contenido gratuito.
     */
    @Column(name = "precio_cancion", nullable = false, columnDefinition = "NUMERIC(10,2)")
    private Double precioCancion;

    /**
     * Duración de la canción en segundos.
     */
    @Column(name = "duracion_segundos", nullable = false)
    private Integer duracionSegundos;

    /**
     * URL de la imagen de portada almacenada en Cloudinary.
     */
    @Column(name = "url_portada", columnDefinition = "TEXT")
    private String urlPortada;

    /**
     * URL del archivo de audio almacenado en Cloudinary.
     */
    @Column(name = "url_audio", columnDefinition = "TEXT")
    private String urlAudio;

    /**
     * Contador de reproducciones acumuladas de la canción.
     */
    @Column(name = "reproducciones", nullable = false)
    @Builder.Default
    private Long reproducciones = 0L;

    /**
     * Fecha y hora de publicación de la canción.
     */
    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    /**
     * Descripción de la canción. Máximo 1000 caracteres.
     */
    @Column(name = "descripcion", length = 1000, columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Relación con álbumes mediante tabla intermedia.
     * Una canción puede estar incluida en múltiples álbumes.
     */
    @OneToMany(mappedBy = "cancion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlbumCancion> albumCanciones = new ArrayList<>();

    /**
     * Establece valores por defecto al momento de persistencia si no están definidos.
     */
    @PrePersist
    protected void onCreate() {
        if (fechaPublicacion == null) {
            fechaPublicacion = LocalDateTime.now();
        }
        if (reproducciones == null) {
            reproducciones = 0L;
        }
    }

    /**
     * Incrementa el contador de reproducciones en una unidad.
     */
    public void incrementarReproducciones() {
        this.reproducciones++;
    }

    /**
     * Verifica si la canción es de acceso gratuito.
     *
     * @return true si el precio es 0.00
     */
    public boolean esGratuita() {
        return this.precioCancion != null && this.precioCancion == 0.0;
    }

    /**
     * Verifica si la canción pertenece a un artista específico.
     *
     * @param idArtista identificador del artista
     * @return true si la canción pertenece al artista indicado
     */
    public boolean perteneceArtista(Long idArtista) {
        return this.idArtista != null && this.idArtista.equals(idArtista);
    }
}