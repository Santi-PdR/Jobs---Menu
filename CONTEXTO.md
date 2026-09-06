# CONTEXTO — Jobs · Aviso a los ocupantes

Documento maestro del estado vigente. Las auditorías antiguas son históricas y no deben revertir este contrato.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Versión actual | **0.45.0** |
| Artefacto esperado | **`jobsmenu-0.45.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **32 (0–31)** |
| Destino de prueba | `C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods` |

## Reglas duras

1. `main` es la única rama entregable.
2. Todo JAR lleva versión: `jobsmenu-<mod_version>.jar`; nunca `jobsmenu-latest.jar`.
3. `gradle.properties` es la fuente de verdad de versión.
4. CI debe estar verde antes de publicar.
5. `dev-latest` conserva un único JAR Jobs versionado y su tag apunta al mismo `main` publicado.
6. Java fuente permanece ASCII; texto visible vive en `lang` cuando corresponde.
7. **La pantalla Gráficos no se tematiza ni se reconstruye con Jobs.**
8. `PantallaOpcionesJobs` es una `Screen` Jobs propia; no hereda de `OptionsScreen` ni oculta widgets vanilla/modded para usarlos como backend.
9. Con Embeddium, Gráficos usa el `ConfigScreenFactory` oficial registrado en Forge y abre la Screen devuelta sin modificarla.
10. Sin Embeddium, Gráficos usa `VideoSettingsScreen` vanilla y esa Screen también queda completamente fuera de Jobs.
11. Jobs no enlaza clases internas de Embeddium/Sodium ni usa reflection para abrir Gráficos.
12. **No existe botón MODPACK ni permiso `permitirOptionsNaturalUnaVez`.**
13. Toda Screen de terceros queda fuera de skin, banda, transición, hover/click Jobs y recolocación de widgets.
14. Un subflujo iniciado por una Screen de terceros sigue siendo externo hasta volver a una Screen Jobs.
15. Una sesión Jobs activa por sí sola no autoriza a reemplazar Options/Multiplayer/Mundos/Mods: las redirecciones administrativas sólo nacen desde padres Jobs concretos.
16. Chat, inventario, contenedores y UI normal de gameplay no reciben skin, banda, transición ni reemplazo global de clicks Jobs.
17. Con `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs.
18. Música, camas ambientales y FX puntuales del menú aplican hard-stop al entrar a gameplay.
19. Pausa/Config Jobs pueden conservar tema y gestos breves sin reabrir la sesión musical.
20. PNG 10–17 son totalmente estáticos.
21. JPG 18–31 sólo admiten respiración de cámara sutil, no destructiva y desactivable.
22. El rojo queda reservado a Executores.
23. Accesibilidad, Movimiento reducido y Bajo consumo tienen prioridad sobre decoración.
24. Ningún control visible puede tener una hitbox invisible superpuesta.
25. El servidor oficial único es `JobsDosh.exaroton.me:56477`.
26. ESC y Cancelar de Multiplayer vuelven al padre Jobs con una sola acción.
27. F5/Actualizar y resize/maximizar/cambio de escala GUI conservan selección online por IP y scroll.
28. Un servidor remoto vuelve a Multiplayer Jobs tras salida/kick/pérdida de conexión; un mundo local vuelve al main Jobs.
29. Mundos y Mods usan cierre idempotente; ESC limpia filtro, después suelta foco y recién luego sale.
30. Un preset sólo se muestra como activo si todos los valores que controla coinciden; cualquier desviación relevante muestra `CUSTOM`.
31. Config Jobs recuerda la última categoría usada durante la sesión y `Ctrl+F` abre la búsqueda global de preferencias.
32. Ajustes, Idioma, Mundos y Mods deben conservar sus filtros/foco/scroll relevantes al reconstruirse por resize.
33. Aplicar idioma debe ser transaccional: ante fallo de reload se restauran `Options.languageCode` y `LanguageManager` al valor anterior.
34. Callbacks tardíos de Resource Packs no pueden devolver a Opciones Jobs si el usuario ya salió de `PantallaPaquetesJobs`.
35. Apariencia, Controles, Config, Multiplayer, Mundos y Mods usan cierres protegidos donde una ruta duplicada pueda disparar más de un `setScreen()`.
36. Las tres pistas musicales son Absurdism, REQUIEM y Upon the Hill V2.
37. El build no descarga música ni fondos externos.
38. `assets/jobsmenu/musica_creditada.txt` representa las tres pistas empaquetadas.
39. Los callbacks de resource reload nunca manipulan `SoundInstance` desde el executor de recursos.
40. La música Jobs nunca usa `minecraft:music.menu` como fallback.
41. Los FX ambientales Jobs nunca usan `minecraft:ambient.cave` como fallback.
42. El hard-stop musical ordena también `SoundManager.stop(instance)`.
43. Multiplayer sólo guarda `servers.dat` cuando su normalización realmente modifica datos.
44. Config Jobs no programa guardado cuando el valor solicitado ya coincide con el actual.
45. El hover vanilla preservado cachea botones por Screen/init en vez de recorrer todos los hijos por frame.
46. `PantallaSonidoJobs` resuelve el `Field` reflectivo de `OptionsList` una vez por JVM y lo reutiliza.

