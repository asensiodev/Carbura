alter table public.vehicles
    add column if not exists next_itv_date date,
    add column if not exists insurance_renewal_date date,
    add column if not exists next_service_odometer_km integer;

alter table public.vehicles
    drop constraint if exists vehicles_next_service_odometer_km_check;

alter table public.vehicles
    add constraint vehicles_next_service_odometer_km_check
    check (next_service_odometer_km is null or next_service_odometer_km >= 0);
