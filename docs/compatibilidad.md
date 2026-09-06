# Compatibilidad — Jobs Menu 0.41.1

## Perfil soportado

| Componente | Estado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente |
| Artefacto | `jobsmenu-0.41.1.jar` |

Jobs distingue entre pantallas que controla, pantallas vanilla/Forge cuya lógica conserva y pantallas de terceros que debe respetar.

## Frontera de gameplay

Con mundo/servidor cargado:

- no se crea ni dibuja `TransicionInterfazJobs`;
- chat, inventario, contenedores y pantallas no Jobs quedan fuera de skin/banda/reemplazo global de click;
- música, camas ambientales y FX puntuales del menú reciben hard-stop;
- Pausa/Config Jobs pueden mantener tema/feedback breve sin reactivar la sesión.

## Gráficos — flujo natural del modpack

0.41.1 deja de integrar un proveedor gráfico concreto. `PantallaOpcionesJobs` **es un `OptionsScreen` real** y llama primero a `super.init()`. Esto permite que mixins y hooks de Embeddium, Oculus y otros mods modifiquen la misma instancia que encontrarían en el flujo normal de Minecraft.

Después de esa inicialización Jobs conserva el `AbstractButton` de `options.video` y oculta los controles externos usados como backend. El botón Gráficos visible de Jobs ejecuta el `onPress()` del control natural. No construye `VideoSettingsScreen`, no llama un factory de Embeddium y no enlaza clases internas de mods gráficos.

La captura se repite en el primer render —cuando ya terminó el ciclo de inicialización de Forge— y justo antes de usarla. Por eso también se recogen reemplazos tardíos del botón.

`OptionsScreen.render()` no se ejecuta: dibujaría otra vez el fondo y el título vanilla. Jobs reutiliza su lógica de inicialización y callbacks, pero renderiza sólo widgets Jobs.

La exclusión visual/sonora continúa reconociendo:

- `VideoSettingsScreen` vanilla;
- `me.jellysquid.mods.sodium.client.gui.*`;
- `org.embeddedt.embeddium.gui.*`;
- `org.embeddedt.embeddium.impl.gui.*`;
- pantallas gráficas conocidas de Iris/Oculus.

Esas GUI no reciben chrome Jobs, transición, recolocación de widgets ni reemplazo de clicks. Si un proveedor futuro usa clases distintas, la ruta de apertura seguirá siendo natural, aunque puede requerir ampliar la exclusión visual.

## Audio — 0.41

### FX puntuales

`RastreadorAudioJobs` conserva referencias a los `SoundInstance` puntuales creados por `MezclaAudio.ambiental()`. Antes de registrar otro purga los ya finalizados consultando `SoundManager.isActive`; al cerrar la visita llama `SoundManager.stop` sobre los restantes.

Un registro ambiental faltante se resuelve con `null`. Ya no existe fallback a `SoundEvents.AMBIENT_CAVE` para sonidos Jobs.

### Música

El catálogo sigue siendo exclusivamente Jobs y sin `SoundEvents.MUSIC_MENU`. `GestorMusica` corta el `MusicManager` una vez al iniciar visita. `BloqueoMusicaVanillaJobs` intercepta nuevas instancias `SoundSource.MUSIC` mientras `SesionMenu` está activa, reemplazando el antiguo `stopPlaying()` por tick. Las pistas Jobs se reproducen en `MASTER`.

Mods que inyecten música propia de menú mediante `SoundSource.MUSIC` quedan silenciados durante la visita Jobs por diseño; Jobs posee la banda sonora de su menú.

## Resource reload

`RecargaRecursosCliente` usa generación atómica y ejecuta invalidaciones en el hilo cliente. Además de música/camas, 0.41 descarta referencias puntuales del motor anterior y reinicia el aviso de registros faltantes de `MezclaAudio`.

## Sesión

`SesionMenu.cerrar()` es idempotente: si no existe sesión interna ni música/camas/FX vivos, retorna sin repetir el trabajo. Si aparece estado residual detectable, vuelve a ejecutar hard-stop.

## Config

Los setters boolean/int comprueban el valor actual antes de llamar `set()`. Valores idénticos no abren una nueva ventana de guardado. Los cambios reales conservan el throttle de 250 ms y `guardarPendiente()` al abandonar/cambiar pantalla. El perfil accesible muta sólo los campos que realmente necesitan cambio.

## Multiplayer

`PantallaMultijugadorJobs` conserva `ServerSelectionList`, pinger, favicons, MOTD y detector LAN reales.

- ESC/Cancelar usan padre Jobs directo y guard idempotente.
- F5/Actualizar reconstruye Jobs directamente.
- se conserva la selección online por IP buscando una Entry nueva;
- se conserva `getScrollAmount()` y se restaura con `setScrollAmount()`;
- `resize()` captura selección+scroll antes de que Minecraft reconstruya widgets;
- normalizar el servidor oficial sólo llama `ServerList.save()` si hubo un cambio real;
- conectar usa la propia pantalla Jobs como padre de `ConnectScreen`;
- cancelar/error pre-login vuelve a la lista Jobs;
- logout/kick remoto vuelve a Multiplayer Jobs.

Servidor fijado único: `JobsDosh.exaroton.me:56477`.

## Hover y listas

`EscuchaCliente` cachea los `AbstractButton` vanilla que pueden recibir feedback Jobs. La caché se reconstruye al inicializar/cambiar Screen o variar su cantidad de hijos.

`ListasExpediente` sigue siendo visual: wheel, drag, click, foco y contenido pertenecen a la lista real. Reflection/listas permanecen cacheadas y una scrollbar Jobs no se dibuja dos veces por frame.

## Fondos

- 10–17: PNG estrictamente estáticos;
- 18–31: JPG 1920×1080 con cover y respiración opcional mínima;
- Movimiento reducido/Bajo consumo/escena quieta congelan 18–31.

## Compatibilidad manual

Probar especialmente la **comparación lado a lado entre Gráficos natural y Gráficos desde Jobs**, Embeddium/Oculus, mods que agreguen opciones de vídeo, abrir/cerrar Gráficos repetidamente, volver por ESC/Done a Opciones Jobs, mods que sustituyan `JoinMultiplayerScreen`, mods de audio, resource packs de GUI, múltiples F3+T, listas largas, LAN/ping/favicons, GUI Scale extremos y resize/maximizar.

Regla general: **si tematizar exige duplicar la lógica de Minecraft/Forge o de un proveedor, se conserva la lógica real y se reduce la intervención visual**.
