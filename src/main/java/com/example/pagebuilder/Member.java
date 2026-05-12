package com.example.pagebuilder;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String role;
    private String photoUrl;

    @ManyToOne
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private Team team;

    // ✅ Campo extra para recibir el teamId desde Android
    @Transient // No se guarda en BD, solo para recibir el JSON
    private Long teamId;
}