# Auditoría UI 0.15.0 — archivos compactos

## Motivo

El pase parte de problemas observados dentro de Minecraft: texto de Options solapado, pantallas demasiado grandes y vacías, scroll inestable en Idioma, ausencia de ayuda en Config Jobs, títulos débiles en Singleplayer/Multiplayer, blanco puro en Mods y abuso del papel en Resource Packs.

## Hallazgos y correcciones

| Área | Causa | Cambio 0.15.0 |
|---|---|---|
| Options | La nota y el rótulo Minecraft compartían altura. | Se elimina la nota redundante, se reduce el panel a 404×288 y Config Jobs recibe tooltip. |
| Config Jobs | Los nombres no explicaban consecuencias. | Cada tab, toggle y slider enlaza su `.detalle` mediante tooltip; panel máximo 480×300. |
| Idioma | `ListaIdiomas` se renderizaba manualmente y otra vez desde `Screen`; la barra usaba `getScrollBottom()` como si fuera `y1`. | Una sola pasada de render y lectura de los campos SRG `y0/y1`. |
| Pantallas vanilla | Paneles a ancho completo con mucho espacio libre. | `GeometriaExpediente` compacta Sonido, Video, Chat, Accesibilidad, Online, Mouse y Teclas. |
| Singleplayer | Título vanilla sin identidad y dirt aislado. | **Archivo de turnos**, buscador reubicado y marco oscuro con margen. |
| Multiplayer | Título débil y sin punto de acceso canónico. | **Puestos de acceso**, tarjeta superior y servidor oficial persistente en primer lugar. |
| Mods | Forge fija `0xFFFFFF` para lista e información. | Tinte sepia de render, marco oscuro y conservación de búsqueda, orden, Config, logos y carpeta. |
| Resource Packs | Papel gigante repetido y dirt. | Archivo oscuro; las dos listas y sus hitboxes permanecen vanilla. |
| Música | Versiones previas generaban un pack visible. | Se elimina `MusicaPropia`; migración deselecciona y borra sólo `jobsmenu-musica-activa`. |

## Servidor oficial

- IP: `JobsDosh.exaroton.me:56477`.
- Español: **Servidor oficial de Jobs**.
- Inglés: **Jobs Official Server**.
- Se guarda en `servers.dat`, se actualiza al idioma vigente y se mueve al primer renglón.
- Editar y borrar quedan desactivados cuando ese renglón está seleccionado; la conexión conserva la lógica vanilla.

## Principio visual

Papel ya no significa “cualquier pantalla Jobs”. Se usa para formularios compactos. Listas largas usan archivo oscuro, bordes de instalación y texto claro/sepia. Así el fondo del Nivel sigue visible en los márgenes sin dejar superficies enormes vacías.

## Verificación automática

La entrega debe pasar:

1. `tools/verificar_version.py`;
2. `tools/verificar_fondos.py`;
3. `tools/verificar.py` sin fallos;
4. build Forge con Java 17;
5. workflow de `main` y publicación de `jobsmenu-0.15.0.jar`.

## Prueba manual obligatoria

Comprobar GUI Scale 2–4, ES/EN/es_uy, scroll superior/inferior de Idioma, tooltips completos, servidor oficial tras refrescar, búsqueda y acciones de mundos/mods/recursos, ausencia del pack musical legado y continuidad de audio al navegar.
