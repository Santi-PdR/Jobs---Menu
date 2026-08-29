# Informe final de la Evolución 3

> Informe histórico. El estado actual es 0.10.0 y se documenta en
> [`EVOLUCION_4.md`](EVOLUCION_4.md), `CHANGELOG.md` y `KNOWN_ISSUES.md`.
> Las pendientes de este informe no sustituyen el checklist vigente.

**Proyecto:** Jobs · Aviso a los ocupantes
**Plataforma objetivo:** Minecraft 1.20.1 · Forge 47.4.x · Java 17
**Rama de trabajo:** `arena/01a04e24-jobs-menu`
**Fuente oficial preservada:** `main` permanece en `dc9ccca960ba3d797c6980c7cc3f34bccffd3747`
**Alcance:** cliente; no se añaden gameplay, entidades, economía ni comandos.

Este informe distingue deliberadamente tres niveles de evidencia:

- **Verificado en el checkout:** lectura del código y recursos, auditoría estática,
  sintaxis Python, diff limpio y renders del espejo procedural.
- **No verificable en este sandbox:** `clean build`, porque no hay `java` ni
  `JAVA_HOME` y no se pudo instalar OpenJDK 17.
- **Pendiente en Minecraft:** arranque Forge, navegación real, SoundEngine,
  resource packs, rendimiento de GPU, modpack completo y comportamiento de
  guardado. La lista operativa está en [`../KNOWN_ISSUES.md`](../KNOWN_ISSUES.md).

No se presenta la compilación ni el funcionamiento dentro de Minecraft como
hechos consumados.

---

## 1. Resumen de la intervención

Se revisaron ramas, tags de seguridad y el árbol completo de Java, recursos,
audio, herramientas, workflow y documentación. Se conservaron las
implementaciones útiles y `main` no fue modificado. La evolución se concentró
en estabilidad antes que en adornos:

1. un snapshot temporal por frame para que escena, hoja, rótulo, reloj y audio
   no crucen una frontera de rotación con instantes diferentes;
2. lifecycle de audio con una instancia de música, tres camas ambientales por
   nivel y apagado inmediato cuando el usuario desactiva ambiente;
3. accesibilidad efectiva para movimiento y destellos reducidos;
4. menos trabajo repetido en el render mediante cachés de texto y medidas;
5. layout que deriva ancho y margen de la ventana disponible;
6. Trono con un primer plano de ruina bajo que deja respirar el asiento vacío;
7. La Suspensión como apagón raro, localizado, sin susto ni estado persistente;
8. CI/documentación/artefactos y estado legal de la pista sin afirmar pruebas no
   realizadas.

---

## 2. Treinta mejoras evaluadas

Cada punto incluye problema, solución, beneficio, riesgo, compatibilidad, coste y
decisión. «Implementada» significa aplicada en código/documentación; no equivale
a una prueba dentro del cliente de Minecraft.

