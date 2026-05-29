package com.inventario.service;

import com.inventario.model.Categoria;
import com.inventario.model.Producto;
import com.inventario.repository.CategoriaRepository;
import com.inventario.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    @Autowired private ProductoRepository productoRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    public List<Producto> findAll() {
        return productoRepository.findByActivoTrue();
    }

    public Optional<Producto> findById(Long id) {
        return productoRepository.findByIdWithRelations(id);
    }

    @Transactional
    public Producto save(Producto producto) {
        // Solo generar código automático si es un producto nuevo (sin id) y sin código asignado
        if (producto.getId() == null && (producto.getCodigo() == null || producto.getCodigo().isBlank())) {
            producto.setCodigo(generarCodigo(producto.getCategoria()));
        }
        return productoRepository.save(producto);
    }

    /**
     * Genera un código automático según la categoría.
     * Formato: PREFIJO-NNN  (ej: ELEC-001, OFIC-003)
     *
     * El prefijo se deriva tomando las primeras 4 letras del nombre de la categoría,
     * en mayúsculas y sin tildes. Si no hay categoría, usa "PROD".
     *
     * El número es la cantidad total de productos en esa categoría + 1,
     * con mínimo 3 dígitos (001, 002 … 999, 1000).
     */
    private String generarCodigo(Categoria categoria) {
        String prefijo;
        long siguiente;

        if (categoria != null && categoria.getId() != null) {
            // Prefijo: primeras 4 letras del nombre, sin tildes, en mayúsculas
            prefijo = normalizarPrefijo(categoria.getNombre());
            // Siguiente número = total de productos en esa categoría (incluyendo inactivos) + 1
            siguiente = productoRepository.countByCategoriaId(categoria.getId()) + 1;
        } else {
            prefijo = "PROD";
            siguiente = productoRepository.count() + 1;
        }

        // Formatear número con mínimo 3 dígitos
        String numero = String.format("%03d", siguiente);
        String codigo = prefijo + "-" + numero;

        // En el caso (raro) de colisión, incrementar hasta encontrar uno libre
        while (productoRepository.existsByCodigo(codigo)) {
            siguiente++;
            numero = String.format("%03d", siguiente);
            codigo = prefijo + "-" + numero;
        }

        return codigo;
    }

    /**
     * Convierte el nombre de la categoría en un prefijo de hasta 4 letras.
     * Ejemplos:
     *   "Electrónica"  → "ELEC"
     *   "Oficina"      → "OFIC"
     *   "Herramientas" → "HERR"
     *   "Limpieza"     → "LIMP"
     *   "TI"           → "TI"
     */
    private String normalizarPrefijo(String nombreCategoria) {
        if (nombreCategoria == null || nombreCategoria.isBlank()) return "PROD";

        // Quitar tildes y caracteres especiales
        String limpio = nombreCategoria
            .toUpperCase()
            .replace('Á', 'A').replace('É', 'E').replace('Í', 'I')
            .replace('Ó', 'O').replace('Ú', 'U').replace('Ñ', 'N')
            .replaceAll("[^A-Z]", ""); // solo letras

        // Tomar las primeras 4 letras
        return limpio.length() >= 4 ? limpio.substring(0, 4) : limpio;
    }

    @Transactional
    public void deleteById(Long id) {
        productoRepository.findById(id).ifPresent(p -> {
            p.setActivo(false);
            productoRepository.save(p);
        });
    }

    public List<Producto> findProductosStockBajo() {
        return productoRepository.findProductosConStockBajo();
    }

    public long countTotal() {
        return productoRepository.countByActivoTrue();
    }

    public long countStockBajo() {
        return productoRepository.countProductosStockBajo();
    }
}
