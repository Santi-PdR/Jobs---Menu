# Checklist manual de aceptación — 0.23.0

Este checklist se ejecuta dentro de una instancia Forge 1.20.1 real. CI certifica código, recursos y build; **no certifica estética, hitboxes, audio ni compatibilidad visual dentro de Minecraft**.

## Preparación

- [ ] Java 17, Forge 47.x y Minecraft 1.20.1.
- [ ] Instancia `test-1` cerrada antes de sustituir el JAR.
- [ ] Sólo un `jobsmenu-0.23.0.jar` activo en `mods`.
- [ ] Guardar `latest.log` ante crash, pantalla vacía, textura morado/negro o audio huérfano.

## Matriz mínima de layout

Probar como mínimo 854×480, 1280×720, 1920×1080, ventana estrecha, poca altura, GUI Scale 2/3/4, español, inglés y Español (Uruguay).

En todos:

- [ ] no hay texto sobre botones;
- [ ] no hay footer sobre Volver;
- [ ] no hay controles fuera de pantalla;
- [ ] foco de teclado visible;
- [ ] no aparecen claves `jobsmenu.*`;
- [ ] no aparece título vanilla por debajo de Jobs;
- [ ] no hay hitboxes invisibles capturando click/foco;
- [ ] el código técnico de pantalla no tapa el título real;
- [ ] el contador de controles no invade cabeceras;
- [ ] el rail inferior de teclas no tapa botones;
- [ ] los módulos UI/A/M quedan fuera de controles interactivos.

## Main screen 0.23.0

- [ ] `TitleScreen` entra a Jobs, no al título vanilla.
- [ ] Los cuatro renglones principales responden dentro de su hitbox.
- [ ] Renunciar conserva segunda confirmación.
- [ ] El HUD lateral aparece sólo cuando hay ancho suficiente.
- [ ] El HUD lateral no pisa hoja, reloj, crédito ni rótulo de Nivel.
- [ ] El HUD muestra Nivel y estado correctos.
- [ ] Suspension, transición y normal se distinguen.
- [ ] Los cuatro LEDs reflejan rotación, ambiente, música y sonidos UI.
- [ ] El código de perfil coincide con la configuración reconocida.
- [ ] La barra de progreso avanza durante la estancia.
- [ ] La barra de progreso completa durante el traslado.
- [ ] Las marcas 0/25/50/75/100 de la barra son legibles.
- [ ] Las cápsulas F/M/TAB/ENTER no pisan otros elementos.
- [ ] Las marcas técnicas siguen la luz del Nivel sin volverse ilegibles.
- [ ] F cambia de Nivel sólo cuando la rotación está habilitada.
- [ ] M alterna silencio.
- [ ] Música/ambiente continúan al entrar a subpantallas.
- [ ] Entrar a mundo/servidor corta música y ambiente desde gameplay.
- [ ] Salir de mundo/servidor/kick vuelve a Jobs.

## Pausa 0.23.0

- [ ] El mundo real sigue visible detrás de la hoja.
- [ ] El oscurecido por capas no tapa completamente el contexto.
- [ ] La sombra de la hoja no invade renglones ni hitboxes.
- [ ] Las guías laterales se mantienen fuera de los controles.
- [ ] El código técnico `PAUSE` no compite con la cabecera de la hoja.
- [ ] El rail inferior incluye M además de TAB/ENTER/ESC cuando cabe.
- [ ] Los módulos de audio se ven sin tapar el estado LOCAL/SERVER.
- [ ] El panel contextual distingue LOCAL/SERVER correctamente.
- [ ] El código de expediente no invade controles.
- [ ] Escape reanuda.
- [ ] Condiciones abre `PantallaOpcionesJobs`.
- [ ] Volver desde Options regresa a pausa.
- [ ] M sigue alternando silencio del mod.
- [ ] Salir de mundo local muestra guardado y vuelve a Jobs.
- [ ] Salir de servidor desconecta sin reproducir audio de menú dentro del mundo.

## Capa profesional compartida

