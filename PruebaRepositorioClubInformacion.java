package com.example.pagebuilder;

import com.example.pagebuilder.ClubInformacion; // Renombrado
import com.example.pagebuilder.ClubRepositorio; // Renombrado
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals; // Importa la aserción para igualdad
import static org.junit.jupiter.api.Assertions.assertNotNull; // Importa la aserción para no nulo
import static org.junit.jupiter.api.Assertions.assertTrue; // Importa la aserción para verdadero

@DataJpaTest
public class PruebaRepositorioClubInformacion { // Renombrado
    @Autowired
    private TestEntityManager gestorEntidadesPrueba; // Renombrado
    @Autowired
    private ClubRepositorio clubRepositorio; // Renombrado
    @Test
    void probarGuardarYBuscarClubInformacion() { // Renombrado
        ClubInformacion clubInfo = new ClubInformacion(); // Renombrado
        clubInfo.setNombre("Mi Club de Baloncesto"); // Renombrado
        clubInfo.setUrlLogo("http://example.com/logo.png"); // Renombrado
        clubInfo.setFacebook("https://facebook.com/miclub"); // Establece la URL de Facebook
        clubInfo.setColorFondoEncabezado("#FF0000"); // Renombrado
        ClubInformacion infoGuardada = clubRepositorio.save(clubInfo); // Renombrado
        assertNotNull(infoGuardada.getId()); // Verifica que el ID no sea nulo después de guardar
        ClubInformacion infoEncontrada = clubRepositorio.findById(infoGuardada.getId()).orElse(null); // Renombrado
        assertNotNull(infoEncontrada); // Verifica que la información encontrada no sea nula
        assertEquals("Mi Club de Baloncesto", infoEncontrada.getNombre()); // Verifica el nombre del club
        assertEquals("http://example.com/logo.png", infoEncontrada.getUrlLogo()); // Verifica la URL del logo
    }
}