| # | Problema real | Solución y beneficio | Riesgo / compatibilidad | Coste / decisión |
|---|---|---|---|---|
| I01 | La escena y el audio podían leer niveles distintos al cambiar el reloj dentro de un frame. | `RotacionNiveles.Estado` captura índice, nivel, luz, instante, estancia y transición una vez; la pantalla lo reparte. Elimina mezclas visuales difíciles de reproducir. | Bajo; API interna y compatible con Forge 47.x. | Sin coste de memoria relevante; **implementada**. |
| I02 | `font.split` de la nota rotativa se ejecutaba en cada frame. | `NotaAviso` conserva líneas por texto y ancho, invalidando solo cuando cambia una de esas entradas. | Bajo; mantiene `AbstractButton`, foco y narración. | Menos trabajo CPU; **implementada**. |
| I03 | La cabecera ya se medía, pero el render podía volver a partir texto. | Se reutilizan listas medidas en `PantallaNivel`; números de nivel y tarifa tienen caché por variante. | Bajo; idioma/resource pack requiere reconstrucción normal de pantalla. | Menos alocaciones; **implementada**. |
| I04 | Cada renglón volvía a medir su etiqueta y leía la luz por separado. | Se cachea el ancho de etiqueta y la pantalla inyecta el mismo `luzFrame` a renglones y aviso. | Bajo; la hitbox sigue siendo el widget vanilla. | Menos llamadas al reloj y al font renderer; **implementada**. |
| I05 | `movimiento_reducido` solo ocultaba polvo/presencia, pero dejaba fuego, agua, telas, haces y fuga vivos. | Con movimiento reducido se pasa un tiempo fijo a planta, materiales, tratamiento y dirección de arte; la presencia queda oculta. | Bajo; `destellos_reducidos` sigue siendo independiente. | Sin fills extra; **implementada**. |
| I06 | La escena usaba instantes distintos para cámara, presencia, penumbra y eventos ambientales. | El tiempo de escena, `Presencia`, `RelojAparicion` y `EventosAmbientales` aceptan el instante capturado. | Bajo; no cambia el contrato de los overloads existentes. | Menos inconsistencias de borde; **implementada**. |
| I07 | `RelojAparicion` formateaba con locale del sistema y podía cambiar entre lecturas. | Se añadió variante con instante/restante y `Locale.ROOT`; el render usa un solo restante. | Bajo; solo afecta formato administrativo `MM:SS`. | Negligible; **implementada**. |
| I08 | Un resource pack local ya activo no recargaba una pista reemplazada. | `MusicaPropia` compara archivos y llama a `reloadResourcePacks()` solo si el recurso cambió. | Medio; la recarga real debe probarse con resource packs. No toca pantallas ajenas. | Una recarga solo cuando corresponde; **implementada**. |
| I09 | Desactivar ambiente mientras la pantalla seguía abierta dejaba camas sonando. | `GestorAmbiente` detiene y limpia capas inmediatamente cuando `sonido_ambiente=false`; al reactivar las crea de nuevo. | Bajo; puede producir un fundido según el motor, pero no deja instancias huérfanas. | O(3) al cambiar la opción; **implementada**. |
| I10 | Los chispazos visuales y sonoros consultaban relojes distintos. | `chispazoActual(Estado)` y `atenderChispazos(Estado)` usan el mismo `dentro` de transición. | Bajo; el cálculo sigue siendo procedural y determinista. | Sin coste extra; **implementada**. |
| I11 | La presencia podía saltar de posición/modo al cruzar un frame. | `Presencia` ofrece overloads con instante para visibilidad, modo, sombra y dibujo; audio y escena comparten valor. | Bajo; overloads conservan callers antiguos. | Sin allocations nuevas; **implementada**. |
| I12 | Ventanas estrechas podían mantener una hoja de 214 px y sacar contenido del viewport. | `PantallaNivel` y `PantallaEstancia` derivan ancho/margen del espacio disponible y miden textos con ese ancho. | Bajo; resoluciones extremas siguen requiriendo checklist manual. | Sin texturas nuevas; **implementada**. |
| I13 | La pausa duplicaba métricas fijas y el subtítulo se partía al dibujar. | La pausa calcula hoja, margen y líneas en `init`, y reutiliza la partición al render. | Bajo; no reemplaza `OptionsScreen`, guardado o navegación vanilla. | Menos trabajo por frame; **implementada**. |
| I14 | El primer plano del Trono era un tambor diagonal que cerraba el cuadro y tapaba su foco. | Se reemplazó por zócalo irregular bajo, derrumbes laterales y seis cascotes separados. El eje y el asiento vacío quedan legibles. | Bajo; solo renderer procedural del cliente. | Menos geometría dominante; **implementada**. |
| I15 | El espejo Python del renderer seguía mostrando el tambor antiguo. | `tools/vista_previa.py` se sincronizó con el primer plano nuevo y se regeneraron las láminas. | Medio de proceso: exige mantener dos implementaciones sincronizadas. | Coste de mantenimiento documentado; **implementada**. |
| I16 | El workflow usaba `gradle` global, una rama inexistente y JAR `1.0.0` fijo. | Usa `./gradlew`, Temurin 17, `clean build`, auditoría estática y artefacto `jobsmenu-${{ github.sha }}`; se dispara en la rama de trabajo y PR a `main`. | Bajo; no modifica `main`; el job real aún debe correr en GitHub. | Sin coste runtime; **implementada**. |
| I17 | El crédito de REQUIEM estaba marcado aunque el recurso empaquetado no coincidía con la pista de referencia. | Se retiró el marcador de crédito y se documentó que `music/REQUIEM-Forsaken-OST.ogg` no viaja al JAR sin permiso. La ranura de fábrica es la pieza del mod. | Bajo y legalmente más seguro; REQUIEM local sigue usando `MASTER` si se aporta con autorización. | Un archivo de marca menos; **implementada**. |
| I18 | La documentación decía que REQUIEM estaba integrada/sonando sin evidencia de runtime. | `docs/musica.md`, `music/LEEME.txt`, README y known issues separan recurso estático, pista local y prueba pendiente. | Bajo; no cambia el SoundEngine. | Coste documental; **implementada**. |
| I19 | `PantallaNivel` podía reconstruir widgets al resize y mantener referencias ambiguas para actualizar luz. | Se conservan referencias explícitas a renglones y nota, se limpian en `init` y se actualizan desde el snapshot. | Bajo; depende del lifecycle normal de `Screen#init`. | Lista pequeña de widgets; **implementada**. |
| I20 | El método público de luz/audio incentivaba lecturas repetidas e inconsistentes. | Se conservaron overloads de compatibilidad y se introdujeron overloads con `Estado`, evitando romper callers existentes. | Bajo; no hay mixin ni reemplazo de pantallas de terceros. | API interna más explícita; **implementada**. |
| I21 | `CapaAmbiente` consultaba nivel y luz por separado en cada tick. | Captura un `Estado` por tick de capa y usa su instante para presencia, nivel y luz. | Bajo; tres capas pueden capturar ticks consecutivos, aceptable para audio no visual. | Menos lecturas repetidas dentro de cada capa; **implementada**. |
| I22 | El documento maestro decía dos camas, pero el código tenía BASE, CARACTER y ACTIVIDAD. | CONTEXTO y documentación pasan a describir 30 camas: tres por cada uno de diez niveles. | Bajo; corrige documentación, no altera registros. | Sin coste; **implementada**. |
| I23 | El silencio del menú no era explícito al apagar la opción de ambiente. | La ruta de configuración cierra capas en caliente; la salida de pantalla sigue llamando `cerrar()`. | Bajo; requiere confirmar `SoundManager` en vivo. | O(3) en transición de estado; **implementada**. |
| I24 | La pausa podía ocupar un ancho fijo y desalinear texto con hitboxes después de resize. | Hoja y widgets comparten ancho derivado, y `RenglonTablon` mide su mensaje cacheado. | Bajo; navegación vanilla se conserva. | Bajo; **implementada**. |
| I25 | Faltaban archivos de proceso solicitados para riesgos y cambios. | Se añadieron `CHANGELOG.md`, `KNOWN_ISSUES.md` y `docs/checklist-manual.md`. | Ninguno de runtime. | Coste documental; **implementada**. |
| I26 | La workflow podía publicar un artefacto sin relación visible con el commit. | El artifact usa SHA y solo acepta `build/libs/jobsmenu-*.jar` no vacío. | Bajo; no cambia el nombre del JAR producido. | Sin coste runtime; **implementada**. |
| I27 | Las notas públicas mencionaban una herramienta de servicio que debía permanecer oculta. | Se retiró la combinación concreta y se dejó solo que el flujo público no la anuncia. | Ninguno en runtime; no se elimina la herramienta. | Coste de limpieza documental; **implementada**. |
| I28 | La vista previa documentaba cuatro/cinco fondos cuando el catálogo tiene diez. | README, CONTEXTO y la herramienta ahora usan hoja de contacto de diez niveles; se generaron evidencias actuales. | Bajo; el PNG sigue siendo espejo y no una prueba Forge. | Tiempo de render Python; **implementada**. |
| I29 | El texto de configuración describía movimiento reducido de forma incompleta. | Comentarios de `ConfigTurno` y CONTEXTO ahora especifican congelado de animación completa. | Ninguno de API. | Negligible; **implementada**. |
| I30 | El build no se podía certificar en el sandbox y el README podía presentarlo como hecho. | Se ejecutó el intento real, se registró el error de Java ausente y se documentó el bloqueo; CI queda preparado para Java 17. | Riesgo residual: compilación y arranque siguen pendientes. | No solucionable sin JDK/acceso de instalación; **documentada como pendiente**. |