## Estado 0.45.0

### Búsqueda transversal de Ajustes

`PantallaBuscarAjustesJobs` centraliza el descubrimiento de preferencias sin duplicar controles. `Ctrl+F` desde Config Jobs abre el buscador; nombre, descripción y categoría participan del filtro. Enter o doble clic navega a la categoría real del ajuste. Filtro, foco y scroll sobreviven a `resize()`.

Config Jobs recuerda además la última categoría utilizada durante la sesión y el indicador de perfil muestra `CUSTOM` explícitamente cuando ningún preset coincide.

### Idioma transaccional

`PantallaIdiomaJobs` conserva idioma pendiente, filtro, foco y scroll durante resize. Antes de aplicar un idioma guarda el valor anterior. Si `reloadResourcePacks()` falla, restaura tanto `Options.languageCode` como `LanguageManager`, mantiene la pantalla abierta y muestra feedback de error.

### Continuidad y callbacks

Mundos y Mods conservan búsqueda/foco en resize. Apariencia y Controles añaden cierres idempotentes. El callback de Resource Packs sólo vuelve a Opciones Jobs mientras `PantallaPaquetesJobs` siga siendo la Screen activa; un callback tardío no puede secuestrar una navegación posterior.

### Rendimiento

`PantallaSonidoJobs` deja de recorrer `SoundOptionsScreen.getDeclaredFields()` en cada `init()`: el `Field` de `OptionsList` se resuelve y cachea una sola vez por JVM.

## Estado 0.44 que sigue vigente

- Gráficos original/intocable: Embeddium vía `ConfigScreenFactory`; fallback `VideoSettingsScreen` vanilla.
- No existe MODPACK ni `permitirOptionsNaturalUnaVez`.
- `PantallaOpcionesJobs` es una `Screen` Jobs independiente.
- Redirecciones administrativas acotadas a padres Jobs concretos.
- Screens externas y sus subflujos no reciben skin, bandas, transiciones, hover, clicks ni trabajo de `ListasExpediente`.

## Estado heredado 0.41–0.43 que sigue vigente

- Perfiles exactos; modificaciones relevantes muestran `CUSTOM`.
- `RastreadorAudioJobs` corta FX puntuales al cerrar visita/gameplay.
- `GestorMusica` no hace `stopPlaying()` por tick y bloquea nueva música vanilla por evento.
- `SesionMenu.cerrar()` es idempotente.
- F5/Actualizar y `resize()` de Multiplayer conservan selección+scroll.
- `ServerList.save()` sólo corre si la normalización cambió datos.
- Setters de config omiten valores idénticos.
- Hover vanilla preservado usa caché.
- Publicación `dev-latest`: publicar JAR → mover tag → limpiar assets.

## Música

1. Absurdism
2. REQUIEM — `Emmy Z - Forsaken OST`
3. Upon the Hill V2 — `ft. @iCosmicCoffee`

Inicio aleatorio o fijo, crossfade sin repetición inmediata, `N` sólo en Aleatoria, `M` mute Jobs, reload invalida instancias viejas y gameplay corta toda la sesión sonora Jobs.

## Multiplayer

Servidor oficial primero, único y protegido; `Ghoul Outbreak` no reaparece; conectar usa Jobs como padre; F5 y resize conservan selección+scroll; Cancelar/error pre-login vuelve a Jobs; logout/kick remoto vuelve a Multiplayer Jobs.

## Fondos

- 0–9: escenas procedurales Jobs.
- 10–17: PNG históricos estrictamente estáticos.
- 18–31: JPG 1920×1080 directos con respiración mínima opcional.

## Verificación

CI ejecuta política de versión/tag, fondos, verificador general, UI/música, continuidad Multiplayer/documentación, optimización, créditos/reload, identidad musical/hard-stop, runtime 0.41, aislamiento externo, UX 0.43, Gráficos 0.44 y `tools/verificar_calidad_045.py`, seguido del build Forge Java 17 y publicación versionada sólo desde `main` verde.

La validación visual, input, audio perceptivo y compatibilidad final con el modpack siguen siendo manuales en `test-1`.
