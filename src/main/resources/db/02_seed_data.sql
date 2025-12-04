INSERT INTO users (full_name, email, phone, password, organization_id, active, role, creator)
VALUES
('ADMIN', 'waturnos@gmail.com', '1111111', '{bcrypt}$2a$10$uOty4UgdxoogqOudG16TPuw8bxz7tQ3oRG4.MK40r56HUE0Eu6mda', null, TRUE, 'ADMIN', 'system');


-- ===========================================================
-- 🔥 LIMPIEZA DE TABLA
-- ===========================================================
DELETE FROM categories;
ALTER SEQUENCE categories_id_seq RESTART WITH 11;
BEGIN;

-- 1️⃣ Desactivar triggers (incluye FKs) en organization
ALTER TABLE organization DISABLE TRIGGER ALL;

-- 2️⃣ Limpiar categorías + resetear IDs
TRUNCATE TABLE categories RESTART IDENTITY CASCADE;

-- ===========================================================
--   CATEGORÍAS PADRE
-- ===========================================================
INSERT INTO categories (name, slug, active) VALUES
('Deporte', 'deporte', true),
('Salud', 'salud', true),
('Belleza y cuidado personal', 'belleza', true),
('Bienestar', 'bienestar', true),
('Educación y clases', 'educacion', true),
('Automotor', 'automotor', true),
('Eventos', 'eventos', true);

-- ===========================================================
--   SUBCATEGORÍAS
-- ===========================================================

-- 1️⃣ Deporte (parent_id = 1)
INSERT INTO categories (name, slug, active, parent_id) VALUES
('Pádel', 'padel', true, 1),
('Fútbol', 'futbol', true, 1),
('Tenis', 'tenis', true, 1),
('Gimnasios', 'gimnasios', true, 1),
('Crossfit', 'crossfit', true, 1);

-- 2️⃣ Salud (parent_id = 2)
INSERT INTO categories (name, slug, active, parent_id) VALUES
('Consultorios médicos', 'consultorios-medicos', true, 2),
('Clínica médica', 'clinica-medica', true, 2),
('Odontología', 'odontologia', true, 2),
('Kinesiología', 'kinesiologia', true, 2),
('Nutrición', 'nutricion', true, 2),
('Laboratorios', 'laboratorios', true, 2),
('Dermatología', 'dermatologia', true, 2),
('Cardiología', 'cardiologia', true, 2),
('Oftalmología', 'oftalmologia', true, 2),
('Traumatología', 'traumatologia', true, 2),
('Gastroenterología', 'gastroenterologia', true, 2);

-- 3️⃣ Belleza y cuidado personal (parent_id = 3)
INSERT INTO categories (name, slug, active, parent_id) VALUES
('Peluquería', 'peluqueria', true, 3),
('Barbería', 'barberia', true, 3),
('Manicura', 'manicura', true, 3),
('Estética facial', 'estetica-facial', true, 3),
('Masajes estéticos', 'masajes-esteticos', true, 3);

-- 4️⃣ Bienestar (parent_id = 4)
INSERT INTO categories (name, slug, active, parent_id) VALUES
('Masajes terapéuticos', 'masajes-terapeuticos', true, 4),
('Yoga', 'yoga', true, 4),
('Pilates', 'pilates', true, 4),
('Reiki', 'reiki', true, 4),
('Meditación guiada', 'meditacion-guiada', true, 4);

-- 5️⃣ Educación y clases (parent_id = 5)
INSERT INTO categories (name, slug, active, parent_id) VALUES
('Clases particulares', 'clases-particulares', true, 5),
('Música', 'musica', true, 5),
('Idiomas', 'idiomas', true, 5),
('Informática', 'informatica', true, 5);

-- 6️⃣ Automotor (parent_id = 6)
INSERT INTO categories (name, slug, active, parent_id) VALUES
('Lavadero', 'lavadero', true, 6),
('Taller mecánico', 'taller-mecanico', true, 6),
('Gomería', 'gomeria', true, 6);

-- 7️⃣ Eventos (parent_id = 7)
INSERT INTO categories (name, slug, active, parent_id) VALUES
('Fotografía', 'fotografia', true, 7),
('Sonido', 'sonido', true, 7),
('Organización de eventos', 'organizacion-eventos', true, 7),
('Salones para fiestas', 'salones-fiestas', true, 7),
('Cumpleaños infantiles', 'cumpleanos-infantiles', true, 7),
('Quinchos y parrillas', 'quinchos-parrillas', true, 7);

-- 3️⃣ Volver a activar triggers en organization
ALTER TABLE organization ENABLE TRIGGER ALL;

COMMIT;