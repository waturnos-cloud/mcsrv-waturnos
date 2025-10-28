-- ============================================
-- ORGANIZATION
-- ============================================
INSERT INTO organization (id, name, logo_url, timezone, type, default_language, active, creator, modificator, status)
VALUES
(1, 'Barbería Tandil', 'https://cdn.waturnos.com/logos/barberia.png', 'America/Argentina/Buenos_Aires', 'Barbería', 'es', TRUE, 'system', NULL, 'ACTIVE'),
(2, 'Gimnasio FitZone', 'https://cdn.waturnos.com/logos/gym.png', 'America/Argentina/Buenos_Aires', 'Gimnasio', 'es', TRUE, 'system', NULL, 'ACTIVE'),
(3, 'Clínica Dental Mar', 'https://cdn.waturnos.com/logos/dental.png', 'America/Argentina/Buenos_Aires', 'Consultorio', 'es', TRUE, 'system', NULL, 'ACTIVE');

-- ============================================
-- LOCATION
-- ============================================
INSERT INTO location (id, organization_id, name, address, phone, email, latitude, longitude, active, creator)
VALUES
(1, 1, 'Sucursal Centro', 'San Martín 200, Tandil', '2494000001', 'centro@barberia.com', -37.3215, -59.1332, TRUE, 'system'),
(2, 2, 'Sucursal Norte', 'Av. España 1500, Tandil', '2494000002', 'norte@fitzone.com', -37.3100, -59.1280, TRUE, 'system'),
(3, 3, 'Sucursal Mar del Plata', 'Colón 3000, Mar del Plata', '2234000003', 'mar@dentalmar.com', -38.0000, -57.5500, TRUE, 'system');

-- ============================================
-- ORGANIZATION_PROPS
-- ============================================
INSERT INTO organization_props (organization_id, key, value)
VALUES
(1, 'color_theme', 'dark'),
(2, 'currency', 'ARS'),
(3, 'booking_limit', '60');

-- ============================================
-- USERS
-- ============================================
INSERT INTO users (id, full_name, email, phone, password, organization_id, active, role, creator)
VALUES
(1, 'Jose Admin', 'admin@demo.com', '2494111111', '{bcrypt}$2a$10$uOty4UgdxoogqOudG16TPuw8bxz7tQ3oRG4.MK40r56HUE0Eu6mda', 1, TRUE, 'admin', 'system'),
(2, 'Laura Gym', 'laura@fitzone.com', '2494222222', '{bcrypt}$2a$10$uOty4UgdxoogqOudG16TPuw8bxz7tQ3oRG4.MK40r56HUE0Eu6mda', 2, TRUE, 'manager', 'system'),
(3, 'Pedro Dental', 'pedro@dentalmar.com', '2234333333', '{bcrypt}$2a$10$uOty4UgdxoogqOudG16TPuw8bxz7tQ3oRG4.MK40r56HUE0Eu6mda', 3, TRUE, 'admin', 'system');

-- ============================================
-- USER_PROPS
-- ============================================
INSERT INTO users_props (users_id, key, value)
VALUES
(1, 'theme', 'dark'),
(2, 'notifications', 'enabled'),
(3, 'language', 'es');

-- ============================================
-- PROVIDER
-- ============================================
INSERT INTO provider (id, full_name, email, phone, photo_url, bio, organization_id, creator)
VALUES
(1, 'Carlos Estilo', 'carlos@barberia.com', '2494551111', 'https://cdn.waturnos.com/photos/carlos.jpg', 'Especialista en cortes modernos.', 1, 'system'),
(2, 'Ana Fit', 'ana@fitzone.com', '2494662222', 'https://cdn.waturnos.com/photos/ana.jpg', 'Personal trainer y nutricionista.', 2, 'system'),
(3, 'Sofía Dente', 'sofia@dentalmar.com', '2234773333', 'https://cdn.waturnos.com/photos/sofia.jpg', 'Odontóloga especializada en ortodoncia.', 3, 'system');

-- ============================================
-- SERVICE
-- ============================================
INSERT INTO service (id, name, description, duration_minutes, price, advance_payment, future_days, provider_id, location_id, creator)
VALUES
(1, 'Corte clásico', 'Corte de cabello tradicional.', 30, 3500.00, 0, 7, 1, 1, 'system'),
(2, 'Entrenamiento personalizado', 'Sesión individual de entrenamiento funcional.', 60, 5000.00, 20, 14, 2, 2, 'system'),
(3, 'Limpieza dental', 'Limpieza profesional y control odontológico.', 45, 7000.00, 50, 10, 3, 3, 'system');

-- ============================================
-- SERVICE_PROPS
-- ============================================
INSERT INTO service_props (service_id, key, value)
VALUES
(1, 'nivel', 'básico'),
(2, 'requiere_turno', 'true'),
(3, 'instrumental', 'manual');

