package com.ondra.contenidos.services;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;

    @Value("${microservices.usuarios.url:http://localhost:8080}")
    private String usuariosServiceUrl;

    /**
     * Añade una canción o álbum a favoritos.
     *
     * <p>Valida que el contenido no esté previamente marcado como favorito
     * y que exista en el sistema.</p>
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

        if (tipo == TipoContenido.CANCION) {
            if (dto.getIdCancion() == null) {
                throw new IllegalArgumentException("ID de canción es requerido para favoritos de tipo CANCION");
            }

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
     * Lista los favoritos de un usuario con paginación y filtro opcional por tipo.
     *
     * <p>Ordena los favoritos por fecha de agregado descendente. Si se especifica
     * un tipo de contenido, filtra solo canciones o álbumes.</p>
     *
     * @param idUsuario identificador del usuario
     * @param tipoContenido tipo de contenido a filtrar (CANCION o ALBUM), opcional
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
     * @param idUsuario identificador del usuario
     * @param idAlbum identificador del álbum
     * @throws FavoritoNotFoundException si el álbum no está en favoritos
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
            dto.setNombreArtista(obtenerNombreArtista(favorito.getCancion().getIdArtista()));
        }

        if (favorito.getAlbum() != null) {
            dto.setAlbum(convertirAlbumADTO(favorito.getAlbum()));
            dto.setNombreArtista(obtenerNombreArtista(favorito.getAlbum().getIdArtista()));
        }

        return dto;
    }

    /**
     * Obtiene el nombre artístico desde el microservicio de usuarios.
     *
     * @param idArtista identificador del artista
     * @return nombre completo del artista o "Artista Desconocido" si falla la consulta
     */
    private String obtenerNombreArtista(Long idArtista) {
        try {
            String url = usuariosServiceUrl
                    + "/usuarios/" + idArtista + "/nombre-completo?tipo=ARTISTA";

            log.debug("📞 Llamando a microservicio usuarios: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("nombreCompleto");
            }

            return "Artista Desconocido";

        } catch (Exception e) {
            log.warn("⚠️ Error al obtener nombre del artista {}: {}", idArtista, e.getMessage());
            return "Artista Desconocido";
        }
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