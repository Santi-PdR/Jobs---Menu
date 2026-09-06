# Checklist manual de aceptación — 0.46.0

CI certifica código, recursos y build; este checklist certifica la experiencia real dentro de Forge 1.20.1.

## Preparación

- [ ] Java 17 + Forge 47.x + Minecraft 1.20.1.
- [ ] cerrar `test-1` antes de sustituir el JAR.
- [ ] dejar un único `jobsmenu-0.46.0.jar` en `mods`.
- [ ] conservar `latest.log` si aparece crash, audio huérfano o recurso faltante.

## Config Jobs / búsqueda — 0.46

- [ ] `Ctrl+F` abre el buscador transversal;
- [ ] buscar por nombre, detalle o categoría devuelve resultados correctos;
- [ ] Enter abre la categoría del resultado seleccionado;
- [ ] doble clic hace lo mismo sin doble navegación;
- [ ] abrir un resultado de la categoría actual vuelve a esa misma Config sin recreaciones extra;
- [ ] el buscador no necesita simular teclas 1–6 para navegar;
- [ ] primer ESC con texto limpia filtro, segundo suelta foco y el siguiente vuelve a Config;
- [ ] Volver/ESC repetidos no producen doble retorno;
- [ ] resize/maximizar conserva filtro, foco razonable y scroll;
- [ ] mover el mouse por una lista larga no muestra stutter creciente;
- [ ] Config recuerda la última pestaña usada durante la sesión;
- [ ] editar un valor de preset muestra `CUSTOM` y reaplicar el preset restaura su nombre.

## Idioma + Force Unicode Font — 0.46

- [ ] `Ctrl+F` sigue enfocando búsqueda;
- [ ] elegir idioma y redimensionar conserva selección pendiente, filtro y scroll;
- [ ] cambiar **sólo Force Unicode Font** y pulsar Aplicar provoca una recarga de recursos y aplica el cambio sin reiniciar;
- [ ] cambiar idioma + Unicode juntos provoca una sola recarga y aplica ambos;
- [ ] aplicar sin cambios vuelve al padre sin recargar innecesariamente;
- [ ] una recarga exitosa vuelve al padre una sola vez;
- [ ] si reload falla, la pantalla queda abierta y muestra feedback de error;
- [ ] tras fallo, `Options.languageCode`, `LanguageManager` y Force Unicode Font vuelven juntos al estado anterior;
- [ ] un segundo intento funciona sin reiniciar;
- [ ] un callback que termine tarde no cambia de Screen si el usuario ya navegó a otra;
- [ ] ESC/Volver no produce doble `setScreen()`.

## Mundos / Mods — continuidad heredada

En ambas pantallas:

- [ ] `Ctrl+F` enfoca búsqueda;
- [ ] resize conserva filtro y foco relevante;
- [ ] ESC con texto limpia filtro, luego suelta foco y después sale;
- [ ] botón/ESC repetido no produce doble retorno.

## Navegación / callbacks

- [ ] Apariencia, Controles y Config abren/cierran repetidamente sin salto doble;
- [ ] Resource Packs aplica/cierra y vuelve a Opciones Jobs una sola vez;
- [ ] abandonar Resource Packs y navegar a otra Screen impide que un callback tardío vuelva inesperadamente;
- [ ] cambiar GUI Scale dentro de estos flujos no deja pantallas bloqueadas.

## Sonido / rendimiento

- [ ] abrir/cerrar Sonido muchas veces no genera stutter creciente;
- [ ] sliders y categorías vanilla siguen completos y funcionales;
- [ ] no desaparecen controles por fallo de reflection;
- [ ] hover/click Jobs permitido sigue sonando una sola vez donde corresponde.

## Gráficos — contrato 0.44 heredado

Con Embeddium instalado:

