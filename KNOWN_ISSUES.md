# Riesgos y pruebas pendientes — 0.46.0

Este archivo contiene sólo riesgos vigentes. El historial vive en `CHANGELOG.md` y en las auditorías de `docs/`.

## Certificado automáticamente

Antes de publicar, GitHub Actions comprueba:

- Java 17 + Forge build 1.20.1 y JAR versionado;
- integridad de fondos e idiomas;
- Gráficos mantiene el contrato intocable 0.44: Embeddium vía `ConfigScreenFactory`, fallback vanilla, sin chrome/transición/click Jobs;
- no existen MODPACK ni rutas obsoletas de Options;
- Screens de terceros y subflujos externos quedan aislados;
- búsqueda transversal de Config Jobs disponible con `Ctrl+F` y continuidad de filtro/foco/scroll;
- el buscador navega mediante categoría explícita y no sintetiza teclas sobre su padre;
- filas del buscador cachean textos y contador fuera del hot path de render;
- Config recuerda última categoría y muestra `CUSTOM` explícitamente;
- **Idioma y Force Unicode Font se aplican en una sola transacción de resource reload**;
- ante fallo de reload se restauran idioma, `LanguageManager` y Unicode al estado anterior;
- un callback tardío de Idioma no navega si `PantallaIdiomaJobs` dejó de ser la Screen actual;
- Idioma y Buscador añaden cierres protegidos;
- Mundos y Mods conservan filtro/foco en resize;
- Resource Packs no puede devolver tarde a Opciones Jobs después de abandonar su pantalla;
- Sonido cachea el `Field` reflectivo de `OptionsList` una vez por JVM;
- continuidad Multiplayer, selección por IP y scroll en F5/resize;
- audio/reload/hard-stop, perfiles exactos y optimizaciones heredadas;
- el workflow mueve explícitamente `dev-latest` a `GITHUB_SHA` y publica sólo desde `main` verde.

## Lo que CI no puede certificar

En `test-1` comprobar:

1. `Ctrl+F` desde Config abre el buscador y devuelve resultados útiles en español/inglés;
2. Enter/doble clic abre siempre la categoría correcta sin pantalla intermedia ni doble navegación;
3. ESC del buscador limpia texto, suelta foco y finalmente vuelve a Config con una acción por etapa;
4. resize/maximizar conserva filtro, foco razonable y scroll del buscador;
5. cambiar categoría, cerrar Config y reabrir conserva la última categoría de la sesión;
6. editar un preset muestra `CUSTOM`;
7. en Idioma, cambiar **sólo Force Unicode Font** y pulsar Aplicar produce una recarga visible y vuelve una sola vez al padre;
8. cambiar idioma + Unicode juntos produce **una sola recarga** y aplica ambos cambios;
9. si resource reload falla, idioma efectivo y Unicode vuelven juntos al estado anterior y puede reintentarse;
10. una finalización tardía de reload no puede devolver al padre si el cliente ya está en otra Screen;
11. filtro, selección pendiente y scroll de Idioma sobreviven a resize;
12. Resource Packs no secuestra otra navegación con un callback tardío;
13. con Embeddium instalado, Gráficos sigue siendo la interfaz original y sin Jobs encima;
14. sin Embeddium, Video Settings vanilla sigue intacto;
15. las tres pistas/créditos son correctos y gameplay corta música/camas/FX;
16. F3+T/resource packs no duplican audio;
17. F5 y resize conservan selección+scroll Multiplayer;
18. LAN, ping, MOTD y favicons siguen funcionando;
19. servidor oficial sigue primero/único/protegido y `Ghoul Outbreak` no vuelve;
20. sliders/toggles persisten tras reinicio;
21. chat/inventario/contenedores siguen fuera de Jobs;
22. PNG 10–17 siguen estáticos y JPG 18–31 conservan movimiento mínimo;
23. GUI Scale 2/3/4 no provoca solapes.

## Riesgos vigentes

### Gráficos

- La ruta Embeddium depende de que el mod siga registrando `ConfigScreenHandler.ConfigScreenFactory` en Forge 1.20.1. Si falla, Jobs cae a Video Settings vanilla.
- Jobs no intenta adivinar otros proveedores gráficos.

### Búsqueda / configuración

- La búsqueda usa un catálogo explícito de preferencias Jobs. Una opción nueva debe incorporarse a `AJUSTES` y al verificador correspondiente.
- La última categoría vive sólo durante la sesión del cliente.

### Idioma / Unicode

- CI certifica la transacción y rollback estáticos, pero no puede provocar todas las formas de fallo de ResourceManager/modpack.
- Otros mods pueden reaccionar a una recarga fallida por su cuenta; Jobs sólo garantiza coherencia de sus valores vanilla administrados.
- La prueba importante de 0.46 es cambiar únicamente Unicode: antes podía quedar persistido sin una recarga inmediata.

### Navegación

- Las redirecciones administrativas están deliberadamente acotadas a padres Jobs concretos. Un mod que sustituya completamente clases vanilla puede necesitar compatibilidad específica.
- Las pantallas externas se detectan por namespace; una inyección dentro de `net.minecraft.*` conserva el tratamiento Minecraft de esa clase.

### Audio / Config / Multiplayer

- Mods que sustituyan por completo el motor de sonido pueden requerir compatibilidad específica.
- Persistencia Forge debe validarse tras reinicio real.
- El scroll Multiplayer puede ser limitado por listas alteradas por terceros; LAN sigue siendo efímero.

### Pipeline

- Tras publicar hay que comprobar que `refs/tags/dev-latest` resuelva exactamente al SHA de `main` que generó el JAR.

## Mitigaciones

- `tools/verificar_lifecycle_046.py`: transacción Idioma/Unicode, callback tardío y navegación explícita del buscador.
- `tools/verificar_calidad_045.py`: búsqueda, continuidad, cierres, rollback base y callback de packs.
- `tools/verificar_graficos_044.py`: Gráficos intocable y ausencia de MODPACK.
- `tools/verificar_ux_043.py`: perfiles exactos y navegación heredada.
- `tools/verificar_compatibilidad_042.py`: aislamiento genérico de terceros.
- verificadores de runtime, audio, reload, optimización, continuidad y versión siguen activos.

## Reporte útil

Ante un fallo, guardar versión/JAR, SHA-256, `latest.log`, pantalla, resolución, GUI Scale, secuencia exacta, filtro/foco/scroll previo y posterior, idioma/Unicode anterior y pendiente, perfil indicado, selección Multiplayer y qué GUI exacta abrió Gráficos.
