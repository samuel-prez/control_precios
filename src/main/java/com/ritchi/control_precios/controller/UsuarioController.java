package com.ritchi.control_precios.controller;

import com.ritchi.control_precios.model.entity.Usuario;
import com.ritchi.control_precios.service.UsuarioService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named(value = "usuarioController")
@ViewScoped
public class UsuarioController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsuarioService usuarioService;

    private Usuario nuevoUsuario;
    private Usuario usuarioEditando;
    private String rolSeleccionado;
    private List<Usuario> usuarios;

    public UsuarioController() {
        // Constructor vacío necesario para CDI
    }

    @PostConstruct
    public void init() {
        nuevoUsuario = new Usuario();
        cargarUsuarios();
    }

    public void crearUsuario() {
        try {
            if (nuevoUsuario.getNombre() == null || nuevoUsuario.getNombre().trim().isEmpty()) {
                mostrarError("El nombre de usuario es obligatorio");
                return;
            }

            if (nuevoUsuario.getEmail() == null || nuevoUsuario.getEmail().trim().isEmpty()) {
                mostrarError("El correo electrónico es obligatorio");
                return;
            }

            if (nuevoUsuario.getPassword() == null || nuevoUsuario.getPassword().trim().isEmpty()) {
                mostrarError("La contraseña es obligatoria");
                return;
            }

            if (rolSeleccionado == null || rolSeleccionado.trim().isEmpty()) {
                mostrarError("Debe seleccionar un rol");
                return;
            }

            usuarioService.crearUsuario(
                nuevoUsuario.getNombre().trim(),
                nuevoUsuario.getEmail().trim(),
                nuevoUsuario.getPassword(),
                rolSeleccionado.trim(),
                nuevoUsuario.getTelefono()
            );

            mostrarExito("Usuario creado correctamente");

            limpiarFormulario();
            cargarUsuarios();

        } catch (RuntimeException e) {
            e.printStackTrace();
            mostrarError("Error: " + e.getMessage());
        }
    }

    public void prepararEdicionUsuario(Usuario usuario) {
        this.usuarioEditando = usuario;
    }

    public void actualizarUsuario() {
        try {
            if (usuarioEditando == null) return;
            usuarioService.actualizarUsuario(usuarioEditando);
            mostrarExito("Usuario actualizado correctamente");
            cargarUsuarios();
        } catch (RuntimeException e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    private void cargarUsuarios() {
        usuarios = usuarioService.obtenerTodosUsuarios();
    }

    private void limpiarFormulario() {
        nuevoUsuario = new Usuario();
        rolSeleccionado = null;
    }

    private void mostrarExito(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensaje));
    }

    private void mostrarError(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", mensaje));
    }

    private void mostrarAdvertencia(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", mensaje));
    }

    // ========== GETTERS Y SETTERS ==========

    public Usuario getNuevoUsuario() {
        return nuevoUsuario;
    }

    public void setNuevoUsuario(Usuario nuevoUsuario) {
        this.nuevoUsuario = nuevoUsuario;
    }

    public String getRolSeleccionado() {
        return rolSeleccionado;
    }

    public void setRolSeleccionado(String rolSeleccionado) {
        this.rolSeleccionado = rolSeleccionado;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario getUsuarioEditando() {
        return usuarioEditando;
    }

    public void setUsuarioEditando(Usuario usuarioEditando) {
        this.usuarioEditando = usuarioEditando;
    }
}