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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // ========== ENDPOINTS PÚBLICOS ==========

        // Géneros - Públicos (todos los GET)
        if ("GET".equals(method) && path.startsWith("/api/generos")) {
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

        // POST reproducir - público o autenticado
        if ("POST".equals(method) && path.matches("/api/canciones/\\d+/reproducir")) {
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
     * Extrae userId, tipoUsuario y artistId (si aplica) del token.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null && validateToken(token)) {
                // ✅ EXTRAER TODOS LOS CLAIMS DEL TOKEN
                Claims claims = extractAllClaims(token);

                String userId = String.valueOf(claims.get("userId"));
                String tipoUsuario = (String) claims.get("tipoUsuario");
                Object artistIdObj = claims.get("artistId");  // ✅ Usando artistId

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,  // Principal (authentication.getName())
                                null,
                                Collections.emptyList()
                        );

                // ✅ CREAR MAP PERSONALIZADO CON LOS CLAIMS
                Map<String, Object> additionalDetails = new HashMap<>();
                additionalDetails.put("userId", userId);
                additionalDetails.put("tipoUsuario", tipoUsuario);

                if (artistIdObj != null) {
                    String artistId = String.valueOf(artistIdObj);
                    additionalDetails.put("artistId", artistId);  // ✅ Usando artistId
                    log.debug("✅ Usuario {} autenticado con artistId: {}", userId, artistId);
                } else {
                    log.debug("✅ Usuario {} autenticado (sin artistId)", userId);
                }

                // ✅ ESTABLECER EL MAP COMO DETAILS
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

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
        return true;
    }

    /**
     * Extrae todos los claims del token JWT.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

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