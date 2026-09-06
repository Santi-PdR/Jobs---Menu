# Riesgos y pruebas pendientes — 0.40.0

Este archivo contiene sólo riesgos vigentes. El historial vive en `CHANGELOG.md` y en las auditorías de `docs/`.

## Certificado automáticamente

Antes de publicar, GitHub Actions comprueba:

- Java 17 y Forge build 1.20.1;
- JAR versionado y política de `dev-latest`;
- integridad de PNG 10–17 y JPEG 18–31;
- paridad ES/EN, recursos y coherencia estática;
- Video Settings aislado de capas Jobs;
- frontera dura de gameplay;
- salida/refresh de Multiplayer Jobs y selección por IP;
- contratos de optimización 0.38;
- créditos y generaciones de resource reload 0.39;
- catálogo musical estático de 0.40;
- ausencia de `SoundEvents.MUSIC_MENU`/fallback vanilla dentro de `GestorMusica`;
- hard-stop directo al `SoundManager`;
- build y publicación sólo desde `main` verde.

## Lo que CI no puede certificar

En `test-1` hay que comprobar:

1. que las tres pistas siguen reproduciéndose y acreditándose correctamente;
2. que ninguna falla/reload termina reproduciendo música de menú vanilla;
3. que una secuencia rápida idioma → F3+T → resource pack no duplica música ni ambiente;
4. que entrar a gameplay durante reproducción/crossfade corta el audio Jobs inmediatamente;
5. que volver al menú inicia/reanuda una visita limpia sin dos instancias;
6. que Main → Options → Mods → Recursos → volver conserva una sola visita;
7. que ESC/Cancelar/F5 de Multiplayer siguen funcionando con una sola acción;
8. que LAN, ping, favicons y MOTD sobreviven a varias recargas F5;
9. que Video Settings mantiene todas las opciones del juego/mod de vídeo;
10. que chat, inventario y contenedores no reciben transiciones ni skin Jobs;
11. que los PNG 10–17 siguen completamente estáticos;
12. que la respiración 18–31 es sutil y se congela con Movimiento reducido/Bajo consumo;
13. que GUI Scale 2/3/4 no provoca solapes;
14. que no hay audio Jobs huérfano al cerrar el juego o desconectarse.

## Riesgos vigentes

### Audio y resource reload

- Si otro mod sustituye por completo `SoundEngine` o intercepta `SoundManager.stop`, la compatibilidad final sigue necesitando prueba real.
- Una pista Jobs cuyo SoundEvent no esté registrado se omite y reintenta; no hay fallback vanilla. El log avisa una vez por visita/reload para evitar spam.
- El `RegistryObject` puede existir aunque un resource pack rompa el archivo de sonido físico; ese caso sólo puede validarse dentro del motor real.
- La mezcla de las tres pistas y ambientes sigue siendo una prueba perceptiva manual.

### Créditos

- `musica_creditada.txt` es una compuerta interna. Si desaparece, `GestorMusica` oculta créditos por diseño.
- El texto del crédito se toma del catálogo Java y no de metadata del OGG; cualquier cambio de archivo debe actualizar código/documentación juntos.

### Multiplayer

- F5 reconstruye `JoinMultiplayerScreen` Jobs para refrescar detector LAN/pinger. La selección online se conserva por IP, no reutilizando Entries viejas.
- Entradas LAN son efímeras y dependen del nuevo detector.
- Mods que sustituyan totalmente `JoinMultiplayerScreen` pueden necesitar compatibilidad específica.

### Interfaces y fondos

- Resource packs de GUI agresivos pueden requerir ajustes visuales.
- Las scrollbars Jobs son presentación; wheel/click/drag pertenecen a la lista real.
- JPG 18–31 usan cover y pueden recortar bordes en relaciones no 16:9.
- No existe profiler GPU automático; rendimiento perceptivo se mide en el modpack real.

## Mitigaciones

- `tools/verificar_audio_identidad.py` protege identidad musical, catálogo estable y hard-stop 0.40.
- `tools/verificar_reload_creditos.py` protege créditos/reload 0.39.
- `tools/verificar_optimizacion.py` protege caminos calientes 0.38.
- `tools/verificar_ui_musica.py` protege catálogo, sesión y UI/audio.
- `tools/verificar_continuidad.py` protege Multiplayer y documentación vigente.
- `dev-latest` sólo se publica después del build real de Forge.

## Reporte útil

Ante un fallo, guardar versión/JAR, SHA-256, `latest.log`, pista, pantalla/nivel, resolución, GUI Scale, secuencia de reload y mods de UI/vídeo/audio relevantes.
