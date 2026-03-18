package com.ritchi.control_precios.controller;

import com.ritchi.control_precios.model.dto.ColorPrecioDTO;
import com.ritchi.control_precios.model.dto.ProductoAgrupadoDTO;
import com.ritchi.control_precios.model.dto.ProductoClienteDTO;
import com.ritchi.control_precios.model.entity.Cliente;
import com.ritchi.control_precios.model.entity.ClienteProducto;
import com.ritchi.control_precios.model.entity.ClienteProductoColor;
import com.ritchi.control_precios.model.entity.Producto;
import com.ritchi.control_precios.model.entity.ProductoColor;
import com.ritchi.control_precios.service.ClienteProductoService;
import com.ritchi.control_precios.service.ClienteService;
import com.ritchi.control_precios.service.NotificacionEmailService;
import com.ritchi.control_precios.service.ProductoService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;
import org.primefaces.PrimeFaces;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Named("clienteProductoController")
@ViewScoped
@Component
public class ClienteProductoController implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ClienteProductoService clienteProductoService;
    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final NotificacionEmailService notificacionEmailService;
    private final com.ritchi.control_precios.repository.ClienteProductoColorRepository clienteProductoColorRepository;
    private final com.ritchi.control_precios.service.ExcelExportService excelExportService;
    private final com.ritchi.control_precios.service.PdfExportService pdfExportService;

    private List<ClienteProducto> productosDelCliente;
    private List<ProductoClienteDTO> vistaCompletaProductos;
    private List<ProductoAgrupadoDTO> productosAgrupados;
    private ClienteProducto nuevoClienteProducto;
    private ClienteProducto clienteProductoEditando;
    private String terminoBusqueda;
    private Integer clienteSeleccionadoId;
    private Integer productoSeleccionadoId;
    private List<Cliente> clientes;
    private List<Producto> productos;
    private String filtroCodigoEstilo;
    private String filtroCodigoPrototipo;
    private Producto productoSeleccionado;
    private ProductoAgrupadoDTO productoSeleccionadoParaColores;
    private List<ColorPrecioDTO> coloresDelProducto;
    private ColorPrecioDTO colorSeleccionado;
    private boolean todosColorsMismoPrecio;
    private String estadoGeneralSeleccionado;
    private Producto productoParaImagen;
    private ProductoAgrupadoDTO productoEditando;
    private ProductoAgrupadoDTO productoSeleccionadoFila;
    private List<ColorPrecioDTO> coloresSeleccionados;
    private boolean todosSeleccionados;
    private String filtroColor;
    private Cliente clienteSeleccionadoObj;
    private List<ProductoColor> coloresProductoParaAsignar;
    private List<Integer> coloresSeleccionadosIds;
    private List<Producto> productosDisponiblesCache;
    private Integer colorSeleccionadoParaAgregar;
    private Date filtroFecha = new Date();

    private String tipoPrecioAsignar = "INICIAL";
    private java.math.BigDecimal precioInicialSeleccionados;
    private java.math.BigDecimal precioNegociadoSeleccionados;
    private String observacionesSeleccionados;

    private String estadoPorAplicar;
    private String observacionCambioEstado;

    private Integer mesesVigencia;

    public ClienteProductoController(ClienteProductoService clienteProductoService,
                                    ClienteService clienteService,
                                    ProductoService productoService,
                                    NotificacionEmailService notificacionEmailService,
                                    com.ritchi.control_precios.repository.ClienteProductoColorRepository clienteProductoColorRepository,
                                    com.ritchi.control_precios.service.ExcelExportService excelExportService,
                                    com.ritchi.control_precios.service.PdfExportService pdfExportService) {
        this.clienteProductoService = clienteProductoService;
        this.clienteService = clienteService;
        this.productoService = productoService;
        this.notificacionEmailService = notificacionEmailService;
        this.clienteProductoColorRepository = clienteProductoColorRepository;
        this.excelExportService = excelExportService;
        this.pdfExportService = pdfExportService;
    }

    @PostConstruct
    public void init() {
        productosDelCliente = new ArrayList<>();
        productosAgrupados = new ArrayList<>();
        coloresDelProducto = new ArrayList<>();
        coloresSeleccionados = new ArrayList<>();
        nuevoClienteProducto = new ClienteProducto();
        todosColorsMismoPrecio = false;
        todosSeleccionados = false;

        coloresProductoParaAsignar = new ArrayList<>();
        coloresSeleccionadosIds = new ArrayList<>();

        cargarClientes();
        cargarProductos();
    }

    private void cargarClientes() {
        try {
            clientes = clienteService.obtenerTodosClientes();
        } catch (Exception e) {
            clientes = new ArrayList<>();
        }
    }

    private void cargarProductos() {
        try {
            productos = productoService.obtenerTodosProductos();
        } catch (Exception e) {
            productos = new ArrayList<>();
        }
    }

    public void seleccionarCliente(Integer idCliente) {
        this.clienteSeleccionadoId = idCliente;
        cargarProductosDelCliente();
    }

    public void onClienteRowSelect(org.primefaces.event.SelectEvent<Cliente> event) {
        Cliente cliente = event.getObject();
        if (cliente != null) {
            this.clienteSeleccionadoObj = cliente;
            seleccionarCliente(cliente.getIdCliente());
        }
    }

    public Cliente getClienteSeleccionadoObj() {
        return clienteSeleccionadoObj;
    }

    public void setClienteSeleccionadoObj(Cliente clienteSeleccionadoObj) {
        this.clienteSeleccionadoObj = clienteSeleccionadoObj;
    }

    public void cargarProductosDelCliente() {
        if (clienteSeleccionadoId == null) {
            mostrarError("Debes seleccionar un cliente");
            productosDelCliente = new ArrayList<>();
            vistaCompletaProductos = new ArrayList<>();
            productosAgrupados = new ArrayList<>();
            return;
        }

        try {
            productosDelCliente = clienteProductoService.obtenerProductosDelCliente(clienteSeleccionadoId);
            vistaCompletaProductos = clienteProductoService.obtenerVistaCompletaProductosCliente(clienteSeleccionadoId);
            productosAgrupados = clienteProductoService.obtenerProductosAgrupados(clienteSeleccionadoId);

            for (ProductoAgrupadoDTO prod : productosAgrupados) {
                String estadoCalculado = clienteProductoService.calcularEstadoGeneral(
                    prod.getIdProducto(), clienteSeleccionadoId, prod.getIdClienteProducto());
                prod.setEstadoGeneral(estadoCalculado);
            }
        } catch (Exception e) {
            mostrarError("Error al cargar productos del cliente");
            productosDelCliente = new ArrayList<>();
            vistaCompletaProductos = new ArrayList<>();
            productosAgrupados = new ArrayList<>();
        }
    }

    public void buscarProductos() {
        if (clienteSeleccionadoId == null) {
            mostrarError("Debes seleccionar un cliente");
            return;
        }

        if (terminoBusqueda == null || terminoBusqueda.trim().isEmpty()) {
            cargarProductosDelCliente();
            return;
        }

        try {
            List<ProductoAgrupadoDTO> todosProductos = clienteProductoService.obtenerProductosAgrupados(clienteSeleccionadoId);

            String termino = terminoBusqueda.toLowerCase().trim();
            productosAgrupados = todosProductos.stream()
                .filter(p ->
                    (p.getCodigoEstilo() != null && p.getCodigoEstilo().toLowerCase().contains(termino)) ||
                    (p.getCodigoPrototipo() != null && p.getCodigoPrototipo().toLowerCase().contains(termino)) ||
                    (p.getEstiloCliente() != null && p.getEstiloCliente().toLowerCase().contains(termino)) ||
                    (p.getDescripcionProducto() != null && p.getDescripcionProducto().toLowerCase().contains(termino))
                )
                .collect(java.util.stream.Collectors.toList());

            for (ProductoAgrupadoDTO prod : productosAgrupados) {
                String estadoCalculado = clienteProductoService.calcularEstadoGeneral(
                    prod.getIdProducto(), clienteSeleccionadoId, prod.getIdClienteProducto());
                prod.setEstadoGeneral(estadoCalculado);
            }

            if (productosAgrupados.isEmpty()) {
                mostrarAdvertencia("No se encontraron productos con el término: " + terminoBusqueda);
            }
        } catch (Exception e) {
            mostrarError("Error al buscar productos");
            productosAgrupados = new ArrayList<>();
        }
    }

    public void agregarProductoACliente() {
        try {
            if (clienteSeleccionadoId == null) {
                mostrarError("Debes seleccionar un cliente");
                return;
            }

            if (productoSeleccionadoId == null) {
                mostrarError("Debes seleccionar un producto");
                return;
            }

            Cliente cliente = clienteService.obtenerClientePorId(clienteSeleccionadoId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            Producto producto = productoService.obtenerProductoPorId(productoSeleccionadoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (clienteProductoService.existeProductoEnCliente(clienteSeleccionadoId, productoSeleccionadoId)) {
                mostrarError("Este producto ya está asignado a este cliente");
                return;
            }

            if (producto.getEsExclusivo() != null && producto.getEsExclusivo()) {
                boolean clientePermitido = false;

                if (producto.getClientesExclusivos() != null && !producto.getClientesExclusivos().isEmpty()) {
                    for (Cliente c : producto.getClientesExclusivos()) {
                        if (c.getIdCliente().equals(clienteSeleccionadoId)) {
                            clientePermitido = true;
                            break;
                        }
                    }
                }

                if (!clientePermitido && producto.getClienteExclusivo() != null
                        && producto.getClienteExclusivo().getIdCliente().equals(clienteSeleccionadoId)) {
                    clientePermitido = true;
                }

                if (!clientePermitido) {
                    mostrarError("Este producto es exclusivo del cliente '"
                        + producto.getNombresClientesExclusivos()
                        + "' y no puede ser asignado a otro cliente.");
                    return;
                }
            }

            ClienteProducto cp = new ClienteProducto();
            cp.setCliente(cliente);
            cp.setProducto(producto);
            cp.setEstiloCliente(nuevoClienteProducto.getEstiloCliente());
            cp.setPrecioActual(nuevoClienteProducto.getPrecioActual());
            cp.setObservaciones(nuevoClienteProducto.getObservaciones());
            cp.setEstado("ACTIVO");
            cp.setFechaCotizacion(new Date());
            cp.setCreadoEn(new Date());

            ClienteProducto cpGuardado = clienteProductoService.crearClienteProducto(cp);

            List<ProductoColor> coloresActivos = productoService.obtenerColoresActivosDelProducto(productoSeleccionadoId);
            List<Integer> idsColores = new ArrayList<>();
            List<String> nombresColores = new ArrayList<>();
            for (ProductoColor pc : coloresActivos) {
                idsColores.add(pc.getIdProductoColor());
                if (pc.getColor() != null && pc.getColor().getNombre() != null) {
                    nombresColores.add(pc.getColor().getNombre());
                }
            }

            if (!idsColores.isEmpty()) {
                clienteProductoService.guardarColoresClienteProducto(
                        cpGuardado.getIdClienteProducto(),
                        idsColores
                );
            }

            notificacionEmailService.notificarNuevoProductoAsignado(cliente, producto, nombresColores);

            mostrarExito("Producto agregado correctamente");

            cargarProductosDelCliente();
            limpiarFormulario();

        } catch (Exception e) {
            mostrarError("Error al agregar producto: " + e.getMessage());
        }
    }

    public void prepararEdicion(ClienteProducto cp) {
        this.clienteProductoEditando = new ClienteProducto();
        this.clienteProductoEditando.setIdClienteProducto(cp.getIdClienteProducto());
        this.clienteProductoEditando.setCliente(cp.getCliente());
        this.clienteProductoEditando.setProducto(cp.getProducto());
        this.clienteProductoEditando.setEstiloCliente(cp.getEstiloCliente());
        this.clienteProductoEditando.setPrecioActual(cp.getPrecioActual());
        this.clienteProductoEditando.setObservaciones(cp.getObservaciones());
        this.clienteProductoEditando.setEstado(cp.getEstado());
        this.clienteProductoEditando.setFechaCotizacion(cp.getFechaCotizacion());
    }

    public void actualizarClienteProducto() {
        try {
            clienteProductoService.actualizarClienteProducto(clienteProductoEditando);
            mostrarExito("Registro actualizado correctamente");
            cargarProductosDelCliente();
            clienteProductoEditando = null;
        } catch (Exception e) {
            mostrarError("Error al actualizar: " + e.getMessage());
        }
    }

    public void eliminarClienteProducto(Integer idClienteProducto) {
        try {
            String usuarioActual = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
            clienteProductoService.eliminarClienteProducto(idClienteProducto,
                    usuarioActual != null ? usuarioActual : "Sistema");
            mostrarExito("Registro eliminado correctamente");
            cargarProductosDelCliente();
        } catch (Exception e) {
            mostrarError("Error al eliminar: " + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        nuevoClienteProducto = new ClienteProducto();
        productoSeleccionadoId = null;
        productoSeleccionado = null;
        filtroCodigoEstilo = null;
        filtroCodigoPrototipo = null;
        coloresProductoParaAsignar = new ArrayList<>();
        coloresSeleccionadosIds = new ArrayList<>();
    }

    public List<Producto> getProductosFiltrados() {
        if (productosDisponiblesCache == null || productosDisponiblesCache.isEmpty()) {
            return new ArrayList<>();
        }

        if (filtroCodigoEstilo == null || filtroCodigoEstilo.trim().isEmpty()) {
            return productosDisponiblesCache;
        }

        String filtro = filtroCodigoEstilo.toLowerCase().trim();
        List<Producto> resultado = new ArrayList<>();

        for (Producto p : productosDisponiblesCache) {
            boolean coincide = false;

            if (p.getCodigoEstilo() != null && p.getCodigoEstilo().toLowerCase().contains(filtro)) {
                coincide = true;
            }
            if (p.getCodigoPrototipo() != null && p.getCodigoPrototipo().toLowerCase().contains(filtro)) {
                coincide = true;
            }

            if (coincide) {
                resultado.add(p);
            }
        }

        return resultado;
    }

    public void cargarProductosDisponibles() {
        Integer idClienteActual = clienteSeleccionadoId;
        if (idClienteActual == null && clienteSeleccionadoObj != null) {
            idClienteActual = clienteSeleccionadoObj.getIdCliente();
        }

        if (idClienteActual != null) {
            productosDisponiblesCache = productoService.obtenerProductosDisponiblesParaCliente(idClienteActual);
        } else {
            productosDisponiblesCache = productoService.obtenerProductosNoExclusivos();
        }
    }

    public void onProductoSelect(org.primefaces.event.SelectEvent<Producto> event) {
        this.productoSeleccionado = event.getObject();
        this.productoSeleccionadoId = productoSeleccionado.getIdProducto();

        try {
            this.coloresProductoParaAsignar = productoService.obtenerProductoColores(productoSeleccionadoId);
            this.coloresSeleccionadosIds = new ArrayList<>();
            for (ProductoColor pc : coloresProductoParaAsignar) {
                coloresSeleccionadosIds.add(pc.getIdProductoColor());
            }
        } catch (Exception e) {
            this.coloresProductoParaAsignar = new ArrayList<>();
            this.coloresSeleccionadosIds = new ArrayList<>();
        }
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

    public String formatearObservacion(String obs) {
        if (obs == null || obs.trim().isEmpty()) return "";
        String[] lineas = obs.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i];
            if (linea.startsWith("[")) {
                int cierre = linea.indexOf("]");
                if (cierre > 0) {
                    sb.append("<b>").append(linea, 0, cierre + 1).append("</b>")
                      .append(linea.substring(cierre + 1));
                } else {
                    sb.append(linea);
                }
            } else {
                sb.append(linea);
            }
            if (i < lineas.length - 1) {
                sb.append("<br/>");
            }
        }
        return sb.toString();
    }

    public void seleccionarProductoParaColores(ProductoAgrupadoDTO producto) {
        this.productoSeleccionadoParaColores = producto;
        this.filtroColor = null;
        cargarColoresDelProducto();
    }

    public void onRowToggle(ToggleEvent event) {
        ProductoAgrupadoDTO producto = (ProductoAgrupadoDTO) event.getData();

        if (event.getVisibility() == Visibility.VISIBLE) {
            seleccionarProductoParaColores(producto);
            productoExpandidoId = producto.getIdClienteProducto();
        } else {
            if (productoExpandidoId != null && productoExpandidoId.equals(producto.getIdClienteProducto())) {
                cerrarGestionColores();
                productoExpandidoId = null;
            }
        }
    }

    private Integer productoExpandidoId = null;

    public void onProductoRowSelect(SelectEvent<ProductoAgrupadoDTO> event) {
        ProductoAgrupadoDTO producto = event.getObject();
        if (producto != null) {
            seleccionarProductoParaColores(producto);

            PrimeFaces.current().executeScript("manejarAcordeon();");
        }
    }

    public Integer getProductoExpandidoId() {
        return productoExpandidoId;
    }

    public void setProductoExpandidoId(Integer productoExpandidoId) {
        this.productoExpandidoId = productoExpandidoId;
    }

    private void recargarProductoSeleccionado() {
        if (productoSeleccionadoParaColores != null && clienteSeleccionadoId != null) {
            try {
                productosAgrupados = clienteProductoService.obtenerProductosAgrupados(clienteSeleccionadoId);

                for (ProductoAgrupadoDTO prod : productosAgrupados) {
                    String estadoCalculado = clienteProductoService.calcularEstadoGeneral(
                        prod.getIdProducto(), clienteSeleccionadoId, prod.getIdClienteProducto());
                    prod.setEstadoGeneral(estadoCalculado);
                }

                for (ProductoAgrupadoDTO p : productosAgrupados) {
                    if (p.getIdProducto().equals(productoSeleccionadoParaColores.getIdProducto())) {
                        this.productoSeleccionadoParaColores = p;
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public void cargarColoresDelProducto() {
        if (productoSeleccionadoParaColores == null || clienteSeleccionadoId == null) {
            coloresDelProducto = new ArrayList<>();
            return;
        }

        try {
            coloresDelProducto = clienteProductoService.obtenerColoresConPrecios(
                productoSeleccionadoParaColores.getIdProducto(),
                clienteSeleccionadoId,
                productoSeleccionadoParaColores.getIdClienteProducto()
            );

            todosSeleccionados = false;
            for (ColorPrecioDTO color : coloresDelProducto) {
                color.setSeleccionado(false);
            }
        } catch (Exception e) {
            mostrarError("Error al cargar colores del producto");
            coloresDelProducto = new ArrayList<>();
        }
    }

    public void agregarColorAdicional(Integer idProductoColor) {
        try {
            if (productoSeleccionadoParaColores == null) {
                mostrarError("No hay producto seleccionado");
                return;
            }

            Integer idClienteProducto = productoSeleccionadoParaColores.getIdClienteProducto();
            if (idClienteProducto == null) {
                mostrarError("No se encontró la relación cliente-producto");
                return;
            }

            clienteProductoService.agregarColorAClienteProducto(idClienteProducto, idProductoColor);
            mostrarExito("Color agregado correctamente");
            cargarColoresDelProducto();
            recargarProductoSeleccionado();

        } catch (Exception e) {
            mostrarError("Error al agregar color: " + e.getMessage());
        }
    }

    public List<ProductoColor> getColoresDisponiblesParaAgregar() {
        if (productoSeleccionadoParaColores == null) {
            return new ArrayList<>();
        }

        List<ProductoColor> todosLosColores = productoService.obtenerColoresActivosDelProducto(
            productoSeleccionadoParaColores.getIdProducto()
        );

        return todosLosColores != null ? todosLosColores : new ArrayList<>();
    }

    public void agregarColorAdicionalSeleccionado() {
        if (colorSeleccionadoParaAgregar == null) {
            mostrarError("Debe seleccionar un color");
            return;
        }
        agregarColorAdicional(colorSeleccionadoParaAgregar);
        colorSeleccionadoParaAgregar = null;
    }

    public void agregarTodosLosColores() {
        if (productoSeleccionadoParaColores == null) {
            mostrarError("No hay producto seleccionado");
            return;
        }

        List<ProductoColor> todosLosColores = productoService.obtenerColoresActivosDelProducto(
            productoSeleccionadoParaColores.getIdProducto()
        );

        if (todosLosColores == null || todosLosColores.isEmpty()) {
            mostrarAdvertencia("El producto no tiene colores disponibles");
            return;
        }

        int coloresAgregados = 0;
        for (ProductoColor pc : todosLosColores) {
            try {
                clienteProductoService.agregarColorAClienteProducto(
                    productoSeleccionadoParaColores.getIdClienteProducto(),
                    pc.getIdProductoColor()
                );
                coloresAgregados++;
            } catch (Exception e) {
            }
        }

        if (coloresAgregados > 0) {
            cargarColoresDelProducto();
            recargarProductoSeleccionado();
            mostrarExito("Se agregaron " + coloresAgregados + " color(es) al producto");
        } else {
            mostrarError("No se pudo agregar ningún color");
        }
    }

    public Integer getColorSeleccionadoParaAgregar() {
        return colorSeleccionadoParaAgregar;
    }

    public void setColorSeleccionadoParaAgregar(Integer colorSeleccionadoParaAgregar) {
        this.colorSeleccionadoParaAgregar = colorSeleccionadoParaAgregar;
    }

    public Date getFiltroFecha() { return filtroFecha; }
    public void setFiltroFecha(Date filtroFecha) { this.filtroFecha = filtroFecha; }

    public void cerrarGestionColores() {
        this.productoSeleccionadoParaColores = null;
        this.coloresDelProducto = new ArrayList<>();
    }

    public void limpiarSeleccionProducto() {
        cerrarGestionColores();
    }

    public void toggleActivarColor(Integer idProductoColor, Boolean nuevoEstado) {
        try {
            ProductoColor productoColor = productoService.obtenerProductoColorPorId(idProductoColor);
            if (productoColor != null) {
                productoColor.setActivo(nuevoEstado);

                productoService.actualizarProductoColor(productoColor);

                if (nuevoEstado != null && nuevoEstado) {
                    mostrarExito("Color activado correctamente");
                } else {
                    mostrarExito("Color marcado como N/A (No Aplica)");
                }

                cargarColoresDelProducto();
                recargarProductoSeleccionado();
            }
        } catch (Exception e) {
            mostrarError("Error al cambiar estado del color");
        }
    }

    public List<ClienteProducto> getProductosDelCliente() {
        return productosDelCliente;
    }

    public void setProductosDelCliente(List<ClienteProducto> productosDelCliente) {
        this.productosDelCliente = productosDelCliente;
    }

    public ClienteProducto getNuevoClienteProducto() {
        return nuevoClienteProducto;
    }

    public void setNuevoClienteProducto(ClienteProducto nuevoClienteProducto) {
        this.nuevoClienteProducto = nuevoClienteProducto;
    }

    public ClienteProducto getClienteProductoEditando() {
        return clienteProductoEditando;
    }

    public void setClienteProductoEditando(ClienteProducto clienteProductoEditando) {
        this.clienteProductoEditando = clienteProductoEditando;
    }

    public String getTerminoBusqueda() {
        return terminoBusqueda;
    }

    public void setTerminoBusqueda(String terminoBusqueda) {
        this.terminoBusqueda = terminoBusqueda;
    }

    public Integer getClienteSeleccionadoId() {
        return clienteSeleccionadoId;
    }

    public void setClienteSeleccionadoId(Integer clienteSeleccionadoId) {
        this.clienteSeleccionadoId = clienteSeleccionadoId;
    }

    public Integer getProductoSeleccionadoId() {
        return productoSeleccionadoId;
    }

    public void setProductoSeleccionadoId(Integer productoSeleccionadoId) {
        this.productoSeleccionadoId = productoSeleccionadoId;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    public List<ProductoClienteDTO> getVistaCompletaProductos() {
        return vistaCompletaProductos;
    }

    public void setVistaCompletaProductos(List<ProductoClienteDTO> vistaCompletaProductos) {
        this.vistaCompletaProductos = vistaCompletaProductos;
    }

    public String getFiltroCodigoEstilo() {
        return filtroCodigoEstilo;
    }

    public void setFiltroCodigoEstilo(String filtroCodigoEstilo) {
        this.filtroCodigoEstilo = filtroCodigoEstilo;
    }

    public String getFiltroCodigoPrototipo() {
        return filtroCodigoPrototipo;
    }

    public void setFiltroCodigoPrototipo(String filtroCodigoPrototipo) {
        this.filtroCodigoPrototipo = filtroCodigoPrototipo;
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(Producto productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }

    public List<ProductoAgrupadoDTO> getProductosAgrupados() {
        return productosAgrupados;
    }

    public void setProductosAgrupados(List<ProductoAgrupadoDTO> productosAgrupados) {
        this.productosAgrupados = productosAgrupados;
    }

    public ProductoAgrupadoDTO getProductoSeleccionadoParaColores() {
        return productoSeleccionadoParaColores;
    }

    public void setProductoSeleccionadoParaColores(ProductoAgrupadoDTO productoSeleccionadoParaColores) {
        this.productoSeleccionadoParaColores = productoSeleccionadoParaColores;
    }

    public List<ColorPrecioDTO> getColoresDelProducto() {
        return coloresDelProducto;
    }

    public void setColoresDelProducto(List<ColorPrecioDTO> coloresDelProducto) {
        this.coloresDelProducto = coloresDelProducto;
    }

    public List<ColorPrecioDTO> getColoresFiltrados() {
        if (coloresDelProducto == null || coloresDelProducto.isEmpty()) {
            return coloresDelProducto;
        }

        if (filtroColor == null || filtroColor.trim().isEmpty()) {
            return coloresDelProducto;
        }

        String filtroLower = filtroColor.toLowerCase().trim();

        return coloresDelProducto.stream()
            .filter(color -> {
                boolean coincideNombre = color.getNombreColor() != null &&
                                       color.getNombreColor().toLowerCase().contains(filtroLower);

                boolean coincideCodigo = color.getCodigoColor() != null &&
                                       color.getCodigoColor().toLowerCase().contains(filtroLower);

                return coincideNombre || coincideCodigo;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    public ColorPrecioDTO getColorSeleccionado() {
        return colorSeleccionado;
    }

    public void setColorSeleccionado(ColorPrecioDTO colorSeleccionado) {
        this.colorSeleccionado = colorSeleccionado;
    }

    public boolean isTodosColorsMismoPrecio() {
        return todosColorsMismoPrecio;
    }

    public void setTodosColorsMismoPrecio(boolean todosColorsMismoPrecio) {
        this.todosColorsMismoPrecio = todosColorsMismoPrecio;
    }

    public void seleccionarProductoParaVerImagen(ProductoAgrupadoDTO productoDTO) {
        try {
            Optional<Producto> productoOpt = productoService.obtenerProductoPorId(productoDTO.getIdProducto());

            if (productoOpt.isPresent()) {
                this.productoParaImagen = productoOpt.get();
            } else {
                this.productoParaImagen = null;
            }
        } catch (Exception e) {
            this.productoParaImagen = null;
        }
    }

    public String obtenerImagenBase64() {
        if (productoParaImagen == null || productoParaImagen.getImagenProducto() == null || productoParaImagen.getImagenProducto().isEmpty()) {
            return null;
        }
        String imagenBase64 = productoParaImagen.getImagenProducto();
        if (imagenBase64.startsWith("data:image")) {
            return imagenBase64;
        }
        return "data:image/jpeg;base64," + imagenBase64;
    }

    public String obtenerImagenBase64_2() {
        if (productoParaImagen == null || productoParaImagen.getImagenProducto2() == null || productoParaImagen.getImagenProducto2().isEmpty()) {
            return null;
        }
        String imagenBase64 = productoParaImagen.getImagenProducto2();
        if (imagenBase64.startsWith("data:image")) {
            return imagenBase64;
        }
        return "data:image/jpeg;base64," + imagenBase64;
    }

    public void prepararEdicionProducto(ProductoAgrupadoDTO producto) {
        this.productoEditando = producto;
    }

    public void actualizarProductoCliente() {
        try {
            if (productoEditando == null) {
                mostrarAdvertencia("No hay producto seleccionado para editar");
                return;
            }

            ClienteProducto clienteProducto = clienteProductoService.obtenerPorId(productoEditando.getIdClienteProducto());
            if (clienteProducto == null) {
                mostrarError("No se encontró el producto del cliente");
                return;
            }

            clienteProducto.setEstiloCliente(productoEditando.getEstiloCliente());
            clienteProducto.setObservaciones(productoEditando.getObservacionesCliente());
            clienteProducto.setEstado(productoEditando.getEstadoClienteProducto());

            clienteProductoService.actualizarClienteProducto(clienteProducto);

            cargarProductosDelCliente();

            mostrarExito("Producto actualizado correctamente");
            productoEditando = null;

        } catch (Exception e) {
            mostrarError("No se pudo actualizar el producto: " + e.getMessage());
        }
    }

    public void eliminarProductoDeCliente(Integer idClienteProducto) {
        try {
            if (idClienteProducto == null) {
                mostrarError("ID de producto inválido");
                return;
            }

            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

            String usuarioActual = externalContext.getRemoteUser();
            clienteProductoService.eliminarClienteProducto(idClienteProducto,
                    usuarioActual != null ? usuarioActual : "Sistema");
            mostrarExito("Producto eliminado del cliente correctamente");
            cargarProductosDelCliente();

            if (productoSeleccionadoFila != null &&
                productoSeleccionadoFila.getIdClienteProducto().equals(idClienteProducto)) {
                productoSeleccionadoFila = null;
            }

        } catch (Exception e) {
            mostrarError("Error al eliminar producto: " + e.getMessage());
        }
    }

    public Producto getProductoParaImagen() {
        return productoParaImagen;
    }

    public void setProductoParaImagen(Producto productoParaImagen) {
        this.productoParaImagen = productoParaImagen;
    }

    public ProductoAgrupadoDTO getProductoEditando() {
        return productoEditando;
    }

    public void setProductoEditando(ProductoAgrupadoDTO productoEditando) {
        this.productoEditando = productoEditando;
    }

    public ProductoAgrupadoDTO getProductoSeleccionadoFila() {
        return productoSeleccionadoFila;
    }

    public void setProductoSeleccionadoFila(ProductoAgrupadoDTO productoSeleccionadoFila) {
        this.productoSeleccionadoFila = productoSeleccionadoFila;
    }

    public String getEstadoGeneralSeleccionado() {
        return estadoGeneralSeleccionado;
    }

    public void setEstadoGeneralSeleccionado(String estadoGeneralSeleccionado) {
        this.estadoGeneralSeleccionado = estadoGeneralSeleccionado;
    }

    public List<ColorPrecioDTO> getColoresSeleccionados() {
        return coloresSeleccionados;
    }

    public void setColoresSeleccionados(List<ColorPrecioDTO> coloresSeleccionados) {
        this.coloresSeleccionados = coloresSeleccionados;
    }

    public void refrescarSiNoHayEdicion() {
        if (productoSeleccionadoFila == null && clienteSeleccionadoId != null) {
            cargarProductosDelCliente();
            org.primefaces.PrimeFaces.current().ajax().update("formProductosCliente:tablaProductosCliente");
        }
    }

    public void toggleSeleccionColor(ColorPrecioDTO color) {
        actualizarEstadoTodosSeleccionados();
    }

    public void toggleSeleccionarTodos() {
        todosSeleccionados = !todosSeleccionados;

        for (ColorPrecioDTO color : coloresDelProducto) {
            color.setSeleccionado(todosSeleccionados);
        }
    }

    private void actualizarEstadoTodosSeleccionados() {
        if (coloresDelProducto == null || coloresDelProducto.isEmpty()) {
            todosSeleccionados = false;
            return;
        }

        todosSeleccionados = true;
        for (ColorPrecioDTO color : coloresDelProducto) {
            if (!color.isSeleccionado()) {
                todosSeleccionados = false;
                break;
            }
        }
    }

    public boolean isTodosSeleccionados() {
        return todosSeleccionados;
    }

    public void setTodosSeleccionados(boolean todosSeleccionados) {
        this.todosSeleccionados = todosSeleccionados;
    }

    public void activarSeleccionados() {
        try {
            int contador = 0;
            for (ColorPrecioDTO color : coloresDelProducto) {
                if (color.isSeleccionado()) {
                    if (color.getIdProductoColor() != null) {
                        productoService.activarColor(color.getIdProductoColor());
                        color.setActivo(true);
                        contador++;
                    }
                }
            }

            if (contador > 0) {
                mostrarExito("Se activaron " + contador + " color(es)");
                cargarColoresDelProducto();
                recargarProductoSeleccionado();
            } else {
                mostrarAdvertencia("No se seleccionaron colores para activar");
            }
        } catch (Exception e) {
            mostrarError("Error al activar colores: " + e.getMessage());
        }
    }

    public void desactivarSeleccionados() {
        try {
            int contador = 0;
            for (ColorPrecioDTO color : coloresDelProducto) {
                if (color.isSeleccionado()) {
                    if (color.getIdProductoColor() != null) {
                        productoService.desactivarColor(color.getIdProductoColor());
                        color.setActivo(false);
                        contador++;
                    }
                }
            }

            if (contador > 0) {
                mostrarExito("Se desactivaron " + contador + " color(es)");
                cargarColoresDelProducto();
                recargarProductoSeleccionado();
            } else {
                mostrarAdvertencia("No se seleccionaron colores para desactivar");
            }
        } catch (Exception e) {
            mostrarError("Error al desactivar colores: " + e.getMessage());
        }
    }

    public void onCellEdit(org.primefaces.event.CellEditEvent<?> event) {
        try {
            Object oldValue = event.getOldValue();
            Object newValue = event.getNewValue();

            if (newValue != null && !newValue.equals(oldValue)) {
                ColorPrecioDTO color = (ColorPrecioDTO) event.getRowData();

                if (newValue instanceof String &&
                    (oldValue == null || oldValue instanceof String) &&
                    newValue.equals(color.getCodigoColor())) {
                    ProductoColor productoColor = productoService.obtenerProductoColorPorId(color.getIdProductoColor());
                    if (productoColor != null) {
                        productoColor.setCodigoColor((String) newValue);
                        productoService.actualizarProductoColor(productoColor);

                        cargarColoresDelProducto();

                        org.primefaces.PrimeFaces.current().ajax().update("@row");
                        org.primefaces.PrimeFaces.current().ajax().update("messages");

                        mostrarExito("Código actualizado para el color " + color.getNombreColor());
                    }
                }
            }
        } catch (Exception e) {
            mostrarError("Error al actualizar: " + e.getMessage());
        }
    }

    public void cambiarEstadoColor(ColorPrecioDTO color) {
        try {
            if ("N/A".equals(color.getActivo() != null && !color.getActivo() ? "N/A" : null)) {
                productoService.desactivarColor(color.getIdProductoColor());
                mostrarExito("Color " + color.getNombreColor() + " desactivado (N/A)");
            } else {
                productoService.activarColor(color.getIdProductoColor());
                mostrarExito("Color " + color.getNombreColor() + " activado");
            }

            cargarColoresDelProducto();
            recargarProductoSeleccionado();
        } catch (Exception e) {
            mostrarError("Error al cambiar estado: " + e.getMessage());
        }
    }

    public void guardarPrecioDirecto(ColorPrecioDTO color) {
        if (color.getIdClienteProductoColor() == null) {
            mostrarError("No se puede guardar: color sin asignación al cliente");
            return;
        }
        try {
            com.ritchi.control_precios.model.entity.ClienteProductoColor cpc =
                clienteProductoColorRepository.findById(color.getIdClienteProductoColor()).orElse(null);
            if (cpc == null) {
                mostrarError("No se encontró la asignación del color");
                return;
            }
            cpc.setValorInicial(color.getValorInicial());
            cpc.setValorNegociado(color.getValorNegociado());
            if (color.getValorInicial() != null
                    && (cpc.getNombreEstado() == null || "SIN PRECIO".equals(cpc.getNombreEstado()))) {
                cpc.setNombreEstado("PENDIENTE");
                cpc.setFechaSolicitud(new Date());
            }
            clienteProductoColorRepository.save(cpc);
            cargarColoresDelProducto();
        } catch (Exception e) {
            mostrarError("Error al guardar precio: " + e.getMessage());
        }
    }

    public void cambiarEstadoSeleccionados(String nuevoEstado, String observacionGlobal) {
        try {
            List<ColorPrecioDTO> seleccionados = new ArrayList<>();
            if (coloresDelProducto != null) {
                for (ColorPrecioDTO color : coloresDelProducto) {
                    if (color.isSeleccionado()) {
                        seleccionados.add(color);
                    }
                }
            }

            if (seleccionados.isEmpty()) {
                mostrarError("Debe seleccionar al menos un color");
                return;
            }

            for (ColorPrecioDTO color : seleccionados) {
                if (color.getIdClienteProductoColor() == null) continue;
                com.ritchi.control_precios.model.entity.ClienteProductoColor cpc =
                    clienteProductoColorRepository.findById(color.getIdClienteProductoColor()).orElse(null);
                if (cpc != null) {
                    cpc.setNombreEstado(nuevoEstado);
                    if (observacionGlobal != null && !observacionGlobal.trim().isEmpty()) {
                        cpc.setObservaciones(observacionGlobal);
                    }
                    clienteProductoColorRepository.save(cpc);
                }
            }

            if (clienteSeleccionadoId != null && productoSeleccionadoParaColores != null) {
                try {
                    Cliente cliente = clienteService.obtenerClientePorId(clienteSeleccionadoId).orElse(null);
                    if (cliente != null) {
                        List<String> nombresColores = new ArrayList<>();
                        for (ColorPrecioDTO color : seleccionados) {
                            nombresColores.add(color.getNombreColor() +
                                (color.getCodigoColor() != null ? " (" + color.getCodigoColor() + ")" : ""));
                        }

                        if ("NEGOCIANDO".equals(nuevoEstado) || "DESAPROBADO".equals(nuevoEstado)) {
                            notificacionEmailService.notificarCambioEstadoCatalogo(
                                cliente.getNombre(),
                                productoSeleccionadoParaColores.getCodigoEstilo(),
                                productoSeleccionadoParaColores.getCodigoPrototipo(),
                                nuevoEstado,
                                nombresColores,
                                observacionGlobal
                            );
                        }
                    }
                } catch (Exception e) {
                }
            }

            cargarColoresDelProducto();
            recargarProductoSeleccionado();

            for (ColorPrecioDTO color : coloresDelProducto) {
                color.setSeleccionado(false);
            }
            coloresSeleccionados = new ArrayList<>();

        } catch (Exception e) {
            mostrarError("Error al cambiar estado: " + e.getMessage());
        }
    }

    public String getTipoPrecioAsignar() { return tipoPrecioAsignar; }
    public void setTipoPrecioAsignar(String tipoPrecioAsignar) { this.tipoPrecioAsignar = tipoPrecioAsignar; }

    public java.math.BigDecimal getPrecioInicialSeleccionados() { return precioInicialSeleccionados; }
    public void setPrecioInicialSeleccionados(java.math.BigDecimal precioInicialSeleccionados) { this.precioInicialSeleccionados = precioInicialSeleccionados; }

    public java.math.BigDecimal getPrecioNegociadoSeleccionados() { return precioNegociadoSeleccionados; }
    public void setPrecioNegociadoSeleccionados(java.math.BigDecimal precioNegociadoSeleccionados) { this.precioNegociadoSeleccionados = precioNegociadoSeleccionados; }

    public String getObservacionesSeleccionados() { return observacionesSeleccionados; }
    public void setObservacionesSeleccionados(String observacionesSeleccionados) { this.observacionesSeleccionados = observacionesSeleccionados; }

    public void asignarPrecioASeleccionados() {
        try {
            List<ColorPrecioDTO> seleccionados = new ArrayList<>();
            if (coloresDelProducto != null) {
                for (ColorPrecioDTO color : coloresDelProducto) {
                    if (color.isSeleccionado()) seleccionados.add(color);
                }
            }
            if (seleccionados.isEmpty()) {
                mostrarError("Debe seleccionar al menos un color");
                return;
            }
            boolean esInicial = "INICIAL".equals(tipoPrecioAsignar);
            java.math.BigDecimal valor = esInicial ? precioInicialSeleccionados : precioNegociadoSeleccionados;
            if (valor == null) {
                mostrarError("Ingrese el precio antes de asignar");
                return;
            }
            for (ColorPrecioDTO color : seleccionados) {
                if (color.getIdClienteProductoColor() == null) continue;
                com.ritchi.control_precios.model.entity.ClienteProductoColor cpc =
                    clienteProductoColorRepository.findById(color.getIdClienteProductoColor()).orElse(null);
                if (cpc == null) continue;
                if (esInicial) {
                    cpc.setValorInicial(valor);
                    if (cpc.getNombreEstado() == null || "SIN PRECIO".equals(cpc.getNombreEstado())) {
                        cpc.setNombreEstado("PENDIENTE");
                        cpc.setFechaSolicitud(new Date());
                    }
                } else {
                    cpc.setValorNegociado(valor);
                    cpc.setNombreEstado("NEGOCIANDO");
                }
                if (observacionesSeleccionados != null && !observacionesSeleccionados.trim().isEmpty()) {
                    cpc.setObservaciones(observacionesSeleccionados);
                }
                clienteProductoColorRepository.save(cpc);
            }
            precioInicialSeleccionados = null;
            precioNegociadoSeleccionados = null;
            observacionesSeleccionados = null;
            tipoPrecioAsignar = "INICIAL";
            for (ColorPrecioDTO color : coloresDelProducto) color.setSeleccionado(false);
            coloresSeleccionados = new ArrayList<>();
            cargarColoresDelProducto();
            recargarProductoSeleccionado();
            mostrarExito("Precio asignado a " + seleccionados.size() + " color(es)");
        } catch (Exception e) {
            mostrarError("Error al asignar precio: " + e.getMessage());
        }
    }

    public String getEstadoPorAplicar() { return estadoPorAplicar; }
    public void setEstadoPorAplicar(String estadoPorAplicar) { this.estadoPorAplicar = estadoPorAplicar; }

    public String getObservacionCambioEstado() { return observacionCambioEstado; }
    public void setObservacionCambioEstado(String observacionCambioEstado) { this.observacionCambioEstado = observacionCambioEstado; }

    public Integer getMesesVigencia() { return mesesVigencia; }
    public void setMesesVigencia(Integer mesesVigencia) { this.mesesVigencia = mesesVigencia; }

    /** Abre el diálogo de asignación masiva de precios validando que haya selección. */
    public void abrirAsignacionPrecioSeleccionados() {
        long seleccionados = coloresDelProducto == null ? 0 :
            coloresDelProducto.stream().filter(ColorPrecioDTO::isSeleccionado).count();
        if (seleccionados == 0) {
            mostrarError("Selecciona al menos un color antes de asignar precio");
        }
    }

    /** Abre el diálogo de cambio de vigencia validando que haya selección. */
    public void abrirCambioVigencia() {
        long seleccionados = coloresDelProducto == null ? 0 :
            coloresDelProducto.stream().filter(ColorPrecioDTO::isSeleccionado).count();
        if (seleccionados == 0) {
            mostrarError("Selecciona al menos un color antes de cambiar la vigencia");
            return;
        }
        org.primefaces.PrimeFaces.current().executeScript("PF('dlgCambiarVigencia').show()");
    }

    /** Prepara el cambio de estado. Si es APROBADO lo aplica directo; si requiere observación abre diálogo. */
    public void prepararCambioEstado(String nuevoEstado) {
        long seleccionados = coloresDelProducto == null ? 0 :
            coloresDelProducto.stream().filter(ColorPrecioDTO::isSeleccionado).count();
        if (seleccionados == 0) {
            mostrarError("Selecciona al menos un color");
            return;
        }
        this.estadoPorAplicar = nuevoEstado;
        this.observacionCambioEstado = null;
        if ("APROBADO".equals(nuevoEstado)) {
            cambiarEstadoSeleccionados(nuevoEstado, null);
        }
    }

    /** Confirma el cambio de estado desde el diálogo con observación. */
    public void confirmarCambioEstado() {
        cambiarEstadoSeleccionados(estadoPorAplicar, observacionCambioEstado);
        observacionCambioEstado = null;
        estadoPorAplicar = null;
    }

    /** Cambia la vigencia final de los colores seleccionados sumando N meses a la fecha de solicitud. */
    public void cambiarVigenciaSeleccionados() {
        if (mesesVigencia == null) {
            mostrarError("Selecciona los meses de vigencia");
            return;
        }
        try {
            List<ColorPrecioDTO> seleccionados = new ArrayList<>();
            if (coloresDelProducto != null) {
                for (ColorPrecioDTO color : coloresDelProducto) {
                    if (color.isSeleccionado()) seleccionados.add(color);
                }
            }
            if (seleccionados.isEmpty()) {
                mostrarError("Selecciona al menos un color");
                return;
            }
            java.util.Calendar cal = java.util.Calendar.getInstance();
            for (ColorPrecioDTO color : seleccionados) {
                if (color.getIdClienteProductoColor() == null) continue;
                com.ritchi.control_precios.model.entity.ClienteProductoColor cpc =
                    clienteProductoColorRepository.findById(color.getIdClienteProductoColor()).orElse(null);
                if (cpc == null) continue;
                Date base = cpc.getFechaSolicitud() != null ? cpc.getFechaSolicitud() : new Date();
                cal.setTime(base);
                cal.add(java.util.Calendar.MONTH, mesesVigencia);
                cpc.setVigenciaFinal(cal.getTime());
                if (cpc.getVigenciaInicio() == null) cpc.setVigenciaInicio(base);
                clienteProductoColorRepository.save(cpc);
            }
            mesesVigencia = null;
            for (ColorPrecioDTO color : coloresDelProducto) color.setSeleccionado(false);
            coloresSeleccionados = new ArrayList<>();
            cargarColoresDelProducto();
            mostrarExito("Vigencia actualizada para " + seleccionados.size() + " color(es)");
        } catch (Exception e) {
            mostrarError("Error al cambiar vigencia: " + e.getMessage());
        }
    }

    /** Notifica manualmente a CATÁLOGO sobre los colores pendientes del producto seleccionado. */
    public void enviarNotificacionPendientesManual() {
        if (clienteSeleccionadoObj == null || productoSeleccionadoParaColores == null) {
            mostrarError("Selecciona un cliente y un producto primero");
            return;
        }
        try {
            List<String> pendientes = new ArrayList<>();
            if (coloresDelProducto != null) {
                for (ColorPrecioDTO c : coloresDelProducto) {
                    if ("PENDIENTE".equals(c.getNombreEstado()) || c.getNombreEstado() == null) {
                        pendientes.add(c.getNombreColor() +
                            (c.getCodigoColor() != null ? " (" + c.getCodigoColor() + ")" : ""));
                    }
                }
            }
            if (pendientes.isEmpty()) {
                mostrarError("No hay colores pendientes para notificar");
                return;
            }
            notificacionEmailService.notificarColoresSinPrecio(
                clienteSeleccionadoObj.getNombre(),
                productoSeleccionadoParaColores.getCodigoEstilo(),
                productoSeleccionadoParaColores.getCodigoPrototipo(),
                pendientes
            );
            mostrarExito("Notificación de pendientes enviada");
        } catch (Exception e) {
            mostrarError("Error al enviar notificación: " + e.getMessage());
        }
    }

    /** Notifica manualmente a COSTOS sobre los colores aprobados del producto seleccionado. */
    public void enviarNotificacionAprobadosManual() {
        if (clienteSeleccionadoObj == null || productoSeleccionadoParaColores == null) {
            mostrarError("Selecciona un cliente y un producto primero");
            return;
        }
        try {
            List<String> aprobados = new ArrayList<>();
            if (coloresDelProducto != null) {
                for (ColorPrecioDTO c : coloresDelProducto) {
                    if ("APROBADO".equals(c.getNombreEstado())) {
                        aprobados.add(c.getNombreColor() +
                            (c.getCodigoColor() != null ? " (" + c.getCodigoColor() + ")" : ""));
                    }
                }
            }
            if (aprobados.isEmpty()) {
                mostrarError("No hay colores aprobados para notificar");
                return;
            }
            notificacionEmailService.notificarColoresAprobados(
                clienteSeleccionadoObj.getNombre(),
                productoSeleccionadoParaColores.getCodigoEstilo(),
                productoSeleccionadoParaColores.getCodigoPrototipo(),
                aprobados
            );
            mostrarExito("Notificación de aprobados enviada");
        } catch (Exception e) {
            mostrarError("Error al enviar notificación: " + e.getMessage());
        }
    }

    public String getFiltroColor() {
        return filtroColor;
    }

    public void setFiltroColor(String filtroColor) {
        this.filtroColor = filtroColor;
    }

    public void filtrarColores() {
    }

    public void limpiarFiltroColor() {
        this.filtroColor = null;
    }

    public List<ProductoColor> getColoresProductoParaAsignar() {
        return coloresProductoParaAsignar;
    }

    public void setColoresProductoParaAsignar(List<ProductoColor> coloresProductoParaAsignar) {
        this.coloresProductoParaAsignar = coloresProductoParaAsignar;
    }

    public List<Integer> getColoresSeleccionadosIds() {
        return coloresSeleccionadosIds;
    }

    public void setColoresSeleccionadosIds(List<Integer> coloresSeleccionadosIds) {
        this.coloresSeleccionadosIds = coloresSeleccionadosIds;
    }

    public void descargarExcelCliente() {
        descargarExcel("PENDIENTE");
    }

    public void descargarExcelClienteNegociado() {
        descargarExcel("NEGOCIANDO");
    }

    public void descargarPdfCliente() {
        descargarPdf("PENDIENTE");
    }

    public void descargarPdfClienteNegociado() {
        descargarPdf("NEGOCIANDO");
    }

    private void descargarPdf(String estadoFiltro) {
        if (clienteSeleccionadoId == null || productosAgrupados == null || productosAgrupados.isEmpty()) {
            mostrarError("Selecciona un cliente con productos para descargar");
            return;
        }
        try {
            String nombreCliente = clienteSeleccionadoObj != null ? clienteSeleccionadoObj.getNombre() : "Cliente";
            byte[] pdfBytes = pdfExportService.exportarClienteProductosPdf(
                nombreCliente, clienteSeleccionadoId, productosAgrupados, filtroFecha, estadoFiltro);

            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();

            String sufijo = "NEGOCIANDO".equals(estadoFiltro) ? "negociados" : "pendientes";
            String filename = "precios_" + sufijo + "_"
                + nombreCliente.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]", "_").trim() + ".pdf";

            ec.responseReset();
            ec.setResponseContentType("application/pdf");
            ec.setResponseContentLength(pdfBytes.length);
            ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            ec.getResponseOutputStream().write(pdfBytes);
            fc.responseComplete();
        } catch (Exception e) {
            mostrarError("Error al generar PDF: " + e.getMessage());
        }
    }

    private void descargarExcel(String estadoFiltro) {
        if (clienteSeleccionadoId == null || productosAgrupados == null || productosAgrupados.isEmpty()) {
            mostrarError("Selecciona un cliente con productos para descargar");
            return;
        }
        try {
            String nombreCliente = clienteSeleccionadoObj != null ? clienteSeleccionadoObj.getNombre() : "Cliente";
            byte[] excelBytes = excelExportService.exportarClienteProductos(
                nombreCliente, clienteSeleccionadoId, productosAgrupados, filtroFecha, estadoFiltro);

            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();

            String sufijo = "NEGOCIANDO".equals(estadoFiltro) ? "negociados" : "pendientes";
            String filename = "precios_" + sufijo + "_"
                + nombreCliente.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]", "_").trim() + ".xlsx";

            ec.responseReset();
            ec.setResponseContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            ec.setResponseContentLength(excelBytes.length);
            ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            ec.getResponseOutputStream().write(excelBytes);
            fc.responseComplete();
        } catch (Exception e) {
            mostrarError("Error al generar Excel: " + e.getMessage());
        }
    }
}
