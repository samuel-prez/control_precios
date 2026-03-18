package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.entity.Cliente;
import com.ritchi.control_precios.model.entity.Producto;
import com.ritchi.control_precios.model.entity.Usuario;
import com.ritchi.control_precios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionEmailService {

    private final JavaMailSender mailSender;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.notificacion.email.from:noreply@controlprecios.com}")
    private String fromEmail;

    @Value("${app.notificacion.email.subject:Nuevo producto asignado - Control de Precios}")
    private String defaultSubject;

    public NotificacionEmailService(JavaMailSender mailSender, UsuarioRepository usuarioRepository) {
        this.mailSender = mailSender;
        this.usuarioRepository = usuarioRepository;
    }

    @Async
    public void notificarNuevoProductoAsignado(Cliente cliente, Producto producto, List<String> nombresColores) {
        try {
            List<Usuario> usuariosCostos = usuarioRepository.findByRolNombre("ROLE_COSTOS");

            if (usuariosCostos.isEmpty()) {
                return;
            }

            String mensaje = construirMensaje(cliente, producto, nombresColores);

            for (Usuario usuario : usuariosCostos) {
                if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
                    enviarCorreo(usuario.getEmail(), defaultSubject, mensaje);
                }
            }
        } catch (Exception e) {
        }
    }

    private String construirMensaje(Cliente cliente, Producto producto, List<String> nombresColores) {
        StringBuilder sb = new StringBuilder();
        sb.append("Se ha asignado un nuevo producto a un cliente.\n\n");
        sb.append("=== DETALLES ===\n\n");
        sb.append("Cliente: ").append(cliente.getNombre()).append("\n");
        sb.append("Producto: ").append(producto.getCodigoEstilo());
        if (producto.getCodigoPrototipo() != null && !producto.getCodigoPrototipo().isEmpty()) {
            sb.append(" / ").append(producto.getCodigoPrototipo());
        }
        sb.append("\n");
        if (producto.getDescripcion() != null && !producto.getDescripcion().isEmpty()) {
            sb.append("Descripcion: ").append(producto.getDescripcion()).append("\n");
        }
        sb.append("\nColores asignados (").append(nombresColores.size()).append("):\n");
        sb.append("-------------------\n");
        for (String color : nombresColores) {
            sb.append("  • ").append(color).append("\n");
        }
        sb.append("\nPor favor, ingresa al sistema para asignar los precios correspondientes.\n\n");
        sb.append("---\n");
        sb.append("Este es un mensaje automatico del Sistema de Control de Precios.");
        return sb.toString();
    }

    private void enviarCorreo(String destinatario, String asunto, String mensaje) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            mail.setTo(destinatario);
            mail.setSubject(asunto);
            mail.setText(mensaje);
            mailSender.send(mail);
        } catch (Exception e) {
        }
    }

    @Async
    public void notificarNuevoColorEnProducto(Producto producto, String nombreColor,
                                               String agregadoPor, List<String> nombresClientes) {
        try {
            List<Usuario> usuariosCostos = usuarioRepository.findByRolNombre("ROLE_COSTOS");
            List<Usuario> usuariosCatalogo = usuarioRepository.findByRolNombre("ROLE_CATALOGO");

            if (usuariosCostos.isEmpty() && usuariosCatalogo.isEmpty()) {
                return;
            }

            String asunto = "Nuevo color agregado al producto " + producto.getCodigoEstilo();
            String mensaje = construirMensajeNuevoColor(producto, nombreColor, agregadoPor, nombresClientes);

            for (Usuario usuario : usuariosCostos) {
                if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
                    enviarCorreo(usuario.getEmail(), asunto, mensaje);
                }
            }

            for (Usuario usuario : usuariosCatalogo) {
                if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
                    enviarCorreo(usuario.getEmail(), asunto, mensaje);
                }
            }

        } catch (Exception e) {
        }
    }

    private String construirMensajeNuevoColor(Producto producto, String nombreColor,
                                               String agregadoPor, List<String> nombresClientes) {
        StringBuilder sb = new StringBuilder();
        sb.append("NUEVO COLOR AGREGADO A PRODUCTO\n");
        sb.append("================================\n\n");
        sb.append("Se ha agregado un nuevo color a un producto existente.\n\n");
        sb.append("=== DETALLES ===\n\n");
        sb.append("Producto: ").append(producto.getCodigoEstilo());
        if (producto.getCodigoPrototipo() != null && !producto.getCodigoPrototipo().isEmpty()) {
            sb.append(" / ").append(producto.getCodigoPrototipo());
        }
        sb.append("\n");
        if (producto.getDescripcion() != null && !producto.getDescripcion().isEmpty()) {
            sb.append("Descripción: ").append(producto.getDescripcion()).append("\n");
        }
        sb.append("\nNuevo color agregado: ").append(nombreColor).append("\n");
        sb.append("Agregado por: ").append(agregadoPor != null ? agregadoPor : "Sistema").append("\n\n");

        if (nombresClientes != null && !nombresClientes.isEmpty()) {
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("CLIENTES QUE TIENEN ESTE PRODUCTO:\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            for (String cliente : nombresClientes) {
                sb.append("  • ").append(cliente).append("\n");
            }
            sb.append("\nTotal de clientes afectados: ").append(nombresClientes.size()).append("\n\n");
            sb.append("NOTA: Los clientes listados tienen este producto asignado.\n");
            sb.append("Considere agregar el nuevo color a las cotizaciones correspondientes.\n\n");
        } else {
            sb.append("Este producto aún no está asignado a ningún cliente.\n\n");
        }

        sb.append("---\n");
        sb.append("Este es un mensaje automático del Sistema de Control de Precios.");
        return sb.toString();
    }

    @Async
    public void notificarColoresSinPrecio(String nombreCliente, String codigoEstilo,
                                           String codigoPrototipo, List<String> coloresSinPrecio) {
        try {
            if (coloresSinPrecio == null || coloresSinPrecio.isEmpty()) {
                return;
            }

            List<Usuario> usuariosCatalogo = usuarioRepository.findByRolNombre("ROLE_CATALOGO");

            if (usuariosCatalogo.isEmpty()) {
                return;
            }

            String asunto = "Colores SIN PRECIO - " + nombreCliente + " - " + codigoEstilo;
            String mensaje = construirMensajeColoresSinPrecio(nombreCliente, codigoEstilo, codigoPrototipo, coloresSinPrecio);

            for (Usuario usuario : usuariosCatalogo) {
                if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
                    enviarCorreo(usuario.getEmail(), asunto, mensaje);
                }
            }

        } catch (Exception e) {
        }
    }

    private String construirMensajeColoresSinPrecio(String nombreCliente, String codigoEstilo,
                                                     String codigoPrototipo, List<String> colores) {
        StringBuilder sb = new StringBuilder();
        sb.append("COLORES SIN PRECIO ASIGNADO\n");
        sb.append("===========================\n\n");
        sb.append("Los siguientes colores aún no tienen precio asignado.\n");
        sb.append("Por favor, coordine con COSTOS para la asignación de precios.\n\n");
        sb.append("=== DETALLES ===\n\n");
        sb.append("Cliente: ").append(nombreCliente).append("\n");
        sb.append("Producto: ").append(codigoEstilo);
        if (codigoPrototipo != null && !codigoPrototipo.isEmpty()) {
            sb.append(" / ").append(codigoPrototipo);
        }
        sb.append("\n\n");
        sb.append("Colores SIN PRECIO (").append(colores.size()).append("):\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        for (String color : colores) {
            sb.append("  • ").append(color).append("\n");
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("---\n");
        sb.append("Este es un mensaje automático del Sistema de Control de Precios.");
        return sb.toString();
    }

    @Async
    public void notificarColoresAprobados(String nombreCliente, String codigoEstilo,
                                           String codigoPrototipo, List<String> coloresAprobados) {
        try {
            if (coloresAprobados == null || coloresAprobados.isEmpty()) {
                return;
            }

            List<Usuario> usuariosCostos = usuarioRepository.findByRolNombre("ROLE_COSTOS");

            if (usuariosCostos.isEmpty()) {
                return;
            }

            String asunto = "Colores APROBADOS por CATÁLOGO - " + nombreCliente + " - " + codigoEstilo;
            String mensaje = construirMensajeColoresAprobados(nombreCliente, codigoEstilo, codigoPrototipo, coloresAprobados);

            for (Usuario usuario : usuariosCostos) {
                if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
                    enviarCorreo(usuario.getEmail(), asunto, mensaje);
                }
            }

        } catch (Exception e) {
        }
    }

    private String construirMensajeColoresAprobados(String nombreCliente, String codigoEstilo,
                                                     String codigoPrototipo, List<String> colores) {
        StringBuilder sb = new StringBuilder();
        sb.append("COLORES APROBADOS POR CATÁLOGO\n");
        sb.append("==============================\n\n");
        sb.append("El equipo de CATÁLOGO ha APROBADO los siguientes colores.\n");
        sb.append("Los precios están listos para facturación.\n\n");
        sb.append("=== DETALLES ===\n\n");
        sb.append("Cliente: ").append(nombreCliente).append("\n");
        sb.append("Producto: ").append(codigoEstilo);
        if (codigoPrototipo != null && !codigoPrototipo.isEmpty()) {
            sb.append(" / ").append(codigoPrototipo);
        }
        sb.append("\n\n");
        sb.append("Colores APROBADOS (").append(colores.size()).append("):\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        for (String color : colores) {
            sb.append("  ✓ ").append(color).append("\n");
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("---\n");
        sb.append("Este es un mensaje automático del Sistema de Control de Precios.");
        return sb.toString();
    }

    @Async
    public void notificarCambioEstadoCatalogo(String nombreCliente, String codigoEstilo,
                                               String codigoPrototipo, String nuevoEstado,
                                               List<String> colores, String observacion) {
        try {
            if (colores == null || colores.isEmpty()) {
                return;
            }

            List<Usuario> usuariosCostos = usuarioRepository.findByRolNombre("ROLE_COSTOS");
            if (usuariosCostos.isEmpty()) {
                return;
            }

            String estadoLabel = "NEGOCIANDO".equals(nuevoEstado) ? "NEGOCIANDO" : "DESAPROBADO";
            String asunto = "Colores en " + estadoLabel + " - " + nombreCliente + " - " + codigoEstilo;
            String mensaje = construirMensajeCambioEstado(nombreCliente, codigoEstilo, codigoPrototipo,
                    nuevoEstado, colores, observacion);

            for (Usuario usuario : usuariosCostos) {
                if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
                    enviarCorreo(usuario.getEmail(), asunto, mensaje);
                }
            }
        } catch (Exception e) {
        }
    }

    private String construirMensajeCambioEstado(String nombreCliente, String codigoEstilo,
                                                  String codigoPrototipo, String nuevoEstado,
                                                  List<String> colores, String observacion) {
        StringBuilder sb = new StringBuilder();
        boolean esNegociando = "NEGOCIANDO".equals(nuevoEstado);

        if (esNegociando) {
            sb.append("COLORES PUESTOS EN NEGOCIACIÓN POR CATÁLOGO\n");
            sb.append("============================================\n\n");
            sb.append("El equipo de CATÁLOGO ha marcado los siguientes colores como NEGOCIANDO.\n");
            sb.append("Se requiere revisión y ajuste de precios.\n\n");
        } else {
            sb.append("COLORES DESAPROBADOS POR CATÁLOGO\n");
            sb.append("==================================\n\n");
            sb.append("El equipo de CATÁLOGO ha DESAPROBADO los siguientes colores.\n");
            sb.append("Se requiere revisión y posible reasignación de precios.\n\n");
        }

        sb.append("=== DETALLES ===\n\n");
        sb.append("Cliente: ").append(nombreCliente).append("\n");
        sb.append("Producto: ").append(codigoEstilo);
        if (codigoPrototipo != null && !codigoPrototipo.isEmpty()) {
            sb.append(" / ").append(codigoPrototipo);
        }
        sb.append("\nEstado aplicado: ").append(nuevoEstado).append("\n\n");

        String icono = esNegociando ? "↔" : "✗";
        sb.append("Colores afectados (").append(colores.size()).append("):\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        for (String color : colores) {
            sb.append("  ").append(icono).append(" ").append(color).append("\n");
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        if (observacion != null && !observacion.trim().isEmpty()) {
            sb.append("Observación de CATÁLOGO:\n");
            sb.append("  \"").append(observacion.trim()).append("\"\n\n");
        }

        sb.append("---\n");
        sb.append("Este es un mensaje automático del Sistema de Control de Precios.");
        return sb.toString();
    }

    @Async
    public void notificarColoresPendientes(String nombreCliente, String codigoEstilo,
                                            String codigoPrototipo, List<String> coloresPendientes) {
        try {
            if (coloresPendientes == null || coloresPendientes.isEmpty()) {
                return;
            }

            List<Usuario> usuariosCatalogo = usuarioRepository.findByRolNombre("ROLE_CATALOGO");

            if (usuariosCatalogo.isEmpty()) {
                return;
            }

            String asunto = "Precios PENDIENTES - " + nombreCliente + " - " + codigoEstilo;
            String mensaje = construirMensajeColoresPendientes(nombreCliente, codigoEstilo, codigoPrototipo, coloresPendientes);

            for (Usuario usuario : usuariosCatalogo) {
                if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
                    enviarCorreo(usuario.getEmail(), asunto, mensaje);
                }
            }

        } catch (Exception e) {
        }
    }

    private String construirMensajeColoresPendientes(String nombreCliente, String codigoEstilo,
                                                      String codigoPrototipo, List<String> colores) {
        StringBuilder sb = new StringBuilder();
        sb.append("PRECIOS PENDIENTES DE NEGOCIACIÓN\n");
        sb.append("==================================\n\n");
        sb.append("COSTOS ha asignado precios iniciales a los siguientes colores.\n");
        sb.append("Es momento de negociar con el cliente.\n\n");
        sb.append("=== DETALLES ===\n\n");
        sb.append("Cliente: ").append(nombreCliente).append("\n");
        sb.append("Producto: ").append(codigoEstilo);
        if (codigoPrototipo != null && !codigoPrototipo.isEmpty()) {
            sb.append(" / ").append(codigoPrototipo);
        }
        sb.append("\n\n");
        sb.append("Colores PENDIENTES (").append(colores.size()).append("):\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        for (String color : colores) {
            sb.append("  ⏳ ").append(color).append("\n");
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("Por favor, ingrese al sistema para negociar los precios con el cliente.\n\n");
        sb.append("---\n");
        sb.append("Este es un mensaje automático del Sistema de Control de Precios.");
        return sb.toString();
    }

}
