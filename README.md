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
| Versión | **0.4.0** |
| Minecraft | 1.20.1 |
| Forge | 47.x |
| Java | 17 |
| Lado | Cliente (el servidor no necesita el mod) |

## Qué trae la 0.4.0

Cuatro correcciones sobre la entrega anterior, todas pedidas después de probarla en el juego: los fondos
dejaron de ser el mismo recinto pintado de otro color, la interfaz cambió de sonido por tercera vez, el
fondo dejó de callarse entre evento y evento, y el ambiente ya no se apaga a sí mismo por un detalle del
motor de sonido.

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

### La interfaz, por tercera vez

Las dos familias anteriores eran clics: primero clics comunes, después clics buenos —sellos, interruptores,
ruedas dentadas—. Suenan bien sueltos y ninguna de las dos funcionaba, porque el problema no era la calidad
de cada pieza sino la categoría. **Un clic es un objeto que se manipula, y acá no hay ningún objeto**: hay una
hoja clavada en una pared y un edificio alrededor.

En esta generación la interfaz **no tiene sonido propio**. Lo que se oye al mover el cursor es el edificio
enterándose: la instalación eléctrica, el aire, el papel. Los ocho gestos salen del mismo material que los
ambientes y siguen cuatro reglas duras —ningún ataque por debajo de 8 ms, nada por encima de 5 kHz, cuerpo
grave siempre presente, y todos por la misma sala—. Pasar el cursor es un soplo de aire; elegir es un escalón
de corriente oído desde el otro lado de la pared; confirmar es la red que se tensa y cae, y la confirmación
es el vacío que deja; acción inválida es el circuito intentando cerrar dos veces sin engancharse, sin un solo
pitido de error.

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
pantalla y **sigue sonando durante el apagón**. Sobre el tema que pediste, leé la nota de la entrega: el
archivo que viene empaquetado es una pieza original, y la ranura queda lista para el que autorices.

## Compilar

Requiere JDK 17 instalado.

```powershell
.\gradlew build
```

El `.jar` queda en `build\libs\jobsmenu-0.4.0.jar` y se copia a la carpeta `mods` de la instancia.

> Si `gradle\wrapper\gradle-wrapper.jar` no existe todavía, el bloque de despliegue lo descarga solo.

## Herramientas sin JDK

```powershell
python tools\verificar.py       # versiones, idiomas, JSON, ASCII, llaves, símbolos, audio y niveles
python tools\vista_previa.py    # dibuja el menú a PNG para revisar la escena
python tools\vista_previa.py --contacto docs\vista_previa.png   # los cuatro niveles juntos
python tools\vista_previa.py --presencia docs\presencia.png     # la manifestación del fondo, paso a paso
python tools\sonidos.py         # regenera las 34 piezas de audio (requiere numpy, scipy y soundfile)
```

## Documentación

Todo el diseño —canon del servidor, identidad, paleta, voz, alcance por fases y reglas de trabajo— está en
[`CONTEXTO.md`](CONTEXTO.md).
