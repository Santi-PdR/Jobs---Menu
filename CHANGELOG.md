# Registro de cambios

## Evolución 6 — Bloque PowerShell de despliegue reforzado — 2026-08-29

- El bloque de compilar/desplegar del README se reescribió con **despliegue por
  fases**: el JAR nuevo entra a `mods` como `.pendiente` (el launcher ignora lo
  que no termina en `.jar`), se compara su SHA256 con el compilado, recién
  entonces se respaldan y borran los JARs anteriores, y el `.pendiente` pasa a
  su nombre final. Nunca hay una ventana con cero JARs ni dos JARs activos.
- Valida **JDK 17 completo**: `java` + `javac`, y si `JAVA_HOME` está definido
  comprueba que su `java` también sea 17 (gradlew.bat prioriza JAVA_HOME).
- Comprueba `gradle\wrapper\gradle-wrapper.jar` antes de compilar y avisa que
  está ignorado por `.gitignore`; comprueba que `git` exista y lee la rama con
  `--show-current` y respaldo a `rev-parse`.
- El bloque usa solo ASCII (independiente de la página de códigos de la
  consola). Revisado estáticamente (balance, ASCII, estructura); **sin
  ejecutar**: este entorno no tiene PowerShell ni JDK. La primera ejecución
  real es en el PC del owner.

## Evolución 6 — Documentación completa — 2026-08-29

- `docs/PLAN_EVOLUCION_6.md`: las 31 propuestas evaluadas (13 mejoras de
  sistemas + 10 artísticas + 8 funciones) con problema/solución/beneficio/
  riesgo/coste/impacto/decisión.
- `docs/DIRECCION_ARTISTICA.md` (lenguaje visual, cámara por nivel,
  materiales, iluminación y reglas) y `docs/FONDOS_EXPLICADOS.md` (los diez
  recintos con su mejora de la Evolución 6).
- `docs/musica.md`, `docs/compatibilidad.md` y `docs/checklist-manual.md`
  actualizados: vigilancia de fantasma, camas vivas en pantallas hijas,
  opciones nuevas (duración de estancia, salto F, perfil accesible, bajo
  consumo) y rama correcta `arena/01a04ff1-jobs-menu`.
- README: lista de documentación al día. Verificador en verde (1 aviso).
- Reintento de build: gradle.org y maven.minecraftforge.net siguen bloqueados
  (HTTP 000); el `clean build` con Java 17 queda para el equipo local.

## Evolución 6 — Segunda auditoría y cierre — 2026-08-29

- Revisión de los propios cambios: imports (eliminado el import muerto de
  `Nivel` en `GestorAmbiente`), métodos sin llamadas (ninguno: los detectados
  se referencian por `this::` o `@SubscribeEvent`), ASCII en todos los
  archivos tocados, llaves balanceadas, espejo Java↔Python verificado método a
  método y claves de idioma ES/EN idénticas.
- Rendimiento: las matrices locales por frame de `Servicio.haz`,
  `Biblioteca.paginasDobladas` y `Natatorio.marcasProfundidad` pasan a
  constantes `static final`; el colgador central de la bandeja de cables de
  `Servicio` deja de crear un arreglo temporal.
- Documentación contradictoria corregida: el bloque PowerShell del README
  validaba la rama anterior `arena/01a04e24-jobs-menu`; ahora exige
  `arena/01a04ff1-jobs-menu`. CONTEXTO actualizado (tabla de opciones,
  sección 4.2, fila 0.10.0-E6 y cabecera con la rama correcta).
- `tools/vista_previa.py` acepta `--nivel N` con espacio; cabecera de la
  matriz de fondos con la rama y las filas implementadas.

## Evolución 6 — Etapa 2: dirección artística de los diez fondos — 2026-08-29

- Un commit por escenario, cada uno con su espejo en `tools/vista_previa.py` y
  su fila marcada en `docs/AUDITORIA_FONDOS_50X10.md`:
  - Trono rediseñado (`08ed9bf`): tarima 1.18, estrado de seis escalones,
    ábside con tres dovelas concéntricas, hueco de corona ausente y
    estandartes torcidos.
  - Administración: abertura de mantenimiento lateral (AD-15).
  - Depósito: lona de carga caída (DE-17).
  - Servicio: bandeja de cables con colgadores y bucle suelto (SE-11).
  - Natatorio: sarro bajo el rebosadero (NA-22).
  - Sala de piedra: dovelas visibles en el arco más cercano (SA-11).
  - Biblioteca: arco de acceso entre estantes (BI-12).
  - Invernadero: pasarela oxidada sobre los cultivos (IN-14).
  - Catacumbas: pasadizo estrecho detrás del arco del fondo (CA-13).
  - Cisterna: galería de mantenimiento sobre el agua (CI-11).
