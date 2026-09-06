# Compatibilidad — Jobs Menu 0.43.0

## Perfil soportado

| Componente | Estado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente |
| Artefacto | `jobsmenu-0.43.0.jar` |

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

La segunda identidad del control es su ranura original (`x/y/ancho/alto`). Si un mod sustituye el botón después de `init()` y cambia también su etiqueta, Jobs puede reconocer el reemplazo si ocupa aproximadamente la misma ranura. Esto permite conservar modificaciones naturales sin acoplarse al proveedor.

Si no se puede resolver un control natural válido, Jobs no inventa una ruta de respaldo: emite feedback negado. La prioridad es no perder opciones de otros mods por abrir una Screen incorrecta.

`OptionsScreen.render()` no se ejecuta porque dibujaría otra vez fondo/título vanilla. Jobs reutiliza inicialización/callbacks y renderiza sólo widgets Jobs.

El botón **MODPACK** abre el `OptionsScreen` completo y natural con un permiso de un solo uso. Ese acceso sirve como garantía para cualquier botón o inyección que Jobs no conozca.

## Propiedad de pantallas de terceros

La exclusión no enumera Sodium, Embeddium, Iris u Oculus. `EscuchaCliente.esPantallaTerceros()` aplica una regla general:

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

### Navegación interna de un mod — 0.43

Una sesión Jobs puede seguir abierta mientras se visita una configuración externa, principalmente para mantener continuidad del menú fuera de gameplay. Eso **no** da permiso para secuestrar el flujo del tercero.

`flujoExternoActivo` acompaña al usuario aunque una GUI externa abra después una Screen vanilla. Mientras ese marcador siga activo:

- Options/Worlds/Multiplayer/Mods vanilla no se convierten en equivalentes Jobs;
- `TitleScreen` tampoco es reemplazada por `PantallaNivel`;
- una pausa vanilla no se convierte en `PantallaEstancia`;
- no se añaden piel, bandas, transiciones, hover/click ni trabajo de listas Jobs.

El marcador se limpia al regresar explícitamente a una Screen Jobs, al entrar/salir de gameplay o cuando el flujo termina. Los retornos reales desde mundo/servidor conservan el comportamiento Jobs porque usan su estado de retorno específico antes de estas reglas.

## Perfiles — detección exacta 0.43

Los presets no son una segunda configuración paralela: escriben valores reales de `ConfigTurno`. Por eso el indicador de perfil sólo puede afirmar que un preset está activo cuando los valores que **ese preset controla** siguen coincidiendo.

`PerfilesJobs.actual()` compara cada preset de forma explícita: escena, movimiento/destellos, contraste/texto, papel/interfaz, guía/avisos, eventos/presencia/respiración, suspensión, rotación, bajo consumo, duraciones y volúmenes correspondientes. Si una edición manual relevante rompe esa combinación, el estado pasa a `CUSTOM`.

Opciones deliberadamente ajenas al preset —como la pista musical seleccionada o el nivel fijo— no invalidan el perfil.

## Mundos y Mods — búsqueda/cierre 0.43

`PantallaMundosJobs` y `PantallaModsJobs` usan el mismo contrato de teclado:

1. `Ctrl+F` enfoca la búsqueda;
2. `ESC` con texto escrito limpia el filtro;
3. `ESC` con el filtro vacío pero foco activo abandona el campo;
4. el siguiente `ESC` vuelve al padre.

Ambas pantallas usan además un guard `cerrando` que evita que dos rutas de cierre cercanas llamen dos veces a `setScreen()`.

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

`dev-latest` es una release rodante, pero el asset mantiene versión. El flujo estable es:

1. verificar y compilar;
2. publicar el JAR versionado;
3. mover `dev-latest` al `$GITHUB_SHA` publicado;
4. eliminar assets Jobs obsoletos.

La verificación final debe confirmar que el SHA del tag coincide exactamente con el SHA de `main` que publicó el JAR.

## Fondos

- 10–17: PNG estrictamente estáticos;
- 18–31: JPG 1920×1080 con cover y respiración opcional mínima;
- Movimiento reducido/Bajo consumo/escena quieta congelan 18–31.

## Compatibilidad manual

Probar especialmente comparación lado a lado entre Gráficos natural y Gráficos desde Jobs, MODPACK, configuraciones de mods no gráficos, submenús y `TitleScreen` abiertos desde esas configuraciones, perfiles modificados a mano, búsqueda/ESC de Mundos y Mods, Embeddium/Oculus, mods que sustituyan `JoinMultiplayerScreen`, mods de audio, resource packs de GUI, múltiples F3+T, listas largas, LAN/ping/favicons, GUI Scale extremos y resize/maximizar.

Regla general: **si tematizar exige duplicar o adivinar la lógica de Minecraft/Forge/otro mod, se conserva la lógica real y Jobs reduce su intervención**.
