package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    
    // Buscar por códigos
    Optional<Producto> findByCodigoEstilo(String codigoEstilo);
    Optional<Producto> findByCodigoPrototipo(String codigoPrototipo);
    
    // Buscar por código estilo o prototipo
    @Query("SELECT p FROM Producto p WHERE p.codigoEstilo = :codigo OR p.codigoPrototipo = :codigo")
    Optional<Producto> findByCodigo(@Param("codigo") String codigo);
    
    // Validaciones
    boolean existsByCodigoEstilo(String codigoEstilo);
    boolean existsByCodigoPrototipo(String codigoPrototipo);
}