# Evolución 5 — auditoría de entrega, lifecycle y fondos

**Fecha:** 2026-08-29
**Proyecto:** Jobs · Aviso a los ocupantes
**Versión de código:** 0.10.0
**Minecraft:** 1.20.1 · Forge 47.x · Java 17 objetivo
**Rama fijada de esta sesión:** `arena/01a04e24-jobs-menu`

## Estado real

Esta evolución continúa sobre un snapshot estable. No se presenta como terminada
solo porque una parte haya compilado. La auditoría estática pasa; el build correcto
de esta rama con Java 17 y la prueba dentro de Minecraft siguen pendientes.

La matriz específica de fondos está en
[`AUDITORIA_FONDOS_50X10.md`](AUDITORIA_FONDOS_50X10.md): contiene 50 criterios
concretos por cada uno de los diez escenarios, con target y prueba de aceptación.
Las filas todavía no tienen el estado de "implementada" hasta que exista un diff,
una comparación visual y una verificación manual. No se cuentan cambios globales
de color como mejoras de escenario.

## Incidencia de la prueba Windows

La ejecución comunicada desde PowerShell se hizo en
`arena/01a04e0d-jobs-menu`, que es otro snapshot. El archivo de propiedades de esa
rama produjo el artefacto 0.9.0, por eso no apareció `jobsmenu-0.10.0.jar`. También
se usó Java 21.0.12 y el alias de Microsoft Store para Python; ese resultado no
certifica 0.10.0 ni Java 17.

Gradle terminó con `BUILD SUCCESSFUL`, pero el operador continuó pegando comandos
después de los `throw`. Por eso la salida final que decía que el despliegue había
terminado era falsa. La copia del JAR anterior falló antes de `Remove-Item` y no
hay evidencia de pérdida del archivo viejo. El procedimiento corregido está en el
README: valida rama, versión, Java, Python, auditoría y JAR en ese orden, y usa
`-LiteralPath` para los archivos que salen de `mods`.

## Bugs corregidos en esta pasada

- Un reporte runtime del JAR 0.9.0 mostró un crash reproducible al pasar el
  cursor por una fila: `MezclaAudio.gesto` llamaba `.get()` sobre el registro
  ausente `jobsmenu:ui.pasar`. La mezcla ahora comprueba `isPresent()` antes de
  resolver cualquier sonido; usa click vanilla para UI, omite capas ambientales
  faltantes y mantiene música vanilla como último respaldo. La corrección aún
  debe probarse instalando el JAR 0.10.0 dentro de Minecraft.
- El retorno desde la pausa propia podía volver a interceptar el `TitleScreen`
  vanilla y atrapar al jugador de nuevo en `PantallaNivel`. `SesionMenu` ahora
  marca una única salida deliberada al título y `EscuchaCliente` la consume sin
  reemplazarla.
- El botón de ajustes añadido a `OptionsScreen` podía duplicarse si la pantalla
  se reconstruía desde otro listener y no tenía tooltip propio. Ahora la inserción
  es idempotente y el control tiene una descripción localizada.
- Los renglones ahora muestran pressed state, incluida la primera confirmación de
  salida, y un borde terminal sobrio para acciones irreversibles.
- `destellos_reducidos` ahora tiene una compuerta común para pulsos de luz,
  fuego, velas, lámparas, haces, rebote y grano; también evita eventos con
  entrada/salida luminosa y estabiliza los jirones de vapor y reflejos de agua.
  El movimiento espacial puede continuar sin convertirlo en una variación de
  luminancia. La comprobación dentro de Minecraft sigue pendiente.
- La validación OGG comprueba firma Vorbis, mono y 44.100 Hz para todos los
  recursos, no solo un subconjunto.
- Las camas largas de los niveles 6, 8 y 9 se regeneraron con periodos efectivos
  menos repetitivos.
- El Trono dejó de usar un eje perfectamente centrado: la cámara tiene fuga
  lateral y horizonte más bajo, con el ajuste reflejado en `tools/vista_previa.py`.
  La lámina estática de 854×480 conserva la silueta de la silla y la tarima.
- El salón del Trono suma un dintel roto y una losa escalonada en primer plano;
  ambos tienen espejo procedural y dejan libre el eje de lectura.
- El evento ambiental del Nivel 9 añade un cascote lateral sincronizado con la
  misma ventana que la silueta lejana; no cruza la tarima ni introduce otro reloj.
- Los cinco escalones reciben desgaste localizado y un único brillo de canto,
  evitando el acabado dorado uniforme. La piedra baja del ábside suma humedad
  localizada, limitada a sus juntas y sin velo global. La matriz marca TR-01,
  TR-15, TR-22, TR-24, TR-31 y TR-37 como implementados en estático.
- Administración suma un dintel pesado, una placa lateral con cuatro remaches y
  una luminaria fuera de servicio para romper la repetición. La matriz marca
  AD-07, AD-11 y AD-23 como implementados en estático.
- Depósito suma una carga suspendida con soldaduras en los nodos, una bahía con
  pilar ausente y una puerta lateral de muelle con umbral y bisagras. La matriz
  marca DE-04, DE-06, DE-12 y DE-22 como implementados en estático.
