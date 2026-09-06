# CONTEXTO — Jobs · Aviso a los ocupantes

Documento maestro del estado vigente. Las auditorías antiguas son históricas y no deben revertir este contrato.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama entregable | `main` |
| Mod id | `jobsmenu` |
| Versión actual | **0.41.1** |
| Artefacto esperado | **`jobsmenu-0.41.1.jar`** |
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
5. `dev-latest` conserva un único JAR Jobs versionado y sólo se actualiza desde `main`.
6. Java fuente permanece ASCII; texto visible vive en `lang` cuando corresponde.
7. **Gráficos debe seguir la ruta natural del `OptionsScreen` real después de los hooks del modpack. Jobs no elige Embeddium, vanilla ni otro proveedor por su cuenta.**
8. Jobs no usa `ConfigScreenFactory`, reflection ni clases internas de Embeddium/Sodium para abrir Gráficos.
9. La GUI gráfica resultante queda fuera de skin, banda, transición, hover/click Jobs y recolocación de widgets.
10. Chat, inventario, contenedores y UI normal de gameplay no reciben skin, banda, transición ni reemplazo global de clicks Jobs.
11. Con `Minecraft.level != null` no se crea ni dibuja ninguna transición Jobs.
12. Música, camas ambientales y FX puntuales del menú aplican hard-stop al entrar a gameplay.
13. Pausa/Config Jobs pueden conservar tema y gestos breves sin reabrir la sesión musical.
14. PNG 10–17 son totalmente estáticos.
15. JPG 18–31 sólo admiten respiración de cámara sutil, no destructiva y desactivable.
16. El rojo queda reservado a Executores.
17. Accesibilidad, Movimiento reducido y Bajo consumo tienen prioridad sobre decoración.
18. Ningún control visible puede tener una hitbox invisible superpuesta.
19. Pantallas complejas conservan lógica Minecraft/Forge real cuando eso protege compatibilidad.
20. El servidor oficial único es `JobsDosh.exaroton.me:56477`.
21. ESC y Cancelar de Multiplayer deben volver al padre Jobs con una sola acción.
22. F5/Actualizar y resize/maximizar/cambio de escala GUI conservan selección online por IP y posición de scroll; no crean Multiplayer vanilla intermedio.
23. Un servidor remoto vuelve a Multiplayer Jobs tras salida/kick/pérdida de conexión; un mundo local vuelve al main Jobs.
24. Las tres pistas musicales son Absurdism, REQUIEM y Upon the Hill V2.
25. El build no descarga música ni fondos externos.
26. `assets/jobsmenu/musica_creditada.txt` debe representar las tres pistas empaquetadas.
27. Los callbacks de resource reload nunca manipulan `SoundInstance` desde el executor de recursos.
28. La música Jobs nunca usa `minecraft:music.menu` como fallback.
29. Los FX ambientales Jobs nunca usan `minecraft:ambient.cave` como fallback.
30. El catálogo musical se construye una sola vez por JVM.
31. El hard-stop musical ordena también `SoundManager.stop(instance)`.
32. Los FX puntuales Jobs se rastrean mientras están activos y se cortan al cerrar la visita.
33. Multiplayer sólo guarda `servers.dat` cuando su normalización realmente modifica datos.
34. Config Jobs no programa guardado cuando el valor solicitado ya coincide con el actual.
35. El hover vanilla preservado cachea botones por Screen/init en vez de recorrer todos los hijos por frame.
36. Jobs corta `MusicManager` una vez al abrir visita y bloquea nuevas instancias `SoundSource.MUSIC` mientras la sesión está activa; no hace polling de stop por tick.

## Estado 0.41.1

### Gráficos: delegación natural

`PantallaOpcionesJobs` hereda de `OptionsScreen`. Al inicializarse ejecuta primero `super.init()`: Minecraft crea sus controles reales y los mixins del modpack pueden modificar exactamente la misma instancia que modificarían en el menú normal.

