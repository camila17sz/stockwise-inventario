package com.inventario.config;

import com.inventario.model.Categoria;
import com.inventario.model.Producto;
import com.inventario.model.Proveedor;
import com.inventario.repository.CategoriaRepository;
import com.inventario.repository.ProductoRepository;
import com.inventario.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class EntityConverterConfig implements WebMvcConfigurer {

    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ProveedorRepository proveedorRepository;
    @Autowired private ProductoRepository productoRepository;

    @Override
    public void addFormatters(FormatterRegistry registry) {

        registry.addConverter(new Converter<String, Categoria>() {
            @Override
            public Categoria convert(String id) {
                if (id == null || id.isBlank()) return null;
                try { return categoriaRepository.findById(Long.parseLong(id)).orElse(null); }
                catch (NumberFormatException e) { return null; }
            }
        });

        registry.addConverter(new Converter<String, Proveedor>() {
            @Override
            public Proveedor convert(String id) {
                if (id == null || id.isBlank()) return null;
                try { return proveedorRepository.findById(Long.parseLong(id)).orElse(null); }
                catch (NumberFormatException e) { return null; }
            }
        });

        registry.addConverter(new Converter<String, Producto>() {
            @Override
            public Producto convert(String id) {
                if (id == null || id.isBlank()) return null;
                try { return productoRepository.findById(Long.parseLong(id)).orElse(null); }
                catch (NumberFormatException e) { return null; }
            }
        });
    }
}
