package com.ondra.contenidos.services;

import com.ondra.contenidos.clients.UsuariosClient;
import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.mappers.CancionMapper;
import com.ondra.contenidos.models.dao.Album;
import com.ondra.contenidos.models.dao.Cancion;
import com.ondra.contenidos.models.dao.Favorito;
import com.ondra.contenidos.models.enums.TipoContenido;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestión de favoritos de usuarios.
 *
 * <p>Proporciona operaciones para añadir, listar y eliminar canciones y álbumes
 * marcados como favoritos, con validación de duplicados y verificación de contenido.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final CancionRepository cancionRepository;
    private final AlbumRepository albumRepository;
    private final CancionMapper cancionMapper;
    private final UsuariosClient usuariosClient;

    private static class DatosArtista {
        String nombre;
        String slug;
    }

    /**
     * Añade una canción o álbum a favoritos.
     *
     * <p>Valida que el contenido no esté previamente marcado como favorito
     * y que exista en el sistema. Si se añade un álbum, también añade automáticamente
     * todas sus canciones a favoritos.</p>
     *
     * @param idUsuario identificador del usuario
     * @param dto datos del contenido a añadir
     * @return favorito creado
     * @throws IllegalArgumentException si el tipo de contenido es inválido o faltan datos requeridos
     * @throws FavoritoYaExisteException si el contenido ya está en favoritos
     * @throws CancionNotFoundException si la canción no existe
     * @throws AlbumNotFoundException si el álbum no existe
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

        if (tipo == TipoContenido.CANCIÓN) {
            if (dto.getIdCancion() == null) {
                throw new IllegalArgumentException("ID de canción es requerido para favoritos de tipo CANCION");
            }

            if (favoritoRepository.existsByUsuarioAndCancion(idUsuario, dto.getIdCancion())) {
                throw new FavoritoYaExisteException("La canción ya está en favoritos");
            }

            Cancion cancion = cancionRepository.findById(dto.getIdCancion())
                    .orElseThrow(() -> new CancionNotFoundException(dto.getIdCancion()));

            favorito.setCancion(cancion);

        } else if (tipo == TipoContenido.ÁLBUM) {
            if (dto.getIdAlbum() == null) {
                throw new IllegalArgumentException("ID de álbum es requerido para favoritos de tipo ÁLBUM");
            }

            if (favoritoRepository.existsByUsuarioAndAlbum(idUsuario, dto.getIdAlbum())) {
                throw new FavoritoYaExisteException("El álbum ya está en favoritos");
            }

            Album album = albumRepository.findById(dto.getIdAlbum())
                    .orElseThrow(() -> new AlbumNotFoundException(dto.getIdAlbum()));

            favorito.setAlbum(album);

            // Añadir todas las canciones del álbum a favoritos
            agregarCancionesDeAlbumAFavoritos(idUsuario, album);
        }

        Favorito favoritoGuardado = favoritoRepository.save(favorito);

        log.info("✅ Favorito agregado - ID: {}", favoritoGuardado.getIdFavorito());
        return convertirADTO(favoritoGuardado);
    }

    /**
     * Añade todas las canciones de un álbum a favoritos del usuario.
     *
     * <p>Omite las canciones que ya están en favoritos.</p>
     *
     * @param idUsuario identificador del usuario
     * @param album álbum cuyas canciones se añadirán
     */
    private void agregarCancionesDeAlbumAFavoritos(Long idUsuario, Album album) {
        log.debug("🎵 Añadiendo canciones del álbum {} a favoritos del usuario {}",
                album.getIdAlbum(), idUsuario);

        List<Favorito> favoritosNuevos = new ArrayList<>();

        album.getAlbumCanciones().forEach(albumCancion -> {
            Cancion cancion = albumCancion.getCancion();

            // Solo añadir si no está ya en favoritos
            if (!favoritoRepository.existsByUsuarioAndCancion(idUsuario, cancion.getIdCancion())) {
                Favorito favoritoCancion = Favorito.builder()
                        .idUsuario(idUsuario)
                        .tipoContenido(TipoContenido.CANCIÓN)
                        .cancion(cancion)
                        .build();

                favoritosNuevos.add(favoritoCancion);
            }
        });

        if (!favoritosNuevos.isEmpty()) {
            favoritoRepository.saveAll(favoritosNuevos);
            log.info("✅ {} canciones del álbum añadidas a favoritos", favoritosNuevos.size());
        }
    }

    /**
     * Lista los favoritos de un usuario con paginación y filtro opcional por tipo.
     *
     * <p>Ordena los favoritos por fecha de agregado descendente. Si se especifica
     * un tipo de contenido, filtra solo canciones o álbumes.</p>
     *
     * @param idUsuario identificador del usuario
     * @param tipoContenido tipo de contenido a filtrar (CANCION o ÁLBUM), opcional
     * @param pagina número de página (base 1)
     * @param limite cantidad de elementos por página
     * @return favoritos paginados con metadatos
     * @throws IllegalArgumentException si el tipo de contenido es inválido
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
                if (tipo == TipoContenido.CANCIÓN) {
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
     * Elimina una canción de favoritos.
     *
     * @param idUsuario identificador del usuario
     * @param idCancion identificador de la canción
     * @throws FavoritoNotFoundException si la canción no está en favoritos
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
     * Elimina un álbum de favoritos.
     *
     * <p>También elimina automáticamente todas las canciones del álbum de favoritos.</p>
     *
     * @param idUsuario identificador del usuario
     * @param idAlbum identificador del álbum
     * @throws FavoritoNotFoundException si el álbum no está en favoritos
     */
    @Transactional
    public void eliminarFavoritoAlbum(Long idUsuario, Long idAlbum) {
        log.debug("🗑️ Eliminando álbum de favoritos - Usuario: {}, Álbum: {}", idUsuario, idAlbum);

        Favorito favorito = favoritoRepository.findByUsuarioAndAlbum(idUsuario, idAlbum)
                .orElseThrow(() -> new FavoritoNotFoundException("El álbum no está en favoritos"));

        // Eliminar todas las canciones del álbum de favoritos
        eliminarCancionesDeAlbumDeFavoritos(idUsuario, favorito.getAlbum());

        favoritoRepository.delete(favorito);
        log.info("✅ Álbum eliminado de favoritos");
    }

    /**
     * Elimina todas las canciones de un álbum de favoritos del usuario.
     *
     * @param idUsuario identificador del usuario
     * @param album álbum cuyas canciones se eliminarán
     */
    private void eliminarCancionesDeAlbumDeFavoritos(Long idUsuario, Album album) {
        log.debug("🎵 Eliminando canciones del álbum {} de favoritos del usuario {}",
                album.getIdAlbum(), idUsuario);

        List<Long> idsCanciones = album.getAlbumCanciones().stream()
                .map(albumCancion -> albumCancion.getCancion().getIdCancion())
                .toList();

        int cancionesEliminadas = 0;
        for (Long idCancion : idsCanciones) {
            favoritoRepository.findByUsuarioAndCancion(idUsuario, idCancion)
                    .ifPresent(fav -> {
                        favoritoRepository.delete(fav);
                    });
            cancionesEliminadas++;
        }

        log.info("✅ {} canciones del álbum eliminadas de favoritos", cancionesEliminadas);
    }

    /**
     * Verifica si una canción está marcada como favorita.
     *
     * @param idUsuario identificador del usuario
     * @param idCancion identificador de la canción
     * @return true si la canción está en favoritos
     */
    @Transactional(readOnly = true)
    public boolean esCancionFavorita(Long idUsuario, Long idCancion) {
        return favoritoRepository.existsByUsuarioAndCancion(idUsuario, idCancion);
    }

    /**
     * Verifica si un álbum está marcado como favorito.
     *
     * @param idUsuario identificador del usuario
     * @param idAlbum identificador del álbum
     * @return true si el álbum está en favoritos
     */
    @Transactional(readOnly = true)
    public boolean esAlbumFavorito(Long idUsuario, Long idAlbum) {
        return favoritoRepository.existsByUsuarioAndAlbum(idUsuario, idAlbum);
    }

    /**
     * Elimina todos los favoritos de un usuario.
     *
     * <p>Utilizado cuando se elimina un usuario del sistema.</p>
     *
     * @param idUsuario identificador del usuario
     */
    @Transactional
    public void eliminarTodosLosFavoritos(Long idUsuario) {
        log.debug("🗑️ Eliminando todos los favoritos - Usuario: {}", idUsuario);
        favoritoRepository.deleteByIdUsuario(idUsuario);
        log.info("✅ Todos los favoritos del usuario eliminados");
    }

    /**
     * Convierte una entidad Favorito a su representación DTO.
     *
     * <p>Incluye información del contenido y obtiene el nombre del artista
     * desde el microservicio de usuarios.</p>
     *
     * @param favorito entidad a convertir
     * @return DTO del favorito
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
            DatosArtista datos = obtenerDatosArtista(favorito.getCancion().getIdArtista());
            dto.setNombreArtista(datos.nombre);
            dto.setSlugArtista(datos.slug);
        }

        if (favorito.getAlbum() != null) {
            dto.setAlbum(convertirAlbumADTO(favorito.getAlbum()));
            DatosArtista datos = obtenerDatosArtista(favorito.getAlbum().getIdArtista());
            dto.setNombreArtista(datos.nombre);
            dto.setSlugArtista(datos.slug);
        }

        return dto;
    }

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