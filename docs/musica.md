# Música — Jobs Menu 0.41.0

## Catálogo empaquetado

1. **Absurdism** — `assets/jobsmenu/sounds/musica/defecto.ogg` — evento `musica.tema`.
2. **REQUIEM** — `assets/jobsmenu/sounds/musica/requiem.ogg` — evento `musica.requiem` — crédito **Emmy Z - Forsaken OST**. Fuente autorizada archivada: `music/REQUIEM-Forsaken-OST.ogg`.
3. **Upon the Hill V2** — `assets/jobsmenu/sounds/musica/upon_the_hill_v2.ogg` — evento `musica.upon_hill` — crédito **ft. @iCosmicCoffee**. Fuente autorizada archivada: `music/upon_the_hill_v2_q4.ogg`.

Los tres eventos usan `stream: true`. El build no descarga canciones ni depende de servicios externos.

## Identidad Jobs

`CATALOGO` se construye una sola vez por JVM. Una pista se resuelve mediante `MezclaAudio.resolver(pista.evento(), null)`: no existe fallback a `minecraft:music.menu`. Si una pista propia falta, se omite/reintenta sin retirar prematuramente la válida.

## 0.41 — MusicManager sin polling

Antes `GestorMusica.atender()` llamaba `MusicManager.stopPlaying()` en cada tick de menú. 0.41 cambia el modelo:

1. al iniciar una visita Jobs se corta una vez la música gestionada por Minecraft;
2. `BloqueoMusicaVanillaJobs` escucha `PlaySoundEvent` con prioridad alta;
3. mientras `SesionMenu` está activa, una nueva instancia `SoundSource.MUSIC` se cancela antes de entrar al motor;
4. las pistas Jobs usan `SoundSource.MASTER`, por lo que siguen reproduciéndose normalmente.

Esto evita 20 órdenes de stop por segundo y mantiene la banda sonora Jobs exclusiva. Música de otros mods que use `SoundSource.MUSIC` durante el menú también queda silenciada mientras Jobs está activo, deliberadamente.

## FX puntuales — 0.41

Eventos ambientales, apagones y sonidos sueltos creados por `MezclaAudio.ambiental()` ahora pasan por `RastreadorAudioJobs`.

- un SoundEvent faltante devuelve `null`; no hay fallback a `minecraft:ambient.cave`;
- antes de registrar una instancia nueva se purgan referencias que `SoundManager.isActive` ya considera finalizadas;
- al cerrar la visita/entrar a gameplay las instancias restantes reciben `SoundManager.stop`;
- resource reload descarta referencias ligadas al motor anterior.

Las camas continuas siguen administradas por `GestorAmbiente`; la música, por `GestorMusica`.

## Hard-stop

La frontera gameplay no usa fade. El corte cubre:

- música principal/entrante;
- camas ambientales activas;
- FX puntuales registrados.

`SesionMenu.cerrar()` es idempotente: después del corte inicial no repite el trabajo cada tick salvo que detecte estado Jobs residual.

## Créditos

`assets/jobsmenu/musica_creditada.txt` enumera `absurdism`, `requiem` y `upon_the_hill_v2`. `GestorMusica.creditoAlfa()` exige ese marcador y la opción `credito_musica`.

- Absurdism: título sin autor inventado.
- REQUIEM: `Emmy Z - Forsaken OST`.
- Upon the Hill V2: `ft. @iCosmicCoffee`.

## Selección y sesión

En Config Jobs > Audio: Aleatoria, Absurdism, REQUIEM o Upon the Hill V2. Una pista fija no rota y `N` sólo actúa en Aleatoria. `M` controla mute Jobs. Navegar entre pantallas Jobs sigue siendo una sola visita.

## Resource reload

Idioma, F3+T y Resource Packs pueden reconstruir el `SoundEngine`. `RecargaRecursosCliente` usa una generación atómica y procesa invalidaciones en el hilo cliente. 0.41 invalida además el rastreador de FX puntuales y reinicia el estado de aviso de registros faltantes de la mezcla.

## Feedback de interfaz

Los gestos UI son independientes de la sesión musical. Click/hover/toggle/volver/negado usan sonidos Jobs en superficies Jobs válidas; Video Settings conserva vanilla; chat/inventario/gameplay no Jobs quedan fuera.

## Prueba manual

1. Forzar las tres pistas y confirmar que ninguna deriva a música vanilla.
2. Probar Aleatoria + `N`, pista fija y crossfade.
3. Disparar un evento/apagón y entrar inmediatamente a gameplay: no debe quedar cola audible.
4. Permanecer en el menú suficiente tiempo para varios FX y verificar que no crece audio acumulado.
5. Ejecutar idioma → F3+T → resource pack.
6. Probar un mod/resource pack de audio y verificar que el bloqueo MUSIC sólo opera durante la visita Jobs.
7. Volver al menú y comprobar una única instancia por capa/pista.
