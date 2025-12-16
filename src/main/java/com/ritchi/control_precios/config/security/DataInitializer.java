package com.ritchi.control_precios.config.security;

import com.ritchi.control_precios.model.entity.ClienteTipo;
import com.ritchi.control_precios.model.entity.Rol;
import com.ritchi.control_precios.model.entity.Usuario;
import com.ritchi.control_precios.repository.ClienteTipoRepository;
import com.ritchi.control_precios.repository.RolRepository;
import com.ritchi.control_precios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClienteTipoRepository clienteTipoRepository;

    public DataInitializer(RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            ClienteTipoRepository clienteTipoRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.clienteTipoRepository = clienteTipoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Iniciando inicialización de datos...");

        initializeRoles();
        initializeAdminUser();
        initializeTestUser();
        initializeClienteTipos();
        displayUserInfo();

        System.out.println("✅ Inicialización de datos completada");
    }

    private void initializeRoles() {
        if (rolRepository.count() == 0) {
            System.out.println("📋 Creando roles del sistema...");

            Rol adminRol = new Rol();
            adminRol.setNombre("ROLE_ADMIN");
            adminRol.setDescripcion("Administrador del sistema con acceso completo");

            Rol userRol = new Rol();
            userRol.setNombre("ROLE_USER");
            userRol.setDescripcion("Usuario regular con permisos básicos");

            rolRepository.saveAll(List.of(adminRol, userRol));
            
            System.out.println("✅ Roles creados: ADMIN, USER");
        } else {
            System.out.println("ℹ️ Roles ya existen en la base de datos");
        }
    }

    private void initializeAdminUser() {
        if (usuarioRepository.findByNombre("admin").isEmpty()) {
            System.out.println("👤 Creando usuario administrador...");

            Rol adminRol = rolRepository.findByNombre("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("❌ Rol ADMIN no encontrado"));

            Usuario admin = new Usuario();
            admin.setNombre("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@controlprecios.com");
            admin.setRol(adminRol);
            admin.setCreadoEn(new Date());

            usuarioRepository.save(admin);
            System.out.println("✅ Usuario administrador creado: admin / admin123");
        } else {
            System.out.println("ℹ️ Usuario admin ya existe");
        }
    }

    private void initializeTestUser() {
        if (usuarioRepository.findByNombre("user").isEmpty()) {
            System.out.println("👤 Creando usuario regular...");

            Rol userRol = rolRepository.findByNombre("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("❌ Rol USER no encontrado"));

            Usuario user = new Usuario();
            user.setNombre("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@controlprecios.com");
            user.setRol(userRol);
            user.setCreadoEn(new Date());

            usuarioRepository.save(user);
            System.out.println("✅ Usuario regular creado: user / user123");
        } else {
            System.out.println("ℹ️ Usuario user ya existe");
        }
    }

    private void displayUserInfo() {
        System.out.println("\n📊 RESUMEN DE USUARIOS CREADOS:");
        System.out.println("=================================");

        List<Usuario> usuarios = usuarioRepository.findAll();
        for (Usuario usuario : usuarios) {
            System.out.println("👤 " + usuario.getNombre() +
                    " | 📧 " + usuario.getEmail() +
                    " | 👑 " + (usuario.getRol() != null ? usuario.getRol().getNombre() : "Sin rol"));
        }
        System.out.println("=================================\n");
    }

    private void initializeClienteTipos() {
        if (clienteTipoRepository.count() == 0) {
            System.out.println("📂 Creando tipos de cliente...");

            ClienteTipo catalogo = new ClienteTipo();
            catalogo.setNombre("CATALOGO");

            ClienteTipo hilos = new ClienteTipo();
            hilos.setNombre("HILOS");

            clienteTipoRepository.saveAll(List.of(catalogo, hilos));
            
            System.out.println("✅ Tipos de cliente creados: CATALOGO, HILOS");
        } else {
            System.out.println("ℹ️ Tipos de cliente ya existen");
        }
    }
}