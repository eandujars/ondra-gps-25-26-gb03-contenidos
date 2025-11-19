package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.Album;
import com.ondra.contenidos.models.enums.GeneroMusical;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones de base de datos sobre Album.
 */
@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    // ========== BÚSQUEDA POR ARTISTA ==========

    /**
     * Busca todos los álbumes de un artista ordenados por fecha de publicación descendente.
     */
    List<Album> findByIdArtistaOrderByFechaPublicacionDesc(Long idArtista);

    /**
     * Busca álbumes de un artista con paginación.
     */
    Page<Album> findByIdArtista(Long idArtista, Pageable pageable);

    /**
     * Cuenta el número de álbumes de un artista.
     */
    long countByIdArtista(Long idArtista);

    // ========== BÚSQUEDA POR GÉNERO ==========

    /**
     * Busca álbumes por género con paginación.
     */
    Page<Album> findByGenero(GeneroMusical genero, Pageable pageable);

    /**
     * Busca álbumes por género ordenados por fecha.
     */
    List<Album> findByGeneroOrderByFechaPublicacionDesc(GeneroMusical genero);

    // ========== BÚSQUEDA POR TEXTO ==========

    /**
     * Busca álbumes por término en título o descripción.
     * Búsqueda insensible a mayúsculas/minúsculas.
     */
    @Query("SELECT a FROM Album a WHERE " +
            "LOWER(a.tituloAlbum) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Album> buscarPorTexto(@Param("busqueda") String busqueda, Pageable pageable);

    // ========== BÚSQUEDA COMBINADA ==========

    /**
     * Búsqueda avanzada con múltiples filtros opcionales.
     * Cualquier parámetro puede ser null para omitir ese filtro.
     */
    @Query("SELECT a FROM Album a WHERE " +
            "(:idArtista IS NULL OR a.idArtista = :idArtista) AND " +
            "(:genero IS NULL OR a.genero = :genero) AND " +
            "(:busqueda IS NULL OR " +
            "LOWER(a.tituloAlbum) LIKE LOWER(CONCAT('%', CAST(:busqueda AS string), '%')) OR " +
            "LOWER(a.descripcion) LIKE LOWER(CONCAT('%', CAST(:busqueda AS string), '%')))")
    Page<Album> buscarConFiltros(
            @Param("idArtista") Long idArtista,
            @Param("genero") GeneroMusical genero,
            @Param("busqueda") String busqueda,
            Pageable pageable
    );

    // ========== BÚSQUEDA POR PRECIO ==========

    /**
     * Busca álbumes con un precio específico.
     */
    Page<Album> findByPrecioAlbum(Double precio, Pageable pageable);

    /**
     * Busca álbumes en un rango de precio.
     */
    Page<Album> findByPrecioAlbumBetween(Double min, Double max, Pageable pageable);

    /**
     * Busca álbumes gratuitos (precio = 0.00).
     */
    @Query("SELECT a FROM Album a WHERE a.precioAlbum = 0.0")
    Page<Album> findAlbumesGratuitos(Pageable pageable);

    // ========== VALIDACIONES ==========

    /**
     * Verifica si existe un álbum con un ID y que pertenezca a un artista específico.
     */
    boolean existsByIdAlbumAndIdArtista(Long idAlbum, Long idArtista);

    // ========== ELIMINACIÓN MASIVA ==========

    /**
     * Elimina todos los álbumes de un artista.
     * Usado cuando se elimina un artista del sistema.
     */
    @Modifying
    void deleteByIdArtista(Long idArtista);
}