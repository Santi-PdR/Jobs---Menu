# Riesgos y pruebas pendientes — 0.25.0

Este documento contiene únicamente riesgos vigentes. El historial vive en `CHANGELOG.md` y en auditorías de `docs/`.

## Estado certificado automáticamente

Antes de publicar una entrega, GitHub Actions comprueba:

- Java 17;
- nombre/versionado obligatorio del JAR;
- integridad PNG/CRC/IDAT de fondos 10–17;
- recursos, idiomas, ASCII Java y coherencia estática;
- separación de paleta entre escena e interfaz;
- contratos del reproductor musical y hard stop de gameplay;
- build Forge 1.20.1;
- creación de `jobsmenu-0.25.0.jar`;
- publicación en `dev-latest` únicamente desde `main`.

Un build que no termina en verde no debe actualizar la release.

## Importante: qué queda sin certificar

CI **no ejecuta Minecraft con una ventana real**. Las modificaciones pueden quedar compiladas y verificadas sin que eso confirme estética, hitboxes, scrolling, sensación de input o convivencia visual con otros mods.

Prioridad manual de 0.25.0:

1. Las tres pistas — Absurdism, REQUIEM y Upon the Hill V2 — deben poder sonar en visitas distintas.
2. `N` debe hacer crossfade a otra pista y no apilar cambios si se pulsa durante un crossfade.
3. El crédito y `TRK` del HUD deben seguir a la pista dominante correcta.
4. `M`, F3+T, Alt+Tab y navegación por subpantallas no deben duplicar instancias.
5. Entrar a mundo/servidor debe cortar inmediatamente música y ambiente Jobs.
6. Main con HUD ampliado, `NXT`, sesión, volumen, MUTE y pista actual.
7. Atajos 1–4 del main y 1–2 de pausa; EditBox no debe dispararlos al escribir.
8. Breadcrumb, KEY/PTR, controles vanilla/Forge y scrollbars nuevas.
9. GUI Scale 2/3/4, ultrawide, Movimiento reducido, Bajo consumo, Alto contraste e Interfaz mínima.
10. PNG 10–17 durante navegación y traslados, sin movimiento interno.

El procedimiento completo está en `docs/checklist-manual.md`.

## Riesgos conocidos

### Interfaces y listas

- `PielVanillaJobs` y `CapaProfesionalJobs` son capas posteriores al render. Un mod/resource pack que cambie radicalmente geometría u orden puede requerir compatibilidad específica.
- La barra contextual y breadcrumb se ocultan o reducen según espacio; su equilibrio final requiere prueba visual, especialmente en GUI Scale 4 y ultrawide.
- El estado `KEY/PTR` es informativo y depende del foco/hover que Minecraft expone en ese frame.
- `ListasExpediente` depende de datos internos de `AbstractSelectionList` 1.20.1 para dibujar la scrollbar Jobs. Existe reflection defensiva y fallback.
- La nueva scrollbar es visual: rueda, click y drag siguen perteneciendo a la lista real. Cualquier desalineación debe reportarse con captura y GUI Scale.
- Fuentes con métricas extremas pueden forzar más elipsis de las previstas.

### Atajos de teclado

- `AtajosInterfazJobs` actúa sólo en `PantallaNivel` y `PantallaEstancia`, ignora EditBox enfocado y exige cero modificadores.
- Otro mod que intercepte `ScreenEvent.KeyPressed.Pre` antes o después puede alterar la convivencia de un atajo; por eso 1–4 se limita a las pantallas propias y no se registra como keymapping global.
- La salida de la pausa no tiene atajo numérico intencionalmente. El main conserva la confirmación existente del renglón 04 antes de cerrar Minecraft.

### Video

- Sin Embeddium se usa `PantallaVideoJobs`.
- Con Embeddium se respeta su pantalla real y no se reconstruye por reflection profunda.

### Audio

- La música/ambiente depende de SoundEngine y del lifecycle de la sesión. F3+T, Alt+Tab y cambios rápidos de pantalla deben probarse manualmente para descartar una instancia fantasma perceptible.
- La instrumentación de sesión sólo muestra datos locales temporales; no cambia ni reinicia el audio.
- El catálogo de tres pistas ya está empaquetado. Queda pendiente validar dentro de Minecraft la mezcla percibida, diferencias de loudness y crossfade entre fuentes de distinta frecuencia.

### Fondos

- PNG 10–17 son rasterizados; la calidad máxima depende de la fuente original.
- El filtrado lineal reduce pixelado al escalar, pero no inventa detalle ausente.
- No se permite mover, deformar ni animar internamente el PNG.
- Sí se permiten fades, apagones, transición de expediente y overlays globales que no cambien geometría ni contenido de la imagen.
- Si se agregan 18–19 como PNG, heredan el mismo contrato.

### Rendimiento

- No existe profiler GPU automático. Bajo consumo reduce trabajo decorativo, pero el coste final depende de GPU, resolución, GUI Scale, shaders y otros mods.
- Breadcrumb, telemetría local y códigos de control son texto/rectángulos 2D; aun así, el resultado final debe probarse en el equipo real.
- Movimiento reducido y Bajo consumo sustituyen actividad continua por marcas estáticas.

### Release

- `dev-latest` es rodante. El asset lleva versión, pero el tag no es una release histórica inmutable.

## Mitigaciones vigentes

- PNG 10–17 aislados de movimiento interno y efectos procedurales.
- Paleta UI separada de la escena y verificada en CI.
- `CapaProfesionalJobs` no captura input.
- Atajos numéricos protegidos frente a EditBox y modificadores.
- Config Jobs conectada directamente a `ConfigTurno`.
- Redirecciones sensibles por clase exacta.
- Pantallas externas no reciben skin indiscriminadamente.
- Scrollbar visual conserva comportamiento real y tiene fallback.
- `NativeImage` valida fondos en runtime y existe fallback procedural.
- Reproductor musical ligado a sesión con watchdog de SoundEngine, tres pistas independientes y selección sin repetición inmediata.
- Hard stop al entrar a gameplay.
- JAR versionado y verificado por CI.
- Movimiento reducido, destellos reducidos y Bajo consumo tienen prioridad.

## Regla de reporte

Un problema observado en Minecraft debe incluir versión exacta del JAR, SHA-256 si está disponible, pantalla/Nivel, ruta usada, resolución/GUI Scale, mods que sustituyan UI/video, opciones de accesibilidad relevantes, si ocurrió durante transición/Suspensión y captura. Adjuntar `latest.log` cuando afecte crash, recursos o audio.

Un fallo visual o sonoro no se considera corregido sólo porque el proyecto compile.
