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
 * Repositorio para operaciones sobre la tabla intermedia AlbumCancion.
 */
@Repository
public interface AlbumCancionRepository extends JpaRepository<AlbumCancion, Long> {

    // ========== BÚSQUEDA POR ÁLBUM ==========

    /**
     * Busca todas las canciones de un álbum ordenadas por número de pista.
     */
    List<AlbumCancion> findByAlbumIdAlbumOrderByNumeroPistaAsc(Long idAlbum);

    /**
     * Cuenta el número de canciones en un álbum.
     */
    long countByAlbumIdAlbum(Long idAlbum);

    // ========== BÚSQUEDA POR CANCIÓN ==========

    /**
     * Obtiene todos los álbumes que contienen una canción específica.
     */
    List<AlbumCancion> findByCancionIdCancionOrderByNumeroPistaAsc(Long idCancion);

    /**
     * Cuenta en cuántos álbumes aparece una canción.
     */
    long countByCancionIdCancion(Long idCancion);

    // ========== VALIDACIONES ==========

    /**
     * Verifica si una canción ya está en un álbum específico.
     */
    boolean existsByAlbumIdAlbumAndCancionIdCancion(Long idAlbum, Long idCancion);

    /**
     * Verifica si un número de pista ya está ocupado en un álbum.
     */
    boolean existsByAlbumIdAlbumAndNumeroPista(Long idAlbum, Integer numeroPista);

    /**
     * Busca una relación específica entre álbum y canción.
     */
    Optional<AlbumCancion> findByAlbumIdAlbumAndCancionIdCancion(Long idAlbum, Long idCancion);

    // ========== ELIMINACIÓN ==========

    /**
     * Elimina todas las canciones de un álbum.
     */
    @Modifying
    void deleteByAlbumIdAlbum(Long idAlbum);

    /**
     * Elimina una canción de todos los álbumes.
     * Usado cuando se elimina una canción del sistema.
     */
    @Modifying
    void deleteByCancionIdCancion(Long idCancion);

    /**
     * Elimina una canción específica de un álbum específico.
     */
    @Modifying
    @Query("DELETE FROM AlbumCancion ac WHERE ac.album.idAlbum = :idAlbum AND ac.cancion.idCancion = :idCancion")
    void eliminarCancionDeAlbum(@Param("idAlbum") Long idAlbum, @Param("idCancion") Long idCancion);
}