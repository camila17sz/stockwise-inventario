package com.inventario.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "codigos_verificacion")
@Data
@NoArgsConstructor
public class CodigoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 6)
    private String codigo;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    // Datos del usuario pendiente de verificación
    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;   // ya encriptado

    @Column(length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String rol;

    public CodigoVerificacion(String email, String codigo,
                               String username, String password,
                               String nombre, String rol) {
        this.email    = email;
        this.codigo   = codigo;
        this.expiraEn = LocalDateTime.now().plusMinutes(15);
        this.username = username;
        this.password = password;
        this.nombre   = nombre;
        this.rol      = rol;
    }

    public boolean estaVigente() {
        return LocalDateTime.now().isBefore(expiraEn);
    }
}
