# Sistema de Auditoría - WATurnos

## Descripción General

Sistema completo de auditoría con soporte para contexto threadlocal, mensajes internacionalizados y control de acceso por roles.

## Características

### 1. Columnas de la Tabla Audit

```sql
- id: identificador único
- event_date: fecha/hora del evento
- event: código del evento (ej: CLIENT_CREATE)
- username: nombre del usuario que ejecutó la acción
- email: email del usuario
- organization_id: ID de la organización
- organization_name: nombre de la organización
- role: rol del usuario (ADMIN, MANAGER, PROVIDER)
- success: boolean indicando si la operación fue exitosa
- error_message: mensaje de error (si falló)
- method_signature: firma del método auditado
- duration_ms: duración de la ejecución en milisegundos
- ip_address: IP del cliente
- user_agent: user agent del navegador
- request_id: ID de correlación del request
- service_id: ID del servicio relacionado (opcional)
- service_name: nombre del servicio relacionado (opcional)
```

### 2. Uso de la Anotación @AuditAspect

**Formato Simplificado:**
```java
@AuditAspect("EVENT_CODE")
public void myMethod() {
    // ...
}
```

El código del evento se usa para buscar la descripción en `messages.properties` con la clave `audit.event.EVENT_CODE`.

### 3. Contexto de Auditoría (ThreadLocal)

Para popular información adicional como `serviceId`, `serviceName`, `organizationId` y `organizationName`, usa `AuditContext`:

```java
// En tu service method
public void updateService(Long serviceId) {
    ServiceEntity service = serviceRepository.findById(serviceId).orElseThrow();
    
    // Establecer contexto de auditoría completo
    AuditContext.setService(serviceId, service.getName());
    AuditContext.setOrganization(service.getOrganizationId(), service.getOrganizationName());
    
    // ... lógica del método
}

// O individualmente
public void someMethod() {
    AuditContext.setServiceId(123L);
    AuditContext.setServiceName("Consulta General");
    AuditContext.setOrganizationId(5L);
    AuditContext.setOrganizationName("Clínica Central");
}
```

**Métodos disponibles en AuditContext:**
- `AuditContext.setServiceId(Long serviceId)`
- `AuditContext.setServiceName(String serviceName)`
- `AuditContext.setService(Long serviceId, String serviceName)`
- `AuditContext.setOrganizationId(Long organizationId)`
- `AuditContext.setOrganizationName(String organizationName)`
- `AuditContext.setOrganization(Long organizationId, String organizationName)`
- `AuditContext.clear()` (se llama automáticamente al finalizar el aspecto)

**Comportamiento de Fallback:**
- Si `organizationId`/`organizationName` NO se establecen en el contexto, el aspecto los obtendrá automáticamente del `SessionUtil` (usuario actual)
- Esto permite que los ADMIN (sin organización) no necesiten setear estos valores
- Los servicios que operan sobre entidades con organización DEBERÍAN popularlos para mayor precisión

### 4. Mensajes de Auditoría

Agregar en `messages.properties`:
```properties
audit.event.MY_EVENT_CODE=Descripción de la acción realizada
```

**Ejemplo:**
```properties
audit.event.SERVICE_CREATE=Creación de servicio
audit.event.SERVICE_UPDATE=Actualización de servicio
audit.event.SERVICE_DELETE=Eliminación de servicio
```

### 5. Control de Acceso por Roles

Los eventos disponibles para cada rol se configuran en `application.yml` bajo `app.audit.events`:

```yaml
app:
  audit:
    events:
      admin:
        - CLIENT_LIST_BY_ORG
        - CLIENT_CREATE
        - CLIENT_DELETE
        # ... todos los eventos
      manager:
        - CLIENT_LIST_BY_ORG
        - CLIENT_CREATE
        # ... eventos de gestión
      provider:
        - CLIENT_CREATE
        - CLIENT_UPDATE
        # ... eventos limitados
```

#### ADMIN
- Puede ver **todas** las auditorías de cualquier organización
- Eventos disponibles: configurables en `app.audit.events.admin`

#### MANAGER
- Puede ver auditorías solo de **su organización**
- Eventos disponibles: configurables en `app.audit.events.manager`

#### PROVIDER
- Puede ver solo auditorías relacionadas con **sus propios servicios**
- Eventos disponibles: configurables en `app.audit.events.provider`

### 6. Endpoints de Consulta

#### GET /audit
Obtiene auditorías filtradas por rol del usuario actual.

**Parámetros:**
- `organizationId` (opcional, solo ADMIN)
- `fromDate` (opcional, formato ISO: 2025-11-26T00:00:00)
- `toDate` (opcional, formato ISO: 2025-12-31T23:59:59)
- `event` (opcional, código del evento)

**Ejemplo:**
```bash
GET /msvc-waturnos/v1.0/audit?fromDate=2025-11-01T00:00:00&toDate=2025-11-30T23:59:59&event=CLIENT_CREATE
```

#### GET /audit/events
Obtiene los códigos de eventos que el usuario actual puede consultar según su rol.

**Ejemplo:**
```bash
GET /msvc-waturnos/v1.0/audit/events
```

**Respuesta para MANAGER:**
```json
{
  "success": true,
  "message": "Available events",
  "data": [
    "CLIENT_LIST_BY_ORG",
    "CLIENT_CREATE",
    "CLIENT_DELETE",
    "ORG_UPDATE_BASIC",
    "USER_CREATE_PROVIDER"
  ]
}
```

## Ejemplo Completo de Implementación

### 1. Anotar el método y popular contexto

