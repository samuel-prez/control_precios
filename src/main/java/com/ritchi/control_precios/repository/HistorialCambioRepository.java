package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.entity.HistorialCambio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialCambioRepository extends JpaRepository<HistorialCambio, Integer> {

    List<HistorialCambio> findAllByOrderByFechaCambioDesc();

    List<HistorialCambio> findByTipoCambioOrderByFechaCambioDesc(String tipoCambio);
}
