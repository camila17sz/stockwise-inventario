# StockWise — Sistema de Gestión de Inventario

Proyecto académico — Spring Boot + Thymeleaf + Supabase (PostgreSQL)

## Requisitos previos
- Java 17 o superior
- Maven 3.6+
- Conexión a internet (para Supabase)

## Ejecutar el proyecto

### Opción 1: Maven wrapper (recomendado)
```bash
./mvnw spring-boot:run
```

### Opción 2: Maven instalado
```bash
mvn spring-boot:run
```

### Opción 3: Desde VS Code
1. Abre la carpeta `inventario-app` en VS Code
2. Instala la extensión "Spring Boot Extension Pack"
3. Haz clic en el botón ▶ Run que aparece sobre `InventarioApplication.java`
4. O usa el menú: Run > Start Debugging

## Acceso
- URL: http://localhost:8080
- Usuario: **admin**
- Contraseña: **admin123**

## Módulos del sistema
| Módulo | URL | Descripción |
|--------|-----|-------------|
| Login | /login | Autenticación |
| Dashboard | /dashboard | Resumen y alertas |
| Productos | /productos | CRUD de productos |
| Categorías | /categorias | CRUD de categorías |
| Movimientos | /movimientos | Entradas/salidas de stock |
| Proveedores | /proveedores | CRUD de proveedores |

## Base de datos
- **Proveedor**: Supabase (PostgreSQL en la nube)
- Las tablas se crean automáticamente al iniciar (JPA DDL auto=update)
- Se cargan datos de prueba automáticamente al primer inicio
