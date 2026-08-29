# Evolución 4 — diez funciones nuevas y cincuenta mejoras perceptibles

**Proyecto:** Jobs · Aviso a los ocupantes
**Versión:** 0.10.0
**Plataforma:** Minecraft 1.20.1 · Forge 47.x · Java 17
**Rama:** `arena/01a04e24-jobs-menu`
**Alcance:** cliente; no se añaden entidades, ítems, economía, comandos ni gameplay.

Este documento registra una ampliación de percepción sobre la Evolución 3. Las
pruebas estáticas y los renders no equivalen a una prueba dentro de Minecraft.
El build Forge y la validación de SoundEngine siguen pendientes para esta rama
con Java 17. El `BUILD SUCCESSFUL` obtenido el 29/08/2026 en Windows fue en la
rama `arena/01a04e0d-jobs-menu`, con Java 21, y produjo el snapshot 0.9.0; no
certifica este documento ni debe usarse como evidencia de 0.10.0.

## 1. Diez funciones nuevas

Las primeras nueve agregan controles o salidas nuevas. La décima no duplica La
Suspensión existente: agrega el control explícito para permitirla o silenciarla.
Todas aparecen en la subpantalla de Opciones del aviso y conservan defaults que
no cambian el aspecto anterior salvo que el jugador los active.

| # | Problema | Solución y beneficio | Riesgo / compatibilidad / coste | Decisión |
|---|---|---|---|---|
| F01 · Alto contraste | La tinta secundaria y algunos bordes perdían fuerza sobre el papel apagado o con GUI scale desfavorable. | Paleta de tinta/papel reforzada, sin usar el rojo reservado a Executores. Mejora lectura y mantiene la identidad amarilla. | Bajo; sólo colores propios. Sin texturas ni coste de memoria. | Implementada. |
| F02 · Texto grande | El texto pequeño de la hoja y la pausa no sirve igual para todas las distancias de lectura. | Escala tipografía y aire vertical cuando hay espacio; en ventanas compactas conserva el layout seguro. | Bajo; se recalculan medidas y splits. Requiere revisar idiomas largos. | Implementada. |
| F03 · Papel limpio | La cinta y la sombra son atmósfera, pero pueden distraer o dificultar la lectura. | Quita decoración y conserva documento, bordes, widgets e interacción. | Bajo; no toca la escena ni pantallas de terceros. | Implementada. |
| F04 · Presencia separable | La silueta lejana no debe ser obligatoria para quien quiere una escena neutra. | `presencia_fondo` controla figura, reflejo, ducking y anuncio sonoro. | Bajo; el fondo conserva geometría. No agrega listeners. | Implementada. |
| F05 · Eventos ambientales separables | Barridos, humedad, polvo y eventos sueltos pueden ser demasiado activos para una lectura sostenida. | `eventos_ambientales` desconecta la capa visual y la agenda de sonidos sin apagar las camas continuas. | Bajo; las capas existentes terminan suavemente. | Implementada. |
| F06 · Duración de avisos | Siete segundos no son ideales para todas las velocidades de lectura. | `duracion_avisos` permite 4–15 s y reinicia el ciclo sin cambiar hitboxes. | Bajo; un entero acotado y una lectura de reloj. | Implementada. |
| F07 · Respiración de cámara independiente | Quien quiere animación material no siempre quiere que se mueva el punto de fuga. | `respiracion_camara` separa el vaivén de fuga de la animación del recinto. | Bajo; sólo dos senos, sin allocations nuevas. | Implementada. |
| F08 · Estado de instalación | La escena cambia de normal a traslado o suspensión sin una lectura explícita. | Rótulo discreto localizado: normal, traslado o suspendida. No compite con la cuenta. | Bajo; texto medido en cada render de pantalla, sin overlay global. | Implementada. |
| F09 · Guía de lectura | El foco de teclado existía, pero no siempre era evidente en una lista de formulario. | `guia_lectura` activa/desactiva el velo de fila; la marca de casilla y la hitbox siguen intactas. | Bajo; no altera navegación ni narración vanilla. | Implementada. |
| F10 · Control de La Suspensión | El apagón raro puede no encajar en una sesión concreta o en una prueba de accesibilidad. | `suspension_rara` permite desactivarlo en caliente; si está activo conserva duración, silencio parcial, suspiro único y no estado persistente. | Bajo; el reloj sigue siendo determinista y la opción se guarda con límite. | Implementada; el evento aún requiere prueba runtime. |

