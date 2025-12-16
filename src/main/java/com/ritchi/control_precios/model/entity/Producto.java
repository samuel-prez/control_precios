package com.ritchi.control_precios.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "codigo_prototipo", length = 50)
    private String codigoPrototipo;

    @Column(name = "codigo_estilo", length = 50)
    private String codigoEstilo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "imagen_producto", columnDefinition = "TEXT")
    private String imagenProducto;

    @Column(name = "creado_en")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creadoEn;
}