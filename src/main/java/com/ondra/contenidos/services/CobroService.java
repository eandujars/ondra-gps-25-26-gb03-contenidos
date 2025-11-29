package com.ondra.contenidos.services;

import com.ondra.contenidos.clients.UsuariosClient;
import com.ondra.contenidos.dto.MetodoCobroBasicoDTO;
import com.ondra.contenidos.models.dao.*;
import com.ondra.contenidos.models.enums.EstadoCobro;
import com.ondra.contenidos.models.enums.TipoCobro;
import com.ondra.contenidos.models.enums.TipoContenido;
import com.ondra.contenidos.repositories.CobroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de cobros de artistas.
 *
 * <p>Proporciona operaciones para generación automática de cobros por compras
 * y reproducciones, consulta de totales, y procesamiento de pagos mensuales.</p>
 *
 * <p>Configuración: El artista recibe el 80% del precio de venta y 5€ por cada
 * 1000 reproducciones acumuladas (valores configurables).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CobroService {

    private final CobroRepository cobroRepository;
    private final UsuariosClient usuariosClient;

    @Value("${app.cobro.porcentaje-artista:0.80}")
    private BigDecimal porcentajeArtista;

    @Value("${app.cobro.reproducciones-umbral:1000}")
    private long umbralReproducciones;

    @Value("${app.cobro.pago-por-mil-reproducciones:5.00}")
    private BigDecimal pagoPorMilReproducciones;

    /**
     * Obtiene o asigna un método de cobro para un artista.
     *
     * <p>Consulta el primer método de cobro activo del artista en el microservicio
     * de usuarios. Si no tiene métodos configurados, retorna null y registra una
     * advertencia, dejando los cobros pendientes hasta que configure uno.</p>
     *
     * @param idArtista identificador del artista
     * @return identificador del método de cobro o null si no tiene configurado
     */
    private Long obtenerOAsignarMetodoCobro(Long idArtista) {
        try {
            MetodoCobroBasicoDTO metodoCobro = usuariosClient.obtenerPrimerMetodoCobro(idArtista);
            if (metodoCobro != null) {
                log.debug("✅ Método de cobro {} asignado al artista {}",
                        metodoCobro.getIdMetodoCobro(), idArtista);
                return metodoCobro.getIdMetodoCobro();
            } else {
                log.warn("⚠️ El artista {} no tiene métodos de cobro registrados. " +
                        "Los cobros quedarán pendientes hasta que configure uno.", idArtista);
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Error al consultar método de cobro del artista {}: {}",
                    idArtista, e.getMessage());
            return null;
        }
    }

    /**
     * Genera un cobro por compra de contenido.
     *
     * <p>Calcula el monto del artista aplicando el porcentaje configurado sobre
     * el precio pagado y crea un registro de cobro en estado pendiente.</p>
     *
     * @param compra registro de la compra realizada
     * @param idArtista identificador del artista propietario
     * @param tipoContenido tipo de contenido comprado
     * @param idContenido identificador del contenido específico
     */
    @Transactional
    public void generarCobroPorCompra(
            Compra compra,
            Long idArtista,
            TipoContenido tipoContenido,
            Long idContenido) {

        BigDecimal montoArtista = compra.getPrecioPagado()
                .multiply(porcentajeArtista)
                .setScale(2, RoundingMode.HALF_UP);

        Long idMetodoCobro = obtenerOAsignarMetodoCobro(idArtista);

        Cobro cobro = Cobro.builder()
                .idArtista(idArtista)
                .tipoCobro(TipoCobro.COMPRA)
                .monto(montoArtista)
                .compra(compra)
                .tipoContenido(tipoContenido)
                .estado(EstadoCobro.PENDIENTE)
                .idMetodoCobro(idMetodoCobro)
                .descripcion(String.format("Compra de %s (80%% de %.2f€)",
                        tipoContenido.name().toLowerCase(), compra.getPrecioPagado()))
                .build();

        if (tipoContenido == TipoContenido.CANCIÓN) {
            cobro.setIdCancion(idContenido);
        } else {
            cobro.setIdAlbum(idContenido);
        }

        cobroRepository.save(cobro);

        log.info("💰 Cobro generado para artista {} por compra: {}€ {}",
                idArtista, montoArtista,
                idMetodoCobro == null ? "(sin método de cobro asignado)" : "");
    }

    /**
     * Verifica y genera cobros por reproducciones de una canción.
     *
     * <p>Calcula el umbral actual de reproducciones y genera un cobro si se ha
     * cruzado un nuevo umbral desde el último cobro. Solo se genera un cobro
     * por el último umbral alcanzado, evitando duplicados.</p>
     *
     * @param cancion canción cuyas reproducciones verificar
     */
    @Transactional
    public void verificarYGenerarCobroPorReproducciones(Cancion cancion) {
        Long totalReproducciones = cancion.getReproducciones();

        if (totalReproducciones < umbralReproducciones) {
            log.debug("ℹ️ Canción '{}' solo tiene {} reproducciones (umbral: {})",
                    cancion.getTituloCancion(), totalReproducciones, umbralReproducciones);
            return;
        }

        Optional<Cobro> ultimoCobroOpt = cobroRepository
                .findUltimoCobroPorReproduccionCancion(cancion.getIdCancion());

        long reproduccionesYaCobradas = 0L;

        if (ultimoCobroOpt.isPresent()) {
            Cobro ultimoCobro = ultimoCobroOpt.get();
            reproduccionesYaCobradas = ultimoCobro.getReproduccionesAcumuladas();

            log.debug("📊 Último cobro encontrado para '{}': {} reproducciones cobradas",
                    cancion.getTituloCancion(), reproduccionesYaCobradas);
        } else {
            log.debug("📊 No hay cobros previos para '{}'", cancion.getTituloCancion());
        }

        long umbralActual = (totalReproducciones / umbralReproducciones) * umbralReproducciones;

        if (umbralActual > reproduccionesYaCobradas) {
            Long idMetodoCobro = obtenerOAsignarMetodoCobro(cancion.getIdArtista());

            Cobro cobro = Cobro.builder()
                    .idArtista(cancion.getIdArtista())
                    .tipoCobro(TipoCobro.REPRODUCCION)
                    .monto(pagoPorMilReproducciones)
                    .tipoContenido(TipoContenido.CANCIÓN)
                    .idCancion(cancion.getIdCancion())
                    .reproduccionesAcumuladas(umbralActual)
                    .estado(EstadoCobro.PENDIENTE)
                    .idMetodoCobro(idMetodoCobro)
                    .descripcion(String.format("Cobro por alcanzar %d reproducciones de '%s'",
                            umbralActual, cancion.getTituloCancion()))
                    .build();

            cobroRepository.save(cobro);

            log.info("💰 Cobro por reproducciones generado: {} - {}€ (Umbral: {} de {} totales) {}",
                    cancion.getTituloCancion(),
                    pagoPorMilReproducciones,
                    umbralActual,
                    totalReproducciones,
                    idMetodoCobro == null ? "(sin método de cobro asignado)" : "");
        } else {
            long reproduccionesPendientes = totalReproducciones - reproduccionesYaCobradas;
            long faltanParaSiguiente = umbralReproducciones - (reproduccionesPendientes % umbralReproducciones);

            log.debug("ℹ️ No se genera cobro para '{}'. Faltan {} reproducciones para el siguiente umbral " +
                            "(actual: {}, cobradas: {}, siguiente umbral: {})",
                    cancion.getTituloCancion(), faltanParaSiguiente, totalReproducciones,
                    reproduccionesYaCobradas, reproduccionesYaCobradas + umbralReproducciones);
        }
    }

    /**
     * Calcula el total de ingresos generados por un artista.
     *
     * @param idArtista identificador del artista
     * @return suma de todos los cobros del artista
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalIngresos(Long idArtista) {
        return cobroRepository.totalIngresosByArtista(idArtista);
    }

    /**
     * Calcula el total de cobros pendientes de pago para un artista.
     *
     * @param idArtista identificador del artista
     * @return suma de cobros en estado pendiente
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalPendiente(Long idArtista) {
        return cobroRepository.totalPendienteByArtista(idArtista);
    }

    /**
     * Marca todos los cobros pendientes de un artista como pagados.
     *
     * @param idArtista identificador del artista
     * @param idMetodoCobro identificador del método de cobro utilizado
     * @return cantidad de cobros procesados
     */
    @Transactional
    public int marcarComoPagados(Long idArtista, Long idMetodoCobro) {
        List<Cobro> cobrosPendientes = cobroRepository
                .findByIdArtistaAndEstado(idArtista, EstadoCobro.PENDIENTE);

        if (cobrosPendientes.isEmpty()) {
            log.info("💳 No hay cobros pendientes para el artista {}", idArtista);
            return 0;
        }

        LocalDateTime fechaPago = LocalDateTime.now();

        for (Cobro cobro : cobrosPendientes) {
            cobro.setEstado(EstadoCobro.PAGADO);
            cobro.setIdMetodoCobro(idMetodoCobro);
            cobro.setFechaPago(fechaPago);
        }

        cobroRepository.saveAll(cobrosPendientes);

        BigDecimal totalPagado = cobrosPendientes.stream()
                .map(Cobro::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("💳 {} cobros marcados como pagados para artista {} - Total: {}€ - Método: {}",
                cobrosPendientes.size(), idArtista, totalPagado, idMetodoCobro);

        return cobrosPendientes.size();
    }

    /**
     * Marca cobros específicos como pagados.
     *
     * <p>Solo procesa cobros que estén en estado pendiente, ignorando los demás.</p>
     *
     * @param idsCobros lista de identificadores de cobros a marcar
     * @param idMetodoCobro identificador del método de cobro utilizado
     * @return cantidad de cobros procesados exitosamente
     */
    @Transactional
    public int marcarCobrosEspecificosComoPagados(
            List<Long> idsCobros,
            Long idMetodoCobro) {

        List<Cobro> cobros = cobroRepository.findAllById(idsCobros);

        if (cobros.isEmpty()) {
            log.warn("⚠️ No se encontraron cobros con los IDs proporcionados");
            return 0;
        }

        LocalDateTime fechaPago = LocalDateTime.now();

        for (Cobro cobro :cobros) {
            if (cobro.getEstado() != EstadoCobro.PENDIENTE) {
                log.warn("⚠️ Cobro {} ya está en estado {}", cobro.getIdCobro(), cobro.getEstado());
                continue;
            }cobro.setEstado(EstadoCobro.PAGADO);
            cobro.setIdMetodoCobro(idMetodoCobro);
            cobro.setFechaPago(fechaPago);
        }

        cobroRepository.saveAll(cobros);

        log.info("💳 {} cobros marcados como pagados - Método: {}",
                cobros.size(), idMetodoCobro);

        return cobros.size();
    }

    /**
     * Procesa automáticamente todos los cobros pendientes del sistema.
     *
     * <p>Utilizado para procesamiento masivo mensual. Marca todos los cobros
     * pendientes de todos los artistas como pagados en una única operación.</p>
     *
     * @param idMetodoCobro identificador del método de cobro utilizado (opcional)
     * @return resumen con cantidad de cobros procesados, monto total y fecha
     */
    @Transactional
    public ResumenProcesamientoPagos procesarPagosMensuales(Long idMetodoCobro) {
        log.info("🔄 Iniciando procesamiento mensual de pagos...");

        List<Cobro> todosPendientes = cobroRepository.findAll().stream()
                .filter(c -> c.getEstado() == EstadoCobro.PENDIENTE)
                .toList();

        if (todosPendientes.isEmpty()) {
            log.info("✅ No hay cobros pendientes para procesar");
            return new ResumenProcesamientoPagos(0, BigDecimal.ZERO, LocalDateTime.now());
        }

        Long metodoPago = (idMetodoCobro != null) ? idMetodoCobro : 1L;
        LocalDateTime fechaPago = LocalDateTime.now();

        for (Cobro cobro : todosPendientes) {
            cobro.setEstado(EstadoCobro.PAGADO);
            cobro.setIdMetodoCobro(metodoPago);
            cobro.setFechaPago(fechaPago);
        }

        cobroRepository.saveAll(todosPendientes);

        BigDecimal montoTotal = todosPendientes.stream()
                .map(Cobro::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("✅ Procesamiento mensual completado:");
        log.info("   • Cobros procesados: {}", todosPendientes.size());
        log.info("   • Monto total: {}€", montoTotal);
        log.info("   • Método de pago: {}", metodoPago);

        return new ResumenProcesamientoPagos(
                todosPendientes.size(),
                montoTotal,
                fechaPago
        );
    }

    /**
     * Resumen del resultado de un procesamiento de pagos.
     *
     * @param cobrosProcessados cantidad de cobros marcados como pagados
     * @param montoTotal suma total de los cobros procesados
     * @param fechaProcesamiento fecha y hora del procesamiento
     */
    public record ResumenProcesamientoPagos(
            int cobrosProcessados,
            BigDecimal montoTotal,
            LocalDateTime fechaProcesamiento
    ) {}
}