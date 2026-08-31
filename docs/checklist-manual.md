# Checklist manual de aceptación — 0.12.0

Este checklist es para una instancia Forge 1.20.1 real. La auditoría estática y el build no sustituyen las comprobaciones visuales, de navegación, audio y lifecycle.

## Preparación

- [ ] Java 17, Forge 47.x y Minecraft 1.20.1.
- [ ] Instancia `test-1` cerrada antes de sustituir el JAR.
- [ ] Sólo un `jobsmenu-0.12.0.jar` activo en `mods`.
- [ ] Respaldar `config/jobsmenu-client.toml` y `options.txt` si se van a comparar preferencias.
- [ ] Guardar `latest.log` de cualquier prueba que termine en crash, pantalla vacía, textura morado/negro o audio huérfano.

## Título y pausa

- [ ] El título Jobs sólo sustituye el `TitleScreen` vanilla previsto.
- [ ] `01`, `02`, `03` y `04` responden dentro de su hitbox real.
- [ ] Renunciar exige la confirmación prevista y no se activa por inercia.
- [ ] Escape sigue bloqueado en el título propio.
- [ ] Escape reanuda desde la pausa propia.
- [ ] F3+Esc conserva su pausa vanilla sin menú.
- [ ] Desde la pausa, **Condiciones de estancia** abre directamente `PantallaOpcionesJobs`.
- [ ] Volver desde ese hub regresa a la pausa, no al título.
- [ ] Dejar el turno mantiene la secuencia de guardado/desconexión de Minecraft y muestra el título vanilla una sola vez.

## Hub de Opciones Jobs

- [ ] Piel, Sonido, Video, Controles, Idioma, Chat, Resource Packs, Accesibilidad, Online y Ajustes Jobs abren la pantalla correcta.
- [ ] El slider de FOV modifica el FOV real y conserva su valor tras reiniciar.
- [ ] Volver regresa al padre correcto tanto desde título como desde pausa.
- [ ] No aparecen botones vanilla duplicados ni controles invisibles capturando foco.
- [ ] En poca altura/ancho lógico se activa el layout compacto sin solapar FOV, filas y Volver.
- [ ] Tab/Shift+Tab recorren controles en un orden razonable; Enter/Espacio activan el foco.

## Sonido

- [ ] La lista completa de Sonido sigue disponible y hace scroll hasta el final.
- [ ] Cabecera/pie Jobs no cubren sliders vanilla.
- [ ] El botón vanilla Done no aparece ni recibe foco.
- [ ] Volver retorna al hub Jobs.
- [ ] Abrir/cerrar Sonido repetidamente no reinicia música ni camas ambientales.

## Video

- [ ] Sin Embeddium, la pantalla vanilla tematizada contiene todas las opciones esperadas.
- [ ] Con Embeddium, se abre su pantalla real y Jobs no rompe sus pestañas/opciones.
- [ ] La banda contextual en una pantalla externa no cubre controles críticos.
- [ ] No hay duplicación de pantalla o bucle de redirecciones.

## Controles, Mouse y Teclas

- [ ] Agacharse y Correr muestran **Mantener/Alternar** según el valor real.
- [ ] Autojump y pestaña de operador muestran Activado/Desactivado correctamente.
- [ ] Mouse abre la pantalla real de sensibilidad/opciones y hace scroll completo.
- [ ] Teclas conserva búsqueda/lista/reasignación, conflictos y reset vanilla.
- [ ] Escape/Volver siempre retorna al hub de Controles y luego al hub principal.

## Idioma

- [ ] La lista muestra todos los idiomas disponibles y hace scroll completo.
- [ ] Un clic selecciona pendiente; doble clic aplica.
- [ ] Aplicar ES ↔ EN recarga recursos y vuelve al padre correcto.
- [ ] Fuente Unicode cambia el valor real y persiste.
- [ ] Durante la recarga no se aceptan clics duplicados.
- [ ] Música y ambiente se recuperan después de recargar recursos sin apilar instancias.

## Chat, Online y Resource Packs

- [ ] Chat conserva todas sus opciones y scroll.
- [ ] Online conserva notificaciones Realms y listado de servidor.
- [ ] Resource Packs mantiene ambas listas, selección, orden, abrir carpeta y aplicar.
- [ ] Aplicar un pack no corta permanentemente música/ambiente.
- [ ] El estilo Jobs no reduce hitboxes ni oculta tooltips.

## Accesibilidad