---

## 3. Cinco funciones nuevas evaluadas

| # | Problema / oportunidad | Solución, beneficio, riesgo, compatibilidad y coste | Decisión |
|---|---|---|---|
| NF01 · Volumen maestro y silencio rápido | Música, ambiente y gestos tenían controles parciales; quien abre el menú necesita silenciarlo sin navegar. | `volumen_aviso` centraliza el multiplicador y la tecla de servicio M alterna cero con el último valor. Beneficio: control inmediato. Riesgo bajo: un único multiplicador sobre audio del cliente. Compatible con Master del juego; no usa Music vanilla para REQUIEM. Coste mínimo. | **Implementada**; validar tecla, persistencia y mezcla en Minecraft. |
| NF02 · Rotación en calma | 24 s por nivel puede ser corto para leer y observar diez recintos. | `rotacion_calma` duplica la estancia a 48 s y deja transición/chispazos iguales. Beneficio: contemplación sin cambiar geometría. Riesgo bajo: solo reloj procedural. Coste cero runtime. | **Implementada**; validar ciclo real. |
| NF03 · Fecha del turno | El aviso administrativo no tenía contexto temporal visible. | `mostrar_fecha` estampa día/mes/hora medidos y traducidos en la hoja. Beneficio: refuerza turno y legibilidad narrativa. Riesgo bajo: cadenas largas se miden. Compatible con idiomas. Coste de una línea de texto. | **Implementada**; validar idiomas y GUI scale. |
| NF04 · Música local sin recompilar | Cambiar una pista exigía renombrar/activar recursos o podía dejar al usuario sin diagnóstico. | `MusicaPropia` crea `jobsmenu-musica`, valida OGG Vorbis mono, rota candidatos, arma el pack local, lo activa y recarga solo si cambia. Beneficio: personalización legal local y diagnóstico en log. Riesgo medio: PackRepository/SoundManager deben probarse en Forge 1.20.1. No intercepta pantallas de otros mods. Coste: I/O al preparar y recarga solo cuando cambia. | **Implementada**; validar con pista válida, inválida, varios archivos, F3+T y cierre. |
| NF05 · La Suspensión, apagón raro prolongado | Un evento ambiental raro podía dar identidad al menú, pero tocaba luz, audio, accesibilidad y sincronía en una parte crítica. | Ranuras deterministas de 48 min con desfase de hasta 3,5 min (separación aproximada de 45–52 min), ventana de 22 s y luz monótona al 4 %. La escena oculta chispazos, motas, presencia y eventos visuales; `ACTIVIDAD` conserva el piso sonoro, las otras camas y la música ceden, y `NIVEL_APAGON` se reutiliza como suspiro grave a 0,82. El rótulo se localiza. No ocurre con rotación fija. | Medio; no agrega recursos ni estado persistente, pero debe probarse con SoundEngine, redimensionado, movimiento/destellos reducidos y el modpack. | Coste bajo de runtime; **implementada**, pendiente de validación dentro de Minecraft. |

