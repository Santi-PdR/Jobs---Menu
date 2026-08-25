# CONTEXTO — Jobs · Aviso a los ocupantes

> Documento maestro del mod de menús del servidor **Jobs**.
> Todo cambio de identidad, alcance o proceso se decide **aquí primero** y después se programa.

| Campo | Valor |
|---|---|
| Repositorio | `Santi-PdR/Jobs---Menu` |
| Rama de trabajo | `arena/01a03962-jobs-menu` |
| Mod id | `jobsmenu` |
| Nombre visible | Jobs · Aviso a los ocupantes |
| Paquete Java | `com.santipdr.jobsmenu` |
| Versión actual | **0.2.0** |
| Plataforma | Minecraft **1.20.1** · Forge **47.x** · Java **17** |
| Alcance | Menús (Title / Pause / Options), escena viva, audio, lore. **Sin gameplay.** |
| Lado | **Cliente**. El mod no toca el servidor ni exige instalarse en él. |

---

## 1. De qué va el servidor (canon)

Jobs es un **backrooms con peaje**. Estás atrapado en un nivel, y la única forma de avanzar al siguiente es
**trabajar y juntar el dinero que cuesta la salida**. Por el camino te cruzás con otros supervivientes, armás
tu negocio, y cada tanto aparecen los **Executores**: entidades inmortales que rondan, te persiguen y te
matan. No se derrotan. Se sobreviven.

Los cuatro ejes que el menú tiene que respirar:

1. **Encierro.** Estás *dentro* de algo. El pasillo no se termina, sólo continúa.
2. **Trabajo y dinero.** No sos un héroe: sos mano de obra ahorrando para un pasaje.
3. **La salida es real y cuesta.** Ese es el motor. Hay un vano al fondo y tiene precio.
4. **Executores.** Inmortales, cíclicos, inevitables. El reloj corre siempre.

La tensión central del menú es esa: **un espacio que no te deja salir y una tarifa que sí.**

**Grafía canónica:** *Executor* / *Executores* (mayúscula inicial). Es el término del servidor y no se
"corrige" a Ejecutor. En inglés: *Executor* / *Executors*. Los niveles se escriben *Nivel 0*, *Nivel 1*, …

---

## 2. Identidad del menú

![Vista previa del menú](docs/vista_previa.png)

*Generada con `tools/vista_previa.py` — es un espejo del código de la escena, no una maqueta a mano.*

### 2.1 Concepto visual

**Un aviso fotocopiado, pegado con cinta a la pared de un pasillo amarillo infinito.** El jugador no está
"en un menú": está parado frente a la hoja que explica cuánto cuesta irse.

- Papel mural amarillo mostaza, cielorraso de placas, alfombra húmeda, fluorescentes zumbando. El pasillo
  se dibuja en **perspectiva de un punto** y se pierde al fondo.
- Al final del pasillo hay un **vano oscuro**: la salida al Nivel 1. Nunca se ilumina. Cada tanto **algo lo
  cruza** — no entra ni sale, sólo pasa.
- El terror es **burocrático y luminoso**. No hay oscuridad: hay un amarillo que no se apaga nunca y una
  administración que te cobra el pasaje.

Nada de lo anterior: sin pergamino, sin sepia, sin depósito industrial, sin lámpara de sodio.

### 2.2 Voz narrativa

La voz es la de **la administración del nivel**: la entidad anónima que redactó el aviso, fijó la tarifa y
no piensa dar explicaciones. Fría, breve, con un fondo de amenaza que nunca se aclara.

**Sí:**
- "La salida existe. Cuesta lo que cuesta."
- "El dinero no sirve de nada aquí. Sólo sirve para irse."
- "Si oye el zumbido detenerse, no era el fluorescente."
- "Gracias por su permanencia. Es involuntaria, pero se agradece igual."

**No:**
- Jerga de taller o software ("build", "commit", "config", "render", "bug").
- Épica heroica. A los Executores no se los derrota.
- Guiños meta o chistes que rompan el tono. Se admite **humor negro seco**.

