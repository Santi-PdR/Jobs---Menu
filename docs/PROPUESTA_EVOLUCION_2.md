# Propuesta de Evolución 2 — Jobs · Aviso a los ocupantes

> Documento histórico de decisiones de la Evolución 2. Su snapshot de rama y
> HEAD corresponde a esa auditoría; el informe de cierre de la Evolución 3 está en
> [`INFORME_FINAL_EVOLUCION_3.md`](INFORME_FINAL_EVOLUCION_3.md) y el estado vigente
> de la rama en [`EVOLUCION_4.md`](EVOLUCION_4.md). Las propuestas y hallazgos
> antiguos no sustituyen la documentación actual.
>
> Documento de decisión. Audita el estado real (snapshot histórico: rama `arena/01a04c05-jobs-menu`, HEAD `2d93732`),
> lista las mejoras y funciones nuevas evaluadas con su veredicto, y define el orden de ejecución.
> No se implementa nada aquí: se decide. El estado final de cada ítem (implementado / pospuesto /
> rechazado, y en qué commit) se actualiza en el **Informe final de la Evolución 2**.
>
> Método: cada ítem fue verificado contra el código actual (no contra lo que dicen los comentarios).
> Los comentarios que contradecían el código se anotan como hallazgo y se corrigen o se descartan.

---

## 0. Resumen de la auditoría (lo que está hecho)

Fuentes revisadas en esta evolución:

- **Java (42 archivos, 10.600 LOC):** `JobsMenu`, `client/*` (EscuchaCliente, SesionMenu, AtajoOverworld,
  RecargaRecursosCliente, AjustesAviso), `config/ConfigTurno`, `ui/*` (Paleta, HojaPapel, NotaAviso,
  RenglonTablon, RelojAparicion), `screen/*` (PantallaNivel, PantallaEstancia, PantallaAjustesAviso),
  `scene/*` (EscenaNivel, Marco, Nivel, RotacionNiveles, Presencia, EventosAmbientales,
  TratamientoEscena, MaterialesEscena, DireccionArte) y `scene/planta/*` (Planta, Trazo, PrimerPlano y
  las 10 plantas: Sala, Nave, Servicio, Natatorio, Cripta, Biblioteca, Invernadero, Catacumba,
  Cisterna, Trono).
- **Recursos:** lang es/en (completos), `sounds.json`, los 80 archivos de sonido (5,1 MB),
  `musica_creditada.txt`, `pack.mcmeta`, `mods.toml`.
- **Build:** `build.gradle`, `settings.gradle`, `gradle.properties`, `.github/workflows/build.yml`,
  `.gitignore`.
- **Docs y herramientas:** README, CONTEXTO, docs/compatibilidad, docs/musica, tools/verificar.py,
  tools/vista_previa.py (espejo Python del renderer), tools/sonidos.py, tools/muestras.py.
- **Historia:** `main` (oficial, intacto, no se toca), rama de trabajo, 25 tags de seguridad previos
  (conservados), Backup A `seguridad/2026-08-29/evolucion-2/backup-A-inicial` creado y verificado.
- **Render:** los 10 fondos generados en 854×480 (`--nivel=N --desnudo`) y revisados en contact
  sheet; comparación con la hoja pre-evolución disponible.

## Hallazgos confirmados (bugs y riesgos reales, con ubicación)

