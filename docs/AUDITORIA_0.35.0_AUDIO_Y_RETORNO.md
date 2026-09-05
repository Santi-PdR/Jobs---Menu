# Auditoría 0.35.0 — audio de interfaz y retorno tras gameplay

Fecha: 2026-09-05

## Estado revisado

La base 0.34.0 ya tenía dos correcciones válidas que se conservan:

- 0.31.0 eliminó el fallback `minecraft:ui.button.click` de los gestos Jobs y añadió sustitución de clicks vanilla durante la sesión de menú.
- 0.34.0 unificó Escape/Cancelar en `PantallaMultijugadorJobs`, eliminó rutas de salida duplicadas y limitó transiciones/capas Jobs fuera de gameplay.

La revisión de 0.35.0 se concentra en los huecos que quedaron entre esos dos contratos.

## Hallazgo 1 — click vanilla posible en pausa/configuración Jobs

`SesionMenu.activa()` exige `cliente.level == null`. Eso es correcto para música y ambiente, pero el listener de `PlaySoundEvent` usaba esa misma condición para decidir si reemplazaba `UI_BUTTON_CLICK`.

Resultado: una pantalla propia Jobs abierta con un mundo cargado podía seguir siendo visualmente Jobs y, si conservaba un widget vanilla real, ese widget quedaba fuera de la sustitución global de click. Los widgets propios (`BotonExpediente`, `ToggleExpediente`, `SliderExpediente`) no sufrían este problema porque reproducen sus gestos directamente.

### Corrección

Se introduce el concepto de **superficie Jobs activa** separado de la **sesión musical**:

- pantalla propia Jobs → puede usar feedback corto de interfaz;
- pantalla auxiliar vanilla dentro de una sesión de menú → puede usar feedback Jobs;
- Video Settings → siempre excluida;
- pantalla no Jobs con mundo cargado → siempre excluida.

Esto permite click Jobs en pausa/configuración sin abrir `SesionMenu` ni reactivar música/ambiente.

## Hallazgo 2 — hover faltante en widgets vanilla preservados

`PielVanillaJobs` dibuja hover/foco para botones y sliders vanilla, pero el sonido `UI_PASAR` sólo estaba implementado dentro de widgets Jobs propios y algunos componentes especializados.

### Corrección

`EscuchaCliente` mantiene estado débil por instancia (`WeakHashMap`) para botones/sliders vanilla visibles en una superficie Jobs. Al entrar con ratón o foco de teclado dispara `UI_PASAR` una sola vez.

- los widgets Jobs propios se excluyen para no duplicar sonido;
- cambiar de pantalla limpia el estado;
- `MezclaAudio` conserva además su debounce global de 80 ms.

## Hallazgo 3 — retorno de servidor perdía contexto

`retornoDesdeJuego` reconducía Title, Multiplayer o Realms siempre a `PantallaNivel`. Eso impedía caer en vanilla, pero también descartaba el contexto de servidor. Además, una desconexión normal de Minecraft puede terminar en `TitleScreen`, así que no basta con reconocer sólo `JoinMultiplayerScreen`.

### Corrección

- mientras existe un nivel cargado se conserva si `Minecraft#getCurrentServer()` identifica una conexión remota;
- `LoggingOut` captura ese estado antes de que `clearLevel` pueda borrar el contexto;
- si el logout era remoto, tanto `TitleScreen` como `JoinMultiplayerScreen` se reconducen a `PantallaMultijugadorJobs(new PantallaNivel())`;
- si era un mundo local, Title/Realms continúan a `PantallaNivel`;
- Cancelar/error antes del login sigue usando la `PantallaMultijugadorJobs` original como padre de `ConnectScreen`.

Con esto:

1. cancelar conexión → lista Jobs;
2. fallo al conectar → lista Jobs;
3. salir manualmente del servidor aunque vanilla abra Title → lista Jobs;
4. kick/pérdida de conexión → puede mostrarse primero el mensaje de desconexión y, al continuar a Multiplayer/Title, se reconduce a lista Jobs;
5. salir de un mundo local → main Jobs;
6. ESC desde la lista multijugador devuelta → main Jobs en una acción.

## Fronteras que no cambian

- música y camas se detienen con login/tick de gameplay;
- chat, inventario, contenedores y pantallas no Jobs con mundo cargado no reciben piel, transición, hover ni click Jobs;
- Video Settings sigue vanilla e intocable;
- los fondos 10–31, música empaquetada y servidor oficial no se modifican;
- Escape y Cancelar de Multiplayer siguen usando `super.onClose()` con guard idempotente.

## Pruebas manuales prioritarias

1. Entrar a un servidor y confirmar que la música/ambiente Jobs desaparecen totalmente.
2. Abrir pausa y Config Jobs dentro del servidor; verificar click/hover Jobs sin música.
3. Abrir chat, inventario y Video Settings; verificar que no reciben sonido Jobs.
4. Salir manualmente del servidor; debe abrir Multijugador Jobs incluso si vanilla intenta abrir Title.
5. Forzar error/kick y continuar desde la pantalla de desconexión; debe terminar en Multijugador Jobs.
6. Cancelar una conexión en progreso; debe volver a la misma lista Jobs.
7. Introducir IP inválida/error de conexión; debe volver a la misma lista Jobs.
8. Pulsar ESC/Cancelar una sola vez en Multiplayer; no debe necesitar repetición.
9. Probar navegación por teclado sobre widgets vanilla preservados; el hover debe dispararse una vez por entrada de foco, no continuamente.
10. Salir de un mundo singleplayer; debe volver al main Jobs, no a Multijugador.

## Entrega

Versión objetivo: **0.35.0**  
Artefacto: **`jobsmenu-0.35.0.jar`**  
Destino de prueba: `test-1`
