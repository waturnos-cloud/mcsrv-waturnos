# MercadoPago API Integration - Documentación para Frontend

## 📋 Índice
1. [Configuración Inicial](#configuración-inicial)
2. [Vinculación OAuth de MercadoPago](#vinculación-oauth-de-mercadopago)
3. [Gestión de Payment Providers](#gestión-de-payment-providers)
4. [Creación de Preferencias de Pago](#creación-de-preferencias-de-pago)
5. [Flujo Completo de Pago](#flujo-completo-de-pago)
6. [Webhooks y Notificaciones](#webhooks-y-notificaciones)

---

## 🔧 Configuración Inicial

### Base URL
```
https://www.waturnos.com/msvc-waturnos/v1.0
```

### Headers requeridos en todas las llamadas
```javascript
{
  "Authorization": "Bearer YOUR_JWT_TOKEN",
  "Content-Type": "application/json"
}
```

---

## 🔐 Vinculación OAuth de MercadoPago

**ESTE ES EL FLUJO RECOMENDADO** para que cada manager/provider vincule su cuenta de MercadoPago sin tener que copiar y pegar tokens manualmente.

### Paso 1: Botón "Vincular MercadoPago"

Cuando el manager hace clic en el botón, redirige a la URL de autorización de MercadoPago:

```javascript
function vincularMercadoPago(userId) {
  const clientId = '3829891358541942';
  const redirectUri = 'https://www.waturnos.com/oauth/mercadopago/callback';
  const state = userId; // Usamos el userId para identificar quién autorizó
  
  const authUrl = `https://auth.mercadopago.com/authorization?client_id=${clientId}&response_type=code&platform_id=mp&state=${state}&redirect_uri=${encodeURIComponent(redirectUri)}`;
  
  window.location.href = authUrl;
}
```

### Paso 2: Callback de MercadoPago

MercadoPago redirige de vuelta a: `https://www.waturnos.com/oauth/mercadopago/callback?code=TG-xxx&state=123`

Crea una página en esa ruta que procese la respuesta:

```javascript
// En /oauth/mercadopago/callback
async function procesarCallbackMercadoPago() {
  const urlParams = new URLSearchParams(window.location.search);
  const code = urlParams.get('code');
  const userId = urlParams.get('state'); // Recuperamos el userId
  
  if (!code) {
    alert('Error: No se recibió código de autorización');
    window.location.href = '/configuracion';
    return;
  }
  
  try {
    // Llamar al backend para intercambiar el código por tokens
    const response = await fetch(
      `https://www.waturnos.com/msvc-waturnos/v1.0/users/${userId}/payment-providers/mercadopago/oauth`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          code: code,
          redirectUri: 'https://www.waturnos.com/oauth/mercadopago/callback'
        })
      }
    );
    
    if (!response.ok) {
      throw new Error('Error al vincular cuenta de MercadoPago');
    }
    
    const message = await response.text();
    alert('✅ Cuenta de MercadoPago vinculada exitosamente');
    window.location.href = '/configuracion'; // Redirigir a configuración
    
  } catch (error) {
    console.error('Error:', error);
    alert('❌ Error al vincular MercadoPago: ' + error.message);
    window.location.href = '/configuracion';
  }
}

// Ejecutar al cargar la página
procesarCallbackMercadoPago();
```

### Componente React completo (ejemplo)

```javascript
// ConfiguracionMercadoPago.jsx
import { useState, useEffect } from 'react';
import { useAuth } from './hooks/useAuth';

