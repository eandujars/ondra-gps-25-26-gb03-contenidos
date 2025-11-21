package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritoDTO {

    private Long idFavorito;
    private Long idUsuario;
    private String tipoContenido;
    private CancionDTO cancion;
    private AlbumDTO album;
    private LocalDateTime fechaAgregado;
}