# Checklist manual de aceptación — 0.20.0

Este checklist se ejecuta dentro de una instancia Forge 1.20.1 real. CI certifica código, recursos y build; **no certifica estética, hitboxes, audio ni compatibilidad visual dentro de Minecraft**.

## Preparación

- [ ] Java 17, Forge 47.x y Minecraft 1.20.1.
- [ ] Instancia `test-1` cerrada antes de sustituir el JAR.
- [ ] Sólo un `jobsmenu-0.20.0.jar` activo en `mods`.
- [ ] Guardar `latest.log` ante crash, pantalla vacía, textura morado/negro o audio huérfano.

## Matriz mínima de layout

Probar como mínimo:

- [ ] 854×480.
- [ ] 1280×720.
- [ ] 1920×1080.
- [ ] ventana estrecha.
- [ ] poca altura.
- [ ] GUI Scale 2.
- [ ] GUI Scale 3.
- [ ] GUI Scale 4.
- [ ] español.
- [ ] inglés.
- [ ] Español (Uruguay).

En todos:

- [ ] no hay texto sobre botones;
- [ ] no hay footer sobre Volver;
- [ ] no hay controles fuera de pantalla;
- [ ] foco de teclado visible;
- [ ] no aparecen claves `jobsmenu.*`;
- [ ] no aparece título vanilla por debajo de Jobs;
- [ ] no hay hitboxes invisibles capturando click/foco.

## Título y sesión

- [ ] `TitleScreen` entra a Jobs, no al título vanilla.
- [ ] Los renglones principales responden dentro de su hitbox.
- [ ] Renunciar conserva confirmación.
- [ ] Música/ambiente continúan al entrar a subpantallas.
- [ ] Entrar a mundo/servidor corta música y ambiente desde gameplay.
- [ ] Salir de mundo/servidor/kick vuelve a Jobs.

## Pausa 0.20.0

- [ ] El mundo real sigue visible detrás de la hoja.
- [ ] El nuevo oscurecido por capas no tapa completamente el contexto.
- [ ] La sombra de la hoja no invade renglones ni hitboxes.
- [ ] Las guías laterales se mantienen fuera de los controles.
- [ ] Escape reanuda.
- [ ] Condiciones abre `PantallaOpcionesJobs`.
- [ ] Volver desde Options regresa a pausa.
- [ ] M sigue alternando silencio del mod.
- [ ] Salir de mundo local muestra guardado y vuelve a Jobs.
- [ ] Salir de servidor desconecta sin reproducir audio de menú dentro del mundo.

## Options / Config Jobs

- [ ] Config Jobs aparece como acción principal.
- [ ] Piel, Sonido, Video, Controles, Idioma, Chat, Recursos, Accesibilidad y Online abren la pantalla correcta.
- [ ] FOV aparece sólo cuando cabe.
- [ ] Ventana pequeña reduce contenido antes de solapar.
- [ ] Las cinco categorías de Config funcionan y conservan valores.
- [ ] Tabs, toggles y sliders muestran tooltip localizado.

## Widgets Jobs

- [ ] NORMAL, PRINCIPAL, JOBS y TERMINAL se distinguen.
- [ ] Hover y foco de teclado son diferentes y visibles.
- [ ] Press no desplaza hitbox.
- [ ] Disabled tiene estado físico legible.
- [ ] Toggle separa etiqueta y estado.
- [ ] Slider mantiene drag y teclado y muestra escala/porcentaje cuando cabe.
- [ ] Bajo consumo elimina tweens decorativos continuos.
- [ ] Movimiento reducido simplifica transiciones.

## Mundos

- [ ] Lista y previews cargan completas.
- [ ] La superficie central Jobs no tapa mundos ni buscador.
- [ ] Scrollbar Jobs coincide con el scroll real.
- [ ] Ctrl+F enfoca búsqueda y Escape la limpia.
- [ ] Crear/editar/borrar/recrear conservan callbacks vanilla.
- [ ] El título visible es Archivo de turnos / Shift archive.

## Multijugador

- [ ] `JobsDosh.exaroton.me:56477` aparece primero y una sola vez.
- [ ] `Ghoul Outbreak` no aparece.
- [ ] La tarjeta oficial no tapa la lista.
- [ ] Estado de selección/protección visible.
- [ ] Edit/Delete se desactivan para el servidor oficial.
- [ ] Ping, MOTD, LAN, Direct Connect, Add/Edit/Delete/Refresh y Cancel funcionan.
- [ ] F5 refresca.
- [ ] Scrollbar Jobs coincide con el scroll real.

## Mods / Forge — rediseño avanzado

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

## Resource Packs — doble archivador

- [ ] Las dos listas están visualmente separadas.
- [ ] No reaparece dirt vanilla.
- [ ] Selección, orden y aplicar funcionan.
- [ ] Abrir carpeta funciona.
- [ ] Scrollbar Jobs no se dibuja fuera de su lista.
- [ ] No aparece `jobsmenu-musica-activa` legado.

## Idioma — responsive avanzado

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

## Sonido — 0.20.0

- [ ] Todas las opciones vanilla esperadas siguen presentes.
- [ ] Lista llega hasta el final.
- [ ] La bandeja interior no tapa sliders.
- [ ] Raíles laterales no invaden controles.
- [ ] Scrollbar Jobs funciona.
- [ ] Cerrar expediente vuelve al padre.
- [ ] El nuevo marco no cambia hitboxes de sliders.

## Video — 0.20.0

Sin Embeddium:

- [ ] Abre `PantallaVideoJobs`.
- [ ] Marco de calibración no tapa opciones.
- [ ] Marcas de escala quedan fuera de los hitboxes.
- [ ] Scrollbar Jobs funciona.
- [ ] Cerrar expediente vuelve al padre.

Con Embeddium:

- [ ] Se abre la pantalla real de Embeddium.
- [ ] Jobs no reconstruye tabs/opciones de Embeddium.
- [ ] No aparecen controles Jobs superpuestos incorrectamente.

## Otras pantallas

- [ ] Chat, Mouse, Teclas, Online y Accesibilidad conservan todas las opciones vanilla esperadas.
- [ ] Agacharse/Correr conservan Mantener/Alternar.
- [ ] Reasignación y conflictos de teclas funcionan.
- [ ] La Guía de accesibilidad vanilla no se superpone a Cerrar expediente.

## Audio y recarga

- [ ] Absurdism entra con fade-in limpio.
- [ ] Title → Options → Mods → Recursos → volver no reinicia ni duplica música.
- [ ] F3+T no crea instancias fantasma.
- [ ] Alt+Tab no duplica audio.
- [ ] Ducking funciona en transición/Suspensión/presencia.
- [ ] Desde el primer tick jugable no queda audio de menú.

## Fondos 10–17

- [ ] Los ocho PNG cargan sin morado/negro.
- [ ] No zoom.
- [ ] No paneo.
- [ ] No parallax.
- [ ] No flicker.
- [ ] No scanlines animadas.
- [ ] No niebla móvil.
- [ ] No motas/presencia sobre el PNG.

## Cierre de prueba

Si todo pasa:

- [ ] conservar el SHA-256 del JAR probado;
- [ ] anotar resolución/GUI Scale usados;
- [ ] confirmar que `test-1\mods` contiene un único `jobsmenu-0.20.0.jar`;
- [ ] reportar cualquier defecto visual con captura y `latest.log` si afecta recursos/audio/crash.
