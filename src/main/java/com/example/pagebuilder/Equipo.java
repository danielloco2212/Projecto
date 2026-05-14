package com.example.pagebuilder;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder // Anotaciones de Lombok para getters, setters, constructores y builder
public class Equipo { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Generación automática del ID
    private Long id;
    private String nombre;
    private String categoria; 
    
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL) 
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Miembro> miembros; // Lista de miembros del equipo (antes 'members')

    // Métodos de compatibilidad para la web
    public String getName() { return nombre; }
    public String getCategory() { return categoria; }
    public List<Miembro> getMembers() { return miembros; }
}