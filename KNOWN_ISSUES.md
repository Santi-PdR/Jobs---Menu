# Riesgos y pruebas pendientes — 0.12.0

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

CI puede certificar archivos y compilación, pero no reemplaza Minecraft real. Antes de considerar 0.12.0 completamente validada hay que probar en `test-1`:

1. **Familia completa de interfaces.** Abrir Opciones, Sonido, Video, Controles, Mouse, Teclas, Idioma, Chat, Accesibilidad, Online, Resource Packs, Piel y ajustes Jobs desde el menú principal y volver por Escape/botón.
2. **Pausa → Opciones.** Desde un mundo, abrir pausa y entrar a Condiciones de estancia. Debe aparecer directamente el hub Jobs y Volver debe regresar a la pausa, no al título.
3. **Multijugador.** Selección, ping, MOTD, LAN, entrar, conexión directa, añadir, editar, borrar, refrescar y cancelar. Los botones Jobs deben reflejar correctamente cuándo las acciones vanilla están activas.
4. **Video con Embeddium.** Con Embeddium presente debe abrir su propia pantalla, no una copia incompleta. Jobs sólo debe aportar contexto sin bloquear controles.
5. **Listas largas.** Sonido, teclas, chat, accesibilidad e idioma deben hacer scroll completo sin que cabecera, pie o botón Volver cubran filas.
6. **Idioma.** Cambiar ES ↔ EN, aplicar, esperar la recarga de recursos y comprobar que música/ambiente se recuperan sin duplicarse.
7. **Accesibilidad integrada.** Movimiento reducido, destellos reducidos, alto contraste y texto grande deben aparecer junto a las opciones de accesibilidad vanilla y guardar correctamente.
8. **Controles.** Agacharse/Correr deben mostrar Mantener/Alternar según su valor real; Autojump y pestaña de operador deben seguir usando Activado/Desactivado.
9. **Resoluciones y GUI Scale.** 854×480, 1280×720, ventana estrecha, poca altura y GUI Scale extremos. Revisar especialmente el modo compacto del hub de opciones y el pie de formulario.
10. **Teclado y narración.** Tab, Shift+Tab, flechas, Enter, Espacio y Escape en botones, sliders, toggles y listas. No debe existir un botón vanilla invisible que siga capturando foco/click.
11. **Los 18 niveles.** Recorrer 0–17 y comprobar que abrir/cerrar interfaces no corta la continuidad visual/audio del Nivel vigente.
12. **Fondos 10–17.** Confirmar que ninguno muestra textura morado/negro y que zoom/paneo/efectos no enseñan bordes.
13. **Movimiento reducido.** Debe simplificar también las transiciones entre expedientes sin romper navegación.
14. **Bajo consumo.** Debe mantener identidad y legibilidad quitando las capas más caras.
15. **Destellos reducidos.** No debe introducir flashes nuevos durante transición, ronda, Suspensión ni navegación UI.
16. **La Suspensión.** Sesión larga para comprobar apagón, mezcla, rótulo y recuperación, incluyendo una subpantalla abierta durante el evento.
17. **Audio/lifecycle.** Abrir muchas subpantallas, volver, F3+T, Alt+Tab, cambiar idioma y entrar a mundo sin duplicados ni pistas huérfanas.
18. **Despliegue.** Confirmar que sólo queda un `jobsmenu-0.12.0.jar` en `test-1\mods`.

## Riesgos conocidos

- Las envolturas de Sonido/Video/Chat/Accesibilidad/Mouse/Teclas/Online dependen de estructuras vanilla de 1.20.1. Se conserva fallback defensivo para reflection, pero un mod que reemplace por completo una de esas clases puede requerir compatibilidad específica.
- La pantalla de Embeddium se respeta como implementación externa. Su estética interna no se fuerza a Jobs porque hacerlo por reflection profunda sería más frágil que útil.
- Los diálogos secundarios de multijugador que siguen siendo vanilla pueden recibir sólo la banda contextual Jobs. La lista principal sí tiene tratamiento completo.
- Resource packs que sustituyan fuentes por métricas extremas pueden forzar elipsis o alterar el balance de los formularios aunque las cadenas ES/EN estén verificadas.
- Los fondos suministrados son recursos rasterizados; en resoluciones muy altas el límite visual es la fuente original, no el renderer.
- No existe un profiler GPU automático en el repositorio. `bajo_consumo` reduce capas, pero el coste final depende de GPU, GUI Scale y resolución.
- `MusicaPropia` sigue esperando OGG Vorbis compatible con Minecraft. Un archivo renombrado o codificado con otro formato debe ser rechazado y explicado por log.
- `dev-latest` es una release rodante: su asset lleva versión, pero el tag no representa una versión histórica inmutable.

## Mitigaciones vigentes

- Sustitución por clase exacta en los puntos de entrada importantes para no pisar subclases de otros mods.
- Listas vanilla conservan ancho y lógica; Jobs ajusta sólo presentación y banda vertical cuando es seguro.
- Botones vanilla sustituidos se desactivan además de ocultarse.
- Pantallas externas pueden conservarse y recibir sólo contexto visual mínimo.
- PNG de imagen validados en CI y nuevamente por `NativeImage` en runtime.
- Fallback procedural si un fondo no puede decodificarse.
- JAR siempre versionado y política comprobada por CI.
- Configuración de movimiento/destellos/bajo consumo separada.
- Audio por visita, no por instancia de pantalla.
- Guardado diferido de sliders/configuración.
- Transiciones visuales y sonoras basadas en el mismo snapshot temporal.
- El deploy descarga y valida el reemplazo antes de borrar una versión instalada.

## Regla de reporte

Un problema observado en Minecraft debe registrarse con:

- versión exacta del JAR;
- pantalla y Nivel donde ocurrió;
- ruta usada para llegar a la pantalla (título, pausa, subpantalla);
- resolución y GUI Scale;
- mods que sustituyan UI/video, especialmente Embeddium;
- opciones de accesibilidad/bajo consumo activas;
- si ocurrió durante transición/Suspensión;
- `latest.log` cuando sea un problema de recurso, audio o crash.

No se considera corregido un fallo visual sólo porque el proyecto compile.
