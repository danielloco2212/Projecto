package com.example.pagebuilder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository // Indica que esta interfaz es un repositorio de Spring Data JPA
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> { 
    Optional<Usuario> findByUsername(String username); // Busca un usuario por su nombre de usuario
}