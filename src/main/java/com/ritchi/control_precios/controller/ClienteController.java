package com.ritchi.control_precios.controller;

import com.ritchi.control_precios.model.entity.Cliente;
import com.ritchi.control_precios.model.entity.ClienteTipo;
import com.ritchi.control_precios.service.ClienteService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named(value = "clienteController")
@ViewScoped
public class ClienteController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClienteService clienteService;

    private List<ClienteTipo> tiposCliente;
    private ClienteTipo tipoSeleccionado;
    private List<Cliente> clientesFiltrados;
    
    // Para crear nuevo cliente
    private Cliente nuevoCliente;
    private boolean mostrarDialogCrear;

    @PostConstruct
    public void init() {
        System.out.println("🔷 ClienteController inicializado");
        tiposCliente = clienteService.obtenerTodosTipos();
        nuevoCliente = new Cliente();
        mostrarDialogCrear = false;
    }

    public void seleccionarTipo(ClienteTipo tipo) {
        System.out.println("Tipo seleccionado: " + tipo.getNombre());
        this.tipoSeleccionado = tipo;
        cargarClientesPorTipo();
    }

    private void cargarClientesPorTipo() {
        if (tipoSeleccionado != null) {
            clientesFiltrados = clienteService.obtenerClientesPorTipo(tipoSeleccionado.getIdClienteTipo());
            System.out.println("✅ Clientes cargados: " + clientesFiltrados.size());
        }
    }

    public void abrirDialogCrear() {
        nuevoCliente = new Cliente();
        mostrarDialogCrear = true;
    }

    public void crearCliente() {
        System.out.println("\n🔷 ===== INICIO CREAR CLIENTE =====");
        System.out.println("Nombre: " + nuevoCliente.getNombre());
        System.out.println(" Correo: " + nuevoCliente.getCorreo());
        System.out.println(" Tipo: " + (tipoSeleccionado != null ? tipoSeleccionado.getNombre() : "null"));

        try {
            // Validaciones
            if (nuevoCliente.getNombre() == null || nuevoCliente.getNombre().trim().isEmpty()) {
                mostrarError("El nombre del cliente es obligatorio");
                return;
            }

            if (nuevoCliente.getCorreo() == null || nuevoCliente.getCorreo().trim().isEmpty()) {
                mostrarError("El correo electrónico es obligatorio");
                return;
            }

            if (tipoSeleccionado == null) {
                mostrarError("Debe seleccionar un tipo de cliente");
                return;
            }

            // Crear cliente
            clienteService.crearCliente(
                nuevoCliente.getNombre().trim(),
                nuevoCliente.getCorreo().trim(),
                tipoSeleccionado.getIdClienteTipo()
            );

            mostrarExito("Cliente creado correctamente");
            
            // Recargar lista
            cargarClientesPorTipo();
            
          
            mostrarDialogCrear = false;
            nuevoCliente = new Cliente();

            System.out.println("🔷 ===== FIN CREAR CLIENTE =====\n");

        } catch (RuntimeException e) {
            System.err.println("❌ Error: " + e.getMessage());
            mostrarError(e.getMessage());
        }
    }

    public void volverATipos() {
        tipoSeleccionado = null;
        clientesFiltrados = null;
    }

    private void mostrarExito(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "✅ Éxito", mensaje));
    }

    private void mostrarError(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Error", mensaje));
    }

 

    public List<ClienteTipo> getTiposCliente() {
        return tiposCliente;
    }

    public ClienteTipo getTipoSeleccionado() {
        return tipoSeleccionado;
    }

    public void setTipoSeleccionado(ClienteTipo tipoSeleccionado) {
        this.tipoSeleccionado = tipoSeleccionado;
    }

    public List<Cliente> getClientesFiltrados() {
        return clientesFiltrados;
    }

    public Cliente getNuevoCliente() {
        return nuevoCliente;
    }

    public void setNuevoCliente(Cliente nuevoCliente) {
        this.nuevoCliente = nuevoCliente;
    }

    public boolean isMostrarDialogCrear() {
        return mostrarDialogCrear;
    }

    public void setMostrarDialogCrear(boolean mostrarDialogCrear) {
        this.mostrarDialogCrear = mostrarDialogCrear;
    }
}