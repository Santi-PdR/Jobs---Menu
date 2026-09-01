# Registro de cambios

## 0.16.0 — Interfaz profesional y respuesta inmediata — 2026-09-01

### Sistema visual compartido

- Nuevo pulido global con márgenes seguros, foco inequívoco de ratón/teclado, entrada corta y confirmación no modal al cambiar opciones.
- Botones, toggles, sliders, campos vanilla y barras de lista reciben jerarquía, estados claros y respuesta visual consistente sin alterar sus hitboxes.
- Las transiciones y microanimaciones respetan movimiento reducido y bajo consumo.

### Navegación y pantallas

- Idioma incorpora búsqueda por nombre/código, contador, códigos visibles, `Ctrl+F`, limpieza con `Esc` y toggle Unicode real; conserva una sola lista y una sola barra.
- Ajustes de aviso identifica categoría, subraya la activa, muestra página y permite navegar con `1`–`5` o flechas.
- Multiplayer fija y protege `JobsDosh.exaroton.me:56477`, lo traduce, marca como fijado, añade ayuda a cada acción y refresca con `F5`.
- Singleplayer enfoca la búsqueda con `Ctrl+F` y la limpia con `Esc` sin perder la lógica vanilla.
- Mods y Resource Packs usan archivo oscuro, rótulos compactos y tipografía entintada; los recursos propios ya no aparecen como un pack redundante.

### Entrega

- Paridad completa `es_es` / `en_us` para todos los textos y ayudas nuevos.
- Auditoría trazable de 64 mejoras en `docs/AUDITORIA_0.16.0_64_MEJORAS.md`.
- Versión y artefacto: **0.16.0** / **`jobsmenu-0.16.0.jar`**.

## 0.15.0 — Archivos compactos y navegación profesional — 2026-09-01

### Interfaz

- Opciones y Ajustes del aviso reducen su superficie y eliminan la nota que se solapaba con la sección Minecraft.
- Tabs, toggles y sliders de Jobs reciben tooltips localizados; toggles incorporan confirmación visual de presión.
- Idioma deja de renderizar la lista dos veces y la scrollbar usa los límites verticales reales.
- Sonido, Video, Chat, Accesibilidad, Online, Mouse y Teclas comparten geometría compacta.
- Se añade archivo oscuro para contenido extenso, evitando usar papel como fondo universal.

### Mundos, servidores, mods y recursos

- Singleplayer pasa a **Archivo de turnos** y conserva previews, búsqueda y acciones vanilla.
- Multiplayer pasa a **Puestos de acceso** y mantiene `JobsDosh.exaroton.me:56477` como servidor oficial traducido, guardado y primero en la lista.
- Mods conserva búsqueda, orden, Config, carpeta, logos y panel de información, con tinta sepia en lugar de blanco puro.
- Resource Packs conserva sus dos listas sin dirt ni hoja gigante.

### Música y migración

- Se elimina `MusicaPropia` y la generación de `jobsmenu-musica-activa`.
- `LimpiezaRecursosLegados` desactiva y borra exclusivamente ese pack legado; la pista incluida permanece dentro del JAR.

### Entrega

- Versión y artefacto: **0.15.0** / **`jobsmenu-0.15.0.jar`**.

## 0.14.1 — Pulido visual e integración de interfaces — 2026-09-01

### Correcciones visibles

- Accesibilidad oculta/desactiva la **Guía de accesibilidad** vanilla cuando Jobs ya usa su propio cierre, eliminando el solape observado con `Cerrar expediente`.
- Multijugador separa título, subtítulo y nota contextual; la lista empieza debajo de esas tres capas.
- El pie de `ChromeExpediente` reserva espacio para overlays de terceros y usa el formulario localizado completo cuando el ancho lo permite.
- Las hojas grandes ganan reglas/pliegues estáticos muy tenues para reducir el vacío visual sin recargar la interfaz.

### Idioma y pantallas preservadas

- Español (Uruguay), Argentina, Chile, Ecuador, México y Venezuela reutilizan el catálogo `es_es`, evitando mezclas español/inglés en botones, títulos y subtítulos Jobs.
- Nuevo `PantallaMundosJobs`: conserva `SelectWorldScreen` y toda su lógica, pero integra el selector de mundos en el papel/chrome Jobs.
- Nuevo `PantallaModsJobs`: conserva `ModListScreen` de Forge con búsqueda, logos, Config, información y carpeta de mods, pero evita que se vea como una pantalla aislada.
- Resource Packs elimina el bloque visual de dirt que rompía continuidad durante una visita Jobs.
- `ListasExpediente` incorpora un fallback más seguro cuando no puede obtener todos los campos internos de una lista.

