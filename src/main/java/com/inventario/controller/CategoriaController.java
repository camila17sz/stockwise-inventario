package com.inventario.controller;

import com.inventario.model.Categoria;
import com.inventario.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired private CategoriaService categoriaService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaService.findAll());
        model.addAttribute("activeMenu", "categorias");
        return "categorias/lista";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("categoria", new Categoria());
        model.addAttribute("activeMenu", "categorias");
        return "categorias/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@Valid @ModelAttribute Categoria categoria, BindingResult result,
                          Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("activeMenu", "categorias");
            return "categorias/form";
        }
        categoriaService.save(categoria);
        flash.addFlashAttribute("success", "Categoría guardada exitosamente.");
        return "redirect:/categorias";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return categoriaService.findById(id).map(c -> {
            model.addAttribute("categoria", c);
            model.addAttribute("activeMenu", "categorias");
            return "categorias/form";
        }).orElseGet(() -> {
            flash.addFlashAttribute("error", "Categoría no encontrada.");
            return "redirect:/categorias";
        });
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        categoriaService.deleteById(id);
        flash.addFlashAttribute("success", "Categoría eliminada.");
        return "redirect:/categorias";
    }
}
