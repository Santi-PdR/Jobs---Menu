# CONTEXTO — Jobs · Aviso a los ocupantes

## Regla vigente de build y despliegue

Esta regla reemplaza cualquier procedimiento historico que aparezca mas abajo en este documento.

- `main` es la rama de entrega. Los cambios estructurales se preparan en una rama de trabajo y solo se integran despues de pasar CI.
- El CI de GitHub es quien verifica y compila la entrega de desarrollo con Java 17.
- El build pasa primero por `tools/verificar.py` y luego por Gradle/Forge.
- El JAR estable para pruebas se publica como `jobsmenu-latest.jar` en la release rodante `dev-latest`.
- El usuario no necesita compilar localmente para una prueba normal: solo ejecuta el PowerShell de `docs/DESPLIEGUE.md`.
- El **unico destino local permitido** es `C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`.
- `jobs-2`, `Test2.0` y cualquier otra instancia no forman parte del flujo vigente.
- Nunca se elimina el JAR instalado antes de descargar y validar el reemplazo.

La escena vigente contiene quince niveles: diez plantas procedurales (0-9) y cinco fondos suministrados integrados como niveles 10-14. Ver `docs/NIVELES_10_14.md`.
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
| Hoja, pie | **Avisos rotativos** (entre 4 y 15 s, 7 s por defecto, o a mano; con ajuste de línea) |
| Esquina superior derecha | **Cuenta regresiva a la próxima ronda**, sobre placa oscura |
| Esquina inferior izquierda | Rótulo del nivel actual (cartel de pared, aparece con la luz nueva) |

Renglones del formulario:

| # | Etiqueta | Acción real |
|---|---|---|
| 01 | Unirse a una cuadrilla | `JoinMultiplayerScreen` |
| 02 | Registro de intervenciones | `net.minecraftforge.client.gui.ModListScreen` |
| 03 | Condiciones de estancia | `OptionsScreen` de vanilla (con un boton "Ajustes del aviso" que el mod inserta, hacia `PantallaAjustesAviso`) |
| — | *(hueco de 10 px)* | |
| 04 | Renunciar al nivel | `Minecraft#stop()` |

**Por qué ese orden y no el vanilla.** Los renglones siguen la frecuencia de uso real de un tablón, no la
costumbre de Mojang: a la cuadrilla se entra todos los días, el registro se consulta seguido, las
condiciones se tocan una vez. El hueco antes de renunciar no es decorativo: separar lo irreversible del
resto es lo que evita que alguien lo pulse por inercia bajando la lista.

La selección de mundo mantiene una herramienta de servicio interna, fuera de la
interfaz y de la documentación pública. No forma parte del flujo normal del aviso.

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
de la nota para que el aviso recién traído dure entero. La duración se configura entre 4 y 15 segundos y
parte de 7 s. El que quiere leerlos todos no espera; el que no los mira, ni se entera de que se podía. Suena
con `ui.alternar` — pasar una hoja no es elegir una opción, y no tiene por qué sonar igual.

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

### 3.4 Los niveles

`client/scene/Nivel.java` es el catálogo. Cada nivel cambia proporción, ancho, colores, reflejo y qué cosas
cuelgan de las paredes:

| Clave | Nivel | Reflejo | Señas |
|---|---|---|---|
| `nivel0` | Sección administrativa | 0.16 | Papel mural amarillo, zócalo, humedad total |
| `nivel1` | Depósito | 0.30 | Hormigón, mucho más ancho, neblina |
| `nivel2` | Pasillos de servicio | 0.22 | Estrecho y alto, óxido, tuberías |
| `nivel3` | Las piscinas | 0.62 | Azulejo, casi cuadrado, todo se refleja |
| `nivel4` | La sala | 0.20 | Piedra cálida, fuego, bóveda, candil de rueda — el guiño al lobby |
| `nivel5` | La biblioteca | 0.14 | Estanterías hasta el techo, lámparas verdes, quietud |
| `nivel6` | El invernadero | 0.18 | Vidrio y hierro, plantas, luz cenital difusa |
| `nivel7` | Las catacumbas | 0.24 | Túnel de piedra fría, nichos, farol — el pariente oscuro de la sala |
| `nivel8` | La cisterna | 0.80 | Columnas sobre agua negra que las refleja, focos sumergidos |
| `nivel9` | El salón del trono | 0.26 | Ruinas, columnas partidas, un trono vacío bajo un haz de luz |

