# Riesgos y pruebas pendientes — 0.17.0

Este documento contiene únicamente riesgos vigentes. Los incidentes históricos quedan en `CHANGELOG.md` y las auditorías de `docs/`.

## Estado certificado automáticamente

Antes de publicar una entrega, GitHub Actions comprueba:

- Java 17;
- nombre/versionado obligatorio del JAR;
- integridad PNG/CRC/IDAT de fondos 10–17;
- recursos, idiomas, ASCII Java y coherencia estática;
- separación de paleta entre escena e interfaz en los componentes compartidos principales;
- contrato del reproductor musical (Absurdism, fades, crossfade preparado y hard stop de gameplay);
- build Forge 1.20.1;
- creación de `jobsmenu-0.17.0.jar`;
- publicación en `dev-latest` únicamente desde `main`.

Un build que no termina en verde no debe actualizar la release.

## Pruebas manuales prioritarias

1. **Options → Config Jobs.** El botón de configuración del mod debe verse claramente como acción principal de ancho completo, separado de la sección de opciones Minecraft.
2. **Mods → Jobs Menu → Config.** Debe abrir exactamente la misma `PantallaAjustesAviso` que Options.
3. **Cinco categorías de Config.** Visual, Nivel, Audio, Accesibilidad y Sistema deben cambiar sin perder valores.
4. **Persistencia.** Cambiar un toggle/slider, salir, volver y comprobar el valor.
5. **Layout.** GUI Scale 2, 3 y 4; 854×480; 1280×720; ventana estrecha. Tabs, controles, pie y Volver no pueden solaparse.
6. **Widgets.** Botón JOBS, botones normales/principales, toggles y sliders deben responder a hover, click, Tab, Enter, Espacio y Escape sin hitboxes invisibles.
7. **Paleta 0.17.** Botones, foco, campos, sliders y transiciones no deben heredar amarillo de pared/fluorescente. La escena sí puede conservarlo cuando corresponde al Nivel.
8. **Archivo oscuro.** Mundos, Multiplayer, Mods y Recursos deben usar superficies grafito/gris; controles vanilla cubiertos no pueden reaparecer como papel amarillo/claro.
9. **Idioma.** Buscador centrado, selección gris neutra, Ctrl+F y Escape para limpiar búsqueda.
10. **Movimiento reducido.** Debe simplificar microanimaciones y transición entre expedientes.
11. **Diálogos vanilla auxiliares.** Direct Connect, Add Server y confirmaciones conservan lógica original con presentación Jobs.
12. **Pantallas de terceros.** Embeddium u otras interfaces externas no reciben `PielVanillaJobs` indiscriminadamente.
13. **Scrollbar Jobs.** Rueda, click y drag deben coincidir con el tirador visual.
14. **Accesibilidad vanilla.** Primera/última fila, scrollbar y `Cerrar expediente` separados; la Guía vanilla no debe superponerse.
15. **Multijugador.** Ping, MOTD, selección, LAN, Direct Connect, Add/Edit/Delete/Refresh y Cancel funcionan realmente.
16. **Servidor oficial.** Una sola entrada para `JobsDosh.exaroton.me:56477`; `Ghoul Outbreak` no reaparece.
17. **Mundos.** Previews, selección, crear/editar/borrar/recrear y volver conservan lógica vanilla.
18. **Mods / Forge.** Búsqueda, orden, selección, Config, información y abrir carpeta.
19. **Resource Packs.** Sin bloque aislado de dirt; selección, orden, aplicar y carpeta intactos.
20. **ES/EN/Uruguay.** No mezclar `Close file`, `Notice settings` u otras cadenas inglesas.
21. **Absurdism.** En arranque limpio debe entrar con fade-in y volumen perceptible sin golpe inicial.
22. **Continuidad musical.** Title → Options → Mods → Recursos → volver no reinicia ni duplica la pista.
23. **F3+T / Alt+Tab.** No crear instancias fantasma ni duplicadas.
24. **Ducking.** Transición de Nivel, Suspensión y presencia bajan música sin corte digital.
25. **Entrada a gameplay.** Desde el primer tick jugable no se oye música ni ambiente del menú.
26. **Retorno.** Salir de mundo/servidor/kick vuelve a `PantallaNivel` y crea una visita musical nueva.
27. **Segunda pista futura.** Cuando exista el OGG autorizado de la referencia `t9KaSaGEwvI`, probar crossfade continuo y sin pico de mezcla.
28. **PNG 10–17.** Ningún zoom, paneo, parallax, flicker, scanline animada, niebla móvil, motas o presencia.
29. **18 niveles.** Recorrer 0–17 verificando continuidad de escena/audio.
30. **Entrega.** En `test-1\mods` debe quedar un único `jobsmenu-0.17.0.jar`.

## Riesgos conocidos

- `PielVanillaJobs` es una capa visual posterior al render. Un mod/resource pack que cambie radicalmente geometría u orden puede requerir compatibilidad específica.
- La scrollbar Jobs depende de datos internos de `AbstractSelectionList` 1.20.1; existe reflection defensiva y fallback.
- Las envolturas de pantallas vanilla dependen de estructuras de Minecraft 1.20.1; reemplazos totales de terceros pueden necesitar integración dedicada.
- Embeddium se respeta como pantalla externa y no se reconstruye por reflection profunda.
- Fuentes con métricas extremas pueden forzar elipsis o alterar el layout.
- Los PNG suministrados son rasterizados; la calidad máxima depende de la fuente.
- No existe profiler GPU automático; bajo consumo reduce capas pero el coste depende del equipo.
- La segunda pista solicitada no está empaquetada mientras no exista un OGG autorizado; la URL por sí sola no es un recurso de build.
- `dev-latest` es rodante; el asset lleva versión pero el tag no es una release histórica inmutable.

## Mitigaciones vigentes

- PNG 10–17 aislados de capas animadas.
- Paleta UI separada de la escena y verificada en CI.
- Config Jobs conectada directamente a `ConfigTurno`.
- Redirecciones sensibles por clase exacta.
- Pantallas de terceros no reciben skin de controles indiscriminadamente.
- Botones vanilla duplicados se desactivan además de ocultarse cuando corresponde.
- Scrollbar visual conserva comportamiento real y tiene fallback.
- `NativeImage` valida fondos en runtime y existe fallback procedural.
- Reproductor musical ligado a sesión con watchdog de SoundEngine.
- Hard stop de audio al entrar en gameplay.
- JAR versionado y verificado por CI.
- Movimiento reducido, destellos reducidos y bajo consumo tienen prioridad.

## Regla de reporte

Un problema observado en Minecraft debe incluir versión exacta del JAR, pantalla/Nivel, ruta usada, resolución/GUI Scale, mods que sustituyan UI/video, opciones de accesibilidad, si ocurrió durante transición/Suspensión y `latest.log` cuando afecte crash/recursos/audio.

Un fallo visual o sonoro no se considera corregido sólo porque el proyecto compile.
