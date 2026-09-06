# Checklist manual de aceptación — 0.42.0

CI certifica código, recursos y build; este checklist certifica la experiencia real dentro de Forge 1.20.1.

## Preparación

- [ ] Java 17 + Forge 47.x + Minecraft 1.20.1.
- [ ] cerrar `test-1` antes de sustituir el JAR.
- [ ] dejar un único `jobsmenu-0.42.0.jar` en `mods`.
- [ ] conservar `latest.log` si aparece crash, audio huérfano o recurso faltante.

## Gráficos / flujo natural — 0.42

Con el modpack real de `test-1`:

- [ ] abrir un `OptionsScreen` normal de referencia si es posible y observar qué abre su botón Gráficos;
- [ ] **Gráficos de Jobs abre la misma interfaz y conserva las mismas categorías/opciones añadidas por mods**;
- [ ] Embeddium mantiene todas sus opciones y pestañas;
- [ ] opciones/inyecciones de Oculus u otros mods gráficos presentes en el flujo normal siguen presentes;
- [ ] si un mod cambia el texto del botón gráfico pero conserva aproximadamente su ranura, Jobs sigue abriendo el callback sustituido;
- [ ] ESC/Done/Cerrar desde la GUI gráfica vuelve correctamente a Opciones Jobs;
- [ ] la GUI gráfica externa no recibe marco, banda, transición, recolocación ni click/hover Jobs;
- [ ] Opciones Jobs no muestra título/fondo vanilla encima del chrome Jobs;
- [ ] no existen controles vanilla/modded visibles ni hitboxes invisibles debajo del panel Jobs;
- [ ] abrir/cerrar Gráficos varias veces no duplica widgets ni rompe el foco.

Sin mods que sustituyan Gráficos:

- [ ] la misma ruta natural termina en `VideoSettingsScreen` vanilla;
- [ ] volver desde vanilla regresa a Opciones Jobs;
- [ ] la pantalla vanilla de vídeo tampoco recibe tematización ni transición Jobs.

## MODPACK / Opciones completas — 0.42

- [ ] el botón `MODPACK` aparece en Opciones Jobs sin solaparse con Online/Cerrar expediente;
- [ ] `MODPACK` abre el `OptionsScreen` completo y natural del modpack;
- [ ] cualquier botón, categoría o inyección que otro mod agregue al Options normal también aparece allí;
- [ ] entrar en un submenú desde MODPACK no añade chrome, transición, click ni hover Jobs;
- [ ] volver desde un submenú externo regresa al Options natural y no a una reconstrucción Jobs inesperada;
- [ ] cerrar finalmente el Options natural vuelve a Opciones Jobs con una sola acción.

## Pantallas de otros mods — 0.42

- [ ] abrir una pantalla de configuración de cualquier mod desde Mods/Options no añade chrome Jobs;
- [ ] esa Screen externa conserva sus clicks, hover, sliders, tooltips y navegación propios;
- [ ] si la Screen externa abre otra Screen propia, Jobs no interviene;
- [ ] si la Screen externa abre un `OptionsScreen`, `SelectWorldScreen`, `JoinMultiplayerScreen` o `ModListScreen` vanilla como subflujo, Jobs no lo reemplaza por una pantalla Jobs sólo porque la sesión del menú siga activa;
- [ ] un subflujo externo que pasa temporalmente por una Screen `net.minecraft.*` sigue sin recibir banda/click Jobs;
- [ ] volver desde una Screen externa al padre Jobs funciona sin pantalla intermedia ni doble ESC;
- [ ] una Screen Forge estándar (`net.minecraftforge.*`) que Jobs sí tematiza históricamente conserva su lógica completa.

## Audio / lifecycle — 0.41 heredado

- [ ] Aleatoria reproduce sólo Absurdism, REQUIEM o Upon the Hill V2;
- [ ] nunca aparece música de menú vanilla;
- [ ] eventos, apagones y FX ambientales suenan como Jobs, nunca como `ambient.cave` vanilla;
- [ ] entrar a mundo/servidor durante una pista, crossfade o FX puntual corta **todo** el audio de menú inmediatamente;
- [ ] volver al menú no duplica música, camas ni FX;
- [ ] permanecer varios minutos en gameplay no produce audio residual ni tirones periódicos por cierre repetido;
- [ ] F3+T/reload reconstruye audio una sola vez;
- [ ] Alt+Tab no crea instancias fantasma.

## Música / créditos

- [ ] Absurdism muestra título sin autor inventado;
- [ ] REQUIEM muestra `Emmy Z - Forsaken OST`;
- [ ] Upon the Hill V2 muestra `ft. @iCosmicCoffee`;
- [ ] `N` sólo cambia pista en Aleatoria;
- [ ] `M` silencia/restaura sin cambiar pista;
- [ ] una pista entrante inválida nunca corta prematuramente la válida.

## Config Jobs

- [ ] toggles aplican el cambio una sola vez;
- [ ] sliders arrastrados rápido no producen stutter por escritura continua;
- [ ] salir de Config conserva el último valor;
- [ ] reiniciar Minecraft conserva música, volúmenes, nivel fijo y accesibilidad;
- [ ] aplicar dos veces el mismo perfil no causa efectos secundarios ni reinicios de audio;
- [ ] Perfil accesible sigue activando Movimiento reducido, Destellos reducidos, Alto contraste y Texto grande.

## Multiplayer — 0.41 heredado

- [ ] `JobsDosh.exaroton.me:56477` aparece primero y una sola vez;
- [ ] `Ghoul Outbreak` no aparece;
- [ ] servidor oficial no se puede editar/borrar;
- [ ] Direct Connect/Add/Edit/Delete funcionan para el resto;
- [ ] seleccionar un servidor, bajar bastante en la lista y pulsar F5 conserva **selección y scroll**;
- [ ] botón Actualizar hace lo mismo;
- [ ] maximizar/restaurar la ventana conserva selección y scroll;
- [ ] redimensionar varias veces conserva selección y scroll;
- [ ] cambiar GUI Scale no manda la lista al inicio durante el resize;
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

## Gameplay

- [ ] chat, inventario y contenedores no reciben skin ni transición;
- [ ] Pausa/Config Jobs conservan sólo tematización/feedback permitidos;
- [ ] con mundo cargado no aparece ninguna transición Jobs.

## Fondos / accesibilidad

- [ ] PNG 10–17 permanecen totalmente estáticos;
- [ ] JPG 18–31 mantienen respiración sutil/no destructiva;
- [ ] Movimiento reducido, Bajo consumo o escena quieta congelan 18–31;
- [ ] Texto grande/Alto contraste no causan solapes críticos.

## Publicación

- [ ] la release `dev-latest` contiene exactamente `jobsmenu-0.42.0.jar`;
- [ ] el SHA-256 descargado coincide con el digest de GitHub;
- [ ] `refs/tags/dev-latest` apunta exactamente al mismo SHA que `main` publicado;
- [ ] el workflow completó en orden `Publish rolling development release` → `Move rolling development tag` → `Remove obsolete release JARs`.

## Diagnóstico de fallos

Guardar `latest.log` y anotar pista, selector, secuencia de reload, nivel, estado mundo/servidor, selección/scroll Multiplayer, mod dueño de la Screen externa, si se entró por Gráficos o MODPACK, qué opciones faltaron y valores de config modificados.