**Nivel 4 es la otra cara del mod.** Los cuatro primeros son backrooms: fluorescente que no se apaga, terror
burocrático y luminoso. La sala es lo contrario —piedra excavada, antorchas, un candelabro de rueda con
velas colgado de la bóveda, estandartes— y su luz es **fuego que titila**, no tubo. Convive en la rotación
como quinto recinto (`client/scene/planta/Cripta.java`). Su primer plano es el borde de una mesa larga de
banquete: se mira la sala desde la cabecera. El vano del fondo es la boca de un túnel sin luz.

### 3.5 La transición

`client/scene/RotacionNiveles.java`. **El nivel no se funde con el siguiente: se apaga la luz.** Estancia de
24 s por defecto, o 48 s con `rotacion_calma`; la transición dura 2.6 s y el ciclo resultante es de 26.6 s
o 50.6 s, derivado del reloj del sistema (sin estado que sincronizar).

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

Con *destellos reducidos*, la subida es lineal y sin titileos. Con *movimiento reducido* se congela
la animacion completa de plantas, materiales, tratamiento, fuga y presencia, y esta ultima no aparece.
Cualquiera de los dos deja la escena legible; son controles independientes.

**La Suspensión** es un evento raro del mismo reloj: aparece una vez por ranura de 48 minutos,
con un desfase determinista de hasta tres minutos y medio, así que la separación observada queda
aproximadamente entre 45 y 52 minutos. Dura 22 segundos y no escribe estado. Durante ese intervalo
la luz baja de forma monótona hasta el 4 %, sin chispazos; la cama `ACTIVIDAD` queda como el piso
sonoro del edificio, las otras camas se hunden, la música se atenúa y suena una única pérdida de
corriente grave. El rótulo cambia a «El edificio suspira.» durante el evento. No ocurre con
`rotar_niveles=false`, no agrega sustos ni toca el mundo.

> **Nota técnica que costó un bug:** `GuiGraphics#fillGradient` interpola **sólo en vertical**. Las viñetas
> laterales se dibujan columna por columna con `fill`, no con `fillGradient`.

### 3.6 Sonido

**Setenta y cuatro piezas** en total: **73 sintetizadas** para el mod con `tools/sonidos.py` (numpy + scipy +
soundfile) —ninguna muestra de terceros, ninguna licencia de por medio— más la pista de música
(`musica/defecto.ogg`, ver §3.8). Todas mono, 44.1 kHz, OGG Vorbis. Semilla fija `0x4A4F4253`, así que
regenerar las sintetizadas da siempre el mismo resultado.

> **Por qué mono, y no es un detalle menor.** El motor de sonido de Minecraft trata distinto lo mono y lo
> estéreo, y una pieza estéreo puede quedar muda aunque el archivo sea válido y los volúmenes estén al máximo.
> Pasó de verdad con la música (ver §3.8). Desde entonces la regla es dura: **todo el audio del mod es mono**.

Las seis piezas de la 0.2.0 se descartaron enteras. El problema no era la mezcla: eran genéricas y se
repetían. Esta tanda parte de una regla distinta —**todo tiene que sonar al mismo edificio**— y de ahí
salen las cuatro familias.

> **Los conteos de esta sección son del diseño original de cuatro recintos** (niveles 0–3). Desde 0.7.0 los
> seis recintos nuevos (4–9) siguen exactamente el mismo patrón —tres camas continuas y tres eventos por
> nivel—, cada uno con sus propias piezas. El total hoy es de 74 `.ogg` (73 sintetizadas + la música). Las
> tablas de abajo se conservan porque explican el *criterio* de diseño, que vale igual para los diez.

#### Interfaz (8)

`ui/{pasar, elegir, confirmar, volver, alternar, abrir, cerrar, negado}`. `MezclaAudio.gesto` los emite con
el tono corrido ±2 %, así que dos pulsaciones seguidas nunca suenan idénticas.

**Cuarta generación.** Van tres descartadas y cada una falló distinto; conviene dejarlo escrito porque los
errores son fáciles de repetir. La primera eran clics. La segunda, clics mejores —sellos, interruptores,
ruedas dentadas—: el problema no era la calidad de cada pieza sino la categoría, porque **un clic es un
objeto que se manipula y acá no hay ningún objeto**; hay una hoja clavada en una pared y un edificio
alrededor.

