# Checklist manual de aceptación — 0.39.0

CI certifica código, recursos y build; este checklist certifica la experiencia real dentro de Forge 1.20.1.

## Preparación

- [ ] Java 17 + Forge 47.x + Minecraft 1.20.1.
- [ ] cerrar `test-1` antes de sustituir el JAR.
- [ ] dejar un único `jobsmenu-0.39.0.jar` en `mods`.
- [ ] conservar `latest.log` si aparece crash, audio huérfano o recurso faltante.

## Main / layout

Probar 854×480, 1280×720, 1920×1080, ventana estrecha y GUI Scale 2/3/4.

- [ ] no hay títulos vanilla duplicados ni claves `jobsmenu.*` visibles;
- [ ] nombre y nota del nivel no chocan con metadatos/créditos;
- [ ] la antigua barra visible `1-4/F/M/N/TAB/ENTER` no reaparece;
- [ ] botones, sliders y campos mantienen hitboxes correctas;
- [ ] foco teclado y hover se distinguen;
- [ ] español, inglés y Español (Uruguay) mantienen textos completos.

## Créditos musicales — 0.39

- [ ] con `Crédito musical` activado aparece el bloque de crédito durante su ventana temporal;
- [ ] Absurdism muestra `Absurdism` sin autor inventado;
- [ ] REQUIEM muestra `REQUIEM` + `Emmy Z - Forsaken OST`;
- [ ] Upon the Hill V2 muestra `Upon the Hill V2` + `ft. @iCosmicCoffee`;
- [ ] desactivar `Crédito musical` oculta el bloque sin afectar reproducción;
- [ ] cambiar de pista por crossfade actualiza el crédito al tema dominante.

## Resource reload — 0.39

Ejecutar varias veces, incluyendo una secuencia corta:

1. cambiar idioma;
2. F3+T;
3. aplicar/quitar un resource pack;
4. volver al main.

Comprobar:

- [ ] no se superponen dos copias de una pista;
- [ ] no aparecen 6/9 camas ambientales por duplicación de reload;
- [ ] la música vuelve una sola vez cuando corresponde;
- [ ] el ambiente del nivel vuelve sin quedar mudo;
- [ ] el crédito sigue funcionando después del reload;
- [ ] entrar a un mundo/servidor inmediatamente después del reload corta todo el audio de menú;
- [ ] volver al menú inicia una visita limpia.

## Música / sesión

- [ ] selector Aleatoria/Absurdism/REQUIEM/Upon the Hill V2 persiste al reiniciar;
- [ ] una pista fija no rota automáticamente;
- [ ] `N` cambia pista sólo en Aleatoria y no apila crossfades;
- [ ] `M` silencia/restaura sin cambiar de pista;
- [ ] Main → Options → Mods → Recursos → volver no reinicia la visita ni la pista;
- [ ] clicks/hover Jobs no vuelven al sonido vanilla;
- [ ] pausa/config dentro de gameplay puede tener feedback breve, pero música/ambiente siguen apagados.

## Video Settings

- [ ] abre la pantalla vanilla completa;
- [ ] no hay marco, transición, recolocación ni click Jobs encima;
- [ ] aparecen todas las opciones del juego/mod gráfico;
- [ ] Done/Volver funciona normalmente;
- [ ] Embeddium/Oculus no pierde opciones por Jobs.

## Gameplay

- [ ] chat no recibe skin, banda ni transición Jobs;
- [ ] inventario/contenedores tampoco;
- [ ] Pausa y Config Jobs conservan tema permitido;
- [ ] ninguna pantalla durante gameplay muestra animación de entrada/salida Jobs;
- [ ] al primer tick jugable música y ambiente Jobs están detenidos.

## Multiplayer

- [ ] `JobsDosh.exaroton.me:56477` aparece primero y una sola vez;
- [ ] `Ghoul Outbreak` no aparece;
- [ ] el servidor oficial no se puede editar/borrar;
- [ ] Direct Connect/Add/Edit/Delete funcionan para el resto;
- [ ] seleccionar un servidor online y pulsar F5 conserva esa selección por IP;
- [ ] Actualizar hace lo mismo;
- [ ] no aparece una pantalla Multiplayer vanilla intermedia;
- [ ] F5 suena una vez y no duplica el click del botón Actualizar;
- [ ] ESC vuelve al padre Jobs con una pulsación;
- [ ] Cancelar vuelve con un click;
- [ ] Cancelar conexión/error pre-login vuelve a la misma lista Jobs;
- [ ] salir/kick/pérdida de conexión de servidor vuelve a Multiplayer Jobs;
- [ ] salir de mundo local vuelve al main Jobs;
- [ ] LAN, ping, MOTD y favicons siguen funcionando tras varias recargas.

## Fondos

### 10–17

- [ ] los ocho PNG cargan sin morado/negro;
- [ ] permanecen totalmente estáticos en todo momento.

### 18–31

- [ ] los 14 JPG cargan y corresponden a su nivel;
- [ ] cover no corta el foco principal de forma grave;
- [ ] respiración de cámara es lenta/sutil;
- [ ] Movimiento reducido la congela;
- [ ] Bajo consumo la congela;
- [ ] desactivar escena viva la congela;
- [ ] no se agregan objetos falsos ni deformaciones.

## Bajo consumo / accesibilidad

- [ ] Bajo consumo reduce animación/detalle sin romper lectura;
- [ ] Movimiento reducido elimina desplazamientos molestos;
- [ ] Destellos reducidos elimina flicker agresivo;
- [ ] Alto contraste mejora legibilidad;
- [ ] Texto grande no causa solapes críticos;
- [ ] Perfil accesible aplica el conjunto esperado.

## Diagnóstico de fallos

Si audio/reload falla, guardar `latest.log` y anotar:

- pista seleccionada;
- si estaba en Aleatoria o fija;
- secuencia de reload realizada;
- nivel visible;
- si había mundo/servidor cargado;
- mods de audio/resource packs activos.

El diagnóstico interno de Jobs ahora incluye pista dominante y generación de resource reload, útiles para comparar el momento exacto del fallo.