-- ============================================
-- CLIENT
-- ============================================
INSERT INTO client (id, full_name, email, phone, password, organization_id, creator)
VALUES
(1, 'Mariano Pérez', 'mariano@mail.com', '2494771111', '{bcrypt}$2a$10$uOty4UgdxoogqOudG16TPuw8bxz7tQ3oRG4.MK40r56HUE0Eu6mda', 1, 'system'),
(2, 'Lucía Gómez', 'lucia@mail.com', '2494882222', '{bcrypt}$2a$10$uOty4UgdxoogqOudG16TPuw8bxz7tQ3oRG4.MK40r56HUE0Eu6mda', 2, 'system'),
(3, 'Tomás Díaz', 'tomas@mail.com', '2234993333', '{bcrypt}$2a$10$uOty4UgdxoogqOudG16TPuw8bxz7tQ3oRG4.MK40r56HUE0Eu6mda', 3, 'system');

-- ============================================
-- CLIENT_PROPS
-- ============================================
INSERT INTO client_props (client_id, key, value)
VALUES
(1, 'preferencias', 'turnos por la tarde'),
(2, 'membresía', 'gold'),
(3, 'recordatorio', 'whatsapp');

-- ============================================
-- BOOKING
-- ============================================
INSERT INTO booking (id, start_time, end_time, status, notes, organization_id, client_id, provider_id, service_id, created_at)
VALUES
(1, '2025-10-21 10:00:00-03', '2025-10-21 10:30:00-03', 'CONFIRMED', 'Cliente puntual.', 1, 1, 1, 1, NOW()),
(2, '2025-10-21 11:00:00-03', '2025-10-21 12:00:00-03', 'RESERVED', 'Primera clase.', 2, 2, 2, 2, NOW()),
(3, '2025-10-21 14:00:00-03', '2025-10-21 14:45:00-03', 'COMPLETED', 'Revisión completa.', 3, 3, 3, 3, NOW());

-- ============================================
-- RECURRENCE
-- ============================================
INSERT INTO recurrence (id, service_id, client_id, pattern, interval, weekdays, start_date, end_date)
VALUES
(1, 2, 2, 'weekly', 1, '2,4', '2025-10-01', '2025-12-31'),
(2, 1, 1, 'biweekly', 2, '1,3', '2025-10-01', '2025-11-30'),
(3, 3, 3, 'monthly', 1, NULL, '2025-10-15', '2026-03-15');

-- ============================================
-- PAYMENT
-- ============================================
INSERT INTO payment (id, booking_id, amount, method, status, currency, voucher, paid_at)
VALUES
(1, 1, 3500.00, 'efectivo', 'pagado', 'ARS', 'VOU123', '2025-10-21 10:40:00'),
(2, 2, 1000.00, 'mercadopago', 'pendiente', 'ARS', 'MP456', NULL),
(3, 3, 7000.00, 'tarjeta', 'pagado', 'ARS', 'VISA789', '2025-10-21 14:50:00');

-- ============================================
-- NOTIFICATION
-- ============================================
INSERT INTO notification (id, type, target, message, status, channel, related_booking_id, sent_at)
VALUES
(1, 'recordatorio', '2494771111', 'Recordá tu turno en Barbería Tandil', 'enviado', 'whatsapp', 1, NOW()),
(2, 'confirmación', '2494882222', 'Tu entrenamiento fue confirmado', 'pendiente', 'whatsapp', 2, NULL),
(3, 'agradecimiento', '2234993333', 'Gracias por tu visita a Clínica Dental Mar', 'enviado', 'email', 3, NOW());

-- ============================================
-- AVAILABILITY
-- ============================================
INSERT INTO availability (id, day_of_week, start_time, end_time, service_id)
VALUES
(1, 1, '09:00', '18:00', 1),
(2, 2, '08:00', '20:00', 2),
(3, 3, '10:00', '17:00', 3);

-- ============================================
-- UNAVAILABILITY
-- ============================================
INSERT INTO unavailability (id, start_day, end_day, start_time, end_time, day_of_week, service_id)
VALUES
(1, '2025-12-24', '2025-12-26', '00:00', '23:59', NULL, 1),
(2, '2025-12-31', '2026-01-01', '00:00', '23:59', NULL, 2),
(3, '2025-11-20', '2025-11-20', '09:00', '12:00', 3, 3);

-- ============================================
-- PROVIDER_ORGANIZATION
-- ============================================
INSERT INTO provider_organization (id, provider_id, organization_id, location_id, start_date, active, creator)
VALUES
(1, 1, 1, 1, '2024-01-01', TRUE, 'system'),
(2, 2, 2, 2, '2024-03-15', TRUE, 'system'),
(3, 3, 3, 3, '2024-06-01', TRUE, 'system');