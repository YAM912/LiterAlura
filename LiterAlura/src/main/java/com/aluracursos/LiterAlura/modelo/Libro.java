// Clase Libro
package com.aluracursos.LiterAlura.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Entidad que representa un libro en la base de datos

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título no puede estar vacío")
    private String titulo;

    @Pattern(regexp = "^(es|en|fr|pt)$", message = "Idioma no válido")
    private String idioma;

    @NotBlank(message = "El autor no puede estar vacío")
    private String autor;

    @Min(value = 0, message = "El número de descargas no puede ser negativo")
    private int descargas;

    public Libro(String titulo, String idioma, String autor, int descargas) {
        this.titulo = titulo;
        this.idioma = idioma;
        this.autor = autor;
        this.descargas = descargas;
    }

    public Libro(String title, String s, String autorNombre, Integer downloadCount) {
    }
}
