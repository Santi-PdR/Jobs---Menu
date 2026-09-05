# Compatibilidad y despliegue — 0.37.0

## Perfil soportado

| Componente | Estado documentado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente; el servidor no necesita Jobs Menu |
| Versión del mod | **0.37.0** |
| Artefacto | `build/libs/jobsmenu-0.37.0.jar` |

Jobs Menu distingue entre **pantallas que controla**, **pantallas vanilla cuya lógica conserva** y **pantallas de otros mods que debe respetar**. La compatibilidad tiene prioridad sobre una reimplementación cosmética frágil.

## Regla de sustitución

Las redirecciones principales se hacen por **clase exacta**.

- `TitleScreen` vanilla puede convertirse en `PantallaNivel`.
- La pausa real vanilla puede convertirse en `PantallaEstancia`.
- `OptionsScreen` vanilla dentro del flujo Jobs puede convertirse en `PantallaOpcionesJobs`.
- `JoinMultiplayerScreen` vanilla dentro del flujo Jobs puede convertirse en `PantallaMultijugadorJobs`.
- `SelectWorldScreen` vanilla puede envolverse como `PantallaMundosJobs` conservando la lista/previews originales.
- `ModListScreen` de Forge puede envolverse como `PantallaModsJobs` conservando su panel, búsqueda y acciones.

Una subclase de otro mod no se sustituye automáticamente sólo por heredar de estas clases. El retorno desde gameplay es una excepción contextual: cuando Jobs sabe que acaba de salir de un servidor remoto, un destino vanilla de Title o Multiplayer se reconduce a `PantallaMultijugadorJobs`.

## Frontera entre menú y gameplay

Fuera de gameplay, las transiciones sólo se notifican cuando el origen o el destino es una pantalla propia de Jobs. Si el cambio no pertenece a ese flujo, la transición pendiente se cancela.

**Con un mundo o servidor cargado no existe ninguna animación de transición Jobs.** Desde 0.36.0 la frontera tiene varias compuertas deliberadamente redundantes:

- `usaTransicionJobs()` rechaza cualquier transición si `Minecraft.level != null`;
- login, logout y el tick de gameplay cancelan una transición pendiente;
- el render no ejecuta `TransicionInterfazJobs.dibujar()` durante gameplay;
- `PulidoInterfazJobs.notificarApertura()` no se registra durante gameplay, por lo que desaparece también su animación corta de entrada.

La pausa Jobs y sus pantallas propias de configuración siguen tematizadas y pueden conservar foco/feedback de UI, pero aparecen directamente sobre el juego, sin barrido ni fundido. Cualquier pantalla no Jobs con un mundo cargado queda además fuera del postprocesado global. Esto incluye chat, inventario, contenedores y pantallas de gameplay de otros mods: no reciben piel, banda contextual, transición ni reemplazo de clicks.

El feedback corto de interfaz está separado del lifecycle musical: una pantalla propia Jobs puede reemplazar clicks vanilla y emitir hover aun con un mundo cargado, pero eso no reactiva `SesionMenu`, música ni camas ambientales.

## Options y Config Jobs

`PantallaOpcionesJobs` es un hub propio. La configuración del mod aparece como acción principal diferenciada de las opciones Minecraft.

`PantallaAjustesAviso` es también una pantalla propia: ya no usa `OptionsList`/`OptionInstance` como interfaz visible. Sus cinco categorías escriben directamente sobre `ConfigTurno` mediante widgets Jobs.

Las dos entradas válidas son:

- Options → Config Jobs;
- **Mods → Jobs Menu → Config**.

Ambas abren la misma implementación.

## Pantallas vanilla preservadas

Se conserva la lógica de Minecraft/Forge en:

- Sonido;
- Video vanilla;
- Chat;
- Accesibilidad;
- Mouse;
- Teclas;
- Online;
- Resource Packs;
- Seleccionar mundo;
- Mods de Forge;
- diálogos de servidor y confirmación cuando conviene.

Las envolturas ejecutan primero la lógica original. Jobs retira fondos/bandas cuando es seguro, reserva espacio para su chrome y desactiva botones `Done` duplicados cuando los sustituye.

## `PielVanillaJobs` y feedback preservado

Durante una sesión Jobs, pantallas auxiliares cuyo paquete pertenece a `net.minecraft.*` pueden recibir una capa visual posterior al render:

- botones de papel/tinta;
- estados hover/foco visibles;
- borde administrativo en campos de texto.

Los botones y sliders vanilla que siguen vivos por compatibilidad reciben además `UI_PASAR` una sola vez al entrar con ratón o foco de teclado. Sus clicks `ui.button.click` se sustituyen por `UI_ELEGIR` en cualquier superficie Jobs válida. Los widgets Jobs propios se excluyen de ese seguimiento para no duplicar sus sonidos.

La capa **no cambia**:

