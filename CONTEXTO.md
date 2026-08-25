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
| Versión actual | **0.3.0** |
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
| Hoja, pie | **Avisos rotativos** (cambian cada 7 s, o a mano; con ajuste de línea) |
| Esquina superior derecha | **Cuenta regresiva a la próxima ronda**, sobre placa oscura |
| Esquina inferior derecha | Sello: `jobsmenu 0.3.0` |

Renglones del formulario:

| # | Etiqueta | Acción real |
|---|---|---|
| 01 | Unirse a una cuadrilla | `JoinMultiplayerScreen` |
| 02 | Registro de intervenciones | `net.minecraftforge.client.gui.ModListScreen` |
| 03 | Condiciones de estancia | `OptionsScreen` |
| — | *(hueco de 10 px)* | |
| 04 | Renunciar al nivel | `Minecraft#stop()` |

**Por qué ese orden y no el vanilla.** Los renglones siguen la frecuencia de uso real de un tablón, no la
costumbre de Mojang: a la cuadrilla se entra todos los días, el registro se consulta seguido, las
condiciones se tocan una vez. El hueco antes de renunciar no es decorativo: separar lo irreversible del
resto es lo que evita que alguien lo pulse por inercia bajando la lista.

**Fichar turno salió del tablón.** La partida de un jugador se abre con **Control + S**
(`client/AtajoOverworld.java`, sobre `ScreenEvent.KeyPressed` / `KeyReleased`, con anti-repetición mientras
la tecla sigue pulsada). Es la salida de servicio, y las salidas de servicio no se anuncian en el tablón; se
aclara al pie de la hoja, en letra chica (`jobsmenu.tablon.atajo`).

### 3.1 La cuenta regresiva (pieza de identidad)

Ciclo fijo de **13 minutos** anclado al reloj del sistema. No depende de la partida ni del servidor: es
ambiente, no mecánica.

- Formato `MM:SS`. Bajo 60 s pasa a `ALERTA`; bajo 8 s pulsa y el rótulo cambia a **RONDA INMINENTE**.
- Al llegar a cero: 4 s de **RONDA EN CURSO** y **el fluorescente baja** (la escena se apaga y se recupera).
- Con *destellos reducidos*, no parpadea: queda fijo en `ALERTA`.

Es deliberadamente inútil: no podés hacer nada al respecto. Ese es el punto.


### 3.2 La línea de avisos

`client/ui/NotaAviso.java` es un `AbstractButton`, no un texto dibujado: entra en el recorrido del tabulador,
se subraya a lápiz al pasarle el cursor y se puede **pasar a mano** con un clic, que además reinicia el reloj
de siete segundos para que el aviso recién traído dure entero. El que quiere leerlos todos no espera; el que
no los mira, ni se entera de que se podía. Suena con `ui.alternar` — pasar una hoja no es elegir una opción,
y no tiene por qué sonar igual.

Su índice vive en campos estáticos: si viviese en la instancia, redimensionar la ventana mandaría el aviso de
vuelta al primero.

### 3.3 Escena viva

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

**La presencia** (`client/scene/Presencia.java`) reemplaza a la silueta caminante de la 0.2.0. Ver §3.7.

### 3.4 Los cuatro niveles

`client/scene/Nivel.java` es el catálogo. Cada nivel cambia proporción, ancho, colores, reflejo y qué cosas
cuelgan de las paredes:

| Clave | Nivel | Proporción | Semiancho | Reflejo | Señas |
|---|---|---|---|---|---|
| `nivel0` | Sección administrativa | 0.92 | 0.082 | 0.16 | Papel mural amarillo, zócalo, humedad total |
| `nivel1` | Depósito | 0.98 | 0.132 | 0.30 | Hormigón, mucho más ancho, neblina |
| `nivel2` | Pasillos de servicio | 0.78 | 0.070 | 0.22 | Estrecho y alto, óxido, tuberías |
| `nivel3` | Las piscinas | 1.02 | 0.098 | 0.62 | Azulejo, casi cuadrado, todo se refleja |

### 3.5 La transición

`client/scene/RotacionNiveles.java`. **El nivel no se funde con el siguiente: se apaga la luz.** Estancia de
24 s, transición de 2.6 s, ciclo de 26.6 s derivado del reloj del sistema (sin estado que sincronizar).

El primer 42 % de la transición apaga con `1 - t²`, con dos titileos de agonía por el camino. El índice del
nivel salta **a mitad del apagón**, cuando no se ve nada. El 58 % restante enciende con `arranqueTubo`: no
es una rampa, es un tubo fluorescente costándole arrancar — `0.55`, cae a `0.05`, salta a `0.80`, cae a
`0.10`, se estabiliza en `0.35` y recién ahí sube parejo.

