package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.entity.ClienteTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteTipoRepository extends JpaRepository<ClienteTipo, Integer> {
    Optional<ClienteTipo> findByNombre(String nombre);
}