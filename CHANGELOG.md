# Registro de cambios

## 0.41.0 — Runtime, audio y continuidad Multiplayer — 2026-09-05

### Audio / lifecycle

- Se añade `RastreadorAudioJobs` para conservar los FX puntuales Jobs mientras están activos y aplicarles `SoundManager.stop` al cerrar la visita o entrar a gameplay.
- El rastreador purga referencias finalizadas mediante `SoundManager.isActive` antes de añadir nuevas instancias.
- `MezclaAudio.ambiental()` deja de usar `SoundEvents.AMBIENT_CAVE` como respaldo: un registro Jobs faltante se omite en silencio.
- `RecargaRecursosCliente` invalida también FX puntuales y estado de mezcla después de reconstruir recursos.
- `SesionMenu.cerrar()` se vuelve idempotente: no repite un cierre completo en cada tick de gameplay si ya no existe estado Jobs vivo.

### Música vanilla

- `GestorMusica.atender()` deja de llamar `MusicManager.stopPlaying()` cada tick.
- Una visita nueva corta el MusicManager una vez.
- Nuevo `BloqueoMusicaVanillaJobs` cancela nuevas instancias `SoundSource.MUSIC` durante la sesión Jobs, manteniendo la banda sonora del menú exclusiva sin polling de stop.

### Multiplayer

- F5/Actualizar conserva selección por IP y posición de scroll de `ServerSelectionList`.
- La lista reconstruida restaura una Entry nueva y después aplica `setScrollAmount()`.
- `ServerList.save()` sólo se ejecuta cuando la normalización del servidor oficial realmente cambia nombre, duplicados, posición o alta/baja.

### Config / rendimiento UI

- Setters boolean/int omiten valores idénticos y no programan escrituras TOML innecesarias.
- Perfil accesible modifica sólo los campos que requieren cambio.
- Se añaden contadores internos de cambios aplicados/omitidos/guardados para diagnóstico.
- Hover de controles vanilla preservados usa una caché de `AbstractButton` reconstruida al inicializar/cambiar Screen, evitando recorrer todos los hijos por frame.

### Diagnóstico y CI

- Diagnóstico oculto añade estado interno/cierres de sesión, FX puntuales activos/registrados/purgados y métricas de config.
- Nuevo `tools/verificar_runtime_041.py` protege audio puntual, cierre idempotente, bloqueo MUSIC, config, hover y Multiplayer.
- README, CONTEXTO, KNOWN_ISSUES, checklist, compatibilidad, música, despliegue e índice documental se sincronizan con 0.41.0.
- Nueva auditoría `docs/AUDITORIA_0.41.0_RUNTIME_MULTIPLAYER.md`.
- Versión: **0.41.0**.
- Artefacto esperado: **`jobsmenu-0.41.0.jar`**.

## 0.40.0 — Identidad musical y hard-stop reforzado — 2026-09-05

### Música

- `GestorMusica` deja de usar `SoundEvents.MUSIC_MENU` como fallback. Una pista Jobs faltante se omite/reintenta sin reproducir música vanilla.
- El catálogo de Absurdism, REQUIEM y Upon the Hill V2 pasa a `CATALOGO`, construido una sola vez por JVM.
- `catalogo()` devuelve la misma estructura y título/autor/cantidad consultan el catálogo estático, eliminando arrays repetidos durante sesión/HUD/crossfade.
- Los cambios fijo/manual/automático resuelven primero la pista entrante; la actual no empieza a retirarse si la nueva no pudo crearse.
- El aviso de pista faltante se limita a una advertencia por visita/reload.

### Corte de audio

- El hard-stop inmediato pone volumen y ganancias a cero, marca la instancia con `stop()` y ordena además `SoundManager.stop(instance)`.
- Gameplay, cierre de sesión y resource reload conservan frontera dura sin fade residual.

### Calidad

- Nuevo `tools/verificar_audio_identidad.py` bloquea fallback vanilla, recreación del catálogo y pérdida del stop directo.
- Versión: **0.40.0**; artefacto **`jobsmenu-0.40.0.jar`**.

## 0.39.0 — Créditos musicales y resource reload — 2026-09-05

- Se restaura `assets/jobsmenu/musica_creditada.txt` con `absurdism`, `requiem` y `upon_the_hill_v2`.
- Resource reload usa `AtomicLong GENERACION` y reprograma si llega otra generación durante el cierre.
- `SesionMenu.abrir()` corta reaperturas de una visita ya activa.
- Diagnóstico oculto añade pista dominante y generación de reload.
- Nuevo `tools/verificar_reload_creditos.py`.
- Versión: **0.39.0**; artefacto **`jobsmenu-0.39.0.jar`**.

## 0.38.0 — Optimización global — 2026-09-05

- Reflection/listas cacheadas por clase/Screen, scrollbar deduplicada por frame y cachés liberadas al cerrar.
- Fondos aplican filtrado por objeto de textura.
- Menos asignaciones/recorridos en UI, avisos, Multiplayer y rotación.
- Bajo consumo reduce draw calls reales.
- JAR reproducible sin timestamp variable.
- Nuevo `tools/verificar_optimizacion.py`.
- Versión: **0.38.0**; artefacto **`jobsmenu-0.38.0.jar`**.

## 0.37.0 — Continuidad de Multiplayer y documentación — 2026-09-05

- F5/Actualizar conserva IP seleccionada y restaura una Entry nueva.
- Guard `cerrando` impide reconstrucciones repetidas.
- F5 emite `UI_ALTERNAR` Jobs.
- Se crea `docs/README.md` y `tools/verificar_continuidad.py`.
- Versión: **0.37.0**; artefacto **`jobsmenu-0.37.0.jar`**.

## 0.36.0 — Cierre fiable de Multiplayer y cero transiciones en gameplay — 2026-09-05

- ESC/Cancelar convergen en `cerrarAlPadre()` sin `super.onClose()`/`popGuiLayer()`.
- F5 reconstruye directamente Jobs.
- Con `Minecraft.level != null` no se crea ni dibuja transición Jobs.
- Versión: **0.36.0**; artefacto **`jobsmenu-0.36.0.jar`**.

## 0.35.0 — Feedback Jobs y retorno contextual tras servidor — 2026-09-05

- Click/hover Jobs funciona también en controles vanilla preservados de Pausa/Config Jobs.
- Sesión remota vuelve a Multiplayer Jobs; mundo local al main Jobs.
- Versión: **0.35.0**; artefacto **`jobsmenu-0.35.0.jar`**.

## Histórico anterior

Las versiones 0.34.0 y anteriores permanecen documentadas en auditorías históricas de `docs/` y en Git. Para el estado vigente mandan `CONTEXTO.md`, `README.md`, `KNOWN_ISSUES.md`, `docs/README.md`, `docs/checklist-manual.md` y las auditorías recientes.
