package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    
    List<Cliente> findByClienteTipo_IdClienteTipo(Integer idClienteTipo);
    
    Optional<Cliente> findByNombre(String nombre);
    
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
    
    boolean existsByCorreo(String correo);
}