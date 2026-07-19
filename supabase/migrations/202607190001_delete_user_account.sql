-- Authenticated, transactional account deletion.
-- The caller can delete only the identity represented by auth.uid().

create or replace function public.delete_current_user_account()
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
  current_user_id uuid := auth.uid();
  current_family_id uuid;
  replacement_owner_id uuid;
  owned_family record;
  remaining_member_count bigint;
  deleted_user_count bigint;
begin
  if current_user_id is null then
    raise exception using
      errcode = '42501',
      message = 'Account deletion requires an authenticated user';
  end if;

  select up.family_id
  into current_family_id
  from public.user_profiles up
  where up.user_id = current_user_id
    and up.deleted_at is null
  limit 1;

  if current_family_id is not null then
    perform 1
    from public.families f
    where f.id = current_family_id
    for update;

    perform 1
    from public.user_profiles up
    where up.family_id = current_family_id
      and up.deleted_at is null
    for update;

    select count(*)
    into remaining_member_count
    from public.user_profiles up
    where up.family_id = current_family_id
      and up.user_id <> current_user_id
      and up.deleted_at is null;

    if remaining_member_count > 0 then
      if exists (
        select 1
        from public.families f
        where f.id = current_family_id
          and f.created_by = current_user_id
      ) then
        select up.user_id
        into replacement_owner_id
        from public.user_profiles up
        where up.family_id = current_family_id
          and up.user_id <> current_user_id
          and up.deleted_at is null
        order by up.created_at, up.id
        limit 1;

        update public.families
        set created_by = replacement_owner_id
        where id = current_family_id;
      end if;

      delete from public.user_profiles
      where user_id = current_user_id;
    else
      delete from public.reminders
      where family_id = current_family_id;

      delete from public.maintenance_records
      where family_id = current_family_id;

      delete from public.vehicles
      where family_id = current_family_id;

      delete from public.maintenance_types
      where family_id = current_family_id;

      delete from public.user_profiles
      where family_id = current_family_id;

      delete from public.families
      where id = current_family_id;
    end if;
  end if;

  for owned_family in
    select f.id
    from public.families f
    where f.created_by = current_user_id
    for update
  loop
    select up.user_id
    into replacement_owner_id
    from public.user_profiles up
    where up.family_id = owned_family.id
      and up.user_id <> current_user_id
      and up.deleted_at is null
    order by up.created_at, up.id
    limit 1;

    if replacement_owner_id is not null then
      update public.families
      set created_by = replacement_owner_id
      where id = owned_family.id;
    else
      delete from public.reminders
      where family_id = owned_family.id;

      delete from public.maintenance_records
      where family_id = owned_family.id;

      delete from public.vehicles
      where family_id = owned_family.id;

      delete from public.maintenance_types
      where family_id = owned_family.id;

      delete from public.user_profiles
      where family_id = owned_family.id;

      delete from public.families
      where id = owned_family.id;
    end if;
  end loop;

  delete from auth.users
  where id = current_user_id;

  get diagnostics deleted_user_count = row_count;
  if deleted_user_count <> 1 then
    raise exception 'Authenticated user could not be deleted';
  end if;
end;
$$;

revoke all on function public.delete_current_user_account() from public;
revoke all on function public.delete_current_user_account() from anon;
grant execute on function public.delete_current_user_account() to authenticated;
