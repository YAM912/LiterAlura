package com.aluracursos.LiterAlura.modelo.gutendex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GutendexLibro {
    private Long id;
    private String title;
    private List<GutendexAutor> authors;
    private List<String> languages;

    @JsonProperty("Numero de descargas")
    private Integer downloadCount;
}