---

## 4. Decisiones de compatibilidad

- Solo se reemplazan clases exactas de título/pausa y solo se adapta
  `OptionsScreen` con el chequeo de clase exacta existente.
- No se envuelven ni reemplazan pantallas de Embeddium, Oculus, ImmediatelyFast,
  Sophisticated Backpacks/Core, Architectury, Cloth Config, Controlling,
  Searchables, Chat Heads, 3D Skin Layers, TRansition, TRender, LowDragLib u
  otros mods. La lista de observaciones y mitigaciones está en
  [`compatibilidad.md`](compatibilidad.md).
- Se mantienen widgets nativos (`AbstractButton`, `OptionsList`,
  `OptionInstance`), narración, foco, teclado/mouse, scroll y flujo de vanilla.
- La música propia usa `SoundSource.MASTER`; el slider Music vanilla no es su
  control. REQUIEM no se presenta como redistribuible sin autorización.
- El ambiente se declara y reproduce como `AMBIENT`, con slider propio y
  volumen maestro del aviso. Su interacción con el slider de vanilla necesita
  la prueba manual del checklist.

---

## 5. Evidencia ejecutada

| Verificación | Resultado |
|---|---|
| Revisión de ramas/tags y preservación de `main` | **OK en checkout**; `main` sigue apuntando a `dc9ccca`. |
| `python3 tools/verificar.py` | **OK** — 0 avisos, 0 fallos. |
| `python3 -m py_compile tools/verificar.py tools/vista_previa.py tools/sonidos.py tools/muestras.py` | **OK**. |
| `git diff --check` | **OK**. |
| `python3 tools/vista_previa.py 854 480 docs/vista_previa.png --nivel=0` | **OK**; PNG generado. |
| `--contacto docs/contacto-actual.png --desnudo` | **OK**; 10 niveles, 960×1350. |
| `--eventos docs/eventos-actual.png` | **OK**; 10 eventos, 960×1350. |
| `--presencia docs/presencia-actual.png --nivel=9 --desnudo` | **OK**; presencia sobre Trono, 1260×472. |
| Simulación determinista de ranuras de La Suspensión | **OK**; 64 intervalos entre 45,18 y 50,74 min; no sustituye la prueba real. |
| `./gradlew clean build --no-daemon` | **BLOQUEADO**: no existe `java` y `JAVA_HOME` no está definido. |
| Arranque de Minecraft/Forge, navegación, SoundEngine, modpack, FPS/GPU | **PENDIENTE**; no ejecutado en este entorno. |

