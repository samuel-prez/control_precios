package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.entity.Precio;
import com.ritchi.control_precios.model.entity.Cliente;
import com.ritchi.control_precios.model.entity.ProductoColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PrecioRepository extends JpaRepository<Precio, Integer> {
    
    // Obtener todos los precios de un cliente
    List<Precio> findByCliente(Cliente cliente);
    List<Precio> findByClienteIdCliente(Integer idCliente);
    
    // Obtener precios aprobados
    List<Precio> findByClienteIdClienteAndEstadoTrue(Integer idCliente);
    
    // Obtener precios pendientes
    List<Precio> findByClienteIdClienteAndEstadoFalse(Integer idCliente);
    
    // Buscar por producto color
    List<Precio> findByProductoColor(ProductoColor productoColor);
    
    // Buscar si ya existe cotización para cliente y producto color
    @Query("SELECT p FROM Precio p WHERE p.cliente.idCliente = :idCliente " +
           "AND p.productoColor.idProductoColor = :idProductoColor " +
           "AND p.estado = false")
    List<Precio> findPendientesByClienteAndProductoColor(
        @Param("idCliente") Integer idCliente, 
        @Param("idProductoColor") Integer idProductoColor
    );
}