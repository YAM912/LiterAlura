package com.aluracursos.LiterAlura.modelo.gutendex;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GutendexRespuesta {
    private int count;
    private String next;
    private String previous;
    private List<GutendexLibro> results;
}