| # | Hallazgo | Ubicación | Gravedad |
|---|---|---|---|
| A1 | **Movimiento reducido no congela la animación de las plantas.** `EscenaNivel` pasa un `tiempo` real aunque `movimientoReducido` esté activo; fuego, causticas, estandartes, haces y vaivenes de todas las plantas siguen animándose. La opción sólo apaga motas/presencia/eventos. | `EscenaNivel.dibujar` | Alta (accesibilidad) |
| A2 | **El deslizador de volumen escribe el TOML en disco en cada fotograma de arrastre.** Cada `set` de la UI llama `fijar(...)` → `set()` + `save()`; mover un slider 0→100 produce decenas de escrituras por segundo. | `ConfigTurno.fijar` + `PantallaAjustesAviso.deslizador` | Media (rendimiento/SSD) |
| A3 | **CI desactualizado y roto por diseño:** la workflow sólo corre en la rama `codex/jobs-menu-professional-v100-2026-08-28` (no existe), usa `gradle` global en vez del wrapper, y sube `build/libs/jobsmenu-1.0.0.jar` hardcodeado (con 0.9.0 el paso `if-no-files-found: error` fallaría). | `.github/workflows/build.yml` | Alta (calidad de entrega) |
| A4 | **Doble pasada atmosférica sobre el punto de fuga:** `TratamientoEscena.profundidad` + `DireccionArte.profundidad` + `EscenaNivel.vineta` pintan tres capas de niebla/viñeta alrededor de la fuga. Duplica trabajo y ensucia el centro del cuadro. | `DireccionArte.profundidad` | Media (visual + perf) |
| A5 | **Reflejo vertical del natatorio se dibuja como zigzag de rayos.** Los 4×5 segmentos se desplazan con `sin(t*1.3 + i + s)*4` con fase distinta por segmento: se ven como un relámpago blanco en el agua (visible en render n3). | `DireccionArte.natatorio` | Alta (calidad visual) |
| A6 | **`EventosAmbientales.siluetaLejana` usa su propio reloj** (`System.currentTimeMillis() % VENTANA_MS`) en vez del `progreso` de la ventana: la silueta puede saltar de posición en el pico del pulso. | `EventosAmbientales.siluetaLejana` | Media (coherencia) |
| A7 | **Pitch artificial de las camas de ambiente:** `pitch = 0.96 + 0.03*nivel` da 1.23 en el nivel 9 — un cambio de tono del 23 %, audible como "cinta acelerada", no como "otro sitio". | `CapaAmbiente` constructor | Media (audio) |
| A8 | **`String.format` y múltiples lecturas de reloj por frame en el reloj de ronda:** `PantallaNivel.ronda()` llama 4 métodos que leen el reloj por separado y `formatoRestante()` formatea un String cada frame. | `RelojAparicion` + `PantallaNivel.ronda` | Baja (perf) |
| A9 | **`PantallaNivel.cabecera()` vuelve a partir el texto con `font.split` en cada frame**, aunque ya se midió en `init()`; el resultado de la medición no se reutiliza. | `PantallaNivel.cabecera` | Baja (perf) |
| A10 | **Alocación por frame en la presencia doble:** `float[][] posiciones = new float[][]{...}` se crea en cada frame en que la figura es visible. | `Presencia.dibujar` | Baja (perf) |
| A11 | **`GestorMusica.stopPlaying()` se llama dos veces por tick** (en `atender()` y en `tick()`). No rompe nada, es trabajo duplicado. | `GestorMusica` | Baja (perf) |
| A12 | **Comentario de `AjustesAviso` contradice el código** ("arriba a la izquierda" vs código arriba a la derecha) — riesgo de que un futuro cambio use la posición equivocada. | `AjustesAviso` | Baja (documentación) |
| A13 | **`GestorMusica.soltar()` está vacío.** La intención (dejar caer la instancia para que complete su fundido) es correcta y funciona vía `tick()`, pero un método público vacío se lee como código muerto. | `GestorMusica.soltar` | Baja (claridad) |
| A14 | **Comentario de `verificar.py` desactualizado:** dice "8 de interfaz, 4 ambientes de sala, 13 eventos" con `PIEZAS_ESPERADAS = 74`; la identidad sonora actual son 30 camas (10×base/caracter/actividad) + 31 eventos + 3 transición + 1 figura + 1 música + 8 UI. | `tools/verificar.py` | Baja (mantenimiento) |
| A15 | **La vista previa `vista_previa.py` es un espejo del renderer.** Cualquier cambio en `scene/` debe espejarse o la herramienta deja de ser fiable. Es un costo de proceso, no un bug. | `tools/vista_previa.py` | Proceso |
| A16 | **Rutas de música sospechosas:** en `sounds.json` el evento `musica.tema` usa `stream:false`; todas las camas ambientales también. No hay doble copia de REQUIEM (una sola en `sounds/musica/defecto.ogg`, respaldo en `music/`). Verificado, OK. | recursos | — |
| A17 | **No hay `CHANGELOG.md` ni `KNOWN_ISSUES.md`** en la raíz aunque el plan los exige. | docs | Proceso |
| A18 | **`PantallaNivel` y `PantallaEstancia` mantienen métricas de hoja duplicadas** (constantes y métodos casi idénticos). Compartir `HojaPapel` ya se hace; compartir la métrica entera acoplaría pantallas con propósitos distintos. Se documenta, no se fuerza. | screens | Decisión |

## Orden de trabajo

1. **Fase B — Mejoras generales** (esta lista): estabilidad, robustez, compatibilidad, coherencia,
   UI/UX, audio, accesibilidad, performance. → commits separados por tema → `verificar.py` → commit
   → **Backup B** (tag, verificado recuperable).
2. **Fase C — Los 10 fondos:** revisión individual (composición, cámara, foco, luz, material,
   movimiento), Trono rehecho desde cero si hace falta. → commits → segunda auditoría → build limpio
   en el PC del owner → docs → **Backup C**.
3. **Informe final de 35 puntos.**

---

# I. Mejoras (30 evaluadas)

## I-01 — Congelar la animación de las plantas con "movimiento reducido"
- **Problema/oportunidad (A1):** la opción accesible no cumplía su promesa: fuego, agua, telas y haces
  seguían moviéndose.
