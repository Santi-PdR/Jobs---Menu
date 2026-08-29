# Segunda auditoría integral — Jobs Menu 0.10.0

**Fecha:** 2026-08-29
**Rama auditada:** `arena/01a04e24-jobs-menu`
**Commit auditado:** `8e5c0ef` (`fix: resolve Forge compile errors`)
**Base oficial:** `main` en `dc9ccca`
**Plataforma objetivo:** Minecraft 1.20.1 · Forge 47.4.0 · Java 17

## Resultado ejecutivo

La segunda auditoría estática no encontró fallos en los contratos que puede comprobar
sin Forge ejecutándose. La rama contiene 43 clases Java, 74 OGG del mod, 33 WAV de
materia prima, 230 claves por idioma y cinco láminas PNG documentales.

El estado no se declara terminado: el build real de Windows alcanzó `compileJava`
pero encontró ocho errores antes de `8e5c0ef`; esos ocho errores fueron corregidos
en ese commit y aún falta repetir el build con la rama actual. El arranque de
Minecraft, el SoundEngine, el modpack, GUI scales y GPU siguen siendo pruebas
manuales pendientes.

La matriz de fondos registra 35 mejoras implementadas estáticamente de 500 criterios
posibles (50 por cada uno de los diez escenarios). Las filas restantes no se cuentan
como implementadas. Por tanto, esta auditoría no afirma todavía que se haya superado
el objetivo de más de 75 mejoras perceptibles.

## Seguridad de ramas y recuperación

- `main` no fue modificada ni fusionada automáticamente.
- La rama correcta existe en remoto y apunta a `8e5c0ef`.
- Se conservaron las etiquetas de seguridad existentes:
  - `seguridad/2026-08-29/main`
  - `seguridad/2026-08-29/evolucion-3/backup-A-inicial`
  - `seguridad/2026-08-29/codex-professional-backgrounds`
  - `seguridad/2026-08-29/backup-pre-professional-pass-2026-08-28`
  - `seguridad/2026-08-29/backup-codex-v100-a-main-2026-08-28`
- Este documento se convierte en el punto de recuperación B de la segunda
  auditoría. El Backup C queda pendiente hasta completar el build y la prueba
  dentro de Minecraft.
- No se copiaron backups dentro de ninguna carpeta `mods`.
- La rama publicada no incluye `.github/workflows/build.yml`: la conexión de Arena
  no tiene permiso `Workflows`. La compilación se documenta y ejecuta manualmente.

## Comprobaciones ejecutadas

| Comprobación | Resultado | Alcance |
|---|---|---|
| `python3 tools/verificar.py` | Superada, 0 fallos y 1 aviso | Versiones, `mods.toml`, idiomas, audio, recursos, Java estático, lifecycle y matriz |
| `python3 -m py_compile ...` | Superada | Herramientas Python |
| `git diff --check` | Superada | Espacios y formato del checkout |
| Imports Java, búsqueda estática | Sin imports evidentemente huérfanos | Revisión heurística, no sustituto del compilador |
| OGG | 74 recursos presentes y auditados | Firma, relación con `sounds.json`, mono y 44.100 Hz según auditoría previa |
| Build Forge | Pendiente de repetir tras `8e5c0ef` | Requiere JDK 17 y memoria del equipo Windows |
| Minecraft/modpack | Pendiente | No se puede sustituir por una prueba estática |

El único aviso actual es la ausencia de `gradle/wrapper/gradle-wrapper.jar`. El
procedimiento manual descarga Gradle 8.1.1 directamente y no usa `gradlew.bat` cuando
ese binario no está disponible.

## Revisión de lifecycle y estado

### Hallazgos cerrados

- `SesionMenu` separa la visita del menú de la `Screen` concreta, por lo que
  Opciones, Mods, Recursos y otras pantallas hijas no reinician la música.
- `EscuchaCliente` usa `ScreenEvent.Opening` y exige clases exactas para el título
  y la pausa. No sustituye subclases de otros mods ni la pausa técnica de `F3+Esc`.
