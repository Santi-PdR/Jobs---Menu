# Riesgos y pruebas pendientes — 0.13.0

Este documento describe sólo el estado vigente. El historial de incidentes de builds anteriores se conserva en `CHANGELOG.md` y en las auditorías históricas de `docs/`.

## Estado certificado por CI

Antes de publicar una entrega, GitHub Actions comprueba:

- Java 17;
- política de nombre versionado (`tools/verificar_version.py`);
- integridad real de los fondos 10–17 (`tools/verificar_fondos.py`: CRC + IDAT + descompresión);
- recursos, idiomas y coherencia estática (`tools/verificar.py`);
- build Forge/Gradle;
- creación del JAR versionado;
- publicación en `dev-latest` únicamente desde `main`.

Un build que no termina en verde no actualiza la release de prueba.

## Pruebas manuales todavía necesarias

CI puede certificar archivos y compilación, pero no reemplaza Minecraft real. Antes de considerar 0.13.0 completamente validada hay que probar en `test-1`:

1. **PNG 10–17 estáticos.** Permanecer varios segundos en cada uno. No debe existir zoom, paneo, respiración, scanline, flicker, niebla móvil, motas, presencia ni otra animación aplicada sobre la imagen.
2. **Transición entre PNG.** Los apagones/cambios de Nivel pueden seguir ocurriendo, pero al estabilizarse el Nivel la imagen debe quedar inmóvil.
3. **Mods → Jobs Menu → Config.** El botón Config de Forge debe volver a existir y abrir `PantallaAjustesAviso`.
4. **Config desde Jobs.** El botón de ajustes del aviso en Condiciones de estancia debe abrir la misma pantalla y regresar al hub correcto.
5. **Scrollbar Jobs.** Sonido, Chat, Accesibilidad, Teclas, Online, Resource Packs y ajustes deben mostrar la barra de papel/tinta cuando el contenido exceda el viewport.
6. **Scroll real.** Rueda, click y arrastre de la barra deben conservar el comportamiento vanilla; no puede cambiar la posición del hitbox respecto a la barra visual.
7. **Fallback de scrollbar.** Con interfaces modificadas por otros mods, un fallo de reflection debe dejar la barra vanilla utilizable en vez de romper la pantalla.
8. **Accesibilidad.** Primera y última fila, scrollbar y `Cerrar expediente` deben permanecer separados, especialmente en GUI Scale 3/4.
9. **Botones ocultos.** No debe existir un `Done` vanilla invisible capturando click, foco, Tab o Enter detrás de un botón Jobs.
10. **Hub de Opciones.** En ventanas pequeñas no deben cruzarse la última fila, FOV y `Cerrar expediente`. Si no hay espacio, el FOV duplicado puede omitirse.
11. **Pie de formulario.** Formulario/Nivel, versión y botón central deben permanecer legibles sin solaparse.
12. **Familia completa de interfaces.** Abrir Opciones, Sonido, Video, Controles, Mouse, Teclas, Idioma, Chat, Accesibilidad, Online, Resource Packs, Piel y ajustes Jobs desde el menú principal y volver por Escape/botón.
13. **Pausa → Opciones.** Desde un mundo, abrir pausa y entrar a Condiciones de estancia. Debe aparecer directamente el hub Jobs y Volver debe regresar a la pausa, no al título.
14. **Multijugador.** Selección, ping, MOTD, LAN, entrar, conexión directa, añadir, editar, borrar, refrescar y cancelar.
15. **Video con Embeddium.** Con Embeddium presente debe abrir su propia pantalla, no una copia incompleta.
16. **Idioma.** Cambiar ES ↔ EN, aplicar, esperar la recarga y comprobar que música/ambiente se recuperan sin duplicarse.
17. **Controles.** Agacharse/Correr deben mostrar Mantener/Alternar según su valor real.
18. **Resoluciones y GUI Scale.** 854×480, 1280×720, ventana estrecha, poca altura y GUI Scale extremos.
19. **Teclado y narración.** Tab, Shift+Tab, flechas, Enter, Espacio y Escape en botones, sliders, toggles y listas.
20. **Los 18 niveles.** Recorrer 0–17 y comprobar continuidad visual/audio al abrir/cerrar interfaces.
21. **Movimiento reducido / bajo consumo / destellos reducidos.** Deben seguir afectando escenas procedurales y transiciones donde corresponde sin volver a animar los PNG 10–17.
22. **La Suspensión.** Sesión larga para comprobar apagón, mezcla, rótulo y recuperación, incluyendo una subpantalla abierta durante el evento.
23. **Audio/lifecycle.** Abrir muchas subpantallas, volver, F3+T, Alt+Tab, cambiar idioma y entrar a mundo sin duplicados ni pistas huérfanas.
24. **Despliegue.** Confirmar que sólo queda un `jobsmenu-0.13.0.jar` en `test-1\mods`.

