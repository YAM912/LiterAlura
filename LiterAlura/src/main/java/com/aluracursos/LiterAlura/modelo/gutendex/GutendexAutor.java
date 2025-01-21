package com.aluracursos.LiterAlura.modelo.gutendex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GutendexAutor {
    private String name;

    @JsonProperty("Año de nacimiento")
    private String birthYear;

    @JsonProperty("Año de muerte")
    private String deathYear;
}
