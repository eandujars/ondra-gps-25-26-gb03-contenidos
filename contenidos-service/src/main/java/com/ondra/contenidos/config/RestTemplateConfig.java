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

@Slf4j
@Configuration
public class RestTemplateConfig {

    /**
     * Bean de RestTemplate configurado para comunicación entre microservicios.
     *
     * @param builder RestTemplateBuilder proporcionado por Spring Boot
     * @return RestTemplate configurado
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("🔧 Configurando RestTemplate para comunicación entre microservicios");

        // Configurar los timeouts en milisegundos
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(5))   // 5 segundos
                .setResponseTimeout(Timeout.ofSeconds(10)) // 10 segundos
                .build();

        // Crear cliente HTTP con la configuración
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        // Factory para RestTemplate usando Apache HttpClient
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        return builder
                .requestFactory(() -> requestFactory)
                .interceptors(loggingInterceptor())
                .build();
    }

    /**
     * Interceptor para logging de peticiones y respuestas HTTP.
     *
     * @return ClientHttpRequestInterceptor
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