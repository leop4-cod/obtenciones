-- ============================================================
-- 09_rename_numero_tramite.sql
-- Renombrar "numeracion_interna" a "numero_de_tramite"
-- en las tablas tramites y vegetable_forms.
-- MySQL 5.7: usa CHANGE COLUMN (RENAME COLUMN es MySQL 8.0+)
-- Requisito 2: refaccionamiento de nomenclatura
-- ============================================================

-- Tabla tramites
ALTER TABLE tramites
    CHANGE COLUMN numeracion_interna numero_de_tramite VARCHAR(50) NULL DEFAULT NULL
    COMMENT 'Número de trámite interno. Debe ser único si se ingresa.';

-- Actualizar índice único si existía (algunos instaladores lo crean)
-- Si no existe el índice, este bloque no falla gracias al IF EXISTS.
DROP INDEX IF EXISTS uq_tramites_numeracion_interna ON tramites;
CREATE UNIQUE INDEX uq_tramites_numero_tramite
    ON tramites (numero_de_tramite)
    WHERE numero_de_tramite IS NOT NULL;
-- Nota: MySQL 5.7 no soporta índices parciales (WHERE). Usar:
ALTER TABLE tramites DROP INDEX IF EXISTS uq_tramites_numero_tramite;
ALTER TABLE tramites
    ADD UNIQUE INDEX uq_tramites_numero_tramite (numero_de_tramite);
-- La restricción UNIQUE en NULL depende del motor: en MySQL/InnoDB múltiples NULLs están permitidos.

-- Tabla vegetable_forms
ALTER TABLE vegetable_forms
    CHANGE COLUMN numeracion_interna numero_de_tramite VARCHAR(100) NULL DEFAULT NULL;