La tercera acertó el concepto —que suene el edificio y no la interfaz— y falló en el método: los ocho gestos
se construían apilando senoidales sobre múltiplos de 50 Hz con `_red()`. **Una pila de senoidales es lo que
el oído reconoce como sintetizador**, y ocho gestos hechos con el mismo apilado son ocho largos distintos del
mismo zumbido. De ahí que siguieran sin gustar con la idea correcta.

Lo que cambia en esta generación es **la síntesis**. Un objeto real no suena con senoidales armónicas: suena
con modos, resonancias inarmónicas que arrancan juntas y se apagan cada una a su ritmo, las agudas primero.
Esa evolución del timbre mientras suena es lo que el oído lee como material. La función `modal()` de
`tools/sonidos.py` excita un ruido cortísimo y lo pasa por un banco de resonadores muy selectivos; el `Q` de
cada uno sale del decaimiento pedido. No queda un solo oscilador senoidal en la familia.

Y los ocho **ya no son el mismo material**, que era el fondo del problema: el menú necesita que el oído
distinga confirmar de volver sin pensarlo.

| Gesto | Material | Centroide | Ataque |
|---|---|---|---|
| `pasar` | aire desplazado, no resuena nada | 1047 Hz | 34 ms |
| `elegir` | madera del tablón, seca | 718 Hz | 11 ms |
| `alternar` | cerámica del azulejo, corta | 1618 Hz | 8 ms |
| `confirmar` | hormigón, grave, con el vacío detrás | 573 Hz | 8 ms |
| `volver` | el mismo hormigón una quinta abajo | 411 Hz | 41 ms |
| `abrir` | el recinto llenándose de aire | 83 Hz | 224 ms |
| `cerrar` | lo de arriba, al revés | 325 Hz | 13 ms |
| `negado` | dos golpes sordos sobre algo que no cede | 869 Hz | 8 ms |

`_red()` se conserva, pero degradado a lo que debía ser desde el principio: apoyo grave de algunos gestos y
material de la transición. Es cimiento, no material.

Reglas duras de la familia, todas en `tools/sonidos.py`:

| Regla | Motivo |
|---|---|
| Ningún ataque por debajo de 6 ms | Un ataque instantáneo es lo que el oído lee como «clic de computadora» |
| Techo en 5 kHz | Todo lo que pasa de ahí suena a plástico |
| Cuerpo grave siempre presente | Sin grave, el gesto flota por encima del ambiente en vez de apoyarse |
| Una sola sala para los ocho | Distinta cantidad de sala, misma sala: es el mismo sitio |
| Entre 90 y 700 ms | Más corto es un clic; más largo estorba |
| Modos afinados en la menor | La escala del tema: un gesto encima de la música no choca nunca |

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

#### Ambiente por nivel: tres camas continuas (30)

El pedido de la 0.4.0 fue literal: **tiene que haber sonido de fondo a toda hora** —aire, agua—, no sólo
eventos espaciados. Cada nivel monta ahora **tres bucles simultáneos y permanentes**: base, carácter y
actividad. La tercera es casi silenciosa y evita que un ambiente continuo se vuelva una pared plana.

Una sola cama tiene un problema que no se arregla alargándola: por muy bien empalmado que esté el bucle, a
los tres o cuatro pases el oído aprende el archivo. La solución son **tres camas con duraciones distintas**
y fases independientes; sus combinaciones tardan mucho en volver a alinearse, con archivos pequeños.

| Cama | Archivo | Qué lleva | Con el apagón |
|---|---|---|---|
| Base | `ambiente/nivel0..9` | La nota estable del sitio, instalación y volumen de aire | Cede casi del todo: lo que la produce está enchufado |
| Carácter | `caracter/nivel0..9` | Aire, agua, tuberías y movimiento material del recinto | Aguanta más: el edificio sigue moviéndose a oscuras |
| Actividad | `actividad/nivel0..9` | Cola distante y casi silenciosa, una presencia sonora ocasional | Se conserva como suelo del silencio |

Que las tres reaccionen distinto a la luz es lo que hace que la transición suene a **corte de corriente** y no
a bajada de volumen general. Las respiraciones también corren a velocidades distintas y arrancan
desfasadas —cada cama nace con su propia fase—, porque si subieran y bajaran juntas el conjunto
volvería a tener un pulso único y audible.

