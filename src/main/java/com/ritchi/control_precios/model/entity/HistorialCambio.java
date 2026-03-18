package com.ritchi.control_precios.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "historial_cambio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCambio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Integer idHistorial;

    @Column(name = "tipo_cambio", nullable = false, length = 20)
    private String tipoCambio; // "PRECIO" o "ESTADO"

    @Column(name = "cliente_nombre", length = 100)
    private String clienteNombre;

    @Column(name = "producto_descripcion", length = 200)
    private String productoDescripcion;

    @Column(name = "color_nombre", length = 100)
    private String colorNombre;

    @Column(name = "valor_anterior", precision = 18, scale = 2)
    private BigDecimal valorAnterior;

    @Column(name = "valor_nuevo", precision = 18, scale = 2)
    private BigDecimal valorNuevo;

    @Column(name = "estado_anterior", length = 50)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", length = 50)
    private String estadoNuevo;

    @Column(name = "fecha_cambio", nullable = false)
    private Timestamp fechaCambio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}
