package com.ritchi.control_precios.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "producto_color", indexes = {
    @Index(name = "idx_pc_producto", columnList = "id_producto"),
    @Index(name = "idx_pc_color", columnList = "id_color"),
    @Index(name = "idx_pc_activo", columnList = "id_producto,activo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoColor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_color")
    private Integer idProductoColor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_color", nullable = false)
    private Color color;

    @Column(name = "codigo_color", length = 50)
    private String codigoColor;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "creado_en")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creadoEn;
}