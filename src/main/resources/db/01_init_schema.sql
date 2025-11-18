-- =============================================
-- CONFIGURACIÓN DE ESQUEMA Y LIMPIEZA
-- =============================================
CREATE SCHEMA IF NOT EXISTS public;
SET search_path TO public;

DO
$$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
        EXECUTE 'DROP TABLE IF EXISTS public.' || quote_ident(r.tablename) || ' CASCADE';
    END LOOP;
END
$$;

SHOW search_path;


-- =============================================
-- MODELO DE DATOS WATurnos
-- =============================================


-- ===========================================================
--   TABLE: categories
--   Estructura jerárquica para Categoría / Subcategoría
-- ===========================================================

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(120),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL
);


-- Tabla: organization
CREATE TABLE organization (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    logo_url TEXT,
    timezone VARCHAR(100),
    type BIGINT REFERENCES categories(id),
    default_language VARCHAR(10),
    active BOOLEAN DEFAULT TRUE,
    simple_organization BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creator VARCHAR(100),
    modificator VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    CONSTRAINT chk_org_status CHECK (status IN ('PENDING','ACTIVE','INACTIVE','DELETED'))
);

-- Tabla: location
CREATE TABLE location (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creator VARCHAR(100),
    modificator VARCHAR(100),
    main BOOLEAN DEFAULT FALSE
);

-- Tabla: organization_props
CREATE TABLE organization_props (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT REFERENCES organization(id) ON DELETE CASCADE,
    key VARCHAR(255),
    value TEXT
);

-- Tabla: users (incluye providers)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    password TEXT,
    organization_id BIGINT REFERENCES organization(id) ON DELETE CASCADE,
    photo_url TEXT,
    bio TEXT,
    active BOOLEAN DEFAULT TRUE,
    role VARCHAR(50),
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creator VARCHAR(100),
    modificator VARCHAR(100),
    CONSTRAINT uq_users_email_org UNIQUE (organization_id, email)
);

-- Tabla: users_props
CREATE TABLE users_props (
    id BIGSERIAL PRIMARY KEY,
    users_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    key VARCHAR(255),
    value TEXT
);

-- Tabla: service
CREATE TABLE service (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    duration_minutes INT,
    price DECIMAL(10,2),
    type BIGINT REFERENCES categories(id),
    advance_payment INT CHECK (advance_payment BETWEEN 0 AND 100),
    future_days INT,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    location_id BIGINT REFERENCES location(id) ON DELETE SET NULL,
    organization_id BIGINT REFERENCES organization(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creator VARCHAR(100),
    modificator VARCHAR(100),
    capacity INTEGER NOT NULL,
    CHECK (capacity > 0)
);

-- Tabla: service_props
CREATE TABLE service_props (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT REFERENCES service(id) ON DELETE CASCADE,
    key VARCHAR(255),
    value TEXT
);

-- Tabla: client
CREATE TABLE client (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    password TEXT,
    organization_id BIGINT REFERENCES organization(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    creator VARCHAR(100),
    modificator VARCHAR(100),
    CONSTRAINT uq_client_email_org UNIQUE (organization_id, email)
);

-- Tabla: client_props
CREATE TABLE client_props (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES client(id) ON DELETE CASCADE,
    key VARCHAR(255),
    value TEXT
);

-- Tabla: recurrence
CREATE TABLE recurrence (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT REFERENCES service(id) ON DELETE CASCADE,
    client_id BIGINT REFERENCES client(id) ON DELETE SET NULL,
    pattern VARCHAR(50),
    interval SMALLINT DEFAULT 1,
    weekdays VARCHAR(20),
    start_date DATE,
    end_date DATE,
    count INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: booking
CREATE TABLE booking (
    id BIGSERIAL PRIMARY KEY,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('FREE','RESERVED','NO_SHOW','COMPLETED','CANCELLED')),
    notes TEXT,
    service_id BIGINT REFERENCES service(id) ON DELETE SET NULL,
    recurrence_id BIGINT REFERENCES recurrence(id) ON DELETE SET NULL,
    organization_id BIGINT REFERENCES organization(id) ON DELETE CASCADE,
    cancel_reason TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    free_slots INTEGER NOT NULL,
    CHECK (free_slots >= 0),
    CONSTRAINT chk_booking_times CHECK (start_time < end_time)
    
);

-- Tabla: payment
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT REFERENCES booking(id) ON DELETE SET NULL,
    amount DECIMAL(10,2),
    method VARCHAR(50),
    status VARCHAR(50),
    currency VARCHAR(10),
    voucher VARCHAR(100),
    paid_at TIMESTAMP
);

-- Tabla: notification
CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50),
    target VARCHAR(255),
    message TEXT,
    status VARCHAR(50),
    channel VARCHAR(50),
    related_booking_id BIGINT REFERENCES booking(id) ON DELETE SET NULL,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: availability
CREATE TABLE availability (
    id BIGSERIAL PRIMARY KEY,
    day_of_week SMALLINT,
    start_time TIME,
    end_time TIME,
    service_id BIGINT REFERENCES service(id) ON DELETE CASCADE
);

