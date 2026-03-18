package com.ritchi.control_precios.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class ColorPrecioDTO implements Serializable {

    private Integer idColor;
    private String nombreColor;
    private String codigoHex;
    private Integer idProductoColor;
    private String codigoColor;
    private Boolean activo;
    private Integer idClienteProductoColor;
    private boolean seleccionado;

    // Campos de precio
    private BigDecimal valorInicial;
    private BigDecimal valorNegociado;
    private String observaciones;
    private String nombreEstado;
    private Date fechaSolicitud;
    private Date vigenciaInicio;
    private Date vigenciaFinal;

    /** Constructor básico – usado por la query sin datos de precio */
    public ColorPrecioDTO(Integer idColor, String nombreColor, String codigoHex,
                          Integer idProductoColor, String codigoColor, Boolean activo,
                          Integer idClienteProductoColor) {
        this.idColor = idColor;
        this.nombreColor = nombreColor;
        this.codigoHex = codigoHex;
        this.idProductoColor = idProductoColor;
        this.codigoColor = codigoColor;
        this.activo = activo;
        this.idClienteProductoColor = idClienteProductoColor;
    }

    /** Constructor completo – usado por la query con datos de precio */
    public ColorPrecioDTO(Integer idColor, String nombreColor, String codigoHex,
                          Integer idProductoColor, String codigoColor, Boolean activo,
                          Integer idClienteProductoColor,
                          BigDecimal valorInicial, BigDecimal valorNegociado,
                          String observaciones, String nombreEstado,
                          Date fechaSolicitud, Date vigenciaFinal) {
        this(idColor, nombreColor, codigoHex, idProductoColor, codigoColor, activo, idClienteProductoColor);
        this.valorInicial = valorInicial;
        this.valorNegociado = valorNegociado;
        this.observaciones = observaciones;
        this.nombreEstado = nombreEstado;
        this.fechaSolicitud = fechaSolicitud;
        this.vigenciaFinal = vigenciaFinal;
    }
}
