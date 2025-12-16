package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.entity.Cliente;
import com.ritchi.control_precios.model.entity.ClienteTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    List<Cliente> findByClienteTipo(ClienteTipo clienteTipo);
    List<Cliente> findByClienteTipoIdClienteTipo(Integer idClienteTipo);
    Optional<Cliente> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}