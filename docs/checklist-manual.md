# Checklist manual de aceptación — 0.25.0

Este checklist se ejecuta dentro de una instancia Forge 1.20.1 real. CI certifica código, recursos y build; **no certifica estética, hitboxes, audio ni compatibilidad visual dentro de Minecraft**.

## Preparación

- [ ] Java 17, Forge 47.x y Minecraft 1.20.1.
- [ ] Instancia `test-1` cerrada antes de sustituir el JAR.
- [ ] Sólo un `jobsmenu-0.25.0.jar` activo en `mods`.
- [ ] Guardar `latest.log` ante crash, pantalla vacía, textura morado/negro o audio huérfano.

## Matriz mínima de layout

Probar 854×480, 1280×720, 1920×1080, ventana estrecha, poca altura, GUI Scale 2/3/4, español, inglés y Español (Uruguay).

En todos:

- [ ] no hay texto sobre botones ni footer sobre Volver;
- [ ] no hay controles fuera de pantalla ni hitboxes invisibles;
- [ ] foco de teclado visible y distinto del hover;
- [ ] no aparecen claves `jobsmenu.*` ni títulos vanilla duplicados;
- [ ] código técnico, breadcrumb, reloj de sesión y barra inferior no pisan contenido;
- [ ] la instrumentación secundaria desaparece antes que el contenido al reducir ventana.

## Main screen 0.25.0

- [ ] `TitleScreen` entra a Jobs.
- [ ] Los cuatro renglones responden dentro de su hitbox.
- [ ] 1/2/3/4 activan los renglones 01/02/03/04 respectivamente.
- [ ] Keypad 1/2/3/4 hace lo mismo.
- [ ] El primer 4 sólo arma la confirmación de Renunciar; no cierra de inmediato.
- [ ] El segundo 4 dentro de la ventana de confirmación ejecuta la salida igual que el click real.
- [ ] F cambia de Nivel sólo con rotación habilitada.
- [ ] M alterna silencio y el HUD pasa a `MUTE` al llegar a cero.
- [ ] HUD muestra Nivel y normal/transición/Suspensión correctamente.
- [ ] LEDs R/A/M/U reflejan rotación, ambiente, música y sonidos UI.
- [ ] Perfil reconocido o `CUSTOM` es coherente.
- [ ] Progreso de estancia avanza y el cursor acompaña.
- [ ] `NXT MM:SS`, `NXT HOLD` y `NXT MOVE` aparecen en el estado correcto.
- [ ] `T+MM:SS` avanza sin reiniciarse al volver de subpantallas.
- [ ] Barra de volumen y valor numérico coinciden con Config Jobs.
- [ ] Chips 1-4/F/M/TAB/ENTER no pisan hoja, ronda, crédito ni rótulo del Nivel.

## Pausa 0.25.0

- [ ] El mundo real sigue visible detrás de la hoja.
- [ ] 1 y keypad 1 reanudan.
- [ ] 2 y keypad 2 abren Condiciones.
- [ ] 3 y keypad 3 **no** desconectan ni accionan la salida.
- [ ] Escape reanuda y M alterna silencio.
- [ ] Condiciones vuelve correctamente a pausa.
- [ ] LOCAL/SERVER y código de expediente no invaden controles.
- [ ] Salir manualmente conserva guardado/desconexión real y retorna a Jobs.

## Atajos y campos de texto

- [ ] Con un buscador/EditBox enfocado, escribir 1–4 produce texto y no acciona renglones.
- [ ] Ctrl+1, Alt+1, Shift+1 y combinaciones equivalentes no disparan el atajo Jobs.
- [ ] Ctrl+F continúa funcionando en Mundos, Mods e Idioma.
- [ ] F5 continúa refrescando Multiplayer.
- [ ] F1–F5 continúan aplicando perfiles en Config Jobs donde corresponde.
- [ ] Los atajos visibles de cada pantalla corresponden con funciones realmente implementadas.

## Instrumentación contextual 0.25.0

- [ ] Código MAIN/PAUSE/OPTIONS/CONFIG/WORLDS/MULTI/MODS/RESOURCES/LANG/etc. coincide con la pantalla.
- [ ] En ventanas amplias aparece el título real de la Screen como contexto secundario.
- [ ] El contador activos/totales y su barra cambian coherentemente.
- [ ] Breadcrumb conserva como máximo tres familias recientes y no repite la misma consecutivamente.
- [ ] Breadcrumb se reinicia al iniciar una visita nueva al menú.
- [ ] `T+MM:SS` no se reinicia al abrir Options/Mods/Resources dentro de la misma visita.
- [ ] `Sxx` aumenta al abrir pantallas distintas y no aumenta cada frame.
- [ ] `Vxxx` coincide con volumen Jobs; cero se muestra como `MUTE`.
- [ ] Foco por teclado muestra `KEY`; hover sin foco muestra `PTR`.
- [ ] Tipo de control se reconoce como TOG/SLD/TXT/ROW/BTN/CTL.
- [ ] Posición `nn/nn` cambia al navegar con TAB.
- [ ] Etiqueta del control actual se recorta antes de invadir metadatos.
- [ ] Sin control actual, el perfil aparece como contexto cuando hay espacio.
- [ ] Interfaz mínima elimina breadcrumb/telemetría secundaria antes de afectar usabilidad.
- [ ] Movimiento reducido y Bajo consumo reemplazan actividad móvil por referencias fijas.
- [ ] Alto contraste refuerza la instrumentación sin volverla ilegible.

