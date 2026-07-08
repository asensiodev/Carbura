-- Sync v0 compatibility for local-first maintenance and reminders.

alter table public.maintenance_records
  alter column maintenance_type_id drop not null;

alter table public.maintenance_records
  add column if not exists maintenance_type_key text,
  add column if not exists maintenance_type_code text,
  add column if not exists next_due_date date;

alter table public.reminders
  add column if not exists maintenance_type_key text;

create or replace function public.ensure_maintenance_type_scope()
returns trigger
language plpgsql
as $$
begin
  if new.maintenance_type_id is null then
    return new;
  end if;

  if not exists (
    select 1
    from public.maintenance_types mt
    where mt.id = new.maintenance_type_id
      and mt.deleted_at is null
      and (mt.is_global = true or mt.family_id = new.family_id)
  ) then
    raise exception 'maintenance_type_id is not available for this family';
  end if;

  return new;
end;
$$;