function ConfiguracionMercadoPago() {
  const { user, token } = useAuth();
  const [isConfigured, setIsConfigured] = useState(false);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    verificarConfiguracion();
  }, []);
  
  async function verificarConfiguracion() {
    try {
      const response = await fetch(
        `https://www.waturnos.com/msvc-waturnos/v1.0/users/${user.id}/payment-providers/MERCADO_PAGO`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );
      
      const data = await response.json();
      setIsConfigured(data.isConfigured);
    } catch (error) {
      console.error('Error al verificar configuración:', error);
    } finally {
      setLoading(false);
    }
  }
  
  function vincularMercadoPago() {
    const clientId = '3829891358541942';
    const redirectUri = 'https://www.waturnos.com/oauth/mercadopago/callback';
    const state = user.id;
    
    const authUrl = `https://auth.mercadopago.com/authorization?client_id=${clientId}&response_type=code&platform_id=mp&state=${state}&redirect_uri=${encodeURIComponent(redirectUri)}`;
    
    window.location.href = authUrl;
  }
  
  async function desvincularMercadoPago() {
    if (!confirm('¿Estás seguro de desvincular tu cuenta de MercadoPago?')) {
      return;
    }
    
    try {
      const response = await fetch(
        `https://www.waturnos.com/msvc-waturnos/v1.0/users/${user.id}/payment-providers/MERCADO_PAGO`,
        {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );
      
      if (!response.ok) {
        throw new Error('Error al desvincular');
      }
      
      alert('✅ Cuenta desvinculada exitosamente');
      setIsConfigured(false);
    } catch (error) {
      console.error('Error:', error);
      alert('❌ Error al desvincular MercadoPago');
    }
  }
  
  if (loading) {
    return <div>Cargando...</div>;
  }
  
  return (
    <div className="mercadopago-config">
      <h2>Configuración de MercadoPago</h2>
      
      {isConfigured ? (
        <div className="configurado">
          <p>✅ Tu cuenta de MercadoPago está vinculada</p>
          <p>Los clientes podrán pagar sus reservas directamente a tu cuenta.</p>
          <button onClick={desvincularMercadoPago} className="btn-danger">
            Desvincular MercadoPago
          </button>
        </div>
      ) : (
        <div className="no-configurado">
          <p>⚠️ No tienes MercadoPago configurado</p>
          <p>Vincula tu cuenta para empezar a recibir pagos.</p>
          <button onClick={vincularMercadoPago} className="btn-primary">
            Vincular MercadoPago
          </button>
        </div>
      )}
    </div>
  );
}

export default ConfiguracionMercadoPago;
```

### Página de Callback (OAuthCallback.jsx)

```javascript
import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from './hooks/useAuth';

function OAuthMercadoPagoCallback() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { token } = useAuth();
  
  useEffect(() => {
    procesarCallback();
  }, []);
  
  async function procesarCallback() {
    const code = searchParams.get('code');
    const userId = searchParams.get('state');
    
    if (!code) {
      alert('❌ Error: No se recibió código de autorización');
      navigate('/configuracion');
      return;
    }
    
    try {
      const response = await fetch(
        `https://www.waturnos.com/msvc-waturnos/v1.0/users/${userId}/payment-providers/mercadopago/oauth`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify({
            code: code,
            redirectUri: 'https://www.waturnos.com/oauth/mercadopago/callback'
          })
        }
      );
      
      if (!response.ok) {
        throw new Error('Error al vincular cuenta');
      }
      
      alert('✅ Cuenta de MercadoPago vinculada exitosamente');
      navigate('/configuracion');
      
    } catch (error) {
      console.error('Error:', error);
      alert('❌ Error al vincular MercadoPago: ' + error.message);
      navigate('/configuracion');
    }
  }
  
  return (
    <div className="oauth-callback">
      <h2>Procesando vinculación...</h2>
      <p>Por favor espera mientras vinculamos tu cuenta de MercadoPago.</p>
    </div>
  );
}

export default OAuthMercadoPagoCallback;
```

---

## 1️⃣ Gestión de Payment Providers

Estos endpoints permiten a los managers/providers vincular su cuenta de MercadoPago.

### 1.1. Vincular cuenta de MercadoPago (Manual)

**Endpoint:** `POST /users/{userId}/payment-providers`

**Descripción:** Permite al provider guardar manualmente su access_token y public_key de MercadoPago.

**Request:**
```javascript
POST /users/123/payment-providers
Content-Type: application/json
Authorization: Bearer eyJhbGc...

{
  "type": "MERCADO_PAGO",
  "accessToken": "APP_USR-0f169ccc-337b-4c98-be66-4aed88d10bf8",
  "publicKey": "APP_USR-7475203392916547-120818-1ec401e57d8461c088eca893c916e09a-3050749380",
  "accountId": "3050749380",
  "webhookUrl": "https://www.waturnos.com/msvc-waturnos/v1.0/webhooks/mercadopago",
  "sandboxMode": true
}
```

**Response:**
```javascript
HTTP/1.1 200 OK
Content-Type: text/plain

