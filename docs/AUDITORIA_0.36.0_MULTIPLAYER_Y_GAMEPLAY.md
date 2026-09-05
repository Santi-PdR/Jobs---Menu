# Auditoria 0.36.0 — Multiplayer y transiciones durante gameplay

Fecha: 2026-09-05

## Motivo

La prueba real reporto dos regresiones que 0.34/0.35 no resolvieron completamente:

1. ESC/Cancelar podia seguir necesitando multiples intentos para abandonar Multiplayer.
2. Pausa/configuracion Jobs seguian mostrando animaciones de transicion mientras existia un mundo o servidor cargado.

## Hallazgo 1 — se verificaba la ruta equivocada de cierre

`PantallaMultijugadorJobs` heredaba `JoinMultiplayerScreen` y hacia que su boton Cancelar llamara a `onClose()`. La implementacion 0.34/0.35 consideraba correcto delegar despues a `super.onClose()`.

La fuente real de Forge/Minecraft 1.20.1 muestra que esa equivalencia no existe:

- el boton Cancelar creado por `JoinMultiplayerScreen` ejecuta directamente `minecraft.setScreen(lastScreen)`;
- `JoinMultiplayerScreen` no redefine `onClose()` para hacer eso;
- `Screen.onClose()` llama a `minecraft.popGuiLayer()`;
- ESC entra por `Screen.keyPressed`, que invoca `onClose()` cuando `shouldCloseOnEsc()` es verdadero.

Por tanto, la solucion anterior unifico ESC/Cancelar, pero los unifico sobre una ruta que no replica al boton Cancelar vanilla.

### Correccion 0.36.0

`PantallaMultijugadorJobs` conserva ahora su propio `pantallaPadre`.

- `onClose()` -> `cerrarAlPadre()`;
- boton Cancelar -> `cerrarAlPadre()`;
- `cerrarAlPadre()` aplica `cerrando` y hace `minecraft.setScreen(padreDestino())` una sola vez;
- no se ejecuta `super.onClose()`;
- el fallback del padre es `PantallaNivel`.

Esto deja una unica ruta, pero ahora esa ruta representa correctamente la navegacion esperada.

## Mejora adicional — refresh sin pantalla vanilla intermedia

Antes F5/Actualizar pulsaba el boton vanilla oculto. El callback privado de vanilla construia una nueva `JoinMultiplayerScreen`, y Jobs dependia de `ScreenEvent.Opening` para volver a envolverla.

0.36.0 reemplaza esa cadena por `refrescarLista()`:

- crea directamente `new PantallaMultijugadorJobs(padreDestino())`;
- conserva el mismo padre;
- vuelve a cargar `servers.dat`, ping, LAN y widgets mediante el `init()` normal;
- evita un destino vanilla transitorio y reduce el numero de redirecciones del listener.

## Hallazgo 2 — habia dos sistemas de transicion

Apagar solo `TransicionInterfazJobs` no bastaba. `PulidoInterfazJobs` registra ademas una animacion corta de entrada para cada pantalla.

### Correccion 0.36.0

Mientras `Minecraft.level != null`:

- `usaTransicionJobs()` devuelve false;
- cualquier transicion pendiente se cancela al abrir pantalla;
- login, logout y cada tick de gameplay cancelan defensivamente el estado;
- el render no llama a `TransicionInterfazJobs.dibujar()`;
- no se llama a `PulidoInterfazJobs.notificarApertura()`.

Resultado esperado: Pausa Jobs, Config Jobs y sus subpantallas conservan su tema y controles, pero aparecen directamente sobre gameplay, sin barrido, fundido ni entrada animada.

## Lo que no cambia

- chat, inventario y contenedores siguen fuera de piel/transicion Jobs;
- Video Settings sigue completamente vanilla;
- hover/click Jobs puede funcionar en pausa/configuracion sin reactivar musica;
- musica y ambiente mantienen hard-stop al entrar a gameplay;
- salir/kick/perder conexion de un servidor remoto sigue volviendo a Multijugador Jobs;
- el servidor oficial y los fondos no cambian.

## Pruebas prioritarias en test-1

1. Main -> Multiplayer -> ESC una vez -> main Jobs.
2. Main -> Multiplayer -> Cancelar una vez -> main Jobs.
3. Repetir ambos casos con un servidor seleccionado y sin seleccion.
4. Pulsar F5 y el boton Actualizar varias veces; nunca debe verse Multiplayer vanilla.
5. Direct Connect/Add/Edit y volver; el padre debe seguir siendo Multijugador Jobs.
6. Cancelar una conexion y provocar un error de conexion; debe volver a la lista Jobs.
7. Entrar a servidor -> abrir pausa -> Config -> subpantallas: cero barridos/fundidos/entrada.
8. Repetir en singleplayer.
9. Abrir chat/inventario: siguen sin capas Jobs.
10. Salir del servidor y desde Multijugador Jobs pulsar ESC una vez.

## Entrega

Version objetivo: **0.36.0**  
Artefacto: **`jobsmenu-0.36.0.jar`**  
Destino: `test-1`
