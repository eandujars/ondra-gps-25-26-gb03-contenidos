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
public class CompraDTO {

    private Long idCompra;
    private Long idUsuario;
    private String tipoContenido;
    private CancionDTO cancion;
    private AlbumDTO album;
    private BigDecimal precioPagado;
    private LocalDateTime fechaCompra;
    private String metodoPago;
    private String idTransaccion;
}