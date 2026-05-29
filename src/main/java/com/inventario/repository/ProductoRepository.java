package com.inventario.repository;

import com.inventario.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.proveedor WHERE p.activo = true")
    List<Producto> findByActivoTrue();

    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.proveedor WHERE p.activo = true AND p.stockActual <= p.stockMinimo")
    List<Producto> findProductosConStockBajo();

    List<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId);

    List<Producto> findByProveedorIdAndActivoTrue(Long proveedorId);

    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.categoria LEFT JOIN FETCH p.proveedor WHERE p.id = :id")
    Optional<Producto> findByIdWithRelations(Long id);

    long countByActivoTrue();

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.activo = true AND p.stockActual <= p.stockMinimo")
    long countProductosStockBajo();

    // Cuenta TODOS los productos de una categoría (activos e inactivos)
    // para que el número del código nunca se repita aunque se elimine un producto
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.id = :categoriaId")
    long countByCategoriaId(@Param("categoriaId") Long categoriaId);

    // Verifica si ya existe un código exacto (para evitar duplicados en edge cases)
    boolean existsByCodigo(String codigo);
}
