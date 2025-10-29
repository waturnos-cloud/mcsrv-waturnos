-- =============================================
-- MODELO DE DATOS WATurnos - Generado 2025-10-20
-- Descripción: Modelo de base de datos para sistema de turnos multiorganización y multiprofesional.
-- Cada tabla incluye explicación de sus columnas y finalidad.
-- =============================================

-- Tabla: organization
-- Representa un negocio que utiliza WATurnos (ej: barbería, centro médico)
CREATE TABLE organization (
    id BIGSERIAL PRIMARY KEY, -- Identificador único de la organización
    name VARCHAR(255), -- Nombre de la organización
    logo_url TEXT, -- URL del logo para mostrar en la interfaz
    timezone VARCHAR(100), -- Zona horaria de la organización (ej: America/Argentina/Buenos_Aires)
    type VARCHAR(100), -- Tipo de organización (barbería, consultorio, etc.)
    default_language VARCHAR(10), -- Idioma predeterminado para comunicaciones
    active BOOLEAN DEFAULT TRUE, -- Indica si la organización está activa o suspendida
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha de alta
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha de alta
    creator VARCHAR(100), -- creador
    modificator VARCHAR(100), -- modificator
    status VARCHAR(50) NOT NULL     -- Estado de la organization: e.g. 'PENDING', 'ACTIVE', 'INACTIVE', 'DELETED'

);

-- Tabla: location
-- Representa una ubicación física o sucursal asociada a una organización.
CREATE TABLE location (
    id BIGSERIAL PRIMARY KEY,                            -- Identificador único de la ubicación
    organization_id BIGINT NOT NULL 
        REFERENCES organization(id) ON DELETE CASCADE,   -- Organización propietaria
    name VARCHAR(255) NOT NULL,                          -- Nombre de la sucursal (ej: "Centro", "Sucursal Norte")
    address VARCHAR(255),                                -- Dirección física completa
    phone VARCHAR(50),                                   -- Teléfono local
    email VARCHAR(255),                                  -- Email de contacto de la sede
    latitude DECIMAL(10,6),                              -- Coordenadas geográficas (opcional)
    longitude DECIMAL(10,6),
    active BOOLEAN DEFAULT TRUE,                         -- Si la sede está habilitada
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creator VARCHAR(100),
    modificator VARCHAR(100),
    main BOOLEAN DEFAULT FALSE -- Indica si la dirección es la principal
);


-- Tabla: organization_props
-- Clave-valor extendido para configuración adicional de cada organización
CREATE TABLE organization_props (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organization(id) ON DELETE CASCADE,
    key VARCHAR(255), -- 
    value TEXT -- Valor asociado
);

-- Tabla: user
-- Usuarios que administran la plataforma (admin, gestor, o proveedores con acceso)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255), -- Nombre completo
    email VARCHAR(255) UNIQUE, -- Email único para login
    phone VARCHAR(50), -- Teléfono (opcional)
    password_hash TEXT, -- Contraseña hasheada
    organization_id BIGINT REFERENCES organization(id), -- Organización a la que pertenece
    active BOOLEAN DEFAULT TRUE, -- Si está habilitado
    role VARCHAR(50), -- admin / manager / provider
    last_login_at TIMESTAMP, -- Último acceso
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha de alta
    creator VARCHAR(100), -- creador
    modificator VARCHAR(100) -- modificator
);

-- Tabla: users_props
-- Configuración extra por usuario (clave-valor)
CREATE TABLE users_props (
    id BIGSERIAL PRIMARY KEY,
    users_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    key VARCHAR(255),
    value TEXT
);

-- Tabla: provider
-- Profesionales que brindan los servicios (peluqueros, kinesiólogos, etc.)
CREATE TABLE provider (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    photo_url TEXT, -- Imagen pública del profesional
    bio TEXT, -- Breve descripción del profesional
    active BOOLEAN DEFAULT TRUE,
    organization_id BIGINT REFERENCES organization(id),
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha de alta
    creator VARCHAR(100), -- creador
    modificator VARCHAR(100) -- modificator
);

