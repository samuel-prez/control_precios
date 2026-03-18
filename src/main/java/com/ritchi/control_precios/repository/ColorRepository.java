package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColorRepository extends JpaRepository<Color, Integer> {

    List<Color> findAllByOrderByNombreAsc();

    Color findByNombreIgnoreCase(String nombre);
}
