<<<<<<< HEAD
# Invento Acción - Proyecto mínimo

## Requisitos
- Java 17
- Maven
- Visual Studio Code (opcional: extensión Java)

## Ejecutar local
1. Abrir la carpeta `invento-accion` en VS Code.
2. En terminal ejecutar:
   mvn clean package
   mvn spring-boot:run
3. Acceder:
   - API: http://localhost:8080/api/v1/activos
   - H2 Console: http://localhost:8080/h2-console
     - JDBC URL: jdbc:h2:mem:invento
     - Usuario: sa
     - Contraseña: (vacía)
4. Documentación OpenAPI:
   - Swagger UI: http://localhost:8080/swagger-ui.html

## Endpoints principales
- GET /api/v1/activos
- GET /api/v1/activos/{id}
- POST /api/v1/activos
- GET /api/v1/activos/search?q=texto
- POST /api/v1/movimientos
- GET /api/v1/movimientos/activo/{activoId}

## Notas
- Proyecto intencionalmente simple para servir de esqueleto.
- Para producción: migrar a PostgreSQL, añadir seguridad (Spring Security + JWT), validaciones y pruebas.
=======
# Invento-Accion
Proyecto Escolar
>>>>>>> 08f943de6a719d474c3693973886aa260dd2fb16
