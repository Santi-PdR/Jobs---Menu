# Jobs · Aviso a los ocupantes

Mod **de cliente** que reemplaza los menús de Minecraft por los del servidor **Jobs**: un aviso fotocopiado
y pegado con cinta a la pared de un pasillo amarillo que no se termina. Dice en qué nivel estás, cuánto
cuesta la salida al siguiente, y cuánto falta para la próxima ronda de los **Executores**.

Al fondo del pasillo hay un vano oscuro. Cada tanto algo lo cruza.

El fondo va cambiando de nivel solo. Entre uno y otro se corta la luz.

No añade objetos, ni entidades, ni mecánicas. Sólo cambia lo que ves antes de entrar a trabajar.

![Vista previa del menú](docs/vista_previa.png)

| | |
|---|---|
| Versión | **0.8.3** |
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente (el servidor no necesita el mod) |

## Qué trae la 0.8.3

- **La música, arreglada de raíz (de verdad esta vez).** El archivo estaba bien, pero era **estéreo** y todas
  las camas de ambiente que sí se oían eran **mono**: el motor de sonido de Minecraft las trata distinto, y
  con los sliders al 100 el tema quedaba mudo mientras el ambiente sonaba. Se re-codificó REQUIEM a **mono**
  (con más volumen) y se marcó como `stream:false`, igual que las camas que funcionan. Ahora suena.
- **El salón del trono, rehecho.** Antes el trono era una silla diminuta perdida al fondo y no se sentía como
  una sala del trono. Ahora hay un **trono alto y coronado** —con brazos, cojín y el hueco de la corona que
  falta— sobre un **estrado de cinco escalones**, enmarcado por un **ábside** de piedra al fondo y bañado por
  un **haz de luz cenital** con polvo suspendido. Es el centro de la escena, y está vacío.
- **Respiración de cámara.** Todo el recinto se mece ahora con un vaivén lentísimo de un par de píxeles, como
  si quien mira respirara. No es un temblor: es que el pasillo nunca está del todo quieto. Se apaga con
  *movimiento reducido*.

## Qué trae la 0.8.2

Un solo menú de ajustes, y la música arreglada:

- **Se acabaron los dos menús de configuración.** La versión anterior tenía una hoja de ajustes propia
  además de las opciones del juego. Ahora hay **uno solo**: "Condiciones de estancia" abre las opciones de
  Minecraft de siempre —imagen, sonido, controles, idioma, recursos— y ahí, arriba a la izquierda, hay un
  botón **"Ajustes del aviso"** que abre los ajustes del mod como una sección más, con los mismos
  interruptores y deslizadores nativos que el resto del menú de opciones. Nada de dos sitios donde buscar.
- **La música ya suena.** El problema no era el archivo: Minecraft tiene su propio reproductor de música de
  menú que usa el mismo canal (Music) que el tema del mod, y lo tapaba. Ahora, mientras el aviso está
  abierto, se silencia el reproductor de vanilla para dejarle el canal al tema. *(Nota: el deslizador
  **Música** del juego tiene que estar por encima de cero; si está al mínimo, no hay mod que lo levante —por
  eso el ajuste de volumen del aviso lo aclara.)*

## Qué trae la 0.8.1

- **Las opciones del juego siguen a un renglón de distancia.** En la 0.8.0, "Condiciones de estancia" pasó a
  mostrar los ajustes del mod y dejaba fuera los del equipo (imagen, sonido general, controles, idioma,
  paquetes de recursos). Ahora la misma hoja lleva, al pie y apartado, un renglón **"Ajustes del equipo"** que
  abre las opciones reales de Minecraft tal cual. No se perdió ninguna: lo del sitio está en la hoja, lo de la
  máquina a un toque.

## Qué trae la 0.8.0

Dos pantallas nuevas con la misma piel del aviso, y por fin los ajustes al alcance de la mano:

- **Condiciones de estancia: las opciones del mod, en la hoja.** El renglón "03 Condiciones de estancia" ya no
  abre las opciones de Minecraft: abre una hoja propia con todos los ajustes del mod. Cada uno es una casilla
  que se marca —imagen, sonido, accesibilidad— y los dos volúmenes son **reglas graduadas** dibujadas a mano,
  no barras rellenas. Lo que tocás **se guarda solo**: ya no hay que editar ningún archivo a mano.
