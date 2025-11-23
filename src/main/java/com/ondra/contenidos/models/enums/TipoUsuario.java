package com.ondra.contenidos.models.enums;

/**
 * Enumeración que representa el tipo de usuario en el sistema.
 *
 * <p>Diferencia entre usuarios normales y artistas para funcionalidades
 * como comentarios, valoraciones y permisos de contenido.</p>
 */
public enum TipoUsuario {
    /**
     * Usuario normal de la plataforma.
     */
    NORMAL,

    /**
     * Usuario con perfil de artista.
     */
    ARTISTA
}