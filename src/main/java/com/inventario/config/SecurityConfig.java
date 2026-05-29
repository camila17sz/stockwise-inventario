package com.inventario.config;

import com.inventario.service.UsuarioDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/login", "/registro", "/registro/verificar", "/registro/reenviar").permitAll()

                // ── MANEJADOR: acceso completo a movimientos ──────────────────
                .requestMatchers("/movimientos", "/movimientos/**")
                    .hasAnyRole("ADMIN", "MANEJADOR")

                // ── Solo ADMIN puede gestionar productos, categorías, proveedores ──
                .requestMatchers(
                    "/productos/nuevo", "/productos/guardar",
                    "/productos/editar/**", "/productos/eliminar/**")
                    .hasRole("ADMIN")
                .requestMatchers(
                    "/proveedores/nuevo", "/proveedores/guardar",
                    "/proveedores/editar/**", "/proveedores/eliminar/**")
                    .hasRole("ADMIN")
                .requestMatchers(
                    "/categorias/nuevo", "/categorias/guardar",
                    "/categorias/editar/**", "/categorias/eliminar/**")
                    .hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
            .userDetailsService(usuarioDetailsService)
            .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }
}
