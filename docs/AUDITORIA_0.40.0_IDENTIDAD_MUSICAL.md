# Auditoría 0.40.0 — identidad musical y hard-stop

Fecha: 2026-09-05
Base: 0.39.0
Alcance: audio de menú, lifecycle y documentación. Sin cambios de gameplay, Video Settings, Multiplayer ni fondos.

## Problemas encontrados

### Fallback musical vanilla

`GestorMusica.resolverPista()` resolvía cualquier pista Jobs contra `SoundEvents.MUSIC_MENU.value()` como respaldo. Si un registro propio fallaba, el usuario podía escuchar música vanilla desde una instancia gestionada por Jobs. Eso era seguro contra crash, pero rompía identidad y hacía más difícil diagnosticar un registro faltante.

### Catálogo recreado

`catalogo()` devolvía `new Pista[]` en cada consulta. El método se usa en arranque, sincronización de selector, crossfade, cambio manual, créditos y diagnóstico. La colección sólo tiene tres entradas y no cambia durante la JVM, por lo que recrearla repetidamente no aportaba nada.

### Corte dependiente de la instancia

El hard-stop ponía volumen/ganancias a cero y llamaba `stop()` sobre la instancia. 0.40 añade también una orden directa al `SoundManager` para que la frontera gameplay/reload no dependa únicamente del siguiente mantenimiento interno del motor.

## Cambios

- `CATALOGO` es `static final` y se construye una sola vez.
- `catalogo()` devuelve `CATALOGO` para mantener la firma interna histórica sin asignaciones nuevas.
- título, autor y cantidad consultan directamente `CATALOGO`.
- `resolverPista()` usa `MezclaAudio.resolver(..., null)`; no importa ni usa `SoundEvents.MUSIC_MENU`.
- una pista que no resuelve no crea `GestorMusica` y no fuerza la salida de la pista actual.
- cambios fijo/manual/automático crean primero la entrante y sólo luego inician crossfade.
- fallo de registro programa reintento y registra una advertencia única por visita/reload.
- hard-stop inmediato llama `stop()` y `SoundManager.stop(instance)`.

## CI

Se añade `tools/verificar_audio_identidad.py`, que falla si:

- reaparece `SoundEvents.MUSIC_MENU` o `minecraft:music.menu` en `GestorMusica`;
- `catalogo()` vuelve a construir arrays;
- desaparece `CATALOGO`;
- la resolución propia deja de usar `null` como respaldo;
- se pierde la llamada directa a `SoundManager.stop(instance)`;
- CHANGELOG/documentación dejan de reflejar 0.40.0.

## Contratos preservados

- hard-stop de música/ambiente en gameplay;
- créditos y generación de resource reload de 0.39;
- sonidos Jobs de UI sin fallback vanilla;
- Video Settings completamente vanilla;
- chat/inventario/contenedores fuera de Jobs;
- retorno y F5 de Multiplayer Jobs;
- servidor oficial único;
- PNG 10–17 estáticos;
- JPG 18–31 no destructivos;
- optimizaciones 0.38 y build reproducible.

## Prueba manual requerida

- reproducir las tres pistas en modo fijo y Aleatoria;
- confirmar ausencia de música vanilla en reloads/errores;
- entrar a gameplay durante pista y crossfade;
- repetir idioma/F3+T/resource pack;
- volver al menú y comprobar una sola instancia audible;
- revisar `latest.log` ante cualquier pista ausente.
