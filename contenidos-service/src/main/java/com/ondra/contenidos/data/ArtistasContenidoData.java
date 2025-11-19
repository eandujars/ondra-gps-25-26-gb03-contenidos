package com.ondra.contenidos.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * Datos predefinidos de artistas con sus álbumes y canciones para inicialización de la base de datos.
 *
 * <p>Contiene información estructurada de artistas, incluyendo álbumes, singles y canciones asociadas.
 * Los identificadores de artista deben coincidir con los registrados en el microservicio de usuarios.</p>
 */
public class ArtistasContenidoData {

    /**
     * Lista de artistas con su contenido musical predefinido.
     *
     * <p>Incluye artistas de diversos géneros: pop, trap latino, rock alternativo,
     * EDM, flamenco urbano y reggaetón.</p>
     */
    public static final List<ArtistaContenido> ARTISTAS = Arrays.asList(
            new ArtistaContenido(
                    2L,
                    "aitana",
                    Arrays.asList(
                            new AlbumInfo("Alpha", 2L, 2022, 19.99,
                                    "Álbum debut de Aitana con sonidos pop frescos y letras personales.",
                                    Arrays.asList(
                                            new CancionInfo("Los Angeles", 1, 2L, 1.29),
                                            new CancionInfo("Las Babys", 2, 2L, 1.29),
                                            new CancionInfo("Darari", 3, 29L, 1.29),
                                            new CancionInfo("Aqyne", 4, 2L, 1.29),
                                            new CancionInfo("Mi Amor", 5, 2L, 1.29),
                                            new CancionInfo("Formentera", 6, 2L, 1.29),
                                            new CancionInfo("En El Coche", 7, 2L, 1.29)
                                    )),
                            new AlbumInfo("Cuarto Azul", 2L, 2020, 17.99,
                                    "Reedición de su primer disco con nuevos temas que marcaron su carrera.",
                                    Arrays.asList(
                                            new CancionInfo("6 de Febrero", 1, 2L, 1.29),
                                            new CancionInfo("Segundo Intento", 2, 2L, 1.29),
                                            new CancionInfo("Cuando Hables con El", 3, 2L, 1.29),
                                            new CancionInfo("Superestrella", 4, 2L, 1.29),
                                            new CancionInfo("Conexion Psiquica", 5, 2L, 1.29)
                                    ))
                    )
            ),

            new ArtistaContenido(
                    1L,
                    "duki",
                    Arrays.asList(
                            new AlbumInfo("Ameri", 28L, 2023, 22.99,
                                    "Álbum que consolida a Duki como uno de los referentes del trap latino.",
                                    Arrays.asList(
                                            new CancionInfo("Nueva Era", 1, 28L, 1.49),
                                            new CancionInfo("Brindis", 2, 28L, 1.49),
                                            new CancionInfo("Hardaway", 3, 28L, 1.49),
                                            new CancionInfo("Ameri", 4, 28L, 1.49)
                                    )),
                            new AlbumInfo("Desde el Fin del Mundo", 28L, 2021, 21.99,
                                    "Álbum conceptual de Duki que explora trap melódico y letras introspectivas.",
                                    Arrays.asList(
                                            new CancionInfo("Sudor y Trabajo", 1, 28L, 1.49),
                                            new CancionInfo("Malbec", 2, 28L, 1.49),
                                            new CancionInfo("Rapido", 3, 28L, 1.49),
                                            new CancionInfo("Cascada", 4, 28L, 1.49),
                                            new CancionInfo("Pintao", 5, 28L, 1.49)
                                    ))
                    ),
                    Arrays.asList(
                            new SingleInfo("Antes de Perderte", 28L, 1.49,
                                    "Single emotivo de Duki sobre relaciones y despedidas.")
                    )
            ),

            new ArtistaContenido(
                    6L,
                    "sanguijuelasdelguadiana",
                    Arrays.asList(
                            new AlbumInfo("Revolá", 1L, 2023, 16.99,
                                    "Álbum debut de la banda extremeña con rock alternativo y letras crudas sobre la España profunda.",
                                    Arrays.asList(
                                            new CancionInfo("Intro", 1, 1L, 1.29),
                                            new CancionInfo("1000 Amapolas", 2, 1L, 1.29),
                                            new CancionInfo("Jaribe", 3, 1L, 1.29),
                                            new CancionInfo("Septiembre", 4, 1L, 1.29),
                                            new CancionInfo("Llevadme a Mi Extremadura", 5, 1L, 1.29),
                                            new CancionInfo("Revola", 6, 1L, 1.29),
                                            new CancionInfo("Intacto", 7, 1L, 1.29)
                                    ))
                    )
            ),

            new ArtistaContenido(
                    3L,
                    "avicii",
                    Arrays.asList(
                            new AlbumInfo("True", 8L, 2013, 24.99,
                                    "Álbum revolucionario que mezcló EDM con folk y country, con 'Wake Me Up' como hit mundial.",
                                    Arrays.asList(
                                            new CancionInfo("Hey Brother", 1, 8L, 1.49),
                                            new CancionInfo("You Make Me", 2, 8L, 1.49),
                                            new CancionInfo("Addicted to You", 3, 8L, 1.49)
                                    ))
                    ),
                    Arrays.asList(
                            new SingleInfo("Levels", 8L, 1.49,
                                    "El himno que definió la era dorada del EDM."),
                            new SingleInfo("The Nights", 8L, 1.49,
                                    "Canción inspiracional que se convirtió en un himno generacional."),
                            new SingleInfo("Wake Me Up", 8L, 1.49,
                                    "Fusión innovadora de EDM y folk que rompió barreras musicales.")
                    )
            ),

            new ArtistaContenido(
                    5L,
                    "rosalia",
                    Arrays.asList(
                            new AlbumInfo("El Mal Querer", 2L, 2018, 21.99,
                                    "Álbum conceptual basado en una novela medieval, fusionando flamenco con pop urbano.",
                                    Arrays.asList(
                                            new CancionInfo("Malamente", 1, 2L, 1.99),
                                            new CancionInfo("Pienso en Tu Mira", 2, 2L, 1.99),
                                            new CancionInfo("Bagdad", 3, 2L, 1.99),
                                            new CancionInfo("Nana", 4, 2L, 1.99)
                                    ))
                    ),
                    Arrays.asList(
                            new SingleInfo("Despecha", 2L, 1.99,
                                    "Single veraniego con ritmos de merengue dominicano.")
                    )
            ),

            new ArtistaContenido(
                    4L,
                    "daddyyankee",
                    Arrays.asList(
                            new AlbumInfo("Prestige", 19L, 2012, 19.99,
                                    "Álbum que consolidó a Daddy Yankee como el Rey del Reggaetón mundial.",
                                    Arrays.asList(
                                            new CancionInfo("Perros Salvajes", 1, 19L, 1.49),
                                            new CancionInfo("Limbo", 2, 19L, 1.49),
                                            new CancionInfo("Lovumba", 3, 19L, 1.49),
                                            new CancionInfo("Pasarela", 4, 19L, 1.49),
                                            new CancionInfo("El Amante", 5, 19L, 1.49)
                                    ))
                    ),
                    Arrays.asList(
                            new SingleInfo("Gasolina", 19L, 0.00,
                                    "El himno que llevó el reggaetón al mundo entero. Legendario.")
                    )
            )
    );

