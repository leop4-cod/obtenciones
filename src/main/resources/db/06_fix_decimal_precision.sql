-- ============================================================
-- MIGRACIÓN 06: Ampliar precisión de columnas decimales en anualidades
-- Ejecutar si los montos del Excel superan 99,999,999.99
-- ============================================================

ALTER TABLE anualidades
    MODIFY COLUMN monto                DECIMAL(15,2) NULL,
    MODIFY COLUMN valor_pagado_recargo DECIMAL(15,2) NULL;
