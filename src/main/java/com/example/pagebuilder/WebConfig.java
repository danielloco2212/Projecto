package com.example.pagebuilder;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String UPLOAD_DIR = "uploads";
    private static final String STORAGE_PATH = System.getProperty("user.home") + File.separator + ".adminbaloncesto";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadAbsolutePath = Paths.get(STORAGE_PATH, UPLOAD_DIR).toFile().getAbsolutePath();
        registry.addResourceHandler("/" + UPLOAD_DIR + "/**")
                .addResourceLocations("file:" + uploadAbsolutePath + File.separator);
    }
}