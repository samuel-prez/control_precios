package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByNombre(String nombre);

    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.rol WHERE u.nombre = :username")
    Optional<Usuario> findByUsernameWithRole(@Param("username") String username);

    @Query("SELECT u FROM Usuario u JOIN u.rol r WHERE r.nombre = :rolNombre")
    List<Usuario> findByRolNombre(@Param("rolNombre") String rolNombre);

    boolean existsByNombre(String nombre);
    boolean existsByEmail(String email);
}