-- First-login bootstrap for authenticated users.
-- Creates a personal family and profile in one server-side operation.

grant usage on schema public to authenticated;

grant select, insert, update, delete on table public.families to authenticated;
grant select, insert, update, delete on table public.user_profiles to authenticated;
grant select, insert, update, delete on table public.vehicles to authenticated;
grant select, insert, update, delete on table public.maintenance_types to authenticated;
grant select, insert, update, delete on table public.maintenance_records to authenticated;
grant select, insert, update, delete on table public.reminders to authenticated;

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
set search_path = public
as $$
declare
  current_user_id uuid := auth.uid();
  existing_profile public.user_profiles%rowtype;
  created_family_id uuid;
  safe_display_name text := nullif(trim(profile_display_name), '');
begin
  if current_user_id is null then
    raise exception 'Cannot ensure profile without an authenticated user';
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

revoke all on function public.ensure_user_profile(text, text) from public;
grant execute on function public.ensure_user_profile(text, text) to authenticated;

grant execute on function public.current_user_family_ids() to authenticated;
grant execute on function public.can_access_family(uuid) to authenticated;
