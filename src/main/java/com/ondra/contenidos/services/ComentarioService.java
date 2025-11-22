package com.ondra.contenidos.services;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.models.dao.Album;
import com.ondra.contenidos.models.dao.Cancion;
import com.ondra.contenidos.models.dao.Comentario;
import com.ondra.contenidos.models.enums.TipoContenido;
import com.ondra.contenidos.models.enums.TipoUsuario;
import com.ondra.contenidos.repositories.AlbumRepository;
import com.ondra.contenidos.repositories.CancionRepository;
import com.ondra.contenidos.repositories.ComentarioRepository;
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
 * Servicio para gestión de comentarios de usuarios y artistas.
 *
 * <p>Proporciona operaciones para crear, editar, listar y eliminar comentarios
 * sobre canciones y álbumes, con validación de permisos y consulta de datos
 * desde el microservicio de usuarios.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final CancionRepository cancionRepository;
    private final AlbumRepository albumRepository;
    private final RestTemplate restTemplate;

    @Value("${microservices.usuarios.url:http://localhost:8080}")
    private String usuariosServiceUrl;

    /**
     * Crea un nuevo comentario sobre un contenido musical.
     *
     * <p>Obtiene el nombre del usuario desde el microservicio de usuarios y
     * valida la existencia del contenido asociado.</p>
     *
     * @param idUsuario identificador del usuario que comenta
     * @param tipoUsuario tipo de usuario (USUARIO o ARTISTA)
     * @param dto datos del comentario a crear
     * @return comentario creado
     * @throws IllegalArgumentException si el tipo de contenido o usuario es inválido, o faltan datos requeridos
     * @throws CancionNotFoundException si la canción no existe
     * @throws AlbumNotFoundException si el álbum no existe
     */
    @Transactional
    public ComentarioDTO crearComentario(Long idUsuario, String tipoUsuario, CrearComentarioDTO dto) {
        log.debug("➕ Creando comentario - Usuario: {}, Tipo: {}", idUsuario, dto.getTipoContenido());

        TipoContenido tipo;
        try {
            tipo = TipoContenido.valueOf(dto.getTipoContenido().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de contenido inválido: " + dto.getTipoContenido());
        }

        TipoUsuario tipoUsuarioEnum;
        try {
            tipoUsuarioEnum = TipoUsuario.valueOf(tipoUsuario.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de usuario inválido: " + tipoUsuario);
        }

        String nombreUsuario = obtenerNombreUsuario(idUsuario, tipoUsuarioEnum);

        Comentario comentario = Comentario.builder()
                .idUsuario(idUsuario)
                .tipoUsuario(tipoUsuarioEnum)
                .nombreUsuario(nombreUsuario)
                .tipoContenido(tipo)
                .contenido(dto.getContenido())
                .build();

        if (tipo == TipoContenido.CANCION) {
            if (dto.getIdCancion() == null) {
                throw new IllegalArgumentException("ID de canción es requerido para comentarios de tipo CANCION");
            }

            Cancion cancion = cancionRepository.findById(dto.getIdCancion())
                    .orElseThrow(() -> new CancionNotFoundException(dto.getIdCancion()));

            comentario.setCancion(cancion);

        } else if (tipo == TipoContenido.ALBUM) {
            if (dto.getIdAlbum() == null) {
                throw new IllegalArgumentException("ID de álbum es requerido para comentarios de tipo ALBUM");
            }

            Album album = albumRepository.findById(dto.getIdAlbum())
                    .orElseThrow(() -> new AlbumNotFoundException(dto.getIdAlbum()));

            comentario.setAlbum(album);
        }

        Comentario comentarioGuardado = comentarioRepository.save(comentario);
        log.info("✅ Comentario creado - ID: {}", comentarioGuardado.getIdComentario());

        return convertirADTO(comentarioGuardado);
    }

    /**
     * Edita el contenido de un comentario existente.
     *
     * <p>Solo el autor del comentario puede editarlo.</p>
     *
     * @param idComentario identificador del comentario a editar
     * @param idUsuario identificador del usuario que solicita la edición
     * @param dto datos actualizados del comentario
     * @return comentario actualizado
     * @throws ComentarioNotFoundException si el comentario no existe
     * @throws AccesoDenegadoException si el usuario no es el autor
     */
    @Transactional
    public ComentarioDTO editarComentario(Long idComentario, Long idUsuario, EditarComentarioDTO dto) {
        log.debug("✏️ Editando comentario - ID: {}, Usuario: {}", idComentario, idUsuario);

        Comentario comentario = comentarioRepository.findById(idComentario)
                .orElseThrow(() -> new ComentarioNotFoundException(idComentario));

        if (!comentario.getIdUsuario().equals(idUsuario)) {
            throw new AccesoDenegadoException("No tienes permiso para editar este comentario");
        }

        comentario.setContenido(dto.getContenido());
        Comentario comentarioActualizado = comentarioRepository.save(comentario);

        log.info("✅ Comentario editado - ID: {}", idComentario);
        return convertirADTO(comentarioActualizado);
    }

    /**
     * Lista los comentarios de una canción con paginación.
     *
     * <p>Ordena los comentarios por fecha de publicación descendente.</p>
     *
     * @param idCancion identificador de la canción
     * @param pagina número de página (base 1)
     * @param limite cantidad de elementos por página
     * @return comentarios paginados con metadatos
     * @throws CancionNotFoundException si la canción no existe
     */
    @Transactional(readOnly = true)
    public ComentariosPaginadosDTO listarComentariosCancion(Long idCancion, Integer pagina, Integer limite) {
        log.debug("📋 Listando comentarios de canción - ID: {}", idCancion);

        if (!cancionRepository.existsById(idCancion)) {
            throw new CancionNotFoundException(idCancion);
        }

        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite, Sort.by(Sort.Direction.DESC, "fechaPublicacion"));
        Page<Comentario> paginaComentarios = comentarioRepository.findByCancion(idCancion, pageable);

        return ComentariosPaginadosDTO.builder()
                .comentarios(paginaComentarios.getContent().stream()
                        .map(this::convertirADTO)
                        .toList())
                .paginaActual(paginaComentarios.getNumber() + 1)
                .totalPaginas(paginaComentarios.getTotalPages())
                .totalElementos(paginaComentarios.getTotalElements())
                .elementosPorPagina(paginaComentarios.getSize())
                .build();
    }

    /**
     * Lista los comentarios de un álbum con paginación.
     *
     * <p>Ordena los comentarios por fecha de publicación descendente.</p>
     *
     * @param idAlbum identificador del álbum
     * @param pagina número de página (base 1)
     * @param limite cantidad de elementos por página
     * @return comentarios paginados con metadatos
     * @throws AlbumNotFoundException si el álbum no existe
     */
    @Transactional(readOnly = true)
    public ComentariosPaginadosDTO listarComentariosAlbum(Long idAlbum, Integer pagina, Integer limite) {
        log.debug("📋 Listando comentarios de álbum - ID: {}", idAlbum);

        if (!albumRepository.existsById(idAlbum)) {
            throw new AlbumNotFoundException(idAlbum);
        }

        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite, Sort.by(Sort.Direction.DESC, "fechaPublicacion"));
        Page<Comentario> paginaComentarios = comentarioRepository.findByAlbum(idAlbum, pageable);

        return ComentariosPaginadosDTO.builder()
                .comentarios(paginaComentarios.getContent().stream()
                        .map(this::convertirADTO)
                        .toList())
                .paginaActual(paginaComentarios.getNumber() + 1)
                .totalPaginas(paginaComentarios.getTotalPages())
                .totalElementos(paginaComentarios.getTotalElements())
                .elementosPorPagina(paginaComentarios.getSize())
                .build();
    }

    /**
     * Lista todos los comentarios de un usuario con paginación.
     *
     * <p>Ordena los comentarios por fecha de publicación descendente.</p>
     *
     * @param idUsuario identificador del usuario
     * @param pagina número de página (base 1)
     * @param limite cantidad de elementos por página
     * @return comentarios paginados con metadatos
     */
    @Transactional(readOnly = true)
    public ComentariosPaginadosDTO listarComentariosUsuario(Long idUsuario, Integer pagina, Integer limite) {
        log.debug("📋 Listando comentarios de usuario - ID: {}", idUsuario);

        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite, Sort.by(Sort.Direction.DESC, "fechaPublicacion"));
        Page<Comentario> paginaComentarios = comentarioRepository.findByIdUsuario(idUsuario, pageable);

        return ComentariosPaginadosDTO.builder()
                .comentarios(paginaComentarios.getContent().stream()
                        .map(this::convertirADTO)
                        .toList())
                .paginaActual(paginaComentarios.getNumber() + 1)
                .totalPaginas(paginaComentarios.getTotalPages())
                .totalElementos(paginaComentarios.getTotalElements())
                .elementosPorPagina(paginaComentarios.getSize())
                .build();
    }

    /**
     * Elimina un comentario existente.
     *
     * <p>Puede eliminarlo el autor del comentario o el propietario del contenido
     * (si es artista).</p>
     *
     * @param idComentario identificador del comentario a eliminar
     * @param idUsuario identificador del usuario que solicita la eliminación
     * @param tipoUsuario tipo de usuario que solicita la eliminación
     * @throws ComentarioNotFoundException si el comentario no existe
     * @throws AccesoDenegadoException si el usuario no tiene permiso
     */
    @Transactional
    public void eliminarComentario(Long idComentario, Long idUsuario, String tipoUsuario) {
        log.debug("🗑️ Eliminando comentario - ID: {}, Usuario: {}", idComentario, idUsuario);

        Comentario comentario = comentarioRepository.findById(idComentario)
                .orElseThrow(() -> new ComentarioNotFoundException(idComentario));

        boolean esAutor = comentario.getIdUsuario().equals(idUsuario);
        boolean esDuenoContenido = false;

        if ("ARTISTA".equalsIgnoreCase(tipoUsuario)) {
            if (comentario.getCancion() != null) {
                esDuenoContenido = comentario.getCancion().getIdArtista().equals(idUsuario);
            } else if (comentario.getAlbum() != null) {
                esDuenoContenido = comentario.getAlbum().getIdArtista().equals(idUsuario);
            }
        }

        if (!esAutor && !esDuenoContenido) {
            throw new AccesoDenegadoException("No tienes permiso para eliminar este comentario");
        }

        comentarioRepository.delete(comentario);
        log.info("✅ Comentario eliminado - ID: {}", idComentario);
    }

    /**
     * Elimina todos los comentarios de un usuario.
     *
     * <p>Utilizado cuando se elimina un usuario del sistema.</p>
     *
     * @param idUsuario identificador del usuario
     */
    @Transactional
    public void eliminarTodosLosComentarios(Long idUsuario) {
        log.debug("🗑️ Eliminando todos los comentarios - Usuario: {}", idUsuario);
        comentarioRepository.deleteByIdUsuario(idUsuario);
        log.info("✅ Todos los comentarios del usuario eliminados");
    }

    /**
     * Obtiene el nombre del usuario desde el microservicio de usuarios.
     *
     * @param idUsuario identificador del usuario
     * @param tipoUsuario tipo de usuario
     * @return nombre completo del usuario o "Usuario Desconocido" si falla la consulta
     */
    private String obtenerNombreUsuario(Long idUsuario, TipoUsuario tipoUsuario) {
        try {
            String url = usuariosServiceUrl + "/api/usuarios/" + idUsuario + "/nombre-completo";
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

            return "Usuario Desconocido";
        } catch (Exception e) {
            log.warn("⚠️ Error al obtener nombre del usuario {}: {}", idUsuario, e.getMessage());
            return "Usuario Desconocido";
        }
    }

    /**
     * Convierte una entidad Comentario a su representación DTO.
     *
     * @param comentario entidad a convertir
     * @return DTO del comentario
     */
    private ComentarioDTO convertirADTO(Comentario comentario) {
        ComentarioDTO dto = ComentarioDTO.builder()
                .idComentario(comentario.getIdComentario())
                .idUsuario(comentario.getIdUsuario())
                .tipoUsuario(comentario.getTipoUsuario().name())
                .nombreUsuario(comentario.getNombreUsuario())
                .tipoContenido(comentario.getTipoContenido().name())
                .contenido(comentario.getContenido())
                .fechaPublicacion(comentario.getFechaPublicacion())
                .fechaUltimaEdicion(comentario.getFechaUltimaEdicion())
                .editado(comentario.fueEditado())
                .build();

        if (comentario.getCancion() != null) {
            dto.setIdContenido(comentario.getCancion().getIdCancion());
            dto.setTituloContenido(comentario.getCancion().getTituloCancion());
            dto.setUrlPortada(comentario.getCancion().getUrlPortada());
        } else if (comentario.getAlbum() != null) {
            dto.setIdContenido(comentario.getAlbum().getIdAlbum());
            dto.setTituloContenido(comentario.getAlbum().getTituloAlbum());
            dto.setUrlPortada(comentario.getAlbum().getUrlPortada());
        }

        return dto;
    }
}