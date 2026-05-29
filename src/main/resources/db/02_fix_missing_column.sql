-- Fix for missing column in vegetable_forms table
ALTER TABLE vegetable_forms ADD COLUMN numeracion_interna VARCHAR(100) NULL;