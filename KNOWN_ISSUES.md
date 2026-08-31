# Riesgos y pruebas pendientes — 0.11.0

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

CI puede certificar archivos y compilación, pero no reemplaza Minecraft real. Antes de considerar una versión completamente validada hay que probar en `test-1`:

1. **Los 18 niveles.** Recorrer 0–17 y comprobar encuadre, puntos focales, rótulo, hoja y cuenta regresiva.
2. **Fondos 10–17.** Confirmar que ninguno muestra textura morado/negro y que zoom/paneo/efectos no enseñan bordes.
3. **Pulido 0.11.0.** Verificar halo, barrido de exposición y transición física en niveles claros y oscuros.
4. **Movimiento reducido.** Debe congelar el movimiento decorativo sin romper iluminación ni navegación.
5. **Bajo consumo.** Debe mantener identidad y legibilidad quitando las capas más caras.
6. **Destellos reducidos.** No debe introducir flashes nuevos durante transición, ronda o Suspensión.
7. **La Suspensión.** Sesión larga para comprobar apagón, mezcla, rótulo y recuperación.
8. **Audio largo.** Escuchar BASE/CARÁCTER varios minutos; la microderiva tonal debe sentirse como variación natural, nunca como cinta acelerada.
9. **Música y lifecycle.** Abrir Opciones/Mods, volver, F3+T, Alt+Tab y entrar a mundo sin duplicados ni pistas huérfanas.
10. **Resoluciones.** 854×480, 1280×720, ventana estrecha, poca altura y GUI Scale extremos.
11. **Teclado.** Tab, Shift+Tab, flechas, Enter, Espacio, Escape, M y F.
12. **Despliegue.** Confirmar que sólo queda un `jobsmenu-<version>.jar` en `test-1\mods`.

## Riesgos conocidos

- Los fondos suministrados son recursos rasterizados; en resoluciones muy altas el límite visual es la fuente original, no el renderer.
- La integración con mods que sustituyan por completo `TitleScreen`, `OptionsScreen` o el SoundEngine sólo puede confirmarse con el modpack real.
- No existe un profiler GPU automático en el repositorio. `bajo_consumo` reduce capas, pero el coste final depende de GPU, GUI Scale y resolución.
- Los efectos procedurales usan `GuiGraphics`; resource packs que cambien agresivamente tipografía o tamaños pueden alterar el layout aunque las cadenas ES/EN estén verificadas.
- `MusicaPropia` sigue esperando OGG Vorbis compatible con Minecraft. Un archivo renombrado o codificado con otro formato debe ser rechazado y explicado por log.
- `dev-latest` es una release rodante: su asset lleva versión, pero el tag no representa una versión histórica inmutable.

## Mitigaciones vigentes

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
- nivel donde ocurrió;
- resolución y GUI Scale;
- opciones de accesibilidad/bajo consumo activas;
- si ocurrió durante transición/Suspensión;
- `latest.log` cuando sea un problema de recurso, audio o crash.

No se considera corregido un fallo visual sólo porque el proyecto compile.