- Servicio suma una compuerta de inspección entreabierta, una válvula con aro,
  eje y manija, una manguera caída con brida y cal mineral localizada. La matriz
  marca SE-13, SE-17, SE-18 y SE-24 como implementados en estático.
- Natatorio suma una ventana alta rota, una rejilla de desagüe lateral y tres
  placas físicas de profundidad ancladas al borde. La matriz marca NA-13, NA-14
  y NA-46 como implementados en estático. Todo esto aún requiere validación
  dentro de Minecraft.
- Sala de piedra suma un nicho lateral con fondo oscuro, una marca de
  peregrinación erosionada y cera acumulada bajo el candil. La matriz marca
  SA-14, SA-25 y SA-46 como implementados en estático. Todo esto aún requiere
  validación dentro de Minecraft.
- Biblioteca suma páginas dobladas en estantes cercanos, polvo en cinco
  recovecos alternos y condensación limitada al ventanal. La matriz marca
  BI-22, BI-24 y BI-25 como implementados en estático. Todo esto aún requiere
  validación dentro de Minecraft.
- Invernadero suma un panel de techo roto, una canaleta que termina en
  depósito y una puerta lateral de vidrio entreabierta. La matriz marca IN-12,
  IN-13 e IN-15 como implementados en estático. Todo esto aún requiere
  validación dentro de Minecraft.
- Catacumbas suma un drenaje estrecho, un sillar de reparación y arañazos
  anclados a un umbral. La matriz marca CA-15, CA-24 y CA-25 como implementados
  en estático. Todo esto aún requiere validación dentro de Minecraft.
- Cisterna suma una compuerta de inspección, una tubería de entrada con gota
  y tres marcas de nivel sobre una columna. La matriz marca CI-12, CI-13 y CI-14
  como implementados en estático. Todo esto aún requiere validación dentro de
  Minecraft.
- El procedimiento PowerShell ya no puede confundir una rama parecida, un alias
  de Python, Java 21 o un `BUILD SUCCESSFUL` de otra versión con una entrega válida.

## Contratos preservados

- `main` permanece intacta y no se hace merge automático.
- La rama de trabajo de esta sesión no se cambia por la rama Windows parecida.
- REQUIEM usa `Master` y el slider propio del aviso; no depende de `Music` vanilla.
- Movimiento reducido congela animaciones; destellos reducidos elimina parpadeos.
- Las pantallas de otros mods no se reemplazan indiscriminadamente.
- `OptionsScreen` se intercepta solo por clase exacta y el botón se omite si no
  existe una esquina segura.
- Los backups permanecen fuera de `mods`.
- El atajo administrativo oculto no forma parte de menús, controles, tooltips ni
  documentación pública.

## Backups y commits

Backup específico previo a la próxima reescritura de fondos:

```text
seguridad/2026-08-29/evolucion-5/backup-pre-backgrounds
```

Commits relevantes de la rama:

```text
07dd79e feat: add broken throne hall lintel and foreground slab
6c33af0 feat: lower throne hall horizon for grounded composition
3fa7686 feat: offset throne hall focal axis
d35f9f5 perf: remove transient scene arrays from render loop
668c2a2 fix: make vanilla settings insertion idempotent and narrated
672f62b test: validate per-background audit coverage
13da3f1 fix: release vanilla title after leaving a world
e8902ae docs: keep hidden admin shortcut undocumented
d06c32b docs: correct Windows build and deployment procedure
d518e53 feat: make terminal and pressed button states visible
dcb1afd fix: diversify long ambience loop periods
ddfbdbf test: validate Vorbis channel and sample rate
```

La subida de la rama de esta sesión queda pendiente de resolver el permiso de
GitHub para la workflow; no se volvió a ejecutar un push a ciegas. `main` no fue
modificada.

## Validación ejecutada

```text
python3 tools/verificar.py
Verificacion superada. 0 aviso(s), ningun fallo.

python3 -m py_compile tools/verificar.py tools/vista_previa.py tools/sonidos.py tools/muestras.py
OK

git diff --check
OK

74 OGG inspeccionados con soundfile: todos mono a 44100 Hz
```

No se pudo ejecutar `clean build` en este sandbox por ausencia de Java 17. El
`BUILD SUCCESSFUL` de la prueba Windows pertenece a otra rama y a Java 21; debe
repetirse en la rama correcta antes de generar un JAR de entrega.

## Próximo bloque profesional

1. Activar JDK 17 y Python 3 real en el equipo de prueba.
2. Ejecutar el bloque PowerShell completo en una sola pegada, nunca línea a línea.
3. Implementar y revisar un escenario por commit; el primer ajuste del Trono ya
   está aplicado, pero solo cubre TR-01 y su aceptación de Minecraft sigue abierta.
4. Actualizar el espejo `tools/vista_previa.py` junto con cualquier cambio de
   `EscenaNivel` o una planta.
5. Verificar las 50 filas del escenario cambiado en 320×240, 854×480, 720p, 4K,
   4:3, ultrawide, GUI Scale extremo, movimiento reducido y destellos reducidos.
6. Probar navegación y audio en Title, Pause, Options, Sound, Video, Controls,
   Accessibility, Language, Resource Packs, Mods, Singleplayer, Multiplayer,
   Select World, Direct Connect, Add Server, desconexión y confirmaciones.
7. Solo entonces marcar filas como implementadas, crear backup final y preparar
   la entrega.
