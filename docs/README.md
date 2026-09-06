# Índice de documentación — Jobs Menu

Este directorio contiene el **contrato vigente** y auditorías históricas. Si una auditoría antigua contradice el estado actual, manda `CONTEXTO.md` y la auditoría más reciente.

## Documentación vigente

1. [`../CONTEXTO.md`](../CONTEXTO.md) — fuente maestra de alcance y reglas duras.
2. [`../README.md`](../README.md) — resumen operativo de la entrega vigente.
3. [`../KNOWN_ISSUES.md`](../KNOWN_ISSUES.md) — riesgos reales y límites de CI.
4. [`../CHANGELOG.md`](../CHANGELOG.md) — historial de versiones.
5. [`checklist-manual.md`](checklist-manual.md) — aceptación dentro de Minecraft.
6. [`compatibilidad.md`](compatibilidad.md) — fronteras con vanilla/Forge, gameplay y otros mods.
7. [`DESPLIEGUE.md`](DESPLIEGUE.md) — build/release/instalación.
8. [`musica.md`](musica.md) — catálogo, créditos y lifecycle de audio.
9. [`FONDOS_18_31.md`](FONDOS_18_31.md) — asignación de JPG 18–31.

## Contratos que no deben regredir

- **0.46.0:** Idioma y Force Unicode Font se aplican juntos mediante una sola transacción de resource reload; ante fallo se revierten juntos.
- Un callback tardío de Idioma no navega si `PantallaIdiomaJobs` ya no es la Screen activa.
- El buscador de Ajustes usa navegación explícita de categoría; no simula teclas 1–6 contra su padre.
- El buscador conserva filtro/foco/scroll y evita traducciones/formato repetido por frame.
- Config recuerda la última categoría de sesión y muestra `CUSTOM` si ningún preset coincide.
- Resource Packs no puede devolver a Opciones Jobs si ya se abandonó `PantallaPaquetesJobs`.
- Ajustes, Idioma, Mundos y Mods conservan estado relevante durante resize.
- Sonido cachea el `Field` reflectivo de `OptionsList`.
- **Gráficos no se tematiza ni se reconstruye con Jobs.**
- `PantallaOpcionesJobs` es una `Screen` propia; Embeddium abre su factory registrada y sin Embeddium se usa `VideoSettingsScreen` vanilla.
- **No existe MODPACK.**
- Pantallas de terceros y sus subflujos quedan fuera de chrome, transición, hover/click y recolocación Jobs.
- `SesionMenu.activa()` no autoriza por sí sola redirecciones administrativas.
- Chat, inventario, contenedores y UI normal de gameplay quedan fuera de Jobs; con mundo cargado no existen transiciones Jobs ni audio de menú.
- Multiplayer mantiene padre Jobs, selección por IP + scroll en F5/resize y no guarda `servers.dat` sin cambios.
- PNG 10–17 son estáticos; JPG 18–31 sólo admiten respiración mínima/desactivable.
- Música Jobs no usa `minecraft:music.menu`; FX Jobs no usan `minecraft:ambient.cave`; música/camas/FX reciben hard-stop en gameplay.
- `dev-latest` debe apuntar al mismo SHA de `main` que publicó el JAR.

## Auditoría vigente de la entrega

- [`AUDITORIA_0.46.0_LIFECYCLE_IDIOMA_Y_BUSQUEDA.md`](AUDITORIA_0.46.0_LIFECYCLE_IDIOMA_Y_BUSQUEDA.md) — transacción Idioma/Unicode, callback tardío, navegación explícita y hot path del buscador.
- [`AUDITORIA_0.45.0_CALIDAD_GLOBAL.md`](AUDITORIA_0.45.0_CALIDAD_GLOBAL.md) — búsqueda transversal, continuidad de resize, cierres y callbacks.
- [`AUDITORIA_0.44.0_GRAFICOS_Y_NAVEGACION.md`](AUDITORIA_0.44.0_GRAFICOS_Y_NAVEGACION.md) — Gráficos intocable, eliminación de MODPACK y redirecciones acotadas.
- [`AUDITORIA_0.43.0_UX_NAVEGACION.md`](AUDITORIA_0.43.0_UX_NAVEGACION.md) — perfiles exactos y navegación robusta.
- [`AUDITORIA_0.42.0_COMPATIBILIDAD_TERCEROS.md`](AUDITORIA_0.42.0_COMPATIBILIDAD_TERCEROS.md) — aislamiento genérico de terceros.
- [`AUDITORIA_0.41.0_RUNTIME_MULTIPLAYER.md`](AUDITORIA_0.41.0_RUNTIME_MULTIPLAYER.md) — audio puntual, sesión, config y Multiplayer.
- [`AUDITORIA_0.40.0_IDENTIDAD_MUSICAL.md`](AUDITORIA_0.40.0_IDENTIDAD_MUSICAL.md) — identidad musical y hard-stop.
- [`AUDITORIA_0.39.0_CREDITOS_Y_RELOAD.md`](AUDITORIA_0.39.0_CREDITOS_Y_RELOAD.md) — créditos y generaciones de reload.
- [`AUDITORIA_0.38.0_OPTIMIZACION_GLOBAL.md`](AUDITORIA_0.38.0_OPTIMIZACION_GLOBAL.md) — optimización global.
- [`AUDITORIA_0.37.0_CONTINUIDAD_MULTIPLAYER_Y_DOCS.md`](AUDITORIA_0.37.0_CONTINUIDAD_MULTIPLAYER_Y_DOCS.md) — continuidad F5 y documentación.

## Histórico

El `OptionsScreen` oculto, detección gráfica por ranura y botón MODPACK de 0.41.1/0.42 son históricos y **no describen el contrato vigente**.
