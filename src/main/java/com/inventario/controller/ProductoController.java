package com.inventario.controller;

import com.inventario.model.Producto;
import com.inventario.service.CategoriaService;
import com.inventario.service.ProductoService;
import com.inventario.service.ProveedorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired private ProductoService productoService;
    @Autowired private CategoriaService categoriaService;
    @Autowired private ProveedorService proveedorService;

    // TODOS los roles autenticados pueden ver la lista
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoService.findAll());
        model.addAttribute("activeMenu", "productos");
        return "productos/lista";
    }

    // Solo ADMIN puede crear/editar/eliminar
    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaService.findActivas());
        model.addAttribute("proveedores", proveedorService.findActivos());
        model.addAttribute("activeMenu", "productos");
        return "productos/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@Valid @ModelAttribute Producto producto, BindingResult result,
                          Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.findActivas());
            model.addAttribute("proveedores", proveedorService.findActivos());
            model.addAttribute("activeMenu", "productos");
            return "productos/form";
        }
        productoService.save(producto);
        flash.addFlashAttribute("success", "Producto guardado exitosamente.");
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return productoService.findById(id).map(p -> {
            model.addAttribute("producto", p);
            model.addAttribute("categorias", categoriaService.findActivas());
            model.addAttribute("proveedores", proveedorService.findActivos());
            model.addAttribute("activeMenu", "productos");
            return "productos/form";
        }).orElseGet(() -> {
            flash.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/productos";
        });
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        productoService.deleteById(id);
        flash.addFlashAttribute("success", "Producto eliminado.");
        return "redirect:/productos";
    }
}
