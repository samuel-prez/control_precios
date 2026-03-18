package com.ritchi.control_precios.controller;

import com.ritchi.control_precios.model.entity.Cliente;
import com.ritchi.control_precios.model.entity.Color;
import com.ritchi.control_precios.model.entity.Producto;
import com.ritchi.control_precios.model.entity.ProductoColor;
import com.ritchi.control_precios.service.ClienteService;
import com.ritchi.control_precios.service.ColorService;
import com.ritchi.control_precios.service.ExcelImportService;
import com.ritchi.control_precios.service.ImageCompressionService;
import com.ritchi.control_precios.service.NotificacionEmailService;
import com.ritchi.control_precios.service.ProductoService;
import com.ritchi.control_precios.model.entity.ClienteProducto;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.model.file.UploadedFile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("productoGestionController")
@ViewScoped
@Component
public class ProductoGestionController implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ProductoService productoService;
    private final ImageCompressionService imageCompressionService;
    private final ColorService colorService;
    private final ExcelImportService excelImportService;
    private final ClienteService clienteService;
    private final com.ritchi.control_precios.repository.ClienteProductoRepository clienteProductoRepository;
    private final com.ritchi.control_precios.repository.ClienteProductoColorRepository clienteProductoColorRepository;
    private final NotificacionEmailService notificacionEmailService;

    private List<Producto> productos;
    private List<Producto> productosFiltrados;
    private String terminoBusqueda;
    private Producto productoSeleccionado;
    private Producto productoSeleccionadoVer;
    private Producto productoEditando;
    private Producto nuevoProducto;
    private Producto productoSeleccionadoFila;
    private List<ProductoColor> coloresDelProducto;
    private ProductoColor nuevoColor;
    private ProductoColor productoColorEditando;
    private UploadedFile uploadedFile;
    private UploadedFile uploadedFile2;
    private List<Color> coloresDisponibles;
    private List<Color> coloresFiltrados;
    private Integer colorSeleccionadoId;
    private String terminoBusquedaColor;

    private UploadedFile archivoExcel;
    private StreamedContent plantillaExcel;

    private List<Cliente> clientesDisponibles;
    private Integer clienteExclusivoId;
    private List<Integer> clientesExclusivosIds = new ArrayList<>();
    private boolean esExclusivo;

    public ProductoGestionController(ProductoService productoService,
                                    ImageCompressionService imageCompressionService,
                                    ColorService colorService,
                                    ExcelImportService excelImportService,
                                    ClienteService clienteService,
                                    com.ritchi.control_precios.repository.ClienteProductoRepository clienteProductoRepository,
                                    com.ritchi.control_precios.repository.ClienteProductoColorRepository clienteProductoColorRepository,
                                    NotificacionEmailService notificacionEmailService) {
        this.productoService = productoService;
        this.imageCompressionService = imageCompressionService;
        this.colorService = colorService;
        this.excelImportService = excelImportService;
        this.clienteService = clienteService;
        this.clienteProductoRepository = clienteProductoRepository;
        this.clienteProductoColorRepository = clienteProductoColorRepository;
        this.notificacionEmailService = notificacionEmailService;
    }

    @PostConstruct
    public void init() {
        cargarProductos();
        cargarColoresDisponibles();
        cargarClientesDisponibles();
        nuevoProducto = new Producto();
        nuevoColor = new ProductoColor();
        coloresDelProducto = new ArrayList<>();
        terminoBusqueda = "";
        esExclusivo = false;
        clienteExclusivoId = null;
        clientesExclusivosIds = new ArrayList<>();
    }

    private void cargarColoresDisponibles() {
        try {
            coloresDisponibles = colorService.obtenerColoresActivos();
            coloresFiltrados = new ArrayList<>(coloresDisponibles);
        } catch (Exception e) {
            coloresDisponibles = new ArrayList<>();
            coloresFiltrados = new ArrayList<>();
        }
    }

    private void cargarClientesDisponibles() {
        try {
            clientesDisponibles = clienteService.obtenerTodosClientes();
        } catch (Exception e) {
            clientesDisponibles = new ArrayList<>();
        }
    }

    public void filtrarColores() {
        if (terminoBusquedaColor == null || terminoBusquedaColor.trim().isEmpty()) {
            coloresFiltrados = new ArrayList<>(coloresDisponibles);
        } else {
            String termino = terminoBusquedaColor.toLowerCase().trim();
            coloresFiltrados = coloresDisponibles.stream()
                .filter(c -> c.getNombre() != null && c.getNombre().toLowerCase().contains(termino))
                .collect(java.util.stream.Collectors.toList());
        }
    }

    public List<Color> completarColor(String query) {
        if (query == null || query.trim().isEmpty()) {
            return coloresDisponibles.stream().limit(50).toList();
        }

        String queryLower = query.toLowerCase().trim();

        List<Color> filtrados = coloresDisponibles.stream()
            .filter(c -> c.getNombre() != null && c.getNombre().toLowerCase().contains(queryLower))
            .collect(java.util.stream.Collectors.toList());

        filtrados.sort((c1, c2) -> {
            String n1 = c1.getNombre().toLowerCase();
            String n2 = c2.getNombre().toLowerCase();

            boolean exacto1 = n1.equals(queryLower);
            boolean exacto2 = n2.equals(queryLower);
            if (exacto1 && !exacto2) return -1;
            if (!exacto1 && exacto2) return 1;

            boolean empieza1 = n1.startsWith(queryLower);
            boolean empieza2 = n2.startsWith(queryLower);
            if (empieza1 && !empieza2) return -1;
            if (!empieza1 && empieza2) return 1;

            return n1.compareTo(n2);
        });

        return filtrados.stream().limit(50).toList();
    }

    private void cargarProductos() {
        try {
            productos = productoService.obtenerTodosProductos();
            productosFiltrados = new ArrayList<>(productos);
        } catch (Exception e) {
            productos = new ArrayList<>();
            productosFiltrados = new ArrayList<>();
        }
    }

    public void buscarProductos() {
        if (terminoBusqueda == null || terminoBusqueda.trim().isEmpty()) {
            productosFiltrados = new ArrayList<>(productos);
        } else {
            String termino = terminoBusqueda.toLowerCase().trim();
            productosFiltrados = productos.stream()
                .filter(p ->
                    (p.getCodigoEstilo() != null && p.getCodigoEstilo().toLowerCase().contains(termino)) ||
                    (p.getCodigoPrototipo() != null && p.getCodigoPrototipo().toLowerCase().contains(termino)) ||
                    (p.getDescripcion() != null && p.getDescripcion().toLowerCase().contains(termino))
                )
                .collect(java.util.stream.Collectors.toList());
        }
    }

    public void crearProducto() {
        try {
            boolean tieneCodigoEstilo = nuevoProducto.getCodigoEstilo() != null && !nuevoProducto.getCodigoEstilo().trim().isEmpty();
            boolean tieneCodigoPrototipo = nuevoProducto.getCodigoPrototipo() != null && !nuevoProducto.getCodigoPrototipo().trim().isEmpty();

            if (!tieneCodigoEstilo && !tieneCodigoPrototipo) {
                mostrarError("Debes ingresar al menos el Código Estilo o el Código Prototipo");
                return;
            }

            if (uploadedFile != null && uploadedFile.getSize() > 0) {
                if (uploadedFile.getSize() > 5 * 1024 * 1024) {
                    mostrarError("La imagen 1 es demasiado grande. Tamaño máximo: 5MB");
                    return;
                }
                String imagenBase64 = imageCompressionService.compressAndEncodeImage(
                    uploadedFile.getContent(), 800, 600, 0.85f);
                nuevoProducto.setImagenProducto(imagenBase64);
            }

            if (uploadedFile2 != null && uploadedFile2.getSize() > 0) {
                if (uploadedFile2.getSize() > 5 * 1024 * 1024) {
                    mostrarError("La imagen 2 es demasiado grande. Tamaño máximo: 5MB");
                    return;
                }
                String imagenBase64_2 = imageCompressionService.compressAndEncodeImage(
                    uploadedFile2.getContent(), 800, 600, 0.85f);
                nuevoProducto.setImagenProducto2(imagenBase64_2);
            }

            if (clientesExclusivosIds != null && !clientesExclusivosIds.isEmpty()) {
                nuevoProducto.setEsExclusivo(true);
                List<Cliente> clientesExcl = new ArrayList<>();
                for (Integer clienteId : clientesExclusivosIds) {
                    clienteService.obtenerClientePorId(clienteId).ifPresent(clientesExcl::add);
                }
                nuevoProducto.setClientesExclusivos(clientesExcl);
                if (!clientesExcl.isEmpty()) {
                    nuevoProducto.setClienteExclusivo(clientesExcl.get(0));
                }
            } else {
                nuevoProducto.setEsExclusivo(false);
                nuevoProducto.setClientesExclusivos(new ArrayList<>());
                nuevoProducto.setClienteExclusivo(null);
            }

            nuevoProducto.setCreadoEn(new Date());
            productoService.crearProducto(nuevoProducto);

            mostrarExito("Producto creado correctamente");

            cargarProductos();
            nuevoProducto = new Producto();
            uploadedFile = null;
            uploadedFile2 = null;
            esExclusivo = false;
            clienteExclusivoId = null;
            clientesExclusivosIds = new ArrayList<>();

        } catch (IOException e) {
            mostrarError("Error al procesar la imagen: " + e.getMessage());
        } catch (Exception e) {
            mostrarError("Error al crear producto: " + e.getMessage());
        }
    }

    public void seleccionarProducto(Producto producto) {
        if (productoSeleccionado != null && !productoSeleccionado.getIdProducto().equals(producto.getIdProducto())) {
            limpiarSeleccion();
        }
    }

    private void cargarColoresDelProducto(Integer idProducto) {
        try {
            coloresDelProducto = productoService.obtenerProductoColores(idProducto);
        } catch (Exception e) {
            coloresDelProducto = new ArrayList<>();
        }
    }

    public void agregarColor() {
        try {
            if (colorSeleccionadoId == null) {
                mostrarError("Debes seleccionar un color");
                return;
            }

            Color color = colorService.obtenerColorPorId(colorSeleccionadoId);
            boolean colorYaExiste = coloresDelProducto.stream()
                    .anyMatch(pc -> pc.getColor().getIdColor().equals(colorSeleccionadoId));

            if (colorYaExiste) {
                mostrarError("Este producto ya tiene el color '" + color.getNombre() + "'");
                return;
            }

            ProductoColor nuevoProductoColor = new ProductoColor();
            nuevoProductoColor.setProducto(productoSeleccionado);
            nuevoProductoColor.setColor(color);
            nuevoProductoColor.setCreadoEn(new Date());
            ProductoColor colorGuardado = productoService.crearProductoColor(nuevoProductoColor);

            mostrarExito("Color '" + color.getNombre() + "' agregado correctamente");

            String usuarioActual = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();

            List<ClienteProducto> clientesConProducto = clienteProductoRepository
                    .findByProductoIdProducto(productoSeleccionado.getIdProducto());
            List<String> nombresClientes = new ArrayList<>();

            int clientesActualizados = 0;
            for (ClienteProducto cp : clientesConProducto) {
                if (cp.getCliente() != null && cp.getCliente().getNombre() != null) {
                    nombresClientes.add(cp.getCliente().getNombre());

                    try {
                        com.ritchi.control_precios.model.entity.ClienteProductoColor cpc =
                            new com.ritchi.control_precios.model.entity.ClienteProductoColor();
                        cpc.setClienteProducto(cp);
                        cpc.setProductoColor(colorGuardado);
                        cpc.setActivo(true);
                        cpc.setCreadoEn(new Date());
                        clienteProductoColorRepository.save(cpc);
                        clientesActualizados++;
                    } catch (Exception e) {
                    }
                }
            }

            if (clientesActualizados > 0) {
                mostrarExito("Color '" + color.getNombre() + "' agregado correctamente y asignado a " + clientesActualizados + " cliente(s)");
            }

            if (!nombresClientes.isEmpty()) {
                notificacionEmailService.notificarNuevoColorEnProducto(
                        productoSeleccionado,
                        color.getNombre(),
                        usuarioActual,
                        nombresClientes
                );
            }

            cargarColoresDelProducto(productoSeleccionado.getIdProducto());
            colorSeleccionadoId = null;
            terminoBusquedaColor = "";
            coloresFiltrados = new ArrayList<>(coloresDisponibles);

        } catch (Exception e) {
            mostrarError("Error al agregar color: " + e.getMessage());
        }
    }

    public void prepararEdicionCodigo(ProductoColor productoColor) {
        this.productoColorEditando = new ProductoColor();
        this.productoColorEditando.setIdProductoColor(productoColor.getIdProductoColor());
        this.productoColorEditando.setProducto(productoColor.getProducto());
        this.productoColorEditando.setColor(productoColor.getColor());
        this.productoColorEditando.setCodigoColor(productoColor.getCodigoColor());
        this.productoColorEditando.setActivo(productoColor.getActivo());
        this.productoColorEditando.setCreadoEn(productoColor.getCreadoEn());
    }

    public void actualizarCodigoColor() {
        try {
            if (productoColorEditando == null) {
                mostrarError("Error: No hay color seleccionado para editar");
                return;
            }

            productoService.actualizarProductoColor(productoColorEditando);

            mostrarExito("Código actualizado correctamente");
            cargarColoresDelProducto(productoSeleccionado.getIdProducto());
            productoColorEditando = null;

        } catch (Exception e) {
            mostrarError("Error al actualizar código: " + e.getMessage());
        }
    }

    @Transactional
    public void eliminarColor(Integer idColor) {
        try {
            ProductoColor productoColor = coloresDelProducto.stream()
                    .filter(pc -> pc.getIdProductoColor().equals(idColor))
                    .findFirst()
                    .orElse(null);

            if (productoColor != null) {
                clienteProductoColorRepository.deleteByProductoColor_IdProductoColor(idColor);
            }

            productoService.eliminarProductoColor(idColor);

            mostrarExito("Color eliminado correctamente");
            cargarColoresDelProducto(productoSeleccionado.getIdProducto());

        } catch (Exception e) {
            mostrarError("Error al eliminar color: " + e.getMessage());
        }
    }

    public void prepararEdicion(Producto producto) {
        this.productoEditando = new Producto();
        this.productoEditando.setIdProducto(producto.getIdProducto());
        this.productoEditando.setCodigoEstilo(producto.getCodigoEstilo());
        this.productoEditando.setCodigoPrototipo(producto.getCodigoPrototipo());
        this.productoEditando.setDescripcion(producto.getDescripcion());
        this.productoEditando.setImagenProducto(producto.getImagenProducto());
        this.productoEditando.setImagenProducto2(producto.getImagenProducto2());
        this.productoEditando.setCreadoEn(producto.getCreadoEn());
        this.productoEditando.setTallas(producto.getTallas());
        this.productoEditando.setEsExclusivo(producto.getEsExclusivo());
        this.productoEditando.setClienteExclusivo(producto.getClienteExclusivo());
        this.productoEditando.setClientesExclusivos(producto.getClientesExclusivos() != null ?
            new ArrayList<>(producto.getClientesExclusivos()) : new ArrayList<>());

        this.esExclusivo = producto.getEsExclusivo() != null && producto.getEsExclusivo();
        this.clienteExclusivoId = producto.getClienteExclusivo() != null ?
            producto.getClienteExclusivo().getIdCliente() : null;

        this.clientesExclusivosIds = new ArrayList<>();
        if (producto.getClientesExclusivos() != null && !producto.getClientesExclusivos().isEmpty()) {
            for (Cliente c : producto.getClientesExclusivos()) {
                this.clientesExclusivosIds.add(c.getIdCliente());
            }
        } else if (producto.getClienteExclusivo() != null) {
            this.clientesExclusivosIds.add(producto.getClienteExclusivo().getIdCliente());
        }

        this.uploadedFile = null;
        this.uploadedFile2 = null;
    }

    public void actualizarProducto() {
        try {
            boolean tieneCodigoEstilo = productoEditando.getCodigoEstilo() != null && !productoEditando.getCodigoEstilo().trim().isEmpty();
            boolean tieneCodigoPrototipo = productoEditando.getCodigoPrototipo() != null && !productoEditando.getCodigoPrototipo().trim().isEmpty();

            if (!tieneCodigoEstilo && !tieneCodigoPrototipo) {
                mostrarError("Debes ingresar al menos el Código Estilo o el Código Prototipo");
                return;
            }

            if (uploadedFile != null && uploadedFile.getSize() > 0) {
                if (uploadedFile.getSize() > 5 * 1024 * 1024) {
                    mostrarError("La imagen 1 es demasiado grande. Tamaño máximo: 5MB");
                    return;
                }
                String imagenBase64 = imageCompressionService.compressAndEncodeImage(
                    uploadedFile.getContent(), 800, 600, 0.85f);
                productoEditando.setImagenProducto(imagenBase64);
            }

            if (uploadedFile2 != null && uploadedFile2.getSize() > 0) {
                if (uploadedFile2.getSize() > 5 * 1024 * 1024) {
                    mostrarError("La imagen 2 es demasiado grande. Tamaño máximo: 5MB");
                    return;
                }
                String imagenBase64_2 = imageCompressionService.compressAndEncodeImage(
                    uploadedFile2.getContent(), 800, 600, 0.85f);
                productoEditando.setImagenProducto2(imagenBase64_2);
            }

            if (clientesExclusivosIds != null && !clientesExclusivosIds.isEmpty()) {
                productoEditando.setEsExclusivo(true);
                List<Cliente> clientesExcl = new ArrayList<>();
                for (Integer clienteId : clientesExclusivosIds) {
                    clienteService.obtenerClientePorId(clienteId).ifPresent(clientesExcl::add);
                }
                productoEditando.setClientesExclusivos(clientesExcl);
                if (!clientesExcl.isEmpty()) {
                    productoEditando.setClienteExclusivo(clientesExcl.get(0));
                }
            } else {
                productoEditando.setEsExclusivo(false);
                productoEditando.setClientesExclusivos(new ArrayList<>());
                productoEditando.setClienteExclusivo(null);
            }

            productoService.actualizarProducto(productoEditando);

            mostrarExito("Producto actualizado correctamente");

            cargarProductos();
            productoEditando = null;
            uploadedFile = null;
            uploadedFile2 = null;
            esExclusivo = false;
            clienteExclusivoId = null;
            clientesExclusivosIds = new ArrayList<>();

        } catch (IOException e) {
            mostrarError("Error al procesar la imagen: " + e.getMessage());
        } catch (Exception e) {
            mostrarError("Error al actualizar producto: " + e.getMessage());
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public void eliminarProducto(Integer idProducto) {
        try {
            List<ProductoColor> colores = productoService.obtenerProductoColores(idProducto);

            List<com.ritchi.control_precios.model.entity.ClienteProducto> clienteProductos =
                    clienteProductoRepository.findAll().stream()
                            .filter(cp -> cp.getProducto().getIdProducto().equals(idProducto))
                            .collect(java.util.stream.Collectors.toList());

            for (com.ritchi.control_precios.model.entity.ClienteProducto cp : clienteProductos) {
                clienteProductoColorRepository.deleteByClienteProducto_IdClienteProducto(cp.getIdClienteProducto());
            }

            if (!clienteProductos.isEmpty()) {
                clienteProductoRepository.deleteAll(clienteProductos);
            }

            for (ProductoColor color : colores) {
                productoService.eliminarProductoColor(color.getIdProductoColor());
            }

            productoService.eliminarProducto(idProducto);

            mostrarExito("Producto eliminado correctamente");
            cargarProductos();
            if (productoSeleccionado != null && productoSeleccionado.getIdProducto().equals(idProducto)) {
                productoSeleccionado = null;
                coloresDelProducto = new ArrayList<>();
            }

            if (productoSeleccionadoFila != null && productoSeleccionadoFila.getIdProducto().equals(idProducto)) {
                productoSeleccionadoFila = null;
            }

        } catch (Exception e) {
            mostrarError("Error al eliminar producto: " + e.getMessage());
        }
    }

    public void seleccionarProductoParaVer(Producto producto) {
        this.productoSeleccionadoVer = producto;
    }

    public void limpiarSeleccion() {
        this.productoSeleccionado = null;
        this.coloresDelProducto = new ArrayList<>();
    }

    public void abrirPanelColores(Producto producto) {
        this.productoSeleccionado = producto;
        cargarColoresDelProducto(producto.getIdProducto());
        nuevoColor = new ProductoColor();
    }

    public List<ProductoColor> obtenerPrimerosColores(Integer idProducto, int cantidad) {
        try {
            List<ProductoColor> colores = productoService.obtenerProductoColores(idProducto);
            return colores.stream().limit(cantidad).toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public int contarColores(Integer idProducto) {
        try {
            List<ProductoColor> colores = productoService.obtenerProductoColores(idProducto);
            return colores.size();
        } catch (Exception e) {
            return 0;
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

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(Producto productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }

    public Producto getNuevoProducto() {
        return nuevoProducto;
    }

    public void setNuevoProducto(Producto nuevoProducto) {
        this.nuevoProducto = nuevoProducto;
    }

    public List<ProductoColor> getColoresDelProducto() {
        return coloresDelProducto;
    }

    public void setColoresDelProducto(List<ProductoColor> coloresDelProducto) {
        this.coloresDelProducto = coloresDelProducto;
    }

    public ProductoColor getNuevoColor() {
        return nuevoColor;
    }

    public void setNuevoColor(ProductoColor nuevoColor) {
        this.nuevoColor = nuevoColor;
    }

    public Producto getProductoSeleccionadoVer() {
        return productoSeleccionadoVer;
    }

    public void setProductoSeleccionadoVer(Producto productoSeleccionadoVer) {
        this.productoSeleccionadoVer = productoSeleccionadoVer;
    }

    public Producto getProductoEditando() {
        return productoEditando;
    }

    public void setProductoEditando(Producto productoEditando) {
        this.productoEditando = productoEditando;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public UploadedFile getUploadedFile2() {
        return uploadedFile2;
    }

    public void setUploadedFile2(UploadedFile uploadedFile2) {
        this.uploadedFile2 = uploadedFile2;
    }

    public List<Color> getColoresDisponibles() {
        return coloresDisponibles;
    }

    public void setColoresDisponibles(List<Color> coloresDisponibles) {
        this.coloresDisponibles = coloresDisponibles;
    }

    public Integer getColorSeleccionadoId() {
        return colorSeleccionadoId;
    }

    public void setColorSeleccionadoId(Integer colorSeleccionadoId) {
        this.colorSeleccionadoId = colorSeleccionadoId;
    }

    public Producto getProductoSeleccionadoFila() {
        return productoSeleccionadoFila;
    }

    public void setProductoSeleccionadoFila(Producto productoSeleccionadoFila) {
        this.productoSeleccionadoFila = productoSeleccionadoFila;
    }

    public List<Producto> getProductosFiltrados() {
        return productosFiltrados;
    }

    public void setProductosFiltrados(List<Producto> productosFiltrados) {
        this.productosFiltrados = productosFiltrados;
    }

    public String getTerminoBusqueda() {
        return terminoBusqueda;
    }

    public void setTerminoBusqueda(String terminoBusqueda) {
        this.terminoBusqueda = terminoBusqueda;
    }

    public List<Color> getColoresFiltrados() {
        return coloresFiltrados;
    }

    public void setColoresFiltrados(List<Color> coloresFiltrados) {
        this.coloresFiltrados = coloresFiltrados;
    }

    public String getTerminoBusquedaColor() {
        return terminoBusquedaColor;
    }

    public void setTerminoBusquedaColor(String terminoBusquedaColor) {
        this.terminoBusquedaColor = terminoBusquedaColor;
    }

    public ProductoColor getProductoColorEditando() {
        return productoColorEditando;
    }

    public void setProductoColorEditando(ProductoColor productoColorEditando) {
        this.productoColorEditando = productoColorEditando;
    }

    public List<Cliente> getClientesDisponibles() {
        return clientesDisponibles;
    }

    public void setClientesDisponibles(List<Cliente> clientesDisponibles) {
        this.clientesDisponibles = clientesDisponibles;
    }

    public Integer getClienteExclusivoId() {
        return clienteExclusivoId;
    }

    public void setClienteExclusivoId(Integer clienteExclusivoId) {
        this.clienteExclusivoId = clienteExclusivoId;
    }

    public List<Integer> getClientesExclusivosIds() {
        return clientesExclusivosIds;
    }

    public void setClientesExclusivosIds(List<Integer> clientesExclusivosIds) {
        this.clientesExclusivosIds = clientesExclusivosIds;
    }

    public boolean isEsExclusivo() {
        return esExclusivo;
    }

    public void setEsExclusivo(boolean esExclusivo) {
        this.esExclusivo = esExclusivo;
    }

    public boolean isUsuarioSoloConsulta() {
        try {
            org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null) {
                return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_CONSULTA"));
            }
        } catch (Exception e) {
        }
        return false;
    }

    public StreamedContent getPlantillaExcel() {
        try {
            byte[] contenido = excelImportService.generarPlantillaProductos();
            InputStream stream = new ByteArrayInputStream(contenido);

            return DefaultStreamedContent.builder()
                    .name("plantilla_productos.xlsx")
                    .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .stream(() -> stream)
                    .build();
        } catch (Exception e) {
            mostrarError("Error al generar la plantilla: " + e.getMessage());
            return null;
        }
    }

    public void importarProductosDesdeExcel() {

        if (archivoExcel == null || archivoExcel.getSize() == 0) {
            mostrarError("Debe seleccionar un archivo Excel");
            return;
        }

        String fileName = archivoExcel.getFileName().toLowerCase();
        if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
            mostrarError("El archivo debe ser un archivo Excel (.xlsx o .xls)");
            return;
        }

        try {
            InputStream inputStream = archivoExcel.getInputStream();
            ExcelImportService.ImportResult resultado = excelImportService.importarProductos(inputStream);

            if (resultado.tieneErrores()) {
                StringBuilder erroresMsg = new StringBuilder();
                erroresMsg.append(resultado.getResumen()).append("\n\n");
                erroresMsg.append("ERRORES (filas rechazadas):\n");
                for (String error : resultado.getErrores()) {
                    erroresMsg.append("• ").append(error).append("\n");
                }
                if (resultado.tieneAdvertencias()) {
                    erroresMsg.append("\nADVERTENCIAS:\n");
                    for (String adv : resultado.getAdvertencias()) {
                        erroresMsg.append("• ").append(adv).append("\n");
                    }
                }
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Importación con errores", erroresMsg.toString()));
            } else if (resultado.tieneAdvertencias()) {
                StringBuilder advMsg = new StringBuilder();
                advMsg.append(resultado.getResumen()).append("\n\nADVERTENCIAS:\n");
                for (String adv : resultado.getAdvertencias()) {
                    advMsg.append("• ").append(adv).append("\n");
                }
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Importación exitosa con advertencias", advMsg.toString()));
            } else {
                mostrarExito("Importación exitosa. " + resultado.getResumen());
            }

            cargarProductos();
            archivoExcel = null;

        } catch (Exception e) {
            mostrarError("Error al importar el archivo: " + e.getMessage());
        }
    }

    public UploadedFile getArchivoExcel() {
        return archivoExcel;
    }

    public void setArchivoExcel(UploadedFile archivoExcel) {
        this.archivoExcel = archivoExcel;
    }
}