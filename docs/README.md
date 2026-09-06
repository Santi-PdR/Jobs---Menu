# Índice de documentación — Jobs Menu

Este directorio contiene tanto el **contrato vigente** como auditorías históricas. Una auditoría antigua explica cómo era el mod en ese momento; no debe usarse para revertir decisiones posteriores.

## Documentación vigente

Para trabajar sobre `main`, leer en este orden:

1. [`../CONTEXTO.md`](../CONTEXTO.md) — fuente maestra de alcance, reglas duras y estado actual.
2. [`../README.md`](../README.md) — resumen operativo de la entrega vigente.
3. [`../KNOWN_ISSUES.md`](../KNOWN_ISSUES.md) — riesgos reales y límites de CI.
4. [`../CHANGELOG.md`](../CHANGELOG.md) — evolución por versión.
5. [`checklist-manual.md`](checklist-manual.md) — aceptación dentro de Minecraft.
6. [`compatibilidad.md`](compatibilidad.md) — fronteras con vanilla/Forge, gameplay y otros mods.
7. [`DESPLIEGUE.md`](DESPLIEGUE.md) — flujo de build/release/instalación.
8. [`musica.md`](musica.md) — catálogo, créditos y lifecycle de audio.
9. [`FONDOS_18_31.md`](FONDOS_18_31.md) — asignación de JPG 18–31.

## Contratos que no deben regredir

- **Gráficos no se tematiza ni se reconstruye con Jobs.**
- `PantallaOpcionesJobs` es una `Screen` propia, no un `OptionsScreen` con controles ocultos.
- Con Embeddium, Gráficos abre la `ConfigScreenFactory` registrada por el propio mod; sin Embeddium usa `VideoSettingsScreen` vanilla.
- Jobs no enlaza clases internas de Embeddium/Sodium ni usa reflection para Gráficos.
- **No existe botón MODPACK** ni flujo alternativo de Options completo.
- Cualquier `Screen` de terceros queda sin chrome, transición, hover/click ni recolocación Jobs.
- La navegación iniciada por una Screen de terceros no se redirige sólo porque la sesión siga activa.
- `SesionMenu.activa()` no autoriza por sí sola sustituciones administrativas; el origen debe ser una pantalla Jobs concreta.
- Los perfiles sólo se identifican como preset cuando todos los valores controlados coinciden; de lo contrario se muestra `CUSTOM`.
- En Mundos/Mods, ESC limpia búsqueda y foco antes de salir, y el cierre es idempotente.
- Chat, inventario, contenedores y UI normal de gameplay quedan fuera de Jobs.
- Con mundo cargado no existen transiciones Jobs ni música/ambiente de menú.
- Multiplayer mantiene padre Jobs para ESC/Cancelar, conexión, error y retorno tras servidor.
- F5/Actualizar y resize conservan **selección por IP + scroll** sin reutilizar Entries viejas.
- `servers.dat` no se guarda al abrir/recargar si el servidor oficial ya está correcto.
- PNG 10–17 son estáticos; JPG 18–31 sólo admiten respiración sutil/desactivable.
- `musica_creditada.txt` representa Absurdism, REQUIEM y Upon the Hill V2.
- Resource reload usa generaciones y no pierde una recarga posterior.
- La música Jobs no usa fallback a `minecraft:music.menu`.
- Los FX Jobs no usan fallback a `minecraft:ambient.cave`.
- Música, camas y FX puntuales reciben hard-stop en gameplay.
- Config Jobs no escribe valores idénticos.
- Hover vanilla preservado usa caché de botones.
- `dev-latest` debe apuntar al mismo SHA de `main` que publicó el JAR.

## Auditoría vigente de la entrega

- [`AUDITORIA_0.44.0_GRAFICOS_Y_NAVEGACION.md`](AUDITORIA_0.44.0_GRAFICOS_Y_NAVEGACION.md) — Gráficos intocable, eliminación de MODPACK y redirecciones administrativas acotadas.
- [`AUDITORIA_0.43.0_UX_NAVEGACION.md`](AUDITORIA_0.43.0_UX_NAVEGACION.md) — perfiles exactos, búsqueda/ESC y cierre idempotente.
- [`AUDITORIA_0.42.0_COMPATIBILIDAD_TERCEROS.md`](AUDITORIA_0.42.0_COMPATIBILIDAD_TERCEROS.md) — origen del aislamiento genérico y publicación consistente.
- [`AUDITORIA_0.41.0_RUNTIME_MULTIPLAYER.md`](AUDITORIA_0.41.0_RUNTIME_MULTIPLAYER.md) — audio puntual, sesión idempotente, config, hover y continuidad Multiplayer.
- [`AUDITORIA_0.40.0_IDENTIDAD_MUSICAL.md`](AUDITORIA_0.40.0_IDENTIDAD_MUSICAL.md) — identidad musical y hard-stop.
- [`AUDITORIA_0.39.0_CREDITOS_Y_RELOAD.md`](AUDITORIA_0.39.0_CREDITOS_Y_RELOAD.md) — créditos y generaciones de reload.
- [`AUDITORIA_0.38.0_OPTIMIZACION_GLOBAL.md`](AUDITORIA_0.38.0_OPTIMIZACION_GLOBAL.md) — optimización global y build reproducible.
- [`AUDITORIA_0.37.0_CONTINUIDAD_MULTIPLAYER_Y_DOCS.md`](AUDITORIA_0.37.0_CONTINUIDAD_MULTIPLAYER_Y_DOCS.md) — continuidad F5 y documentación.

## Histórico

Los demás `AUDITORIA_*.md`, revisiones, catálogos y planes son evidencia histórica. Si contradicen `CONTEXTO.md`, `KNOWN_ISSUES.md`, `compatibilidad.md` o una auditoría más reciente, manda el documento vigente más nuevo.

En particular, el `OptionsScreen` oculto, la detección gráfica por ranura y el botón MODPACK de 0.41.1/0.42 son históricos y **no describen el contrato actual**.
