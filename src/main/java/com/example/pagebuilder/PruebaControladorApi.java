package com.example.pagebuilder;

import com.example.pagebuilder.ClubInformacion; 
import com.example.pagebuilder.ClubRepositorio; 
import com.example.pagebuilder.Noticia; 
import com.example.pagebuilder.NoticiaRepositorio; 
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

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testapi;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.show-sql=true",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc // Configura MockMvc para pruebas de controladores Spring
public class PruebaControladorApi { 
    @Autowired
    private MockMvc mockMvc; // Objeto para simular peticiones HTTP
    @Autowired
    private ClubRepositorio clubRepositorio; 
    @Autowired
    private NoticiaRepositorio noticiaRepositorio; 
    @BeforeEach
    void configurar() {  
        clubRepositorio.deleteAll(); 
        noticiaRepositorio.deleteAll();  
        
        clubRepositorio.save(ClubInformacion.builder()
                .nombre("Club de Prueba")
                .urlLogo("http://test.com/logo.png")
                .build());
                
        Noticia noticia1 = new Noticia(); 
        noticia1.setTitulo("Noticia de Prueba 1"); 
        noticia1.setContenido("Contenido de la noticia 1"); 
        noticiaRepositorio.save(noticia1); 
        Noticia noticia2 = new Noticia(); 
        noticia2.setTitulo("Noticia de Prueba 2"); 
        noticia2.setContenido("Contenido de la noticia 2"); 
        noticiaRepositorio.save(noticia2); 
    }
    @Test
    void probarObtenerInfoClub() throws Exception { 
        mockMvc.perform(get("/api/club").contentType(MediaType.APPLICATION_JSON)) // Realiza una petición GET a /api/club
                .andExpect(status().isOk()) // Espera un estado HTTP 200 OK
                .andExpect(jsonPath("$.nombre").value("Club de Prueba")) // Verifica el nombre del club en el JSON 
                .andExpect(jsonPath("$.urlLogo").value("http://test.com/logo.png")); // Verifica la URL del logo en el JSON 
    }
    @Test
    void probarObtenerNoticias() throws Exception { 
        mockMvc.perform(get("/api/posts").contentType(MediaType.APPLICATION_JSON)) // Realiza una petición GET a /api/posts
                .andExpect(status().isOk()) // Espera un estado HTTP 200 OK
                .andExpect(jsonPath("$[0].titulo").value("Noticia de Prueba 2")) // Verifica la noticia más reciente primero
                .andExpect(jsonPath("$[1].titulo").value("Noticia de Prueba 1")); // Verifica la noticia más antigua después
    }
}