package com.example.pagebuilder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Esto es lo que permite buscar el usuario para el login
    Optional<User> findByUsername(String username);
}