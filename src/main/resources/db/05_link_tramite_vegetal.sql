-- Migración 05: vincula tramites administrativos con solicitudes del portal (vegetable_forms)
-- Solo se usan tramites del portal que ya estén en proceso (flowPhase != 'INITIAL')
ALTER TABLE tramites
    ADD COLUMN IF NOT EXISTS vegetable_form_id INTEGER NULL
        REFERENCES vegetable_forms(id) ON DELETE SET NULL;
