package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.entity.ClienteProductoColor;
import com.ritchi.control_precios.model.entity.Usuario;
import com.ritchi.control_precios.repository.ClienteProductoColorRepository;
import com.ritchi.control_precios.repository.UsuarioRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AlertaPreciosPendientesService {

    private final ClienteProductoColorRepository clienteProductoColorRepository;
    private final UsuarioRepository usuarioRepository;
    private final JavaMailSender mailSender;

    public AlertaPreciosPendientesService(ClienteProductoColorRepository clienteProductoColorRepository,
                                          UsuarioRepository usuarioRepository,
                                          JavaMailSender mailSender) {
        this.clienteProductoColorRepository = clienteProductoColorRepository;
        this.usuarioRepository = usuarioRepository;
        this.mailSender = mailSender;
    }

    /**
     * Se ejecuta de lunes a viernes a las 8:00 AM.
     * Alerta al rol CATALOGO sobre precios PENDIENTES que aún no han sido comunicados al cliente.
     */
    @Scheduled(cron = "${app.alerta.pendientes.cron:0 0 8 * * MON-FRI}")
    @Transactional(readOnly = true)
    public void alertarPreciosPendientes() {
        List<ClienteProductoColor> pendientes = clienteProductoColorRepository.findPendientesConDetalle();
        if (pendientes.isEmpty()) return;

        List<Usuario> usuariosCatalogo = usuarioRepository.findByRolNombre("ROLE_CATALOGO");
        if (usuariosCatalogo.isEmpty()) return;

        Map<String, List<ClienteProductoColor>> porCliente = new LinkedHashMap<>();
        for (ClienteProductoColor cpc : pendientes) {
            String nombreCliente = cpc.getClienteProducto().getCliente().getNombre();
            porCliente.computeIfAbsent(nombreCliente, k -> new ArrayList<>()).add(cpc);
        }

        String asunto = "⚠ Precios PENDIENTES sin enviar al cliente — " + porCliente.size() + " cliente(s)";
        String mensaje = construirMensaje(porCliente);

        for (Usuario usuario : usuariosCatalogo) {
            if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) {
                try {
                    SimpleMailMessage mail = new SimpleMailMessage();
                    mail.setTo(usuario.getEmail().trim());
                    mail.setSubject(asunto);
                    mail.setText(mensaje);
                    mailSender.send(mail);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private String construirMensaje(Map<String, List<ClienteProductoColor>> porCliente) {
        StringBuilder sb = new StringBuilder();
        sb.append("ALERTA — PRECIOS PENDIENTES SIN COMUNICAR AL CLIENTE\n");
        sb.append("======================================================\n\n");
        sb.append("Los siguientes productos tienen precios asignados (PENDIENTE) que aún\n");
        sb.append("no han sido comunicados/enviados al cliente.\n\n");
        sb.append("Por favor, envíe las listas de precios correspondientes.\n\n");

        int totalColores = 0;
        for (Map.Entry<String, List<ClienteProductoColor>> entry : porCliente.entrySet()) {
            String cliente = entry.getKey();
            List<ClienteProductoColor> colores = entry.getValue();
            totalColores += colores.size();

            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("CLIENTE: ").append(cliente).append("\n");
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

            Map<String, List<String>> porProducto = new LinkedHashMap<>();
            for (ClienteProductoColor cpc : colores) {
                String prod = cpc.getClienteProducto().getProducto().getCodigoEstilo();
                String desc = cpc.getClienteProducto().getProducto().getDescripcion();
                String clave = prod + (desc != null && !desc.isEmpty() ? " — " + desc : "");
                String colorNombre = cpc.getProductoColor().getColor().getNombre();
                porProducto.computeIfAbsent(clave, k -> new ArrayList<>()).add(colorNombre);
            }

            for (Map.Entry<String, List<String>> prod : porProducto.entrySet()) {
                sb.append("  Producto: ").append(prod.getKey()).append("\n");
                for (String color : prod.getValue()) {
                    sb.append("    ⏳ ").append(color).append("\n");
                }
            }
            sb.append("\n");
        }

        sb.append("======================================================\n");
        sb.append("Total: ").append(totalColores).append(" color(es) pendiente(s) en ")
          .append(porCliente.size()).append(" cliente(s)\n\n");
        sb.append("---\n");
        sb.append("Mensaje automático — Sistema de Control de Precios");
        return sb.toString();
    }
}
