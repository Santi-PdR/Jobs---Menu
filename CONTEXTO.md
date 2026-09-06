# CONTEXTO — Jobs · Aviso a los ocupantes

Documento maestro del estado vigente. Las auditorías antiguas son históricas y no deben revertir este contrato.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Versión actual | **0.46.0** |
| Artefacto esperado | **`jobsmenu-0.46.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **32 (0–31)** |
| Destino de prueba | `C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods` |

## Reglas duras

1. `main` es la única rama entregable.
2. Todo JAR lleva versión `jobsmenu-<mod_version>.jar`; nunca `jobsmenu-latest.jar`.
3. `gradle.properties` es la fuente de verdad de versión y CI debe quedar verde antes de publicar.
4. `dev-latest` conserva un único JAR Jobs versionado y su tag apunta al mismo SHA de `main` que lo publicó.
5. Java fuente permanece ASCII; texto visible vive en `lang` cuando corresponde.
6. **Gráficos no se tematiza ni se reconstruye con Jobs.** `PantallaOpcionesJobs` es una `Screen` propia.
7. Con Embeddium se usa su `ConfigScreenHandler.ConfigScreenFactory`; sin Embeddium se usa `VideoSettingsScreen` vanilla.
8. La Screen gráfica abierta queda fuera de skin, chrome, transición, hover/click Jobs y recolocación. Jobs no enlaza clases internas del proveedor ni usa reflection para abrirla.
9. **No existe botón MODPACK** ni permiso de un solo uso para Options natural.
10. Toda Screen de terceros y su subflujo quedan fuera de la intervención Jobs hasta regresar a una Screen Jobs.
11. Una sesión Jobs activa por sí sola no autoriza redirecciones administrativas; el origen debe ser un padre Jobs concreto.
12. Chat, inventario, contenedores y UI normal de gameplay no reciben skin, banda, transición ni reemplazo global de clicks Jobs.
13. Con `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs.
14. Música, camas ambientales y FX puntuales reciben hard-stop al entrar a gameplay.
15. PNG 10–17 son totalmente estáticos; JPG 18–31 sólo admiten respiración mínima, no destructiva y desactivable.
16. El rojo queda reservado a Executores; accesibilidad, Movimiento reducido y Bajo consumo tienen prioridad sobre decoración.
17. Ningún control visible puede tener una hitbox invisible superpuesta.
18. El servidor oficial único es `JobsDosh.exaroton.me:56477`; `Ghoul Outbreak` no reaparece.
19. Multiplayer conserva padre Jobs correcto; F5 y resize mantienen selección por IP + scroll y sólo guardan `servers.dat` si hubo cambios reales.
20. Mundos/Mods usan cierre idempotente y ESC por etapas: limpiar filtro, soltar foco y después salir.
21. Un preset sólo aparece activo si coinciden todos los valores que controla; de lo contrario se muestra `CUSTOM`.
22. Config Jobs recuerda la última categoría de la sesión y `Ctrl+F` abre la búsqueda global.
23. Ajustes, Idioma, Mundos y Mods conservan filtro/foco/scroll relevante al reconstruirse por resize.
24. **Idioma y Force Unicode Font se aplican como una única transacción.** Ninguno se considera aplicado hasta que finaliza `reloadResourcePacks()`.
25. Ante fallo de reload se restauran juntos `Options.languageCode`, `LanguageManager` y `forceUnicodeFont` al estado anterior.
26. Un callback tardío de Idioma sólo puede navegar al padre si `PantallaIdiomaJobs` sigue siendo la Screen actual.
27. El buscador de Ajustes navega por API explícita de categoría; no simula teclas contra la pantalla padre.
28. Resource Packs sólo puede devolver a Opciones Jobs mientras `PantallaPaquetesJobs` siga activa.
29. Apariencia, Controles, Config, Idioma, Buscador, Multiplayer, Mundos y Mods protegen rutas de cierre donde pueda existir más de un `setScreen()`.
30. Las tres pistas son Absurdism, REQUIEM y Upon the Hill V2; el build no descarga música ni fondos externos.
31. `assets/jobsmenu/musica_creditada.txt` representa esas tres pistas.
32. Resource reload de audio vuelve al hilo cliente y no manipula `SoundInstance` desde el executor de recursos.
33. La música Jobs nunca usa `minecraft:music.menu`; los FX Jobs nunca usan `minecraft:ambient.cave` como fallback.
34. El hard-stop ordena también `SoundManager.stop(instance)`.
35. Config Jobs no programa guardado para valores idénticos; el hover vanilla preservado usa caché.
36. `PantallaSonidoJobs` resuelve el `Field` reflectivo de `OptionsList` una vez por JVM.

## Estado 0.46.0

### Idioma + Unicode transaccional

`PantallaIdiomaJobs` mantiene separados los valores aplicados y pendientes de idioma y Force Unicode Font. `Aplicar y cerrar` compara ambos estados y, si existe cualquier cambio, actualiza Options una sola vez y ejecuta una sola recarga de recursos. Sólo tras una recarga exitosa actualiza los estados aplicados.

Si la recarga falla, restaura idioma y Unicode al estado previo, persiste ese rollback y mantiene la pantalla disponible para reintentar. El callback vuelve al padre únicamente cuando `minecraft.screen == this`, evitando que una finalización tardía secuestre otra navegación. El cierre tiene guard idempotente.

### Buscador de Ajustes robusto

`PantallaBuscarAjustesJobs` ya no vuelve al padre y simula `1–6`. Ahora llama a `PantallaAjustesAviso.abrirCategoriaDesdeBusqueda(int)`, que valida el índice y abre explícitamente la categoría correcta. El buscador protege también su cierre.

Los textos traducidos de cada resultado y el contador se calculan al reconstruir el filtro, no en cada frame. Se conserva filtro/foco/scroll en resize.

## Estado heredado vigente

- 0.45: búsqueda global, última categoría, `CUSTOM`, continuidad de resize, callback seguro de Resource Packs y caché de reflection de Sonido.
- 0.44: Gráficos original/intocable, sin MODPACK, `PantallaOpcionesJobs` independiente y redirecciones acotadas.
- 0.43: perfiles exactos y ESC/búsqueda robustos.
- 0.42: aislamiento genérico de pantallas de terceros.
- 0.41: hard-stop de audio, sesión idempotente, continuidad Multiplayer y escrituras de config/servers protegidas.

## Música

1. Absurdism
2. REQUIEM — `Emmy Z - Forsaken OST`
3. Upon the Hill V2 — `ft. @iCosmicCoffee`

Inicio aleatorio o fijo, crossfade sin repetición inmediata, `N` sólo en Aleatoria, `M` mute Jobs, reload invalida instancias viejas y gameplay corta toda la sesión sonora Jobs.

## Fondos

- 0–9: escenas procedurales Jobs.
- 10–17: PNG históricos estrictamente estáticos.
- 18–31: JPG 1920×1080 directos con respiración mínima opcional.

## Verificación

CI ejecuta todos los contratos históricos vigentes más `tools/verificar_lifecycle_046.py`, seguido del build Forge Java 17. La publicación sólo ocurre desde `main` verde; después se mueve `dev-latest` al SHA publicado y se eliminan assets Jobs obsoletos.

La validación visual, input, audio perceptivo y compatibilidad final con el modpack siguen siendo manuales en `test-1`.
