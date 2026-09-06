# Auditoría 0.43.0 — UX y navegación

## Objetivo

Reducir estados engañosos y cerrar rutas de navegación que todavía podían sentirse inconsistentes después del aislamiento externo de 0.42.0.

## Perfiles

Antes, `PerfilesJobs.actual()` reconocía perfiles con heurísticas demasiado amplias. Una configuración personalizada podía seguir mostrándose como Equilibrado, Inmersivo o Rendimiento aunque varios valores del preset ya hubieran cambiado.

0.43.0 reemplaza esas heurísticas por comparadores explícitos por preset. Se comprueban únicamente los campos que cada perfil escribe. Si cualquiera de esos campos deja de coincidir, el resultado pasa a `CUSTOM`. Opciones no controladas por el preset, como pista musical o nivel fijo, permanecen libres.

## Mundos y Mods

Las dos pantallas conservan su lógica vanilla/Forge, pero unifican el comportamiento de búsqueda:

- `Ctrl+F` enfoca el filtro;
- `ESC` con texto lo limpia;
- `ESC` con filtro vacío abandona el foco;
- el siguiente `ESC` vuelve al padre.

Ambas rutas de cierre incorporan un guard `cerrando` para que un mismo gesto no produzca dos llamadas a `setScreen()`.

## Subflujos externos

0.42 ya marcaba como externo un flujo nacido desde MODPACK o una Screen de terceros. 0.43 extiende esa frontera a dos sustituciones globales que todavía podían escapar al guard:

- `TitleScreen` no se reemplaza por `PantallaNivel` mientras el subflujo siga externo;
- una `PauseScreen` vanilla tampoco se sustituye por `PantallaEstancia` dentro de ese flujo.

Los retornos reales de gameplay conservan prioridad y siguen llevando al destino Jobs correcto.

## Verificación

`tools/verificar_ux_043.py` protege:

- existencia de matchers exactos para los cinco perfiles;
- ausencia de heurísticas antiguas de identificación;
- contrato de búsqueda/ESC y cierre idempotente de Mundos/Mods;
- guard de `flujoExternoActual` para TitleScreen y pausa.

CI ejecuta este verificador antes del build Forge Java 17.

## Aceptación manual prioritaria

1. Aplicar cada perfil, modificar un valor controlado y comprobar que aparece `CUSTOM`.
2. Probar `Ctrl+F` y la secuencia de ESC en Mundos y Mods.
3. Desde MODPACK o una GUI externa, abrir submenús vanilla y `TitleScreen` y confirmar que Jobs no secuestra el flujo.
4. Confirmar que arranque normal, salida de mundo y salida de servidor siguen regresando a sus pantallas Jobs correspondientes.
