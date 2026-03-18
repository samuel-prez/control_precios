package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.entity.Color;
import com.ritchi.control_precios.repository.ColorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ColorService {

    private final ColorRepository colorRepository;

    public ColorService(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    public List<Color> obtenerColoresActivos() {
        return colorRepository.findAllByOrderByNombreAsc();
    }

    public Color obtenerColorPorId(Integer idColor) {
        return colorRepository.findById(idColor)
                .orElseThrow(() -> new RuntimeException("Color no encontrado con ID: " + idColor));
    }

    public Color obtenerColorPorNombre(String nombre) {
        return colorRepository.findByNombreIgnoreCase(nombre);
    }
}
