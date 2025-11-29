package com.ondra.contenidos.services;

import com.ondra.contenidos.clients.UsuariosClient;
import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.models.dao.Album;
import com.ondra.contenidos.models.dao.Cancion;
import com.ondra.contenidos.models.dao.Valoracion;
import com.ondra.contenidos.models.enums.TipoContenido;
import com.ondra.contenidos.models.enums.TipoUsuario;
import com.ondra.contenidos.repositories.AlbumRepository;
import com.ondra.contenidos.repositories.CancionRepository;
import com.ondra.contenidos.repositories.ValoracionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestión de valoraciones de usuarios y artistas.
 *
 * <p>Proporciona operaciones para crear, editar, listar y eliminar valoraciones
 * sobre canciones y álbumes, con cálculo de promedios y validación de duplicados.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final CancionRepository cancionRepository;
    private final AlbumRepository albumRepository;
    private final UsuariosClient usuariosClient;

    /**
     * Crea una nueva valoración sobre un contenido musical.
     *
     * <p>Valida que el usuario no haya valorado previamente el mismo contenido,
     * obtiene el nombre del usuario desde el microservicio de usuarios y verifica
     * la existencia del contenido asociado.</p>
     *
     * @param idUsuario identificador del usuario que valora
     * @param tipoUsuario tipo de usuario (USUARIO o ARTISTA)
     * @param dto datos de la valoración a crear
     * @return valoración creada
     * @throws IllegalArgumentException si el tipo de contenido o usuario es inválido, o faltan datos requeridos
     * @throws ValoracionYaExisteException si el usuario ya valoró el contenido
     * @throws CancionNotFoundException si la canción no existe
     * @throws AlbumNotFoundException si el álbum no existe
     */
    @Transactional
    public ValoracionDTO crearValoracion(Long idUsuario, String tipoUsuario, CrearValoracionDTO dto) {
        log.debug("➕ Creando valoración - Usuario: {}, Tipo: {}", idUsuario, dto.getTipoContenido());

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

        if (tipo == TipoContenido .CANCIÓN) {
            if (dto.getIdCancion() == null) {
                throw new IllegalArgumentException("ID de canción es requerido para valoraciones de tipo CANCION");
            }

            if (valoracionRepository.existsByUsuarioAndCancion(idUsuario, dto.getIdCancion())) {
                throw new ValoracionYaExisteException("Ya has valorado esta canción. Puedes editar tu valoración existente.");
            }
        } else if (tipo == TipoContenido.ÁLBUM) {
            if (dto.getIdAlbum() == null) {
                throw new IllegalArgumentException("ID de álbum es requerido para valoraciones de tipo ÁLBUM");
            }

            if (valoracionRepository.existsByUsuarioAndAlbum(idUsuario, dto.getIdAlbum())) {
                throw new ValoracionYaExisteException("Ya has valorado este álbum. Puedes editar tu valoración existente.");
            }
        }

        String nombreUsuario = obtenerNombreUsuario(idUsuario, tipoUsuarioEnum);

        Valoracion valoracion = Valoracion.builder()
                .idUsuario(idUsuario)
                .tipoUsuario(tipoUsuarioEnum)
                .nombreUsuario(nombreUsuario)
                .tipoContenido(tipo)
                .valor(dto.getValor())
                .build();

        if (tipo == TipoContenido.CANCIÓN) {
            Cancion cancion = cancionRepository.findById(dto.getIdCancion())
                    .orElseThrow(() -> new CancionNotFoundException(dto.getIdCancion()));

            valoracion.setCancion(cancion);

        } else if (tipo == TipoContenido.ÁLBUM) {
            Album album = albumRepository.findById(dto.getIdAlbum())
                    .orElseThrow(() -> new AlbumNotFoundException(dto.getIdAlbum()));

            valoracion.setAlbum(album);
        }

        Valoracion valoracionGuardada = valoracionRepository.save(valoracion);
        log.info("✅ Valoración creada - ID: {}", valoracionGuardada.getIdValoracion());

        return convertirADTO(valoracionGuardada);
    }

    /**
     * Edita el valor de una valoración existente.
     *
     * <p>Solo el autor de la valoración puede editarla.</p>
     *
     * @param idValoracion identificador de la valoración a editar
     * @param idUsuario identificador del usuario que solicita la edición
     * @param dto datos actualizados de la valoración
     * @return valoración actualizada
     * @throws ValoracionNotFoundException si la valoración no existe
     * @throws AccesoDenegadoException si el usuario no es el autor
     */
    @Transactional
    public ValoracionDTO editarValoracion(Long idValoracion, Long idUsuario, EditarValoracionDTO dto) {
        log.debug("✏️ Editando valoración - ID: {}, Usuario: {}", idValoracion, idUsuario);

        Valoracion valoracion = valoracionRepository.findById(idValoracion)
                .orElseThrow(() -> new ValoracionNotFoundException(idValoracion));

        if (!valoracion.getIdUsuario().equals(idUsuario)) {
            throw new AccesoDenegadoException("No tienes permiso para editar esta valoración");
        }

        valoracion.setValor(dto.getValor());
        Valoracion valoracionActualizada = valoracionRepository.save(valoracion);

        log.info("✅ Valoración editada - ID: {}", idValoracion);
        return convertirADTO(valoracionActualizada);
    }

    /**
     * Obtiene la valoración de un usuario para una canción específica.
     *
     * @param idUsuario identificador del usuario
     * @param idCancion identificador de la canción
     * @return valoración del usuario o null si no existe
     */
    @Transactional(readOnly = true)
    public ValoracionDTO obtenerValoracionUsuarioCancion(Long idUsuario, Long idCancion) {
        log.debug("🔍 Obteniendo valoración de usuario {} para canción {}", idUsuario, idCancion);

        return valoracionRepository.findByUsuarioAndCancion(idUsuario, idCancion)
                .map(this::convertirADTO)
                .orElse(null);
    }

    /**
     * Obtiene la valoración de un usuario para un álbum específico.
     *
     * @param idUsuario identificador del usuario
     * @param idAlbum identificador del álbum
     * @return valoración del usuario o null si no existe
     */
    @Transactional(readOnly = true)
    public ValoracionDTO obtenerValoracionUsuarioAlbum(Long idUsuario, Long idAlbum) {
        log.debug("🔍 Obteniendo valoración de usuario {} para álbum {}", idUsuario, idAlbum);

        return valoracionRepository.findByUsuarioAndAlbum(idUsuario, idAlbum)
                .map(this::convertirADTO)
                .orElse(null);
    }

    /**
     * Lista las valoraciones de una canción con paginación y cálculo de promedio.
     *
     * <p>Ordena las valoraciones por fecha descendente e incluye el promedio
     * de todas las valoraciones.</p>
     *
     * @param idCancion identificador de la canción
     * @param pagina número de página (base 1)
     * @param limite cantidad de elementos por página
     * @return valoraciones paginadas con promedio
     * @throws CancionNotFoundException si la canción no existe
     */
    @Transactional(readOnly = true)
    public ValoracionesPaginadasDTO listarValoracionesCancion(Long idCancion, Integer pagina, Integer limite) {
        log.debug("📋 Listando valoraciones de canción - ID: {}", idCancion);

        if (!cancionRepository.existsById(idCancion)) {
            throw new CancionNotFoundException(idCancion);
        }

        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite, Sort.by(Sort.Direction.DESC, "fechaValoracion"));
        Page<Valoracion> paginaValoraciones = valoracionRepository.findByCancion(idCancion, pageable);

        Double promedio = valoracionRepository.calcularPromedioCancion(idCancion);

        return ValoracionesPaginadasDTO.builder()
                .valoraciones(paginaValoraciones.getContent().stream()
                        .map(this::convertirADTO)
                        .toList())
                .paginaActual(paginaValoraciones.getNumber() + 1)
                .totalPaginas(paginaValoraciones.getTotalPages())
                .totalElementos(paginaValoraciones.getTotalElements())
                .elementosPorPagina(paginaValoraciones.getSize())
                .valoracionPromedio(promedio)
                .build();
    }

    /**
     * Lista las valoraciones de un álbum con paginación y cálculo de promedio.
     *
     * <p>Ordena las valoraciones por fecha descendente e incluye el promedio
     * de todas las valoraciones.</p>
     *
     * @param idAlbum identificador del álbum
     * @param pagina número de página (base 1)
     * @param limite cantidad de elementos por página
     * @return valoraciones paginadas con promedio
     * @throws AlbumNotFoundException si el álbum no existe
     */
    @Transactional(readOnly = true)
    public ValoracionesPaginadasDTO listarValoracionesAlbum(Long idAlbum, Integer pagina, Integer limite) {
        log.debug("📋 Listando valoraciones de álbum - ID: {}", idAlbum);

        if (!albumRepository.existsById(idAlbum)) {
            throw new AlbumNotFoundException(idAlbum);
        }

        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite, Sort.by(Sort.Direction.DESC, "fechaValoracion"));
        Page<Valoracion> paginaValoraciones = valoracionRepository.findByAlbum(idAlbum, pageable);

        Double promedio = valoracionRepository.calcularPromedioAlbum(idAlbum);

        return ValoracionesPaginadasDTO.builder()
                .valoraciones(paginaValoraciones.getContent().stream()
                        .map(this::convertirADTO)
                        .toList())
                .paginaActual(paginaValoraciones.getNumber() + 1)
                .totalPaginas(paginaValoraciones.getTotalPages())
                .totalElementos(paginaValoraciones.getTotalElements())
                .elementosPorPagina(paginaValoraciones.getSize())
                .valoracionPromedio(promedio)
                .build();
    }

    /**
     * Lista todas las valoraciones de un usuario con paginación.
     *
     * <p>Ordena las valoraciones por fecha descendente.</p>
     *
     * @param idUsuario identificador del usuario
     * @param pagina número de página (base 1)
     * @param limite cantidad de elementos por página
     * @return valoraciones paginadas del usuario
     */
    @Transactional(readOnly = true)
    public ValoracionesPaginadasDTO listarValoracionesUsuario(Long idUsuario, Integer pagina, Integer limite) {
        log.debug("📋 Listando valoraciones de usuario - ID: {}", idUsuario);

        pagina = (pagina != null && pagina > 0) ? pagina - 1 : 0;
        limite = (limite != null && limite > 0 && limite <= 100) ? limite : 20;

        Pageable pageable = PageRequest.of(pagina, limite, Sort.by(Sort.Direction.DESC, "fechaValoracion"));
        Page<Valoracion> paginaValoraciones = valoracionRepository.findByIdUsuario(idUsuario, pageable);

        return ValoracionesPaginadasDTO.builder()
                .valoraciones(paginaValoraciones.getContent().stream()
                        .map(this::convertirADTO)
                        .toList())
                .paginaActual(paginaValoraciones.getNumber() + 1)
                .totalPaginas(paginaValoraciones.getTotalPages())
                .totalElementos(paginaValoraciones.getTotalElements())
                .elementosPorPagina(paginaValoraciones.getSize())
                .build();
    }

    /**
     * Calcula el promedio de valoraciones de una canción.
     *
     * @param idCancion identificador de la canción
     * @return promedio de valoraciones o null si no hay valoraciones
     */
    @Transactional(readOnly = true)
    public Double obtenerPromedioCancion(Long idCancion) {
        log.debug("📊 Calculando promedio de canción - ID: {}", idCancion);
        return valoracionRepository.calcularPromedioCancion(idCancion);
    }

    /**
     * Calcula el promedio de valoraciones de un álbum.
     *
     * @param idAlbum identificador del álbum
     * @return promedio de valoraciones o null si no hay valoraciones
     */
    @Transactional(readOnly = true)
    public Double obtenerPromedioAlbum(Long idAlbum) {
        log.debug("📊 Calculando promedio de álbum - ID: {}", idAlbum);
        return valoracionRepository.calcularPromedioAlbum(idAlbum);
    }

    /**
     * Elimina una valoración existente.
     *
     * <p>Puede eliminarla el autor de la valoración o el propietario del contenido
     * (si es artista).</p>
     *
     * @param idValoracion identificador de la valoración a eliminar
     * @param idUsuario identificador del usuario que solicita la eliminación
     * @param tipoUsuario tipo de usuario que solicita la eliminación
     * @throws ValoracionNotFoundException si la valoración no existe
     * @throws AccesoDenegadoException si el usuario no tiene permiso
     */
    @Transactional
    public void eliminarValoracion(Long idValoracion, Long idUsuario, String tipoUsuario) {
        log.debug("🗑️ Eliminando valoración - ID: {}, Usuario: {}", idValoracion, idUsuario);

        Valoracion valoracion = valoracionRepository.findById(idValoracion)
                .orElseThrow(() -> new ValoracionNotFoundException(idValoracion));

        boolean esAutor = valoracion.getIdUsuario().equals(idUsuario);
        boolean esDuenoContenido = false;

        if ("ARTISTA".equalsIgnoreCase(tipoUsuario)) {
            if (valoracion.getCancion() != null) {
                esDuenoContenido = valoracion.getCancion().getIdArtista().equals(idUsuario);
            } else if (valoracion.getAlbum() != null) {
                esDuenoContenido = valoracion.getAlbum().getIdArtista().equals(idUsuario);
            }
        }

        if (!esAutor && !esDuenoContenido) {
            throw new AccesoDenegadoException("No tienes permiso para eliminar esta valoración");
        }

        valoracionRepository.delete(valoracion);
        log.info("✅ Valoración eliminada - ID: {}", idValoracion);
    }

    /**
     * Elimina todas las valoraciones de un usuario.
     *
     * <p>Utilizado cuando se elimina un usuario del sistema.</p>
     *
     * @param idUsuario identificador del usuario
     */
    @Transactional
    public void eliminarTodasLasValoraciones(Long idUsuario) {
        log.debug("🗑️ Eliminando todas las valoraciones - Usuario: {}", idUsuario);
        valoracionRepository.deleteByIdUsuario(idUsuario);
        log.info("✅ Todas las valoraciones del usuario eliminadas");
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
            String nombreCompleto = usuariosClient.obtenerNombreCompleto(idUsuario);
            return nombreCompleto != null ? nombreCompleto : "Usuario Desconocido";
        } catch (Exception e) {
            log.warn("⚠️ Error al obtener nombre del usuario {}: {}", idUsuario, e.getMessage());
            return "Usuario Desconocido";
        }
    }

    /**
     * Convierte una entidad Valoracion a su representación DTO.
     *
     * @param valoracion entidad a convertir
     * @return DTO de la valoración
     */
    private ValoracionDTO convertirADTO(Valoracion valoracion) {
        ValoracionDTO dto = ValoracionDTO.builder()
                .idValoracion(valoracion.getIdValoracion())
                .idUsuario(valoracion.getIdUsuario())
                .tipoUsuario(valoracion.getTipoUsuario().name())
                .nombreUsuario(valoracion.getNombreUsuario())
                .tipoContenido(valoracion.getTipoContenido().name())
                .valor(valoracion.getValor())
                .fechaValoracion(valoracion.getFechaValoracion())
                .fechaUltimaEdicion(valoracion.getFechaUltimaEdicion())
                .editada(valoracion.fueEditada())
                .build();

        if (valoracion.getCancion() != null) {
            dto.setIdContenido(valoracion.getCancion().getIdCancion());
            dto.setTituloContenido(valoracion.getCancion().getTituloCancion());
            dto.setUrlPortada(valoracion.getCancion().getUrlPortada());
        } else if (valoracion.getAlbum() != null) {
            dto.setIdContenido(valoracion.getAlbum().getIdAlbum());
            dto.setTituloContenido(valoracion.getAlbum().getTituloAlbum());
            dto.setUrlPortada(valoracion.getAlbum().getUrlPortada());
        }

        return dto;
    }
}