-- Tabla: service
-- Servicios que se ofrecen en la organización, cada uno asociado a un provider
CREATE TABLE service (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255), -- Nombre del servicio (ej: Corte, Consulta)
    description TEXT, -- Descripción del servicio
    duration_minutes INT, -- Duración total
    price DECIMAL(10,2), -- Precio final
    advance_payment INT, -- porcentaje de pago por adelantado, de 0 a 100
    future_days INT, -- cantidad de dias a futuro 
    provider_id BIGINT REFERENCES provider(id),
    location_id BIGINT REFERENCES location(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha de alta
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha de modificacion
    creator VARCHAR(100), -- creador
    modificator VARCHAR(100) -- modificator
);

-- Tabla: service_props
-- Extensión flexible para servicios (ej: color favorito, categorías)
CREATE TABLE service_props (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT REFERENCES service(id) ON DELETE CASCADE,
    key VARCHAR(255),
    value TEXT
);

-- Tabla: client
-- Personas que toman turnos (clientes)
CREATE TABLE client (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    password_hash TEXT, -- Si se permite login de cliente
    organization_id BIGINT REFERENCES organization(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha de modificacion
    creator VARCHAR(100), -- creador
    modificator VARCHAR(100) -- modificator
);

-- Tabla: client_props
-- Clave-valor para datos extendidos del cliente
CREATE TABLE client_props (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES client(id) ON DELETE CASCADE,
    key VARCHAR(255),
    value TEXT
);

-- Tabla: booking
-- Registro de turnos/reservas
CREATE TABLE booking (
    id BIGSERIAL PRIMARY KEY,                      -- Identificador único del turno
    start_time TIMESTAMPTZ NOT NULL,               -- Fecha y hora de inicio (con zona horaria)
    end_time TIMESTAMPTZ NOT NULL,                 -- Fecha y hora de fin (con zona horaria)
    status VARCHAR(50) NOT NULL,                   -- Estado del turno: e.g. 'reserved', 'confirmed', 'completed', 'cancelled'
    notes TEXT,                                    -- Notas internas del turno (anotaciones del profesional/admin)
    organization_id BIGINT REFERENCES organization(id) ON DELETE SET NULL, -- Organización dueña del turno
    client_id BIGINT REFERENCES client(id) ON DELETE SET NULL,             -- Cliente que reservó
    provider_id BIGINT REFERENCES provider(id) ON DELETE SET NULL,         -- Profesional que atiende
    service_id BIGINT REFERENCES service(id) ON DELETE SET NULL,           -- Servicio asociado
    recurrence_id BIGINT,                          -- Referencia opcional a la tabla recurrence (si aplica)
    cancel_reason TEXT,                            -- Motivo de cancelación (texto libre)
    created_at TIMESTAMPTZ DEFAULT now(),          -- Fecha y hora de creación del registro
    updated_at TIMESTAMPTZ DEFAULT now()           -- Fecha y hora de última modificación (actualizar con trigger/app)
);

-- Tabla: recurrence
-- Recurrencia de turnos
CREATE TABLE recurrence (
    id BIGSERIAL PRIMARY KEY,   -- Identificador único de la recurrencia
    service_id BIGINT,          -- Referencia al servicio
    client_id BIGINT,           -- Referencia al cliente
    pattern VARCHAR(50),        -- daily, weekly, monthly, etc.
    interval SMALLINT DEFAULT 1,-- cada cuántos días/semanas/etc se repite
    weekdays VARCHAR(20),       -- ej: "1,3,5" para lunes, miércoles y viernes (si aplica)
    start_date DATE,             -- desde cuándo se repite
    end_date DATE,              -- hasta cuándo se repite
    count INT,                  -- número total de repeticiones (opcional)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- Tabla: payment
-- Pagos asociados a turnos
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT, -- Referencia al turno
    amount DECIMAL(10,2), -- Monto pagado
    method VARCHAR(50), -- Método (efectivo, MP, etc.)
    status VARCHAR(50), -- Estado (pagado, pendiente)
    currency VARCHAR(10), -- Moneda (ej: ARS, USD)
    voucher VARCHAR(100), -- ID de referencia externa
    paid_at TIMESTAMP
);

-- Tabla: notification
-- Notificaciones automáticas (recordatorios, confirmaciones, etc.)
CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50), -- Tipo de evento (recordatorio, confirmación, etc.)
    target VARCHAR(255), -- Email o número destino
    message TEXT,
    status VARCHAR(50), -- enviado, pendiente, error
    channel VARCHAR(50), -- whatsapp, email, push, etc.
    related_booking_id BIGINT, -- Turno relacionado
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: availability
-- Disponibilidad semanal por servicio
CREATE TABLE availability (
    id BIGSERIAL PRIMARY KEY,
    day_of_week SMALLINT, -- 0=Lunes, ..., 6=Domingo
    start_time TIME,
    end_time TIME,
    service_id BIGINT REFERENCES service(id)
);

