package com.ondra.contenidos.services;

import com.ondra.contenidos.clients.UsuariosClient;
import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.models.dao.Cancion;
import com.ondra.contenidos.models.dao.Cobro;
import com.ondra.contenidos.models.enums.EstadoCobro;
import com.ondra.contenidos.models.enums.TipoCobro;
import com.ondra.contenidos.models.enums.TipoContenido;
import com.ondra.contenidos.repositories.AlbumRepository;
import com.ondra.contenidos.repositories.CancionRepository;
import com.ondra.contenidos.repositories.CobroRepository;
import com.ondra.contenidos.specifications.CobroSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Servicio para consulta y listado de cobros.
 *
 * <p>Proporciona operaciones de consulta avanzada con filtros, paginación,
 * ordenamiento y obtención de resúmenes agrupados por mes.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CobroListService {

    private final CobroRepository cobroRepository;
    private final CancionRepository cancionRepository;
    private final AlbumRepository albumRepository;
    private final UsuariosClient usuariosClient;

    /**
     * Lista cobros con filtros avanzados y paginación.
     *
     * <p>Soporta filtrado por artista, estado, tipo de cobro, tipo de contenido,
     * rango de fechas o mes/año específico, y rango de montos. Incluye estadísticas
     * calculadas sobre todos los cobros filtrados.</p>
     *
     * @param filtros objeto con criterios de filtrado, paginación y ordenamiento
     * @return página de cobros con estadísticas de montos totales, pendientes y pagados
     */
    @Transactional(readOnly = true)
    public CobrosPaginadosDTO listarCobrosConFiltros(FiltrosCobrosDTO filtros) {
        log.debug("📋 Listando cobros con filtros - Artista: {}", filtros.getIdArtista());

        int pagina = (filtros.getPagina() != null && filtros.getPagina() > 0)
                ? filtros.getPagina() - 1 : 0;
        int limite = (filtros.getLimite() != null && filtros.getLimite() > 0 && filtros.getLimite() <= 100)
                ? filtros.getLimite() : 20;

        Sort sort = configurarOrdenamiento(filtros.getOrdenarPor(), filtros.getDireccion());
        Pageable pageable = PageRequest.of(pagina, limite, sort);

        EstadoCobro estado = parseEstadoCobro(filtros.getEstado());
        TipoCobro tipoCobro = parseTipoCobro(filtros.getTipoCobro());
        TipoContenido tipoContenido = parseTipoContenido(filtros.getTipoContenido());

        log.debug("🔍 Filtros aplicados - Estado: {}, TipoCobro: {}, TipoContenido: {}, Mes: {}, Año: {}",
                estado, tipoCobro, tipoContenido, filtros.getMes(), filtros.getAnio());

        LocalDateTime fechaDesde = filtros.getFechaDesde();
        LocalDateTime fechaHasta = filtros.getFechaHasta();

        if (filtros.getMes() != null && filtros.getAnio() != null &&
                filtros.getMes() > 0 && filtros.getAnio() > 0) {
            try {
                YearMonth yearMonth = YearMonth.of(filtros.getAnio(), filtros.getMes());
                fechaDesde = yearMonth.atDay(1).atStartOfDay();
                fechaHasta = yearMonth.atEndOfMonth().atTime(23, 59, 59);
                log.debug("📅 Filtrando por mes/año: {}/{}", filtros.getMes(), filtros.getAnio());
            } catch (Exception e) {
                log.warn("⚠️ Valores inválidos para mes/año: {}/{} - Ignorando filtro",
                        filtros.getMes(), filtros.getAnio());
            }
        }

        Specification<Cobro> spec = CobroSpecifications.conFiltros(
                filtros.getIdArtista(),
                estado,
                tipoCobro,
                tipoContenido,
                fechaDesde,
                fechaHasta,
                filtros.getMontoMinimo(),
                filtros.getMontoMaximo()
        );

        Page<Cobro> paginaCobros = cobroRepository.findAll(spec, pageable);

        List<Cobro> todosLosCobros = cobroRepository.findAll(spec);

        BigDecimal totalMonto = todosLosCobros.stream()
                .map(Cobro::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montoPendiente = todosLosCobros.stream()
                .filter(c -> c.getEstado() == EstadoCobro.PENDIENTE)
                .map(Cobro::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, String> cacheNombreMetodos = new HashMap<>();

        BigDecimal montoPagado = todosLosCobros.stream()
                .filter(c -> c.getEstado() == EstadoCobro.PAGADO)
                .map(Cobro::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CobrosPaginadosDTO.builder()
                .cobros(paginaCobros.getContent().stream()
                        .map(cobro -> convertirADTO(cobro, cacheNombreMetodos))
                        .collect(Collectors.toList()))
                .paginaActual(paginaCobros.getNumber() + 1)
                .totalPaginas(paginaCobros.getTotalPages())
                .totalElementos(paginaCobros.getTotalElements())
                .elementosPorPagina(paginaCobros.getSize())
                .totalMonto(totalMonto)
                .montoPendiente(montoPendiente)
                .montoPagado(montoPagado)
                .build();
    }

    /**
     * Obtiene resumen de cobros agrupados por mes y año.
     *
     * <p>Incluye totales, cantidades y desglose por estado para cada mes.</p>
     *
     * @param idArtista identificador del artista
     * @return lista de resúmenes mensuales ordenados
     */
    @Transactional(readOnly = true)
    public List<ResumenCobrosDTO> obtenerResumenPorMes(Long idArtista) {
        log.debug("📊 Obteniendo resumen por mes - Artista: {}", idArtista);

        List<Object[]> resultados = cobroRepository.obtenerResumenPorMes(idArtista);

        return resultados.stream()
                .map(row -> ResumenCobrosDTO.builder()
                        .mes((Integer) row[0])
                        .anio((Integer) row[1])
                        .totalCobros((BigDecimal) row[2])
                        .cantidadCobros((Long) row[3])
                        .montoPendiente((BigDecimal) row[4])
                        .montoPagado((BigDecimal) row[5])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Lista cobros de un mes y año específicos con paginación.
     *
     * <p>Incluye estadísticas de montos totales, pendientes y pagados para el periodo consultado.</p>
     *
     * @param idArtista identificador del artista
     * @param mes mes a consultar (1-12)
     * @param anio año a consultar
     * @param pagina número de página (comienza en 1)
     * @param limite elementos por página (máximo 100)
     * @return página de cobros con estadísticas del periodo
     */
    @Transactional(readOnly = true)
    public CobrosPaginadosDTO listarCobrosPorMes(Long idArtista, Integer mes, Integer anio,
                                                 Integer pagina, Integer limite) {
        log.debug("📋 Listando cobros - Artista: {}, Mes: {}, Año: {}", idArtista, mes, anio);

        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite,
                Sort.by(Sort.Direction.DESC, "fechaCobro"));

        Page<Cobro> paginaCobros = cobroRepository.findByArtistaAndMesAnio(
                idArtista, mes, anio, pageable);

        BigDecimal totalMonto = paginaCobros.getContent().stream()
                .map(Cobro::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montoPendiente = paginaCobros.getContent().stream()
                .filter(c -> c.getEstado() == EstadoCobro.PENDIENTE)
                .map(Cobro::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montoPagado = paginaCobros.getContent().stream()
                .filter(c -> c.getEstado() == EstadoCobro.PAGADO)
                .map(Cobro::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, String> cacheNombreMetodos = new HashMap<>();

        return CobrosPaginadosDTO.builder()
                .cobros(paginaCobros.getContent().stream()
                        .map(cobro -> convertirADTO(cobro, cacheNombreMetodos))
                        .collect(Collectors.toList()))
                .paginaActual(paginaCobros.getNumber() + 1)
                .totalPaginas(paginaCobros.getTotalPages())
                .totalElementos(paginaCobros.getTotalElements())
                .elementosPorPagina(paginaCobros.getSize())
                .totalMonto(totalMonto)
                .montoPendiente(montoPendiente)
                .montoPagado(montoPagado)
                .build();
    }

    /**
     * Convierte una entidad Cobro a su representación DTO con información enriquecida.
     *
     * <p>Resuelve el título del contenido asociado y el nombre del método de cobro
     * utilizando una caché para optimizar consultas repetidas.</p>
     *
     * @param cobro entidad a convertir
     * @param cacheNombreMetodos caché de nombres de métodos de cobro por ID
     * @return DTO del cobro con información completa
     */
    private CobroDTO convertirADTO(Cobro cobro, Map<Long, String> cacheNombreMetodos) {
        String tituloContenido = null;

        if (cobro.getTipoContenido() == TipoContenido.CANCIÓN && cobro.getIdCancion() != null) {
            tituloContenido = cancionRepository.findById(cobro.getIdCancion())
                    .map(Cancion::getTituloCancion)
                    .orElse("Canción desconocida");
        } else if (cobro.getTipoContenido() == TipoContenido.ÁLBUM && cobro.getIdAlbum() != null) {
            tituloContenido = albumRepository.findById(cobro.getIdAlbum())
                    .map(album -> album.getTituloAlbum())
                    .orElse("Álbum desconocido");
        }

        Long idMetodoCobro = cobro.getIdMetodoCobro();
        String nombreMetodoCobro = null;
        if (idMetodoCobro != null) {
            nombreMetodoCobro = cacheNombreMetodos.computeIfAbsent(
                    idMetodoCobro,
                    this::resolverNombreMetodoCobro
            );
        }

        return CobroDTO.builder()
                .idCobro(cobro.getIdCobro())
                .idArtista(cobro.getIdArtista())
                .tipoCobro(cobro.getTipoCobro().name())
                .monto(cobro.getMonto())
                .fechaCobro(cobro.getFechaCobro())
                .tipoContenido(cobro.getTipoContenido() != null ? cobro.getTipoContenido().name() : null)
                .idCancion(cobro.getIdCancion())
                .idAlbum(cobro.getIdAlbum())
                .tituloContenido(tituloContenido)
                .reproduccionesAcumuladas(cobro.getReproduccionesAcumuladas())
                .estado(cobro.getEstado().name())
                .idMetodoCobro(cobro.getIdMetodoCobro())
                .nombreMetodoCobro(nombreMetodoCobro)
                .fechaPago(cobro.getFechaPago())
                .descripcion(cobro.getDescripcion())
                .idCompra(cobro.getCompra() != null ? cobro.getCompra().getIdCompra() : null)
                .build();
    }

    /**
     * Resuelve el nombre del método de cobro consultando el microservicio de usuarios.
     *
     * @param idMetodoCobro identificador del método de cobro
     * @return nombre formateado del método o null si no se encuentra
     */
    private String resolverNombreMetodoCobro(Long idMetodoCobro) {
        MetodoCobroBasicoDTO metodo = usuariosClient.obtenerMetodoCobro(idMetodoCobro);
        if (metodo != null && metodo.getTipo() != null) {
            return mapearTipoANombre(metodo.getTipo());
        }
        return null;
    }

    /**
     * Mapea el tipo de método de cobro a su nombre formateado.
     *
     * @param tipo tipo del método de cobro
     * @return nombre formateado del método
     */
    private String mapearTipoANombre(String tipo) {
        if (tipo == null) {
            return null;
        }
        return switch (tipo.toLowerCase()) {
            case "paypal" -> "PayPal";
            case "transferencia" -> "Transferencia";
            case "bizum" -> "Bizum";
            case "tarjeta" -> "Tarjeta";
            default -> tipo;
        };
    }

    /**
     * Configura el ordenamiento de resultados según criterio y dirección.
     *
     * <p>Criterios soportados: monto o fecha de cobro. Por defecto ordena por fecha descendente.</p>
     *
     * @param ordenarPor campo de ordenamiento
     * @param direccion dirección de ordenamiento (ASC/DESC)
     * @return objeto Sort configurado
     */
    private Sort configurarOrdenamiento(String ordenarPor, String direccion) {
        Sort.Direction dir = "ASC".equalsIgnoreCase(direccion)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String campo = "MONTO".equalsIgnoreCase(ordenarPor) ? "monto" : "fechaCobro";

        return Sort.by(dir, campo);
    }

    /**
     * Parsea una cadena a enum EstadoCobro.
     *
     * @param estado cadena con el estado
     * @return enum EstadoCobro o null si no es válido
     */
    private EstadoCobro parseEstadoCobro(String estado) {
        if (estado == null || estado.isBlank()) return null;
        try {
            return EstadoCobro.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Parsea una cadena a enum TipoCobro.
     *
     * @param tipo cadena con el tipo de cobro
     * @return enum TipoCobro o null si no es válido
     */
    private TipoCobro parseTipoCobro(String tipo) {
        if (tipo == null || tipo.isBlank()) return null;
        try {
            return TipoCobro.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Parsea una cadena a enum TipoContenido.
     *
     * @param tipo cadena con el tipo de contenido
     * @return enum TipoContenido o null si no es válido
     */
    private TipoContenido parseTipoContenido(String tipo) {
        if (tipo == null || tipo.isBlank()) return null;
        try {
            return TipoContenido.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}