-- Tabla: unavailability
CREATE TABLE unavailability (
    id BIGSERIAL PRIMARY KEY,
    start_day DATE,
    end_day DATE,
    start_time TIME,
    end_time TIME,
    day_of_week SMALLINT,
    service_id BIGINT REFERENCES service(id) ON DELETE CASCADE
);

-- Tabla: password_reset_token
CREATE TABLE password_reset_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(36) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    client_id BIGINT REFERENCES client(id) ON DELETE CASCADE,
    CONSTRAINT chk_one_user_or_client CHECK (
        (user_id IS NOT NULL AND client_id IS NULL) OR
        (user_id IS NULL AND client_id IS NOT NULL)
    )
);

-- Tabla: booking_client
CREATE TABLE booking_client (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    
    -- Restricción para asegurar que un cliente no se reserve dos veces en el mismo booking
    CONSTRAINT uk_booking_client UNIQUE (booking_id, client_id), 
    
    -- Definición de la clave foránea a booking
    CONSTRAINT fk_booking_client_booking 
        FOREIGN KEY (booking_id) 
        REFERENCES booking (id) 
        ON DELETE CASCADE, -- Si se borra el booking, se borran las entradas aquí
        
    -- Definición de la clave foránea a client
    CONSTRAINT fk_booking_client_client
        FOREIGN KEY (client_id) 
        REFERENCES client (id) 
        ON DELETE CASCADE -- Si se borra el cliente, se borran las entradas aquí
);

-- Índice único por nombre + padre
CREATE UNIQUE INDEX uk_category_name_parent
ON categories (name, parent_id);


-- ============================================
-- ÍNDICES DE OPTIMIZACIÓN WATurnos
-- ============================================

-- ORGANIZATION
CREATE INDEX idx_organization_status ON organization(status);
CREATE INDEX idx_organization_active ON organization(active);

-- LOCATION
CREATE INDEX idx_location_organization ON location(organization_id);
CREATE INDEX idx_location_active ON location(active);
CREATE INDEX idx_location_name ON location(name);

-- USERS
CREATE INDEX idx_users_organization ON users(organization_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_users_fullname ON users(full_name);

-- SERVICE
CREATE INDEX idx_service_user ON service(user_id);
CREATE INDEX idx_service_location ON service(location_id);
CREATE INDEX idx_service_organization ON service(organization_id);
CREATE INDEX idx_service_price ON service(price);
CREATE INDEX idx_service_name ON service(name);
CREATE INDEX idx_service_duration ON service(duration_minutes);

-- CLIENT
CREATE INDEX idx_client_organization ON client(organization_id);
CREATE INDEX idx_client_email ON client(email);
CREATE INDEX idx_client_fullname ON client(full_name);

-- BOOKING
CREATE INDEX idx_booking_client ON booking(client_id);
CREATE INDEX idx_booking_service ON booking(service_id);
CREATE INDEX idx_booking_status ON booking(status);
CREATE INDEX idx_booking_start_time ON booking(start_time);
CREATE INDEX idx_booking_end_time ON booking(end_time);
CREATE INDEX idx_booking_org ON booking(organization_id);

-- RECURRENCE
CREATE INDEX idx_recurrence_service ON recurrence(service_id);
CREATE INDEX idx_recurrence_client ON recurrence(client_id);
CREATE INDEX idx_recurrence_pattern ON recurrence(pattern);

-- PAYMENT
CREATE INDEX idx_payment_booking ON payment(booking_id);
CREATE INDEX idx_payment_status ON payment(status);

-- NOTIFICATION
CREATE INDEX idx_notification_booking ON notification(related_booking_id);
CREATE INDEX idx_notification_channel ON notification(channel);
CREATE INDEX idx_notification_status ON notification(status);

-- AVAILABILITY
CREATE INDEX idx_availability_service ON availability(service_id);
CREATE INDEX idx_availability_day_of_week ON availability(day_of_week);

-- UNAVAILABILITY
CREATE INDEX idx_unavailability_service ON unavailability(service_id);
CREATE INDEX idx_unavailability_range ON unavailability(start_day, end_day);

-- PROPS TABLES
CREATE INDEX idx_organizationprops_key ON organization_props(key);
CREATE INDEX idx_serviceprops_key ON service_props(key);
CREATE INDEX idx_usersprops_key ON users_props(key);
CREATE INDEX idx_clientprops_key ON client_props(key);


-- ============================================
-- AJUSTE DE SECUENCIAS
-- ============================================
DO $$
DECLARE
    tbl RECORD;
BEGIN
    FOR tbl IN SELECT tablename FROM pg_tables WHERE schemaname = 'public' LOOP
        EXECUTE format(
            'SELECT setval(pg_get_serial_sequence(''%I.%I'', ''id''), COALESCE(MAX(id), 1), false) FROM %I.%I;',
            'public', tbl.tablename, 'public', tbl.tablename
        );
    END LOOP;
END $$;

--Se actualiza secuencia para que en los inserts de booking haga de a 100 el insert 
ALTER SEQUENCE booking_id_seq INCREMENT BY 100;