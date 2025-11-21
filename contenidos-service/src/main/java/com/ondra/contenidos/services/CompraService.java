package com.ondra.contenidos.services;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.mappers.CancionMapper;
import com.ondra.contenidos.models.dao.Album;
import com.ondra.contenidos.models.dao.Compra;
import com.ondra.contenidos.repositories.CompraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Servicio para la gestión del historial de compras.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final CancionMapper cancionMapper;

    /**
     * Listar historial de compras de un usuario con paginación
     */
    @Transactional(readOnly = true)
    public ComprasPaginadasDTO listarCompras(Long idUsuario, String tipoContenido, Integer pagina, Integer limite) {
        log.debug("📋 Listando compras - Usuario: {}, Tipo: {}, Página: {}", idUsuario, tipoContenido, pagina);

        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite, Sort.by(Sort.Direction.DESC, "fechaCompra"));

        Page<Compra> paginaCompras;

        if (tipoContenido != null && !tipoContenido.isBlank()) {
            try {
                Compra.TipoContenido tipo = Compra.TipoContenido.valueOf(tipoContenido.toUpperCase());

                if (tipo == Compra.TipoContenido.CANCION) {
                    paginaCompras = compraRepository.findCancionesCompradasByUsuario(idUsuario, pageable);
                } else {
                    paginaCompras = compraRepository.findAlbumesCompradosByUsuario(idUsuario, pageable);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Tipo de contenido inválido: " + tipoContenido);
            }
        } else {
            paginaCompras = compraRepository.findByIdUsuario(idUsuario, pageable);
        }

        return ComprasPaginadasDTO.builder()
                .compras(paginaCompras.getContent().stream()
                        .map(this::convertirADTO)
                        .toList())
                .paginaActual(paginaCompras.getNumber() + 1)
                .totalPaginas(paginaCompras.getTotalPages())
                .totalElementos(paginaCompras.getTotalElements())
                .elementosPorPagina(paginaCompras.getSize())
                .build();
    }

    /**
     * Verificar si un usuario ha comprado una canción
     */
    @Transactional(readOnly = true)
    public boolean haCompradoCancion(Long idUsuario, Long idCancion) {
        return compraRepository.existsByUsuarioAndCancion(idUsuario, idCancion);
    }

    /**
     * Verificar si un usuario ha comprado un álbum
     */
    @Transactional(readOnly = true)
    public boolean haCompradoAlbum(Long idUsuario, Long idAlbum) {
        return compraRepository.existsByUsuarioAndAlbum(idUsuario, idAlbum);
    }

    /**
     * Obtener total gastado por un usuario
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalGastado(Long idUsuario) {
        return compraRepository.totalGastadoByUsuario(idUsuario);
    }

    /**
     * Convertir entidad Compra a DTO
     */
    private CompraDTO convertirADTO(Compra compra) {
        CompraDTO dto = CompraDTO.builder()
                .idCompra(compra.getIdCompra())
                .idUsuario(compra.getIdUsuario())
                .tipoContenido(compra.getTipoContenido().name())
                .precioPagado(compra.getPrecioPagado())
                .fechaCompra(compra.getFechaCompra())
                .metodoPago(compra.getMetodoPago())
                .idTransaccion(compra.getIdTransaccion())
                .build();

        if (compra.getCancion() != null) {
            dto.setCancion(cancionMapper.toDTO(compra.getCancion()));
        }

        if (compra.getAlbum() != null) {
            dto.setAlbum(convertirAlbumADTO(compra.getAlbum()));
        }

        return dto;
    }

    public void eliminarTodasLasCompras(Long idUsuario) {
        compraRepository.deleteByIdUsuario(idUsuario);
    }

    /**
     * Convertir entidad Album a AlbumDTO
     */
    private AlbumDTO convertirAlbumADTO(Album album) {
        return AlbumDTO.builder()
                .idAlbum(album.getIdAlbum())
                .tituloAlbum(album.getTituloAlbum())
                .idArtista(album.getIdArtista())
                .genero(album.getGenero().getNombre())
                .precioAlbum(album.getPrecioAlbum())
                .urlPortada(album.getUrlPortada())
                .fechaPublicacion(album.getFechaPublicacion())
                .duracionTotalSegundos(album.getDuracionTotalSegundos())
                .totalCanciones(album.getTotalCanciones())
                .totalPlayCount(album.getTotalPlayCount())
                .build();
    }
}