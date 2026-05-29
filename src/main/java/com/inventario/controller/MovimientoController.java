package com.inventario.controller;

import com.inventario.model.Movimiento;
import com.inventario.model.Producto;
import com.inventario.service.MovimientoService;
import com.inventario.service.ProductoService;
import com.inventario.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/movimientos")
public class MovimientoController {

    @Autowired private MovimientoService movimientoService;
    @Autowired private ProductoService productoService;
    @Autowired private ProveedorService proveedorService;

    @GetMapping
    public String listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long proveedorId,
            Model model) {

        boolean hayFiltro = (tipo != null && !tipo.isBlank())
                || (motivo != null && !motivo.isBlank())
                || productoId != null
                || proveedorId != null;

        model.addAttribute("movimientos",
            hayFiltro
                ? movimientoService.findWithFilters(tipo, motivo, productoId, proveedorId)
                : movimientoService.findAll()
        );

        model.addAttribute("productos",   productoService.findAll());
        model.addAttribute("proveedores", proveedorService.findAll());
        model.addAttribute("tipos",       Movimiento.TipoMovimiento.values());
        model.addAttribute("motivos",     Movimiento.Motivo.values());

        model.addAttribute("filtroTipo",        tipo);
        model.addAttribute("filtroMotivo",      motivo);
        model.addAttribute("filtroProductoId",  productoId);
        model.addAttribute("filtroProveedorId", proveedorId);
        model.addAttribute("hayFiltro",         hayFiltro);
        model.addAttribute("activeMenu",        "movimientos");
        return "movimientos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("movimiento", new Movimiento());
        model.addAttribute("productos",  productoService.findAll());
        model.addAttribute("tipos",      Movimiento.TipoMovimiento.values());
        model.addAttribute("motivos",    Movimiento.Motivo.values());
        model.addAttribute("activeMenu", "movimientos");
        return "movimientos/form";
    }

    /**
     * Recibe los campos individualmente para evitar problemas de binding
     * con el objeto Producto anidado.
     */
    @PostMapping("/registrar")
    public String registrar(
            @RequestParam Long productoId,
            @RequestParam String tipo,
            @RequestParam String motivo,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) String observacion,
            RedirectAttributes flash) {
        try {
            Movimiento m = new Movimiento();

            Producto p = new Producto();
            p.setId(productoId);
            m.setProducto(p);

            m.setTipo(Movimiento.TipoMovimiento.valueOf(tipo));
            m.setMotivo(Movimiento.Motivo.valueOf(motivo));
            m.setCantidad(cantidad);
            m.setObservacion(observacion);

            movimientoService.registrar(m);
            flash.addFlashAttribute("success", "Movimiento registrado exitosamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/movimientos";
    }
}
