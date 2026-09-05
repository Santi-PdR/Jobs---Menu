# Auditoría 0.37.0 — Continuidad de Multiplayer y documentación

Fecha: 2026-09-05

## Objetivo

Continuar el endurecimiento de 0.35.0/0.36.0 sin ampliar Jobs hacia gameplay ni volver a reconstruir Video Settings. La revisión se concentró en el lifecycle de `PantallaMultijugadorJobs`, feedback de recarga y coherencia documental.

## Hallazgo 1 — F5 perdía contexto de selección

En 0.36.0 `refrescarLista()` ya evitaba la pantalla vanilla intermedia, pero creaba una nueva `PantallaMultijugadorJobs` sin transportar selección. El usuario podía estar inspeccionando un servidor y, tras F5/Actualizar, tenía que localizarlo de nuevo.

### Corrección 0.37.0

- antes de reemplazar la pantalla se obtiene la IP del `OnlineServerEntry` seleccionado;
- la pantalla nueva recibe sólo esa IP como `servidorPreferido`;
- tras cargar la lista real y asegurar el servidor oficial, se recorren las Entries recién creadas;
- si una IP coincide, se llama `setSelected()` y `onSelectedChange()`;
- no se conserva ni reutiliza una referencia a la Entry vieja.

La decisión de transportar sólo IP es deliberada: una entrada LAN es efímera y debe depender del nuevo detector LAN, no de un objeto perteneciente a la pantalla anterior.

## Hallazgo 2 — Recargas repetidas podían competir

El guard `cerrando` protegía ESC/Cancelar, pero 0.36.0 no lo activaba antes de F5. Aunque `setScreen()` normalmente reemplaza la pantalla inmediatamente, dos entradas muy próximas no necesitaban tener una segunda ruta válida.

### Corrección 0.37.0

`refrescarLista()` marca `cerrando = true` antes de `setScreen()`. El botón Actualizar queda inactivo en esa instancia y otra llamada F5 sobre la pantalla saliente se descarta.

## Hallazgo 3 — Feedback y texto de F5

La recarga por botón ya tenía sonido Jobs por el widget, pero el atajo F5 no emitía feedback propio. Además, la ayuda inferior contenía el literal duro `JOBS/SERVER`, que no describía realmente la acción ni seguía localización.

### Corrección 0.37.0

- F5 por teclado emite `UI_ALTERNAR` una vez;
- `refrescarLista()` no emite sonido adicional, por lo que el botón Actualizar no duplica audio;
- el indicador pasa a `F5 // <selectServer.refresh>` y reutiliza la traducción de Minecraft (`Actualizar`/`Refresh`).

## Hallazgo 4 — Documentación desincronizada

`CHANGELOG.md` terminaba en 0.34.0 aunque 0.35.0 y 0.36.0 ya estaban en `main`. Además, `docs/` acumulaba auditorías históricas sin un índice que dijera claramente cuáles gobiernan el estado actual.

### Corrección 0.37.0

- se recuperan las entradas 0.35.0 y 0.36.0 en el changelog y se añade 0.37.0;
- se sincronizan README, CONTEXTO, KNOWN_ISSUES, checklist y compatibilidad;
- se crea `docs/README.md` con orden de lectura, contratos permanentes y separación vigente/histórico;
- se añade `tools/verificar_continuidad.py` al workflow para fijar selección F5, guard de recarga, feedback, índice y presencia de versiones recientes en el changelog.

## Contratos preservados

Esta entrega **no cambia**:

- Video Settings vanilla;
- cero transiciones Jobs durante gameplay;
- hard-stop inmediato de música y ambiente al entrar a mundo/servidor;
- feedback breve Jobs permitido sólo en superficies propias de pausa/configuración;
- retorno de servidor remoto a Multiplayer Jobs;
- retorno de mundo local al main Jobs;
- servidor oficial `JobsDosh.exaroton.me:56477`;
- fondos, catálogo musical y configuración de pista fija/aleatoria;
- pie limpio del main sin barra global visible de atajos.

## Validación automática esperada

CI debe ejecutar:

1. política de versión;
2. fondos;
3. verificación estática general;
4. UI/música;
5. continuidad Multiplayer/documentación;
6. Forge build con Java 17;
7. JAR `jobsmenu-0.37.0.jar`.

El build Forge es especialmente importante para confirmar que `ServerSelectionList.children()`, `setSelected()` y `JoinMultiplayerScreen.onSelectedChange()` son compatibles con las mappings oficiales 1.20.1 usadas por el proyecto.

## Validación manual pendiente

En `test-1` comprobar:

- seleccionar un servidor guardado → F5 → la misma entrada continúa seleccionada;
- repetir con el botón Actualizar;
- F5 rápido repetido no apila pantallas;
- F5 suena una vez y Actualizar no duplica sonido;
- LAN/ping/MOTD/favicons sobreviven a varias recargas;
- Cancelar/error de conexión vuelven a la lista Jobs;
- ESC/Cancelar siguen cerrando en una acción;
- salida/kick de servidor continúa regresando a Multiplayer Jobs;
- chat/inventario permanecen sin Jobs y pausa/configuración sin transiciones durante gameplay.