- **Solución:** `EscenaNivel` calcula `tiempo` real sólo si `movimiento` es verdadero; si no, pasa un
  instante fijo (`3.0F`) a la planta, a MaterialesEscena, a Tratamiento y a DireccionArte. La luz sigue
  viva (es otra opción, `destellos_reducidos`).
- **Beneficio:** accesibilidad real; la escena queda estática y legible para quien lo pide.
- **Riesgo:** bajo; un valor fijo ya se usaba con `escena_viva=false`.
- **Compatibilidad:** ninguna (sólo tiempo de animación).
- **Costo:** cero; no cambia el número de fills.
- **Decisión:** **IMPLEMENTAR** (Fase B).

## I-02 — Guardado diferido y con límite de frecuencia en la config
- **Problema/oportunidad (A2):** arrastrar un slider escribía el TOML decenas de veces por segundo.
- **Solución:** los setters hacen `set()` siempre y piden guardado; el guardado real se ejecuta como
  máximo 1 vez cada 250 ms y se fuerza al cerrar la pantalla de ajustes o al cambiar de pantalla
  (hook en `PantallaAjustesAviso.removed`, `ScreenEvent.Opening`/`Closing` de `EscuchaCliente`).
- **Beneficio:** menos escrituras de disco, sin perder el último valor (flush garantizado en los
  cambios de pantalla).
- **Riesgo:** bajo; API de Forge (`ConfigValue.save()`) estable en 47.x. Si el usuario cierra el juego
  a mitad de arrastre (ventana < 250 ms), el valor queda en memoria pero puede no persistir; se
  documenta como límite conocido (aceptable para un slider).
- **Compatibilidad:** ninguna (config propia).
- **Costo:** N/A.
- **Decisión:** **IMPLEMENTAR**.

## I-03 — CI que corre de verdad y publica el JAR correcto
- **Problema/oportunidad (A3):** la workflow no se disparaba en ninguna rama existente y el nombre
  del artefacto estaba hardcodeado a 1.0.0.
- **Solución:** disparar en `push` a `arena/01a04c05-jobs-menu` y en `pull_request` a `main`; usar
  `gradle` con `gradle-version: 8.1.1` (el wrapper .jar está gitignoreado por decisión previa) y
  derivar el nombre del JAR de `mod_version` de `gradle.properties`.
- **Beneficio:** cada commit de la rama de trabajo queda verificado automáticamente (Java 17,
  `tools/verificar.py`, `clean build`), y el artefacto se llama como la versión real.
- **Riesgo:** bajo; `main` no se modifica (la workflow sólo corre; el PR a main no se crea).
- **Compatibilidad:** N/A (CI).
- **Costo:** N/A.
- **Decisión:** **IMPLEMENTAR**.

## I-04 — Una sola pasada atmosférica sobre la fuga
- **Problema/oportunidad (A4):** tres capas de niebla/viñeta duplicaban área y ensuciaban el centro.
- **Solución:** quitar `DireccionArte.profundidad()` (mantener `TratamientoEscena.profundidad` y la
  viñeta de borde de `EscenaNivel`); el halo de color de identidad por nivel se conserva dentro de
  `TratamientoEscena` añadiendo un halo tenue por nivel (o se mantiene el de `EscenaNivel.vineta`).
  Espejo en `vista_previa.py`.
- **Beneficio:** menos fills (≈14 rects grandes por frame), centro menos ahogado, imagen más limpia.
- **Riesgo:** medio (cambio visual). Se valida con renders antes/después.
- **Compatibilidad:** ninguna.
- **Costo de render:** negativo (ahorra).
- **Decisión:** **IMPLEMENTAR** (validar render).

## I-05 — Reflejo del natatorio: de zigzag a columna de luz suave
- **Problema/oportunidad (A5):** el reflejo de las luminarias sobre el agua se dibujaba como un
  relámpago blanco.
- **Solución:** en `DireccionArte.natatorio` reemplazar los 4×5 segmentos con fase por segmento por
  una columna continua por luminaria: 6 tramos apilados con deriva única por luz (≤ 1 px) y
  desvanecido progresivo, como lo que ya hace `Natatorio.reflejoLuces` para el trampolín.
- **Beneficio:** el agua se lee como agua; desaparece el artefacto.
- **Riesgo:** bajo (dibujo acotado a nivel3).
- **Compatibilidad:** ninguna.
- **Costo:** menor o igual (menos fills).
- **Decisión:** **IMPLEMENTAR** (validar render).

## I-06 — Silueta lejana coherente con su ventana
- **Problema/oportunidad (A6):** la silueta del evento usaba su propio reloj y podía saltar.
- **Solución:** usar `progreso` (el mismo que produce la ventana y el pulso) como avance.
- **Beneficio:** el movimiento se ve continuo dentro del pulso; sin salto en el pico.
- **Riesgo:** nulo.
- **Compatibilidad:** ninguna.
- **Costo:** cero.
- **Decisión:** **IMPLEMENTAR** (espejo en Python).