Los PNG procedurales son evidencia de composición, no evidencia de API Forge,
SoundEngine, lifecycle ni funcionamiento dentro del juego.

---

## 6. Arte y audio: resultado de la segunda auditoría

Los diez recintos mantienen una silueta y paleta propias. La revisión se centró
en evitar que microdetalle y atmósfera ganaran a la arquitectura: administración
(sala), depósito (nave), servicio (cañerías), natatorio (agua), sala de piedra,
biblioteca, invernadero, catacumbas, cisterna y Trono.

El Trono queda específicamente con ábside, haz cenital, tarima, columnas y
asiento vacío como foco; el antiguo tambor diagonal ya no tapa el primer plano.
La hoja de contacto actual y los tres renders de auditoría permiten revisar la
composición fuera de Minecraft.

El audio queda organizado en base, carácter y actividad por nivel, eventos
ocasionales, transición, presencia, gestos y pista. El ciclo de vida conserva la
música durante Opciones/Mods y corta ambiente al salir o desactivarlo. Todo lo
relativo a decodificación, sliders y mezcla debe confirmarse con una instancia
Forge real.

---

## 7. Estado de backups y ramas

- `main` oficial no fue modificado ni mergeado.
- Backup A inicial de esta evolución: `seguridad/2026-08-29/evolucion-3/backup-A-inicial`.
- Backup B: `seguridad/2026-08-29/evolucion-3/backup-B-general`, apuntando al
  commit general `38280e3` antes del commit de fondos finales.
- Backup C: `seguridad/2026-08-29/evolucion-3/backup-C-final`, creado sobre
  el commit final después de backgrounds, segunda auditoría, documentación y
  la implementación de las cinco funciones nuevas.
- Los backups son tags de Git, no archivos dentro de `mods`.
- La rama a subir es únicamente `arena/01a04e24-jobs-menu`.
- El tag A sí fue subido a `origin`. La subida de la rama y de los tags B/C fue
  rechazada por GitHub porque el GitHub App de esta sesión no tiene permiso
  `workflows` para crear o actualizar `.github/workflows/build.yml`; no se
  considerará completada hasta reconectar GitHub con ese permiso.

---

## 8. Próximo paso no oculto

Instalar/activar Java 17 en el entorno o ejecutar el job de GitHub, correr
`./gradlew clean build --no-daemon`, instalar el JAR reobfuscado en una instancia
Forge 1.20.1 y completar [`docs/checklist-manual.md`](checklist-manual.md).
Solo después de esa prueba se puede actualizar el informe con un resultado de
Minecraft; hasta entonces, cualquier afirmación de runtime sería incorrecta.
