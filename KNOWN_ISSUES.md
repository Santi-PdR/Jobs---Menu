# Riesgos y pruebas pendientes — 0.39.0

Este archivo contiene sólo riesgos vigentes. El historial vive en `CHANGELOG.md` y en las auditorías de `docs/`.

## Certificado automáticamente

Antes de publicar, GitHub Actions comprueba:

- Java 17 y Forge build 1.20.1;
- JAR versionado y política de `dev-latest`;
- integridad de PNG 10–17 y JPEG 18–31;
- paridad ES/EN, recursos y coherencia estática;
- Video Settings aislado de capas Jobs;
- frontera dura de gameplay;
- salida/refresh de Multiplayer Jobs;
- selección por IP al usar F5/Actualizar;
- contratos de optimización 0.38;
- presencia del marcador de catálogo musical acreditado;
- ids `absurdism`, `requiem` y `upon_the_hill_v2` dentro del marcador;
- generación atómica de resource reload y reprogramación cuando llega una generación nueva;
- guard de reapertura de `SesionMenu`;
- build y publicación sólo desde `main` verde.

## Lo que CI no puede certificar

CI no abre Minecraft con una ventana real. En `test-1` hay que comprobar:

1. que los tres créditos musicales aparecen durante su ventana de HUD;
2. que REQUIEM muestra `Emmy Z - Forsaken OST` y Upon the Hill V2 `ft. @iCosmicCoffee`;
3. que una secuencia rápida idioma → F3+T → resource pack no duplica música ni ambiente;
4. que después de reload la pista vuelve una sola vez;
5. que entrar a gameplay inmediatamente después de reload mantiene hard-stop total;
6. que Main → Options → Mods → Recursos → volver conserva una sola visita musical;
7. que ESC/Cancelar/F5 de Multiplayer siguen funcionando con una sola acción;
8. que LAN, ping, favicons y MOTD sobreviven a varias recargas F5;
9. que Video Settings mantiene todas las opciones del juego/mod de vídeo;
10. que chat, inventario y contenedores no reciben transiciones ni skin Jobs;
11. que los PNG 10–17 siguen completamente estáticos;
12. que la respiración 18–31 es sutil y se congela con Movimiento reducido/Bajo consumo;
13. que GUI Scale 2/3/4 no provoca solapes;
14. que no hay audio Jobs huérfano al cerrar el juego o desconectarse.

## Riesgos vigentes

### Resource reload

- La generación atómica evita perder una recarga posterior, pero el comportamiento final del `SoundEngine` depende también de otros mods de audio/resource packs.
- El reintento musical posterior al reload sigue siendo temporal y debe probarse con F3+T repetido.
- Un mod que reemplace por completo el sistema de recursos/sonido puede requerir compatibilidad específica.

### Créditos

- `musica_creditada.txt` es una compuerta de autorización interna. Si desaparece, `GestorMusica` vuelve a ocultar créditos por diseño.
- El texto del crédito se toma del catálogo Java y no de metadata del OGG; cualquier cambio de archivo debe actualizar código/documentación juntos.

### Multiplayer

- F5 reconstruye `JoinMultiplayerScreen` Jobs para refrescar detector LAN/pinger. La selección online se conserva por IP, no reutilizando Entries viejas.
- Entradas LAN son efímeras y dependen del nuevo detector.
- Mods que sustituyan totalmente `JoinMultiplayerScreen` pueden necesitar compatibilidad específica.

### Interfaces

- Las pantallas Jobs propias pueden necesitar ajustes con resource packs de GUI muy agresivos.
- Las capas de scrollbar son visuales; wheel/click/drag pertenecen a la lista real.
- Video Settings se deja deliberadamente fuera de Jobs para no perder opciones de Minecraft/Embeddium/Sodium.

### Fondos

- JPG 18–31 son 1920×1080 y usan cover; relaciones de aspecto no 16:9 pueden recortar bordes.
- No existe profiler GPU automático; rendimiento perceptivo debe medirse en el modpack real.

### Audio

- La mezcla de las tres pistas y ambientes debe validarse de oído.
- F3+T, Alt+Tab y cambios rápidos de pantalla siguen siendo casos manuales importantes.
- El fallback musical de emergencia del motor no forma parte de la identidad de gestos de UI; los gestos Jobs no deben volver a `ui.button.click` vanilla.

## Mitigaciones

- `tools/verificar_reload_creditos.py` fija los contratos nuevos de 0.39.
- `tools/verificar_optimizacion.py` protege los caminos calientes de 0.38.
- `tools/verificar_ui_musica.py` protege catálogo, hard-stop y UI/audio.
- `tools/verificar_continuidad.py` protege Multiplayer y documentación vigente.
- el diagnóstico oculto registra pista dominante, capas ambientales y generación de reload.
- `dev-latest` sólo se publica después del build real de Forge.

## Reporte útil

Ante un fallo, guardar versión/JAR, SHA-256, `latest.log`, pantalla/nivel, resolución, GUI Scale, opciones de Movimiento reducido/Bajo consumo y mods de UI/vídeo/audio relevantes.
