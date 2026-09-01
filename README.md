# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1 que reemplaza el flujo de menús por la interfaz del servidor **Jobs**: expedientes, avisos administrativos y recintos que continúan detrás de la interfaz.

La salida existe. Cuesta. Los **Executores** vuelven. El menú no intenta parecer una skin puesta encima de Minecraft: intenta sentirse como otra parte de la instalación.

| | |
|---|---|
| Versión | **0.13.0** |
| Artefacto | **`jobsmenu-0.13.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **18 (0–17)** |

## 0.13.0 · Estabilidad visual y controles

Esta versión consolida la familia de interfaces creada en 0.12.0 y corrige varios detalles que todavía podían sentirse poco integrados o provocar cruces de elementos en escalas de GUI exigentes.

### Fondos PNG 10–17: estáticos por diseño

Por requisito del proyecto, los ocho PNG suministrados para los niveles **10–17** ya no reciben animaciones.

Se eliminaron de esos fondos:

- zoom y paneo;
- parallax o respiración de cámara;
- scanlines y barridos;
- flicker propio;
- niebla móvil;
- partículas, motas y presencia superpuestas;
- tratamientos globales animados que pudieran hacer parecer que la imagen se mueve.

`PlantaImagen` hace ahora un **cover centrado y estable**. Sólo conserva una integración estática muy leve y sigue participando de apagones/cambios de Nivel porque esos efectos pertenecen al flujo general del menú, no a la animación del PNG.

Los niveles 0–9 continúan siendo recintos procedurales vivos.

Los PNG 10–17 siguen verificándose antes de compilar: firma PNG, CRC, IDAT, descompresión y dimensiones. En runtime `NativeImage` vuelve a comprobar cada recurso. Un PNG inválido cae a un fallback seguro en vez de mostrar la textura morado/negro.

### Configuración recuperada

El mod vuelve a registrar una pantalla de configuración de Forge. Por tanto debe existir de nuevo:

**Mods → Jobs Menu → Config**

Ese acceso abre la misma `PantallaAjustesAviso` utilizada desde el hub Jobs, evitando dos sistemas de configuración diferentes.

### Scrollbar Jobs

Las listas vanilla conservan toda su lógica real de Minecraft —rueda, click, drag, posición y cantidad de scroll— pero la barra gris original queda sustituida visualmente por una barra propia de Jobs:

- carril fino de archivador;
- papel e tinta;
- tirador proporcional al contenido;
- sin rojo genérico, que continúa reservado a Executores;
- fallback seguro a la scrollbar vanilla si una modificación externa impide localizar la lista.

### Layout y solapes

- Accesibilidad reserva más espacio entre cabecera, lista y botón de cierre.
- Los botones `Done` vanilla duplicados quedan ocultos **e inactivos** de forma global en las pantallas Jobs.
- El pie de formulario deja libre el centro de la pantalla para que nunca compita con `Cerrar expediente`.
- El hub de Opciones calcula el espacio real antes de mostrar el slider de FOV; en una ventana extremadamente pequeña prefiere omitir ese duplicado antes que superponer hitboxes.
- Los ajustes propios reservan una franja inferior completa para navegación y metadatos.

## Familia de interfaces Jobs

La arquitectura introducida en 0.12.0 continúa vigente. `GripeVerde` se tomó únicamente como referencia de estructura/UX; su estética victoriana/cuarentena no se copia.

### Pantallas propias

- **Condiciones de estancia / Opciones:** hub Jobs en dos columnas, FOV integrado cuando hay espacio y acceso a todas las áreas de configuración.
- **Multijugador:** conserva lista de servidores, ping, MOTD, LAN y acciones de Minecraft bajo presentación Jobs.
- **Controles:** hub propio para mouse, teclas y hábitos de control.
- **Idioma:** selector real de idiomas con recarga de recursos.
- **Personalización de piel:** ficha administrativa del ocupante.

### Vanilla preservado, presentación Jobs

Estas pantallas mantienen internamente la lógica de Minecraft y sus opciones reales:

- Sonido
- Video
- Chat
- Accesibilidad
- Mouse
- Teclas
- Opciones online
- Paquetes de recursos
- Ajustes propios del aviso

Las pantallas de terceros no se sustituyen indiscriminadamente. Las redirecciones automáticas usan **clase exacta** y las interfaces externas pueden recibir sólo una banda contextual.

### Lenguaje visual compartido

- `ChromeExpediente`: recinto vigente, papel administrativo, bordes, cabeceras y pie seguro.
- `BotonExpediente`: botones de tinta/papel, foco de teclado y sonidos Jobs.
- `SliderExpediente`: slider propio para controles simples como FOV.
- `ToggleExpediente`: interruptor enlazado al estado real.
- `ListasExpediente`: integración de listas vanilla y scrollbar Jobs sin reimplementar su comportamiento.
- `TransicionInterfazJobs`: gesto breve entre expedientes, respetando movimiento reducido.

## Audio

- Tres camas ambientales por recinto: BASE, CARÁCTER y ACTIVIDAD.
- Eventos ocasionales con silencios deliberados.
- Música de menú independiente y mezcla contextual.
- Gestos propios para foco, selección, alternancia, confirmación, apertura, cierre y rechazo.
- BASE/CARÁCTER usan microvariación tonal de ciclo largo; ACTIVIDAD se mantiene estable para no deformar materiales reconocibles.

## Regla obligatoria de versión

**Todo JAR entregado debe incluir la versión en el nombre.**

Correcto:

```text
jobsmenu-0.13.0.jar
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
6. Publicación de **`jobsmenu-0.13.0.jar`** en `dev-latest` sólo desde `main`.

## Documentación

- [`CONTEXTO.md`](CONTEXTO.md): contrato vigente del proyecto.
- [`CHANGELOG.md`](CHANGELOG.md): historial de cambios.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos y pruebas pendientes.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): instalación del build versionado.
- [`docs/AUDITORIA_0.12.0_INTERFACES.md`](docs/AUDITORIA_0.12.0_INTERFACES.md): arquitectura base de interfaces.
- [`docs/DIRECCION_ARTISTICA.md`](docs/DIRECCION_ARTISTICA.md): lenguaje visual.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): convivencia con otros mods.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): prueba dentro de Minecraft.

El historial largo de evoluciones anteriores sigue en `docs/`; README y CONTEXTO describen siempre el estado vigente.