## 2. Cincuenta mejoras perceptibles auditadas

«Implementada» significa que el comportamiento está en el código actual. Las
filas marcadas «conservada» son contratos perceptibles de Evoluciones anteriores
que fueron revisados para que esta ampliación no los rompiera.

### Visual, lectura y composición

| # | Mejora | Resultado / evidencia |
|---|---|---|
| M01 | Tinta principal más contrastada | `Paleta.tintaPrincipal()` evita que el título se lave. Implementada. |
| M02 | Tinta secundaria más contrastada | Bordes, notas y estado conservan jerarquía. Implementada. |
| M03 | Papel reforzado | `Paleta.papelAviso()` mejora el soporte sin blanco puro. Implementada. |
| M04 | Sombra de papel opcional | `papel_limpio` elimina sólo la sombra, no el contenido. Implementada. |
| M05 | Cinta proporcional | El ancho de cinta ya no queda desmedido en hojas estrechas. Implementada. |
| M06 | Bordes de hoja más sobrios | Papel limpio baja el alfa del borde sin convertirlo en una caja plana. Implementada. |
| M07 | Tipografía grande en aviso | Escala y altura se calculan antes de ubicar widgets. Implementada. |
| M08 | Tipografía grande en pausa | La pausa usa la misma escala y mantiene las tres acciones. Implementada. |
| M09 | Ancho de texto compensado | Los `font.split` reservan espacio para la escala grande. Implementada. |
| M10 | Alto vertical compensado | Filas, huecos y reglas crecen juntos; no se pisan. Implementada. |
| M11 | Estado de instalación localizado | Español e inglés tienen los tres estados. Implementada. |
| M12 | Estado no invasivo | Se coloca abajo a la derecha y desaparece con interfaz mínima. Implementada. |
| M13 | Guía de lectura reversible | El velo de foco se puede apagar sin quitar casilla ni navegación. Implementada. |
| M14 | Dots de renglón con escala | El relleno empieza después de la etiqueta grande. Implementada. |
| M15 | Texto de fila escalado desde su origen | Evita desplazamiento de labels y conserva su hitbox. Implementada. |
| M16 | Pausa con papel limpio | La misma opción visual se aplica a la hoja de suspensión. Implementada. |
| M17 | Crédito usa papel accesible | La firma musical toma el tono de papel del modo elegido. Implementada. |
| M18 | Duración de aviso visible en opciones | Slider 4–15 s con unidad localizada y tooltip. Implementada. |
| M19 | Defaults explícitos | Los diez controles tienen defaults seguros y documentados. Implementada. |
| M20 | Resolución compacta protegida | Texto grande no se fuerza en ventanas donde rompería la composición. Implementada. |

### Escena y movimiento

| # | Mejora | Resultado / evidencia |
|---|---|---|
| M21 | Respiración de fuga independiente | Se puede conservar movimiento de planta sin vaivén de cámara. Implementada. |
| M22 | Menos motas en baja resolución | 24 partículas en viewport pequeño, 52 en viewport amplio. Implementada. |
| M23 | Sin array temporal en biblioteca | Reemplazado `new int[] {-1, 1}` por iteración escalar. Implementada. |
| M24 | Eventos visuales apagables | `EventosAmbientales` respeta su opción antes de calcular fases. Implementada. |
| M25 | Presencia visual apagable | Figura y reflejo desaparecen juntos. Implementada. |
| M26 | Presencia sonora apagable | No se agenda el gesto de figura cuando está desactivada. Implementada. |
| M27 | Suspensión apagable | El snapshot no crea la ventana rara si el control está desactivado. Implementada. |
| M28 | Suspensión sin parpadeo | `luzSuspension` usa entrada/salida monotónicas y piso de 4 %. Implementada. |
| M29 | Suspensión sin efectos aéreos | No aparecen motas, presencia ni eventos visuales durante el apagón raro. Implementada. |
| M30 | Suspensión sin doble transición | Suspiro y transición normal no apilan dos secuencias de apagón. Implementada. |

### Audio y respuesta