### Limpieza y entrega

- `ChromeExpediente` vuelve a usar la clave localizada `jobsmenu.interfaz.formulario`, eliminando el aviso pendiente del verificador estático sin añadir texto duro a Java.
- README, CONTEXTO, riesgos, compatibilidad, checklist y despliegue se sincronizan con el estado real.
- La versión sube a **0.14.1**.
- Artefacto obligatorio: **`jobsmenu-0.14.1.jar`**.
- El PowerShell se entrega únicamente después de PR verde, merge a `main`, build verde en `main` y actualización de `dev-latest`.

---

## 0.14.0 — Profesionalización del sistema de interfaz — 2026-09-01

### Options y Config

- `PantallaOpcionesJobs` separa explícitamente la configuración **Jobs** de las opciones generales de Minecraft.
- Config del mod pasa a ser una acción principal de ancho completo y utiliza el nuevo tipo visual `BotonExpediente.Tipo.JOBS`.
- El acceso estándar **Mods → Jobs Menu → Config** sigue abriendo exactamente la misma configuración.
- `PantallaAjustesAviso` se reescribe por completo: deja de usar `OptionsList`/`OptionInstance` como superficie visual y pasa a cinco categorías propias (visual, Nivel, audio, accesibilidad y sistema).
- Los controles del mod quedan conectados directamente a los getters/setters reales de `ConfigTurno`; no se crea un estado visual duplicado.

### Widgets de segunda generación

- `BotonExpediente` gana jerarquía JOBS, marca administrativa, sombra diferenciada, foco progresivo y respuesta de presión.
- `SliderExpediente` incorpora escala, marcas de lectura, tirador de papel/tinta y foco propio.
- `ToggleExpediente` separa casilla, etiqueta y valor en una cápsula de estado en vez de parecer un botón Sí/No.
- Las microinteracciones de foco se simplifican cuando está activo movimiento reducido.

### Chrome y composición

- `ChromeExpediente` recibe sombra en dos planos, pestaña de archivador, borde secundario y perforaciones más legibles.
- Cabeceras con reglas laterales y truncado seguro de títulos/subtítulos.
- Nuevo `ChromeExpediente.seccion` para crear jerarquía interna consistente entre hubs.
- Vignette estática alrededor del documento para separar la interfaz del recinto sin animar los PNG suministrados.
- Banda contextual externa más sólida y coherente con el resto del expediente.

### Diálogos vanilla auxiliares

- Nueva `PielVanillaJobs`: tematiza visualmente botones y campos de texto de pantallas `net.minecraft.*` durante una sesión Jobs.
- La capa se dibuja después del render vanilla y conserva hitboxes, listeners, foco, validaciones y lógica original.
- Sirve para Direct Connect, Add Server, confirmaciones y otros diálogos secundarios que no merece la pena reimplementar.
- Las pantallas de otros mods no reciben esta piel de controles; conservan su implementación y sólo pueden recibir contexto mínimo.

### Scrollbar y transición

- La scrollbar Jobs gana canaleta, topes, marcas de recorrido y agarres internos conservando rueda/click/drag de Minecraft.
- `TransicionInterfazJobs` incorpora sombra de carpeta, doble fibra de papel y marcas de archivo.
- Movimiento reducido sigue reemplazando el gesto por un fade corto.

### Fondos 10–17

- Se mantiene sin excepción la regla de 0.13.0: los PNG 10–17 son estáticos.
- El nuevo chrome puede dibujar capas estáticas de interfaz alrededor del expediente, pero no añade zoom, paneo, flicker, niebla móvil ni otros movimientos al PNG.

### Entrega

- La versión sube a **0.14.0**.
- Artefacto obligatorio: **`jobsmenu-0.14.0.jar`**.
- README y CONTEXTO se actualizan al nuevo centro de control y a la Config Jobs completamente propia.

---

## 0.13.0 — PNG estáticos y pulido de controles — 2026-08-31

### Fondos 10–17