Cuando la luz vuelve, el pasillo es otro. Nadie lo comenta.

**El apagón apaga también la hoja.** Hasta esta versión el pasillo se quedaba a oscuras pero la hoja, los
renglones y la letra chica seguían a plena luz, flotando legibles en la nada: el detalle que rompía toda la
ilusión, porque el papel no emite. Ahora todo lo impreso pasa por un factor de luz —`0.10 + 0.90·luz`— y el
blanco del papel se oscurece con `Paleta.iluminar`. Se deja un diez por ciento para que la composición no
desaparezca y el ojo sepa que la hoja sigue ahí, en la penumbra. El reloj de ronda es lo único que no se
atenúa: está sobre placa oscura y no pertenece al papel.

Con *destellos reducidos*, la subida es lineal y sin titileos. Con *movimiento reducido* no hay polvo ni
presencia. Cualquiera de los dos deja la escena legible.

> **Nota técnica que costó un bug:** `GuiGraphics#fillGradient` interpola **sólo en vertical**. Las viñetas
> laterales se dibujan columna por columna con `fill`, no con `fillGradient`.

### 3.6 Sonido

**Treinta piezas**, todas sintetizadas para el mod con `tools/sonidos.py` (numpy + scipy + soundfile):
ninguna muestra de terceros, ninguna licencia de por medio. Mono, 44.1 kHz, OGG Vorbis, 838 kB en total.
Semilla fija `0x4A4F4253`, así que regenerarlas da siempre el mismo resultado.

Las seis piezas de la 0.2.0 se descartaron enteras. El problema no era la mezcla: eran genéricas y se
repetían. Esta tanda parte de una regla distinta —**todo tiene que sonar al mismo edificio**— y de ahí
salen las cuatro familias.

#### Interfaz (8)

`ui/{pasar, elegir, confirmar, volver, alternar, abrir, cerrar, negado}`. Materiales del mismo universo:
papel, contacto eléctrico, sello de goma, carpeta. Cortos, sin agudos, sin clicks duros. `MezclaAudio.gesto`
los emite con el tono corrido ±2 %, así que dos pulsaciones seguidas nunca suenan idénticas.

**Los ocho suenan; ninguno quedó de adorno.** Cada uno tiene un momento y sólo uno:

| Pieza | Cuándo |
|---|---|
| `pasar` | Cursor o foco sobre un renglón. Más bajo aún sobre la línea de avisos. |
| `elegir` | Se marca un renglón que abre otra pantalla |
| `confirmar` | Se marca el renglón terminal (renunciar) |
| `alternar` | Se pasa un aviso a mano |
| `abrir` | El aviso aparece por primera vez en la sesión |
| `volver` | Se regresa al aviso desde una pantalla hija |
| `cerrar` | El aviso queda atrás y otra pantalla toma su lugar |
| `negado` | Se pulsa un renglón inactivo |

Los tres de entrada y salida (`abrir`, `volver`, `cerrar`) se disparan desde `EscuchaCliente`, en
`ScreenEvent.Opening`, y **no** desde `init()`/`removed()` de la pantalla. El motivo es concreto: `init()`
se vuelve a ejecutar cada vez que cambia el tamaño de la ventana, así que colgados de ahí sonarían al
redimensionar el juego. El evento, además, dice de dónde se viene, que es justo lo que distingue *abrir* de
*volver*.

> **Dos silencios que hubo que forzar.** `AbstractWidget` reproduce el `UI_BUTTON_CLICK` de vanilla —el
> «clac» de madera del menú original— **antes** de llamar a `onPress()`. Sin anular `playDownSound`, cada
> renglón sonaba dos veces: el clac genérico y el sello del mod detrás. Es exactamente el sonido que el
> aviso no quiere tener, y encima delataba que abajo hay un botón común.
>
> Y al revés: un widget con `active = false` descarta el click sin llamar a nada, así que el renglón
> inactivo se quedaba mudo y `ui/negado` no sonaba nunca. `RenglonTablon` intercepta `mouseClicked` para que
> el relé intente cerrar y no enganche. Un renglón que no hace nada **y** no dice nada se lee como una
> pantalla colgada, que es peor que una negativa.

#### Ambiente por nivel (4)

`ambiente/nivel0..3`, de 20, 23, 18 y 24 segundos. **Duraciones desparejas a propósito:** con bucles de
igual largo el oído encuentra la costura enseguida. Cada uno es un room tone base más su capa
característica.

