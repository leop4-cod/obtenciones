-- ============================================================
-- MIGRACIÓN 04: Módulo de Anualidades
-- Ejecutar después de 03_crear_tramites.sql
-- Compatible con MySQL 5.7
-- ============================================================

CREATE TABLE IF NOT EXISTS anualidades (
    id                   INT            PRIMARY KEY AUTO_INCREMENT,
    vegetable_form_id    INT            NOT NULL,
    anio                 INT            NOT NULL COMMENT '1 a 20',
    etiqueta             VARCHAR(20)    NOT NULL COMMENT 'Año1 ... Año20',
    fecha_vencimiento    DATE           NULL,
    fecha_pago           DATE           NULL,
    monto                DECIMAL(10,2)  NULL,
    numero_comprobante   VARCHAR(150)   NULL,
    estado               VARCHAR(50)    NOT NULL DEFAULT 'PENDIENTE',
    observaciones        TEXT           NULL,
    usuario_registro     VARCHAR(100)   NULL,
    fecha_creacion       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_anu_veg      FOREIGN KEY (vegetable_form_id) REFERENCES vegetable_forms(id) ON DELETE CASCADE,
    CONSTRAINT uq_anu_veg_anio UNIQUE (vegetable_form_id, anio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices de búsqueda frecuente
DROP PROCEDURE IF EXISTS crear_indices_anualidades;
DELIMITER $$
CREATE PROCEDURE crear_indices_anualidades()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'anualidades' AND INDEX_NAME = 'idx_anu_estado'
    ) THEN
        CREATE INDEX idx_anu_estado ON anualidades(estado);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'anualidades' AND INDEX_NAME = 'idx_anu_vencimiento'
    ) THEN
        CREATE INDEX idx_anu_vencimiento ON anualidades(fecha_vencimiento);
    END IF;
END$$
DELIMITER ;
CALL crear_indices_anualidades();
DROP PROCEDURE IF EXISTS crear_indices_anualidades;