## I-07 — Pitch sutil en las camas de ambiente
- **Problema/oportunidad (A7):** hasta +23 % de tono por nivel sonaba a cinta acelerada.
- **Solución:** `pitch = 0.975 + 0.004 * nivel` (rango 0.975–1.011) y mantener el matiz de la capa
  CARACTER (×0.995). La personalidad de cada nivel se sigue logrando con la respiración y las
  ventanas distintas, no con el tono.
- **Beneficio:** ambiente audiblemente natural; sin "chipmunk" en niveles altos.
- **Riesgo:** nulo (sólo audio).
- **Compatibilidad:** ninguna.
- **Costo:** N/A.
- **Decisión:** **IMPLEMENTAR**.

## I-08 — Reloj de ronda: una lectura de reloj por frame
- **Problema/oportunidad (A8):** 4 lecturas de reloj + `String.format` por frame.
- **Solución:** snapshot único (`long falta`) en `PantallaNivel.ronda()`; sobrecargas de
  `RelojAparicion.enRonda/enAlerta/inminente/formatoRestante/color` que reciben `falta`; la entrada de
  la cadena se cachea por segundo (el texto formateado no cambia dentro del mismo segundo).
- **Beneficio:** 1 reloj y 0 format por frame fuera del cambio de segundo.
- **Riesgo:** nulo (misma salida).
- **Compatibilidad:** ninguna.
- **Costo:** negativo.
- **Decisión:** **IMPLEMENTAR**.

## I-09 — Cabecera de la hoja medida una vez y dibujada desde caché
- **Problema/oportunidad (A9):** `font.split` re-ejecutado cada frame.
- **Solución:** guardar las líneas partidas (subtítulo, nivel actual, tarifa) como campos calculados
  en `init()` y dibujarlas; `medirCabecera()` reutiliza esos campos.
- **Beneficio:** 3 splits por frame → 0 (los splits son baratos pero se suman al resto del render).
- **Riesgo:** bajo: la pantalla se reconstruye al cambiar idioma o resolución, así que el caché
  nunca queda viejo.
- **Compatibilidad:** ninguna.
- **Costo:** negativo.
- **Decisión:** **IMPLEMENTAR**.

## I-10 — Presencia sin alocaciones por frame
- **Problema/oportunidad (A10):** `float[][]` nuevo en cada frame visible.
- **Solución:** desplegar el caso doble con dos llamadas explícitas (parámetros fijos), sin arreglos.
- **Beneficio:** cero alloc en el hot path del render.
- **Riesgo:** nulo (mismo dibujo).
- **Compatibilidad:** ninguna.
- **Costo:** cero.
- **Decisión:** **IMPLEMENTAR** (espejo en Python).

## I-11 — Un solo `stopPlaying()` por tick
- **Problema/oportunidad (A11):** duplicado.
- **Solución:** dejar el corte del gestor de música de vanilla sólo en `atender()` (que es el que
  decide si es momento) y quitar la llamada redundante de `tick()`.
- **Beneficio:** menos ruido por tick; comportamiento idéntico.
- **Riesgo:** nulo (verificado: `atender()` corre en cada tick de cliente).
- **Compatibilidad:** ninguna.
- **Costo:** N/A.
- **Decisión:** **IMPLEMENTAR**.

## I-12 — Comentario de posición corregido en `AjustesAviso`
- **Problema/oportunidad (A12):** el comentario decía "arriba a la izquierda"; el código la pone a la
  derecha. Además del texto, se documenta en este mismo ítem la decisión de posición y su porqué
  (esquina libre de la grilla de vanilla, clase exacta `OptionsScreen`).
- **Solución:** corregir el comentario.
- **Beneficio:** sin trampas para el siguiente que toque el archivo.
- **Riesgo:** nulo.
- **Decisión:** **IMPLEMENTAR**.

## I-13 — `GestorMusica.soltar()`: eliminar el método vacío
- **Problema/oportunidad (A13):** método público vacío con comentario de tres líneas.
- **Solución:** sustituir por un método con semántica explícita: `SesionMenu.cerrar()` ya no llama a
  nada (el fundido lo resuelve `tick()`); se elimina `soltar()` y se deja el porqué en el comentario
  de `cerrar()`.
- **Beneficio:** menos superficie confusa.
- **Riesgo:** nulo (nadie más lo usa).
- **Decisión:** **IMPLEMENTAR**.

## I-14 — Comentario de `PIEZAS_ESPERADAS` actualizado en `verificar.py`
- **Problema/oportunidad (A14):** el comentario describía la identidad sonora de una versión vieja.
- **Solución:** actualizar la descripción (8 UI + 30 camas + 31 eventos + 3 transición + 1 figura +
  1 música = 74, que es lo que la herramienta ya cuenta) y hacer la cuenta derivada en vez de un
  literal si es trivial.
