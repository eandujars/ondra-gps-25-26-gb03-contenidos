package com.ondra.contenidos.clients;

import com.ondra.contenidos.dto.MetodoCobroBasicoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

/**
 * Cliente HTTP para comunicación con el microservicio de Usuarios.
 *
 * <p>Gestiona operaciones relacionadas con información de usuarios.
 * Todas las peticiones incluyen autenticación mediante token de servicio.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsuariosClient {

    private final RestTemplate restTemplate;

    @Value("${microservices.usuarios.url}")
    private String usuariosServiceUrl;

    @Value("${microservices.service-token}")
    private String serviceToken;

    /**
     * Obtiene los datos completos de un usuario.
     *
     * @param idUsuario ID del usuario
     * @return mapa con los datos del usuario o null si no existe
     */
    public Map<String, Object> obtenerDatosUsuario(Long idUsuario) {
        return obtenerDatosUsuario(idUsuario, null);
    }

    /**
     * Obtiene los datos completos de un usuario con tipo específico.
     *
     * @param idUsuario ID del usuario
     * @param tipo Tipo de usuario (USUARIO o ARTISTA), opcional
     * @return mapa con los datos del usuario o null si no existe
     */
    public Map<String, Object> obtenerDatosUsuario(Long idUsuario, String tipo) {
        try {
            String url = usuariosServiceUrl + "/usuarios/" + idUsuario + "/datos-usuario";
            if (tipo != null && !tipo.isBlank()) {
                url += "?tipo=" + tipo;
            }
            log.debug("👤 Obteniendo datos del usuario {} (tipo: {})", idUsuario, tipo);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> datosUsuario = response.getBody();
            log.debug("✅ Datos obtenidos para usuario {}", idUsuario);

            return datosUsuario;

        } catch (Exception e) {
            log.error("❌ Error al obtener datos del usuario {}: {}", idUsuario, e.getMessage());
            return Collections.emptyMap(); // Return empty map instead of null
        }
    }

    /**
     * Verifica si un usuario existe en el sistema.
     *
     * @param idUsuario ID del usuario
     * @return true si el usuario existe, false en caso contrario
     */
    public boolean existeUsuario(Long idUsuario) {
        try {
            String url = usuariosServiceUrl + "/usuarios/" + idUsuario + "/existe";
            log.debug("🔍 Verificando existencia del usuario {}", idUsuario);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    Boolean.class
            );

            boolean existe = response.getBody() != null && response.getBody();
            log.debug("Usuario {} existe: {}", idUsuario, existe);

            return existe;

        } catch (Exception e) {
            log.error("❌ Error al verificar usuario {}: {}", idUsuario, e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el nombre completo de un usuario.
     *
     * @param idUsuario ID del usuario
     * @return nombre completo del usuario o null si no existe
     */
    public String obtenerNombreCompleto(Long idUsuario) {
        try {
            Map<String, Object> datosUsuario = obtenerDatosUsuario(idUsuario);

            if (datosUsuario != null) {
                return (String) datosUsuario.get("nombreCompleto");
            }

            return null;

        } catch (Exception e) {
            log.error("❌ Error al obtener nombre del usuario {}: {}", idUsuario, e.getMessage());
            return null;
        }
    }


    /**
     * Obtiene el primer método de cobro disponible para un artista.
     */
    public MetodoCobroBasicoDTO obtenerPrimerMetodoCobro(Long idArtista) {
        try {
            String url = usuariosServiceUrl + "/internal/metodos-cobro/artistas/" + idArtista + "/primer";
            log.debug("Consultando primer método de cobro del artista {}", idArtista);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<MetodoCobroBasicoDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    MetodoCobroBasicoDTO.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Error al obtener método de cobro del artista {}: {}", idArtista, e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene un método de cobro concreto por su ID.
     */
    public MetodoCobroBasicoDTO obtenerMetodoCobro(Long idMetodoCobro) {
        if (idMetodoCobro == null) {
            return null;
        }

        try {
            String url = usuariosServiceUrl + "/internal/metodos-cobro/" + idMetodoCobro;
            log.debug("Consultando método de cobro {}", idMetodoCobro);

            HttpHeaders headers = createServiceHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<MetodoCobroBasicoDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    MetodoCobroBasicoDTO.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Error al obtener método de cobro {}: {}", idMetodoCobro, e.getMessage());
            return null;
        }
    }

    /**
     * Crea headers HTTP con autenticación de servicio.
     *
     * @return HttpHeaders configurados con token de servicio
     */
    private HttpHeaders createServiceHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Service-Token", serviceToken);
        return headers;
    }
}