| Nivel | Base | Carácter |
|---|---|---|
| 0 · Administrativa | Zumbido de balastro, siseo, la oficina vacía | Aire acondicionado con batida lenta de motor, reja de retorno |
| 1 · Depósito | Cola larga, estructura que trabaja, volumen grande | Corriente de aire cruzando la nave, silbido por un hueco alto, chapa dilatándose |
| 2 · Servicio | Tubería con presión, calor, metal cerca | Agua circulando resonada en el diámetro del caño, la bomba dos paredes más allá, goteo casi rítmico |
| 3 · Piscinas | Eco de recinto enorme, ventilación distante, azulejo | Canaleta de rebalse corriendo sin parar, masa de agua en el vaso, lengüetazos sueltos, climatización |

Los niveles 4–9 mantienen la misma arquitectura de tres capas, con timbres propios:
la sala de piedra usa fuego y cadena; la biblioteca, madera, papel y reloj; el
invernadero, vidrio, agua y hojas; las catacumbas, piedra y viento; la cisterna,
gotas y resonancia; y el Trono, ruina, aire alto y actividad distante. Son diez
fondos y treinta camas, no cuatro fondos repintados.

> **Los pesos no suman uno, y no tienen por qué.** Dos ruidos sin relación se suman en potencia, no en
> amplitud. Con 0.82 y 0.66 el conjunto queda en √(0.82² + 0.66²) = 1.05, prácticamente el mismo volumen que
> tenía la cama única. Con los valores intuitivos —1.00 y 0.82— el ambiente subía casi un tercio y se comía
> la música.

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
| `client/sound/SonidosNivel.java` | Registro diferido de los 74 eventos |
| `client/sound/MezclaAudio.java` | La mezcla y los gestos. Un solo lugar decide volúmenes. |
| `client/sound/CapaAmbiente.java` | Una cama continua: su bucle, su respiración, su reacción a la luz. Tres instancias por nivel, con papel `BASE`, `CARACTER` o `ACTIVIDAD` |
| `client/sound/GestorAmbiente.java` | Levanta las tres camas de cada nivel, sortea los eventos, sigue la transición, dispara el titileo y las detiene al desactivar ambiente |
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
**más clara** que el vano, como una silueta a contraluz; si no, queda más oscura. El contraste se
mantiene entre los diez niveles, sin convertirla en protagonista.

**Su atmósfera.** Mientras está, el ambiente baja a 0.62, suena `figura/presencia` lejos con reverberación,
y la escena pierde hasta un 8 % de luz (`Presencia.sombra()`). Es un cambio que casi nadie puede señalar y
que todo el mundo siente.

Con *movimiento reducido* o la escena quieta, no aparece.

### 3.8 La música

`client/sound/GestorMusica.java`. Ranura con volumen propio (`volumen_musica`), que arranca sola al abrir el
menú, **no se reinicia al cambiar de pantalla** y **sigue sonando durante el apagón**: es lo único que
atraviesa la transición, y por eso la transición no se siente como un corte.

**Sobre el tema.** La referencia solicitada es *REQUIEM — Forsaken OST*, del
canal **Emmy Z**. Es una obra de terceros con copyright. El archivo se conserva
en `music/REQUIEM-Forsaken-OST.ogg` como referencia del owner, pero no se copia
al JAR por defecto ni se presenta como redistribuible sin permiso escrito.

- La ranura empaquetada `sounds/musica/defecto.ogg` contiene la pieza original del
  mod. El marcador de crédito solo debe existir cuando la pista y su permiso de
  uso estén confirmados; por eso no se atribuye REQUIEM al recurso de fábrica.
- REQUIEM o cualquier pista local se reproduce por `SoundSource.MASTER`, no por
  `MUSIC`. El Master del juego, el volumen propio de música y
  `volumen_aviso` forman la cadena de mezcla; el slider Music vanilla no es el
  control de REQUIEM.
- Los gestos/eventos usan `MASTER` mediante `forUI`; las camas declaradas como
  `ambient` usan `AMBIENT` y su volumen propio. El gestor de música vanilla se
  detiene mientras el aviso está abierto para que no compita con el tema.
- **Vía sin recompilar:** dejar un OGG Vorbis mono en la raíz de la instancia,
  dentro de `jobsmenu-musica/`; el mod arma y activa el paquete local. El cambio
  y el estado de la pista se registran sin afirmar que ya fueron probados en
  una instancia real. Procedimiento completo en **`docs/musica.md`**.

