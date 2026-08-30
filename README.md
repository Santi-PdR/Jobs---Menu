# Jobs · Aviso a los ocupantes

Mod **de cliente** que reemplaza los menús de Minecraft por los del servidor **Jobs**: un aviso fotocopiado
y pegado con cinta a la pared de un pasillo amarillo que no se termina. Dice en qué nivel estás, cuánto
cuesta la salida al siguiente, y cuánto falta para la próxima ronda de los **Executores**.

Al fondo del pasillo hay un vano oscuro. Cada tanto algo lo cruza.

El fondo va cambiando de nivel solo. Entre uno y otro se corta la luz.

La rotacion actual tiene **15 niveles**: diez recintos procedurales y cinco fondos suministrados integrados con luz, ambiente y frases propias. Ver [`docs/NIVELES_10_14.md`](docs/NIVELES_10_14.md).

No añade objetos, ni entidades, ni mecánicas. Sólo cambia lo que ves antes de entrar a trabajar.

![Vista previa del menú](docs/vista_previa.png)

| | |
|---|---|
| Versión | **0.10.0** |
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente (el servidor no necesita el mod) |

## Qué trae la 0.10.0

Esta es la versión actual de Jobs Menu. No cambia pantallas de mods ajenos ni
toca el mundo: concentra el trabajo en el ciclo de vida del menú, el audio, la
accesibilidad y la legibilidad de sus quince recintos.

- **Transición coherente por frame.** La escena captura nivel, luz y estado del
  apagón en un mismo instante. La planta, el papel y los eventos ya no pueden
  cruzar la frontera de un nivel en momentos distintos.
- **Accesibilidad respetada.** Movimiento reducido congela la animación completa;
  destellos reducidos conserva la lectura sin parpadeos. Los controles mantienen
  sus hitboxes nativas, entran en Tab y tienen narración vanilla.
- **Audio con lifecycle controlado.** El tema del menú tiene una única instancia,
  depende de `Master` y del volumen del aviso, sobrevive a Opciones/Mods y se
  invalida al recargar recursos o entrar a un mundo. Las camas ambientales se
  detienen sin quedar huérfanas y los eventos respetan silencio y ducking.
- **Personalización útil.** Se añadieron volumen maestro del aviso con M,
  rotación en calma (24 s o 48 s) y fecha del turno. Los cambios de configuración
  se aplican al instante y se guardan con límite de escritura, también al salir.
- **La Suspensión.** Una vez cada aproximadamente 45–52 minutos, el edificio
  queda a oscuras durante 22 segundos: la luz baja sin parpadeos, el ambiente se
  reduce a su respiración más baja, la música cede y el rótulo avisa que el
  edificio suspira. Es un evento raro del fondo, no una mecánica ni un susto.
- **Diez funciones nuevas de percepción.** Alto contraste, texto grande, papel
  limpio, guía de lectura, estado de instalación, respiración de cámara
  independiente, duración configurable de avisos, presencia y eventos
  ambientales separables, y control de La Suspensión. Todo se integra en la
  pantalla de Opciones nativa y conserva los valores por defecto anteriores.
- **Fondos revisados individualmente.** Los diez niveles conservan arquitectura
  propia, materiales distinguibles, luz principal, rebotes y un punto focal. El
  Trono fue ajustado para que el ábside, el haz cenital, las columnas y el
  estrado conduzcan la mirada hacia un asiento vacío realmente legible.
- **Entrega verificable.** La auditoría estática, el procedimiento reproducible de
  compilación y el informe de compatibilidad están sincronizados con Forge 47.x,
  Java 17 y el nombre real del JAR. La evolución vigente está en
  [`docs/EVOLUCION_6.md`](docs/EVOLUCION_6.md) con su catálogo
  [`docs/CATALOGO_MEJORAS_Y_FUNCIONES.md`](docs/CATALOGO_MEJORAS_Y_FUNCIONES.md)
  y su informe final [`docs/INFORME_FINAL_EVOLUCION_6.md`](docs/INFORME_FINAL_EVOLUCION_6.md).
  El historial de decisiones está en
  [`docs/PROPUESTA_EVOLUCION_2.md`](docs/PROPUESTA_EVOLUCION_2.md) y
  [`docs/EVOLUCION_4.md`](docs/EVOLUCION_4.md).

