# Auditoría 0.41.0 — Runtime, audio, Embeddium y continuidad Multiplayer

## Objetivo

Reducir trabajo repetido que todavía quedaba después de 0.38–0.40, cerrar audio de menú que no estaba representado por los gestores de música/camas, corregir la delegación de Gráficos hacia Embeddium y mejorar continuidad de Multiplayer sin ampliar Jobs a gameplay.

## Hallazgos

1. El botón Gráficos de `PantallaOpcionesJobs` construía siempre `VideoSettingsScreen`, por lo que podía saltarse la GUI real de Embeddium.
2. Embeddium 1.20.1 registra oficialmente un `ConfigScreenHandler.ConfigScreenFactory` en Forge que crea su pantalla real (`SodiumOptionsGUI`); por tanto no hace falta reflection ni importar clases internas.
3. El detector anterior de pantalla gráfica externa exigía que el nombre contuviera `embeddium/sodium`, `video` y `screen`; eso no cubría correctamente `SodiumOptionsGUI`.
4. `MezclaAudio.ambiental()` lanzaba `SimpleSoundInstance` puntuales y perdía la referencia inmediatamente. Música y camas tenían hard-stop explícito; estos FX no.
5. Los FX ambientales podían caer en `SoundEvents.AMBIENT_CAVE`, introduciendo identidad vanilla si faltaba un registro Jobs.
6. `SesionMenu.cerrar()` se invoca defensivamente desde gameplay y repetía el cierre completo aunque ya no quedara estado de audio vivo.
7. `GestorMusica.atender()` llamaba `MusicManager.stopPlaying()` en cada tick de menú para impedir música vanilla.
8. F5 conservaba servidor seleccionado por IP pero no el scroll de la lista.
9. Incluso con F5 corregido, `Screen.resize()` podía reconstruir Multiplayer y perder selección/scroll al maximizar, redimensionar o cambiar GUI Scale.
10. `asegurarServidorOficial()` guardaba `servers.dat` incluso cuando no había nada que modificar.
11. Los setters de `ConfigTurno` llamaban `set()` y programaban persistencia aunque el valor solicitado ya fuera el actual.
12. El seguimiento de hover vanilla recorría todos los `children()` de la Screen en cada render.
13. CI seguía usando `actions/checkout@v4`; la versión oficial vigente comprobada durante esta tanda es v7.

## Correcciones

### Gráficos / Embeddium

Se añade `CompatGraficos` como puente opcional:

- busca el contenedor `embeddium` con `ModList`;
- obtiene su `ConfigScreenHandler.ConfigScreenFactory` mediante el extension point de Forge;
- llama la `screenFunction` con Opciones Jobs como pantalla anterior;
- no usa `Class.forName`, reflection ni imports de Embeddium;
- si no existe proveedor o falla la construcción, devuelve `null` y `PantallaOpcionesJobs` abre `VideoSettingsScreen` vanilla como fallback;
- un fallo de factory se registra una sola vez y no rompe el menú.

`EscuchaCliente.esVideoIntocable()` deja de adivinar por palabras sueltas y reconoce por prefijo:

- `me.jellysquid.mods.sodium.client.gui.*`;
- `org.embeddedt.embeddium.gui.*`;
- `org.embeddedt.embeddium.impl.gui.*`;
- pantallas conocidas de Iris/Oculus;
- `VideoSettingsScreen` vanilla.

Esas superficies quedan fuera de chrome/banda Jobs, transiciones y sustitución de clicks/hover. Además se elimina la conversión a minúsculas que antes se hacía durante cada consulta.

### `RastreadorAudioJobs`

Nueva capa de lifecycle para FX puntuales Jobs:

- registra cada `SoundInstance` ambiental puntual;
- purga referencias terminadas mediante `SoundManager.isActive`;
- aplica `SoundManager.stop` a todas las instancias conocidas al cerrar sesión;
- descarta referencias al producirse resource reload;
- expone sólo contadores internos para diagnóstico.

`MezclaAudio.ambiental()` usa ahora `resolver(evento, null)`: si el registro falta, se omite sin reproducir `ambient.cave` vanilla.

### Sesión

`SesionMenu.cerrar()` calcula si todavía necesita actuar a partir de:

- flag interno de visita;
- música Jobs viva;
- número de camas ambientales;
- número de FX puntuales registrados.

Si todo está limpio retorna inmediatamente. El primer ingreso a gameplay sigue ejecutando el hard-stop completo porque la sesión todavía está activa.

### Música vanilla

