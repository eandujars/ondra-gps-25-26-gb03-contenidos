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
 * Repositorio para operaciones de base de datos sobre álbumes.
 *
 * <p>Proporciona métodos de consulta por artista, género, texto y precio,
 * así como validaciones y eliminación en cascada.</p>
 */
@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    /**
     * Busca todos los álbumes de un artista ordenados por fecha descendente.
     *
     * @param idArtista identificador del artista
     * @return lista de álbumes ordenados
     */
    List<Album> findByIdArtistaOrderByFechaPublicacionDesc(Long idArtista);

    /**
     * Busca álbumes de un artista con paginación.
     *
     * @param idArtista identificador del artista
     * @param pageable configuración de paginación
     * @return página de álbumes
     */
    Page<Album> findByIdArtista(Long idArtista, Pageable pageable);

    /**
     * Cuenta el número de álbumes de un artista.
     *
     * @param idArtista identificador del artista
     * @return cantidad de álbumes
     */
    long countByIdArtista(Long idArtista);

    /**
     * Busca álbumes por género con paginación.
     *
     * @param genero género musical
     * @param pageable configuración de paginación
     * @return página de álbumes del género
     */
    Page<Album> findByGenero(GeneroMusical genero, Pageable pageable);

    /**
     * Busca álbumes por género ordenados por fecha descendente.
     *
     * @param genero género musical
     * @return lista de álbumes ordenados
     */
    List<Album> findByGeneroOrderByFechaPublicacionDesc(GeneroMusical genero);

    /**
     * Busca álbumes por término en título o descripción.
     *
     * @param busqueda término de búsqueda
     * @param pageable configuración de paginación
     * @return página de álbumes que coinciden con la búsqueda
     */
    @Query("SELECT a FROM Album a WHERE " +
            "LOWER(a.tituloAlbum) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Album> buscarPorTexto(@Param("busqueda") String busqueda, Pageable pageable);

    /**
     * Búsqueda avanzada con múltiples filtros opcionales.
     *
     * @param idArtista identificador del artista
     * @param genero género musical
     * @param busqueda término de búsqueda
     * @param pageable configuración de paginación
     * @return página de álbumes que coinciden con los filtros
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

    /**
     * Busca álbumes con un precio específico.
     *
     * @param precio precio exacto
     * @param pageable configuración de paginación
     * @return página de álbumes con el precio indicado
     */
    Page<Album> findByPrecioAlbum(Double precio, Pageable pageable);

    /**
     * Busca álbumes en un rango de precio.
     *
     * @param min precio mínimo
     * @param max precio máximo
     * @param pageable configuración de paginación
     * @return página de álbumes dentro del rango
     */
    Page<Album> findByPrecioAlbumBetween(Double min, Double max, Pageable pageable);

    /**
     * Busca álbumes gratuitos.
     *
     * @param pageable configuración de paginación
     * @return página de álbumes con precio 0.00
     */
    @Query("SELECT a FROM Album a WHERE a.precioAlbum = 0.0")
    Page<Album> findAlbumesGratuitos(Pageable pageable);

    /**
     * Verifica si existe un álbum de un artista específico.
     *
     * @param idAlbum identificador del álbum
     * @param idArtista identificador del artista
     * @return true si el álbum existe y pertenece al artista
     */
    boolean existsByIdAlbumAndIdArtista(Long idAlbum, Long idArtista);

    /**
     * Elimina todos los álbumes de un artista.
     *
     * @param idArtista identificador del artista
     */
    @Modifying
    void deleteByIdArtista(Long idArtista);
}