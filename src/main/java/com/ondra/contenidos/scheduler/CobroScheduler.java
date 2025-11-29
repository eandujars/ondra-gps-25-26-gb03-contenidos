package com.ondra.contenidos.scheduler;

import com.ondra.contenidos.services.CobroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler para procesar automáticamente pagos mensuales.
 *
 * <p>Se ejecuta el primer día de cada mes a las 00:00.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CobroScheduler {

    private final CobroService cobroService;

    /**
     * Procesa todos los cobros pendientes el primer día de cada mes.
     *
     * <p>Expresión cron: "0 0 0 1 * ?" = A las 00:00 del día 1 de cada mes
     *
     * <p>Para testing, puedes cambiar la expresión a:
     * - "0 * * * * ?" = Cada minuto (testing)
     * - "0 0 0 * * ?" = Cada día a medianoche
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void procesarPagosMensualesAutomatico() {
        log.info("🗓️ Ejecutando procesamiento automático mensual de pagos...");

        try {
            CobroService.ResumenProcesamientoPagos resumen =
                    cobroService.procesarPagosMensuales(null);

            log.info("✅ Procesamiento automático completado:");
            log.info("   • Cobros procesados: {}", resumen.cobrosProcessados());
            log.info("   • Monto total: {}€", resumen.montoTotal());
            log.info("   • Fecha: {}", resumen.fechaProcesamiento());

        } catch (Exception e) {
            log.error("❌ Error durante el procesamiento automático de pagos", e);
        }
    }
}