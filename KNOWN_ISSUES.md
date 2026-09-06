# Riesgos y pruebas pendientes — 0.45.0

Este archivo contiene sólo riesgos vigentes. El historial vive en `CHANGELOG.md` y en las auditorías de `docs/`.

## Certificado automáticamente

Antes de publicar, GitHub Actions comprueba:

- Java 17 + Forge build 1.20.1 y JAR versionado;
- integridad de fondos e idiomas;
- Gráficos mantiene el contrato intocable 0.44: Embeddium vía `ConfigScreenFactory`, fallback vanilla, sin chrome/transition/click Jobs;
- no existen MODPACK ni rutas naturales obsoletas de Options;
- Screens de terceros y subflujos externos quedan aislados;
- búsqueda transversal de Config Jobs disponible con `Ctrl+F`;
- Config recuerda última categoría y muestra `CUSTOM` explícitamente;
- buscador de Ajustes conserva filtro/foco/scroll en resize;
- Idioma conserva pendiente/filtro/scroll y restaura el idioma anterior si falla el reload;
- Mundos y Mods conservan filtro/foco en resize;
- Apariencia, Controles y Config usan cierres protegidos;
- Resource Packs no puede devolver tarde a Opciones Jobs después de abandonar su pantalla;
- Sonido cachea el `Field` reflectivo de `OptionsList` una vez por JVM;
- perfiles exactos, frontera dura de gameplay y aislamiento externo heredados;
- continuidad Multiplayer, selección por IP y scroll en F5 y resize;
- guard de `servers.dat` para no guardar si no hubo cambios;
- audio/reload/hard-stop y optimizaciones heredadas;
- el workflow mueve explícitamente `dev-latest` a `GITHUB_SHA`;
- build y publicación sólo desde `main` verde.

## Lo que CI no puede certificar

En `test-1` comprobar:

1. `Ctrl+F` desde Config Jobs abre el buscador y devuelve resultados útiles en español/inglés;
2. Enter/doble clic abre la categoría correcta y ESC limpia/quita foco/cierra sin saltos;
3. resize/maximizar conserva filtro y scroll del buscador de Ajustes;
4. cambiar de categoría, cerrar Config y reabrir conserva la última categoría de la sesión;
5. editar un preset muestra `CUSTOM` en el indicador superior;
6. en Idioma, seleccionar otro idioma y redimensionar conserva selección, filtro y scroll;
7. forzar un fallo de resource reload no deja `Options.languageCode` y `LanguageManager` desincronizados;
8. Mundos y Mods conservan filtro/foco tras resize y mantienen ESC por etapas;
9. abrir/cerrar Apariencia, Controles y Config repetidamente no produce doble retorno;
10. salir de Resource Packs y navegar a otra pantalla no permite que un callback tardío vuelva a Opciones Jobs;
11. con Embeddium instalado, Gráficos sigue siendo la interfaz original y sin Jobs encima;
12. sin Embeddium, Video Settings vanilla sigue intacto;
13. las tres pistas y créditos son correctos, sin música vanilla;
14. eventos/apagones/FX se cortan al entrar a mundo/servidor;
15. F3+T/resource packs no duplican música, camas ni FX;
16. F5 y resize conservan selección+scroll Multiplayer;
17. LAN, ping, MOTD y favicons siguen funcionando;
18. servidor oficial sigue primero/único/protegido y `Ghoul Outbreak` no vuelve;
19. sliders/toggles persisten tras reinicio;
20. chat/inventario/contenedores siguen fuera de Jobs;
21. PNG 10–17 siguen estáticos y JPG 18–31 conservan movimiento mínimo;
22. GUI Scale 2/3/4 no provoca solapes.

## Riesgos vigentes

### Gráficos

- La ruta Embeddium depende de que ese mod siga registrando `ConfigScreenHandler.ConfigScreenFactory` en Forge 1.20.1. Si falta o falla, Jobs cae de forma segura a Video Settings vanilla.
- Jobs no intenta adivinar proveedores gráficos distintos de Embeddium.

### Búsqueda / configuración

- La búsqueda trabaja sobre el catálogo explícito de preferencias propias de Jobs; una opción nueva debe añadirse al catálogo y al verificador 0.45.
- La última categoría recordada vive durante la sesión del cliente, no se persiste como preferencia de usuario.

### Idioma

- Un fallo real de reload depende del ResourceManager/modpack; CI sólo puede verificar la existencia del rollback, no provocar todos los fallos externos posibles.
- El rollback restaura el selector de idioma, pero otros mods pueden reaccionar de forma independiente a un reload fallido.

### Navegación

- Las redirecciones administrativas están deliberadamente acotadas a padres Jobs concretos. Un mod que sustituya totalmente las clases vanilla de navegación puede necesitar compatibilidad específica.
- Las pantallas externas se detectan por namespace; un mod que inyecte comportamiento dentro de una clase `net.minecraft.*` conserva el tratamiento Minecraft de esa clase.

### Audio / Config / Multiplayer

- Mods que sustituyan por completo el motor de sonido pueden requerir compatibilidad específica.
- Los cambios reales de config siguen dependiendo de persistencia Forge y deben probarse tras reiniciar.
- El scroll Multiplayer puede ser limitado por listas alteradas por terceros.
- Entradas LAN siguen siendo efímeras y se recrean con el detector nuevo.

### Pipeline

- La comprobación definitiva tras publicar sigue siendo que `refs/tags/dev-latest` resuelva exactamente al SHA de `main` que generó el JAR.

## Mitigaciones

- `tools/verificar_calidad_045.py`: búsqueda, continuidad, rollback, cierres y callback de packs.
- `tools/verificar_graficos_044.py`: Gráficos intocable y ausencia de MODPACK.
- `tools/verificar_ux_043.py`: perfiles exactos y ESC/búsqueda heredada.
- `tools/verificar_compatibilidad_042.py`: aislamiento genérico de terceros.
- verificadores históricos de runtime, audio, reload, optimización, continuidad y versión siguen activos.

## Reporte útil

Ante un fallo, guardar versión/JAR, SHA-256, `latest.log`, pantalla/nivel, resolución, GUI Scale, secuencia de navegación, filtro/foco/scroll previo y posterior, idioma anterior/pendiente, perfil indicado, nombre del mod dueño de la Screen externa y qué GUI exacta abrió Gráficos.
