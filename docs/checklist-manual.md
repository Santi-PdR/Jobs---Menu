# Checklist manual de aceptación — 0.38.0

Este checklist se ejecuta dentro de una instancia Forge 1.20.1 real. CI certifica código, recursos y build; no certifica estética, input, audio perceptivo, FPS ni compatibilidad visual dentro de Minecraft.

## Preparación

- [ ] Java 17, Forge 47.x y Minecraft 1.20.1.
- [ ] Instancia `test-1` cerrada antes de sustituir el JAR.
- [ ] Sólo un `jobsmenu-0.38.0.jar` activo en `mods`.
- [ ] Guardar `latest.log` ante crash, pantalla vacía, textura morado/negro o audio huérfano.

## Layout

Probar al menos 854×480, 1280×720, 1920×1080, ventana estrecha y GUI Scale 2/3/4, en español, inglés y Español (Uruguay).

- [ ] no hay texto sobre botones ni controles fuera de pantalla;
- [ ] no hay hitboxes invisibles superpuestos;
- [ ] foco de teclado y hover se distinguen;
- [ ] no aparecen claves `jobsmenu.*` ni títulos vanilla duplicados;
- [ ] barra inferior y metadatos no pisan el contenido principal;
- [ ] el rótulo del nivel no se solapa con el estado de instalación;
- [ ] el crédito musical se oculta de forma limpia en ventanas compactas y no sale de pantalla;
- [ ] una pista sin autor no deja una segunda línea vacía;
- [ ] volumen muestra porcentaje, los tiempos muestran segundos y nivel/pista no muestran porcentajes falsos.

## Optimización 0.38.0

- [ ] Mundos, Mods, Resource Packs, Idioma y Multiplayer con listas largas se sienten fluidos al mover ratón, hacer scroll y cambiar foco;
- [ ] cada lista muestra una sola scrollbar Jobs por frame, sin doble borde, parpadeo ni grosor variable;
- [ ] rueda, click y drag de scrollbar siguen perteneciendo a la lista vanilla/Forge y funcionan normalmente;
- [ ] abrir/cerrar/reabrir varias pantallas no deja scrollbar, foco o selección de una Screen anterior;
- [ ] alternar repetidamente entre pantallas Jobs no acumula hover fantasma ni sonidos repetidos;
- [ ] con Bajo consumo desactivado la composición procedural conserva el acabado normal anterior;
- [ ] con Bajo consumo activado la escena sigue legible y estable aunque use menos bandas/capas;
- [ ] comparar una escena procedural 0–9 con Bajo consumo ON/OFF y confirmar que OFF mantiene mayor fineza de vignette/profundidad;
- [ ] después de mostrar fondos 10–31, pulsar F3+T y confirmar que vuelven a cargar con filtrado correcto, sin morado/negro ni aspecto pixelado inesperado;
- [ ] varias recargas de recursos consecutivas no degradan ni duplican el tratamiento de las texturas;
- [ ] el aviso administrativo sigue rotando en el intervalo configurado y no queda congelado por la caché de texto;
- [ ] si se prueba una ventana especial de fecha/hora, aparece igual que antes;
- [ ] la luz/transición de nivel y las camas de audio permanecen sincronizadas pese al snapshot compartido de `RotacionNiveles`;
- [ ] Alto contraste sigue afectando botones, sliders y campos vanilla tematizados en toda la Screen;
- [ ] navegación rápida Main → Options → Mods → Recursos → volver no deja elementos o datos visuales de la pantalla anterior.

## Video Settings vanilla

- [ ] Video abre la pantalla vanilla completa, sin marco, cabecera, transición ni recolocación Jobs;
- [ ] aparecen todas las opciones vanilla esperadas y se puede recorrer la lista completa;
- [ ] Done es el botón vanilla, está visible y vuelve al hub Jobs;
- [ ] los clicks y hover dentro de Video Settings son vanilla;
- [ ] FOV aparece sólo dentro de Video Settings, no duplicado en el hub;
- [ ] Online ocupa la última fila completa del hub;
- [ ] con Embeddium/Oculus instalados no hay crash ni opciones recortadas por Jobs.

## Main

