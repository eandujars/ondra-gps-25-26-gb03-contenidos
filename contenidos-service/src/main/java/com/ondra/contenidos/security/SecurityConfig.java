package com.ondra.contenidos.security;

import com.ondra.contenidos.security.JwtAuthenticationFilter;
import com.ondra.contenidos.security.ServiceTokenFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración central de Spring Security para el microservicio de Contenidos.
 *
 * <p>Define las reglas de autorización y la cadena de filtros de seguridad.
 * Distingue entre endpoints públicos (consultas de catálogo) y protegidos
 * (operaciones de escritura que requieren autenticación JWT).</p>
 *
 * <p><strong>Filtros aplicados en orden:</strong></p>
 * <ol>
 *   <li>ServiceTokenFilter - Autenticación entre microservicios</li>
 *   <li>JwtAuthenticationFilter - Autenticación de usuarios</li>
 *   <li>UsernamePasswordAuthenticationFilter - Filtro estándar de Spring</li>
 * </ol>
 *
 * <p>Esta configuración debe estar sincronizada con las exclusiones definidas
 * en {@link JwtAuthenticationFilter#shouldNotFilter(HttpServletRequest)}.</p>
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ServiceTokenFilter serviceTokenFilter;

    /**
     * Configura CORS para permitir peticiones desde el frontend.
     *
     * <p>Configuración actual: permite localhost:4200 con credenciales.</p>
     *
     * @return Fuente de configuración CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Configura la cadena de filtros de seguridad y las reglas de autorización.
     *
     * <p><strong>Reglas de acceso:</strong></p>
     * <ul>
     *   <li>Géneros: Todos los endpoints GET son públicos (catálogo)</li>
     *   <li>Multimedia: Todos los endpoints requieren autenticación</li>
     *   <li>Canciones y Álbumes: GET públicos, POST/PUT/DELETE protegidos</li>
     *   <li>Favoritos: GET público para consultas, resto protegidos</li>
     *   <li>Health Check: Público para monitoreo</li>
     * </ul>
     *
     * @param http Configurador de seguridad HTTP
     * @return Cadena de filtros configurada
     * @throws Exception Si ocurre error en la configuración
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Géneros - Públicos (catálogo)
                        .requestMatchers(HttpMethod.GET, "/api/generos/**").permitAll()

                        // Multimedia - Protegidos (requieren JWT)
                        .requestMatchers("/api/multimedia/**").authenticated()

                        // Canciones - GET públicos, resto protegidos
                        .requestMatchers(HttpMethod.GET, "/api/canciones/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/canciones/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/canciones/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/canciones/**").authenticated()

                        // Álbumes - GET públicos, resto protegidos
                        .requestMatchers(HttpMethod.GET, "/api/albumes/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/albumes/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/albumes/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/albumes/**").authenticated()

                        // Favoritos - GET público, resto protegidos
                        .requestMatchers(HttpMethod.GET, "/api/favoritos", "/api/favoritos/**").permitAll()
                        .requestMatchers("/api/favoritos/**").authenticated()

                        // Compras - Todos los endpoints protegidos
                        .requestMatchers("/api/compras/**").authenticated()

                        // Carrito - Todos los endpoints protegidos
                        .requestMatchers("/api/carrito/**").authenticated()

                        // Health Check - Público
                        .requestMatchers("/actuator/**").permitAll()

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )

                // Orden de filtros: ServiceToken -> JWT -> UsernamePassword
                .addFilterBefore(serviceTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("SecurityFilterChain de Contenidos configurado correctamente");
        return http.build();
    }
}