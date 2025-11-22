package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.Comentario;
import com.ondra.contenidos.models.enums.TipoContenido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestión de comentarios en contenidos musicales.
 *
 * <p>Proporciona operaciones de consulta y eliminación de comentarios sobre
 * canciones y álbumes, con soporte para paginación y estadísticas.</p>
 */
@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    /**
     * Obtiene los comentarios de un usuario con paginación.
     *
     * @param idUsuario identificador del usuario
     * @param pageable configuración de paginación
     * @return página de comentarios del usuario
     */
    Page<Comentario> findByIdUsuario(Long idUsuario, Pageable pageable);

    /**
     * Obtiene todos los comentarios de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return lista completa de comentarios
     */
    List<Comentario> findByIdUsuario(Long idUsuario);

    /**
     * Obtiene los comentarios de una canción ordenados por fecha descendente.
     *
     * @param idCancion identificador de la canción
     * @param pageable configuración de paginación
     * @return página de comentarios de la canción
     */
    @Query("SELECT c FROM Comentario c WHERE c.cancion.idCancion = :idCancion ORDER BY c.fechaPublicacion DESC")
    Page<Comentario> findByCancion(@Param("idCancion") Long idCancion, Pageable pageable);

    /**
     * Obtiene los comentarios de un álbum ordenados por fecha descendente.
     *
     * @param idAlbum identificador del álbum
     * @param pageable configuración de paginación
     * @return página de comentarios del álbum
     */
    @Query("SELECT c FROM Comentario c WHERE c.album.idAlbum = :idAlbum ORDER BY c.fechaPublicacion DESC")
    Page<Comentario> findByAlbum(@Param("idAlbum") Long idAlbum, Pageable pageable);

    /**
     * Obtiene todos los comentarios de una canción sin paginación.
     *
     * @param idCancion identificador de la canción
     * @return lista completa de comentarios
     */
    @Query("SELECT c FROM Comentario c WHERE c.cancion.idCancion = :idCancion ORDER BY c.fechaPublicacion DESC")
    List<Comentario> findByCancionList(@Param("idCancion") Long idCancion);

    /**
     * Obtiene todos los comentarios de un álbum sin paginación.
     *
     * @param idAlbum identificador del álbum
     * @return lista completa de comentarios
     */
    @Query("SELECT c FROM Comentario c WHERE c.album.idAlbum = :idAlbum ORDER BY c.fechaPublicacion DESC")
    List<Comentario> findByAlbumList(@Param("idAlbum") Long idAlbum);

    /**
     * Obtiene comentarios de un usuario filtrados por tipo de contenido.
     *
     * @param idUsuario identificador del usuario
     * @param tipoContenido tipo de contenido a filtrar
     * @param pageable configuración de paginación
     * @return página de comentarios filtrados
     */
    Page<Comentario> findByIdUsuarioAndTipoContenido(Long idUsuario, TipoContenido tipoContenido, Pageable pageable);

    /**
     * Cuenta el total de comentarios de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return número de comentarios
     */
    long countByIdUsuario(Long idUsuario);

    /**
     * Cuenta el total de comentarios de una canción.
     *
     * @param idCancion identificador de la canción
     * @return número de comentarios
     */
    @Query("SELECT COUNT(c) FROM Comentario c WHERE c.cancion.idCancion = :idCancion")
    long countByCancion(@Param("idCancion") Long idCancion);

    /**
     * Cuenta el total de comentarios de un álbum.
     *
     * @param idAlbum identificador del álbum
     * @return número de comentarios
     */
    @Query("SELECT COUNT(c) FROM Comentario c WHERE c.album.idAlbum = :idAlbum")
    long countByAlbum(@Param("idAlbum") Long idAlbum);

    /**
     * Elimina todos los comentarios de un usuario.
     *
     * @param idUsuario identificador del usuario
     */
    void deleteByIdUsuario(Long idUsuario);

    /**
     * Elimina todos los comentarios de una canción.
     *
     * @param idCancion identificador de la canción
     */
    @Query("DELETE FROM Comentario c WHERE c.cancion.idCancion = :idCancion")
    void deleteByCancion(@Param("idCancion") Long idCancion);

    /**
     * Elimina todos los comentarios de un álbum.
     *
     * @param idAlbum identificador del álbum
     */
    @Query("DELETE FROM Comentario c WHERE c.album.idAlbum = :idAlbum")
    void deleteByAlbum(@Param("idAlbum") Long idAlbum);

    /**
     * Verifica si existe un comentario por identificador y usuario.
     *
     * @param idComentario identificador del comentario
     * @param idUsuario identificador del usuario
     * @return true si el usuario es propietario del comentario
     */
    boolean existsByIdComentarioAndIdUsuario(Long idComentario, Long idUsuario);
}