- Por requisito del proyecto, los PNG 10–17 dejan de recibir cualquier animación visual propia.
- `PlantaImagen` elimina zoom, paneo, scanline, flicker, niebla móvil y efectos particulares por escena; el recorte pasa a ser un cover centrado y estable.
- `EscenaNivel` identifica `PlantaImagen` y no ejecuta sobre esos niveles las capas animadas de materiales, tratamiento, dirección artística, presencia, motas, eventos ni pulido de cámara.
- Se conserva únicamente una integración estática mínima y los apagones/transiciones generales del menú.
- La validación fuerte PNG/CRC/IDAT y el fallback con `NativeImage` continúan vigentes.

### Configuración

- Se restaura el acceso estándar de Forge **Mods → Jobs Menu → Config** mediante `ConfigScreenHandler.ConfigScreenFactory`.
- Ese botón abre la misma `PantallaAjustesAviso` utilizada desde el hub Jobs, sin duplicar configuraciones.

### Scrollbar y listas

- `ListasExpediente` incorpora una scrollbar visual propia de Jobs sobre las listas vanilla.
- Se conservan rueda, click, drag, posición y cantidad de scroll de Minecraft; sólo cambia la presentación.
- El carril y tirador usan papel/tinta y evitan el rojo genérico, reservado a Executores.
- La integración usa reflection defensiva y vuelve a la scrollbar vanilla si una pantalla externa cambia internamente la lista.

### Layout y solapes

- `EscuchaCliente` desactiva todos los botones `Done` vanilla duplicados dentro de pantallas Jobs para evitar hitboxes invisibles superpuestos.
- Accesibilidad reserva más espacio entre cabecera, última fila, scrollbar y botón Volver.
- `PantallaAjustesAviso` reserva un footer completo para navegación y metadatos.
- `ChromeExpediente` divide el pie en extremos y deja libre el centro para `Cerrar expediente`.
- `PantallaOpcionesJobs` valida el espacio disponible para el slider FOV; en ventanas extremadamente pequeñas lo omite antes que crear un solape.

### Entrega

- La versión sube a **0.13.0**.
- El artefacto obligatorio es **`jobsmenu-0.13.0.jar`**.
- README y CONTEXTO documentan como regla permanente que los PNG 10–17 son estáticos.

---

## 0.12.0 — Familia de interfaces Jobs — 2026-08-31

### Dirección de interfaz

- Las pantallas que todavía se sentían ajenas al mod dejan de depender visualmente del gris/dirt de Minecraft y pasan a compartir un lenguaje de **expediente administrativo**: papel fotocopiado, tinta, archivo, fluorescente y el Nivel vigente detrás.
- `GripeVerde` se usó únicamente como referencia de arquitectura de UI: conservar lógica vanilla cuando aporta compatibilidad y reimplementar sólo los hubs donde Jobs necesita una jerarquía propia. No se trasladó su tema victoriano.
- El rojo sigue reservado a Executores. Incluso las acciones terminales usan tinta y peso visual, no color de alarma.

### Hub de opciones y navegación

- Nuevo `PantallaOpcionesJobs`: reemplaza el salto al hub vanilla por una mesa de configuración coherente con el resto del aviso.
- Accesos integrados a Piel, Sonido, Video, Controles, Idioma, Chat, Resource Packs, Accesibilidad, Online y ajustes propios de Jobs.
- FOV pasa a un `SliderExpediente` propio sin duplicar el valor de Minecraft.
- El layout del hub ahora tiene modo compacto para ventanas con poco ancho/alto lógico, evitando cruces entre botones, FOV y Volver.
- La pausa abre directamente el hub Jobs; no depende de una redirección del menú principal que, por diseño, no existe mientras hay un mundo cargado.

### Pantallas vanilla tematizadas sin perder lógica

- Nuevas envolturas Jobs para Sonido, Video, Chat, Accesibilidad, Mouse, Teclas, Online y selección de Resource Packs.
- Las listas vanilla conservan su ancho y comportamiento interno; Jobs sólo retira fondo/bandas y reserva espacio vertical para cabecera y pie. Esto evita romper columnas, hitboxes o widgets inyectados por otras pantallas.
- Los botones `Done` vanilla sustituidos por navegación Jobs quedan también inactivos, no sólo invisibles.
- Accesibilidad incorpora al final de la lista de Minecraft las ayudas propias del mod: movimiento reducido, destellos reducidos, alto contraste y texto grande.
- Los controles de Agacharse/Correr conservan la semántica vanilla **Mantener/Alternar**, en vez de mostrarse como un simple Sí/No.

### Interfaces propias

