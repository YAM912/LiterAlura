package com.aluracursos.LiterAlura.repositorio;

import com.aluracursos.LiterAlura.modelo.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

//Repositorio para realizar operaciones CRUD con la entidad Autor

public interface AutorRepositorio extends JpaRepository<Autor, Long> {

    // Búsqueda de autores vivos en un año específico
    List<Autor> findByVivoTrueAndAnioNacimientoLessThanEqualAndAnioMuerteGreaterThanEqual(int anioNacimiento, int anioMuerte);

    // Búsqueda de autor por nombre
    Optional<Autor> findByNombre(String nombre);

    // Búsqueda de autores por nombre parcial
    List<Autor> findByNombreContainingIgnoreCase(String nombre);

    // Búsqueda de autores por rango de años de nacimiento
    List<Autor> findByAnioNacimientoBetween(int anioInicio, int anioFin);
}