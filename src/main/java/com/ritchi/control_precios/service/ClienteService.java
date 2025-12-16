package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.entity.Cliente;
import com.ritchi.control_precios.model.entity.ClienteTipo;
import com.ritchi.control_precios.repository.ClienteRepository;
import com.ritchi.control_precios.repository.ClienteTipoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteTipoRepository clienteTipoRepository;

    public ClienteService(ClienteRepository clienteRepository, 
                         ClienteTipoRepository clienteTipoRepository) {
        this.clienteRepository = clienteRepository;
        this.clienteTipoRepository = clienteTipoRepository;
    }

    public List<ClienteTipo> obtenerTodosTipos() {
        return clienteTipoRepository.findAll();
    }

    public List<Cliente> obtenerClientesPorTipo(Integer idTipo) {
        return clienteRepository.findByClienteTipoIdClienteTipo(idTipo);
    }

    public void crearCliente(String nombre, String correo, Integer idClienteTipo) {
        // Validar correo único
        if (clienteRepository.existsByCorreo(correo)) {
            throw new RuntimeException("El correo '" + correo + "' ya está registrado");
        }

        // Validar formato de email
        if (!correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException("El formato del correo electrónico no es válido");
        }

        // Buscar tipo de cliente
        ClienteTipo tipo = clienteTipoRepository.findById(idClienteTipo)
            .orElseThrow(() -> new RuntimeException("Tipo de cliente no encontrado"));

        // Crear cliente
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setCorreo(correo);
        cliente.setClienteTipo(tipo);
        cliente.setCreadoEn(new Date());

        clienteRepository.save(cliente);
        System.out.println("✅ Cliente creado: " + nombre + " - Tipo: " + tipo.getNombre());
    }

    public List<Cliente> obtenerTodosClientes() {
        return clienteRepository.findAll();
    }
}