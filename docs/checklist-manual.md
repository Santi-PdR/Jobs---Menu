# Checklist manual de aceptación — 0.41.0

CI certifica código, recursos y build; este checklist certifica la experiencia real dentro de Forge 1.20.1.

## Preparación

- [ ] Java 17 + Forge 47.x + Minecraft 1.20.1.
- [ ] cerrar `test-1` antes de sustituir el JAR.
- [ ] dejar un único `jobsmenu-0.41.0.jar` en `mods`.
- [ ] conservar `latest.log` si aparece crash, audio huérfano o recurso faltante.

## Audio / lifecycle — 0.41

- [ ] Aleatoria reproduce sólo Absurdism, REQUIEM o Upon the Hill V2;
- [ ] nunca aparece música de menú vanilla;
- [ ] eventos, apagones y FX ambientales suenan como Jobs, nunca como `ambient.cave` vanilla;
- [ ] entrar a mundo/servidor durante una pista, crossfade o FX puntual corta **todo** el audio de menú inmediatamente;
- [ ] volver al menú no duplica música, camas ni FX;
- [ ] permanecer varios minutos en gameplay no produce audio residual ni tirones periódicos por cierre repetido;
- [ ] F3+T/reload reconstruye audio una sola vez y sigue permitiendo créditos;
- [ ] Alt+Tab no crea instancias fantasma.

## Música / créditos

- [ ] Absurdism muestra título sin autor inventado;
- [ ] REQUIEM muestra `Emmy Z - Forsaken OST`;
- [ ] Upon the Hill V2 muestra `ft. @iCosmicCoffee`;
- [ ] `N` sólo cambia pista en Aleatoria;
- [ ] `M` silencia/restaura sin cambiar pista;
- [ ] una pista entrante inválida nunca corta prematuramente la válida.

## Config Jobs — 0.41

- [ ] toggles aplican el cambio una sola vez;
- [ ] sliders arrastrados rápido no producen stutter por escritura continua;
- [ ] salir de Config conserva el último valor;
- [ ] reiniciar Minecraft conserva música, volúmenes, nivel fijo y accesibilidad;
- [ ] aplicar dos veces el mismo perfil no causa efectos secundarios ni reinicios de audio;
- [ ] Perfil accesible sigue activando Movimiento reducido, Destellos reducidos, Alto contraste y Texto grande.

## Multiplayer — 0.41

- [ ] `JobsDosh.exaroton.me:56477` aparece primero y una sola vez;
- [ ] `Ghoul Outbreak` no aparece;
- [ ] servidor oficial no se puede editar/borrar;
- [ ] Direct Connect/Add/Edit/Delete funcionan para el resto;
- [ ] seleccionar un servidor, bajar bastante en la lista y pulsar F5 conserva **selección y scroll**;
- [ ] botón Actualizar hace lo mismo;
- [ ] varias recargas seguidas no saltan al inicio de lista;
- [ ] LAN, ping, MOTD y favicons siguen funcionando;
- [ ] F5 suena una sola vez;
- [ ] ESC vuelve al padre Jobs con una pulsación;
- [ ] Cancelar conexión/error pre-login vuelve a la lista Jobs;
- [ ] salida/kick de servidor remoto vuelve a Multiplayer Jobs;
- [ ] salir de mundo local vuelve al main Jobs.

## Main / layout

Probar 854×480, 1280×720, 1920×1080, ventana estrecha y GUI Scale 2/3/4.

- [ ] no hay títulos vanilla duplicados ni claves `jobsmenu.*` visibles;
- [ ] nombre/nota/crédito no chocan;
- [ ] no reaparece la barra visible `1-4/F/M/N/TAB/ENTER`;
- [ ] botones/sliders/campos mantienen hitboxes correctas;
- [ ] hover/foco en controles vanilla preservados sigue sonando una sola vez al entrar;
- [ ] no se nota retraso adicional al mover el mouse por pantallas con muchos widgets.

## Video Settings / gameplay

- [ ] Video Settings abre completo y vanilla, también con Embeddium/Oculus;
- [ ] no tiene marco/transición/recolocación/click Jobs;
- [ ] chat, inventario y contenedores no reciben skin ni transición;
- [ ] Pausa/Config Jobs conservan sólo tematización/feedback permitidos;
- [ ] con mundo cargado no aparece ninguna transición Jobs.

## Fondos / accesibilidad

- [ ] PNG 10–17 permanecen totalmente estáticos;
- [ ] JPG 18–31 mantienen respiración sutil/no destructiva;
- [ ] Movimiento reducido, Bajo consumo o escena quieta congelan 18–31;
- [ ] Texto grande/Alto contraste no causan solapes críticos.

## Diagnóstico de fallos

Guardar `latest.log` y anotar pista, selector, secuencia de reload, nivel, estado mundo/servidor, selección/scroll Multiplayer, valores de config modificados y mods de audio/resource packs activos.