- [ ] Código MAIN/PAUSE/OPTIONS/CONFIG/etc. coincide con la pantalla.
- [ ] Contador activo/visible cambia de forma coherente entre pantallas.
- [ ] Badge de perfil aparece sólo cuando el perfil puede reconocerse.
- [ ] UI/A/M distinguen encendido y apagado.
- [ ] TAB/ENTER/ESC son legibles en 1280×720 y 1920×1080.
- [ ] Ventana pequeña elimina elementos secundarios antes de solapar.
- [ ] Foco de teclado es más fuerte que hover de ratón.
- [ ] Hover no altera hitbox ni captura eventos nuevos.
- [ ] La línea segmentada inferior no invade botones.
- [ ] Alto contraste refuerza la instrumentación sin volverla agresiva.
- [ ] Movimiento reducido congela la respiración de foco.
- [ ] Bajo consumo elimina la actividad superior continua.
- [ ] El expediente raro, si aparece, queda fuera de controles.

## Transiciones y atmósfera

- [ ] La transición entre expedientes se siente suave y no bloquea input.
- [ ] La transición completa dura aproximadamente 470 ms.
- [ ] Se leen doble borde, registros y perforaciones sin que parezcan scanlines.
- [ ] La ficha interna de transición no deja residuos al terminar.
- [ ] La segunda sombra de salida desaparece completamente.
- [ ] Movimiento reducido simplifica la transición a fade.
- [ ] Bajo consumo elimina barridos decorativos continuos.
- [ ] Las cuatro esquinas de registro quedan fuera del contenido principal.
- [ ] Los ticks de calibración son discretos.
- [ ] Papel limpio elimina ticks secundarios.
- [ ] El pulso de borde inferior sólo aparece en pantallas grandes.
- [ ] Los barridos horizontales/verticales son sutiles y no mueven el background.
- [ ] Cambiar rápidamente entre pantallas no deja overlays congelados.

## Options / Config Jobs

- [ ] Config Jobs aparece como acción principal.
- [ ] Piel, Sonido, Video, Controles, Idioma, Chat, Recursos, Accesibilidad y Online abren la pantalla correcta.
- [ ] FOV aparece sólo cuando cabe.
- [ ] Ventana pequeña reduce contenido antes de solapar.
- [ ] Las seis categorías de Config funcionan y conservan valores.
- [ ] Perfiles Equilibrado/Inmersivo/Rendimiento/Accesible/Mínimo aplican valores reales.
- [ ] El código de perfil global cambia al aplicar un preset reconocible.
- [ ] Tabs, toggles y sliders muestran tooltip localizado.

## Widgets Jobs

- [ ] NORMAL, PRINCIPAL, JOBS y TERMINAL se distinguen.
- [ ] Hover y foco de teclado son diferentes y visibles.
- [ ] Press no desplaza hitbox.
- [ ] Disabled tiene estado físico legible.
- [ ] Toggle separa etiqueta y estado.
- [ ] Slider mantiene drag y teclado y muestra escala/porcentaje cuando cabe.
- [ ] El foco externo de 0.23.0 no duplica de forma molesta el foco interno del widget.
- [ ] Bajo consumo elimina tweens decorativos continuos.
- [ ] Movimiento reducido simplifica transiciones.

## Mundos

- [ ] Lista y previews cargan completas.
- [ ] La superficie central Jobs no tapa mundos ni buscador.
- [ ] Scrollbar Jobs coincide con el scroll real.
- [ ] Ctrl+F enfoca búsqueda y Escape la limpia.
- [ ] Crear/editar/borrar/recrear conservan callbacks vanilla.
- [ ] El título visible es Archivo de turnos / Shift archive.
- [ ] La capa profesional no tapa el buscador ni previews.

## Multijugador

- [ ] `JobsDosh.exaroton.me:56477` aparece primero y una sola vez.
- [ ] `Ghoul Outbreak` no aparece.
- [ ] La tarjeta oficial no tapa la lista.
- [ ] Estado de selección/protección visible.
- [ ] Edit/Delete se desactivan para el servidor oficial.
- [ ] Ping, MOTD, LAN, Direct Connect, Add/Edit/Delete/Refresh y Cancel funcionan.
- [ ] F5 refresca.
- [ ] Scrollbar Jobs coincide con el scroll real.
- [ ] La capa profesional no tapa ping/MOTD/botones.

