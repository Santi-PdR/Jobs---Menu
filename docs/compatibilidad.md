# Compatibilidad — Jobs Menu 0.41.0

## Perfil soportado

| Componente | Estado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente |
| Artefacto | `jobsmenu-0.41.0.jar` |

Jobs distingue entre pantallas que controla, pantallas vanilla/Forge cuya lógica conserva y pantallas de terceros que debe respetar.

## Frontera de gameplay

Con mundo/servidor cargado:

- no se crea ni dibuja `TransicionInterfazJobs`;
- chat, inventario, contenedores y pantallas no Jobs quedan fuera de skin/banda/reemplazo global de click;
- música, camas ambientales y FX puntuales del menú reciben hard-stop;
- Pausa/Config Jobs pueden mantener tema/feedback breve sin reactivar la sesión.

Video Settings queda fuera de Jobs incluso durante una visita de menú.

## Audio — 0.41

### FX puntuales

`RastreadorAudioJobs` conserva referencias a los `SoundInstance` puntuales creados por `MezclaAudio.ambiental()`. Antes de registrar otro purga los ya finalizados consultando `SoundManager.isActive`; al cerrar la visita llama `SoundManager.stop` sobre los restantes.

Un registro ambiental faltante se resuelve con `null`. Ya no existe fallback a `SoundEvents.AMBIENT_CAVE` para sonidos Jobs.

### Música

El catálogo sigue siendo exclusivamente Jobs y sin `SoundEvents.MUSIC_MENU`. `GestorMusica` corta el `MusicManager` una vez al iniciar visita. `BloqueoMusicaVanillaJobs` intercepta nuevas instancias `SoundSource.MUSIC` mientras `SesionMenu` está activa, reemplazando el antiguo `stopPlaying()` por tick. Las pistas Jobs se reproducen en `MASTER`, por lo que no son bloqueadas por ese guard.

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
- 0.41 conserva también `getScrollAmount()` y lo restaura con `setScrollAmount()`;
- normalizar el servidor oficial sólo llama `ServerList.save()` si hubo un cambio real;
- conectar usa la propia pantalla Jobs como padre de `ConnectScreen`;
- cancelar/error pre-login vuelve a la lista Jobs;
- logout/kick remoto vuelve a Multiplayer Jobs.

Servidor fijado único: `JobsDosh.exaroton.me:56477`.

## Hover y listas

`EscuchaCliente` cachea los `AbstractButton` vanilla que pueden recibir feedback Jobs. La caché se reconstruye al inicializar/cambiar Screen o variar su cantidad de hijos; el render normal recorre sólo esa lista, no todos los children.

`ListasExpediente` sigue siendo visual: wheel, drag, click, foco y contenido pertenecen a la lista real. Reflection/listas permanecen cacheadas y una scrollbar Jobs no se dibuja dos veces por frame.

## Fondos

- 10–17: PNG estrictamente estáticos;
- 18–31: JPG 1920×1080 con cover y respiración opcional mínima;
- Movimiento reducido/Bajo consumo/escena quieta congelan 18–31.

## Compatibilidad manual

Probar especialmente Embeddium/Oculus, mods que sustituyan `JoinMultiplayerScreen`, mods de audio/MusicManager/SoundEngine, resource packs de GUI, múltiples F3+T, listas de servidores largas, LAN/ping/favicons, GUI Scale extremos y salida/kick con reemplazos de `DisconnectedScreen`.

Regla general: **si tematizar exige duplicar la lógica de Minecraft/Forge, se conserva la lógica real y se reduce la intervención visual**.
