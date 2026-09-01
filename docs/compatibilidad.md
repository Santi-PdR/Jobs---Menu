# Compatibilidad y despliegue — 0.13.0

## Perfil soportado

| Componente | Estado documentado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente; el servidor no necesita Jobs Menu |
| Versión del mod | **0.13.0** |
| Artefacto | `build/libs/jobsmenu-0.13.0.jar` |

Jobs Menu modifica la experiencia de menús del cliente, pero distingue entre **pantallas que controla** y **pantallas que debe respetar**. La compatibilidad tiene prioridad sobre una skin perfecta: cuando una implementación externa ya resuelve una interfaz compleja, Jobs aporta contexto visual y no intenta copiarla de forma frágil.

## Regla de sustitución de pantallas

Las redirecciones principales se hacen por **clase exacta**.

- `TitleScreen` vanilla puede convertirse en `PantallaNivel`.
- La pausa real vanilla puede convertirse en `PantallaEstancia`.
- `OptionsScreen` vanilla dentro del flujo Jobs puede convertirse en `PantallaOpcionesJobs`.
- `JoinMultiplayerScreen` vanilla dentro del flujo Jobs puede convertirse en `PantallaMultijugadorJobs`.

Una subclase proporcionada por otro mod no se sustituye automáticamente sólo por heredar de una de estas clases. Las pantallas externas abiertas durante una visita pueden recibir una banda contextual discreta sin cambiar sus widgets ni su lógica.

## Opciones y subpantallas vanilla

### Hubs propios

- Opciones/Condiciones de estancia.
- Controles.
- Idioma.
- Piel.
- Multijugador principal.

Estos hubs escriben sobre `Options`, `LanguageManager`, `ServerSelectionList` o datos reales; no mantienen copias independientes de la configuración.

### Envolturas de clases vanilla

- Sonido.
- Video vanilla.
- Chat.
- Accesibilidad.
- Mouse.
- Teclas.
- Online.
- Resource Packs.

Se ejecuta primero la lógica vanilla y después Jobs aplica presentación. Los límites verticales de listas sólo se cambian cuando es seguro. Los botones `Done` sustituidos por navegación Jobs se dejan invisibles **e inactivos** para que no conserven hitboxes ocultos.

## Scrollbar Jobs

`ListasExpediente` conserva `AbstractSelectionList` como fuente de verdad para:

- posición de scroll;
- wheel;
- click;
- drag;
- tamaño del contenido.

Después del render vanilla, Jobs cubre la barra gris y dibuja su propia barra papel/tinta sobre el mismo hitbox. La integración usa reflection defensiva con nombres SRG. Si otro mod cambia la implementación y no puede resolverse, la lista conserva la scrollbar vanilla en vez de fallar.

Debe probarse especialmente con listas modificadas por mods y con GUI Scale altos.

## Acceso Config de Forge

`JobsMenu` registra `ConfigScreenHandler.ConfigScreenFactory`. La ruta:

**Mods → Jobs Menu → Config**

abre `PantallaAjustesAviso`, la misma pantalla disponible desde el hub Jobs. Esto evita dos estados de configuración diferentes.

## Fondos PNG 10–17

Desde 0.13.0 estos fondos son **estáticos por requisito del proyecto**.

No reciben:

- zoom/paneo;
- respiración de cámara;
- flicker;
- niebla móvil;
- scanline;
- motas/presencia;
- materiales o tratamiento animado global.

Siguen participando en apagones y transiciones de Nivel porque esos estados pertenecen al flujo del menú. Continúan validados por `tools/verificar_fondos.py` y por `NativeImage` en runtime.

## Embeddium y pantallas de video externas

`PantallaOpcionesJobs` intenta abrir la pantalla real de Embeddium mediante sus clases conocidas. Si están disponibles, se usa esa implementación completa. Si no, se abre `PantallaVideoJobs` basada en video vanilla.

No se reconstruye internamente el panel de Embeddium. Si una versión cambia paquetes o constructores, el fallback es video vanilla tematizado, no un crash.

La compatibilidad con Oculus/Embeddium Extra u otros addons de video debe comprobarse en el modpack real.

## Accesibilidad

La pantalla de accesibilidad mantiene todas las opciones vanilla y añade al final cuatro ayudas del mod:

- movimiento reducido;
- destellos reducidos;
- alto contraste;
- texto grande.

0.13.0 reserva explícitamente espacio entre cabecera, lista, scrollbar, footer y botón Volver. Todas las ayudas escriben en `ConfigTurno` y usan el mismo guardado diferido que el resto de Jobs.

## Resource Packs e idioma

Los cambios de idioma y paquetes usan la recarga real de recursos de Minecraft. `RecargaRecursosCliente` invalida las referencias de música/ambiente en el hilo del cliente para que no sobrevivan instancias atadas a un `SoundEngine` anterior.

Probar especialmente:

- cambiar ES ↔ EN;
- F3+T;
- aplicar/quitar packs;
- volver al menú tras la recarga;
- comprobar que música y ambiente no se duplican.

## Multijugador

`PantallaMultijugadorJobs` conserva:

- `ServerSelectionList`;
- ping;
- MOTD;
- favicons;
- búsqueda LAN;
- servidores guardados;
- acciones vanilla de seleccionar, conexión directa, añadir, editar, borrar, refrescar y cancelar.

Los diálogos secundarios que siguen siendo vanilla pueden mostrar sólo la banda contextual Jobs. Reimplementar protocolo/conexión/validaciones por cosmética añadiría riesgo sin mejorar comportamiento.

## Audio y lifecycle

La visita al menú mantiene continuidad de música y camas ambientales al navegar. Abrir Opciones, Sonido, Mods o una interfaz hija no debe reiniciar el recinto.

Al entrar a un mundo o terminar la visita, las camas se cierran. Las recargas de recursos se tratan aparte para no conservar referencias a un motor de sonido viejo.

## Instancia de referencia: SKLauncher `test-1`

La instancia de prueba documentada es:

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\
```

El JAR de esta entrega debe quedar únicamente como:

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods\jobsmenu-0.13.0.jar
```

No se mantiene un `.ps1` dentro del repositorio. El procedimiento de entrega está en [`DESPLIEGUE.md`](DESPLIEGUE.md).

## Límites que requieren prueba manual

- Mods que sustituyen completamente una pantalla después de que Jobs ya la haya abierto.
- Mods que reconstruyen widgets/listas fuera del `init()` vanilla.
- Scrollbar Jobs con listas inyectadas o modificadas por otros mods.
- Fuentes de resource packs con métricas muy distintas.
- GUI Scale extremos.
- Narración con widgets inyectados por otros mods.
- Embeddium/Oculus/addons de video en las versiones exactas del modpack.
- GPU y rendimiento real de las escenas procedurales 0–9.
- SoundEngine con el conjunto completo de mods de audio.

## Estado de certificación

CI certifica en cada commit de la rama:

1. Java 17.
2. Política de artefacto versionado.
3. PNG 10–17.
4. Recursos, idiomas, ASCII de fuentes y coherencia estática.
5. Build Forge.
6. Preparación de `jobsmenu-0.13.0.jar`.

La publicación a `dev-latest` se ejecuta únicamente desde `main`.

Un build verde no certifica hitboxes, estética, narración, scroll, sonido ni compatibilidad visual dentro del modpack. Esa aceptación está en [`checklist-manual.md`](checklist-manual.md) y los riesgos actuales en [`KNOWN_ISSUES.md`](../KNOWN_ISSUES.md).