- **Estancia en suspenso: la pausa, tematizada.** Al pausar la partida aparece el aviso en vez del menú gris:
  *Retomar el turno*, *Condiciones de estancia* y *Dejar el turno*. El botón de salir replica **exactamente**
  la secuencia de guardado de Minecraft, así que los mods que guardan o respaldan en segundo plano siguen
  funcionando igual. Se puede apagar (`pausa_propia = false`) y vuelve la pausa de siempre.
- La hoja de papel —el dibujo del papel, la cinta, el borde húmedo— pasó a ser una pieza compartida, así las
  tres pantallas (aviso, condiciones y pausa) envejecen igual y no se separan con el tiempo.

## Qué trae la 0.7.2

Más voz de la administración, y un arreglo de maquetación que sólo se veía en fechas señaladas:

- **Cuatro avisos rotativos nuevos** (de 16 a 20): el ascensor que nunca funcionó, el turno indefinido, lo
  que pide comida y las luces que se apagan solas. La misma voz seca de siempre.
- **Tres easter eggs nuevos por fecha y hora.** El **Día del Trabajador** —guiño directo al nombre del
  server—, la **Navidad** y el minuto justo de la **medianoche**, el cambio de turno. Aparecen sólo esos días
  y sólo de vez en cuando: se descubren, no se anuncian.
- **La hoja ya no baila en las fechas señaladas.** Reservaba alto midiendo sólo los avisos comunes; una nota
  especial más larga empujaba los renglones hacia abajo. Ahora mide también las notas especiales, así la hoja
  tiene el mismo tamaño cualquier día del año.

## Qué trae la 0.7.1

Pulido de inmersión sobre lo que ya había:

- **La hoja del aviso ya no miente.** Antes decía siempre "Se encuentra usted en el Nivel 0" aunque el fondo
  estuviera mostrando las catacumbas. Ahora la cabecera sigue al recinto: si ves el Nivel 7, la hoja dice
  Nivel 7 y "Salida al Nivel 8". El mismo aviso, releído por la administración de cada nivel.
- **Easter eggs por fecha y hora.** La administración cuela una nota propia en año nuevo, el 31 de octubre,
  los viernes 13 y de 3 a 4 de la madrugada. Aparecen sólo esos días y sólo de vez en cuando: se descubren,
  no se anuncian, y siguen la misma voz seca de siempre.
- **La biblioteca se lee mejor:** la pared del fondo detrás del ventanal ya no es un agujero negro.

## Qué trae la 0.7.0

**Cinco fondos nuevos, diez en total.** Al lado de los cuatro backrooms y la sala de piedra ahora rotan:

- **La biblioteca** — estanterías de madera hasta el techo, lámparas de pantalla verde, un ventanal gris al
  fondo. El sitio más quieto de todos.
- **El invernadero** — una nave de vidrio y hierro tomada por las plantas, iluminada desde el techo por luz
  natural difusa, con haces polvorientos y vaho verde.
- **Las catacumbas** — un túnel de piedra fría con nichos en las paredes, un farol colgado y goteras. El
  pariente oscuro de la sala.
- **La cisterna** — un aljibe enorme: columnas naciendo de un agua negra que las refleja, focos sumergidos,
  gotas con eco larguísimo.
- **El salón del trono** — una sala de audiencias en ruinas, columnas partidas, y al fondo, bajo un haz de
  luz, un trono vacío.

Cada uno tiene **su propio audio**: tres camas de ambiente (base, carácter, actividad) y tres eventos
sueltos, sintetizados desde cero como los demás. Son 30 piezas nuevas, 74 en total.

**La música se arregló de raíz.** REQUIEM no sonaba porque el archivo venía codificado por FFmpeg, y el
decodificador de Minecraft lo descartaba. Se re-codificó con el encoder de referencia (libVorbis) y ahora
suena; además entra más rápido y a mejor volumen.

**Más frases.** Los avisos rotativos bajo los botones pasaron de 8 a 16, y cada fondo tiene ahora tres notas
que van rotando: cada vez que un nivel vuelve, dice algo distinto.

## Qué trae la 0.6.5

