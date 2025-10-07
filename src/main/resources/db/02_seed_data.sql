-- ===============================================
-- WATurnos - Seed Data
-- ===============================================
SET search_path TO waturnos;

-- TENANTS
INSERT INTO tenants (name, whatsapp_number, email, address)
VALUES
  ('Barbería Tandil', '+5492494000001', 'info@barberiatandil.com', 'Av. Colón 123, Tandil'),
  ('Gimnasio MDP', '+5492235000002', 'info@gym-mdp.com', 'San Martín 456, Mar del Plata');

-- USERS
INSERT INTO users (tenant_id, name, email, password_hash, role)
VALUES
  (1, 'Carlos Barber', 'carlos@barberiatandil.com', 'hashed123', 'OWNER'),
  (1, 'Pablo Asistente', 'pablo@barberiatandil.com', 'hashed456', 'STAFF'),
  (2, 'Laura Fit', 'laura@gym-mdp.com', 'hashed789', 'OWNER');

-- GLOBAL CUSTOMERS
INSERT INTO global_customers (name, phone, email)
VALUES
  ('Sebastián Villanueva', '+5492494555555', 'seba@mail.com'),
  ('Juan Pérez', '+5492494666666', 'juan@mail.com'),
  ('María Gómez', '+5492235777777', 'maria@mail.com');

-- CUSTOMERS (relación con tenants)
INSERT INTO customers (tenant_id, global_customer_id)
VALUES
  (1, 1),
  (2, 1),
  (1, 2),
  (2, 3);

-- SERVICES
INSERT INTO services (tenant_id, name, duration_min, price)
VALUES
  (1, 'Corte de Pelo', 30, 3000),
  (1, 'Arreglo de Barba', 20, 2000),
  (2, 'Entrenamiento Funcional', 60, 2500);

-- STAFF
INSERT INTO staff (tenant_id, name, phone, email)
VALUES
  (1, 'Pablo Barber', '+5492494777777', 'pablo@barberiatandil.com'),
  (2, 'Sofía Trainer', '+5492235888888', 'sofia@gym-mdp.com');

-- SERVICE_STAFF
INSERT INTO service_staff (service_id, staff_id)
VALUES
  (1, 1),
  (2, 1),
  (3, 2);

-- BOOKINGS
INSERT INTO bookings (tenant_id, customer_id, service_id, staff_id, start_time, end_time, status, notes)
VALUES
  (1, 1, 1, 1, '2025-10-06 10:00:00', '2025-10-06 10:30:00', 'CONFIRMED', 'Turno confirmado por WhatsApp'),
  (1, 3, 2, 1, '2025-10-06 11:00:00', '2025-10-06 11:20:00', 'PENDING', 'Pendiente de confirmación'),
  (2, 2, 3, 2, '2025-10-06 18:00:00', '2025-10-06 19:00:00', 'CONFIRMED', 'Clase grupal');

-- HISTÓRICO inicial
INSERT INTO booking_status_history (booking_id, old_status, new_status, reason)
VALUES
  (1, NULL, 'CONFIRMED', 'Reserva creada y confirmada'),
  (2, NULL, 'PENDING', 'Reserva pendiente'),
  (3, NULL, 'CONFIRMED', 'Clase confirmada');