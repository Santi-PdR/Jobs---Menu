# Música — Jobs Menu 0.39.0

## Catálogo empaquetado

1. **Absurdism** — `assets/jobsmenu/sounds/musica/defecto.ogg` — evento `musica.tema`.
2. **REQUIEM** — `assets/jobsmenu/sounds/musica/requiem.ogg` — evento `musica.requiem` — crédito **Emmy Z - Forsaken OST**.
3. **Upon the Hill V2** — `assets/jobsmenu/sounds/musica/upon_the_hill_v2.ogg` — evento `musica.upon_hill` — crédito **ft. @iCosmicCoffee**.

Los tres eventos usan `stream: true` en `sounds.json`. El build no descarga canciones ni depende de servicios externos.

## Marcador de catálogo acreditado

`GestorMusica.creditoAlfa()` sólo permite mostrar créditos cuando existe:

`assets/jobsmenu/musica_creditada.txt`

En una etapa antigua ese marcador se había eliminado porque todavía no representaba correctamente la pista empaquetada de entonces. El catálogo actual sí contiene tres pistas identificadas y documentadas, por lo que 0.39.0 restaura el marcador con estas ids:

- `absurdism`;
- `requiem`;
- `upon_the_hill_v2`.

El marcador no contiene audio ni modifica la reproducción. Es una compuerta explícita para la presentación del crédito.

## Selección

En `Ajustes del aviso > Audio` se puede elegir:

- Aleatoria;
- Absurdism;
- REQUIEM;
- Upon the Hill V2.

La selección vive en `pista_musica` (0–3). Una pista fija no rota automáticamente y `N` no la sustituye. Al volver a Aleatoria, el gestor conserva la pista actual y reactiva la rotación posterior.

## Sesión

- La música pertenece a `SesionMenu`, no a una Screen.
- Una visita nueva elige pista aleatoria o fija según configuración.
- Navegar Main → Options → Mods → Recursos → volver sigue siendo una sola visita.
- Desde 0.39.0 `SesionMenu.abrir()` no vuelve a inicializar ambiente/música si la visita ya estaba activa.
- El cambio automático usa crossfade y evita repetición inmediata.
- `N` en Aleatoria solicita cambio manual y no apila otro crossfade.
- `M` controla mute Jobs.
- Entrar a mundo/servidor aplica hard-stop inmediato a música y ambiente.
- Pausa/configuración Jobs dentro de gameplay puede emitir feedback breve sin abrir una nueva sesión musical.

## Resource reload — 0.39

Idioma, F3+T y Resource Packs pueden reconstruir el `SoundEngine`. `RecargaRecursosCliente` no toca instancias desde el executor de recursos: incrementa una generación y agenda la invalidación en el hilo cliente.

Si aparece otra generación mientras se está procesando la anterior, se agenda otra pasada. Así una ráfaga de reloads no debe dejar una invalidación posterior sin procesar.

Después del reload:

- `GestorMusica.recursosRecargados()` descarta instancias viejas y reinicia su ventana de reintento;
- `GestorAmbiente.recursosRecargados()` cierra camas ligadas al motor anterior;
- si la visita Jobs sigue activa, el mantenimiento normal recrea el audio correspondiente;
- el marcador de créditos vuelve a evaluarse contra el ResourceManager nuevo.

## Créditos visibles

`PantallaNivel` consulta la pista dominante real durante crossfade:

- Absurdism: título sin autor inventado;
- REQUIEM: `Emmy Z - Forsaken OST`;
- Upon the Hill V2: `ft. @iCosmicCoffee`.

El bloque sólo aparece durante su ventana de entrada/permanencia/salida y puede desactivarse desde Config Jobs.

## Feedback de interfaz

Los gestos de UI son independientes de la sesión musical:

- click/hover/toggle/volver/negado usan sonidos Jobs en superficies Jobs válidas;
- Video Settings conserva su sonido vanilla;
- chat, inventario y gameplay no Jobs quedan fuera;
- F5 de Multiplayer emite un único `UI_ALTERNAR` sin cambiar/reiniciar la pista.

## Prueba manual

1. Forzar cada una de las tres pistas y verificar título/autor.
2. Volver a Aleatoria y probar `N` durante y fuera de crossfade.
3. Navegar por varias subpantallas y confirmar continuidad de la pista.
4. Ejecutar idioma → F3+T → resource pack en secuencia corta y comprobar una sola reconstrucción audible.
5. Entrar a gameplay justo después del reload y confirmar hard-stop.
6. Volver al menú y comprobar que ambiente, música y créditos reaparecen limpiamente.
7. Probar Alt+Tab y F3+T repetido para detectar instancias fantasma.