**Un quinto fondo: La sala.** El primer recinto que no es un backroom. Una sala de piedra excavada, cálida,
iluminada por **fuego**: bóveda de sillares, columnas, antorchas en las paredes que titilan cada una a su
ritmo, estandartes que ondean, y un **candelabro de rueda de carro con velas** colgado del centro. En el
suelo, runas que laten; en primer plano, el borde de una mesa larga de banquete con un candelabro y una
jarra —se mira la sala desde la cabecera—. Al fondo, la boca oscura de un túnel. Es el guiño al lobby del
server, y convive con los cuatro backrooms en la rotación. Trae su propio audio: el aire tibio de la sala, el
crepitar del fuego, la construcción vieja que se acomoda, y sucesos sueltos (una antorcha que prende, la
cadena del candil, piedra asentándose).

**Y la música ahora se escucha de verdad.** En la 0.6.4 se arregló que REQUIEM no sonaba —el paso de
compilación que la metía era frágil y no llegaba a ejecutarse—; ahora la pista es directamente el archivo de
recursos, entra en seis segundos en vez de veinte y suena a un volumen que se oye.

## Qué trae la 0.6.3

**REQUIEM ya está integrada y suena.** El owner subió la pista al repo
(`music/REQUIEM-Forsaken-OST.ogg`) y el mod la hornea en el `.jar` al compilar: es la música del menú de
fábrica, con el crédito **REQUIEM · Emmy Z · Forsaken OST** apareciendo arriba a la derecha al empezar. El
archivo se verificó: OGG Vorbis a 44.1 kHz, 3:16, sin saturación, con la junta del bucle limpia (final e
inicio casi en silencio, sin clic). El build ahora toma **cualquier** `.ogg` de `music/`, así que el nombre
del archivo ya no importa.

## Qué trae la 0.6.2

**La música viaja dentro del JAR, con crédito en pantalla.** La pista **REQUIEM · Emmy Z · Forsaken OST**
está incluida (`music/REQUIEM-Forsaken-OST.ogg`): al compilar, queda horneada en el `.jar` reemplazando al
tema sintetizado, y el menú muestra el crédito arriba a la derecha, una vez por sesión, entrando y saliendo
suave. El crédito sólo aparece si hay una pista con autor; sobre el tema propio del mod no se muestra, porque
esa pieza no es de nadie más. Cualquier `.ogg` que dejes en `music/` sirve —el nombre da igual—; los pasos
exactos están en [`docs/musica.md`](docs/musica.md).

> La pista es obra de Emmy Z y está incluida por decisión del owner para un server entre amigos con el
> crédito puesto. Para repartir el mod públicamente haría falta permiso escrito de la autora.

**Ctrl+S es ahora una herramienta oculta.** El atajo que salta a la selección de mundos sigue funcionando
igual, pero ya no se anuncia en ninguna parte del menú: es una comodidad de desarrollo y administración,
invisible para quien sólo viene a jugar. La hoja quedó más limpia sin esa línea al pie.

**Más atmósfera en la Poolroom.** El vapor sobre el agua dejó de ser una banda pareja: ahora son jirones que
se arrastran muy despacio de un lado a otro, como el aire cargado de un natatorio cerrado. Es el segundo
movimiento continuo del agua, junto con las cáusticas.

**El sonido de recorrer los renglones cansa menos.** `ui.pasar` —el gesto que más veces suena— tenía casi un
quinto de su energía por encima de 5 kHz, el brillo agudo que fatiga el oído en una sesión larga. Se le
recortó el techo sin quitarle el carácter de papel: sigue siendo el mismo roce, ahora escuchable una hora
seguida sin molestar. Los otros siete gestos no se tocaron.

## Qué trae la 0.6.1

Una ronda de pulido, sin rehacer nada de lo que ya funcionaba.

**El agua del natatorio ahora es agua.** Los tubos del techo se reflejan sobre la superficie de la pileta:
una columna de luz que cae bajo cada luminaria, estirada hacia la cámara y **partida en trozos que tiemblan**
cada uno a su ritmo. Un reflejo entero se lee como un espejo; uno roto y tembloroso se lee como una lámina de
agua, y ese era el detalle que le faltaba a la escena. El azulejado del recinto lleva además algo más de
humedad, que es lo propio de un natatorio cerrado.

**Se quitó el sello de versión** que aparecía en la esquina: era información técnica que no le decía nada a
quien mira el aviso. Y se retiró código muerto que se había quedado de reescrituras anteriores —un
constructor sin uso, un par de métodos que ya nadie llamaba— sin tocar nada de lo que se ve ni se oye.

## Qué trae la 0.6.0

Una pasada fina sobre todo, hecha midiendo el conjunto en vez de revisando archivos sueltos. Es de donde
salieron los hallazgos que importan.

