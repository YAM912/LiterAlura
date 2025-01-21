// Clase Menu
package com.aluracursos.LiterAlura.menuUsuario;

import com.aluracursos.LiterAlura.modelo.Autor;
import com.aluracursos.LiterAlura.modelo.Libro;
import com.aluracursos.LiterAlura.servicio.LiteraturaServicio;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

// Clase que gestiona el menú interactivo de la aplicación

@Component
public class Menu {
    private final LiteraturaServicio literaturaServicio;
    private final Scanner scanner;

    public Menu(LiteraturaServicio literaturaServicio) {
        this.literaturaServicio = literaturaServicio;
        this.scanner = new Scanner(System.in);
    }

    public void mostrarMenu() {
        int opcion;
        do {
            System.out.println("\n=== MENU LITERALURA ===");
            System.out.println("1. Buscar libro por titulo");
            System.out.println("2. Listar libros registrados");
            System.out.println("3. Listar autores registrados");
            System.out.println("4. Listar autores vivos en un determinado año");
            System.out.println("5. Listar libros por idioma");
            System.out.println("6. Ver estadísticas");
            System.out.println("7. Ver Top 10 libros más descargados");
            System.out.println("8. Buscar autor por nombre");
            System.out.println("9. Listar autores por rango de nacimiento");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción del 0 al 9: ");

            opcion = obtenerOpcion();
            procesarOpcion(opcion);

        } while (opcion != 0);
        scanner.close();
    }
    private int obtenerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void procesarOpcion(int opcion) {
        try {
            switch (opcion) {
                case 1 -> buscarLibroPorTitulo();
                case 2 -> listarLibros();
                case 3 -> listarAutores();
                case 4 -> listarAutoresVivos();
                case 5 -> listarLibrosPorIdioma();
                case 6 -> mostrarEstadisticas();
                case 7 -> mostrarTop10Libros();
                case 8 -> buscarAutorPorNombre();
                case 9 -> listarAutoresPorRangoNacimiento();
                case 0 -> System.out.println("Saliendo de la aplicación");
                default -> System.out.println("Opción inválida");
            }
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void buscarLibroPorTitulo() {
        System.out.print("Ingrese el título del libro: ");
        String titulo = scanner.nextLine();
        try {
            Optional<Libro> libro = literaturaServicio.buscarYRegistrarLibro(titulo);
            libro.ifPresentOrElse(
                    l -> System.out.println("Libro registrado: " + l),
                    () -> System.out.println("No se encontró el libro")
            );
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listarLibros() {
        literaturaServicio.listarLibros().forEach(System.out::println);
    }

    private void listarAutores() {
        literaturaServicio.listarAutores().forEach(System.out::println);
    }

    private void listarAutoresVivos() {
        System.out.print("Ingrese el año: ");
        try {
            int anio = Integer.parseInt(scanner.nextLine());
            literaturaServicio.listarAutoresVivos(anio).forEach(System.out::println);
        } catch (NumberFormatException e) {
            System.out.println("Por favor, ingrese un año válido");
        }
    }

    private void listarLibrosPorIdioma() {
        System.out.println("\nIdiomas disponibles:");
        System.out.println("1. Español - es");
        System.out.println("2. Inglés - en");
        System.out.println("3. Francés - fr");
        System.out.println("4. Portugués - pt");
        System.out.print("Seleccione un idioma (1-4): ");

        try {
            int idiomaOpcion = Integer.parseInt(scanner.nextLine());
            String idioma = switch (idiomaOpcion) {
                case 1 -> "es";
                case 2 -> "en";
                case 3 -> "fr";
                case 4 -> "pt";
                default -> throw new IllegalArgumentException("Opción de idioma inválida");
            };
            literaturaServicio.listarLibrosPorIdioma(idioma).forEach(System.out::println);
        } catch (NumberFormatException e) {
            System.out.println("Por favor, ingrese un número válido");
        }
    }

    private void mostrarEstadisticas() {
        var stats = literaturaServicio.generarEstadisticas();
        System.out.println("\n=== ESTADÍSTICAS ===");
        System.out.println("Total de libros: " + stats.getTotalLibros());
        System.out.println("Total de autores: " + (int)stats.getTotalAutores());
        System.out.println("Promedio de libros por autor: " +
                String.format("%.2f", stats.getPromedioLibrosPorAutor()));
        System.out.println("\nDistribución por idioma:");
        stats.getDistribucionPorIdioma()
                .forEach((idioma, cantidad) ->
                        System.out.println(idioma + ": " + cantidad + " libros"));
    }

    private void mostrarTop10Libros() {
        System.out.println("\n=== TOP 10 LIBROS MÁS DESCARGADOS ===");
        literaturaServicio.obtenerTop10Libros()
                .forEach(libro ->
                        System.out.println(libro.getTitulo() +
                                " por " + libro.getAutor() +
                                " (" + libro.getDescargas() + " descargas)"));
    }

    private void buscarAutorPorNombre() {
        System.out.print("Ingrese el nombre del autor: ");
        String nombre = scanner.nextLine();

        // Llama al servicio para buscar autores
        List<Autor> autores = literaturaServicio.buscarAutorPorNombre(nombre);
        if (autores.isEmpty()) {
            System.out.println("No se encontraron autores con ese nombre.");
        } else {
            autores.forEach(System.out::println);
        }
    }


    private void listarAutoresPorRangoNacimiento() {
        try {
            System.out.print("Ingrese el año inicial: ");
            int anioInicio = Integer.parseInt(scanner.nextLine());

            System.out.print("Ingrese el año final: ");
            int anioFin = Integer.parseInt(scanner.nextLine());

            if (anioInicio > anioFin) {
                System.out.println("El año inicial no puede ser mayor que el año final.");
                return;
            }

            // Llama al servicio para listar los autores
            List<Autor> autores = literaturaServicio.listarAutoresPorRangoNacimiento(anioInicio, anioFin);
            if (autores.isEmpty()) {
                System.out.println("No se encontraron autores en ese rango.");
            } else {
                autores.forEach(System.out::println);
            }
        } catch (NumberFormatException e) {
            System.out.println("Por favor, ingrese un número válido.");
        }
    }

}