# Riesgos y pruebas pendientes — 0.20.0

Este documento contiene únicamente riesgos vigentes. El historial vive en `CHANGELOG.md` y en auditorías de `docs/`.

## Estado certificado automáticamente

Antes de publicar una entrega, GitHub Actions comprueba:

- Java 17;
- nombre/versionado obligatorio del JAR;
- integridad PNG/CRC/IDAT de fondos 10–17;
- recursos, idiomas, ASCII Java y coherencia estática;
- separación de paleta entre escena e interfaz en componentes compartidos;
- contratos del reproductor musical y hard stop de gameplay;
- build Forge 1.20.1;
- creación de `jobsmenu-0.20.0.jar`;
- publicación en `dev-latest` únicamente desde `main`.

Un build que no termina en verde no debe actualizar la release.

## Importante: qué queda sin certificar

CI **no ejecuta Minecraft con una ventana real**. Por lo tanto, las modificaciones visuales más recientes se consideran compiladas/certificadas, pero necesitan prueba manual en `test-1` para confirmar estética, hitboxes, scrolling y convivencia con otros mods.

Las pantallas prioritarias de 0.20.0 son:

1. Mods / Forge.
2. Resource Packs.
3. Idioma.
4. Sonido.
5. Video vanilla y Embeddium.
6. Pausa.
7. Mundos.
8. Multiplayer.

El procedimiento completo está en `docs/checklist-manual.md`.

## Riesgos conocidos

### Interfaces y listas

- `PielVanillaJobs` es una capa visual posterior al render. Un mod/resource pack que cambie radicalmente geometría u orden puede requerir compatibilidad específica.
- `ListasExpediente` depende de datos internos de `AbstractSelectionList` 1.20.1 para dibujar la scrollbar Jobs. Existe reflection defensiva y fallback.
- Mods, Resource Packs, Mundos y Multiplayer conservan listas reales; una sustitución total por otro mod puede no recibir el mismo acabado Jobs.
- Fuentes con métricas extremas pueden forzar elipsis o alterar layout.
- GUI Scale 4 y ventanas extremadamente pequeñas son el principal caso de estrés para Idioma y paneles compactos.

### Video

- Sin Embeddium se usa `PantallaVideoJobs`.
- Con Embeddium se respeta su pantalla real. El aspecto no será idéntico al panel vanilla Jobs porque no se reconstruye una UI externa por reflection profunda.

### Audio

- La música/ambiente depende de SoundEngine y del lifecycle de la sesión. F3+T, Alt+Tab y cambios rápidos de pantalla deben probarse manualmente para descartar una instancia fantasma perceptible.
- La segunda pista solicitada no está empaquetada mientras no exista el OGG autorizado en `music/menu_nueva.ogg`.

### Fondos

- PNG 10–17 son rasterizados; la calidad máxima depende de la fuente original.
- No se permite animarlos internamente. Cualquier efecto visual nuevo debe mantenerse fuera del PNG y respetar la regla de estático.

### Rendimiento

- No existe profiler GPU automático. Bajo consumo reduce trabajo decorativo, pero el coste final depende de GPU, resolución, GUI Scale, shaders y otros mods.

### Release

- `dev-latest` es rodante. El asset lleva versión, pero el tag no es una release histórica inmutable.

## Mitigaciones vigentes

- PNG 10–17 aislados de capas animadas.
- Paleta UI separada de la escena y verificada en CI.
- Config Jobs conectada directamente a `ConfigTurno`.
- Redirecciones sensibles por clase exacta.
- Pantallas externas no reciben skin indiscriminadamente.
- Botones vanilla duplicados se desactivan además de ocultarse cuando corresponde.
- Scrollbar visual conserva comportamiento real y tiene fallback.
- `NativeImage` valida fondos en runtime y existe fallback procedural.
- Reproductor musical ligado a sesión con watchdog de SoundEngine.
- Hard stop al entrar a gameplay.
- JAR versionado y verificado por CI.
- Movimiento reducido, destellos reducidos y Bajo consumo tienen prioridad.

## Regla de reporte

Un problema observado en Minecraft debe incluir:

- versión exacta del JAR;
- SHA-256 si está disponible;
- pantalla/Nivel;
- ruta usada para llegar;
- resolución y GUI Scale;
- mods que sustituyan UI/video;
- opciones de accesibilidad relevantes;
- si ocurrió durante transición/Suspensión;
- captura para defectos visuales;
- `latest.log` cuando afecte crash, recursos o audio.

Un fallo visual o sonoro no se considera corregido sólo porque el proyecto compile.
