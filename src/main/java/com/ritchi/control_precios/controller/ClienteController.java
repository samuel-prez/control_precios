package com.ritchi.control_precios.controller;

import com.ritchi.control_precios.model.entity.Cliente;
import com.ritchi.control_precios.model.entity.ClienteTipo;
import com.ritchi.control_precios.service.ClienteService;
import com.ritchi.control_precios.service.ClienteTipoService;
import org.springframework.stereotype.Component;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("clienteController")
@ViewScoped
@Component
public class ClienteController implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ClienteService clienteService;
    private final ClienteTipoService clienteTipoService;

    private List<ClienteTipo> tiposCliente;
    private List<Cliente> clientesFiltrados;
    private ClienteTipo tipoSeleccionado;
    private Cliente clienteSeleccionado;
    private Cliente nuevoCliente;

    public ClienteController(ClienteService clienteService, ClienteTipoService clienteTipoService) {
        this.clienteService = clienteService;
        this.clienteTipoService = clienteTipoService;
    }

    @PostConstruct
    public void init() {
        cargarTiposCliente();
        clientesFiltrados = new ArrayList<>();
        nuevoCliente = new Cliente();
    }

    private void cargarTiposCliente() {
        try {
            tiposCliente = clienteTipoService.obtenerTodosTipos();
        } catch (Exception e) {
            tiposCliente = new ArrayList<>();
        }
    }

    public void seleccionarTipoPorNombre(String nombreTipo) {
        try {
            ClienteTipo tipo = clienteTipoService.obtenerPorNombre(nombreTipo);
            if (tipo != null) {
                seleccionarTipo(tipo);
            }
        } catch (Exception e) {
            // ignorado
        }
    }

    public void seleccionarTipo(ClienteTipo tipo) {
        this.tipoSeleccionado = tipo;
        cargarClientesPorTipo(tipo.getIdClienteTipo());
        nuevoCliente = new Cliente();
    }

    private void cargarClientesPorTipo(Integer idTipo) {
        try {
            clientesFiltrados = clienteService.obtenerClientesPorTipo(idTipo);
        } catch (Exception e) {
            clientesFiltrados = new ArrayList<>();
        }
    }

    public void crearCliente() {
        try {
            if (nuevoCliente.getNombre() == null || nuevoCliente.getNombre().trim().isEmpty()) {
                mostrarError("El nombre del cliente es obligatorio");
                return;
            }

            if (nuevoCliente.getCorreo() != null && !nuevoCliente.getCorreo().trim().isEmpty()) {
                if (!nuevoCliente.getCorreo().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    mostrarError("El formato del correo electrónico no es válido");
                    return;
                }
            }

            nuevoCliente.setClienteTipo(tipoSeleccionado);
            nuevoCliente.setCreadoEn(new Date());
            clienteService.crearCliente(nuevoCliente);

            mostrarExito("Cliente creado correctamente");
            cargarClientesPorTipo(tipoSeleccionado.getIdClienteTipo());
            nuevoCliente = new Cliente();

        } catch (Exception e) {
            mostrarError("Error al crear cliente: " + e.getMessage());
        }
    }

    public void seleccionarCliente(Cliente cliente) {
        this.clienteSeleccionado = cliente;
    }

    public void eliminarCliente(Integer idCliente) {
        try {
            clienteService.eliminarCliente(idCliente);
            mostrarExito("Cliente eliminado correctamente");
            if (tipoSeleccionado != null) {
                cargarClientesPorTipo(tipoSeleccionado.getIdClienteTipo());
            }
        } catch (Exception e) {
            mostrarError("Error al eliminar cliente: " + e.getMessage());
        }
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_WARN, titulo, mensaje));
    }

    private void mostrarExito(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensaje));
    }

    private void mostrarError(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", mensaje));
    }

    public List<ClienteTipo> getTiposCliente() {
        return tiposCliente;
    }

    public void setTiposCliente(List<ClienteTipo> tiposCliente) {
        this.tiposCliente = tiposCliente;
    }

    public List<Cliente> getClientesFiltrados() {
        return clientesFiltrados;
    }

    public void setClientesFiltrados(List<Cliente> clientesFiltrados) {
        this.clientesFiltrados = clientesFiltrados;
    }

    public ClienteTipo getTipoSeleccionado() {
        return tipoSeleccionado;
    }

    public void setTipoSeleccionado(ClienteTipo tipoSeleccionado) {
        this.tipoSeleccionado = tipoSeleccionado;
    }

    public Cliente getClienteSeleccionado() {
        return clienteSeleccionado;
    }

    public void setClienteSeleccionado(Cliente clienteSeleccionado) {
        this.clienteSeleccionado = clienteSeleccionado;
    }

    public Cliente getNuevoCliente() {
        return nuevoCliente;
    }

    public void setNuevoCliente(Cliente nuevoCliente) {
        this.nuevoCliente = nuevoCliente;
    }
}