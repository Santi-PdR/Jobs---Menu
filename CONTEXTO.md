# CONTEXTO — Jobs · Aviso a los ocupantes

Documento maestro del estado **vigente** del mod. El historial de implementaciones anteriores vive en `CHANGELOG.md` y `docs/`; este archivo describe lo que debe ser verdad hoy.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama de entrega | `main` |
| Mod id | `jobsmenu` |
| Nombre visible | Jobs · Aviso a los ocupantes |
| Versión actual | **0.16.1** |
| Artefacto esperado | **`jobsmenu-0.16.1.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **18 (0–17)** |
| Alcance | Menús, interfaces, escena, audio, lore y accesibilidad. Sin gameplay. |

## 1. Reglas duras de entrega

1. **El JAR siempre lleva versión en el nombre.** Nunca volver a publicar `jobsmenu-latest.jar`. La forma es `jobsmenu-<mod_version>.jar`.
2. `gradle.properties` es la fuente de verdad de la versión. README, CONTEXTO y changelog se actualizan en la misma entrega.
3. `main` es la rama entregable. Trabajo estructural en rama aparte; merge sólo después de CI verde.
4. CI obligatorio: Java 17 → política de versión → fondos → verificador estático → Forge build → JAR versionado.
5. `dev-latest` puede seguir siendo una release rodante, pero debe contener **un solo JAR y ese JAR debe estar versionado**.
6. Los fondos 10–17 deben superar validación PNG/CRC/IDAT. Runtime vuelve a probarlos con `NativeImage`.
7. **Los PNG 10–17 son estáticos por requisito del proyecto.** No se les agrega zoom, paneo, parallax, flicker, niebla móvil, scanlines animadas, motas, presencia ni otra capa animada.
8. Todo Java permanece ASCII; acentos y texto visible pertenecen a los archivos de idioma.
9. ES/EN tienen paridad estricta de claves.
10. El rojo es exclusivo de los Executores. No se usa como color genérico de botones peligrosos.
11. Accesibilidad y bajo consumo tienen prioridad sobre efectos decorativos.
12. Ningún control visible puede solaparse con otro control o conservar un hitbox vanilla invisible debajo.
13. Cuando se conserva una pantalla vanilla por compatibilidad, Jobs puede cambiar **su presentación**, pero no debe duplicar ni reimplementar a ciegas su lógica sensible.
14. El PowerShell se entrega **después** de CI verde y sólo instala el build publicado en `test-1`; no sustituye el proceso de compilación/certificación.

## 2. Identidad

Jobs es un **backrooms con peaje**. El ocupante trabaja, junta dinero y paga para pasar al siguiente Nivel. Los Executores son cíclicos, inevitables y no se presentan como enemigos derrotables.

La interfaz habla con voz administrativa: seca, breve y burocrática. No hay UI futurista ni HUD de combate. El lenguaje visual base es:

- papel fotocopiado y archivado;
- tinta oscura;
- fluorescentes y luz del recinto;
- bordes, sellos y marcas de formulario;
- instalación vieja pero operativa;
- amenaza sugerida, no jumpscares.

Grafía canónica: **Executor / Executores**.

## 3. Niveles y escena

Hay 18 niveles:

- 0–9: recintos procedurales vivos;
- 10–17: fondos PNG suministrados, validados y **estáticos**.

Todos comparten rotación de Nivel, apagón, música, camas ambientales, avisos, ronda, accesibilidad y estado de instalación. Esto no significa que las imágenes 10–17 reciban animación interna.

`PlantaImagen` realiza sólo un cover centrado estable, valida el PNG con `NativeImage`, lee dimensiones reales y aplica una integración estática mínima. Si un PNG falla se usa fallback procedural; nunca es aceptable dejar la textura morado/negro.

Sobre niveles 10–17 no se ejecutan las capas animadas de materiales, dirección artística, tratamiento, presencia, motas, eventos ni pulido de cámara. Los apagones y transiciones entre Niveles se conservan porque pertenecen al estado general del menú, no a la animación de la imagen.

## 4. Interfaz 0.16.1

0.16.1 es el pase de corrección basado en capturas de aceptación: Idioma registra la lista como renderizable; las pantallas vanilla conservadas borran sus cabeceras de forma opaca antes de dibujar Jobs; las fuentes de archivos oscuros usan tinta cálida; Ajustes separa pestañas, sección e indicador; y se eliminan los nombres históricos del pack musical redundante.

El servidor oficial `JobsDosh.exaroton.me:56477` queda traducido, fijado, primero en la lista y protegido frente a edición o borrado. Los recursos audiovisuales propios forman parte del mod y no se presentan como un paquete seleccionable redundante.

La lista completa y verificable está en `docs/AUDITORIA_0.16.0_64_MEJORAS.md`.

### Base heredada de 0.15.0

0.15.0 conserva la arquitectura autónoma y aplica un segundo pase basado en uso real: paneles compactos, marcos oscuros para archivos extensos, scroll estable, ayuda contextual y jerarquías nuevas para mundos, servidores, mods y recursos.

### 4.1 Principio de compatibilidad

`Santi-PdR/GripeVerde` fue sólo referencia de arquitectura de UI. Su tema victoriano/cuarentena no se copia.

Regla vigente:

- hubs y jerarquías simples pueden ser pantallas Jobs completas;
- lógica vanilla compleja se conserva cuando aporta compatibilidad;
- la presentación vanilla puede cubrirse o enmarcarse sin reemplazar hitboxes/listeners;
- las redirecciones importantes usan clase exacta para no barrer subclases de otros mods;
- una pantalla de terceros no recibe una reimplementación falsa sólo para mantener estética.

### 4.2 Options como centro de control

`PantallaOpcionesJobs` separa explícitamente dos capas:

1. **Jobs**, con Config del mod como acción principal de ancho completo y jerarquía `BotonExpediente.Tipo.JOBS`;
2. **Minecraft**, con accesos a Piel, Sonido, Video, Controles, Idioma, Chat, Resource Packs, Accesibilidad, Online y FOV cuando cabe.

El botón de Config no puede volver a quedar escondido entre opciones genéricas.

### 4.3 Config Jobs sin OptionsList

`PantallaAjustesAviso` ya no usa `OptionsList` ni `OptionInstance` como superficie visual.

Es una pantalla Jobs propia dividida en cinco categorías:

- visual;
- Nivel;
- audio;
- accesibilidad;
- sistema.

Usa directamente:

- `BotonExpediente`;
- `ToggleExpediente`;
- `SliderExpediente`;
- getters/setters reales de `ConfigTurno`.

No hay estado decorativo duplicado. Forge **Mods → Jobs Menu → Config** y el botón de Options abren la misma implementación.

Cada tab, toggle y slider tiene un tooltip localizado con su clave `.detalle`. El panel máximo es 480×300 y deja margen real alrededor de la interfaz.

### 4.4 Widgets de segunda generación

- `BotonExpediente`: estados normal/principal/JOBS/terminal, foco, presión, sombra, marcas administrativas y sonidos propios.
- `SliderExpediente`: escala visible, tirador de tinta/papel, marcas de lectura y microfeedback sonoro.
- `ToggleExpediente`: casilla + etiqueta + cápsula de estado; deja de parecer un botón Sí/No.
- Los efectos de foco respetan movimiento reducido.

### 4.5 Chrome compartido

`ChromeExpediente` aporta:

- sombra en dos planos;
- panel de papel;
- doble borde;
- perforaciones/marcas de archivo;
- pestaña de archivador;
- cabeceras con reglas laterales;
- divisores y rótulos de sección;
- elipsis segura;
- pie con formulario/Nivel/versión;
- vignette estática de interfaz;
- banda contextual para pantallas auxiliares.

`panelArchivo`, `cabeceraArchivo` y `pieArchivo` forman la variante oscura para listas extensas. Papel identifica formularios compactos; el archivo oscuro identifica mundos, servidores, mods y recursos. Ninguna pantalla debe usar papel por defecto sólo por pertenecer a Jobs.

El chrome puede añadir textura **estática** alrededor de un PNG, pero no mueve ni anima los fondos 10–17.

### 4.6 Diálogos vanilla auxiliares

`PielVanillaJobs` se aplica durante una sesión Jobs sólo a pantallas cuyo paquete es `net.minecraft.*`.

La capa se dibuja **después** del render vanilla y tematiza botones/campos conservando:

- hitbox original;
- listener original;
- foco original;
- validación y navegación original.

Esto permite que diálogos como Direct Connect, Add Server o confirmaciones no rompan visualmente la experiencia sin reimplementar protocolos.

Las pantallas de otros mods no reciben esta piel de controles.

### 4.7 Scrollbar y listas

`ListasExpediente` conserva rueda, click, drag, cantidad y posición de scroll de Minecraft. Sólo sustituye la presentación de la barra por:

- canaleta de archivador;
- topes;
- marcas de recorrido;
- tirador proporcional;
- agarres internos.

Si reflection falla, se conserva un fallback utilizable en vez de bloquear o dibujar una barra Jobs incorrecta.

### 4.8 Transiciones

`TransicionInterfazJobs` usa una hoja/carpeta con sombra, doble fibra y marcas de archivo. No bloquea input ni cambia la Screen.

Con movimiento reducido se convierte en un fade breve sin desplazamiento obligatorio.

### 4.9 Pase visual basado en capturas

El pase 0.15.0 corrige específicamente problemas que una compilación estática no podía detectar por sí sola:

- el botón vanilla de **Guía de accesibilidad** ya no aparece encima de `Cerrar expediente`;
- Multijugador se titula **Puestos de acceso** y fija `JobsDosh.exaroton.me:56477` como **Servidor oficial de Jobs** / **Jobs Official Server** en el primer renglón;
- Español (Uruguay), Argentina, Chile, Ecuador, México y Venezuela reutilizan el catálogo `es_es` para no mezclar inglés con español;
- `PantallaMundosJobs` conserva `SelectWorldScreen`, mueve el buscador y presenta **Archivo de turnos** sin hoja gigante;
- `PantallaModsJobs` conserva Forge Mods y tiñe el blanco duro hacia tinta sepia;
- Resource Packs usa archivo oscuro sin alterar sus dos listas;
- Idioma se renderiza una sola vez y su scrollbar usa los límites reales `y0/y1`;
- Sonido, Video, Chat, Accesibilidad, Online, Mouse y Teclas adoptan geometría compacta compartida;
- el sistema de música local que generaba `jobsmenu-musica-activa` se elimina y migra limpiando el pack legado;
- el pie reserva la esquina derecha para overlays externos y usa el formulario localizado completo cuando el ancho lo permite.

## 5. Pantallas vigentes

Familia Jobs propia o tematizada:

- Title / `PantallaNivel`;
- Pausa / `PantallaEstancia`;
- Options;
- Config Jobs;
- Multijugador;
- Seleccionar mundo / `PantallaMundosJobs`;
- Mods / `PantallaModsJobs`;
- Controles;
- Mouse;
- Teclas;
- Idioma;
- Piel;
- Sonido;
- Video;
- Chat;
- Accesibilidad;
- Online;
- Resource Packs.

Embeddium conserva su propia pantalla de vídeo cuando está presente. Jobs no reconstruye internamente su UI por reflection profunda.

## 6. Sonido

El mod usa sus propios gestos de interfaz y no debe mezclar el click vanilla sobre widgets propios.

- UI: pasar, elegir, confirmar, volver, alternar, abrir, cerrar y negado.
- Ambiente: BASE + CARÁCTER + ACTIVIDAD por nivel.
- Eventos: ocasionales, ponderados y con silencios deliberados.
- Música: independiente de las camas del recinto.

Todo audio empaquetado debe ser mono. Un sonido nuevo necesita una función identificable; cantidad no equivale a calidad.

## 7. Accesibilidad / rendimiento

Controles vigentes incluyen:

- movimiento reducido;
- destellos reducidos;
- alto contraste;
- texto grande;
- papel limpio;
- guía de lectura;
- bajo consumo;
- perfil accesible.

Una interfaz nueva no puede saltarse estas preferencias. Los PNG 10–17 permanecen estáticos independientemente de ellas.

## 8. Pruebas mínimas antes de una entrega

Además del CI:

- GUI scale 2, 3 y 4;
- ES, EN y Español (Uruguay);
- Title → Options → Config Jobs → cinco categorías → volver;
- **Mods → Jobs Menu → Config** → misma pantalla;
- botón Config claramente visible y separado de opciones Minecraft;
- botones/toggles/sliders sin solapes en 854×480 y ventanas estrechas;
- tabs de Config con mouse, Tab, Enter, Espacio y Escape;
- Direct Connect / Add Server / confirmaciones con piel Jobs pero lógica intacta;
- Accesibilidad: primera/última fila, scrollbar, ausencia de la Guía vanilla superpuesta y Volver;
- scrollbar Jobs: rueda, click, drag y fallback;
- Multijugador: seleccionar/directo/agregar/editar/borrar/refrescar;
- Seleccionar mundo: previews, selección, crear/editar/borrar/recrear y volver;
- Mods: búsqueda, orden, Config, información y abrir carpeta;
- cambio de idioma + recarga de recursos;
- Resource Packs sin dirt aislado y con selección/aplicación intactas;
- pause in-world → Options → volver;
- Embeddium presente y ausente;
- movimiento reducido / destellos reducidos / bajo consumo;
- niveles 10–17: ningún zoom, paneo, niebla móvil, flicker, scanline animada, motas o presencia visual;
- 18 niveles y transición entre fondos.

## 9. Documentación vigente

- `README.md`: resumen de 0.16.1.
- `CHANGELOG.md`: historial.
- `KNOWN_ISSUES.md`: pruebas/riesgos pendientes.
- `docs/AUDITORIA_0.15.0_UI_POLISH.md`: auditoría del pase compacto y de archivos.
- `docs/AUDITORIA_0.16.0_64_MEJORAS.md`: inventario del pase profesional.
- `docs/AUDITORIA_0.16.1_CAPTURAS.md`: correcciones verificadas contra capturas reales.
- `docs/AUDITORIA_0.14.0_UI.md`: arquitectura previa conservada como registro.
- `docs/DESPLIEGUE.md`: instalación.
- `docs/compatibilidad.md`: convivencia con otros mods.
- `docs/checklist-manual.md`: verificación dentro de Minecraft.

Si un documento histórico contradice este archivo en versión, rama, cantidad de niveles, carácter estático de los PNG 10–17 o procedimiento de entrega, **manda CONTEXTO**.