## Riesgos conocidos

- La scrollbar Jobs depende de datos internos de `AbstractSelectionList` de Minecraft 1.20.1. Se usa reflection defensiva y fallback, pero una lista profundamente sustituida por otro mod puede conservar el aspecto vanilla.
- Las envolturas de Sonido/Video/Chat/Accesibilidad/Mouse/Teclas/Online dependen de estructuras vanilla de 1.20.1. Un mod que reemplace por completo una de esas clases puede requerir compatibilidad específica.
- La pantalla de Embeddium se respeta como implementación externa. Su estética interna no se fuerza a Jobs porque hacerlo por reflection profunda sería más frágil que útil.
- Los diálogos secundarios de multijugador que siguen siendo vanilla pueden recibir sólo la banda contextual Jobs.
- Resource packs que sustituyan fuentes por métricas extremas pueden forzar elipsis o alterar el balance de los formularios.
- Los fondos suministrados son recursos rasterizados; en resoluciones muy altas el límite visual es la fuente original, no el renderer.
- No existe un profiler GPU automático en el repositorio. `bajo_consumo` reduce capas de los niveles procedurales, pero el coste final depende de GPU, GUI Scale y resolución.
- `MusicaPropia` sigue esperando OGG Vorbis compatible con Minecraft.
- `dev-latest` es una release rodante: su asset lleva versión, pero el tag no representa una versión histórica inmutable.

## Mitigaciones vigentes

- PNG 10–17 aislados de las capas animadas globales además de tener `PlantaImagen` estática.
- Sustitución por clase exacta en los puntos de entrada importantes para no pisar subclases de otros mods.
- Listas vanilla conservan su lógica; Jobs cambia presentación y scrollbar con fallback seguro.
- Botones vanilla sustituidos se desactivan además de ocultarse.
- Footer con centro reservado para navegación.
- Pantallas externas pueden conservarse y recibir sólo contexto visual mínimo.
- PNG validados en CI y nuevamente por `NativeImage` en runtime.
- Fallback procedural si un fondo no puede decodificarse.
- JAR siempre versionado y política comprobada por CI.
- Configuración de movimiento/destellos/bajo consumo separada.
- Audio por visita, no por instancia de pantalla.
- Guardado diferido de sliders/configuración.
- El deploy descarga y valida el reemplazo antes de borrar una versión instalada.

## Regla de reporte

Un problema observado en Minecraft debe registrarse con:

- versión exacta del JAR;
- pantalla y Nivel donde ocurrió;
- ruta usada para llegar a la pantalla (título, pausa, Mods→Config, subpantalla);
- resolución y GUI Scale;
- mods que sustituyan UI/video, especialmente Embeddium;
- opciones de accesibilidad/bajo consumo activas;
- si ocurrió durante transición/Suspensión;
- `latest.log` cuando sea un problema de recurso, audio o crash.

No se considera corregido un fallo visual sólo porque el proyecto compile.
