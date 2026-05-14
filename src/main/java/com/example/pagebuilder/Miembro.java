package com.example.pagebuilder;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity // Indica que esta clase es una entidad JPA
@Data @NoArgsConstructor @AllArgsConstructor @Builder //Lombok para getters, setters, constructores y builder
public class Miembro { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 
    private String nombre;
    private String rol; 
    private String urlFoto; 

    @ManyToOne // Relación muchos a uno con Equipo
    @ToString.Exclude
    @EqualsAndHashCode.Exclude // Excluye 'equipo' de equals/hashCode
    @JsonIgnore
    private Equipo equipo; 

    
    @Transient
    private Long idEquipo; 

    // Métodos de compatibilidad para la web
    public String getName() { return nombre; }
    public String getRole() { return rol; }
    public String getPhotoUrl() { return urlFoto; }
}