package com.aluracursos.LiterAlura.servicio;

// Importaciones necesarias
import com.aluracursos.LiterAlura.modelo.*;
import com.aluracursos.LiterAlura.modelo.gutendex.*;
import com.aluracursos.LiterAlura.repositorio.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio principal que maneja la lógica de negocio para libros y autores.
 * @Service indica que es un componente de servicio de Spring
 * @Slf4j proporciona un logger automáticamente (viene de Lombok)
 */
@Service
@Slf4j
public class LiteraturaServicio {
    // Inyección de dependencias mediante campos finales
    private final LibroRepositorio libroRepository;
    private final AutorRepositorio autorRepository;
    private final WebClient webClient;

    /**
     * Constructor que inicializa los repositorios y configura el cliente web.
     * Spring inyectará automáticamente las dependencias necesarias.
     */
    public LiteraturaServicio(
            LibroRepositorio libroRepository,
            AutorRepositorio autorRepository,
            WebClient.Builder webClientBuilder) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;

        // Configura el cliente web con URL base y un filtro para logging
        this.webClient = webClientBuilder
                .baseUrl("https://gutendex.com/books/")
                // Añade un filtro para logear todas las peticiones
                .filter((request, next) -> {
                    log.info("Realizando petición a: {}", request.url());
                    return next.exchange(request);
                })
                .build();
    }

    /**
     * Busca un libro por título en la API Gutendex y lo registra en la base de datos.
     * @Transactional asegura que toda la operación se realice como una única transacción
     */
    @Transactional
    public Optional<Libro> buscarYRegistrarLibro(String titulo) {
        // Verifica si el libro ya existe en la base de datos
        if (libroRepository.findByTituloContainingIgnoreCase(titulo)
                .stream()
                .findAny()
                .isPresent()) {
            throw new RuntimeException("No se permite registrar el mismo libro más de una vez");
        }

        try {
            // Realiza la petición HTTP a Gutendex
            return Optional.ofNullable(webClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .queryParam("search", titulo)
                                    .build())
                            .retrieve()
                            // Maneja específicamente las redirecciones 3xx
                            .onStatus(
                                    status -> status.is3xxRedirection(),
                                    response -> {
                                        log.info("Redirección detectada a: {}",
                                                response.headers().asHttpHeaders().getLocation());
                                        return Mono.error(new RuntimeException("Error de redirección"));
                                    }
                            )
                            // Convierte la respuesta al tipo GutendexRespuesta
                            .bodyToMono(GutendexRespuesta.class)
                            .block())
                    // Procesa la respuesta
                    .map(response -> response.getResults().stream().findFirst())
                    .flatMap(optionalBook -> optionalBook.map(this::convertirYGuardarLibro));
        } catch (Exception e) {
            log.error("Error al buscar el libro en Gutendex: {}", e.getMessage());
            throw new RuntimeException("No se pudo encontrar el libro. Por favor, intente de nuevo.");
        }
    }

    /**
     * Convierte un libro de Gutendex a nuestro modelo y lo guarda en la base de datos
     */
    private Libro convertirYGuardarLibro(GutendexLibro gutendexBook) {
        // Validar título
        String titulo = gutendexBook.getTitle();
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título del libro no puede estar vacío.");
        }

        // Validar idioma
        String idioma = gutendexBook.getLanguages().isEmpty() ? null : gutendexBook.getLanguages().get(0);
        if (idioma == null || idioma.isBlank()) {
            throw new IllegalArgumentException("El idioma del libro no puede estar vacío.");
        }

        // Validar autor
        String autorNombre = gutendexBook.getAuthors().isEmpty() ?
                "Desconocido" : gutendexBook.getAuthors().get(0).getName();

        if (autorNombre == null || autorNombre.isBlank()) {
            throw new IllegalArgumentException("El autor del libro no puede estar vacío.");
        }

        // Crear instancia de Libro con los datos validados
        Libro libro = new Libro(titulo, idioma, autorNombre,
                gutendexBook.getDownloadCount() != null ? gutendexBook.getDownloadCount() : 0);

        // Guardar autor si no es "Desconocido" y no existe
        if (!autorNombre.equals("Desconocido")) {
            guardarAutorSiNoExiste(gutendexBook.getAuthors().get(0));
        }

        // Guardar y retornar el libro
        return libroRepository.save(libro);
    }


    /**
     * Guarda un autor en la base de datos si no existe
     * @Transactional asegura la integridad de la operación
     */
    @Transactional
    private void guardarAutorSiNoExiste(GutendexAutor gutendexAuthor) {
        // Busca el autor por nombre y si no existe lo crea
        autorRepository.findByNombre(gutendexAuthor.getName())
                .orElseGet(() -> {
                    Autor autor = new Autor();
                    autor.setNombre(gutendexAuthor.getName());

                    // Intenta convertir los años a números
                    try {
                        autor.setAnioNacimiento(
                                gutendexAuthor.getBirthYear() != null ?
                                        Integer.parseInt(gutendexAuthor.getBirthYear()) : 0
                        );
                        autor.setAnioMuerte(
                                gutendexAuthor.getDeathYear() != null ?
                                        Integer.parseInt(gutendexAuthor.getDeathYear()) : 0
                        );
                    } catch (NumberFormatException e) {
                        log.warn("Error al parsear año para autor {}: {}",
                                gutendexAuthor.getName(), e.getMessage());
                        autor.setAnioNacimiento(0);
                        autor.setAnioMuerte(0);
                    }

                    autor.setVivo(gutendexAuthor.getDeathYear() == null);
                    return autorRepository.save(autor);
                });
    }

    // Métodos de consulta que utilizan los repositorios JPA

    /**
     * Obtiene todos los libros de la base de datos
     */
    public List<Libro> listarLibros() {
        return libroRepository.findAll();
    }

    /**
     * Obtiene todos los autores de la base de datos
     */
    public List<Autor> listarAutores() {
        return autorRepository.findAll();
    }

    /**
     * Lista los autores que estaban vivos en un año específico
     */
    public List<Autor> listarAutoresVivos(int anio) {
        return autorRepository
                .findByVivoTrueAndAnioNacimientoLessThanEqualAndAnioMuerteGreaterThanEqual(anio, anio);
    }

    /**
     * Lista los libros por idioma
     */
    public List<Libro> listarLibrosPorIdioma(String idioma) {
        return libroRepository.findByIdioma(idioma);
    }

    /**
     * Genera estadísticas de los libros usando Stream API
     */
    public EstadisticasLibros generarEstadisticas() {
        List<Libro> libros = libroRepository.findAll();

        // Cuenta autores únicos usando Stream
        long totalAutores = libros.stream()
                .map(Libro::getAutor)
                .distinct()
                .count();

        // Agrupa libros por idioma y cuenta
        Map<String, Long> librosPorIdioma = libros.stream()
                .collect(Collectors.groupingBy(
                        Libro::getIdioma,
                        Collectors.counting()
                ));

        double promedioLibrosPorAutor = calcularPromedioLibrosPorAutor(libros);

        return new EstadisticasLibros(
                libros.size(),
                totalAutores,
                librosPorIdioma,
                promedioLibrosPorAutor
        );
    }

    /**
     * Obtiene los 10 libros más descargados de Gutendex
     */
    public List<LibroPopular> obtenerTop10Libros() {
        try {
            return webClient.get()
                    .uri("?sort=download_count")
                    .retrieve()
                    .bodyToMono(GutendexRespuesta.class)
                    .map(response -> response.getResults().stream()
                            .limit(10)
                            .map(book -> new LibroPopular(
                                    book.getTitle(),
                                    book.getDownloadCount() != null ? book.getDownloadCount() : 0,
                                    book.getAuthors().isEmpty() ? "Desconocido" : book.getAuthors().get(0).getName()
                            ))
                            .collect(Collectors.toList())
                    )
                    .block();
        } catch (Exception e) {
            log.error("Error al obtener top 10 libros: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // Clases internas para manejar datos específicos

    @Data
    @AllArgsConstructor
    public static class EstadisticasLibros {
        private int totalLibros;
        private double totalAutores;
        private Map<String, Long> distribucionPorIdioma;
        private double promedioLibrosPorAutor;
    }

    @Data
    @AllArgsConstructor
    public static class LibroPopular {
        private String titulo;
        private int descargas;
        private String autor;
    }

    /**
     * Calcula el promedio de libros por autor
     */
    private double calcularPromedioLibrosPorAutor(List<Libro> libros) {
        Map<String, Long> librosPorAutor = libros.stream()
                .collect(Collectors.groupingBy(
                        Libro::getAutor,
                        Collectors.counting()
                ));

        return librosPorAutor.values().stream()
                .mapToDouble(Long::doubleValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Busca autores por nombre (búsqueda parcial, ignora mayúsculas/minúsculas)
     */
    public List<Autor> buscarAutorPorNombre(String nombre) {
        return autorRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Lista autores nacidos entre dos años específicos
     */
    public List<Autor> listarAutoresPorRangoNacimiento(int anioInicio, int anioFin) {
        return autorRepository.findByAnioNacimientoBetween(anioInicio, anioFin);
    }
}