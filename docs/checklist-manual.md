# Checklist manual de aceptación — 0.10.0

Este checklist es para una instancia Forge real. La auditoría estática y el build
no sustituyen las comprobaciones visuales, de navegación, audio y lifecycle.

## Incidente de la prueba anterior

La ejecución del 29/08/2026 quedó sin validar: la terminal estaba en
`arena/01a04e0d-jobs-menu`, tenía Java 21 y el alias de Python de Microsoft
Store, y Gradle produjo el JAR del snapshot 0.9.0. No marcar ninguna casilla de
esta sección por ese resultado. La rama objetivo de la Evolución 6 es
`arena/01a04ff1-jobs-menu`, con JDK 17 y Python 3 reales.

## Preparación

- [ ] Forge 47.x, Minecraft 1.20.1 y Java 17.
- [ ] Instancia limpia y luego instancia con el modpack completo.
- [ ] Respaldar `config/jobsmenu-client.toml` y `options.txt`.
- [ ] Capturar logs antes y después de abrir el menú.

## Pantallas y navegación

- [ ] El título propio solo sustituye a `TitleScreen` vanilla.
- [ ] `01`, `02`, `03` y `04` responden exactamente dentro de su hitbox.
- [ ] El segundo clic de `Renunciar al nivel` es intencional y no accidental.
- [ ] El puntero sobre un control y el foco con Tab producen el mismo estado visual.
- [ ] Tab y Shift+Tab recorren cada botón; Enter y Espacio activan el botón enfocado.
- [ ] Escape permanece bloqueado en el título propio.
- [ ] Escape reanuda desde la pausa propia.
- [ ] Dejar el turno desde la pausa desconecta y muestra el `TitleScreen` vanilla una sola vez, sin volver a envolverlo.
- [ ] Opciones, Mods, Multijugador y las pantallas hijas vuelven al padre correcto.
- [ ] No se duplican widgets al redimensionar.
- [ ] F3+Esc mantiene su pausa vanilla.

## Layout y accesibilidad

- [ ] 854x480, 1280x720, 16:9 y una relación más estrecha.
- [ ] GUI scale mínimo y máximo razonable.
- [ ] Español, inglés y una cadena larga mediante resource pack.
- [ ] Interfaz mínima oculta decoración sin quitar acciones.
- [ ] Alto contraste refuerza tinta y papel sin introducir rojo fuera de Executores.
- [ ] Texto grande re-mide hoja, pausa, filas y texto largo sin solaparse.
- [ ] Papel limpio retira cinta y sombra, pero conserva bordes, texto y acciones.
- [ ] Guía de lectura se puede alternar sin romper foco, hitbox ni narración.
- [ ] Estado de instalación muestra normal, traslado y suspendida sin tapar la cuenta.
- [ ] Movimiento reducido congela agua, telas, fuego, haces, polvo y presencia.
- [ ] Destellos reducidos elimina el parpadeo, pero conserva la lectura.
- [ ] Respiración de cámara se puede apagar sin detener la animación material.
- [ ] Presencia y eventos ambientales se pueden apagar por separado.
- [ ] Duración de avisos funciona entre 4 y 15 segundos y no cambia la hitbox.
- [ ] Tooltips y narración no muestran claves sin traducir.

## Evolución 6 — opciones nuevas

- [ ] `duracion_estancia` entre 15 y 90 s se aplica en vivo: el apagón y el
  cambio de nivel ocurren según el valor; con `rotacion_calma` la estancia se
  duplica.
- [ ] Tecla F salta de nivel con antirrepetición (dos pulsos seguidos no
  avanzan dos niveles), suena el gesto y no interfiere con otros atajos.
- [ ] `perfil_accesible` enciende juntas movimiento reducido, destellos
  reducidos, alto contraste y texto grande; marcar a mano cualquiera de esas
  cuatro desactiva el perfil y persiste al reiniciar.
