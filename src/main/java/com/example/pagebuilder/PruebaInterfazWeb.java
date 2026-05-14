package com.example.pagebuilder;

import com.example.pagebuilder.Application; // Importa la clase principal de la aplicación
import com.example.pagebuilder.ClubInformacion; 
import com.example.pagebuilder.ClubRepositorio; 
import com.example.pagebuilder.Noticia; 
import com.example.pagebuilder.NoticiaRepositorio; 
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class, properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testweb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.show-sql=true",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class PruebaInterfazWeb { 
    @LocalServerPort // Inyecta el puerto aleatorio en el que se ejecuta la aplicación
    private int puerto; 
    private WebDriver navegador; 
    @Autowired
    private ClubRepositorio clubRepositorio; 
    @Autowired
    private NoticiaRepositorio noticiaRepositorio;
    @BeforeAll
    static void configurarClase() { 
        WebDriverManager.chromedriver().setup(); // Configura el ChromeDriver automáticamente
    }
    @BeforeEach
    void configurar() {
        navegador = new ChromeDriver(); // Inicializa el navegador Chrome 
        clubRepositorio.deleteAll(); 
        noticiaRepositorio.deleteAll(); 
        clubRepositorio.save(ClubInformacion.builder().nombre("Club de Prueba").build());
        noticiaRepositorio.save(new Noticia(null, "Noticia Web", "Contenido de la noticia web", null)); 
    }
    @AfterEach
    void desmontar() { 
        if (navegador != null) navegador.quit(); 
    }
    @Test
    void probarCargaPaginaInicioYMuestraNombreClub() { 
        navegador.get("http://localhost:" + puerto + "/"); // Abre la URL de la aplicación 
        assertTrue(navegador.getTitle().contains("Club de Prueba")); // Verifica que el título de la página contenga "Club de Prueba"
        assertTrue(navegador.getPageSource().contains("Noticia Web")); // Verifica que el contenido de la página contenga "Noticia Web"
    }
}