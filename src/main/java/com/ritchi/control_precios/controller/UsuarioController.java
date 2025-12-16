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
    private String rolSeleccionado;
    private List<Usuario> usuarios;

    public UsuarioController() {
        // Constructor vacío necesario para CDI
    }

    @PostConstruct
    public void init() {
        System.out.println("🔷 UsuarioController inicializado");
        nuevoUsuario = new Usuario();
        cargarUsuarios();
    }

    public void crearUsuario() {
        System.out.println("\n🔷 ===== INICIO CREAR USUARIO =====");
        System.out.println("📋 Nombre: " + nuevoUsuario.getNombre());
        System.out.println("📧 Email: " + nuevoUsuario.getEmail());
        System.out.println("🔑 Password: " + (nuevoUsuario.getPassword() != null ? "✓" : "✗"));
        System.out.println("👤 Rol seleccionado: '" + rolSeleccionado + "'");

        try {
            // ✅ Validar nombre
            if (nuevoUsuario.getNombre() == null || nuevoUsuario.getNombre().trim().isEmpty()) {
                mostrarError("El nombre de usuario es obligatorio");
                return;
            }

            // ✅ Validar email
            if (nuevoUsuario.getEmail() == null || nuevoUsuario.getEmail().trim().isEmpty()) {
                mostrarError("El correo electrónico es obligatorio");
                return;
            }

            // ✅ Validar contraseña
            if (nuevoUsuario.getPassword() == null || nuevoUsuario.getPassword().trim().isEmpty()) {
                mostrarError("La contraseña es obligatoria");
                return;
            }

            // ✅ Validar rol (CRÍTICO)
            if (rolSeleccionado == null || rolSeleccionado.trim().isEmpty()) {
                System.out.println("❌ Validación falló: Rol vacío");
                mostrarError("Debe seleccionar un rol");
                return;
            }

            System.out.println("✅ Todas las validaciones pasadas");
            System.out.println("🔄 Llamando al servicio...");

            // Crear el usuario
            usuarioService.crearUsuario(
                nuevoUsuario.getNombre().trim(),
                nuevoUsuario.getEmail().trim(),
                nuevoUsuario.getPassword(),
                rolSeleccionado.trim()
            );

            System.out.println("✅ Usuario creado exitosamente en BD");

            mostrarExito("Usuario creado correctamente");

            // Limpiar y recargar
            limpiarFormulario();
            cargarUsuarios();

            System.out.println("🔷 ===== FIN CREAR USUARIO =====\n");

        } catch (RuntimeException e) {
            System.err.println("❌ Error al crear usuario: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error: " + e.getMessage());
        }
    }

    private void cargarUsuarios() {
        System.out.println("🔄 Cargando usuarios...");
        usuarios = usuarioService.obtenerTodosUsuarios();
        System.out.println("✅ Usuarios cargados: " + usuarios.size());
    }

    private void limpiarFormulario() {
        System.out.println("🧹 Limpiando formulario...");
        nuevoUsuario = new Usuario();
        rolSeleccionado = null;
    }

    private void mostrarExito(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "✅ Éxito", mensaje));
    }

    private void mostrarError(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Error", mensaje));
    }

    private void mostrarAdvertencia(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_WARN, "⚠️ Advertencia", mensaje));
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
        System.out.println("🔷 setRolSeleccionado llamado con: '" + rolSeleccionado + "'");
        this.rolSeleccionado = rolSeleccionado;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}