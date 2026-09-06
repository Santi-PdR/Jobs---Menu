# Registro de cambios

## 0.39.0 — Créditos musicales y resource reload — 2026-09-05

### Música y créditos

- Se restaura `assets/jobsmenu/musica_creditada.txt`, recurso que `GestorMusica.creditoAlfa()` ya usaba como compuerta para mostrar créditos pero que había desaparecido durante una etapa anterior del catálogo.
- El marcador actual enumera `absurdism`, `requiem` y `upon_the_hill_v2`; no añade audio ni descarga recursos externos.
- Los créditos vuelven a ser coherentes con el catálogo empaquetado: Absurdism sin autor inventado, REQUIEM con `Emmy Z - Forsaken OST` y Upon the Hill V2 con `ft. @iCosmicCoffee`.

### Resource reload y sesión

- `RecargaRecursosCliente` sustituye el único booleano de reload por una tarea pendiente + `AtomicLong GENERACION`.
- Cada callback incrementa la generación; si otra recarga termina mientras se procesa la anterior, se agenda una nueva pasada en el hilo cliente.
- El callback de recursos sigue sin manipular `SoundInstance` desde el executor de reload.
- `SesionMenu.abrir()` retorna inmediatamente si la visita ya estaba activa, evitando reabrir mantenimiento ambiental al navegar entre subpantallas Jobs.
- El diagnóstico oculto añade pista dominante y generación de resource reload.

### Calidad y documentación

- Nuevo `tools/verificar_reload_creditos.py` fija marcador, ids de las tres pistas, generación/reprogramación de reload, guard de sesión y diagnóstico.
- El workflow ejecuta el nuevo verificador antes del build Forge.
- README, CONTEXTO, KNOWN_ISSUES, checklist, compatibilidad, música, despliegue e índice documental se sincronizan con 0.39.0.
- Nueva auditoría `docs/AUDITORIA_0.39.0_CREDITOS_Y_RELOAD.md`.
- Versión: **0.39.0**.
- Artefacto esperado: **`jobsmenu-0.39.0.jar`**.

## 0.38.0 — Optimización global — 2026-09-05

- `ListasExpediente` cachea fields reflection por clase y listas por Screen viva en vez de redescubrirlos por frame.
- La scrollbar Jobs se deduplica por frame y sus cachés se liberan al cerrar la Screen.
- Fondos de imagen aplican filtrado lineal por objeto de textura, no por frame.
- `NotaAviso`, `PulidoInterfazJobs`, `PielVanillaJobs`, Multiplayer y `RotacionNiveles` reducen asignaciones/recorridos redundantes.
- Bajo consumo reduce draw calls reales de tratamientos de escena.
- El JAR usa orden reproducible y deja de incorporar un timestamp variable de build.
- Se añade `tools/verificar_optimizacion.py`.
- Versión: **0.38.0**; artefacto **`jobsmenu-0.38.0.jar`**.

## 0.37.0 — Continuidad de Multiplayer y documentación — 2026-09-05

- F5/Actualizar conserva la IP del servidor online seleccionado y restaura una Entry nueva tras reconstruir `PantallaMultijugadorJobs`.
- La recarga usa el guard `cerrando` para impedir reconstrucciones repetidas sobre la instancia saliente.
- F5 emite `UI_ALTERNAR` Jobs y el indicador usa `selectServer.refresh` localizado.
- Se crea `docs/README.md` y `tools/verificar_continuidad.py`.
- Versión: **0.37.0**; artefacto **`jobsmenu-0.37.0.jar`**.

## 0.36.0 — Cierre fiable de Multiplayer y cero transiciones en gameplay — 2026-09-05

- `PantallaMultijugadorJobs` guarda su padre Jobs y ESC/Cancelar convergen en `cerrarAlPadre()` sin `super.onClose()`/`popGuiLayer()`.
- F5/Actualizar reconstruye directamente Jobs con el mismo padre.
- Con `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs.
- Pausa/Config conservan tematización permitida; chat, inventario, contenedores y Video Settings quedan fuera.
- Versión: **0.36.0**; artefacto **`jobsmenu-0.36.0.jar`**.

## 0.35.0 — Feedback Jobs y retorno contextual tras servidor — 2026-09-05

- Click/hover Jobs de controles vanilla preservados se desacoplan de la sesión musical y funcionan también en Pausa/Config Jobs sin reactivar música.
- Se memoriza contexto remoto antes del logout para devolver salida/kick/pérdida de conexión a Multiplayer Jobs.
- Mundo local sigue regresando al main Jobs.
- Versión: **0.35.0**; artefacto **`jobsmenu-0.35.0.jar`**.

## Histórico anterior

Las versiones 0.34.0 y anteriores permanecen documentadas en las auditorías y archivos históricos de `docs/` y en el historial Git. Entre los hitos conservados están: Video Settings vanilla (0.33), ambientes 18–31 y controles semánticos (0.32), selector musical fijo y conexión contextual (0.31), composición adaptativa del main (0.30), revisión visual de fondos (0.29), retirada de la barra visible de atajos (0.28), incorporación de JPG 18–31 (0.27), catálogo musical de tres pistas (0.25) y las etapas anteriores de UI/escena/audio.

Para el estado vigente mandan `CONTEXTO.md`, `README.md`, `KNOWN_ISSUES.md`, `docs/README.md`, `docs/checklist-manual.md` y las auditorías más recientes.
