-- ============================================================
-- MIGRACIÓN 12: Agregar fecha_creacion a resolucion y actualizar estado DELIVERED a EN_PROCESO
-- ============================================================

ALTER TABLE resolucion ADD COLUMN fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Actualizar estados históricos en la base de datos
UPDATE vegetable_forms SET status = 'EN_PROCESO' WHERE status = 'DELIVERED';
UPDATE tramites SET estado_actual = 'EN_PROCESO' WHERE estado_actual = 'DELIVERED';
