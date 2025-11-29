package com.ondra.contenidos.services;

import com.ondra.contenidos.clients.UsuariosClient;
import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.mappers.CancionMapper;
import com.ondra.contenidos.models.dao.Album;
import com.ondra.contenidos.models.dao.Compra;
import com.ondra.contenidos.models.enums.TipoContenido;
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
import java.util.Map;

/**
 * Servicio para gestión del historial de compras.
 *
 * <p>Proporciona operaciones para consultar el historial de compras de usuarios,
 * verificar propiedad de contenidos y calcular estadísticas de gasto.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final CancionMapper cancionMapper;
    private final UsuariosClient usuariosClient;
    private final CobroService cobroService;

    private static class DatosArtista {
        String nombre;
        String slug;
    }

    /**
     * Lista el historial de compras de un usuario con paginación y filtro opcional por tipo.
     *
     * <p>Ordena las compras por fecha descendente. Si se especifica un tipo de contenido,
     * filtra solo canciones o álbumes.</p>
     *
     * @param idUsuario identificador del usuario
     * @param tipoContenido tipo de contenido a filtrar (CANCION o ÁLBUM), opcional
     * @param pagina número de página (base 1)
     * @param limite cantidad de elementos por página
     * @return compras paginadas con metadatos
     * @throws IllegalArgumentException si el tipo de contenido es inválido
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
                TipoContenido tipo = TipoContenido.valueOf(tipoContenido.toUpperCase());

                if (tipo == TipoContenido.CANCIÓN) {
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
     * Verifica si un usuario ha comprado una canción específica.
     *
     * @param idUsuario identificador del usuario
     * @param idCancion identificador de la canción
     * @return true si el usuario ha comprado la canción
     */
    @Transactional(readOnly = true)
    public boolean haCompradoCancion(Long idUsuario, Long idCancion) {
        return compraRepository.existsByUsuarioAndCancion(idUsuario, idCancion);
    }

    /**
     * Verifica si un usuario ha comprado un álbum específico.
     *
     * @param idUsuario identificador del usuario
     * @param idAlbum identificador del álbum
     * @return true si el usuario ha comprado el álbum
     */
    @Transactional(readOnly = true)
    public boolean haCompradoAlbum(Long idUsuario, Long idAlbum) {
        return compraRepository.existsByUsuarioAndAlbum(idUsuario, idAlbum);
    }

    /**
     * Calcula el total gastado por un usuario en compras.
     *
     * @param idUsuario identificador del usuario
     * @return suma total de todas las compras del usuario
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalGastado(Long idUsuario) {
        return compraRepository.totalGastadoByUsuario(idUsuario);
    }

    /**
     * Elimina todas las compras de un usuario.
     *
     * <p>Utilizado cuando se elimina un usuario del sistema.</p>
     *
     * @param idUsuario identificador del usuario
     */
    public void eliminarTodasLasCompras(Long idUsuario) {
        compraRepository.deleteByIdUsuario(idUsuario);
    }

    /**
     * Convierte una entidad Compra a su representación DTO.
     *
     * <p>Incluye información del contenido comprado y obtiene el nombre del artista
     * desde el microservicio de usuarios.</p>
     *
     * @param compra entidad a convertir
     * @return DTO de la compra
     */
    private CompraDTO convertirADTO(Compra compra) {
        CompraDTO dto = CompraDTO.builder()
                .idCompra(compra.getIdCompra())
                .idUsuario(compra.getIdUsuario())
                .tipoContenido(compra.getTipoContenido().name())
                .precioPagado(compra.getPrecioPagado())
                .fechaCompra(compra.getFechaCompra())
                .idMetodoPago(compra.getIdMetodoPago())
                .idTransaccion(compra.getIdTransaccion())
                .build();

        if (compra.getCancion() != null) {
            dto.setCancion(cancionMapper.toDTO(compra.getCancion()));

            DatosArtista datos = obtenerDatosArtista(compra.getCancion().getIdArtista());

            dto.setNombreArtista(datos.nombre);
            dto.setSlugArtista(datos.slug);
        }

        if (compra.getAlbum() != null) {
            dto.setAlbum(convertirAlbumADTO(compra.getAlbum()));

            DatosArtista datos = obtenerDatosArtista(compra.getAlbum().getIdArtista());

            dto.setNombreArtista(datos.nombre);
            dto.setSlugArtista(datos.slug);
        }

        return dto;
    }

    /**
     * Obtiene el nombre artístico desde el microservicio de usuarios.
     *
     * @param idArtista identificador del artista
     * @return nombre completo del artista o "Artista Desconocido" si falla la consulta
     */
    private DatosArtista obtenerDatosArtista(Long idArtista) {
        try {
            Map<String, Object> datosUsuario = usuariosClient.obtenerDatosUsuario(idArtista, "ARTISTA");

            if (datosUsuario != null) {
                DatosArtista datos = new DatosArtista();
                datos.nombre = (String) datosUsuario.get("nombreCompleto");
                datos.slug = (String) datosUsuario.get("slug");
                return datos;
            }

        } catch (Exception e) {
            log.warn("⚠️ Error al obtener datos del artista {}: {}", idArtista, e.getMessage());
        }

        DatosArtista fallback = new DatosArtista();
        fallback.nombre = "Artista Desconocido";
        fallback.slug = null;
        return fallback;
    }

    /**
     * Convierte una entidad Album a su representación DTO.
     *
     * @param album entidad a convertir
     * @return DTO del álbum
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