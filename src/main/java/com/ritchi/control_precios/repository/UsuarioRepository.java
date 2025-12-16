package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    Optional<Usuario> findByNombre(String nombre);
    
    // AGREGAR ESTE MÉTODO NUEVO:
    Optional<Usuario> findByEmail(String email);
    
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.rol WHERE u.nombre = :username")
    Optional<Usuario> findByUsernameWithRole(@Param("username") String username);
    
    boolean existsByNombre(String nombre);
    boolean existsByEmail(String email);
}