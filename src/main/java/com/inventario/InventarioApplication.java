package com.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class InventarioApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventarioApplication.class, args);
    }
}