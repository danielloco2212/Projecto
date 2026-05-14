package com.example.pagebuilder;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder // Utilizamos Lombok para getters, setters, constructores y builder
public class Imagen { 
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 
    private String nombreArchivo; 
    private String urlImagen; 
}