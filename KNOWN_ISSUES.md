# Riesgos y pruebas pendientes — 0.35.0

Este documento contiene riesgos vigentes. El historial está en `CHANGELOG.md` y en las auditorías de `docs/`.

## Certificado automáticamente

Antes de publicar, GitHub Actions comprueba:

- Java 17;
- JAR versionado;
- integridad de PNG 10–17;
- presencia, firma y dimensiones 1920×1080 de JPEG 18–31;
- paridad ES/EN, recursos y coherencia estática;
- contratos UI/música y hard-stop de gameplay;
- aislamiento completo de Video Settings vanilla;
- salida única e idempotente de Multiplayer;
- transiciones limitadas a pantallas propias Jobs;
- frontera dura que excluye chat, inventario y UI de gameplay;
- ausencia de `PantallaVideoJobs` y reflection de páginas Embeddium;
- rango `nivel_fijo` coherente con los 32 niveles;
- perfiles ambientales explícitos para los niveles 18-31;
- lecturas semánticas en sliders de volumen, tiempo, nivel y pista;
- Forge build 1.20.1;
- publicación de `jobsmenu-0.35.0.jar` en `dev-latest` sólo desde `main`.

Un pipeline fallido no debe actualizar la release.

## Lo que CI no certifica

CI no abre Minecraft con ventana real. Después del deploy hay que validar visualmente:

1. Los niveles 18–31 cargan su JPG correcto, sin textura morado/negro.
2. El encuadre cover no corta zonas importantes en GUI Scale 2/3/4 y distintas relaciones de aspecto.
3. La respiración de cámara de 18–31 es sutil y no resulta molesta.
4. Movimiento reducido, Bajo consumo o escena quieta congelan los JPG nuevos.
5. PNG 10–17 siguen completamente estáticos.
6. `nivel_fijo` puede seleccionar cualquier nivel hasta 31.
7. `N`, `M`, F3+T, Alt+Tab y navegación por subpantallas no duplican audio.
8. Gameplay corta inmediatamente música y ambiente Jobs.
9. Mods y Resource Packs conservan sus listas reales.
10. ESC/Volver en Mundos y Multiplayer regresa en una sola acción.
11. Chat e inventario no muestran barridos, bandas ni pieles Jobs.
12. Pausa y configuración conservan la tematización permitida.
13. Controles vanilla conservados dentro de pausa/configuración usan click y hover Jobs sin reactivar música.
14. Salir o perder conexión de un servidor vuelve a Multijugador Jobs, y desde allí ESC vuelve al main en una sola acción.

## Riesgos vigentes

### Fondos 18–31

- Los JPG son 1920×1080 y se renderizan directamente desde recursos; el coste depende de GPU, resolución y otros mods gráficos.
- El movimiento de cámara de 18–31 es un recorte/zoom mínimo en runtime. No reescribe ni deforma el archivo, pero su sensación final requiere prueba visual.
- Algunos fondos tienen composición cerca de los bordes; una ventana no 16:9 puede recortar laterales o parte superior/inferior por el comportamiento cover.
- No existe profiler GPU automático.

### Fondos 10–17

- Permanecen rasterizados a su resolución histórica.
- Filtrado lineal suaviza el escalado, pero no inventa detalle.
- No reciben zoom, paneo, parallax, flicker, partículas, foreground dinámico ni deformación.

### Interfaces

- La composición adaptativa reserva zonas independientes para rótulo, estado y crédito; aun así debe revisarse visualmente con traducciones o resource packs que cambien mucho el ancho del texto.
- Las capas Jobs posteriores al render sólo actúan en el flujo de menús. Chat, inventario y pantallas no Jobs con un mundo cargado se excluyen de forma explícita.
- Las pantallas propias de pausa/configuración pueden necesitar compatibilidad específica con mods/resource packs que reorganicen profundamente una Screen.
- Scrollbars Jobs son visuales; rueda, click y drag pertenecen a la lista real.
- Jobs abre Video Settings vanilla y no fuerza ni reconstruye la interfaz de Embeddium. La integración que otros mods hagan sobre la clase vanilla debe validarse en el modpack real.

### Audio

- El modo fijo mantiene una sola pista hasta que el usuario vuelva a Aleatoria o elija otra; `N` se rechaza deliberadamente para no contradecir esa preferencia y lo confirma con `UI_NEGADO`.
- Los niveles 18-31 reutilizan camas y eventos autorizados de 0-9 con mezclas propias; la separación perceptiva entre fondos requiere prueba dentro del juego.
- Los clicks vanilla se sustituyen en superficies Jobs. En pausa/configuración dentro de gameplay sólo se permite feedback corto de interfaz; música y ambiente permanecen detenidos.
- Hover de controles vanilla preservados se deduplica por instancia y tiene además el debounce global de `UI_PASAR`; su sensación final requiere prueba con teclado y ratón.
- Las pantallas de terceros fuera de la sesión conservan su audio original.
- La mezcla perceptiva entre Absurdism, REQUIEM y Upon the Hill V2 sólo puede validarse dentro del juego.
- F3+T, Alt+Tab y cambios rápidos de pantalla deben probarse para descartar instancias fantasma perceptibles.

### Navegación tras servidor

- El retorno se decide por la primera superficie vanilla abierta tras `LoggingOut`: `JoinMultiplayerScreen` se convierte en Multijugador Jobs; Title/Realms vuelve al main Jobs.
- Mods que reemplacen completamente esos destinos después del evento de apertura pueden requerir compatibilidad específica.

### Release

- `dev-latest` es rodante. El asset lleva versión, pero el tag no representa una release histórica inmutable.

## Mitigaciones

- Los 14 JPG nuevos están versionados directamente en la ruta de recursos; no hay ZIP/Base64 ni extracción de build.
- `tools/verificar_fondos.py` valida los 22 fondos de imagen (10–31).
- `NativeImage` valida el recurso en runtime y existe fallback procedural ante fallo.
- Movimiento reducido y Bajo consumo tienen prioridad.
- Pantallas Forge/vanilla complejas conservan lógica real.
- Reproductor musical está ligado a la sesión y aplica hard-stop en gameplay.
- El feedback corto de UI se evalúa por superficie Jobs y no abre `SesionMenu`.
- JAR versionado y CI obligatorio antes de publicar.

## Reporte

Un fallo visto en Minecraft debe incluir versión/JAR, SHA-256 si está disponible, nivel/pantalla, resolución, GUI Scale, opciones de Movimiento reducido/Bajo consumo, mods de UI/vídeo relevantes y captura. Adjuntar `latest.log` si afecta crash, recursos o audio.

Un defecto visual o sonoro no se considera corregido sólo porque compile.
