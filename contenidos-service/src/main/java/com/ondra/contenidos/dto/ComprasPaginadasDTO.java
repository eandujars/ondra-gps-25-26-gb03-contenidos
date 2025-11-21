package com.ondra.contenidos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprasPaginadasDTO {

    private List<CompraDTO> compras;
    private int paginaActual;
    private int totalPaginas;
    private long totalElementos;
    private int elementosPorPagina;
}