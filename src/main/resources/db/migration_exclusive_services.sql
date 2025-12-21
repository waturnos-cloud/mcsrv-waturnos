-- Migración para asegurar que todos los usuarios existentes tengan exclusive_services = false
-- Este script debe ejecutarse una sola vez después de agregar la columna

-- Si la columna ya existe sin el constraint NOT NULL, actualizarla
UPDATE users 
SET exclusive_services = false 
WHERE exclusive_services IS NULL;

-- Asegurar que la columna tenga el constraint NOT NULL y DEFAULT FALSE
-- Si ya tiene estos valores, este comando no hará nada

-- Para verificar que todo está correcto:
-- SELECT COUNT(*) FROM users WHERE exclusive_services IS NULL;
-- Resultado esperado: 0