-- Tabla: unavailability
-- Ausencias excepcionales o cierres por feriado
CREATE TABLE unavailability (
    id BIGSERIAL PRIMARY KEY,
    start_day DATE,
    end_day DATE,
    start_time TIME,
    end_time TIME,
    day_of_week SMALLINT,
    service_id BIGINT REFERENCES service(id)
);


-- Tabla: provider_organization
-- Relación N a N entre proveedores y organizaciones (por ejemplo, un profesional trabaja en varias sucursales).
CREATE TABLE provider_organization (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL 
        REFERENCES provider(id) ON DELETE CASCADE,
    organization_id BIGINT NOT NULL 
        REFERENCES organization(id) ON DELETE CASCADE,
    start_date DATE DEFAULT CURRENT_DATE,                -- Desde cuándo trabaja en la organización
    end_date DATE,                                       -- Hasta cuándo (si aplica)
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creator VARCHAR(100),
    modificator VARCHAR(100),
    UNIQUE (provider_id, organization_id, location_id)   -- Evita duplicados de asignación
);


-- ============================================
-- RELACIONES PRINCIPALES
-- ============================================

-- user
ALTER TABLE users
    ADD CONSTRAINT fk_user_org
        FOREIGN KEY (organization_id)
        REFERENCES organization(id)
        ON DELETE CASCADE;

-- users_props
ALTER TABLE users_props
    ADD CONSTRAINT fk_usersprops_user
        FOREIGN KEY (users_id)
        REFERENCES users(id)
        ON DELETE CASCADE;

-- provider
ALTER TABLE provider
    ADD CONSTRAINT fk_provider_org
        FOREIGN KEY (organization_id)
        REFERENCES organization(id)
        ON DELETE CASCADE;

-- service
ALTER TABLE service
    ADD CONSTRAINT fk_service_provider
        FOREIGN KEY (provider_id)
        REFERENCES provider(id)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_service_location
        FOREIGN KEY (location_id)
        REFERENCES location(id)
        ON DELETE SET NULL;

-- service_props
ALTER TABLE service_props
    ADD CONSTRAINT fk_serviceprops_service
        FOREIGN KEY (service_id)
        REFERENCES service(id)
        ON DELETE CASCADE;

-- client
ALTER TABLE client
    ADD CONSTRAINT fk_client_org
        FOREIGN KEY (organization_id)
        REFERENCES organization(id)
        ON DELETE CASCADE;

-- client_props
ALTER TABLE client_props
    ADD CONSTRAINT fk_clientprops_client
        FOREIGN KEY (client_id)
        REFERENCES client(id)
        ON DELETE CASCADE;

-- booking
ALTER TABLE booking
    ADD CONSTRAINT fk_booking_org
        FOREIGN KEY (organization_id)
        REFERENCES organization(id)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_booking_client
        FOREIGN KEY (client_id)
        REFERENCES client(id)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_booking_provider
        FOREIGN KEY (provider_id)
        REFERENCES provider(id)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_booking_service
        FOREIGN KEY (service_id)
        REFERENCES service(id)
        ON DELETE SET NULL;

