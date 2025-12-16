package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.entity.Cliente;
import com.ritchi.control_precios.model.entity.Precio;
import com.ritchi.control_precios.model.entity.ProductoColor;
import com.ritchi.control_precios.model.entity.Usuario;
import com.ritchi.control_precios.repository.ClienteRepository;
import com.ritchi.control_precios.repository.PrecioRepository;
import com.ritchi.control_precios.repository.ProductoColorRepository;
import com.ritchi.control_precios.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class PrecioService {

    private final PrecioRepository precioRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoColorRepository productoColorRepository;
    private final UsuarioRepository usuarioRepository;

    public PrecioService(PrecioRepository precioRepository,
                        ClienteRepository clienteRepository,
                        ProductoColorRepository productoColorRepository,
                        UsuarioRepository usuarioRepository) {
        this.precioRepository = precioRepository;
        this.clienteRepository = clienteRepository;
        this.productoColorRepository = productoColorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea una nueva cotización (línea de precio) para un cliente
     */
    public void crearCotizacion(Integer idCliente, 
                               Integer idProductoColor,
                               BigDecimal valorInicial,
                               BigDecimal valorFinal,
                               String observaciones) {
        
        // Buscar cliente
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Buscar producto color
        ProductoColor productoColor = productoColorRepository.findById(idProductoColor)
            .orElseThrow(() -> new RuntimeException("Color de producto no encontrado"));

        // Obtener usuario actual
        Usuario usuario = obtenerUsuarioActual();

        // Crear cotización
        Precio precio = new Precio();
        precio.setCliente(cliente);
        precio.setProductoColor(productoColor);
        precio.setValorInicial(valorInicial);
        precio.setValorFinal(valorFinal);
        precio.setObservaciones(observaciones);
        precio.setFechaSolicitud(new Date());
        precio.setEstado(false); // Pendiente por defecto
        precio.setUsuario(usuario);

        precioRepository.save(precio);

        System.out.println("✅ Cotización creada para cliente: " + cliente.getNombre() +
                         " | Producto: " + productoColor.getProducto().getCodigoEstilo() +
                         " | Color: " + productoColor.getColor());
    }

    /**
     * Obtiene todas las cotizaciones de un cliente
     */
    public List<Precio> obtenerCotizacionesPorCliente(Integer idCliente) {
        return precioRepository.findByClienteIdCliente(idCliente);
    }

    /**
     * Obtiene cotizaciones aprobadas de un cliente
     */
    public List<Precio> obtenerCotizacionesAprobadas(Integer idCliente) {
        return precioRepository.findByClienteIdClienteAndEstadoTrue(idCliente);
    }

    /**
     * Obtiene cotizaciones pendientes de un cliente
     */
    public List<Precio> obtenerCotizacionesPendientes(Integer idCliente) {
        return precioRepository.findByClienteIdClienteAndEstadoFalse(idCliente);
    }

    /**
     * Aprueba una cotización
     */
    public void aprobarCotizacion(Integer idPrecio, 
                                  Date vigenciaInicio, 
                                  Date vigenciaFinal) {
        Precio precio = precioRepository.findById(idPrecio)
            .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        precio.setEstado(true);
        precio.setFechaAprobacion(new Date());
        precio.setVigenciaInicio(vigenciaInicio);
        precio.setVigenciaFinal(vigenciaFinal);

        precioRepository.save(precio);

        System.out.println("✅ Cotización aprobada: ID " + idPrecio);
    }

    /**
     * Actualiza el valor final de una cotización
     */
    public void actualizarValorFinal(Integer idPrecio, BigDecimal nuevoValorFinal) {
        Precio precio = precioRepository.findById(idPrecio)
            .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        precio.setValorFinal(nuevoValorFinal);
        precioRepository.save(precio);

        System.out.println("✅ Valor final actualizado para cotización: ID " + idPrecio);
    }

    /**
     * Verifica si ya existe una cotización pendiente para un cliente y producto-color
     */
    public boolean existeCotizacionPendiente(Integer idCliente, Integer idProductoColor) {
        List<Precio> pendientes = precioRepository.findPendientesByClienteAndProductoColor(
            idCliente, idProductoColor
        );
        return !pendientes.isEmpty();
    }

    /**
     * Obtiene todas las cotizaciones
     */
    public List<Precio> obtenerTodasCotizaciones() {
        return precioRepository.findAll();
    }

    /**
     * Obtiene el usuario actualmente autenticado
     */
    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        return usuarioRepository.findByNombre(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }
}