package com.ondra.contenidos.mappers;

import com.ondra.contenidos.dto.AlbumDTO;
import com.ondra.contenidos.dto.AlbumDetalleDTO;
import com.ondra.contenidos.dto.CancionAlbumDTO;
import com.ondra.contenidos.models.dao.Album;
import com.ondra.contenidos.models.dao.AlbumCancion;
import com.ondra.contenidos.models.dao.Cancion;
import com.ondra.contenidos.models.enums.GeneroMusical;
import com.ondra.contenidos.repositories.ComentarioRepository;
import com.ondra.contenidos.repositories.ValoracionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlbumMapperTest {

    private AlbumMapper albumMapper;
    private ValoracionRepository valoracionRepository;
    private ComentarioRepository comentarioRepository;

    @BeforeEach
    void setUp() {
        valoracionRepository = Mockito.mock(ValoracionRepository.class);
        comentarioRepository = Mockito.mock(ComentarioRepository.class);
        albumMapper = new AlbumMapper(valoracionRepository, comentarioRepository);
    }

    @Test
    void toDTOList_returnsEmptyList_whenInputIsNull() {
        List<AlbumDTO> result = albumMapper.toDTOList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toDTOList_returnsList_whenInputHasAlbums() {
        Album album = Album.builder()
                .idAlbum(1L)
                .tituloAlbum("Test Album")
                .idArtista(1L)
                .genero(GeneroMusical.POP)
                .precioAlbum(10.0)
                .build();
        Mockito.when(valoracionRepository.calcularPromedioAlbum(1L)).thenReturn(4.0);
        Mockito.when(comentarioRepository.countByAlbum(1L)).thenReturn(2L);

        List<AlbumDTO> result = albumMapper.toDTOList(List.of(album));
        assertEquals(1, result.size());
        assertEquals("Test Album", result.get(0).getTituloAlbum());
    }

    @Test
    void toDetalleDTO_returnsDTO_withTrackList() {
        Cancion cancion = Cancion.builder()
                .idCancion(1L)
                .tituloCancion("Cancion 1")
                .duracionSegundos(200)
                .urlPortada("urlPortada")
                .urlAudio("urlAudio")
                .precioCancion(1.99)
                .reproducciones(100L)
                .build();

        AlbumCancion albumCancion = AlbumCancion.builder()
                .cancion(cancion)
                .numeroPista(1)
                .build();

        Album album = Album.builder()
                .idAlbum(1L)
                .tituloAlbum("Test Album")
                .idArtista(1L)
                .genero(GeneroMusical.POP)
                .precioAlbum(10.0)
                .albumCanciones(List.of(albumCancion))
                .build();

        Mockito.when(valoracionRepository.calcularPromedioAlbum(1L)).thenReturn(4.0);
        Mockito.when(comentarioRepository.countByAlbum(1L)).thenReturn(2L);

        AlbumDetalleDTO dto = albumMapper.toDetalleDTO(album);
        assertNotNull(dto);
        assertEquals("Test Album", dto.getTituloAlbum());
        assertNotNull(dto.getTrackList());
        assertEquals(1, dto.getTrackList().size());
        CancionAlbumDTO track = dto.getTrackList().get(0);
        assertEquals("Cancion 1", track.getTituloCancion());
        assertEquals(1, track.getTrackNumber());
    }
}