La reproducción, el decodificador, el slider Master, la recarga del paquete y
el lifecycle de `SoundManager` quedan pendientes de prueba dentro de Minecraft.

---

## 4. Configuración (cliente)

`config/jobsmenu-client.toml`, definida en `config/ConfigTurno.java`.

| Clave | Def. | Qué hace |
|---|---|---|
| `menu_propio` | `true` | Sustituye el título vanilla. En `false` el mod queda invisible. |
| `pausa_propia` | `true` | Sustituye la pausa del juego por "Estancia en suspenso", con la misma piel. En `false` vuelve la pausa vanilla. |
| `escena_viva` | `true` | Fondo animado; en `false`, misma composición pero quieta. |
| `movimiento_reducido` | `false` | Congela la animación de la escena y oculta la presencia del fondo. |
| `destellos_reducidos` | `false` | Congela el parpadeo de los tubos y el pulso de alerta. |
| `alto_contraste` | `false` | Refuerza tinta, papel y siluetas para mejorar la lectura. |
| `texto_grande` | `false` | Aumenta la tipografía y el aire de la hoja cuando la ventana lo permite. |
| `papel_limpio` | `false` | Quita cinta y sombra decorativas, sin quitar contenido. |
| `interfaz_minima` | `false` | Deja sólo la cabecera y los renglones: sin hoja, sin avisos, sin reloj. |
| `mostrar_cuenta_regresiva` | `true` | Control fino del reloj de ronda. |
| `mostrar_fecha` | `true` | Estampa la fecha y hora del turno en la hoja. |
| `mostrar_estado_instalacion` | `true` | Muestra si el recinto está normal, en traslado o suspendido. |
| `guia_lectura` | `true` | Resalta suavemente la fila enfocada. |
| `avisos_rotativos` | `true` | Control fino de la línea de avisos. En `false` la línea no existe. |
| `duracion_avisos` | `7` | Segundos de lectura de cada nota, entre 4 y 15. |
| `rotar_niveles` | `true` | En `false`, el fondo se queda en un solo nivel. |
| `rotacion_calma` | `false` | Duplica la estancia de cada nivel, de 24 a 48 s. |
| `nivel_fijo` | `0` | Qué nivel mostrar cuando la rotación está apagada (0–9). |
| `sonido_botones` | `true` | Los ocho gestos de interfaz. |
| `sonido_ambiente` | `true` | Ambiente por nivel, eventos ocasionales y los golpes de la transición. |
| `eventos_ambientales` | `true` | Permite los cambios visuales y sonoros breves del recinto. |
| `presencia_fondo` | `true` | Permite la presencia ambigua y sus reflejos. |
| `respiracion_camara` | `true` | Activa el vaivén mínimo de la fuga, independiente de otros movimientos. |
| `suspension_rara` | `true` | Permite La Suspensión, el apagón raro de 22 s. |
| `duracion_estancia` | `24` | Segundos que permanece cada nivel antes del apagón, 15–90 (con calma, ×2). |
| `bajo_consumo` | `false` | Modo de bajo consumo: sin polvo, grano, presencia, motas ni respiración de cámara; el recinto y su audio quedan intactos. |
| `perfil_accesible` | `false` | Enciende juntas movimiento reducido, destellos reducidos, alto contraste y texto grande; tocar cualquiera de esas cuatro a mano lo desactiva. |
| `volumen_ambiente` | `55` | Volumen del ambiente, 0–100. |
| `volumen_aviso` | `100` | Volumen maestro del mod: música, ambiente y gestos, 0–100; M silencia y restaura. |
| `musica_menu` | `true` | La música del menú. |
| `volumen_musica` | `70` | Volumen de la música, 0–100. |
| `credito_musica` | `true` | Mostrar el crédito de la pista (título y autor) al empezar a sonar, arriba a la derecha. |

Accesibilidad primero: **cualquiera de esos interruptores deja un menú usable y legible**, nunca uno roto.

### 4.1 Estado de la Evolución 5

La revisión de entrega del 29/08/2026 está documentada en
[`docs/EVOLUCION_5.md`](docs/EVOLUCION_5.md) y la matriz de fondos en
[`docs/AUDITORIA_FONDOS_50X10.md`](docs/AUDITORIA_FONDOS_50X10.md). Hay cincuenta
criterios específicos por cada uno de los diez escenarios; no se marca un criterio
como implementado por cambiar únicamente colores o por compilar. La reescritura
individual de fondos queda después del commit estable y del backup
`seguridad/2026-08-29/evolucion-5/backup-pre-backgrounds`.