- `tools/vista_previa.py` acepta `--nivel N` (forma con espacio) además de
  `--nivel=N`.
- Verificador estático en verde (1 aviso: `gradle-wrapper.jar`). Build con
  Java 17, JAR y prueba en Minecraft siguen pendientes (entorno sin JDK 17 ni
  wrapper, red bloqueada a gradle.org/maven.minecraftforge.net).

## Evolución 6 — Etapa 1: configuración, continuidad del ambiente y bajo consumo — 2026-08-29

- `ConfigTurno` ampliado: `duracion_estancia` (15–90 s), `bajo_consumo` y
  `perfil_accesible` (enciende juntas movimiento reducido, destellos reducidos,
  alto contraste y texto grande; los ajustes manuales lo desactivan). Accesos
  blindados contra config sin cargar.
- Continuidad del ambiente por visita (`SesionMenu` + `mantenerCamas()` en el
  tick del cliente): las camas ambientales sobreviven a Opciones/Mods abiertos
  y a la rotación; solo entrar a mundo o salir del menú las detiene.
- Salto manual de nivel (tecla F) con antirrepetición y sonido de alternar.
- Bajo consumo enganchado al render: sin polvo, grano, presencia, motas ni
  respiración de cámara; el recinto y su audio intactos.
- Vigilancia de instancia fantasma de `GestorMusica` blindada contra pausa y
  falta de foco; diagnóstico oculto Ctrl+D (no documentado en la UI).
- `PantallaAjustesAviso` sin `addTitle` (no existe en 1.20.1).
- Textos ES/EN sincronizados.

## Segunda auditoría — 2026-08-29

- Se ejecutó la segunda auditoría estática sobre `8e5c0ef`.
- La auditoría quedó con 0 fallos y 1 aviso: falta `gradle-wrapper.jar`; el
  procedimiento manual usa Gradle 8.1.1 descargado directamente.
- Se registraron los ocho errores de `compileJava` detectados en Windows y su
  corrección en `8e5c0ef`.
- Se deja explícito que el build posterior a esa corrección, el JAR 0.10.0 y la
  prueba dentro de Minecraft todavía están pendientes.
- El informe completo está en [`docs/AUDITORIA_SEGUNDA.md`](docs/AUDITORIA_SEGUNDA.md).

## Corrección de entrega — 2026-08-29

- Se corrigió el bloque PowerShell del README para que no cambie de rama ni
  actualice `main` automáticamente. Ahora exige explícitamente
  `arena/01a04e24-jobs-menu` antes de compilar.
- Se corrigió la captura de `java -version` en Windows PowerShell, cuyo texto
  llega por `stderr` y antes podía quedar vacío.
- Se evita aceptar los alias `py`/`python` de Microsoft Store como si fueran
  una instalación real de Python.
- El artefacto se valida después de leer `mod_version`, y el despliegue ya no
  puede declarar éxito si el JAR no existe o si un paso previo falló.
- El backup de JARs anteriores usa `-LiteralPath` y `.FullName`; así no intenta
  resolver un archivo de `mods` contra el directorio del repositorio.
- La sesión del 29/08/2026 se registró como **no válida para certificar 0.10.0**:
  se ejecutó en `arena/01a04e0d-jobs-menu`, con Java 21 y sin Python disponible.
  Aunque Gradle terminó con `BUILD SUCCESSFUL`, produjo el snapshot 0.9.0 y no
  se desplegó el JAR 0.10.0. La salida final de éxito del bloque anterior era
  incorrecta porque se continuó pegando comandos después de varios `throw`.

## 0.10.0 — Evolución perceptible

- Se corrigió un crash runtime del JAR 0.9.0 al pasar el cursor por una fila
  cuando `jobsmenu:ui.pasar` no estaba presente en el registro. Los sonidos
  ahora tienen resolución segura, respaldo vanilla para UI y omisión controlada
  para capas ambientales faltantes.
- Se añadieron diez funciones configurables: alto contraste, texto grande,
  papel limpio, guía de lectura, estado de instalación, respiración de cámara,
  duración de avisos, presencia separable, eventos ambientales separables y
  control de La Suspensión.
- Se revisaron y documentaron cincuenta mejoras perceptibles de interfaz,
  escena, audio, lifecycle, rendimiento, compatibilidad y entrega en
  [`docs/EVOLUCION_4.md`](docs/EVOLUCION_4.md).
- La pantalla de Opciones mantiene controles nativos, navegación por teclado,
  foco, tooltips y guardado diferido.
