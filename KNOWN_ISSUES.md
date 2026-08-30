# Riesgos y pruebas pendientes — 0.10.0

Este archivo separa lo que fue verificado estáticamente de lo que todavía necesita
una ejecución real de Minecraft Forge 1.20.1.

## Evolución 6 (rama `arena/01a04ff1-jobs-menu`) — pendientes 2026-08-29

- **Build no ejecutable en este entorno.** No hay JDK 17 instalado y la red hacia
  `services.gradle.org` / `maven.minecraftforge.net` está bloqueada (errores de
  conexión SSL). El `clean build` con Java 17, el JAR `jobsmenu-0.10.0.jar` y
  el despliegue en una instancia deben hacerse localmente siguiendo el
  procedimiento del README. No se presenta ningún JAR de esta rama como
  validado.
- **Validación dentro de Minecraft pendiente para las 10 mejoras artísticas.**
  Las filas AD-15, DE-17, SE-11, NA-22, SA-11, BI-12, IN-14, CA-13, CI-11 y
  TR-09/10/11/16/17 están implementadas y verificadas por el espejo Python,
  pero la comprobación visual en el juego real (todas las resoluciones, GUI
  scale, durante transición y con la hoja encima) sigue pendiente.
- **Salto manual y continuidad en runtime.** El salto de nivel con F y las
  camas vivas en pantallas hijas están verificados estáticamente y por el
  diseño del ciclo de vida; necesitan la prueba real (rotar con Opciones
  abiertas, entrar/salir de mundo y servidor).
- **Bloque PowerShell del README.** Fue reforzado (despliegue por fases con
  verificación SHA256, JDK 17 completo con `JAVA_HOME`, comprobación del
  wrapper y de la rama) y revisado estáticamente, pero **no ejecutado**: este
  entorno no tiene PowerShell. La primera ejecución real en Windows es también
  la primera prueba del pipeline; el bloque se detiene con mensaje claro en
  cada punto de fallo y no toca los JARs existentes hasta validar el nuevo.
- **Perfil accesible y bajo consumo.** Su comportamiento de opciones está
  verificado estáticamente; la legibilidad del recinto con ambas opciones
  activadas necesita revisión en Minecraft.

## Incidente de despliegue registrado — 2026-08-29

La terminal Windows usada para la prueba estaba en la rama
`arena/01a04e0d-jobs-menu`, no en la rama de esta sesión
`arena/01a04e24-jobs-menu`. El bloque correcto debe detenerse en ese punto.

Además, el `PATH` tenía Java `21.0.12` y no tenía una instalación real de Python;
el comando `py` resolvió al alias de Microsoft Store. Gradle terminó con
`BUILD SUCCESSFUL`, pero ese resultado corresponde al snapshot 0.9.0 y no
certifica el mod 0.10.0 ni Java 17. El JAR esperado `jobsmenu-0.10.0.jar` no
existió en `build\libs`.

El bloque antiguo también tenía dos fallos de operación: no comprobaba de forma
segura todos los códigos de salida cuando se pegaban líneas sueltas y pasaba un
objeto `FileInfo` directamente a `Copy-Item`, que pudo resolverse contra el
repositorio en vez de contra `test-1\mods`. El README ya fue corregido. No hay
evidencia de que se haya borrado un JAR válido: la copia del JAR anterior falló
antes de `Remove-Item`, y el nuevo nunca se copió.

Para repetir la prueba válida: abrir una terminal nueva, activar JDK 17 y
Python 3, situarse en `arena/01a04e24-jobs-menu` y pegar el bloque completo,
no línea por línea. Revisar `test-1\mods` y la carpeta fechada
`test-1\jobsmenu-backups` antes de iniciar Minecraft.

## Pendiente de probar en Minecraft

1. Abrir el título, redimensionar, cambiar GUI scale y recorrer todos los botones
   con mouse, Tab, Shift+Tab, Enter, Espacio, Escape y flechas.
2. Confirmar los bordes de hitbox en 854x480, 1280x720, una ventana estrecha y
   una ventana de poca altura.
3. Probar el flujo completo de salida en mundo local, servidor dedicado, Realms,
   desconexión inesperada y F3+Esc.
4. Verificar la pista activa —la original por defecto o una copia local autorizada—
   con Master en 0/100, Music en 0/100, volumen del aviso en 0/100, el interruptor
   M, Opciones, F3+T, Alt+Tab, minimizar y cierre del juego.
5. Cambiar o quitar una pista de `jobsmenu-musica/` en la raíz de la instancia con
   el paquete interno ya activo y comprobar que se recarga sin duplicar el sonido.
6. Activar resource packs, cambiar idioma y usar pantallas aportadas por mods de
   configuración o rendimiento.
7. Dejar el menú abierto durante varios ciclos y confirmar que los diez niveles,
   eventos raros y camas de audio no forman una repetición evidente.
8. Mantener una sesión hasta La Suspensión (aprox. cada 45–52 min) y comprobar
   en vivo el apagón de 22 s, el 4 % de luz, el silencio parcial, el suspiro, la
   nota del rótulo, la música atenuada y que no se dispare con la rotación fija.

## Riesgos conocidos

- `MusicaPropia` solo acepta OGG Vorbis. Un archivo renombrado o estéreo puede
  ser rechazado por Minecraft; el log debe informar la cabecera inválida.
- La integración con otros mods solo se puede confirmar con el modpack real.
  La regla defensiva actual es no tocar subclases de `OptionsScreen`, no
  envolver `ModListScreen` y no interceptar pantallas de configuración ajenas.
- No hay un perfilador de GPU dentro del repositorio. El espejo Python valida
  composición y geometría, pero no mide el coste de `GuiGraphics` en hardware.
- Texto grande, alto contraste y papel limpio se validaron estáticamente; sus
  bordes de layout necesitan probarse en GUI scale extremos y con resource packs
  que alarguen las cadenas.
- La Suspensión puede desactivarse desde Opciones, pero el timing, el ducking y
  la ausencia de duplicados de audio necesitan una sesión real de 52 minutos.
- En este entorno no está instalado Java 17; por eso `./gradlew clean build` no
  puede ejecutarse aquí. La rama publicada no incluye un workflow de GitHub porque
  la conexión de Arena no tiene permiso `Workflows`; el build debe ejecutarse
  localmente con JDK 17. No se debe presentar el JAR como validado hasta que el
  build local termine con `BUILD SUCCESSFUL` y se pruebe dentro de Minecraft.

## Mitigaciones ya aplicadas

- Validación estática de idiomas, `sounds.json`, recursos, ASCII, símbolos y
  nivel de `nivel_fijo` mediante `python3 tools/verificar.py`.
- Guardado diferido de configuración con vuelco al cerrar una pantalla.
- Invalidez explícita de instancias de audio después de recarga de recursos y
  entrada a un mundo.
- Movimiento y destellos reducidos independientes, con escena legible aun con
  ambas opciones activadas.
- Bloque PowerShell del README reforzado: despliegue por fases con hash,
  JDK completo, wrapper y rama correcta; revisado estáticamente, pendiente de
  su primera ejecución real.
