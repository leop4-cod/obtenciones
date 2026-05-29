-- ============================================================
-- 11_remove_assigned_user.sql
-- Eliminar columnas de "usuario asignado" de vegetable_forms
-- El trabajo es colaborativo: no existe responsable único.
-- Requisito 4: gestión colaborativa y auditoría
-- ============================================================

-- Eliminar índice si existe
ALTER TABLE vegetable_forms DROP INDEX IF EXISTS idx_assigned_user;

-- Eliminar las columnas
ALTER TABLE vegetable_forms
    DROP COLUMN assigned_user,
    DROP COLUMN assigned_date;