- **Beneficio:** la herramienta sigue siendo la fuente de verdad legible.
- **Riesgo:** nulo.
- **Decisión:** **IMPLEMENTAR** (mantener el número 74; verificado contra `sounds.json`).

## I-15 — Registro de pantalla de opciones: no expandirse a subclases
- **Problema/oportunidad:** ya se hace (clase exacta `OptionsScreen`); se **documenta** en
  `docs/compatibilidad.md` como decisión firme y se añade prueba de humo en `verificar.py`
  (revisar que no haya `instanceof OptionsScreen` en el fuente).
- **Solución:** documentar + verificación estática.
- **Beneficio:** el próximo cambio no puede romperlo por accidente.
- **Riesgo:** nulo.
- **Decisión:** **IMPLEMENTAR** (sólo doc + verificación).

## I-16 — Salida de la pausa propia: `mouseHandler.grabMouse()` al reanudar
- **Problema/oportunidad:** `PantallaEstancia.reanudar()` ya lo hace (verificado); se añade al mismo
  ítem la **narración de apertura de la hoja de pausa** como accesibilidad: la pantalla ya usa
  `Component.translatable` en el título; la narración de los renglones ya funciona vía
  `RenglonTablon.updateWidgetNarration`.
- **Solución:** no hay bug; se documenta el comportamiento como verificado y se pospone cualquier
  narración de resumen (ver NF-05).
- **Decisión:** **REVISADO, sin cambio** (documentar en informe).

## I-17 — Rango del reloj de ronda y umbrales
- **Problema/oportunidad:** el ciclo de 13 min y los umbrales son constantes mágicas deliberadas y
  coherentes con el lore; `RelojAparicion` no tiene estado mutable y no depende de la partida.
- **Decisión:** **MANTENER** (rechazo de "configurarlos": la arbitrariedad es el punto; el jugador no
  puede negociar con los Executores). Se documenta como rechazo con motivo.

## I-18 — Estado de `escenaViva=false` y música/ambiente
- **Problema/oportunidad:** con la escena apagada, `sonidoAmbiente` sigue sonando si está activo.
  Es coherente (son dos opciones), pero la UI no lo explica.
- **Solución:** línea de detalle en `jobsmenu.ajustes.escena.detalle` y `volambiente.detalle`
  ("suena aunque la escena esté quieta").
- **Beneficio:** sin sorpresas.
- **Riesgo:** nulo.
- **Decisión:** **IMPLEMENTAR** (sólo lang).

## I-19 — `PantallaEstancia`: teclado completo
- **Problema/oportunidad:** verificado: Tab/Shift+Tab y Enter/Space funcionan por componentes
  natives; Escape reanuda (override `onClose`) como la pausa de vanilla; `shouldCloseOnEsc` no se
  toca. No hay bug.
- **Decisión:** **REVISADO, sin cambio** (documentar).

## I-20 — Botón de ajustes: ancho mínimo legible e i18n
- **Problema/oportunidad:** el botón usa `jobsmenu.ajustes.boton` (traducido) y ancho 80–120 px;
  en alemán/francés un texto largo puede truncarse a 80 px.
- **Solución:** medir el texto (`font.width`) y fijar `ancho = max(90, min(140, fontWidth + 16))`,
  recortado al ancho de la pantalla.
- **Beneficio:** botón legible en todos los idiomas.
- **Riesgo:** bajo.
- **Compatibilidad:** el botón sigue en la esquina libre, sin tocar la grilla.
- **Decisión:** **IMPLEMENTAR**.

## I-21 — `MusicaPropia`: rotación de pistas estable
- **Problema/oportunidad:** la rotación por `.ultima-ogg` está bien; se encontró que el log anuncia
  "se alternan en cada arranque" con `totalPistas > 1` y eso es correcto.
- **Decisión:** **REVISADO, sin cambio** (documentar en informe; el único cambio fue de doc en
  versions previas).

## I-22 — `NotaAviso`: sonido de foco por teclado
- **Problema/oportunidad:** `isHoveredOrFocused` (verificado) hace que Tab produzca el roce igual que
  el ratón; es coherente. El umbral de 80 ms evita ráfagas.
- **Decisión:** **REVISADO, sin cambio**.

## I-23 — `RenglonTablon`: hitbox = visual
- **Problema/oportunidad:** verificado: el resaltado ocupa exactamente la región clicable (ya
  corregido en 0.9.0 por la nota con `ACTIVE` real). Se añade la regla como comentario vigilado.
- **Decisión:** **REVISADO, sin cambio** (no repetir mejora ya hecha).

## I-24 — `PantallaAjustesAviso`: opción "volumen maestro del aviso" (parte de NF-01)
- **Problema/oportunidad:** no hay forma de bajar TODO el audio del mod de una vez.
- **Solución:** slider 0–100 `volumen_aviso` que multiplica música, ambiente, transición, figura y
  gestos (NUEVA clave en la mesa de mezcla). La tecla **M** en el aviso alterna entre 0 y el último
  valor recordado (silenciar/desilenciar).
