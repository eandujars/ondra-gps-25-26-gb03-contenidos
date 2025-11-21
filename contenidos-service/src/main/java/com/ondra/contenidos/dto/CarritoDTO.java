package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoDTO {

    private Long idCarrito;
    private Long idUsuario;
    private List<CarritoItemDTO> items;
    private int cantidadItems;
    private BigDecimal precioTotal;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}