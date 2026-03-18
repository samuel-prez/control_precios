package com.ritchi.control_precios.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "cliente_producto_color", indexes = {
    @Index(name = "idx_cpc_cliente_producto", columnList = "id_cliente_producto"),
    @Index(name = "idx_cpc_producto_color", columnList = "id_producto_color"),
    @Index(name = "idx_cpc_activo", columnList = "id_cliente_producto,activo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteProductoColor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente_producto_color")
    private Integer idClienteProductoColor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente_producto", nullable = false)
    private ClienteProducto clienteProducto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto_color", nullable = false)
    private ProductoColor productoColor;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "creado_en")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creadoEn;

    @Column(name = "valor_inicial", precision = 18, scale = 2)
    private BigDecimal valorInicial;

    @Column(name = "valor_negociado", precision = 18, scale = 2)
    private BigDecimal valorNegociado;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @Column(name = "nombre_estado", length = 50)
    private String nombreEstado = "SIN PRECIO";

    @Column(name = "fecha_solicitud")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaSolicitud;

    @Column(name = "vigencia_inicio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date vigenciaInicio;

    @Column(name = "vigencia_final")
    @Temporal(TemporalType.TIMESTAMP)
    private Date vigenciaFinal;
}
