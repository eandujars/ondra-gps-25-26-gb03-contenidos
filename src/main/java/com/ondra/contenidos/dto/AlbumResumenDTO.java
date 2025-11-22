package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con información resumida de un álbum.
 *
 * <p>Utilizado en contextos donde se requiere información básica
 * sin el detalle completo del álbum.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResumenDTO {

    /**
     * Identificador único del álbum.
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