- [ ] `bajo_consumo` quita polvo, grano, presencia, motas y respiración de
  cámara pero conserva el recinto completo y su audio.
- [ ] Con Opciones o Mods abiertos, rotar de nivel no deja el recinto nuevo en
  silencio (camas vivas en pantallas hijas); salir al mundo sí detiene el
  ambiente.
- [ ] La música no se reinicia al abrir y cerrar Opciones; una segunda instancia
  congelada (p. ej. tras F3+T) se detecta y se invalida sin falsas alarmas por
  pausa o falta de foco.

## Audio

- [ ] La pista activa —original por defecto o copia local autorizada— tiene una
  sola instancia tras redimensionar y navegar por Opciones.
- [ ] Music vanilla no compite dentro del título; vuelve al salir.
- [ ] Master 0 y Music 0 se comportan según la decisión documentada; la música
  del mod depende de Master, no del slider Music.
- [ ] Volumen maestro del aviso 0/100 y tecla M.
- [ ] Volumen de música y ambiente 0/100 en tiempo real.
- [ ] F3+T, resource pack, Alt+Tab, minimizar y recuperar foco.
- [ ] Entrada a mundo local, servidor, Realms, desconexión y cierre.
- [ ] No queda ambiente, evento ni transición al abandonar el menú.
- [ ] Mantener una sesión abierta hasta La Suspensión (aprox. cada 45–52 min):
  apagón de 22 s, luz cercana al 4 %, sin parpadeos, sin motas/presencia/eventos
  visuales, suspiro grave único, nota "El edificio suspira." y música atenuada sin
  reiniciarse.
- [ ] Confirmar que La Suspensión no aparece con `rotar_niveles=false`, tampoco
  tras redimensionar o entrar/salir de Opciones.

## Arte y estabilidad

- [ ] Los diez niveles se reconocen sin leer el nombre.
- [ ] El Trono conserva óculo, abismo, puentes, columnas, estrado y asiento vacío.
- [ ] No hay rectángulos flotantes, geometría fuera de escala ni texto tapado.
- [ ] Permanecer varios minutos no aumenta instancias de sonido ni memoria visible.
- [ ] `python3 tools/verificar.py` termina sin fallos.
- [ ] `./gradlew clean build --no-daemon` termina con Java 17.
- [ ] El JAR reobfuscado se instala y arranca en una instancia limpia.

## Despliegue en `test-1`

- [ ] El repositorio local es `C:\Users\santi\Desktop\Jobs---Menu` y la rama
  actual es `arena/01a04ff1-jobs-menu`.
- [ ] Existe `gradle\wrapper\gradle-wrapper.jar` (ignorado por `.gitignore`);
  sin él el bloque se detiene con su mensaje antes de compilar.
- [ ] Java 17 completo (`java` + `javac`), `JAVA_HOME` coherente si está
  definido, y `python tools\verificar.py` termina sin fallos.
- [ ] `clean build --no-daemon` termina con `BUILD SUCCESSFUL`.
- [ ] Existe `build\libs\jobsmenu-0.10.0.jar` antes de tocar `mods`.
- [ ] El bloque termina con `OK: desplegado jobsmenu-0.10.0.jar` y muestra
  commit, carpeta de backup y SHA256; el hash del JAR en `mods` coincide con el
  del compilado. Durante el proceso no hay ventana con cero JARs ni dos JARs
  activos (el nuevo entra como `.pendiente` y pasa a su nombre final al final).
- [ ] La instancia se cerró y `mods` contiene el JAR 0.10.0, sin backups ni
  versiones viejas de `jobsmenu`.

El bloque reproducible y las rutas completas están al final del README para
copiarlos directamente en una terminal PowerShell nueva. No se mantiene ningún
archivo `.ps1` en el repositorio.

## Evidencia

Registrar resolución, GUI scale, idioma, mods activos, commit probado, log y
capturas. Un resultado estático o un build no reemplaza estas pruebas manuales.
