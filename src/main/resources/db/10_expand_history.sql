-- ============================================================
-- 10_expand_history.sql
-- Ampliar tabla history para auditoría detallada campo a campo
-- Requisito 4: gestión colaborativa y auditoría
-- ============================================================

ALTER TABLE `history`
    ADD COLUMN tipo_entidad     VARCHAR(100) NULL DEFAULT NULL
        COMMENT 'Clase/entidad modificada: VegetableForms, Tramite, Anualidad, etc.'
        AFTER application_number,
    ADD COLUMN id_entidad       VARCHAR(100) NULL DEFAULT NULL
        COMMENT 'ID o número identificador de la entidad modificada'
        AFTER tipo_entidad,
    ADD COLUMN tipo_accion      VARCHAR(50)  NULL DEFAULT NULL
        COMMENT 'CREAR | ACTUALIZAR | ELIMINAR | CAMBIO_ESTADO | PAGO | ASIGNACION'
        AFTER id_entidad,
    ADD COLUMN campo_modificado VARCHAR(200) NULL DEFAULT NULL
        COMMENT 'Nombre del campo o sección que cambió'
        AFTER tipo_accion,
    ADD COLUMN valor_anterior   TEXT         NULL DEFAULT NULL
        COMMENT 'Valor antes del cambio'
        AFTER campo_modificado,
    ADD COLUMN valor_nuevo      TEXT         NULL DEFAULT NULL
        COMMENT 'Valor después del cambio'
        AFTER valor_anterior;

-- Índices para las consultas más frecuentes
CREATE INDEX idx_history_entidad  ON `history` (tipo_entidad, id_entidad);
CREATE INDEX idx_history_appnum   ON `history` (application_number);
CREATE INDEX idx_history_fecha    ON `history` (fecha);
