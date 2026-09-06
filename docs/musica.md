# Música — Jobs Menu 0.40.0

## Catálogo empaquetado

1. **Absurdism** — `assets/jobsmenu/sounds/musica/defecto.ogg` — evento `musica.tema`.
2. **REQUIEM** — `assets/jobsmenu/sounds/musica/requiem.ogg` — evento `musica.requiem` — crédito **Emmy Z - Forsaken OST**. Fuente autorizada archivada: `music/REQUIEM-Forsaken-OST.ogg`.
3. **Upon the Hill V2** — `assets/jobsmenu/sounds/musica/upon_the_hill_v2.ogg` — evento `musica.upon_hill` — crédito **ft. @iCosmicCoffee**. Fuente autorizada archivada: `music/upon_the_hill_v2_q4.ogg`.

Los tres eventos usan `stream: true` en `sounds.json`. El build no descarga canciones ni depende de servicios externos.

## 0.40.0 — identidad Jobs sin fallback vanilla

El catálogo de runtime vive en `CATALOGO` y se construye una sola vez por JVM. `catalogo()` devuelve esa misma estructura; título, autor, cantidad y crossfade no crean arrays nuevos por consulta.

La resolución de una pista usa `MezclaAudio.resolver(pista.evento(), null)`. Por diseño **no existe fallback a `minecraft:music.menu`**. Si el SoundEvent propio no está disponible:

- la pista entrante no se crea;
- la pista actual no se empieza a retirar;
- se programa reintento;
- se registra una advertencia única por visita/reload;
- el resultado temporal es silencio/continuidad de la pista válida, nunca música vanilla.

Esto separa la identidad Jobs de cualquier recuperación defensiva del motor.

## Hard-stop reforzado

Al cortar una instancia musical de forma inmediata:

1. volumen y ganancias quedan en cero;
2. se marca `stop()`;
3. se llama también `SoundManager.stop(instance)`.

Se usa al entrar a gameplay, cerrar la sesión, reconstruir el motor tras resource reload o recuperar una instancia fantasma. No se hace fade en la frontera gameplay.

## Marcador de catálogo acreditado

`GestorMusica.creditoAlfa()` sólo permite mostrar créditos cuando existe `assets/jobsmenu/musica_creditada.txt`, que enumera:

- `absurdism`;
- `requiem`;
- `upon_the_hill_v2`.

El marcador no contiene audio ni modifica la reproducción; habilita la presentación de créditos del catálogo empaquetado.

## Selección

En `Ajustes del aviso > Audio` se puede elegir Aleatoria, Absurdism, REQUIEM o Upon the Hill V2. La selección vive en `pista_musica` (0–3). Una pista fija no rota automáticamente y `N` no la sustituye. Al volver a Aleatoria se conserva la pista actual y se reactiva la rotación posterior.

## Sesión

- La música pertenece a `SesionMenu`, no a una Screen.
- Navegar Main → Options → Mods → Recursos → volver sigue siendo una sola visita.
- `SesionMenu.abrir()` no reinicializa una visita ya activa.
- El cambio automático usa crossfade y evita repetición inmediata.
- `N` en Aleatoria solicita cambio manual y no apila otro crossfade.
- `M` controla mute Jobs.
- Entrar a mundo/servidor aplica hard-stop inmediato.
- Pausa/configuración Jobs dentro de gameplay puede emitir feedback breve sin abrir sesión musical.

## Resource reload

Idioma, F3+T y Resource Packs pueden reconstruir el `SoundEngine`. `RecargaRecursosCliente` incrementa una generación y agenda invalidación en el hilo cliente. Si aparece otra generación mientras se procesa la anterior, se agenda otra pasada.

Después del reload, música/ambiente ligados al motor anterior se descartan, el marcador de créditos vuelve a evaluarse y el mantenimiento normal recrea sólo las instancias válidas.

## Créditos visibles

- Absurdism: título sin autor inventado.
- REQUIEM: `Emmy Z - Forsaken OST`.
- Upon the Hill V2: `ft. @iCosmicCoffee`.

El bloque sólo aparece durante su ventana temporal y puede desactivarse desde Config Jobs.

## Feedback de interfaz

Los gestos UI son independientes de la sesión musical: click/hover/toggle/volver/negado usan sonidos Jobs en superficies Jobs válidas; Video Settings conserva vanilla; chat/inventario/gameplay no Jobs quedan fuera; F5 de Multiplayer emite un único `UI_ALTERNAR` sin cambiar la pista.

## Prueba manual

1. Forzar las tres pistas y verificar que ninguna deriva a música vanilla.
2. Probar Aleatoria + `N` y cambios fijos durante crossfade.
3. Entrar a gameplay durante pista normal y durante crossfade: el audio Jobs debe desaparecer inmediatamente.
4. Ejecutar idioma → F3+T → resource pack en secuencia corta.
5. Volver al menú y comprobar una única instancia de música/ambiente.
6. Probar Alt+Tab y F3+T repetido para detectar instancias fantasma.
