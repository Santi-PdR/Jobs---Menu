# Checklist manual de aceptación — 0.40.0

CI certifica código, recursos y build; este checklist certifica la experiencia real dentro de Forge 1.20.1.

## Preparación

- [ ] Java 17 + Forge 47.x + Minecraft 1.20.1.
- [ ] cerrar `test-1` antes de sustituir el JAR.
- [ ] dejar un único `jobsmenu-0.40.0.jar` en `mods`.
- [ ] conservar `latest.log` si aparece crash, audio huérfano o recurso faltante.

## Identidad musical — 0.40

- [ ] Aleatoria reproduce únicamente Absurdism, REQUIEM o Upon the Hill V2;
- [ ] nunca aparece música de menú vanilla de Minecraft como sustitución de una pista Jobs;
- [ ] forzar cada pista fija mantiene título/autor correctos;
- [ ] `N` cambia pista sólo en Aleatoria y no apila crossfades;
- [ ] durante un crossfade la pista saliente no se corta antes de que la entrante realmente haya empezado;
- [ ] `M` silencia/restaura sin cambiar de pista;
- [ ] entrar a un mundo/servidor durante reproducción o crossfade corta Jobs inmediatamente;
- [ ] volver al menú no crea dos copias de la misma pista.

## Créditos y resource reload — 0.39/0.40

Ejecutar varias veces: cambiar idioma → F3+T → aplicar/quitar resource pack → volver al main.

- [ ] no se superponen dos copias de una pista;
- [ ] no aparecen 6/9 camas ambientales por duplicación de reload;
- [ ] la música vuelve una sola vez cuando corresponde;
- [ ] el ambiente del nivel vuelve sin quedar mudo;
- [ ] Absurdism muestra título sin autor inventado;
- [ ] REQUIEM muestra `Emmy Z - Forsaken OST`;
- [ ] Upon the Hill V2 muestra `ft. @iCosmicCoffee`;
- [ ] el crédito sigue funcionando después del reload;
- [ ] gameplay inmediatamente después del reload mantiene hard-stop total.

## Main / layout

Probar 854×480, 1280×720, 1920×1080, ventana estrecha y GUI Scale 2/3/4.

- [ ] no hay títulos vanilla duplicados ni claves `jobsmenu.*` visibles;
- [ ] nombre y nota del nivel no chocan con metadatos/créditos;
- [ ] la antigua barra visible `1-4/F/M/N/TAB/ENTER` no reaparece;
- [ ] botones, sliders y campos mantienen hitboxes correctas;
- [ ] foco teclado y hover se distinguen;
- [ ] español, inglés y Español (Uruguay) mantienen textos completos.

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

## Fondos / accesibilidad

- [ ] PNG 10–17 cargan y permanecen totalmente estáticos;
- [ ] JPG 18–31 cargan y mantienen respiración sutil/no destructiva;
- [ ] Movimiento reducido, Bajo consumo o escena quieta congelan 18–31;
- [ ] Bajo consumo reduce detalle sin romper lectura;
- [ ] Destellos reducidos elimina flicker agresivo;
- [ ] Alto contraste y Texto grande no crean solapes críticos.

## Diagnóstico de fallos

Guardar `latest.log` y anotar pista, selector Aleatoria/fija, secuencia de reload, nivel, si había mundo/servidor cargado y mods de audio/resource packs activos.
