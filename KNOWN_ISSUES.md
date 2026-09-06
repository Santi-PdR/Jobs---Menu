# Riesgos y pruebas pendientes — 0.41.0

Este archivo contiene sólo riesgos vigentes. El historial vive en `CHANGELOG.md` y en las auditorías de `docs/`.

## Certificado automáticamente

Antes de publicar, GitHub Actions comprueba:

- Java 17 + Forge build 1.20.1 y JAR versionado;
- integridad de fondos e idiomas;
- Gráficos consulta el `ConfigScreenFactory` oficial de Embeddium y conserva fallback vanilla;
- las GUI de Sodium/Embeddium quedan aisladas del chrome/transiciones/click Jobs;
- frontera dura de gameplay;
- continuidad Multiplayer, selección por IP y scroll en F5 **y resize**;
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

1. con Embeddium instalado, Gráficos abre su GUI real y no la vanilla;
2. sin Embeddium, Gráficos cae correctamente a `VideoSettingsScreen`;
3. ESC/Done desde Gráficos vuelve a Opciones Jobs y la GUI externa no recibe decoración/click Jobs;
4. las tres pistas y créditos correctos, sin música vanilla;
5. eventos/apagones/FX puntuales se cortan al entrar a mundo/servidor;
6. no aparece sonido de cueva vanilla si un FX Jobs falla;
7. idioma → F3+T → resource pack no duplica música, camas ni FX;
8. Main → Options → Mods → Recursos mantiene una sola visita;
9. gameplay no acumula cierres/lag por lifecycle de audio;
10. F5 conserva servidor seleccionado y **misma zona aproximada de scroll**;
11. maximizar/redimensionar/cambiar GUI Scale conserva selección+scroll Multiplayer;
12. abrir/refrescar Multiplayer mantiene LAN, ping, MOTD y favicons;
13. servidor oficial sigue primero/único/protegido y `Ghoul Outbreak` no vuelve;
14. sliders/toggles persisten correctamente tras reinicio;
15. chat/inventario/contenedores siguen fuera de Jobs;
16. PNG 10–17 siguen estáticos y JPG 18–31 conservan movimiento mínimo;
17. GUI Scale 2/3/4 no provoca solapes.

## Riesgos vigentes

### Gráficos / Embeddium

- Jobs usa el extension point oficial de Forge que Embeddium 1.20.1 registra. Si una versión futura de Embeddium deja de registrar ese factory, Jobs cae a vanilla en lugar de romper el menú; esa compatibilidad deberá revalidarse.
- Mods que reemplacen la pantalla gráfica sin usar Embeddium o con paquetes completamente distintos pueden requerir una exclusión adicional para evitar overlays Jobs.
- Oculus/Iris se excluyen por sus paquetes gráficos conocidos; su flujo final debe comprobarse en el modpack real porque pueden cambiar clases entre versiones.

### Audio

- Mods que sustituyan por completo `SoundEngine`, `SoundManager` o el pipeline `PlaySoundEvent` pueden requerir compatibilidad específica.
- Durante una visita Jobs se bloquea `SoundSource.MUSIC` para que la banda sonora del menú sea exclusiva. Un mod que pretenda reproducir su propia música de menú bajo esa categoría quedará silenciado mientras Jobs esté activo; es intencional.
- Los FX puntuales se purgan usando `SoundManager.isActive`; la percepción final del corte debe comprobarse con audio real.

### Config

- El guard de valores idénticos evita trabajo inútil, pero los cambios reales siguen dependiendo del sistema de persistencia de Forge y deben probarse tras reiniciar Minecraft.
- Los sliders conservan guardado diferido; cerrar/cambiar Screen fuerza `guardarPendiente()`.

### Multiplayer

- El scroll se restaura después de reconstruir la lista; listas alteradas por otro mod pueden cambiar su rango máximo y Minecraft puede ajustarlo.
- `resize()` conserva contexto de la lista propia, pero otro mod que reemplace completamente `JoinMultiplayerScreen` puede requerir compatibilidad.
- Entradas LAN siguen siendo efímeras y se recrean con el detector nuevo.

### Interfaces y fondos

- Resource packs de GUI agresivos pueden requerir ajustes.
- No existe profiler GPU automático; rendimiento perceptivo se mide en el modpack real.

## Mitigaciones

- `tools/verificar_graficos_041.py`: factory Embeddium, fallback vanilla y aislamiento de GUI gráfica.
- `tools/verificar_runtime_041.py`: runtime/audio/config/Multiplayer 0.41.
- `tools/verificar_audio_identidad.py`: identidad musical/hard-stop 0.40.
- `tools/verificar_reload_creditos.py`: créditos/reload 0.39.
- `tools/verificar_optimizacion.py`: caminos calientes 0.38.
- `tools/verificar_ui_musica.py` y `tools/verificar_continuidad.py`: UI/audio/navegación.
- `dev-latest` sólo se publica después de build Forge real.

## Reporte útil

Ante un fallo, guardar versión/JAR, SHA-256, `latest.log`, pista, pantalla/nivel, resolución, GUI Scale, secuencia de reload, posición/selección Multiplayer, si Embeddium/Oculus estaban cargados, qué GUI abrió Gráficos y mods de UI/vídeo/audio relevantes.
