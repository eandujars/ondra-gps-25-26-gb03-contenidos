package com.ondra.contenidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal del microservicio de Contenidos.
 *
 * <p>Gestiona el catálogo de contenidos musicales de la plataforma,
 * incluyendo canciones, álbumes, favoritos, valoraciones, comentarios
 * y sistema de cobros para artistas. Integra almacenamiento de archivos
 * multimedia mediante Cloudinary.</p>
 *
 * <p>Funcionalidades principales:</p>
 * <ul>
 *   <li>Gestión de canciones y álbumes</li>
 *   <li>Catálogo de géneros musicales</li>
 *   <li>Sistema de favoritos y valoraciones</li>
 *   <li>Comentarios de usuarios y artistas</li>
 *   <li>Carrito de compras</li>
 *   <li>Gestión de cobros por reproducciones y ventas</li>
 *   <li>Almacenamiento de archivos de audio y portadas</li>
 *   <li>Procesamiento automático mensual de pagos</li>
 * </ul>
 */
@SpringBootApplication
@EnableScheduling
public class ContenidosServiceApplication {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(ContenidosServiceApplication.class, args);
    }
}