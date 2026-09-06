# Registro de cambios

## 0.45.0 — Búsqueda global, continuidad y robustez transversal — 2026-09-06

### Ajustes Jobs

- Nueva `PantallaBuscarAjustesJobs` accesible con `Ctrl+F` desde Config Jobs.
- Busca nombre, detalle y categoría de las preferencias propias del mod.
- Enter o doble clic abre la categoría correspondiente sin duplicar la lógica de los controles.
- Búsqueda, foco y scroll se conservan durante resize/maximizar.
- Config Jobs recuerda la última categoría usada durante la sesión.
- El estado superior muestra `CUSTOM` cuando ningún preset coincide, en lugar de desaparecer.
- Config Jobs añade guard idempotente de cierre.

### Idioma

- El idioma pendiente, filtro, foco y scroll sobreviven a `resize()`.
- Aplicar idioma se vuelve transaccional: se conserva el idioma anterior antes de cambiar `Options.languageCode` y `LanguageManager`.
- Si `reloadResourcePacks()` falla, ambos estados se restauran y se muestra feedback de error en lugar de dejar una selección fantasma.
- Una recarga fallida no cierra la pantalla ni obliga a reiniciar para recuperar coherencia.

### Mundos / Mods

- Ambos filtros conservan valor y foco durante resize/maximizar/cambio de escala GUI.
- La reconstrucción deja de borrar silenciosamente la búsqueda activa.

### Navegación / robustez

- Apariencia y Controles usan cierre idempotente y comprueban `minecraft != null` antes de volver al padre.
- El callback de Resource Packs sólo vuelve a Opciones Jobs si `PantallaPaquetesJobs` continúa siendo la pantalla activa; un callback tardío ya no puede secuestrar otra navegación.
- Se mantiene intacto el contrato de Gráficos 0.44: Embeddium/Video Settings siguen fuera de chrome y lógica visual Jobs.

### Rendimiento

- `PantallaSonidoJobs` cachea una sola vez por JVM el `Field` reflectivo que resuelve `OptionsList`.
- Los `init()` posteriores sólo leen el campo ya resuelto, evitando volver a recorrer `SoundOptionsScreen.getDeclaredFields()`.

### Calidad

- Nuevo `tools/verificar_calidad_045.py` protege búsqueda global, estado `CUSTOM`, última categoría, rollback de idioma, continuidad de filtros, cierres seguros, caché de reflection y callback de Resource Packs.
- CI añade `Verify global quality 0.45` antes del build Forge.
- README, CONTEXTO, KNOWN_ISSUES, checklist y auditoría se sincronizan con 0.45.0.
- Versión: **0.45.0**.
- Artefacto esperado: **`jobsmenu-0.45.0.jar`**.

## 0.44.0 — Gráficos intocable y salida de configuración corregida — 2026-09-06

### Gráficos

- `PantallaOpcionesJobs` deja de heredar de `OptionsScreen` y vuelve a ser una `Screen` Jobs independiente.
- Se eliminan `super.init()`, `botonVideoNatural`, la detección por ranura, la sincronización tardía y el ocultado de widgets vanilla/modded.
- El botón Gráficos ya no usa controles invisibles como backend.
- Se restaura `CompatGraficos` como puente mínimo: pide a Forge la `ConfigScreenHandler.ConfigScreenFactory` registrada por Embeddium y abre la Screen devuelta sin modificarla.
- No se enlazan clases internas de Embeddium/Sodium y no se usa reflection.
- Sin Embeddium o ante fallo seguro de su factory, se abre `VideoSettingsScreen` vanilla.
- Tanto la Screen de Embeddium como Video Settings vanilla quedan fuera de chrome, transiciones, hover/click y recolocación Jobs.

### MODPACK / navegación

- Se elimina por completo el botón **MODPACK**.
- Se eliminan `abrirOpcionesModpack()`, `permitirOptionsNaturalUnaVez`, `optionsNaturalSolicitado` y el permiso de un solo uso asociado.
- Se corrige el bucle por el que el flujo de Options natural podía volver repetidamente a configuración y dificultar la salida.
- `SesionMenu.activa()` deja de autorizar por sí sola redirecciones administrativas. Options/Multiplayer/Mundos/Mods sólo se convierten a Jobs cuando el padre es `PantallaNivel`, `PantallaEstancia` o `PantallaOpcionesJobs`.
- `PantallaOpcionesJobs.onClose()` añade guard idempotente.
- El callback de resource packs evita `setScreen(this)` si esa Screen ya es la actual.

### Calidad

- Nuevo `tools/verificar_graficos_044.py` protege ausencia de MODPACK, arquitectura de Opciones simple, factory gráfica original y redirecciones acotadas.
- `tools/verificar_ui_musica.py` y `tools/verificar_compatibilidad_042.py` se actualizan al contrato vigente.
- CI añade `Verify untouched graphics and Options 0.44` antes del build Forge.
- README, CONTEXTO, KNOWN_ISSUES, checklist y compatibilidad se actualizan a 0.44.0.
- Versión: **0.44.0**.
- Artefacto esperado: **`jobsmenu-0.44.0.jar`**.

## 0.43.0 — Perfiles exactos, búsqueda y navegación robusta — 2026-09-06

### Perfiles / configuración

