# Validación de Cambios en Availability - Documentación

## 📋 Resumen

Cuando un manager/provider edita los horarios de disponibilidad de un servicio, puede afectar a clientes que ya tienen turnos reservados. Este endpoint permite validar el impacto **antes** de confirmar los cambios.

---

## 🔍 Endpoint de Validación

### Validar impacto de cambios en availability

**Endpoint:** `POST /services/availability/validate`

**Descripción:** 
Valida si los cambios propuestos en `listAvailability` afectarían a bookings existentes. Retorna:
- Cantidad de turnos afectados
- Lista detallada de clientes afectados con su información de contacto

**Request:**
```javascript
POST /services/availability/validate
Content-Type: application/json
Authorization: Bearer eyJhbGc...

{
  "serviceId": 5,
  "newAvailability": [
    {
      "dayOfWeek": 1,      // 1=Lunes, 2=Martes, ..., 7=Domingo
      "startTime": "09:00",
      "endTime": "18:00"
    },
    {
      "dayOfWeek": 2,
      "startTime": "10:00",
      "endTime": "16:00"
    }
  ]
}
```

**Response (Sin impacto):**
```javascript
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "Validation completed",
  "data": {
    "affectedCount": 0,
    "affectedBookings": []
  }
}
```

**Response (Con impacto):**
```javascript
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "Validation completed",
  "data": {
    "affectedCount": 3,
    "affectedBookings": [
      {
        "bookingId": 456,
        "clientFullName": "Juan Pérez",
        "clientPhone": "+54 11 1234-5678",
        "clientEmail": "juan@example.com",
        "startTime": "2025-12-15T19:00:00"
      },
      {
        "bookingId": 457,
        "clientFullName": "María García",
        "clientPhone": "+54 11 8765-4321",
        "clientEmail": "maria@example.com",
        "startTime": "2025-12-16T08:00:00"
      },
      {
        "bookingId": 458,
        "clientFullName": "Carlos López",
        "clientPhone": "+54 11 5555-6666",
        "clientEmail": "carlos@example.com",
        "startTime": "2025-12-17T19:30:00"
      }
    ]
  }
}
```

---

## 🎯 Flujo Recomendado en Frontend

### Paso 1: Usuario edita availability

```javascript
// Usuario modifica los horarios en el formulario
function onAvailabilityChange(newAvailability) {
  // Guardar temporalmente en estado local
  setModifiedAvailability(newAvailability);
}
```

### Paso 2: Validar antes de guardar

```javascript
async function validateBeforeSave(serviceId, newAvailability) {
  try {
    const response = await fetch(
      'https://www.waturnos.com/msvc-waturnos/v1.0/services/availability/validate',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          serviceId: serviceId,
          newAvailability: newAvailability
        })
      }
    );
    
    const result = await response.json();
    
    if (result.data.affectedCount > 0) {
      // Hay turnos afectados, mostrar advertencia
      return result.data;
    } else {
      // Sin impacto, puede guardar directamente
      return null;
    }
    
  } catch (error) {
    console.error('Error validating availability change:', error);
    throw error;
  }
}
```

### Paso 3: Mostrar advertencia si hay impacto

```javascript
async function handleSaveClick() {
  setLoading(true);
  
  const impact = await validateBeforeSave(serviceId, modifiedAvailability);
  
  if (impact && impact.affectedCount > 0) {
    // Mostrar modal de confirmación
    setShowWarningModal(true);
    setAffectedBookings(impact.affectedBookings);
    setLoading(false);
  } else {
    // Sin impacto, guardar directamente
    await saveService();
  }
}
```

### Paso 4: Confirmación del usuario

```javascript
function WarningModal({ affectedBookings, onConfirm, onCancel }) {
  return (
    <div className="modal">
      <h2>⚠️ Atención: Turnos Afectados</h2>
      <p>
        Los cambios en los horarios afectarán a <strong>{affectedBookings.length}</strong> turno(s) ya reservado(s).
        Estos turnos serán cancelados automáticamente y los clientes serán notificados.
      </p>
      
      <div className="affected-list">
        <h3>Clientes afectados:</h3>
        {affectedBookings.map(booking => (
          <div key={booking.bookingId} className="affected-item">
            <p><strong>{booking.clientFullName}</strong></p>
            <p>📅 {new Date(booking.startTime).toLocaleString()}</p>
            <p>📞 {booking.clientPhone}</p>
            <p>✉️ {booking.clientEmail}</p>
          </div>
        ))}
      </div>
      
      <div className="modal-actions">
        <button onClick={onCancel} className="btn-secondary">
          Cancelar
        </button>
        <button onClick={onConfirm} className="btn-danger">
          Confirmar cambios y notificar clientes
        </button>
      </div>
    </div>
  );
}
```

### Paso 5: Guardar con confirmación

```javascript
async function confirmAndSave() {
  setShowWarningModal(false);
  setLoading(true);
  
  try {
    // Guardar el servicio con el nuevo availability
    await saveService();
    
    // El backend se encargará de:
    // 1. Actualizar el servicio
    // 2. Cancelar los bookings afectados
    // 3. Notificar a los clientes por email/SMS
    
    alert('✅ Servicio actualizado. Los clientes afectados serán notificados.');
    
  } catch (error) {
    console.error('Error saving service:', error);
    alert('❌ Error al guardar los cambios');
  } finally {
    setLoading(false);
  }
}
```