## Evolución reciente

La etapa 1 añadió duración de estancia configurable, salto manual de nivel,
perfil accesible, modo de bajo consumo y continuidad del ambiente al navegar
pantallas hijas. La etapa 2 dio a cada uno de los diez fondos una mejora
artística propia (una fila implementada por escenario en la matriz de
auditoría de fondos) y rediseñó el Trono desde cero. El build automatizado con Java 17 está activo;
la prueba final dentro de Minecraft sigue siendo manual: ver [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md).

## Historial resumido

Las versiones anteriores añadieron los diez recintos, la pausa tematizada, la
ruta de música local, las camas ambientales, el Trono y la primera auditoría
profesional. El detalle histórico que todavía importa está en
[`CHANGELOG.md`](CHANGELOG.md); este README conserva sólo el estado vigente para
evitar instrucciones antiguas o afirmaciones desactualizadas sobre REQUIEM.

## Compilacion y despliegue

La entrega normal ya no se compila en la PC del usuario. GitHub Actions usa Java 17, ejecuta `tools/verificar.py`, compila con Forge/Gradle y, solo si todo termina correctamente, actualiza la release rodante `dev-latest` con `jobsmenu-latest.jar`.

La **unica instancia de prueba y despliegue** es:

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods
```

No se mantienen rutas alternativas para `jobs-2`, `Test2.0` ni otras instancias. El procedimiento completo y el PowerShell canonico estan en [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md).

Para una prueba normal, el usuario solo abre PowerShell y pega el bloque de despliegue: descarga el ultimo JAR que ya paso CI, valida su cabecera antes de tocar la instalacion actual y reemplaza solamente los JAR de Jobs Menu dentro de `test-1\\mods`.

El build local queda disponible solo para desarrollo o diagnostico:

```powershell
.\gradlew.bat clean build --no-daemon
```

Debe ejecutarse con JDK 17 y terminar en `BUILD SUCCESSFUL`. El artefacto versionado local es `build\\libs\\jobsmenu-0.10.0.jar`.

## Herramientas sin JDK

```powershell
python tools\verificar.py       # versiones, idiomas, JSON, ASCII, llaves, símbolos, audio y niveles
python tools\vista_previa.py    # dibuja el menú a PNG para revisar la escena
python tools\vista_previa.py --contacto docs\contacto-actual.png   # los diez niveles juntos
python tools\vista_previa.py --presencia docs\presencia.png     # la manifestación del fondo, paso a paso
python tools\sonidos.py         # regenera las 73 piezas sintetizadas (74 OGG con la música; requiere numpy, scipy y soundfile)
```

## Documentación

Todo el diseño —canon del servidor, identidad, paleta, voz, alcance por fases y reglas de trabajo— está en
[`CONTEXTO.md`](CONTEXTO.md). Para la entrega de esta evolución: [`CHANGELOG.md`](CHANGELOG.md),
[`KNOWN_ISSUES.md`](KNOWN_ISSUES.md), [`docs/EVOLUCION_6.md`](docs/EVOLUCION_6.md),
[`docs/PLAN_EVOLUCION_6.md`](docs/PLAN_EVOLUCION_6.md),
[`docs/CATALOGO_MEJORAS_Y_FUNCIONES.md`](docs/CATALOGO_MEJORAS_Y_FUNCIONES.md),
[`docs/INFORME_FINAL_EVOLUCION_6.md`](docs/INFORME_FINAL_EVOLUCION_6.md),
[`docs/DIRECCION_ARTISTICA.md`](docs/DIRECCION_ARTISTICA.md),
[`docs/FONDOS_EXPLICADOS.md`](docs/FONDOS_EXPLICADOS.md),
[`docs/checklist-manual.md`](docs/checklist-manual.md), [`docs/compatibilidad.md`](docs/compatibilidad.md),
[`docs/musica.md`](docs/musica.md) y [`docs/AUDITORIA_FONDOS_50X10.md`](docs/AUDITORIA_FONDOS_50X10.md).