Se elimina el `stopPlaying()` desde el camino por tick de `GestorMusica.atender()`.

- `nuevaVisita()` corta el MusicManager una sola vez;
- `BloqueoMusicaVanillaJobs` cancela nuevas instancias `SoundSource.MUSIC` mediante `PlaySoundEvent` mientras `SesionMenu` está activa;
- la música Jobs usa `MASTER`, por lo que queda fuera de ese bloqueo.

### Multiplayer

`PantallaMultijugadorJobs` conserva en refresh:

- IP de la Entry online seleccionada;
- `ServerSelectionList.getScrollAmount()`.

La nueva pantalla busca una Entry fresca por IP, ejecuta `onSelectedChange()` y restaura scroll con `setScrollAmount()`.

Además, `resize(Minecraft, width, height)` captura IP y scroll **antes** de delegar en `super.resize()`. Como Minecraft reconstruye widgets durante resize, `init()` puede restaurar el mismo contexto al maximizar, redimensionar o cambiar GUI Scale.

La normalización del servidor oficial mantiene un `boolean cambiado`. `ServerList.save()` y `updateOnlineServers()` se ejecutan sólo si hubo una modificación real.

### Config

Los helpers `fijar(BooleanValue/IntValue)` comparan primero `get()` con el valor nuevo. Una solicitud idéntica:

- no llama `set()`;
- no abre guardado pendiente;
- incrementa sólo una métrica interna de cambio omitido.

El perfil accesible aplica el mismo criterio a su flag y las cuatro opciones agrupadas.

### Hover vanilla

`EscuchaCliente` conserva una lista de `AbstractButton` vanilla relevantes. Se reconstruye al inicializar/cambiar la Screen o variar su número de hijos. El render normal recorre sólo esa caché y mantiene el `WeakHashMap` de estado hover existente.

## Resource reload

La generación atómica de 0.39 permanece. 0.41 añade a la pasada del hilo cliente:

- `RastreadorAudioJobs.recursosRecargados()`;
- `MezclaAudio.recursosRecargados()`.

No se manipulan `SoundInstance` desde el executor de recursos.

## Diagnóstico

El diagnóstico interno añade:

- estado seguro/interno de `SesionMenu` y cierres efectivos;
- FX puntuales activos, registrados, purgados y barridos;
- cambios de config aplicados/omitidos, guardados y edad del último save;
- generación de reload ya existente;
- presencia de Embeddium, aperturas mediante factory y fallbacks vanilla de Gráficos.

No se añade ninguna función visible de usuario.

## Protección CI

`tools/verificar_runtime_041.py` fija:

- rastreador y hard-stop de FX;
- ausencia de fallback `AMBIENT_CAVE`;
- cierre idempotente;
- bloqueo de `SoundSource.MUSIC` y ausencia de `stopPlaying()` en `atender()`;
- no-op de config para valores idénticos;
- cache de hover;
- selección+scroll Multiplayer, continuidad en resize y save condicional;
- campos de diagnóstico.

Nuevo `tools/verificar_graficos_041.py` fija:

- uso del `ConfigScreenFactory` de Forge para Embeddium;
- ausencia de reflection;
- fallback vanilla explícito;
- exclusión de Sodium/Embeddium de los efectos Jobs.

GitHub Actions actualiza `actions/checkout` a v7. Todos los verificadores históricos siguen ejecutándose antes del build Forge real con Java 17.

## Prueba manual prioritaria

1. Con Embeddium instalado, abrir Gráficos y confirmar que aparece su GUI real; volver por ESC/Done a Opciones Jobs.
2. Repetir sin Embeddium y confirmar fallback vanilla.
3. Confirmar que Embeddium/Oculus no reciben marco, banda, transición ni click Jobs.
4. Disparar un FX puntual y entrar a un mundo durante su reproducción.
5. Permanecer en gameplay y confirmar que no reaparece audio ni se perciben cortes periódicos.
6. F5 en una lista larga, con servidor seleccionado y scroll lejos del inicio.
7. Maximizar/redimensionar/cambiar GUI Scale en Multiplayer y confirmar que selección+scroll se conservan.
8. Abrir Multiplayer varias veces con servidor oficial ya correcto y observar que no hay efectos secundarios en `servers.dat`/lista.
9. Arrastrar sliders, salir de Config y reiniciar Minecraft para validar persistencia.
10. Probar F3+T/resource packs y mods de audio.
11. Confirmar que chat, inventario, contenedores y UI normal de gameplay permanecen fuera de Jobs.
