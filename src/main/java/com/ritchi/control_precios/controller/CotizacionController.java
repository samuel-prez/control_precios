package com.ritchi.control_precios.controller;

import com.ritchi.control_precios.model.entity.Cliente;
import com.ritchi.control_precios.model.entity.Precio;
import com.ritchi.control_precios.model.entity.Producto;
import com.ritchi.control_precios.model.entity.ProductoColor;
import com.ritchi.control_precios.service.ClienteService;
import com.ritchi.control_precios.service.PrecioService;
import com.ritchi.control_precios.service.ProductoService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Named(value = "cotizacionController")
@ViewScoped
public class CotizacionController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClienteService clienteService;

    @Inject
    private ProductoService productoService;

    @Inject
    private PrecioService precioService;

    // Estado de la interfaz
    private List<Cliente> todosClientes;
    private Cliente clienteSeleccionado;
    private boolean mostrarFormulario;

    // Búsqueda de producto
    private String codigoBusqueda;
    private Producto productoEncontrado;
    private List<ProductoColor> coloresDisponibles;
    private Integer colorSeleccionado;

    // Nueva cotización
    private CotizacionDTO nuevaCotizacion;

    // Lista de cotizaciones del cliente
    private List<Precio> cotizacionesCliente;

    @PostConstruct
    public void init() {
        System.out.println("📋 CotizacionController inicializado");
        todosClientes = clienteService.obtenerTodosClientes();
        nuevaCotizacion = new CotizacionDTO();
        cotizacionesCliente = new ArrayList<>();
        mostrarFormulario = false;
    }

    // ========== MÉTODOS DE NAVEGACIÓN ==========

    public void seleccionarCliente(Cliente cliente) {
        this.clienteSeleccionado = cliente;
        this.mostrarFormulario = true;
        cargarCotizacionesCliente();
        System.out.println("✅ Cliente seleccionado: " + cliente.getNombre());
    }

    public void cambiarCliente() {
        this.clienteSeleccionado = null;
        this.mostrarFormulario = false;
        limpiarFormulario();
    }

    public void nuevaCotizacionBtn() {
        this.mostrarFormulario = true;
        limpiarFormulario();
    }

    // ========== BÚSQUEDA DE PRODUCTO ==========

    public void buscarProducto() {
        System.out.println("\n🔍 ===== BUSCANDO PRODUCTO =====");
        System.out.println("Código ingresado: " + codigoBusqueda);

        try {
            if (codigoBusqueda == null || codigoBusqueda.trim().isEmpty()) {
                mostrarError("Debe ingresar un código de producto");
                return;
            }

            // Buscar producto por código
            productoEncontrado = productoService.buscarProductoPorCodigo(codigoBusqueda.trim())
                .orElse(null);

            if (productoEncontrado == null) {
                mostrarAdvertencia("No se encontró ningún producto con el código: " + codigoBusqueda);
                coloresDisponibles = new ArrayList<>();
                return;
            }

            // Cargar colores disponibles
            coloresDisponibles = productoService.obtenerColoresPorProducto(
                productoEncontrado.getIdProducto()
            );

            if (coloresDisponibles.isEmpty()) {
                mostrarAdvertencia("El producto no tiene colores registrados. " +
                    "Por favor, agregue colores primero en la sección de Productos.");
            } else {
                mostrarExito("Producto encontrado: " + productoEncontrado.getCodigoEstilo());
            }

            System.out.println("✅ Producto encontrado: " + productoEncontrado.getCodigoEstilo());
            System.out.println("🎨 Colores disponibles: " + coloresDisponibles.size());

        } catch (Exception e) {
            System.err.println("❌ Error al buscar producto: " + e.getMessage());
            mostrarError("Error al buscar producto: " + e.getMessage());
        }
    }

    // ========== CREAR COTIZACIÓN ==========

    public void crearCotizacion() {
        System.out.println("\n💰 ===== CREANDO COTIZACIÓN =====");

        try {
            // Validaciones
            if (clienteSeleccionado == null) {
                mostrarError("Debe seleccionar un cliente");
                return;
            }

            if (productoEncontrado == null) {
                mostrarError("Debe buscar y seleccionar un producto");
                return;
            }

            if (colorSeleccionado == null) {
                mostrarError("Debe seleccionar un color");
                return;
            }

            if (nuevaCotizacion.getValorInicial() == null || 
                nuevaCotizacion.getValorInicial().compareTo(BigDecimal.ZERO) <= 0) {
                mostrarError("El valor inicial debe ser mayor a cero");
                return;
            }

            System.out.println("📋 Cliente: " + clienteSeleccionado.getNombre());
            System.out.println("📦 Producto: " + productoEncontrado.getCodigoEstilo());
            System.out.println("🎨 Color ID: " + colorSeleccionado);
            System.out.println("💵 Valor: $" + nuevaCotizacion.getValorInicial());

            // Crear cotización
            precioService.crearCotizacion(
                clienteSeleccionado.getIdCliente(),
                colorSeleccionado,
                nuevaCotizacion.getValorInicial(),
                nuevaCotizacion.getValorFinal(),
                nuevaCotizacion.getObservaciones()
            );

            mostrarExito("Cotización creada exitosamente");

            // Recargar lista y limpiar formulario
            cargarCotizacionesCliente();
            limpiarFormulario();

            System.out.println("💰 ===== COTIZACIÓN CREADA =====\n");

        } catch (RuntimeException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            mostrarError(e.getMessage());
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private void cargarCotizacionesCliente() {
        if (clienteSeleccionado != null) {
            cotizacionesCliente = precioService.obtenerCotizacionesPorCliente(
                clienteSeleccionado.getIdCliente()
            );
            System.out.println("📋 Cotizaciones cargadas: " + cotizacionesCliente.size());
        }
    }

    public void limpiarFormulario() {
        codigoBusqueda = null;
        productoEncontrado = null;
        coloresDisponibles = new ArrayList<>();
        colorSeleccionado = null;
        nuevaCotizacion = new CotizacionDTO();
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

    public List<Cliente> getTodosClientes() {
        return todosClientes;
    }

    public Cliente getClienteSeleccionado() {
        return clienteSeleccionado;
    }

    public void setClienteSeleccionado(Cliente clienteSeleccionado) {
        this.clienteSeleccionado = clienteSeleccionado;
    }

    public boolean isMostrarFormulario() {
        return mostrarFormulario;
    }

    public String getCodigoBusqueda() {
        return codigoBusqueda;
    }

    public void setCodigoBusqueda(String codigoBusqueda) {
        this.codigoBusqueda = codigoBusqueda;
    }

    public Producto getProductoEncontrado() {
        return productoEncontrado;
    }

    public List<ProductoColor> getColoresDisponibles() {
        return coloresDisponibles;
    }

    public Integer getColorSeleccionado() {
        return colorSeleccionado;
    }

    public void setColorSeleccionado(Integer colorSeleccionado) {
        this.colorSeleccionado = colorSeleccionado;
    }

    public CotizacionDTO getNuevaCotizacion() {
        return nuevaCotizacion;
    }

    public void setNuevaCotizacion(CotizacionDTO nuevaCotizacion) {
        this.nuevaCotizacion = nuevaCotizacion;
    }

    public List<Precio> getCotizacionesCliente() {
        return cotizacionesCliente;
    }

    // ========== DTO PARA NUEVA COTIZACIÓN ==========

    public static class CotizacionDTO implements Serializable {
        private String estiloCliente;
        private BigDecimal valorInicial;
        private BigDecimal valorFinal;
        private String observaciones;

        public CotizacionDTO() {
            this.valorInicial = BigDecimal.ZERO;
            this.valorFinal = BigDecimal.ZERO;
        }

        // Getters y Setters
        public String getEstiloCliente() {
            return estiloCliente;
        }

        public void setEstiloCliente(String estiloCliente) {
            this.estiloCliente = estiloCliente;
        }

        public BigDecimal getValorInicial() {
            return valorInicial;
        }

        public void setValorInicial(BigDecimal valorInicial) {
            this.valorInicial = valorInicial;
        }

        public BigDecimal getValorFinal() {
            return valorFinal;
        }

        public void setValorFinal(BigDecimal valorFinal) {
            this.valorFinal = valorFinal;
        }

        public String getObservaciones() {
            return observaciones;
        }

        public void setObservaciones(String observaciones) {
            this.observaciones = observaciones;
        }
    }
}