**La familia de sonidos de interfaz se rehízo entera, por sexta vez, y esta vez el diagnóstico fue
numérico:** siete de los ocho gestos tenían el centroide entre 374 y 649 Hz, los ocho tenían cero energía
por encima de 4 kHz y los ocho tenían la misma envolvente de golpe-y-cola. Era un solo sonido con ocho
alturas. La causa era que toda la materia prima eran impactos, y el oído clasifica por envolvente antes que
por material. Ahora hay **cinco clases de gesto distintas** —roce, trinquete, pestillo, posar con peso,
succión— y el centroide va de 264 a 5080 Hz. El parentesco lo cargan la sala común y que ningún ataque sea
instantáneo, no la altura.

**Dos errores de mezcla que llevaban versiones sin detectarse.** El sesgo de las esperas entre eventos
estaba invertido —hacía `sesgo*sesgo`, que acerca a cero— así que el ambiente sonaba al doble de densidad de
lo diseñado. Y `ambiente/nivel2` tenía el **100 % de su energía por debajo de 60 Hz**: en auriculares
normales ese nivel no tenía ambiente.

**La música ya no pide nada:** dejás un `.ogg` en una carpeta y suena. **Los textos ya no se solapan:** la
hoja se mide de arriba abajo en vez de tener dos anclajes que chocaban. Y en los fondos se rehicieron los
tres elementos que todavía parecían pegatinas.

### Cuatro recintos, no cuatro paletas

Cada nivel es ahora **un tipo de local distinto**, con su propia planta dibujada: la administración es una
**sala** ancha de cielorraso bajo; el depósito es una **nave** de hileras altas; el servicio es un **haz de
cañerías** estrecho que dobla; las piscinas son un **natatorio** con vaso, calles y escalerilla. No comparten
geometría: cambian la proporción, el semiancho, la altura del horizonte, el testero del fondo y la cantidad
de tramos en fuga. La hoja de contacto de las cuatro está en `docs/vista_previa.png`.

### El fondo ya no se calla nunca

El pedido era literal: tiene que haber sonido de fondo a toda hora, aire o agua, no solo eventos espaciados.
Ahora cada nivel monta **dos camas continuas a la vez**:

- la **base**, que es la nota del sitio —el volumen de aire, la sala, el zumbido de la instalación—, y que se
  va casi del todo cuando se corta la luz porque casi todo lo que la produce está enchufado;
- el **carácter**, que es lo que se mueve —el aire corriendo por el conducto, la masa de agua del vaso, la
  circulación de las cañerías, el goteo— y que **aguanta el apagón**, porque el agua sigue moviéndose a
  oscuras.

Las dos camas de cada nivel duran distinto a propósito (por ejemplo 24 y 43 segundos) y respiran a
velocidades distintas: como no son múltiplos, la combinación tarda más de un cuarto de hora en repetirse.
Ese es todo el truco para que un fondo de dos archivos chicos no se vuelva reconocible.

### La interfaz, por cuarta vez

Van tres familias descartadas y cada una falló por un motivo distinto. Las dos primeras eran clics —primero
comunes, después buenos: sellos, interruptores, ruedas dentadas—. El problema no era la calidad de cada pieza
sino la categoría: **un clic es un objeto que se manipula, y acá no hay ningún objeto**; hay una hoja clavada
en una pared y un edificio alrededor.

La tercera acertó el concepto —que suene el edificio, no la interfaz— y lo arruinó en la ejecución: los ocho
gestos estaban construidos apilando senoidales sobre múltiplos de 50 Hz. **Una pila de senoidales es
exactamente lo que el oído reconoce como sintetizador**, y ocho gestos hechos con el mismo apilado terminaban
siendo ocho largos distintos del mismo zumbido. Por eso seguían sin gustar aunque la idea fuera correcta.

Esta cuarta generación cambia el método de síntesis. Un objeto real no suena con senoidales armónicas: suena
con **modos**, un puñado de resonancias inarmónicas que arrancan juntas y se apagan cada una a su ritmo, las
agudas primero. Eso es lo que hace que el oído diga «chapa», «hormigón» o «cerámica» en vez de «tono». Los
ocho gestos se sintetizan ahora filtrando ruido con resonadores afinados en proporciones inarmónicas, y no
queda un solo oscilador senoidal en la familia.

