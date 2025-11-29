package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.Cancion;
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
 * Repositorio para operaciones de base de datos sobre canciones.
 *
 * <p>Proporciona métodos de consulta por artista, género, texto y precio,
 * así como operaciones de estadísticas y actualización de reproducciones.</p>
 */
@Repository
public interface CancionRepository extends JpaRepository<Cancion, Long> {

    /**
     * Busca todas las canciones de un artista ordenadas por fecha descendente.
     *
     * @param idArtista identificador del artista
     * @return lista de canciones ordenadas
     */
    List<Cancion> findByIdArtistaOrderByFechaPublicacionDesc(Long idArtista);

    /**
     * Busca canciones de un artista con paginación.
     *
     * @param idArtista identificador del artista
     * @param pageable configuración de paginación
     * @return página de canciones
     */
    Page<Cancion> findByIdArtista(Long idArtista, Pageable pageable);

    /**
     * Cuenta el número de canciones de un artista.
     *
     * @param idArtista identificador del artista
     * @return cantidad de canciones
     */
    long countByIdArtista(Long idArtista);

    /**
     * Busca canciones por género con paginación.
     *
     * @param genero género musical
     * @param pageable configuración de paginación
     * @return página de canciones del género
     */
    Page<Cancion> findByGenero(GeneroMusical genero, Pageable pageable);

    /**
     * Busca canciones por género ordenadas por fecha descendente.
     *
     * @param genero género musical
     * @return lista de canciones ordenadas
     */
    List<Cancion> findByGeneroOrderByFechaPublicacionDesc(GeneroMusical genero);

    /**
     * Busca canciones por término en título o descripción.
     *
     * @param busqueda término de búsqueda
     * @param pageable configuración de paginación
     * @return página de canciones que coinciden con la búsqueda
     */
    @Query("SELECT c FROM Cancion c WHERE " +
            "LOWER(c.tituloCancion) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Cancion> buscarPorTexto(@Param("busqueda") String busqueda, Pageable pageable);

    /**
     * Búsqueda avanzada con múltiples filtros opcionales.
     *
     * @param idArtista identificador del artista
     * @param genero género musical
     * @param busqueda término de búsqueda
     * @param pageable configuración de paginación
     * @return página de canciones que coinciden con los filtros
     */
    @Query("SELECT c FROM Cancion c WHERE " +
            "(:idArtista IS NULL OR c.idArtista = :idArtista) AND " +
            "(:genero IS NULL OR c.genero = :genero) AND " +
            "(:busqueda IS NULL OR " +
            "LOWER(c.tituloCancion) LIKE LOWER(CONCAT('%', CAST(:busqueda AS string), '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', CAST(:busqueda AS string), '%')))")
    Page<Cancion> buscarConFiltros(
            @Param("idArtista") Long idArtista,
            @Param("genero") GeneroMusical genero,
            @Param("busqueda") String busqueda,
            Pageable pageable
    );

    /**
     * Busca canciones con un precio específico.
     *
     * @param precio precio exacto
     * @param pageable configuración de paginación
     * @return página de canciones con el precio indicado
     */
    Page<Cancion> findByPrecioCancion(Double precio, Pageable pageable);

    /**
     * Busca canciones en un rango de precio.
     *
     * @param min precio mínimo
     * @param max precio máximo
     * @param pageable configuración de paginación
     * @return página de canciones dentro del rango
     */
    Page<Cancion> findByPrecioCancionBetween(Double min, Double max, Pageable pageable);

    /**
     * Busca canciones gratuitas.
     *
     * @param pageable configuración de paginación
     * @return página de canciones con precio 0.00
     */
    @Query("SELECT c FROM Cancion c WHERE c.precioCancion = 0.0")
    Page<Cancion> findCancionesGratuitas(Pageable pageable);

    /**
     * Calcula el total de reproducciones de todas las canciones de un artista.
     *
     * @param idArtista identificador del artista
     * @return suma de reproducciones
     */
    @Query("SELECT COALESCE(SUM(c.reproducciones), 0) FROM Cancion c WHERE c.idArtista = :idArtista")
    Long getTotalReproduccionesByArtista(@Param("idArtista") Long idArtista);

    /**
     * Obtiene la suma total de reproducciones de todas las canciones.
     *
     * @return total de reproducciones en la plataforma
     */
    @Query("SELECT COALESCE(SUM(c.reproducciones), 0) FROM Cancion c")
    Long getTotalReproducciones();

    /**
     * Obtiene las canciones más reproducidas.
     *
     * @param pageable configuración de paginación
     * @return página de canciones ordenadas por reproducciones descendente
     */
    Page<Cancion> findAllByOrderByReproduccionesDesc(Pageable pageable);

    /**
     * Verifica si existe una canción de un artista específico.
     *
     * @param idCancion identificador de la canción
     * @param idArtista identificador del artista
     * @return true si la canción existe y pertenece al artista
     */
    boolean existsByIdCancionAndIdArtista(Long idCancion, Long idArtista);

    /**
     * Verifica si existe una canción por identificador.
     *
     * @param idCancion identificador de la canción
     * @return true si existe la canción
     */
    @Override
    boolean existsById(Long idCancion);

    /**
     * Incrementa el contador de reproducciones de una canción.
     *
     * @param idCancion identificador de la canción
     */
    @Modifying
    @Query("UPDATE Cancion c SET c.reproducciones = c.reproducciones + 1 WHERE c.idCancion = :idCancion")
    void incrementarReproducciones(@Param("idCancion") Long idCancion);

    /**
     * Elimina todas las canciones de un artista.
     *
     * @param idArtista identificador del artista
     */
    @Modifying
    void deleteByIdArtista(Long idArtista);

    /**
     * Búsqueda avanzada con múltiples filtros opcionales incluyendo rango de precio.
     *
     * @param idArtista identificador del artista
     * @param genero género musical
     * @param busqueda término de búsqueda
     * @param minPrecio precio mínimo
     * @param maxPrecio precio máximo
     * @param pageable configuración de paginación
     * @return página de canciones que coinciden con los filtros
     */
    @Query("SELECT c FROM Cancion c WHERE " +
            "(:idArtista IS NULL OR c.idArtista = :idArtista) AND " +
            "(:genero IS NULL OR c.genero = :genero) AND " +
            "(:busqueda IS NULL OR " +
            "LOWER(c.tituloCancion) LIKE LOWER(CONCAT('%', CAST(:busqueda AS string), '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', CAST(:busqueda AS string), '%'))) AND " +
            "(:minPrecio IS NULL OR c.precioCancion >= :minPrecio) AND " +
            "(:maxPrecio IS NULL OR c.precioCancion <= :maxPrecio)")
    Page<Cancion> buscarConFiltrosYPrecio(
            @Param("idArtista") Long idArtista,
            @Param("genero") GeneroMusical genero,
            @Param("busqueda") String busqueda,
            @Param("minPrecio") Double minPrecio,
            @Param("maxPrecio") Double maxPrecio,
            Pageable pageable
    );
}