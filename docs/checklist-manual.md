# Checklist manual de aceptación — 0.45.0

CI certifica código, recursos y build; este checklist certifica la experiencia real dentro de Forge 1.20.1.

## Preparación

- [ ] Java 17 + Forge 47.x + Minecraft 1.20.1.
- [ ] cerrar `test-1` antes de sustituir el JAR.
- [ ] dejar un único `jobsmenu-0.45.0.jar` en `mods`.
- [ ] conservar `latest.log` si aparece crash, audio huérfano o recurso faltante.

## Config Jobs / búsqueda — 0.45

- [ ] `Ctrl+F` desde Config Jobs abre el buscador transversal;
- [ ] buscar por nombre de opción devuelve resultados correctos;
- [ ] buscar por palabras de la descripción también devuelve resultados;
- [ ] Enter abre la categoría del resultado seleccionado;
- [ ] doble clic hace lo mismo sin doble navegación;
- [ ] primer ESC con texto escrito limpia el filtro;
- [ ] segundo ESC con filtro vacío quita foco;
- [ ] siguiente ESC vuelve a Config Jobs;
- [ ] resize/maximizar conserva filtro, foco razonable y scroll;
- [ ] Config recuerda la última pestaña usada durante la sesión;
- [ ] editar un valor controlado por un preset muestra `CUSTOM` arriba;
- [ ] aplicar de nuevo un preset restaura su nombre;
- [ ] cerrar Config repetidamente con ESC/Volver no produce doble `setScreen()`.

## Idioma — 0.45

- [ ] `Ctrl+F` sigue enfocando búsqueda;
- [ ] elegir otro idioma y redimensionar conserva selección pendiente;
- [ ] filtro y scroll sobreviven a resize/maximizar;
- [ ] aplicar idioma exitosamente recarga y vuelve al padre una sola vez;
- [ ] si un reload falla, la pantalla queda abierta y muestra feedback de error;
- [ ] tras ese fallo, el idioma efectivo vuelve al anterior en vez de quedar parcialmente aplicado;
- [ ] un segundo intento puede realizarse sin reiniciar Minecraft.

## Mundos / Mods — continuidad 0.45

En ambas pantallas:

- [ ] `Ctrl+F` enfoca el campo de búsqueda;
- [ ] resize/maximizar conserva el filtro escrito;
- [ ] si el campo estaba enfocado, la reconstrucción no rompe el flujo de edición;
- [ ] con texto escrito, primer `ESC` vacía el filtro y no sale;
- [ ] con filtro vacío pero campo enfocado, segundo `ESC` suelta el foco;
- [ ] el siguiente `ESC` vuelve al padre Jobs;
- [ ] botón/ESC repetido no produce doble retorno.

## Navegación / callbacks — 0.45

- [ ] Apariencia abre/cierra repetidamente sin salto doble;
- [ ] Controles abre/cierra repetidamente sin salto doble;
- [ ] Resource Packs aplica/cierra y vuelve a Opciones Jobs una sola vez;
- [ ] si se abandona Resource Packs y se navega a otra pantalla, un callback tardío no vuelve inesperadamente a Opciones Jobs;
- [ ] cambiar GUI Scale dentro de estos flujos no deja pantallas bloqueadas.

## Sonido / rendimiento — 0.45

- [ ] abrir/cerrar Sonido muchas veces no genera stutter creciente;
- [ ] sliders y categorías vanilla siguen completos y funcionales;
- [ ] no desaparecen controles por fallo de reflection;
- [ ] hover/click Jobs permitido sigue sonando una sola vez donde corresponde.

## Gráficos — contrato 0.44 heredado

Con Embeddium instalado:

- [ ] abrir **Opciones → Gráficos**;
- [ ] se abre la interfaz original de Embeddium;
- [ ] no aparece marco, cabecera, banda, transición, overlay ni recolocación Jobs;
- [ ] botones, pestañas, tooltips y controles son los originales de Embeddium/modpack;
- [ ] no se escucha reemplazo Jobs del click/hover dentro de Gráficos;
- [ ] ESC/Done vuelve una sola vez a Opciones Jobs;
- [ ] abrir/cerrar Gráficos varias veces no duplica widgets ni cambia su diseño.

Sin Embeddium:

- [ ] Gráficos abre `VideoSettingsScreen` vanilla;
- [ ] esa pantalla tampoco recibe skin/transición/click Jobs;
- [ ] ESC/Done vuelve a Opciones Jobs.

## MODPACK eliminado / salida de configuración

- [ ] **no existe botón MODPACK**;
- [ ] no existe hueco/hitbox invisible de MODPACK;
- [ ] repetir abrir/cerrar Opciones no crea bucle;
- [ ] volver desde Gráficos, Sonido, Controles, Idioma, Chat, Recursos, Accesibilidad y Online deja Opciones Jobs usable.

## Navegación externa

- [ ] una Screen de otro mod no recibe chrome, transición, hover/click ni recolocación Jobs;
- [ ] si esa Screen abre otra Screen propia, Jobs no interviene;
- [ ] si abre un submenú vanilla, Jobs no lo sustituye sólo porque la sesión del menú siga activa;
- [ ] volver desde una Screen externa al padre Jobs funciona sin pantalla intermedia ni doble ESC.

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

## Config Jobs / persistencia

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

- [ ] la release `dev-latest` contiene exactamente `jobsmenu-0.45.0.jar`;
- [ ] el SHA-256 descargado coincide con el digest de GitHub;
- [ ] `refs/tags/dev-latest` apunta exactamente al mismo SHA que `main` publicado;
- [ ] el workflow completó `Publish` → `Move rolling development tag` → `Remove obsolete release JARs`.

## Diagnóstico de fallos

Guardar `latest.log` y anotar pantalla, secuencia exacta de navegación, resolución, GUI Scale, filtro/foco/scroll antes y después, idioma anterior/pendiente, selección/scroll Multiplayer, perfil indicado y qué GUI exacta abrió Gráficos.
