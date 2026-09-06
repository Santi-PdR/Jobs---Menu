# Checklist manual de aceptación — 0.44.0

CI certifica código, recursos y build; este checklist certifica la experiencia real dentro de Forge 1.20.1.

## Preparación

- [ ] Java 17 + Forge 47.x + Minecraft 1.20.1.
- [ ] cerrar `test-1` antes de sustituir el JAR.
- [ ] dejar un único `jobsmenu-0.44.0.jar` en `mods`.
- [ ] conservar `latest.log` si aparece crash, audio huérfano o recurso faltante.

## Gráficos — 0.44

Con Embeddium instalado:

- [ ] abrir **Opciones → Gráficos**;
- [ ] se abre la interfaz original de Embeddium;
- [ ] no aparece marco, cabecera, banda, transición, overlay ni recolocación Jobs;
- [ ] los botones, pestañas, tooltips y controles son los originales de Embeddium/modpack;
- [ ] no se escucha reemplazo Jobs del click/hover dentro de Gráficos;
- [ ] ESC/Done vuelve una sola vez a Opciones Jobs;
- [ ] abrir/cerrar Gráficos varias veces no duplica widgets ni cambia su diseño;
- [ ] cambiar resolución/GUI Scale y volver a entrar mantiene la interfaz gráfica original.

Sin Embeddium:

- [ ] Gráficos abre `VideoSettingsScreen` vanilla;
- [ ] esa pantalla tampoco recibe skin/transición/click Jobs;
- [ ] ESC/Done vuelve a Opciones Jobs.

## MODPACK eliminado / salida de configuración — 0.44

- [ ] **no existe botón MODPACK** en Opciones Jobs;
- [ ] no existe un hueco/hitbox invisible donde estaba MODPACK;
- [ ] entrar/salir de Opciones Jobs con ESC funciona siempre con una sola salida;
- [ ] el botón Volver funciona una sola vez;
- [ ] repetir abrir/cerrar Opciones muchas veces no crea bucle ni obliga a pulsar ESC repetidamente;
- [ ] volver desde Gráficos, Sonido, Controles, Idioma, Chat, Recursos, Accesibilidad y Online deja Opciones Jobs usable;
- [ ] aplicar/cerrar resource packs no reconstruye Opciones dos veces.

## Navegación externa — 0.44

- [ ] una Screen de otro mod no recibe chrome, transición, hover/click ni recolocación Jobs;
- [ ] si esa Screen abre otra Screen propia, Jobs no interviene;
- [ ] si abre un submenú vanilla, Jobs no lo sustituye sólo porque la sesión del menú siga activa;
- [ ] una `OptionsScreen`, `SelectWorldScreen`, `JoinMultiplayerScreen` o `ModListScreen` abierta fuera de un padre Jobs no se captura por el simple hecho de que `SesionMenu` esté activa;
- [ ] volver desde una Screen externa al padre Jobs funciona sin pantalla intermedia ni doble ESC.

## Perfiles / estado CUSTOM — 0.43 heredado

- [ ] aplicar Equilibrado marca Equilibrado;
- [ ] cambiar un valor controlado por el preset pasa a CUSTOM;
- [ ] volver a aplicar el preset restaura su identificación;
- [ ] repetir con Inmersivo, Rendimiento, Accesible y Mínimo;
- [ ] cambiar una opción que el preset no controla no invalida el perfil;
- [ ] Perfil accesible sigue activando Movimiento reducido, Destellos reducidos, Alto contraste y Texto grande.

## Mundos / Mods — 0.43 heredado

En ambas pantallas:

- [ ] `Ctrl+F` enfoca el campo de búsqueda;
- [ ] con texto escrito, primer `ESC` vacía el filtro y no sale;
- [ ] con filtro vacío pero campo enfocado, segundo `ESC` suelta el foco;
- [ ] el siguiente `ESC` vuelve al padre Jobs;
- [ ] botón/ESC repetido no produce doble retorno;
- [ ] resize/maximizar no deja el guard de cierre bloqueado.

## Audio / lifecycle

- [ ] Aleatoria reproduce sólo Absurdism, REQUIEM o Upon the Hill V2;
- [ ] nunca aparece música de menú vanilla;
- [ ] eventos/apagones/FX no usan `ambient.cave`;
- [ ] entrar a mundo/servidor corta todo el audio de menú inmediatamente;
- [ ] volver al menú no duplica música, camas ni FX;
- [ ] F3+T/reload reconstruye audio una sola vez;
- [ ] Alt+Tab no crea instancias fantasma.

## Música / créditos

- [ ] Absurdism muestra título sin autor inventado;
- [ ] REQUIEM muestra `Emmy Z - Forsaken OST`;
- [ ] Upon the Hill V2 muestra `ft. @iCosmicCoffee`;
- [ ] `N` sólo cambia pista en Aleatoria;
- [ ] `M` silencia/restaura sin cambiar pista.

## Config Jobs

- [ ] toggles aplican el cambio una sola vez;
- [ ] sliders rápidos no producen stutter por escritura continua;
- [ ] salir conserva el último valor;
- [ ] reiniciar Minecraft conserva música, volúmenes, nivel fijo y accesibilidad;
- [ ] aplicar dos veces el mismo perfil no causa efectos secundarios.

## Multiplayer

- [ ] `JobsDosh.exaroton.me:56477` aparece primero y una sola vez;
- [ ] `Ghoul Outbreak` no aparece;
- [ ] servidor oficial no se puede editar/borrar;
- [ ] Direct Connect/Add/Edit/Delete funcionan para el resto;
- [ ] F5/Actualizar conserva selección y scroll;
- [ ] maximizar/restaurar, resize y GUI Scale conservan selección y scroll;
- [ ] LAN, ping, MOTD y favicons siguen funcionando;
- [ ] F5 suena una sola vez;
- [ ] ESC vuelve al padre Jobs con una pulsación;
- [ ] Cancelar conexión/error pre-login vuelve a la lista Jobs;
- [ ] salida/kick remoto vuelve a Multiplayer Jobs;
- [ ] salir de mundo local vuelve al main Jobs.

## Main / layout

Probar 854×480, 1280×720, 1920×1080, ventana estrecha y GUI Scale 2/3/4.

- [ ] no hay títulos vanilla duplicados ni claves `jobsmenu.*` visibles;
- [ ] nombre/nota/crédito no chocan;
- [ ] no reaparece la barra visible `1-4/F/M/N/TAB/ENTER`;
- [ ] botones/sliders/campos mantienen hitboxes correctas;
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

- [ ] la release `dev-latest` contiene exactamente `jobsmenu-0.44.0.jar`;
- [ ] el SHA-256 descargado coincide con el digest de GitHub;
- [ ] `refs/tags/dev-latest` apunta exactamente al mismo SHA que `main` publicado;
- [ ] el workflow completó `Publish` → `Move rolling development tag` → `Remove obsolete release JARs`.

## Diagnóstico de fallos

Guardar `latest.log` y anotar pantalla, secuencia exacta de navegación, resolución, GUI Scale, selección/scroll Multiplayer, estado del buscador, perfil indicado y qué GUI exacta abrió Gráficos.