- `RecargaRecursosCliente` invalida audio en el hilo del cliente y agrupa solicitudes
  de reload con `AtomicBoolean`.
- La escena usa un `RotacionNiveles.Estado` capturado para que nivel, luz y
  suspensión sean coherentes en el mismo frame.
- La salida a un mundo cierra la sesión propia y deja preparado el regreso único
  al título vanilla.
- La configuración tiene límites declarados para enteros y guardado diferido;
  el bloque Windows se niega a desplegar si la auditoría o el build fallan.

### Riesgos todavía abiertos

- La invalidación de una instancia de audio y el estado real de OpenAL deben
  comprobarse con `F3+T`, resource packs, Alt+Tab, minimización y entrada/salida
  de mundo.
- Debe comprobarse que `SoundManager.play` acepte siempre los fallbacks vanilla
  en el entorno exacto de Forge 47.4.0.
- Las lecturas de `Minecraft.getInstance()` y `client.screen` están protegidas en
  los caminos revisados, pero la prueba de carreras requiere juego real.
- No hay un perfilador de GPU en el proyecto; el coste de `GuiGraphics` necesita
  medirse en una GTX 1050 o Intel UHD 630 con y sin Embeddium/Oculus.

## Revisión de audio

- Los consumidores propios pasan por `MezclaAudio.resolver`.
- Los `RegistryObject` se comprueban con `isPresent()` antes de `get()`.
- Los gestos de UI tienen fallback a `UI_BUTTON_CLICK` vanilla.
- Una capa ambiental ausente se omite mediante fallback nulo controlado en
  `GestorAmbiente`.
- La música tiene fallback a `MUSIC_MENU` vanilla.
- REQUIEM sigue dependiendo de `SoundSource.MASTER`, del volumen propio de música
  y del volumen maestro del aviso; no se mueve al slider vanilla `Music`.
- Se conserva el silencio como parte de la mezcla y se evita crear instancias
  desde cada reconstrucción de pantalla.
- La comprobación del registro seguro se volvió una regresión estática para que
  no regresen accesos directos `SonidosNivel.*.get()`.

### Observación del compilador

El build Windows previo a `8e5c0ef` informó cuatro warnings de APIs deprecadas:
los constructores de `ResourceLocation` y los accesos `ModLoadingContext.get()` y
`FMLJavaModLoadingContext.get()`. No impidieron `compileJava`, pero se mantienen
anotados para una futura limpieza compatible con Forge 1.20.1; no se reemplazan sin
confirmar la API exacta del entorno objetivo.

## Revisión de UI, compatibilidad y accesibilidad

- La intervención de `OptionsScreen` es idempotente, localizada y usa tooltip y
  narración propia sin rehacer la grilla vanilla.
- La pausa propia se limita a `PauseScreen` exacta con título `menu.game`.
- No se añadieron mixins ni dependencias nuevas.
- Las pantallas de Mods, Recursos, Singleplayer, Multiplayer, Controls, Language,
  Video, Sound y pantallas de configuración de terceros no se reemplazan
  indiscriminadamente.
- Los widgets conservan foco, Tab, Shift+Tab, Enter, Espacio, hover, hitbox y
  narración vanilla dentro de lo que puede verificarse por lectura de código.
- `movimiento_reducido` congela agua, telas, fuego, haces, polvo, presencia y
  eventos visuales; `destellos_reducidos` es independiente.
- `Ctrl+S` no aparece en menús, idioma, tooltips, README, CHANGELOG ni checklist.
- El layout se mide de forma responsiva, pero falta probarlo en GUI Scale 1–4,
  4:3, 16:10, ultrawide, 4K y ventanas extremas.

## Revisión visual y matriz de escenarios

Cada escenario posee una planta propia y un espejo en `tools/vista_previa.py`.
Las siguientes filas tienen implementación estática y quedan pendientes de
validación dentro de Minecraft:

