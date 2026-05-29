-- ============================================================
-- 08_parametros_sistema.sql
-- Tabla de parámetros de negocio configurables desde la UI
-- Requisito 1: valores de pagos/costos NO hardcodeados
-- ============================================================

CREATE TABLE IF NOT EXISTS parametros_sistema (
    id           INT          NOT NULL AUTO_INCREMENT,
    clave        VARCHAR(100) NOT NULL,
    valor        VARCHAR(500) NOT NULL,
    descripcion  VARCHAR(500),
    tipo         VARCHAR(20)  NOT NULL DEFAULT 'TEXTO'
                 COMMENT 'ENTERO | DECIMAL | TEXTO | BOOLEANO',
    activo       TINYINT(1)   NOT NULL DEFAULT 1,
    usuario_mod  VARCHAR(100),
    fecha_mod    TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_parametros_clave (clave)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Valores por defecto (mismos que estaban hardcodeados) ──
INSERT INTO parametros_sistema (clave, valor, descripcion, tipo) VALUES
('MESES_PLAZO_PAGO',       '4',                              'Meses de plazo normal para pagar anualidad sin recargo',         'ENTERO'),
('MESES_PERIODO_GRACIA',   '6',                              'Meses adicionales de período de gracia (con recargo)',            'ENTERO'),
('PORCENTAJE_RECARGO',     '10.00',                          'Porcentaje de recargo durante el período de gracia',              'DECIMAL'),
('ANIOS_INCREMENTO_COSTO', '5',                              'Cada cuántos años se incrementa el costo base de la anualidad',   'ENTERO'),
('PORCENTAJE_INCREMENTO',  '5.00',                           'Porcentaje de incremento acumulado por grupo de años',            'DECIMAL'),
('DIAS_ALERTA_ANUALIDAD',  '15',                             'Días de anticipación para mostrar alertas de anualidades',        'ENTERO'),
('URL_APP_EXTERNA',        'http://localhost:8081/api',      'URL base del aplicativo externo de variedades (en desarrollo)',   'TEXTO'),
('TOKEN_APP_EXTERNA',      '',                               'Token Bearer de autenticación con el aplicativo externo',         'TEXTO');
