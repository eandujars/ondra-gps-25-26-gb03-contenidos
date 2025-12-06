package com.ondra.contenidos.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de RestTemplate para comunicación HTTP entre microservicios.
 *
 * <p>Utiliza Apache HttpClient 5 para establecer timeouts de conexión
 * y respuesta, junto con interceptores de logging.</p>
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

    /**
     * Configura un bean RestTemplate con timeouts y logging de peticiones.
     *
     * @param builder constructor proporcionado por Spring Boot
     * @return instancia configurada de RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("🔧 Configurando RestTemplate para comunicación entre microservicios");

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(5)) // Usar setConnectionRequestTimeout en vez de setConnectTimeout
                .setResponseTimeout(Timeout.ofSeconds(10))
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return builder
                .requestFactory(() -> requestFactory)
                .interceptors(loggingInterceptor())
                .build();
    }

    /**
     * Proporciona un interceptor para registrar detalles de peticiones y respuestas HTTP.
     *
     * @return interceptor de logging configurado
     */
    private ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            log.debug("📤 Request: {} {} - Body size: {} bytes",
                    request.getMethod(),
                    request.getURI(),
                    body.length);

            var response = execution.execute(request, body);

            log.debug("📥 Response: {} - Status: {}",
                    request.getURI(),
                    response.getStatusCode());

            return response;
        };
    }
}