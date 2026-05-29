package com.inventario.repository;

import com.inventario.model.CodigoVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Long> {

    Optional<CodigoVerificacion> findByEmail(String email);

    Optional<CodigoVerificacion> findByEmailAndCodigo(String email, String codigo);

    void deleteByEmail(String email);
}
