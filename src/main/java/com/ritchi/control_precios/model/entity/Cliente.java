package com.ritchi.control_precios.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cliente_tipo")
    private ClienteTipo clienteTipo;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "correo", length = 100)
    private String correo;

    @Column(name = "creado_en")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creadoEn;
}