| # | Mejora | Resultado / evidencia |
|---|---|---|
| M31 | Agenda de eventos desconectable | La opción evita nuevos eventos, sin cortar camas continuas a mitad. Implementada. |
| M32 | Piso de actividad durante suspensión | Sólo la cama `ACTIVIDAD` queda como respiración del edificio. Implementada. |
| M33 | Ducking musical de suspensión | La música cede a 18 % sin reiniciarse. Implementada. |
| M34 | Suspiro único por ranura | `cicloSuspension` evita repetir el sonido por resize o varios frames. Implementada. |
| M35 | Pitch de suspiro documentado | Reutiliza `NIVEL_APAGON` a 0.82, sin recurso no verificado. Implementada. |
| M36 | Control maestro preservado | M y `volumen_aviso` siguen gobernando las funciones nuevas. Conservada. |
| M37 | Canal Master preservado | REQUIEM no depende del slider Music vanilla. Conservada. |
| M38 | Tres camas preservadas | Base, carácter y actividad continúan separadas por papel. Conservada. |
| M39 | Silencio fuera de menú preservado | Las funciones nuevas no abren audio fuera de la sesión. Conservada. |
| M40 | Eventos y presencia comparten reloj | El estado del frame mantiene sincronía A/V. Conservada. |

### Robustez, compatibilidad y entrega

| # | Mejora | Resultado / evidencia |
|---|---|---|
| M41 | Configuración acotada | Duración se limita a 4–15; porcentajes continúan en 0–100. Implementada. |
| M42 | Guardado diferido | Los nuevos toggles no escriben el TOML en cada frame. Conservada. |
| M43 | Guardado al salir | `PantallaAjustesAviso.removed` fuerza el último valor. Conservada. |
| M44 | Sin widgets duplicados | `init()` limpia y reconstruye la lista nativa. Conservada. |
| M45 | Pantallas ajenas intactas | No se amplió ningún `instanceof`; sólo clase exacta de opciones. Conservada. |
| M46 | Foco y narración intactos | F01–F10 no sustituyen `AbstractButton`, `OptionsList` ni narración. Conservada. |
| M47 | Idiomas en paridad | Las nuevas claves existen en `es_es.json` y `en_us.json`. Implementada. |
| M48 | Auditoría específica de funciones | `verificar.py` revisa invariantes de suspensión y claves. Implementada. |
| M49 | Documentación sincronizada | README, CONTEXTO, CHANGELOG, KNOWN_ISSUES, checklist, compatibilidad, música y `music/LEEME.txt` reflejan 0.10.0. Implementada. |
| M50 | Estado de validación honesto | Build Forge, SoundEngine, GPU y modpack quedan marcados como pendientes. Implementada. |

## 3. Archivos principales

- `ConfigTurno.java`: diez controles nuevos, límites y persistencia diferida.
- `PantallaAjustesAviso.java`: controles nativos y slider de duración.
- `Paleta.java`, `HojaPapel.java`, `RenglonTablon.java`: contraste, papel,
  guía y texto grande.
- `PantallaNivel.java`, `PantallaEstancia.java`: layout y estados visibles.
- `EscenaNivel.java`, `EventosAmbientales.java`, `Presencia.java`: movimiento y
  eventos separables, menor ruido en baja resolución.
- `RotacionNiveles.java`, `CapaAmbiente.java`, `GestorAmbiente.java`,
  `GestorMusica.java`: control y mezcla de La Suspensión.
- `tools/verificar.py`: invariantes estáticos de la evolución.
- Bloque PowerShell copiable al final de `README.md`: actualización reproducible
  de la rama, auditoría, build y despliegue seguro en la instancia local; no se
  mantiene un archivo de PowerShell.

## 4. Validación ejecutada

- `python3 tools/verificar.py`: debe terminar con cero fallos y cero avisos.
- `python3 -m py_compile tools/verificar.py tools/vista_previa.py tools/sonidos.py tools/muestras.py`.
- `python3 -m json.tool` sobre los dos idiomas.
- `git diff --check`.
- Renders procedurales previos conservados; la nueva configuración visual debe
  revisarse además en Minecraft, porque el espejo no ejecuta `OptionsList`,
  `Font` ni `SoundEngine` de Forge.
- `./gradlew clean build --no-daemon`: pendiente por Java 17 ausente.

## 5. Pruebas manuales añadidas

El checklist exige probar cada nuevo control en español e inglés, GUI scale
pequeño y grande, ventana estrecha, navegación con Tab, pausa propia, entrada a
Opciones, F3+T y salida al mundo. También exige verificar La Suspensión con el
control activo y desactivado, sin afirmar éxito hasta ejecutarlo realmente.
