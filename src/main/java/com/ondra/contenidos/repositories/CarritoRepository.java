package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestión de carritos de compra.
 *
 * <p>Proporciona operaciones para crear, consultar y eliminar carritos
 * asociados a usuarios, con soporte para carga eager de items.</p>
 */
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    /**
     * Busca el carrito de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return Optional con el carrito si existe
     */
    Optional<Carrito> findByIdUsuario(Long idUsuario);

    /**
     * Verifica si existe un carrito para un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return true si existe el carrito
     */
    boolean existsByIdUsuario(Long idUsuario);

    /**
     * Busca el carrito de un usuario con carga eager de items.
     *
     * @param idUsuario identificador del usuario
     * @return Optional con el carrito e items si existe
     */
    @Query("SELECT c FROM Carrito c LEFT JOIN FETCH c.items WHERE c.idUsuario = :idUsuario")
    Optional<Carrito> findByIdUsuarioWithItems(@Param("idUsuario") Long idUsuario);

    /**
     * Elimina el carrito de un usuario.
     *
     * @param idUsuario identificador del usuario
     */
    void deleteByIdUsuario(Long idUsuario);
}