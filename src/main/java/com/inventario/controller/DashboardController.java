package com.inventario.controller;

import com.inventario.model.Producto;
import com.inventario.service.AlertaEmailService;
import com.inventario.service.CategoriaService;
import com.inventario.service.MovimientoService;
import com.inventario.service.ProductoService;
import com.inventario.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

@Controller
public class DashboardController {

    @Autowired private ProductoService productoService;
    @Autowired private CategoriaService categoriaService;
    @Autowired private ProveedorService proveedorService;
    @Autowired private MovimientoService movimientoService;
    @Autowired private AlertaEmailService alertaEmailService;

    @GetMapping("/")
    public String root() { return "redirect:/dashboard"; }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // KPIs
        model.addAttribute("totalProductos",   productoService.countTotal());
        model.addAttribute("totalCategorias",  categoriaService.count());
        model.addAttribute("totalProveedores", proveedorService.count());
        model.addAttribute("totalMovimientos", movimientoService.countTotal());
        model.addAttribute("alertasStock",     productoService.countStockBajo());
        model.addAttribute("productosStockBajo",  productoService.findProductosStockBajo());
        model.addAttribute("ultimosMovimientos",
            movimientoService.findAll().stream().limit(5).toList());

        // ── Datos para Google Charts ──────────────────────────────────────

        // 1) Pie Chart: Stock actual vs Stock mínimo por producto (top 8)
        List<Producto> productos = productoService.findAll();
        List<List<Object>> stockData = new ArrayList<>();
        stockData.add(Arrays.asList("Producto", "Stock Actual", "Stock Mínimo"));
        productos.stream().limit(8).forEach(p ->
            stockData.add(Arrays.asList(p.getNombre(), p.getStockActual(), p.getStockMinimo()))
        );

        // 2) Pie Chart: Distribución de productos por categoría
        Map<String, Long> porCategoria = new LinkedHashMap<>();
        productos.forEach(p -> {
            String cat = (p.getCategoria() != null) ? p.getCategoria().getNombre() : "Sin categoría";
            porCategoria.merge(cat, 1L, Long::sum);
        });
        List<List<Object>> categoriaData = new ArrayList<>();
        categoriaData.add(Arrays.asList("Categoría", "Cantidad"));
        porCategoria.forEach((cat, count) -> categoriaData.add(Arrays.asList(cat, count)));

        // 3) Bar Chart: Entradas vs Salidas (de los últimos movimientos)
        long entradas = movimientoService.findAll().stream()
            .filter(m -> m.getTipo().name().equals("ENTRADA")).count();
        long salidas = movimientoService.findAll().stream()
            .filter(m -> m.getTipo().name().equals("SALIDA")).count();
        List<List<Object>> movData = new ArrayList<>();
        movData.add(Arrays.asList("Tipo", "Cantidad"));
        movData.add(Arrays.asList("Entradas", entradas));
        movData.add(Arrays.asList("Salidas",  salidas));

        try {
            ObjectMapper mapper = new ObjectMapper();
            model.addAttribute("chartStockData",     mapper.writeValueAsString(stockData));
            model.addAttribute("chartCategoriaData", mapper.writeValueAsString(categoriaData));
            model.addAttribute("chartMovData",       mapper.writeValueAsString(movData));
        } catch (Exception e) {
            model.addAttribute("chartStockData",     "[]");
            model.addAttribute("chartCategoriaData", "[]");
            model.addAttribute("chartMovData",       "[]");
        }

        model.addAttribute("activeMenu", "dashboard");
        return "dashboard/home";
    }

    /**
     * Endpoint para enviar manualmente el resumen de alertas de stock.
     * Solo accesible para ADMIN desde el dashboard.
     */
    @PostMapping("/dashboard/enviar-alertas")
    public String enviarAlertas(RedirectAttributes flash) {
        List<Producto> enAlerta = productoService.findProductosStockBajo();
        if (enAlerta.isEmpty()) {
            flash.addFlashAttribute("alertMsg", "✅ No hay productos con stock bajo en este momento.");
        } else {
            alertaEmailService.enviarResumenStockBajo(enAlerta);
            flash.addFlashAttribute("alertMsg",
                "📧 Resumen enviado: " + enAlerta.size() + " producto(s) en alerta.");
        }
        return "redirect:/dashboard";
    }

        @GetMapping("/dashboard/test-email")
    public String testEmail(RedirectAttributes flash) {
    List<Producto> enAlerta = productoService.findProductosStockBajo();
    if (enAlerta.isEmpty()) {
        // Si no hay productos en alerta, crea uno falso para la prueba
        com.inventario.model.Producto fake = new com.inventario.model.Producto();
        fake.setNombre("Producto de Prueba");
        fake.setCodigo("TEST-001");
        fake.setStockActual(2);
        fake.setStockMinimo(10);
        alertaEmailService.enviarAlertaStockBajo(fake);
        flash.addFlashAttribute("alertMsg", "📧 Correo de prueba enviado.");
    } else {
        alertaEmailService.enviarResumenStockBajo(enAlerta);
        flash.addFlashAttribute("alertMsg", "📧 Resumen real enviado con " + enAlerta.size() + " producto(s).");
    }
    return "redirect:/dashboard";
    }
}
