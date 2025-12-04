package com.ondra.contenidos.dto;

import com.ondra.contenidos.dto.AlbumDTO;
import com.ondra.contenidos.dto.CancionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO completo con el contenido de un artista.
 * Usado por el microservicio Usuarios para mostrar el perfil completo del artista.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContenidoCompletoArtistaDTO {

    /**
     * ID del artista.
     */
    private Long idArtista;

    /**
     * Total de reproducciones de todas las canciones del artista.
     */
    private Long totalReproducciones;

    /**
     * Número total de canciones del artista.
     */
    private Integer totalCanciones;

    /**
     * Número total de álbumes del artista.
     */
    private Integer totalAlbumes;

    /**
     * Lista de todas las canciones del artista.
     */
    private List<CancionDTO> canciones;

    /**
     * Lista de todos los álbumes del artista.
     */
    private List<AlbumDTO> albumes;
}