package com.ondra.contenidos.mappers;

import com.ondra.contenidos.dto.AlbumResumenConPistaDTO;
import com.ondra.contenidos.dto.CancionDTO;
import com.ondra.contenidos.dto.CancionDetalleDTO;
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

class CancionMapperTest {

    private CancionMapper cancionMapper;
    private ValoracionRepository valoracionRepository;
    private ComentarioRepository comentarioRepository;

    @BeforeEach
    void setUp() {
        valoracionRepository = Mockito.mock(ValoracionRepository.class);
        comentarioRepository = Mockito.mock(ComentarioRepository.class);
        cancionMapper = new CancionMapper(valoracionRepository, comentarioRepository);
    }

    @Test
    void toDTOList_returnsEmptyList_whenInputIsNull() {
        List<CancionDTO> result = cancionMapper.toDTOList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toDTOList_returnsList_whenInputHasCanciones() {
        Cancion cancion = Cancion.builder()
                .idCancion(1L)
                .tituloCancion("Cancion Test")
                .idArtista(1L)
                .genero(GeneroMusical.POP)
                .precioCancion(1.99)
                .duracionSegundos(200)
                .urlPortada("urlPortada")
                .urlAudio("urlAudio")
                .reproducciones(100L)
                .build();
        Mockito.when(valoracionRepository.calcularPromedioCancion(1L)).thenReturn(4.0);
        Mockito.when(comentarioRepository.countByCancion(1L)).thenReturn(2L);

        List<CancionDTO> result = cancionMapper.toDTOList(List.of(cancion));
        assertEquals(1, result.size());
        assertEquals("Cancion Test", result.get(0).getTituloCancion());
    }

    @Test
    void toDetalleDTO_returnsDTO_withAlbumes() {
        Album album = Album.builder()
                .idAlbum(1L)
                .tituloAlbum("Album Test")
                .urlPortada("urlPortada")
                .build();

        AlbumCancion albumCancion = AlbumCancion.builder()
                .album(album)
                .numeroPista(2)
                .build();

        Cancion cancion = Cancion.builder()
                .idCancion(1L)
                .tituloCancion("Cancion Detalle")
                .idArtista(1L)
                .genero(GeneroMusical.POP)
                .precioCancion(1.99)
                .duracionSegundos(200)
                .urlPortada("urlPortada")
                .urlAudio("urlAudio")
                .reproducciones(100L)
                .albumCanciones(List.of(albumCancion))
                .build();

        Mockito.when(valoracionRepository.calcularPromedioCancion(1L)).thenReturn(4.0);
        Mockito.when(comentarioRepository.countByCancion(1L)).thenReturn(2L);

        CancionDetalleDTO dto = cancionMapper.toDetalleDTO(cancion);
        assertNotNull(dto);
        assertEquals("Cancion Detalle", dto.getTituloCancion());
        assertNotNull(dto.getAlbumes());
        assertEquals(1, dto.getAlbumes().size());
        AlbumResumenConPistaDTO albumResumen = dto.getAlbumes().get(0);
        assertEquals("Album Test", albumResumen.getTituloAlbum());
        assertEquals(2, albumResumen.getNumeroPista());
    }
}