- [ ] **Opciones → Gráficos** abre la interfaz original de Embeddium;
- [ ] no aparece marco, cabecera, banda, transición, overlay ni recolocación Jobs;
- [ ] botones, pestañas, tooltips y controles son los originales del modpack;
- [ ] no se reemplaza hover/click dentro de Gráficos;
- [ ] ESC/Done vuelve una sola vez a Opciones Jobs;
- [ ] abrir/cerrar varias veces no duplica widgets.

Sin Embeddium:

- [ ] se abre `VideoSettingsScreen` vanilla intacto;
- [ ] ESC/Done vuelve a Opciones Jobs.

## MODPACK / terceros

- [ ] no existe botón MODPACK ni hitbox invisible equivalente;
- [ ] una Screen de otro mod no recibe chrome, transición, hover/click ni recolocación Jobs;
- [ ] submenús abiertos desde una Screen externa siguen fuera de Jobs;
- [ ] volver al padre Jobs funciona sin pantalla intermedia ni doble ESC.

## Audio / lifecycle

- [ ] Aleatoria reproduce sólo Absurdism, REQUIEM o Upon the Hill V2;
- [ ] nunca aparece música de menú vanilla ni fallback `ambient.cave`;
- [ ] entrar a mundo/servidor corta inmediatamente música, camas y FX;
- [ ] volver al menú no duplica audio;
- [ ] F3+T/resource reload reconstruye audio una sola vez;
- [ ] Alt+Tab no crea instancias fantasma;
- [ ] créditos: REQUIEM = `Emmy Z - Forsaken OST`, Upon the Hill V2 = `ft. @iCosmicCoffee`.

## Config / persistencia

- [ ] toggles aplican un cambio una sola vez;
- [ ] sliders rápidos no causan stutter por escritura continua;
- [ ] salir conserva último valor y reiniciar Minecraft mantiene música, volúmenes, nivel fijo y accesibilidad;
- [ ] aplicar dos veces el mismo perfil no causa efectos secundarios.

## Multiplayer

- [ ] `JobsDosh.exaroton.me:56477` aparece primero y una sola vez;
- [ ] `Ghoul Outbreak` no aparece;
- [ ] servidor oficial no se puede editar/borrar;
- [ ] Direct Connect/Add/Edit/Delete funcionan para el resto;
- [ ] F5 y resize/GUI Scale conservan selección y scroll;
- [ ] LAN, ping, MOTD y favicons siguen funcionando;
- [ ] ESC vuelve al padre con una pulsación;
- [ ] Cancelar/error pre-login vuelve a la lista Jobs;
- [ ] salida/kick remoto vuelve a Multiplayer Jobs y mundo local al main Jobs.

## Main / gameplay / fondos

Probar 854×480, 1280×720, 1920×1080, ventana estrecha y GUI Scale 2/3/4.

- [ ] no hay títulos vanilla duplicados ni claves `jobsmenu.*` visibles;
- [ ] no reaparece la barra visible `1-4/F/M/N/TAB/ENTER`;
- [ ] controles mantienen hitboxes correctas;
- [ ] chat, inventario y contenedores no reciben skin ni transición;
- [ ] con mundo cargado no aparece ninguna transición Jobs;
- [ ] PNG 10–17 permanecen totalmente estáticos;
- [ ] JPG 18–31 mantienen respiración sutil/no destructiva y se congelan con Movimiento reducido/Bajo consumo/escena quieta.

## Publicación

- [ ] `dev-latest` contiene exactamente `jobsmenu-0.46.0.jar`;
- [ ] SHA-256 descargado coincide con digest de GitHub;
- [ ] `refs/tags/dev-latest` apunta exactamente al mismo SHA que `main` publicado;
- [ ] workflow completó publicación → movimiento de tag → limpieza de assets.

## Diagnóstico

Guardar `latest.log` y anotar pantalla, secuencia exacta, resolución, GUI Scale, filtro/foco/scroll, idioma y Unicode anterior/pendiente, selección Multiplayer, perfil indicado y GUI exacta que abrió Gráficos.
