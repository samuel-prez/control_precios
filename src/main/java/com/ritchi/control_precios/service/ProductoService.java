package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.entity.Color;
import com.ritchi.control_precios.model.entity.Producto;
import com.ritchi.control_precios.model.entity.ProductoColor;
import com.ritchi.control_precios.repository.ColorRepository;
import com.ritchi.control_precios.repository.ProductoRepository;
import com.ritchi.control_precios.repository.ProductoColorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoColorRepository productoColorRepository;
    private final ColorRepository colorRepository;

    public ProductoService(ProductoRepository productoRepository,
                          ProductoColorRepository productoColorRepository,
                          ColorRepository colorRepository) {
        this.productoRepository = productoRepository;
        this.productoColorRepository = productoColorRepository;
        this.colorRepository = colorRepository;
    }

    public List<Producto> obtenerTodosProductos() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerProductosDisponiblesParaCliente(Integer idCliente) {
        return productoRepository.findProductosDisponiblesParaCliente(idCliente);
    }

    public List<Producto> obtenerProductosNoExclusivos() {
        return productoRepository.findProductosNoExclusivos();
    }

    public List<Producto> buscarPorCodigoEstilo(String codigoEstilo) {
        return productoRepository.findByCodigoEstiloContainingIgnoreCase(codigoEstilo);
    }

    public List<Producto> buscarPorCodigoPrototipo(String codigoPrototipo) {
        return productoRepository.findByCodigoPrototipoContainingIgnoreCase(codigoPrototipo);
    }

    public Optional<Producto> obtenerProductoPorId(Integer id) {
        return productoRepository.findById(id);
    }

    public Producto crearProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto crearProducto(String codigoPrototipo, String codigoEstilo, String descripcion, String imagenBase64) {
        Producto producto = new Producto();
        producto.setCodigoPrototipo(codigoPrototipo);
        producto.setCodigoEstilo(codigoEstilo);
        producto.setDescripcion(descripcion);
        producto.setImagenProducto(imagenBase64);
        producto.setCreadoEn(new Date());
        return productoRepository.save(producto);
    }

    public Optional<Producto> buscarProductoPorCodigo(String codigo) {
        Optional<Producto> porEstilo = productoRepository.findByCodigoEstilo(codigo);
        if (porEstilo.isPresent()) {
            return porEstilo;
        }
        return productoRepository.findByCodigoPrototipo(codigo);
    }

    public Producto actualizarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public void eliminarProducto(Integer id) {
        productoRepository.deleteById(id);
    }

    public List<ProductoColor> obtenerProductoColores(Integer idProducto) {
        return productoColorRepository.findByProducto_IdProducto(idProducto);
    }

    public List<ProductoColor> obtenerColoresActivosDelProducto(Integer idProducto) {
        return productoColorRepository.findByProducto_IdProductoAndActivoTrue(idProducto);
    }

    public List<ProductoColor> obtenerTodosColores() {
        return productoColorRepository.findAll();
    }

    public ProductoColor crearProductoColor(ProductoColor productoColor) {
        return productoColorRepository.save(productoColor);
    }

    public ProductoColor obtenerProductoColorPorId(Integer id) {
        return productoColorRepository.findById(id).orElse(null);
    }

    public ProductoColor actualizarProductoColor(ProductoColor productoColor) {
        ProductoColor saved = productoColorRepository.save(productoColor);
        productoColorRepository.flush();
        return saved;
    }

    public void eliminarProductoColor(Integer id) {
        productoColorRepository.deleteById(id);
    }

    public void activarColor(Integer idProductoColor) {
        ProductoColor productoColor = productoColorRepository.findById(idProductoColor)
            .orElseThrow(() -> new RuntimeException("ProductoColor no encontrado con ID: " + idProductoColor));

        productoColor.setActivo(true);
        productoColorRepository.save(productoColor);
    }

    public void desactivarColor(Integer idProductoColor) {
        ProductoColor productoColor = productoColorRepository.findById(idProductoColor)
            .orElseThrow(() -> new RuntimeException("ProductoColor no encontrado con ID: " + idProductoColor));

        productoColor.setActivo(false);
        productoColorRepository.save(productoColor);
    }

    public List<ProductoColor> obtenerColoresPorProducto(Integer idProducto) {
        return productoColorRepository.findByProducto_IdProducto(idProducto);
    }

    public void eliminarColor(Integer idProductoColor) {
        productoColorRepository.deleteById(idProductoColor);
    }

    public void agregarColorAProducto(Integer idProducto, String colorNombre) {
        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + idProducto));

        Color color = colorRepository.findByNombreIgnoreCase(colorNombre);
        if (color == null) {
            color = new Color();
            color.setNombre(colorNombre);
            color.setCreadoEn(new Date());
            color = colorRepository.save(color);
        }

        ProductoColor productoColor = new ProductoColor();
        productoColor.setProducto(producto);
        productoColor.setColor(color);
        productoColor.setActivo(true);
        productoColor.setCreadoEn(new Date());
        productoColorRepository.save(productoColor);
    }
}