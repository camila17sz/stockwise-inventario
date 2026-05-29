package com.inventario.controller;

import com.inventario.model.Proveedor;
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
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired private ProveedorService proveedorService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("proveedores", proveedorService.findAll());
        model.addAttribute("activeMenu", "proveedores");
        return "proveedores/lista";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("activeMenu", "proveedores");
        return "proveedores/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@Valid @ModelAttribute Proveedor proveedor, BindingResult result,
                          Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("activeMenu", "proveedores");
            return "proveedores/form";
        }
        proveedorService.save(proveedor);
        flash.addFlashAttribute("success", "Proveedor guardado exitosamente.");
        return "redirect:/proveedores";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        return proveedorService.findById(id).map(p -> {
            model.addAttribute("proveedor", p);
            model.addAttribute("activeMenu", "proveedores");
            return "proveedores/form";
        }).orElseGet(() -> {
            flash.addFlashAttribute("error", "Proveedor no encontrado.");
            return "redirect:/proveedores";
        });
    }

    @GetMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        proveedorService.deleteById(id);
        flash.addFlashAttribute("success", "Proveedor eliminado.");
        return "redirect:/proveedores";
    }
}
