# Música — Jobs Menu 0.25.0

## Catálogo empaquetado

1. **Absurdism** — `assets/jobsmenu/sounds/musica/defecto.ogg` — evento `musica.tema`.
2. **REQUIEM** — `assets/jobsmenu/sounds/musica/requiem.ogg` — evento `musica.requiem`. Fuente autorizada: `music/REQUIEM-Forsaken-OST.ogg`. Crédito: **Emmy Z — Forsaken OST**.
3. **Upon the Hill V2** — `assets/jobsmenu/sounds/musica/upon_the_hill_v2.ogg` — evento `musica.upon_hill`. Fuente autorizada: `music/upon_the_hill_v2_q4.ogg`. El archivo recibido identifica `ft. @iCosmicCoffee`.

Los tres recursos de música usan `stream: true` en `sounds.json`. El build no descarga canciones ni depende de servicios externos.

## Sesión

- La música pertenece a `SesionMenu`, no a una Screen.
- Una visita empieza en una pista aleatoria.
- La siguiente pista se elige entre las otras dos, evitando repetir inmediatamente.
- El cambio automático ocurre aproximadamente cada 2–4 minutos y usa crossfade.
- `N` en el main solicita un cambio manual; no encadena otro mientras ya existe un crossfade.
- `M` mantiene el mute global Jobs.
- Options, Mods, Mundos, Multiplayer y Recursos no reinician la pista.
- Transiciones, Suspensión y presencia aplican ducking.
- F3+T reconstruye de forma defensiva la sesión de audio.
- Entrar a mundo/servidor aplica hard-stop inmediato.

## Créditos

`PantallaNivel` consulta la pista dominante real durante un crossfade. Por eso REQUIEM no puede aparecer como crédito mientras suena Absurdism o Upon the Hill. Absurdism conserva su nombre sin atribución inventada; REQUIEM muestra `Emmy Z - Forsaken OST`; Upon the Hill V2 muestra la mención `ft. @iCosmicCoffee` recibida con el archivo.

## Prueba manual

1. Abrir Jobs varias veces y confirmar que el inicio no está fijado siempre a la misma pista.
2. Pulsar `N` y comprobar crossfade a otra canción.
3. Pulsar `N` repetidamente durante el crossfade: no debe crear varias instancias.
4. Comprobar que el HUD y el crédito cambian al tema dominante.
5. Navegar por subpantallas: la música no reinicia.
6. Probar `M`, F3+T y Alt+Tab.
7. Entrar a un mundo/servidor: ningún audio Jobs debe sobrevivir.