**Regla dura:** todo texto que ve el jugador vive en `lang/*.json` y pasa por esta voz. Ningún literal
suelto en `.java`.

### 2.3 Paleta

Definida en `client/ui/Paleta.java`. ARGB, con alfa explícito.

| Nombre | Hex | Uso |
|---|---|---|
| `PARED` | `#D8C24F` | El papel mural. El color de la casa. |
| `PARED_ALTA` | `#E6D264` | Pared lavada por el fluorescente |
| `PARED_BAJA` | `#9A8630` | Pared cerca del zócalo |
| `MOHO` | `#5E5222` | Humedad, filtraciones, juntas y bordes |
| `ALFOMBRA` | `#8A7638` | El piso |
| `ALFOMBRA_OSCURA` | `#4C401E` | Piso en sombra |
| `TECHO` | `#D5CB9B` | Placas del cielorraso |
| `FLUOR` | `#FFF7D2` | El tubo. La única luz que existe. |
| `PAPEL` | `#F0E9CE` | La hoja del aviso |
| `TINTA` | `#14120C` | Texto principal |
| `TINTA_TENUE` | `#4A422A` | Letra chica, sellos, notas |
| `VANO` | `#0D0B07` | El hueco del fondo. Nunca se aclara. |
| `ALERTA` | `#8E1B12` | Executores |
| `ALERTA_BRILLO` | `#C42B18` | Executores, pulso de ronda inminente |

Reglas de color:
- El **rojo es exclusivo de los Executores**. Nada más en la interfaz puede usarlo.
- La **única fuente de luz es el fluorescente**. `Paleta.iluminar()` apaga cualquier color hacia `VANO`
  según la distancia; nada se ilumina por su cuenta.
- Nunca blanco puro. El techo más limpio sigue siendo hueso viejo.

### 2.4 Tipografía y composición

- Fuente del juego, sin texturas de fuente propias (por ahora).
- Todo el contenido vive **dentro de la hoja**, alineado a la izquierda, como un formulario real.
- Los botones son **renglones de formulario**: casilla marcable al margen, número de orden, etiqueta y
  puntos suspensivos de relleno hasta el borde. Al enfocar, la casilla queda marcada.
- La hoja tiene sombra proyectada y un trozo de cinta adhesiva en el borde superior.

---

## 3. Elementos del menú principal

Implementados en `client/screen/PantallaNivel.java`.

| Zona | Contenido |
|---|---|
| Hoja, cabecera | `JOBS` + `AVISO A LOS OCUPANTES DEL NIVEL` |
| Hoja, bajo la línea | **Nivel actual** y **tarifa de salida** — el motor del server |
| Hoja, cuerpo | Los cuatro renglones del formulario |
| Hoja, pie | **Avisos rotativos** (cambian cada 7 s, con ajuste de línea) |
| Esquina superior derecha | **Cuenta regresiva a la próxima ronda**, sobre placa oscura |
| Esquina inferior derecha | Sello: `jobsmenu 0.2.0` |

Renglones del formulario:

| # | Etiqueta | Acción real |
|---|---|---|
| 01 | Fichar turno | `SelectWorldScreen` |
| 02 | Unirse a una cuadrilla | `JoinMultiplayerScreen` |
| 03 | Condiciones de estancia | `OptionsScreen` |
| 04 | Renunciar al nivel | `Minecraft#stop()` |

### 3.1 La cuenta regresiva (pieza de identidad)

Ciclo fijo de **13 minutos** anclado al reloj del sistema. No depende de la partida ni del servidor: es
ambiente, no mecánica.

- Formato `MM:SS`. Bajo 60 s pasa a `ALERTA`; bajo 8 s pulsa y el rótulo cambia a **RONDA INMINENTE**.
- Al llegar a cero: 4 s de **RONDA EN CURSO** y **el fluorescente baja** (la escena se apaga y se recupera).
- Con *destellos reducidos*, no parpadea: queda fijo en `ALERTA`.

Es deliberadamente inútil: no podés hacer nada al respecto. Ese es el punto.

