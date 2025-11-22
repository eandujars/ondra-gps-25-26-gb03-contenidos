package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.AlbumCancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones sobre la relación álbum-canción.
 *
 * <p>Gestiona la tabla intermedia que vincula canciones con álbumes,
 * incluyendo validaciones de duplicados y números de pista.</p>
 */
@Repository
public interface AlbumCancionRepository extends JpaRepository<AlbumCancion, Long> {

    /**
     * Busca todas las canciones de un álbum ordenadas por número de pista.
     *
     * @param idAlbum identificador del álbum
     * @return lista de relaciones ordenadas
     */
    List<AlbumCancion> findByAlbumIdAlbumOrderByNumeroPistaAsc(Long idAlbum);

    /**
     * Cuenta el número de canciones en un álbum.
     *
     * @param idAlbum identificador del álbum
     * @return cantidad de canciones
     */
    long countByAlbumIdAlbum(Long idAlbum);

    /**
     * Obtiene todos los álbumes que contienen una canción específica.
     *
     * @param idCancion identificador de la canción
     * @return lista de relaciones ordenadas por número de pista
     */
    List<AlbumCancion> findByCancionIdCancionOrderByNumeroPistaAsc(Long idCancion);

    /**
     * Cuenta en cuántos álbumes aparece una canción.
     *
     * @param idCancion identificador de la canción
     * @return cantidad de álbumes
     */
    long countByCancionIdCancion(Long idCancion);

    /**
     * Verifica si una canción ya está en un álbum específico.
     *
     * @param idAlbum identificador del álbum
     * @param idCancion identificador de la canción
     * @return true si la relación existe
     */
    boolean existsByAlbumIdAlbumAndCancionIdCancion(Long idAlbum, Long idCancion);

    /**
     * Verifica si un número de pista ya está ocupado en un álbum.
     *
     * @param idAlbum identificador del álbum
     * @param numeroPista número de pista a verificar
     * @return true si el número de pista ya existe
     */
    boolean existsByAlbumIdAlbumAndNumeroPista(Long idAlbum, Integer numeroPista);

    /**
     * Busca una relación específica entre álbum y canción.
     *
     * @param idAlbum identificador del álbum
     * @param idCancion identificador de la canción
     * @return relación encontrada
     */
    Optional<AlbumCancion> findByAlbumIdAlbumAndCancionIdCancion(Long idAlbum, Long idCancion);

    /**
     * Elimina todas las canciones de un álbum.
     *
     * @param idAlbum identificador del álbum
     */
    @Modifying
    void deleteByAlbumIdAlbum(Long idAlbum);

    /**
     * Elimina una canción de todos los álbumes.
     *
     * @param idCancion identificador de la canción
     */
    @Modifying
    void deleteByCancionIdCancion(Long idCancion);

    /**
     * Elimina una canción específica de un álbum específico.
     *
     * @param idAlbum identificador del álbum
     * @param idCancion identificador de la canción
     */
    @Modifying
    @Query("DELETE FROM AlbumCancion ac WHERE ac.album.idAlbum = :idAlbum AND ac.cancion.idCancion = :idCancion")
    void eliminarCancionDeAlbum(@Param("idAlbum") Long idAlbum, @Param("idCancion") Long idCancion);
}