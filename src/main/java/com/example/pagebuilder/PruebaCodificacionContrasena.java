package com.example.pagebuilder;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PruebaCodificacionContrasena { 
    @Test
    void probarCodificacionContrasena() { 
        BCryptPasswordEncoder codificadorContrasena = new BCryptPasswordEncoder(); 
        String contrasenaSinCodificar = "mySecretPassword123"; 
        
        String contrasenaCodificada = codificadorContrasena.encode(contrasenaSinCodificar); 
        assertNotNull(contrasenaCodificada); // Asegura que la contraseña codificada no sea nula
        assertTrue(contrasenaCodificada.length() > 0); // Asegura que la contraseña codificada no esté vacía
        assertTrue(codificadorContrasena.matches(contrasenaSinCodificar, contrasenaCodificada)); // Verifica que la contraseña sin codificar coincida con la codificada
        String segundaCodificacion = codificadorContrasena.encode(contrasenaSinCodificar); 
        assertNotEquals(contrasenaCodificada, segundaCodificacion, "Los hashes deberían ser diferentes gracias al salt"); 
        assertTrue(codificadorContrasena.matches(contrasenaSinCodificar, segundaCodificacion)); // Verifica que la contraseña sin codificar coincida con la segunda codificación
    }
}