Y sobre todo, **los ocho ya no son el mismo material**. Un menú necesita que el oído distinga confirmar de
volver sin pensarlo, y eso no se consigue con duraciones distintas del mismo timbre:

| Gesto | Material | Centroide |
|---|---|---|
| pasar | aire desplazado, no resuena nada | 1047 Hz |
| elegir | la madera del tablón, seca | 718 Hz |
| alternar | cerámica del azulejo, corta | 1618 Hz |
| confirmar | hormigón, grave, con el vacío detrás | 573 Hz |
| volver | el mismo hormigón una quinta abajo | 411 Hz |
| abrir | el recinto llenándose de aire | 83 Hz |
| cerrar | lo de arriba, al revés | 325 Hz |
| negado | dos golpes sordos sobre algo que no cede | 869 Hz |

Se mantienen las reglas duras que sí eran buenas —ningún ataque por debajo de 6 ms, nada por encima de 5 kHz,
todos por la misma sala— y se agrega una nueva: los modos se afinan sobre la escala del tema del menú, en la
menor. No como melodía, sino para que un gesto que suene encima de la música no choque nunca.

### El ambiente que no sonaba

En la 0.3.0 el ambiente estaba bien registrado, bien mezclado y era inaudible. La causa: el motor de sonido
**descarta cualquier sonido cuyo volumen sea cero en el instante de arrancar**, y no lo vuelve a mirar nunca.
Todas las camas entran desde cero para poder subir sin escalón, así que se perdían en el mismo fotograma en
que nacían. Se corrigió en las camas y en la música.

### La presencia del fondo

La figura que caminaba cruzando el vano ya no está. En su lugar hay algo que **no se mueve**: aparece ya
estando ahí, sin anatomía, contra la abertura del fondo, y lo único que hace es dejar de estar. Vuelve unos
segundos después corrida hacia el otro lado, lo justo para que no se pueda asegurar que se movió. Mientras
está, la escena pierde un ocho por ciento de luz y suena algo lejos. **No hay ni va a haber sustos.**

### Menús

- **Los renglones se reordenaron** por frecuencia de uso, no por costumbre: cuadrilla, registro de
  intervenciones, condiciones de estancia y, apartado por un hueco, renunciar al nivel.
- **Registro de intervenciones** abre la lista de mods, integrada como un renglón más del formulario.
- **La partida de un jugador salió del tablón** y se abre con **Control + S**, sin repetición mientras se
  mantiene pulsada. Se aclara al pie de la hoja, en letra chica.
- **La línea de avisos ya no es decorado**: se puede pasar a mano con un clic, entra en el recorrido del
  tabulador y se subraya a lápiz bajo el cursor.
- **El apagón apaga también la hoja.** Antes el pasillo se quedaba a oscuras y el papel seguía legible,
  flotando en la nada. Ahora la tinta, el blanco del papel y hasta la letra chica se apagan con el
  fluorescente, y vuelven con él.
- **Fuera el «clac» de vanilla.** Minecraft reproduce su click de madera antes de cualquier acción propia:
  cada renglón sonaba dos veces. Ahora suena sólo el sello del mod.
- Microdetalles en todos los renglones: la sangría se desplaza unos píxeles bajo el cursor, la casilla se
  marca al pulsar, el renglón terminal se distingue del resto y el teclado suena igual que el ratón. El
  renglón inactivo también contesta: un relé que intenta cerrar y no engancha.

### Música

El menú tiene ranura de música con su propio volumen, que arranca sola, no se reinicia al cambiar de
pantalla y **sigue sonando durante el apagón**. El archivo empaquetado es una pieza original compuesta para
el mod, así que se puede repartir sin arrastrar derechos de nadie.

La pista que pediste es **REQUIEM, de Emmy Z: la banda sonora de un juego comercial**, y no se puede meter
dentro del JAR sin permiso escrito de la autora. No está integrada y no voy a decir que lo está.

Lo que sí quedó hecho, y en 0.6.0 quedó terminado de verdad, es que poner tu propia copia sea trivial:

> **Dejá tu archivo `.ogg` en `.minecraft/jobsmenu-musica/`. Eso es todo.**

No hay que renombrarlo, no hay que crear carpetas y **no hay que activar nada en Opciones**. El mod crea la
carpeta, detecta el archivo sea cual sea su nombre, lo instala en un paquete de recursos, lo activa solo y
recarga en caliente para que suene en el acto. Si dejás un archivo que no es OGG, te lo dice en el log en
vez de quedarse mudo. Está todo explicado en **`docs/musica.md`**.

