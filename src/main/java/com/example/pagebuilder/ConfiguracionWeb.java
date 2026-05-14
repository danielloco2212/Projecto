package com.example.pagebuilder;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    private static final String DIRECTORIO_SUBIDAS = "uploads"; 
    private static final String RUTA_ALMACENAMIENTO = System.getProperty("user.home") + File.separator + ".adminbaloncesto"; 

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String rutaAbsolutaSubidas = Paths.get(RUTA_ALMACENAMIENTO, DIRECTORIO_SUBIDAS).toFile().getAbsolutePath(); 
        registry.addResourceHandler("/" + DIRECTORIO_SUBIDAS + "/**") 
                .addResourceLocations("file:" + rutaAbsolutaSubidas + File.separator); 
    }
}