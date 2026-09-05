# Riesgos y pruebas pendientes — 0.38.0

Este documento contiene riesgos vigentes. El historial está en `CHANGELOG.md` y en las auditorías de `docs/`.

## Certificado automáticamente

Antes de publicar, GitHub Actions comprueba:

- Java 17 y Forge build 1.20.1;
- JAR versionado y política de `dev-latest`;
- integridad de PNG 10–17 y JPEG 18–31;
- paridad ES/EN, recursos y coherencia estática;
- contratos UI/música y hard-stop de gameplay;
- aislamiento completo de Video Settings vanilla;
- salida única/idempotente y continuidad de selección de Multiplayer;
- feedback F5 Jobs y ausencia del antiguo `JOBS/SERVER`;
- frontera dura que excluye chat, inventario y UI de gameplay;
- ausencia de `PantallaVideoJobs` y reflection de páginas Embeddium;
- rango `nivel_fijo` coherente con 32 niveles;
- perfiles ambientales y lecturas semánticas de sliders;
- índice/documentación vigente sincronizada;
- caché de fields/listas de `ListasExpediente` y deduplicación de scrollbar por frame;
- liberación de cachés al cerrar Screen;
- filtro de `PlantaImagen` por instancia de textura, no por frame;
- caché de texto/calendario de `NotaAviso`;
- reloj único y recorrido unificado de widgets en `PulidoInterfazJobs`;
- snapshot de `RotacionNiveles` compartido dentro del mismo milisegundo;
- Alto contraste resuelto una vez por pasada de `PielVanillaJobs`;
- reducción de capas/draw calls en Bajo consumo;
- `preserveFileTimestamps=false`, `reproducibleFileOrder=true` y ausencia de `Implementation-Timestamp`;
- publicación de `jobsmenu-0.38.0.jar` en `dev-latest` sólo desde `main` verde.

Un pipeline fallido no debe actualizar la release.

## Lo que CI no certifica

CI no abre Minecraft con ventana real ni mide FPS/GPU. Después del deploy hay que validar:

1. Los niveles 10–17 siguen completamente estáticos y los 18–31 cargan el JPG correcto.
2. El encuadre/respiración de 18–31 sigue igual en modo normal y se congela con Movimiento reducido/Bajo consumo.
3. F3+T vuelve a mostrar los fondos con filtrado correcto, sin textura morado/negro ni pérdida del filtro.
4. Bajo consumo mantiene la identidad visual aunque use menos bandas/capas.
5. Comparar fluidez de Mundos, Mods, Resource Packs, Idioma y Multiplayer con listas largas.
6. Scrollbar Jobs aparece una sola vez, sigue alineada y no afecta wheel/click/drag de la lista real.
7. `N`, `M`, F3+T, Alt+Tab y navegación no duplican audio.
8. Gameplay corta inmediatamente música y ambiente Jobs.
9. ESC/Cancelar/F5 de Multiplayer conservan el comportamiento 0.37.0 y F5 conserva selección online por IP.
10. Varias recargas F5 conservan LAN, ping, favicons, MOTD y selección por teclado.
11. Chat/inventario/containers siguen sin piel, bandas, transición ni click Jobs.
12. Pausa/Config Jobs durante gameplay conservan tema y feedback breve pero ninguna transición ni música/ambiente.
13. Salir/kick/perder conexión de un servidor vuelve a Multijugador Jobs.
14. Los avisos rotan en el mismo ritmo y las ventanas especiales siguen apareciendo en la fecha/hora correspondiente.
15. Alto contraste continúa aplicándose a todos los widgets vanilla tematizados.

## Riesgos vigentes

### Rendimiento y cachés

- Las optimizaciones reducen trabajo redundante, pero CI no puede cuantificar una mejora de FPS; la ganancia depende de resolución, cantidad de widgets/listas y otros mods.
- `ListasExpediente` cachea sólo la Screen viva y se invalida en `estilizar()`/cierre. Un mod que reconstruya internamente una lista después de `init()` sin pasar por esas rutas podría requerir compatibilidad específica.
- `RotacionNiveles` comparte el record únicamente si varias consultas caen en el mismo milisegundo; no es un cache de larga duración.
- `PlantaImagen` compara identidad del `AbstractTexture`; resource reload debe crear/reentregar la textura y activar de nuevo el filtro, por eso F3+T forma parte del checklist manual.

### Bajo consumo

- Reduce bandas de vignette, capas de profundidad/rebote y líneas de humedad. La intensidad global se conserva, pero el acabado puede verse algo menos fino a resoluciones grandes, lo cual es deliberado para este perfil.
- No debe alterar el modo normal. Cualquier diferencia visual con Bajo consumo desactivado es una regresión.

### Fondos

- Los JPG 18–31 son 1920×1080; el coste depende de GPU, resolución y mods gráficos.
- `NativeImage` sigue validando cada recurso en su primer uso y existe fallback procedural si falla.
- Los PNG 10–17 mantienen resolución histórica; filtrado lineal suaviza escalado pero no inventa detalle.

### Interfaces

- Scrollbars Jobs siguen siendo visuales; posición, wheel, click y drag pertenecen a `AbstractSelectionList` real.
- Mods/resource packs que reconstruyan profundamente Screens/listas pueden necesitar compatibilidad específica.
- Jobs abre Video Settings vanilla y no reimplementa Embeddium/Oculus.
- La piel posterior de controles conserva listeners, foco e hitboxes vanilla.

### Multiplayer

- F5 transporta sólo la IP de una entrada online guardada y busca una Entry nueva en la lista reconstruida.
- Entradas LAN son efímeras y no se fuerzan por IP; se dejan renacer con el detector nuevo.
- Mods que sustituyan por completo `JoinMultiplayerScreen` pueden requerir compatibilidad específica.

### Audio

- El modo fijo conserva una sola pista; `N` se rechaza con `UI_NEGADO` para no contradecir la preferencia.
- Los niveles 18–31 reutilizan camas/eventos autorizados; separación perceptiva requiere prueba dentro del juego.
- Click/hover Jobs dentro de pausa/configuración no deben reactivar música ni ambiente.
- La mezcla perceptiva de las tres pistas y los cambios F3+T/Alt+Tab siguen siendo pruebas manuales.

### Navegación tras servidor

- Servidor remoto vuelve a Multijugador Jobs; mundo local vuelve al main Jobs.
- Mods que sustituyan destinos de desconexión después del evento pueden requerir compatibilidad específica.

### Build/release

- Quitar timestamps variables mejora reproducibilidad, pero ForgeGradle/reobf y la versión exacta de herramientas también participan del binario; el hash publicado sigue siendo la autoridad de la entrega.
- `dev-latest` es rodante y no representa una release histórica inmutable.

## Mitigaciones

- `tools/verificar_fondos.py` valida los 22 fondos de imagen.
- `tools/verificar_ui_musica.py` protege Video Settings, UI neutral y lifecycle musical.
- `tools/verificar_continuidad.py` protege retorno/F5/documentación.
- `tools/verificar_optimizacion.py` protege los caminos calientes introducidos en 0.38.0.
- Movimiento reducido y Bajo consumo tienen prioridad sobre decoración.
- JAR versionado y CI obligatorio antes de publicar.

## Reporte

Un fallo visto en Minecraft debe incluir versión/JAR, SHA-256 si está disponible, pantalla/nivel, resolución, GUI Scale, opciones de Movimiento reducido/Bajo consumo, mods de UI/vídeo relevantes y captura. Adjuntar `latest.log` si afecta crash, recursos o audio.

Un defecto visual, sonoro o de rendimiento no se considera corregido sólo porque compile.
