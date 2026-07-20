ALTER TABLE public.maintenance_records
    ADD COLUMN IF NOT EXISTS maintenance_type_label text;
