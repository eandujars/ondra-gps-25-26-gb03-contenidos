package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.Favorito;
import com.ondra.contenidos.models.dao.Favorito.TipoContenido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    /**
     * Buscar todos los favoritos de un usuario con paginación
     */
    Page<Favorito> findByIdUsuario(Long idUsuario, Pageable pageable);

    /**
     * Buscar todos los favoritos de un usuario
     */
    List<Favorito> findByIdUsuario(Long idUsuario);

    /**
     * Buscar favoritos de un usuario por tipo de contenido
     */
    Page<Favorito> findByIdUsuarioAndTipoContenido(Long idUsuario, TipoContenido tipoContenido, Pageable pageable);

    /**
     * Buscar favoritos de canciones de un usuario
     */
    @Query("SELECT f FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.cancion IS NOT NULL")
    Page<Favorito> findCancionesFavoritasByUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);

    /**
     * Buscar favoritos de álbumes de un usuario
     */
    @Query("SELECT f FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.album IS NOT NULL")
    Page<Favorito> findAlbumesFavoritosByUsuario(@Param("idUsuario") Long idUsuario, Pageable pageable);

    /**
     * Verificar si una canción está en favoritos
     */
    @Query("SELECT COUNT(f) > 0 FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.cancion.idCancion = :idCancion")
    boolean existsByUsuarioAndCancion(@Param("idUsuario") Long idUsuario, @Param("idCancion") Long idCancion);

    /**
     * Verificar si un álbum está en favoritos
     */
    @Query("SELECT COUNT(f) > 0 FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.album.idAlbum = :idAlbum")
    boolean existsByUsuarioAndAlbum(@Param("idUsuario") Long idUsuario, @Param("idAlbum") Long idAlbum);

    /**
     * Encontrar favorito específico de canción
     */
    @Query("SELECT f FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.cancion.idCancion = :idCancion")
    Optional<Favorito> findByUsuarioAndCancion(@Param("idUsuario") Long idUsuario, @Param("idCancion") Long idCancion);

    /**
     * Encontrar favorito específico de álbum
     */
    @Query("SELECT f FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.album.idAlbum = :idAlbum")
    Optional<Favorito> findByUsuarioAndAlbum(@Param("idUsuario") Long idUsuario, @Param("idAlbum") Long idAlbum);

    /**
     * Contar favoritos de un usuario
     */
    long countByIdUsuario(Long idUsuario);

    /**
     * Contar favoritos de canciones de un usuario
     */
    @Query("SELECT COUNT(f) FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.cancion IS NOT NULL")
    long countCancionesFavoritasByUsuario(@Param("idUsuario") Long idUsuario);

    /**
     * Contar favoritos de álbumes de un usuario
     */
    @Query("SELECT COUNT(f) FROM Favorito f WHERE f.idUsuario = :idUsuario AND f.album IS NOT NULL")
    long countAlbumesFavoritosByUsuario(@Param("idUsuario") Long idUsuario);

    /**
     * Eliminar todos los favoritos de un usuario
     */
    void deleteByIdUsuario(Long idUsuario);
}