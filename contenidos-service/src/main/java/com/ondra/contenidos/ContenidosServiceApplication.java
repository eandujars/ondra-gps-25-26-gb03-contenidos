package com.ondra.contenidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal del microservicio de Contenidos.
 *
 * <p>Este microservicio gestiona el catálogo de contenidos musicales de la plataforma,
 * incluyendo canciones, álbumes, artistas y géneros musicales. Proporciona endpoints
 * para la consulta, creación, actualización y eliminación de contenidos, así como
 * la gestión de archivos multimedia a través de Cloudinary.</p>
 *
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Gestión de canciones y sus metadatos</li>
 *   <li>Gestión de álbumes y relaciones con canciones</li>
 *   <li>Gestión de artistas</li>
 *   <li>Catálogo de géneros musicales</li>
 *   <li>Subida y almacenamiento de archivos de audio y portadas en Cloudinary</li>
 * </ul>
 *
 * <p><strong>Configuración:</strong> El microservicio requiere configuración de base de datos,
 * Cloudinary y seguridad JWT en application.properties o application.yml.</p>
 *
 * @author Ondra
 * @version 1.0
 * @since 2025
 */
@SpringBootApplication
public class ContenidosServiceApplication {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     *
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(ContenidosServiceApplication.class, args);
    }

}