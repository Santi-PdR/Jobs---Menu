# Registro de cambios

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
