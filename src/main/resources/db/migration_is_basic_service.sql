-- Migración para agregar la columna is_basic a la tabla service
-- Este script debe ejecutarse una sola vez en bases de datos existentes

-- Agregar la columna is_basic con valor por defecto FALSE
ALTER TABLE service 
  ADD COLUMN IF NOT EXISTS is_basic BOOLEAN NOT NULL DEFAULT FALSE;

-- Agregar comentario explicativo
COMMENT ON COLUMN service.is_basic IS 'Indica si el servicio es básico o no';

-- Actualizar servicios existentes (todos como no básicos por defecto)
UPDATE service 
SET is_basic = false 
WHERE is_basic IS NULL;

-- Verificación: contar servicios por tipo
-- SELECT is_basic, COUNT(*) FROM service GROUP BY is_basic;