-- recurrence
ALTER TABLE recurrence
    ADD CONSTRAINT fk_recurrence_service
        FOREIGN KEY (service_id)
        REFERENCES service(id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_recurrence_client
        FOREIGN KEY (client_id)
        REFERENCES client(id)
        ON DELETE SET NULL;

-- payment
ALTER TABLE payment
    ADD CONSTRAINT fk_payment_booking
        FOREIGN KEY (booking_id)
        REFERENCES booking(id)
        ON DELETE SET NULL;

-- notification
ALTER TABLE notification
    ADD CONSTRAINT fk_notification_booking
        FOREIGN KEY (related_booking_id)
        REFERENCES booking(id)
        ON DELETE SET NULL;

-- availability / unavailability
ALTER TABLE availability
    ADD CONSTRAINT fk_availability_service
        FOREIGN KEY (service_id)
        REFERENCES service(id)
        ON DELETE CASCADE;

ALTER TABLE unavailability
    ADD CONSTRAINT fk_unavailability_service
        FOREIGN KEY (service_id)
        REFERENCES service(id)
        ON DELETE CASCADE;

-- location
ALTER TABLE location
    ADD CONSTRAINT fk_location_org
        FOREIGN KEY (organization_id)
        REFERENCES organization(id)
        ON DELETE CASCADE;

-- provider_organization
ALTER TABLE provider_organization
    ADD CONSTRAINT fk_provider_org_provider
        FOREIGN KEY (provider_id)
        REFERENCES provider(id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_provider_org_org
        FOREIGN KEY (organization_id)
        REFERENCES organization(id)
        ON DELETE CASCADE,
    ADD CONSTRAINT fk_provider_org_location
        FOREIGN KEY (location_id)
        REFERENCES location(id)
        ON DELETE SET NULL;

-- ============================================
-- CONSTRAINTS ADICIONALES DE INTEGRIDAD
-- ============================================

-- Evitar estados inválidos
ALTER TABLE organization
    ADD CONSTRAINT chk_org_status
        CHECK (status IN ('PENDING','ACTIVE','INACTIVE','DELETED'));

ALTER TABLE booking
    ADD CONSTRAINT chk_booking_status
        CHECK (status IN ('RESERVED','CONFIRMED','COMPLETED','CANCELLED'));

-- Evitar superposiciones lógicas básicas
ALTER TABLE booking
    ADD CONSTRAINT chk_booking_times
        CHECK (start_time < end_time);

-- Porcentaje de pago válido
ALTER TABLE service
    ADD CONSTRAINT chk_service_advance_payment
        CHECK (advance_payment BETWEEN 0 AND 100);

-- Sin nombres duplicados por organización
ALTER TABLE provider
    ADD CONSTRAINT uq_provider_email_org UNIQUE (organization_id, email);

ALTER TABLE client
    ADD CONSTRAINT uq_client_email_org UNIQUE (organization_id, email);


-- ============================================
-- ÍNDICES DE RELACIONES Y CONSULTAS COMUNES
-- ============================================

-- ORGANIZATION
CREATE INDEX idx_org_status ON organization(status);

-- LOCATION
CREATE INDEX idx_location_org ON location(organization_id);
CREATE INDEX idx_location_active ON location(active);

-- USER
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_org ON users(organization_id);
CREATE INDEX idx_user_role ON users(role);

-- PROVIDER
CREATE INDEX idx_provider_org ON provider(organization_id);
CREATE INDEX idx_provider_active ON provider(active);

-- SERVICE
CREATE INDEX idx_service_provider ON service(provider_id);
CREATE INDEX idx_service_location ON service(location_id);
CREATE INDEX idx_service_price ON service(price);

-- CLIENT
CREATE INDEX idx_client_org ON client(organization_id);
CREATE INDEX idx_client_email ON client(email);

-- BOOKING
CREATE INDEX idx_booking_org ON booking(organization_id);
CREATE INDEX idx_booking_client ON booking(client_id);
CREATE INDEX idx_booking_provider ON booking(provider_id);
CREATE INDEX idx_booking_service ON booking(service_id);
CREATE INDEX idx_booking_start_time ON booking(start_time);
CREATE INDEX idx_booking_status ON booking(status);

-- PAYMENT
CREATE INDEX idx_payment_booking ON payment(booking_id);
CREATE INDEX idx_payment_status ON payment(status);

-- NOTIFICATION
CREATE INDEX idx_notification_booking ON notification(related_booking_id);
CREATE INDEX idx_notification_channel ON notification(channel);
CREATE INDEX idx_notification_status ON notification(status);

-- PROVIDER_ORGANIZATION
CREATE INDEX idx_provider_org_provider ON provider_organization(provider_id);
CREATE INDEX idx_provider_org_org ON provider_organization(organization_id);
CREATE INDEX idx_provider_org_location ON provider_organization(location_id);    