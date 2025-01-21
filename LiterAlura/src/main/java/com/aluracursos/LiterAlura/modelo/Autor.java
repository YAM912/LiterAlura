package com.aluracursos.LiterAlura.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Entidad que representa un autor en la base de datos

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del autor no puede estar vacío")
    private String nombre;

    private boolean vivo;

    @Min(value = 0, message = "El año de nacimiento no puede ser negativo")
    private int anioNacimiento;

    @Min(value = 0, message = "El año de muerte no puede ser negativo")
    private int anioMuerte;

    @PrePersist
    @PreUpdate
    private void validarFechas() {
        if (anioMuerte > 0 && anioMuerte < anioNacimiento) {
            throw new IllegalStateException("El año de muerte no puede ser anterior al año de nacimiento");
        }
        vivo = anioMuerte == 0;
    }
}