## Controles Jobs y controles vanilla/Forge

- [ ] NORMAL/PRINCIPAL/JOBS/TERMINAL siguen diferenciados.
- [ ] Toggle separa etiqueta y estado y mantiene teclado/click.
- [ ] Slider Jobs conserva drag/teclado/valor.
- [ ] Botones vanilla tematizados mantienen callback e hitbox originales.
- [ ] Botón vanilla hover y foco KEY se distinguen.
- [ ] Texto truncado muestra marca de recorte sin tapar la etiqueta.
- [ ] Disabled se entiende como indisponible y no parece seleccionado.
- [ ] Slider vanilla conserva drag y teclado.
- [ ] Escala de slider vanilla muestra marcas reforzadas en 0/50/100.
- [ ] Foco KEY de slider muestra notch superior sin mover el control.
- [ ] EditBox conserva cursor, selección, copy/paste y escritura.
- [ ] EditBox enfocado muestra doble marco/notch sin tapar texto.
- [ ] Campos no editables se distinguen sin parecer deshabilitados de forma incorrecta.

## Scrollbars Jobs 0.25.0

Probar Mundos, Multiplayer, Mods, Resource Packs e Idioma:

- [ ] rueda, click y drag reales siguen funcionando;
- [ ] scrollbar coincide con el scroll real de la lista;
- [ ] tramo recorrido del canal avanza con el scroll;
- [ ] escala visual 0/25/50/75/100 queda alineada;
- [ ] topes y chevrons no invaden entradas;
- [ ] cursor externo izquierdo/derecho coincide con la posición;
- [ ] thumb tiene doble sombra y grip sin salirse del canal;
- [ ] al llegar arriba/abajo no hay overflow visual.

## Pantallas grandes

### Mundos
- [ ] búsqueda, crear/editar/borrar/recrear conservan callbacks vanilla.
- [ ] Ctrl+F y Escape funcionan.
- [ ] previews, breadcrumb y scrollbar no se pisan.

### Multiplayer
- [ ] `JobsDosh.exaroton.me:56477` aparece primero y una sola vez.
- [ ] `Ghoul Outbreak` no aparece.
- [ ] servidor oficial no se puede editar/borrar desde Jobs.
- [ ] Ping/MOTD/LAN/Direct Connect/Add/Edit/Delete/Refresh/Cancel funcionan.
- [ ] F5 refresca y la barra contextual lo anuncia.

### Mods / Forge
- [ ] lista completa, A–Z/Z–A, buscar, Config y Open mods folder funcionan.
- [ ] catálogo y detalle no se solapan.
- [ ] no reaparece dirt/título Forge duplicado.
- [ ] Ctrl+F/Escape, scrollbar y barra contextual conviven con la lista real.

### Resource Packs
- [ ] las dos listas siguen separadas y sin dirt vanilla.
- [ ] selección, orden, aplicar y abrir carpeta funcionan.
- [ ] scrollbar no se dibuja fuera de cada lista.

### Idioma
- [ ] lista, buscador, portapapeles, Ctrl+F, Escape y Aplicar funcionan.
- [ ] hover, pendiente y aplicado se distinguen.
- [ ] ES/EN y Español (Uruguay) conservan textos Jobs correctos.

## Sonido, Video y sesión

- [ ] Sonido conserva opciones vanilla y scrollbar Jobs.
- [ ] Sin Embeddium abre Video Jobs; con Embeddium se respeta su UI real.
- [ ] En visitas nuevas pueden iniciar **Absurdism**, **REQUIEM** o **Upon the Hill V2**.
- [ ] `N` cambia mediante crossfade a una pista distinta de la actual.
- [ ] Pulsar `N` otra vez durante el crossfade no crea una tercera instancia.
- [ ] El HUD `TRK` y el crédito visible siguen a la pista dominante real.
- [ ] REQUIEM muestra `Emmy Z - Forsaken OST`; Upon the Hill V2 muestra `ft. @iCosmicCoffee`; Absurdism no inventa autor.
- [ ] La rotación automática cambia aproximadamente cada 2–4 minutos y no repite inmediatamente la misma pista.
- [ ] `M` silencia/restaura el sistema sin cambiar de pista accidentalmente.
- [ ] Main → Options → Mods → Recursos → volver no reinicia ni duplica música.
- [ ] F3+T y Alt+Tab no crean instancias fantasma.
- [ ] entrar a gameplay corta música/ambiente desde el primer tick jugable.
- [ ] salir de mundo/servidor/kick recupera Jobs.

## Fondos 10–17

- [ ] los ocho PNG cargan sin morado/negro y con filtrado lineal.
- [ ] no zoom, paneo, parallax, flicker, scanlines animadas, niebla móvil, motas ni presencia propia.
- [ ] fades/apagones/transición de expediente funcionan sin mover ni deformar la imagen.
- [ ] breadcrumb, HUD, foco y demás overlays pueden pasar por encima sin alterar el PNG.

## Cierre de prueba

Si todo pasa:

- [ ] conservar SHA-256 del JAR probado;
- [ ] anotar resolución/GUI Scale usados;
- [ ] confirmar que `test-1\mods` contiene un único `jobsmenu-0.25.0.jar`;
- [ ] reportar defecto visual con captura y `latest.log` si afecta recursos/audio/crash.
