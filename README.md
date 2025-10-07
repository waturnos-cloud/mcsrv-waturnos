# WATurnos API (MVP)
**Stack:** Spring Boot 3, JPA, PostgreSQL, JWT, Swagger, MapStruct, Lombok

## Setup rápido
1. Java 17, Maven 3.9+
2. Crear DB `waturnos` en Postgres y ejecutar `db/ddl/waturnos.sql` (colocá tu DDL completo).
3. Editar `src/main/resources/application.yml` (DB y JWT).
4. `mvn spring-boot:run`
5. Swagger: `http://localhost:8080/swagger-ui.html`
6. Login: `POST /api/auth/login` con `{"email":"admin@demo.com","password":"admin"}` → copia el `token`.

## Paquetes
- `entity/`, `repository/`, `service/`, `service/impl/`
- `dto/`, `mapper/` (MapStruct)
- `security/` (JWT)
- `controller/` (REST con DTOs)
- `config/` (OpenAPI + DataLoader)

Generado: 2025-10-24T12:06:44.655408
