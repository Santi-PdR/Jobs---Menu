# Jobs · Aviso a los ocupantes

Mod **exclusivamente de cliente** para Minecraft Forge 1.20.1 que reemplaza el flujo de menús por la interfaz del servidor **Jobs**: expedientes, avisos administrativos y recintos que continúan detrás de la interfaz.

La salida existe. Cuesta. Los **Executores** vuelven. El objetivo no es aplicar una skin sobre Minecraft: el menú debe sentirse como otra dependencia de la instalación.

| | |
|---|---|
| Versión | **0.14.0** |
| Artefacto | **`jobsmenu-0.14.0.jar`** |
| Minecraft | **1.20.1** |
| Forge | **47.x** |
| Java | **17** |
| Lado | **Cliente** |
| Niveles | **18 (0–17)** |

## 0.14.0 · Centro de control y lenguaje de interfaz

0.14.0 profundiza el trabajo de 0.12/0.13. La prioridad es que navegar por Opciones, Config y diálogos auxiliares deje de sentirse como entrar y salir de Minecraft vanilla.

### Options ahora separa Jobs de Minecraft

`PantallaOpcionesJobs` deja de mezclar la configuración del mod como un botón más del grid.

- **Config de Jobs** es ahora la acción principal de ancho completo y usa una jerarquía visual exclusiva.
- Las opciones de Minecraft quedan agrupadas debajo como una segunda sección.
- FOV se integra en la última fila cuando existe espacio real.
- El layout sigue degradando de forma segura en ventanas pequeñas antes de permitir solapes.
- El acceso de Forge **Mods → Jobs Menu → Config** continúa disponible y abre exactamente el mismo sistema.

### Config del mod completamente propia

`PantallaAjustesAviso` ya no depende visualmente de `OptionsList` ni de controles vanilla.

La configuración se reorganiza como un expediente Jobs con cinco áreas:

- visual;
- Nivel;
- audio;
- accesibilidad;
- sistema.

Cada área usa `BotonExpediente`, `ToggleExpediente` y `SliderExpediente` conectados directamente con `ConfigTurno`. No existe un segundo estado de configuración ni una copia decorativa de los valores.

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

Nueva `PielVanillaJobs`.

Durante una visita Jobs, diálogos vanilla que conviene conservar por compatibilidad —por ejemplo conexión directa, añadir servidor o confirmaciones— mantienen su lógica y hitboxes originales, pero sus botones y campos de texto reciben una capa de papel/tinta Jobs después del render de Minecraft.

Las pantallas de terceros no reciben esta sustitución de controles: mantienen su implementación y sólo pueden recibir contexto visual mínimo.

### Scrollbar y transiciones

- La scrollbar Jobs mantiene rueda/click/drag de Minecraft, pero ahora usa canaleta, topes, marcas de recorrido y tirador de expediente.
- `TransicionInterfazJobs` gana sombra de carpeta, doble fibra de papel y marcas de archivo.
- Movimiento reducido sigue convirtiendo la transición en un fade breve sin desplazamiento obligatorio.

## Fondos PNG 10–17: estáticos por diseño

La regla introducida en 0.13.0 sigue siendo dura: los ocho PNG suministrados para los niveles **10–17 no se animan**.

No reciben zoom, paneo, parallax, respiración, scanlines animadas, flicker, niebla móvil, motas, presencia ni tratamientos globales que muevan la imagen. `PlantaImagen` hace un cover centrado estable y una integración fija mínima.

Los apagones/cambios de Nivel continúan porque pertenecen al flujo del menú, no a la animación del PNG. Los niveles 0–9 siguen siendo recintos procedurales vivos.

Los PNG 10–17 siguen pasando firma PNG, CRC, IDAT, descompresión y dimensiones en CI, y `NativeImage` vuelve a validarlos en runtime. Un recurso roto cae a fallback seguro, nunca se acepta la textura morado/negro como resultado final.

## Familia de interfaces Jobs

Además de Options y Config, permanecen tematizados:

- Multijugador;
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
- Gestos propios para foco, selección, alternancia, confirmación, apertura, cierre y rechazo.
- BASE/CARÁCTER usan microvariación tonal de ciclo largo; ACTIVIDAD permanece estable para no deformar materiales reconocibles.

## Regla obligatoria de versión

**Todo JAR entregado debe incluir la versión en el nombre.**

Correcto:

```text
jobsmenu-0.14.0.jar
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
6. Publicación de **`jobsmenu-0.14.0.jar`** en `dev-latest` sólo desde `main`.

## Documentación

- [`CONTEXTO.md`](CONTEXTO.md): contrato vigente del proyecto.
- [`CHANGELOG.md`](CHANGELOG.md): historial de cambios.
- [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md): riesgos y pruebas pendientes.
- [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md): instalación del build versionado.
- [`docs/AUDITORIA_0.14.0_UI.md`](docs/AUDITORIA_0.14.0_UI.md): revisión de esta evolución.
- [`docs/DIRECCION_ARTISTICA.md`](docs/DIRECCION_ARTISTICA.md): lenguaje visual.
- [`docs/compatibilidad.md`](docs/compatibilidad.md): convivencia con otros mods.
- [`docs/checklist-manual.md`](docs/checklist-manual.md): prueba dentro de Minecraft.

El historial largo de evoluciones anteriores sigue en `docs/`; README y CONTEXTO describen siempre el estado vigente.