| Nivel | Qué se oye |
|---|---|
| 0 · Administrativa | Zumbido de balastro, siseo de aire acondicionado, la oficina vacía |
| 1 · Depósito | Cola de reverberación larga, estructura que trabaja, aire moviéndose en volumen grande |
| 2 · Servicio | Tubería con presión, calor, metal cerca, espacio estrecho |
| 3 · Piscinas | Eco de recinto enorme, agua en movimiento, ventilación distante, azulejo |

#### Eventos ocasionales (13)

Tres o cuatro por nivel, disparados por `CapaAmbiente` con probabilidad, retardo y volumen variables:
`nivel0_{tubo, placa, puerta}`, `nivel1_{metal, estructura, lejano}`, `nivel2_{cano, valvula, goteo}`,
`nivel3_{gota, ondas, ventilacion, lejano}`. Nunca dos seguidos, nunca a volumen fijo.

#### Transición, presencia y música (5)

`nivel/{titileo, apagon, encendido}`, `figura/presencia` y `musica/defecto`. Los chispazos de
`nivel/encendido` caen en 0.02, 0.14, 0.22, 0.32 y 0.40 del avance, para casar exactamente con
`arranqueTubo()`.

#### Arquitectura del audio

| Clase | Responsabilidad |
|---|---|
| `client/sound/SonidosNivel.java` | Registro diferido de los 30 eventos |
| `client/sound/MezclaAudio.java` | La mezcla y los gestos. Un solo lugar decide volúmenes. |
| `client/sound/CapaAmbiente.java` | Una capa: su bucle, sus eventos, sus probabilidades |
| `client/sound/GestorAmbiente.java` | Abre y cierra las capas, sigue la transición, dispara el titileo |
| `client/sound/GestorMusica.java` | La música: arranca sola, no se reinicia, sobrevive al cambio de pantalla |

#### La mezcla

| Nivel | Ganancia | Criterio |
|---|---|---|
| Música | 0.42 | Atmósfera. Se tiene que poder ignorar. |
| Ambiente | 0.80 | Audible debajo de todo |
| Transición | 0.85 | Prioridad momentánea: es el único momento que manda |
| Eventos | 0.55 | Sutiles, ocasionales |
| Interfaz | 0.50 | Breve |
| Presencia | 0.40 | Lejos, y agacha el ambiente a 0.62 mientras está |

### 3.7 La presencia del fondo

`client/scene/Presencia.java`. **La figura caminante de la 0.2.0 está descartada por concepto, no por
dibujo.** Algo que cruza el vano con las piernas alternando es un personaje; un personaje se lee enseguida,
se entiende, y a la tercera pasada deja de importar. Encima atravesaba el centro de la composición y le
robaba la escena al aviso, que es lo que el jugador debería estar mirando.

La versión nueva invierte las cuatro decisiones:

1. **No se mueve.** Aparece ya estando ahí. No entra, no sale, no camina. Lo único que hace es dejar de
   estar. Que algo quieto aparezca donde no había nada inquieta más que cualquier movimiento.
2. **No tiene anatomía.** Una columna que se afina hacia arriba, sin cabeza, sin hombros, sin piernas.
   Podría ser una persona muy alta o podría ser un caño. Esa duda es todo el efecto.
3. **Está lejos.** Ocupa la abertura del fondo, nunca el primer plano. No se acerca y no crece.
4. **Entra y sale lento.** Campana `sin²` sobre varios segundos, alfa máximo 0.52. Sin apariciones súbitas.
   **No hay ni va a haber sustos.**

El recurso más fuerte no es ninguna de las cuatro sino **la reaparición**: se muestra 7 s, desaparece 4.5 s,
y vuelve 4 s corrida al otro lado del vano (`+0.41w` → `-0.34w`). El jugador no llega a estar seguro de que
se movió. Ciclo completo cada 71 s.

**Proporciones.** Ancho 0.26 del semiancho de la abertura, alto 1.35 del semialto, 14 segmentos. La
proporción es la única decisión que importa: con 1:14 se lee como una grieta en la pared, con 1:5 se lee
como algo que podría estar parado ahí.

**El color no es fijo.** Pintarla siempre del color del vano parecía correcto y no lo es: en los niveles con
la abertura casi negra (el 0 y el 2) una figura negra sobre fondo negro no existe. `Presencia.tinte()`
deriva el color del fondo de cada nivel — si su luminancia es menor a 0.16, la presencia queda un punto
**más clara** que el vano, como una silueta a contraluz; si no, queda más oscura. Mismo contraste en los
cuatro niveles, en ninguno se la ve del todo.

