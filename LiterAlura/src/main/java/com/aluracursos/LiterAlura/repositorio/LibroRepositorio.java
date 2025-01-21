package com.aluracursos.LiterAlura.repositorio;

import com.aluracursos.LiterAlura.modelo.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Repositorio para realizar operaciones CRUD con la entidad Libro
public interface LibroRepositorio extends JpaRepository<Libro, Long> {

    // Búsqueda de libros por título parcial
    List<Libro> findByTituloContainingIgnoreCase(String titulo);

    // Búsqueda de libros por idioma
    List<Libro> findByIdioma(String idioma);
}