- hitboxes;
- listeners;
- validación;
- foco;
- protocolo;
- datos de la pantalla.

Por eso Direct Connect, Add Server y confirmaciones pueden verse integrados sin duplicar su lógica.

Las pantallas de terceros no reciben `PielVanillaJobs`; como máximo reciben la banda contextual general durante una visita de menú sin gameplay.

## Scrollbar Jobs

`ListasExpediente` conserva `AbstractSelectionList` como fuente de verdad para posición, wheel, click, drag y tamaño del contenido.

Después del render vanilla, Jobs cubre la barra gris y dibuja sobre el mismo hitbox cuando puede resolver los datos internos:

- canaleta de archivador;
- topes;
- marcas de recorrido;
- tirador proporcional;
- agarres internos.

La integración usa reflection defensiva con nombres SRG. Si no puede resolverse una lista modificada, se conserva una scrollbar utilizable/fallback en vez de abortar o inventar coordenadas.

## Video Settings vanilla y mods de rendimiento

`PantallaOpcionesJobs` abre directamente `VideoSettingsScreen`. Jobs no reconstruye páginas de Embeddium, no mueve la lista, no oculta Done y no dibuja pieles, marcos, transiciones ni feedback de hover/click encima.

Si Embeddium, Oculus u otro addon modifica la clase vanilla por sus propios mecanismos, Jobs no interfiere. Esa integración debe probarse en el modpack real porque pertenece al otro mod.

## Fondos PNG 10–17

Los fondos 10–17 son **estáticos por requisito permanente**.

No reciben zoom, paneo, respiración, parallax, flicker, niebla móvil, scanline animada, motas, presencia ni tratamientos globales que desplacen la imagen.

`ChromeExpediente` puede dibujar elementos estáticos de interfaz delante/alrededor del documento; eso no altera el PNG.

Los apagones/transiciones de Nivel continúan fuera de gameplay porque representan el estado general del menú. `tools/verificar_fondos.py` y `NativeImage` siguen validando los recursos.

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

La Guía de accesibilidad vanilla no se mantiene como botón inferior dentro de `PantallaAccesibilidadJobs`, porque Jobs ya proporciona su propio cierre y ese botón duplicado provocaba solape.

Movimiento reducido simplifica microinteracciones y transiciones del menú. Los PNG 10–17 permanecen estáticos con cualquier combinación de ajustes. Durante gameplay las transiciones de pantalla Jobs están deshabilitadas independientemente de estos perfiles.

## Resource Packs e idioma

Idioma y paquetes usan la recarga real de recursos de Minecraft. `RecargaRecursosCliente` invalida referencias de música/ambiente asociadas a un `SoundEngine` anterior.

`es_ar`, `es_cl`, `es_ec`, `es_mx`, `es_uy` y `es_ve` reutilizan el catálogo Jobs `es_es` durante `processResources`; esto evita que variantes españolas queden con cadenas propias del mod en inglés.

Probar ES ↔ EN, Español (Uruguay), F3+T, aplicar/quitar packs y volver al menú sin duplicados de audio. Resource Packs usa archivo oscuro y no debe dejar dirt ni generar un paquete musical Jobs.

## Multijugador

`PantallaMultijugadorJobs` conserva `ServerSelectionList`, ping, MOTD, favicons, LAN y las acciones de seleccionar, conexión directa, añadir, editar y borrar sobre la lógica real de Minecraft.

### ESC y Cancelar

La corrección 0.36.0 deja de asumir que `super.onClose()` equivale al botón Cancelar vanilla. En Minecraft/Forge 1.20.1 no es así: el botón Cancelar de `JoinMultiplayerScreen` vuelve directamente a `lastScreen`, mientras `Screen.onClose()` usa `popGuiLayer()`.

Jobs guarda por ello su propio `pantallaPadre` y hace que:

- ESC → `onClose()` → `cerrarAlPadre()`;
- botón Cancelar → `cerrarAlPadre()`;
- `cerrarAlPadre()` aplique un guard idempotente y llame una sola vez a `minecraft.setScreen(padreDestino())`;
- no exista una llamada real a `super.onClose();` en esa ruta.

Así ambas acciones terminan exactamente en el mismo padre Jobs sin depender del stack de capas Forge.

### Actualizar/F5

Desde 0.37.0 Actualizar/F5 reconstruye directamente Jobs **sin perder la selección de un servidor guardado**:

