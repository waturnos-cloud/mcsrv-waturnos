WATurnos Postman Collection
===========================

Cómo importar
-------------
1) Abrí Postman → Import → arrastrá este ZIP, o importá los JSON individuales.
2) Seleccioná el environment **WATurnos - Local**.
3) Ejecutá **Auth → POST /auth/login** para obtener un JWT. El test guarda el token en `jwtToken` automáticamente.
4) Probá el resto de endpoints. Todos usan `Authorization: Bearer {jwtToken}`.

Variables del environment
-------------------------
- baseUrl = http://localhost:8080
- jwtToken = (se completa con el login)
- organizationId, providerId, serviceId, clientId, bookingId, etc. = IDs de prueba.

Notas
-----
- Las rutas están prefijadas con `/api/v1`.
- Los bodies de ejemplo tienen datos realistas (barbería/consultorio).
- Ajustá IDs según tus inserts iniciales.
