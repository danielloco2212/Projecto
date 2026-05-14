package com.example.pagebuilder;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PruebaCodificacionContrasena { // Renombrado
    @Test
    void probarCodificacionContrasena() { // Renombrado
        BCryptPasswordEncoder codificadorContrasena = new BCryptPasswordEncoder(); // Renombrado
        String contrasenaSinCodificar = "mySecretPassword123"; // Renombrado
        
        String contrasenaCodificada = codificadorContrasena.encode(contrasenaSinCodificar); // Renombrado
        assertNotNull(contrasenaCodificada); // Asegura que la contraseña codificada no sea nula
        assertTrue(contrasenaCodificada.length() > 0); // Asegura que la contraseña codificada no esté vacía
        assertTrue(codificadorContrasena.matches(contrasenaSinCodificar, contrasenaCodificada)); // Verifica que la contraseña sin codificar coincida con la codificada
        String segundaCodificacion = codificadorContrasena.encode(contrasenaSinCodificar); // Renombrado
        assertNotEquals(contrasenaCodificada, segundaCodificacion, "Los hashes deberían ser diferentes gracias al salt"); // Los hashes deberían ser diferentes debido al salt aleatorio
        assertTrue(codificadorContrasena.matches(contrasenaSinCodificar, segundaCodificacion)); // Verifica que la contraseña sin codificar coincida con la segunda codificación
    }
}