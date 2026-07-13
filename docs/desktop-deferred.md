# Desktop diferido en Entrega 2

Desktop permanece en la vision multiplataforma de Carbura, pero se difirio en Entrega 2 para estabilizar el MVP Android. iOS puede evaluarse en una fase posterior.

Motivo:

- Entrega 2 prioriza un MVP Android conectado al backend y la base de datos.
- Desktop no debe bloquear Gradle, la compilacion Android ni la entrega de capacidades.
- Los modulos compartidos `core:*`, `feature:*` y `app:shared` permiten incorporar despues un shell `app:desktop`.

Condiciones para retomarlo:

- La compilacion Android permanece estable.
- Los flujos de vehiculos, mantenimiento, recordatorios, auth y sync estan cerrados funcionalmente.
- El alcance disponible permite avanzar en Desktop sin poner en riesgo CI, release ni evidencias E2E.
