package com.example.pagebuilder;

import com.example.pagebuilder.ClubInformacion; 
import com.example.pagebuilder.ClubRepositorio; 
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals; // Importa la aserción para igualdad
import static org.junit.jupiter.api.Assertions.assertNotNull; // Importa la aserción para no nulo
import static org.junit.jupiter.api.Assertions.assertTrue; // Importa la aserción para verdadero

@DataJpaTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.show-sql=true",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
public class PruebaRepositorioClubInformacion {
    @Autowired
    private TestEntityManager gestorEntidadesPrueba; 
    @Autowired
    private ClubRepositorio clubRepositorio; 
    @Test
    void probarGuardarYBuscarClubInformacion() { 
        ClubInformacion clubInfo = new ClubInformacion(); 
        clubInfo.setNombre("Mi Club de Baloncesto"); 
        clubInfo.setUrlLogo("http://example.com/logo.png"); 
        clubInfo.setFacebook("https://facebook.com/miclub"); 
        clubInfo.setColorFondoEncabezado("#FF0000"); 
        ClubInformacion infoGuardada = clubRepositorio.save(clubInfo); 
        assertNotNull(infoGuardada.getId()); // Verifica que el ID no sea nulo después de guardar
        ClubInformacion infoEncontrada = clubRepositorio.findById(infoGuardada.getId()).orElse(null); 
        assertNotNull(infoEncontrada); // Verifica que la información encontrada no sea nula
        assertEquals("Mi Club de Baloncesto", infoEncontrada.getNombre()); // Verifica el nombre del club
        assertEquals("http://example.com/logo.png", infoEncontrada.getUrlLogo()); // Verifica la URL del logo
    }
}