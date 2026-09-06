# Riesgos y pruebas pendientes — 0.42.0

Este archivo contiene sólo riesgos vigentes. El historial vive en `CHANGELOG.md` y en las auditorías de `docs/`.

## Certificado automáticamente

Antes de publicar, GitHub Actions comprueba:

- Java 17 + Forge build 1.20.1 y JAR versionado;
- integridad de fondos e idiomas;
- `PantallaOpcionesJobs` hereda de `OptionsScreen` y delega Gráficos al control natural;
- la ranura original de vídeo puede reconocer un control sustituto aunque cambie su texto;
- Jobs no usa `CompatGraficos`, `ConfigScreenFactory`, reflection ni construcción directa de Video Settings;
- las Screens de terceros se aíslan de forma genérica, sin depender de paquetes Embeddium/Sodium/Iris;
- una Screen de terceros no habilita redirecciones administrativas Jobs en sus subflujos;
- `VideoSettingsScreen` vanilla sigue fuera de chrome/transiciones/click Jobs;
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
- el workflow contiene el movimiento explícito de `dev-latest` a `GITHUB_SHA`;
- build y publicación sólo desde `main` verde.

## Lo que CI no puede certificar

En `test-1` comprobar:

1. con el modpack real, Gráficos abre exactamente la misma GUI/opciones que abriría el OptionsScreen normal;
2. opciones, pestañas o inyecciones añadidas por Embeddium/Oculus/otros mods siguen presentes;
3. si un mod cambia el texto del botón gráfico pero conserva su ranura, Jobs sigue abriendo su flujo correcto;
4. sin mods gráficos, la ruta natural sigue terminando en Video Settings vanilla;
5. ESC/Done desde Gráficos vuelve a Opciones Jobs y la GUI externa no recibe decoración/click Jobs;
6. abrir una pantalla de configuración de cualquier otro mod no añade bandas, transiciones ni sonidos Jobs;
7. desde una pantalla externa, abrir un submenú vanilla no provoca que Jobs lo sustituya por una pantalla propia;
8. no reaparece fondo/título vanilla dentro de Opciones Jobs;
9. no existen botones/hitboxes invisibles debajo del panel Jobs;
10. las tres pistas y créditos son correctos, sin música vanilla;
11. eventos/apagones/FX puntuales se cortan al entrar a mundo/servidor;
12. idioma → F3+T → resource pack no duplica música, camas ni FX;
13. F5 y resize conservan selección+scroll Multiplayer;
14. LAN, ping, MOTD y favicons siguen funcionando;
15. servidor oficial sigue primero/único/protegido y `Ghoul Outbreak` no vuelve;
16. sliders/toggles persisten tras reinicio;
17. chat/inventario/contenedores siguen fuera de Jobs;
18. PNG 10–17 siguen estáticos y JPG 18–31 conservan movimiento mínimo;
19. GUI Scale 2/3/4 no provoca solapes.

## Riesgos vigentes

### Gráficos / modpack

- La detección alternativa por ranura usa tolerancias pequeñas de posición/tamaño. Un mod que rediseñe por completo `OptionsScreen`, moviendo el control gráfico a otra zona y cambiando además su etiqueta, puede no ser reconocible sin una API propia del proveedor. Jobs prefiere no inventar una ruta en ese caso.
- Integraciones que sustituyan el botón después del primer render y justo entre sincronización/click son casos extremos que deben probarse en el modpack real.
- Las pantallas de terceros se detectan por namespace de clase. Un mod que inyecte comportamiento dentro de una `Screen` cuyo tipo siga siendo `net.minecraft.*` conserva el tratamiento Minecraft de esa Screen; esto es intencional para no romper mixins vanilla.

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

### Pipeline

- CI verifica que el workflow contenga el movimiento explícito del tag. La comprobación definitiva sigue siendo posterior a publicar: `refs/tags/dev-latest` debe resolver exactamente al SHA de `main` que generó el JAR.

### Interfaces y fondos

- Resource packs de GUI agresivos pueden requerir ajustes.
- No existe profiler GPU automático; rendimiento perceptivo se mide en el modpack real.

## Mitigaciones

- `tools/verificar_compatibilidad_042.py`: flujo gráfico natural, aislamiento genérico de terceros y ausencia de dependencias por proveedor.
- `tools/verificar_runtime_041.py`: runtime/audio/config/Multiplayer 0.41.
- `tools/verificar_audio_identidad.py`: identidad musical/hard-stop 0.40.
- `tools/verificar_reload_creditos.py`: créditos/reload 0.39.
- `tools/verificar_optimizacion.py`: caminos calientes 0.38.
- `tools/verificar_ui_musica.py` y `tools/verificar_continuidad.py`: UI/audio/navegación.
- `tools/verificar_version.py`: JAR versionado, limpieza de release y movimiento obligatorio de `dev-latest`.
- `dev-latest` sólo se publica después de build Forge real.

## Reporte útil

Ante un fallo, guardar versión/JAR, SHA-256, `latest.log`, pista, pantalla/nivel, resolución, GUI Scale, secuencia de reload, posición/selección Multiplayer, nombre del mod que abrió la Screen externa, qué GUI abrió Gráficos y qué opciones faltaron.
