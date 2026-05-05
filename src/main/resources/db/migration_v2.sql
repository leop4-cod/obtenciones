-- ============================================================
--  adminobtenciones — Migration V2
--  Compatible con: MySQL 5.7.29
--  Aplicar una sola vez sobre la BD senadi_vegetable
-- ============================================================

-- 1. Tabla de comprobantes de pago (relación 1:N con vegetable_forms)
-- Reemplaza el campo paymentReceiptId (1:1) por una tabla dedicada.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS comprobante_pago (
    id                INT          NOT NULL AUTO_INCREMENT,
    vegetable_form_id INT          NOT NULL,
    nombre_archivo    VARCHAR(255) NOT NULL COMMENT 'Nombre original del archivo',
    ruta_archivo      VARCHAR(500) NOT NULL COMMENT 'Ruta absoluta en el servidor',
    fecha_carga       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cargado_por       VARCHAR(100)     NULL,
    tamano_bytes      BIGINT           NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comp_pago_form
        FOREIGN KEY (vegetable_form_id)
        REFERENCES vegetable_forms (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    INDEX idx_comp_pago_form (vegetable_form_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Comprobantes de pago por trámite (1:N)';


-- 2. Tabla de secuencias numéricas atómicas
-- Elimina race conditions en generación de OV-YYYY-XXXX, CD-YYYY-XXXX, etc.
-- INSERT ... ON DUPLICATE KEY UPDATE garantiza atomicidad en MySQL 5.7.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS secuencia_numeracion (
    tipo   VARCHAR(10) NOT NULL COMMENT 'Prefijo: OV, CD, RES, COB',
    anio   SMALLINT    NOT NULL COMMENT 'Año fiscal',
    ultimo INT         NOT NULL DEFAULT 0 COMMENT 'Último número emitido',
    PRIMARY KEY (tipo, anio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Secuencias numéricas por tipo y año — acceso atómico';

-- Inicializar para el año actual con los contadores en 0.
-- Si ya existen filas de años anteriores se conservan (ON DUPLICATE KEY).
INSERT INTO secuencia_numeracion (tipo, anio, ultimo)
VALUES
    ('OV',  YEAR(NOW()), 0),
    ('CD',  YEAR(NOW()), 0),
    ('RES', YEAR(NOW()), 0),
    ('COB', YEAR(NOW()), 0)
ON DUPLICATE KEY UPDATE tipo = tipo;  -- no-op si ya existe


-- 3. Ajuste en vegetable_forms: payment_receipt_id era 1:1 y ya no se usa.
--    Se mantiene la columna para no romper datos históricos, pero se marca
--    como deprecada. Los nuevos registros usan comprobante_pago.
--    (Descomentar solo si se desea eliminarla físicamente tras migración de datos)
-- ALTER TABLE vegetable_forms DROP COLUMN payment_receipt_id;


-- 4. Migrar comprobantes históricos (ejecutar solo si existen datos en SFTP/filesystem
--    y se conoce la ruta de los archivos anteriores).
--    Este bloque es un ejemplo orientativo; adaptar rutas según entorno.
-- INSERT INTO comprobante_pago (vegetable_form_id, nombre_archivo, ruta_archivo, fecha_carga, cargado_por)
-- SELECT id,
--        CONCAT('comprobante_historico_', id, '.pdf'),
--        CONCAT('/opt/uploads/obtenciones/', id, '/pdf_voucher_breederfrm_', id, '.pdf'),
--        COALESCE(application_date, NOW()),
--        'MIGRACION'
-- FROM vegetable_forms
-- WHERE payment_receipt_id IS NOT NULL
--   AND NOT EXISTS (SELECT 1 FROM comprobante_pago cp WHERE cp.vegetable_form_id = vegetable_forms.id);


-- ============================================================
--  FIN DEL SCRIPT
-- ============================================================