El resultado de PowerShell del 29/08/2026 no certifica 0.10.0: se ejecutó en la
rama `arena/01a04e0d-jobs-menu`, con Java 21 y sin Python real, y produjo el
snapshot 0.9.0. El bloque de despliegue vigente valida rama, versión, herramientas,
auditoría, build y artefacto antes de tocar `mods`; no se debe ejecutar por líneas
sueltas ni interpretar un `BUILD SUCCESSFUL` de otra rama como entrega.

**Dónde se tocan estos ajustes.** No hay una pantalla de opciones aparte: son un solo menú. El renglón
*Condiciones de estancia* del aviso (y el mismo renglón de la pausa) abren el `OptionsScreen` de vanilla —el
de imagen, sonido, controles, idioma, recursos—, y ahí el mod inserta un botón **Ajustes del aviso**
(`client/AjustesAviso.java`, sobre `ScreenEvent.Init.Post`) que lleva a `PantallaAjustesAviso`, una
subpantalla de opciones nativa (`OptionsSubScreen` + `OptionsList` + `OptionInstance`) con todos los
interruptores y deslizadores de esta tabla. Cada control aplica el cambio al instante y el guardado se agrupa
con un límite de 250 ms, con vuelco al salir de la pantalla: no hace falta editar el `.toml` a mano.
`PantallaAjustesAviso` no usa `addTitle` (no existe en 1.20.1; `OptionsList` solo ofrece
`addBig`/`addSmall`/`addAll`), para no romper con otras pantallas de opciones de mods.

### 4.2 Estado de la Evolución 6

La evolución 6 (rama `arena/01a04ff1-jobs-menu`, base `811586e`) añadió:
`duracion_estancia`, `bajo_consumo` y `perfil_accesible` (ver tabla), la
continuidad del ambiente por visita (`SesionMenu` + `mantenerCamas()` en el
tick del cliente), el salto manual de nivel (F), la vigilancia de instancia
fantasma de `GestorMusica` blindada contra pausa y falta de foco, y el
diagnóstico oculto Ctrl+D (no documentado en la UI). La etapa artística dio
una mejora por cada uno de los diez fondos (filas AD-15, DE-17, SE-11, NA-22,
SA-11, BI-12, IN-14, CA-13, CI-11 y TR-09/10/11/16/17 de
`docs/AUDITORIA_FONDOS_50X10.md`). Documentación: `docs/EVOLUCION_6.md`,
`docs/CATALOGO_MEJORAS_Y_FUNCIONES.md`, `docs/INFORME_FINAL_EVOLUCION_6.md`.
Build con Java 17, JAR y despliegue en `test-1` hechos el 29/08
(`BUILD SUCCESSFUL`, commit `f23bc66`, SHA256 `305662E3…`); falta la prueba
dentro de Minecraft (`KNOWN_ISSUES.md`).

---

## 5. Alcance por fases

Las filas siguientes son el registro histórico de decisiones y entregas. Para el
comportamiento vigente de 0.10.0 mandan las secciones 3 y 4, `CHANGELOG.md` y
`docs/EVOLUCION_6.md`; en particular, las notas antiguas sobre empaquetar REQUIEM
no sustituyen el contrato actual de música original o pista local autorizada.