"Payment provider configured successfully"
```

**Ejemplo con Fetch:**
```javascript
async function vincularMercadoPago(userId, mercadoPagoData) {
  const response = await fetch(
    `https://www.waturnos.com/msvc-waturnos/v1.0/users/${userId}/payment-providers`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        type: 'MERCADO_PAGO',
        accessToken: mercadoPagoData.accessToken,
        publicKey: mercadoPagoData.publicKey,
        accountId: mercadoPagoData.accountId,
        webhookUrl: 'https://www.waturnos.com/msvc-waturnos/v1.0/webhooks/mercadopago',
        sandboxMode: true
      })
    }
  );
  
  if (!response.ok) {
    throw new Error('Error al vincular MercadoPago');
  }
  
  return await response.text();
}
```

---

### 1.2. Consultar configuración de MercadoPago

**Endpoint:** `GET /users/{userId}/payment-providers/{type}`

**Descripción:** Verifica si el provider tiene configurado MercadoPago.

**Request:**
```javascript
GET /users/123/payment-providers/MERCADO_PAGO
Authorization: Bearer eyJhbGc...
```

**Response (Configurado):**
```javascript
HTTP/1.1 200 OK
Content-Type: application/json

{
  "type": "MERCADO_PAGO",
  "publicKey": "APP_USR-7475203392916547-120818-1ec401e57d8461c088eca893c916e09a-3050749380",
  "accountId": "3050749380",
  "webhookUrl": "https://www.waturnos.com/msvc-waturnos/v1.0/webhooks/mercadopago",
  "sandboxMode": true,
  "isConfigured": true
}
```

**Response (No configurado):**
```javascript
HTTP/1.1 200 OK
Content-Type: application/json

{
  "type": "MERCADO_PAGO",
  "isConfigured": false
}
```

**Ejemplo con Fetch:**
```javascript
async function verificarMercadoPago(userId) {
  const response = await fetch(
    `https://www.waturnos.com/msvc-waturnos/v1.0/users/${userId}/payment-providers/MERCADO_PAGO`,
    {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    }
  );
  
  const data = await response.json();
  return data.isConfigured;
}
```

---

### 1.3. Desvincular cuenta de MercadoPago

**Endpoint:** `DELETE /users/{userId}/payment-providers/{type}`

**Descripción:** Elimina la vinculación de MercadoPago del provider.

**Request:**
```javascript
DELETE /users/123/payment-providers/MERCADO_PAGO
Authorization: Bearer eyJhbGc...
```

**Response:**
```javascript
HTTP/1.1 200 OK
Content-Type: text/plain

"Payment provider removed successfully"
```

**Ejemplo con Fetch:**
```javascript
async function desvincularMercadoPago(userId) {
  const response = await fetch(
    `https://www.waturnos.com/msvc-waturnos/v1.0/users/${userId}/payment-providers/MERCADO_PAGO`,
    {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    }
  );
  
  if (!response.ok) {
    throw new Error('Error al desvincular MercadoPago');
  }
  
  return await response.text();
}
```

---

## 2️⃣ Creación de Preferencias de Pago

Este es el endpoint principal para iniciar el flujo de pago.

### 2.1. Crear preferencia de pago

**Endpoint:** `POST /payments/create-preference`

**Descripción:** 
Crea una preferencia de pago en MercadoPago para un booking específico. 
El sistema automáticamente:
1. Obtiene el provider asociado al servicio del booking
2. Lee el access_token de ese provider
3. Crea la preferencia en MercadoPago
4. Devuelve la URL de checkout para redirigir al cliente

**Request:**
```javascript
POST /payments/create-preference
Content-Type: application/json
Authorization: Bearer eyJhbGc...

{
  "bookingId": 456,
  "amount": 5000.50,
  "description": "Reserva de turno - Corte de pelo"
}
```

**Response:**
```javascript
HTTP/1.1 201 Created
Content-Type: application/json

{
  "preferenceId": "123456789-abc-def-ghi",
  "initPoint": "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=123456789-abc-def-ghi",
  "sandboxInitPoint": "https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=123456789-abc-def-ghi"
}
```

**Ejemplo con Fetch:**
```javascript
async function crearPreferenciaPago(bookingId, amount, description) {
  const response = await fetch(
    'https://www.waturnos.com/msvc-waturnos/v1.0/payments/create-preference',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        bookingId: bookingId,
        amount: amount,
        description: description
      })
    }
  );
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Error al crear preferencia de pago');
  }
  
  return await response.json();
}
```

**Ejemplo de uso completo:**
```javascript
// Cuando el cliente quiere pagar
async function iniciarPago(bookingId) {
  try {
    // 1. Crear preferencia
    const preferencia = await crearPreferenciaPago(
      bookingId,
      5000.50,
      'Reserva de turno - Corte de pelo'
    );
    
    // 2. Redirigir al cliente a MercadoPago
    // En producción usar initPoint, en sandbox usar sandboxInitPoint
    window.location.href = preferencia.sandboxInitPoint; // o preferencia.initPoint
    
  } catch (error) {
    console.error('Error al iniciar pago:', error);
    alert('No se pudo iniciar el pago. Por favor, intente nuevamente.');
  }
}
```

---

## 3️⃣ Flujo Completo de Pago

### Paso a paso desde el frontend

```javascript
// PASO 1: Verificar que el provider tiene MercadoPago configurado
async function verificarConfiguracionPago(providerId) {
  const config = await fetch(
    `https://www.waturnos.com/msvc-waturnos/v1.0/users/${providerId}/payment-providers/MERCADO_PAGO`,
    {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    }
  ).then(r => r.json());
  
  return config.isConfigured;
}

