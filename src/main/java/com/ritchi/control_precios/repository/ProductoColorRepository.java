package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.dto.ColorPrecioDTO;
import com.ritchi.control_precios.model.entity.ProductoColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoColorRepository extends JpaRepository<ProductoColor, Integer> {

    List<ProductoColor> findByProducto_IdProducto(Integer idProducto);

    List<ProductoColor> findByProducto_IdProductoAndActivoTrue(Integer idProducto);

    List<ProductoColor> findByColor_NombreContainingIgnoreCase(String nombreColor);

    @Query("SELECT new com.ritchi.control_precios.model.dto.ColorPrecioDTO(" +
           "c.idColor, c.nombre, c.codigoHex, " +
           "pc.idProductoColor, pc.codigoColor, pc.activo, " +
           "CAST(null AS integer)) " +
           "FROM ProductoColor pc " +
           "JOIN pc.color c " +
           "WHERE pc.producto.idProducto = :idProducto " +
           "AND pc.activo = true " +
           "ORDER BY pc.idProductoColor DESC")
    List<ColorPrecioDTO> obtenerColoresConPreciosDelProducto(@Param("idProducto") Integer idProducto, @Param("idCliente") Integer idCliente, @Param("idClienteProducto") Integer idClienteProducto);

    @Query("SELECT new com.ritchi.control_precios.model.dto.ColorPrecioDTO(" +
           "c.idColor, c.nombre, c.codigoHex, " +
           "pc.idProductoColor, pc.codigoColor, cpc.activo, " +
           "cpc.idClienteProductoColor, " +
           "cpc.valorInicial, cpc.valorNegociado, " +
           "cpc.observaciones, cpc.nombreEstado, " +
           "cpc.fechaSolicitud, cpc.vigenciaFinal) " +
           "FROM ClienteProductoColor cpc " +
           "JOIN cpc.productoColor pc " +
           "JOIN pc.color c " +
           "WHERE cpc.clienteProducto.idClienteProducto = :idClienteProducto " +
           "AND cpc.activo = true " +
           "ORDER BY cpc.idClienteProductoColor DESC")
    List<ColorPrecioDTO> obtenerColoresConPreciosPorClienteProducto(@Param("idCliente") Integer idCliente, @Param("idClienteProducto") Integer idClienteProducto);

}