- `ipSeleccionada()` extrae únicamente la IP del `OnlineServerEntry` seleccionado;
- antes de `setScreen()` se activa `cerrando`, de modo que una segunda pulsación no puede encolar otra reconstrucción sobre la misma pantalla;
- la nueva `PantallaMultijugadorJobs` recibe padre + IP, no una referencia a la Entry anterior;
- después de `updateOnlineServers`, `restaurarSeleccionPreferida()` recorre las Entries nuevas y selecciona la que tenga la misma IP;
- `onSelectedChange()` sincroniza los estados vanilla tras la restauración;
- una entrada LAN efímera no se fuerza artificialmente porque depende del nuevo ciclo del detector LAN;
- no existe una `JoinMultiplayerScreen` vanilla intermedia;
- F5 por teclado emite `UI_ALTERNAR`; el botón Actualizar conserva su propio gesto de click y `refrescarLista()` no añade un segundo sonido;
- el indicador inferior reutiliza la traducción vanilla `selectServer.refresh` y ya no contiene el literal `JOBS/SERVER`.

`init()` sigue recreando `servers.dat`, detector LAN, pinger y widgets reales. La preservación por IP mejora continuidad sin acoplar objetos de listas distintas.

La cabecera **Puestos de acceso** reserva una tarjeta para `JobsDosh.exaroton.me:56477`. El servidor se guarda con nombre localizado, se deduplica por IP, se mueve al primer renglón y no permite Edit/Delete desde los botones Jobs. La migración retira `Ghoul Outbreak` y entradas que suplanten el nombre oficial con otra IP; Jobs es el único servidor instalado automáticamente por el mod.

`ConnectScreen` se abre con `PantallaMultijugadorJobs` como padre. Cancelar o fallar antes del login vuelve a esa misma lista.

Para una sesión ya conectada, Jobs memoriza durante los ticks jugables si `Minecraft#getCurrentServer()` indica servidor remoto y vuelve a comprobarlo en `LoggingOut`. Forge dispara `LoggingOut` antes de continuar la limpieza de nivel, de modo que el contexto se captura aunque la desconexión vanilla termine después en `TitleScreen`. Si el retorno era remoto, tanto Title como `JoinMultiplayerScreen` se reconducen a una nueva `PantallaMultijugadorJobs` con `PantallaNivel` como padre. Un mundo local continúa al main Jobs.

Los diálogos auxiliares pueden recibir `PielVanillaJobs`, pero siguen usando la lógica original.

## Selector de mundos y Mods

`PantallaMundosJobs` conserva `SelectWorldScreen`: previews, selección y operaciones vanilla siguen siendo responsabilidad de Minecraft. Jobs lo presenta como **Archivo de turnos**, reubica el buscador y elimina bandas/fondos que rompían continuidad.

`PantallaModsJobs` conserva `ModListScreen` de Forge: búsqueda, orden, logos, panel de información, Config y carpeta de mods permanecen intactos. El render blanco fijo se atenúa con gris neutro sin reimplementar el registro de mods ni amarillear sus recursos.

## Audio y lifecycle

La visita al menú mantiene continuidad de música y camas ambientales al navegar. Abrir Options, Config, Sonido, Mods o una pantalla hija no debe reiniciar el recinto.

La música se distribuye dentro del JAR. `LimpiezaRecursosLegados` sólo retira el antiguo `resourcepacks/jobsmenu-musica-activa`; Jobs no crea un pack nuevo.

Los eventos de login/logout y el tick defensivo con nivel activo detienen inmediatamente música y camas. No hay cola ni fundido audible dentro de un mundo o servidor. Los gestos breves de UI son independientes de ese lifecycle: en pausa/configuración Jobs pueden sonar sin abrir una visita ni mantener camas.

Al abandonar un mundo local, Title/Realms se reconduce a `PantallaNivel`. Al abandonar un servidor remoto, Title o Multiplayer se reconducen a `PantallaMultijugadorJobs`. Una pantalla de desconexión puede conservar antes su mensaje y, al continuar, cae en la lista Jobs.

## Instancia de referencia

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\
```

El JAR de esta entrega debe quedar únicamente como:

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods\jobsmenu-0.37.0.jar
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
- SoundEngine con el conjunto completo de mods de audio;
- detector LAN/pinger/favicons y preservación de selección después de múltiples F5/Actualizar;
- retorno tras kick/desconexión con mods que sustituyan `DisconnectedScreen`, `TitleScreen` o `JoinMultiplayerScreen`.

## Estado de certificación

CI certifica:

1. Java 17;
2. política de artefacto versionado;
3. PNG 10–17 y JPEG 18–31;
4. recursos/idiomas/ASCII/coherencia estática;
5. contratos específicos de cierre directo de Multiplayer y ausencia de transiciones durante gameplay;
6. contrato 0.37 de selección preservada por IP, guard de recarga y feedback F5;
7. sincronización mínima de documentación vigente e índice `docs/README.md`;
8. build Forge;
9. preparación de `jobsmenu-0.37.0.jar`.

La publicación a `dev-latest` sólo ocurre desde `main`.

Un build verde no certifica estética, hitboxes, narración, scroll ni compatibilidad visual dentro del modpack. Esa aceptación depende de la prueba manual documentada.
