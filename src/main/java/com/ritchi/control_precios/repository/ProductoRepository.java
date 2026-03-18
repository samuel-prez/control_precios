package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByCodigoEstiloContainingIgnoreCase(String codigoEstilo);

    List<Producto> findByCodigoPrototipoContainingIgnoreCase(String codigoPrototipo);

    List<Producto> findByDescripcionContainingIgnoreCase(String descripcion);

    Optional<Producto> findByCodigoEstilo(String codigoEstilo);

    Optional<Producto> findByCodigoPrototipo(String codigoPrototipo);

    @Query(value = "SELECT DISTINCT p.* FROM producto p " +
           "LEFT JOIN producto_cliente_exclusivo pce ON p.id_producto = pce.id_producto " +
           "WHERE p.es_exclusivo = 0 OR p.es_exclusivo IS NULL " +
           "OR (p.id_cliente_exclusivo IS NOT NULL AND p.id_cliente_exclusivo = :idCliente) " +
           "OR pce.id_cliente = :idCliente", nativeQuery = true)
    List<Producto> findProductosDisponiblesParaCliente(@Param("idCliente") Integer idCliente);

    @Query(value = "SELECT * FROM producto p WHERE p.es_exclusivo = 0 OR p.es_exclusivo IS NULL", nativeQuery = true)
    List<Producto> findProductosNoExclusivos();
}