package com.ritchi.control_precios.controller;

import com.ritchi.control_precios.model.entity.Producto;
import com.ritchi.control_precios.model.entity.ProductoColor;
import com.ritchi.control_precios.service.ProductoService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Base64;
import java.util.List;

@Named(value = "productoController")
@ViewScoped
public class ProductoController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ProductoService productoService;

    private List<Producto> productos;
    private Producto nuevoProducto;
    private boolean mostrarDialogCrear;
    private String imagenBase64;
    private boolean tieneImagen;
    private Producto productoSeleccionado;
    private List<ProductoColor> coloresProducto;
    private String nuevoColor;
    private boolean mostrarDialogColores;
    private boolean mostrarDialogDetalles;

    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 800;
    private static final float QUALITY = 0.7f;

    @PostConstruct
    public void init() {
        System.out.println("📷 ProductoController inicializado");
        cargarProductos();
        nuevoProducto = new Producto();
        imagenBase64 = null;
        tieneImagen = false;
    }

    private void cargarProductos() {
        productos = productoService.obtenerTodosProductos();
        System.out.println("✅ Productos cargados: " + productos.size());
    }

    public void abrirDialogCrear() {
        nuevoProducto = new Producto();
        imagenBase64 = null;
        tieneImagen = false;
        mostrarDialogCrear = true;
    }

    public void handleFileUpload(FileUploadEvent event) {
        try {
            UploadedFile file = event.getFile();
            
            System.out.println("\n📸 ===== PROCESAMIENTO DE IMAGEN =====");
            System.out.println("📁 Archivo: " + file.getFileName());
            System.out.println("📏 Tamaño: " + (file.getSize() / 1024) + " KB");

            String contentType = file.getContentType();
            if (!contentType.startsWith("image/")) {
                mostrarError("Solo se permiten archivos de imagen");
                return;
            }

            long maxSize = 10 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                mostrarError("La imagen no puede superar los 10MB");
                return;
            }

            byte[] imagenComprimida = comprimirImagen(file.getContent(), contentType);

            if (imagenComprimida == null) {
                mostrarError("Error al procesar la imagen");
                return;
            }

            System.out.println("📏 Tamaño comprimido: " + (imagenComprimida.length / 1024) + " KB");

            String base64Image = Base64.getEncoder().encodeToString(imagenComprimida);
            imagenBase64 = "data:" + contentType + ";base64," + base64Image;
            tieneImagen = true;

            mostrarExito("Imagen cargada y optimizada: " + file.getFileName());
            System.out.println("✅ Imagen procesada exitosamente");

        } catch (Exception e) {
            System.err.println("❌ Error al cargar imagen: " + e.getMessage());
            mostrarError("Error al cargar la imagen: " + e.getMessage());
        }
    }

    private byte[] comprimirImagen(byte[] imagenOriginal, String contentType) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imagenOriginal);
            BufferedImage imagenBuffer = ImageIO.read(bais);

            if (imagenBuffer == null) {
                System.err.println("❌ No se pudo leer la imagen");
                return null;
            }

            int anchoOriginal = imagenBuffer.getWidth();
            int altoOriginal = imagenBuffer.getHeight();

            System.out.println("📐 Dimensiones originales: " + anchoOriginal + "x" + altoOriginal);

            int nuevoAncho = anchoOriginal;
            int nuevoAlto = altoOriginal;

            if (anchoOriginal > MAX_WIDTH || altoOriginal > MAX_HEIGHT) {
                double ratio = (double) anchoOriginal / altoOriginal;

                if (anchoOriginal > altoOriginal) {
                    nuevoAncho = MAX_WIDTH;
                    nuevoAlto = (int) (MAX_WIDTH / ratio);
                } else {
                    nuevoAlto = MAX_HEIGHT;
                    nuevoAncho = (int) (MAX_HEIGHT * ratio);
                }

                System.out.println("📐 Nuevas dimensiones: " + nuevoAncho + "x" + nuevoAlto);
            }

            BufferedImage imagenRedimensionada = new BufferedImage(
                    nuevoAncho, nuevoAlto, BufferedImage.TYPE_INT_RGB);

            Graphics2D g2d = imagenRedimensionada.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.drawImage(imagenBuffer, 0, 0, nuevoAncho, nuevoAlto, null);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            String formato = "jpg";
            if (contentType.contains("png")) {
                formato = "png";
            }

            ImageIO.write(imagenRedimensionada, formato, baos);

            return baos.toByteArray();

        } catch (IOException e) {
            System.err.println("❌ Error al comprimir imagen: " + e.getMessage());
            return null;
        }
    }

    public void limpiarImagen() {
        imagenBase64 = null;
        tieneImagen = false;
        mostrarInfo("Imagen eliminada");
    }

    public void crearProducto() {
        System.out.println("\n📷 ===== INICIO CREAR PRODUCTO =====");
        
        String estilo = nuevoProducto.getCodigoEstilo();
        String prototipo = nuevoProducto.getCodigoPrototipo();
        
        System.out.println("📋 Código Estilo: '" + estilo + "'");
        System.out.println("📋 Código Prototipo: '" + prototipo + "'");
        System.out.println("📝 Descripción: " + nuevoProducto.getDescripcion());
        
        boolean estiloVacio = estilo == null || estilo.trim().isEmpty();
        boolean prototipoVacio = prototipo == null || prototipo.trim().isEmpty();
        
        System.out.println("🔍 Estilo vacío? " + estiloVacio);
        System.out.println("🔍 Prototipo vacío? " + prototipoVacio);
        
        try {
            if (estiloVacio && prototipoVacio) {
                System.out.println("❌ Validación falló: Ambos códigos están vacíos");
                mostrarError("Debe ingresar al menos un código (Estilo o Prototipo)");
                return;
            }
            
            System.out.println("✅ Validación de códigos PASADA");
            
            if (nuevoProducto.getDescripcion() == null || nuevoProducto.getDescripcion().trim().isEmpty()) {
                mostrarError("La descripción es obligatoria");
                return;
            }

            productoService.crearProducto(
                    prototipo,
                    estilo,
                    nuevoProducto.getDescripcion(),
                    imagenBase64);

            mostrarExito("Producto creado correctamente");
            cargarProductos();
            mostrarDialogCrear = false;

            System.out.println("📷 ===== FIN CREAR PRODUCTO =====\n");

        } catch (RuntimeException e) {
            System.err.println("❌ Error: " + e.getMessage());
            mostrarError(e.getMessage());
        }
    }

    public void abrirDialogColores(Producto producto) {
        productoSeleccionado = producto;
        coloresProducto = productoService.obtenerColoresPorProducto(producto.getIdProducto());
        nuevoColor = "";
        mostrarDialogColores = true;
    }

    public void agregarColor() {
        System.out.println("🎨 Agregando color: " + nuevoColor);

        try {
            if (nuevoColor == null || nuevoColor.trim().isEmpty()) {
                mostrarError("Debe ingresar un color");
                return;
            }

            productoService.agregarColorAProducto(
                    productoSeleccionado.getIdProducto(),
                    nuevoColor.trim().toUpperCase());

            mostrarExito("Color agregado correctamente");
            coloresProducto = productoService.obtenerColoresPorProducto(productoSeleccionado.getIdProducto());
            nuevoColor = "";

        } catch (RuntimeException e) {
            mostrarError(e.getMessage());
        }
    }

    public void eliminarColor(ProductoColor color) {
        try {
            productoService.eliminarColor(color.getIdProductoColor());
            mostrarExito("Color eliminado");
            coloresProducto = productoService.obtenerColoresPorProducto(productoSeleccionado.getIdProducto());
        } catch (Exception e) {
            mostrarError("Error al eliminar color: " + e.getMessage());
        }
    }

    public void abrirDialogDetalles(Producto producto) {
        productoSeleccionado = producto;
        mostrarDialogDetalles = true;
    }

    public List<ProductoColor> obtenerColoresDelProducto(Integer idProducto) {
        if (idProducto == null) {
            return List.of();
        }
        return productoService.obtenerColoresPorProducto(idProducto);
    }

    private void mostrarExito(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "✅ Éxito", mensaje));
    }

    private void mostrarError(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Error", mensaje));
    }

    private void mostrarInfo(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "ℹ️ Info", mensaje));
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public Producto getNuevoProducto() {
        return nuevoProducto;
    }

    public void setNuevoProducto(Producto nuevoProducto) {
        this.nuevoProducto = nuevoProducto;
    }

    public boolean isMostrarDialogCrear() {
        return mostrarDialogCrear;
    }

    public void setMostrarDialogCrear(boolean mostrarDialogCrear) {
        this.mostrarDialogCrear = mostrarDialogCrear;
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public List<ProductoColor> getColoresProducto() {
        return coloresProducto;
    }

    public String getNuevoColor() {
        return nuevoColor;
    }

    public void setNuevoColor(String nuevoColor) {
        this.nuevoColor = nuevoColor;
    }

    public boolean isMostrarDialogColores() {
        return mostrarDialogColores;
    }

    public void setMostrarDialogColores(boolean mostrarDialogColores) {
        this.mostrarDialogColores = mostrarDialogColores;
    }

    public String getImagenBase64() {
        return imagenBase64;
    }

    public void setImagenBase64(String imagenBase64) {
        this.imagenBase64 = imagenBase64;
    }

    public boolean isTieneImagen() {
        return tieneImagen;
    }

    public void setTieneImagen(boolean tieneImagen) {
        this.tieneImagen = tieneImagen;
    }

    public boolean isMostrarDialogDetalles() {
        return mostrarDialogDetalles;
    }

    public void setMostrarDialogDetalles(boolean mostrarDialogDetalles) {
        this.mostrarDialogDetalles = mostrarDialogDetalles;
    }
}