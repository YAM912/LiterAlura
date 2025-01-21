// Archivo principal
package com.aluracursos.LiterAlura;

import com.aluracursos.LiterAlura.menuUsuario.Menu;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

// Clase principal que inicia la aplicación Spring Boot
// @SpringBootApplication combina @Configuration, @EnableAutoConfiguration y @ComponentScan

@SpringBootApplication
public class LiterAluraAplicacion {
	public static void main(String[] args) {
		// Inicia el contexto de Spring y obtiene el contenedor de beans
		ApplicationContext context = SpringApplication.run(LiterAluraAplicacion.class, args);

		// Muestra mensaje de inicio
		System.out.println("Aplicación iniciada. Mostrando el menú...");

		// Obtiene el bean de Menu del contexto y lo ejecuta
		Menu menu = context.getBean(Menu.class);
		menu.mostrarMenu();
	}
}