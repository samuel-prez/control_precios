package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.entity.Rol;
import com.ritchi.control_precios.model.entity.Usuario;
import com.ritchi.control_precios.repository.RolRepository;
import com.ritchi.control_precios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                         RolRepository rolRepository,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void crearUsuario(String nombre, String email, String password, String rolNombre, String telefono) {
        if (usuarioRepository.existsByNombre(nombre)) {
            throw new RuntimeException("El nombre de usuario '" + nombre + "' ya está en uso");
        }

        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El correo electrónico '" + email + "' ya está registrado");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException("El formato del correo electrónico no es válido");
        }

        if (password.length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        }

        Rol rol = rolRepository.findByNombre(rolNombre)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + rolNombre));

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setTelefono(telefono);
        usuario.setRol(rol);
        usuario.setCreadoEn(new Date());

        usuarioRepository.save(usuario);
    }

    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    public Optional<Usuario> buscarPorNombre(String nombre) {
        return usuarioRepository.findByNombre(nombre);
    }

    public void actualizarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() == null) {
            throw new RuntimeException("Usuario no válido para actualizar");
        }

        Usuario existente = usuarioRepository.findById(usuario.getIdUsuario())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        existente.setEmail(usuario.getEmail());
        existente.setTelefono(usuario.getTelefono());

        usuarioRepository.save(existente);
    }
}