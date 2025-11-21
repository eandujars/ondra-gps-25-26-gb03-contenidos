package com.ondra.contenidos.services;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.mappers.CancionMapper;
import com.ondra.contenidos.models.dao.Album;
import com.ondra.contenidos.models.dao.Cancion;
import com.ondra.contenidos.models.dao.Favorito;
import com.ondra.contenidos.models.dao.Favorito.TipoContenido;
import com.ondra.contenidos.repositories.AlbumRepository;
import com.ondra.contenidos.repositories.CancionRepository;
import com.ondra.contenidos.repositories.FavoritoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para la gestión de favoritos de usuarios.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final CancionRepository cancionRepository;
    private final AlbumRepository albumRepository;
    private final CancionMapper cancionMapper;

    /**
     * Agregar una canción o álbum a favoritos
     */
    @Transactional
    public FavoritoDTO agregarFavorito(Long idUsuario, AgregarFavoritoDTO dto) {
        log.debug("➕ Agregando favorito - Usuario: {}, Tipo: {}", idUsuario, dto.getTipoContenido());

        TipoContenido tipo;
        try {
            tipo = TipoContenido.valueOf(dto.getTipoContenido().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de contenido inválido: " + dto.getTipoContenido());
        }

        Favorito favorito = Favorito.builder()
                .idUsuario(idUsuario)
                .tipoContenido(tipo)
                .build();

        if (tipo == TipoContenido.CANCION) {
            if (dto.getIdCancion() == null) {
                throw new IllegalArgumentException("ID de canción es requerido para favoritos de tipo CANCION");
            }

            // Verificar si ya existe
            if (favoritoRepository.existsByUsuarioAndCancion(idUsuario, dto.getIdCancion())) {
                throw new FavoritoYaExisteException("La canción ya está en favoritos");
            }

            Cancion cancion = cancionRepository.findById(dto.getIdCancion())
                    .orElseThrow(() -> new CancionNotFoundException(dto.getIdCancion()));

            favorito.setCancion(cancion);

        } else if (tipo == TipoContenido.ALBUM) {
            if (dto.getIdAlbum() == null) {
                throw new IllegalArgumentException("ID de álbum es requerido para favoritos de tipo ALBUM");
            }

            // Verificar si ya existe
            if (favoritoRepository.existsByUsuarioAndAlbum(idUsuario, dto.getIdAlbum())) {
                throw new FavoritoYaExisteException("El álbum ya está en favoritos");
            }

            Album album = albumRepository.findById(dto.getIdAlbum())
                    .orElseThrow(() -> new AlbumNotFoundException(dto.getIdAlbum()));

            favorito.setAlbum(album);
        }

        Favorito favoritoGuardado = favoritoRepository.save(favorito);
        log.info("✅ Favorito agregado - ID: {}", favoritoGuardado.getIdFavorito());

        return convertirADTO(favoritoGuardado);
    }

    /**
     * Listar favoritos de un usuario con paginación
     */
    @Transactional(readOnly = true)
    public FavoritosPaginadosDTO listarFavoritos(Long idUsuario, String tipoContenido, Integer pagina, Integer limite) {
        log.debug("📋 Listando favoritos - Usuario: {}, Tipo: {}, Página: {}", idUsuario, tipoContenido, pagina);

        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite, Sort.by(Sort.Direction.DESC, "fechaAgregado"));

        Page<Favorito> paginaFavoritos;

        if (tipoContenido != null && !tipoContenido.isBlank()) {
            try {
                TipoContenido tipo = TipoContenido.valueOf(tipoContenido.toUpperCase());

                if (tipo == TipoContenido.CANCION) {
                    paginaFavoritos = favoritoRepository.findCancionesFavoritasByUsuario(idUsuario, pageable);
                } else {
                    paginaFavoritos = favoritoRepository.findAlbumesFavoritosByUsuario(idUsuario, pageable);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Tipo de contenido inválido: " + tipoContenido);
            }
        } else {
            paginaFavoritos = favoritoRepository.findByIdUsuario(idUsuario, pageable);
        }

        return FavoritosPaginadosDTO.builder()
                .favoritos(paginaFavoritos.getContent().stream()
                        .map(this::convertirADTO)
                        .toList())
                .paginaActual(paginaFavoritos.getNumber() + 1)
                .totalPaginas(paginaFavoritos.getTotalPages())
                .totalElementos(paginaFavoritos.getTotalElements())
                .elementosPorPagina(paginaFavoritos.getSize())
                .build();
    }

    /**
     * Eliminar una canción de favoritos
     */
    @Transactional
    public void eliminarFavoritoCancion(Long idUsuario, Long idCancion) {
        log.debug("🗑️ Eliminando canción de favoritos - Usuario: {}, Canción: {}", idUsuario, idCancion);

        Favorito favorito = favoritoRepository.findByUsuarioAndCancion(idUsuario, idCancion)
                .orElseThrow(() -> new FavoritoNotFoundException("La canción no está en favoritos"));

        favoritoRepository.delete(favorito);
        log.info("✅ Canción eliminada de favoritos");
    }

    /**
     * Eliminar un álbum de favoritos
     */
    @Transactional
    public void eliminarFavoritoAlbum(Long idUsuario, Long idAlbum) {
        log.debug("🗑️ Eliminando álbum de favoritos - Usuario: {}, Álbum: {}", idUsuario, idAlbum);

        Favorito favorito = favoritoRepository.findByUsuarioAndAlbum(idUsuario, idAlbum)
                .orElseThrow(() -> new FavoritoNotFoundException("El álbum no está en favoritos"));

        favoritoRepository.delete(favorito);
        log.info("✅ Álbum eliminado de favoritos");
    }

    /**
     * Verificar si una canción está en favoritos
     */
    @Transactional(readOnly = true)
    public boolean esCancionFavorita(Long idUsuario, Long idCancion) {
        return favoritoRepository.existsByUsuarioAndCancion(idUsuario, idCancion);
    }

    /**
     * Verificar si un álbum está en favoritos
     */
    @Transactional(readOnly = true)
    public boolean esAlbumFavorito(Long idUsuario, Long idAlbum) {
        return favoritoRepository.existsByUsuarioAndAlbum(idUsuario, idAlbum);
    }

    /**
     * Eliminar todos los favoritos de un usuario
     */
    @Transactional
    public void eliminarTodosLosFavoritos(Long idUsuario) {
        log.debug("🗑️ Eliminando todos los favoritos - Usuario: {}", idUsuario);
        favoritoRepository.deleteByIdUsuario(idUsuario);
        log.info("✅ Todos los favoritos del usuario eliminados");
    }

    /**
     * Convertir entidad Favorito a DTO
     */
    private FavoritoDTO convertirADTO(Favorito favorito) {
        FavoritoDTO dto = FavoritoDTO.builder()
                .idFavorito(favorito.getIdFavorito())
                .idUsuario(favorito.getIdUsuario())
                .tipoContenido(favorito.getTipoContenido().name())
                .fechaAgregado(favorito.getFechaAgregado())
                .build();

        if (favorito.getCancion() != null) {
            dto.setCancion(cancionMapper.toDTO(favorito.getCancion()));
        }

        if (favorito.getAlbum() != null) {
            dto.setAlbum(convertirAlbumADTO(favorito.getAlbum()));
        }

        return dto;
    }

    /**
     * Convertir entidad Album a AlbumDTO (simplificado)
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