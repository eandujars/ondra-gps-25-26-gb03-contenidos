package com.ondra.contenidos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgregarFavoritoDTO {

    @NotNull(message = "El tipo de contenido es obligatorio")
    private String tipoContenido; // "CANCION" o "ALBUM"

    private Long idCancion;

    private Long idAlbum;
}