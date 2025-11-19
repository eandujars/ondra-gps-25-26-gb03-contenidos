package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con información resumida de un álbum.
 * Usado cuando se necesita mostrar información básica del álbum
 * en contextos donde no se requiere el detalle completo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResumenDTO {

    /**
     * ID único del álbum.
     */
    private Long idAlbum;

    /**
     * Título del álbum.
     */
    private String tituloAlbum;

    /**
     * URL de la portada del álbum en Cloudinary.
     */
    private String urlPortada;
}