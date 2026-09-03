# Riesgos y pruebas pendientes — 0.23.0

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
- creación de `jobsmenu-0.23.0.jar`;
- publicación en `dev-latest` únicamente desde `main`.

Un build que no termina en verde no debe actualizar la release.

## Importante: qué queda sin certificar

CI **no ejecuta Minecraft con una ventana real**. Por lo tanto, las modificaciones visuales se consideran compiladas/certificadas, pero necesitan prueba manual en `test-1` para confirmar estética, hitboxes, scrolling y convivencia con otros mods.

Las pantallas prioritarias de 0.23.0 son:

1. Main screen con HUD de turno y progreso.
2. PNG 10–17 durante fades, overlays y cambios de Nivel.
3. Pausa en singleplayer y multiplayer.
4. Options y Config Jobs con la nueva capa profesional.
5. Mods / Forge y Resource Packs para comprobar que rails y foco no pisan listas.
6. Idioma y buscadores.
7. Sonido y Video.
8. Mundos y Multiplayer.
9. Movimiento reducido, Bajo consumo y Alto contraste.

El procedimiento completo está en `docs/checklist-manual.md`.

## Riesgos conocidos

### Interfaces y listas

- `PielVanillaJobs` y `CapaProfesionalJobs` son capas visuales posteriores al render. Un mod/resource pack que cambie radicalmente geometría u orden puede requerir compatibilidad específica.
- La nueva capa no captura input ni modifica hitboxes, pero su posición final debe revisarse en GUI Scale 4, ultrawide y ventanas extremadamente pequeñas.
- `ListasExpediente` depende de datos internos de `AbstractSelectionList` 1.20.1 para dibujar la scrollbar Jobs. Existe reflection defensiva y fallback.
- Mods, Resource Packs, Mundos y Multiplayer conservan listas reales; una sustitución total por otro mod puede no recibir el mismo acabado Jobs.
- Fuentes con métricas extremas pueden forzar elipsis o alterar layout.
- El HUD contextual del main se oculta automáticamente en viewports pequeños para evitar solapes; su composición final requiere verificación manual en resoluciones ultrawide.

### Video

- Sin Embeddium se usa `PantallaVideoJobs`.
- Con Embeddium se respeta su pantalla real. El aspecto no será idéntico al panel vanilla Jobs porque no se reconstruye una UI externa por reflection profunda.

### Audio

- La música/ambiente depende de SoundEngine y del lifecycle de la sesión. F3+T, Alt+Tab y cambios rápidos de pantalla deben probarse manualmente para descartar una instancia fantasma perceptible.
- La segunda pista solicitada no está empaquetada mientras no exista el OGG autorizado en `music/menu_nueva.ogg`.

### Fondos

- PNG 10–17 son rasterizados; la calidad máxima depende de la fuente original.
- El filtrado lineal reduce pixelado al escalar, pero no inventa detalle ausente en el archivo fuente.
- No se permite mover, deformar ni animar internamente el PNG.
- Sí se permiten fades, apagones, transición de expediente y overlays globales que no cambien geometría ni contenido de la imagen.
- Si se agregan 18–19 como PNG, heredan el mismo contrato.

### Rendimiento

- No existe profiler GPU automático. Bajo consumo reduce trabajo decorativo, pero el coste final depende de GPU, resolución, GUI Scale, shaders y otros mods.
- Barridos globales, pulso de borde y respiración de foco se omiten con Movimiento reducido o Bajo consumo.
- La instrumentación está compuesta por rectángulos 2D simples y texto; aun así, el coste final debe comprobarse en el equipo real del usuario.

### Release

- `dev-latest` es rodante. El asset lleva versión, pero el tag no es una release histórica inmutable.

## Mitigaciones vigentes

- PNG 10–17 aislados de movimiento interno y efectos procedurales.
- Paleta UI separada de la escena y verificada en CI.
- `CapaProfesionalJobs` no captura input.
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
