\set ON_ERROR_STOP on

begin;

create function pg_temp.expect_denied(command text)
returns void
language plpgsql
as $$
begin
  execute command;
  raise exception 'Expected authorization denial: %', command;
exception
  when insufficient_privilege or check_violation then
    null;
end;
$$;

create function pg_temp.expect_affected_rows(command text, expected_rows bigint)
returns void
language plpgsql
as $$
declare
  affected_rows bigint;
begin
  execute command;
  get diagnostics affected_rows = row_count;
  if affected_rows <> expected_rows then
    raise exception 'Expected % affected rows, got %: %', expected_rows, affected_rows, command;
  end if;
end;
$$;

insert into auth.users (id, aud, role, email) values
  ('10000000-0000-0000-0000-000000000001', 'authenticated', 'authenticated', 'owner-a@example.test'),
  ('10000000-0000-0000-0000-000000000002', 'authenticated', 'authenticated', 'member-a@example.test'),
  ('10000000-0000-0000-0000-000000000003', 'authenticated', 'authenticated', 'owner-b@example.test'),
  ('10000000-0000-0000-0000-000000000004', 'authenticated', 'authenticated', 'new-user@example.test');

insert into public.families (id, name, created_by) values
  ('20000000-0000-0000-0000-000000000001', 'Family A', '10000000-0000-0000-0000-000000000001'),
  ('20000000-0000-0000-0000-000000000002', 'Family B', '10000000-0000-0000-0000-000000000003');

insert into public.user_profiles (id, user_id, family_id, display_name, email) values
  ('30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Owner A', 'owner-a@example.test'),
  ('30000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', 'Member A', 'member-a@example.test'),
  ('30000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002', 'Owner B', 'owner-b@example.test');

insert into public.vehicles (id, family_id, name, vehicle_type) values
  ('family-b-vehicle', '20000000-0000-0000-0000-000000000002', 'Foreign vehicle', 'car');