- [ ] Todas las opciones vanilla de accesibilidad siguen presentes.
- [ ] Al final aparecen las ayudas Jobs: movimiento reducido, destellos reducidos, alto contraste y texto grande.
- [ ] Cambiarlas ahí actualiza inmediatamente el aviso y persiste en config.
- [ ] Movimiento reducido simplifica también `TransicionInterfazJobs`.
- [ ] Destellos reducidos no deja flashes durante cambio de Nivel, UI o Suspensión.
- [ ] Alto contraste nunca usa rojo fuera de Executores.
- [ ] Texto grande no genera solapamientos al volver al título/pausa.

## Piel

- [ ] Cada `PlayerModelPart` refleja el estado real y se puede alternar.
- [ ] Mano principal cambia izquierda/derecha y persiste.
- [ ] Los textos largos usan elipsis antes de salir de la ficha.

## Multijugador

- [ ] La lista conserva servidores guardados, iconos, ping, MOTD y búsqueda LAN.
- [ ] Seleccionar sólo se activa cuando la acción vanilla puede ejecutarse.
- [ ] Editar/Borrar respetan el estado real de la selección.
- [ ] Conexión directa funciona.
- [ ] Añadir servidor funciona y persiste.
- [ ] Editar servidor modifica el registro correcto.
- [ ] Borrar conserva la confirmación/flujo vanilla.
- [ ] Refrescar repuebla la lista.
- [ ] Cancelar vuelve al padre correcto.
- [ ] Los diálogos secundarios vanilla con banda Jobs siguen siendo totalmente utilizables.

## Layout y escalado

Probar al menos:

- [ ] 854×480.
- [ ] 1280×720.
- [ ] 1920×1080.
- [ ] Ventana estrecha.
- [ ] Ventana con poca altura.
- [ ] GUI Scale mínimo razonable.
- [ ] GUI Scale máximo razonable.
- [ ] Español.
- [ ] Inglés.
- [ ] Fuente/resource pack con métricas más anchas de lo normal.

En todos los casos:

- [ ] No hay texto encima de botones.
- [ ] No hay pie de formulario encima de Volver.
- [ ] No hay controles fuera de pantalla.
- [ ] El foco de teclado se ve.
- [ ] La narración no pronuncia claves `jobsmenu.*` sin traducir.

## Escena y audio durante interfaces

- [ ] Los 18 niveles 0–17 siguen rotando según configuración.
- [ ] Abrir una interfaz no reinicia el Nivel ni cambia de fondo por sí solo.
- [ ] Las camas BASE/CARÁCTER/ACTIVIDAD mantienen continuidad durante una visita al menú.
- [ ] Cambiar de Nivel con una subpantalla abierta no deja el nuevo recinto sin ambiente.
- [ ] Entrar a un mundo sí detiene el ambiente del menú.
- [ ] Tecla M mantiene su comportamiento en las pantallas donde está documentada.
- [ ] F3+T, Alt+Tab, minimizar/restaurar y recarga de resource packs no duplican sonidos.
- [ ] La Suspensión puede ocurrir sin romper una pantalla hija y se recupera correctamente.

## Fondos y efectos

- [ ] Niveles 10–17 nunca muestran morado/negro.
- [ ] Zoom/paneo no enseñan bordes de PNG.
- [ ] Movimiento reducido congela capas decorativas previstas.
- [ ] Bajo consumo mantiene la composición principal y quita las capas caras.
- [ ] Destellos reducidos no crea flashes al navegar entre expedientes.

## Build y entrega

- [ ] `python tools/verificar_version.py` termina sin fallos.
- [ ] `python tools/verificar_fondos.py` termina sin fallos.
- [ ] `python tools/verificar.py` termina sin fallos.
- [ ] GitHub Actions compila con Java 17.
- [ ] El artefacto generado se llama exactamente `jobsmenu-0.12.0.jar`.
- [ ] La PR de 0.12.0 queda verde antes de fusionar.
- [ ] `main` vuelve a compilar en verde después del merge.
- [ ] `dev-latest` contiene el JAR 0.12.0 versionado y no un `jobsmenu-latest.jar` genérico.

## Evidencia

Por cada fallo registrar:

- versión exacta del JAR;
- pantalla y ruta usada para llegar;
- Nivel visible;
- resolución y GUI Scale;
- idioma;
- mods de UI/video activos (especialmente Embeddium);
- opciones de accesibilidad/bajo consumo;
- captura o video corto cuando sea visual;
- `latest.log` cuando afecte recursos, audio, conexión o crash.

Un build verde certifica compilación y verificadores; la aceptación visual requiere esta prueba dentro de Minecraft.