// PASO 2: Mostrar botón de pago si está configurado
async function mostrarBotonPago(bookingId, providerId) {
  const tieneMP = await verificarConfiguracionPago(providerId);
  
  if (!tieneMP) {
    document.getElementById('btn-pagar').style.display = 'none';
    document.getElementById('msg-sin-pago').style.display = 'block';
    return;
  }
  
  document.getElementById('btn-pagar').style.display = 'block';
  document.getElementById('btn-pagar').onclick = () => procesarPago(bookingId);
}

// PASO 3: Procesar el pago
async function procesarPago(bookingId) {
  try {
    // Mostrar loader
    mostrarLoader(true);
    
    // Crear preferencia
    const response = await fetch(
      'https://www.waturnos.com/msvc-waturnos/v1.0/payments/create-preference',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          bookingId: bookingId,
          amount: 5000.50,
          description: 'Reserva de turno'
        })
      }
    );
    
    if (!response.ok) {
      throw new Error('Error al crear la preferencia de pago');
    }
    
    const preferencia = await response.json();
    
    // Redirigir a MercadoPago
    // Usar sandboxInitPoint para testing, initPoint para producción
    window.location.href = preferencia.sandboxInitPoint;
    
  } catch (error) {
    console.error('Error:', error);
    alert('No se pudo procesar el pago');
    mostrarLoader(false);
  }
}
```

---

## 4️⃣ URLs de Retorno

Cuando el cliente completa (o cancela) el pago en MercadoPago, será redirigido a estas URLs:

### URLs configuradas en el backend:

```javascript
// Pago exitoso
https://www.waturnos.com/payment/success

// Pago rechazado/fallido
https://www.waturnos.com/payment/failure

// Pago pendiente
https://www.waturnos.com/payment/pending
```

### Query params que MercadoPago enviará:

```
?collection_id=12345678
&collection_status=approved
&payment_id=12345678
&status=approved
&external_reference=456
&payment_type=credit_card
&merchant_order_id=9876543
&preference_id=123456789-abc-def-ghi
&site_id=MLA
&processing_mode=aggregator
&merchant_account_id=null
```

### Ejemplo de página de éxito:

```javascript
// En /payment/success
function mostrarResultadoPago() {
  const urlParams = new URLSearchParams(window.location.search);
  const paymentId = urlParams.get('payment_id');
  const status = urlParams.get('status');
  const externalReference = urlParams.get('external_reference'); // Este es el bookingId
  
  if (status === 'approved') {
    document.getElementById('mensaje').innerText = 
      `¡Pago exitoso! Tu reserva #${externalReference} ha sido confirmada.`;
  } else if (status === 'pending') {
    document.getElementById('mensaje').innerText = 
      `Pago en proceso. Te notificaremos cuando se confirme.`;
  }
  
  // Opcional: Consultar el estado del booking en tu backend
  fetch(`https://www.waturnos.com/msvc-waturnos/v1.0/bookings/${externalReference}`)
    .then(r => r.json())
    .then(booking => {
      console.log('Estado del booking:', booking.status);
    });
}
```

---

## 5️⃣ Webhooks y Notificaciones

### ⚠️ Importante: Configuración en MercadoPago

El backend ya tiene el webhook configurado en:
```
https://www.waturnos.com/msvc-waturnos/v1.0/webhooks/mercadopago
```

**El manager/admin debe:**
1. Ir al panel de MercadoPago → Integraciones → Webhooks
2. Agregar la URL del webhook arriba
3. Seleccionar eventos: `payment` (pagos)

### Flujo de notificación:

```
1. Cliente paga en MercadoPago
2. MercadoPago envía notificación al webhook
3. Backend consulta el pago en MercadoPago API
4. Backend actualiza el estado del booking:
   - approved → CONFIRMED
   - pending → PENDING
   - rejected → CANCELLED