- **Beneficio:** control de audio de un solo gesto; sin pasar por tres interruptores.
- **Riesgo:** bajo; es una multiplicación más en la mezcla.
- **Compatibilidad:** ninguna.
- **Costo de audio:** N/A.
- **Decisión:** **IMPLEMENTAR** (Ver NF-01).

## I-25 — Nivel fijo: la rotación apagada debe seguir sonando el nivel que se ve
- **Problema/oportunidad:** verificado: con `rotar_niveles=false`, `RotacionNiveles.indiceActual()`
  devuelve el fijo y `CapaAmbiente` usa ese índice; el ambiente corresponde al nivel mostrado. OK.
- **Decisión:** **REVISADO, sin cambio**.

## I-26 — Compatibilidad musical: REQUIEM y el deslizador Música
- **Problema/oportunidad:** REQUIEM se reproduce por `SoundSource.MASTER` (constructor de
  `GestorMusica`), por lo que NO depende del deslizador de Música de vanilla; depende del deslizador
  Maestro + `volumen_musica` propio + `volumen_aviso` (nuevo). Es exactamente lo que pide el
  mandato. Los gestos y ambiente también van por MASTER; las camas ambientales declaradas como
  `ambient` en sounds.json se reproducen con fuente MASTER vía `SimpleSoundInstance.forUI` — el
  deslizador Ambient de vanilla NO las gobierna.
- **Solución:** documentar este comportamiento en `docs/musica.md` y en el texto de ayuda
  (`volambiente.detalle`), y **no** cambiar el canal (cambiarlo a AMBIENT haría que el deslizador
  Ambient de vanilla las duplicara/bajara y rompería la promesa de control propio).
- **Beneficio:** una sola fuente de verdad para el audio del mod; sin sorpresas.
- **Riesgo:** nulo (sin cambio de código).
- **Decisión:** **IMPLEMENTAR** (documentación) — cambio evaluado y **rechazado** en su variante de
  canal (motivo: control dual de volumen).

## I-27 — Duplicado de causticas en el natatorio
- **Problema/oportunidad:** `Natatorio.caustica` (planta) + `DireccionArte.causticas` + el rizo de
  `EventosAmbientales.humedadViva` pintan tres tratamientos de agua distintos que se superponen.
- **Solución:** dejar la caustica de la planta como base, reducir `causticas` del arte a 4 líneas
  sutiles para el nivel3 (no 8) y quitar el rizo de `humedadViva` para nivel3 (dejar el velo).
- **Beneficio:** agua más limpia y legible; menos fills.
- **Riesgo:** visual medio; validar con render.
- **Decisión:** **IMPLEMENTAR** (validar render; espejo Python).

## I-28 — `PantallaNivel`: `fecha` del turno en la hoja (parte de NF-04)
- **Problema/oportunidad:** no hay ni fecha ni hora en la pantalla (contextual info).
- **Solución:** línea "Turno del {fecha} — {hora}" en la cabecera, opción `mostrar_fecha` (default
  true), mostrada como texto pequeño bajo la tarifa.
- **Beneficio:** el aviso deja de ser atemporal; refuerza la ficción del turno.
- **Riesgo:** bajo (una línea medida con `font.split` como el resto).
- **Compatibilidad:** ninguna.
- **Decisión:** **IMPLEMENTAR** (Ver NF-04).

## I-29 — Ajustes: cadencia de rotación (parte de NF-02)
- **Problema/oportunidad:** 24 s por nivel es fijo; quien disfruta la escena quiere más tiempo.
- **Solución:** opción `rotacion_calma` (default false) que alarga la estancia de 24 s a 48 s sin
  tocar la transición (2,6 s) ni los chispazos.
- **Beneficio:** personalización simple y sin romper el ritmo del apagón.
- **Riesgo:** nulo (sólo `ESTANCIA_MS`).
- **Decisión:** **IMPLEMENTAR** (Ver NF-02).

## I-30 — Revisión de "magic values" restantes
- **Problema/oportunidad:** se revisaron `CICLO_MS=13min`, `PERIODO_MS=71s`, `VENTANA_MS=6,2s`,
  `MOTAS=52`, `ANCHO_HOJA=214`, etc. Todos tienen comentario de diseño o son lore deliberado; no
  se convierten en config (bloat).
- **Decisión:** **REVISADO, sin cambio** (documentar rechazo con motivos — uno por valor).

---

# II. Funciones nuevas (5 evaluadas)

## NF-01 — Volumen maestro del aviso + silencio con M
- **Qué es:** slider «Volumen del aviso» (0–100, nuevo `volumen_aviso`, default 100) que gobierna
  TODO el audio del mod (música, ambiente, eventos, transición, figura, gestos, y el nuevo sonido de
  suspensión). En el aviso, la tecla **M** silencia/restaura sin pasar por el menú (guarda el último
  valor no-cero).
