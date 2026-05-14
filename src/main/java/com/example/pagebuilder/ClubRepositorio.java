package com.example.pagebuilder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository // Indica que esta interfaz es un repositorio de Spring Data JPA
public interface ClubRepositorio extends JpaRepository<ClubInformacion, Long> { 
}