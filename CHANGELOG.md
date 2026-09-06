# Registro de cambios

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
- CI ejecuta el nuevo verificador antes del build Forge.
- README, CONTEXTO, KNOWN_ISSUES, checklist, compatibilidad y música se sincronizan con 0.40.0.
- Versión: **0.40.0**.
- Artefacto esperado: **`jobsmenu-0.40.0.jar`**.

## 0.39.0 — Créditos musicales y resource reload — 2026-09-05

### Música y créditos

- Se restaura `assets/jobsmenu/musica_creditada.txt`, compuerta usada por `GestorMusica.creditoAlfa()` para mostrar créditos.
- El marcador enumera `absurdism`, `requiem` y `upon_the_hill_v2`.
- Absurdism no inventa autor, REQUIEM acredita `Emmy Z - Forsaken OST` y Upon the Hill V2 `ft. @iCosmicCoffee`.

### Resource reload y sesión

- `RecargaRecursosCliente` usa `AtomicLong GENERACION` además del guard de tarea pendiente.
- Si llega una generación nueva mientras se procesaba la anterior, se agenda otra pasada en el hilo cliente.
- `SesionMenu.abrir()` corta reaperturas de una visita ya activa.
- Diagnóstico oculto añade pista dominante y generación de reload.
- Nuevo `tools/verificar_reload_creditos.py`.
- Versión: **0.39.0**; artefacto **`jobsmenu-0.39.0.jar`**.

## 0.38.0 — Optimización global — 2026-09-05

- `ListasExpediente` cachea fields reflection por clase y listas por Screen viva.
- Scrollbar Jobs deduplicada por frame y cachés liberadas al cerrar Screen.
- Fondos de imagen aplican filtrado por objeto de textura, no por frame.
- `NotaAviso`, `PulidoInterfazJobs`, `PielVanillaJobs`, Multiplayer y `RotacionNiveles` reducen asignaciones/recorridos redundantes.
- Bajo consumo reduce draw calls reales.
- JAR con orden reproducible y sin timestamp variable de build.
- Nuevo `tools/verificar_optimizacion.py`.
- Versión: **0.38.0**; artefacto **`jobsmenu-0.38.0.jar`**.

## 0.37.0 — Continuidad de Multiplayer y documentación — 2026-09-05

- F5/Actualizar conserva la IP del servidor online seleccionado y restaura una Entry nueva.
- Guard `cerrando` impide reconstrucciones repetidas.
- F5 emite `UI_ALTERNAR` Jobs y el indicador usa `selectServer.refresh` localizado.
- Se crea `docs/README.md` y `tools/verificar_continuidad.py`.
- Versión: **0.37.0**; artefacto **`jobsmenu-0.37.0.jar`**.

## 0.36.0 — Cierre fiable de Multiplayer y cero transiciones en gameplay — 2026-09-05

- ESC/Cancelar convergen en `cerrarAlPadre()` sin `super.onClose()`/`popGuiLayer()`.
- F5 reconstruye directamente Jobs con el mismo padre.
- Con `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs.
- Pausa/Config conservan tematización permitida; chat, inventario, contenedores y Video Settings quedan fuera.
- Versión: **0.36.0**; artefacto **`jobsmenu-0.36.0.jar`**.

## 0.35.0 — Feedback Jobs y retorno contextual tras servidor — 2026-09-05

- Click/hover Jobs de controles vanilla preservados funcionan también en Pausa/Config Jobs sin reactivar música.
- Se memoriza contexto remoto antes del logout para devolver salida/kick/pérdida de conexión a Multiplayer Jobs.
- Mundo local sigue regresando al main Jobs.
- Versión: **0.35.0**; artefacto **`jobsmenu-0.35.0.jar`**.

## Histórico anterior

Las versiones 0.34.0 y anteriores permanecen documentadas en auditorías y archivos históricos de `docs/` y en el historial Git. Entre los hitos: navegación fiable/gameplay (0.34), Video Settings vanilla (0.33), ambientes 18–31 y controles semánticos (0.32), selector musical fijo y conexión contextual (0.31), composición adaptativa (0.30), revisión visual de fondos (0.29), retirada de la barra visible de atajos (0.28), JPG 18–31 (0.27) y catálogo musical de tres pistas (0.25).

Para el estado vigente mandan `CONTEXTO.md`, `README.md`, `KNOWN_ISSUES.md`, `docs/README.md`, `docs/checklist-manual.md` y las auditorías más recientes.
