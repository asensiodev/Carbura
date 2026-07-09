-- Sync v0 uses stable client-generated ids such as `vehicle-123`.
-- Remote family ids stay uuid, but syncable entity ids must accept text ids.

alter table public.maintenance_records
  drop constraint if exists maintenance_records_vehicle_id_family_id_fkey;

alter table public.reminders
  drop constraint if exists reminders_vehicle_id_family_id_fkey;

alter table public.vehicles
  alter column id drop default,
  alter column id type text using id::text,
  alter column id set default gen_random_uuid()::text;

alter table public.maintenance_records
  alter column id drop default,
  alter column id type text using id::text,
  alter column id set default gen_random_uuid()::text,
  alter column vehicle_id type text using vehicle_id::text;

alter table public.reminders
  alter column id drop default,
  alter column id type text using id::text,
  alter column id set default gen_random_uuid()::text,
  alter column vehicle_id type text using vehicle_id::text;

alter table public.maintenance_records
  add constraint maintenance_records_vehicle_id_family_id_fkey
  foreign key (vehicle_id, family_id)
  references public.vehicles(id, family_id)
  on delete cascade;

alter table public.reminders
  add constraint reminders_vehicle_id_family_id_fkey
  foreign key (vehicle_id, family_id)
  references public.vehicles(id, family_id)
  on delete cascade;
