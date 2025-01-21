package com.aluracursos.LiterAlura.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// Configuración para el cliente HTTP WebClient
// Proporciona un builder para crear instancias de WebClient

@Configuration
public class WebClienteConfiguracion {

    //Bean que proporciona un builder para WebClient
    // @return WebClient.Builder configurado

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
