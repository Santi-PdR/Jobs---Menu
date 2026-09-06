# Registro de cambios

## 0.41.1 — Flujo gráfico natural del modpack — 2026-09-06

### Gráficos / compatibilidad

- Se corrige la regresión de 0.41.0 que abría la GUI gráfica mediante un puente específico de Embeddium y podía saltarse modificaciones de otros mods.
- `PantallaOpcionesJobs` pasa a heredar de `OptionsScreen` y ejecuta su `init()` real antes de construir el chrome Jobs.
- El botón **Gráficos** de Jobs conserva y ejecuta el `onPress()` del botón natural `options.video` después de los hooks del modpack.
- La captura se repite en el primer render y justo antes de abrir Gráficos para recoger sustituciones tardías realizadas por Forge/mixins.
- Los widgets externos usados como backend quedan invisibles para evitar controles/hitboxes visuales debajo del panel Jobs.
- Jobs no llama `OptionsScreen.render()`, porque esa ruta volvería a dibujar fondo/título vanilla; sólo renderiza sus widgets propios.
- Se elimina `CompatGraficos`: no hay `ConfigScreenFactory`, lookup directo de `embeddium`, reflection ni construcción directa de `VideoSettingsScreen` desde Jobs.
- Se conserva `esVideoIntocable()` para que la GUI gráfica resultante quede fuera de chrome, transiciones y reemplazo de clicks Jobs.

### Verificación / documentación

- `tools/verificar_graficos_041.py` ahora exige delegación al `OptionsScreen` natural y prohíbe volver a introducir un proveedor gráfico directo.
- `tools/verificar_ui_musica.py` se simplifica y actualiza para proteger los contratos vigentes sin depender de la antigua regla “Video Settings vanilla”.
- README, CONTEXTO, KNOWN_ISSUES, checklist y compatibilidad se actualizan a 0.41.1.
- Versión: **0.41.1**.
- Artefacto esperado: **`jobsmenu-0.41.1.jar`**.

## 0.41.0 — Runtime, audio, Embeddium y continuidad Multiplayer — 2026-09-06

### Gráficos / Embeddium

- El botón **Gráficos** dejó de forzar `VideoSettingsScreen` cuando Embeddium estaba instalado.
- Se introdujo `CompatGraficos` para consumir directamente el `ConfigScreenHandler.ConfigScreenFactory` de Embeddium.
- Esta integración resolvía Embeddium, pero 0.41.1 la reemplaza porque podía saltarse hooks/opciones añadidos por otros mods al flujo natural de `OptionsScreen`.

### Audio / lifecycle

- Se añade `RastreadorAudioJobs` para conservar los FX puntuales Jobs mientras están activos y aplicarles `SoundManager.stop` al cerrar la visita o entrar a gameplay.
- El rastreador purga referencias finalizadas mediante `SoundManager.isActive` antes de añadir nuevas instancias.
- `MezclaAudio.ambiental()` deja de usar `SoundEvents.AMBIENT_CAVE` como respaldo: un registro Jobs faltante se omite en silencio.
- `RecargaRecursosCliente` invalida también FX puntuales y estado de mezcla después de reconstruir recursos.
- `SesionMenu.cerrar()` se vuelve idempotente: no repite un cierre completo en cada tick de gameplay si ya no existe estado Jobs vivo.

### Música vanilla

- `GestorMusica.atender()` deja de llamar `MusicManager.stopPlaying()` cada tick.
- Una visita nueva corta el MusicManager una vez.
- `BloqueoMusicaVanillaJobs` cancela nuevas instancias `SoundSource.MUSIC` durante la sesión Jobs.

### Multiplayer

- F5/Actualizar conserva selección por IP y posición de scroll de `ServerSelectionList`.
- La lista reconstruida restaura una Entry nueva y después aplica `setScrollAmount()`.
- `resize()` conserva ese mismo contexto antes de reconstruir widgets.
- `ServerList.save()` sólo se ejecuta cuando la normalización del servidor oficial realmente cambia datos.

### Config / rendimiento UI

- Setters boolean/int omiten valores idénticos y no programan escrituras TOML innecesarias.
- Perfil accesible modifica sólo los campos que requieren cambio.
- Hover de controles vanilla preservados usa una caché de `AbstractButton`.
- Diagnóstico y verificadores runtime se amplían.

- Versión: **0.41.0**; artefacto **`jobsmenu-0.41.0.jar`**.

## 0.40.0 — Identidad musical y hard-stop reforzado — 2026-09-05

### Música

- `GestorMusica` deja de usar `SoundEvents.MUSIC_MENU` como fallback. Una pista Jobs faltante se omite/reintenta sin reproducir música vanilla.
- El catálogo de Absurdism, REQUIEM y Upon the Hill V2 pasa a `CATALOGO`, construido una sola vez por JVM.
- `catalogo()` devuelve la misma estructura y título/autor/cantidad consultan el catálogo estático.
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
