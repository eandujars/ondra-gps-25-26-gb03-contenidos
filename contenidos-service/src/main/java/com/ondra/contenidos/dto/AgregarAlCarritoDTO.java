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
public class AgregarAlCarritoDTO {

    @NotNull(message = "El tipo de producto es obligatorio")
    private String tipoProducto; // "CANCION" o "ALBUM"

    private Long idCancion;

    private Long idAlbum;
}