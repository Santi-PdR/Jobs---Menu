# Riesgos y pruebas pendientes — 0.41.0

Este archivo contiene sólo riesgos vigentes. El historial vive en `CHANGELOG.md` y en las auditorías de `docs/`.

## Certificado automáticamente

Antes de publicar, GitHub Actions comprueba:

- Java 17 + Forge build 1.20.1 y JAR versionado;
- integridad de fondos e idiomas;
- Video Settings aislado y frontera dura de gameplay;
- continuidad Multiplayer, selección por IP y scroll en F5;
- guard de `servers.dat` para no guardar si no hubo cambios;
- optimizaciones históricas 0.38;
- créditos/reload 0.39;
- identidad musical/hard-stop 0.40;
- rastreo/hard-stop de FX puntuales 0.41;
- ausencia de fallback `AMBIENT_CAVE` para FX Jobs;
- cierre de sesión idempotente;
- setters de config que omiten valores idénticos;
- cache de botones vanilla para hover;
- bloqueo de `SoundSource.MUSIC` durante sesión Jobs sin `stopPlaying()` por tick;
- build y publicación sólo desde `main` verde.

## Lo que CI no puede certificar

En `test-1` comprobar:

1. las tres pistas y créditos correctos, sin música vanilla;
2. eventos/apagones/FX puntuales se cortan al entrar a mundo/servidor;
3. no aparece sonido de cueva vanilla si un FX Jobs falla;
4. idioma → F3+T → resource pack no duplica música, camas ni FX;
5. Main → Options → Mods → Recursos mantiene una sola visita;
6. gameplay no acumula cierres/lag por lifecycle de audio;
7. F5 conserva servidor seleccionado y **misma zona aproximada de scroll**;
8. abrir/refrescar Multiplayer mantiene LAN, ping, MOTD y favicons;
9. servidor oficial sigue primero/único/protegido y `Ghoul Outbreak` no vuelve;
10. sliders/toggles persisten correctamente tras reinicio;
11. Video Settings mantiene todas sus opciones vanilla/mod gráfico;
12. chat/inventario/contenedores siguen fuera de Jobs;
13. PNG 10–17 siguen estáticos y JPG 18–31 conservan movimiento mínimo;
14. GUI Scale 2/3/4 no provoca solapes.

## Riesgos vigentes

### Audio

- Mods que sustituyan por completo `SoundEngine`, `SoundManager` o el pipeline `PlaySoundEvent` pueden requerir compatibilidad específica.
- Durante una visita Jobs se bloquea `SoundSource.MUSIC` para que la banda sonora del menú sea exclusiva. Un mod que pretenda reproducir su propia música de menú bajo esa categoría quedará silenciado mientras Jobs esté activo; es intencional.
- Los FX puntuales se purgan usando `SoundManager.isActive`; la percepción final del corte debe comprobarse con audio real.

### Config

- El guard de valores idénticos evita trabajo inútil, pero los cambios reales siguen dependiendo del sistema de persistencia de Forge y deben probarse tras reiniciar Minecraft.
- Los sliders conservan guardado diferido; cerrar/cambiar Screen fuerza `guardarPendiente()`.

### Multiplayer

- El scroll se restaura después de reconstruir la lista; listas alteradas por otro mod pueden cambiar su rango máximo y Minecraft puede ajustarlo.
- Entradas LAN siguen siendo efímeras y se recrean con el detector nuevo.
- Mods que sustituyan completamente `JoinMultiplayerScreen` pueden requerir compatibilidad.

### Interfaces y fondos

- Resource packs de GUI agresivos pueden requerir ajustes.
- No existe profiler GPU automático; rendimiento perceptivo se mide en el modpack real.

## Mitigaciones

- `tools/verificar_runtime_041.py`: runtime/audio/config/Multiplayer 0.41.
- `tools/verificar_audio_identidad.py`: identidad musical/hard-stop 0.40.
- `tools/verificar_reload_creditos.py`: créditos/reload 0.39.
- `tools/verificar_optimizacion.py`: caminos calientes 0.38.
- `tools/verificar_ui_musica.py` y `tools/verificar_continuidad.py`: UI/audio/navegación.
- `dev-latest` sólo se publica después de build Forge real.

## Reporte útil

Ante un fallo, guardar versión/JAR, SHA-256, `latest.log`, pista, pantalla/nivel, resolución, GUI Scale, secuencia de reload, posición/selección Multiplayer y mods de UI/vídeo/audio relevantes.
