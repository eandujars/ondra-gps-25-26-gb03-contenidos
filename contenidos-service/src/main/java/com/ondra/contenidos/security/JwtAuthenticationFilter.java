package com.ondra.contenidos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Filtro de autenticación JWT para validar tokens de acceso de usuarios.
 *
 * <p>Se ejecuta después de {@link ServiceTokenFilter} y antes de la cadena de seguridad
 * de Spring Security. Valida tokens JWT en el header Authorization y establece
 * la autenticación en el contexto de seguridad.</p>
 *
 * <p>Los endpoints públicos definidos en {@link #shouldNotFilter(HttpServletRequest)}
 * son excluidos automáticamente de la validación JWT.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Determina si el filtro debe omitirse para una petición específica.
     *
     * <p>Se omite el filtro para:</p>
     * <ul>
     *   <li>Endpoints GET de géneros (catálogo público)</li>
     *   <li>Endpoints GET de canciones (reproducción pública)</li>
     *   <li>Endpoints GET de álbumes (consulta pública)</li>
     *   <li>Health checks de Actuator</li>
     *   <li>Peticiones ya autenticadas por ServiceTokenFilter</li>
     * </ul>
     *
     * @param request Petición HTTP entrante
     * @return true si el filtro debe omitirse, false si debe aplicarse
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Géneros - Públicos (todos los GET)
        if ("GET".equals(method) && path.startsWith("/generos")) {
            return true;
        }

        // Canciones - GETs públicos
        if ("GET".equals(method) && path.startsWith("/api/canciones")) {
            return true;
        }

        // Álbumes - GETs públicos
        if ("GET".equals(method) && path.startsWith("/api/albumes")) {
            return true;
        }

        // Health Check
        if (path.startsWith("/actuator")) {
            return true;
        }

        // Peticiones service-to-service ya autenticadas
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return true;
        }

        return false;
    }

    /**
     * Procesa la petición validando el token JWT y estableciendo la autenticación.
     *
     * <p>Extrae el token del header Authorization, lo valida y establece el contexto
     * de seguridad con el userId extraído del token. Si el token es inválido o ha
     * expirado, retorna una respuesta de error apropiada.</p>
     *
     * @param request Petición HTTP
     * @param response Respuesta HTTP
     * @param filterChain Cadena de filtros de Spring Security
     * @throws ServletException Si ocurre error en el servlet
     * @throws IOException Si ocurre error de I/O
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null && validateToken(token)) {
                String userId = extractUserIdFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.emptyList()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Usuario {} autenticado desde JWT", userId);
            }

        } catch (ExpiredJwtException e) {
            log.warn("Token expirado: {}", e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "TOKEN_EXPIRED", "El token ha expirado");
            return;

        } catch (SignatureException | MalformedJwtException e) {
            log.warn("Token inválido: {}", e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "INVALID_TOKEN", "Token inválido");
            return;

        } catch (UnsupportedJwtException | IllegalArgumentException e) {
            log.warn("Token no soportado: {}", e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "INVALID_TOKEN", "Token inválido");
            return;

        } catch (Exception e) {
            log.error("Error validando JWT", e);
            writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "INTERNAL_ERROR", "Error al procesar el token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     *
     * @param request Petición HTTP
     * @return Token JWT sin el prefijo "Bearer ", o null si no existe
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Valida la firma y estructura del token JWT.
     *
     * @param token Token JWT a validar
     * @return true si el token es válido
     * @throws ExpiredJwtException Si el token ha expirado
     * @throws SignatureException Si la firma es inválida
     * @throws MalformedJwtException Si el formato es incorrecto
     */
    private boolean validateToken(String token) {
        Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
        return true;
    }

    /**
     * Extrae el userId del payload del token JWT.
     *
     * @param token Token JWT
     * @return userId como String
     * @throws IllegalArgumentException Si el token no contiene userId
     */
    private String extractUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            throw new IllegalArgumentException("Token sin userId");
        }

        return String.valueOf(userIdObj);
    }

    /**
     * Genera la clave secreta para firmar/verificar tokens JWT.
     *
     * @return Clave secreta HMAC
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Escribe una respuesta de error en formato JSON.
     *
     * @param response Respuesta HTTP
     * @param status Código de estado HTTP
     * @param error Código de error
     * @param message Mensaje descriptivo del error
     * @throws IOException Si ocurre error al escribir la respuesta
     */
    private void writeErrorResponse(HttpServletResponse response, int status,
                                    String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"error\":\"%s\",\"message\":\"%s\"}", error, message
        ));
    }
}