- [ ] `TitleScreen` entra a Jobs.
- [ ] 1/2/3/4 y keypad 1/2/3/4 activan los cuatro renglones correspondientes.
- [ ] escribir números en un EditBox no activa atajos.
- [ ] F cambia de nivel cuando corresponde.
- [ ] M alterna silencio Jobs.
- [ ] N inicia un cambio real de pista sin apilar crossfades.
- [ ] el main no muestra la antigua barra inferior 1-4/F/M/N/TAB/ENTER.
- [ ] el nombre y la nota del nivel quedan legibles y no desaparecen si necesitan dos líneas.
- [ ] al cambiar de nivel la primera nota vuelve a empezar desde nota0.
- [ ] no aparece `SHIFT CONTROL`.
- [ ] no aparece un `JOBS / LEVEL` técnico duplicado sobre el fondo.
- [ ] no aparece `%s` literal en la fecha.

## Pausa y navegación

- [ ] el mundo real permanece visible detrás de la pausa Jobs.
- [ ] abrir chat no muestra transición, banda, piel ni hover/click Jobs.
- [ ] abrir inventario o un contenedor no muestra transición, banda, piel ni hover/click Jobs.
- [ ] Pausa y Config Jobs sí conservan la tematización.
- [ ] Pausa, Config Jobs y sus subpantallas aparecen sin barrido, fundido ni animación de entrada mientras hay mundo/servidor cargado.
- [ ] cerrar esas pantallas durante gameplay tampoco deja una transición residual en la siguiente pantalla.
- [ ] botones/sliders vanilla conservados dentro de Pausa/Config usan hover y click Jobs.
- [ ] esos sonidos breves no reactivan música ni ambiente dentro del mundo/servidor.
- [ ] 1 reanuda y 2 abre Condiciones; 3 no desconecta.
- [ ] ESC reanuda correctamente.
- [ ] Mundos vuelve al main Jobs con una sola pulsación de ESC.
- [ ] Multiplayer vuelve con una sola pulsación de ESC o Cancelar.
- [ ] salir de un mundo recupera `PantallaNivel` sin quedarse en TitleScreen vanilla.
- [ ] salir manualmente de un servidor vuelve a Multijugador Jobs, no a Multiplayer vanilla.
- [ ] un kick/pérdida de conexión puede mostrar su mensaje y, al continuar, vuelve a Multijugador Jobs.
- [ ] desde ese Multijugador Jobs, ESC vuelve al main en una sola acción.

## Mods / Resource Packs / Idioma

- [ ] Mods muestra la lista completa de Forge, selección, búsqueda, Config y carpeta de mods.
- [ ] la lista real de Mods no queda vacía ni desplazada por la tematización.
- [ ] Resource Packs mantiene dos listas separadas y utilizables.
- [ ] seleccionar, ordenar, aplicar y abrir carpeta funcionan.
- [ ] Idioma conserva lista, búsqueda, Ctrl+F, portapapeles y Aplicar.

## Multiplayer

- [ ] `JobsDosh.exaroton.me:56477` aparece primero y una sola vez.
- [ ] `Ghoul Outbreak` no aparece.
- [ ] el servidor oficial no se puede editar ni borrar desde Jobs.
- [ ] Direct Connect/Add/Edit/Delete funcionan para las demás entradas.
- [ ] seleccionar un servidor guardado y pulsar F5 mantiene seleccionado ese mismo servidor por IP.
- [ ] el botón Actualizar también conserva la selección actual.
- [ ] si no había servidor online seleccionado, la recarga no inventa selección.
- [ ] F5 y Actualizar reconstruyen directamente Multijugador Jobs, sin flash ni pantalla Multiplayer vanilla intermedia.
- [ ] el indicador inferior dice `F5 // ACTUALIZAR` / `F5 // REFRESH` según idioma y no muestra `JOBS/SERVER`.
- [ ] F5 reproduce un único gesto Jobs de alternar; clickear Actualizar conserva sólo el gesto normal del botón.
- [ ] pulsar F5 repetidamente muy rápido no apila pantallas ni produce varias salidas/entradas perceptibles.
- [ ] F5 y Actualizar siguen detectando LAN, favicons, ping y MOTD correctamente después de varias recargas.
- [ ] una entrada LAN efímera puede reaparecer por el detector sin depender de una Entry anterior.
- [ ] Cancelar durante conexión vuelve a la misma lista Jobs.
- [ ] un error antes del login vuelve a la misma lista Jobs.
- [ ] ESC vuelve al padre Jobs con una sola pulsación.
- [ ] el botón Cancelar vuelve al mismo padre Jobs con un solo click.
- [ ] alternar rápidamente ESC/Cancelar no reabre Multiplayer ni exige pulsaciones repetidas.

