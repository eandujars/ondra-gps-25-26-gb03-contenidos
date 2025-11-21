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
public class FavoritosPaginadosDTO {

    private List<FavoritoDTO> favoritos;
    private int paginaActual;
    private int totalPaginas;
    private long totalElementos;
    private int elementosPorPagina;
}