| Escenario | Filas aceptadas | Mejoras cubiertas |
|---|---:|---|
| Administración | 3 | luminarias asimétricas, dintel pesado, placa remachada |
| Depósito | 4 | carga y jerarquía vertical, pilar ausente, puerta de muelle, soldaduras |
| Servicio | 4 | compuerta, válvula, manguera, cal mineral |
| Natatorio | 3 | ventana rota, desagüe con rejilla, placas de profundidad |
| Sala de piedra | 3 | nicho, cera, marca de peregrinación |
| Biblioteca | 3 | páginas dobladas, condensación localizada, polvo en recovecos |
| Invernadero | 3 | panel roto, canaleta y depósito, puerta entreabierta |
| Catacumbas | 3 | drenaje, arañazos, sillar reparado |
| Cisterna | 3 | compuerta, tubería con gota, marcas de nivel |
| Trono | 6 | eje y horizonte, losa, escalones, humedad, brillo de canto, cascote |
| **Total** | **35** | **Implementadas estáticamente; Minecraft pendiente** |

Las restantes 465 filas son criterios de diseño y aceptación, no código entregado.
Añadirlas solo mediante pequeños rectángulos o estados duplicados sería contrario
al criterio de esta auditoría; cada nueva fila deberá aportar una silueta,
material, arquitectura, comportamiento o comprobación realmente distinguible.

## Correcciones verificadas en esta segunda auditoría

- Los cinco errores de coordenadas `float` detectados por `compileJava` se
  convierten ahora con `Math.round` antes de llamar a `GuiGraphics.fill`.
- La etiqueta de orden del tablón se convierte a `Component.literal` antes de
  dibujarse.
- Los tres fallbacks `SoundEvents` se convierten de `Holder.Reference` a
  `SoundEvent` mediante `.value()`.
- La prueba estática de audio conserva su contrato después del formateo del
  código.
- La documentación dejó de afirmar que hay CI publicado cuando la rama remota
  no contiene el workflow.

## Decisiones de segunda auditoría

### Implementado

- Resolver audio defensivamente en un punto común.
- Lifecycle de visita separado de las pantallas.
- Invalidez de audio después de reload.
- Estado de escena compartido por frame.
- Compatibilidad conservadora con pantallas vanilla y de terceros.
- Accesibilidad y configuración con límites.
- Diez plantas y espejo procedural sincronizado.
- Auditoría reproducible de recursos, idiomas, audio y scripts.

### Pospuesto

- Build final y JAR 0.10.0: falta ejecutar el build después de `8e5c0ef`.
- Prueba Forge/Minecraft real: no se certifica por compilación.
- Validación del modpack, OpenAL, GPU, GUI scales y resource packs.
- Backup C: requiere build final y evidencia de Minecraft.
- Limpieza de warnings de APIs deprecadas: se necesita confirmar la API target
  exacta antes de cambiar llamadas compatibles.
- Añadir más de 75 mejoras perceptibles: se continuará solo con cambios
  comprobables, no rellenando la matriz artificialmente.

### Rechazado

- Añadir mixins globales para pintar pantallas de otros mods.
- Mover la música al slider vanilla `Music`.
- Crear instancias de audio por frame o por resize.
- Documentar o exponer el atajo administrativo oculto.
- Copiar backups, JARs antiguos o archivos de trabajo a `mods`.
- Tratar un `BUILD SUCCESSFUL` de otra rama o del JAR 0.9.0 como validación de
  la versión 0.10.0.

## Cierre y siguiente evidencia necesaria

Esta segunda auditoría queda **estáticamente superada con reservas**:

1. repetir `clean build` en `arena/01a04e24-jobs-menu` con JDK 17;
2. confirmar que `build/libs/jobsmenu-0.10.0.jar` existe y no es el JAR 0.9.0;
3. instalar únicamente ese JAR en la instancia de prueba, conservando backups fuera
   de `mods`;
4. probar navegación, audio, reload, salida a mundo, resource packs, GUI scales,
   movimiento reducido, destellos reducidos y modpack;
5. registrar logs, hash, capturas y resultado antes de crear Backup C.

Hasta completar esos pasos, el resultado correcto es “auditoría estática superada;
validación de build y Minecraft pendiente”, no “funciona dentro de Minecraft”.
