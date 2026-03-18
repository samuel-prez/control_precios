package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.entity.ClienteProductoColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteProductoColorRepository extends JpaRepository<ClienteProductoColor, Integer> {

    List<ClienteProductoColor> findByClienteProducto_IdClienteProducto(Integer idClienteProducto);

    List<ClienteProductoColor> findByClienteProducto_IdClienteProductoAndActivoTrue(Integer idClienteProducto);

    Optional<ClienteProductoColor> findByClienteProducto_IdClienteProductoAndProductoColor_IdProductoColor(
            Integer idClienteProducto, Integer idProductoColor);

    @Query("SELECT cpc FROM ClienteProductoColor cpc " +
           "WHERE cpc.clienteProducto.idClienteProducto = :idClienteProducto " +
           "AND cpc.activo = true")
    List<ClienteProductoColor> findColoresActivosPorClienteProducto(@Param("idClienteProducto") Integer idClienteProducto);

    @Query("SELECT cpc.productoColor.idProductoColor FROM ClienteProductoColor cpc " +
           "WHERE cpc.clienteProducto.idClienteProducto = :idClienteProducto " +
           "AND cpc.activo = true")
    List<Integer> findIdsColoresActivosPorClienteProducto(@Param("idClienteProducto") Integer idClienteProducto);

    void deleteByClienteProducto_IdClienteProducto(Integer idClienteProducto);

    void deleteByProductoColor_IdProductoColor(Integer idProductoColor);

    @Query("SELECT cpc FROM ClienteProductoColor cpc " +
           "JOIN FETCH cpc.clienteProducto cp " +
           "JOIN FETCH cp.cliente c " +
           "JOIN FETCH cp.producto p " +
           "JOIN FETCH cpc.productoColor pc " +
           "JOIN FETCH pc.color col " +
           "WHERE cpc.nombreEstado = 'PENDIENTE' AND cpc.activo = true " +
           "ORDER BY c.nombre, p.codigoEstilo")
    List<ClienteProductoColor> findPendientesConDetalle();

    @Query("SELECT cpc FROM ClienteProductoColor cpc " +
           "JOIN FETCH cpc.clienteProducto cp " +
           "JOIN FETCH cp.cliente c " +
           "JOIN FETCH cp.producto p " +
           "WHERE cpc.nombreEstado IN ('PENDIENTE', 'NEGOCIANDO') AND cpc.activo = true " +
           "ORDER BY c.nombre, p.codigoEstilo")
    List<ClienteProductoColor> findPendientesYNegociadosSinDetalle();
}
