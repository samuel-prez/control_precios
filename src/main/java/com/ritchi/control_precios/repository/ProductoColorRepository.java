package com.ritchi.control_precios.repository;

import com.ritchi.control_precios.model.entity.ProductoColor;
import com.ritchi.control_precios.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductoColorRepository extends JpaRepository<ProductoColor, Integer> {
    
    // Obtener todos los colores de un producto
    List<ProductoColor> findByProducto(Producto producto);
    List<ProductoColor> findByProductoIdProducto(Integer idProducto);
    
    // Buscar color específico de un producto
    Optional<ProductoColor> findByProductoIdProductoAndColor(Integer idProducto, String color);
    
    // Validar si existe ese color para el producto
    boolean existsByProductoIdProductoAndColor(Integer idProducto, String color);
}