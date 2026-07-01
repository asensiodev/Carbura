-- Carbura initial Supabase schema.
-- Apply from Supabase SQL Editor or Supabase CLI in a project without production data.

create extension if not exists pgcrypto;

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create table public.families (
  id uuid primary key default gen_random_uuid(),
  name text not null check (char_length(trim(name)) between 1 and 120),
  invite_code text unique check (invite_code is null or invite_code ~ '^[A-Z0-9]{6}$'),
  created_by uuid not null references auth.users(id) on delete restrict,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz
);

create table public.user_profiles (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null unique references auth.users(id) on delete cascade,
  family_id uuid not null references public.families(id) on delete cascade,
  display_name text not null check (char_length(trim(display_name)) between 1 and 120),
  email text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz
);

create table public.vehicles (
  id uuid primary key default gen_random_uuid(),
  family_id uuid not null references public.families(id) on delete cascade,
  name text not null check (char_length(trim(name)) between 1 and 120),
  vehicle_type text not null check (vehicle_type in ('car', 'motorcycle', 'van', 'other')),
  brand text,
  model text,
  license_plate text,
  current_odometer_km integer not null default 0 check (current_odometer_km >= 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  unique (id, family_id)
);

create table public.maintenance_types (
  id uuid primary key default gen_random_uuid(),
  family_id uuid references public.families(id) on delete cascade,
  code text,
  name text not null check (char_length(trim(name)) between 1 and 120),
  is_global boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  constraint maintenance_types_scope_check check (
    (is_global = true and family_id is null) or
    (is_global = false and family_id is not null)
  )
);

create table public.maintenance_records (
  id uuid primary key default gen_random_uuid(),
  family_id uuid not null references public.families(id) on delete cascade,
  vehicle_id uuid not null,
  maintenance_type_id uuid not null references public.maintenance_types(id) on delete restrict,
  performed_on date not null,
  odometer_km integer check (odometer_km is null or odometer_km >= 0),
  cost_cents integer check (cost_cents is null or cost_cents >= 0),
  currency text not null default 'EUR' check (currency ~ '^[A-Z]{3}$'),
  workshop text,
  notes text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  foreign key (vehicle_id, family_id) references public.vehicles(id, family_id) on delete cascade
);

create table public.reminders (
  id uuid primary key default gen_random_uuid(),
  family_id uuid not null references public.families(id) on delete cascade,
  vehicle_id uuid not null,
  maintenance_type_id uuid references public.maintenance_types(id) on delete set null,
  title text not null check (char_length(trim(title)) between 1 and 160),
  due_date date,
  due_odometer_km integer check (due_odometer_km is null or due_odometer_km >= 0),
  notify_days_before integer not null default 30 check (notify_days_before >= 0),
  completed_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  deleted_at timestamptz,
  constraint reminders_due_check check (due_date is not null or due_odometer_km is not null),
  foreign key (vehicle_id, family_id) references public.vehicles(id, family_id) on delete cascade
);

create or replace function public.ensure_maintenance_type_scope()
returns trigger
language plpgsql
as $$
begin
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

create or replace function public.ensure_optional_maintenance_type_scope()
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

create index families_created_by_idx on public.families(created_by);
create index user_profiles_user_id_idx on public.user_profiles(user_id);
create index user_profiles_family_id_idx on public.user_profiles(family_id);
create index vehicles_family_id_idx on public.vehicles(family_id);
create index vehicles_family_updated_idx on public.vehicles(family_id, updated_at);
create index maintenance_types_family_id_idx on public.maintenance_types(family_id);
create index maintenance_records_family_id_idx on public.maintenance_records(family_id);
create index maintenance_records_vehicle_date_idx on public.maintenance_records(vehicle_id, performed_on desc);
create index maintenance_records_family_updated_idx on public.maintenance_records(family_id, updated_at);
create index reminders_family_id_idx on public.reminders(family_id);
create index reminders_vehicle_due_date_idx on public.reminders(vehicle_id, due_date);
create index reminders_family_updated_idx on public.reminders(family_id, updated_at);

create trigger families_set_updated_at
before update on public.families
for each row execute function public.set_updated_at();

create trigger user_profiles_set_updated_at
before update on public.user_profiles
for each row execute function public.set_updated_at();

create trigger vehicles_set_updated_at
before update on public.vehicles
for each row execute function public.set_updated_at();

create trigger maintenance_types_set_updated_at
before update on public.maintenance_types
for each row execute function public.set_updated_at();

create trigger maintenance_records_set_updated_at
before update on public.maintenance_records
for each row execute function public.set_updated_at();

create trigger reminders_set_updated_at
before update on public.reminders
for each row execute function public.set_updated_at();

create trigger maintenance_records_type_scope
before insert or update on public.maintenance_records
for each row execute function public.ensure_maintenance_type_scope();

create trigger reminders_type_scope
before insert or update on public.reminders
for each row execute function public.ensure_optional_maintenance_type_scope();

create or replace function public.current_user_family_ids()
returns setof uuid
language sql
security definer
set search_path = public
stable
as $$
  select up.family_id
  from public.user_profiles up
  where up.user_id = auth.uid()
    and up.deleted_at is null;
$$;

create or replace function public.can_access_family(target_family_id uuid)
returns boolean
language sql
security definer
set search_path = public
stable
as $$
  select exists (
    select 1
    from public.user_profiles up
    where up.user_id = auth.uid()
      and up.family_id = target_family_id
      and up.deleted_at is null
  ) or exists (
    select 1
    from public.families f
    where f.id = target_family_id
      and f.created_by = auth.uid()
      and f.deleted_at is null
  );
$$;

alter table public.families enable row level security;
alter table public.user_profiles enable row level security;
alter table public.vehicles enable row level security;
alter table public.maintenance_types enable row level security;
alter table public.maintenance_records enable row level security;
alter table public.reminders enable row level security;

create policy families_select_own
on public.families for select
to authenticated
using (public.can_access_family(id));

create policy families_insert_own
on public.families for insert
to authenticated
with check (created_by = auth.uid());

create policy families_update_own
on public.families for update
to authenticated
using (public.can_access_family(id))
with check (public.can_access_family(id));

create policy families_delete_own
on public.families for delete
to authenticated
using (public.can_access_family(id));

create policy user_profiles_select_own_family
on public.user_profiles for select
to authenticated
using (user_id = auth.uid() or public.can_access_family(family_id));

create policy user_profiles_insert_self
on public.user_profiles for insert
to authenticated
with check (user_id = auth.uid() and public.can_access_family(family_id));

create policy user_profiles_update_own_family
on public.user_profiles for update
to authenticated
using (user_id = auth.uid() or public.can_access_family(family_id))
with check (user_id = auth.uid() or public.can_access_family(family_id));

create policy user_profiles_delete_own_family
on public.user_profiles for delete
to authenticated
using (user_id = auth.uid() or public.can_access_family(family_id));

create policy vehicles_family_access
on public.vehicles for all
to authenticated
using (public.can_access_family(family_id))
with check (public.can_access_family(family_id));

create policy maintenance_types_select_global_or_family
on public.maintenance_types for select
to authenticated
using (is_global = true or public.can_access_family(family_id));

create policy maintenance_types_insert_family
on public.maintenance_types for insert
to authenticated
with check (is_global = false and public.can_access_family(family_id));

create policy maintenance_types_update_family
on public.maintenance_types for update
to authenticated
using (is_global = false and public.can_access_family(family_id))
with check (is_global = false and public.can_access_family(family_id));

create policy maintenance_types_delete_family
on public.maintenance_types for delete
to authenticated
using (is_global = false and public.can_access_family(family_id));

create policy maintenance_records_family_access
on public.maintenance_records for all
to authenticated
using (public.can_access_family(family_id))
with check (public.can_access_family(family_id));

create policy reminders_family_access
on public.reminders for all
to authenticated
using (public.can_access_family(family_id))
with check (public.can_access_family(family_id));

insert into public.maintenance_types (code, name, is_global) values
  ('itv', 'ITV', true),
  ('insurance', 'Seguro', true),
  ('oil_change', 'Cambio de aceite', true),
  ('tires', 'Neumaticos', true),
  ('general_review', 'Revision general', true),
  ('repair', 'Averia o reparacion', true);
