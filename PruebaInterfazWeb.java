package com.example.pagebuilder;

import com.example.pagebuilder.Application; // Importa la clase principal de la aplicación
import com.example.pagebuilder.ClubInformacion; // Renombrado
import com.example.pagebuilder.ClubRepositorio; // Renombrado
import com.example.pagebuilder.Noticia; // Renombrado
import com.example.pagebuilder.NoticiaRepositorio; // Renombrado
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class PruebaInterfazWeb { // Renombrado
    @LocalServerPort // Inyecta el puerto aleatorio en el que se ejecuta la aplicación
    private int puerto; // Renombrado
    private WebDriver navegador; // Renombrado
    @Autowired
    private ClubRepositorio clubRepositorio; // Renombrado
    @Autowired
    private NoticiaRepositorio noticiaRepositorio; // Renombrado
    @BeforeAll
    static void configurarClase() { // Renombrado
        WebDriverManager.chromedriver().setup(); // Configura el ChromeDriver automáticamente
    }
    @BeforeEach
    void configurar() { // Renombrado
        navegador = new ChromeDriver(); // Inicializa el navegador Chrome (Renombrado)
        clubRepositorio.deleteAll(); // Renombrado
        noticiaRepositorio.deleteAll(); // Renombrado
        clubRepositorio.save(ClubInformacion.builder().nombre("Club de Prueba").build());
        noticiaRepositorio.save(new Noticia(null, "Noticia Web", "Contenido de la noticia web", null)); // Renombrado
    }
    @AfterEach
    void desmontar() { // Renombrado
        if (navegador != null) navegador.quit(); // Cierra el navegador si está abierto (Renombrado)
    }
    @Test
    void probarCargaPaginaInicioYMuestraNombreClub() { // Renombrado
        navegador.get("http://localhost:" + puerto + "/"); // Abre la URL de la aplicación (Renombrado)
        assertTrue(navegador.getTitle().contains("Club de Prueba")); // Verifica que el título de la página contenga "Club de Prueba"
        assertTrue(navegador.getPageSource().contains("Noticia Web")); // Verifica que el contenido de la página contenga "Noticia Web"
    }
}