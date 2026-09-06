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

- Video Settings permanece vanilla y sin capas Jobs.
- Chat, inventario, contenedores y UI normal de gameplay quedan fuera de Jobs.
- Con mundo cargado no existen transiciones Jobs ni música/ambiente de menú.
- Pausa/Config Jobs sólo conservan tematización/feedback breve permitido.
- Multiplayer mantiene padre Jobs para ESC/Cancelar, conexión, error y retorno tras servidor.
- F5/Actualizar conserva **selección por IP + scroll** sin reutilizar Entries viejas.
- `servers.dat` no se guarda al abrir/recargar si el servidor oficial ya está correcto.
- PNG 10–17 son estáticos; JPG 18–31 sólo admiten respiración sutil/desactivable.
- `musica_creditada.txt` representa Absurdism, REQUIEM y Upon the Hill V2.
- Resource reload usa generaciones y no pierde una recarga posterior.
- La música Jobs no usa fallback a `minecraft:music.menu`.
- Los FX Jobs no usan fallback a `minecraft:ambient.cave`.
- Música, camas y FX puntuales reciben hard-stop en gameplay.
- `MusicManager.stopPlaying()` no se ejecuta por tick: nueva música `SoundSource.MUSIC` se bloquea por evento durante la sesión Jobs.
- Config Jobs no escribe valores idénticos.
- Hover vanilla preservado usa una caché de botones, no un scan completo por frame.
- El JAR usa orden reproducible y `main` es la única rama entregable.

## Auditoría vigente de la entrega

- [`AUDITORIA_0.41.0_RUNTIME_MULTIPLAYER.md`](AUDITORIA_0.41.0_RUNTIME_MULTIPLAYER.md) — audio puntual, sesión idempotente, config, hover y continuidad Multiplayer.
- [`AUDITORIA_0.40.0_IDENTIDAD_MUSICAL.md`](AUDITORIA_0.40.0_IDENTIDAD_MUSICAL.md) — eliminación del fallback musical vanilla, catálogo estático y hard-stop musical.
- [`AUDITORIA_0.39.0_CREDITOS_Y_RELOAD.md`](AUDITORIA_0.39.0_CREDITOS_Y_RELOAD.md) — créditos y generaciones de reload.
- [`AUDITORIA_0.38.0_OPTIMIZACION_GLOBAL.md`](AUDITORIA_0.38.0_OPTIMIZACION_GLOBAL.md) — optimización global y build reproducible.
- [`AUDITORIA_0.37.0_CONTINUIDAD_MULTIPLAYER_Y_DOCS.md`](AUDITORIA_0.37.0_CONTINUIDAD_MULTIPLAYER_Y_DOCS.md) — continuidad F5 y documentación.
- [`AUDITORIA_0.36.0_MULTIPLAYER_Y_GAMEPLAY.md`](AUDITORIA_0.36.0_MULTIPLAYER_Y_GAMEPLAY.md) — cierre de Multiplayer y cero transiciones en gameplay.
- [`AUDITORIA_0.35.0_AUDIO_Y_RETORNO.md`](AUDITORIA_0.35.0_AUDIO_Y_RETORNO.md) — feedback Jobs y retorno contextual.

## Histórico

Los demás `AUDITORIA_*.md`, revisiones, catálogos y planes son evidencia histórica. Si contradicen `CONTEXTO.md`, `KNOWN_ISSUES.md`, `compatibilidad.md`, `musica.md` o una auditoría más reciente, manda el documento vigente más nuevo.

En particular, menciones históricas a fallback `MUSIC_MENU` o `AMBIENT_CAVE` no describen el contrato actual.
