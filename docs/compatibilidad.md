# Compatibilidad y despliegue

## Perfil soportado

| Componente | Estado documentado |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente; el servidor no necesita Jobs Menu |
| Versión del mod | **0.10.0** |
| Artefacto | `build/libs/jobsmenu-0.10.0.jar` |

Jobs Menu dibuja su menú y sus overlays sin reemplazar indiscriminadamente las
pantallas de otros mods. Las opciones del mod viven en la pantalla nativa de
Opciones mediante `OptionsList` y `OptionInstance`, por lo que se conservan la
navegación, la narración, el foco, el teclado, el mouse y el scroll de vanilla.
La compatibilidad efectiva con el modpack debe confirmarse dentro de Minecraft;
la compilación no prueba el render ni el audio.

## Instancia de referencia: SKLauncher `test-1`

La instancia usada para el despliegue documentado es:

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\
```

El JAR debe quedar únicamente en:

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods\jobsmenu-0.10.0.jar
```

Antes de abrir el juego, cierra Minecraft y retira sólo los JARs anteriores que
coincidan con `jobsmenu-*.jar`. No copies backups, tags ni otros mods a `mods`.
El bloque PowerShell del README comprueba el artefacto nuevo antes de borrar
versiones viejas y guarda una copia fechada de la configuración del mod. Se pega
directamente en una terminal nueva; no se genera ningún archivo de PowerShell.

## Límites conocidos de integración

- Embeddium, Oculus, ImmediatelyFast, Sophisticated Backpacks/Core, Architectury,
  Cloth Config, Controlling, Searchables, Chat Heads, 3D Skin Layers, TRansition,
  TRender, LowDragLib y los demás mods del pack deben probarse en conjunto. Jobs
  Menu no debe interceptar sus pantallas ni asumir que su renderer es el activo.
- La mezcla de REQUIEM o de una pista local usa `Master` y el volumen propio del
  mod; las camas ambientales respetan su canal ambiental. Hay que verificar en
  vivo que el gestor de música, el silencio y la suspensión no dejen sonidos
  huérfanos.
- Los `RegistryObject<SoundEvent>` se resuelven con presencia comprobada. Un
  registro ausente ya no puede tumbar el render de un widget: UI vuelve al click
  vanilla y una cama ambiental faltante se omite con un único aviso de log.
- GUI scales extremos, narración, alto contraste, texto grande, movimiento y
  destellos reducidos requieren comprobación manual en la instancia.
- La auditoría estática no reemplaza la prueba con `SoundEngine`, GPU,
  redimensionado, suspensión ni el arranque real del modpack.

## Estado de las comprobaciones

| Comprobación | Estado |
|---|---|
| `python3 tools/verificar.py` | Superada: 0 avisos, 0 fallos |
| JSON de idiomas | Superado |
| `py_compile` de herramientas | Superado |
| `git diff --check` | Superado en la última validación registrada |
| `clean build --no-daemon` | **Pendiente para esta rama con Java 17**; la prueba Windows del 29/08 usó la rama 0.9.0 y Java 21, por lo que no certifica 0.10.0 |
| Arranque Forge en `test-1` | Pendiente |
| Audio, modpack, GPU y GUI scales | Pendientes de prueba manual |

La prueba de despliegue que terminó con `BUILD SUCCESSFUL` no debe marcarse
como build de 0.10.0: se realizó en `arena/01a04e0d-jobs-menu` y el artefacto
esperado no apareció. El bloque vigente del README detiene el proceso si la rama,
la versión, Python o Java no coinciden.

La matriz de aceptación de fondos de la próxima etapa está en
[`AUDITORIA_FONDOS_50X10.md`](AUDITORIA_FONDOS_50X10.md), y el registro de la
corrección de entrega está en [`EVOLUCION_5.md`](EVOLUCION_5.md).

Las incidencias que afectan a la validación runtime se mantienen en
[`KNOWN_ISSUES.md`](../KNOWN_ISSUES.md). Las pruebas paso a paso están en
[`checklist-manual.md`](checklist-manual.md).
