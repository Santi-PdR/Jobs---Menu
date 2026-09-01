# Riesgos y pruebas pendientes — 0.16.1

Este documento contiene únicamente riesgos vigentes. Los incidentes históricos quedan en `CHANGELOG.md` y las auditorías de `docs/`.

## Estado certificado automáticamente

Antes de publicar una entrega, GitHub Actions comprueba:

- Java 17;
- nombre/versionado obligatorio del JAR;
- integridad PNG/CRC/IDAT de fondos 10–17;
- recursos, idiomas, ASCII Java y coherencia estática;
- build Forge 1.20.1;
- creación de `jobsmenu-0.16.1.jar`;
- publicación en `dev-latest` únicamente desde `main`.

Un build que no termina en verde no debe actualizar la release.

## Pruebas manuales prioritarias

1. **Options → Config Jobs.** El botón de configuración del mod debe verse claramente como acción principal de ancho completo, separado de la sección de opciones Minecraft.
2. **Mods → Jobs Menu → Config.** Debe abrir exactamente la misma `PantallaAjustesAviso` que Options, no una pantalla alternativa.
3. **Cinco categorías de Config.** Visual, Nivel, Audio, Accesibilidad y Sistema deben cambiar sin perder valores ni regresar a una pantalla vanilla.
4. **Persistencia.** Cambiar un toggle/slider, salir, volver a abrir Config y comprobar que el valor sigue aplicado.
5. **Layout de Config.** GUI Scale 2, 3 y 4; 854×480; 1280×720; ventana estrecha. Tabs, controles, pie y Volver no pueden solaparse.
6. **Widgets nuevos.** Botón JOBS, botones normales/principales, toggles y sliders deben responder a hover, click, Tab, Enter, Espacio y Escape sin hitboxes invisibles.
7. **Movimiento reducido.** Debe quitar/simplificar microanimaciones de foco y convertir la transición entre expedientes en fade breve.
8. **Diálogos vanilla auxiliares.** Direct Connect, Add Server y confirmaciones deben conservar su lógica pero mostrar botones/campos integrados con Jobs durante la sesión.
9. **Pantallas de terceros.** Embeddium u otras interfaces externas no deben recibir `PielVanillaJobs`; sólo contexto visual mínimo cuando corresponda.
10. **Scrollbar Jobs.** Rueda, click y drag deben coincidir con el tirador visual; probar Sonido, Chat, Accesibilidad, Teclas, Online y cualquier lista larga.
11. **Accesibilidad vanilla.** Primera/última fila, scrollbar y `Cerrar expediente` deben quedar separados; la Guía de accesibilidad vanilla no debe reaparecer superpuesta.
12. **Options completo.** Piel, Sonido, Video, Controles, Idioma, Chat, Resource Packs, Accesibilidad, Online y FOV deben abrir/volver correctamente.
13. **Multijugador.** Ping, MOTD, selección, LAN, Direct Connect, Add/Edit/Delete/Refresh y Cancel deben conservar funcionamiento real.
14. **Idioma y resource reload.** ES ↔ EN, F3+T y Resource Packs no deben duplicar música/ambiente ni romper el chrome.
15. **Pausa → Options.** Desde un mundo, volver debe regresar a la pausa correcta y no al título.
16. **PNG 10–17 estáticos.** Permanecer varios segundos en cada uno: ningún zoom, paneo, parallax, flicker, scanline animada, niebla móvil, motas o presencia.
17. **Transición entre PNG.** El apagón/cambio de Nivel puede existir; una vez estabilizado, el PNG vuelve a estar completamente inmóvil.
18. **Los 18 niveles.** Recorrer 0–17 verificando continuidad de escena/audio al navegar por interfaces.
19. **Audio/lifecycle.** Abrir muchas pantallas, Alt+Tab, F3+T, cambiar idioma, entrar a mundo y volver sin loops duplicados ni sonidos huérfanos.
20. **Entrega.** En `test-1\mods` debe quedar un único `jobsmenu-0.16.1.jar`.
21. **Español (Uruguay).** Seleccionar `Español (Uruguay)` y comprobar que Jobs no mezcla `Close file`, `Notice settings` u otras cadenas inglesas.
22. **Seleccionar mundo.** Previews, selección, crear/editar/borrar/recrear y volver deben conservar lógica vanilla mientras el marco permanece Jobs.
23. **Mods / Forge.** Búsqueda, orden A–Z/Z–A, selección, Config, panel de información y abrir carpeta deben seguir funcionando dentro del chrome Jobs.
24. **Resource Packs.** No debe quedar un bloque aislado de dirt/bandas vanilla; selección, orden, aplicar y abrir carpeta deben conservarse.

## Riesgos conocidos

- `PielVanillaJobs` es deliberadamente una capa visual posterior al render. Conserva el comportamiento vanilla, pero un resource pack o mod que cambie radicalmente dimensiones/orden de render puede requerir compatibilidad específica.
- La scrollbar Jobs depende de datos internos de `AbstractSelectionList` 1.20.1. Hay reflection defensiva y fallback; una lista profundamente reemplazada puede conservar aspecto vanilla.
- Las envolturas de Sonido/Video/Chat/Accesibilidad/Mouse/Teclas/Online dependen de estructuras de Minecraft 1.20.1. Mods que las sustituyan completamente pueden necesitar integración dedicada.
- Embeddium se respeta como pantalla externa. No se intenta reconstruir su interfaz por reflection profunda.
- Resource packs con fuentes de métricas extremas pueden forzar elipsis o alterar el equilibrio del layout.
- Los aliases de español se generan desde `es_es` durante el procesado de recursos; una variante futura de Minecraft no incluida explícitamente requerirá añadir su alias.
- Los PNG suministrados son rasterizados; su calidad máxima depende de la imagen fuente.
- No existe profiler GPU automático. Bajo consumo reduce capas, pero el coste final depende de resolución, GUI Scale y GPU.
- verificar que la migración retire `resourcepacks/jobsmenu-musica-activa` y conserve intactos los demás paquetes del usuario.
- `dev-latest` es rodante; el asset lleva versión, pero el tag no es una release histórica inmutable.

## Mitigaciones vigentes

- PNG 10–17 aislados de todas las capas animadas del renderer.
- Config Jobs propia conectada directamente a `ConfigTurno`.
- Sustituciones importantes por clase exacta.
- Pantallas de terceros no reciben skin de controles vanilla.
- Botones vanilla duplicados se desactivan además de ocultarse.
- Footer con zona central y esquina derecha reservadas para navegación/overlays.
- Scrollbar visual conserva el comportamiento real de Minecraft y tiene fallback seguro.
- `NativeImage` vuelve a validar fondos en runtime y existe fallback procedural.
- JAR versionado y verificado por CI.
- Movimiento reducido, destellos reducidos y bajo consumo tienen prioridad sobre decoración.
- Audio gestionado por visita, no por instancia individual de pantalla.
- El deploy valida el JAR nuevo antes de borrar la versión instalada.

## Regla de reporte

Un problema observado en Minecraft debe incluir:

- versión exacta del JAR;
- pantalla/Nivel;
- ruta usada para llegar a ella;
- resolución y GUI Scale;
- mods que sustituyan UI/video;
- opciones de accesibilidad/bajo consumo activas;
- si ocurrió durante transición/Suspensión;
- `latest.log` para crashes, recursos o audio.

Un fallo visual no se considera corregido sólo porque el proyecto compile.