```java
@Service
public class ServiceServiceImpl {
    
    @AuditAspect("SERVICE_CREATE")
    public ServiceEntity create(ServiceEntity service) {
        // Popular contexto de auditoría con organización
        AuditContext.setOrganization(service.getOrganizationId(), service.getOrganization().getName());
        AuditContext.setService(null, service.getName());
        
        ServiceEntity saved = serviceRepository.save(service);
        
        // Actualizar con el ID después de guardar
        AuditContext.setServiceId(saved.getId());
        
        return saved;
    }
    
    @AuditAspect("SERVICE_UPDATE")
    public ServiceEntity update(ServiceEntity service) {
        // Establecer contexto completo antes de la operación
        AuditContext.setOrganization(service.getOrganizationId(), service.getOrganization().getName());
        AuditContext.setService(service.getId(), service.getName());
        
        return serviceRepository.save(service);
    }
}

// Ejemplo con un Admin creando una organización (sin org en sesión)
@Service 
public class OrganizationServiceImpl {
    
    @AuditAspect("ORG_CREATE")
    public Organization create(Organization org) {
        // Admin no tiene organización en sesión,
        // así que seteamos la org que se está creando
        Organization saved = organizationRepository.save(org);
        
        AuditContext.setOrganization(saved.getId(), saved.getName());
        
        return saved;
    }
}

// Ejemplo con Manager/Provider (org viene del SessionUtil automáticamente)
@Service
public class ClientServiceImpl {
    
    @AuditAspect("CLIENT_CREATE")
    public Client create(Client client) {
        // Como Manager/Provider ya tienen org en sesión,
        // no es necesario setear organizationId/organizationName
        // (el aspecto lo toma automáticamente del SessionUtil)
        // Pero PODEMOS setearlo para mayor precisión:
        AuditContext.setOrganization(client.getOrganizationId(), client.getOrganization().getName());
        
        return clientRepository.save(client);
    }
}
```

### 2. Agregar mensaje en messages.properties

```properties
audit.event.SERVICE_CREATE=Creación de servicio
audit.event.SERVICE_UPDATE=Actualización de servicio
```

### 3. Consultar auditorías desde el front

```javascript
// Como ADMIN - ver auditorías de org 5
fetch('/msvc-waturnos/v1.0/audit?organizationId=5&fromDate=2025-11-01T00:00:00')

// Como MANAGER - ver auditorías de mi org
fetch('/msvc-waturnos/v1.0/audit?fromDate=2025-11-01T00:00:00')

// Como PROVIDER - ver mis auditorías
fetch('/msvc-waturnos/v1.0/audit')

// Obtener eventos disponibles para filtros
fetch('/msvc-waturnos/v1.0/audit/events')
```

## Notas Importantes

1. **ThreadLocal**: El contexto se limpia automáticamente después de cada request para evitar memory leaks.

2. **Async**: Las auditorías se guardan de forma asíncrona (`@Async`) para no bloquear la ejecución del método principal.

3. **Índices**: La tabla tiene índices en:
   - `event_date` (para búsquedas por fecha)
   - `organization_id` (para filtros por organización)
   - `event` (para filtros por tipo de evento)
   - `service_id` (para búsquedas de auditorías por servicio)

4. **Eventos Personalizados**: Si no se especifica el event code en la anotación, se genera automáticamente desde el nombre de la clase y método.

5. **Configuración de Eventos por Rol**: Los eventos disponibles para cada rol se definen en `application.yml` bajo `app.audit.events`. Esto permite agregar/quitar eventos sin modificar código Java.

## Configuración en application.yml

```yaml
app:
  audit:
    events:
      # Eventos que ADMIN puede ver (todos)
      admin:
        - CLIENT_LIST_BY_ORG
        - CLIENT_CREATE
        - CLIENT_DELETE
        - CLIENT_FIND_BY_ID
        - CLIENT_FIND_BY_FIELDS
        - CLIENT_ASSIGN_ORG
        - CLIENT_UNASSIGN_ORG
        - CLIENT_NOTIFY
        - CLIENT_UPDATE
        - CLIENT_UPCOMING_BOOKINGS
        - ORG_CREATE
        - ORG_UPDATE_BASIC
        - ORG_UPDATE_LOCATIONS
        - ORG_STATUS_CHANGE
        - USER_CREATE_MANAGER
        - USER_UPDATE_MANAGER
        - USER_CREATE_PROVIDER
        - USER_UPDATE_PROVIDER
      
      # Eventos que MANAGER puede ver (su organización)
      manager:
        - CLIENT_LIST_BY_ORG
        - CLIENT_CREATE
        - CLIENT_DELETE
        - CLIENT_FIND_BY_ID
        - CLIENT_FIND_BY_FIELDS
        - CLIENT_ASSIGN_ORG
        - CLIENT_UNASSIGN_ORG
        - CLIENT_NOTIFY
        - CLIENT_UPDATE
        - CLIENT_UPCOMING_BOOKINGS
        - ORG_UPDATE_BASIC
        - ORG_UPDATE_LOCATIONS
        - USER_CREATE_PROVIDER
        - USER_UPDATE_PROVIDER
      
      # Eventos que PROVIDER puede ver (solo sus servicios)
      provider:
        - CLIENT_CREATE
        - CLIENT_UPDATE
        - CLIENT_NOTIFY
        - CLIENT_UPCOMING_BOOKINGS
```

## Migración de Base de Datos

Si ya tienes datos en la tabla audit antigua, ejecuta:

```sql
-- Renombrar columnas existentes
ALTER TABLE audit RENAME COLUMN event_code TO event;
ALTER TABLE audit DROP COLUMN behavior;

-- Agregar nuevas columnas
ALTER TABLE audit ADD COLUMN service_id BIGINT;
ALTER TABLE audit ADD COLUMN service_name VARCHAR(255);

-- Crear nuevo índice
CREATE INDEX IF NOT EXISTS idx_audit_service ON audit(service_id);
```
