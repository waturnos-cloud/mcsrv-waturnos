# Deployment en Render - Backend WATurnos

Este documento describe cómo desplegar el backend de WATurnos en Render.com para el ambiente **demo**.

## Requisitos Previos

- Cuenta en [Render.com](https://render.com)
- Repositorio GitHub: `waturnos-cloud/mcsrv-waturnos`
- Acceso a la base de datos Neon PostgreSQL
- Credenciales de servicios externos (Gmail, MercadoPago, Google OAuth)

## Opción 1: Deployment con Blueprint (Recomendado)

### Paso 1: Conectar Repositorio

1. Ve a [Render Dashboard](https://dashboard.render.com)
2. Click en "New +" → "Blueprint"
3. Conecta el repositorio: `waturnos-cloud/mcsrv-waturnos`
4. Selecciona la rama: `demo`
5. Render detectará automáticamente el `render.yaml`

### Paso 2: Configurar Variables de Entorno

Render te pedirá configurar las variables marcadas con `sync: false`:

#### Base de Datos (Neon)
```
DB_URL=jdbc:postgresql://ep-blue-mountain-ad5nhyod-pooler.c-2.us-east-1.aws.neon.tech:5432/neondb?sslmode=require&currentSchema=waturnos
DB_USERNAME=neondb_owner
DB_PASSWORD=npg_cpViMu5TF2Jx
```

#### JWT
```
JWT_SECRET=<generar-secreto-seguro-produccion>
```

#### Email (Gmail)
```
MAIL_USERNAME=waturnos@gmail.com
MAIL_PASSWORD=<app-password-gmail>
```

#### Google OAuth
```
GOOGLE_CLIENT_ID=382879720068-v3mrbu4qr82fkorrc3al21ec751plh7f.apps.googleusercontent.com
```

#### MercadoPago
```
MERCADOPAGO_ACCESS_TOKEN=APP_USR-7523345870016221-120914-7941204731c1ccccf616d5c5f75e88f6-3050749380
MERCADOPAGO_PUBLIC_KEY=APP_USR-af026ae9-ba89-4947-b00f-c8dfc555fbfc
MERCADOPAGO_APP_ID=3829891358541942
MERCADOPAGO_APP_SECRET=NTfiTHVI1cTOTVWpCihrG152Eios2Bvd
```

### Paso 3: Deploy

1. Click en "Apply"
2. Render iniciará el build y deployment automáticamente
3. El servicio estará disponible en: `https://mcsrv-waturnos.onrender.com`

## Opción 2: Deployment Manual

### Paso 1: Crear Web Service

1. Ve a [Render Dashboard](https://dashboard.render.com)
2. Click en "New +" → "Web Service"
3. Conecta el repositorio: `waturnos-cloud/mcsrv-waturnos`

### Paso 2: Configurar Servicio

```
Name: mcsrv-waturnos
Region: Oregon (US West)
Branch: demo
Runtime: Java
Build Command: mvn clean install -DskipTests
Start Command: java -jar target/waturnos-api-1.0.0.jar
Instance Type: Free
```

### Paso 3: Variables de Entorno

Agregar todas las variables listadas en "Opción 1 - Paso 2"

Agregar además:
```
SPRING_PROFILES_ACTIVE=demo
PORT=8085
MERCADOPAGO_SANDBOX_MODE=true
ENABLE_CRON_ONSTART=false
```

### Paso 4: Deploy

Click en "Create Web Service" y esperar a que termine el build.

## Verificación del Deployment

### 1. Health Check

Una vez deployado, verifica que el servicio esté corriendo:

```bash
curl https://mcsrv-waturnos.onrender.com/msvc-waturnos/v1.0/actuator/health
```

Respuesta esperada:
```json
{"status":"UP"}
```

### 2. Verificar Perfil Activo

En los logs de Render deberías ver:
```
The following 1 profile is active: "demo"
```

### 3. Verificar CORS

En los logs deberías ver:
```
✅ Total CORS allowed origins: X
```

### 4. Swagger UI

Accede a la documentación de la API:
```
https://mcsrv-waturnos.onrender.com/msvc-waturnos/v1.0/swagger-ui.html
```

## Configuración de Dominios Personalizados (Opcional)

### Backend
1. En Render Dashboard → Tu servicio → "Settings"
2. En "Custom Domain" → "Add Custom Domain"
3. Agregar: `api-demo.waturnos.com`
4. Configurar DNS según instrucciones de Render

Si usas dominio personalizado, actualizar:
- `application-demo.yml` → `app.baseUrl`
- Frontend `.env.demo` → `VITE_API_BASE`

## Troubleshooting

### Error: Puerto en uso
- Render asigna automáticamente el puerto via variable `PORT`
- Verificar que `application.yml` tenga: `server.port: ${PORT:8085}`

### Error: Base de datos no conecta
- Verificar credenciales de Neon
- Verificar que la IP de Render esté permitida en Neon (normalmente Neon acepta todas las IPs)
- Revisar logs: "Connection refused" o "Authentication failed"

### Error: CORS
- Verificar que `SPRING_PROFILES_ACTIVE=demo` esté configurado
- Verificar que los dominios en `application-demo.yml` coincidan con los reales
- Agregar `https://*.onrender.com` si es necesario

### Build falla
- Verificar que Java 17 esté configurado en `pom.xml`
- Verificar que Maven tenga acceso a repositorios centrales
- Revisar logs de build en Render

## Auto-Deploy

Render puede configurarse para auto-deploy en cada push a la rama `demo`:

1. En Settings → "Build & Deploy"
2. Habilitar "Auto-Deploy" → "Yes"

Cada commit a `demo` disparará un nuevo deployment automáticamente.

## Monitoreo

### Logs en Tiempo Real
```bash
# Ver logs en Render Dashboard
Dashboard → Tu Servicio → Logs
```

### Métricas
- CPU, Memoria, Request count disponibles en Render Dashboard
- Considerar integrar con servicios externos (Datadog, New Relic) para producción

## Ambientes

| Ambiente | Rama | URL | Perfil Spring |
|----------|------|-----|---------------|
| Dev | develop | local | dev |
| Demo | demo | mcsrv-waturnos.onrender.com | demo |
| Release | release | (pendiente) | release |
| Production | main | (pendiente) | prod |

## Siguiente Pasos

1. ✅ Configurar deployment de demo
2. ⏳ Configurar GitHub Actions para CI/CD
3. ⏳ Implementar Cloudinary para almacenamiento de imágenes
4. ⏳ Configurar ambiente de release
5. ⏳ Configurar ambiente de producción

## Contacto

Para problemas o preguntas sobre el deployment, contactar al equipo de DevOps.
