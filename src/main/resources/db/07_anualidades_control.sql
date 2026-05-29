-- ============================================================
-- Migración 07: Control de plazos, recargos e incrementos
-- Amplía la tabla anualidades con las reglas de negocio:
--   - 4 meses de plazo normal sin recargo
--   - 6 meses de gracia con recargo porcentual
--   - Incremento automático cada 5 años
--   - Estado PROCESO_EXPIRADO tras vencer el período de gracia
-- ============================================================

ALTER TABLE anualidades
    -- Monto base que debe pagarse en esta anualidad
    ADD COLUMN IF NOT EXISTS monto_original        DECIMAL(15,2)  NULL,

    -- Porcentaje de recargo aplicado durante el período de gracia (ej: 10.00 = 10%)
    ADD COLUMN IF NOT EXISTS porcentaje_recargo    DECIMAL(5,2)   NULL DEFAULT 10.00,

    -- Fecha límite de pago normal (fecha_vencimiento + 4 meses)
    ADD COLUMN IF NOT EXISTS fecha_limite_pago     DATE           NULL,

    -- Fecha límite incluyendo gracia (fecha_vencimiento + 10 meses)
    ADD COLUMN IF NOT EXISTS fecha_limite_gracia   DATE           NULL,

    -- Porcentaje de incremento quinquenal que se aplicó al calcular monto_original
    ADD COLUMN IF NOT EXISTS incremento_aplicado   DECIMAL(5,2)   NULL DEFAULT 0.00;

-- Índices para acelerar las transiciones automáticas de estado
CREATE INDEX IF NOT EXISTS idx_anu_limite_pago   ON anualidades (fecha_limite_pago);
CREATE INDEX IF NOT EXISTS idx_anu_limite_gracia ON anualidades (fecha_limite_gracia);

-- Recalcular fecha_limite_pago y fecha_limite_gracia para registros
-- que ya tengan fecha_vencimiento definida (datos migrados del sistema anterior)
UPDATE anualidades
SET
    fecha_limite_pago   = DATE_ADD(fecha_vencimiento, INTERVAL 4 MONTH),
    fecha_limite_gracia = DATE_ADD(fecha_vencimiento, INTERVAL 10 MONTH)
WHERE fecha_vencimiento IS NOT NULL
  AND fecha_limite_pago IS NULL;

-- Inicializar monto_original con el monto ya pagado donde no esté definido
UPDATE anualidades
SET monto_original = monto
WHERE monto IS NOT NULL
  AND monto_original IS NULL;