| Fase | Contenido | Estado |
|---|---|---|
| **0.1.0** | Esqueleto Forge, config, paleta, pasillo procedural, aviso, renglones, reloj de ronda | **Entregado** |
| **0.2.0** | Escena rehecha, cuatro niveles rotando con apagón, audio completo, rótulo de nivel | **Entregado** |
| **0.3.0** | Audio rehecho (30 piezas, ambientes por capas, música), presencia nueva, herramienta de servicio interna, tablón reordenado, registro de mods | **Entregado** |
| **0.4.0** | Cuatro tipologías de recinto reales, interfaz de cuarta generación (síntesis modal), segunda cama continua por nivel, ambiente audible, vía legal para la música | **Entregado** |
| **0.5.0** | Tercera cama de ambiente, sincronía A/V de la transición, presencia con cuatro modos, primeros planos | **Entregado** |
| **0.6.0** | Interfaz de sexta generación (cinco clases de gesto), música con detección automática, jerarquía de UI medida, luminarias y vanos con cuerpo, sesgo de eventos corregido | **Entregado** |
| **0.6.1** | Ronda de pulido: el agua del natatorio devuelve los tubos del techo (reflejo roto y tembloroso, el detalle que la vuelve agua), más humedad en el azulejado, sello de versión eliminado de la esquina, código muerto retirado | **Entregado** |
| **0.6.2** | Música: vía para hornear una pista con licencia dentro del JAR con crédito en pantalla; la herramienta de servicio pasa a quedar fuera de la interfaz; vapor del natatorio en jirones que se arrastran; `ui.pasar` con el brillo agudo recortado para no cansar | **Entregado** |
| **0.6.3** | La pista REQUIEM (Emmy Z) subida por el owner queda integrada: el build hornea cualquier `.ogg` de `music/` en el JAR (nombre libre), suena de fábrica con su crédito. OGG verificado (Vorbis 44.1 kHz, 3:16, sin clipping, junta de bucle limpia) | **Entregado** |
| **0.6.4** | Arreglo de que la música no sonara: `defecto.ogg` ES ahora REQUIEM directo en los recursos (el horneado en build era frágil y no se ejecutaba); marcador de crédito como recurso real; mezcla de música 0.34→0.55 y entrada 20 s→6 s para que se escuche | **Entregado** |
| **0.6.5** | Nivel 4, **La sala**: quinto recinto de piedra cálida iluminada por fuego (bóveda, columnas, antorchas, estandartes, candil de rueda con velas, runas en el suelo, mesa de banquete en primer plano). Audio propio: tres camas (aire tibio, fuego, la sala vieja) y tres eventos (antorcha, cadena, piedra). El guiño al lobby del server, conviviendo con los cuatro backrooms | **Entregado** |
| **0.7.0** | Cinco recintos nuevos (biblioteca, invernadero, catacumbas, cisterna, salón del trono) con su audio propio (30 piezas más), diez fondos en rotación; REQUIEM re-codificada con libVorbis para que suene; más frases (16 avisos, 3 notas por nivel) | **Entregado** |
| **0.7.1** | La cabecera de la hoja sigue al recinto (dice el Nivel y la tarifa reales, no el fijo "Nivel 0"); easter eggs por fecha/hora en los avisos (año nuevo, 31/10, viernes 13, las 3 AM); pared del fondo de la biblioteca más legible | **Entregado** |
| **0.7.2** | Más frases (avisos rotativos de 16 a 20) y tres easter eggs nuevos (Día del Trabajador —guiño al nombre del server—, Navidad, medianoche); arreglo de maquetación: la hoja ahora reserva alto también para las notas especiales, que en una fecha señalada podían empujar los renglones | **Entregado** |
| **0.8.0** | Condiciones de estancia (opciones del mod con la piel del aviso: casillas y reglas graduadas que escriben y guardan la config sin tocar el archivo) y pausa propia ("Estancia en suspenso"), replicando la salida guardada de vanilla para no chocar con los mods de guardado en segundo plano | **Entregado** |
| **0.8.1** | Las condiciones de estancia recuperan el acceso a las opciones reales del juego: un renglon "Ajustes del equipo" al pie de la hoja abre el `OptionsScreen` de vanilla (imagen, sonido, controles, idioma, recursos). Los ajustes del mod y los del equipo conviven sin que se pierda ninguno | **Entregado** |
| **0.8.2** | Un solo menu de ajustes: se retira la hoja de opciones propia y los ajustes del mod pasan a una subpantalla de opciones nativa (`OptionsSubScreen` + `OptionsList` + `OptionInstance`), a la que se llega por un boton "Ajustes del aviso" que el mod inserta en la pantalla de opciones del juego. Arreglo de la musica: se calla al gestor de musica de vanilla mientras el aviso esta abierto, para que el tema no compita por el canal `MUSIC` (era la causa de que el ambiente se oyera y la musica no) | **Entregado** |
| **0.8.3** | Arreglo real de la musica: la pista era estereo y las camas que si sonaban eran mono; el motor las trata distinto. Se re-codifico REQUIEM a mono (mas volumen) y `stream:false`, igual que el ambiente. Nivel 9 rehecho: trono alto y coronado (brazos, cojin, hueco de la corona) sobre estrado de cinco escalones, abside de piedra al fondo y haz cenital con polvo. Nueva respiracion de camara: la fuga deriva unos pixeles en un vaiven lentisimo en todos los niveles (se apaga con movimiento reducido). El espejo Python queda sincronizado | **Entregado** |
| **0.9.0** | Evolución profesional: revisión de los diez recintos y Trono con primer plano bajo; snapshot temporal por frame para nivel/luz/audio; congelado real de movimiento reducido; cachés de texto y layout responsivo; tres camas ambientales con silencio intencional; lifecycle de audio y apagado inmediato al desactivar ambiente; música por `MASTER`; configuración con guardado limitado; **La Suspensión**, apagón raro localizado de 22 s; auditoría estática, procedimiento de compilación Java 17, documentación y pruebas pendientes separadas | **Código y auditoría estática entregados; build e integración dentro de Minecraft pendientes** |
| **0.10.0** | Evolución de percepción: 10 funciones nuevas de lectura, contraste, papel, cámara, eventos y estado; 50 mejoras auditadas en UI, escena, audio, lifecycle, rendimiento y documentación | **Código y auditoría estática entregados; build e integración dentro de Minecraft pendientes** |
| **0.10.0-E6** | Evolución 6 (rama `arena/01a04ff1-jobs-menu`): configuración ampliada (`duracion_estancia`, `bajo_consumo`, `perfil_accesible`), continuidad del ambiente por visita, salto manual de nivel, bajo consumo en render, vigilancia de instancia fantasma, diagnóstico oculto; etapa artística con una mejora por fondo (10 filas de la matriz) y Trono rediseñado; Backup C `backup-C-final-evolucion6` | **Código, auditoría y build entregados (29/08); integración dentro de Minecraft pendiente** |
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
12. **Todo el audio va en MONO.** El motor de Minecraft trata distinto lo mono y lo estereo;
    una pieza estereo puede quedar muda aunque el archivo sea valido y los volumenes esten al
    maximo. Costo varias entregas descubrir que era eso lo que impedia oir la musica. Cualquier
    `.ogg` que entre al mod se comprueba mono antes de darlo por bueno.