### 3.2 Escena viva

`client/scene/EscenaNivel.java`, todo procedural (cero texturas). Reescrita entera en 0.2.0: la primera
versión apilaba rectángulos que no convergían y se leía como una escalera, no como un pasillo.

**La geometría sale de un solo punto.** Fuga en `(0.545·ancho, 0.520·alto)` — descentrada a propósito, un
pasillo perfectamente simétrico parece una maqueta. La abertura del fondo mide `w = ancho·semiancho` y
`h = w·proporcion`, ambos del nivel activo. De ahí salen las cuatro aristas a las esquinas de la pantalla.

**La clave es que las tres superficies comparten la serie de profundidades.** `PANELES = 26` tramos, cada
uno a `profundidadPanel(j) = 26/j`. Suelo, cielorraso y paredes recorren la misma serie, así que sus juntas
caen alineadas y el ojo cierra la caja. Con series distintas —el error de la 0.1.0— el pasillo se despega.

Cada punto conoce su distancia al eje: `dx = |x - fugaX| / w`, y de ahí `lejos = clamp(1/dx, 0, 1)`. Eso
gradúa la luz (paredes `0.52 + 0.48·lejos`, techo `0.60 + 0.40·lejos`, suelo `0.55 + 0.45·lejos`) y la
opacidad de cada junta, que se desvanece hacia la cámara en vez de cortarse en un marco rectangular.

Los grosores llevan tope: transversales `max(1, min(h·0.075, h·dx·0.010))`. Sin ese tope, los tramos
cercanos se dibujaban como escalones enormes.

Encima de la caja: humedades deterministas (semilla fija — siempre el mismo pasillo), hilera de
fluorescentes con parpadeo desfasado, reflejo en el suelo según el material del nivel, zócalo, tuberías o
marcos según corresponda, el vano del fondo, polvo y viñeta perimetral.

**La silueta** cruza la abertura del fondo cada 47 s durante 2.6 s: cabeza, hombros y piernas que alternan.
Un solo rectángulo parecía un poste; con tres partes ya camina.

### 3.3 Los cuatro niveles

`client/scene/Nivel.java` es el catálogo. Cada nivel cambia proporción, ancho, colores, reflejo y qué cosas
cuelgan de las paredes:

| Clave | Nivel | Proporción | Semiancho | Reflejo | Señas |
|---|---|---|---|---|---|
| `nivel0` | Sección administrativa | 0.92 | 0.082 | 0.16 | Papel mural amarillo, zócalo, humedad total |
| `nivel1` | Depósito | 0.98 | 0.132 | 0.30 | Hormigón, mucho más ancho, neblina |
| `nivel2` | Pasillos de servicio | 0.78 | 0.070 | 0.22 | Estrecho y alto, óxido, tuberías |
| `nivel3` | Las piscinas | 1.02 | 0.098 | 0.62 | Azulejo, casi cuadrado, todo se refleja |

### 3.4 La transición

`client/scene/RotacionNiveles.java`. **El nivel no se funde con el siguiente: se apaga la luz.** Estancia de
24 s, transición de 2.6 s, ciclo de 26.6 s derivado del reloj del sistema (sin estado que sincronizar).

El primer 42 % de la transición apaga con `1 - t²`, con dos titileos de agonía por el camino. El índice del
nivel salta **a mitad del apagón**, cuando no se ve nada. El 58 % restante enciende con `arranqueTubo`: no
es una rampa, es un tubo fluorescente costándole arrancar — `0.55`, cae a `0.05`, salta a `0.80`, cae a
`0.10`, se estabiliza en `0.35` y recién ahí sube parejo.

Cuando la luz vuelve, el pasillo es otro. Nadie lo comenta.

Con *destellos reducidos*, la subida es lineal y sin titileos. Con *movimiento reducido* no hay polvo ni
silueta. Cualquiera de los dos deja la escena legible.

> **Nota técnica que costó un bug:** `GuiGraphics#fillGradient` interpola **sólo en vertical**. Las viñetas
> laterales se dibujan columna por columna con `fill`, no con `fillGradient`.

