# Riesgos y pruebas pendientes — 0.41.1

Este archivo contiene sólo riesgos vigentes. El historial vive en `CHANGELOG.md` y en las auditorías de `docs/`.

## Certificado automáticamente

Antes de publicar, GitHub Actions comprueba:

- Java 17 + Forge build 1.20.1 y JAR versionado;
- integridad de fondos e idiomas;
- `PantallaOpcionesJobs` hereda de `OptionsScreen` y conserva el botón natural de Gráficos;
- Jobs no usa `CompatGraficos`, `ConfigScreenFactory`, reflection ni construcción directa de Video Settings;
- las GUI de vídeo conocidas quedan aisladas de chrome/transiciones/click Jobs;
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

1. con el modpack real, Gráficos abre exactamente la misma GUI/opciones que abriría el OptionsScreen normal;
2. opciones, pestañas o inyecciones añadidas por Embeddium/Oculus/otros mods siguen presentes;
3. sin mods gráficos, la ruta natural sigue terminando en Video Settings vanilla;
4. ESC/Done desde Gráficos vuelve a Opciones Jobs y la GUI externa no recibe decoración/click Jobs;
5. no reaparece fondo/título vanilla dentro de Opciones Jobs;
6. no existen botones/hitboxes invisibles debajo del panel Jobs;
7. las tres pistas y créditos son correctos, sin música vanilla;
8. eventos/apagones/FX puntuales se cortan al entrar a mundo/servidor;
9. idioma → F3+T → resource pack no duplica música, camas ni FX;
10. F5 y resize conservan selección+scroll Multiplayer;
11. LAN, ping, MOTD y favicons siguen funcionando;
12. servidor oficial sigue primero/único/protegido y `Ghoul Outbreak` no vuelve;
13. sliders/toggles persisten tras reinicio;
14. chat/inventario/contenedores siguen fuera de Jobs;
15. PNG 10–17 siguen estáticos y JPG 18–31 conservan movimiento mínimo;
16. GUI Scale 2/3/4 no provoca solapes.

## Riesgos vigentes

### Gráficos / modpack

- Jobs identifica el control natural mediante el texto localizado `options.video`. Un mod que elimine ese control y lo sustituya por una ruta totalmente distinta, sin conservar ese botón, puede requerir compatibilidad específica.
- El botón se vuelve a capturar después de `init()` y justo antes de pulsarlo para recoger sustituciones tardías. Integraciones que muten la pantalla de forma dinámica durante cada frame deberán probarse en el modpack real.
- `EscuchaCliente` mantiene exclusiones por paquetes conocidos para no decorar GUI gráficas externas; proveedores con clases completamente distintas pueden necesitar una exclusión adicional.

### Audio

- Mods que sustituyan por completo `SoundEngine`, `SoundManager` o el pipeline `PlaySoundEvent` pueden requerir compatibilidad específica.
- Durante una visita Jobs se bloquea `SoundSource.MUSIC` para que la banda sonora del menú sea exclusiva.
- Los FX puntuales se purgan usando `SoundManager.isActive`; la percepción final del corte debe comprobarse con audio real.

### Config

- El guard de valores idénticos evita trabajo inútil, pero los cambios reales siguen dependiendo de la persistencia de Forge y deben probarse tras reiniciar Minecraft.
- Los sliders conservan guardado diferido; cerrar/cambiar Screen fuerza `guardarPendiente()`.

### Multiplayer

- El scroll se restaura después de reconstruir la lista; listas alteradas por otro mod pueden cambiar su rango máximo y Minecraft puede ajustarlo.
- Otro mod que reemplace completamente `JoinMultiplayerScreen` puede requerir compatibilidad.
- Entradas LAN siguen siendo efímeras y se recrean con el detector nuevo.

### Interfaces y fondos

- Resource packs de GUI agresivos pueden requerir ajustes.
- No existe profiler GPU automático; rendimiento perceptivo se mide en el modpack real.

## Mitigaciones

- `tools/verificar_graficos_041.py`: flujo gráfico natural, ausencia de puente directo y aislamiento de GUI externa.
- `tools/verificar_runtime_041.py`: runtime/audio/config/Multiplayer 0.41.
- `tools/verificar_audio_identidad.py`: identidad musical/hard-stop 0.40.
- `tools/verificar_reload_creditos.py`: créditos/reload 0.39.
- `tools/verificar_optimizacion.py`: caminos calientes 0.38.
- `tools/verificar_ui_musica.py` y `tools/verificar_continuidad.py`: UI/audio/navegación.
- `dev-latest` sólo se publica después de build Forge real.

## Reporte útil

Ante un fallo, guardar versión/JAR, SHA-256, `latest.log`, pista, pantalla/nivel, resolución, GUI Scale, secuencia de reload, posición/selección Multiplayer, mods gráficos cargados, qué GUI abrió Gráficos y qué opciones faltaron.
