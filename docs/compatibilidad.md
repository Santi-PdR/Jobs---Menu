# Compatibilidad — Jobs Menu 0.44.0

## Perfil soportado

| Componente | Estado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente |
| Artefacto | `jobsmenu-0.44.0.jar` |

Jobs distingue entre pantallas que controla y pantallas ajenas que debe respetar completamente.

## Frontera de gameplay

Con mundo/servidor cargado:

- no se crea ni dibuja `TransicionInterfazJobs`;
- chat, inventario, contenedores y pantallas no Jobs quedan fuera de skin/banda/reemplazo global de click;
- música, camas ambientales y FX puntuales del menú reciben hard-stop;
- Pausa/Config Jobs pueden mantener tema/feedback breve sin reactivar la sesión.

## Gráficos — contrato 0.44

`PantallaOpcionesJobs` **ya no hereda de `OptionsScreen`**. No llama `super.init()`, no crea widgets vanilla/modded ocultos, no memoriza ranuras y no ejecuta un botón gráfico invisible.

El botón Gráficos de Jobs sólo decide qué Screen abrir:

- con Embeddium, `CompatGraficos` consulta su `ConfigScreenHandler.ConfigScreenFactory` registrado en Forge y usa la Screen devuelta por el propio mod;
- sin Embeddium o si la factory no entrega una Screen válida, se abre `VideoSettingsScreen` vanilla.

Después de obtener esa Screen, Jobs no la modifica. No hay wrapper, chrome, overlay, recolocación, transición ni reemplazo de click/hover.

`CompatGraficos` no usa reflection y no enlaza clases internas como `SodiumOptionsGUI` o `EmbeddiumVideoOptionsScreen`.

## MODPACK eliminado

El acceso MODPACK introducido en 0.42 se elimina por completo porque podía crear un ciclo de retorno en configuración. Ya no existen:

- botón `MODPACK`;
- `abrirOpcionesModpack()`;
- `permitirOptionsNaturalUnaVez`;
- `optionsNaturalSolicitado`;
- un `OptionsScreen` completo alternativo abierto desde Opciones Jobs.

## Propiedad de pantallas de terceros

`EscuchaCliente.esPantallaTerceros()` aplica una regla general:

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

`VideoSettingsScreen` vanilla se declara también intocable explícitamente.

### Navegación interna de terceros

`flujoExternoActivo` acompaña al usuario cuando una GUI externa abre otra Screen. Ese flujo no habilita sustituciones Jobs sólo porque la sesión del menú continúe activa.

0.44 endurece además el origen administrativo: `SesionMenu.activa()` ya no basta para sustituir `OptionsScreen`, `JoinMultiplayerScreen`, `SelectWorldScreen` o `ModListScreen`. Esas conversiones sólo nacen desde padres Jobs concretos: `PantallaNivel`, `PantallaEstancia` y `PantallaOpcionesJobs`.

Esto reduce capturas accidentales y bucles de retorno.

## Perfiles — 0.43 heredado

`PerfilesJobs.actual()` compara cada preset de forma explícita. Si una edición manual rompe un valor controlado por el preset, el estado pasa a `CUSTOM`. Opciones deliberadamente libres —como pista musical o nivel fijo— no invalidan el perfil.

## Mundos y Mods — 0.43 heredado

1. `Ctrl+F` enfoca búsqueda.
2. ESC con texto limpia el filtro.
3. ESC con filtro vacío pero foco activo abandona el campo.
4. El siguiente ESC vuelve al padre.

Ambas pantallas usan guard `cerrando` para evitar dos `setScreen()` por una misma salida.

## Audio / resource reload / sesión

- `RastreadorAudioJobs` corta FX puntuales al cerrar visita.
- No existe fallback a `AMBIENT_CAVE` ni `MUSIC_MENU`.
- Resource reload usa generación atómica y vuelve al hilo cliente.
- `SesionMenu.cerrar()` es idempotente.
- `MusicManager.stopPlaying()` no se ejecuta por tick.

## Config

Los setters comprueban el valor actual antes de guardar. Los cambios reales mantienen throttle y `guardarPendiente()` al abandonar/cambiar pantalla.

Opciones Jobs añade también un guard de cierre idempotente. El callback de resource packs sólo ejecuta `setScreen(this)` si esa Screen no es ya la actual.

## Multiplayer

- ESC/Cancelar usan padre Jobs directo y guard idempotente;
- F5/Actualizar reconstruye Jobs directamente;
- selección online se conserva por IP;
- scroll se conserva en F5 y resize;
- `ServerList.save()` sólo se ejecuta si la normalización cambió datos;
- cancelar/error pre-login vuelve a Jobs;
- logout/kick remoto vuelve a Multiplayer Jobs.

Servidor fijado único: `JobsDosh.exaroton.me:56477`.

## Pipeline de publicación

1. verificar y compilar;
2. publicar el JAR versionado;
3. mover `dev-latest` al `$GITHUB_SHA` publicado;
4. eliminar assets Jobs obsoletos.

## Fondos

- 10–17: PNG estrictamente estáticos;
- 18–31: JPG 1920×1080 con respiración opcional mínima;
- Movimiento reducido/Bajo consumo/escena quieta congelan 18–31.

## Compatibilidad manual prioritaria

Probar Embeddium real, fallback vanilla, retorno ESC/Done desde Gráficos, ausencia total de MODPACK, repetición de abrir/cerrar Opciones, configuraciones de otros mods, perfiles CUSTOM, búsqueda/ESC de Mundos y Mods, resource packs, F3+T, Multiplayer con listas largas, GUI Scale extremos y resize/maximizar.

Regla general: **si tematizar exige duplicar o adivinar lógica ajena, se conserva la Screen original y Jobs no la toca**.
