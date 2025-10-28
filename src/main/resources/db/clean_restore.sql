-- TRUNCATE todas las tablas del esquema público
DO $$
DECLARE
    sql TEXT := '';
BEGIN
    SELECT string_agg(format('TRUNCATE TABLE %I.%I CASCADE;', schemaname, tablename), ' ')
    INTO sql
    FROM pg_tables
    WHERE schemaname = 'public';
    EXECUTE sql;

    -- Reinicia todas las secuencias
    SELECT string_agg(format('ALTER SEQUENCE %I.%I RESTART WITH 1;', sequence_schema, sequence_name), ' ')
    INTO sql
    FROM information_schema.sequences
    WHERE sequence_schema = 'public';
    EXECUTE sql;
END $$;