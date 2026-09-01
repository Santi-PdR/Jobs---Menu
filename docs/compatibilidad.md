# Compatibilidad y despliegue — 0.14.0

## Perfil soportado

| Componente | Estado documentado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente; el servidor no necesita Jobs Menu |
| Versión del mod | **0.14.0** |
| Artefacto | `build/libs/jobsmenu-0.14.0.jar` |

Jobs Menu distingue entre **pantallas que controla**, **pantallas vanilla cuya lógica conserva** y **pantallas de otros mods que debe respetar**. La compatibilidad tiene prioridad sobre una reimplementación cosmética frágil.

## Regla de sustitución

Las redirecciones principales se hacen por **clase exacta**.

- `TitleScreen` vanilla puede convertirse en `PantallaNivel`.
- La pausa real vanilla puede convertirse en `PantallaEstancia`.
- `OptionsScreen` vanilla dentro del flujo Jobs puede convertirse en `PantallaOpcionesJobs`.
- `JoinMultiplayerScreen` vanilla dentro del flujo Jobs puede convertirse en `PantallaMultijugadorJobs`.

Una subclase de otro mod no se sustituye automáticamente sólo por heredar de estas clases.

## Options y Config Jobs

`PantallaOpcionesJobs` es un hub propio. La configuración del mod aparece como acción principal diferenciada de las opciones Minecraft.

`PantallaAjustesAviso` es también una pantalla propia desde 0.14.0: ya no usa `OptionsList`/`OptionInstance` como interfaz visible. Sus cinco categorías escriben directamente sobre `ConfigTurno` mediante widgets Jobs.

Las dos entradas válidas son:

- Options → Config Jobs;
- **Mods → Jobs Menu → Config**.

Ambas abren la misma implementación.

## Pantallas vanilla preservadas

Se conserva la lógica de Minecraft en:

- Sonido;
- Video vanilla;
- Chat;
- Accesibilidad;
- Mouse;
- Teclas;
- Online;
- Resource Packs;
- diálogos de servidor y confirmación cuando conviene.

Las envolturas ejecutan primero la lógica original. Jobs retira fondos/bandas cuando es seguro, reserva espacio para su chrome y desactiva botones `Done` duplicados cuando los sustituye.

## `PielVanillaJobs`

Durante una sesión Jobs, pantallas auxiliares cuyo paquete pertenece a `net.minecraft.*` pueden recibir una capa visual posterior al render:

- botones de papel/tinta;
- estados hover/foco visibles;
- borde administrativo en campos de texto.

La capa **no cambia**:

- hitboxes;
- listeners;
- validación;
- foco;
- protocolo;
- datos de la pantalla.

Por eso Direct Connect, Add Server y confirmaciones pueden verse integrados sin duplicar su lógica.

Las pantallas de terceros no reciben `PielVanillaJobs`; como máximo reciben la banda contextual general durante la visita.

## Scrollbar Jobs

`ListasExpediente` conserva `AbstractSelectionList` como fuente de verdad para posición, wheel, click, drag y tamaño del contenido.

Después del render vanilla, Jobs cubre la barra gris y dibuja sobre el mismo hitbox:

- canaleta de archivador;
- topes;
- marcas de recorrido;
- tirador proporcional;
- agarres internos.

La integración usa reflection defensiva con nombres SRG. Si no puede resolverse una lista modificada, se conserva la presentación/funcionalidad disponible en vez de abortar la pantalla.

## Embeddium y video externo

`PantallaOpcionesJobs` intenta abrir la pantalla real de Embeddium mediante sus clases públicas conocidas. Si están disponibles, utiliza esa implementación completa. Si no, usa `PantallaVideoJobs` sobre video vanilla.

No se reconstruye internamente Embeddium mediante reflection profunda. Oculus, Embeddium Extra y addons deben probarse en el modpack real.

## Fondos PNG 10–17

Los fondos 10–17 son **estáticos por requisito permanente**.

No reciben zoom, paneo, respiración, parallax, flicker, niebla móvil, scanline animada, motas, presencia ni tratamientos globales que desplacen la imagen.

`ChromeExpediente` puede dibujar elementos estáticos de interfaz delante/alrededor del documento; eso no altera el PNG.

Los apagones/transiciones de Nivel continúan porque representan el estado general del menú. `tools/verificar_fondos.py` y `NativeImage` siguen validando los recursos.

## Accesibilidad

La familia Jobs respeta:

- movimiento reducido;
- destellos reducidos;
- alto contraste;
- texto grande;
- papel limpio;
- guía de lectura;
- bajo consumo;
- perfil accesible.

Movimiento reducido simplifica microinteracciones y transiciones. Los PNG 10–17 permanecen estáticos con cualquier combinación de ajustes.

## Resource Packs e idioma

Idioma y paquetes usan la recarga real de recursos de Minecraft. `RecargaRecursosCliente` invalida referencias de música/ambiente asociadas a un `SoundEngine` anterior.

Probar ES ↔ EN, F3+T, aplicar/quitar packs y volver al menú sin duplicados de audio.

## Multijugador

`PantallaMultijugadorJobs` conserva `ServerSelectionList`, ping, MOTD, favicons, LAN, servidores guardados y las acciones vanilla de seleccionar, conexión directa, añadir, editar, borrar, refrescar y cancelar.

Los diálogos auxiliares pueden recibir `PielVanillaJobs`, pero siguen usando la lógica original.

## Audio y lifecycle

La visita al menú mantiene continuidad de música y camas ambientales al navegar. Abrir Options, Config, Sonido, Mods o una pantalla hija no debe reiniciar el recinto.

Al entrar a un mundo o terminar la visita, las camas se cierran. Las recargas de recursos se manejan aparte para no conservar referencias al motor de sonido anterior.

## Instancia de referencia

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\
```

El JAR de esta entrega debe quedar únicamente como:

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods\jobsmenu-0.14.0.jar
```

No se mantiene un `.ps1` dentro del repositorio. El procedimiento está en [`DESPLIEGUE.md`](DESPLIEGUE.md).

## Límites que requieren prueba manual

- mods que sustituyen pantallas después de que Jobs ya las haya abierto;
- widgets/listas reconstruidos por otros mods fuera del `init()` vanilla;
- `PielVanillaJobs` con resource packs de GUI muy agresivos;
- scrollbar con listas profundamente modificadas;
- fuentes con métricas extremas;
- GUI Scale extremos;
- narración y navegación de teclado;
- Embeddium/Oculus/addons exactos del modpack;
- GPU/rendimiento de escenas procedurales 0–9;
- SoundEngine con el conjunto completo de mods de audio.

## Estado de certificación

CI certifica:

1. Java 17;
2. política de artefacto versionado;
3. PNG 10–17;
4. recursos/idiomas/ASCII/coherencia estática;
5. build Forge;
6. preparación de `jobsmenu-0.14.0.jar`.

La publicación a `dev-latest` sólo ocurre desde `main`.

Un build verde no certifica estética, hitboxes, narración, scroll ni compatibilidad visual dentro del modpack. Esa aceptación depende de la prueba manual documentada.
