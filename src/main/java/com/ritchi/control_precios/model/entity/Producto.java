package com.ritchi.control_precios.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Column(name = "imagen_producto", columnDefinition = "LONGTEXT")
    private String imagenProducto;

    @Column(name = "imagen_producto2", columnDefinition = "LONGTEXT")
    private String imagenProducto2;

    @Column(name = "es_exclusivo")
    private Boolean esExclusivo = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "producto_cliente_exclusivo",
        joinColumns = @JoinColumn(name = "id_producto"),
        inverseJoinColumns = @JoinColumn(name = "id_cliente")
    )
    private List<Cliente> clientesExclusivos = new ArrayList<>();

    // campo legacy
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente_exclusivo")
    private Cliente clienteExclusivo;

    @Column(name = "tallas", length = 255)
    private String tallas;

    @Column(name = "creado_en")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creadoEn;

    public String getNombresClientesExclusivos() {
        if (clientesExclusivos == null || clientesExclusivos.isEmpty()) {
            return clienteExclusivo != null ? clienteExclusivo.getNombre() : "-";
        }
        return clientesExclusivos.stream()
            .map(Cliente::getNombre)
            .reduce((a, b) -> a + ", " + b)
            .orElse("-");
    }
}