- Nuevo `PantallaPielJobs`: ficha de identificación para capas del modelo y mano principal.
- Nuevo `PantallaIdiomaJobs`: lista real de idiomas, selección pendiente, doble clic para aplicar, fuente Unicode y recarga de recursos sin abandonar el flujo Jobs.
- Nuevo `PantallaControlesJobs`: hub de Mouse/Teclas y controles booleanos frecuentes.
- Nuevo `PantallaMultijugadorJobs`: conserva `ServerSelectionList`, ping, MOTD, LAN y acciones vanilla, pero sustituye el marco y la superficie de interacción por Jobs.
- Los diálogos externos o de otros mods que no se reemplazan pueden recibir una banda contextual mínima en vez de ser forzados a una copia incompleta.

### Sistema compartido de UI

- Nuevo `ChromeExpediente`: fondo vivo, hoja, doble borde, marcas de archivador, cabeceras, divisores, pie de formulario, Nivel actual y versión runtime.
- Nuevo `BotonExpediente`: estados normal/foco/pulsado/deshabilitado, navegación por teclado, narración, elipsis segura y gestos sonoros del mod.
- Nuevo `SliderExpediente`: control entero con papel/tinta y feedback auditivo limitado mientras se arrastra.
- Nuevo `ToggleExpediente`: enlaza getters/setters reales y admite etiquetas semánticas personalizadas.
- Nuevo `ListasExpediente`: estilización defensiva de listas vanilla mediante reflection acotada y fallback seguro.
- Nuevo `TransicionInterfazJobs`: transición breve entre expedientes; con movimiento reducido se simplifica para no añadir animación innecesaria.

### Compatibilidad y seguridad

- `EscuchaCliente` sólo sustituye clases vanilla exactas en los puntos de entrada previstos; una subclase de otro mod no se pisa de forma indiscriminada.
- Embeddium conserva su propia pantalla de vídeo cuando está presente y recibe únicamente contexto visual Jobs.
- El audio ambiental y la música mantienen continuidad entre pantallas de una misma visita.
- El cambio de idioma continúa pasando por el sistema real de recursos de Minecraft y los gestores de audio ya existentes se recuperan mediante el listener de recarga.

### Entrega

- La versión sube a **0.12.0**.
- El artefacto exigido es **`jobsmenu-0.12.0.jar`**.
- ES/EN incorporan las nuevas claves de interfaz con paridad verificada.
- README, CONTEXTO, riesgos y auditoría se actualizan al nuevo sistema de interfaces.

---

## 0.11.0 — Pulido profesional y entregas versionadas — 2026-08-31

### Entrega y versión

- La versión sube a **0.11.0**.
- Nueva regla dura del proyecto: **todo JAR entregado, publicado e instalado debe llevar la versión en el nombre**.
- GitHub Actions deja de renombrar el build a `jobsmenu-latest.jar` y publica `jobsmenu-0.11.0.jar` dentro de la release rodante `dev-latest`.
- Nuevo `tools/verificar_version.py`: el CI falla si falta `mod_version`, no parece SemVer o el workflow vuelve a publicar un nombre genérico sin versión.
- El PowerShell de despliegue ya no depende de una URL con nombre fijo: consulta `dev-latest`, encuentra el único `jobsmenu-<version>.jar`, lo descarga y valida antes de sustituir la copia instalada.

### Escena y transiciones

- Nuevo `PulidoEscena`: capa común para los **18 niveles** que integra recintos procedurales y fondos de imagen bajo un mismo lenguaje de cámara/instalación.
- Halo residual de fluorescente muy tenue para reforzar volumen y fuente de luz.
- Barrido de exposición lento que evita que la escena se sienta completamente estática sin convertirse en efecto CRT.
- La transición de Nivel gana masa visual: durante el apagado la oscuridad entra desde los bordes y, al volver la corriente, una banda de luz atraviesa el encuadre antes de estabilizarse.
- La Suspensión recibe un borde opresivo estable y sin flashes adicionales.
- Todos estos efectos respetan `escena_viva`, `movimiento_reducido`, `destellos_reducidos`, `bajo_consumo` y el estado de Suspensión.

### Fondos 10–17

- Se conserva la reparación previa de los PNG 10, 13 y 17 y la validación fuerte de los ocho fondos de imagen.
- `PlantaImagen` continúa verificando cada recurso con `NativeImage`, obtiene sus dimensiones reales y usa fallback procedural si un PNG no puede decodificarse.
- Los fondos de imagen mantienen zoom/paneo cinematográfico, vignette, respiración de luz y efectos particulares por escena.