13. **El build cuida la memoria.** `reobfJar` (ForgeGradle) abre un proceso Java aparte que
    reserva ~1/4 de la RAM fisica; en equipos con archivo de paginacion chico el build moria con
    `errno=1455` o el daemon crasheaba. `gradle.properties` va contenido (`-Xmx1024m`, GC serial,
    sin paralelismo) y `build.gradle` capa el heap del fork de reobf en `afterEvaluate`. No subir
    esos numeros a la ligera: la causa de raiz suele ser el archivo de paginacion de Windows.

---

## 7. Herramientas

| Archivo | Para qué |
|---|---|
| `tools/verificar.py` | Sustituto del compilador ausente, en 11 bloques: versiones sincronizadas, **`mods.toml` parseado y validado contra el esquema de Forge 47**, paridad y validez de los `lang`, claves usadas vs. existentes, ASCII puro y balance de delimitadores en `.java`, **metodos llamados que la clase no declara**, recursos (`pack_format`, archivos de Gradle), **coherencia del audio** (los `.ogg` existen, arrancan con la firma `OggS`, `sounds.json` los nombra y Java los registra), **los niveles** (cada uno con su nombre y su nota traducidos, y `nivel_fijo` con el rango correcto), **invariantes de La Suspension** (duración, rotación, escena, audio y localización) y **conexiones de las diez funciones perceptibles** (configuración, opciones y traducciones). |
| `tools/vista_previa.py` | Espejo en Python de la escena. Dibuja el menú a PNG sin Minecraft para revisar composición, perspectiva y paleta. Acepta `--nivel=N`, `--figura=0..1`, `--contacto salida.png` (los diez niveles en una tira) y `--presencia salida.png` (los seis instantes de la manifestación). Escribe el PNG a mano con `zlib` (no necesita Pillow). **Se sincroniza a mano con `EscenaNivel.java` y `Presencia.java`: si cambia uno, cambia el otro.** |
| `tools/sonidos.py` | Genera las 73 piezas `.ogg` sintetizadas desde cero con numpy, scipy y soundfile (reverberación por convolución incluida). Semilla fija `0x4A4F4253`. Escribe en bloques de 4 s: `sf.write()` con OGG de más de 60 s da segfault en libsndfile 1.2.2. Todas mono. Ninguna pieza viene de una muestra ajena; la música (`musica/defecto.ogg`) es la excepción y no la toca este generador. |

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
