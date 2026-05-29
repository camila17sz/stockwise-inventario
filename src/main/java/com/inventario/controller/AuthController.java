package com.inventario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(
        @RequestParam(value = "error",    required = false) String error,
        @RequestParam(value = "logout",   required = false) String logout,
        @RequestParam(value = "expired",  required = false) String expired,
        @RequestParam(value = "registro", required = false) String registro,
        Model model) {

        if (error    != null) model.addAttribute("errorMsg",   "Usuario o contraseña incorrectos. Por favor intenta de nuevo.");
        if (logout   != null) model.addAttribute("logoutMsg",  "Sesión cerrada exitosamente.");
        if (expired  != null) model.addAttribute("errorMsg",   "Tu sesión ha expirado. Por favor inicia sesión nuevamente.");
        if (registro != null) model.addAttribute("successMsg", "¡Cuenta creada exitosamente! Ya puedes iniciar sesión.");

        return "auth/login";
    }
}
