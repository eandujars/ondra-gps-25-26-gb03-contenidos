package com.ondra.contenidos.mappers;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.models.dao.Album;
import com.ondra.contenidos.models.dao.AlbumCancion;
import com.ondra.contenidos.models.enums.GeneroMusical;
import com.ondra.contenidos.repositories.ComentarioRepository;
import com.ondra.contenidos.repositories.ValoracionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversión entre entidades Album y sus DTOs.
 *
 * <p>Gestiona la transformación bidireccional de datos de álbumes,
 * incluyendo el cálculo de estadísticas agregadas y relaciones con canciones.</p>
 */
@Component
@RequiredArgsConstructor
public class AlbumMapper {

    private final ValoracionRepository valoracionRepository;
    private final ComentarioRepository comentarioRepository;

    /**
     * Convierte una entidad Album a AlbumDTO.
     *
     * @param album entidad a convertir
     * @return DTO con información básica del álbum
     */
    public AlbumDTO toDTO(Album album) {
        if (album == null) {
            return null;
        }

        Double valoracionMedia = valoracionRepository.calcularPromedioAlbum(album.getIdAlbum());
        Long totalComentarios = comentarioRepository.countByAlbum(album.getIdAlbum());

        return AlbumDTO.builder()
                .idAlbum(album.getIdAlbum())
                .tituloAlbum(album.getTituloAlbum())
                .idArtista(album.getIdArtista())
                .genero(album.getGenero().getNombre())
                .precioAlbum(album.getPrecioAlbum())
                .urlPortada(album.getUrlPortada())
                .totalCanciones(album.getTotalCanciones())
                .duracionTotalSegundos(album.getDuracionTotalSegundos())
                .totalPlayCount(album.getTotalPlayCount())
                .valoracionMedia(valoracionMedia != null ? Math.round(valoracionMedia * 100.0) / 100.0 : null)
                .totalComentarios(totalComentarios)
                .fechaPublicacion(album.getFechaPublicacion())
                .descripcion(album.getDescripcion())
                .build();
    }

    /**
     * Convierte una entidad Album a AlbumDetalleDTO.
     * Incluye la lista completa de canciones ordenadas por número de pista.
     *
     * @param album entidad a convertir
     * @return DTO con información detallada del álbum
     */
    public AlbumDetalleDTO toDetalleDTO(Album album) {
        if (album == null) {
            return null;
        }

        Double valoracionMedia = valoracionRepository.calcularPromedioAlbum(album.getIdAlbum());
        Long totalComentarios = comentarioRepository.countByAlbum(album.getIdAlbum());

        List<CancionAlbumDTO> trackList = album.getAlbumCanciones().stream()
                .map(ac -> CancionAlbumDTO.builder()
                        .idCancion(ac.getCancion().getIdCancion())
                        .tituloCancion(ac.getCancion().getTituloCancion())
                        .duracionSegundos(ac.getCancion().getDuracionSegundos())
                        .trackNumber(ac.getNumeroPista())
                        .urlPortada(ac.getCancion().getUrlPortada())
                        .urlAudio(ac.getCancion().getUrlAudio())
                        .precioCancion(ac.getCancion().getPrecioCancion())
                        .reproducciones(ac.getCancion().getReproducciones())
                        .build())
                .collect(Collectors.toList());

        return AlbumDetalleDTO.builder()
                .idAlbum(album.getIdAlbum())
                .tituloAlbum(album.getTituloAlbum())
                .idArtista(album.getIdArtista())
                .genero(album.getGenero().getNombre())
                .precioAlbum(album.getPrecioAlbum())
                .urlPortada(album.getUrlPortada())
                .totalCanciones(album.getTotalCanciones())
                .duracionTotalSegundos(album.getDuracionTotalSegundos())
                .totalPlayCount(album.getTotalPlayCount())
                .valoracionMedia(valoracionMedia != null ? Math.round(valoracionMedia * 100.0) / 100.0 : null)
                .totalComentarios(totalComentarios)
                .fechaPublicacion(album.getFechaPublicacion())
                .descripcion(album.getDescripcion())
                .trackList(trackList)
                .build();
    }

    /**
     * Convierte un CrearAlbumDTO a una entidad Album.
     *
     * @param dto datos del álbum a crear
     * @param idArtista identificador del artista propietario extraído del JWT
     * @return entidad Album lista para persistir
     * @throws IllegalArgumentException si el idGenero no es válido
     */
    public Album toEntity(CrearAlbumDTO dto, Long idArtista) {
        if (dto == null) {
            return null;
        }

        GeneroMusical genero = GeneroMusical.fromId(dto.getIdGenero());

        return Album.builder()
                .tituloAlbum(dto.getTituloAlbum())
                .idArtista(idArtista)
                .genero(genero)
                .precioAlbum(dto.getPrecioAlbum())
                .urlPortada(dto.getUrlPortada())
                .descripcion(dto.getDescripcion())
                .build();
    }

    /**
     * Actualiza una entidad Album existente con los datos de EditarAlbumDTO.
     * Solo actualiza los campos que no son null en el DTO.
     *
     * @param album entidad existente a actualizar
     * @param dto datos de actualización
     * @throws IllegalArgumentException si el idGenero no es válido
     */
    public void updateEntity(Album album, EditarAlbumDTO dto) {
        if (album == null || dto == null) {
            return;
        }

        if (dto.getTituloAlbum() != null) {
            album.setTituloAlbum(dto.getTituloAlbum());
        }

        if (dto.getIdGenero() != null) {
            GeneroMusical genero = GeneroMusical.fromId(dto.getIdGenero());
            album.setGenero(genero);
        }

        if (dto.getPrecioAlbum() != null) {
            album.setPrecioAlbum(dto.getPrecioAlbum());
        }

        if (dto.getUrlPortada() != null) {
            album.setUrlPortada(dto.getUrlPortada());
        }

        if (dto.getDescripcion() != null) {
            album.setDescripcion(dto.getDescripcion());
        }
    }

    /**
     * Convierte una lista de entidades Album a una lista de DTOs.
     *
     * @param albumes lista de entidades
     * @return lista de DTOs
     */
    public List<AlbumDTO> toDTOList(List<Album> albumes) {
        if (albumes == null) {
            return List.of();
        }

        return albumes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un AlbumCancion a CancionAlbumDTO.
     *
     * @param albumCancion relación álbum-canción
     * @return DTO de la canción con información del álbum
     */
    public CancionAlbumDTO toCancionAlbumDTO(AlbumCancion albumCancion) {
        if (albumCancion == null) {
            return null;
        }

        return CancionAlbumDTO.builder()
                .idCancion(albumCancion.getCancion().getIdCancion())
                .tituloCancion(albumCancion.getCancion().getTituloCancion())
                .duracionSegundos(albumCancion.getCancion().getDuracionSegundos())
                .trackNumber(albumCancion.getNumeroPista())
                .urlPortada(albumCancion.getCancion().getUrlPortada())
                .urlAudio(albumCancion.getCancion().getUrlAudio())
                .precioCancion(albumCancion.getCancion().getPrecioCancion())
                .reproducciones(albumCancion.getCancion().getReproducciones())
                .build();
    }
}