- **Por qué encaja:** el eje "control de audio" está en el mandato; el mod ya tiene dos volúmenes
  parciales y faltaba el maestro. Un menú de lobby debe poder callarse de un toque.
- **Qué mejora:** control, UX, respuesta audiovisual.
- **Riesgo:** bajo; multiplicador único en `MezclaAudio` y `GestorMusica`/`CapaAmbiente`.
- **Compatibilidad:** ninguna.
- **Costo:** N/A.
- **Decisión:** **IMPLEMENTAR** (Fase B).

## NF-02 — Cadencia de la rotación (normal / en calma)
- **Qué es:** opción «Rotación en calma» que duplica la estancia por nivel (24 s → 48 s); la
  transición de apagón no cambia.
- **Por qué encaja:** personalización del ambiente sin tocar el núcleo (el ciclo, el apagón, los
  chispazos).
- **Qué mejora:** ambience, personalización.
- **Riesgo:** nulo.
- **Decisión:** **IMPLEMENTAR**.

## NF-03 — La Suspensión: apagón largo y raro
- **Qué es:** una vez cada ~45–52 min (ventana derivada del reloj de sistema, sin estado), el
  edificio se apaga **de verdad** durante 22 s: luz al 4 %, sin parpadeos, ambiente al mínimo
  (piso de ACTIVIDAD), un suspiro grave (reutiliza `NIVEL_APAGON` a pitch 0.82) y una nota al
  rotular: «El edificio suspira.» Respeta `destellos_reducidos` y `movimiento_reducido` (sin
  chispazos, sin motas), y jamás ocurre con `rotar_niveles=false`.
- **Por qué encaja:** eje "eventos raros" y "calidad de transición"; es raro (≈1 cada 45 min), corto,
  sin susto, silencioso en la mayor parte de la mezcla.
- **Qué mejora:** ambience, rareza, transición.
- **Riesgo:** medio (toca `RotacionNiveles.luzDisponible` y `GestorAmbiente`); se valida con la vista
  previa (parámetro de penumbra) y con pruebas de reloj en el código.
- **Compatibilidad:** ninguna.
- **Costo de render:** 0 (reutiliza el camino de luz existente).
- **Decisión:** **IMPLEMENTAR**.

## NF-04 — Fecha del turno en la hoja
- **Qué es:** una línea «Turno del {día de semana, día de mes} — HH:MM» bajo la tarifa, medida y
  partida como el resto; opción `mostrar_fecha` (default activa; la traduce es/en).
- **Por qué encaja:** información contextual; el aviso es un documento administrativo y los
  documentos llevan fecha. Refuerza el reloj de los Executores (el tiempo corre).
- **Qué mejora:** info contextual, coherencia.
- **Riesgo:** bajo; reutiliza `LocalDateTime` de `NotaAviso` (API ya usada).
- **Decisión:** **IMPLEMENTAR**.

## NF-05 — Narración de apertura del aviso (resumen accesible)
- **Qué es:** al entrar por primera vez en la sesión (y al cambiar de nivel), el lector de pantalla
  anuncia el resumen: «Aviso a los ocupantes. Nivel 3: las piscinas. Salida al nivel 4. La próxima
  ronda ocurre en …». Se apoya en el narrador de Minecraft (`Minecraft.getNarrator().say`).
- **Por qué encaja:** eje accesibilidad; el mod ya tiene widgets narrables pero la hoja como
  documento no se anuncia.
- **Riesgo:** **medio-alto**: la API de narración difiere entre versiones y no puedo compilar en el
  sandbox; un fallo aquí rompe el menú principal (pantalla crítica). Requiere validación en vivo.
- **Decisión:** **POSPONER** con motivo: se implementa en una sesión posterior, con prueba de
  compilación en el PC del owner, detrás de una comprobación de `narrator` activo. No se arriesga la
  pantalla de título por una mejora de accesibilidad sin poder verificar la API.

---

# III. Decisión pantalla por pantalla

