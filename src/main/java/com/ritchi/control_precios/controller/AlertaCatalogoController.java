package com.ritchi.control_precios.controller;

import com.ritchi.control_precios.model.dto.AlertaProductoDTO;
import com.ritchi.control_precios.model.entity.ClienteProductoColor;
import com.ritchi.control_precios.repository.ClienteProductoColorRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

@Named("alertaCatalogo")
@SessionScoped
@Component
public class AlertaCatalogoController implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ClienteProductoColorRepository clienteProductoColorRepository;

    private List<AlertaProductoDTO> alertas = new ArrayList<>();
    private boolean mostrarAlerta = false;

    public AlertaCatalogoController(ClienteProductoColorRepository clienteProductoColorRepository) {
        this.clienteProductoColorRepository = clienteProductoColorRepository;
    }

    @PostConstruct
    public void init() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return;

        boolean esCatalogo = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CATALOGO".equals(a.getAuthority()));
        if (!esCatalogo) return;

        cargarAlertas();
        mostrarAlerta = !alertas.isEmpty();
    }

    private void cargarAlertas() {
        try {
            List<ClienteProductoColor> registros =
                    clienteProductoColorRepository.findPendientesYNegociadosSinDetalle();

            Map<String, long[]> counts = new LinkedHashMap<>();
            Map<String, String[]> meta = new LinkedHashMap<>();

            for (ClienteProductoColor cpc : registros) {
                String clienteNombre = cpc.getClienteProducto().getCliente().getNombre();
                Integer idCp = cpc.getClienteProducto().getIdClienteProducto();
                String clave = clienteNombre + "|" + idCp;

                if (!meta.containsKey(clave)) {
                    String estilo = cpc.getClienteProducto().getProducto().getCodigoEstilo();
                    String proto  = cpc.getClienteProducto().getProducto().getCodigoPrototipo();
                    String desc   = cpc.getClienteProducto().getProducto().getDescripcion();
                    meta.put(clave, new String[]{ clienteNombre, estilo, proto, desc });
                    counts.put(clave, new long[]{0, 0}); // [pendiente, negociando]
                }

                long[] c = counts.get(clave);
                if ("PENDIENTE".equals(cpc.getNombreEstado()))  c[0]++;
                if ("NEGOCIANDO".equals(cpc.getNombreEstado())) c[1]++;
            }

            alertas = new ArrayList<>();
            for (Map.Entry<String, String[]> entry : meta.entrySet()) {
                String[] m = entry.getValue();
                long[] c   = counts.get(entry.getKey());
                alertas.add(new AlertaProductoDTO(m[0], m[1], m[2], m[3], c[0], c[1]));
            }

        } catch (Exception e) {
            alertas = new ArrayList<>();
        }
    }

    public void cerrarAlerta() {
        mostrarAlerta = false;
    }

    public boolean isMostrarAlerta()          { return mostrarAlerta; }
    public List<AlertaProductoDTO> getAlertas() { return alertas; }
    public int getTotalProductos()             { return alertas.size(); }
}
