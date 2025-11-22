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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Filtro de autenticación JWT para validación de tokens de usuario.
 *
 * <p>Valida tokens JWT del header Authorization y establece la autenticación
 * en el contexto de seguridad. Extrae userId, tipoUsuario y artistId del token.</p>
 *
 * <p>Se ejecuta después de ServiceTokenFilter y antes de la cadena estándar
 * de Spring Security. Los endpoints públicos definidos en shouldNotFilter
 * son excluidos de la validación.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Determina si el filtro debe omitirse para la petición actual.
     *
     * <p>Excluye endpoints públicos como consultas GET de catálogo
     * y peticiones ya autenticadas por ServiceTokenFilter.</p>
     *
     * @param request petición HTTP entrante
     * @return true si el filtro debe omitirse, false en caso contrario
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("GET".equals(method) && path.startsWith("/api/generos")) {
            return true;
        }

        if ("GET".equals(method) && path.startsWith("/api/canciones")) {
            return true;
        }

        if ("GET".equals(method) && path.startsWith("/api/albumes")) {
            return true;
        }

        if ("GET".equals(method) && path.startsWith("/api/comentarios")) {
            if (path.equals("/api/comentarios/mis-comentarios")) {
                return false;
            }
            return true;
        }

        if ("GET".equals(method) && path.startsWith("/api/valoraciones")) {
            return true;
        }

        if ("POST".equals(method) && path.matches("/api/canciones/\\d+/reproducir")) {
            return true;
        }

        if (path.startsWith("/actuator")) {
            return true;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return true;
        }

        return false;
    }

    /**
     * Procesa la petición validando el token JWT y estableciendo la autenticación.
     *
     * <p>Extrae userId, tipoUsuario y artistId del token y los almacena
     * en los detalles de autenticación para su uso posterior.</p>
     *
     * @param request petición HTTP
     * @param response respuesta HTTP
     * @param filterChain cadena de filtros
     * @throws ServletException si ocurre error en el servlet
     * @throws IOException si ocurre error de I/O
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null && validateToken(token)) {
                Claims claims = extractAllClaims(token);

                String userId = String.valueOf(claims.get("userId"));
                String tipoUsuario = (String) claims.get("tipoUsuario");
                Object artistIdObj = claims.get("artistId");

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.emptyList()
                        );

                Map<String, Object> additionalDetails = new HashMap<>();
                additionalDetails.put("userId", userId);
                additionalDetails.put("tipoUsuario", tipoUsuario);

                if (artistIdObj != null) {
                    String artistId = String.valueOf(artistIdObj);
                    additionalDetails.put("artistId", artistId);
                    log.debug("✅ Usuario {} autenticado con artistId: {}", userId, artistId);
                } else {
                    log.debug("✅ Usuario {} autenticado (sin artistId)", userId);
                }

                authentication.setDetails(additionalDetails);
                SecurityContextHolder.getContext().setAuthentication(authentication);
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
     * @param request petición HTTP
     * @return token JWT sin el prefijo Bearer, o null si no existe
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
     * @param token token JWT a validar
     * @return true si el token es válido
     */
    private boolean validateToken(String token) {
        Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
        return true;
    }

    /**
     * Extrae todos los claims del payload del token JWT.
     *
     * @param token token JWT
     * @return claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtiene la clave de firma para validación de tokens.
     *
     * @return clave secreta HMAC
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Escribe una respuesta de error en formato JSON.
     *
     * @param response respuesta HTTP
     * @param status código de estado HTTP
     * @param error código de error
     * @param message mensaje descriptivo
     * @throws IOException si ocurre error al escribir
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