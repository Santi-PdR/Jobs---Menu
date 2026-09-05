# Música — Jobs Menu 0.37.0

## Catálogo empaquetado

1. **Absurdism** — `assets/jobsmenu/sounds/musica/defecto.ogg` — evento `musica.tema`.
2. **REQUIEM** — `assets/jobsmenu/sounds/musica/requiem.ogg` — evento `musica.requiem`. Fuente autorizada: `music/REQUIEM-Forsaken-OST.ogg`. Crédito: **Emmy Z — Forsaken OST**.
3. **Upon the Hill V2** — `assets/jobsmenu/sounds/musica/upon_the_hill_v2.ogg` — evento `musica.upon_hill`. Fuente autorizada: `music/upon_the_hill_v2_q4.ogg`. El archivo recibido identifica `ft. @iCosmicCoffee`.

Los tres recursos de música usan `stream: true` en `sounds.json`. El build no descarga canciones ni depende de servicios externos.

Las pistas musicales conservan mono/estéreo y 44,1/48 kHz según la fuente autorizada. Los sonidos de interfaz, ambiente y eventos mantienen el contrato mono 44,1 kHz.

## Selección en configuración

En `Ajustes del aviso > Audio` se puede elegir **Aleatoria**, **Absurdism**, **REQUIEM** o **Upon the Hill V2**. La elección se guarda en `pista_musica` (0–3). Una pista fija no rota automáticamente y la tecla `N` no la sustituye; al volver a Aleatoria se conserva la pista actual y se reactiva la rotación para el siguiente intervalo.

## Sesión

- La música pertenece a `SesionMenu`, no a una Screen.
- Una visita empieza en una pista aleatoria o en la pista fija elegida.
- La siguiente pista se elige entre las otras dos, evitando repetir inmediatamente.
- El cambio automático ocurre aproximadamente cada 2–4 minutos y usa crossfade.
- `N` en el main solicita un cambio manual; no encadena otro mientras ya existe un crossfade.
- `M` mantiene el mute global Jobs.
- Options, Mods, Mundos, Multiplayer y Recursos no reinician la pista.
- Transiciones, Suspensión y presencia aplican ducking únicamente dentro del flujo de menú.
- F3+T reconstruye de forma defensiva la sesión de audio.
- Entrar a mundo/servidor aplica hard-stop inmediato a música y ambiente.
- Desde 0.36.0 no se crean ni dibujan transiciones Jobs mientras existe un nivel cargado; esto no altera el hard-stop ni el feedback breve de UI permitido en pausa/configuración.

## Feedback de interfaz

Desde 0.35.0 el feedback corto de UI no se confunde con la sesión musical. Botones y sliders de una pantalla propia Jobs pueden seguir usando `UI_PASAR`, `UI_ELEGIR`, confirmar, volver o negado dentro de pausa/configuración aunque haya un mundo cargado. Eso **no** abre `SesionMenu`, no reinicia una pista y no vuelve a levantar camas ambientales.

Los controles vanilla preservados por compatibilidad sustituyen `minecraft:ui.button.click` sólo en superficies Jobs válidas. También reciben un hover Jobs al entrar con ratón o foco de teclado. Video Settings, chat, inventario y pantallas no Jobs conservan el audio que les corresponda y quedan fuera de esta sustitución.

Desde 0.37.0 el atajo F5 de Multiplayer emite `UI_ALTERNAR` como feedback de teclado. Ese gesto tampoco pertenece a `SesionMenu`: refrescar servidores no reinicia, adelanta ni modifica la pista musical. El botón Actualizar conserva su gesto normal y `refrescarLista()` no añade un segundo sonido.

## Créditos

`PantallaNivel` consulta la pista dominante real durante un crossfade. Por eso REQUIEM no puede aparecer como crédito mientras suena Absurdism o Upon the Hill. Absurdism conserva su nombre sin atribución inventada; REQUIEM muestra `Emmy Z - Forsaken OST`; Upon the Hill V2 muestra la mención `ft. @iCosmicCoffee` recibida con el archivo.

## Prueba manual

1. Abrir Jobs varias veces y confirmar que el inicio no está fijado siempre a la misma pista.
2. Pulsar `N` y comprobar crossfade a otra canción.
3. Pulsar `N` repetidamente durante el crossfade: no debe crear varias instancias.
4. Comprobar que el HUD y el crédito cambian al tema dominante.
5. Navegar por subpantallas: la música no reinicia.
6. Probar `M`, F3+T y Alt+Tab.
7. Entrar a un mundo/servidor: música y ambiente Jobs deben detenerse por completo.
8. Abrir pausa/configuración Jobs dentro del mundo: click/hover Jobs pueden responder, pero música y ambiente deben seguir apagados y no debe aparecer ninguna transición de entrada/salida.
9. Abrir Video Settings, chat e inventario: no deben recibir sustitución de sonido Jobs ni transición Jobs.
10. En Multiplayer, pulsar F5: debe sonar un solo `UI_ALTERNAR` y la música actual debe continuar sin reinicio/cambio.

Absurdism source 0.26: music/Absurdism-_slowed-piano-part-only_.ogg -> assets/jobsmenu/sounds/musica/defecto.ogg