### 3.5 Sonido

Seis piezas, **todas sintetizadas para el mod** con `tools/sonidos.py` (numpy + soundfile): no hay muestras
de terceros, así que no hay licencias de por medio. Mono, 44.1 kHz, OGG Vorbis, 144 kB en total.

| Evento | Qué es | Dónde suena |
|---|---|---|
| `ambiente.zumbido` | Fluorescente: 50/100/150/200 Hz, siseo, aire y carraspeos | En bucle, todo el menú |
| `aviso.recorrer` | Roce de papel | Al pasar el foco por un renglón |
| `aviso.marcar` | Sello y clic | Al marcar la casilla |
| `aviso.pesado` | Interruptor de pared | Al saltar a otra pantalla |
| `nivel.apagon` | El tubo se rinde | Al empezar la transición |
| `nivel.encendido` | Chispazos y arranque | Cuando el nivel nuevo se instala |

El zumbido es un `AbstractTickableSoundInstance` en `SoundSource.AMBIENT` sin atenuación. **Su volumen
sigue a la luz de la escena** (`0.35 + 0.65·luz`, suavizado a 0.08 por tick) y su tono cambia con el nivel:
cuando la luz cae en la transición, el zumbido cae con ella. El silencio del apagón es parte del efecto.

`nivel.encendido` está sincronizado con `arranqueTubo`: los chispazos caen donde la luz titila.

---

## 4. Configuración (cliente)

`config/jobsmenu-client.toml`, definida en `config/ConfigTurno.java`.

| Clave | Def. | Qué hace |
|---|---|---|
| `menu_propio` | `true` | Sustituye el título vanilla. En `false` el mod queda invisible. |
| `escena_viva` | `true` | Fondo animado; en `false`, misma composición pero quieta. |
| `movimiento_reducido` | `false` | Apaga polvo y silueta. |
| `destellos_reducidos` | `false` | Congela el parpadeo de los tubos y el pulso rojo. |
| `interfaz_minima` | `false` | Deja sólo la cabecera y los renglones: sin hoja, sin avisos, sin reloj. |
| `mostrar_cuenta_regresiva` | `true` | Control fino del reloj de ronda. |
| `avisos_rotativos` | `true` | Control fino de la línea de avisos. |
| `rotar_niveles` | `true` | En `false`, el fondo se queda en un solo nivel. |
| `nivel_fijo` | `0` | Qué nivel mostrar cuando la rotación está apagada (0–3). |
| `sonido_botones` | `true` | Roce, sello e interruptor de los renglones. |
| `sonido_ambiente` | `true` | Zumbido del fluorescente y los golpes de la transición. |
| `volumen_ambiente` | `55` | Volumen del zumbido, 0–100. |

Accesibilidad primero: **cualquiera de esos interruptores deja un menú usable y legible**, nunca uno roto.

---

## 5. Alcance por fases

| Fase | Contenido | Estado |
|---|---|---|
| **0.1.0** | Esqueleto Forge, config, paleta, pasillo procedural, aviso, renglones, reloj de ronda | **Entregado** |
| **0.2.0** | Escena rehecha, cuatro niveles rotando con apagón, audio completo, rótulo de nivel | **Entregado** |
| 0.3.0 | Pausa ("Estancia en suspenso") y opciones con la misma piel | Pendiente |
| 0.4.0 | Texturas propias (papel mural, alfombra, hoja) y viñeta en textura | Pendiente |
| 0.5.0 | Lore: expediente de niveles, avisos con memoria, easter eggs por fecha/hora | Pendiente |
| 1.0.0 | Pulido, accesibilidad completa, empaquetado para repartir | Pendiente |

Fuera de alcance, explícitamente: entidades, ítems, mecánicas, comandos, economía real, cualquier cosa que
toque el servidor. **La tarifa del menú es decorativa**: no lee el dinero real del jugador.

---

## 6. Reglas de trabajo vigentes

