package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.entity.ClienteTipo;
import com.ritchi.control_precios.repository.ClienteTipoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClienteTipoService {

    private final ClienteTipoRepository clienteTipoRepository;

    public ClienteTipoService(ClienteTipoRepository clienteTipoRepository) {
        this.clienteTipoRepository = clienteTipoRepository;
    }

    public List<ClienteTipo> obtenerTodosTipos() {
        return clienteTipoRepository.findAll();
    }

    public ClienteTipo obtenerPorId(Integer id) {
        return clienteTipoRepository.findById(id).orElse(null);
    }

    public ClienteTipo obtenerPorNombre(String nombre) {
        return clienteTipoRepository.findByNombre(nombre).orElse(null);
    }
}