- La hoja y la pausa comparten contraste, papel limpio y tipografía grande sin
  reemplazar pantallas de terceros.
- La Suspensión puede desactivarse sin reiniciar el juego y conserva la música,
  el silencio parcial y el rótulo localizado cuando está activa.
- Se sincronizaron compatibilidad, música, checklist, riesgos y el bloque
  PowerShell copiable para traer la rama, compilar y desplegar en SKLauncher `test-1`.
- La auditoría de la siguiente etapa deja 50 criterios específicos para cada
  uno de los diez fondos en [`docs/AUDITORIA_FONDOS_50X10.md`](docs/AUDITORIA_FONDOS_50X10.md);
  no se cuentan como implementados hasta tener diff, comparación visual y prueba
  dentro de Minecraft.
- El estado completo de la corrección de entrega y lifecycle está en
  [`docs/EVOLUCION_5.md`](docs/EVOLUCION_5.md).
- El botón de ajustes de `OptionsScreen` evita duplicarse durante reconstrucciones
  y ahora ofrece un tooltip localizado para narración y lectura asistida.
- El Trono usa una fuga lateral y un horizonte más bajo, conservando el eje de la
  tarima en el espejo estático para que la sala no se lea como un pasillo simétrico.
  También suma un dintel roto y una losa escalonada de primer plano, sin cerrar el
  foco del asiento vacío.
- Las pulsaciones de luz de plantas y tratamiento común pasan por una compuerta
  compartida de `destellos_reducidos`; eventos luminosos, vapor y reflejos de agua
  se estabilizan, sin acoplar movimiento visual y luminancia.
- El Trono suma un cascote lateral sincronizado con la silueta ambiental; cae fuera
  del eje y desaparece antes de alcanzar la tarima. Los escalones también ganan
  desgaste localizado y un único brillo de canto. La base del ábside incorpora
  humedad localizada, limitada a las juntas bajas.
- Administración gana un dintel pesado, una placa lateral remachada y una
  interrupción puntual de la fila de luminarias para evitar la repetición.
- Depósito gana una carga suspendida con soldaduras, una bahía abierta por un
  pilar ausente y una puerta lateral de muelle con jambas y umbral.
- Servicio gana compuerta de inspección, válvula mecánica, manguera caída y cal
  mineral localizada para que la red técnica tenga objetos anclados.
- Natatorio gana una ventana alta rota, una rejilla de desagüe lateral y tres
  placas de profundidad ancladas al borde para reforzar la arquitectura acuática.
- Biblioteca gana páginas dobladas, polvo localizado y condensación restringida
  al ventanal para separar abandono, gravedad y humedad.
- Invernadero gana un panel de techo roto, una canaleta con depósito y una
  puerta lateral de vidrio entreabierta para dar ruta y escala al abandono.
- Catacumbas gana drenaje, reparación de sillar y arañazos de uso para
  reforzar humedad, historia y escala sin iconografía explícita.
- Cisterna gana compuerta, entrada de agua y marcas de nivel para reforzar
  mantenimiento, origen y escala sobre el reflejo oscuro.

> La evolución está validada por auditoría estática y renders del espejo. El
> build Forge, el SoundEngine, la navegación real y el modpack siguen requiriendo
> prueba dentro de Minecraft 1.20.1 con Java 17.

## 0.9.0 — Evolución profesional

- Se integró la auditoría de las ramas `arena/01a04c05-jobs-menu` y
  `arena/01a04e0d-jobs-menu` en la rama de trabajo actual, conservando las
  mejoras verificables y sin modificar `main`.
- Se añadió un snapshot inmutable de rotación para mantener nivel, luz y
  transición coherentes dentro de un frame.
- Se evitó volver a partir la nota rotativa en cada frame.
- Se corrigió la recarga de una pista personalizada cuando el paquete interno
  ya estaba activo y el archivo había cambiado.
- Se corrigió la workflow para usar Java 17, `./gradlew`, el wrapper y el
  nombre real del artefacto.
- Se revisaron las diez composiciones y se reforzaron el óculo, el abismo, los
  puentes, el estrado y el foco del Trono.
- Se añadieron documentación de compatibilidad, auditoría, checklist manual y
  estados explícitos de riesgos pendientes.

> El renderer sigue siendo procedural y el mod continúa siendo de cliente.
> Las comprobaciones estáticas no sustituyen la prueba dentro de Minecraft.

## 0.8.3

- Música REQUIEM convertida a una ruta de recurso directa, mono y `stream:false`.
- Revisión visual inicial del salón del Trono y respiración de cámara.

## 0.8.2 y anteriores

Consultar las entradas históricas en [README.md](README.md).
