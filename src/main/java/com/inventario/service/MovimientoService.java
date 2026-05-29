package com.inventario.service;

import com.inventario.model.Movimiento;
import com.inventario.model.Producto;
import com.inventario.repository.MovimientoRepository;
import com.inventario.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MovimientoService {

    @Autowired private MovimientoRepository movimientoRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private AlertaEmailService alertaEmailService;

    public List<Movimiento> findAll() {
        return movimientoRepository.findAllWithProducto();
    }

    public Optional<Movimiento> findById(Long id) {
        return movimientoRepository.findById(id);
    }

    public List<Movimiento> findByProducto(Long productoId) {
        return movimientoRepository.findByProductoId(productoId);
    }

    public List<Movimiento> findWithFilters(String tipoStr, String motivoStr,
                                             Long productoId, Long proveedorId) {
        Movimiento.TipoMovimiento tipo = null;
        if (tipoStr != null && !tipoStr.isBlank()) {
            tipo = Movimiento.TipoMovimiento.valueOf(tipoStr);
        }
        Movimiento.Motivo motivo = null;
        if (motivoStr != null && !motivoStr.isBlank()) {
            motivo = Movimiento.Motivo.valueOf(motivoStr);
        }
        return movimientoRepository.findWithFilters(tipo, motivo, productoId, proveedorId);
    }

    @Transactional
    public Movimiento registrar(Movimiento movimiento) {
        Producto producto = movimiento.getProducto();
        if (producto != null && producto.getId() != null) {
            Producto p = productoRepository.findByIdWithRelations(producto.getId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (movimiento.getTipo() == Movimiento.TipoMovimiento.ENTRADA) {
                p.setStockActual(p.getStockActual() + movimiento.getCantidad());
            } else {
                int nuevoStock = p.getStockActual() - movimiento.getCantidad();
                if (nuevoStock < 0) {
                    throw new RuntimeException("Stock insuficiente. Stock actual: " + p.getStockActual());
                }
                p.setStockActual(nuevoStock);
            }

            productoRepository.save(p);

            // ── Disparar alerta de email si el stock queda en o bajo el mínimo ──
            if (p.getStockActual() <= p.getStockMinimo()) {
                alertaEmailService.enviarAlertaStockBajo(p);
            }

            movimiento.setProducto(p);
        }
        return movimientoRepository.save(movimiento);
    }

    public long countTotal() {
        return movimientoRepository.count();
    }
}