insert into public.maintenance_types (id, family_id, name, is_global) values
  ('40000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'Foreign type', false);

insert into public.maintenance_records (id, family_id, vehicle_id, performed_on) values
  ('family-b-record', '20000000-0000-0000-0000-000000000002', 'family-b-vehicle', current_date);

insert into public.reminders (id, family_id, vehicle_id, title, due_date) values
  ('family-b-reminder', '20000000-0000-0000-0000-000000000002', 'family-b-vehicle', 'Foreign reminder', current_date);

set local role authenticated;
select set_config('request.jwt.claim.sub', '10000000-0000-0000-0000-000000000002', true);

-- A member can change only their own safe profile fields.
select pg_temp.expect_affected_rows(
  $sql$update public.user_profiles set display_name = 'Member updated' where user_id = auth.uid()$sql$,
  1
);
select pg_temp.expect_denied(
  $sql$update public.user_profiles set family_id = '20000000-0000-0000-0000-000000000002' where user_id = auth.uid()$sql$
);
select pg_temp.expect_denied(
  $sql$update public.user_profiles set user_id = '10000000-0000-0000-0000-000000000003' where user_id = auth.uid()$sql$
);
select pg_temp.expect_denied(
  $sql$insert into public.user_profiles (user_id, family_id, display_name) values ('10000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', 'Injected')$sql$
);
select pg_temp.expect_denied(
  $sql$delete from public.user_profiles where user_id = auth.uid()$sql$
);

-- Membership does not confer profile or family administration.
select pg_temp.expect_denied(
  $sql$insert into public.families (name, created_by) values ('Injected family', auth.uid())$sql$
);
select pg_temp.expect_affected_rows(
  $sql$update public.user_profiles set display_name = 'Compromised' where user_id = '10000000-0000-0000-0000-000000000001'$sql$,
  0
);
select pg_temp.expect_affected_rows(
  $sql$update public.families set name = 'Compromised' where id = '20000000-0000-0000-0000-000000000001'$sql$,
  0
);
select pg_temp.expect_affected_rows(
  $sql$delete from public.families where id = '20000000-0000-0000-0000-000000000001'$sql$,
  0
);

-- A supplied foreign family ID never grants product access.
do $$
begin
  if exists (select 1 from public.vehicles where family_id = '20000000-0000-0000-0000-000000000002')
    or exists (select 1 from public.maintenance_types where family_id = '20000000-0000-0000-0000-000000000002')
    or exists (select 1 from public.maintenance_records where family_id = '20000000-0000-0000-0000-000000000002')
    or exists (select 1 from public.reminders where family_id = '20000000-0000-0000-0000-000000000002') then
    raise exception 'Cross-family product rows were visible';
  end if;
end;
$$;

select pg_temp.expect_denied(
  $sql$insert into public.vehicles (id, family_id, name, vehicle_type) values ('foreign-insert', '20000000-0000-0000-0000-000000000002', 'Injected', 'car')$sql$
);
select pg_temp.expect_denied(
  $sql$insert into public.maintenance_types (id, family_id, name, is_global) values ('40000000-0000-0000-0000-000000000099', '20000000-0000-0000-0000-000000000002', 'Injected', false)$sql$
);
select pg_temp.expect_denied(
  $sql$insert into public.maintenance_records (id, family_id, vehicle_id, performed_on) values ('foreign-record', '20000000-0000-0000-0000-000000000002', 'family-b-vehicle', current_date)$sql$
);
select pg_temp.expect_denied(
  $sql$insert into public.reminders (id, family_id, vehicle_id, title, due_date) values ('foreign-reminder', '20000000-0000-0000-0000-000000000002', 'family-b-vehicle', 'Injected', current_date)$sql$
);
select pg_temp.expect_affected_rows(
  $sql$update public.vehicles set name = 'Compromised' where id = 'family-b-vehicle'$sql$,
  0
);
select pg_temp.expect_affected_rows(
  $sql$update public.maintenance_types set name = 'Compromised' where id = '40000000-0000-0000-0000-000000000002'$sql$,
  0
);
select pg_temp.expect_affected_rows(
  $sql$update public.maintenance_records set notes = 'Compromised' where id = 'family-b-record'$sql$,
  0
);
select pg_temp.expect_affected_rows(
  $sql$update public.reminders set title = 'Compromised' where id = 'family-b-reminder'$sql$,
  0
);
select pg_temp.expect_affected_rows($sql$delete from public.reminders where id = 'family-b-reminder'$sql$, 0);
select pg_temp.expect_affected_rows($sql$delete from public.maintenance_records where id = 'family-b-record'$sql$, 0);
select pg_temp.expect_affected_rows($sql$delete from public.maintenance_types where id = '40000000-0000-0000-0000-000000000002'$sql$, 0);
select pg_temp.expect_affected_rows($sql$delete from public.vehicles where id = 'family-b-vehicle'$sql$, 0);

-- Same-family product operations remain available.
select pg_temp.expect_affected_rows(
  $sql$insert into public.vehicles (id, family_id, name, vehicle_type) values ('family-a-vehicle', '20000000-0000-0000-0000-000000000001', 'Own vehicle', 'car')$sql$,
  1
);
select pg_temp.expect_affected_rows($sql$update public.vehicles set name = 'Own updated' where id = 'family-a-vehicle'$sql$, 1);
select pg_temp.expect_affected_rows($sql$delete from public.vehicles where id = 'family-a-vehicle'$sql$, 1);

-- The creator can administer the family and safe fields of its profiles.
select set_config('request.jwt.claim.sub', '10000000-0000-0000-0000-000000000001', true);
select pg_temp.expect_affected_rows(
  $sql$update public.families set name = 'Family A updated' where id = '20000000-0000-0000-0000-000000000001'$sql$,
  1
);
select pg_temp.expect_denied(
  $sql$update public.families set created_by = '10000000-0000-0000-0000-000000000002' where id = '20000000-0000-0000-0000-000000000001'$sql$
);
select pg_temp.expect_affected_rows(
  $sql$update public.user_profiles set display_name = 'Member administered' where user_id = '10000000-0000-0000-0000-000000000002'$sql$,
  1
);

-- Provisioning remains available only through the authenticated RPC.
select set_config('request.jwt.claim.sub', '10000000-0000-0000-0000-000000000004', true);
select * from public.ensure_user_profile('New User', 'new-user@example.test');

do $$
begin
  if not exists (
    select 1
    from public.user_profiles up
    join public.families f on f.id = up.family_id
    where up.user_id = auth.uid()
      and f.created_by = auth.uid()
  ) then
    raise exception 'ensure_user_profile did not provision the authenticated user';
  end if;
end;
$$;

select pg_temp.expect_affected_rows(
  $sql$delete from public.families where id = (select family_id from public.user_profiles where user_id = auth.uid())$sql$,
  1
);

rollback;
