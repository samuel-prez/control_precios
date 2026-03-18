package com.ritchi.control_precios.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoClienteDTO implements Serializable {

    private Integer idProducto;
    private String codigoEstilo;
    private String codigoPrototipo;
    private String descripcionProducto;
    private String imagenProducto;

    private Integer idProductoColor;
    private String nombreColor;

    private Integer idClienteProducto;
    private String estiloCliente;
    private BigDecimal precioActualCliente;
    private String observacionesCliente;
    private String estadoClienteProducto;
    private Date fechaCotizacion;

    private Integer idCliente;
    private String nombreCliente;
}
