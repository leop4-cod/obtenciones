-- ============================================================
-- MIGRACIÓN 03: Módulo de Trámites SENADI
-- Ejecutar después de migration_v2.sql
-- ============================================================

-- Tabla principal de trámites
CREATE TABLE IF NOT EXISTS tramites (
    id                   INT            PRIMARY KEY AUTO_INCREMENT,
    numero_tramite       VARCHAR(100),
    numeracion_interna   VARCHAR(50)    NULL UNIQUE COMMENT 'Numeración interna de la empresa, ej: INT-2025-001',
    titular              VARCHAR(200),
    denominacion         VARCHAR(300),
    estado_actual        VARCHAR(100)   NOT NULL DEFAULT 'DELIVERED'
                         COMMENT 'DELIVERED | ABANDONADA | EN TRÁMITE | PENDIENTE | ATENDIDO | NEGADO | CADUCADO | ACEPTADO | ...',
    puede_editar         TINYINT(1)     NOT NULL DEFAULT 1
                         COMMENT 'true solo cuando estado_actual = DELIVERED',
    fecha_creacion       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    fecha_presentacion   DATE           NULL      COMMENT 'No puede ser fecha futura',
    observaciones        TEXT           NULL,
    usuario_creacion     VARCHAR(100)   NULL,
    usuario_modificacion VARCHAR(100)   NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices de búsqueda frecuente (compatibles con MySQL 5.7)
DROP PROCEDURE IF EXISTS crear_indices_tramites;
DELIMITER $$
CREATE PROCEDURE crear_indices_tramites()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tramites' AND INDEX_NAME = 'idx_tramites_estado'
    ) THEN
        CREATE INDEX idx_tramites_estado ON tramites(estado_actual);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tramites' AND INDEX_NAME = 'idx_tramites_creacion'
    ) THEN
        CREATE INDEX idx_tramites_creacion ON tramites(fecha_creacion);
    END IF;
END$$
DELIMITER ;
CALL crear_indices_tramites();
DROP PROCEDURE IF EXISTS crear_indices_tramites;

-- Tabla de documentos asociados a cada trámite
CREATE TABLE IF NOT EXISTS documentos_tramite (
    id                   INT            PRIMARY KEY AUTO_INCREMENT,
    tramite_id           INT            NOT NULL,
    nombre_personalizado VARCHAR(255)   NOT NULL COMMENT 'Mínimo 5 caracteres',
    nombre_archivo       VARCHAR(255)   NOT NULL COMMENT 'Nombre original del archivo',
    ruta_archivo         VARCHAR(500)   NOT NULL COMMENT 'Ruta absoluta en el servidor',
    tipo_archivo         VARCHAR(50)    NULL      COMMENT 'pdf | jpg | png | ...',
    tamano_bytes         BIGINT         NULL,
    fecha_carga          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_carga        VARCHAR(50)    NULL,
    CONSTRAINT fk_doc_tramite
        FOREIGN KEY (tramite_id) REFERENCES tramites(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP PROCEDURE IF EXISTS crear_indice_doc_tramite;
DELIMITER $$
CREATE PROCEDURE crear_indice_doc_tramite()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'documentos_tramite' AND INDEX_NAME = 'idx_doc_tramite_id'
    ) THEN
        CREATE INDEX idx_doc_tramite_id ON documentos_tramite(tramite_id);
    END IF;
END$$
DELIMITER ;
CALL crear_indice_doc_tramite();
DROP PROCEDURE IF EXISTS crear_indice_doc_tramite;
