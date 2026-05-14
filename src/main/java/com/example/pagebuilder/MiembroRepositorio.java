package com.example.pagebuilder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository // Indica que esta interfaz es un repositorio de Spring Data JPA
public interface MiembroRepositorio extends JpaRepository<Miembro, Long> { 
    
    List<Miembro> findByEquipo_Id(Long idEquipo); // Busca miembros por el ID del equipo (antes findByTeam_Id)
}