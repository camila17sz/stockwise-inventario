package com.inventario.config;

import com.inventario.model.*;
import com.inventario.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ProveedorRepository proveedorRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ── 3 usuarios, uno por rol ───────────────────────────────────────────
        crearUsuario("admin",    "admin123",    "admin@inventario.com",    "Administrador", "ROLE_ADMIN");
        crearUsuario("manejador","manejador123","manejador@inventario.com","Manejador",     "ROLE_MANEJADOR");
        crearUsuario("visor",    "visor123",    "visor@inventario.com",    "Visor",         "ROLE_VISOR");

        // ── Datos de demo ─────────────────────────────────────────────────────
        if (categoriaRepository.count() == 0) {
            Categoria elec  = cat("Electrónica",  "Equipos y dispositivos electrónicos");
            Categoria ofic  = cat("Oficina",       "Artículos de papelería y oficina");
            Categoria herra = cat("Herramientas",  "Herramientas y equipos");
            Categoria limp  = cat("Limpieza",      "Productos de aseo y limpieza");

            Proveedor prov1 = prov("TecnoDistribuciones S.A.", "ventas@tecnodist.com",  "3001234567", "Cra 50 #45-23, Medellín");
            Proveedor prov2 = prov("Suministros del Norte",    "contacto@sumnorte.com", "3107654321", "Calle 80 #20-10, Medellín");

            prod("Monitor 24\" Full HD",   "ELEC-001", 850000, 15, 5,  elec,  prov1);
            prod("Teclado Mecánico USB",   "ELEC-002", 180000,  3, 10, elec,  prov1);
            prod("Resma de Papel A4",      "OFIC-001",  15000, 50, 20, ofic,  prov2);
            prod("Destornillador de Punta","HERR-001",  25000,  2,  5, herra, prov2);
            prod("Desinfectante 1L",       "LIMP-001",  12000, 30, 15, limp,  prov2);

            System.out.println("✅ Datos de demo cargados.");
        }

        System.out.println("──────────────────────────────────────────");
        System.out.println("  Usuarios:");
        System.out.println("  admin      / admin123     → Administrador (todo)");
        System.out.println("  manejador  / manejador123 → Manejador (solo movimientos)");
        System.out.println("  visor      / visor123     → Visor (solo lectura)");
        System.out.println("──────────────────────────────────────────");
    }

    private void crearUsuario(String username, String pass, String email, String nombre, String rol) {
        if (!usuarioRepository.existsByUsername(username)) {
            Usuario u = new Usuario();
            u.setUsername(username); u.setPassword(passwordEncoder.encode(pass));
            u.setEmail(email); u.setNombre(nombre); u.setRol(rol); u.setActivo(true);
            usuarioRepository.save(u);
            System.out.println("✅ " + username + " / " + pass + "  [" + rol + "]");
        }
    }

    private Categoria cat(String nombre, String desc) {
        Categoria c = new Categoria(); c.setNombre(nombre); c.setDescripcion(desc);
        return categoriaRepository.save(c);
    }
    private Proveedor prov(String nombre, String email, String tel, String dir) {
        Proveedor p = new Proveedor(); p.setNombre(nombre); p.setEmail(email); p.setTelefono(tel); p.setDireccion(dir);
        return proveedorRepository.save(p);
    }
    private void prod(String nombre, String codigo, int precio, int stock, int stockMin, Categoria cat, Proveedor prov) {
        Producto p = new Producto(); p.setNombre(nombre); p.setCodigo(codigo);
        p.setPrecio(new BigDecimal(precio)); p.setStockActual(stock); p.setStockMinimo(stockMin);
        p.setCategoria(cat); p.setProveedor(prov); productoRepository.save(p);
    }
}