5. Frontend puede consultar el estado del booking
```

### No necesitas hacer nada en el frontend para webhooks

El webhook es manejado completamente por el backend. Solo debes:
- Consultar el estado del booking después de la redirección
- Mostrar mensajes apropiados según el estado

---

## 🧪 Testing en Sandbox

### Tarjetas de prueba de MercadoPago:

```javascript
// Pago aprobado
{
  cardNumber: '5031 7557 3453 0604',
  cvv: '123',
  expiryDate: '11/25',
  name: 'APRO'
}

// Pago rechazado
{
  cardNumber: '5031 7557 3453 0604',
  cvv: '123',
  expiryDate: '11/25',
  name: 'OTHE'
}

// Pago pendiente
{
  cardNumber: '5031 7557 3453 0604',
  cvv: '123',
  expiryDate: '11/25',
  name: 'CONT'
}
```

---

## 📦 Tipos de Datos

### PaymentProviderType
```typescript
enum PaymentProviderType {
  MERCADO_PAGO = 'MERCADO_PAGO'
}
```

### AddPaymentRequest
```typescript
interface AddPaymentRequest {
  type: 'MERCADO_PAGO';
  accessToken: string;      // Requerido
  publicKey: string;        // Requerido
  accountId?: string;       // Opcional
  webhookUrl?: string;      // Opcional
  sandboxMode?: boolean;    // Opcional (default: true)
}
```

### PaymentProviderResponse
```typescript
interface PaymentProviderResponse {
  type: 'MERCADO_PAGO';
  publicKey?: string;
  accountId?: string;
  webhookUrl?: string;
  sandboxMode?: boolean;
  isConfigured: boolean;
}
```

### CreatePreferenceRequest
```typescript
interface CreatePreferenceRequest {
  bookingId: number;        // Requerido
  amount: number;           // Requerido (decimal)
  description?: string;     // Opcional
}
```

### PaymentPreferenceResponse
```typescript
interface PaymentPreferenceResponse {
  preferenceId: string;     // ID de la preferencia en MercadoPago
  initPoint: string;        // URL de checkout (producción)
  sandboxInitPoint: string; // URL de checkout (sandbox)
}
```

---

## 🚨 Manejo de Errores

### Errores comunes:

```javascript
// Provider no tiene MercadoPago configurado
{
  status: 500,
  message: "El provider no tiene configurado MercadoPago"
}

// Booking no encontrado
{
  status: 400,
  message: "Booking no encontrado con ID: 456"
}

// Usuario no autorizado
{
  status: 403,
  message: "Access denied"
}

// Token inválido
{
  status: 401,
  message: "Unauthorized"
}
```

### Ejemplo de manejo:

```javascript
async function crearPreferenciaPagoConErrorHandling(bookingId, amount) {
  try {
    const response = await fetch(
      'https://www.waturnos.com/msvc-waturnos/v1.0/payments/create-preference',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({ bookingId, amount })
      }
    );
    
    if (!response.ok) {
      const error = await response.json();
      
      if (response.status === 500 && error.message.includes('no tiene configurado')) {
        throw new Error('El profesional aún no ha configurado su cuenta de pago');
      } else if (response.status === 400) {
        throw new Error('Reserva no encontrada');
      } else if (response.status === 401) {
        // Redirigir al login
        window.location.href = '/login';
        return;
      } else {
        throw new Error(error.message || 'Error desconocido');
      }
    }
    
    return await response.json();
    
  } catch (error) {
    console.error('Error al crear preferencia:', error);
    // Mostrar mensaje al usuario
    alert(error.message);
    throw error;
  }
}
```

---

## ✅ Checklist de Implementación Frontend

- [ ] Crear formulario para que el manager ingrese sus credenciales de MercadoPago
- [ ] Implementar llamada a `POST /users/{userId}/payment-providers`
- [ ] Mostrar indicador visual si el provider tiene MercadoPago configurado
- [ ] Agregar botón "Pagar" en la página de reserva
- [ ] Al hacer clic en "Pagar", llamar a `POST /payments/create-preference`
- [ ] Redirigir al `initPoint` o `sandboxInitPoint` recibido
- [ ] Crear páginas de retorno: `/payment/success`, `/payment/failure`, `/payment/pending`
- [ ] Extraer `external_reference` de los query params para mostrar el bookingId
- [ ] Actualizar la UI del booking según el estado
- [ ] Configurar el webhook en el panel de MercadoPago

---

## 📞 Soporte

Si tienes algún problema con la integración, verifica:
1. ✅ Token JWT válido en todas las peticiones
2. ✅ Provider tiene `access_token` y `public_key` configurados
3. ✅ Booking existe y pertenece a un servicio válido
4. ✅ URLs de retorno configuradas correctamente
5. ✅ Webhook configurado en el panel de MercadoPago