Jobs busca el `AbstractButton` cuyo mensaje es `options.video`, conserva ese objeto como backend y oculta los controles externos para que no existan botones/hitboxes visibles debajo del diseño Jobs. El botón Gráficos de Jobs no crea ninguna Screen: llama `onPress()` sobre ese botón natural.

La captura se repite en el primer render, una vez terminado el ciclo de inicialización de Forge, para recoger sustituciones agregadas después del `init()`. También se vuelve a sincronizar inmediatamente antes de pulsarlo.

Esto significa:

- si Embeddium sustituye naturalmente Video Settings, se abre Embeddium;
- si otro mod modifica esa acción, se conserva su modificación;
- si varios mods encadenan hooks sobre el flujo normal, Jobs no se interpone;
- si nadie lo modifica, actúa el botón vanilla;
- al cerrar la pantalla resultante, el padre sigue siendo `PantallaOpcionesJobs` porque esa es la instancia real sobre la que se construyó el callback.

`OptionsScreen.render()` no se usa porque volvería a dibujar fondo/título vanilla. Jobs reutiliza la lógica de inicialización/navegación, pero renderiza únicamente sus widgets propios.

`CompatGraficos` queda eliminado. No existe consulta directa a `embeddium`, `ConfigScreenHandler.ConfigScreenFactory`, `SodiumOptionsGUI` ni `EmbeddiumVideoOptionsScreen` desde la navegación Jobs.

`EscuchaCliente.esVideoIntocable()` continúa aislando las pantallas gráficas conocidas para que una GUI externa no reciba chrome, transiciones ni sustitución de clicks Jobs.

## Estado heredado 0.41.0

### Audio puntual y hard-stop

`RastreadorAudioJobs` conserva las instancias de eventos/FX ambientales Jobs. Al entrar a gameplay o cerrar visita, cada instancia conocida recibe `SoundManager.stop`. Antes de registrar otra, purga las ya finalizadas mediante `SoundManager.isActive`.

`MezclaAudio.ambiental()` resuelve el SoundEvent con respaldo `null`: un registro faltante produce silencio controlado, no una cueva vanilla.

### Música vanilla sin polling

`GestorMusica.atender()` ya no llama `MusicManager.stopPlaying()` cada tick. Una visita nueva corta la música vanilla una vez y `BloqueoMusicaVanillaJobs` intercepta cualquier `SoundSource.MUSIC` nuevo mientras Jobs posee el menú. Las pistas Jobs usan `MASTER`, por lo que su catálogo no se bloquea.

### Sesión idempotente

`SesionMenu.cerrar()` sólo repite un hard-stop si todavía existe sesión interna, música viva, camas ambientales o FX puntuales registrados. Esto conserva la defensa contra audio residual sin ejecutar cierres completos durante cada tick jugable después del primer corte.

### Multiplayer

F5/Actualizar guarda IP seleccionada y `getScrollAmount()`, reconstruye la pantalla Jobs y restaura una Entry nueva más `setScrollAmount()`. `resize()` captura ese mismo estado antes de que Minecraft vuelva a ejecutar `init()`, de modo que maximizar, redimensionar o cambiar escala GUI conserva contexto. `ServerList.save()` sólo se ejecuta cuando `cambiado` es verdadero.

### Config y UI hot-path

Los setters boolean/int comparan el valor actual antes de `set()`. Valores idénticos se omiten y no generan guardado. El perfil accesible evita reescribir valores ya correctos. `EscuchaCliente` mantiene una lista cacheada de botones vanilla relevantes para hover.

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

CI ejecuta política de versión, fondos, verificador general, UI/música, continuidad Multiplayer/documentación, optimización, créditos/reload, identidad musical/hard-stop, `tools/verificar_runtime_041.py`, `tools/verificar_graficos_041.py`, build Forge Java 17 y publicación versionada sólo desde `main` verde.

La validación visual, input, audio perceptivo y compatibilidad final con el modpack siguen siendo manuales en `test-1`.
