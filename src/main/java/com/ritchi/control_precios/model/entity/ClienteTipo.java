package com.ritchi.control_precios.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Entity
@Table(name = "cliente_tipo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteTipo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente_tipo")
    private Integer idClienteTipo;

    @Column(name = "nombre", length = 100)
    private String nombre;
}