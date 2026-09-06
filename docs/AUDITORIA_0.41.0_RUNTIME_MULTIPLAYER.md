# Auditoría 0.41.0 — Runtime, audio y continuidad Multiplayer

## Objetivo

Reducir trabajo repetido que todavía quedaba después de 0.38–0.40 y cerrar audio de menú que no estaba representado por los gestores de música/camas, sin tocar Video Settings ni ampliar Jobs a gameplay.

## Hallazgos

1. `MezclaAudio.ambiental()` lanzaba `SimpleSoundInstance` puntuales y perdía la referencia inmediatamente. Música y camas tenían hard-stop explícito; estos FX no.
2. Los FX ambientales podían caer en `SoundEvents.AMBIENT_CAVE`, introduciendo identidad vanilla si faltaba un registro Jobs.
3. `SesionMenu.cerrar()` se invoca defensivamente desde gameplay y repetía el cierre completo aunque ya no quedara estado de audio vivo.
4. `GestorMusica.atender()` llamaba `MusicManager.stopPlaying()` en cada tick de menú para impedir música vanilla.
5. F5 conservaba servidor seleccionado por IP pero no el scroll de la lista.
6. `asegurarServidorOficial()` guardaba `servers.dat` incluso cuando no había nada que modificar.
7. Los setters de `ConfigTurno` llamaban `set()` y programaban persistencia aunque el valor solicitado ya fuera el actual.
8. El seguimiento de hover vanilla recorría todos los `children()` de la Screen en cada render.

## Correcciones

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
- generación de reload ya existente.

No se añade ninguna función visible de usuario.

## Protección CI

Nuevo `tools/verificar_runtime_041.py` fija:

- rastreador y hard-stop de FX;
- ausencia de fallback `AMBIENT_CAVE`;
- cierre idempotente;
- bloqueo de `SoundSource.MUSIC` y ausencia de `stopPlaying()` en `atender()`;
- no-op de config para valores idénticos;
- cache de hover;
- selección+scroll Multiplayer y save condicional;
- campos de diagnóstico.

Todos los verificadores históricos siguen ejecutándose antes del build Forge real.

## Prueba manual prioritaria

1. Disparar un FX puntual y entrar a un mundo durante su reproducción.
2. Permanecer en gameplay y confirmar que no reaparece audio ni se perciben cortes periódicos.
3. F5 en una lista larga, con servidor seleccionado y scroll lejos del inicio.
4. Abrir Multiplayer varias veces con servidor oficial ya correcto y observar que no hay efectos secundarios en `servers.dat`/lista.
5. Arrastrar sliders, salir de Config y reiniciar Minecraft para validar persistencia.
6. Probar F3+T/resource packs y mods de audio.
7. Confirmar que Video Settings y UI de gameplay permanecen fuera de Jobs.
