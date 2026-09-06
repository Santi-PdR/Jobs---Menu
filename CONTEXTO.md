# CONTEXTO — Jobs · Aviso a los ocupantes

Documento maestro del estado vigente. Las auditorías antiguas son históricas y no deben revertir este contrato.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Versión actual | **0.43.0** |
| Artefacto esperado | **`jobsmenu-0.43.0.jar`** |
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
5. `dev-latest` conserva un único JAR Jobs versionado, sólo se actualiza desde `main` y el **ref Git del tag debe apuntar al mismo `main` publicado**.
6. Java fuente permanece ASCII; texto visible vive en `lang` cuando corresponde.
7. **Gráficos sigue la ruta natural del `OptionsScreen` real después de los hooks del modpack. Jobs no elige proveedor por su cuenta.**
8. Jobs no usa `ConfigScreenFactory`, reflection ni clases internas de Embeddium/Sodium para abrir Gráficos.
9. Si un mod reemplaza el botón gráfico y cambia su etiqueta, la ranura original puede usarse para reconocer el sustituto sin construir otra Screen.
10. Toda Screen de terceros queda fuera de skin, banda, transición, hover/click Jobs y recolocación de widgets.
11. Una Screen de terceros y su **subflujo completo** tampoco habilitan redirecciones Jobs sólo porque `SesionMenu` siga activa; incluso `TitleScreen` y pausa vanilla se respetan mientras el flujo siga marcado como externo.
12. `VideoSettingsScreen` vanilla también es superficie intocable.
13. Chat, inventario, contenedores y UI normal de gameplay no reciben skin, banda, transición ni reemplazo global de clicks Jobs.
14. Con `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs.
15. Música, camas ambientales y FX puntuales del menú aplican hard-stop al entrar a gameplay.
16. Pausa/Config Jobs pueden conservar tema y gestos breves sin reabrir la sesión musical.
17. PNG 10–17 son totalmente estáticos.
18. JPG 18–31 sólo admiten respiración de cámara sutil, no destructiva y desactivable.
19. El rojo queda reservado a Executores.
20. Accesibilidad, Movimiento reducido y Bajo consumo tienen prioridad sobre decoración.
21. Ningún control visible puede tener una hitbox invisible superpuesta.
22. Pantallas complejas conservan lógica Minecraft/Forge real cuando eso protege compatibilidad.
23. El servidor oficial único es `JobsDosh.exaroton.me:56477`.
24. ESC y Cancelar de Multiplayer deben volver al padre Jobs con una sola acción.
25. F5/Actualizar y resize/maximizar/cambio de escala GUI conservan selección online por IP y posición de scroll; no crean Multiplayer vanilla intermedio.
26. Un servidor remoto vuelve a Multiplayer Jobs tras salida/kick/pérdida de conexión; un mundo local vuelve al main Jobs.
27. Mundos y Mods usan cierre idempotente; con el buscador enfocado, ESC limpia filtro, después suelta foco y recién luego sale.
28. Un preset sólo se muestra como activo si todos los valores que ese preset controla coinciden. Una configuración modificada debe mostrarse como `CUSTOM`.
29. Las tres pistas musicales son Absurdism, REQUIEM y Upon the Hill V2.
30. El build no descarga música ni fondos externos.
31. `assets/jobsmenu/musica_creditada.txt` representa las tres pistas empaquetadas.
32. Los callbacks de resource reload nunca manipulan `SoundInstance` desde el executor de recursos.
33. La música Jobs nunca usa `minecraft:music.menu` como fallback.
34. Los FX ambientales Jobs nunca usan `minecraft:ambient.cave` como fallback.
35. El catálogo musical se construye una sola vez por JVM.
36. El hard-stop musical ordena también `SoundManager.stop(instance)`.
37. Los FX puntuales Jobs se rastrean mientras están activos y se cortan al cerrar la visita.
38. Multiplayer sólo guarda `servers.dat` cuando su normalización realmente modifica datos.
39. Config Jobs no programa guardado cuando el valor solicitado ya coincide con el actual.
40. El hover vanilla preservado cachea botones por Screen/init en vez de recorrer todos los hijos por frame.
41. Jobs corta `MusicManager` una vez al abrir visita y bloquea nuevas instancias `SoundSource.MUSIC` mientras la sesión está activa; no hace polling de stop por tick.

## Estado 0.43.0

### Perfiles sin falsos positivos

`PerfilesJobs.actual()` ya no usa heurísticas cortas como “Bajo consumo + Movimiento reducido = Rendimiento” o “ambiente >= 64 = Inmersivo”. Cada preset tiene un matcher que comprueba los valores que realmente escribe ese perfil: visuales, accesibilidad, ambiente, duración y volumen. Los campos que el preset no controla —como pista musical o nivel fijo— se ignoran a propósito.

El resultado visible es simple: si la configuración todavía coincide, se conserva `EQ`, `IMM`, `PERF`, `ACC` o `MIN`; si se personalizó algo relevante, `CapaProfesionalJobs` y la pestaña de perfiles muestran estado personalizado en vez de identificarlo erróneamente como un preset.

### Búsqueda y cierre de Mundos/Mods

`PantallaMundosJobs` y `PantallaModsJobs` comparten el mismo contrato de ESC cuando el buscador tiene foco: primero vaciar texto, después abandonar foco, después cerrar. Ambas pantallas usan un guard `cerrando` que impide ejecutar dos cambios de Screen por una misma salida.

### Frontera externa completa

El marcador de subflujo externo heredado de 0.42 ahora también bloquea la sustitución global de `TitleScreen` y de la pausa vanilla mientras el usuario siga dentro de un flujo nacido en una GUI externa/MODPACK. El arranque normal y los retornos reales desde gameplay siguen usando Jobs porque esos casos no están marcados como externos.

## Estado heredado 0.42

- Gráficos delega al botón real de `OptionsScreen` y puede reconocer su reemplazo por ranura.
- MODPACK abre el `OptionsScreen` natural completo.
- Screens externas y sus subflujos no reciben skin, bandas, transiciones, hover, clicks ni trabajo de `ListasExpediente`.
- `VideoSettingsScreen` vanilla es intocable.
- publicación `dev-latest`: publicar JAR → mover tag → limpiar assets.

## Estado heredado 0.41

- `RastreadorAudioJobs` corta FX puntuales al cerrar visita/gameplay.
- `MezclaAudio.ambiental()` usa silencio controlado, no `AMBIENT_CAVE`.
- `GestorMusica` no hace `stopPlaying()` por tick y bloquea nueva música vanilla por evento.
- `SesionMenu.cerrar()` es idempotente.
- F5/Actualizar y `resize()` de Multiplayer conservan selección+scroll.
- `ServerList.save()` sólo corre si la normalización cambió datos.
- Setters de config omiten valores idénticos.
- Hover vanilla preservado usa caché.

## Estado heredado importante

### 0.40.0 — identidad musical

- sin fallback a `SoundEvents.MUSIC_MENU`;
- catálogo estático `CATALOGO`;
- pista entrante se resuelve antes de retirar la actual;
- hard-stop directo al `SoundManager`.

### 0.39.0 — reload/créditos

- `musica_creditada.txt` representa las tres pistas;
- resource reload usa generación atómica;
- una visita Jobs no se reinicializa al navegar entre subpantallas.

### 0.38.0 — rendimiento

- reflection/listas cacheadas;
- scrollbars deduplicadas;
- filtrado por instancia de textura;
- menos asignaciones UI;
- snapshots de rotación compartidos;
- Bajo consumo reduce draw calls;
- JAR reproducible.

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

CI ejecuta política de versión/tag, fondos, verificador general, UI/música, continuidad Multiplayer/documentación, optimización, créditos/reload, identidad musical/hard-stop, `tools/verificar_runtime_041.py`, `tools/verificar_compatibilidad_042.py`, `tools/verificar_ux_043.py`, build Forge Java 17 y publicación versionada sólo desde `main` verde.

La validación visual, input, audio perceptivo y compatibilidad final con el modpack siguen siendo manuales en `test-1`.
