package com.example.pagebuilder;

import com.example.pagebuilder.ClubInformacion; // Renombrado
import com.example.pagebuilder.ClubRepositorio; // Renombrado
import com.example.pagebuilder.Noticia; // Renombrado
import com.example.pagebuilder.NoticiaRepositorio; // Renombrado
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // Configura MockMvc para pruebas de controladores Spring
public class PruebaControladorApi { // Renombrado
    @Autowired
    private MockMvc mockMvc; // Objeto para simular peticiones HTTP
    @Autowired
    private ClubRepositorio clubRepositorio; // Renombrado
    @Autowired
    private NoticiaRepositorio noticiaRepositorio; // Renombrado
    @BeforeEach
    void configurar() { // Renombrado
        clubRepositorio.deleteAll(); // Renombrado
        noticiaRepositorio.deleteAll(); // Renombrado
        ClubInformacion clubInfo = new ClubInformacion(); // Renombrado
        clubInfo.setNombre("Club de Prueba"); // Renombrado
        clubInfo.setUrlLogo("http://test.com/logo.png"); // Renombrado
        clubRepositorio.save(clubInfo); // Renombrado
        Noticia noticia1 = new Noticia(); // Renombrado
        noticia1.setTitulo("Noticia de Prueba 1"); // Renombrado
        noticia1.setContenido("Contenido de la noticia 1"); // Renombrado
        noticiaRepositorio.save(noticia1); // Renombrado
        Noticia noticia2 = new Noticia(); // Renombrado
        noticia2.setTitulo("Noticia de Prueba 2"); // Renombrado
        noticia2.setContenido("Contenido de la noticia 2"); // Renombrado
        noticiaRepositorio.save(noticia2); // Renombrado
    }
    @Test
    void probarObtenerInfoClub() throws Exception { // Renombrado
        mockMvc.perform(get("/api/club").contentType(MediaType.APPLICATION_JSON)) // Realiza una petición GET a /api/club
                .andExpect(status().isOk()) // Espera un estado HTTP 200 OK
                .andExpect(jsonPath("$.nombre").value("Club de Prueba")) // Verifica el nombre del club en el JSON (Renombrado)
                .andExpect(jsonPath("$.urlLogo").value("http://test.com/logo.png")); // Verifica la URL del logo en el JSON (Renombrado)
    }
    @Test
    void probarObtenerNoticias() throws Exception { // Renombrado
        mockMvc.perform(get("/api/posts").contentType(MediaType.APPLICATION_JSON)) // Realiza una petición GET a /api/posts
                .andExpect(status().isOk()) // Espera un estado HTTP 200 OK
                .andExpect(jsonPath("$[0].titulo").value("Noticia de Prueba 1")) // Verifica el título de la primera noticia (Renombrado)
                .andExpect(jsonPath("$[1].titulo").value("Noticia de Prueba 2")); // Verifica el título de la segunda noticia (Renombrado)
    }
}