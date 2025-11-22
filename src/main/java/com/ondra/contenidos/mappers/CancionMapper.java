package com.ondra.contenidos.mappers;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.models.dao.AlbumCancion;
import com.ondra.contenidos.models.dao.Cancion;
import com.ondra.contenidos.models.enums.GeneroMusical;
import com.ondra.contenidos.repositories.ComentarioRepository;
import com.ondra.contenidos.repositories.ValoracionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversión entre entidades Cancion y sus DTOs.
 *
 * <p>Gestiona la transformación bidireccional de datos de canciones,
 * incluyendo el cálculo de estadísticas agregadas y relaciones con álbumes.</p>
 */
@Component
@RequiredArgsConstructor
public class CancionMapper {

    private final ValoracionRepository valoracionRepository;
    private final ComentarioRepository comentarioRepository;

    /**
     * Convierte una entidad Cancion a CancionDTO.
     *
     * @param cancion entidad a convertir
     * @return DTO con información básica de la canción
     */
    public CancionDTO toDTO(Cancion cancion) {
        if (cancion == null) {
            return null;
        }

        Double valoracionMedia = valoracionRepository.calcularPromedioCancion(cancion.getIdCancion());
        Long totalComentarios = comentarioRepository.countByCancion(cancion.getIdCancion());

        AlbumResumenDTO albumResumen = null;
        if (cancion.getAlbumCanciones() != null && !cancion.getAlbumCanciones().isEmpty()) {
            AlbumCancion primerAlbum = cancion.getAlbumCanciones().get(0);
            albumResumen = AlbumResumenDTO.builder()
                    .idAlbum(primerAlbum.getAlbum().getIdAlbum())
                    .tituloAlbum(primerAlbum.getAlbum().getTituloAlbum())
                    .urlPortada(primerAlbum.getAlbum().getUrlPortada())
                    .build();
        }

        return CancionDTO.builder()
                .idCancion(cancion.getIdCancion())
                .tituloCancion(cancion.getTituloCancion())
                .idArtista(cancion.getIdArtista())
                .genero(cancion.getGenero().getNombre())
                .precioCancion(cancion.getPrecioCancion())
                .duracionSegundos(cancion.getDuracionSegundos())
                .urlPortada(cancion.getUrlPortada())
                .urlAudio(cancion.getUrlAudio())
                .reproducciones(cancion.getReproducciones())
                .valoracionMedia(valoracionMedia != null ? Math.round(valoracionMedia * 100.0) / 100.0 : null)
                .totalComentarios(totalComentarios)
                .fechaPublicacion(cancion.getFechaPublicacion())
                .descripcion(cancion.getDescripcion())
                .album(albumResumen)
                .build();
    }

    /**
     * Convierte una entidad Cancion a CancionDetalleDTO.
     * Incluye la lista completa de álbumes que contienen la canción.
     *
     * @param cancion entidad a convertir
     * @return DTO con información detallada de la canción
     */
    public CancionDetalleDTO toDetalleDTO(Cancion cancion) {
        if (cancion == null) {
            return null;
        }

        Double valoracionMedia = valoracionRepository.calcularPromedioCancion(cancion.getIdCancion());
        Long totalComentarios = comentarioRepository.countByCancion(cancion.getIdCancion());

        List<AlbumResumenConPistaDTO> albumes = cancion.getAlbumCanciones().stream()
                .map(ac -> AlbumResumenConPistaDTO.builder()
                        .idAlbum(ac.getAlbum().getIdAlbum())
                        .tituloAlbum(ac.getAlbum().getTituloAlbum())
                        .urlPortada(ac.getAlbum().getUrlPortada())
                        .numeroPista(ac.getNumeroPista())
                        .build())
                .collect(Collectors.toList());

        return CancionDetalleDTO.builder()
                .idCancion(cancion.getIdCancion())
                .tituloCancion(cancion.getTituloCancion())
                .idArtista(cancion.getIdArtista())
                .genero(cancion.getGenero().getNombre())
                .precioCancion(cancion.getPrecioCancion())
                .duracionSegundos(cancion.getDuracionSegundos())
                .urlPortada(cancion.getUrlPortada())
                .urlAudio(cancion.getUrlAudio())
                .reproducciones(cancion.getReproducciones())
                .valoracionMedia(valoracionMedia != null ? Math.round(valoracionMedia * 100.0) / 100.0 : null)
                .totalComentarios(totalComentarios)
                .fechaPublicacion(cancion.getFechaPublicacion())
                .descripcion(cancion.getDescripcion())
                .albumes(albumes)
                .build();
    }

    /**
     * Convierte un CrearCancionDTO a una entidad Cancion.
     *
     * @param dto datos de la canción a crear
     * @param idArtista identificador del artista propietario extraído del JWT
     * @return entidad Cancion lista para persistir
     * @throws IllegalArgumentException si el idGenero no es válido
     */
    public Cancion toEntity(CrearCancionDTO dto, Long idArtista) {
        if (dto == null) {
            return null;
        }

        GeneroMusical genero = GeneroMusical.fromId(dto.getIdGenero());

        return Cancion.builder()
                .tituloCancion(dto.getTituloCancion())
                .idArtista(idArtista)
                .genero(genero)
                .precioCancion(dto.getPrecioCancion())
                .duracionSegundos(dto.getDuracionSegundos())
                .urlPortada(dto.getUrlPortada())
                .urlAudio(dto.getUrlAudio())
                .descripcion(dto.getDescripcion())
                .reproducciones(0L)
                .build();
    }

    /**
     * Actualiza una entidad Cancion existente con los datos de EditarCancionDTO.
     * Solo actualiza los campos que no son null en el DTO.
     *
     * @param cancion entidad existente a actualizar
     * @param dto datos de actualización
     * @throws IllegalArgumentException si el idGenero no es válido
     */
    public void updateEntity(Cancion cancion, EditarCancionDTO dto) {
        if (cancion == null || dto == null) {
            return;
        }

        if (dto.getTituloCancion() != null) {
            cancion.setTituloCancion(dto.getTituloCancion());
        }

        if (dto.getIdGenero() != null) {
            GeneroMusical genero = GeneroMusical.fromId(dto.getIdGenero());
            cancion.setGenero(genero);
        }

        if (dto.getPrecioCancion() != null) {
            cancion.setPrecioCancion(dto.getPrecioCancion());
        }

        if (dto.getUrlPortada() != null) {
            cancion.setUrlPortada(dto.getUrlPortada());
        }

        if (dto.getDescripcion() != null) {
            cancion.setDescripcion(dto.getDescripcion());
        }
    }

    /**
     * Convierte una lista de entidades Cancion a una lista de DTOs.
     *
     * @param canciones lista de entidades
     * @return lista de DTOs
     */
    public List<CancionDTO> toDTOList(List<Cancion> canciones) {
        if (canciones == null) {
            return List.of();
        }

        return canciones.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}