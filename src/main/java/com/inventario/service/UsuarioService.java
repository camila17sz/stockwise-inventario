package com.inventario.service;

import com.inventario.model.CodigoVerificacion;
import com.inventario.model.Usuario;
import com.inventario.repository.CodigoVerificacionRepository;
import com.inventario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class UsuarioService {

    // ─── Códigos secretos de acceso ──────────────────────────────────────────
    private static final String CODIGO_ADMIN     = "ADMIN-2024-STOCK";
    private static final String CODIGO_MANEJADOR = "MANEJ-2024-STOCK";

    @Autowired private UsuarioRepository             usuarioRepository;
    @Autowired private CodigoVerificacionRepository  codigoRepo;
    @Autowired private PasswordEncoder               passwordEncoder;
    @Autowired private VerificacionEmailService      emailService;

    private final SecureRandom random = new SecureRandom();

    public enum ResultadoRegistro {
        OK,
        USERNAME_DUPLICADO,
        EMAIL_DUPLICADO,
        CODIGO_INVALIDO,
        PASSWORDS_NO_COINCIDEN
    }

    public enum ResultadoVerificacion {
        OK,
        CODIGO_INCORRECTO,
        CODIGO_EXPIRADO,
        NO_ENCONTRADO
    }

    // ─── PASO 1: Validar datos, guardar código temporal, enviar email ─────────
    @Transactional
    public ResultadoRegistro iniciarRegistro(String username,
                                              String email,
                                              String nombre,
                                              String password,
                                              String confirmarPassword,
                                              String rolSolicitado,
                                              String codigoAcceso) {

        if (!password.equals(confirmarPassword))       return ResultadoRegistro.PASSWORDS_NO_COINCIDEN;
        if (usuarioRepository.existsByUsername(username)) return ResultadoRegistro.USERNAME_DUPLICADO;
        if (usuarioRepository.existsByEmail(email))       return ResultadoRegistro.EMAIL_DUPLICADO;

        // Determinar rol
        String rol;
        switch (rolSolicitado) {
            case "ADMIN":
                if (!CODIGO_ADMIN.equals(codigoAcceso)) return ResultadoRegistro.CODIGO_INVALIDO;
                rol = "ROLE_ADMIN";
                break;
            case "MANEJADOR":
                if (!CODIGO_MANEJADOR.equals(codigoAcceso)) return ResultadoRegistro.CODIGO_INVALIDO;
                rol = "ROLE_MANEJADOR";
                break;
            default:
                rol = "ROLE_VISOR";
        }

        // Generar código de 6 dígitos
        String codigo = String.format("%06d", random.nextInt(1_000_000));

        // Borrar código previo si existe (reenvío)
        codigoRepo.deleteByEmail(email);

        // Guardar registro pendiente
        CodigoVerificacion pendiente = new CodigoVerificacion(
                email, codigo, username,
                passwordEncoder.encode(password),
                nombre, rol);
        codigoRepo.save(pendiente);

        // Enviar correo (async, no bloquea)
        emailService.enviarCodigoVerificacion(email, nombre, codigo);

        return ResultadoRegistro.OK;
    }

    // ─── PASO 2: Verificar código → activar usuario ───────────────────────────
    @Transactional
    public ResultadoVerificacion verificarCodigo(String email, String codigo) {

        CodigoVerificacion pendiente = codigoRepo.findByEmail(email)
                .orElse(null);

        if (pendiente == null)               return ResultadoVerificacion.NO_ENCONTRADO;
        if (!pendiente.estaVigente())        return ResultadoVerificacion.CODIGO_EXPIRADO;
        if (!pendiente.getCodigo().equals(codigo.trim())) return ResultadoVerificacion.CODIGO_INCORRECTO;

        // Crear usuario definitivo
        Usuario u = new Usuario();
        u.setUsername(pendiente.getUsername());
        u.setEmail(pendiente.getEmail());
        u.setNombre(pendiente.getNombre());
        u.setPassword(pendiente.getPassword());
        u.setRol(pendiente.getRol());
        u.setActivo(true);
        usuarioRepository.save(u);

        // Limpiar código usado
        codigoRepo.deleteByEmail(email);

        return ResultadoVerificacion.OK;
    }

    // ─── Reenviar código ──────────────────────────────────────────────────────
    @Transactional
    public boolean reenviarCodigo(String email) {
        CodigoVerificacion pendiente = codigoRepo.findByEmail(email).orElse(null);
        if (pendiente == null) return false;

        String nuevoCodigo = String.format("%06d", random.nextInt(1_000_000));
        pendiente.setCodigo(nuevoCodigo);
        pendiente.setExpiraEn(java.time.LocalDateTime.now().plusMinutes(15));
        codigoRepo.save(pendiente);

        emailService.enviarCodigoVerificacion(email, pendiente.getNombre(), nuevoCodigo);
        return true;
    }
}
