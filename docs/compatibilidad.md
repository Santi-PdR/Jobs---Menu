# Compatibilidad — Jobs Menu 0.42.0

## Perfil soportado

| Componente | Estado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente |
| Artefacto | `jobsmenu-0.42.0.jar` |

Jobs distingue entre pantallas que controla, pantallas Minecraft/Forge cuya lógica conserva y pantallas de terceros que debe respetar completamente.

## Frontera de gameplay

Con mundo/servidor cargado:

- no se crea ni dibuja `TransicionInterfazJobs`;
- chat, inventario, contenedores y pantallas no Jobs quedan fuera de skin/banda/reemplazo global de click;
- música, camas ambientales y FX puntuales del menú reciben hard-stop;
- Pausa/Config Jobs pueden mantener tema/feedback breve sin reactivar la sesión.

## Gráficos — flujo natural reforzado

`PantallaOpcionesJobs` **es un `OptionsScreen` real** y llama primero a `super.init()`. Los mixins y hooks del modpack trabajan sobre la misma instancia que encontrarían en el flujo normal de Minecraft.

Jobs conserva el `AbstractButton` de `options.video` y oculta los controles externos usados como backend. El botón Gráficos visible de Jobs ejecuta el `onPress()` del control natural. No construye `VideoSettingsScreen`, no llama factories de Embeddium y no enlaza clases internas de proveedores gráficos.

0.42 añade una segunda identidad para ese control: su ranura original (`x/y/ancho/alto`). Si un mod sustituye el botón después de `init()` y cambia también su etiqueta, Jobs puede reconocer el reemplazo si ocupa aproximadamente la misma ranura. Esto permite conservar más modificaciones naturales sin acoplarse al mod que las hizo.

Si no se puede resolver un control natural válido, Jobs no inventa una ruta de respaldo: emite feedback negado. La prioridad es no perder opciones de otros mods por abrir una Screen incorrecta.

`OptionsScreen.render()` no se ejecuta porque dibujaría otra vez fondo/título vanilla. Jobs reutiliza inicialización/callbacks y renderiza sólo widgets Jobs.

## Propiedad de pantallas de terceros

La exclusión deja de enumerar Sodium, Embeddium, Iris u Oculus. `EscuchaCliente.esPantallaTerceros()` aplica una regla general:

- `com.santipdr.jobsmenu.client.screen.*` → Jobs;
- `net.minecraft.*` → Minecraft;
- `net.minecraftforge.*` → Forge;
- cualquier otro namespace de `Screen` → tercero.

Una Screen de terceros no recibe:

- `PielVanillaJobs`;
- `ChromeExpediente.bandaContextual`;
- `PulidoInterfazJobs`;
- `TransicionInterfazJobs`;
- hover Jobs;
- reemplazo del click vanilla;
- gestión visual de listas Jobs.

`VideoSettingsScreen` vanilla se declara también intocable de forma explícita aunque esté bajo `net.minecraft.*`.

### Navegación interna de un mod

Una sesión Jobs puede seguir abierta mientras se visita una configuración externa, principalmente para mantener continuidad del menú fuera de gameplay. Eso **no** da permiso para secuestrar el flujo del tercero.

`flujoAdministrativo` exige que la pantalla anterior no sea de terceros. Por tanto, si un mod abre desde su GUI un `OptionsScreen`, `SelectWorldScreen`, `JoinMultiplayerScreen` o `ModListScreen`, Jobs no lo sustituye sólo porque `SesionMenu.activa()` sea verdadera.

Esta regla evita dependencias por nombres concretos y protege mods nuevos que Jobs nunca haya visto.

## Audio — estado heredado 0.41

### FX puntuales

`RastreadorAudioJobs` conserva referencias a los `SoundInstance` puntuales creados por `MezclaAudio.ambiental()`. Antes de registrar otro purga los finalizados con `SoundManager.isActive`; al cerrar visita llama `SoundManager.stop` sobre los restantes.

Un registro ambiental faltante se resuelve con `null`. No existe fallback a `SoundEvents.AMBIENT_CAVE` para sonidos Jobs.

### Música

El catálogo es exclusivamente Jobs y sin `SoundEvents.MUSIC_MENU`. `GestorMusica` corta el `MusicManager` una vez al iniciar visita. `BloqueoMusicaVanillaJobs` intercepta nuevas instancias `SoundSource.MUSIC` mientras `SesionMenu` está activa, sin polling por tick.

## Resource reload

`RecargaRecursosCliente` usa generación atómica y ejecuta invalidaciones en el hilo cliente. Además de música/camas, descarta referencias puntuales del motor anterior y reinicia estado de mezcla.

## Sesión

`SesionMenu.cerrar()` es idempotente: si no existe sesión interna ni música/camas/FX vivos, retorna sin repetir trabajo. Si aparece estado residual detectable, vuelve a ejecutar hard-stop.

## Config

Los setters boolean/int comprueban el valor actual antes de llamar `set()`. Valores idénticos no abren otra ventana de guardado. Los cambios reales conservan throttle y `guardarPendiente()` al abandonar/cambiar pantalla.

## Multiplayer

`PantallaMultijugadorJobs` conserva `ServerSelectionList`, pinger, favicons, MOTD y detector LAN reales.

- ESC/Cancelar usan padre Jobs directo y guard idempotente.
- F5/Actualizar reconstruye Jobs directamente.
- selección online se conserva por IP buscando una Entry nueva;
- scroll se conserva con `getScrollAmount()` / `setScrollAmount()`;
- `resize()` captura selección+scroll antes de reconstruir widgets;
- `ServerList.save()` sólo se ejecuta si la normalización cambió datos;
- cancelar/error pre-login vuelve a Jobs;
- logout/kick remoto vuelve a Multiplayer Jobs.

Servidor fijado único: `JobsDosh.exaroton.me:56477`.

## Pipeline de publicación

`dev-latest` es una release rodante, pero el asset mantiene versión. 0.42 añade un movimiento explícito del ref Git después del build verificado:

```text
git tag -f dev-latest "$GITHUB_SHA"
git push origin refs/tags/dev-latest --force
```

La verificación final debe confirmar que el SHA del tag coincide exactamente con el SHA de `main` que publicó el JAR.

## Fondos

- 10–17: PNG estrictamente estáticos;
- 18–31: JPG 1920×1080 con cover y respiración opcional mínima;
- Movimiento reducido/Bajo consumo/escena quieta congelan 18–31.

## Compatibilidad manual

Probar especialmente comparación lado a lado entre Gráficos natural y Gráficos desde Jobs, mods que cambien etiqueta/posición de Video Settings, configuraciones de mods no gráficos, submenús abiertos desde esas configuraciones, Embeddium/Oculus, mods que sustituyan `JoinMultiplayerScreen`, mods de audio, resource packs de GUI, múltiples F3+T, listas largas, LAN/ping/favicons, GUI Scale extremos y resize/maximizar.

Regla general: **si tematizar exige duplicar o adivinar la lógica de Minecraft/Forge/otro mod, se conserva la lógica real y Jobs reduce su intervención**.
