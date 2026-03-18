package com.ritchi.control_precios.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "cliente_producto", indexes = {
    @Index(name = "idx_cp_cliente", columnList = "id_cliente"),
    @Index(name = "idx_cp_producto", columnList = "id_producto"),
    @Index(name = "idx_cp_estado", columnList = "id_cliente,estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteProducto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente_producto")
    private Integer idClienteProducto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "estilo_cliente", length = 100)
    private String estiloCliente;

    @Column(name = "precio_actual", precision = 10, scale = 2)
    private BigDecimal precioActual;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "estado", length = 20)
    private String estado = "ACTIVO";

    @Column(name = "fecha_cotizacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCotizacion;

    @Column(name = "creado_en")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creadoEn;

    @Column(name = "actualizado_en")
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualizadoEn;
}
