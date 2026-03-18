package com.ritchi.control_precios.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoAgrupadoDTO implements Serializable {

    private Integer idProducto;
    private String codigoEstilo;
    private String codigoPrototipo;
    private String descripcionProducto;
    private String imagenProducto;
    private String imagenProducto2;

    private Integer idClienteProducto;
    private String estiloCliente;
    private String observacionesCliente;
    private String estadoClienteProducto;
    private Date creadoEn;

    private Long totalColores;
    private Long coloresConPrecio;

    private String estadoGeneral;
}
