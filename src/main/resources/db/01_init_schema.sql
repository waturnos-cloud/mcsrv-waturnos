-- ===============================================
-- WATurnos - Init Schema (PostgreSQL)
-- ===============================================

CREATE SCHEMA IF NOT EXISTS waturnos;
SET search_path TO waturnos;

-- ENUMS
CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED');
CREATE TYPE user_role AS ENUM ('OWNER', 'ADMIN', 'STAFF');

-- TENANTS
CREATE TABLE tenants (
    tenant_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    whatsapp_number VARCHAR(20),
    email VARCHAR(255),
    address VARCHAR(255),
    timezone VARCHAR(50) DEFAULT 'America/Argentina/Buenos_Aires',
    created_at TIMESTAMP DEFAULT NOW(),
    api_key VARCHAR(255)
);

-- USERS
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role user_role DEFAULT 'STAFF',
    created_at TIMESTAMP DEFAULT NOW()
);

-- GLOBAL CUSTOMERS (cliente único global)
CREATE TABLE global_customers (
    global_customer_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

-- CUSTOMERS (vínculo tenant ↔ cliente)
CREATE TABLE customers (
    customer_id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    global_customer_id BIGINT NOT NULL REFERENCES global_customers(global_customer_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (tenant_id, global_customer_id)
);

-- SERVICES
CREATE TABLE services (
    service_id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    duration_min INT NOT NULL,
    price DECIMAL(10,2),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- STAFF
CREATE TABLE staff (
    staff_id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

-- SERVICE_STAFF (relación servicio ↔ staff)
CREATE TABLE service_staff (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES services(service_id) ON DELETE CASCADE,
    staff_id BIGINT NOT NULL REFERENCES staff(staff_id) ON DELETE CASCADE,
    UNIQUE (service_id, staff_id)
);

-- BOOKINGS
CREATE TABLE bookings (
    booking_id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES services(service_id) ON DELETE CASCADE,
    staff_id BIGINT REFERENCES staff(staff_id) ON DELETE SET NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status booking_status DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_booking_tenant_date ON bookings (tenant_id, start_time);

-- HISTÓRICO DE ESTADOS
CREATE TABLE booking_status_history (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(booking_id) ON DELETE CASCADE,
    old_status booking_status,
    new_status booking_status NOT NULL,
    changed_at TIMESTAMP DEFAULT NOW(),
    reason TEXT
);

-- TRIGGER para mantener el histórico
CREATE OR REPLACE FUNCTION waturnos.fn_booking_status_history()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO waturnos.booking_status_history (booking_id, old_status, new_status, reason)
        VALUES (
            OLD.booking_id, 
            OLD.status::waturnos.booking_status, 
            NEW.status::waturnos.booking_status, 
            'Cambio automático'
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_booking_status_history
AFTER UPDATE ON bookings
FOR EACH ROW
EXECUTE FUNCTION fn_booking_status_history();

-- ===============================================
-- END INIT SCHEMA
-- ===============================================