### Interfaz

- `HojaPapel` gana sombra en dos planos para separarse del fondo sin leerse como tarjeta flotante.
- El papel usa un degradado vertical mínimo según iluminación en vez de un relleno totalmente plano.
- Se añadieron marcas de fotocopia deterministas y desgaste mínimo de borde; `papel_limpio` sigue eliminando ese ruido decorativo.
- La cinta recibe highlight y sombra propios para leerse como material separado del papel.
- Se mantienen hitboxes, navegación por teclado, narración y jerarquía de `RenglonTablon` sin introducir controles visuales nuevos innecesarios.

### Audio

- Las camas **BASE** y **CARÁCTER** incorporan una microderiva tonal de ciclo largo para que un bucle continuo no vuelva exactamente igual en cada pasada.
- La deriva es deliberadamente mínima y no pretende sonar a pitch shifting.
- **ACTIVIDAD** conserva tono estable para no deformar objetos reconocibles como metal, piedra, agua o madera.
- Se mantienen silencios deliberados, eventos ponderados, ducking por presencia y comportamiento distinto durante apagones.

### Documentación y auditoría

- `CONTEXTO.md` reescrito como documento maestro vigente: 0.11.0, 18 niveles, pipeline real, política de versión, fondos 10–17, accesibilidad y reglas de calidad.
- `README.md` actualizado al estado de 0.11.0.
- `KNOWN_ISSUES.md` deja atrás incidentes históricos y pasa a contener sólo pruebas manuales y riesgos actuales.
- `docs/DESPLIEGUE.md` actualizado al JAR versionado.
- Nuevo `docs/AUDITORIA_0.11.0.md` con diagnóstico, cambios aplicados, decisiones de no hacer y checklist de prueba real.

### Criterio de esta evolución

No se añadieron efectos simplemente porque fueran posibles. Se descartó postprocesado pesado, flashes nuevos, una segunda interfaz de opciones y sonidos redundantes. Cada cambio de 0.11.0 debía mejorar al menos lectura, material, profundidad, continuidad, identidad, sonido, accesibilidad, rendimiento o seguridad de entrega.

---

## 0.10.x — Integración de niveles 10–17 — 2026-08-30 / 2026-08-31

- Integrados ocho fondos suministrados como niveles 10–17.
- Ampliado `nivel_fijo` y la rotación a 18 niveles.
- Corregidos PNG dañados que podían producir la textura morado/negro de Minecraft.
- `tools/verificar_fondos.py` endurecido hasta validar CRC, IDAT, descompresión y flujo de píxeles.
- `PlantaImagen` migrado a validación runtime con `NativeImage` y fallback procedural.
- Añadido movimiento lento y efectos ambientales específicos a los fondos 10–17.
- Audio de los nuevos niveles integrado mediante combinación de camas existentes con mezcla, densidad y timing propios.
- GitHub Actions consolidado como build oficial Java 17 y publicación de `dev-latest`.

---

## 0.10.0 — Evolución 6 — 2026-08-29

- Configuración ampliada con duración de estancia, bajo consumo y perfil accesible.
- Continuidad de ambiente por visita al menú.
- Salto manual de Nivel con F y mute maestro con M.
- Dirección artística individual de los diez recintos procedurales.
- Trono rediseñado y múltiples mejoras de materiales, iluminación, rendimiento y lifecycle.
- Primer build y despliegue real con Java 17 completados después de corregir incompatibilidades de API y endurecer el flujo PowerShell.

---

## 0.9.0 y anteriores — Resumen histórico

Las versiones anteriores construyeron progresivamente la base del mod:

- esqueleto Forge y configuración cliente;
- menú principal diegético;
- cuatro y luego diez recintos procedurales;
- transición por apagado eléctrico;
- pausa tematizada;
- ajustes integrados en Opciones de Minecraft;
- audio por capas y eventos ambientales;
- música con lifecycle propio;
- presencia de fondo sin jumpscare;
- cuenta regresiva de ronda;
- La Suspensión;
- accesibilidad, layout responsivo y herramientas de auditoría.

El detalle histórico completo de decisiones, problemas y experimentos se conserva en los documentos de `docs/` (`EVOLUCION_*`, `AUDITORIA_*`, informes y propuestas). Esos archivos son registro histórico; para el estado vigente mandan `CONTEXTO.md`, `README.md`, `KNOWN_ISSUES.md` y este changelog.