## Mods / Forge

- [ ] Lista completa de mods.
- [ ] A–Z / Z–A funciona.
- [ ] Buscar filtra correctamente.
- [ ] Ctrl+F enfoca búsqueda y Escape limpia.
- [ ] Catálogo y panel de detalle se distinguen visualmente.
- [ ] Logos no quedan tapados por el marco Jobs.
- [ ] Panel de información sigue legible.
- [ ] Config sólo se activa cuando corresponde.
- [ ] Open mods folder conserva acción real.
- [ ] Scrollbar Jobs funciona y coincide con Forge.
- [ ] No aparece título Forge duplicado.
- [ ] Código técnico, contador y rail inferior no pisan la lista.

## Resource Packs

- [ ] Las dos listas están visualmente separadas.
- [ ] No reaparece dirt vanilla.
- [ ] Selección, orden y aplicar funcionan.
- [ ] Abrir carpeta funciona.
- [ ] Scrollbar Jobs no se dibuja fuera de su lista.
- [ ] No aparece `jobsmenu-musica-activa` legado.
- [ ] Capa profesional no invade las dos bandejas.

## Idioma

- [ ] Lista completa hace scroll.
- [ ] Buscador conserva teclado, selección y portapapeles.
- [ ] Ctrl+F enfoca búsqueda.
- [ ] Escape limpia búsqueda antes de cerrar.
- [ ] Hover, selección pendiente e idioma aplicado son distinguibles.
- [ ] Código de idioma se ve como badge sin tapar el nombre.
- [ ] Cambio actual → pendiente es legible.
- [ ] Aplicar recarga recursos y vuelve al padre.
- [ ] Error de recarga no deja la pantalla bloqueada en “aplicando”.
- [ ] GUI Scale 4 / ventana estrecha no desborda panel ni botones.
- [ ] ES ↔ EN funciona.
- [ ] Español (Uruguay) conserva textos Jobs en español.

## Sonido y Video

- [ ] Sonido conserva todas las opciones vanilla esperadas y scrollbar Jobs.
- [ ] La bandeja interior no tapa sliders.
- [ ] Sin Embeddium abre `PantallaVideoJobs` y su marco no tapa opciones.
- [ ] Con Embeddium se abre la pantalla real de Embeddium sin reconstrucción Jobs incorrecta.

## Audio y recarga

- [ ] Absurdism entra con fade-in limpio.
- [ ] Title → Options → Mods → Recursos → volver no reinicia ni duplica música.
- [ ] F3+T no crea instancias fantasma.
- [ ] Alt+Tab no duplica audio.
- [ ] Ducking funciona en transición/Suspensión/presencia donde corresponde.
- [ ] Desde el primer tick jugable no queda audio de menú.
- [ ] Indicadores UI/A/M reflejan configuración después de volver de Config Jobs.

## Fondos 10–17

- [ ] Los ocho PNG cargan sin morado/negro.
- [ ] Se ven suavizados por filtrado lineal al escalar.
- [ ] No zoom.
- [ ] No paneo.
- [ ] No parallax.
- [ ] No flicker propio.
- [ ] No scanlines animadas sobre el PNG.
- [ ] No niebla móvil propia.
- [ ] No motas/presencia sobre el PNG.
- [ ] Fades/apagones globales funcionan sin mover la imagen.
- [ ] Transición de expediente puede pasar por encima sin deformar la imagen.
- [ ] Capa profesional y atmósfera UI pueden pasar por encima sin desplazar el PNG.

## Cierre de prueba

Si todo pasa:

- [ ] conservar el SHA-256 del JAR probado;
- [ ] anotar resolución/GUI Scale usados;
- [ ] confirmar que `test-1\mods` contiene un único `jobsmenu-0.23.0.jar`;
- [ ] reportar cualquier defecto visual con captura y `latest.log` si afecta recursos/audio/crash.
