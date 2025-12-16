package com.ritchi.control_precios.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "precio_linea")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Precio implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_precioLinea")
    private Integer idPrecioLinea;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto_color")
    private ProductoColor productoColor;

    @Column(name = "valor_inicial", precision = 18, scale = 2)
    private BigDecimal valorInicial;

    @Column(name = "valor_final", precision = 18, scale = 2)
    private BigDecimal valorFinal;

    @Column(name = "fecha_solicitud")
    @Temporal(TemporalType.DATE)
    private Date fechaSolicitud;

    @Column(name = "fecha_aprobacion")
    @Temporal(TemporalType.DATE)
    private Date fechaAprobacion;

    @Column(name = "vigencia_incio")
    @Temporal(TemporalType.DATE)
    private Date vigenciaInicio;

    @Column(name = "vigencia_final")
    @Temporal(TemporalType.DATE)
    private Date vigenciaFinal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_precio")
    private Precio precioBase;

    @Column(name = "Estado")
    private Boolean estado; 
}