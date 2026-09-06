# Riesgos y pruebas pendientes — 0.44.0

Este archivo contiene sólo riesgos vigentes. El historial vive en `CHANGELOG.md` y en las auditorías de `docs/`.

## Certificado automáticamente

Antes de publicar, GitHub Actions comprueba:

- Java 17 + Forge build 1.20.1 y JAR versionado;
- integridad de fondos e idiomas;
- `PantallaOpcionesJobs` vuelve a ser una `Screen` propia y no hereda de `OptionsScreen`;
- no existen `MODPACK`, `abrirOpcionesModpack`, `permitirOptionsNaturalUnaVez`, captura por ranura ni widgets gráficos ocultos;
- con Embeddium, Gráficos usa su `ConfigScreenHandler.ConfigScreenFactory` oficial;
- Jobs no enlaza `SodiumOptionsGUI`, `EmbeddiumVideoOptionsScreen` ni usa reflection para Gráficos;
- sin Embeddium existe fallback a `VideoSettingsScreen` vanilla;
- la GUI gráfica devuelta queda fuera de chrome/transiciones/click Jobs;
- las Screens de terceros se aíslan de forma genérica;
- una Screen de terceros no habilita redirecciones administrativas Jobs en sus subflujos;
- `SesionMenu.activa()` ya no basta para interceptar Options/Multiplayer/Mundos/Mods;
- Opciones, Mundos y Mods usan cierres idempotentes donde corresponde;
- los perfiles sólo se consideran activos si coinciden con todos los valores controlados por el preset;
- frontera dura de gameplay;
- continuidad Multiplayer, selección por IP y scroll en F5 y resize;
- guard de `servers.dat` para no guardar si no hubo cambios;
- audio/reload/hard-stop y optimizaciones heredadas;
- el workflow mueve explícitamente `dev-latest` a `GITHUB_SHA`;
- build y publicación sólo desde `main` verde.

## Lo que CI no puede certificar

En `test-1` comprobar:

1. con Embeddium instalado, **Gráficos abre la interfaz original de Embeddium sin marco, título, transición, sonidos de hover/click ni recolocación Jobs**;
2. las pestañas/opciones de Embeddium y las integraciones visibles del modpack siguen presentes;
3. ESC/Done desde Gráficos vuelve a Opciones Jobs una sola vez;
4. abrir/cerrar Gráficos varias veces no acumula widgets ni cambia su aspecto;
5. sin Embeddium, Gráficos abre Video Settings vanilla intacto y vuelve a Opciones Jobs;
6. **no existe botón MODPACK** en ninguna resolución/GUI Scale;
7. entrar y salir repetidamente de Opciones Jobs no queda atrapado en un bucle;
8. una pantalla externa y sus submenús no reciben bandas, transiciones ni sonidos Jobs;
9. aplicar un preset y modificar luego una opción relevante cambia el indicador a CUSTOM;
10. en Mundos y Mods: ESC con búsqueda escrita limpia; segundo ESC suelta foco; tercero vuelve al padre;
11. las tres pistas y créditos son correctos, sin música vanilla;
12. eventos/apagones/FX se cortan al entrar a mundo/servidor;
13. F3+T/resource packs no duplican música, camas ni FX;
14. F5 y resize conservan selección+scroll Multiplayer;
15. LAN, ping, MOTD y favicons siguen funcionando;
16. servidor oficial sigue primero/único/protegido y `Ghoul Outbreak` no vuelve;
17. sliders/toggles persisten tras reinicio;
18. chat/inventario/contenedores siguen fuera de Jobs;
19. PNG 10–17 siguen estáticos y JPG 18–31 conservan movimiento mínimo;
20. GUI Scale 2/3/4 no provoca solapes.

## Riesgos vigentes

### Gráficos

- La ruta Embeddium depende de que ese mod siga registrando `ConfigScreenHandler.ConfigScreenFactory` en Forge 1.20.1. Si falta o falla, Jobs cae de forma segura a Video Settings vanilla.
- Jobs no intenta adivinar proveedores gráficos distintos de Embeddium. La prioridad actual es que la pantalla abierta no sea modificada por Jobs.
- Una integración que sólo exista al entrar por un `OptionsScreen` completo pero no esté presente en la propia Screen registrada por Embeddium debe validarse manualmente en el modpack real.

### Navegación

- Las redirecciones administrativas están deliberadamente acotadas a padres Jobs concretos. Un mod que sustituya totalmente las clases vanilla de navegación puede necesitar compatibilidad específica.
- Las pantallas externas se detectan por namespace; un mod que inyecte comportamiento dentro de una clase `net.minecraft.*` conserva el tratamiento Minecraft de esa clase.

### Perfiles

- La detección exacta compara sólo campos que el preset escribe. Cambiar una preferencia deliberadamente libre no convierte el perfil en CUSTOM.

### Audio / Config / Multiplayer

- Mods que sustituyan por completo el motor de sonido pueden requerir compatibilidad específica.
- Los cambios reales de config siguen dependiendo de persistencia Forge y deben probarse tras reiniciar.
- El scroll Multiplayer puede ser limitado por listas alteradas por terceros.
- Entradas LAN siguen siendo efímeras y se recrean con el detector nuevo.

### Pipeline

- La comprobación definitiva tras publicar sigue siendo que `refs/tags/dev-latest` resuelva exactamente al SHA de `main` que generó el JAR.

## Mitigaciones

- `tools/verificar_graficos_044.py`: Gráficos intocable, ausencia de MODPACK y redirecciones acotadas.
- `tools/verificar_ux_043.py`: perfiles exactos y ESC/búsqueda.
- `tools/verificar_compatibilidad_042.py`: aislamiento genérico de terceros.
- verificadores históricos de runtime, audio, reload, optimización, continuidad y versión siguen activos.

## Reporte útil

Ante un fallo, guardar versión/JAR, SHA-256, `latest.log`, pantalla/nivel, resolución, GUI Scale, secuencia de navegación, estado del buscador, perfil indicado, nombre del mod dueño de la Screen externa y qué GUI exacta abrió Gráficos.
