-- ============================================================
-- Initial Schema for adminobtenciones
-- Create the vegetable_forms table based on the JPA entity
-- ============================================================

CREATE TABLE IF NOT EXISTS vegetable_forms (
    id                                   INT AUTO_INCREMENT PRIMARY KEY,
    create_date                          TIMESTAMP NULL,
    priority_date                        TIMESTAMP NULL,
    application_number                   VARCHAR(255) NULL,
    botanical_taxon                      TEXT NULL,
    common_name                          TEXT NULL,
    provitional_designation              VARCHAR(255) NULL,
    generic_denomination                 VARCHAR(255) NULL,
    denomination_type                    VARCHAR(50) NULL,
    variety_transfer                     TINYINT(1) NULL,
    variety_transfer_type                VARCHAR(50) NULL,
    variety_transfer_description         VARCHAR(255) NULL,
    geographic_origin                    INT NULL,
    has_other_applications               TINYINT(1) NULL,
    priority_claim                       TINYINT(1) NULL,
    in_territory                         TINYINT(1) NULL,
    out_territory                        TINYINT(1) NULL,
    exam_performed                       TINYINT(1) NULL,
    exam_in_process                      TINYINT(1) NULL,
    no_exam_yet                          TINYINT(1) NULL,
    country_exam                         INT NULL,
    living_sample                        TINYINT(1) NULL,
    sample_place                         VARCHAR(255) NULL,
    country_living_sample                INT NULL,
    genealogy                            VARCHAR(255) NULL,
    process_history                      VARCHAR(255) NULL,
    features_description                 VARCHAR(255) NULL,
    geographical_material_origin         VARCHAR(255) NULL,
    geographical_variety_origin          VARCHAR(255) NULL,
    reproduction_mechanism               VARCHAR(255) NULL,
    additional_information               VARCHAR(255) NULL,
    material_variety_identification      TINYINT(1) NULL,
    product_variety_identification       TINYINT(1) NULL,
    status                               VARCHAR(50) NOT NULL,
    application_date                     TIMESTAMP NULL,
    numeracion_interna                   VARCHAR(100) NULL,
    owner_id                             INT NULL,
    person_noti_direction                VARCHAR(50) NULL,
    electronic_communication_consent     TINYINT(1) NULL,
    varietal_group                       VARCHAR(255) NULL,
    discount_file                        VARCHAR(255) NULL,
    assigned_user                        VARCHAR(255) NULL,
    assigned_date                        TIMESTAMP NULL,
    status_flow                          VARCHAR(50) NULL,
    flow_phase                           VARCHAR(50) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes for performance
CREATE INDEX idx_vegetable_forms_status ON vegetable_forms(status);
CREATE INDEX idx_vegetable_forms_application_number ON vegetable_forms(application_number);
CREATE INDEX idx_vegetable_forms_create_date ON vegetable_forms(create_date);