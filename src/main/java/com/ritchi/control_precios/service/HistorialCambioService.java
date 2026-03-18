package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.entity.HistorialCambio;
import com.ritchi.control_precios.repository.HistorialCambioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HistorialCambioService {

    private final HistorialCambioRepository historialCambioRepository;

    public HistorialCambioService(HistorialCambioRepository historialCambioRepository) {
        this.historialCambioRepository = historialCambioRepository;
    }

    public List<HistorialCambio> obtenerTodosLosCambios() {
        return historialCambioRepository.findAllByOrderByFechaCambioDesc();
    }

    public List<HistorialCambio> obtenerCambiosPrecios() {
        return historialCambioRepository.findByTipoCambioOrderByFechaCambioDesc("PRECIO");
    }

    public List<HistorialCambio> obtenerCambiosEstados() {
        return historialCambioRepository.findByTipoCambioOrderByFechaCambioDesc("ESTADO");
    }
}
