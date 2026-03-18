package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.dto.ProductoAgrupadoDTO;
import com.ritchi.control_precios.model.dto.ProductoClienteDTO;
import com.ritchi.control_precios.model.entity.ClienteProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteProductoRepository extends JpaRepository<ClienteProducto, Integer> {

    List<ClienteProducto> findByCliente_IdCliente(Integer idCliente);

    List<ClienteProducto> findByCliente_IdClienteAndEstado(Integer idCliente, String estado);

    List<ClienteProducto> findByCliente_IdClienteAndEstiloClienteContainingIgnoreCase(Integer idCliente, String estiloCliente);

    @Query("SELECT cp FROM ClienteProducto cp WHERE cp.cliente.idCliente = :idCliente AND " +
           "(cp.producto.codigoEstilo LIKE %:codigo% OR cp.producto.codigoPrototipo LIKE %:codigo% OR cp.estiloCliente LIKE %:codigo%)")
    List<ClienteProducto> buscarPorCodigoOEstilo(@Param("idCliente") Integer idCliente, @Param("codigo") String codigo);

    Optional<ClienteProducto> findByCliente_IdClienteAndProducto_IdProducto(Integer idCliente, Integer idProducto);

    List<ClienteProducto> findByProductoIdProducto(Integer idProducto);

    List<ClienteProducto> findByCliente_IdClienteOrderByFechaCotizacionDesc(Integer idCliente);

    @Query("SELECT new com.ritchi.control_precios.model.dto.ProductoClienteDTO(" +
           "p.idProducto, p.codigoEstilo, p.codigoPrototipo, p.descripcion, p.imagenProducto, " +
           "pc.idProductoColor, col.nombre, " +
           "cp.idClienteProducto, cp.estiloCliente, cp.precioActual, cp.observaciones, cp.estado, cp.fechaCotizacion, " +
           "c.idCliente, c.nombre) " +
           "FROM ClienteProducto cp " +
           "JOIN cp.producto p " +
           "JOIN cp.cliente c " +
           "LEFT JOIN ProductoColor pc ON pc.producto.idProducto = p.idProducto " +
           "LEFT JOIN pc.color col " +
           "WHERE c.idCliente = :idCliente " +
           "ORDER BY cp.fechaCotizacion DESC, p.codigoEstilo")
    List<ProductoClienteDTO> obtenerVistaCompletaProductosCliente(@Param("idCliente") Integer idCliente);

    @Query("SELECT new com.ritchi.control_precios.model.dto.ProductoAgrupadoDTO(" +
           "p.idProducto, " +
           "p.codigoEstilo, " +
           "p.codigoPrototipo, " +
           "p.descripcion, " +
           "p.imagenProducto, " +
           "p.imagenProducto2, " +
           "cp.idClienteProducto, " +
           "cp.estiloCliente, " +
           "cp.observaciones, " +
           "cp.estado, " +
           "cp.creadoEn, " +
           "CASE WHEN (SELECT COUNT(cpc) FROM ClienteProductoColor cpc WHERE cpc.clienteProducto.idClienteProducto = cp.idClienteProducto AND cpc.activo = true) > 0 " +
           "THEN (SELECT COUNT(cpc) FROM ClienteProductoColor cpc WHERE cpc.clienteProducto.idClienteProducto = cp.idClienteProducto AND cpc.activo = true) " +
           "ELSE (SELECT COUNT(pc) FROM ProductoColor pc WHERE pc.producto.idProducto = p.idProducto AND pc.activo = true) END, " +
           "(SELECT COUNT(cpc2) FROM ClienteProductoColor cpc2 WHERE cpc2.clienteProducto.idClienteProducto = cp.idClienteProducto AND cpc2.activo = true AND cpc2.valorInicial IS NOT NULL), " +
           "'SIN PRECIO') " +
           "FROM ClienteProducto cp " +
           "JOIN cp.producto p " +
           "WHERE cp.cliente.idCliente = :idCliente " +
           "ORDER BY cp.idClienteProducto DESC")
    List<ProductoAgrupadoDTO> obtenerProductosAgrupadosCliente(@Param("idCliente") Integer idCliente);
}
