# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1 que reemplaza el flujo de menús por la interfaz del servidor **Jobs**: expedientes, avisos administrativos y recintos que siguen vivos detrás de la interfaz.

La salida existe. Cuesta. Los **Executores** vuelven. El menú no intenta parecer una skin puesta encima de Minecraft: intenta sentirse como otra parte de la instalación.

| | |
|---|---|
| Versión | **0.12.0** |
| Artefacto | **`jobsmenu-0.12.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **18 (0–17)** |

## 0.12.0 · Interfaces administrativas

Esta versión amplía el lenguaje visual del mod más allá del Title/Pause. La referencia arquitectónica fue el proyecto `GripeVerde`: se reutilizó la idea de mantener la lógica vanilla cuando conviene y construir una familia visual coherente alrededor de ella. **No se reutiliza su tema victoriano/cuarentena.** Jobs conserva papel fotocopiado, tinta seca, archivo administrativo, fluorescentes, instalación y lenguaje burocrático.

### Pantallas propias

- **Condiciones de estancia / Opciones:** hub Jobs en dos columnas, FOV propio, navegación jerarquizada y acceso directo a los ajustes del aviso.
- **Multijugador:** conserva lista de servidores, ping, MOTD, LAN y acciones de Minecraft, pero reemplaza la superficie de botones y el marco por el registro de cuadrillas de Jobs.
- **Controles:** hub propio para mouse, teclas y hábitos de control.
- **Idioma:** archivo de idiomas propio, selección pendiente, doble clic para aplicar y recarga segura de recursos.
- **Personalización de piel:** ficha de ocupante para partes visibles y mano principal.

### Vanilla preservado, presentación Jobs

Estas pantallas conservan internamente las opciones/clases de Minecraft para mantener compatibilidad, pero usan recinto vivo, papel, marcas de archivo, pie de formulario y navegación Jobs:

- Sonido
- Video
- Chat
- Accesibilidad
- Mouse
- Teclas
- Opciones online
- Paquetes de recursos
- Ajustes propios del aviso

Las pantallas menores o de terceros abiertas durante la visita no se sustituyen a la fuerza: reciben una **banda contextual de expediente**. Las sustituciones automáticas se hacen por **clase exacta**, para no barrer interfaces custom de otros mods.

### Nuevo lenguaje visual compartido

- `ChromeExpediente`: recinto vigente + papel administrativo + bordes de carpeta + sellos de formulario + nivel + versión real instalada.
- `BotonExpediente`: botones sin skin vanilla, con tinta/papel, foco de teclado, respuesta suave y sonidos propios del edificio.
- `SliderExpediente`: slider de tinta para valores como FOV.
- `ToggleExpediente`: interruptores de expediente con estado real, sin duplicar configuración.
- `TransicionInterfazJobs`: paso corto entre expedientes, sin flash blanco y respetando movimiento reducido.
- `ListasExpediente`: integración defensiva para quitar fondos dirt de listas vanilla sin reimplementar su lógica.

## Escena y atmósfera

La rotación contiene diez recintos procedurales (0–9) y ocho fondos suministrados (10–17). Todos participan del mismo sistema de luz, apagones, transición, ambiente, música, avisos, ronda de Executores y accesibilidad.

Los fondos de imagen 10–17 se validan antes de compilar: firma PNG, CRC, flujo IDAT, descompresión y dimensiones. En runtime `PlantaImagen` vuelve a comprobar el recurso con `NativeImage`. Un recurso inválido cae a una escena procedural segura en vez de dejar la textura morado/negro.

Los fondos de imagen mantienen zoom/paneo lento y tratamientos por escena. El sistema global añade profundidad, exposición, halo y apagones con masa visual; `movimiento_reducido`, `destellos_reducidos` y `bajo_consumo` tienen prioridad sobre cualquier efecto decorativo.

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
jobsmenu-0.12.0.jar
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
6. Publicación de `jobsmenu-0.12.0.jar` en `dev-latest` sólo desde `main`.

## Documentación

- [`CONTEXTO.md`](CONTEXTO.md): contrato vigente del proyecto.
- [`CHANGELOG.md`](CHANGELOG.md): historial de cambios.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos/pruebas reales pendientes.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): instalación del build versionado.
- [`docs/AUDITORIA_0.12.0_INTERFACES.md`](docs/AUDITORIA_0.12.0_INTERFACES.md): matriz de interfaces 0.12.0.
- [`docs/DIRECCION_ARTISTICA.md`](docs/DIRECCION_ARTISTICA.md): lenguaje visual.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): convivencia con otros mods.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): prueba dentro de Minecraft.

El historial largo de evoluciones anteriores sigue en `docs/`; README y CONTEXTO describen siempre el estado vigente.