| Pantalla | Decisión | Motivo |
|---|---|---|
| TitleScreen | **Reemplazo completo** (`PantallaNivel`) | Es el corazón del mod; la hoja ES el menú. Clase exacta + config `menu_propio`; navegación, tooltips, tabulación se conservan vía widgets natives. |
| PauseScreen | **Reemplazo completo** (`PantallaEstancia`) | Sólo pausa real (clase exacta + título `menu.game`); Escape reanuda, guardado `local/server/Realms` replica la secuencia vanilla verificada. F3+Esc no se toca. |
| OptionsScreen | **Adaptación discreta**: 1 botón en la esquina superior derecha | La grilla de vanilla no se rehace; clase exacta, sin superponer botones de otros mods. |
| PantallaAjustesAviso (propia) | **Componentes compartidos** (`OptionsList`, `OptionInstance`) | Es una subpantalla más de Opciones; mismos sliders/switch/scroll/tooltips del juego. |
| Sound / Video / Controls / Keybinds / Accessibility | **Sin cambio** | Son pantallas del equipo; el mod no las viste. Sólo se llega a los ajustes del aviso desde Opciones. |
| Language / Resource Packs | **Sin cambio** | Cambiar idioma reconstruye las pantallas del mod (miden textos en `init()`); los packs actúan sobre el mundo, no sobre la hoja. |
| Mods (ModListScreen) | **Sin cambio** | Abierta desde «Registro de intervenciones» (renglón 03). |
| Singleplayer (SelectWorldScreen) | **Sin cambio** | Flujo de vanilla; el servicio interno no forma parte de esta pantalla. |
| Multiplayer (JoinMultiplayerScreen) | **Sin cambio** | «Unirse a una cuadrilla» abre la de vanilla tal cual. |
| Select World / Edit World / Direct Connect / Add Server | **Sin cambio** | Flujos de vanilla; el mod no toca ni guardado ni servidores. |
| Confirmaciones / error / disconnect | **Sin cambio** | Diálogos de vanilla sobre la vida del mundo; interceptarlos es riesgo de compatibilidad con mods de guardado. |
| PantallaNivel / PantallaEstancia | **Tema propio completo** | Son las únicas pantallas "del recinto"; comparten `HojaPapel`, paleta y métrica de tinta. |

---

# IV. Qué se hace en qué fase

- **Fase B (general):** I-01, I-02, I-03, I-04, I-05, I-06, I-07, I-08, I-09, I-10, I-11, I-12,
  I-13, I-14, I-15, I-18, I-20, I-26, I-27 + NF-01, NF-02, NF-04 (los tres implementables con
  recursos existentes y sin riesgo de compilación; NF-01 trae su propio sonido? No: NF-01 no
  necesita sonido nuevo). → `python3 tools/verificar.py` → commit(s) → **Backup B**.
- **Fase C (fondos):** revisión individual de los 10 niveles; Trono desde cero; I-04/I-05/I-27
  validados con renders; luego NF-03 (la Suspensión, depende del estado de la luz) — se reubica a
  Fase C porque toca `RotacionNiveles` y la luz, o se implementa en B antes de los fondos si la
  validación es satisfactoria. **(Decisión: NF-03 en Fase C, después del rediseño de luz, para no
  pelearse con el rediseño del Trono.)**
- **Final:** segunda auditoría, `clean build` con Java 17 (PC del owner), docs (README, CONTEXTO,
  CHANGELOG, KNOWN_ISSUES, compatibilidad, música, auditoría + esta propuesta con estado final),
  **Backup C**, informe de 35 puntos. Las herramientas de servicio internas no se describen en la interfaz pública.

---

# V. Rechazos explícitos (y por qué)

1. **Configurar el ciclo del reloj de Executores (13 min).** La arbitrariedad es el diseño: un reloj
   que el jugador pudiera negociar no sería un reloj de los Executores. Rechazado.
2. **Cambiar el canal del audio del mod de MASTER a AMBIENT/MUSIC.** Duplicaría el control con los
   deslizadores de vanilla y rompería el mandato de que REQUIEM dependa de Maestro + slider propio.
   Rechazado.
3. **Nueva pantalla de ajustes propia (hoja de papel).** Ya hubo dos menús; el mandato es uno solo.
   Rechazado (ya resuelto en 0.8.2).
4. **Meter el botón de ajustes en la grilla de Opciones.** Requeriría rehacer el layout de vanilla y
   pisaría a mods que también lo hacen (Cloth Config, etc.). Rechazado.
5. **Overlay/herramientas de otras pantallas (Sound/Video/Controls/etc.).** Sin beneficio propio;
   riesgo de incompatibilidad con Embeddium/Oculus/Controlling. Rechazado.
6. **`instanceof` amplios sobre pantallas.** Sólo clases exactas; cualquier coincidencia amplia se
   rechaza por diseño (verificación estática en `verificar.py`).
7. **Animación de la figura como "personaje" que camina.** Ya fue rechazada en la revisión de
   Presencia (se lee como personaje y se agota); se mantiene la silueta ambigua.
8. **Más partículas/motas.** Ya se redujo a 52; subirlas ensucia y no aporta identidad. Rechazado.
9. **HUD/marcas in-game durante la partida.** El mod es de menús; no toca el mundo. Rechazado.
10. **Sonido de ambiente que siga sonando fuera del menú.** El mandato dice silencio al salir del
    nivel; ya se cumple (CapaAmbiente se detiene porque `screen != PantallaNivel`). Rechazado el
    "ambiente persistente", que violaría el mandato.