**Su atmósfera.** Mientras está, el ambiente baja a 0.62, suena `figura/presencia` lejos con reverberación,
y la escena pierde hasta un 8 % de luz (`Presencia.sombra()`). Es un cambio que casi nadie puede señalar y
que todo el mundo siente.

Con *movimiento reducido* o la escena quieta, no aparece.

### 3.8 La música

`client/sound/GestorMusica.java`. Ranura con volumen propio (`volumen_musica`), que arranca sola al abrir el
menú, **no se reinicia al cambiar de pantalla** y **sigue sonando durante el apagón**: es lo único que
atraviesa la transición, y por eso la transición no se siente como un corte.

**Sobre el tema pedido.** El enlace de YouTube es *REQUIEM — Forsaken OST*, del canal **Emmy Z**: es obra de
un tercero con copyright, así que **no se empaqueta**. Fingir que está integrada sería mentir. Lo que hay:

- `musica/defecto.ogg`, pieza original de 67 s (La menor, 8 acordes de 9 s, crossfade de 5 s), incluida en
  el JAR y sonando de fábrica.
- La ranura `musica/tema` queda declarada en `sounds.json`: para usar otro archivo alcanza con dejarlo en
  `assets/jobsmenu/sounds/musica/` y apuntar la entrada, **sin tocar una línea de código**.

---

## 4. Configuración (cliente)

`config/jobsmenu-client.toml`, definida en `config/ConfigTurno.java`.

| Clave | Def. | Qué hace |
|---|---|---|
| `menu_propio` | `true` | Sustituye el título vanilla. En `false` el mod queda invisible. |
| `escena_viva` | `true` | Fondo animado; en `false`, misma composición pero quieta. |
| `movimiento_reducido` | `false` | Apaga el polvo y la presencia del fondo. |
| `destellos_reducidos` | `false` | Congela el parpadeo de los tubos y el pulso rojo. |
| `interfaz_minima` | `false` | Deja sólo la cabecera y los renglones: sin hoja, sin avisos, sin reloj. |
| `mostrar_cuenta_regresiva` | `true` | Control fino del reloj de ronda. |
| `avisos_rotativos` | `true` | Control fino de la línea de avisos. En `false` la línea no existe. |
| `rotar_niveles` | `true` | En `false`, el fondo se queda en un solo nivel. |
| `nivel_fijo` | `0` | Qué nivel mostrar cuando la rotación está apagada (0–3). |
| `sonido_botones` | `true` | Los ocho gestos de interfaz. |
| `sonido_ambiente` | `true` | Ambiente por nivel, eventos ocasionales y los golpes de la transición. |
| `volumen_ambiente` | `55` | Volumen del ambiente, 0–100. |
| `musica_menu` | `true` | La música del menú. |
| `volumen_musica` | `70` | Volumen de la música, 0–100. |

Accesibilidad primero: **cualquiera de esos interruptores deja un menú usable y legible**, nunca uno roto.

---

## 5. Alcance por fases

| Fase | Contenido | Estado |
|---|---|---|
| **0.1.0** | Esqueleto Forge, config, paleta, pasillo procedural, aviso, renglones, reloj de ronda | **Entregado** |
| **0.2.0** | Escena rehecha, cuatro niveles rotando con apagón, audio completo, rótulo de nivel | **Entregado** |
| **0.3.0** | Audio rehecho (30 piezas, ambientes por capas, música), presencia nueva, Ctrl+S, tablón reordenado, registro de mods | **Entregado** |
| 0.4.0 | Pausa ("Estancia en suspenso") y opciones con la misma piel | Pendiente |
| 0.5.0 | Texturas propias (papel mural, alfombra, hoja) y viñeta en textura | Pendiente |
| 0.6.0 | Lore: expediente de niveles, avisos con memoria, easter eggs por fecha/hora | Pendiente |
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
| `tools/vista_previa.py` | Espejo en Python de la escena. Dibuja el menú a PNG sin Minecraft para revisar composición, perspectiva y paleta. Acepta `--nivel=N`, `--figura=0..1`, `--contacto salida.png` (los cuatro niveles en una tira) y `--presencia salida.png` (los seis instantes de la manifestación). Escribe el PNG a mano con `zlib` (no necesita Pillow). **Se sincroniza a mano con `EscenaNivel.java` y `Presencia.java`: si cambia uno, cambia el otro.** |
| `tools/sonidos.py` | Genera las 30 piezas `.ogg` desde cero con numpy, scipy y soundfile (reverberación por convolución incluida). Semilla fija `0x4A4F4253`. Escribe en bloques de 4 s: `sf.write()` con OGG de más de 60 s da segfault en libsndfile 1.2.2. Ninguna pieza viene de una muestra ajena. |

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
