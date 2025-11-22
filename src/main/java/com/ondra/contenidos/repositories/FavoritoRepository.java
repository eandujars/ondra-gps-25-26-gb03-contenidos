package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.Favorito;
import com.ondra.contenidos.models.enums.TipoContenido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de contenidos favoritos de usuarios.
 *
 * <p>Proporciona operaciones para añadir, consultar y eliminar canciones
 * y álbumes marcados como favoritos por los usuarios.</p>
 */
@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    /**
     * Obtiene los favoritos de un usuario con paginación.
     *
     * @param idUsuario identificador del usuario
     * @param pageable configuración de paginación
     * @return página de favoritos del usuario
     */
    Page<Favorito> findByIdUsuario(Long idUsuario, Pageable pageable);

    /**
     * Obtiene todos los favoritos de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista completa de favoritos
     */
    List<Favorito> findByIdUsuario(Long idUsuario);

    /**
     * Obtiene favoritos de un usuario filtrados por tipo de contenido.
     *
     * @param idUsuario identificador del usuario
     * @param tipoContenido tipo de contenido a filtrar
     * @param pageable configuración de paginación
     * @return página de favoritos filtrados
     */
    Page<Favorito> findByIdUsuarioAndTipoContenido(Long idUsuario, TipoContenido tipoContenido, Pageable pageable);

    /**
     * Obtiene las canciones favoritas de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @param pageable configuración de paginación
     * @return página de canciones favoritas
     */
    @Query("SELECT f FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.cancion IS NOT NULL")
    Page<Favorito> findCancionesFavoritasByUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);

    /**
     * Obtiene los álbumes favoritos de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @param pageable configuración de paginación
     * @return página de álbumes favoritos
     */
    @Query("SELECT f FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.album IS NOT NULL")
    Page<Favorito> findAlbumesFavoritosByUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);

    /**
     * Verifica si una canción está marcada como favorita.
     *
     * @param idUsuario identificador del usuario
     * @param idCancion identificador de la canción
     * @return true si la canción está en favoritos
     */
    @Query("SELECT COUNT(f) > 0 FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.cancion.idCancion = :idCancion")
    boolean existsByUsuarioAndCancion(@Param("idUsuario") Long idUsuario, @Param("idCancion") Long idCancion);

    /**
     * Verifica si un álbum está marcado como favorito.
     *
     * @param idUsuario identificador del usuario
     * @param idAlbum identificador del álbum
     * @return true si el álbum está en favoritos
     */
    @Query("SELECT COUNT(f) > 0 FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.album.idAlbum = :idAlbum")
    boolean existsByUsuarioAndAlbum(@Param("idUsuario") Long idUsuario, @Param("idAlbum") Long idAlbum);

    /**
     * Busca el favorito específico de una canción.
     *
     * @param idUsuario identificador del usuario
     * @param idCancion identificador de la canción
     * @return Optional con el favorito si existe
     */
    @Query("SELECT f FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.cancion.idCancion = :idCancion")
    Optional<Favorito> findByUsuarioAndCancion(@Param("idUsuario") Long idUsuario, @Param("idCancion") Long idCancion);

    /**
     * Busca el favorito específico de un álbum.
     *
     * @param idUsuario identificador del usuario
     * @param idAlbum identificador del álbum
     * @return Optional con el favorito si existe
     */
    @Query("SELECT f FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.album.idAlbum = :idAlbum")
    Optional<Favorito> findByUsuarioAndAlbum(@Param("idUsuario") Long idUsuario, @Param("idAlbum") Long idAlbum);

    /**
     * Cuenta el total de favoritos de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return número de favoritos
     */
    long countByIdUsuario(Long idUsuario);

    /**
     * Cuenta las canciones favoritas de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return número de canciones favoritas
     */
    @Query("SELECT COUNT(f) FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.cancion IS NOT NULL")
    long countCancionesFavoritasByUsuario(@Param("idUsuario") Long idUsuario);

    /**
     * Cuenta los álbumes favoritos de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return número de álbumes favoritos
     */
    @Query("SELECT COUNT(f) FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.album IS NOT NULL")
    long countAlbumesFavoritosByUsuario(@Param("idUsuario") Long idUsuario);

    /**
     * Elimina todos los favoritos de un usuario.
     *
     * @param idUsuario identificador del usuario
     */
    void deleteByIdUsuario(Long idUsuario);
}