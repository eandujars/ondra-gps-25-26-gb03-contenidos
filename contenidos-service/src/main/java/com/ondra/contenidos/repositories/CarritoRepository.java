package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    /**
     * Buscar carrito por ID de usuario
     */
    Optional<Carrito> findByIdUsuario(Long idUsuario);

    /**
     * Verificar si existe un carrito para un usuario
     */
    boolean existsByIdUsuario(Long idUsuario);

    /**
     * Buscar carrito con items
     */
    @Query("SELECT c FROM Carrito c LEFT JOIN FETCH c.items WHERE c.idUsuario = :idUsuario")
    Optional<Carrito> findByIdUsuarioWithItems(@Param("idUsuario") Long idUsuario);

    /**
     * Eliminar carrito por ID de usuario
     */
    void deleteByIdUsuario(Long idUsuario);
}