    /**
     * Representa un artista con su contenido musical asociado.
     */
    @Data
    @AllArgsConstructor
    public static class ArtistaContenido {
        private Long idArtista;
        private String carpeta;
        private List<AlbumInfo> albumes;
        private List<SingleInfo> singles;

        /**
         * Constructor para artistas sin singles.
         *
         * @param idArtista identificador del artista
         * @param carpeta nombre de carpeta para almacenamiento de archivos
         * @param albumes lista de álbumes del artista
         */
        public ArtistaContenido(Long idArtista, String carpeta, List<AlbumInfo> albumes) {
            this.idArtista = idArtista;
            this.carpeta = carpeta;
            this.albumes = albumes;
            this.singles = Arrays.asList();
        }
    }

    /**
     * Representa la información de un álbum musical.
     */
    @Data
    @AllArgsConstructor
    public static class AlbumInfo {
        private String titulo;
        private Long idGenero;
        private Integer anio;
        private Double precio;
        private String descripcion;
        private List<CancionInfo> canciones;
    }

    /**
     * Representa la información de una canción dentro de un álbum.
     */
    @Data
    @AllArgsConstructor
    public static class CancionInfo {
        private String titulo;
        private Integer numeroPista;
        private Long idGenero;
        private Double precio;
    }

    /**
     * Representa la información de un single independiente.
     */
    @Data
    @AllArgsConstructor
    public static class SingleInfo {
        private String titulo;
        private Long idGenero;
        private Double precio;
        private String descripcion;
    }
}