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
 * Repositorio para operaciones de base de datos sobre Cancion.
 */
@Repository
public interface CancionRepository extends JpaRepository<Cancion, Long> {

    // ========== BÚSQUEDA POR ARTISTA ==========

    /**
     * Busca todas las canciones de un artista ordenadas por fecha de publicación descendente.
     */
    List<Cancion> findByIdArtistaOrderByFechaPublicacionDesc(Long idArtista);

    /**
     * Busca canciones de un artista con paginación.
     */
    Page<Cancion> findByIdArtista(Long idArtista, Pageable pageable);

    /**
     * Cuenta el número de canciones de un artista.
     */
    long countByIdArtista(Long idArtista);

    // ========== BÚSQUEDA POR GÉNERO ==========

    /**
     * Busca canciones por género con paginación.
     */
    Page<Cancion> findByGenero(GeneroMusical genero, Pageable pageable);

    /**
     * Busca canciones por género ordenadas por fecha.
     */
    List<Cancion> findByGeneroOrderByFechaPublicacionDesc(GeneroMusical genero);

    // ========== BÚSQUEDA POR TEXTO ==========

    /**
     * Busca canciones por término en título o descripción.
     * Búsqueda insensible a mayúsculas/minúsculas.
     */
    @Query("SELECT c FROM Cancion c WHERE " +
            "LOWER(c.tituloCancion) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    Page<Cancion> buscarPorTexto(@Param("busqueda") String busqueda, Pageable pageable);

    // ========== BÚSQUEDA COMBINADA ==========

    /**
     * Búsqueda avanzada con múltiples filtros opcionales.
     * Cualquier parámetro puede ser null para omitir ese filtro.
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

    // ========== BÚSQUEDA POR PRECIO ==========

    /**
     * Busca canciones con un precio específico.
     */
    Page<Cancion> findByPrecioCancion(Double precio, Pageable pageable);

    /**
     * Busca canciones en un rango de precio.
     */
    Page<Cancion> findByPrecioCancionBetween(Double min, Double max, Pageable pageable);

    /**
     * Busca canciones gratuitas (precio = 0.00).
     */
    @Query("SELECT c FROM Cancion c WHERE c.precioCancion = 0.0")
    Page<Cancion> findCancionesGratuitas(Pageable pageable);

    // ========== ESTADÍSTICAS ==========

    /**
     * Calcula el total de reproducciones de todas las canciones de un artista.
     */
    @Query("SELECT COALESCE(SUM(c.reproducciones), 0) FROM Cancion c WHERE c.idArtista = :idArtista")
    Long getTotalReproduccionesByArtista(@Param("idArtista") Long idArtista);

    /**
     * Obtiene la suma total de reproducciones de todas las canciones.
     */
    @Query("SELECT COALESCE(SUM(c.reproducciones), 0) FROM Cancion c")
    Long getTotalReproducciones();

    /**
     * Obtiene las canciones más reproducidas.
     */
    Page<Cancion> findAllByOrderByReproduccionesDesc(Pageable pageable);

    // ========== VALIDACIONES ==========

    /**
     * Verifica si existe una canción con un ID y que pertenezca a un artista específico.
     */
    boolean existsByIdCancionAndIdArtista(Long idCancion, Long idArtista);

    /**
     * Verifica si existe una canción por ID.
     */
    @Override
    boolean existsById(Long idCancion);

    // ========== OPERACIONES DE ACTUALIZACIÓN ==========

    /**
     * Incrementa el contador de reproducciones de una canción.
     */
    @Modifying
    @Query("UPDATE Cancion c SET c.reproducciones = c.reproducciones + 1 WHERE c.idCancion = :idCancion")
    void incrementarReproducciones(@Param("idCancion") Long idCancion);

    // ========== ELIMINACIÓN MASIVA ==========

    /**
     * Elimina todas las canciones de un artista.
     * Usado cuando se elimina un artista del sistema.
     */
    @Modifying
    void deleteByIdArtista(Long idArtista);
}