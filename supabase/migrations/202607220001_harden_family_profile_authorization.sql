-- Harden family and profile authorization before exposing another public client.
-- Existing family/profile mappings are preserved; membership remains server-managed.

create or replace function public.current_user_family_ids()
returns setof uuid
language sql
security definer
set search_path = ''
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
set search_path = ''
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

create or replace function public.is_family_owner(target_family_id uuid)
returns boolean
language sql
security definer
set search_path = ''
stable
as $$
  select exists (
    select 1
    from public.families f
    where f.id = target_family_id
      and f.created_by = auth.uid()
  );
$$;

create or replace function public.ensure_user_profile(
  profile_display_name text,
  profile_email text default null
)
returns table (
  user_id uuid,
  family_id uuid,
  display_name text,
  email text
)
language plpgsql
security definer
set search_path = ''
as $$
declare
  current_user_id uuid := auth.uid();
  existing_profile public.user_profiles%rowtype;
  created_family_id uuid;
  safe_display_name text := nullif(pg_catalog.btrim(profile_display_name), '');
begin
  if current_user_id is null then
    raise exception using
      errcode = '42501',
      message = 'Cannot ensure profile without an authenticated user';
  end if;

  select *
  into existing_profile
  from public.user_profiles up
  where up.user_id = current_user_id
    and up.deleted_at is null
  limit 1;

  if found then
    return query
      select
        existing_profile.user_id,
        existing_profile.family_id,
        existing_profile.display_name,
        existing_profile.email;
    return;
  end if;

  if safe_display_name is null then
    safe_display_name := 'Usuario';
  end if;

  insert into public.families (name, created_by)
  values ('Familia de ' || safe_display_name, current_user_id)
  returning id into created_family_id;

  insert into public.user_profiles (user_id, family_id, display_name, email)
  values (current_user_id, created_family_id, safe_display_name, profile_email);

  return query
    select
      current_user_id,
      created_family_id,
      safe_display_name,
      profile_email;
end;
$$;

revoke all on function public.current_user_family_ids() from public;
revoke all on function public.current_user_family_ids() from anon;
revoke all on function public.can_access_family(uuid) from public;
revoke all on function public.can_access_family(uuid) from anon;
revoke all on function public.is_family_owner(uuid) from public;
revoke all on function public.is_family_owner(uuid) from anon;
revoke all on function public.ensure_user_profile(text, text) from public;
revoke all on function public.ensure_user_profile(text, text) from anon;

grant execute on function public.current_user_family_ids() to authenticated;
grant execute on function public.can_access_family(uuid) to authenticated;
grant execute on function public.is_family_owner(uuid) to authenticated;
grant execute on function public.ensure_user_profile(text, text) to authenticated;

drop policy if exists families_insert_own on public.families;
drop policy if exists families_update_own on public.families;
drop policy if exists families_delete_own on public.families;
drop policy if exists user_profiles_insert_self on public.user_profiles;
drop policy if exists user_profiles_update_own_family on public.user_profiles;
drop policy if exists user_profiles_delete_own_family on public.user_profiles;

create policy families_update_owner
on public.families for update
to authenticated
using (created_by = auth.uid())
with check (created_by = auth.uid());

create policy families_delete_owner
on public.families for delete
to authenticated
using (created_by = auth.uid());

create policy user_profiles_update_self_or_owner
on public.user_profiles for update
to authenticated
using (
  user_id = auth.uid()
  or public.is_family_owner(family_id)
)
with check (
  user_id = auth.uid()
  or public.is_family_owner(family_id)
);

revoke all on table public.families from anon;
revoke all on table public.user_profiles from anon;
revoke all on table public.families from authenticated;
revoke all on table public.user_profiles from authenticated;

grant select, delete on table public.families to authenticated;
grant update (name, invite_code, deleted_at) on table public.families to authenticated;
grant select on table public.user_profiles to authenticated;
grant update (display_name, email) on table public.user_profiles to authenticated;
