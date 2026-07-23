#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
container="carbura-rls-test-$$"

cleanup() {
  docker rm -f "$container" >/dev/null 2>&1 || true
}
trap cleanup EXIT

docker run --name "$container" \
  --env POSTGRES_PASSWORD=carbura-test \
  --volume "$repo_root:/workspace:ro" \
  --detach postgres:16-alpine >/dev/null

until docker exec "$container" pg_isready --username postgres >/dev/null 2>&1; do
  sleep 1
done

docker exec "$container" psql \
  --username postgres \
  --set ON_ERROR_STOP=1 \
  --file /workspace/supabase/tests/bootstrap.sql

for migration in "$repo_root"/supabase/migrations/*.sql; do
  docker exec "$container" psql \
    --username postgres \
    --set ON_ERROR_STOP=1 \
    --file "/workspace/${migration#"$repo_root"/}"
done

docker exec "$container" psql \
  --username postgres \
  --set ON_ERROR_STOP=1 \
  --file /workspace/supabase/tests/rls_hardening.sql

printf 'RLS hardening tests passed.\n'