1. **Voz coherente**: ningún texto in-game con jerga de taller o de software.
2. **Docs sincronizados**: si sube la versión, suben `gradle.properties`, este documento y el `README`.
3. **Changelog in-game discrecional**: sólo si el cambio se *ve* o se *oye*.
4. **Revisión obligatoria antes de entregar**: GUI scale 2, 3 y 4, más `interfaz_minima`,
   `movimiento_reducido` y `destellos_reducidos`.
5. **Respaldo antes de cambios estructurales**: `git bundle` del repo.
6. **Sin firmas de memoria**: si una API de Forge/Minecraft no está verificada en el propio repo, no se usa.
   El sandbox no tiene JDK: la compilación real ocurre en el PC del owner.
7. **Verificación estática obligatoria**: `python3 tools/verificar.py` antes de cada entrega.
8. **La escena se mira antes de entregar**: `python3 tools/vista_previa.py`. Si se toca `EscenaNivel.java`,
   **se toca el espejo también** — están sincronizados a mano y esa es su única debilidad.
9. **Cada entrega cierra con el bloque PowerShell** de actualizar + compilar + desplegar.
10. **`mods.toml` se valida parseado, no por grep.** Forge 1.20.1 (rama 47.x) usa
    `mandatory=true` en las dependencias. `type="required"` es sintaxis de NeoForge y de
    Forge posteriores: compila igual y el juego rechaza el jar al arrancar con
    *Missing required field mandatory in dependency*. Ante la duda sobre metadatos de
    plataforma, se consulta la doc de la version exacta — no se escribe de memoria.
11. **Todo metodo que se llama, se declara.** El bloque 7 de `verificar.py` reproduce el
    `cannot find symbol` de `javac` sin compilar. Nacio de un fallo real: la reescritura
    Backrooms se llevo `brilloFluorescente()` y dejo la llamada en pie; el mod no compilo
    en el PC del owner. Si se borra o renombra un metodo privado, hay que seguirle el rastro
    a sus llamadas.

---

## 7. Herramientas

| Archivo | Para qué |
|---|---|
| `tools/verificar.py` | Sustituto del compilador ausente, en 9 bloques: versiones sincronizadas, **`mods.toml` parseado y validado contra el esquema de Forge 47**, paridad y validez de los `lang`, claves usadas vs. existentes, ASCII puro y balance de delimitadores en `.java`, **metodos llamados que la clase no declara**, recursos (`pack_format`, archivos de Gradle), **coherencia del audio** (los `.ogg` existen, arrancan con la firma `OggS`, `sounds.json` los nombra y Java los registra) y **los niveles** (cada uno con su nombre y su nota traducidos, y `nivel_fijo` con el rango correcto). |
| `tools/vista_previa.py` | Espejo en Python de la escena. Dibuja el menú a PNG sin Minecraft para revisar composición, perspectiva y paleta. Acepta `--nivel=N` y `--contacto salida.png` (los cuatro niveles en una tira). Escribe el PNG a mano con `zlib` (no necesita Pillow). **Se sincroniza a mano con `EscenaNivel.java`: si cambia uno, cambia el otro.** |
| `tools/sonidos.py` | Genera los seis `.ogg` desde cero con numpy y soundfile. Requiere un entorno con esas dos bibliotecas; escribe directo en `assets/jobsmenu/sounds/`. Ninguna pieza viene de una muestra ajena. |

---

## 8. Glosario in-fiction

| Término | Significado |
|---|---|
| **Nivel** | Una dimensión del servidor. Se sale pagando. |
| **La salida / el vano** | El hueco al fondo del pasillo. Tiene tarifa. |
| **Tarifa** | Lo que cuesta pasar al nivel siguiente. |
| **Turno** | Una sesión de trabajo. |
| **Cuadrilla** | Un grupo de supervivientes. El multijugador. |
| **Ronda** | El evento cíclico de los Executores. Nunca "spawn", nunca "ataque". |
| **Ocupante** | El jugador, según la administración. |
| **La administración** | Quien escribe los avisos. Nunca se nombra ni se muestra. |
