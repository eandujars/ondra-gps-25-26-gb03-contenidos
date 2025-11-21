package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoItemDTO {

    private Long idCarritoItem;
    private String tipoProducto;
    private Long idCancion;
    private Long idAlbum;
    private BigDecimal precio;
    private String urlPortada;
    private String nombreArtistico;
    private String titulo;
    private LocalDateTime fechaAgregado;
}