- `PerfilesJobs.actual()` deja de inferir presets con unas pocas señales generales.
- Cada preset comprueba ahora todos los valores que realmente controla: escena, accesibilidad, papel/interfaz, eventos/presencia, respiración, suspensión, rotación, bajo consumo, duraciones y volúmenes correspondientes.
- Una configuración modificada manualmente deja de aparecer falsamente como Equilibrado/Inmersivo/Rendimiento/etc. y pasa a estado `CUSTOM`.
- Pista musical y nivel fijo no invalidan un perfil porque los presets no los modifican.

### Mundos / Mods

- `Ctrl+F` conserva el acceso rápido al buscador.
- Con el buscador enfocado, `ESC` limpia primero el filtro; un segundo `ESC` abandona el foco y sólo el siguiente vuelve al padre Jobs.
- `PantallaMundosJobs` y `PantallaModsJobs` añaden guard `cerrando` para evitar dobles cambios de Screen por rutas de cierre superpuestas.
- `init()` reinicia correctamente buscador y guard tras resize/reconstrucción.

### Compatibilidad / navegación externa

- El subflujo externo de 0.42 protege también `TitleScreen`: una GUI ajena que termine allí no es secuestrada por Jobs mientras siga dentro de ese flujo.
- La sustitución de pausa vanilla por `PantallaEstancia` también respeta el marcador externo.
- Los retornos reales desde gameplay mantienen su comportamiento Jobs porque su estado contextual se procesa antes de estas reglas.

### Calidad

- Nuevo `tools/verificar_ux_043.py` protege detección exacta de perfiles, búsqueda/ESC, cierre idempotente y la frontera externa completa.
- CI añade `Verify UX and navigation 0.43` antes del build Forge.
- README, CONTEXTO, KNOWN_ISSUES, checklist y compatibilidad se sincronizan con 0.43.0.
- Versión: **0.43.0**.
- Artefacto esperado: **`jobsmenu-0.43.0.jar`**.

## 0.42.0 — Compatibilidad de terceros y publicación consistente — 2026-09-06

### Compatibilidad / navegación

- `EscuchaCliente` deja de enumerar paquetes concretos de Sodium, Embeddium, Iris u Oculus para decidir qué GUI respetar.
- Nueva regla genérica de propiedad: toda `Screen` que no pertenezca a Jobs, `net.minecraft.*` o `net.minecraftforge.*` se considera de terceros y queda fuera de chrome, bandas, transiciones, hover y reemplazo de clicks Jobs.
- `VideoSettingsScreen` vanilla sigue siendo explícitamente intocable aunque pertenezca a Minecraft.
- Una pantalla de terceros ya no puede activar redirecciones Jobs en sus subflujos sólo porque `SesionMenu` continúe activa.
- Se añade seguimiento de subflujo externo: una GUI vanilla abierta desde una superficie de terceros sigue siendo ajena hasta volver explícitamente a una Screen Jobs.
- `ListasExpediente`, pulido, hover y liberación de listas se omiten también en superficies externas.

### Gráficos / opciones naturales — histórico, reemplazado por 0.44

- `PantallaOpcionesJobs` pasó a usar un `OptionsScreen` real y el `onPress()` del botón natural de vídeo.
- Se memorizó la ranura original del control para intentar reconocer reemplazos.
- Se añadió el botón MODPACK y un permiso de un solo uso.
- **Este diseño queda retirado en 0.44.0 por complejidad y por el bug de retorno de configuración.**

### Pipeline / dev-latest

- El workflow fuerza `dev-latest` a `$GITHUB_SHA` sólo desde `main`.
- La publicación usa orden: publicar JAR → mover tag → eliminar assets Jobs obsoletos.
- `tools/verificar_version.py` exige el paso y su orden.

- Versión: **0.42.0**.
- Artefacto esperado: **`jobsmenu-0.42.0.jar`**.

## 0.41.1 — Flujo gráfico natural del modpack — 2026-09-06

### Gráficos / compatibilidad — histórico

- `PantallaOpcionesJobs` pasó temporalmente a heredar de `OptionsScreen` y ejecutar su `init()` real.
- Jobs conservaba y ejecutaba el `onPress()` del botón natural `options.video`.
- Los widgets externos usados como backend quedaban invisibles.
- Se eliminó temporalmente `CompatGraficos`.
- **0.44.0 retira esta arquitectura.**

- Versión: **0.41.1**.
- Artefacto esperado: **`jobsmenu-0.41.1.jar`**.

## 0.41.0 — Runtime, audio, Embeddium y continuidad Multiplayer — 2026-09-06

### Gráficos / Embeddium

- El botón Gráficos dejó de forzar `VideoSettingsScreen` cuando Embeddium estaba instalado.
- Se introdujo `CompatGraficos` para consumir `ConfigScreenHandler.ConfigScreenFactory`.
- 0.44 recupera la idea del puente mínimo, pero mantiene el aislamiento genérico de terceros añadido después.

### Audio / lifecycle

- Se añade `RastreadorAudioJobs` para conservar los FX puntuales Jobs mientras están activos y aplicarles `SoundManager.stop` al cerrar la visita o entrar a gameplay.
- El rastreador purga referencias finalizadas mediante `SoundManager.isActive` antes de añadir nuevas instancias.
- `MezclaAudio.ambiental()` deja de usar `SoundEvents.AMBIENT_CAVE` como respaldo.
- `RecargaRecursosCliente` invalida también FX puntuales y estado de mezcla después de reconstruir recursos.
- `SesionMenu.cerrar()` se vuelve idempotente.

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
