package com.ritchi.control_precios.service;

import com.ritchi.control_precios.model.entity.Producto;
import com.ritchi.control_precios.model.entity.ProductoColor;
import com.ritchi.control_precios.repository.ProductoColorRepository;
import com.ritchi.control_precios.repository.ProductoRepository;
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

    public ProductoService(ProductoRepository productoRepository,
                          ProductoColorRepository productoColorRepository) {
        this.productoRepository = productoRepository;
        this.productoColorRepository = productoColorRepository;
    }

    // ========== PRODUCTOS ==========
    
    public List<Producto> obtenerTodosProductos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> buscarProductoPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo);
    }

    public void crearProducto(String codigoPrototipo, String codigoEstilo, 
                             String descripcion, String imagenBase64) {
        // Validar códigos únicos
        if (codigoEstilo != null && !codigoEstilo.isEmpty() && 
            productoRepository.existsByCodigoEstilo(codigoEstilo)) {
            throw new RuntimeException("El código estilo '" + codigoEstilo + "' ya existe");
        }

        if (codigoPrototipo != null && !codigoPrototipo.isEmpty() && 
            productoRepository.existsByCodigoPrototipo(codigoPrototipo)) {
            throw new RuntimeException("El código prototipo '" + codigoPrototipo + "' ya existe");
        }

        // Crear producto
        Producto producto = new Producto();
        producto.setCodigoPrototipo(codigoPrototipo);
        producto.setCodigoEstilo(codigoEstilo);
        producto.setDescripcion(descripcion);
        producto.setImagenProducto(imagenBase64);
        producto.setCreadoEn(new Date());

        productoRepository.save(producto);
        
        System.out.println("✅ Producto creado: " + codigoEstilo + " / " + codigoPrototipo);
    }

    // ========== COLORES ==========
    
    public List<ProductoColor> obtenerColoresPorProducto(Integer idProducto) {
        return productoColorRepository.findByProductoIdProducto(idProducto);
    }

    public void agregarColorAProducto(Integer idProducto, String color) {
        // Validar que no exista ese color para el producto
        if (productoColorRepository.existsByProductoIdProductoAndColor(idProducto, color)) {
            throw new RuntimeException("El color '" + color + "' ya existe para este producto");
        }

        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoColor productoColor = new ProductoColor();
        productoColor.setProducto(producto);
        productoColor.setColor(color);
        productoColor.setCreadoEn(new Date());

        productoColorRepository.save(productoColor);
        
        System.out.println("✅ Color agregado: " + color + " al producto " + idProducto);
    }

    public void eliminarColor(Integer idProductoColor) {
        productoColorRepository.deleteById(idProductoColor);
        System.out.println("🗑️ Color eliminado");
    }
}