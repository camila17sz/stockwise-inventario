package com.inventario.repository;

import com.inventario.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @Query("SELECT m FROM Movimiento m JOIN FETCH m.producto p LEFT JOIN FETCH p.proveedor ORDER BY m.createdAt DESC")
    List<Movimiento> findAllWithProducto();

    @Query("SELECT m FROM Movimiento m JOIN FETCH m.producto p LEFT JOIN FETCH p.proveedor WHERE p.id = :productoId ORDER BY m.createdAt DESC")
    List<Movimiento> findByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT m FROM Movimiento m JOIN FETCH m.producto p LEFT JOIN FETCH p.proveedor WHERE m.tipo = :tipo ORDER BY m.createdAt DESC")
    List<Movimiento> findByTipo(@Param("tipo") Movimiento.TipoMovimiento tipo);

    @Query("SELECT m FROM Movimiento m JOIN FETCH m.producto p LEFT JOIN FETCH p.proveedor WHERE m.motivo = :motivo ORDER BY m.createdAt DESC")
    List<Movimiento> findByMotivo(@Param("motivo") Movimiento.Motivo motivo);

    @Query("SELECT m FROM Movimiento m JOIN FETCH m.producto p LEFT JOIN FETCH p.proveedor WHERE p.proveedor.id = :proveedorId ORDER BY m.createdAt DESC")
    List<Movimiento> findByProveedorId(@Param("proveedorId") Long proveedorId);

    @Query("""
        SELECT m FROM Movimiento m JOIN FETCH m.producto p LEFT JOIN FETCH p.proveedor
        WHERE (:tipo IS NULL OR m.tipo = :tipo)
          AND (:motivo IS NULL OR m.motivo = :motivo)
          AND (:productoId IS NULL OR p.id = :productoId)
          AND (:proveedorId IS NULL OR p.proveedor.id = :proveedorId)
        ORDER BY m.createdAt DESC
    """)
    List<Movimiento> findWithFilters(
        @Param("tipo") Movimiento.TipoMovimiento tipo,
        @Param("motivo") Movimiento.Motivo motivo,
        @Param("productoId") Long productoId,
        @Param("proveedorId") Long proveedorId
    );
}
