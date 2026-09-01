# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1 que reemplaza el flujo de menús por la interfaz del servidor **Jobs**: expedientes, avisos administrativos y recintos que continúan detrás de la interfaz.

La salida existe. Cuesta. Los **Executores** vuelven. El objetivo no es aplicar una skin sobre Minecraft: el menú debe sentirse como otra dependencia de la instalación.

| | |
|---|---|
| Versión | **0.16.2** |
| Artefacto | **`jobsmenu-0.16.2.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **18 (0–17)** |

## 0.16.2 · Corrección estructural de interfaz y sesión

0.16.2 elimina el filtro amarillo global y usa una paleta neutra de hueso frío,
grafito y gris verdoso. Las cabeceras custom se dibujan en un plano frontal
opaco para ocultar realmente los títulos vanilla, Idioma usa un buscador
centrado sin sombra y Options deja de imprimir rótulos residuales bajo Config.

Multiplayer conserva un único registro para `JobsDosh.exaroton.me:56477`,
deduplica su IP y elimina el acceso legado `Ghoul Outbreak`. Al entrar a un
mundo o servidor, música y ambiente se detienen inmediatamente; al salir, el
flujo vuelve al menú Jobs incluso tras kick, desconexión o pausa vanilla.

## 0.16.1 · Corrección visual basada en capturas reales

0.16.1 corrige los fallos observados dentro del juego: la lista de Idioma vuelve a renderizarse, los títulos vanilla dejan de sangrar bajo los encabezados Jobs, Ajustes usa pestañas cortas sin indicadores superpuestos, las vistas oscuras abandonan el blanco saturado y el limpiador retira tanto `jobsmenu-musica` como el nombre legado anterior.

### Correcciones del pase visual

- Accesibilidad deja de mostrar el botón vanilla **Guía de accesibilidad** cuando Jobs ya proporciona su propia navegación inferior; no vuelve a solaparse con `Cerrar expediente`.
- Multijugador usa la cabecera **Puestos de acceso** y mantiene `JobsDosh.exaroton.me:56477` guardado, traducido y en el primer renglón.
- Español (Uruguay) y las variantes españolas soportadas reutilizan el archivo `es_es`, evitando mezclas como `Close file`, `Notice settings` o subtítulos en inglés.
- Seleccionar mundo pasa a **Archivo de turnos** y Multiplayer a **Puestos de acceso**, ambos sobre un archivo oscuro con margen completo.
- Mods conserva búsqueda, orden, Config, logos y carpeta, pero transforma el blanco puro de Forge en tinta sepia.
- Resource Packs elimina el papel gigante y el dirt aislado; conserva sus dos listas y acciones vanilla.
- Idioma deja de dibujar su lista dos veces. La scrollbar Jobs usa `y0/y1` reales; ya no desaparece ni deja asomar la barra gris al subir.
- Sonido, Video, Chat, Accesibilidad, Online, Mouse y Teclas usan paneles compactos con contenido ceñido y márgenes visibles.
- Cada tab, toggle y slider de **Ajustes del aviso** expone una explicación localizada mediante tooltip.
- El pack generado `jobsmenu-musica-activa` se retira al migrar: la música incluida vive únicamente dentro del mod.
- El pie reserva espacio para badges y overlays de otros mods y, cuando el ancho lo permite, usa el formulario localizado completo.

### Options ahora separa Jobs de Minecraft

`PantallaOpcionesJobs` deja de mezclar la configuración del mod como un botón más del grid.

- **Config de Jobs** es ahora la acción principal de ancho completo y usa una jerarquía visual exclusiva.
- Las opciones de Minecraft quedan agrupadas debajo como una segunda sección.
- FOV se integra en la última fila cuando existe espacio real.
- El layout sigue degradando de forma segura en ventanas pequeñas antes de permitir solapes.
- El acceso de Forge **Mods → Jobs Menu → Config** continúa disponible y abre exactamente el mismo sistema.

### Config del mod completamente propia

`PantallaAjustesAviso` ya no depende visualmente de `OptionsList` ni de controles vanilla y reduce su superficie máxima a un expediente de 480×300.

La configuración se reorganiza como un expediente Jobs con cinco áreas:

- visual;
- Nivel;
- audio;
- accesibilidad;
- sistema.

Cada área usa `BotonExpediente`, `ToggleExpediente` y `SliderExpediente` conectados directamente con `ConfigTurno`. No existe un segundo estado de configuración ni una copia decorativa de los valores.

Todos los controles muestran su explicación al mantener el puntero encima. Los toggles también incorporan confirmación visual breve al pulsarlos.

Esto reemplaza uno de los últimos bloques grandes de widgets vanilla dentro del propio mod.

### Widgets de segunda generación

- `BotonExpediente` gana una jerarquía **JOBS** para acciones propias del mod, microinteracción de foco, respuesta de presión y marcas de expediente.
- `SliderExpediente` incorpora escala, marcas de lectura, tirador de tinta/papel y foco progresivo.
- `ToggleExpediente` separa etiqueta y estado en una cápsula administrativa en vez de imitar un botón Sí/No.
- Todos respetan movimiento reducido cuando una animación de foco no aporta información necesaria.

### Chrome de expediente más profundo

`ChromeExpediente` incorpora:

- sombra en dos planos;
- pestana de archivador;
- borde secundario;
- cabeceras con reglas laterales;
- rótulos de sección reutilizables;
- elipsis segura en títulos/subtítulos;
- vignette de interfaz para separar escena y documento;
- banda contextual más sólida en pantallas auxiliares.

El ruido visual añadido por el chrome es **estático** y se desactiva cuando corresponde por papel limpio/bajo consumo.

### Diálogos vanilla auxiliares tematizados

`PielVanillaJobs` conserva lógica y hitboxes originales en diálogos vanilla que conviene mantener por compatibilidad —por ejemplo conexión directa, añadir servidor o confirmaciones— y les aplica una capa de papel/tinta Jobs después del render de Minecraft.

Las pantallas de terceros no reciben esta sustitución de controles: mantienen su implementación y sólo pueden recibir contexto visual mínimo.

### Scrollbar y transiciones

- La scrollbar Jobs mantiene rueda/click/drag de Minecraft, pero usa canaleta, topes, marcas de recorrido y tirador de expediente cuando puede obtener los datos reales de la lista.
- Si una lista externa no expone esos datos, el mod prioriza una presentación utilizable antes que romper la pantalla.
- `TransicionInterfazJobs` usa sombra de carpeta, doble fibra de papel y marcas de archivo.
- Movimiento reducido sigue convirtiendo la transición en un fade breve sin desplazamiento obligatorio.

## Fondos PNG 10–17: estáticos por diseño

Los ocho PNG suministrados para los niveles **10–17 no se animan**.

No reciben zoom, paneo, parallax, respiración, scanlines animadas, flicker, niebla móvil, motas, presencia ni tratamientos globales que muevan la imagen. `PlantaImagen` hace un cover centrado estable y una integración fija mínima.

Los apagones/cambios de Nivel continúan porque pertenecen al flujo del menú, no a la animación del PNG. Los niveles 0–9 siguen siendo recintos procedurales vivos.

Los PNG 10–17 siguen pasando firma PNG, CRC, IDAT, descompresión y dimensiones en CI, y `NativeImage` vuelve a validarlos en runtime. Un recurso roto cae a fallback seguro, nunca se acepta la textura morado/negro como resultado final.

## Familia de interfaces Jobs

Además de Options y Config, permanecen tematizados:

- Multijugador;
- Seleccionar mundo;
- Mods / Forge;
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
- Resource Packs;
- Pausa.

Se conserva lógica vanilla cuando aporta compatibilidad y se reimplementa la superficie cuando Jobs necesita una jerarquía propia. Las redirecciones automáticas importantes usan **clase exacta** para no barrer subclases de otros mods.

## Audio

- Tres camas ambientales por recinto: BASE, CARÁCTER y ACTIVIDAD.
- Eventos ocasionales con silencios deliberados.
- Música de menú independiente y mezcla contextual.
- La pista se carga desde `assets/jobsmenu` dentro del JAR; Jobs no crea resource packs musicales auxiliares.
- Gestos propios para foco, selección, alternancia, confirmación, apertura, cierre y rechazo.
- BASE/CARÁCTER usan microvariación tonal de ciclo largo; ACTIVIDAD permanece estable para no deformar materiales reconocibles.

## Regla obligatoria de versión

**Todo JAR entregado debe incluir la versión en el nombre.**

Correcto:

```text
jobsmenu-0.16.2.jar
```

Prohibido:

```text
jobsmenu-latest.jar
```

`gradle.properties` es la fuente de verdad. `tools/verificar_version.py` y GitHub Actions hacen cumplir la regla y limpian assets obsoletos de `dev-latest`.

## Build y entrega

GitHub Actions ejecuta:

1. Java 17.
2. `tools/verificar_version.py`.
3. `tools/verificar_fondos.py`.
4. `tools/verificar.py`.
5. `./gradlew build --stacktrace --no-daemon`.
6. Publicación de **`jobsmenu-0.16.2.jar`** en `dev-latest` sólo desde `main`.

El PowerShell de despliegue se entrega **después** de que el PR y `main` terminen en verde. El usuario no necesita compilar localmente: el script consume el JAR ya construido por CI y lo instala únicamente en `test-1`.

## Documentación

- [`CONTEXTO.md`](CONTEXTO.md): contrato vigente del proyecto.
- [`CHANGELOG.md`](CHANGELOG.md): historial de cambios.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos y pruebas pendientes.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): instalación del build versionado.
- [`docs/AUDITORIA_0.15.0_UI_POLISH.md`](docs/AUDITORIA_0.15.0_UI_POLISH.md): auditoría del pase basado en capturas.
- [`docs/AUDITORIA_0.16.0_64_MEJORAS.md`](docs/AUDITORIA_0.16.0_64_MEJORAS.md): inventario verificable del pase profesional.
- [`docs/AUDITORIA_0.16.1_CAPTURAS.md`](docs/AUDITORIA_0.16.1_CAPTURAS.md): correcciones derivadas de la prueba dentro del juego.
- [`docs/AUDITORIA_0.16.2_SESION_UI.md`](docs/AUDITORIA_0.16.2_SESION_UI.md): contratos de color, retorno y audio del pase actual.
- [`docs/AUDITORIA_0.14.1_UI_POLISH.md`](docs/AUDITORIA_0.14.1_UI_POLISH.md): pase visual anterior.
- [`docs/AUDITORIA_0.14.0_UI.md`](docs/AUDITORIA_0.14.0_UI.md): auditoría de la arquitectura anterior.
- [`docs/DIRECCION_ARTISTICA.md`](docs/DIRECCION_ARTISTICA.md): lenguaje visual.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): convivencia con otros mods.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): prueba dentro de Minecraft.

El historial largo de evoluciones anteriores sigue en `docs/`; README y CONTEXTO describen siempre el estado vigente.
