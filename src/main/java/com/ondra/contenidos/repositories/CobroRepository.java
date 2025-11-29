package com.ondra.contenidos.repositories;

import com.ondra.contenidos.models.dao.Cobro;
import com.ondra.contenidos.models.enums.EstadoCobro;
import com.ondra.contenidos.models.enums.TipoCobro;
import com.ondra.contenidos.models.enums.TipoContenido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CobroRepository extends JpaRepository<Cobro, Long>, JpaSpecificationExecutor<Cobro> {

    /**
     * Busca cobros por artista y estado.
     */
    List<Cobro> findByIdArtistaAndEstado(Long idArtista, EstadoCobro estado);

    /**
     * Encuentra el último cobro por reproducción de una canción.
     */
    @Query("SELECT c FROM Cobro c WHERE c.idCancion = :idCancion " +
            "AND c.tipoCobro = 'REPRODUCCION' " +
            "ORDER BY c.reproduccionesAcumuladas DESC LIMIT 1")
    Optional<Cobro> findUltimoCobroPorReproduccionCancion(@Param("idCancion") Long idCancion);

    /**
     * Calcula el total de ingresos de un artista (todos los cobros).
     */
    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM Cobro c WHERE c.idArtista = :idArtista")
    BigDecimal totalIngresosByArtista(@Param("idArtista") Long idArtista);

    /**
     * Calcula el total pendiente de pago de un artista.
     */
    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM Cobro c " +
            "WHERE c.idArtista = :idArtista AND c.estado = 'PENDIENTE'")
    BigDecimal totalPendienteByArtista(@Param("idArtista") Long idArtista);

    /**
     * Lista todos los cobros de un artista.
     */
    List<Cobro> findByIdArtistaOrderByFechaCobroDesc(Long idArtista);

    /**
     * Obtiene cobros de un mes y año específicos.
     */
    @Query("SELECT c FROM Cobro c WHERE " +
            "c.idArtista = :idArtista AND " +
            "YEAR(c.fechaCobro) = :anio AND " +
            "MONTH(c.fechaCobro) = :mes")
    Page<Cobro> findByArtistaAndMesAnio(
            @Param("idArtista") Long idArtista,
            @Param("mes") Integer mes,
            @Param("anio") Integer anio,
            Pageable pageable
    );

    /**
     * Resumen de cobros por mes y año.
     */
    @Query("SELECT " +
            "MONTH(c.fechaCobro) as mes, " +
            "YEAR(c.fechaCobro) as anio, " +
            "SUM(c.monto) as total, " +
            "COUNT(c) as cantidad, " +
            "SUM(CASE WHEN c.estado = 'PENDIENTE' THEN c.monto ELSE 0 END) as pendiente, " +
            "SUM(CASE WHEN c.estado = 'PAGADO' THEN c.monto ELSE 0 END) as pagado " +
            "FROM Cobro c " +
            "WHERE c.idArtista = :idArtista " +
            "GROUP BY YEAR(c.fechaCobro), MONTH(c.fechaCobro) " +
            "ORDER BY YEAR(c.fechaCobro) DESC, MONTH(c.fechaCobro) DESC")
    List<Object[]> obtenerResumenPorMes(@Param("idArtista") Long idArtista);
}