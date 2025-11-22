package com.ondra.contenidos.services;

import com.ondra.contenidos.dto.*;
import com.ondra.contenidos.exceptions.*;
import com.ondra.contenidos.models.dao.*;
import com.ondra.contenidos.models.enums.TipoContenido;
import com.ondra.contenidos.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestión del carrito de compra.
 *
 * <p>Proporciona operaciones para añadir, eliminar y consultar items del carrito,
 * así como finalizar compras y gestionar el ciclo de vida del carrito por usuario.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final CancionRepository cancionRepository;
    private final AlbumRepository albumRepository;
    private final CompraRepository compraRepository;
    private final RestTemplate restTemplate;

    @Value("${microservices.usuarios.url:http://localhost:8080}")
    private String usuariosServiceUrl;

    /**
     * Obtiene el carrito de un usuario, creándolo si no existe.
     *
     * @param idUsuario identificador del usuario
     * @return carrito del usuario con sus items
     */
    @Transactional
    public CarritoDTO obtenerCarrito(Long idUsuario) {
        log.debug("📋 Obteniendo carrito - Usuario: {}", idUsuario);

        Carrito carrito = carritoRepository.findByIdUsuarioWithItems(idUsuario)
                .orElseGet(() -> {
                    Carrito nuevoCarrito = Carrito.builder()
                            .idUsuario(idUsuario)
                            .build();
                    return carritoRepository.save(nuevoCarrito);
                });

        return convertirADTO(carrito);
    }

    /**
     * Añade un item al carrito del usuario.
     *
     * <p>Valida que el item no exista previamente, verifica la existencia del contenido
     * y obtiene información del artista desde el microservicio de usuarios.</p>
     *
     * @param idUsuario identificador del usuario
     * @param dto datos del item a añadir
     * @return carrito actualizado con el nuevo item
     * @throws IllegalArgumentException si el tipo de producto es inválido o faltan datos requeridos
     * @throws ItemYaEnCarritoException si el item ya existe en el carrito
     * @throws CancionNotFoundException si la canción no existe
     * @throws AlbumNotFoundException si el álbum no existe
     */
    @Transactional
    public CarritoDTO agregarItem(Long idUsuario, AgregarAlCarritoDTO dto) {
        log.debug("➕ Agregando item al carrito - Usuario: {}, Tipo: {}", idUsuario, dto.getTipoProducto());

        Carrito carrito = carritoRepository.findByIdUsuario(idUsuario)
                .orElseGet(() -> {
                    Carrito nuevoCarrito = Carrito.builder()
                            .idUsuario(idUsuario)
                            .build();
                    return carritoRepository.save(nuevoCarrito);
                });

        CarritoItem.TipoProducto tipo;
        try {
            tipo = CarritoItem.TipoProducto.valueOf(dto.getTipoProducto().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de producto inválido: " + dto.getTipoProducto());
        }

        CarritoItem item = CarritoItem.builder()
                .carrito(carrito)
                .tipoProducto(tipo)
                .build();

        if (tipo == CarritoItem.TipoProducto.CANCION) {
            if (dto.getIdCancion() == null) {
                throw new IllegalArgumentException("ID de canción es requerido para items de tipo CANCION");
            }

            if (carritoItemRepository.existsByCarritoAndCancion(carrito.getIdCarrito(), dto.getIdCancion())) {
                throw new ItemYaEnCarritoException("La canción ya está en el carrito");
            }

            Cancion cancion = cancionRepository.findById(dto.getIdCancion())
                    .orElseThrow(() -> new CancionNotFoundException(dto.getIdCancion()));

            item.setCancion(cancion);
            item.setPrecio(BigDecimal.valueOf(cancion.getPrecioCancion()));
            item.setUrlPortada(cancion.getUrlPortada());
            item.setTitulo(cancion.getTituloCancion());

            String nombreArtistico = obtenerNombreArtistico(cancion.getIdArtista());
            item.setNombreArtistico(nombreArtistico);

        } else if (tipo == CarritoItem.TipoProducto.ALBUM) {
            if (dto.getIdAlbum() == null) {
                throw new IllegalArgumentException("ID de álbum es requerido para items de tipo ALBUM");
            }

            if (carritoItemRepository.existsByCarritoAndAlbum(carrito.getIdCarrito(), dto.getIdAlbum())) {
                throw new ItemYaEnCarritoException("El álbum ya está en el carrito");
            }

            Album album = albumRepository.findById(dto.getIdAlbum())
                    .orElseThrow(() -> new AlbumNotFoundException(dto.getIdAlbum()));

            item.setAlbum(album);
            item.setPrecio(BigDecimal.valueOf(album.getPrecioAlbum()));
            item.setUrlPortada(album.getUrlPortada());
            item.setTitulo(album.getTituloAlbum());

            String nombreArtistico = obtenerNombreArtistico(album.getIdArtista());
            item.setNombreArtistico(nombreArtistico);
        }

        carritoItemRepository.save(item);
        log.info("✅ Item agregado al carrito");

        carrito = carritoRepository.findByIdUsuarioWithItems(idUsuario).orElse(carrito);
        return convertirADTO(carrito);
    }

    /**
     * Elimina un item específico del carrito.
     *
     * @param idUsuario identificador del usuario
     * @param idCarritoItem identificador del item a eliminar
     * @return carrito actualizado sin el item eliminado
     * @throws CarritoNotFoundException si el carrito no existe
     * @throws CarritoItemNotFoundException si el item no existe
     * @throws ForbiddenAccessException si el item no pertenece al carrito del usuario
     */
    @Transactional
    public CarritoDTO eliminarItem(Long idUsuario, Long idCarritoItem) {
        log.debug("🗑️ Eliminando item del carrito - Usuario: {}, Item: {}", idUsuario, idCarritoItem);

        Carrito carrito = carritoRepository.findByIdUsuario(idUsuario)
                .orElseThrow(() -> new CarritoNotFoundException("Carrito no encontrado"));

        CarritoItem item = carritoItemRepository.findById(idCarritoItem)
                .orElseThrow(() -> new CarritoItemNotFoundException("Item no encontrado"));

        if (!item.getCarrito().getIdCarrito().equals(carrito.getIdCarrito())) {
            throw new ForbiddenAccessException("Este item no pertenece a tu carrito");
        }

        carritoItemRepository.delete(item);
        log.info("✅ Item eliminado del carrito");

        carrito = carritoRepository.findByIdUsuarioWithItems(idUsuario).orElse(carrito);
        return convertirADTO(carrito);
    }

    /**
     * Elimina todos los items del carrito de un usuario.
     *
     * @param idUsuario identificador del usuario
     * @throws CarritoNotFoundException si el carrito no existe
     */
    @Transactional
    public void vaciarCarrito(Long idUsuario) {
        log.debug("🗑️ Vaciando carrito - Usuario: {}", idUsuario);

        Carrito carrito = carritoRepository.findByIdUsuario(idUsuario)
                .orElseThrow(() -> new CarritoNotFoundException("Carrito no encontrado"));

        carritoItemRepository.deleteByCarritoIdCarrito(carrito.getIdCarrito());
        log.info("✅ Carrito vaciado");
    }

    /**
     * Finaliza la compra creando registros de compra y vaciando el carrito.
     *
     * <p>Genera un identificador único de transacción y crea un registro de compra
     * por cada item del carrito.</p>
     *
     * @param idUsuario identificador del usuario
     * @param idMetodoPago identificador del método de pago utilizado
     * @throws CarritoNotFoundException si el carrito no existe
     * @throws CarritoVacioException si el carrito está vacío
     */
    @Transactional
    public void finalizarCompra(Long idUsuario, Long idMetodoPago) {
        log.debug("💳 Finalizando compra - Usuario: {}, Método de pago: {}", idUsuario, idMetodoPago);

        Carrito carrito = carritoRepository.findByIdUsuarioWithItems(idUsuario)
                .orElseThrow(() -> new CarritoNotFoundException("Carrito no encontrado"));

        if (carrito.getItems().isEmpty()) {
            throw new CarritoVacioException("El carrito está vacío");
        }

        String idTransaccion = "TXN-" + System.currentTimeMillis() + "-" + idUsuario;

        for (CarritoItem item : carrito.getItems()) {
            Compra compra = Compra.builder()
                    .idUsuario(idUsuario)
                    .tipoContenido(item.getTipoProducto() == CarritoItem.TipoProducto.CANCION
                            ? TipoContenido.CANCION
                            : TipoContenido.ALBUM)
                    .cancion(item.getCancion())
                    .album(item.getAlbum())
                    .precioPagado(item.getPrecio())
                    .metodoPago("METODO_PAGO_" + idMetodoPago)
                    .idTransaccion(idTransaccion)
                    .build();

            compraRepository.save(compra);
        }

        vaciarCarrito(idUsuario);
        log.info("✅ Compra finalizada - Transacción: {}", idTransaccion);
    }

    /**
     * Elimina el carrito completo de un usuario.
     *
     * @param idUsuario identificador del usuario
     */
    public void eliminarCarrito(Long idUsuario) {
        carritoRepository.deleteByIdUsuario(idUsuario);
    }

    /**
     * Obtiene el nombre artístico desde el microservicio de usuarios.
     *
     * @param idArtista identificador del artista
     * @return nombre artístico o "Artista Desconocido" si falla la consulta
     */
    private String obtenerNombreArtistico(Long idArtista) {
        try {
            String url = usuariosServiceUrl + "/api/usuarios/" + idArtista + "/nombre-artistico";
            log.debug("📞 Llamando a microservicio usuarios: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("nombreArtistico");
            }

            return "Artista Desconocido";
        } catch (Exception e) {
            log.warn("⚠️ Error al obtener nombre artístico del usuario {}: {}", idArtista, e.getMessage());
            return "Artista Desconocido";
        }
    }

    /**
     * Convierte una entidad Carrito a su representación DTO.
     *
     * @param carrito entidad a convertir
     * @return DTO del carrito
     */
    private CarritoDTO convertirADTO(Carrito carrito) {
        return CarritoDTO.builder()
                .idCarrito(carrito.getIdCarrito())
                .idUsuario(carrito.getIdUsuario())
                .items(carrito.getItems().stream()
                        .map(this::convertirItemADTO)
                        .collect(Collectors.toList()))
                .cantidadItems(carrito.getCantidadItems())
                .precioTotal(carrito.getPrecioTotal())
                .fechaCreacion(carrito.getFechaCreacion())
                .fechaActualizacion(carrito.getFechaActualizacion())
                .build();
    }

    /**
     * Convierte una entidad CarritoItem a su representación DTO.
     *
     * @param item entidad a convertir
     * @return DTO del item
     */
    private CarritoItemDTO convertirItemADTO(CarritoItem item) {
        return CarritoItemDTO.builder()
                .idCarritoItem(item.getIdCarritoItem())
                .tipoProducto(item.getTipoProducto().name())
                .idCancion(item.getCancion() != null ? item.getCancion().getIdCancion() : null)
                .idAlbum(item.getAlbum() != null ? item.getAlbum().getIdAlbum() : null)
                .precio(item.getPrecio())
                .urlPortada(item.getUrlPortada())
                .nombreArtistico(item.getNombreArtistico())
                .titulo(item.getTitulo())
                .fechaAgregado(item.getFechaAgregado())
                .build();
    }
}