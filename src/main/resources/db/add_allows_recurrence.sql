-- Migration: Agregar columna allows_recurrence a la tabla service
-- Fecha: 2026-01-02
-- Descripción: Permite configurar si un servicio admite turnos recurrentes/fijos

-- Agregar la columna allows_recurrence con valor por defecto FALSE
ALTER TABLE service 
  ADD COLUMN IF NOT EXISTS allows_recurrence BOOLEAN NOT NULL DEFAULT FALSE;

-- Agregar comentario descriptivo
COMMENT ON COLUMN service.allows_recurrence IS 'Permite que el servicio tenga turnos recurrentes/fijos';

-- Verificar que la columna se agregó correctamente
-- SELECT column_name, data_type, column_default, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'service' AND column_name = 'allows_recurrence';