## Compilar

Requiere JDK 17 instalado.

```powershell
.\gradlew build
```

El `.jar` queda en `build\libs\jobsmenu-0.8.3.jar` y se copia a la carpeta `mods` de la instancia.

> Si `gradle\wrapper\gradle-wrapper.jar` no existe todavía, el bloque de despliegue lo descarga solo.

> **Si el build falla por memoria** (`os::commit_memory ... failed (errno=1455)` o *the daemon has
> disappeared*), no es el mod: es que a Windows le falta memoria comprometible. El `gradle.properties` ya va
> contenido a propósito (heap chico, GC serial, sin paralelismo) y `build.gradle` limita el proceso de
> reobfuscación, así que suele alcanzar. Si aun así falla, agrandá el **archivo de paginación** de Windows
> (Ver configuración avanzada del sistema → Rendimiento → Opciones avanzadas → Memoria virtual → Cambiar →
> tamaño administrado por el sistema) y reiniciá. El build tiene que terminar en **`BUILD SUCCESSFUL`**: si
> dice `BUILD FAILED`, el `.jar` que quede está a medio hacer y no sirve.

## Compilar y desplegar (bloque completo)

Actualiza, compila y copia el `.jar` a la carpeta `mods` de la instancia, sacando
de paso cualquier versión vieja del mod para que no queden dos `.jar` a la vez.
Ajustá `$repo` e `$instancia` a tu equipo — el ejemplo usa una instancia de
SKLauncher, pero sirve igual para `.minecraft` o cualquier lanzador.

```powershell
# --- 0. Rutas --------------------------------------------------------------
$repo      = "C:\Users\santi\Desktop\Jobs---Menu"
$instancia = "C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1"
$version   = "0.8.3"

Set-Location $repo

# --- 1. Actualizar ---------------------------------------------------------
git fetch origin
git checkout arena/01a03f8f-jobs-menu
git pull origin arena/01a03f8f-jobs-menu

# --- 2. Verificacion estatica (opcional; solo si tenes Python en el PATH) --
$py = Get-Command py -ErrorAction SilentlyContinue
if ($py) {
    py tools\verificar.py
    if ($LASTEXITCODE -ne 0) { Write-Error "verificar.py fallo."; return }
} else {
    Write-Host "Python no encontrado: salto la verificacion estatica." -ForegroundColor Yellow
}

# --- 3. Compilar -----------------------------------------------------------
.\gradlew build
if ($LASTEXITCODE -ne 0) { Write-Error "Build fallido."; return }

# --- 4. Desplegar ----------------------------------------------------------
$jar = Join-Path $repo "build\libs\jobsmenu-$version.jar"
if (-not (Test-Path $jar)) { Write-Error "No aparece $jar"; return }

$mods = Join-Path $instancia "mods"
New-Item -ItemType Directory -Force -Path $mods | Out-Null
Get-ChildItem $mods -Filter "jobsmenu-*.jar" | Remove-Item -Force -ErrorAction SilentlyContinue
Copy-Item $jar $mods -Force
Write-Host "Desplegado jobsmenu-$version.jar en $mods" -ForegroundColor Green
```

> El borrado de `.jar` viejos ocurre **después** de comprobar que el nuevo existe
> (`Test-Path`), así que nunca te quedás sin ninguno si el build no llegó a
> generar el artefacto. La instancia tiene que ser **Forge 47.x / Minecraft
> 1.20.1**; si apunta a otra versión o a Fabric, el `.jar` no carga.

## Herramientas sin JDK

```powershell
python tools\verificar.py       # versiones, idiomas, JSON, ASCII, llaves, símbolos, audio y niveles
python tools\vista_previa.py    # dibuja el menú a PNG para revisar la escena
python tools\vista_previa.py --contacto docs\vista_previa.png   # los cinco niveles juntos
python tools\vista_previa.py --presencia docs\presencia.png     # la manifestación del fondo, paso a paso
python tools\sonidos.py         # regenera las 38 piezas de audio (requiere numpy, scipy y soundfile)
```

## Documentación

Todo el diseño —canon del servidor, identidad, paleta, voz, alcance por fases y reglas de trabajo— está en
[`CONTEXTO.md`](CONTEXTO.md).
