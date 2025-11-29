package com.ondra.contenidos.specifications;

import com.ondra.contenidos.models.dao.Cobro;
import com.ondra.contenidos.models.enums.EstadoCobro;
import com.ondra.contenidos.models.enums.TipoCobro;
import com.ondra.contenidos.models.enums.TipoContenido;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Especificaciones JPA para consultas dinámicas sobre la entidad Cobro.
 *
 * <p>Proporciona métodos estáticos para construir especificaciones reutilizables
 * que permiten filtrar cobros según múltiples criterios.</p>
 */
public class CobroSpecifications {

    /**
     * Construye una especificación con filtros completos para consultas de cobros.
     *
     * @param idArtista identificador del artista
     * @param estado estado del cobro
     * @param tipoCobro tipo de cobro realizado
     * @param tipoContenido tipo de contenido asociado
     * @param fechaDesde fecha inicial del rango de búsqueda
     * @param fechaHasta fecha final del rango de búsqueda
     * @param montoMinimo importe mínimo del cobro
     * @param montoMaximo importe máximo del cobro
     * @return especificación que combina todos los filtros aplicables
     */
    public static Specification<Cobro> conFiltros(
            Long idArtista,
            EstadoCobro estado,
            TipoCobro tipoCobro,
            TipoContenido tipoContenido,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            BigDecimal montoMinimo,
            BigDecimal montoMaximo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (idArtista != null) {
                predicates.add(criteriaBuilder.equal(root.get("idArtista"), idArtista));
            }

            if (estado != null) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), estado));
            }

            if (tipoCobro != null) {
                predicates.add(criteriaBuilder.equal(root.get("tipoCobro"), tipoCobro));
            }

            if (tipoContenido != null) {
                predicates.add(criteriaBuilder.equal(root.get("tipoContenido"), tipoContenido));
            }

            if (fechaDesde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fechaCobro"), fechaDesde));
            }

            if (fechaHasta != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fechaCobro"), fechaHasta));
            }

            if (montoMinimo != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("monto"), montoMinimo));
            }

            if (montoMaximo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("monto"), montoMaximo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Construye una especificación optimizada para consultas de estadísticas.
     *
     * <p>Excluye filtros por montos para mejorar el rendimiento en agregaciones.</p>
     *
     * @param idArtista identificador del artista
     * @param estado estado del cobro
     * @param tipoCobro tipo de cobro realizado
     * @param tipoContenido tipo de contenido asociado
     * @param fechaDesde fecha inicial del rango de búsqueda
     * @param fechaHasta fecha final del rango de búsqueda
     * @return especificación para consultas estadísticas
     */
    public static Specification<Cobro> paraEstadisticas(
            Long idArtista,
            EstadoCobro estado,
            TipoCobro tipoCobro,
            TipoContenido tipoContenido,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (idArtista != null) {
                predicates.add(criteriaBuilder.equal(root.get("idArtista"), idArtista));
            }

            if (estado != null) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), estado));
            }

            if (tipoCobro != null) {
                predicates.add(criteriaBuilder.equal(root.get("tipoCobro"), tipoCobro));
            }

            if (tipoContenido != null) {
                predicates.add(criteriaBuilder.equal(root.get("tipoContenido"), tipoContenido));
            }

            if (fechaDesde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fechaCobro"), fechaDesde));
            }

            if (fechaHasta != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fechaCobro"), fechaHasta));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}