## Música y sesión

- [ ] en visitas distintas pueden iniciar Absurdism, REQUIEM o Upon the Hill V2.
- [ ] Audio permite elegir Aleatoria, Absurdism, REQUIEM o Upon the Hill V2 y conserva la elección al reiniciar;
- [ ] en modo fijo no hay rotación automática ni N rompe la selección;
- [ ] N cambia mediante crossfade a una pista distinta cuando está en Aleatoria;
- [ ] N con pista fija conserva la selección y responde con el gesto negado;
- [ ] click, hover, toggles y sliders propios usan gestos Jobs, nunca `ui.button.click` vanilla;
- [ ] botones/sliders vanilla tematizados también usan `UI_PASAR`/`UI_ELEGIR` en superficies Jobs;
- [ ] apagar y volver a activar Sonidos de interfaz produce una confirmación audible;
- [ ] recorrer 18-31 revela ambientes distintos, sin que todos suenen como Administración;
- [ ] cada cambio entre 18-31 mantiene tres capas y crossfade, sin cortes ni capas huérfanas.
- [ ] pulsar N durante el crossfade no crea una tercera instancia.
- [ ] el crédito visible corresponde a la pista dominante.
- [ ] REQUIEM acredita `Emmy Z - Forsaken OST`.
- [ ] Upon the Hill V2 acredita `ft. @iCosmicCoffee`.
- [ ] Absurdism no inventa autor.
- [ ] M silencia/restaura sin cambiar accidentalmente de pista.
- [ ] Main → Options → Mods → Recursos → volver no reinicia ni duplica música.
- [ ] F3+T y Alt+Tab no crean instancias fantasma.
- [ ] entrar a gameplay corta música y ambiente Jobs desde el primer tick jugable.
- [ ] después del hard-stop, abrir pausa/configuración no vuelve a levantar música ni camas.

## Fondos 10–17

- [ ] los ocho PNG cargan sin morado/negro.
- [ ] permanecen totalmente estáticos: sin zoom, paneo, parallax, flicker, niebla móvil o deformación.
- [ ] fades, apagones y transiciones globales funcionan sin alterar la imagen cuando no hay gameplay.

## Fondos 18–31

Revisar todos: 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30 y 31.

- [ ] cada nivel muestra el JPG correcto según `docs/FONDOS_18_31.md`.
- [ ] todos cargan nítidos y sin textura morado/negro.
- [ ] el cover conserva una composición razonable en 16:9, ventana estrecha y GUI Scale 2/3/4.
- [ ] la respiración mantiene dentro del encuadre el sujeto principal de cada JPG; revisar con especial atención 19, 22, 24, 29, 30 y 31.
- [ ] los nombres visibles coinciden con la tabla de `docs/FONDOS_18_31.md`.
- [ ] la respiración de cámara es lenta y muy sutil; no parece un zoom evidente.
- [ ] no aparecen objetos o efectos falsos sobre la imagen.
- [ ] Movimiento reducido congela completamente el fondo.
- [ ] Bajo consumo congela completamente el fondo.
- [ ] desactivar escena viva congela completamente el fondo.
- [ ] volver a opciones normales recupera el movimiento sin saltos bruscos.
- [ ] `nivel_fijo` permite seleccionar cualquier valor de 0 a 31 también desde la interfaz de configuración.

## Nivel 1 · Depósito

- [ ] se mantiene el renderer `DepositoNuevo` de 0.26.0.
- [ ] `backups/nivel1/Nave_0.25.0.java.txt` sigue siendo sólo backup y no participa en runtime.

## Cierre

Si todo pasa:

- [ ] conservar SHA-256 del JAR probado;
- [ ] anotar resolución y GUI Scale;
- [ ] confirmar que `test-1\mods` contiene un único `jobsmenu-0.38.0.jar`;
- [ ] reportar cualquier defecto visual, sonoro o de rendimiento con captura y `latest.log` cuando afecte recursos/audio/crash.
