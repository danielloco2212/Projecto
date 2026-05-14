package com.example.pagebuilder;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder// Lombok para getters, setters, constructores y builder
public class Noticia { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Generación automática del ID
    private Long id; 
    private String titulo; 
    
    @Lob // Indica que el campo puede almacenar grandes objetos de texto
    @Column(columnDefinition = "TEXT") // Define el tipo de columna en la base de datos como TEXT
    private String contenido; 
    private String urlImagen; 

    // Métodos de compatibilidad para la web
    @JsonIgnore
    public String getTitle() { return titulo; }
    @JsonIgnore
    public String getContent() { return contenido; }
    @JsonIgnore
    public String getImageUrl() { return urlImagen; }
}