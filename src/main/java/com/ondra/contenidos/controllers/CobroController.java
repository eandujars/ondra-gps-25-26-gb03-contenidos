package com.ondra.contenidos.controllers;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.services.CobroListService;
import com.ondra.contenidos.services.CobroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de cobros de artistas.
 *
 * <p>Proporciona endpoints para consultar, filtrar y procesar cobros
 * generados por compras y reproducciones de contenido.</p>
 *
 * <p>Base URL: /api/cobros</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/cobros")
@RequiredArgsConstructor
public class CobroController {

    private final CobroService cobroService;
    private final CobroListService cobroListService;

    /**
     * Lista cobros con filtros avanzados y paginación.
     *
     * @param idArtista filtro por artista
     * @param estado filtro por estado del cobro (PENDIENTE, PAGADO)
     * @param tipoCobro filtro por tipo (COMPRA, REPRODUCCION)
     * @param tipoContenido filtro por tipo de contenido (CANCION, ÁLBUM)
     * @param fechaDesde filtro por fecha inicial
     * @param fechaHasta filtro por fecha final
     * @param mes filtro por mes específico
     * @param anio filtro por año específico
     * @param montoMinimo filtro por monto mínimo
     * @param montoMaximo filtro por monto máximo
     * @param ordenarPor criterio de ordenación (FECHA, MONTO)
     * @param direccion dirección de ordenación (ASC, DESC)
     * @param pagina número de página (1-indexed)
     * @param limite elementos por página (default: 20)
     * @return página de cobros con metadatos de paginación
     */
    @GetMapping
    public ResponseEntity<CobrosPaginadosDTO> listarCobros(
            @RequestParam(required = false) Long idArtista,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipoCobro,
            @RequestParam(required = false) String tipoContenido,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) BigDecimal montoMinimo,
            @RequestParam(required = false) BigDecimal montoMaximo,
            @RequestParam(required = false, defaultValue = "FECHA") String ordenarPor,
            @RequestParam(required = false, defaultValue = "DESC") String direccion,
            @RequestParam(required = false, defaultValue = "1") Integer pagina,
            @RequestParam(required = false, defaultValue = "20") Integer limite) {

        FiltrosCobrosDTO filtros = FiltrosCobrosDTO.builder()
                .idArtista(idArtista)
                .estado(estado)
                .tipoCobro(tipoCobro)
                .tipoContenido(tipoContenido)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .mes(mes)
                .anio(anio)
                .montoMinimo(montoMinimo)
                .montoMaximo(montoMaximo)
                .ordenarPor(ordenarPor)
                .direccion(direccion)
                .pagina(pagina)
                .limite(limite)
                .build();

        CobrosPaginadosDTO resultado = cobroListService.listarCobrosConFiltros(filtros);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtiene cobros de un mes específico para un artista.
     *
     * @param idArtista identificador del artista
     * @param mes mes a consultar (1-12)
     * @param anio año a consultar
     * @param pagina número de página (default: 1)
     * @param limite elementos por página (default: 20)
     * @return página de cobros del mes especificado
     */
    @GetMapping("/mes")
    public ResponseEntity<CobrosPaginadosDTO> listarCobrosPorMes(
            @RequestParam Long idArtista,
            @RequestParam Integer mes,
            @RequestParam Integer anio,
            @RequestParam(required = false, defaultValue = "1") Integer pagina,
            @RequestParam(required = false, defaultValue = "20") Integer limite) {

        CobrosPaginadosDTO resultado = cobroListService.listarCobrosPorMes(
                idArtista, mes, anio, pagina, limite);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtiene resumen de cobros agrupados por mes para un artista.
     *
     * @param idArtista identificador del artista
     * @return lista de resúmenes mensuales con totales
     */
    @GetMapping("/resumen-mensual")
    public ResponseEntity<List<ResumenCobrosDTO>> obtenerResumenMensual(
            @RequestParam Long idArtista) {

        List<ResumenCobrosDTO> resumen = cobroListService.obtenerResumenPorMes(idArtista);
        return ResponseEntity.ok(resumen);
    }

    /**
     * Obtiene totales de ingresos de un artista.
     *
     * @param idArtista identificador del artista
     * @return mapa con totales de ingresos, pendientes y pagados
     */
    @GetMapping("/totales")
    public ResponseEntity<Map<String, BigDecimal>> obtenerTotales(
            @RequestParam Long idArtista) {

        BigDecimal totalIngresos = cobroService.obtenerTotalIngresos(idArtista);
        BigDecimal totalPendiente = cobroService.obtenerTotalPendiente(idArtista);

        return ResponseEntity.ok(Map.of(
                "totalIngresos", totalIngresos,
                "totalPendiente", totalPendiente,
                "totalPagado", totalIngresos.subtract(totalPendiente)
        ));
    }

    /**
     * Marca todos los cobros pendientes de un artista como pagados.
     *
     * @param request mapa con idArtista e idMetodoCobro
     * @return respuesta con cantidad de cobros procesados
     */
    @PostMapping("/marcar-pagados")
    public ResponseEntity<Map<String, Object>> marcarComoPagados(
            @RequestBody Map<String, Object> request) {

        Long idArtista = Long.valueOf(request.get("idArtista").toString());
        Long idMetodoCobro = Long.valueOf(request.get("idMetodoCobro").toString());

        int cantidad = cobroService.marcarComoPagados(idArtista, idMetodoCobro);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Cobros marcados como pagados",
                "cantidadProcesada", cantidad
        ));
    }

    /**
     * Marca cobros específicos como pagados según sus identificadores.
     *
     * @param request mapa con lista de idsCobros e idMetodoCobro
     * @return respuesta con cantidad de cobros procesados
     */
    @PostMapping("/marcar-pagados-especificos")
    public ResponseEntity<Map<String, Object>> marcarCobrosEspecificosComoPagados(
            @RequestBody Map<String, Object> request) {

        @SuppressWarnings("unchecked")
        List<Long> idsCobros = ((List<Integer>) request.get("idsCobros")).stream()
                .map(Long::valueOf)
                .toList();

        Long idMetodoCobro = Long.valueOf(request.get("idMetodoCobro").toString());

        int cantidad = cobroService.marcarCobrosEspecificosComoPagados(
                idsCobros, idMetodoCobro);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Cobros específicos marcados como pagados",
                "cantidadProcesada", cantidad
        ));
    }

    /**
     * Procesa manualmente todos los cobros pendientes del sistema.
     * Ejecuta el mismo proceso que se realiza automáticamente cada mes.
     *
     * @param request mapa opcional con idMetodoCobro
     * @return resumen del procesamiento con cobros procesados, monto total y fecha
     */
    @PostMapping("/procesar-pagos-mensuales")
    public ResponseEntity<Map<String, Object>> procesarPagosMensuales(
            @RequestBody(required = false) Map<String, Object> request) {

        Long idMetodoCobro = null;
        if (request != null && request.containsKey("idMetodoCobro")) {
            idMetodoCobro = Long.valueOf(request.get("idMetodoCobro").toString());
        }

        CobroService.ResumenProcesamientoPagos resumen =
                cobroService.procesarPagosMensuales(idMetodoCobro);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Procesamiento mensual ejecutado correctamente",
                "cobrosProcessados", resumen.cobrosProcessados(),
                "montoTotal", resumen.montoTotal(),
                "fechaProcesamiento", resumen.fechaProcesamiento()
        ));
    }
}