---

## 🔄 Integración con Update Service

El endpoint `/services` (PUT) ya está preparado para:

1. **Detectar cambios en availability** (comparando con el estado actual)
2. **Cancelar bookings afectados** automáticamente
3. **Notificar a los clientes** vía email/SMS sobre la cancelación

**No necesitas hacer nada adicional** en el update, solo asegurarte de que el usuario confirme los cambios si hay impacto.

---

## 📊 Casos de Uso

### Caso 1: Reducir horario de atención

**Antes:**
```javascript
[
  { dayOfWeek: 1, startTime: "08:00", endTime: "20:00" }
]
```

**Después:**
```javascript
[
  { dayOfWeek: 1, startTime: "09:00", endTime: "18:00" }
]
```

**Impacto:** Turnos antes de 09:00 o después de 18:00 serán cancelados.

---

### Caso 2: Eliminar un día de atención

**Antes:**
```javascript
[
  { dayOfWeek: 1, startTime: "09:00", endTime: "18:00" },
  { dayOfWeek: 2, startTime: "09:00", endTime: "18:00" }
]
```

**Después:**
```javascript
[
  { dayOfWeek: 1, startTime: "09:00", endTime: "18:00" }
  // Eliminamos el martes (dayOfWeek: 2)
]
```

**Impacto:** Todos los turnos del martes serán cancelados.

---

### Caso 3: Cambiar horario intermedio

**Antes:**
```javascript
[
  { dayOfWeek: 1, startTime: "09:00", endTime: "13:00" },
  { dayOfWeek: 1, startTime: "15:00", endTime: "19:00" }
]
```

**Después:**
```javascript
[
  { dayOfWeek: 1, startTime: "09:00", endTime: "13:00" },
  { dayOfWeek: 1, startTime: "16:00", endTime: "20:00" }  // Cambió de 15:00 a 16:00
]
```

**Impacto:** Turnos entre 15:00 y 16:00 serán cancelados.

---

## 🎨 Componente React Completo (Ejemplo)

```javascript
import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';

function EditServiceAvailability() {
  const { serviceId } = useParams();
  const [service, setService] = useState(null);
  const [availability, setAvailability] = useState([]);
  const [showWarning, setShowWarning] = useState(false);
  const [affectedBookings, setAffectedBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  
  useEffect(() => {
    loadService();
  }, [serviceId]);
  
  async function loadService() {
    const response = await fetch(
      `https://www.waturnos.com/msvc-waturnos/v1.0/services/${serviceId}`,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    );
    
    const data = await response.json();
    setService(data);
    setAvailability(data.listAvailability);
  }
  
  async function handleSave() {
    setLoading(true);
    
    // Validar impacto
    const validateResponse = await fetch(
      'https://www.waturnos.com/msvc-waturnos/v1.0/services/availability/validate',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          serviceId: serviceId,
          newAvailability: availability
        })
      }
    );
    
    const impact = await validateResponse.json();
    
    if (impact.data.affectedCount > 0) {
      // Mostrar advertencia
      setAffectedBookings(impact.data.affectedBookings);
      setShowWarning(true);
      setLoading(false);
    } else {
      // Guardar directamente
      await saveService();
    }
  }
  
  async function saveService() {
    const response = await fetch(
      'https://www.waturnos.com/msvc-waturnos/v1.0/services',
      {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          ...service,
          listAvailability: availability
        })
      }
    );
    
    if (response.ok) {
      alert('✅ Servicio actualizado exitosamente');
    } else {
      alert('❌ Error al actualizar el servicio');
    }
    
    setLoading(false);
  }
  
  return (
    <div>
      <h1>Editar Horarios de Disponibilidad</h1>
      
      {/* Editor de availability */}
      <AvailabilityEditor 
        value={availability}
        onChange={setAvailability}
      />
      
      <button onClick={handleSave} disabled={loading}>
        {loading ? 'Validando...' : 'Guardar Cambios'}
      </button>
      
      {/* Modal de advertencia */}
      {showWarning && (
        <WarningModal
          affectedBookings={affectedBookings}
          onConfirm={() => {
            setShowWarning(false);
            saveService();
          }}
          onCancel={() => setShowWarning(false)}
        />
      )}
    </div>
  );
}
```

---

## ✅ Resumen

1. **Endpoint de validación:** `POST /services/availability/validate`
2. **Úsalo antes de guardar** para advertir al usuario
3. **Muestra el impacto** con nombre, teléfono, email y hora del turno
4. **El update normal** se encarga de cancelar y notificar automáticamente
5. **No necesitas lógica adicional** en el update, solo la confirmación del usuario

---

## 📞 Soporte

Si necesitas ayuda con la implementación frontend, verifica:
- ✅ Token JWT válido en la petición
- ✅ `serviceId` es correcto
- ✅ `newAvailability` tiene el formato correcto (dayOfWeek, startTime, endTime)
- ✅ Los tiempos están en formato `HH:mm` (24 horas)
