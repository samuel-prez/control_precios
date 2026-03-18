package com.ritchi.control_precios.model.dto;

import java.io.Serializable;

public class AlertaProductoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String clienteNombre;
    private String codigoEstilo;
    private String codigoPrototipo;
    private String descripcion;
    private long totalPendiente;
    private long totalNegociando;

    public AlertaProductoDTO(String clienteNombre, String codigoEstilo, String codigoPrototipo,
                              String descripcion, long totalPendiente, long totalNegociando) {
        this.clienteNombre = clienteNombre;
        this.codigoEstilo = codigoEstilo;
        this.codigoPrototipo = codigoPrototipo;
        this.descripcion = descripcion;
        this.totalPendiente = totalPendiente;
        this.totalNegociando = totalNegociando;
    }

    public String getClienteNombre()    { return clienteNombre; }
    public String getCodigoEstilo()     { return codigoEstilo; }
    public String getCodigoPrototipo()  { return codigoPrototipo; }
    public String getDescripcion()      { return descripcion; }
    public long getTotalPendiente()     { return totalPendiente; }
    public long getTotalNegociando()    { return totalNegociando; }

    /** "EST001 / PROT-A" o solo "EST001" si no hay prototipo */
    public String getCodigoCompleto() {
        if (codigoPrototipo != null && !codigoPrototipo.trim().isEmpty()) {
            return codigoEstilo + " / " + codigoPrototipo;
        }
        return codigoEstilo != null ? codigoEstilo : "";
    }
}
