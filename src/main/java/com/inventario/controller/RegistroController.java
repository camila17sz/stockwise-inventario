package com.inventario.controller;

import com.inventario.service.UsuarioService;
import com.inventario.service.UsuarioService.ResultadoRegistro;
import com.inventario.service.UsuarioService.ResultadoVerificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    // ── Paso 1: mostrar formulario de registro ─────────────────────────────
    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("rol", "VISOR");
        return "auth/registro";
    }

    // ── Paso 1: procesar formulario → enviar código ─────────────────────────
    @PostMapping
    public String procesarRegistro(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String nombre,
            @RequestParam String password,
            @RequestParam String confirmarPassword,
            @RequestParam(defaultValue = "VISOR") String rol,
            @RequestParam(defaultValue = "") String codigoAcceso,
            Model model) {

        ResultadoRegistro resultado = usuarioService.iniciarRegistro(
                username, email, nombre, password, confirmarPassword, rol, codigoAcceso);

        if (resultado == ResultadoRegistro.OK) {
            // Redirigir a pantalla de verificación con el email en la URL
            return "redirect:/registro/verificar?email=" + encode(email);
        }

        // Error: repoblar form
        model.addAttribute("rol",      rol);
        model.addAttribute("username", username);
        model.addAttribute("email",    email);
        model.addAttribute("nombre",   nombre);

        switch (resultado) {
            case USERNAME_DUPLICADO     -> model.addAttribute("errorMsg", "Ese nombre de usuario ya está en uso.");
            case EMAIL_DUPLICADO        -> model.addAttribute("errorMsg", "Ese correo electrónico ya está registrado.");
            case PASSWORDS_NO_COINCIDEN -> model.addAttribute("errorMsg", "Las contraseñas no coinciden.");
            case CODIGO_INVALIDO        -> model.addAttribute("errorMsg", "Código de acceso incorrecto para ese rol.");
            default                     -> model.addAttribute("errorMsg", "Error inesperado. Intenta de nuevo.");
        }

        return "auth/registro";
    }

    // ── Paso 2: mostrar pantalla de verificación ────────────────────────────
    @GetMapping("/verificar")
    public String mostrarVerificacion(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "auth/verificar";
    }

    // ── Paso 2: procesar código ──────────────────────────────────────────────
    @PostMapping("/verificar")
    public String procesarVerificacion(
            @RequestParam String email,
            @RequestParam String codigo,
            Model model) {

        ResultadoVerificacion resultado = usuarioService.verificarCodigo(email, codigo);

        if (resultado == ResultadoVerificacion.OK) {
            return "redirect:/login?registro=ok";
        }

        model.addAttribute("email", email);

        switch (resultado) {
            case CODIGO_INCORRECTO -> model.addAttribute("errorMsg", "Código incorrecto. Verifica e intenta de nuevo.");
            case CODIGO_EXPIRADO   -> model.addAttribute("errorMsg", "El código ha expirado. Solicita uno nuevo.");
            case NO_ENCONTRADO     -> model.addAttribute("errorMsg", "No hay un registro pendiente para este correo.");
            default                -> model.addAttribute("errorMsg", "Error inesperado. Intenta de nuevo.");
        }

        return "auth/verificar";
    }

    // ── Reenviar código ──────────────────────────────────────────────────────
    @PostMapping("/reenviar")
    public String reenviarCodigo(@RequestParam String email) {
        usuarioService.reenviarCodigo(email);
        return "redirect:/registro/verificar?email=" + encode(email) + "&reenviado=true";
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
