#!/usr/bin/env python3
"""Vista previa de la escena del menu, sin Minecraft.

Reimplementa en Python las mismas operaciones de dibujo que usa
`EscenaNivel.java` (fill y fillGradient sobre un lienzo ARGB) para poder mirar
la composicion y la perspectiva antes de compilar. No es un emulador: es un
espejo. Si se cambia la escena en Java, se cambia aqui tambien.

Geometria comun a las cuatro plantas (identica en ambos lados):

    La abertura del fondo es un rectangulo centrado en el punto de fuga, de
    semiancho `w` y semialto `h`. Las cuatro aristas del recinto son rectas
    que pasan por la fuga, asi que en pantalla todo se reduce a una variable:

        dx = |x - fugaX| / w        (columna)
        dy = |y - fugaY| / h        (fila)

    En la abertura del fondo dx = dy = 1. La profundidad de lo que se ve en
    esa columna o fila es z = Z_FONDO / dx, es decir, `lejos = 1/dx` va de 0
    (pegado a la camara) a 1 (la abertura del fondo). Las juntas de pared, las
    del suelo y las del techo se calculan con la MISMA serie de profundidades,
    por eso las tres coinciden y el recinto se lee como un recinto.

    Sobre esa base cada nivel monta su PLANTA: sala, nave, servicio o
    natatorio. Cada una decide su geometria, su cantidad de tramos y que hay
    construido; ninguna comparte silueta con las otras.

Uso:  python3 tools/vista_previa.py [ancho] [alto] [salida.png] [--nivel N]
                                    [--figura=0.0..1.0]
      python3 tools/vista_previa.py --contacto docs/contacto.png [--desnudo]
      python3 tools/vista_previa.py --presencia docs/presencia.png [--nivel N]
      python3 tools/vista_previa.py --eventos docs/eventos.png   # cada recinto
                                    en el pico de su evento ambiental
"""

from __future__ import annotations

import math
import struct
import sys
import time
import zlib
from pathlib import Path

# --------------------------------------------------------------------------
# Paleta de interfaz: espejo de Paleta.java
# --------------------------------------------------------------------------
PAPEL = 0xFFF0E9CE
TINTA = 0xFF14120C
TINTA_TENUE = 0xFF4A422A
VANO = 0xFF0D0B07
ALERTA = 0xFF8E1B12
ALERTA_BRILLO = 0xFFC42B18

SEMILLA = 0x4A4F4253
MASCARA = 0xFFFFFFFFFFFFFFFF

# Geometria comun a todos los niveles: espejo de EscenaNivel.java
FUGA_X = 0.545
FUGA_Y = 0.520
# Menos polvo generico desde la pasada de direccion de arte: pesa mas la
# identidad de cada recinto (espejo de EscenaNivel 0.10.0).
MOTAS = 52

# Color de la luminaria generica. Espejo de Paleta.FLUOR.
FLUOR = 0xFFFFF7D2


def con_alfa(color: int, alfa: float) -> int:
    a = int(max(0.0, min(1.0, alfa)) * 255.0)
    return (a << 24) | (color & 0x00FFFFFF)


def mezclar(desde: int, hasta: int, t: float) -> int:
    f = max(0.0, min(1.0, t))
    a = int(((desde >> 24) & 0xFF) + (((hasta >> 24) & 0xFF) - ((desde >> 24) & 0xFF)) * f)
    r = int(((desde >> 16) & 0xFF) + (((hasta >> 16) & 0xFF) - ((desde >> 16) & 0xFF)) * f)
    g = int(((desde >> 8) & 0xFF) + (((hasta >> 8) & 0xFF) - ((desde >> 8) & 0xFF)) * f)
    b = int((desde & 0xFF) + ((hasta & 0xFF) - (desde & 0xFF)) * f)
    return (a << 24) | (r << 16) | (g << 8) | b


def iluminar(color: int, factor: float) -> int:
    if factor >= 1.0:
        return color
    return mezclar(VANO, color, max(0.0, factor))


def pseudo(indice: int) -> float:
    h = (SEMILLA + indice * 2654435761) & MASCARA
    h ^= h >> 13
    h = (h * 1274126177) & MASCARA
    h ^= h >> 16
    return (h % 10000) / 10000.0


def java_hash(texto: str) -> int:
    # Espejo de String.hashCode() de Java (int de 32 bits con signo), para
    # que las semillas derivadas de la clave del nivel coincidan entre el
    # juego y la vista previa.
    h = 0
    for c in texto:
        h = (31 * h + ord(c)) & 0xFFFFFFFF
    return h - 0x100000000 if h >= 0x80000000 else h


def limitar(v: float, minimo: float, maximo: float) -> float:
    return max(minimo, min(maximo, v))


# --------------------------------------------------------------------------
# Niveles: espejo de Nivel.java
# --------------------------------------------------------------------------
class Nivel:
    """Un nivel del servidor: un tipo de recinto mas una piel de colores."""

    def __init__(self, clave, planta, pared_alta, pared_baja, junta, suelo,
                 suelo_lejos, suelo_junta, techo, techo_junta, niebla, luz,
                 fondo, fuga_x, fuga_y, semi_izq, semi_der, semi_alto,
                 semi_bajo, reflejo, humedad):
        self.clave = clave
        self.planta = planta              # que clase de recinto es
        self.pared_alta = pared_alta      # pared junto al cielorraso
        self.pared_baja = pared_baja      # pared junto al zocalo
        self.junta = junta                # linea entre panel y panel
        self.suelo = suelo                # suelo a los pies
        self.suelo_lejos = suelo_lejos    # suelo contra el fondo
        self.suelo_junta = suelo_junta    # transversales del suelo
        self.techo = techo                # placa del cielorraso
        self.techo_junta = techo_junta    # perfileria del cielorraso
        self.niebla = niebla              # a donde se va todo con la distancia
        self.luz = luz                    # color de la luminaria
        self.fondo = fondo                # lo que hay tras la abertura
        # La camara del recinto. Cuatro bordes independientes y fuga propia:
        # es lo que hace que un nivel no sea otro repintado.
        self.fuga_x = fuga_x              # fuga horizontal, fraccion de pantalla
        self.fuga_y = fuga_y              # fuga vertical, fraccion de pantalla
        self.semi_izq = semi_izq          # cuanto se abre hacia la izquierda
        self.semi_der = semi_der          # cuanto se abre hacia la derecha
        self.semi_alto = semi_alto        # cuanto se abre hacia arriba
        self.semi_bajo = semi_bajo        # cuanto se abre hacia abajo
        self.reflejo = reflejo            # cuanto devuelve el suelo (0..1)
        self.humedad = humedad            # manchas en la pared (0..1)


NIVELES = [
    # Nivel 0 - El papel mural. Sala ancha y baja, la postal del servidor.
    Nivel(clave="nivel0", planta="sala",
          pared_alta=0xFFE6D264, pared_baja=0xFF9A8630, junta=0xFF5E5222,
          suelo=0xFF8A7638, suelo_lejos=0xFF6E5C2A, suelo_junta=0xFF4C401E,
          techo=0xFFD5CB9B, techo_junta=0xFF8E8760,
          niebla=0xFFC9B455, luz=0xFFFFF7D2, fondo=0xFF0D0B07,
          # Sala de reuniones vista desde una esquina: la fuga esta corrida a
          # la derecha, asi que la pared izquierda domina el cuadro y la
          # derecha se va rapido. Es lo contrario de un pasillo centrado.
          fuga_x=0.680, fuga_y=0.470,
          semi_izq=0.330, semi_der=0.105, semi_alto=0.150, semi_bajo=0.135,
          reflejo=0.16, humedad=1.00),

    # Nivel 1 - El deposito. Nave con pilares, cerchas y campanas.
    Nivel(clave="nivel1", planta="nave",
          pared_alta=0xFFB6BAAE, pared_baja=0xFF74786C, junta=0xFF4A4E43,
          suelo=0xFF80847A, suelo_lejos=0xFF5A5E54, suelo_junta=0xFF3C4036,
          techo=0xFF9EA298, techo_junta=0xFF5C6055,
          niebla=0xFF6E7268, luz=0xFFE8F0FF, fondo=0xFF171B1D,
          # Nave enorme mirada desde el suelo: horizonte muy bajo, techo
          # lejisimos. Se abre casi igual a los dos lados porque es un volumen,
          # no un corredor, pero el alto triplica al bajo.
          fuga_x=0.505, fuga_y=0.720,
          semi_izq=0.235, semi_der=0.255, semi_alto=0.300, semi_bajo=0.098,
          reflejo=0.30, humedad=0.35),

    # Nivel 2 - Servicio. El unico que sigue siendo un pasillo, y dobla.
    Nivel(clave="nivel2", planta="servicio",
          pared_alta=0xFF6E4A28, pared_baja=0xFF3E2A17, junta=0xFF241609,
          suelo=0xFF413025, suelo_lejos=0xFF2A1F16, suelo_junta=0xFF1B120C,
          techo=0xFF4A3520, techo_junta=0xFF2A1C0E,
          niebla=0xFF54371C, luz=0xFFFFB65E, fondo=0xFF0B0703,
          # El unico que SI es un pasillo, y se permite serlo: estrecho, alto y
          # con la fuga descentrada a la izquierda porque el haz de canerias
          # dobla hacia alla. Que uno de los cuatro sea un corredor esta bien;
          # el error era que lo fueran los cuatro.
          fuga_x=0.395, fuga_y=0.505,
          semi_izq=0.062, semi_der=0.078, semi_alto=0.108, semi_bajo=0.098,
          reflejo=0.22, humedad=0.75),

    # Nivel 3 - Las piscinas. Abajo no hay suelo: hay agua.
    Nivel(clave="nivel3", planta="natatorio",
          pared_alta=0xFFE4EFEC, pared_baja=0xFFA9C6C2, junta=0xFF7EA5A2,
          suelo=0xFF63B6B4, suelo_lejos=0xFF2F7E82, suelo_junta=0xFF3E9A9A,
          techo=0xFFE8F2F0, techo_junta=0xFFB2CCC9,
          niebla=0xFFBEDCD9, luz=0xFFF4FFFD, fondo=0xFF08171A,
          # Natatorio: recinto ancho y de techo bajo, mirado desde el borde del
          # agua. Se abre mucho a lo ancho y poco a lo alto, la fuga cae bajo el
          # centro y el vaso ocupa casi todo el cuadro inferior.
          fuga_x=0.455, fuga_y=0.330,
          semi_izq=0.300, semi_der=0.270, semi_alto=0.080, semi_bajo=0.124,
          reflejo=0.62, humedad=0.30),

    # Nivel 4 - La sala de piedra. El guino al lobby: piedra calida, fuego,
    # boveda, candil de rueda. La otra cara del mod frente a los backrooms.
    Nivel(clave="nivel4", planta="cripta",
          pared_alta=0xFF9A7444, pared_baja=0xFF5E4227, junta=0xFF34220F,
          suelo=0xFF6E5432, suelo_lejos=0xFF463320, suelo_junta=0xFF2C1C0C,
          techo=0xFF836540, techo_junta=0xFF4E3822,
          niebla=0xFF4A3520, luz=0xFFFFC070, fondo=0xFF0A0603,
          fuga_x=0.505, fuga_y=0.500,
          semi_izq=0.150, semi_der=0.150, semi_alto=0.185, semi_bajo=0.150,
          reflejo=0.20, humedad=0.55),

    # Nivel 5 - La biblioteca. Estanterias de madera oscura, lamparas verdes.
    Nivel(clave="nivel5", planta="biblioteca",
          pared_alta=0xFF7C6142, pared_baja=0xFF4E3B26, junta=0xFF2C2013,
          suelo=0xFF5A4A34, suelo_lejos=0xFF3C3020, suelo_junta=0xFF241B10,
          techo=0xFF6E5C42, techo_junta=0xFF3E3020,
          niebla=0xFF433624, luz=0xFFE9D8A0, fondo=0xFF120E08,
          fuga_x=0.500, fuga_y=0.500,
          semi_izq=0.140, semi_der=0.140, semi_alto=0.150, semi_bajo=0.140,
          reflejo=0.14, humedad=0.45),

    # Nivel 6 - El invernadero. Vidrio y hierro tomados por las plantas.
    Nivel(clave="nivel6", planta="invernadero",
          pared_alta=0xFF8A9A6E, pared_baja=0xFF566040, junta=0xFF3B3B22,
          suelo=0xFF4C5436, suelo_lejos=0xFF343A24, suelo_junta=0xFF20240E,
          techo=0xFFC8D4B0, techo_junta=0xFF6E7A50,
          niebla=0xFF7E8C64, luz=0xFFF2F6E0, fondo=0xFF141810,
          fuga_x=0.500, fuga_y=0.500,
          semi_izq=0.165, semi_der=0.165, semi_alto=0.175, semi_bajo=0.130,
          reflejo=0.18, humedad=0.60),

    # Nivel 7 - Las catacumbas. Tunel de piedra fria con nichos y un farol.
    Nivel(clave="nivel7", planta="catacumba",
          pared_alta=0xFF6A7078, pared_baja=0xFF3C4248, junta=0xFF23282C,
          suelo=0xFF43484C, suelo_lejos=0xFF2A2E32, suelo_junta=0xFF181B1E,
          techo=0xFF565C62, techo_junta=0xFF303539,
          niebla=0xFF32383E, luz=0xFFFFDC96, fondo=0xFF06080A,
          fuga_x=0.470, fuga_y=0.470,
          semi_izq=0.070, semi_der=0.082, semi_alto=0.130, semi_bajo=0.112,
          reflejo=0.24, humedad=0.85),

    # Nivel 8 - La cisterna. Columnas sobre agua negra que lo refleja todo.
    Nivel(clave="nivel8", planta="cisterna",
          pared_alta=0xFF4A5A6E, pared_baja=0xFF2A3644, junta=0xFF17202A,
          suelo=0xFF1E2A38, suelo_lejos=0xFF121A24, suelo_junta=0xFF0A0F16,
          techo=0xFF3A4A5C, techo_junta=0xFF22303E,
          niebla=0xFF1E2A38, luz=0xFFFFC878, fondo=0xFF05080C,
          fuga_x=0.500, fuga_y=0.500,
          semi_izq=0.190, semi_der=0.190, semi_alto=0.092, semi_bajo=0.118,
          reflejo=0.80, humedad=0.55),

    # Nivel 9 - El salon del trono. Ruinas, columnas partidas, un trono vacio.
    Nivel(clave="nivel9", planta="trono",
          pared_alta=0xFF6C6A82, pared_baja=0xFF3E3C50, junta=0xFF242234,
          suelo=0xFF46445A, suelo_lejos=0xFF2C2A3C, suelo_junta=0xFF181628,
          techo=0xFF56546A, techo_junta=0xFF302E44,
          niebla=0xFF34324A, luz=0xFFE8C878, fondo=0xFF0A0812,
          # La camara mira apenas desde el lado izquierdo: el eje del trono
          # sigue la fuga, pero la composicion deja de ser perfectamente espejo.
          fuga_x=0.470, fuga_y=0.530,
          semi_izq=0.160, semi_der=0.140, semi_alto=0.185, semi_bajo=0.140,
          reflejo=0.26, humedad=0.55),
]


# --------------------------------------------------------------------------
# Lienzo: espejo de GuiGraphics
# --------------------------------------------------------------------------
class Lienzo:
    def __init__(self, ancho: int, alto: int) -> None:
        self.ancho = ancho
        self.alto = alto
        self.pix = [[0, 0, 0] for _ in range(ancho * alto)]

    def _componer(self, indice: int, color: int) -> None:
        a = ((color >> 24) & 0xFF) / 255.0
        if a <= 0.0:
            return
        r = (color >> 16) & 0xFF
        g = (color >> 8) & 0xFF
        b = color & 0xFF
        destino = self.pix[indice]
        destino[0] = int(destino[0] + (r - destino[0]) * a)
        destino[1] = int(destino[1] + (g - destino[1]) * a)
        destino[2] = int(destino[2] + (b - destino[2]) * a)

    def fill(self, x0: int, y0: int, x1: int, y1: int, color: int) -> None:
        if x1 < x0:
            x0, x1 = x1, x0
        if y1 < y0:
            y0, y1 = y1, y0
        x0 = max(0, x0)
        y0 = max(0, y0)
        x1 = min(self.ancho, x1)
        y1 = min(self.alto, y1)
        for y in range(y0, y1):
            base = y * self.ancho
            for x in range(x0, x1):
                self._componer(base + x, color)

    def fill_gradient(self, x0: int, y0: int, x1: int, y1: int, arriba: int, abajo: int) -> None:
        if y1 <= y0:
            return
        alto = y1 - y0
        for y in range(max(0, y0), min(self.alto, y1)):
            t = (y - y0) / max(1, alto - 1) if alto > 1 else 0.0
            self.fill(x0, y, x1, y + 1, mezclar(arriba, abajo, t))

    def png(self, ruta: Path) -> None:
        filas = bytearray()
        for y in range(self.alto):
            filas.append(0)
            base = y * self.ancho
            for x in range(self.ancho):
                p = self.pix[base + x]
                filas.append(p[0] & 0xFF)
                filas.append(p[1] & 0xFF)
                filas.append(p[2] & 0xFF)

        def trozo(tipo: bytes, datos: bytes) -> bytes:
            cuerpo = tipo + datos
            return struct.pack(">I", len(datos)) + cuerpo + struct.pack(">I", zlib.crc32(cuerpo) & 0xFFFFFFFF)

        cabecera = struct.pack(">IIBBBBB", self.ancho, self.alto, 8, 2, 0, 0, 0)
        datos = zlib.compress(bytes(filas), 9)
        ruta.write_bytes(b"\x89PNG\r\n\x1a\n" + trozo(b"IHDR", cabecera)
                         + trozo(b"IDAT", datos) + trozo(b"IEND", b""))


# --------------------------------------------------------------------------
# Trazo: espejo de planta/Trazo.java
# --------------------------------------------------------------------------
PASO = 2


def profundidad(j: int, tramos: int) -> float:
    """dx del tramo j de una serie de `tramos`. j=tramos cae en el fondo."""
    return tramos / float(max(1, j))


def velar(color: int, niebla: int, lejos: float, fuerza: float) -> int:
    return mezclar(color, niebla, lejos * lejos * fuerza)


def atenuar(luz: float, lejos: float) -> float:
    return luz * (0.52 + 0.48 * lejos)


class Marco:
    """Encuadre: donde converge la perspectiva y como se abre el recinto.

    Hasta la 0.4.0 esto tenia un solo semiancho `w` y un solo semialto `h`, y
    esa era la razon REAL de que los cuatro niveles se vieran iguales. Con un
    unico semiancho, la pared izquierda y la derecha estan obligadas a
    converger igual, asi que cualquier cosa que se dibujara -sala, nave,
    natatorio- salia siendo el mismo tunel simetrico con la fuga en el medio.
    Cambiar los pilares o el color no arregla eso: el problema era la camara,
    no la decoracion.

    Ahora los cuatro bordes son independientes:

        wi  semiancho hacia la izquierda      ha  semialto hacia arriba
        wd  semiancho hacia la derecha        hb  semialto hacia abajo

    Con eso un recinto puede estar visto desde un rincon (wi != wd), tener el
    horizonte alto o bajo (ha != hb), y dejar de leerse como un pasillo.
    """

    def __init__(self, ancho, alto, fx, fy, wi, wd, ha, hb):
        self.ancho = ancho
        self.alto = alto
        self.fx = fx
        self.fy = fy
        self.wi = wi
        self.wd = wd
        self.ha = ha
        self.hb = hb
        # Medias, solo para escalar cosas que no son geometria del recinto
        # (grosores, tamanos de detalle). Nunca para ubicar una pared.
        self.w = (wi + wd) * 0.5
        self.h = (ha + hb) * 0.5

    def lado(self, signo, dx, frac=1.0):
        """Un punto sobre la pared izquierda (signo<0) o la derecha (signo>0).

        Sirve para lo que va apoyado o repetido contra los laterales -pilares,
        estanterias, canerias-: cada lado usa su propio semiancho, asi que en
        un recinto visto de esquina la hilera de la izquierda queda mas abierta
        que la de la derecha, como corresponde.
        """
        return self.der(dx * frac) if signo > 0 else self.izq(dx * frac)

    def centro(self, dx):
        """Eje visual del recinto a esa profundidad (no es la fuga)."""
        return (self.izq(dx) + self.der(dx)) * 0.5

    def ancho_en(self, dx):
        """Ancho completo del recinto a esa profundidad."""
        return self.der(dx) - self.izq(dx)

    def en_x(self, dx, frac):
        """Punto transversal del recinto, con frac de -1 (izq) a +1 (der).

        Es lo que hay que usar para todo lo que se reparte a lo ancho -placas
        del cielorraso, corridas de losa, calles de la pileta-. Interpolar
        entre las dos paredes reales, y no escalar un semiancho unico, es lo
        que hace que esas series sigan la forma del recinto cuando la camara
        esta descentrada.
        """
        return self.izq(dx) + (self.der(dx) - self.izq(dx)) * (frac + 1.0) * 0.5

    def izq(self, dx):
        """Borde izquierdo del recinto a la profundidad dx."""
        return self.fx - self.wi * dx

    def der(self, dx):
        """Borde derecho del recinto a la profundidad dx."""
        return self.fx + self.wd * dx

    def techo_en(self, dx):
        return self.fy - self.ha * dx

    def suelo_en(self, dx):
        return self.fy + self.hb * dx

    def dx(self, x):
        """Profundidad de una columna. Usa el semiancho del lado que toca."""
        if x < self.fx:
            return (self.fx - x) / self.wi
        return (x - self.fx) / self.wd

    def dy(self, y):
        """Profundidad de una fila. Usa el semialto del lado que toca."""
        if y < self.fy:
            return (self.fy - y) / self.ha
        return (y - self.fy) / self.hb


def t_fondo(lz, m, nivel, luz, testero=None, fuerza=1.0) -> None:
    """El vacio exterior y el muro del fondo.

    El muro del fondo no es un agujero: es una pared que esta lejos. Cada
    planta le pasa su propio color, y encima le apoya lo que tenga -puertas,
    porton, tablero-. Solo si no pasa ninguno se usa el vano del nivel.
    `fuerza` es cuanta luz le llega: 1.0 en un pasillo donde la luminaria mas
    lejana todavia lo alcanza, 2.5 en una sala clara donde el fondo es pared.
    """
    lz.fill(0, 0, lz.ancho, lz.alto, iluminar(nivel.niebla, luz * 0.45))
    color = nivel.fondo if testero is None else testero
    lz.fill_gradient(round(m.izq(1.0)), round(m.techo_en(1.0)),
                     round(m.der(1.0)), round(m.suelo_en(1.0)),
                     iluminar(mezclar(color, nivel.niebla, 0.22),
                              limitar(luz * 0.52 * fuerza, 0.0, 1.0)),
                     iluminar(color, limitar(luz * 0.30 * fuerza, 0.0, 1.0)))


def t_plano(lz, m, arriba, cerca, lejos_c, niebla, luz, velo) -> None:
    """El suelo o el cielo, fila por fila."""
    desde = 0 if arriba else round(m.suelo_en(1.0))
    hasta = round(m.techo_en(1.0)) if arriba else m.alto
    for y in range(desde, hasta, PASO):
        dy = m.dy(y + PASO * 0.5)
        if dy <= 1.0:
            continue
        lej = limitar(1.0 / dy, 0.0, 1.0)
        color = velar(mezclar(cerca, lejos_c, lej), niebla, lej, velo)
        lz.fill(0, y, m.ancho, y + PASO, iluminar(color, atenuar(luz, lej)))


def t_transversales(lz, m, arriba, color, niebla, luz, tramos, alfa) -> None:
    """Las lineas que cruzan el suelo o el cielo y se aprietan hacia la fuga."""
    for j in range(1, tramos + 1):
        dy = profundidad(j, tramos)
        lej = limitar(1.0 / dy, 0.0, 1.0)
        y = m.techo_en(1.0 * dy) if arriba else m.suelo_en(1.0 * dy)
        if y < -4 or y > m.alto + 4:
            continue
        grosor = max(1, min(int(m.h * 0.09), int(m.h * dy * 0.010)))
        x0 = round(m.izq(1.0 * dy))
        x1 = round(m.der(1.0 * dy))
        lz.fill(max(0, x0), int(y), min(m.ancho, x1), int(y) + grosor,
                con_alfa(iluminar(velar(color, niebla, lej, 0.55),
                                  atenuar(luz, lej)), alfa * lej + 0.10))


def t_paredes(lz, m, nivel, luz) -> None:
    """Los dos costados del recinto, columna por columna."""
    for x in range(0, m.ancho, PASO):
        dx = m.dx(x + PASO * 0.5)
        if dx <= 1.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        y0 = m.techo_en(dx)
        y1 = m.suelo_en(dx)
        if y1 < 0 or y0 > m.alto:
            continue
        at = atenuar(luz, lej)
        lz.fill_gradient(x, int(y0), x + PASO, int(y1),
                         iluminar(velar(nivel.pared_alta, nivel.niebla, lej, 0.62), at),
                         iluminar(velar(nivel.pared_baja, nivel.niebla, lej, 0.52), at))


def t_juntas(lz, m, nivel, luz, tramos, lateral, alfa) -> None:
    for j in range(1, tramos + 1):
        dx = profundidad(j, tramos)
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        grosor = max(1, min(int(m.w * 0.10), int(m.w * dx * 0.009)))
        color = con_alfa(iluminar(velar(nivel.junta, nivel.niebla, lej, 0.55), at),
                         alfa * lej + 0.12)
        y0 = int(m.techo_en(dx))
        y1 = int(m.suelo_en(dx))
        for signo in (-1, 1):
            x = m.lado(signo, dx, lateral)
            if -grosor <= x <= m.ancho + grosor:
                lz.fill(int(x), y0, int(x) + grosor, y1, color)


def t_manchas(lz, m, nivel, luz, tramos) -> None:
    """Filtraciones que cuelgan de lo alto y se abren hacia abajo."""
    total = int(16 * nivel.humedad)
    for i in range(total):
        dx = 1.15 + pseudo(i * 3) * (tramos * 0.42)
        signo = -1 if pseudo(i * 3 + 1) < 0.5 else 1
        x = m.lado(signo, dx)
        if x < -40 or x > m.ancho + 40:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        y0 = m.techo_en(dx)
        altura = m.h * dx * (0.25 + pseudo(i * 3 + 2) * 0.55)
        ancho = max(2.0, m.w * dx * (0.05 + pseudo(i * 5) * 0.10))
        pasos = 5
        for k in range(pasos):
            t = k / pasos
            a = 0.30 * (1.0 - t) * (0.35 + 0.65 * lej) * nivel.humedad
            am = ancho * (0.6 + 0.9 * t)
            lz.fill(int(x - am), int(y0 + altura * t),
                    int(x + am), int(y0 + altura * (t + 1.0 / pasos)),
                    con_alfa(iluminar(nivel.junta, luz), a))


def t_luminaria(lz, m, nivel, dx, altura, largo, derrame, luz) -> None:
    """Luminaria empotrada, con artefacto. Espejo de Trazo.luminaria."""
    lej = limitar(1.0 / dx, 0.0, 1.0)
    y = m.techo_en(1.0 * dx * altura)
    cx = m.centro(dx)
    medio = max(1.0, abs(m.ancho_en(dx)) * 0.5 * largo)
    grueso = max(1.0, m.h * dx * 0.026)
    desvio = pseudo(int(dx * 977.0) + 31)
    cansancio = 0.80 + 0.32 * desvio
    fuerza = luz * (0.45 + 0.55 * lej) * cansancio

    if derrame > 0.0:
        for k in (4, 3, 2, 1):
            t = k / 4.0
            ex = medio * (1.0 + t * 2.1)
            ey = grueso * (1.0 + t * 5.5)
            lz.fill(int(cx - ex), int(y - ey), int(cx + ex), int(y + ey),
                    con_alfa(nivel.luz, 0.050 * derrame * fuerza * (1.0 - t * 0.55)))

    cajon_x = medio * 1.14
    cajon_y = grueso * 1.9
    if cajon_y >= 1.0:
        lz.fill(int(cx - cajon_x), int(y - cajon_y), int(cx + cajon_x), int(y + cajon_y),
                con_alfa(iluminar(nivel.techo_junta, 0.62 + 0.34 * fuerza), 0.90))
        lz.fill(int(cx - cajon_x), int(y + cajon_y - max(1.0, grueso * 0.35)),
                int(cx + cajon_x), int(y + cajon_y),
                con_alfa(iluminar(nivel.luz, fuerza * 0.75), 0.40))

    lz.fill(int(cx - medio * 1.04), int(y - grueso * 1.35),
            int(cx + medio * 1.04), int(y + grueso * 1.35),
            con_alfa(iluminar(nivel.luz, min(1.0, fuerza * 0.85)), 0.55))

    lz.fill(int(cx - medio), int(y - grueso), int(cx + medio), int(y + grueso),
            con_alfa(iluminar(nivel.luz, min(1.0, fuerza * 1.25)), 0.92))

    tapa = max(1.0, medio * 0.13)
    oscuro = con_alfa(nivel.techo_junta, 0.45)
    lz.fill(int(cx - medio), int(y - grueso), int(cx - medio + tapa), int(y + grueso), oscuro)
    lz.fill(int(cx + medio - tapa), int(y - grueso), int(cx + medio), int(y + grueso), oscuro)


def t_interior_vano(lz, nivel, x0, y0, x1, y1, lado, luz) -> None:
    """El primer metro de lo que sigue del otro lado. Espejo de Trazo."""
    ancho = x1 - x0
    alto = y1 - y0
    if ancho <= 2 or alto <= 2:
        return
    lz.fill(x0, y0, x1, y1, con_alfa(mezclar(VANO, nivel.niebla, 0.10), 0.97))
    fuga = max(1, round(ancho * 0.34))
    desde = x1 - fuga if lado < 0 else x0
    hasta = x1 if lado < 0 else x0 + fuga
    for k in range(fuga):
        t = 1.0 - k / fuga if lado < 0 else k / fuga
        px = desde + k if lado < 0 else hasta - 1 - k
        lz.fill(px, y0 + round(alto * 0.06 * (1.0 - t)), px + 1, y1,
                con_alfa(iluminar(nivel.pared_baja, luz * 0.30 * t * t), 0.85))
    umbral = max(1, round(alto * 0.10))
    lz.fill_gradient(x0 + 1, y1 - umbral, x1 - 1, y1,
                     con_alfa(VANO, 0.0),
                     con_alfa(iluminar(nivel.suelo_lejos, luz * 0.55), 0.62))
    filo = x0 if lado < 0 else x1 - 1
    lz.fill(filo, y0, filo + 1, y1,
            con_alfa(iluminar(nivel.pared_alta, luz * 0.60), 0.45))


def brillo_fluorescente(tiempo: float, destellos: bool = True) -> float:
    if not destellos:
        return 0.90
    v = (0.90 + 0.030 * math.sin(tiempo * 1.7)
         + 0.015 * math.sin(tiempo * 5.9 + 1.3)
         + 0.008 * math.sin(tiempo * 12.7 + 0.4))
    if int(tiempo * 3.0) % 97 == 0:
        v *= 0.68
    return limitar(v, 0.48, 1.0)


def arranque_tubo(avance: float) -> float:
    """Encendido de un fluorescente frio: dos chispazos y despues se queda."""
    if avance <= 0.0:
        return 0.0
    if avance >= 1.0:
        return 1.0
    if avance < 0.12:
        return 0.55
    if avance < 0.20:
        return 0.05
    if avance < 0.30:
        return 0.80
    if avance < 0.36:
        return 0.10
    if avance < 0.46:
        return 0.35
    return limitar(0.35 + (avance - 0.46) / 0.54 * 0.65, 0.0, 1.0)


# --------------------------------------------------------------------------
# Escena: espejo de EscenaNivel.java
# --------------------------------------------------------------------------
def dibujar(lz: Lienzo, nivel: Nivel, tiempo: float = 3.0, penumbra: float = 0.0,
            luz_global: float = 1.0, presencia_v: float = 0.0,
            presencia_segunda: bool = False, presencia_modo: int = 0,
            polvo: bool = True, destellos: bool = True,
            primer_plano: bool = True, evento_forzado: float = None) -> None:
    fx = lz.ancho * nivel.fuga_x
    fy = lz.alto * nivel.fuga_y
    # Respiracion de camara (espejo de EscenaNivel): la fuga deriva unos pocos
    # pixeles en un vaiven lentisimo. Gated por 'polvo', el equivalente de
    # movimiento en la vista previa.
    if polvo:
        fx += math.sin(tiempo * 0.13) * lz.ancho * 0.0045
        fy += math.sin(tiempo * 0.087 + 1.3) * lz.alto * 0.0038
    m = Marco(lz.ancho, lz.alto, fx, fy,
              lz.ancho * nivel.semi_izq, lz.ancho * nivel.semi_der,
              lz.ancho * nivel.semi_alto, lz.ancho * nivel.semi_bajo)

    luz = brillo_fluorescente(tiempo, destellos) * (1.0 - 0.55 * penumbra) * luz_global
    # La presencia le saca hasta un ocho por ciento a la escena mientras esta,
    # igual que Presencia.sombra(). Nadie lo puede senalar; todo el mundo lo nota.
    luz *= 1.0 - 0.08 * limitar(presencia_v, 0.0, 1.0)
    luz = limitar(luz, 0.0, 1.0)

    PLANTAS[nivel.planta](lz, m, nivel, luz, tiempo)

    # Capas 0.10.0 (espejo de MaterialesEscena, TratamientoEscena, DireccionArte
    # y EventosAmbientales): se pegan a la arquitectura base antes del primer
    # plano, asi el microdetalle recibe la misma atmosfera que el resto.
    materiales_escena(lz, lz.ancho, lz.alto, nivel, luz, tiempo, polvo)
    tratamiento_escena(lz, lz.ancho, lz.alto, nivel, luz, tiempo, polvo)
    direccion_arte(lz, lz.ancho, lz.alto, nivel, luz, tiempo)
    if polvo:
        eventos_ambientales(lz, lz.ancho, lz.alto, nivel, luz,
                            forzado=evento_forzado)

    # El primer plano va despues del recinto y antes de la presencia: lo que
    # esta cerca tapa lo que esta lejos, y la figura vive dentro del recinto.
    if primer_plano:
        PRIMEROS_PLANOS[nivel.planta](lz, m, nivel, luz, tiempo)

    if presencia_v > 0.0:
        presencia(lz, nivel, m, presencia_v, luz,
                  presencia_segunda, tiempo, PISO_PRESENCIA[nivel.planta],
                  presencia_modo)
    if polvo:
        motas(lz, fx, fy, tiempo, luz, nivel)
    vineta(lz, nivel, penumbra, luz)


# --------------------------------------------------------------------------
# Microdetalle de material: espejo de MaterialesEscena.java
# --------------------------------------------------------------------------
def materiales_escena(lz, w, h, nivel, luz, tiempo, movimiento) -> None:
    if nivel.clave == "nivel0":
        papel_mural(lz, w, h, nivel, luz)
    elif nivel.clave in ("nivel1", "nivel2", "nivel8"):
        metal_hormigon(lz, w, h, nivel, luz, nivel.clave != "nivel1")
    elif nivel.clave == "nivel3":
        azulejo(lz, w, h, nivel, luz)
    elif nivel.clave in ("nivel4", "nivel7", "nivel9"):
        grietas = {"nivel4": 18, "nivel7": 26, "nivel9": 22}[nivel.clave]
        piedra(lz, w, h, nivel, luz, grietas)
    elif nivel.clave == "nivel5":
        madera(lz, w, h, nivel, luz)
    elif nivel.clave == "nivel6":
        vidrio_humedo(lz, w, h, nivel, luz, tiempo, movimiento)


def papel_mural(lz, w, h, nivel, luz) -> None:
    sem = numero_nivel(nivel)
    for i in range(28):
        px = pseudo(sem * 3 + i * 17)
        py = pseudo(sem * 3 + i * 31)
        x = int(px * w)
        y = int(py * h * 0.72)
        largo = 3 + int(pseudo(sem * 3 + i * 47) * 14)
        color = nivel.junta if i % 3 == 0 else nivel.pared_baja
        lz.fill(x, y, min(w, x + largo), y + 1, con_alfa(color, 0.028 * luz))


def metal_hormigon(lz, w, h, nivel, luz, industrial) -> None:
    panel = max(44, w // 9)
    x = panel
    while x < w:
        lz.fill(x, int(h * 0.16), x + 1, int(h * 0.78), con_alfa(nivel.junta, 0.10 * luz))
        if industrial:
            y = int(h * 0.24)
            while y < h * 0.73:
                remache(lz, x - 1, y, nivel.luz, luz)
                y += max(18, h // 8)
        x += panel
    sem = numero_nivel(nivel)
    for i in range(18):
        x = int(pseudo(sem * 5 + i * 13) * w)
        y = int((0.22 + pseudo(sem * 5 + i * 23) * 0.55) * h)
        largo = 5 + int(pseudo(sem * 5 + i * 41) * 18)
        lz.fill(x, y, min(w, x + largo), y + 1, con_alfa(VANO, 0.035))


def azulejo(lz, w, h, nivel, luz) -> None:
    paso_x = max(22, w // 25)
    paso_y = max(14, h // 18)
    y_fin = int(h * 0.64)
    y = int(h * 0.18)
    while y < y_fin:
        lz.fill(0, y, w, y + 1, con_alfa(nivel.junta, 0.055 * luz))
        desfase = 0 if ((y // paso_y) & 1) == 0 else paso_x // 2
        x = desfase
        while x < w:
            lz.fill(x, y, x + 1, min(y_fin, y + paso_y), con_alfa(nivel.junta, 0.040 * luz))
            x += paso_x
        y += paso_y


def piedra(lz, w, h, nivel, luz, grietas) -> None:
    junta_y = max(18, h // 12)
    y = int(h * 0.16)
    while y < h * 0.78:
        lz.fill(0, y, w, y + 1, con_alfa(nivel.junta, 0.060 * luz))
        y += junta_y
    sem = numero_nivel(nivel)
    for i in range(grietas):
        x = int(pseudo(sem * 7 + i * 11) * w)
        y = int((0.20 + pseudo(sem * 7 + i * 19) * 0.58) * h)
        dx = 4 + int(pseudo(sem * 7 + i * 29) * 14)
        dy = (1 if pseudo(sem * 7 + i * 37) > 0.5 else -1) * (2 + int(pseudo(sem * 7 + i * 43) * 8))
        grieta(lz, x, y, dx, dy, nivel.junta, luz)


def madera(lz, w, h, nivel, luz) -> None:
    sem = numero_nivel(nivel)
    for lado in range(2):
        x0 = 0 if lado == 0 else int(w * 0.69)
        x1 = int(w * 0.31) if lado == 0 else w
        for i in range(24):
            y = int((0.18 + pseudo(sem * 11 + lado * 101 + i * 7) * 0.60) * h)
            x = x0 + int(pseudo(sem * 11 + lado * 211 + i * 13) * max(1, x1 - x0))
            largo = 8 + int(pseudo(sem * 11 + i * 23) * 30)
            lz.fill(x, y, min(x1, x + largo), y + 1, con_alfa(nivel.pared_alta, 0.025 * luz))


def vidrio_humedo(lz, w, h, nivel, luz, tiempo, movimiento) -> None:
    sem = numero_nivel(nivel)
    for i in range(20):
        x = int(pseudo(sem * 13 + i * 17) * w)
        base = pseudo(sem * 13 + i * 31)
        deriva = ((tiempo * (0.6 + base)) % 18.0) if movimiento else 0.0
        y = int(h * (0.08 + base * 0.50)) + int(deriva)
        largo = 3 + int(base * 13)
        lz.fill(x, y, x + 1, min(h, y + largo), con_alfa(nivel.luz, 0.040 * luz * nivel.humedad))


def remache(lz, x, y, color_luz, luz) -> None:
    lz.fill(x - 1, y - 1, x + 2, y + 2, con_alfa(VANO, 0.42))
    lz.fill(x, y, x + 1, y + 1, con_alfa(color_luz, 0.30 * luz))


def grieta(lz, x, y, dx, dy, color, luz) -> None:
    pasos = max(3, abs(dx))
    for i in range(pasos):
        t = i / pasos
        px = x + int(dx * t)
        py = y + int(dy * t) + (1 if (i & 3) == 0 else 0)
        lz.fill(px, py, px + 1, py + 1, con_alfa(color, 0.16 * luz))


# --------------------------------------------------------------------------
# Tratamiento final: espejo de TratamientoEscena.java
# --------------------------------------------------------------------------
def tratamiento_escena(lz, ancho, alto, nivel, luz, tiempo, movimiento) -> None:
    profundidad_atmosfera(lz, ancho, alto, nivel, luz)
    rebote_suelo(lz, ancho, alto, nivel, luz, tiempo)
    humedad(lz, ancho, alto, nivel, luz, tiempo, movimiento)
    if movimiento:
        grano(lz, ancho, alto, nivel, luz, tiempo)


def profundidad_atmosfera(lz, ancho, alto, nivel, luz) -> None:
    centro_x = int(ancho * nivel.fuga_x)
    centro_y = int(alto * nivel.fuga_y)
    radio_x = max(28, ancho // 5)
    radio_y = max(18, alto // 5)
    for i in range(6, 0, -1):
        t = i / 6.0
        rx = int(radio_x * t)
        ry = int(radio_y * t)
        alfa = 0.010 * (7 - i) * luz
        lz.fill(centro_x - rx, centro_y - ry, centro_x + rx, centro_y + ry,
                con_alfa(nivel.niebla, alfa))
    # Halo de color lejano (antes en prof_arte): identidad cromatica del nivel
    # sin sumar otra capa de oscuridad (espejo de TratamientoEscena 1.x).
    halo_w = max(20, ancho // 5)
    halo_h = max(12, alto // 7)
    for i in range(4, -1, -1):
        a = (0.010 + i * 0.006) * luz
        x0 = centro_x - halo_w - i * 9
        x1 = centro_x + halo_w + i * 9
        y0 = centro_y - halo_h - i * 5
        y1 = centro_y + halo_h + i * 5
        lz.fill(max(0, x0), max(0, y0), min(ancho, x1), min(alto, y1),
                con_alfa(nivel.luz, a))


def rebote_suelo(lz, ancho, alto, nivel, luz, tiempo) -> None:
    fuerza = (0.018 + nivel.reflejo * 0.050) * luz
    respiracion = 0.92 + 0.08 * math.sin(tiempo * 0.37 + numero_nivel(nivel))
    fuerza *= respiracion
    inicio = int(alto * 0.62)
    bandas = 8
    for i in range(bandas):
        t = i / bandas
        y0 = inicio + (alto - inicio) * i // bandas
        y1 = inicio + (alto - inicio) * (i + 1) // bandas
        alfa = fuerza * (1.0 - t) * (1.0 - t)
        lz.fill(0, y0, ancho, y1, con_alfa(nivel.luz, alfa))


def humedad(lz, ancho, alto, nivel, luz, tiempo, movimiento) -> None:
    if nivel.humedad < 0.35:
        return
    sem = numero_nivel(nivel)
    lineas = 3 + round(nivel.humedad * 4.0)
    for i in range(lineas):
        base = pseudo(sem * 17 + i * 19)
        deriva = math.sin(tiempo * (0.08 + i * 0.011) + i) * 0.015 if movimiento else 0.0
        y = int(alto * (0.50 + base * 0.40 + deriva))
        x = int(ancho * pseudo(sem * 17 + i * 31))
        largo = max(16, int(ancho * (0.06 + 0.12 * base)))
        alfa = 0.018 * nivel.humedad * luz
        lz.fill(x, y, min(ancho, x + largo), y + 1, con_alfa(nivel.luz, alfa))


def grano(lz, ancho, alto, nivel, luz, tiempo) -> None:
    fase = int(tiempo * 4.0)
    sem = (numero_nivel(nivel) * 0x45D9F3B) ^ fase
    puntos = max(12, min(40, ancho * alto // 18000))
    for i in range(puntos):
        px = pseudo(sem + i * 17)
        py = pseudo(sem + i * 29)
        x = int(px * ancho)
        y = int(py * alto)
        alfa = (0.012 + pseudo(sem + i * 43) * 0.012) * luz
        color = nivel.luz if (i & 1) == 0 else PAPEL
        lz.fill(x, y, x + 1, y + 1, con_alfa(color, alfa))


# --------------------------------------------------------------------------
# Direccion de arte: espejo de DireccionArte.java
# --------------------------------------------------------------------------
def direccion_arte(lz, ancho, alto, nivel, luz, tiempo) -> None:
    # prof_arte se retiro en la evolucion 2: la niebla y la vineta ya viven en
    # tratamiento_escena / vineta, y el halo de color se movio a
    # profundidad_atmosfera. Una sola pasada atmosferica.
    n = numero_nivel(nivel)
    if n == 0:
        administracion(lz, ancho, alto, nivel, luz, tiempo)
    elif n == 1:
        deposito(lz, ancho, alto, nivel, luz, tiempo)
    elif n == 2:
        servicio_arte(lz, ancho, alto, nivel, luz, tiempo)
    elif n == 3:
        natatorio_arte(lz, ancho, alto, nivel, luz, tiempo)
    elif n == 4:
        sala_piedra(lz, ancho, alto, nivel, luz, tiempo)
    elif n == 5:
        biblioteca_arte(lz, ancho, alto, nivel, luz, tiempo)
    elif n == 6:
        invernadero_arte(lz, ancho, alto, nivel, luz, tiempo)
    elif n == 7:
        catacumbas_arte(lz, ancho, alto, nivel, luz, tiempo)
    elif n == 8:
        cisterna_arte(lz, ancho, alto, nivel, luz, tiempo)
    elif n == 9:
        trono_arte(lz, ancho, alto, nivel, luz, tiempo)


def administracion(lz, w, h, nivel, luz, t) -> None:
    y0 = int(h * 0.43)
    y1 = int(h * 0.73)
    torre_luz(lz, int(w * 0.19), y0, y1, nivel.luz, luz, t, 0.0)
    torre_luz(lz, int(w * 0.81), y0, y1, nivel.luz, luz, t, 1.3)
    runas(lz, int(w * 0.12), int(h * 0.48), nivel.luz, luz)
    runas(lz, int(w * 0.88), int(h * 0.48), nivel.luz, luz)


def deposito(lz, w, h, nivel, luz, t) -> None:
    for i in range(4):
        x = int(w * (0.16 + i * 0.23))
        lz.fill(x, int(h * 0.24), x + max(3, w // 180), int(h * 0.78),
                con_alfa(VANO, 0.28))
        cy = int(h * (0.48 + (i % 2) * 0.06))
        pulso(lz, x - 2, cy, nivel.luz, luz, t, i * 1.1)
    grua_deposito(lz, w, h, nivel, luz, t)


def servicio_arte(lz, w, h, nivel, luz, t) -> None:
    y = int(h * 0.19)
    for i in range(5):
        yy = y + i * max(3, h // 70)
        c = mezclar(nivel.pared_baja, nivel.luz, 0.14 + i * 0.035)
        lz.fill(0, yy, w, yy + max(1, h // 180), con_alfa(c, 0.25 * luz))
    calor = int((math.sin(t * 0.7) * 0.5 + 0.5) * 18)
    lz.fill(int(w * 0.70), int(h * 0.52), int(w * 0.94), int(h * 0.54),
            con_alfa(nivel.luz, (0.025 + calor / 900.0) * luz))
    panel_servicio(lz, w, h, nivel, luz, t)


def natatorio_arte(lz, w, h, nivel, luz, t) -> None:
    # Cuatro lineas apenas (espejo de DireccionArte: la planta ya dibuja su
    # propia red de luz; tres aguas superpuestas eran ruido).
    causticas(lz, w, h, nivel, luz, t, 0.64, 4)
    # Reflejo vertical de las luminarias: una columna por luz, deriva unica
    # (espejo de DireccionArte 1.x: antes cada tramo tenia fase propia y la
    # fila se leia como un relampago).
    for i in range(4):
        x_base = w * (0.28 + i * 0.15)
        deriva = math.sin(t * 0.8 + i * 1.7) * 1.5
        ancho_col = max(2, w // 90)
        for s in range(6):
            yy = int(h * 0.60) + s * max(3, h // 45)
            dx = int(deriva * (0.35 + s * 0.20))
            x = int(x_base) + dx
            alfa = 0.050 * luz * (1.0 - s * 0.14)
            lz.fill(x, yy, x + ancho_col, yy + 1, con_alfa(nivel.luz, alfa))


def sala_piedra(lz, w, h, nivel, luz, t) -> None:
    cadena(lz, int(w * 0.18), 0, int(h * 0.31), nivel.junta, luz, 8)
    cadena(lz, int(w * 0.82), 0, int(h * 0.25), nivel.junta, luz, 7)
    antorcha(lz, int(w * 0.14), int(h * 0.54), nivel.luz, luz, t, 0.0)
    antorcha(lz, int(w * 0.86), int(h * 0.50), nivel.luz, luz, t, 1.8)


def biblioteca_arte(lz, w, h, nivel, luz, t) -> None:
    verde = 0xFF8FAE68
    for lado in (-1, 1):
        x = int(w * 0.24) if lado < 0 else int(w * 0.76)
        pulso(lz, x, int(h * 0.44), verde, luz * 0.75, t, lado)
    for i in range(6):
        y = int(h * (0.28 + i * 0.065))
        lz.fill(int(w * 0.08), y, int(w * 0.28), y + 1, con_alfa(nivel.luz, 0.025 * luz))
        lz.fill(int(w * 0.72), y, int(w * 0.92), y + 1, con_alfa(nivel.luz, 0.025 * luz))
    escalera_biblioteca(lz, w, h, nivel, luz)


def invernadero_arte(lz, w, h, nivel, luz, t) -> None:
    for i in range(4):
        x = int(w * (0.30 + i * 0.13))
        dx = int(math.sin(t * 0.09 + i) * 5.0)
        lz.fill(x + dx, 0, x + dx + max(2, w // 130), int(h * 0.62),
                con_alfa(nivel.luz, 0.028 * luz))
    techo_invernadero(lz, w, h, nivel, luz, t)
    hojas(lz, 0, h, w, nivel.pared_baja, luz, t, False)
    hojas(lz, w, h, w, nivel.pared_baja, luz, t, True)


def catacumbas_arte(lz, w, h, nivel, luz, t) -> None:
    cadena(lz, int(w * 0.48), 0, int(h * 0.28), nivel.junta, luz, 9)
    antorcha(lz, int(w * 0.13), int(h * 0.47), nivel.luz, luz * 0.85, t, 0.7)
    # Los nichos los dibuja la escena de la planta (cat_nichos), excavados con
    # su alfeizar. Los de aca, con dobles bordes claros, se superponian a esos
    # y se leian como cuadros flotantes en la pared: retirados en 0.9.0.


def cisterna_arte(lz, w, h, nivel, luz, t) -> None:
    causticas(lz, w, h, nivel, luz, t, 0.58, 10)
    verde = 0xFF62FF65
    for i in range(5):
        x = int(w * (0.16 + i * 0.17))
        y = int(h * (0.60 + (i % 2) * 0.045))
        pulso(lz, x, y, verde, luz * 0.55, t, i * 0.8)
    ondas_cisterna(lz, w, h, nivel, luz, t)


def trono_arte(lz, w, h, nivel, luz, t) -> None:
    cadena(lz, int(w * 0.22), 0, int(h * 0.34), nivel.junta, luz, 10)
    cadena(lz, int(w * 0.78), 0, int(h * 0.29), nivel.junta, luz, 9)
    lz.fill(int(w * 0.47), 0, int(w * 0.53), int(h * 0.70), con_alfa(nivel.luz, 0.035 * luz))
    antorcha(lz, int(w * 0.18), int(h * 0.55), nivel.luz, luz, t, 0.2)
    antorcha(lz, int(w * 0.82), int(h * 0.55), nivel.luz, luz, t, 2.2)


def grua_deposito(lz, w, h, nivel, luz, t) -> None:
    y = int(h * 0.17)
    metal = con_alfa(mezclar(VANO, nivel.junta, 0.72), 0.72)
    lz.fill(int(w * 0.10), y, int(w * 0.90), y + max(2, h // 120), metal)
    lz.fill(int(w * 0.30), y, int(w * 0.32), int(h * 0.25), metal)
    lz.fill(int(w * 0.68), y, int(w * 0.70), int(h * 0.25), metal)
    x = int(w * (0.54 + math.sin(t * 0.18) * 0.035))
    cable = max(1, w // 260)
    cable_y = int(h * 0.40)
    lz.fill(x, y + max(2, h // 120), x + cable, cable_y,
            con_alfa(nivel.junta, 0.46 * luz))
    carga_ancho = max(12, w // 34)
    carga_alto = max(8, h // 24)
    carga_x0 = x - carga_ancho // 2
    lz.fill(carga_x0, cable_y, carga_x0 + carga_ancho, cable_y + carga_alto,
            con_alfa(mezclar(VANO, nivel.junta, 0.60), 0.82))
    lz.fill(carga_x0, cable_y, carga_x0 + carga_ancho, cable_y + max(2, h // 150),
            con_alfa(nivel.luz, 0.42 * luz))
    cantonera = max(2, w // 190)
    lz.fill(carga_x0, cable_y, carga_x0 + cantonera, cable_y + carga_alto,
            con_alfa(0xFFD18A42, 0.58 * luz))
    lz.fill(carga_x0 + carga_ancho - cantonera, cable_y,
            carga_x0 + carga_ancho, cable_y + carga_alto,
            con_alfa(0xFFD18A42, 0.42 * luz))
    soldadura = con_alfa(iluminar(nivel.luz, luz), 0.58)
    nodo = max(2, w // 260)
    lz.fill(int(w * 0.30) - nodo, y - 1, int(w * 0.30) + nodo, y + 2, soldadura)
    lz.fill(int(w * 0.69) - nodo, y - 1, int(w * 0.69) + nodo, y + 2, soldadura)


def panel_servicio(lz, w, h, nivel, luz, t) -> None:
    x0 = int(w * 0.78)
    x1 = int(w * 0.91)
    y0 = int(h * 0.30)
    y1 = int(h * 0.49)
    lz.fill(x0, y0, x1, y1, con_alfa(VANO, 0.74))
    lz.fill(x0, y0, x1, y0 + max(2, h // 120), con_alfa(nivel.junta, 0.55))
    lz.fill(x0 + max(2, w // 110), y0 + max(3, h // 70),
            x0 + max(4, w // 75), y0 + max(6, h // 47),
            con_alfa(nivel.luz, 0.72 * luz))
    lz.fill(x0 + max(2, w // 110), y0 + max(9, h // 35),
            x0 + max(4, w // 75), y0 + max(12, h // 28),
            con_alfa(0xFFD18A42, 0.48 * luz))
    movimiento = int(math.sin(t * 0.75) * max(1, h // 180))
    lz.fill(x0 + max(5, w // 52), y0 + max(5, h // 52) + movimiento,
            x1 - max(3, w // 90), y0 + max(6, h // 42) + movimiento,
            con_alfa(nivel.luz, 0.26 * luz))
    lz.fill(x0, y1 - max(2, h // 120), x1, y1, con_alfa(nivel.junta, 0.40))


def escalera_biblioteca(lz, w, h, nivel, luz) -> None:
    x = int(w * 0.31)
    y0 = int(h * 0.35)
    y1 = int(h * 0.70)
    ancho = max(2, w // 180)
    claro = con_alfa(nivel.junta, 0.48 * luz)
    lz.fill(x, y0, x + ancho, y1, claro)
    lz.fill(x + max(10, w // 62), y0 + max(2, h // 120),
            x + max(11, w // 58), y1, claro)
    paso = max(8, h // 19)
    for y in range(y0 + 6, y1, paso):
        lz.fill(x, y, x + max(12, w // 50), y + max(1, h // 150), claro)


def techo_invernadero(lz, w, h, nivel, luz, t) -> None:
    metal = con_alfa(nivel.junta, 0.34 * luz)
    y = int(h * 0.14)
    lz.fill(int(w * 0.18), y, int(w * 0.82), y + max(2, h // 120), metal)
    for i in range(4):
        x = int(w * (0.24 + i * 0.17) + math.sin(t * 0.09 + i) * 3.0)
        lz.fill(x, y, x + max(1, w // 220), int(h * 0.34), metal)


def ondas_cisterna(lz, w, h, nivel, luz, t) -> None:
    y = int(h * 0.72)
    color = con_alfa(nivel.luz, 0.12 * luz)
    for i in range(4):
        fase = int(math.sin(t * 0.22 + i * 1.4) * max(2, w // 90))
        x0 = int(w * (0.20 + i * 0.16)) + fase
        largo = max(14, w // 12 - i * max(2, w // 150))
        lz.fill(x0, y + i * max(4, h // 48), x0 + largo,
                y + i * max(4, h // 48) + 1, color)


def torre_luz(lz, x, y0, y1, color, luz, t, fase) -> None:
    ancho = max(8, (y1 - y0) // 12)
    lz.fill(x - ancho, y0, x + ancho, y1, con_alfa(VANO, 0.48))
    paso = max(10, (y1 - y0) // 4)
    y = y0 + 8
    while y < y1 - 4:
        pulso(lz, x, y, color, luz, t, fase + y * 0.01)
        y += paso


def runas(lz, x, y, color, luz) -> None:
    for i in range(5):
        yy = y + i * 7
        lz.fill(x, yy, x + 2, yy + 4, con_alfa(color, 0.36 * luz))
        lz.fill(x + 3, yy + (i % 2), x + 5, yy + 2 + (i % 3), con_alfa(color, 0.22 * luz))


def pulso(lz, x, y, color, luz, t, fase) -> None:
    p = 0.75 + 0.25 * math.sin(t * 1.5 + fase)
    r = 4
    lz.fill(x - r * 2, y - r * 2, x + r * 2, y + r * 2, con_alfa(color, 0.025 * luz * p))
    lz.fill(x - r, y - r, x + r, y + r, con_alfa(color, 0.11 * luz * p))
    lz.fill(x - 2, y - 2, x + 2, y + 2, con_alfa(color, 0.75 * luz * p))


def antorcha(lz, x, y, color, luz, t, fase) -> None:
    """Antorcha de pared con soporte de hierro (espejo de DireccionArte.java)."""
    p = 0.75 + 0.25 * math.sin(t * 7.0 + fase)
    hierro = mezclar(VANO, color, 0.22)
    # Soporte vertical contra la pared (quien sostiene la mensula).
    lz.fill(x - 1, y - 14, x + 2, y + 20, con_alfa(hierro, 0.85))
    # Brazo: sale de la pared y termina debajo de la llama.
    lz.fill(x - 1, y + 8, x + 6, y + 12, con_alfa(hierro, 0.80))
    # Copa del fuego.
    lz.fill(x, y + 2, x + 4, y + 9, con_alfa(hierro, 0.90))
    # Derrame calido.
    lz.fill(x - 20, y - 16, x + 24, y + 18, con_alfa(color, 0.026 * luz * p))
    lz.fill(x - 8, y - 8, x + 12, y + 10, con_alfa(color, 0.075 * luz * p))
    # Llama: nucleo y punta clara.
    lz.fill(x, y - 8, x + 5, y + 3, con_alfa(color, 0.80 * luz))
    lz.fill(x + 1, y - 12, x + 4, y - 6, con_alfa(mezclar(color, 0xFFF3D8, 0.60), 0.70 * luz * p))


def cadena(lz, x, y0, y1, color, luz, eslabones) -> None:
    paso = max(5, (y1 - y0) // max(1, eslabones))
    y = y0
    while y < y1:
        vertical = ((y - y0) // paso) % 2 == 0
        rx = 3 if vertical else 5
        ry = 5 if vertical else 3
        c = mezclar(VANO, color, 0.55)
        lz.fill(x - rx, y, x + rx, y + 1, con_alfa(c, 0.70 * luz))
        lz.fill(x - rx, y + ry * 2, x + rx, y + ry * 2 + 1, con_alfa(c, 0.55 * luz))
        lz.fill(x - rx, y, x - rx + 1, y + ry * 2, con_alfa(c, 0.62 * luz))
        lz.fill(x + rx - 1, y, x + rx, y + ry * 2, con_alfa(c, 0.62 * luz))
        y += paso


def causticas(lz, w, h, nivel, luz, t, desde_y, lineas) -> None:
    for i in range(lineas):
        f = i / max(1, lineas - 1)
        y = int(h * desde_y + f * h * (1.0 - desde_y))
        x = int(w * ((i * 0.173 + 0.11) % 0.86))
        deriva = int(math.sin(t * (0.65 + i * 0.03) + i) * w * 0.018)
        largo = max(10, w // 18 - i * 2)
        lz.fill(x + deriva, y, min(w, x + deriva + largo), y + 1,
                con_alfa(nivel.luz, (0.020 + 0.025 * (1.0 - f)) * luz * nivel.reflejo))


def hojas(lz, borde, h, w, color, luz, t, derecha) -> None:
    for i in range(9):
        y = int(h * (0.20 + i * 0.075))
        largo = 10 + (i % 4) * 6
        deriva = int(math.sin(t * 0.30 + i) * 3.0)
        x0 = w - largo - deriva if derecha else borde + deriva
        x1 = w if derecha else largo + deriva
        lz.fill(max(0, x0), y, min(w, x1), y + 2, con_alfa(color, 0.26 * luz))


def nicho(lz, x, y, w, h, nivel, luz) -> None:
    nw = max(12, w // 18)
    nh = max(14, h // 10)
    lz.fill(x, y, x + nw, y + nh, con_alfa(VANO, 0.32))
    lz.fill(x + 2, y + 2, x + nw - 2, y + nh - 2, con_alfa(nivel.pared_baja, 0.10 * luz))
    lz.fill(x + 4, y + 4, x + nw - 4, y + nh - 4, con_alfa(VANO, 0.26))


# --------------------------------------------------------------------------
# Eventos ambientales: espejo de EventosAmbientales.java
# --------------------------------------------------------------------------
CICLO_MS = 61_000
VENTANA_MS = 6_200


def eventos_ambientales(lz, ancho, alto, nivel, luz, forzado=None) -> None:
    """'forzado' (0..1) fija el progreso del evento para la vista previa.

    Con forzado = None se comporta como Java: la mitad de los ciclos de 61 s
    quedan vacios y el evento ocupa una ventana de 6,2 s. Con forzado = 0.5 se
    dibuja el pico del evento del nivel, sin importar la hora del sistema.
    """
    sem = numero_nivel(nivel) * 31 + 77
    if forzado is None:
        dentro = 0
        # Igual que Java: mitad vacia, ventana de 6,2 s por ciclo de 61 s.
        # Para la vista previa se usa el reloj, como en el juego.
        ahora = int(time.time() * 1000)
        ciclo = ahora // CICLO_MS
        dentro = ahora % CICLO_MS
        sem = hash32(nivel.clave) * 31 + (ciclo ^ (ciclo >> 32))
        if (sem & 1) != 0 or dentro >= VENTANA_MS:
            return
        progreso = dentro / VENTANA_MS
    else:
        progreso = limitar(forzado, 0.0, 1.0)

    pulso_e = math.sin(progreso * math.pi)
    if pulso_e <= 0.001:
        return

    n = numero_nivel(nivel)
    if n <= 2:
        barrido_fluorescente(lz, ancho, alto, nivel, luz, progreso, pulso_e, sem)
    elif n in (3, 6, 8):
        humedad_viva(lz, ancho, alto, nivel, luz, progreso, pulso_e, sem)
    elif n == 5:
        polvo_en_haz(lz, ancho, alto, nivel, luz, progreso, pulso_e, sem)
    elif n == 7:
        silueta_lejana(lz, ancho, alto, pulso_e, sem, progreso)
    elif n == 9:
        silueta_lejana(lz, ancho, alto, pulso_e, sem, progreso)
        cascote_trono(lz, ancho, alto, nivel, luz, pulso_e, sem, progreso)
    else:
        polvo_en_haz(lz, ancho, alto, nivel, luz, progreso, pulso_e, sem)


def barrido_fluorescente(lz, ancho, alto, nivel, luz, progreso, pulso_e, sem) -> None:
    y = int(alto * (0.18 + 0.18 * pseudo(sem + 7)))
    largo = max(26, ancho // 8)
    recorrido = ancho + largo
    x = int(progreso * recorrido) - largo
    alfa = 0.11 * luz * pulso_e
    lz.fill(x, y, min(ancho, x + largo), y + 1, con_alfa(nivel.luz, alfa))
    if x > 0:
        lz.fill(max(0, x - 18), y + 1, x, y + 2, con_alfa(nivel.luz, alfa * 0.35))


def humedad_viva(lz, ancho, alto, nivel, luz, progreso, pulso_e, sem) -> None:
    hum = max(0.35, nivel.humedad)
    base_y = int(alto * (0.58 + 0.18 * pseudo(sem + 13)))
    centro = int(ancho * (0.20 + 0.60 * pseudo(sem + 17)))
    radio = max(18, int(ancho * (0.03 + progreso * 0.08)))
    alfa = 0.08 * hum * luz * pulso_e
    # Sin rizo en el natatorio (nivel3) por el mismo motivo que el Java: la
    # red de luz de la planta y el arte ya cubren el agua.
    if numero_nivel(nivel) != 3:
        lz.fill(max(0, centro - radio), base_y, min(ancho, centro + radio), base_y + 1,
                con_alfa(nivel.luz, alfa))
        lz.fill(max(0, centro - radio // 2), base_y + 3, min(ancho, centro + radio // 2),
                base_y + 4, con_alfa(nivel.luz, alfa * 0.50))
    velo_y = max(0, base_y - 18)
    lz.fill(0, velo_y, ancho, velo_y + 2, con_alfa(nivel.luz, 0.018 * hum * pulso_e))


def polvo_en_haz(lz, ancho, alto, nivel, luz, progreso, pulso_e, sem) -> None:
    cx = int(ancho * (0.38 + 0.24 * pseudo(sem + 23)))
    y0 = int(alto * 0.18)
    y1 = int(alto * 0.72)
    for i in range(14):
        p = pseudo(sem + i * 19)
        x = cx + int((p - 0.5) * ancho * 0.16)
        y = y0 + int(((pseudo(sem + i * 31 + 3) + progreso * 0.18) % 1.0) * (y1 - y0))
        a = (0.08 + p * 0.10) * luz * pulso_e
        lz.fill(x, y, x + (2 if p > 0.82 else 1), y + 1, con_alfa(nivel.luz, a))


def cascote_trono(lz, ancho, alto, nivel, luz, pulso_e, sem, progreso) -> None:
    """Cascote lateral sincronizado con la ventana del evento del Trono."""
    derecha = (sem & 8) == 0
    if derecha:
        posicion = 0.62 + 0.25 * pseudo(sem + 59)
    else:
        posicion = 0.13 + 0.25 * pseudo(sem + 59)
    x = int(ancho * posicion)
    y = int(alto * (0.20 + progreso * 0.34))
    tam = max(2, ancho // 190)
    piedra = mezclar(nivel.junta, nivel.pared_baja, 0.45)
    lz.fill(x, y, x + tam, y + tam, con_alfa(iluminar(piedra, luz), 0.72 * pulso_e))
    lz.fill(x - 1, y + tam, x + tam + 2, y + tam + 1, con_alfa(0x000000, 0.28 * pulso_e))


def silueta_lejana(lz, ancho, alto, pulso_e, sem, progreso) -> None:
    derecha = (sem & 4) == 0
    recorrido = max(24, ancho // 9)
    base_x = int(ancho * (0.43 + 0.12 * pseudo(sem + 41)))
    desplazamiento = int((progreso - 0.5) * recorrido)
    x = base_x + desplazamiento if derecha else base_x - desplazamiento
    y = int(alto * (0.39 + 0.10 * pseudo(sem + 47)))
    h = max(12, alto // 12)
    w = max(3, ancho // 160)
    a = 0.22 * pulso_e
    lz.fill(x, y, x + w, y + h, con_alfa(VANO, a))
    lz.fill(x - 1, y + 2, x + w + 1, y + h // 3, con_alfa(VANO, a * 0.75))


def numero_nivel(nivel: Nivel) -> int:
    return int(nivel.clave.replace("nivel", ""))


def hash32(texto: str) -> int:
    """Hash determinista con signo, igual que String.hashCode() de Java."""
    h = 0
    for c in texto:
        h = (h * 31 + ord(c)) & 0xFFFFFFFF
    if h >= 0x80000000:
        h -= 0x100000000
    return h


# --------------------------------------------------------------------------
# Nivel 0 - La sala: espejo de planta/Sala.java
# --------------------------------------------------------------------------
SALA_TRAMOS = 12
SALA_PLACAS = 7


def sala(lz, m, nivel, luz, tiempo) -> None:
    t_fondo(lz, m, nivel, luz, mezclar(nivel.pared_baja, nivel.niebla, 0.20), 1.5)
    sala_puertas(lz, m, nivel, luz)
    sala_dintel(lz, m, nivel, luz)
    t_plano(lz, m, True, nivel.techo, mezclar(nivel.techo, nivel.niebla, 0.35),
            nivel.niebla, luz, 0.50)
    t_plano(lz, m, False, nivel.suelo, nivel.suelo_lejos, nivel.niebla, luz, 0.58)
    sala_alfombra(lz, m, nivel, luz)
    t_transversales(lz, m, False, nivel.suelo_junta, nivel.niebla, luz, SALA_TRAMOS, 0.38)
    t_transversales(lz, m, True, nivel.techo_junta, nivel.niebla, luz, SALA_TRAMOS, 0.46)
    sala_grilla(lz, m, nivel, luz)
    for j in range(2, SALA_TRAMOS + 1):
        if j == 6:
            continue
        dx = profundidad(j, SALA_TRAMOS)
        if dx <= 6.0:
            t_luminaria(lz, m, nivel, dx, 0.90, 0.30, 1.0, luz)
    t_paredes(lz, m, nivel, luz)
    sala_zocalo(lz, m, nivel, luz)
    t_juntas(lz, m, nivel, luz, SALA_TRAMOS, 1.0, 0.45)
    t_manchas(lz, m, nivel, luz, SALA_TRAMOS)
    sala_cuadros(lz, m, nivel, luz)
    sala_placa(lz, m, nivel, luz)
    sala_abertura(lz, m, nivel, luz)


def sala_abertura(lz, m, nivel, luz) -> None:
    # Abertura de mantenimiento en el lateral derecho: el contrapeso de la
    # placa de administracion. Marco, dos bisagras e interior que no termina
    # en la pared: el sitio tiene instalaciones que cuidar.
    dx = 1.35
    x = m.lado(1.0, dx * 0.74)
    y0 = m.techo_en(dx * 0.55) + m.h * dx * 0.30
    ancho = max(8, round(m.w * dx * 0.20))
    alto = max(12, round(m.h * dx * 0.26))
    x0 = round(x - ancho * 0.5)
    y1 = round(y0 + alto)
    marco = con_alfa(iluminar(nivel.junta, luz * 0.72), 0.85)
    interior = iluminar(velar(nivel.pared_baja, nivel.niebla, 0.80, 0.55), luz * 0.35)
    bisagra = con_alfa(iluminar(nivel.luz, luz * 0.6), 0.7)
    lz.fill(x0, int(y0), x0 + ancho, y1, interior)
    lz.fill(x0, int(y0), x0 + ancho, int(y0) + 1, marco)
    lz.fill(x0, y1 - 1, x0 + ancho, y1, marco)
    lz.fill(x0, int(y0), x0 + 1, y1, marco)
    lz.fill(x0 + ancho - 1, int(y0), x0 + ancho, y1, marco)
    for i in range(2):
        by = int(y0 + alto * (0.30 + i * 0.34))
        lz.fill(x0, by, x0 + 3, by + 3, bisagra)


def sala_puertas(lz, m, nivel, luz) -> None:
    """Tres puertas al fondo: la del medio cerrada, las de los lados abiertas."""
    suelo = m.suelo_en(1.0)
    alto = m.h * 1.30
    for i in range(3):
        centro = m.fx + (m.der(1.0) - m.fx if i > 1 else m.fx - m.izq(1.0)) * (i - 1) * 0.56
        medio = m.w * 0.13
        x0, x1 = round(centro - medio), round(centro + medio)
        y0, y1 = round(suelo - alto), round(suelo)
        if i != 1:
            t_interior_vano(lz, nivel, x0, y0, x1, y1, -1 if i < 1 else 1, luz)
        else:
            lz.fill(x0, y0, x1, y1,
                    iluminar(mezclar(nivel.pared_baja, nivel.junta, 0.35), luz * 0.62))
            lz.fill(x1 - 4, (y0 + y1) // 2, x1 - 2, (y0 + y1) // 2 + 2,
                    iluminar(nivel.luz, luz * 0.55))
        marco = iluminar(mezclar(nivel.junta, nivel.pared_alta, 0.30), luz * 0.70)
        lz.fill(x0 - 1, y0 - 1, x1 + 1, y0 + 1, marco)
        lz.fill(x0 - 1, y0, x0 + 1, y1, marco)
        lz.fill(x1 - 1, y0, x1 + 1, y1, marco)


def sala_dintel(lz, m, nivel, luz) -> None:
    """Dintel pesado de bloques sobre el vano central."""
    dx = 1.0
    techo = m.techo_en(dx)
    suelo = m.suelo_en(dx)
    ancho = m.ancho_en(dx) * 0.40
    alto = m.h * 0.10
    y = techo + (suelo - techo) * 0.22
    piedra = iluminar(velar(nivel.junta, nivel.niebla, 1.0, 0.45), luz * 0.72)
    canto = con_alfa(iluminar(nivel.pared_alta, luz * 0.65), 0.38)
    cx = m.centro(dx)
    lz.fill(round(cx - ancho), round(y), round(cx + ancho), round(y + alto), piedra)
    lz.fill(round(cx - ancho), round(y), round(cx + ancho), round(y + 2), canto)
    for i in range(1, 4):
        x = round(cx - ancho + 2.0 * ancho * i / 4.0)
        lz.fill(x, round(y + 2), x + 1, round(y + alto), con_alfa(nivel.pared_baja, 0.45))


def sala_grilla(lz, m, nivel, luz) -> None:
    """Las longitudinales del cielorraso: lo que lo vuelve una grilla."""
    for i in range(1, SALA_PLACAS):
        frac = (i / SALA_PLACAS) * 2.0 - 1.0
        for y in range(0, max(0, int(m.techo_en(1.0))), PASO):
            dy = m.dy(y + PASO * 0.5)
            if dy <= 1.0:
                continue
            lej = limitar(1.0 / dy, 0.0, 1.0)
            x = m.en_x(dy, frac)
            grosor = max(1, int(m.w * dy * 0.006))
            lz.fill(int(x), y, int(x) + grosor, y + PASO,
                    con_alfa(iluminar(velar(nivel.techo_junta, nivel.niebla, lej, 0.5),
                                      atenuar(luz, lej)), 0.40 * lej + 0.10))


def sala_alfombra(lz, m, nivel, luz) -> None:
    """La franja gastada del centro. Sin bordes rectos."""
    for y in range(round(m.suelo_en(1.0)), m.alto, PASO):
        dy = m.dy(y + PASO * 0.5)
        if dy <= 1.0:
            continue
        lej = limitar(1.0 / dy, 0.0, 1.0)
        medio = m.w * dy * 0.42
        lz.fill(int(m.fx - medio), y, int(m.fx + medio), y + PASO,
                con_alfa(iluminar(nivel.suelo_junta, luz), 0.16 * (0.45 + 0.55 * lej)))


def sala_zocalo(lz, m, nivel, luz) -> None:
    for x in range(0, m.ancho, PASO):
        dx = m.dx(x + PASO * 0.5)
        if dx <= 1.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        y1 = m.suelo_en(dx)
        alto = max(1, int(m.h * dx * 0.055))
        lz.fill(x, int(y1) - alto, x + PASO, int(y1),
                iluminar(nivel.junta, atenuar(luz, lej) * 0.85))


def sala_cuadros(lz, m, nivel, luz) -> None:
    """La marca que dejo el cuadro: el empapelado menos desvaido que el resto.

    Antes se pintaba MAS claro que la pared y se leia como un rectangulo
    luminoso flotante. El papel protegido por el cuadro es mas oscuro y rico;
    y con la alfa modulada por ruido los bordes dejan de ser una linea recta.
    """
    for j in range(3, SALA_TRAMOS):
        if pseudo(820 + j) > 0.38:
            continue
        dxa = profundidad(j, SALA_TRAMOS)
        dxb = profundidad(j + 1, SALA_TRAMOS)
        signo = -1 if pseudo(860 + j) < 0.5 else 1
        lej = limitar(1.0 / dxa, 0.0, 1.0)
        x0 = int(min(m.lado(signo, dxa), m.lado(signo, dxb)))
        x1 = int(max(m.lado(signo, dxa), m.lado(signo, dxb)))
        if x1 <= 0 or x0 >= m.ancho or x1 - x0 < 3:
            continue
        tinta = mezclar(nivel.pared_alta, 0xFF000000, 0.42)
        for col in range(max(0, x0), min(m.ancho, x1)):
            dxc = m.dx(col + 0.5)
            centro = m.techo_en(1.0 * dxc * 0.30)
            medio = m.h * dxc * 0.22
            # Alfa por columna: el contorno se quiebra como el papel real.
            quiebre = 0.70 + 0.60 * pseudo(888 + col * 7)
            alfa = (0.10 * lej + 0.04) * quiebre
            lz.fill(col, int(centro - medio), col + 1, int(centro + medio),
                    con_alfa(iluminar(tinta, luz), alfa))


def sala_placa(lz, m, nivel, luz) -> None:
    """Placa lateral con remaches y ranuras grabadas."""
    dx = 1.20
    x = m.lado(-1.0, dx * 0.76)
    y = m.techo_en(dx * 0.45) + m.h * dx * 0.26
    ancho = max(8, round(m.w * dx * 0.18))
    alto = max(10, round(m.h * dx * 0.22))
    placa = iluminar(velar(nivel.junta, nivel.niebla, 0.83, 0.35), luz * 0.70)
    borde = con_alfa(iluminar(nivel.luz, luz * 0.58), 0.45)
    x0, y0 = round(x - ancho * 0.5), round(y)
    lz.fill(x0, y0, x0 + ancho, y0 + alto, placa)
    lz.fill(x0, y0, x0 + ancho, y0 + 1, borde)
    lz.fill(x0, y0 + alto - 1, x0 + ancho, y0 + alto, borde)
    lz.fill(x0, y0, x0 + 1, y0 + alto, borde)
    lz.fill(x0 + ancho - 1, y0, x0 + ancho, y0 + alto, borde)
    remache = con_alfa(iluminar(nivel.pared_alta, luz), 0.78)
    margen = max(2, ancho // 7)
    for lado in (-1, 1):
        rx = x0 + margen if lado < 0 else x0 + ancho - margen - 1
        lz.fill(rx, y0 + margen, rx + 2, y0 + margen + 2, remache)
        lz.fill(rx, y0 + alto - margen - 2, rx + 2, y0 + alto - margen, remache)
    for i in range(3):
        yy = y0 + alto // 3 + i * max(2, alto // 9)
        lz.fill(x0 + margen, yy, x0 + ancho - margen, yy + 1,
                con_alfa(nivel.pared_baja, 0.55))


# --------------------------------------------------------------------------
# Nivel 1 - La nave: espejo de planta/Nave.java
# --------------------------------------------------------------------------
NAVE_TRAMOS = 16
NAVE_HILERA = 0.62
NAVE_CORDON = 0.66     # altura del cordon inferior de la cercha
NAVE_LEJOS = 5.5       # a partir de aqui la estructura ya salio del cuadro


def nave(lz, m, nivel, luz, tiempo) -> None:
    t_fondo(lz, m, nivel, luz, mezclar(nivel.pared_baja, nivel.niebla, 0.42), 1.35)
    nave_porton(lz, m, nivel, luz)
    nave_puerta_muelle(lz, m, nivel, luz)
    t_plano(lz, m, True, mezclar(nivel.techo, VANO, 0.30),
            mezclar(nivel.techo, nivel.niebla, 0.50), nivel.niebla, luz, 0.62)
    t_plano(lz, m, False, nivel.suelo, nivel.suelo_lejos, nivel.niebla, luz, 0.55)
    nave_losas(lz, m, nivel, luz)
    t_transversales(lz, m, False, nivel.suelo_junta, nivel.niebla, luz, NAVE_TRAMOS, 0.26)
    t_paredes(lz, m, nivel, luz)
    t_juntas(lz, m, nivel, luz, NAVE_TRAMOS, 1.0, 0.26)
    t_manchas(lz, m, nivel, luz, NAVE_TRAMOS)
    nave_estanteria(lz, m, nivel, luz)
    nave_cerchas(lz, m, nivel, luz)
    nave_pilares(lz, m, nivel, luz)
    nave_campanas(lz, m, nivel, luz)
    nave_lona(lz, m, nivel, luz)


def nave_lona(lz, m, nivel, luz) -> None:
    # Lona de carga caida, amontonada contra el piso del lado derecho. La
    # nave es toda lineas rectas; esta es la unica forma blanda: bandas que
    # se pliegan unas sobre otras, con costuras marcadas.
    dx = 2.1
    x = m.lado(1.0, dx * 0.62)
    margen = m.w * dx * 0.2
    if x < -margen or x > m.ancho + margen:
        return
    lej = limitar(1.0 / dx, 0.0, 1.0)
    at = atenuar(luz, lej) * 0.8
    y_suelo = m.suelo_en(dx)
    ancho = m.w * dx * 0.17
    alto = m.h * dx * 0.17
    tela = iluminar(velar(mezclar(nivel.pared_baja, nivel.junta, 0.35), nivel.niebla, lej, 0.4), at * 0.95)
    pliegue = iluminar(tela, 0.70)
    borde = iluminar(tela, 1.18)
    sesgo = ancho * 0.14
    for k in range(4):
        f = k / 4.0
        w = ancho * (1.0 - f * 0.16)
        x0 = x - w * 0.5 + (0.0 if k % 2 == 0 else sesgo * f)
        y0 = int(y_suelo - alto + alto * f * 1.35)
        y1 = min(int(y_suelo), int(y_suelo - alto + alto * (f + 0.55) * 1.35))
        lz.fill(int(x0), y0, int(x0 + w), y1, tela)
        lz.fill(int(x0), y0, int(x0 + w), y0 + 1, borde)
        lz.fill(int(x0 + w * 0.38), y0, int(x0 + w * 0.38) + 1, y1, pliegue)


def nave_porton(lz, m, nivel, luz) -> None:
    """Chapa acanalada, cerrada, con una rendija de luz abajo."""
    suelo = m.suelo_en(1.0)
    alto = m.ha * 1.05
    x0, x1 = round(m.izq(0.62)), round(m.der(0.62))
    y0, y1 = round(suelo - alto), round(suelo)
    # La chapa devuelve algo de luz: si se va a negro, la nave termina en un
    # agujero y se pierde toda la profundidad que arman las cerchas.
    lz.fill_gradient(x0, y0, x1, y1,
                     iluminar(mezclar(nivel.pared_baja, nivel.junta, 0.35), luz * 0.95),
                     iluminar(mezclar(nivel.junta, nivel.pared_baja, 0.40), luz * 0.62))
    paso = max(2, (x1 - x0) // 18)
    for x in range(x0 + paso, x1, paso):
        lz.fill(x, y0, x + 1, y1, con_alfa(VANO, 0.16))
    for k in range(1, 5):
        y = y0 + (y1 - y0) * k // 5
        lz.fill(x0, y, x1, y + 1, con_alfa(VANO, 0.20))
    lz.fill(x0 - 1, y0 - 1, x1 + 1, y0 + 1,
            iluminar(mezclar(nivel.junta, nivel.pared_alta, 0.25), luz * 0.60))
    lz.fill(x0, y1 - 2, x1, y1, con_alfa(nivel.luz, 0.12 * luz))


def nave_puerta_muelle(lz, m, nivel, luz) -> None:
    """Vano lateral del muelle, con jambas, dintel y umbral."""
    dx = 1.35
    pared = m.lado(1.0, dx)
    ancho = max(5.0, m.w * dx * 0.13)
    suelo = m.suelo_en(dx)
    alto = max(10.0, m.h * dx * 0.62)
    y0, y1 = suelo - alto, suelo
    x0, x1 = round(pared - ancho), round(pared)
    hueco = con_alfa(mezclar(VANO, nivel.niebla, 0.15), 0.94)
    lz.fill(x0, round(y0), x1, round(y1), hueco)
    marco = iluminar(velar(nivel.junta, nivel.niebla, 0.72, 0.50), luz * 0.72)
    dintel = max(2, round(m.h * dx * 0.035))
    lz.fill(x0 - 2, round(y0) - dintel, x1 + 2, round(y0), marco)
    lz.fill(x0 - 2, round(y0), x0 + 1, round(y1), marco)
    lz.fill(x1 - 1, round(y0), x1 + 2, round(y1), marco)
    lz.fill(x0 - 2, round(y1) - max(2, dintel // 2), x1 + 2, round(y1), marco)
    lz.fill(x0 + 2, round(y1) - max(2, dintel // 2), x1 - 2, round(y1),
            con_alfa(iluminar(nivel.luz, luz * 0.45), 0.36))
    bisagra = con_alfa(iluminar(nivel.pared_alta, luz), 0.65)
    lz.fill(x0 + 2, round(y0 + alto * 0.22), x0 + 4, round(y0 + alto * 0.28), bisagra)
    lz.fill(x0 + 2, round(y0 + alto * 0.70), x0 + 4, round(y0 + alto * 0.76), bisagra)


def nave_losas(lz, m, nivel, luz) -> None:
    """Dos corridas longitudinales de losa. El suelo deja de ser una mancha."""
    for frac in (-0.55, 0.55):
        for y in range(round(m.suelo_en(1.0)), m.alto, PASO):
            dy = m.dy(y + PASO * 0.5)
            if dy <= 1.0:
                continue
            lej = limitar(1.0 / dy, 0.0, 1.0)
            x = m.en_x(dy, frac)
            grosor = max(1, int(m.w * dy * 0.005))
            lz.fill(int(x), y, int(x) + grosor, y + PASO,
                    con_alfa(iluminar(nivel.suelo_junta, atenuar(luz, lej)),
                             0.30 * lej + 0.08))


def nave_cerchas(lz, m, nivel, luz) -> None:
    """Triangulacion metalica vista. El ojo lee estructura, no techo."""
    for j in range(1, NAVE_TRAMOS + 1):
        dx = profundidad(j, NAVE_TRAMOS)
        if dx > NAVE_LEJOS:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej) * 0.78
        color = iluminar(velar(nivel.junta, nivel.niebla, lej, 0.45), at)
        y_sup = m.techo_en(1.0 * dx * 0.98)
        y_inf = m.techo_en(1.0 * dx * NAVE_CORDON)
        if y_inf < -6:
            continue
        x0 = max(0, int(m.izq(1.0 * dx)))
        x1 = min(m.ancho, int(m.der(1.0 * dx)))
        if x1 - x0 < 6:
            continue
        grosor = max(1, int(m.h * dx * 0.020))
        lz.fill(x0, int(y_sup), x1, int(y_sup) + grosor, color)
        lz.fill(x0, int(y_inf), x1, int(y_inf) + grosor, color)
        # El zigzag entre los dos cordones: sube, baja, sube.
        paso = max(5, (x1 - x0) // 10)
        sube = True
        for x in range(x0, x1 - paso, paso):
            ya, yb = (y_inf, y_sup) if sube else (y_sup, y_inf)
            for k in range(6):
                tt = k / 6.0
                px = int(x + paso * tt)
                py = int(ya + (yb - ya) * tt)
                lz.fill(px, py, px + max(1, paso // 5), py + grosor,
                        con_alfa(color, 0.65))
            sube = not sube


def nave_campanas(lz, m, nivel, luz) -> None:
    """Pocas, colgadas del cordon, y una de cada tres apagada."""
    for j in range(1, NAVE_TRAMOS + 1, 2):
        dx = profundidad(j, NAVE_TRAMOS)
        if dx > NAVE_LEJOS:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        y_techo = m.techo_en(1.0 * dx * NAVE_CORDON)
        y_lampara = m.techo_en(1.0 * dx * (NAVE_CORDON - 0.14))
        medio = max(1.5, m.w * dx * 0.038)
        if y_lampara > m.alto:
            continue
        lz.fill(int(m.fx) - 1, int(y_techo), int(m.fx) + 1, int(y_lampara),
                con_alfa(iluminar(nivel.junta, at), 0.60))
        lz.fill(int(m.fx - medio), int(y_lampara),
                int(m.fx + medio), int(y_lampara + medio * 0.45),
                iluminar(mezclar(nivel.junta, nivel.pared_alta, 0.25), at * 0.85))
        if pseudo(910 + j) <= 0.30:
            continue
        y = y_lampara + medio * 0.45
        for k in (3, 2, 1):
            tt = k / 3.0
            ex = medio * (1.0 + tt * 4.5)
            ey = medio * (1.0 + tt * 3.5)
            lz.fill(int(m.fx - ex), int(y - ey * 0.25), int(m.fx + ex), int(y + ey),
                    con_alfa(nivel.luz, 0.05 * at * (1.0 - tt * 0.5)))
        lz.fill(int(m.fx - medio * 0.6), int(y), int(m.fx + medio * 0.6),
                int(y + max(1.0, medio * 0.30)),
                con_alfa(iluminar(nivel.luz, min(1.0, at * 1.4)), 0.95))


def nave_pilares(lz, m, nivel, luz) -> None:
    """Prismas: cara frontal iluminada, cara lateral en sombra."""
    for j in range(2, NAVE_TRAMOS + 1, 2):
        dx = profundidad(j, NAVE_TRAMOS)
        if dx > NAVE_LEJOS:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        ancho = max(1.5, m.w * dx * 0.040)
        y_techo = m.techo_en(1.0 * dx * NAVE_CORDON)
        y_suelo = m.suelo_en(1.0 * dx)
        for signo in (-1, 1):
            if j == 8 and signo > 0:
                continue
            x = m.lado(signo, dx, NAVE_HILERA)
            if x < -ancho * 2 or x > m.ancho + ancho * 2:
                continue
            frente = iluminar(velar(nivel.pared_alta, nivel.niebla, lej, 0.55), at * 0.88)
            costado = iluminar(velar(nivel.pared_baja, nivel.niebla, lej, 0.50), at * 0.55)
            corte = ancho * 0.40 * (1 if signo < 0 else -1)
            lz.fill(int(x - ancho), int(y_techo), int(x + corte), int(y_suelo),
                    costado if signo < 0 else frente)
            lz.fill(int(x + corte), int(y_techo), int(x + ancho), int(y_suelo),
                    frente if signo < 0 else costado)
            # La base de hormigon: sin ella el pilar flota.
            alto = m.h * dx * 0.07
            lz.fill(int(x - ancho * 1.25), int(y_suelo - alto),
                    int(x + ancho * 1.25), int(y_suelo),
                    iluminar(velar(nivel.junta, nivel.niebla, lej, 0.4), at * 0.70))


def nave_estanteria(lz, m, nivel, luz) -> None:
    """Tres largueros contra la pared izquierda, sin nada encima."""
    for x in range(0, m.ancho, PASO):
        centro = x + PASO * 0.5
        if centro > m.fx:
            continue
        dx = m.dx(centro)
        if dx <= 1.10 or dx > 4.5:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej) * 0.75
        y_suelo = m.suelo_en(dx)
        color = iluminar(velar(nivel.junta, nivel.niebla, lej, 0.5), at)
        for a in (0.22, 0.52, 0.80):
            y = y_suelo - m.h * dx * a
            grosor = max(1, int(m.h * dx * 0.016))
            lz.fill(x, int(y), x + PASO, int(y) + grosor, con_alfa(color, 0.85))


# --------------------------------------------------------------------------
# Nivel 2 - Servicio: espejo de planta/Servicio.java
# --------------------------------------------------------------------------
SERV_TRAMOS = 22
SERV_CODO = 7


def servicio(lz, m, nivel, luz, tiempo) -> None:
    t_fondo(lz, m, nivel, luz, mezclar(nivel.pared_baja, nivel.junta, 0.35))
    serv_tablero(lz, m, nivel, luz)
    t_plano(lz, m, True, nivel.techo, mezclar(nivel.techo, nivel.niebla, 0.42),
            nivel.niebla, luz, 0.58)
    t_plano(lz, m, False, nivel.suelo, nivel.suelo_lejos, nivel.niebla, luz, 0.62)
    t_transversales(lz, m, False, nivel.suelo_junta, nivel.niebla, luz, SERV_TRAMOS, 0.34)
    t_paredes(lz, m, nivel, luz)
    t_juntas(lz, m, nivel, luz, SERV_TRAMOS, 1.0, 0.45)
    t_manchas(lz, m, nivel, luz, SERV_TRAMOS)
    serv_bifurcacion(lz, m, nivel, luz)
    serv_compuerta(lz, m, nivel, luz)
    serv_haz(lz, m, nivel, luz)
    serv_apliques(lz, m, nivel, luz)
    serv_bandeja(lz, m, nivel, luz)
    serv_valvula(lz, m, nivel, luz)
    serv_manguera(lz, m, nivel, luz)
    serv_rejillas(lz, m, nivel, luz)


def serv_bandeja(lz, m, nivel, luz) -> None:
    # Bandeja de cables colgada del techo, con un bucle de cable suelto bajo
    # el colgador central: la instalacion electrica viaja por arriba, y alguien
    # dejo la tarea a medias.
    dy = 3.0
    y_techo = m.techo_en(dy * 0.85)
    y_bandeja = y_techo + m.h * dy * 0.045
    lej = limitar(1.0 / dy, 0.0, 1.0)
    at = atenuar(luz, lej)
    metal = iluminar(velar(nivel.junta, nivel.niebla, lej, 0.45), at * 0.8)
    cable = iluminar(velar(nivel.pared_baja, nivel.niebla, lej, 0.4), at * 0.55)
    lz.fill(0, int(y_bandeja), m.ancho, int(y_bandeja) + 1, con_alfa(metal, 0.85))
    for x in (m.lado(-1.0, dy * 0.62), m.centro(dy), m.lado(1.0, dy * 0.62)):
        if x < -5.0 or x > m.ancho + 5.0:
            continue
        lz.fill(int(x), int(y_techo), int(x) + 1, int(y_bandeja), con_alfa(metal, 0.80))
    ux = m.centro(dy) + m.w * dy * 0.10
    if -5.0 < ux < m.ancho + 5.0:
        y_fondo = int(y_bandeja + m.h * dy * 0.055)
        media = m.w * dy * 0.012
        for k in range(7):
            t = k / 6.0
            curva = abs(t * 2.0 - 1.0)
            x = int(ux - media + (t - 0.5) * media * 2.0)
            y = int(y_bandeja + (y_fondo - y_bandeja) * curva)
            lz.fill(x, y, x + 1, y + 1, con_alfa(cable, 0.9))


def serv_tablero(lz, m, nivel, luz) -> None:
    """La chapa cerrada con su piloto, justo en el punto de fuga."""
    x0 = round(m.izq(1.0 * 0.46))
    x1 = round(m.der(1.0 * 0.46))
    suelo = m.suelo_en(1.0)
    y0 = round(suelo - m.h * 1.22)
    y1 = round(suelo - m.h * 0.42)
    lz.fill(x0, y0, x1, y1,
            iluminar(mezclar(nivel.junta, nivel.pared_baja, 0.40), luz * 0.52))
    lz.fill_gradient(x0, y0, x1, y1,
                     con_alfa(iluminar(nivel.pared_alta, luz * 0.30), 0.22),
                     con_alfa(VANO, 0.30))
    lz.fill(x0, y0, x1, y0 + 1, iluminar(nivel.pared_alta, luz * 0.40))
    lz.fill((x0 + x1) // 2, y0, (x0 + x1) // 2 + 1, y1, con_alfa(VANO, 0.45))

    ancho = x1 - x0
    alto = y1 - y0
    if ancho >= 10 and alto >= 12:
        reja_alto = max(1, alto // 22)
        for hoja in range(2):
            rx0 = x0 + ancho * (1 + hoja * 4) // 10
            rx1 = rx0 + ancho * 3 // 10
            for k in range(3):
                ry = y0 + alto // 8 + k * reja_alto * 3
                if ry + reja_alto >= y1:
                    break
                lz.fill(rx0, ry, rx1, ry + reja_alto, con_alfa(VANO, 0.42))
                lz.fill(rx0, ry + reja_alto, rx1, ry + reja_alto + 1,
                        con_alfa(iluminar(nivel.pared_alta, luz * 0.55), 0.30))
        margen = max(2, ancho // 12)
        cabeza = con_alfa(iluminar(nivel.pared_alta, luz * 0.65), 0.55)
        for ex in range(2):
            for ey in range(2):
                sx = x0 + margen if ex == 0 else x1 - margen - 1
                sy = y0 + margen if ey == 0 else y1 - margen - 1
                lz.fill(sx, sy, sx + 1, sy + 1, cabeza)
    px = x1 - max(3, (x1 - x0) // 8)
    py = y0 + max(3, (y1 - y0) // 6)
    lz.fill(px, py, px + 2, py + 2, con_alfa(ALERTA_BRILLO, 0.85 * luz + 0.15))


def serv_compuerta(lz, m, nivel, luz) -> None:
    """Compuerta de inspeccion entreabierta en la pared izquierda."""
    dx = 1.65
    x = m.lado(-1.0, dx * 0.92)
    suelo = m.suelo_en(dx)
    alto = max(9.0, m.h * dx * 0.34)
    ancho = max(7.0, m.w * dx * 0.26)
    x0, x1 = round(x), round(x + ancho)
    y0, y1 = round(suelo - alto), round(suelo)
    lz.fill(x0, y0, x1, y1, con_alfa(mezclar(nivel.fondo, VANO, 0.22), 0.96))
    marco = iluminar(velar(nivel.junta, nivel.niebla, 0.60, 0.45), luz * 0.68)
    lz.fill(x0, y0, x1, y0 + 2, marco)
    lz.fill(x0, y0, x0 + 2, y1, marco)
    lz.fill(x1 - 2, y0, x1, y1, marco)
    lz.fill(x0, y1 - 2, x1, y1, marco)
    herraje = con_alfa(iluminar(nivel.pared_alta, luz), 0.70)
    lz.fill(x0 + 3, y0 + max(3, round(alto * 0.20)), x0 + 5, y0 + max(4, round(alto * 0.28)), herraje)
    lz.fill(x0 + 3, y0 + round(alto * 0.70), x0 + 5, y0 + round(alto * 0.78), herraje)
    lz.fill(x1 - max(4, round(ancho * 0.18)), y0 + round(alto * 0.46), x1 - 2,
            y0 + round(alto * 0.52), herraje)
    lz.fill(x0 + 3, y0 + 3, x0 + max(5, round(ancho * 0.20)), y1 - 3, con_alfa(VANO, 0.48))


def serv_bifurcacion(lz, m, nivel, luz) -> None:
    """El tramo que se abre de costado. Esto no es un tubo: es una red."""
    dxa = profundidad(SERV_CODO, SERV_TRAMOS)
    dxb = profundidad(SERV_CODO + 2, SERV_TRAMOS)
    lej = limitar(1.0 / dxa, 0.0, 1.0)
    xa = m.izq(1.0 * dxa)
    xb = m.izq(1.0 * dxb)
    x0, x1 = int(min(xa, xb)), int(max(xa, xb))
    if x1 <= 0 or x0 >= m.ancho:
        return
    for col in range(max(0, x0), min(m.ancho, x1)):
        dxc = m.dx(col + 0.5)
        ys = m.suelo_en(dxc)
        altura = 2.0 * m.h * dxc * 0.82
        t = (col - x0) / max(1, x1 - x0)
        lz.fill_gradient(col, int(ys - altura), col + 1, int(ys),
                         con_alfa(iluminar(nivel.fondo, luz * 0.14), 0.95),
                         con_alfa(iluminar(nivel.luz, luz * 0.10 * (1.0 - t)), 0.92))
    if 0 <= x1 < m.ancho:
        dxc = m.dx(x1 + 0.5)
        lz.fill(x1, int(m.suelo_en(dxc) - 2.0 * m.h * dxc * 0.82), x1 + 2,
                int(m.suelo_en(dxc)),
                con_alfa(iluminar(nivel.pared_alta, atenuar(luz, lej)), 0.75))


def serv_haz(lz, m, nivel, luz) -> None:
    """Cinco corridas de distinto diametro bajo el techo, mas abrazaderas."""
    alturas = (0.86, 0.78, 0.70, 0.62, 0.56)
    radios = (0.070, 0.038, 0.054, 0.028, 0.022)
    tonos = (0.45, 0.10, 0.28, 0.05, 0.18)
    for c in range(len(alturas)):
        for x in range(0, m.ancho, PASO):
            dx = m.dx(x + PASO * 0.5)
            if dx <= 1.0:
                continue
            lej = limitar(1.0 / dx, 0.0, 1.0)
            at = atenuar(luz, lej)
            eje = m.techo_en(dx * alturas[c])
            radio = max(1.0, m.h * dx * radios[c])
            base = mezclar(nivel.junta, nivel.pared_alta, 0.20 + tonos[c])
            lz.fill_gradient(x, int(eje - radio), x + PASO, int(eje + radio),
                             iluminar(mezclar(base, nivel.luz, 0.26), at),
                             iluminar(mezclar(base, VANO, 0.40), at))
    for j in range(2, SERV_TRAMOS + 1, 3):
        dx = profundidad(j, SERV_TRAMOS)
        if dx > 8.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej) * 0.85
        for signo in (-1, 1):
            x = m.lado(signo, dx, 0.80)
            if x < 0 or x > m.ancho:
                continue
            y0 = m.techo_en(1.0 * dx * 0.90)
            y1 = m.techo_en(1.0 * dx * 0.54)
            grosor = max(1, int(m.w * dx * 0.012))
            lz.fill(int(x), int(y0), int(x) + grosor, int(y1),
                    con_alfa(iluminar(nivel.junta, at), 0.70))


def serv_apliques(lz, m, nivel, luz) -> None:
    """Van en la pared porque el techo esta ocupado. Luz rasante y sucia."""
    for j in range(2, SERV_TRAMOS + 1, 3):
        dx = profundidad(j, SERV_TRAMOS)
        if dx > 7.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        x = m.der(1.0 * dx * 0.98)
        if x < 0 or x > m.ancho:
            continue
        y = m.techo_en(1.0 * dx * 0.48)
        alto = max(1.5, m.h * dx * 0.070)
        ancho = max(1.5, m.w * dx * 0.030)
        lz.fill(int(x - ancho), int(y - alto), int(x), int(y + alto),
                con_alfa(iluminar(nivel.luz, min(1.0, at * 1.6)), 0.95))
        for s in (-0.35, 0.35):
            lz.fill(int(x - ancho), int(y + alto * s), int(x), int(y + alto * s) + 1,
                    con_alfa(VANO, 0.45))
        for k in (3, 2, 1):
            t = k / 3.0
            lz.fill(int(x - ancho * (1.0 + t * 3.5)), int(y - alto * (1.0 + t * 2.2)),
                    int(x), int(y + alto * (1.0 + t * 2.2)),
                    con_alfa(nivel.luz, 0.075 * at * (1.0 - t * 0.45)))


def serv_valvula(lz, m, nivel, luz) -> None:
    """Valvula con aro, eje y manija, anclada a la pared derecha."""
    dx = 2.15
    x = m.lado(1.0, dx * 0.92)
    y = m.techo_en(dx * 0.46)
    radio = max(4.0, m.w * dx * 0.075)
    metal = iluminar(velar(nivel.junta, nivel.niebla, 0.45, 0.40), luz * 0.72)
    lz.fill(round(x - radio * 1.25), round(y - radio * 1.25),
            round(x + radio * 1.25), round(y + radio * 1.25), con_alfa(VANO, 0.70))
    for i in range(8):
        a = math.pi * 2.0 * i / 8.0
        px, py = x + math.cos(a) * radio, y + math.sin(a) * radio
        lz.fill(round(px - 1.5), round(py - 1.5), round(px + 1.5), round(py + 1.5), metal)
    lz.fill(round(x - radio * 0.95), round(y - 1.0), round(x + radio * 0.95), round(y + 2.0), metal)
    lz.fill(round(x - 1.0), round(y - radio * 0.95), round(x + 2.0), round(y + radio * 0.95), metal)
    lz.fill(round(x - 2.0), round(y - 2.0), round(x + 3.0), round(y + 3.0), iluminar(nivel.luz, luz * 0.70))
    lz.fill(round(x - radio * 0.75), round(y + radio * 1.3), round(x + radio * 0.70), round(y + radio * 1.55),
            con_alfa(iluminar(nivel.pared_alta, luz * 0.42), 0.30))


def serv_manguera(lz, m, nivel, luz) -> None:
    """Manguera de goma en una curva irregular, rematada por una brida."""
    dx = 1.85
    x_base = m.lado(-1.0, dx * 0.83)
    y_base = m.suelo_en(dx) - m.h * dx * 0.13
    goma = iluminar(velar(nivel.pared_baja, nivel.niebla, 0.54, 0.48), luz * 0.68)
    for i in range(9):
        t = i / 8.0
        x = x_base + math.sin(t * math.pi * 1.35) * m.w * dx * 0.085
        y = y_base - m.h * dx * (0.34 - t * 0.24)
        lz.fill(round(x), round(y), round(x + max(2.0, m.w * dx * 0.016)),
                round(y + max(2.0, m.h * dx * 0.026)), goma)
    brida = iluminar(nivel.junta, luz * 0.72)
    lz.fill(round(x_base - 3.0), round(y_base - 2.0), round(x_base + 6.0), round(y_base + 2.0), brida)


def serv_rejillas(lz, m, nivel, luz) -> None:
    for j in range(5, SERV_TRAMOS + 1, 6):
        dx = profundidad(j, SERV_TRAMOS)
        if dx > 6.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej) * 0.70
        x = m.izq(1.0 * dx)
        if x < -20 or x > m.ancho:
            continue
        y_suelo = m.suelo_en(dx)
        alto = m.h * dx * 0.16
        ancho = max(2.0, m.w * dx * 0.10)
        lz.fill(int(x), int(y_suelo - alto), int(x + ancho), int(y_suelo),
                iluminar(mezclar(nivel.junta, VANO, 0.35), at))
        lamas = max(2, int(alto / 3.0))
        for k in range(1, lamas):
            y = y_suelo - alto + alto * k / lamas
            lz.fill(int(x), int(y), int(x + ancho), int(y) + 1,
                    con_alfa(iluminar(nivel.pared_alta, at), 0.35))


# --------------------------------------------------------------------------
# Nivel 3 - El natatorio: espejo de planta/Natatorio.java
# --------------------------------------------------------------------------
NAT_TRAMOS = 14
NAT_CABECERA = 1.55    # dy donde termina la baldosa y empieza el agua
NAT_VASO = 0.82        # semiancho del vaso, en fraccion del recinto
NAT_CALLES = 4


def natatorio(lz, m, nivel, luz, tiempo) -> None:
    t_fondo(lz, m, nivel, luz, mezclar(nivel.pared_baja, nivel.techo, 0.70), 2.15)
    nat_testero(lz, m, nivel, luz)
    nat_ventana_rota(lz, m, nivel, luz)
    t_plano(lz, m, True, nivel.techo, mezclar(nivel.techo, nivel.niebla, 0.30),
            nivel.niebla, luz, 0.44)
    t_transversales(lz, m, True, nivel.techo_junta, nivel.niebla, luz, NAT_TRAMOS, 0.26)
    nat_claraboyas(lz, m, nivel, luz)
    nat_borde(lz, m, nivel, luz)
    nat_desague_lateral(lz, m, nivel, luz)
    nat_agua(lz, m, nivel, luz, tiempo)
    nat_calles(lz, m, nivel, luz, tiempo)
    nat_reflejo_luces(lz, m, nivel, luz, tiempo)
    t_paredes(lz, m, nivel, luz)
    nat_escalerilla(lz, m, nivel, luz)
    nat_marcas_profundidad(lz, m, nivel, luz)
    nat_azulejo(lz, m, nivel, luz)
    t_juntas(lz, m, nivel, luz, NAT_TRAMOS, 1.0, 0.24)
    t_manchas(lz, m, nivel, luz, NAT_TRAMOS)
    nat_caustica(lz, m, nivel, luz, tiempo)
    # El recinto entero se oscurece en los bordes: cuanto mas lejos del
    # centro del vaso, mas se pierde la luz. No es una vineta rectangular
    # comun; es que la humedad y la distancia se llevan la luz de los
    # bordes del recinto, y eso se nota mucho mas en un natatorio que en
    # un pasillo, donde el fondo es una pared y no agua.
    franja = max(12, m.ancho // 5)
    for x in range(0, franja, 4):
        t = 1.0 - x / franja
        lz.fill(x, 0, x + 4, m.alto, con_alfa(VANO, 0.12 * t * t))
        lz.fill(m.ancho - x - 4, 0, m.ancho - x, m.alto, con_alfa(VANO, 0.12 * t * t))
    franja_v = max(8, m.alto // 6)
    for y in range(0, franja_v, 4):
        t = 1.0 - y / franja_v
        lz.fill(0, y, m.ancho, y + 4, con_alfa(VANO, 0.09 * t * t))
        lz.fill(0, m.alto - y - 4, m.ancho, m.alto - y, con_alfa(VANO, 0.09 * t * t))


def nat_testero(lz, m, nivel, luz) -> None:
    """Doble puerta de vaiven al fondo, con su franja de vidrio armado.

    Antes de la puerta va el azulejado del propio testero: con el recinto
    ancho, la pared del fondo es una superficie grande y si queda lisa se lee
    como una chapa gris pegada en el medio del cuadro, no como el fondo de un
    natatorio.
    """
    # Azulejo del testero: hiladas horizontales, mas juntas hacia el zocalo.
    fy0, fy1 = round(m.techo_en(1.0)), round(m.suelo_en(1.0))
    fx0, fx1 = round(m.izq(1.0)), round(m.der(1.0))
    hiladas = 9
    for k in range(1, hiladas):
        f = (k / hiladas) ** 1.25
        y = int(fy0 + (fy1 - fy0) * f)
        desvio = pseudo(k * 191 + 37) * 0.10 - 0.05
        lz.fill(fx0, y, fx1, y + 1,
                con_alfa(iluminar(nivel.junta, luz * (0.85 + desvio)), 0.28))
    # Varias juntas verticales de baldosilla, no solo dos: un azulejo real
    # tiene columnas de teja, y un natatorio de verdad los azulejos son
    # cuadrados que se cuentan desde el techo hasta el suelo.
    columnas = 7
    for c in range(1, columnas):
        f = c / columnas
        desvio = pseudo(c * 271 + 11) * 0.10 - 0.05
        x = int(fx0 + (fx1 - fx0) * f)
        lz.fill(x, fy0, x + 1, fy1,
                con_alfa(iluminar(nivel.junta, luz * (0.85 + desvio)), 0.18))

    suelo = m.suelo_en(1.0 * NAT_CABECERA)
    alto = m.h * 1.05
    x0, x1 = round(m.izq(1.0 * 0.30)), round(m.der(1.0 * 0.30))
    y0, y1 = round(suelo - alto), round(suelo)
    lz.fill(x0 - 2, y0 - 2, x1 + 2, y1,
            iluminar(mezclar(nivel.junta, nivel.techo, 0.30), luz * 0.75))
    lz.fill(x0, y0, x1, y1, iluminar(mezclar(nivel.pared_baja, VANO, 0.45), luz * 0.55))
    lz.fill((x0 + x1) // 2, y0, (x0 + x1) // 2 + 1, y1, con_alfa(VANO, 0.55))
    vy = y0 + (y1 - y0) // 4
    for lado in (0, 1):
        vx0 = x0 + 3 + lado * (x1 - x0) // 2
        vx1 = vx0 + (x1 - x0) // 2 - 6
        lz.fill(vx0, vy, vx1, vy + max(3, (y1 - y0) // 5), con_alfa(VANO, 0.70))
    # La franja de azulejo que corre por debajo del vano, hasta las paredes.
    lz.fill(round(m.izq(1.0 * NAT_CABECERA)), y1 - 2,
            round(m.der(1.0 * NAT_CABECERA)), y1,
            con_alfa(iluminar(nivel.junta, luz), 0.40))


def nat_ventana_rota(lz, m, nivel, luz) -> None:
    """Ventana alta rota: abertura imperfecta hacia otro sector del complejo."""
    dx = 1.18
    centro = m.centro(dx) - m.w * dx * 0.27
    ancho = m.w * dx * 0.22
    alto = m.h * dx * 0.22
    y = m.techo_en(dx) + m.h * dx * 0.18
    x0, x1 = round(centro - ancho * 0.5), round(centro + ancho * 0.5)
    y0, y1 = round(y), round(y + alto)
    lz.fill(x0 - 3, y0 - 3, x1 + 3, y1 + 3, iluminar(nivel.junta, luz * 0.62))
    lz.fill(x0, y0, x1, y1, con_alfa(VANO, 0.76))
    ox, oy = max(2, (x1 - x0) // 7), max(2, (y1 - y0) // 7)
    for xa, xb, ya, yb, a in (
            (x0 + ox, x0 + (x1 - x0) // 2 - 2, y0 + oy, y0 + oy * 2, 0.25),
            (x0 + (x1 - x0) // 2 + 2, x1 - ox, y0 + oy, y0 + oy * 2, 0.18),
            (x0 + ox, x0 + (x1 - x0) // 2 - 3, y1 - oy * 2, y1 - oy, 0.18),
            (x0 + (x1 - x0) // 2 + 3, x1 - ox, y1 - oy * 2, y1 - oy, 0.14)):
        lz.fill(xa, ya, xb, yb, con_alfa(nivel.luz, a))
    lz.fill(x0 + (x1 - x0) // 2 - ox, y0 + oy,
            x0 + (x1 - x0) // 2, y1 - oy, con_alfa(nivel.junta, 0.58))
    lz.fill(x0 + ox, y0 + oy, x1 - ox, y0 + oy + 1,
            con_alfa(iluminar(nivel.techo, luz), 0.28))
    lz.fill(x0 + (x1 - x0) // 3, y0 + oy * 2,
            x0 + (x1 - x0) // 3 + 2, y1 - oy * 2,
            con_alfa(nivel.junta, 0.48))


def nat_desague_lateral(lz, m, nivel, luz) -> None:
    """Rejilla lateral anclada al borde de la piscina."""
    dx = 2.35
    x = round(m.izq(dx * NAT_VASO))
    y = round(m.suelo_en(dx) - m.h * dx * 0.035)
    ancho = max(7, round(m.w * dx * 0.13))
    alto = max(4, round(m.h * dx * 0.035))
    marco = iluminar(mezclar(nivel.junta, nivel.techo, 0.30), luz * 0.58)
    lz.fill(x - 2, y - 2, x + ancho + 2, y + alto + 2, marco)
    lz.fill(x, y, x + ancho, y + alto, con_alfa(VANO, 0.68))
    for r in range(1, 5):
        rx = x + r * ancho // 5
        lz.fill(rx, y + 1, rx + 1, y + alto - 1, con_alfa(nivel.junta, 0.76))


def nat_marcas_profundidad(lz, m, nivel, luz) -> None:
    """Placas físicas de profundidad ancladas al borde."""
    for i, dx in enumerate((1.72, 2.15, 2.72)):
        x = round(m.der(dx * NAT_VASO) - m.w * dx * 0.17)
        y = round(m.suelo_en(dx) - m.h * dx * 0.04)
        ancho = max(8, round(m.w * dx * 0.12))
        alto = max(3, round(m.h * dx * 0.025))
        placa = iluminar(mezclar(nivel.junta, nivel.pared_baja, 0.20), luz * 0.76)
        lz.fill(x, y, x + ancho, y + alto, placa)
        lz.fill(x + 2, y + alto, x + ancho - 2, y + alto + 1,
                con_alfa(VANO, 0.55))
        lz.fill(x + ancho // 2, y + 1, x + ancho // 2 + 1, y + alto - 1,
                con_alfa(nivel.techo, 0.22))


def nat_claraboyas(lz, m, nivel, luz) -> None:
    """Lo unico que ilumina esto entra por arriba, y no alcanza."""
    for j in range(2, NAT_TRAMOS + 1):
        dx = profundidad(j, NAT_TRAMOS)
        if dx > 6.0 or pseudo(1200 + j) <= 0.45:
            continue
        # En el natatorio las luminarias estan mas apagadas: el recinto es
        # grande y humedo, y la luz del techo recorre menos antes de perderse.
        t_luminaria(lz, m, nivel, dx, 0.97, 0.30, 1.10, luz)


def nat_borde(lz, m, nivel, luz) -> None:
    """La baldosa de la orilla. Ocupa todo el suelo; el agua se le apoya."""
    for y in range(round(m.suelo_en(1.0)), m.alto, PASO):
        dy = m.dy(y + PASO * 0.5)
        if dy <= 1.0:
            continue
        lej = limitar(1.0 / dy, 0.0, 1.0)
        desvio = pseudo(int(dy * 337.0) + 13) * 0.12 - 0.06
        color = velar(mezclar(nivel.techo, nivel.pared_baja, 0.30 + desvio),
                      nivel.niebla, lej, 0.38)
        lz.fill(0, y, m.ancho, y + PASO, iluminar(color, atenuar(luz, lej)))
    t_transversales(lz, m, False, nivel.techo_junta, nivel.niebla, luz, NAT_TRAMOS, 0.20)

    # El borde de la orilla: donde la baldosa encuentra el agua, hay un filo
    # de luz que tambien dice donde empieza el vaso. No es una linea negra
    # sino la reflexion del techo en el canto humedo de la baldosa.
    frontera = m.suelo_en(1.0 * NAT_CABECERA)
    fy_borde = round(frontera)
    if 0 <= fy_borde < m.alto:
        filo = con_alfa(iluminar(nivel.techo, luz * 0.80), 0.55)
        lz.fill(0, fy_borde, m.ancho, fy_borde + 1, filo)
        # La fila de baldosilla inmediatamente delante del agua queda
        # mas clara por reflexion del agua mismo.
        lz.fill(0, fy_borde - 1, m.ancho, fy_borde,
                con_alfa(iluminar(nivel.suelo, luz * 0.40), 0.35))
        # Sarro bajo el rebosadero: lenguetas minerales donde el agua
        # se evaporo, no en toda la orilla.
        semilla = java_hash(nivel.clave)
        sarro = con_alfa(mezclar(nivel.pared_baja, nivel.techo, 0.45), 0.34)
        for i in range(9):
            px = pseudo(semilla + i * 23)
            x = int(px * m.ancho)
            largo = 3 + int(pseudo(semilla + i * 41) * 9)
            lz.fill(x, fy_borde + 1, x + 2,
                    min(m.alto, fy_borde + 1 + largo), sarro)


def nat_agua(lz, m, nivel, luz, tiempo) -> None:
    """El vaso: un trapecio que arranca lejos y se abre hacia la camara."""
    desde = round(m.suelo_en(1.0 * NAT_CABECERA))
    for y in range(desde, m.alto, PASO):
        dy = m.dy(y + PASO * 0.5)
        lej = limitar(1.0 / dy, 0.0, 1.0)
        x0 = int(m.en_x(dy, -NAT_VASO))
        x1 = int(m.en_x(dy, NAT_VASO))
        if x1 <= 0 or x0 >= m.ancho:
            continue
        # Cuanto mas cerca, mas hondo se ve el vaso y mas oscuro el agua.
        hondo = limitar((dy - NAT_CABECERA) / (NAT_TRAMOS * 0.35), 0.0, 1.0)
        # El agua del natatorio es mas oscura que el suelo: la masa de agua
        # tiene su propio tono, mas teal y mas hondo que las baldosas que la
        # reciben. Cuanto mas hondo, mas se acerca al color del fondo.
        agua_base = velar(mezclar(nivel.suelo, nivel.pared_baja, 0.45 + hondo * 0.35),
                          nivel.niebla, lej, 0.30)
        lz.fill(max(0, x0), y, min(m.ancho, x1), y + PASO,
                iluminar(agua_base, atenuar(luz, lej) * 0.90))
        canto = con_alfa(iluminar(nivel.techo, luz), 0.60)
        lz.fill(max(0, x0 - 2), y, max(0, x0), y + PASO, canto)
        lz.fill(min(m.ancho, x1), y, min(m.ancho, x1 + 2), y + PASO, canto)

    # La cabecera del vaso, al fondo, y el reflejo del techo sobre ella.
    lz.fill(int(m.en_x(NAT_CABECERA, -NAT_VASO)), desde - 2,
            int(m.en_x(NAT_CABECERA, NAT_VASO)), desde + 1,
            con_alfa(iluminar(nivel.techo, luz), 0.70))
    largo = m.h * 1.4
    for y in range(desde, min(m.alto, desde + int(largo)), PASO):
        tt = (y - desde) / largo
        dy = m.dy(y)
        onda = math.sin(tiempo * 0.45 + tt * 7.0) * m.w * 0.012
        lz.fill(int(m.en_x(dy, -NAT_VASO * 0.70) + onda), y,
                int(m.en_x(dy, NAT_VASO * 0.70) + onda), y + PASO,
                con_alfa(nivel.techo, 0.22 * (1.0 - tt) * (1.0 - tt) * luz))

    # Niebla superficial: el aire sobre el agua dentro de un natatorio
    # cerrado siempre esta cargado de humedad. Es mas visible a poca
    # distancia del borde (arriba del agua en pantalla) y se disipa hacia
    # la camara (abajo en pantalla).
    for y in range(desde, m.alto, PASO):
        dy = m.dy(y + PASO * 0.5)
        lej = limitar(1.0 / dy, 0.0, 1.0)
        x0 = int(m.en_x(dy, -NAT_VASO))
        x1 = int(m.en_x(dy, NAT_VASO))
        if x1 <= 0 or x0 >= m.ancho:
            continue
        prof = limitar((dy - NAT_CABECERA) / (NAT_TRAMOS * 0.35), 0.0, 1.0)
        humedad = (1.0 - prof) * 0.18 * luz
        if humedad <= 0.0:
            continue
        x0i = max(0, x0)
        x1i = min(m.ancho, x1)
        if x1i <= x0i:
            continue
        # Jirones de vapor que se arrastran despacio, no una banda pareja.
        niebla = mezclar(nivel.pared_alta, nivel.pared_baja, 0.50)
        paso = max(PASO * 8, (x1i - x0i) // 10)
        for jx in range(x0i, x1i, paso):
            onda = (math.sin(tiempo * 0.16 + jx * 0.010 + dy * 0.6)
                    + 0.6 * math.sin(tiempo * 0.09 - jx * 0.017))
            jiron = 0.55 + 0.45 * onda
            a = humedad * limitar(jiron, 0.0, 1.2)
            if a <= 0.006:
                continue
            lz.fill(jx, y, min(x1i, jx + paso), y + PASO, con_alfa(niebla, a))

    # Burbujas y motas: en un natatorio quieto hay siempre algo flotando.
    burbujas = 14
    for i in range(burbujas):
        seed = pseudo(i * 53 + 7)
        dxb = round(1.1 + seed * (NAT_TRAMOS * 0.55))
        x_pos = m.en_x(dxb, (pseudo(i * 53 + 11) - 0.5) * NAT_VASO * 1.6)
        y_pos = m.suelo_en(dxb)
        if x_pos < 0 or x_pos > m.ancho or y_pos < desde or y_pos > m.alto:
            continue
        alt = max(1, int(m.h * dxb * 0.035))
        py = int(y_pos) - alt
        if py < desde or py > m.alto:
            continue
        brillo = (0.08 + pseudo(i * 53 + 19) * 0.14) * luz
        lz.fill(int(x_pos), py, int(x_pos) + 1, py + alt,
                con_alfa(iluminar(nivel.luz, brillo), 0.50))


def nat_calles(lz, m, nivel, luz, tiempo) -> None:
    """Las lineas del fondo del vaso, quebradas por el agua que las tapa."""
    desde = round(m.suelo_en(1.0 * NAT_CABECERA))
    for i in range(1, NAT_CALLES):
        frac = (i / NAT_CALLES) * 2.0 - 1.0
        for y in range(desde, m.alto, PASO):
            dy = m.dy(y + PASO * 0.5)
            lej = limitar(1.0 / dy, 0.0, 1.0)
            onda = math.sin(tiempo * 0.5 + dy * 2.2 + i * 1.7) * m.w * 0.010
            x = m.en_x(dy, frac * NAT_VASO) + onda
            grosor = max(1, int(m.w * dy * 0.012))
            lz.fill(int(x), y, int(x) + grosor, y + PASO,
                    con_alfa(iluminar(nivel.techo, luz), 0.26 + 0.16 * lej))


def nat_reflejo_luces(lz, m, nivel, luz, tiempo) -> None:
    """El reflejo de los tubos del techo sobre la superficie del agua.

    Es el detalle que mas hace que el agua se lea como agua: una columna de luz
    alargada bajo cada tubo, estirada hacia la camara y partida en trozos
    temblorosos. Un reflejo entero se lee como espejo; uno roto, como agua.
    """
    desde = round(m.suelo_en(1.0 * NAT_CABECERA))
    for j in range(2, NAT_TRAMOS + 1):
        dx = profundidad(j, NAT_TRAMOS)
        if dx > 6.0 or pseudo(1200 + j) <= 0.45:
            continue
        cx = m.centro(dx)
        if cx < 0 or cx > m.ancho:
            continue
        desvio = pseudo(int(dx * 977.0) + 31)
        cansancio = 0.80 + 0.32 * desvio
        lej_tubo = limitar(1.0 / dx, 0.0, 1.0)
        arranque = max(desde, int(m.suelo_en(dx)))
        largo = m.h * (1.6 + 3.4 * lej_tubo)
        hasta = min(m.alto, arranque + int(largo))
        ancho_base = max(1.5, m.w * dx * 0.05)
        for y in range(arranque, hasta, PASO):
            t = (y - arranque) / largo
            if t >= 1.0:
                break
            dy = m.dy(y + PASO * 0.5)
            eje = m.centro(dy)
            temblor = (math.sin(tiempo * 1.1 + y * 0.12 + j)
                       + 0.5 * math.sin(tiempo * 1.9 + y * 0.05)) * m.w * 0.006
            ancho = ancho_base * (1.0 + t * 1.4) * (0.6 + 0.4 * math.sin(y * 0.4 + tiempo))
            trozo = 0.5 + 0.5 * math.sin(y * 0.55 + tiempo * 2.3 + j)
            alfa = 0.27 * (1.0 - t) * (1.0 - t) * trozo * luz * cansancio
            if alfa <= 0.012:
                continue
            x0 = round(eje - ancho * 0.5 + temblor)
            x1 = round(eje + ancho * 0.5 + temblor)
            lz.fill(x0, y, x1, y + PASO,
                    con_alfa(iluminar(nivel.luz, min(1.0, luz * 1.15)), alfa))


def nat_escalerilla(lz, m, nivel, luz) -> None:
    """Dos barandas curvas asomando del agua. La escala se lee con esto."""
    dy = 1.95
    y = m.suelo_en(1.0 * dy)
    x = m.der(1.0 * dy * NAT_VASO)
    if x > m.ancho + 24 or y > m.alto + 24:
        return
    alto = m.h * dy * 0.30
    sep = m.w * dy * 0.055
    color = iluminar(mezclar(nivel.techo, VANO, 0.25), luz * 0.90)
    grosor = max(2, int(m.w * dy * 0.010))
    for signo in (-1, 1):
        px = x + sep * signo * 0.5
        lz.fill(int(px), int(y - alto), int(px) + grosor, int(y), color)
    lz.fill(int(x - sep * 0.5), int(y - alto), int(x + sep * 0.5 + grosor),
            int(y - alto) + grosor, color)
    for k in range(2):
        py = y - alto * 0.55 + alto * 0.30 * k
        lz.fill(int(x - sep * 0.5), int(py), int(x + sep * 0.5 + grosor),
                int(py) + grosor, con_alfa(color, 0.80))


def nat_azulejo(lz, m, nivel, luz) -> None:
    """La cenefa a la altura de los ojos. Sin ella la pared es revoque."""
    for x in range(0, m.ancho, PASO):
        dx = m.dx(x + PASO * 0.5)
        if dx <= 1.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        y = m.techo_en(1.0 * dx * 0.16)
        alto = max(1, int(m.h * dx * 0.075))
        lz.fill(x, int(y), x + PASO, int(y) + alto,
                con_alfa(iluminar(velar(nivel.junta, nivel.niebla, lej, 0.5), at), 0.55))
        lz.fill(x, int(y) + alto, x + PASO, int(y) + alto + 1,
                con_alfa(iluminar(nivel.techo, at), 0.35))


def nat_caustica(lz, m, nivel, luz, tiempo) -> None:
    """La red de luz que el agua devuelve a las paredes. Sube lentisima."""
    for b in range(7):
        fase = tiempo * (0.10 + pseudo(1300 + b) * 0.07) + b * 0.9
        altura = fase % 1.0
        a = 0.11 * math.sin(math.pi * altura) * luz
        if a <= 0.004:
            continue
        for x in range(0, m.ancho, PASO * 2):
            dx = m.dx(x + PASO)
            if dx <= 1.0:
                continue
            lej = limitar(1.0 / dx, 0.0, 1.0)
            y_suelo = m.suelo_en(dx)
            y = y_suelo - (y_suelo - m.fy) * altura
            grueso = max(1, int(m.h * dx * 0.020))
            ondul = math.sin(x * 0.06 + tiempo * 0.7 + b) * 0.5 + 0.5
            lz.fill(x, int(y), x + PASO * 2, int(y) + grueso,
                    con_alfa(nivel.luz, a * (0.35 + 0.65 * ondul) * (0.4 + 0.6 * lej)))


# --------------------------------------------------------------------------
# Primer plano: lo que esta MAS CERCA que la camara
# --------------------------------------------------------------------------
# Aca esta la razon de fondo por la que los cuatro niveles se seguian leyendo
# como el mismo pasillo aunque cada uno tuviera su arquitectura, su paleta y su
# camara descentrada.
#
# Las cuatro escenas estaban dibujadas ENTERAS entre la fuga y el borde del
# cuadro. Todo lo que se veia estaba lejos y se iba haciendo chico hacia el
# centro. Un encuadre asi solo puede leerse de una manera: como un tubo. Da
# igual que el tubo tenga pileta o estanterias.
#
# Lo que hace que un cuadro se lea como un LUGAR y no como un tunel es que haya
# algo mas cerca que la camara: un montante que corta el borde, una viga que
# entra por arriba, el canto de una mesa. Dos cosas pasan de golpe:
#
#   1. Se rompe el marco. El recinto deja de estar contenido dentro de la
#      pantalla y pasa a continuar fuera de ella, que es como funciona la
#      vision real: uno nunca ve una habitacion entera de una vez.
#   2. Aparece el paralaje. Si lo cercano se mueve mas que lo lejano, el ojo
#      deduce profundidad de verdad, no profundidad dibujada.
#
# Cada planta pone lo suyo. No son adornos: son el elemento que dice desde
# donde se esta mirando. En la sala, el canto del mostrador -se mira desde
# detras del mostrador-. En la nave, una columna cortada -se mira desde detras
# de una columna-. En el servicio, los canos pasan POR ENCIMA de la camara -se
# esta metido en el pasillo tecnico-. En el natatorio, el trampolin entra desde
# arriba -se esta debajo del trampolin, al borde del agua-.

def desvio(tiempo: float, amplitud: float, velocidad: float) -> float:
    """Balanceo lentisimo de la camara.

    No es viento ni temblor: es que nadie sostiene una camara perfectamente
    quieta. Amplitud de pocos pixeles y periodo largo. Se nota si se mira
    fijo diez minutos, y no se nota si se mira diez segundos, que es
    exactamente lo que se busca.
    """
    return math.sin(tiempo * velocidad) * amplitud


def pp_sala(lz, m, nivel, luz, tiempo) -> None:
    """El canto del mostrador, cruzando el borde inferior.

    Se mira la sala desde detras del mostrador de recepcion, que es lo que
    justifica la altura y el descentrado de la camara. Para que se lea como un
    mostrador y no como una sombra en el piso necesita las tres cosas que tiene
    cualquier mueble mirado de cerca: una tapa horizontal que recibe luz
    cenital, un frente vertical en sombra, y un filo entre las dos.
    """
    balance = desvio(tiempo, 3.0, 0.09)
    tapa_y = int(lz.alto * 0.80 + balance)
    x0 = int(lz.ancho * 0.30 + balance * 1.6)

    frente = mezclar(nivel.pared_baja, 0x000000, 0.58)
    tapa = mezclar(nivel.suelo, 0x000000, 0.20)

    # Frente: cae del filo hasta el borde del cuadro, y se oscurece abajo.
    lz.fill_gradient(x0, tapa_y, lz.ancho, lz.alto,
                     iluminar(frente, 0.34 + 0.20 * luz),
                     iluminar(frente, 0.12 + 0.10 * luz))
    # Tapa: una banda horizontal clara. Es lo que dice "esto es un mueble".
    espesor = max(4, int(lz.alto * 0.030))
    lz.fill_gradient(x0, tapa_y - espesor, lz.ancho, tapa_y,
                     iluminar(tapa, 0.62 + 0.34 * luz),
                     iluminar(tapa, 0.44 + 0.26 * luz))
    # Filo iluminado por los tubos del techo.
    lz.fill(x0, tapa_y - espesor, lz.ancho, tapa_y - espesor + 2,
            con_alfa(0xFFFFF3D8, 0.16 + 0.24 * luz))
    # Canto lateral izquierdo: cierra el mueble y deja ver el piso detras.
    lz.fill(x0, tapa_y - espesor, x0 + 3, lz.alto,
            con_alfa(iluminar(frente, 0.20 + 0.14 * luz), 0.9))
    # Una carpeta olvidada sobre la tapa, de canto.
    cx = int(lz.ancho * 0.58 + balance * 1.6)
    lz.fill(cx, tapa_y - espesor - 6, cx + int(lz.ancho * 0.11), tapa_y - espesor,
            con_alfa(iluminar(nivel.pared_alta, 0.66 + 0.30 * luz), 0.88))
    lz.fill(cx, tapa_y - espesor - 6, cx + int(lz.ancho * 0.11), tapa_y - espesor - 5,
            con_alfa(0xFFFFF3D8, 0.20 * luz))


def pp_nave(lz, m, nivel, luz, tiempo) -> None:
    """Una columna de hormigon cortada por el borde izquierdo.

    La nave es el sitio mas grande de los cuatro y el tamano solo se percibe
    por comparacion: sin nada cerca, una nave de treinta metros y un pasillo de
    tres se dibujan igual. La primera version ponia una franja fina y oscura
    contra el borde y no se leia -parecia vineta-. Una columna que convence
    tiene que ser ANCHA (ocupa un sexto del cuadro), tiene que estar CORTADA
    por el borde, y tiene que tener una cara iluminada y otra en sombra, porque
    es un prisma y no una linea.
    """
    balance = desvio(tiempo, 4.5, 0.07)
    # Ancha de verdad. La nave esta llena de pilares verticales a media
    # distancia; una columna de ancho parecido se confunde con ellos y no
    # aporta profundidad. Lo que la delata como cercana es el CONTRASTE DE
    # ESCALA: tiene que ser varias veces mas ancha que los pilares del fondo y
    # taparlos al pasar por delante.
    ancho = int(lz.ancho * 0.235)
    x0 = int(-ancho * 0.16 + balance)
    x1 = x0 + ancho
    # La cara que da a las luminarias es la derecha; el quiebre esta a dos
    # tercios, no en el medio, porque la columna no se ve de frente.
    quiebre = int(x0 + ancho * 0.62)

    # La columna cae dentro de la franja donde la vineta oscurece el cuadro,
    # asi que si se pinta con los valores "realistas" del hormigon en sombra
    # desaparece. Se pinta clara a proposito: esta a un metro de la camara y a
    # un metro las luminarias del techo pegan de lleno.
    hormigon = mezclar(nivel.pared_baja, 0xFFFFFF, 0.22)
    # Cara en sombra: media, no negra.
    lz.fill_gradient(x0, 0, quiebre, lz.alto,
                     iluminar(mezclar(hormigon, 0x000000, 0.34), 0.62 + 0.26 * luz),
                     iluminar(mezclar(hormigon, 0x000000, 0.52), 0.42 + 0.20 * luz))
    # Cara iluminada: la mas clara del cuadro entero.
    lz.fill_gradient(quiebre, 0, x1, lz.alto,
                     iluminar(mezclar(hormigon, 0xFFFFFF, 0.10), 0.86 + 0.14 * luz),
                     iluminar(hormigon, 0.60 + 0.24 * luz))
    # Arista viva entre las dos caras.
    lz.fill(quiebre, 0, quiebre + 2, lz.alto,
            con_alfa(0xFFD9E4EC, 0.10 + 0.16 * luz))
    # Borde derecho: sombra proyectada de la columna sobre lo que hay detras.
    # Sin esto la columna flota; con esto se apoya en el espacio.
    lz.fill(x1, 0, x1 + max(3, int(lz.ancho * 0.010)), lz.alto,
            con_alfa(VANO, 0.34))
    lz.fill(x1 - 1, 0, x1, lz.alto, con_alfa(VANO, 0.50))

    # Junta de encofrado horizontal: da escala y dice "esto es hormigon".
    paso = max(10, int(lz.alto * 0.17))
    y = paso // 2
    while y < lz.alto:
        lz.fill(x0, y, x1, y + 1, con_alfa(VANO, 0.20))
        lz.fill(x0, y + 1, x1, y + 2, con_alfa(0xFFD9E4EC, 0.05 + 0.05 * luz))
        y += paso

    # Numero de columna estarcido, ilegible pero reconocible como marca.
    cy = int(lz.alto * 0.30)
    lz.fill(quiebre + 6, cy, x1 - 5, cy + int(lz.alto * 0.09),
            con_alfa(iluminar(nivel.junta, 0.60 + 0.34 * luz), 0.55))

    # Mancha de humedad subiendo desde el pie: la nave esta abandonada.
    hy = int(lz.alto * 0.72)
    lz.fill_gradient(x0, hy, x1, lz.alto, con_alfa(VANO, 0.0), con_alfa(VANO, 0.34))


def pp_servicio(lz, m, nivel, luz, tiempo) -> None:
    """Los canos que pasan por encima de la camara.

    En el pasillo tecnico la camara no mira el pasillo: esta DENTRO. Dos canos
    cruzan el borde superior de lado a lado, muy cerca, casi en silueta. Son
    los mismos canos que se ven alejarse al fondo, y esa continuidad es la que
    mete al que mira dentro del recinto.
    """
    balance = desvio(tiempo, 2.0, 0.11)
    for indice, (altura, grosor, tono) in enumerate((
            (0.045, 0.052, 0.30),
            (0.125, 0.034, 0.16),
    )):
        y0 = int(lz.alto * altura + balance * (1.0 + indice * 0.4))
        y1 = y0 + int(lz.alto * grosor)
        cuerpo = mezclar(nivel.junta, 0x000000, 0.55 + tono * 0.4)
        lz.fill_gradient(0, y0, lz.ancho, y1,
                         iluminar(cuerpo, 0.42 + 0.26 * luz),
                         iluminar(cuerpo, 0.16 + 0.12 * luz))
        # Reflejo especular corrido: el cano es redondo.
        lz.fill(0, y0 + 1, lz.ancho, y0 + 2, con_alfa(0xFFE8E2CE, 0.06 + 0.12 * luz))
        # Abrazaderas cada tanto.
        paso = int(lz.ancho * 0.27)
        x = int(lz.ancho * 0.08)
        while x < lz.ancho:
            lz.fill(x, y0 - 2, x + 6, y1 + 2,
                    con_alfa(iluminar(cuerpo, 0.30 + 0.20 * luz), 0.95))
            x += paso


def pp_natatorio(lz, m, nivel, luz, tiempo) -> None:
    """El trampolin, entrando desde el borde superior izquierdo.

    Es el elemento que convierte "un pasillo con agua" en "una pileta". La
    primera version salia un trapecio gris colgado del techo en el medio: no se
    leia como nada. Un trampolin se reconoce por otras cosas -que entra desde
    fuera del cuadro, que esta ARRIBA del agua y no del piso, que tiene el
    canto en sombra y la cara superior clara, y que termina en el aire-. Eso es
    lo que se dibuja aca.
    """
    balance = desvio(tiempo, 2.6, 0.06)
    # Entra por arriba a la izquierda y avanza hacia el centro-derecha: una
    # diagonal, no una simetria. La diagonal es la que rompe la caja.
    x_ini, y_ini = -lz.ancho * 0.04, lz.alto * 0.10 + balance
    x_fin, y_fin = lz.ancho * 0.46, lz.alto * 0.36 + balance * 0.4

    pasos = 20
    espesor_ini, espesor_fin = lz.alto * 0.055, lz.alto * 0.022
    for i in range(pasos):
        t_ = i / (pasos - 1)
        x = x_ini + (x_fin - x_ini) * t_
        y = y_ini + (y_fin - y_ini) * t_
        esp = espesor_ini + (espesor_fin - espesor_ini) * t_
        ancho_paso = (x_fin - x_ini) / pasos + 2

        # Cara superior: chapa clara, se va apagando con la distancia.
        cara = mezclar(nivel.pared_alta, 0x000000, 0.10 + 0.30 * t_)
        lz.fill(int(x), int(y), int(x + ancho_paso), int(y + esp * 0.42),
                iluminar(cara, 0.70 + 0.26 * luz * (1.0 - t_ * 0.4)))
        # Canto en sombra: es lo que le da espesor de plancha.
        canto = mezclar(nivel.pared_baja, 0x000000, 0.58 + 0.20 * t_)
        lz.fill(int(x), int(y + esp * 0.42), int(x + ancho_paso), int(y + esp),
                iluminar(canto, 0.30 + 0.20 * luz))

    # Punta del trampolin: remate mas oscuro, colgando sobre el agua.
    lz.fill(int(x_fin - 4), int(y_fin), int(x_fin + 3), int(y_fin + espesor_fin),
            con_alfa(mezclar(nivel.pared_baja, 0x000000, 0.70), 0.9))

    # Baranda: dos tubos que arrancan del borde y mueren a media plancha.
    for alt in (0.16, 0.30):
        for i in range(pasos // 2):
            t_ = i / (pasos // 2 - 1)
            x = x_ini + (x_fin - x_ini) * t_ * 0.55
            y = y_ini + (y_fin - y_ini) * t_ * 0.55 - lz.alto * alt * (1.0 - t_ * 0.35)
            lz.fill(int(x), int(y), int(x + (x_fin - x_ini) / pasos + 2), int(y + 2),
                    con_alfa(iluminar(nivel.junta, 0.60 + 0.30 * luz), 0.75))
    # Montantes verticales de la baranda.
    for t_ in (0.06, 0.30, 0.54):
        x = x_ini + (x_fin - x_ini) * t_ * 0.55
        y = y_ini + (y_fin - y_ini) * t_ * 0.55
        lz.fill(int(x), int(y - lz.alto * 0.30), int(x + 2), int(y),
                con_alfa(iluminar(nivel.junta, 0.55 + 0.28 * luz), 0.7))

    # Sombra de la plancha sobre el agua: lo que la ancla al lugar.
    sy = int(lz.alto * 0.74)
    lz.fill(int(x_ini), sy, int(x_fin * 0.92), sy + int(lz.alto * 0.05),
            con_alfa(VANO, 0.16 + 0.06 * luz))


# --------------------------------------------------------------------------
# Nivel 4 - La sala de piedra: espejo de planta/Cripta.java
# --------------------------------------------------------------------------
CRI_TRAMOS = 13


def _fuego(tiempo, desfase):
    t = tiempo + desfase
    v = (1.0 + 0.06 * math.sin(t * 11.0) + 0.04 * math.sin(t * 17.3 + 1.7)
         + 0.03 * math.sin(t * 6.1 + 0.4))
    if math.sin(t * 3.7 + desfase * 2.0) > 0.985:
        v *= 0.86
    return limitar(v, 0.80, 1.15)


def _linea(lz, x0, y0, x1, y1, grosor, color):
    pasos = max(1, abs(x1 - x0) // PASO)
    for i in range(pasos + 1):
        t = i / pasos
        x = int(x0 + (x1 - x0) * t)
        y = int(y0 + (y1 - y0) * t)
        lz.fill(x, y, x + PASO, y + grosor, color)


def cripta(lz, m, nivel, luz, tiempo) -> None:
    pulso = _fuego(tiempo, 0.0)
    t_fondo(lz, m, nivel, luz, mezclar(nivel.fondo, nivel.pared_baja, 0.18), 1.20)
    cri_tunel(lz, m, nivel, luz)
    cri_boveda(lz, m, nivel, luz)
    t_transversales(lz, m, True, nivel.techo_junta, nivel.niebla, luz, CRI_TRAMOS, 0.30)
    t_plano(lz, m, False, nivel.suelo, nivel.suelo_lejos, nivel.niebla, luz, 0.55)
    t_transversales(lz, m, False, nivel.suelo_junta, nivel.niebla, luz, CRI_TRAMOS, 0.40)
    cri_runas(lz, m, nivel, luz, tiempo)
    t_paredes(lz, m, nivel, luz)
    cri_sillares(lz, m, nivel, luz)
    cri_nicho_lateral(lz, m, nivel, luz)
    cri_marca_peregrinacion(lz, m, nivel, luz)
    t_manchas(lz, m, nivel, luz, CRI_TRAMOS)
    cri_columnas(lz, m, nivel, luz)
    cri_estandartes(lz, m, nivel, luz, tiempo)
    cri_antorchas(lz, m, nivel, luz, tiempo)
    cri_candil(lz, m, nivel, luz, tiempo, pulso)
    cri_cera_candil(lz, m, nivel, luz)


def cri_tunel(lz, m, nivel, luz) -> None:
    suelo = m.suelo_en(1.0)
    alto = m.h * 1.5
    x0, x1 = round(m.izq(0.42)), round(m.der(0.42))
    y0, y1 = round(suelo - alto), round(suelo)
    cx = (x0 + x1) // 2
    radio = (x1 - x0) // 2
    t_interior_vano(lz, nivel, x0, y0 + radio // 2, x1, y1, 0, luz)
    for i in range(17):
        ang = math.pi * i / 16.0
        ax = cx - int(math.cos(ang) * radio)
        ay = (y0 + radio // 2) - int(math.sin(ang) * radio * 0.5)
        borde = max(1, radio // 8)
        lz.fill(ax - borde // 2, ay - borde // 2, ax + borde // 2 + 1, ay + borde // 2 + 1,
                iluminar(mezclar(nivel.junta, nivel.pared_alta, 0.30), luz * 0.62))


def cri_boveda(lz, m, nivel, luz) -> None:
    t_plano(lz, m, True, mezclar(nivel.techo, nivel.pared_baja, 0.30),
            mezclar(nivel.techo, nivel.niebla, 0.45), nivel.niebla, luz, 0.52)
    for j in range(2, CRI_TRAMOS + 1, 2):
        dx = profundidad(j, CRI_TRAMOS)
        if dx > 6.5:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej) * 0.85
        color = iluminar(velar(nivel.junta, nivel.niebla, lej, 0.5), at)
        grosor = max(1, int(m.h * dx * 0.02))
        y_pared = m.techo_en(dx)
        y_cima = m.techo_en(dx * 0.82)
        cx = round(m.centro(dx))
        if j == CRI_TRAMOS - 1:
            cri_dovelas(lz, round(m.izq(dx)), int(y_pared), cx, int(y_cima),
                        round(m.der(dx)), int(y_pared), grosor, color, nivel, luz)
            continue
        _linea(lz, round(m.izq(dx)), int(y_pared), cx, int(y_cima), grosor, color)
        _linea(lz, cx, int(y_cima), round(m.der(dx)), int(y_pared), grosor, color)


def cri_dovelas(lz, x0, y0, cx, yc, x1, y1, grosor, color, nivel, luz) -> None:
    # El arco mas cercano se construye con dovelas: bezier que pasa por la
    # clave, piedras con tono propio y junta perpendicular entre dovelas.
    cy = 2.0 * yc - (y0 + y1) * 0.5
    mortero = con_alfa(iluminar(nivel.junta, luz * 0.55), 0.72)
    for i in range(9):
        t0 = i / 9.0
        t1 = (i + 1) / 9.0
        ax = _bezier(x0, cx, x1, t0)
        ay = _bezier(y0, cy, y1, t0)
        bx = _bezier(x0, cx, x1, t1)
        by = _bezier(y0, cy, y1, t1)
        xa = round(min(ax, bx) - grosor)
        xb = round(max(ax, bx) + grosor)
        ya = round(min(ay, by) - grosor)
        yb = round(max(ay, by) + grosor)
        desvio = pseudo(431 + i * 11) * 0.16 - 0.08
        piedra = iluminar(mezclar(color, nivel.pared_baja, 0.22 + desvio),
                          min(1.0, luz * 0.85 + 0.20))
        lz.fill(xa, ya, xb, yb, con_alfa(piedra, 0.94))
        if i < 8:
            tx = _bezier(x0, cx, x1, t1)
            ty = _bezier(y0, cy, y1, t1)
            u = 1.0 - t1
            tg_x = 2.0 * u * (cx - x0) + 2.0 * t1 * (x1 - cx)
            tg_y = 2.0 * u * (cy - y0) + 2.0 * t1 * (y1 - cy)
            largo = max(1.0, math.hypot(tg_x, tg_y))
            nx, ny = -tg_y / largo, tg_x / largo
            ext = max(2, grosor + 1)
            jx, jy = round(tx + nx * ext), round(ty + ny * ext)
            kx, ky = round(tx - nx * ext), round(ty - ny * ext)
            lz.fill(min(jx, kx), min(jy, ky), max(jx, kx) + 1, max(jy, ky) + 1, mortero)


def _bezier(p0: float, control: float, p1: float, t: float) -> float:
    u = 1.0 - t
    return u * u * p0 + 2.0 * u * t * control + t * t * p1


def cri_sillares(lz, m, nivel, luz) -> None:
    for x in range(0, m.ancho, PASO):
        dx = m.dx(x + PASO * 0.5)
        if dx <= 1.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        y0, y1 = m.techo_en(dx), m.suelo_en(dx)
        hiladas = 6
        for k in range(1, hiladas):
            f = k / hiladas
            y = int(y0 + (y1 - y0) * f)
            desvio = pseudo(int(dx * 131.0) + k * 37 + x // 8) * 0.10 - 0.05
            lz.fill(x, y, x + PASO, y + 1,
                    con_alfa(iluminar(nivel.junta, at * (0.9 + desvio)), 0.30 * lej + 0.10))
    t_juntas(lz, m, nivel, luz, CRI_TRAMOS, 1.0, 0.30)


def cri_nicho_lateral(lz, m, nivel, luz) -> None:
    """Nicho ciego lateral con profundidad fuera del eje de la hoja."""
    dx = 1.55
    centro = m.der(dx * 0.72)
    ancho = m.w * dx * 0.12
    alto = m.h * dx * 0.46
    y1 = m.suelo_en(dx * 0.90) - m.h * dx * 0.12
    y0 = y1 - alto
    x0, x1 = round(centro - ancho), round(centro + ancho)
    iy0, iy1 = round(y0 + alto * 0.15), round(y1)
    lz.fill(x0 - 2, round(y0), x1 + 2, iy1 + 2,
            iluminar(mezclar(nivel.junta, nivel.pared_alta, 0.35), luz * 0.56))
    lz.fill(x0, iy0, x1, iy1, con_alfa(VANO, 0.78))
    lz.fill(x0 + 2, iy0 + 2, x1 - 2, iy1,
            con_alfa(mezclar(VANO, nivel.pared_baja, 0.25), 0.72))
    lz.fill(x0 - 2, round(y0), x1 + 2, round(y0 + 3),
            iluminar(nivel.junta, luz * 0.62))
    lz.fill(x0 - 2, iy1 - 2, x0 + 1, iy1 + 1, con_alfa(nivel.pared_baja, 0.70))
    lz.fill(x1 - 1, iy1 - 2, x1 + 2, iy1 + 1, con_alfa(nivel.pared_baja, 0.70))


def cri_marca_peregrinacion(lz, m, nivel, luz) -> None:
    """Marca de peregrinacion gastada sobre un sillar."""
    dx = 1.38
    x = round(m.izq(dx * 0.70) + m.w * dx * 0.035)
    y = round(m.techo_en(dx * 0.42) + m.h * dx * 0.50)
    color = con_alfa(iluminar(nivel.junta, luz * 0.58), 0.56)
    _linea(lz, x - 7, y, x + 8, y - 2, 2, color)
    _linea(lz, x, y - 8, x - 1, y + 8, 2, color)
    _linea(lz, x - 8, y + 8, x - 3, y + 12, 1, con_alfa(color, 0.52))
    lz.fill(x + 9, y - 1, x + 12, y + 1, con_alfa(nivel.pared_baja, 0.44))


def cri_cera_candil(lz, m, nivel, luz) -> None:
    """Cera acumulada bajo el candil."""
    dx = 1.72
    cx = round(m.centro(dx))
    y = round(m.suelo_en(dx))
    cera = con_alfa(iluminar(nivel.pared_alta, luz * 0.74), 0.62)
    lz.fill(cx - 13, y - 2, cx - 5, y + 1, cera)
    lz.fill(cx - 5, y - 4, cx + 6, y + 1, cera)
    lz.fill(cx + 6, y - 2, cx + 16, y + 1, cera)
    lz.fill(cx - 2, y - 7, cx + 2, y - 2, con_alfa(cera, 0.48))


def cri_columnas(lz, m, nivel, luz) -> None:
    for j in range(3, CRI_TRAMOS + 1, 3):
        dx = profundidad(j, CRI_TRAMOS)
        if dx > 5.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        ancho = max(2.0, m.w * dx * 0.055)
        y_techo = m.techo_en(dx * 0.92)
        y_suelo = m.suelo_en(dx)
        for signo in (-1, 1):
            x = m.lado(signo, dx * 0.80)
            if x < -ancho * 2 or x > m.ancho + ancho * 2:
                continue
            frente = iluminar(velar(nivel.pared_alta, nivel.niebla, lej, 0.45), at * 0.92)
            costado = iluminar(velar(nivel.pared_baja, nivel.niebla, lej, 0.50), at * 0.55)
            corte = ancho * 0.42 * (1 if signo < 0 else -1)
            lz.fill(int(x - ancho), int(y_techo), int(x + corte), int(y_suelo),
                    frente if signo < 0 else costado)
            lz.fill(int(x + corte), int(y_techo), int(x + ancho), int(y_suelo),
                    costado if signo < 0 else frente)
            alto = m.h * dx * 0.06
            cap = iluminar(velar(nivel.junta, nivel.niebla, lej, 0.4), at * 0.8)
            lz.fill(int(x - ancho * 1.3), int(y_suelo - alto), int(x + ancho * 1.3), int(y_suelo), cap)
            lz.fill(int(x - ancho * 1.3), int(y_techo), int(x + ancho * 1.3), int(y_techo + alto), cap)


def cri_estandartes(lz, m, nivel, luz, tiempo) -> None:
    for j in range(2, CRI_TRAMOS + 1, 3):
        if pseudo(700 + j) > 0.6:
            continue
        dx = profundidad(j, CRI_TRAMOS)
        if dx > 5.5:
            continue
        signo = -1 if pseudo(710 + j) < 0.5 else 1
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        x = m.lado(signo, dx * 0.94)
        if x < -20 or x > m.ancho + 20:
            continue
        ancho = max(3.0, m.w * dx * 0.05)
        y_top = m.techo_en(dx * 0.60)
        alto = m.h * dx * 0.55
        onda = math.sin(tiempo * 0.6 + j) * ancho * 0.15
        # La cuerda: del techo al asta. Sin esto la tela parece colgar de la
        # nada, y una tela colgada de la nada es un rectangulo flotante.
        y_techo = m.techo_en(dx * 0.92)
        lz.fill(int(x), int(y_techo), int(x) + 1, int(y_top),
                con_alfa(iluminar(nivel.junta, at * 0.55), 0.50))
        lz.fill(int(x - ancho * 0.5), int(y_top) - 1, int(x + ancho * 0.5), int(y_top),
                con_alfa(iluminar(nivel.junta, at * 0.70), 0.80))
        tela = iluminar(velar(mezclar(nivel.pared_baja, nivel.junta, 0.35), nivel.niebla, lej, 0.4), at * 0.9)
        for k in range(8):
            f = k / 8.0
            ox = onda * f
            xx = x - ancho * 0.5 + ox
            lz.fill(int(xx), int(y_top + alto * f), int(xx + ancho), int(y_top + alto * (f + 0.14)),
                    con_alfa(tela, 0.85))
        lz.fill(int(x - ancho * 0.5), int(y_top), int(x + ancho * 0.5), int(y_top + max(1, alto * 0.06)),
                con_alfa(iluminar(nivel.luz, at), 0.55))
        ey = y_top + alto * 0.4
        lz.fill(int(x - ancho * 0.18), int(ey), int(x + ancho * 0.18), int(ey + alto * 0.14),
                con_alfa(iluminar(nivel.luz, at * 0.8), 0.30))


def cri_antorchas(lz, m, nivel, luz, tiempo) -> None:
    for j in range(2, CRI_TRAMOS + 1, 2):
        dx = profundidad(j, CRI_TRAMOS)
        if dx > 6.5:
            continue
        signo = 1 if j % 4 == 0 else -1
        lej = limitar(1.0 / dx, 0.0, 1.0)
        x = m.lado(signo, dx * 0.90)
        if x < -10 or x > m.ancho + 10:
            continue
        y = m.techo_en(dx * 0.42)
        llama = _fuego(tiempo, j * 1.7)
        at = atenuar(luz, lej) * llama
        medio = max(1.5, m.w * dx * 0.02)
        lz.fill(int(x - medio * 0.3), int(y), int(x + medio * 0.3), int(y + medio * 2.0),
                con_alfa(iluminar(nivel.junta, luz * 0.5), 0.85))
        for k in range(5, 0, -1):
            t = k / 5.0
            ex = medio * (1.0 + t * 5.0)
            ey = medio * (1.0 + t * 6.0)
            lz.fill(int(x - ex), int(y - ey), int(x + ex), int(y + ey),
                    con_alfa(nivel.luz, 0.06 * at * (1.0 - t * 0.5)))
        lz.fill(int(x - medio * 0.6), int(y - medio * 0.8), int(x + medio * 0.6), int(y + medio * 0.6),
                con_alfa(iluminar(nivel.luz, min(1.0, at * 1.4)), 0.95))
        lz.fill(int(x - medio * 0.25), int(y - medio * 1.4), int(x + medio * 0.25), int(y - medio * 0.6),
                con_alfa(iluminar(0xFFFFF3D8, at), 0.75))


def cri_candil(lz, m, nivel, luz, tiempo, pulso) -> None:
    dx = 1.7
    cx = m.centro(dx)
    cy = m.alto * 0.30
    radio = max(10.0, m.ancho * 0.075)
    lej = limitar(1.0 / dx, 0.0, 1.0)
    at = atenuar(luz, lej)
    mece = math.sin(tiempo * 0.5) * radio * 0.06
    cx += mece
    yy = 0
    while yy < cy - radio * 0.4:
        m2 = math.sin(tiempo * 0.5) * radio * 0.06 * yy / max(1.0, cy)
        lz.fill(int(m.centro(dx) + m2 - 1), int(yy), int(m.centro(dx) + m2 + 1), int(yy) + 2,
                con_alfa(iluminar(nivel.junta, at * 0.7), 0.8))
        yy += 4
    madera = iluminar(mezclar(nivel.junta, nivel.pared_alta, 0.30), at * 0.9)
    groso = max(2, int(radio * 0.16))
    seg = 24
    pts = []
    for i in range(seg):
        a = 2 * math.pi * i / seg
        pts.append((int(cx + math.cos(a) * radio), int(cy + math.sin(a) * radio * 0.42)))
    for i in range(seg):
        n = (i + 1) % seg
        _linea(lz, pts[i][0], pts[i][1], pts[n][0], pts[n][1], groso, madera)
    for i in range(seg):
        a = 2 * math.pi * i / seg
        an = 2 * math.pi * ((i + 1) % seg) / seg
        _linea(lz, int(cx + math.cos(a) * radio * 0.55), int(cy + math.sin(a) * radio * 0.55 * 0.42),
               int(cx + math.cos(an) * radio * 0.55), int(cy + math.sin(an) * radio * 0.55 * 0.42),
               max(1, groso // 2), madera)
    for i in range(8):
        a = 2 * math.pi * i / 8
        _linea(lz, int(cx), int(cy), int(cx + math.cos(a) * radio), int(cy + math.sin(a) * radio * 0.42),
               max(1, groso // 2), madera)
    lz.fill(int(cx - radio * 0.14), int(cy - radio * 0.14 * 0.42 - 2),
            int(cx + radio * 0.14), int(cy + radio * 0.14 * 0.42 + 2), iluminar(nivel.junta, at))
    velas = 8
    for i in range(velas):
        a = 2 * math.pi * i / velas + 0.2
        vx = int(cx + math.cos(a) * radio)
        vy_base = int(cy + math.sin(a) * radio * 0.42)
        llama = _fuego(tiempo, i * 2.3 + 5.0)
        av = at * llama
        lz.fill(vx - 1, vy_base - int(radio * 0.18), vx + 1, vy_base,
                con_alfa(iluminar(nivel.pared_alta, at * 0.7), 0.85))
        vy = vy_base - int(radio * 0.18)
        for k in range(3, 0, -1):
            t = k / 3.0
            e = radio * 0.10 * (1.0 + t * 3.5)
            lz.fill(int(vx - e), int(vy - e), int(vx + e), int(vy + e),
                    con_alfa(nivel.luz, 0.10 * av * (1.0 - t * 0.5)))
        lz.fill(vx - 1, vy - 2, vx + 2, vy + 1,
                con_alfa(iluminar(0xFFFFF3D8, min(1.0, av * 1.4)), 0.95))
    for k in range(4, 0, -1):
        t = k / 4.0
        e = radio * (1.2 + t * 1.8)
        lz.fill(int(cx - e), int(cy - e * 0.5), int(cx + e), int(cy + e * 0.42),
                con_alfa(nivel.luz, 0.03 * at * pulso * (1.0 - t * 0.5)))


def cri_runas(lz, m, nivel, luz, tiempo) -> None:
    dx_c = 1.9
    cx = m.centro(dx_c)
    cy = m.suelo_en(dx_c)
    lej = limitar(1.0 / dx_c, 0.0, 1.0)
    latido = 0.5 + 0.5 * math.sin(tiempo * 0.8)
    base = (0.12 + 0.16 * latido) * luz
    color = mezclar(nivel.luz, nivel.pared_alta, 0.35)
    rayos = 12
    largo = m.w * dx_c * 0.5
    for i in range(rayos):
        a = 2 * math.pi * i / rayos
        ex = cx + math.cos(a) * largo
        ey = cy + math.sin(a) * largo * 0.32
        _linea(lz, int(cx), int(cy), int(ex), int(ey), 1,
               con_alfa(iluminar(color, luz), base * (0.6 + 0.4 * lej)))
    for rr in (0.45, 0.85):
        seg = 20
        for i in range(seg):
            a0 = 2 * math.pi * i / seg
            a1 = 2 * math.pi * (i + 1) / seg
            _linea(lz, int(cx + math.cos(a0) * largo * rr), int(cy + math.sin(a0) * largo * rr * 0.32),
                   int(cx + math.cos(a1) * largo * rr), int(cy + math.sin(a1) * largo * rr * 0.32), 1,
                   con_alfa(iluminar(color, luz), base * 0.8))


def pp_cripta(lz, m, nivel, luz, tiempo) -> None:
    balance = desvio(tiempo, 2.4, 0.08)
    llama = 1.0 + 0.05 * math.sin(tiempo * 12.0) + 0.03 * math.sin(tiempo * 7.1 + 1.0)
    tapa_y = int(lz.alto * 0.82 + balance)
    x0 = int(lz.ancho * 0.14 + balance * 1.4)
    x1 = int(lz.ancho * 0.92 + balance * 1.4)
    frente = mezclar(nivel.pared_baja, 0x000000, 0.55)
    tapa = mezclar(nivel.suelo, nivel.pared_alta, 0.30)
    lz.fill_gradient(x0, tapa_y, x1, lz.alto,
                     iluminar(frente, (0.30 + 0.20 * luz) * llama),
                     iluminar(frente, 0.10 + 0.08 * luz))
    espesor = max(5, int(lz.alto * 0.035))
    lz.fill_gradient(x0, tapa_y - espesor, x1, tapa_y,
                     iluminar(tapa, min(1.0, (0.60 + 0.34 * luz) * llama)),
                     iluminar(tapa, 0.42 + 0.24 * luz))
    lz.fill(x0, tapa_y - espesor, x1, tapa_y - espesor + 2,
            con_alfa(iluminar(nivel.luz, min(1.0, luz * llama)), 0.20 + 0.24 * luz))
    for k in range(1, 4):
        vy = tapa_y - espesor + k * espesor // 4
        lz.fill(x0, vy, x1, vy + 1, con_alfa(0x000000, 0.10))
    # Candelabro de mesa: un pie, un brazo corto con sus dos copas y dos velas
    # altas. Antes el brazo cruzaba el pie a mitad de altura y las velas eran
    # dos puntos: a distancia se leia como una cruz flotante en la pared.
    velax = int(lz.ancho * 0.30 + balance * 1.4)
    alto = int(lz.alto * 0.10)
    hierro = iluminar(mezclar(nivel.junta, 0x000000, 0.25), 0.55 + 0.25 * luz)
    pie_y = tapa_y - espesor - alto
    lz.fill(velax - 1, pie_y, velax + 2, tapa_y - espesor, con_alfa(hierro, 0.92))
    # Brazo a la altura de la copa, con caida en el medio (silueta de candelabro).
    brazo_y = pie_y + int(alto * 0.12)
    lz.fill(velax - int(lz.ancho * 0.035), brazo_y, velax + int(lz.ancho * 0.035), brazo_y + 2,
            con_alfa(hierro, 0.92))
    lz.fill(velax - int(lz.ancho * 0.012), brazo_y + int(alto * 0.06),
            velax + int(lz.ancho * 0.012), brazo_y + int(alto * 0.06) + 2,
            con_alfa(hierro, 0.85))
    for s in (-1, 1):
        vx = velax + s * int(lz.ancho * 0.035)
        vy = brazo_y
        ll = 1.0 + 0.10 * math.sin(tiempo * 13.0 + s)
        # Copa y cuerpo de vela.
        lz.fill(vx - 2, vy, vx + 2, vy + 2, con_alfa(hierro, 0.92))
        vela_h = int(alto * 0.26)
        lz.fill(vx - 1, vy - vela_h, vx + 2, vy,
                con_alfa(iluminar(mezclar(nivel.pared_alta, 0xFFF0DC, 0.35), 0.75 * luz), 0.92))
        # Llama: nucleo y punta, con derrame corto.
        for k in range(3, 0, -1):
            t = k / 3.0
            e = lz.ancho * 0.010 * (1.0 + t * 2.2)
            lz.fill(int(vx - e), int(vy - vela_h - e), int(vx + e), int(vy - vela_h + e * 0.6),
                    con_alfa(nivel.luz, 0.07 * luz * ll * (1.0 - t * 0.5)))
        lz.fill(vx - 1, vy - vela_h - int(alto * 0.14), vx + 1, vy - vela_h,
                con_alfa(iluminar(0xFFFFF3D8, min(1.0, luz * ll * 1.4)), 0.95))
    # Jarra.
    jx = int(lz.ancho * 0.66 + balance * 1.4)
    jw = int(lz.ancho * 0.05)
    jh = int(lz.alto * 0.07)
    lz.fill(jx, tapa_y - espesor - jh, jx + jw, tapa_y - espesor,
            con_alfa(iluminar(nivel.junta, 0.50 + 0.30 * luz), 0.90))
    lz.fill(jx + jw, tapa_y - espesor - int(jh * 0.6), jx + jw + int(jw * 0.3),
            tapa_y - espesor - int(jh * 0.25), con_alfa(iluminar(nivel.junta, 0.50 + 0.30 * luz), 0.90))
    lz.fill(jx, tapa_y - espesor - jh, jx + jw, tapa_y - espesor - jh + 2,
            con_alfa(iluminar(nivel.luz, luz * llama), 0.30))


# --------------------------------------------------------------------------
# Nivel 5 - La biblioteca: espejo de planta/Biblioteca.java
# --------------------------------------------------------------------------
BIB_TRAMOS = 15
BIB_HILERA = 0.66


def biblioteca(lz, m, nivel, luz, tiempo) -> None:
    t_fondo(lz, m, nivel, luz, mezclar(nivel.pared_baja, nivel.pared_alta, 0.30), 1.7)
    bib_ventanal(lz, m, nivel, luz)
    t_plano(lz, m, True, nivel.techo, mezclar(nivel.techo, nivel.niebla, 0.40),
            nivel.niebla, luz, 0.52)
    t_transversales(lz, m, True, nivel.techo_junta, nivel.niebla, luz, BIB_TRAMOS, 0.30)
    t_plano(lz, m, False, nivel.suelo, nivel.suelo_lejos, nivel.niebla, luz, 0.55)
    t_transversales(lz, m, False, nivel.suelo_junta, nivel.niebla, luz, BIB_TRAMOS, 0.42)
    bib_alfombra(lz, m, nivel, luz)
    t_paredes(lz, m, nivel, luz)
    t_juntas(lz, m, nivel, luz, BIB_TRAMOS, 1.0, 0.30)
    t_manchas(lz, m, nivel, luz, BIB_TRAMOS)
    bib_estanterias(lz, m, nivel, luz)
    bib_arco_acceso(lz, m, nivel, luz)
    bib_paginas_dobladas(lz, m, nivel, luz)
    bib_polvo_estantes(lz, m, nivel, luz)
    bib_condensacion_ventanal(lz, m, nivel, luz)
    bib_lamparas(lz, m, nivel, luz, tiempo)


def bib_arco_acceso(lz, m, nivel, luz) -> None:
    # Arco de acceso entre estantes: la hilera derecha se interrumpe con un
    # pasaje; al otro lado hay oscuridad y la biblioteca sigue.
    dx = 1.52
    x = m.lado(1.0, dx * BIB_HILERA)
    if x < -20.0 or x > m.ancho + 20.0:
        return
    lej = limitar(1.0 / dx, 0.0, 1.0)
    at = atenuar(luz, lej) * 0.9
    y_suelo = m.suelo_en(dx)
    y_techo = m.techo_en(dx * 0.86)
    ancho = m.w * dx * 0.17
    alto = m.h * dx * 0.30
    x0, x1 = round(x - ancho * 0.5), round(x + ancho * 0.5)
    y_base = round(y_suelo)
    y_top = round(y_suelo - alto)
    radio = max(2, (x1 - x0) // 2)
    if y_top - radio <= y_techo:
        return
    interior = iluminar(velar(VANO, nivel.niebla, lej, 0.42), at * 0.55)
    lz.fill(x0, y_top, x1, y_base, interior)
    for d in range(0, radio + 1, 2):
        f = 1.0 - d / float(radio)
        hw = int(radio * math.sqrt(max(0.0, 1.0 - f * f)))
        lz.fill(x0 + hw, y_top - d, x1 - hw, y_top - d + 2, interior)
    filo = con_alfa(iluminar(nivel.pared_alta, at), 0.55)
    lz.fill(x0 - 2, y_top - radio - 2, x0, y_base, filo)
    lz.fill(x1, y_top - radio - 2, x1 + 2, y_base, filo)


def bib_ventanal(lz, m, nivel, luz) -> None:
    suelo = m.suelo_en(1.0)
    alto = m.h * 1.35
    x0, x1 = round(m.izq(0.34)), round(m.der(0.34))
    y0 = round(suelo - alto)
    y1 = round(suelo - m.h * 0.25)
    lz.fill_gradient(x0, y0, x1, y1,
                     iluminar(mezclar(nivel.niebla, 0xFFB8C0C8, 0.45), luz * 0.75),
                     iluminar(nivel.niebla, luz * 0.45))
    cy = (y0 + y1) // 2
    marco = iluminar(nivel.junta, luz * 0.6)
    for k in range(1, 4):
        vx = x0 + (x1 - x0) * k // 4
        lz.fill(vx, y0, vx + 1, y1, marco)
    lz.fill(x0, cy, x1, cy + 1, marco)
    lz.fill(x0 - 2, y0 - 2, x1 + 2, y0, marco)


def bib_alfombra(lz, m, nivel, luz) -> None:
    for y in range(round(m.suelo_en(1.0)), m.alto, PASO):
        dy = m.dy(y + PASO * 0.5)
        if dy <= 1.0:
            continue
        lej = limitar(1.0 / dy, 0.0, 1.0)
        medio = m.w * dy * 0.30
        color = mezclar(nivel.suelo_junta, nivel.luz, 0.10)
        lz.fill(int(m.centro(dy) - medio), y, int(m.centro(dy) + medio), y + PASO,
                con_alfa(iluminar(color, atenuar(luz, lej)), 0.20))


def bib_estanterias(lz, m, nivel, luz) -> None:
    for x in range(0, m.ancho, PASO):
        dx = m.dx(x + PASO * 0.5)
        if dx <= 1.05 or dx > 6.5:
            continue
        centro = x + PASO * 0.5
        signo = -1 if centro < m.fx else 1
        x_hilera = m.lado(signo, dx * BIB_HILERA)
        if abs(centro - x_hilera) > m.w * dx * 0.5:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        y_techo = m.techo_en(dx * 0.86)
        y_suelo = m.suelo_en(dx)
        if y_suelo - y_techo < 4:
            continue
        madera = iluminar(velar(nivel.pared_baja, nivel.niebla, lej, 0.45), at * 0.75)
        lz.fill(x, int(y_techo), x + PASO, int(y_suelo), madera)
        baldas = 6
        for b in range(baldas):
            f0 = b / baldas
            f1 = (b + 1) / baldas
            yb0 = int(y_techo + (y_suelo - y_techo) * f0) + 1
            yb1 = int(y_techo + (y_suelo - y_techo) * f1) - 1
            if yb1 <= yb0:
                continue
            semilla = pseudo(int(dx * 53.0) + b * 17 + (signo + 1) * 91)
            lomo = mezclar(mezclar(nivel.pared_alta, nivel.junta, 0.35), nivel.luz, 0.12 + semilla * 0.5)
            lz.fill(x, yb0, x + PASO, yb1,
                    iluminar(velar(lomo, nivel.niebla, lej, 0.4), at * (0.7 + 0.3 * semilla)))
            lz.fill(x, yb1, x + PASO, yb1 + 1, con_alfa(iluminar(nivel.junta, at), 0.6))


def bib_paginas_dobladas(lz, m, nivel, luz) -> None:
    """Paginas dobladas que sobresalen de los estantes cercanos."""
    for i, dx in enumerate((1.35, 1.75)):
        signo = -1 if i == 0 else 1
        x = m.lado(signo, dx * (BIB_HILERA - 0.04))
        y = m.techo_en(dx * 0.86) + m.h * dx * (0.30 + i * 0.18)
        ancho = max(4, round(m.w * dx * 0.055))
        alto = max(5, round(m.h * dx * 0.11))
        papel = iluminar(mezclar(nivel.pared_alta, nivel.techo, 0.35), luz * 0.72)
        x0, x1 = round(x - signo * ancho * 0.30), round(x + signo * ancho * 0.70)
        y0, y1 = round(y), round(y + alto)
        lz.fill(min(x0, x1), y0, max(x0, x1), y1, papel)
        lz.fill(min(x0, x1), y0, max(x0, x1), y0 + 1,
                con_alfa(iluminar(nivel.luz, luz), 0.30))
        lz.fill(min(x0, x1) + ancho // 3, y0 + alto // 2,
                min(x0, x1) + ancho // 3 + 1, y1, con_alfa(nivel.junta, 0.45))


def bib_polvo_estantes(lz, m, nivel, luz) -> None:
    """Polvo pegado en recovecos de balda, no una capa uniforme."""
    for i in range(5):
        dx = 1.35 + i * 0.42
        signo = -1 if i % 2 == 0 else 1
        x = round(m.lado(signo, dx * (BIB_HILERA + 0.02)))
        y = round(m.techo_en(dx * 0.86) + m.h * dx * (0.62 + (i % 3) * 0.10))
        ancho = max(3, round(m.w * dx * 0.06))
        lz.fill(x - ancho // 2, y, x + ancho,
                y + max(1, round(m.h * dx * 0.012)),
                con_alfa(mezclar(nivel.junta, nivel.pared_alta, 0.45), 0.28 * luz))


def bib_condensacion_ventanal(lz, m, nivel, luz) -> None:
    """Condensacion minima sobre el ventanal, fuera de los libros."""
    suelo = m.suelo_en(1.0)
    alto = m.h * 1.35
    x0 = round(m.izq(0.34))
    y0 = round(suelo - alto)
    y1 = round(suelo - m.h * 0.25)
    ancho = max(2, (round(m.der(0.34)) - x0) // 4)
    for i in range(4):
        x = x0 + ancho * (i + 1)
        inicio = y0 + (y1 - y0) * (i + 1) // 7
        largo = max(3, (y1 - y0) // (8 + i))
        lz.fill(x, inicio, x + max(1, ancho // 16), inicio + largo,
                con_alfa(iluminar(nivel.techo, luz), 0.16))
        lz.fill(x - 1, inicio + largo, x + max(2, ancho // 12), inicio + largo + 1,
                con_alfa(nivel.luz, 0.20))


def bib_lamparas(lz, m, nivel, luz, tiempo) -> None:
    for j in range(3, BIB_TRAMOS + 1, 2):
        dx = profundidad(j, BIB_TRAMOS)
        if dx > 6.0:
            continue
        signo = -1 if j % 4 == 1 else 1
        lej = limitar(1.0 / dx, 0.0, 1.0)
        x = m.lado(signo, dx * (BIB_HILERA - 0.10))
        if x < -8 or x > m.ancho + 8:
            continue
        y = m.suelo_en(dx * 0.60)
        titil = 0.9 + 0.1 * math.sin(tiempo * 5.0 + j * 1.3)
        at = atenuar(luz, lej) * titil
        medio = max(1.5, m.w * dx * 0.028)
        for k in range(4, 0, -1):
            t = k / 4.0
            e = medio * (1.0 + t * 3.2)
            lz.fill(int(x - e), int(y - e), int(x + e), int(y + e),
                    con_alfa(nivel.luz, 0.06 * at * (1.0 - t * 0.5)))
        verde = mezclar(nivel.luz, 0xFF2E5A3A, 0.55)
        lz.fill(int(x - medio), int(y - medio * 1.4), int(x + medio), int(y - medio * 0.4),
                iluminar(verde, min(1.0, at * 1.1)))
        lz.fill(int(x - medio * 0.5), int(y - medio * 0.4), int(x + medio * 0.5), int(y + medio * 0.3),
                con_alfa(iluminar(0xFFFFF0C0, min(1.0, at * 1.3)), 0.9))
        lz.fill(int(x - 1), int(y + medio * 0.3), int(x + 1), int(y + medio * 1.2),
                con_alfa(iluminar(nivel.junta, at), 0.8))


def pp_biblioteca(lz, m, nivel, luz, tiempo) -> None:
    balance = desvio(tiempo, 2.0, 0.07)
    tapa_y = int(lz.alto * 0.84 + balance)
    x0 = int(lz.ancho * 0.10 + balance * 1.3)
    x1 = int(lz.ancho * 0.90 + balance * 1.3)
    frente = mezclar(nivel.pared_baja, 0x000000, 0.55)
    tapa = mezclar(nivel.suelo, nivel.junta, 0.30)
    lz.fill_gradient(x0, tapa_y, x1, lz.alto,
                     iluminar(frente, 0.28 + 0.18 * luz), iluminar(frente, 0.10 + 0.08 * luz))
    espesor = max(5, int(lz.alto * 0.032))
    lz.fill_gradient(x0, tapa_y - espesor, x1, tapa_y,
                     iluminar(tapa, 0.55 + 0.30 * luz), iluminar(tapa, 0.40 + 0.22 * luz))
    lz.fill(x0, tapa_y - espesor, x1, tapa_y - espesor + 2, con_alfa(0xFF2E5A3A, 0.10 + 0.16 * luz))
    lx = int(lz.ancho * 0.40 + balance * 1.3)
    lw = int(lz.ancho * 0.20)
    lh = max(4, int(lz.alto * 0.03))
    pagina = iluminar(mezclar(nivel.pared_alta, 0xFFFFFFFF, 0.25), 0.5 + 0.4 * luz)
    lz.fill(lx, tapa_y - espesor - lh, lx + lw // 2, tapa_y - espesor, pagina)
    lz.fill(lx + lw // 2, tapa_y - espesor - lh, lx + lw, tapa_y - espesor, pagina)
    lz.fill(lx + lw // 2 - 1, tapa_y - espesor - lh - 2, lx + lw // 2 + 1, tapa_y - espesor,
            con_alfa(iluminar(nivel.junta, luz), 0.8))
    px = int(lz.ancho * 0.70 + balance * 1.3)
    py = tapa_y - espesor
    ph = int(lz.alto * 0.12)
    titil = 0.9 + 0.1 * math.sin(tiempo * 5.0)
    for k in range(4, 0, -1):
        t = k / 4.0
        e = lz.ancho * 0.02 * (1.0 + t * 2.5)
        lz.fill(int(px - e), int(py - ph * 0.6 - e * 0.5), int(px + e), py,
                con_alfa(nivel.luz, 0.06 * luz * titil * (1.0 - t * 0.5)))
    lz.fill(px - 1, py - ph, px + 1, py, con_alfa(iluminar(nivel.junta, luz), 0.85))
    verde = mezclar(nivel.luz, 0xFF2E5A3A, 0.55)
    lz.fill(px - int(lz.ancho * 0.03), py - ph - 3, px + int(lz.ancho * 0.03), py - ph + 4,
            iluminar(verde, min(1.0, luz * titil * 1.1)))


# --------------------------------------------------------------------------
# Nivel 6 - El invernadero: espejo de planta/Invernadero.java
# --------------------------------------------------------------------------
INV_TRAMOS = 14


def invernadero(lz, m, nivel, luz, tiempo) -> None:
    t_fondo(lz, m, nivel, luz, mezclar(nivel.pared_baja, nivel.techo, 0.55), 1.9)
    inv_porton(lz, m, nivel, luz)
    inv_puerta_lateral(lz, m, nivel, luz)
    inv_cristalera(lz, m, nivel, luz)
    inv_panel_roto(lz, m, nivel, luz)
    t_transversales(lz, m, True, nivel.techo_junta, nivel.niebla, luz, INV_TRAMOS, 0.34)
    t_plano(lz, m, False, nivel.suelo, nivel.suelo_lejos, nivel.niebla, luz, 0.52)
    t_transversales(lz, m, False, nivel.suelo_junta, nivel.niebla, luz, INV_TRAMOS, 0.40)
    inv_sendero(lz, m, nivel, luz)
    inv_canaleta_deposito(lz, m, nivel, luz)
    t_paredes(lz, m, nivel, luz)
    t_juntas(lz, m, nivel, luz, INV_TRAMOS, 1.0, 0.28)
    t_manchas(lz, m, nivel, luz, INV_TRAMOS)
    inv_bancos(lz, m, nivel, luz)
    inv_pasarela(lz, m, nivel, luz)
    inv_vegetacion(lz, m, nivel, luz, tiempo)
    inv_haces(lz, m, nivel, luz, tiempo)
    inv_vaho(lz, m, nivel, luz, tiempo)


def inv_porton(lz, m, nivel, luz) -> None:
    """Porton de vidrio de dos hojas: travesano, mullion central y manijas.

    El vidrio es verde y hacia abajo se ve la vegetacion del otro lado, para
    que el conjunto no se lea como una heladera (un panel blanco con reticula).
    """
    suelo = m.suelo_en(1.0)
    alto = m.h * 1.30
    x0, x1 = round(m.izq(0.40)), round(m.der(0.40))
    y0, y1 = round(suelo - alto), round(suelo)
    w = x1 - x0
    h = y1 - y0
    an = m.ancho
    # Fondo de vidrio: de la cumbrera clara al verde profundo del zocalo.
    lz.fill_gradient(x0, y0, x1, y1,
                     iluminar(mezclar(nivel.techo, 0xFFE9F2DC, 0.12), luz * 0.72),
                     iluminar(mezclar(nivel.pared_baja, 0xFF2A3620, 0.25), luz * 0.40))
    # Vegetacion al otro lado del vidrio: siluetas verdes en el tercio inferior.
    for i in range(9):
        fx = x0 + (w * (0.06 + (i * 37 % 89) / 89.0 * 0.88))
        base_y = y1 - h * 0.02
        top = y1 - h * (0.12 + (i * 53 % 71) / 71.0 * 0.14)
        lz.fill(int(fx - w * 0.035), int(top), int(fx + w * 0.045), int(base_y),
                con_alfa(iluminar(mezclar(0xFF2E4020, 0xFF5A7A34, (i * 29 % 17) / 17.0), luz * 0.35), 0.55))
    # Marco perimetral (el dintel de piedra que abraza el porton).
    marco = iluminar(mezclar(nivel.junta, 0xFF1A2412, 0.45), luz * 0.42)
    lz.fill(x0, y0, x1, y0 + 3, marco)
    lz.fill(x0, y1 - 3, x1, y1, marco)
    lz.fill(x0, y0, x0 + 3, y1, marco)
    lz.fill(x1 - 3, y0, x1, y1, marco)
    # Travesano superior: una fila de paños chicos bajo el dintel.
    trans_y = y0 + int(h * 0.16)
    lz.fill(x0, trans_y, x1, trans_y + 3, marco)
    for k in range(1, 6):
        lz.fill(x0 + w * k // 6, y0 + 3, x0 + w * k // 6 + 1, trans_y, marco)
    # Mullion central: separa las dos hojas.
    lz.fill(x0 + w // 2 - 1, trans_y + 3, x0 + w // 2 + 2, y1 - 3, marco)
    # Largueros de cada hoja y rieles horizontales (tres paños por hoja).
    for k in (1, 5):
        lz.fill(x0 + w * k // 6, trans_y + 3, x0 + w * k // 6 + 1, y1 - 3, marco)
    for k in (1, 2):
        yy = trans_y + 3 + (y1 - 3 - trans_y - 3) * k // 3
        lz.fill(x0 + 3, yy, x1 - 3, yy + 2, marco)
    # Manijas: dos tiradores verticales junto al mullion.
    for s in (-1, 1):
        hx = x0 + w // 2 + s * max(3, int(an * 0.009))
        hy0 = trans_y + (y1 - trans_y) // 2
        lz.fill(hx, hy0, hx + 2, hy0 + int(h * 0.10),
                con_alfa(iluminar(0xFFF3D8, luz * 0.55), 0.85))


def inv_puerta_lateral(lz, m, nivel, luz) -> None:
    """Puerta lateral de vidrio entreabierta, con umbral y bisagras."""
    dx = 1.32
    x = m.lado(-1.0, dx * 0.90)
    ancho = m.w * dx * 0.11
    y1 = m.suelo_en(dx * 0.88)
    alto = m.h * dx * 0.58
    x0, x1 = round(x - ancho * 0.18), round(x + ancho * 0.82)
    y0, yf = round(y1 - alto), round(y1)
    marco = iluminar(mezclar(nivel.junta, 0xFF1A2412, 0.35), luz * 0.60)
    lz.fill(x0 - 2, y0 - 2, x1 + 2, yf + 2, marco)
    lz.fill(x0, y0, x1, yf, con_alfa(mezclar(nivel.techo, 0xFF6E8A3A, 0.30), 0.42))
    lz.fill(x0 + 3, y0 + 3, x1 - 4, yf - 3,
            con_alfa(mezclar(nivel.pared_baja, 0xFF4A6A32, 0.35), 0.40))
    lz.fill(x0 + 3, y0 + 3, x0 + 5, yf - 3,
            con_alfa(iluminar(nivel.techo, luz), 0.42))
    lz.fill(x1 - 4, y0 + 3, x1 - 1, yf - 3, marco)
    lz.fill(x0 - 3, yf - 2, x1 + 4, yf + 1, iluminar(nivel.junta, luz * 0.72))
    bisagra = con_alfa(iluminar(nivel.luz, luz * 0.70), 0.80)
    lz.fill(x0 - 1, y0 + round(alto / 4), x0 + 2, y0 + round(alto / 4) + 3, bisagra)
    lz.fill(x0 - 1, y0 + round(alto * 3 / 4), x0 + 2, y0 + round(alto * 3 / 4) + 3, bisagra)
    lz.fill(x1 - 9, y0 + round(alto / 2), x1 - 5, y0 + round(alto / 2) + 2,
            con_alfa(nivel.luz, 0.62))


def inv_panel_roto(lz, m, nivel, luz) -> None:
    """Panel de techo roto, con borde serrado y huecos."""
    dx = 2.45
    cx = m.en_x(dx, -0.34)
    y0 = m.techo_en(dx * 0.82) + m.h * dx * 0.035
    ancho = max(8, round(m.w * dx * 0.18))
    alto = max(6, round(m.h * dx * 0.16))
    x0, x1 = round(cx - ancho * 0.5), round(cx + ancho * 0.5)
    iy0, iy1 = round(y0), round(y0 + alto)
    vidrio = con_alfa(mezclar(nivel.niebla, nivel.techo, 0.30), 0.68)
    lz.fill(x0 - 2, iy0 - 2, x1 + 2, iy1 + 2, iluminar(nivel.junta, luz * 0.55))
    for fila in range(6):
        y = iy0 + fila * alto // 6
        recorte = max(2, ancho // 8) if fila in (1, 4) else 0
        lz.fill(x0 + recorte, y, x1 - (max(3, ancho // 10) if fila == 3 else 0),
                y + max(1, alto // 7), vidrio)
    lz.fill(x0 - 2, iy0 - 2, x1 - ancho // 3, iy0 + 1,
            iluminar(nivel.junta, luz * 0.68))
    lz.fill(x0 + ancho // 5, iy1 - 1, x1 + 2, iy1 + 2,
            iluminar(nivel.junta, luz * 0.62))
    lz.fill(x0 + ancho // 3, iy0 + alto // 3, x0 + ancho // 2,
            iy0 + alto // 3 + 2, con_alfa(VANO, 0.82))
    lz.fill(x0 + ancho * 3 // 5, iy0 + alto * 2 // 3, x1 + 1,
            iy0 + alto * 2 // 3 + 2, con_alfa(VANO, 0.78))


def inv_canaleta_deposito(lz, m, nivel, luz) -> None:
    """Canaleta lateral que termina en un deposito de lluvia."""
    dx = 1.28
    x = round(m.lado(1.0, dx * 0.92))
    y0, y1 = round(m.techo_en(dx * 0.32)), round(m.suelo_en(dx * 0.78))
    ancho = max(3, round(m.w * dx * 0.025))
    metal = iluminar(mezclar(nivel.junta, nivel.pared_baja, 0.30), luz * 0.72)
    lz.fill(x - ancho, y0, x + ancho, y1, metal)
    lz.fill(x - ancho * 2, y0 - 2, x + ancho * 3, y0 + 2, metal)
    lz.fill(x - ancho * 2, y1 - ancho, x + ancho * 4, y1 + ancho, metal)
    tanque_y = round(m.suelo_en(dx * 0.95))
    tanque_x = x - max(7, round(m.w * dx * 0.12))
    tanque_ancho = max(12, round(m.w * dx * 0.24))
    tanque_alto = max(8, round(m.h * dx * 0.16))
    lz.fill(tanque_x, tanque_y - tanque_alto, tanque_x + tanque_ancho, tanque_y, metal)
    lz.fill(tanque_x + 2, tanque_y - tanque_alto + 2, tanque_x + tanque_ancho - 2, tanque_y - 2,
            con_alfa(mezclar(nivel.pared_baja, nivel.suelo, 0.40), 0.62))
    lz.fill(tanque_x - 2, tanque_y - tanque_alto - 2, tanque_x + tanque_ancho + 2,
            tanque_y - tanque_alto + 1, iluminar(nivel.junta, luz * 0.58))


def inv_cristalera(lz, m, nivel, luz) -> None:
    hasta = round(m.techo_en(1.0))
    for y in range(0, hasta, PASO):
        dy = m.dy(y + PASO * 0.5)
        if dy <= 1.0:
            continue
        lej = limitar(1.0 / dy, 0.0, 1.0)
        hacia = 1.0 - limitar(y / max(1, hasta), 0.0, 1.0)
        vidrio = mezclar(nivel.techo, 0xFFFFFFFF, 0.10 + 0.35 * hacia)
        vidrio = velar(vidrio, nivel.niebla, lej, 0.30)
        lz.fill(0, y, m.ancho, y + PASO, iluminar(vidrio, atenuar(luz, lej) * (0.7 + 0.3 * hacia)))
    for j in range(1, INV_TRAMOS + 1):
        dx = profundidad(j, INV_TRAMOS)
        lej = limitar(1.0 / dx, 0.0, 1.0)
        y = m.techo_en(dx * 0.10)
        x = round(m.centro(dx))
        grosor = max(1, int(m.h * dx * 0.012))
        lz.fill(x - grosor, int(y), x + grosor, int(y) + grosor,
                con_alfa(iluminar(nivel.junta, atenuar(luz, lej)), 0.55))


def inv_sendero(lz, m, nivel, luz) -> None:
    for y in range(round(m.suelo_en(1.0)), m.alto, PASO):
        dy = m.dy(y + PASO * 0.5)
        if dy <= 1.0:
            continue
        lej = limitar(1.0 / dy, 0.0, 1.0)
        medio = m.w * dy * 0.22
        color = mezclar(nivel.suelo, nivel.techo, 0.20)
        lz.fill(int(m.centro(dy) - medio), y, int(m.centro(dy) + medio), y + PASO,
                con_alfa(iluminar(color, atenuar(luz, lej)), 0.30))


def inv_bancos(lz, m, nivel, luz) -> None:
    for j in range(2, INV_TRAMOS + 1, 2):
        dx = profundidad(j, INV_TRAMOS)
        if dx > 6.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        for signo in (-1, 1):
            # La mesa vive a la profundidad del banco; antes la base usaba otra
            # profundidad que la x, y la mesa quedaba flotando sobre el suelo.
            prof = dx * 0.62
            # Los tramos mas lejanos caen detras de la pared del fondo (dx<1);
            # ahi no hay suelo, solo pared, y la mesa apareceria pegada a ella.
            if prof < 1.02:
                continue
            x = m.lado(signo, prof)
            if x < -m.w or x > m.ancho + m.w:
                continue
            ancho = max(3.0, m.w * prof * 0.20)
            y = m.suelo_en(prof)
            alto = m.h * prof * 0.05
            # Sombra de contacto: una franja oscura justo debajo del cajon.
            # Es lo que ancla el objeto al suelo; sin ella un cajon se lee
            # como un rectangulo suelto sobre la pendiente de la pared.
            lz.fill(int(x - ancho * 0.62), int(y), int(x + ancho * 0.62), int(y + max(2, alto * 0.5)),
                    con_alfa(mezclar(nivel.fondo, 0x000000, 0.35), 0.30 * at))
            # Cajon plantado en el suelo: el cuerpo va DE la base hacia arriba.
            lz.fill(int(x - ancho * 0.5), int(y - alto), int(x + ancho * 0.5), int(y),
                    iluminar(velar(mezclar(nivel.junta, nivel.pared_baja, 0.40), nivel.niebla, lej, 0.45), at * 0.9))
            # La tierra encima, como un reborde oscuro bien apoyado en el cajon.
            lz.fill(int(x - ancho * 0.5), int(y - alto * 1.35), int(x + ancho * 0.5), int(y - alto),
                    iluminar(velar(0xFF2C2415, nivel.niebla, lej, 0.4), at * 0.6))


def inv_pasarela(lz, m, nivel, luz) -> None:
    # Pasarela oxidada sobre los cultivos: tablon de servicio con soportes al
    # suelo y barandilla de un solo lado. El oxido dice hace cuanto no se usa.
    dx = 2.05
    if m.lado(-1.0, dx * 0.58) > m.ancho or m.lado(1.0, dx * 0.58) < 0:
        return
    lej = limitar(1.0 / dx, 0.0, 1.0)
    at = atenuar(luz, lej)
    y_suelo = m.suelo_en(dx)
    y_deck = y_suelo - m.h * dx * 0.30
    grosor = max(2, int(m.h * dx * 0.020))
    oxido = iluminar(velar(mezclar(nivel.junta, 0xFF7A4E2C, 0.45), nivel.niebla, lej, 0.4), at * 0.85)
    borde = iluminar(oxido, 1.12)
    for signo in (-1, 1):
        x = round(m.lado(signo, dx * 0.58))
        lz.fill(x, int(y_deck), x + grosor, int(y_suelo), oxido)
        lz.fill(x - grosor, int(y_suelo), x + grosor * 2, int(y_suelo) + 2,
                con_alfa(VANO, 0.25 * at))
    x_izq = round(m.lado(-1.0, dx * 0.58))
    x_der = round(m.lado(1.0, dx * 0.58))
    lz.fill(x_izq, int(y_deck), x_der, int(y_deck) + grosor, oxido)
    lz.fill(x_izq, int(y_deck), x_der, int(y_deck) + 1, borde)
    y_riel = y_deck - m.h * dx * 0.055
    lz.fill(x_izq, int(y_riel), x_der, int(y_riel) + 1, borde)
    for i in (1, 2):
        x = x_izq + (x_der - x_izq) * i // 3
        lz.fill(x, int(y_riel), x + 1, int(y_deck), borde)


def inv_vegetacion(lz, m, nivel, luz, tiempo) -> None:
    for i in range(46):
        dx = 1.15 + pseudo(i * 5) * (INV_TRAMOS * 0.42)
        if dx > 7.0:
            continue
        signo = -1 if pseudo(i * 5 + 1) < 0.5 else 1
        frac = 0.44 + pseudo(i * 5 + 2) * 0.55
        # La mata vive a la profundidad dx*frac: su base tiene que estar en el
        # suelo de ESA columna. Antes la base usaba dx*0.72 y, cuando frac era
        # mayor, la planta quedaba dibujada sobre la pared, flotando.
        prof = dx * frac
        # Detras de la pared del fondo (dx<1) no hay suelo: se salta.
        if prof < 1.02:
            continue
        x = m.lado(signo, prof)
        if x < -20 or x > m.ancho + 20:
            continue
        lej = limitar(1.0 / prof, 0.0, 1.0)
        at = atenuar(luz, lej)
        base = m.suelo_en(prof)
        altura = m.h * prof * (0.10 + pseudo(i * 5 + 3) * 0.30)
        ancho_mata = max(2.0, m.w * prof * (0.03 + pseudo(i * 5 + 4) * 0.06))
        verde = mezclar(0xFF3E5A28, 0xFF6E8A3A, pseudo(i * 7))
        verde = velar(verde, nivel.niebla, lej, 0.4)
        vaiven = math.sin(tiempo * 0.4 + i) * ancho_mata * 0.15
        hojas = 6
        for k in range(hojas):
            f = k / hojas
            w = ancho_mata * (1.0 - f * 0.6)
            yy = base - altura * f
            ox = vaiven * f
            lz.fill(int(x - w + ox), int(yy - altura / hojas), int(x + w + ox), int(yy),
                    con_alfa(iluminar(verde, at * (0.7 + 0.3 * f)), 0.9))


def inv_haces(lz, m, nivel, luz, tiempo) -> None:
    for i in range(5):
        fase = tiempo * 0.03 + i * 0.7
        frac = math.sin(fase) * 0.7
        dx_top = 2.0 + i * 1.6
        x_top = m.en_x(dx_top, frac)
        y_top = m.techo_en(dx_top * 0.3)
        x_bot = m.en_x(dx_top * 1.4, frac * 0.7)
        y_bot = m.suelo_en(dx_top * 0.9)
        lej = limitar(1.0 / dx_top, 0.0, 1.0)
        a = 0.05 * luz * (0.5 + 0.5 * lej)
        pasos = 14
        ancho = max(3.0, m.w * dx_top * 0.05)
        for k in range(pasos):
            t = k / pasos
            x = x_top + (x_bot - x_top) * t
            y = y_top + (y_bot - y_top) * t
            lz.fill(int(x - ancho * (1.0 + t)), int(y), int(x + ancho * (1.0 + t)), int(y) + PASO * 2,
                    con_alfa(iluminar(0xFFFFFFF0, luz), a * (1.0 - t * 0.5)))


def inv_vaho(lz, m, nivel, luz, tiempo) -> None:
    desde = round(m.suelo_en(1.0))
    for y in range(desde, m.alto, PASO):
        dy = m.dy(y + PASO * 0.5)
        if dy <= 1.0:
            continue
        lej = limitar(1.0 / dy, 0.0, 1.0)
        humedad = (1.0 - lej) * 0.10 * luz
        if humedad <= 0.005:
            continue
        niebla = mezclar(nivel.niebla, 0xFF6E8A3A, 0.30)
        paso = max(PASO * 8, m.ancho // 9)
        for jx in range(0, m.ancho, paso):
            onda = math.sin(tiempo * 0.14 + jx * 0.012 + dy * 0.5)
            a = humedad * limitar(0.5 + 0.5 * onda, 0.0, 1.0)
            if a <= 0.005:
                continue
            lz.fill(jx, y, min(m.ancho, jx + paso), y + PASO, con_alfa(niebla, a))


def pp_invernadero(lz, m, nivel, luz, tiempo) -> None:
    _fronda(lz, m, tiempo, -m.ancho * 0.02, 0.0, m.ancho * 0.34, m.alto * 0.40, 0xFF223C18, luz, 1.0)
    _fronda(lz, m, tiempo + 3.0, m.ancho * 1.02, 0.0, m.ancho * 0.72, m.alto * 0.30, 0xFF1C3414, luz, -1.0)
    cx = int(m.ancho * 0.80)
    largo = int(m.alto * 0.34)
    for i in range(0, largo, 4):
        t = i / largo
        sway = math.sin(tiempo * 0.5 + t * 3.0) * m.ancho * 0.01
        x = int(cx + sway)
        lz.fill(x - 1, i, x + 2, i + 3, con_alfa(iluminar(0xFF2E4A1E, 0.4 + 0.3 * luz), 0.85))
        if i % 16 == 0:
            lz.fill(x - 4, i, x + 5, i + 4, con_alfa(iluminar(0xFF3E5A28, 0.4 + 0.3 * luz), 0.75))


def _fronda(lz, m, tiempo, bx, by, tx, ty, color, luz, dir) -> None:
    nervios = 9
    mece = math.sin(tiempo * 0.35) * m.ancho * 0.012
    for k in range(nervios):
        a = k / (nervios - 1)
        ex = bx + (tx - bx) * (0.6 + 0.6 * a) + dir * (a - 0.5) * m.ancho * 0.10 + mece
        ey = by + (ty - by) * (0.5 + 0.9 * a)
        pasos = 10
        for p in range(pasos + 1):
            t = p / pasos
            x = int(bx + (ex - bx) * t + mece * t)
            y = int(by + (ey - by) * t)
            ancho = max(2, int(m.ancho * 0.014 * (1.0 - t * 0.5)))
            lz.fill(x - ancho, y - 1, x + ancho, y + 2,
                    con_alfa(iluminar(color, 0.35 + 0.30 * luz), 0.88))


# --------------------------------------------------------------------------
# Nivel 7 - Las catacumbas: espejo de planta/Catacumba.java
# --------------------------------------------------------------------------
CAT_TRAMOS = 20
CAT_PASO_NICHO = 3


def catacumba(lz, m, nivel, luz, tiempo) -> None:
    t_fondo(lz, m, nivel, luz, mezclar(nivel.fondo, nivel.pared_baja, 0.15), 1.10)
    cat_arco(lz, m, nivel, luz)
    cat_pasadizo(lz, m, nivel, luz)
    t_plano(lz, m, True, mezclar(nivel.techo, nivel.pared_baja, 0.30),
            mezclar(nivel.techo, nivel.niebla, 0.50), nivel.niebla, luz, 0.55)
    t_transversales(lz, m, True, nivel.techo_junta, nivel.niebla, luz, CAT_TRAMOS, 0.30)
    cat_arcos(lz, m, nivel, luz)
    t_plano(lz, m, False, nivel.suelo, nivel.suelo_lejos, nivel.niebla, luz, 0.60)
    t_transversales(lz, m, False, nivel.suelo_junta, nivel.niebla, luz, CAT_TRAMOS, 0.42)
    t_paredes(lz, m, nivel, luz)
    cat_sillares(lz, m, nivel, luz)
    cat_drenaje_suelo(lz, m, nivel, luz)
    cat_reparacion_muro(lz, m, nivel, luz)
    cat_aranazos_umbral(lz, m, nivel, luz)
    t_manchas(lz, m, nivel, luz, CAT_TRAMOS)
    cat_nichos(lz, m, nivel, luz, tiempo)
    cat_farol(lz, m, nivel, luz, tiempo)
    cat_goteras(lz, m, nivel, luz, tiempo)


def cat_arco(lz, m, nivel, luz) -> None:
    suelo = m.suelo_en(1.0)
    alto = m.h * 1.55
    x0, x1 = round(m.izq(0.55)), round(m.der(0.55))
    y0, y1 = round(suelo - alto), round(suelo)
    cx = (x0 + x1) // 2
    radio = (x1 - x0) // 2
    t_interior_vano(lz, nivel, x0, y0 + radio // 2, x1, y1, 0, luz)
    for i in range(15):
        ang = math.pi * i / 14.0
        ax = cx - int(math.cos(ang) * radio)
        ay = (y0 + radio // 2) - int(math.sin(ang) * radio * 0.55)
        b = max(1, radio // 7)
        lz.fill(ax - b // 2, ay - b // 2, ax + b // 2 + 1, ay + b // 2 + 1,
                iluminar(nivel.junta, luz * 0.55))


def cat_pasadizo(lz, m, nivel, luz) -> None:
    # Pasadizo estrecho detras del arco del fondo: el tunel no termina en una
    # pared negra, se estrangula y sigue. Segundo umbral, mas alto, con jambas
    # y arco de piedra a media luz.
    suelo = m.suelo_en(1.0)
    x0, x1 = round(m.izq(0.55)), round(m.der(0.55))
    cx = (x0 + x1) // 2
    radio = (x1 - x0) // 2
    y_suelo = round(suelo)
    ancho = max(6, radio // 3)
    alto = max(8, radio // 2)
    px0, px1 = cx - ancho // 2, cx + ancho // 2
    py1 = y_suelo - max(2, radio // 9)
    py0 = py1 - alto
    if px1 <= px0 or py1 <= py0:
        return
    fondo = con_alfa(mezclar(VANO, nivel.niebla, 0.06), 0.98)
    lz.fill(px0, py0, px1, py1, fondo)
    piedra = con_alfa(iluminar(nivel.junta, luz * 0.26), 0.80)
    lz.fill(px0 - 2, py0, px0, py1, piedra)
    lz.fill(px1, py0, px1 + 2, py1, piedra)
    for i in range(7):
        ang = math.pi * i / 6.0
        ax = px0 + int(math.cos(ang) * (ancho / 2))
        ay = py0 - int(math.sin(ang) * (ancho / 2) * 0.62)
        lz.fill(ax - 1, ay - 1, ax + 2, ay + 2, piedra)
    lz.fill(px0, py1 - 1, px1, py1,
            con_alfa(iluminar(nivel.suelo_lejos, luz * 0.18), 0.50))


def cat_arcos(lz, m, nivel, luz) -> None:
    for j in range(1, CAT_TRAMOS + 1, 2):
        dx = profundidad(j, CAT_TRAMOS)
        if dx > 9.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej) * 0.8
        color = iluminar(velar(nivel.junta, nivel.niebla, lej, 0.5), at)
        grosor = max(1, int(m.h * dx * 0.02))
        y_pared = m.techo_en(dx)
        y_cima = m.techo_en(dx * 0.86)
        cx = round(m.centro(dx))
        _linea(lz, round(m.izq(dx)), int(y_pared), cx, int(y_cima), grosor, color)
        _linea(lz, cx, int(y_cima), round(m.der(dx)), int(y_pared), grosor, color)


def cat_sillares(lz, m, nivel, luz) -> None:
    for x in range(0, m.ancho, PASO):
        dx = m.dx(x + PASO * 0.5)
        if dx <= 1.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        y0, y1 = m.techo_en(dx), m.suelo_en(dx)
        hiladas = 7
        for k in range(1, hiladas):
            f = k / hiladas
            y = int(y0 + (y1 - y0) * f)
            desvio = pseudo(int(dx * 149.0) + k * 29 + x // 6) * 0.12 - 0.06
            lz.fill(x, y, x + PASO, y + 1,
                    con_alfa(iluminar(nivel.junta, at * (0.9 + desvio)), 0.28 * lej + 0.10))
    t_juntas(lz, m, nivel, luz, CAT_TRAMOS, 1.0, 0.26)



def cat_drenaje_suelo(lz, m, nivel, luz) -> None:
    """Canal estrecho de humedad que sigue el suelo hacia el fondo."""
    inicio = round(m.suelo_en(1.04))
    for y in range(inicio, m.alto, PASO):
        dx = m.dy(y + PASO * 0.5)
        if dx <= 1.0:
            continue
        x = m.lado(-1.0, dx * 0.70)
        ancho = max(2, round(m.w * dx * 0.018))
        lej = limitar(1.0 / dx, 0.0, 1.0)
        lz.fill(round(x - ancho), y, round(x + ancho), y + PASO,
                con_alfa(iluminar(nivel.junta, atenuar(luz, lej)), 0.62))
        if int(dx * 9.0) % 4 == 0:
            lz.fill(round(x - ancho * 0.35), y - 1, round(x + ancho * 0.35), y + 1,
                    con_alfa(iluminar(nivel.luz, luz), 0.18))


def cat_reparacion_muro(lz, m, nivel, luz) -> None:
    """Sillar de reparacion reciente con mortero y valor distinto."""
    dx = 1.42
    x = round(m.lado(-1.0, dx * 0.86))
    y = round(m.techo_en(dx * 0.48) + m.h * dx * 0.56)
    ancho = max(7, round(m.w * dx * 0.16))
    alto = max(5, round(m.h * dx * 0.13))
    piedra = iluminar(mezclar(nivel.pared_alta, nivel.junta, 0.28), luz * 0.72)
    lz.fill(x - ancho // 2, y, x + ancho // 2, y + alto, piedra)
    lz.fill(x - ancho // 2, y, x + ancho // 2, y + 1,
            con_alfa(iluminar(nivel.techo, luz), 0.32))
    lz.fill(x - ancho // 4, y + alto // 2, x - ancho // 4 + 1, y + alto,
            con_alfa(nivel.junta, 0.58))
    lz.fill(x + ancho // 3, y + alto // 3, x + ancho // 3 + 1, y + alto,
            con_alfa(nivel.junta, 0.46))


def cat_aranazos_umbral(lz, m, nivel, luz) -> None:
    """Aranazos cortos en un umbral, anclados a la base."""
    dx = 1.62
    x = round(m.lado(1.0, dx * 0.76))
    y = round(m.suelo_en(dx))
    color = con_alfa(iluminar(nivel.junta, luz * 0.70), 0.72)
    _linea(lz, x - 13, y - 2, x - 5, y - 6, 1, color)
    _linea(lz, x - 3, y - 1, x + 5, y - 5, 1, color)
    _linea(lz, x + 7, y - 2, x + 14, y - 4, 1, color)

def cat_nichos(lz, m, nivel, luz, tiempo) -> None:
    for j in range(2, CAT_TRAMOS + 1, CAT_PASO_NICHO):
        dx = profundidad(j, CAT_TRAMOS)
        if dx > 7.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        for signo in (-1, 1):
            x = m.lado(signo, dx * 0.98)
            if x < -m.w or x > m.ancho + m.w:
                continue
            ancho = max(3.0, m.w * dx * 0.16)
            centro_y = m.techo_en(dx * 0.45)
            alto = m.h * dx * 0.34
            nx0, nx1 = int(x - ancho * 0.5), int(x + ancho * 0.5)
            ny0, ny1 = int(centro_y - alto * 0.5), int(centro_y + alto * 0.5)
            lz.fill(nx0, ny0, nx1, ny1, con_alfa(mezclar(nivel.fondo, nivel.niebla, 0.10), 0.95))
            # Paredes del hueco en sombra (el lado de dentro de la roca).
            cornisa = iluminar(velar(nivel.junta, nivel.niebla, lej, 0.4), at * 0.35)
            lz.fill(nx0, ny0, nx1, ny0 + 2, cornisa)
            lz.fill(nx0, ny0, nx0 + 2, ny1, cornisa)
            lz.fill(nx1 - 2, ny0, nx1, ny1, cornisa)
            # El hueco se excava en la pared: el borde inferior es el
            # alfeizar, iluminado por la luz del tunel, no un marco.
            alfeizar = iluminar(velar(nivel.junta, nivel.niebla, lej, 0.35), at * 0.60)
            lz.fill(nx0 - 2, ny1, nx1 + 2, ny1 + max(2, int(alto * 0.10)), alfeizar)
            if pseudo(500 + j * 7 + (signo + 1) * 40) > 0.55:
                titil = 0.85 + 0.15 * math.sin(tiempo * 6.0 + j)
                av = at * titil
                vx = (nx0 + nx1) // 2
                vy = ny1 - max(2, int(alto * 0.16))
                for k in range(3, 0, -1):
                    t = k / 3.0
                    e = ancho * 0.28 * (1.0 + t * 2.5)
                    lz.fill(int(vx - e), int(vy - e), int(vx + e), int(vy + e * 0.6),
                            con_alfa(nivel.luz, 0.09 * av * (1.0 - t * 0.5)))
                lz.fill(vx - 1, vy - 2, vx + 2, vy + 1,
                        con_alfa(iluminar(0xFFFFE0A0, min(1.0, av * 1.3)), 0.95))


def cat_farol(lz, m, nivel, luz, tiempo) -> None:
    dx = 2.4
    cx = m.centro(dx) + math.sin(tiempo * 0.6) * m.w * dx * 0.02
    cy = m.techo_en(dx * 0.55)
    lej = limitar(1.0 / dx, 0.0, 1.0)
    titil = 0.88 + 0.12 * math.sin(tiempo * 7.0)
    at = atenuar(luz, lej) * titil
    medio = max(2.0, m.w * dx * 0.03)
    y_techo = m.techo_en(dx * 0.90)
    lz.fill(int(cx) - 1, int(y_techo), int(cx) + 1, int(cy), con_alfa(iluminar(nivel.junta, at * 0.7), 0.8))
    for k in range(5, 0, -1):
        t = k / 5.0
        e = medio * (1.0 + t * 4.5)
        lz.fill(int(cx - e), int(cy - e), int(cx + e), int(cy + e),
                con_alfa(nivel.luz, 0.06 * at * (1.0 - t * 0.5)))
    hierro = iluminar(nivel.junta, at * 0.9)
    lz.fill(int(cx - medio), int(cy - medio * 1.3), int(cx + medio), int(cy + medio * 1.3), hierro)
    lz.fill(int(cx - medio * 0.5), int(cy - medio * 0.7), int(cx + medio * 0.5), int(cy + medio * 0.6),
            con_alfa(iluminar(0xFFFFE0A0, min(1.0, at * 1.4)), 0.95))


def cat_goteras(lz, m, nivel, luz, tiempo) -> None:
    for i in range(10):
        dx = 1.3 + pseudo(i * 11) * (CAT_TRAMOS * 0.35)
        if dx > 7.0:
            continue
        signo = -1 if pseudo(i * 11 + 1) < 0.5 else 1
        x = m.lado(signo, dx * 0.9)
        if x < 0 or x > m.ancho:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        y0, y1 = m.techo_en(dx), m.suelo_en(dx)
        lz.fill(int(x), int(y0), int(x) + 1, int(y1),
                con_alfa(iluminar(mezclar(nivel.pared_alta, 0xFF88AACC, 0.4), atenuar(luz, lej) * 0.5), 0.30))
        fase = (tiempo * 0.3 + pseudo(i * 11 + 2)) % 1.0
        gy = int(y0 + (y1 - y0) * fase)
        lz.fill(int(x) - 1, gy, int(x) + 2, gy + 3,
                con_alfa(iluminar(0xFFBFE0FF, atenuar(luz, lej)), 0.55))


def pp_catacumba(lz, m, nivel, luz, tiempo) -> None:
    piedra = mezclar(nivel.pared_baja, 0x000000, 0.45)
    piedra_clara = iluminar(mezclar(nivel.pared_alta, 0x000000, 0.30), 0.5 + 0.3 * luz)
    w, h = m.ancho, m.alto
    jamba = int(w * 0.14)
    lz.fill_gradient(0, 0, jamba, h, iluminar(piedra, 0.5 + 0.2 * luz), iluminar(piedra, 0.25 + 0.12 * luz))
    lz.fill_gradient(w - jamba, 0, w, h, iluminar(piedra, 0.5 + 0.2 * luz), iluminar(piedra, 0.25 + 0.12 * luz))
    lz.fill(jamba, 0, jamba + 2, h, con_alfa(piedra_clara, 0.5))
    lz.fill(w - jamba - 2, 0, w - jamba, h, con_alfa(piedra_clara, 0.5))
    arco_alto = int(h * 0.22)
    for x in range(jamba, w - jamba, PASO):
        t = (x - jamba) / max(1, (w - 2 * jamba))
        caida = math.sin(math.pi * t)
        borde = int(arco_alto * (1.0 - 0.6 * caida))
        lz.fill_gradient(x, 0, x + PASO, borde, iluminar(piedra, 0.45 + 0.2 * luz), iluminar(piedra, 0.20 + 0.1 * luz))
        lz.fill(x, borde, x + PASO, borde + 2, con_alfa(piedra_clara, 0.45))
    vx = w - jamba // 2
    vy = int(h * 0.55)
    titil = 0.85 + 0.15 * math.sin(tiempo * 6.5)
    # Halo corto, centrado en la LLAMA (no en el pie de la vela), con mas
    # capas y menos alfa: cinco rectangulos pequenos escalonados dejan de
    # leerse como una caja y pasan a ser un resplandor difuso.
    cy = vy - int(h * 0.055)
    for k in range(5, 0, -1):
        t = k / 5.0
        e = w * 0.006 * (1.0 + t * 0.9)
        lz.fill(int(vx - e), int(cy - e), int(vx + e), int(cy + e * 0.7),
                con_alfa(nivel.luz, 0.040 * luz * titil * (1.0 - t * 0.55)))
    # Un saliente de piedra donde descansa la vela: no flota en la jamba.
    lz.fill(vx - 6, vy + 1, vx + 6, vy + 4, con_alfa(iluminar(nivel.junta, 0.4 * luz), 0.85))
    lz.fill(vx - 2, vy - int(h * 0.05), vx + 2, vy, con_alfa(iluminar(nivel.pared_alta, 0.6 * luz), 0.9))
    lz.fill(vx - 1, vy - int(h * 0.065), vx + 1, vy - int(h * 0.05),
            con_alfa(iluminar(0xFFFFE0A0, min(1.0, luz * titil * 1.4)), 0.95))


# --------------------------------------------------------------------------
# Nivel 8 - La cisterna: espejo de planta/Cisterna.java
# --------------------------------------------------------------------------
CIS_TRAMOS = 16
CIS_ORILLA = 1.02
CIS_HILERA = 0.60


def cisterna(lz, m, nivel, luz, tiempo) -> None:
    t_fondo(lz, m, nivel, luz, mezclar(nivel.fondo, nivel.pared_baja, 0.20), 1.10)
    t_plano(lz, m, True, mezclar(nivel.techo, nivel.pared_baja, 0.35),
            mezclar(nivel.techo, nivel.niebla, 0.55), nivel.niebla, luz, 0.55)
    t_transversales(lz, m, True, nivel.techo_junta, nivel.niebla, luz, CIS_TRAMOS, 0.26)
    cis_agua(lz, m, nivel, luz, tiempo)
    t_paredes(lz, m, nivel, luz)
    cis_compuerta_inspeccion(lz, m, nivel, luz)
    t_manchas(lz, m, nivel, luz, CIS_TRAMOS)
    cis_tuberia_entrada(lz, m, nivel, luz, tiempo)
    cis_columnas(lz, m, nivel, luz, tiempo)
    cis_marcas_nivel_columna(lz, m, nivel, luz)
    cis_focos(lz, m, nivel, luz, tiempo)
    cis_gotas(lz, m, nivel, luz, tiempo)


def cis_compuerta_inspeccion(lz, m, nivel, luz) -> None:
    """Compuerta de inspeccion mural, con perimetro y bisagras."""
    dx = 1.34
    centro = m.lado(1.0, dx * 0.72)
    ancho = m.w * dx * 0.16
    alto = m.h * dx * 0.30
    x0, x1 = round(centro - ancho), round(centro + ancho)
    y1 = round(m.suelo_en(dx * 0.72) - m.h * dx * 0.18)
    y0 = round(y1 - alto)
    marco = iluminar(mezclar(nivel.junta, nivel.pared_alta, 0.28), luz * 0.64)
    lz.fill(x0 - 2, y0 - 2, x1 + 2, y1 + 2, marco)
    lz.fill(x0, y0, x1, y1, con_alfa(VANO, 0.72))
    lz.fill(x0 + 3, y0 + 3, x1 - 3, y1 - 3,
            con_alfa(mezclar(nivel.pared_baja, VANO, 0.35), 0.72))
    lz.fill(x0, y0, x1, y0 + 2, con_alfa(iluminar(nivel.techo, luz), 0.52))
    lz.fill(x0 - 2, y1 - 2, x1 + 2, y1 + 1, iluminar(nivel.junta, luz * 0.70))
    bisagra = con_alfa(iluminar(nivel.luz, luz * 0.68), 0.78)
    lz.fill(x1 - 2, y0 + 4, x1 + 1, y0 + 7, bisagra)
    lz.fill(x1 - 2, y1 - 8, x1 + 1, y1 - 5, bisagra)
    lz.fill(x0 + 4, y0 + round(alto / 2), x0 + 8, y0 + round(alto / 2) + 2,
            con_alfa(nivel.luz, 0.54))


def cis_tuberia_entrada(lz, m, nivel, luz, tiempo) -> None:
    """Tuberia de entrada que termina sobre el agua."""
    dx = 2.05
    x = m.lado(-1.0, dx * 0.70)
    grosor = max(2, round(m.w * dx * 0.024))
    y0, y1 = round(m.techo_en(dx * 0.38)), round(m.suelo_en(dx * 0.88))
    metal = iluminar(mezclar(nivel.junta, nivel.pared_alta, 0.22), luz * 0.66)
    lz.fill(round(x - grosor), y0, round(x + grosor), y1, metal)
    lz.fill(round(x - grosor), y0 - grosor, round(x + grosor * 3), y0 + grosor, metal)
    lz.fill(round(x - grosor), y1 - grosor, round(x + grosor * 2), y1 + grosor, metal)
    gota_y = y1 + round(m.h * dx * 0.05)
    fase = (tiempo * 0.25) % 1.0
    caida = gota_y + round(m.h * dx * 0.09 * fase)
    lz.fill(round(x), caida, round(x + grosor), caida + max(2, grosor),
            con_alfa(iluminar(nivel.luz, luz), 0.54))


def cis_marcas_nivel_columna(lz, m, nivel, luz) -> None:
    """Marcas de nivel ancladas a una columna sumergida."""
    dx = 2.25
    x = m.lado(1.0, dx * CIS_HILERA)
    y0, y1 = m.techo_en(dx * 0.92), m.suelo_en(dx)
    ancho = max(6, round(m.w * dx * 0.13))
    color = con_alfa(iluminar(nivel.techo, luz * 0.62), 0.58)
    for i in range(1, 4):
        y = round(y0 + (y1 - y0) * (i / 4.0))
        lz.fill(round(x - ancho), y, round(x + ancho * 0.20), y + 2, color)
        lz.fill(round(x - ancho), y + 2, round(x - ancho + 3), y + 4,
                con_alfa(nivel.junta, 0.48))


def cis_agua(lz, m, nivel, luz, tiempo) -> None:
    desde = round(m.suelo_en(CIS_ORILLA))
    for y in range(desde, m.alto, PASO):
        dy = m.dy(y + PASO * 0.5)
        lej = limitar(1.0 / dy, 0.0, 1.0)
        agua_base = velar(mezclar(nivel.suelo, nivel.fondo, 0.55), nivel.niebla, lej, 0.30)
        lz.fill(0, y, m.ancho, y + PASO, iluminar(agua_base, atenuar(luz, lej) * 0.7))
    if 0 <= desde < m.alto:
        lz.fill(0, desde, m.ancho, desde + 1, con_alfa(iluminar(nivel.luz, luz * 0.5), 0.35))


def cis_galeria(lz, m, nivel, luz, tiempo) -> None:
    # Galeria de mantenimiento sobre el agua, entre las dos hileras de
    # columnas: tablon, barandilla de un lado, anclajes y reflejo partido.
    dx = 1.6
    lej = limitar(1.0 / dx, 0.0, 1.0)
    at = atenuar(luz, lej)
    x_izq = round(m.lado(-1.0, dx))
    x_der = round(m.lado(1.0, dx))
    if x_der <= x_izq + 6:
        return
    desde = round(m.suelo_en(CIS_ORILLA))
    y_deck = desde - max(3, int(m.h * 0.16))
    y_riel = y_deck - max(3, int(m.h * 0.055))
    grosor = max(2, int(m.h * 0.012))
    metal = iluminar(velar(mezclar(nivel.junta, 0x000000, 0.30), nivel.niebla, lej, 0.5), at * 0.9)
    borde = iluminar(nivel.pared_alta, at * 0.7)
    lz.fill(x_izq, y_deck, x_der, y_deck + grosor, metal)
    lz.fill(x_izq, y_deck, x_der, y_deck + 1, con_alfa(iluminar(nivel.luz, at), 0.12))
    lz.fill(x_izq, y_deck + grosor, x_der, y_deck + grosor + 1, con_alfa(VANO, 0.35 * at))
    lz.fill(x_izq, y_riel, x_der, y_riel + 1, borde)
    paso = max(20, (x_der - x_izq) // 5)
    for x in range(x_izq + paso // 2, x_der, paso):
        lz.fill(x, y_riel, x + 1, y_deck, borde)
    for lado in (0, 1):
        ax = x_izq if lado == 0 else x_der - 1
        signo = 1 if lado == 0 else -1
        lz.fill(ax, y_deck, ax + 1, desde + max(4, int(m.h * 0.09)), metal)
        lz.fill(ax, y_deck, ax + signo * max(3, int(m.h * 0.02)), y_deck + 1,
                con_alfa(borde, 0.8))
    for k in range(4):
        t = k / 4.0
        ry = desde + max(2, int(m.h * (0.05 + t * 0.10)))
        onda = math.sin(tiempo * 0.6 + k * 2.1) * m.w * 0.02
        rx0 = round(x_izq + onda * 0.4)
        rx1 = round(x_der + onda)
        lz.fill(rx0, ry, rx1, ry + 1, con_alfa(iluminar(metal, 0.8), (1.0 - t) * 0.20 * luz))


def cis_columnas(lz, m, nivel, luz, tiempo) -> None:
    for j in range(2, CIS_TRAMOS + 1, 2):
        dx = profundidad(j, CIS_TRAMOS)
        if dx > 7.0:
            continue
        if j == 10:
            cis_galeria(lz, m, nivel, luz, tiempo)
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        ancho = max(2.0, m.w * dx * 0.05)
        y_techo = m.techo_en(dx * 0.92)
        y_base = m.suelo_en(max(CIS_ORILLA, dx))
        for signo in (-1, 1):
            x = m.lado(signo, dx * CIS_HILERA)
            if x < -ancho * 2 or x > m.ancho + ancho * 2:
                continue
            frente = iluminar(velar(nivel.pared_alta, nivel.niebla, lej, 0.45), at * 0.9)
            costado = iluminar(velar(nivel.pared_baja, nivel.niebla, lej, 0.50), at * 0.55)
            corte = ancho * 0.42 * (1 if signo < 0 else -1)
            lz.fill(int(x - ancho), int(y_techo), int(x + corte), int(y_base),
                    costado if signo < 0 else frente)
            lz.fill(int(x + corte), int(y_techo), int(x + ancho), int(y_base),
                    frente if signo < 0 else costado)
            cap_a = m.h * dx * 0.05
            lz.fill(int(x - ancho * 1.3), int(y_techo), int(x + ancho * 1.3), int(y_techo + cap_a),
                    iluminar(velar(nivel.junta, nivel.niebla, lej, 0.4), at * 0.8))
            largo = (y_base - y_techo) * 0.8
            pasos = 12
            for k in range(pasos):
                t = k / pasos
                ry0 = int(y_base + largo * t)
                ry1 = int(y_base + largo * (k + 1) / pasos)
                if ry0 >= m.alto:
                    break
                onda = math.sin(tiempo * 0.5 + t * 6.0 + j) * ancho * 0.4
                desvanece = (1.0 - t) * (1.0 - t) * 0.5
                lz.fill(int(x - ancho + onda), ry0, int(x + ancho + onda), max(ry0 + 1, ry1),
                        con_alfa(iluminar(velar(nivel.pared_alta, nivel.niebla, lej, 0.5), at * 0.6), desvanece * luz))


def cis_focos(lz, m, nivel, luz, tiempo) -> None:
    for j in range(3, CIS_TRAMOS + 1, 4):
        dx = profundidad(j, CIS_TRAMOS)
        if dx > 6.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        x = m.centro(dx)
        y = m.suelo_en(max(CIS_ORILLA, dx))
        titil = 0.85 + 0.15 * math.sin(tiempo * 1.5 + j)
        at = atenuar(luz, lej) * titil
        medio = max(2.0, m.w * dx * 0.06)
        for k in range(5, 0, -1):
            t = k / 5.0
            ex = medio * (1.0 + t * 3.0)
            ey = medio * (1.0 + t * 5.0)
            lz.fill(int(x - ex), int(y - ey), int(x + ex), int(y + ey * 0.3),
                    con_alfa(nivel.luz, 0.05 * at * (1.0 - t * 0.5)))
        lz.fill(int(x - medio * 0.5), int(y - 1), int(x + medio * 0.5), int(y + 2),
                con_alfa(iluminar(nivel.luz, min(1.0, at * 1.2)), 0.7))


def cis_gotas(lz, m, nivel, luz, tiempo) -> None:
    desde = round(m.suelo_en(CIS_ORILLA))
    for i in range(8):
        dx = 1.4 + pseudo(i * 13) * (CIS_TRAMOS * 0.4)
        frac = (pseudo(i * 13 + 1) - 0.5) * 1.6
        x = m.en_x(dx, frac)
        y = m.suelo_en(max(CIS_ORILLA, dx))
        if x < 0 or x > m.ancho or y < desde:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        ciclo = (tiempo * 0.4 + pseudo(i * 13 + 2)) % 1.0
        if ciclo > 0.4:
            continue
        r = ciclo / 0.4
        radio = m.w * dx * 0.06 * r
        a = (1.0 - r) * 0.5 * atenuar(luz, lej)
        col = con_alfa(iluminar(nivel.luz, luz), a)
        lz.fill(int(x - radio), int(y), int(x + radio), int(y) + 1, col)
        lz.fill(int(x - radio * 0.6), int(y) + 2, int(x + radio * 0.6), int(y) + 3, col)


def pp_cisterna(lz, m, nivel, luz, tiempo) -> None:
    balance = desvio(tiempo, 1.6, 0.09)
    w, h = m.ancho, m.alto
    hierro = mezclar(nivel.junta, 0x000000, 0.35)
    pas_y = int(h * 0.80 + balance)
    grosor = max(3, int(h * 0.018))
    lz.fill_gradient(0, pas_y, w, pas_y + grosor,
                     iluminar(hierro, 0.40 + 0.20 * luz), iluminar(hierro, 0.20 + 0.10 * luz))
    lz.fill(0, pas_y, w, pas_y + 1, con_alfa(iluminar(nivel.luz, luz), 0.16))
    bajo_y = pas_y + int(h * 0.10)
    lz.fill(0, bajo_y, w, bajo_y + max(2, grosor // 2), iluminar(hierro, 0.28 + 0.14 * luz))
    paso = max(24, int(w * 0.11))
    x = int(paso * 0.5 + balance * 2.0)
    while x < w:
        lz.fill_gradient(x, pas_y, x + max(2, grosor // 2), h,
                         iluminar(hierro, 0.34 + 0.16 * luz), iluminar(hierro, 0.12 + 0.08 * luz))
        x += paso
    lz.fill_gradient(0, bajo_y + grosor // 2, w, h, con_alfa(0x000000, 0.30), con_alfa(0x000000, 0.62))
    fx = int(w * 0.24 + balance * 2.0)
    fy = pas_y
    fh = int(h * 0.09)
    titil = 0.85 + 0.15 * math.sin(tiempo * 6.0)
    for k in range(4, 0, -1):
        t = k / 4.0
        e = w * 0.03 * (1.0 + t * 2.4)
        lz.fill(int(fx - e), int(fy - fh - e * 0.5), int(fx + e), fy,
                con_alfa(nivel.luz, 0.07 * luz * titil * (1.0 - t * 0.5)))
    lz.fill(fx - 3, fy - fh, fx + 3, fy, con_alfa(iluminar(hierro, luz), 0.92))
    lz.fill(fx - 2, fy - fh + 2, fx + 2, fy - 2,
            con_alfa(iluminar(0xFFFFE0A0, min(1.0, luz * titil * 1.3)), 0.9))


# --------------------------------------------------------------------------
# Nivel 9 - El salon del trono: espejo de planta/Trono.java
# --------------------------------------------------------------------------
TRO_TRAMOS = 15
TRO_HILERA = 0.72
TRO_TARIMA = 1.18


def trono(lz, m, nivel, luz, tiempo) -> None:
    t_fondo(lz, m, nivel, luz, mezclar(nivel.pared_baja, nivel.junta, 0.30), 1.15)
    tro_abside(lz, m, nivel, luz)
    t_plano(lz, m, True, mezclar(nivel.techo, nivel.pared_baja, 0.30),
            mezclar(nivel.techo, nivel.niebla, 0.45), nivel.niebla, luz, 0.50)
    t_transversales(lz, m, True, nivel.techo_junta, nivel.niebla, luz, TRO_TRAMOS, 0.28)
    tro_boquetes(lz, m, nivel, luz)
    t_plano(lz, m, False, nivel.suelo, nivel.suelo_lejos, nivel.niebla, luz, 0.52)
    t_transversales(lz, m, False, nivel.suelo_junta, nivel.niebla, luz, TRO_TRAMOS, 0.40)
    tro_alfombra(lz, m, nivel, luz)
    t_paredes(lz, m, nivel, luz)
    tro_sillares(lz, m, nivel, luz)
    tro_dintel_roto(lz, m, nivel, luz)
    tro_humedad_abside(lz, m, nivel, luz)
    t_manchas(lz, m, nivel, luz, TRO_TRAMOS)
    tro_haz_mayor(lz, m, nivel, luz, tiempo)
    tro_trono(lz, m, nivel, luz, tiempo)
    tro_columnas(lz, m, nivel, luz)
    tro_estandartes(lz, m, nivel, luz, tiempo)
    tro_haces(lz, m, nivel, luz, tiempo)


def tro_abside(lz, m, nivel, luz) -> None:
    # El abside: un nicho de piedra al fondo que enmarca el trono. En vez de
    # dejar el testero plano y oscuro, se recorta un arco elevado detras de la
    # tarima, mas claro por dentro, con un reborde iluminado. Le da al trono una
    # pared propia y un lugar en el mundo, no un vacio negro.
    dx = 1.0
    cx = m.centro(dx)
    ancho = m.ancho_en(dx) * 0.30
    y_suelo = m.suelo_en(dx)
    y_arco = m.techo_en(dx) + (y_suelo - m.techo_en(dx)) * 0.20
    hombro = m.techo_en(dx) + (y_suelo - m.techo_en(dx)) * 0.34
    interior = iluminar(mezclar(nivel.pared_baja, nivel.niebla, 0.35), luz * 0.55)
    # Cuerpo del nicho (rectangulo desde el hombro hasta el suelo).
    lz.fill(int(cx - ancho), int(hombro), int(cx + ancho), int(y_suelo), interior)
    # La boveda del nicho: escalones que suben al centro simulando un arco.
    pasos = 7
    for k in range(pasos):
        f = k / (pasos - 1)
        w = ancho * (1.0 - (1.0 - f) * (1.0 - f))
        yk = int(hombro + (y_arco - hombro) * f)
        lz.fill(int(cx - w), int(y_arco) if k == pasos - 1 else yk,
                int(cx + w), yk + max(1, int((hombro - y_arco) / pasos) + 1), interior)
    # Reborde iluminado del arco: el oro del trono se derrama en el marco.
    borde = con_alfa(iluminar(nivel.luz, luz * 0.7), 0.5)
    for k in range(pasos):
        f = k / (pasos - 1)
        w = ancho * (1.0 - (1.0 - f) * (1.0 - f))
        yk = int(hombro + (y_arco - hombro) * f)
        lz.fill(int(cx - w) - 1, yk, int(cx - w) + 1, yk + 3, borde)
        lz.fill(int(cx + w) - 1, yk, int(cx + w) + 1, yk + 3, borde)
    # Dovelas concentricas: tres arcos de piedra que envuelven el vano, como
    # los de una boveda construida. Cada banda es mas ancha y baja un poco
    # mas; el oro solo toca la mas interna.
    piedra_arco = iluminar(velar(nivel.junta, nivel.niebla, 1.0, 0.30), luz * 0.62)
    for d in range(1, 4):
        escala = 1.0 + d * 0.11
        for k in range(pasos):
            f = k / (pasos - 1)
            w = ancho * escala * (1.0 - (1.0 - f) * (1.0 - f))
            yk = int(hombro + (y_arco - hombro) * f + (hombro - y_arco) * d * 0.030 * f)
            lz.fill(int(cx - w) - d, yk, int(cx - w) - d + 2, yk + 3, piedra_arco)
            lz.fill(int(cx + w) + d - 2, yk, int(cx + w) + d, yk + 3, piedra_arco)


def tro_dintel_roto(lz, m, nivel, luz) -> None:
    """Dintel irregular sobre el abside, con dos bloques ausentes."""
    dx = 1.0
    techo = m.techo_en(dx)
    suelo = m.suelo_en(dx)
    y = techo + (suelo - techo) * 0.105
    ancho = m.ancho_en(dx) * 0.43
    bloque = ancho / 6.0
    piedra = iluminar(velar(nivel.junta, nivel.niebla, 1.0, 0.35), luz * 0.68)
    canto = con_alfa(iluminar(nivel.luz, luz * 0.62), 0.38)
    for i in range(6):
        if i in (2, 3):
            continue
        x0 = m.centro(dx) - ancho * 0.5 + bloque * i
        y0 = round(y + (0.0 if i % 2 == 0 else 2.0))
        y1 = y0 + max(3, round(m.h * dx * 0.055))
        lz.fill(round(x0), y0, round(x0 + bloque + 1.0), y1, piedra)
        lz.fill(round(x0), y0, round(x0 + bloque + 1.0), y0 + 1, canto)


def tro_humedad_abside(lz, m, nivel, luz) -> None:
    """Humedad corta y baja en las uniones del abside con el piso."""
    dx = 1.0
    y = m.suelo_en(dx) - m.h * 0.10
    color = mezclar(nivel.niebla, nivel.pared_baja, 0.45)
    for lado in (-1, 1):
        x = m.lado(lado, dx * 0.24)
        ancho = m.ancho_en(dx) * 0.055
        alfa = round(42.0 * nivel.humedad * luz)
        lz.fill(round(x - ancho), round(y - m.h * 0.08), round(x + ancho), round(y + m.h * 0.02),
                con_alfa(iluminar(color, luz * 0.65), alfa / 255.0))
        lz.fill(round(x - ancho * 0.45), round(y - m.h * 0.18), round(x + ancho * 0.45), round(y - m.h * 0.06),
                con_alfa(iluminar(color, luz * 0.52), alfa * 0.65 / 255.0))


def tro_haz_mayor(lz, m, nivel, luz, tiempo) -> None:
    # El haz cenital que cae sobre el trono: el gesto central de la sala. Baja
    # desde el techo hasta la tarima, ancho y luminoso, con motas de polvo
    # suspendidas dentro. Es lo que dice "mira aca" y encuentra un asiento vacio.
    dx = TRO_TARIMA
    cx = m.centro(dx)
    y_top = m.techo_en(dx * 0.30)
    y_bot = m.suelo_en(dx)
    ancho = m.ancho_en(dx) * 0.13
    parpadeo = 0.9 + 0.1 * math.sin(tiempo * 0.5)
    pasos = 26
    for k in range(pasos):
        t = k / (pasos - 1)
        w = ancho * (0.45 + t * 0.75)
        y = int(y_top + (y_bot - y_top) * t)
        a = 0.13 * luz * parpadeo * (0.35 + 0.65 * t)
        lz.fill(int(cx - w), y, int(cx + w), y + int((y_bot - y_top) / pasos) + 1,
                con_alfa(iluminar(0xFFFFF0C0, luz), a))
    # Un charco de luz en el suelo, al pie de la tarima.
    w = ancho * 1.35
    lz.fill(int(cx - w), int(y_bot) - 3, int(cx + w), int(y_bot) + 4,
            con_alfa(iluminar(0xFFFFF0C0, luz), 0.16 * luz))
    # Motas de polvo en el haz.
    for i in range(18):
        px = cx + (pseudo(700 + i) - 0.5) * ancho * 1.6
        py = y_top + ((pseudo(720 + i) + tiempo * 0.02 * (0.5 + pseudo(740 + i))) % 1.0) * (y_bot - y_top)
        s = 1 if pseudo(760 + i) < 0.7 else 2
        lz.fill(int(px), int(py), int(px) + s, int(py) + s,
                con_alfa(iluminar(0xFFFFF6D8, luz), 0.35 * luz))


def tro_boquetes(lz, m, nivel, luz) -> None:
    for j in range(2, TRO_TRAMOS + 1, 3):
        if pseudo(300 + j) > 0.6:
            continue
        dx = profundidad(j, TRO_TRAMOS)
        if dx > 6.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        signo = -1 if pseudo(310 + j) < 0.5 else 1
        cx = m.en_x(dx, signo * 0.34)
        # El hueco sigue el mismo plano del techo; asi no parece flotante.
        cy = m.techo_en(dx)
        w = max(3.0, m.w * dx * 0.095)
        h = max(2.0, m.h * dx * 0.045)
        cielo = con_alfa(iluminar(mezclar(nivel.niebla, 0xFF8090A0, 0.4), luz * 0.7), 0.82)
        lz.fill(int(cx - w), int(cy - h * 0.35), int(cx + w * 0.72), int(cy + h), cielo)
        lz.fill(int(cx + w * 0.72), int(cy - h * 0.10), int(cx + w), int(cy + h * 0.65), cielo)
        lz.fill(int(cx - w), int(cy + h), int(cx + w), int(cy + h) + 2,
                con_alfa(iluminar(nivel.junta, atenuar(luz, lej)), 0.6))


def tro_alfombra(lz, m, nivel, luz) -> None:
    for y in range(round(m.suelo_en(1.0)), m.alto, PASO):
        dy = m.dy(y + PASO * 0.5)
        if dy <= 1.0:
            continue
        lej = limitar(1.0 / dy, 0.0, 1.0)
        medio = m.w * dy * 0.16
        color = mezclar(nivel.suelo_junta, nivel.luz, 0.22)
        lz.fill(int(m.centro(dy) - medio), y, int(m.centro(dy) + medio), y + PASO,
                con_alfa(iluminar(color, atenuar(luz, lej)), 0.35))
        lz.fill(int(m.centro(dy) - medio), y, int(m.centro(dy) - medio + 1), y + PASO,
                con_alfa(iluminar(nivel.luz, atenuar(luz, lej)), 0.35))
        lz.fill(int(m.centro(dy) + medio - 1), y, int(m.centro(dy) + medio), y + PASO,
                con_alfa(iluminar(nivel.luz, atenuar(luz, lej)), 0.35))


def tro_trono(lz, m, nivel, luz, tiempo) -> None:
    dx = TRO_TARIMA
    cx = m.centro(dx)
    suelo = m.suelo_en(dx)
    lej = limitar(1.0 / dx, 0.0, 1.0)
    at = atenuar(luz, lej)
    # El estrado crece con el trono: mas ancho y mas alto que antes, para que
    # la silueta domine el abside sin llegar a taparlo.
    ancho_base = m.ancho_en(dx) * 0.40
    alto_esc = m.h * dx * 0.048
    oro = iluminar(velar(nivel.luz, nivel.niebla, lej, 0.18), min(1.0, at * 1.15))
    oro_vivo = iluminar(nivel.luz, min(1.0, at + 0.25))
    sombra = iluminar(velar(nivel.pared_baja, nivel.niebla, lej, 0.45), at * 0.48)

    # La tarima: seis escalones anchos que suben al trono, cada uno con su
    # canto iluminado. Se lee como un estrado, no como un cajon.
    escalones = 6
    for e in range(escalones):
        w = ancho_base * (1.0 - e * 0.10)
        y_top = suelo - alto_esc * (e + 1)
        col = iluminar(velar(nivel.pared_alta, nivel.niebla, lej, 0.4), at * (0.62 + e * 0.07))
        lz.fill(int(cx - w), int(y_top), int(cx + w), int(suelo - alto_esc * e), col)
        lz.fill(int(cx - w), int(y_top), int(cx + w), int(y_top) + 2,
                con_alfa(oro_vivo, 0.45))
    tro_cantos_gastados(lz, cx, suelo, ancho_base, alto_esc, escalones, nivel, at)

    base = suelo - alto_esc * escalones
    # Proporciones del trono: alto y presente, dominando el abside.
    at_w = ancho_base * 0.60
    respaldo = m.h * dx * 0.74
    asiento_h = m.h * dx * 0.16
    brazo_h = m.h * dx * 0.20

    # Sombra proyectada del trono sobre el abside.
    lz.fill(int(cx - at_w * 0.5) - 2, int(base - respaldo), int(cx + at_w * 0.5) + 2, int(base),
            con_alfa(0x000000, 0.28))

    # Respaldo alto (interior en sombra).
    lz.fill(int(cx - at_w * 0.5), int(base - respaldo), int(cx + at_w * 0.5), int(base), sombra)
    # Montantes dorados gruesos a los lados del respaldo.
    mont = max(2, int(at_w * 0.10))
    lz.fill(int(cx - at_w * 0.5), int(base - respaldo), int(cx - at_w * 0.5) + mont, int(base), oro)
    lz.fill(int(cx + at_w * 0.5) - mont, int(base - respaldo), int(cx + at_w * 0.5), int(base), oro)
    # Nervaduras verticales del respaldo (tres, tenues).
    for r in range(1, 4):
        rx = cx - at_w * 0.5 + at_w * r / 4.0
        lz.fill(int(rx), int(base - respaldo * 0.92), int(rx) + 1, int(base - asiento_h),
                con_alfa(oro, 0.35))
    # Remate coronado: tres picos, el del medio con un HUECO (la corona que no esta).
    pico_y = int(base - respaldo)
    for (fx, ph) in ((-0.5, 0.06), (0.0, 0.13), (0.5, 0.06)):
        px = cx + at_w * fx
        h_pico = m.h * dx * ph
        lz.fill(int(px) - mont // 2, int(pico_y - h_pico), int(px) + mont // 2 + 1, pico_y + 1, oro)
    # El hueco de la corona: un vacio oscuro y ancho donde la corona deberia
    # estar. Es el detalle narrativo de la escena: algo estuvo ocupado aqui y
    # ya no. Dos pasos de arco para que no parezca un rectangulo de tinta.
    hx = int(cx)
    hy = int(pico_y - m.h * dx * 0.11)
    hw = max(3, int(at_w * 0.17))
    vacio = con_alfa(0x000000, 0.72)
    lz.fill(hx - hw, hy, hx + hw, hy + hw * 2, vacio)
    lz.fill(hx - hw + 1, hy - 1, hx + hw - 1, hy + 1, vacio)
    # Asiento.
    lz.fill(int(cx - at_w * 0.5), int(base - asiento_h - brazo_h), int(cx + at_w * 0.5),
            int(base - brazo_h), sombra)
    lz.fill(int(cx - at_w * 0.5), int(base - asiento_h - brazo_h), int(cx + at_w * 0.5),
            int(base - asiento_h - brazo_h) + 2, oro)
    # Brazos: dos bloques dorados macizos a los lados del asiento.
    brazo_w = max(2, int(at_w * 0.14))
    lz.fill(int(cx - at_w * 0.5), int(base - asiento_h - brazo_h),
            int(cx - at_w * 0.5) + brazo_w, int(base - brazo_h * 0.2), oro)
    lz.fill(int(cx + at_w * 0.5) - brazo_w, int(base - asiento_h - brazo_h),
            int(cx + at_w * 0.5), int(base - brazo_h * 0.2), oro)
    # Cojin del asiento: un toque del color de la alfombra, gastado.
    coj = iluminar(velar(mezclar(nivel.suelo_junta, nivel.luz, 0.3), nivel.niebla, lej, 0.4), at * 0.7)
    lz.fill(int(cx - at_w * 0.5) + brazo_w, int(base - asiento_h - brazo_h * 0.55),
            int(cx + at_w * 0.5) - brazo_w, int(base - brazo_h * 0.55), coj)


def tro_cantos_gastados(lz, cx, suelo, ancho_base, alto_esc, escalones, nivel, at) -> None:
    """Desgaste corto en los escalones y un unico brillo de metal gastado."""
    desgaste = iluminar(velar(nivel.suelo_junta, nivel.niebla, 0.70, 0.45), at * 0.80)
    brillo = con_alfa(iluminar(nivel.luz, at), 0.30)
    for e in range(escalones):
        w = ancho_base * (1.0 - e * 0.11)
        y = int(suelo - alto_esc * (e + 1))
        largo = max(2, int(w * (0.10 + pseudo(870 + e) * 0.16)))
        x = int(cx - w + w * (0.16 + pseudo(880 + e) * 0.28))
        lz.fill(x, y, x + largo, y + 2, con_alfa(desgaste, 0.50))
        if e == 2:
            lz.fill(int(cx + w * 0.30), y, int(cx + w * 0.30) + max(2, largo // 2), y + 1, brillo)


def tro_sillares(lz, m, nivel, luz) -> None:
    for x in range(0, m.ancho, PASO):
        dx = m.dx(x + PASO * 0.5)
        if dx <= 1.0:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        y0, y1 = m.techo_en(dx), m.suelo_en(dx)
        hiladas = 6
        for k in range(1, hiladas):
            f = k / hiladas
            y = int(y0 + (y1 - y0) * f)
            desvio = pseudo(int(dx * 139.0) + k * 31 + x // 7) * 0.10 - 0.05
            lz.fill(x, y, x + PASO, y + 1,
                    con_alfa(iluminar(nivel.junta, at * (0.9 + desvio)), 0.26 * lej + 0.10))
    t_juntas(lz, m, nivel, luz, TRO_TRAMOS, 1.0, 0.28)


def tro_columnas(lz, m, nivel, luz) -> None:
    for j in range(2, TRO_TRAMOS + 1, 2):
        dx = profundidad(j, TRO_TRAMOS)
        if dx > 5.5:
            continue
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        ancho = max(2.0, m.w * dx * 0.05)
        y_techo = m.techo_en(dx * 0.95)
        y_suelo = m.suelo_en(dx)
        rota = pseudo(400 + j) < 0.35
        y_tope = y_techo + (y_suelo - y_techo) * (0.35 + pseudo(420 + j) * 0.2) if rota else y_techo
        for signo in (-1, 1):
            x = m.lado(signo, dx * TRO_HILERA)
            if x < -ancho * 2 or x > m.ancho + ancho * 2:
                continue
            frente = iluminar(velar(nivel.pared_alta, nivel.niebla, lej, 0.45), at * 0.9)
            costado = iluminar(velar(nivel.pared_baja, nivel.niebla, lej, 0.5), at * 0.55)
            corte = ancho * 0.4 * (1 if signo < 0 else -1)
            lz.fill(int(x - ancho), int(y_tope), int(x + corte), int(y_suelo),
                    costado if signo < 0 else frente)
            lz.fill(int(x + corte), int(y_tope), int(x + ancho), int(y_suelo),
                    frente if signo < 0 else costado)
            if rota:
                lz.fill(int(x - ancho), int(y_tope), int(x + corte), int(y_tope) + max(1, int(ancho * 0.4)),
                        iluminar(velar(nivel.junta, nivel.niebla, lej, 0.4), at * 0.7))
            else:
                lz.fill(int(x - ancho * 1.3), int(y_techo), int(x + ancho * 1.3), int(y_techo + m.h * dx * 0.05),
                        iluminar(velar(nivel.junta, nivel.niebla, lej, 0.4), at * 0.8))


def tro_estandartes(lz, m, nivel, luz, tiempo) -> None:
    for j in range(3, TRO_TRAMOS + 1, 3):
        if pseudo(600 + j) > 0.55:
            continue
        dx = profundidad(j, TRO_TRAMOS)
        if dx > 5.0:
            continue
        signo = -1 if pseudo(610 + j) < 0.5 else 1
        lej = limitar(1.0 / dx, 0.0, 1.0)
        at = atenuar(luz, lej)
        x = m.lado(signo, dx * (TRO_HILERA - 0.04))
        if x < -20 or x > m.ancho + 20:
            continue
        ancho = max(3.0, m.w * dx * 0.05)
        y_top = m.techo_en(dx * 0.72)
        alto = m.h * dx * 0.48
        onda = math.sin(tiempo * 0.5 + j) * ancho * 0.16
        # Uno de cada tres cuelga torcido: el asta se descolgo y la tela se va
        # desplazando hacia abajo. Rompe la fila de panios alineados.
        torcido = pseudo(615 + j) < 0.30
        sesgo = ancho * 0.55 if torcido else 0.0
        # La cuerda del techo al asta: el estandarte no cuelga de la nada.
        y_techo = m.techo_en(dx * 0.95)
        lz.fill(int(x), int(y_techo), int(x) + 1, int(y_top),
                con_alfa(iluminar(nivel.junta, at * 0.55), 0.50))
        lz.fill(int(x - ancho * 0.5), int(y_top) - 1, int(x + ancho * 0.5), int(y_top),
                con_alfa(iluminar(nivel.junta, at * 0.70), 0.80))
        # Tela: panio oscuro, no una banda de oro. El oro es solo el galon
        # y el emblema, que es lo que dice "estandarte" en una sola mirada.
        tela = iluminar(velar(mezclar(nivel.pared_baja, nivel.junta, 0.35), nivel.niebla, lej, 0.4), at * 0.85)
        for k in range(8):
            f = k / 8.0
            w = ancho * (1.0 - f * 0.5)
            ox = onda * f + sesgo * f
            lz.fill(int(x - w * 0.5 + ox), int(y_top + alto * f), int(x + w * 0.5 + ox), int(y_top + alto * (f + 0.14)),
                    con_alfa(tela, 0.85 * (1.0 - f * 0.3)))
        # Galon superior dorado.
        lz.fill(int(x - ancho * 0.5), int(y_top), int(x + ancho * 0.5), int(y_top + max(1, int(alto * 0.06))),
                con_alfa(iluminar(nivel.luz, at), 0.55))
        # Emblema: un rombo tenue en el centro del panio.
        ey = y_top + alto * 0.40
        lz.fill(int(x - ancho * 0.18), int(ey), int(x + ancho * 0.18), int(ey + alto * 0.14),
                con_alfa(iluminar(nivel.luz, at * 0.8), 0.30))


def tro_haces(lz, m, nivel, luz, tiempo) -> None:
    for i in range(4):
        frac = (pseudo(i * 17) - 0.5) * 1.4
        dx_top = 2.2 + i * 1.4
        x_top = m.en_x(dx_top, frac)
        y_top = m.techo_en(dx_top * 0.4)
        y_bot = m.suelo_en(dx_top)
        lej = limitar(1.0 / dx_top, 0.0, 1.0)
        parpadeo = 0.8 + 0.2 * math.sin(tiempo * 0.4 + i)
        a = 0.05 * luz * (0.5 + 0.5 * lej) * parpadeo
        pasos = 12
        ancho = max(2.0, m.w * dx_top * 0.04)
        for k in range(pasos):
            t = k / pasos
            x = x_top + (m.en_x(dx_top, frac * 0.7) - x_top) * t
            y = y_top + (y_bot - y_top) * t
            lz.fill(int(x - ancho * (1.0 + t)), int(y), int(x + ancho * (1.0 + t)), int(y) + PASO * 2,
                    con_alfa(iluminar(0xFFFFF0C0, luz), a * (1.0 - t * 0.5)))


def pp_losa_rota(lz, w, h, piedra, piedra_luz, piedra_sombra, luz) -> None:
    """Losa escalonada del borde derecho; aporta oclusion sin cerrar el eje."""
    x0 = round(w * 0.76)
    y0 = round(h * 0.79)
    ancho = max(18, round(w * 0.22))
    alto = max(3, round(h * 0.034))
    for i in range(4):
        margen = round(w * (0.012 * i))
        izquierda = x0 - margen
        derecha = min(w, x0 + ancho - margen * 2)
        arriba = y0 + round(h * 0.025 * i)
        lz.fill(izquierda, arriba, derecha, arriba + alto,
                iluminar(piedra_sombra if i == 1 else piedra, 0.52 + 0.10 * luz))
        lz.fill(izquierda, arriba, derecha - max(1, w // 90), arriba + 1,
                con_alfa(piedra_luz, 0.30))
    grieta = max(1, w // 240)
    for i in range(5):
        x = x0 + round(w * 0.055) + i * max(2, w // 110)
        y = y0 - max(1, h // 150) + i * max(1, h // 180)
        lz.fill(x, y, x + grieta, y + max(2, h // 42), con_alfa(0x000000, 0.58))


def pp_trono(lz, m, nivel, luz, tiempo) -> None:
    """Escombro bajo del primer plano, dejando libre el trono.

    El tambor de columna diagonal del renderer anterior cerraba todo el borde
    inferior y competia con el asiento vacio. El espejo conserva profundidad
    con un zocalo irregular y dos pequenos derrumbes laterales.
    """
    w, h = m.ancho, m.alto
    piedra = mezclar(nivel.pared_baja, 0x000000, 0.35)
    piedra_luz = iluminar(mezclar(nivel.pared_alta, 0x000000, 0.10), 0.55 + 0.30 * luz)
    piedra_sombra = iluminar(mezclar(piedra, 0x000000, 0.4), 0.30 + 0.15 * luz)

    pp_losa_rota(lz, w, h, piedra, piedra_luz, piedra_sombra, luz)

    # Zocalo bajo, estable e irregular: ancla la camara sin tapar el estrado.
    paso = max(3, PASO)
    for x in range(0, w, paso):
        ruido = pseudo(900 + x // paso)
        y = int(h * (0.90 + ruido * 0.045))
        lz.fill(x, y, min(w, x + paso + 1), h,
                iluminar(piedra, 0.42 + 0.14 * luz))
        lz.fill(x, y, min(w, x + paso + 1), y + 1,
                con_alfa(piedra_luz, 0.35))

    # Derrumbes laterales: escala y ruina, pero eje central despejado.
    for lado in (-1, 1):
        cx = int(w * 0.10) if lado < 0 else int(w * 0.90)
        for i in range(3):
            ancho = max(4, int(w * (0.045 + i * 0.012)))
            alto = max(3, int(h * (0.035 + i * 0.012)))
            x = cx + lado * (i * ancho // 2) - ancho // 2
            y = int(h * (0.84 + i * 0.025))
            lz.fill(x, y, x + ancho, min(h, y + alto),
                    iluminar(piedra_sombra, 0.8 + 0.1 * luz))
            lz.fill(x, y, x + ancho, y + 1, con_alfa(piedra_luz, 0.42))

    # Seis cascotes bajos: acentos separados, nunca una segunda pared.
    for i in range(6):
        posicion = 0.28 + pseudo(940 + i) * 0.44
        ancho = max(2, int(w * (0.012 + pseudo(950 + i) * 0.022)))
        alto = max(2, int(h * (0.012 + pseudo(960 + i) * 0.018)))
        x = int(w * posicion)
        y = int(h * (0.91 + pseudo(970 + i) * 0.045))
        lz.fill(x, y, x + ancho, min(h, y + alto),
                iluminar(piedra, 0.45 + 0.18 * luz))


PRIMEROS_PLANOS = {
    "sala": pp_sala,
    "nave": pp_nave,
    "servicio": pp_servicio,
    "natatorio": pp_natatorio,
    "cripta": pp_cripta,
    "biblioteca": pp_biblioteca,
    "invernadero": pp_invernadero,
    "catacumba": pp_catacumba,
    "cisterna": pp_cisterna,
    "trono": pp_trono,
}


PLANTAS = {"sala": sala, "nave": nave, "servicio": servicio, "natatorio": natatorio,
           "cripta": cripta, "biblioteca": biblioteca, "invernadero": invernadero,
           "catacumba": catacumba, "cisterna": cisterna, "trono": trono}
PISO_PRESENCIA = {"sala": 0.94, "nave": 1.30, "servicio": 0.98, "natatorio": 1.18,
                  "cripta": 0.98, "biblioteca": 0.96, "invernadero": 0.96,
                  "catacumba": 0.97, "cisterna": 1.00, "trono": 0.98}


# --------------------------------------------------------------------------
# La presencia del fondo: espejo de Presencia.java
# --------------------------------------------------------------------------
# La figura vieja caminaba de un lado al otro del vano con las piernas
# alternando. Se leia como un personaje, cruzaba el centro de la composicion y
# a la tercera pasada dejaba de dar impresion. La que la reemplaza no se mueve,
# no tiene anatomia, esta lejos y entra y sale con una campana lenta.
#
# Aca solo se replica el dibujo. El reloj de 71 segundos que decide cuando
# aparece vive en Java: la vista previa recibe la visibilidad ya calculada,
# para poder mirar el instante que interese sin esperarlo.

SEGMENTOS_PRESENCIA = 14
ALFA_MAXIMO_PRESENCIA = 0.52
# Proporciones del cuerpo, en fracciones de la abertura del fondo. Un
# rectangulo de 1:14 se lee como una grieta en la pared; 1:5 se lee como algo
# que podria estar parado ahi. La ambiguedad esta en esa proporcion.
ANCHO_PRESENCIA = 0.26
ALTURA_PRESENCIA = 1.35


def luminancia(color: int) -> float:
    r = (color >> 16) & 0xFF
    g = (color >> 8) & 0xFF
    b = color & 0xFF
    return (0.299 * r + 0.587 * g + 0.114 * b) / 255.0


def color_presencia(nivel) -> int:
    """De que color es algo que esta contra un fondo negro.

    Pintarla siempre de negro parecia lo correcto y no lo es: en los niveles
    con la abertura del fondo casi negra (el 0 y el 2) la figura se pierde por
    completo, y en los claros se lee como una grieta en la pared. La respuesta
    es no fijar el color sino derivarlo del vano de cada nivel: contra un
    fondo oscuro la presencia es un poco MAS clara que el, como una silueta a
    contraluz; contra un fondo claro es mas oscura. En los dos casos el
    contraste es el mismo y en ninguno se la ve del todo.
    """
    if luminancia(nivel.fondo) < 0.16:
        return mezclar(nivel.fondo, nivel.niebla, 0.30)
    return VANO


# Modos de manifestacion. El defecto de la version anterior no era el dibujo
# sino que SIEMPRE PASABA LO MISMO: la figura se asomaba de la misma forma, con
# la misma silueta y el mismo desvanecido, cada 71 segundos. A la tercera vez el
# ojo ya sabe que esperar y deja de mirar. Lo que provoca el "que acabo de ver"
# no es la silueta: es no poder anticipar QUE va a pasar.
#
#   0 QUIETA      esta ahi y se desvanece. La de siempre, la mas frecuente.
#   1 CORTE       aparece y desaparece dentro de un parpadeo del fluorescente,
#                 tres o cuatro cuadros. Apenas alcanza a registrarse.
#   2 SUMERGIDA   solo el reflejo en el agua. Arriba no hay nada parado. Es el
#                 modo que mas incomoda: hay reflejo de algo que no esta.
#   3 DOBLE       dos siluetas a la vez, en los dos costados del vano.
MODO_QUIETA, MODO_CORTE, MODO_SUMERGIDA, MODO_DOBLE = 0, 1, 2, 3


def modo_presencia(ciclo: int) -> int:
    """Que modo toca en este ciclo. Sesgado: lo raro tiene que ser raro."""
    r = pseudo(ciclo * 31 + 7)
    if r < 0.52:
        return MODO_QUIETA
    if r < 0.74:
        return MODO_CORTE
    if r < 0.90:
        return MODO_SUMERGIDA
    return MODO_DOBLE


def presencia(lz, nivel, m, visible, luz, segunda=False, tiempo=0.0,
              piso=0.94, modo=MODO_QUIETA) -> None:
    """Lo que a veces esta al fondo del recinto."""
    visible = limitar(visible * luz, 0.0, 1.0)
    if visible <= 0.01:
        return

    # En el modo corte la campana se estrangula: la figura solo existe en el
    # pico y los flancos se comen todo lo demas.
    if modo == MODO_CORTE:
        visible = limitar((visible - 0.72) / 0.28, 0.0, 1.0)
        if visible <= 0.01:
            return

    lado = -0.34 if segunda else 0.41
    w = m.w
    base = m.fy + m.hb * piso
    altura = m.h * ALTURA_PRESENCIA
    alfa = ALFA_MAXIMO_PRESENCIA * visible
    tinte = color_presencia(nivel)

    if modo == MODO_DOBLE:
        # Dos siluetas, la segunda mas tenue y mas baja: no son gemelas, es
        # una y "otra cosa" que se le parece.
        posiciones = ((lado, 1.0, 1.0), (-lado * 0.86, 0.62, 0.88))
    else:
        posiciones = ((lado, 1.0, 1.0),)

    for frac, peso, escala in posiciones:
        x = m.en_x(1.0, frac)
        vaiven = math.sin(tiempo * 0.55 + frac) * (w * 0.012)

        # En sumergida no hay cuerpo: solo lo que devuelve el agua.
        if modo != MODO_SUMERGIDA:
            cuerpo_presencia(lz, x + vaiven, base, altura * escala, w,
                             alfa * peso, tinte)

        if nivel.reflejo > 0.20:
            fuerza = 0.85 if modo != MODO_SUMERGIDA else 1.35
            reflejo_presencia(lz, x + vaiven, base, altura * escala, w,
                              alfa * peso * nivel.reflejo * fuerza, tiempo, tinte)


def cuerpo_presencia(lz, x, base, altura, w, alfa, tinte) -> None:
    """Una columna que se afina hacia arriba. Sin cabeza y sin hombros."""
    ancho_pie = max(3.0, w * ANCHO_PRESENCIA)

    for i in range(SEGMENTOS_PRESENCIA):
        desde = i / SEGMENTOS_PRESENCIA
        hasta = (i + 1) / SEGMENTOS_PRESENCIA

        y0 = base - altura * hasta
        y1 = base - altura * desde
        if y1 - y0 < 1.0:
            y1 = y0 + 1.0

        estrechez = 1.0 - 0.55 * desde ** 1.6
        ancho = max(1.4, ancho_pie * estrechez)
        torcion = math.sin(desde * 2.2 + 0.6) * ancho_pie * 0.10
        desvanecido = alfa * (1.0 - 0.42 * desde ** 2.2)

        lz.fill(round(x - ancho * 0.5 + torcion), round(y0),
                round(x + ancho * 0.5 + torcion), round(y1),
                con_alfa(tinte, min(0.95, desvanecido)))


def reflejo_presencia(lz, x, base, altura, w, alfa, tiempo, tinte) -> None:
    """El reflejo en el suelo mojado: estirado, deshecho y mucho mas tenue."""
    largo = altura * 0.70
    tramos = 9

    for i in range(tramos):
        desde = i / tramos
        y0 = base + largo * desde
        y1 = base + largo * (i + 1) / tramos

        ondulacion = math.sin(tiempo * 1.3 + i * 0.9) * w * 0.05 * desde
        ancho = max(1.4, w * ANCHO_PRESENCIA * (1.0 + desde * 0.8))
        desvanecido = alfa * (1.0 - desde) * (1.0 - desde)
        if desvanecido < 0.01:
            continue

        lz.fill(round(x - ancho * 0.5 + ondulacion), round(y0),
                round(x + ancho * 0.5 + ondulacion), round(max(y1, y0 + 1)),
                con_alfa(tinte, desvanecido))


def motas(lz, fx, fy, tiempo, luz, nivel) -> None:
    for i in range(MOTAS):
        base_x = pseudo(i * 7)
        base_y = pseudo(i * 7 + 1)
        velocidad = 0.08 + pseudo(i * 7 + 2) * 0.25
        deriva = math.sin(tiempo * (0.20 + pseudo(i * 7 + 3) * 0.35) + i) * 0.010
        y = (base_y + tiempo * velocidad * 0.040) % 1.0
        x = (base_x + deriva) % 1.0
        px = int(x * lz.ancho)
        py = int(y * lz.alto)
        tam = 1 if pseudo(i * 7 + 4) < 0.82 else 2
        a = (0.07 + pseudo(i * 7 + 5) * 0.17) * luz
        color = nivel.luz if i % 4 == 0 else FLUOR
        lz.fill(px, py, px + tam, py + tam, con_alfa(color, a))


def vineta(lz, nivel, penumbra, luz) -> None:
    franja = max(8, lz.ancho // 6)
    intensidad = 0.36 + 0.43 * penumbra
    paso = 3
    x = 0
    while x < franja:
        t = 1.0 - x / franja
        a = intensidad * t * t
        lz.fill(x, 0, min(franja, x + paso), lz.alto, con_alfa(VANO, a))
        lz.fill(max(0, lz.ancho - x - paso), 0, lz.ancho - x, lz.alto, con_alfa(VANO, a))
        x += paso
    franja_v = max(6, lz.alto // 7)
    y = 0
    while y < franja_v:
        t = 1.0 - y / franja_v
        a = intensidad * 0.72 * t * t
        lz.fill(0, y, lz.ancho, min(franja_v, y + paso), con_alfa(VANO, a))
        lz.fill(0, max(0, lz.alto - y - paso), lz.ancho, lz.alto - y, con_alfa(VANO, a))
        y += paso


# --------------------------------------------------------------------------
# Hoja del aviso: bloque aproximado, solo para juzgar la composicion
# --------------------------------------------------------------------------
def hoja(lz: Lienzo) -> None:
    """La hoja con la metrica REAL de PantallaNivel, no una aproximacion.

    Los numeros de aca son los mismos que los de la pantalla Java. Cuando eran
    distintos -alto fijo de 208, renglones cada 23 px- esta vista previa decia
    que la composicion estaba bien mientras en el juego la nota al pie caia
    encima del ultimo boton. Una vista previa que miente es peor que no tenerla.
    """
    ANCHO_HOJA = 214
    MARGEN = 12
    LINEA = 11
    ALTO_TITULO = 18
    AIRE_TITULO = 4
    AIRE_REGLA = 7
    AIRE_CABECERA = 14
    AIRE_PIE = 16
    ALTO_RENGLON = 20
    SEPARACION = 3
    HUECO_APARTE = 10
    MARGEN_PANTALLA = 12

    # Cabecera: titulo + subtitulo (2 lineas en ingles) + regla + dos parrafos.
    cabecera = ALTO_TITULO + AIRE_TITULO + 2 * LINEA + AIRE_REGLA + 1 + AIRE_REGLA
    cabecera += LINEA + 2 * LINEA
    salto = ALTO_RENGLON + SEPARACION
    lista = 3 * salto + HUECO_APARTE + ALTO_RENGLON
    aviso = 3 * LINEA + 2
    alto_hoja = (MARGEN + cabecera + AIRE_CABECERA + lista
                 + AIRE_PIE + aviso + MARGEN)

    x0 = max(14, int(lz.ancho * 0.07))
    disponible = lz.alto - 2 * MARGEN_PANTALLA
    if alto_hoja > disponible:
        y0 = MARGEN_PANTALLA
    else:
        y0 = max(MARGEN_PANTALLA,
                 min(int(lz.alto * 0.13), lz.alto - MARGEN_PANTALLA - alto_hoja))
    x1 = x0 + ANCHO_HOJA
    y1 = y0 + alto_hoja
    ancho_util = ANCHO_HOJA - 2 * MARGEN

    lz.fill(x0 + 3, y0 + 4, x1 + 3, y1 + 4, con_alfa(VANO, 0.30))
    lz.fill(x0, y0, x1, y1, con_alfa(PAPEL, 0.94))
    for bx, by, bx2, by2 in ((x0, y0, x1, y0 + 1), (x0, y1 - 1, x1, y1),
                             (x0, y0, x0 + 1, y1), (x1 - 1, y0, x1, y1)):
        lz.fill(bx, by, bx2, by2, con_alfa(0xFF5E5222, 0.40))
    centro = (x0 + x1) // 2
    lz.fill(centro - 22, y0 - 4, centro + 22, y0 + 4, con_alfa(PAPEL, 0.45))

    x = x0 + MARGEN
    y = y0 + MARGEN
    lz.fill(x, y, x + 46, y + ALTO_TITULO - 2, con_alfa(TINTA, 0.92))
    y += ALTO_TITULO + AIRE_TITULO
    for i in range(2):
        ancho_linea = ancho_util if i == 0 else int(ancho_util * 0.32)
        lz.fill(x, y + 1, x + ancho_linea, y + 8, con_alfa(TINTA_TENUE, 0.55))
        y += LINEA
    y += AIRE_REGLA
    lz.fill(x, y, x + ancho_util, y + 1, con_alfa(TINTA_TENUE, 0.45))
    y += 1 + AIRE_REGLA
    lz.fill(x, y + 1, x + int(ancho_util * 0.88), y + 8, con_alfa(TINTA_TENUE, 0.55))
    y += LINEA
    for i in range(2):
        ancho_linea = ancho_util if i == 0 else int(ancho_util * 0.18)
        lz.fill(x, y + 1, x + ancho_linea, y + 8, con_alfa(TINTA, 0.70))
        y += LINEA

    y = y0 + MARGEN + cabecera + AIRE_CABECERA
    for i in range(4):
        fy = y + i * salto + (HUECO_APARTE if i == 3 else 0)
        lz.fill(x, fy + 6, x + 7, fy + 13, con_alfa(TINTA_TENUE, 0.70))
        lz.fill(x + 32, fy + 6, x + 32 + 110, fy + 14, con_alfa(TINTA_TENUE, 0.75))
        px = x + 32 + 114
        while px < x + ancho_util - 2:
            lz.fill(px, fy + 12, px + 1, fy + 13, con_alfa(TINTA_TENUE, 0.30))
            px += 3

    # El atajo de servicio ya no se dibuja: queda fuera del flujo normal.
    # El aviso rotativo va directo debajo de la lista.
    y_aviso = y + lista + AIRE_PIE
    for i in range(3):
        ancho_linea = ancho_util if i < 2 else int(ancho_util * 0.55)
        lz.fill(x, y_aviso + i * LINEA + 1, x + ancho_linea,
                y_aviso + i * LINEA + 8, con_alfa(TINTA_TENUE, 0.42))


# --------------------------------------------------------------------------
def render(ancho: int, alto: int, nivel: Nivel, con_hoja: bool = True, **kw) -> Lienzo:
    lz = Lienzo(ancho, alto)
    dibujar(lz, nivel, **kw)
    if con_hoja:
        hoja(lz)
    return lz


def main() -> int:
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    banderas = [a for a in sys.argv[1:] if a.startswith("--")]

    desnudo = "--desnudo" in banderas   # sin la hoja del aviso encima

    if "--contacto" in banderas:
        salida = Path(args[0]) if args else Path("docs/vista_previa.png")
        ancho, alto = 480, 270
        # La grilla se adapta a cuantos niveles haya: dos columnas y las filas
        # que hagan falta. Con cinco niveles quedan tres filas (la ultima con
        # una sola escena) en vez de reventar el indice del lienzo.
        cols = 2
        filas = (len(NIVELES) + cols - 1) // cols
        tira = Lienzo(ancho * cols, alto * filas)
        for i, nv in enumerate(NIVELES):
            # La presencia se muestra en el nivel 3, que es donde mas se
            # nota por el reflejo, y no en el 0, que es la postal del servidor.
            sub = render(ancho, alto, nv, con_hoja=not desnudo, tiempo=3.0 + i,
                         presencia_v=1.0 if i == 3 else 0.0)
            ox = (i % cols) * ancho
            oy = (i // cols) * alto
            for y in range(alto):
                for x in range(ancho):
                    tira.pix[(oy + y) * tira.ancho + ox + x] = sub.pix[y * ancho + x]
        salida.parent.mkdir(parents=True, exist_ok=True)
        tira.png(salida)
        print(f"{salida}  {tira.ancho}x{tira.alto}  ({len(NIVELES)} niveles)")
        return 0

    if "--eventos" in banderas:
        # Tira de eventos ambientales: cada recinto en el pico de su evento,
        # para juzgar si se ven bien sin esperar el ciclo real de 61 s.
        salida = Path(args[0]) if args else Path("docs/eventos.png")
        ancho, alto = 480, 270
        cols = 2
        filas = (len(NIVELES) + cols - 1) // cols
        tira = Lienzo(ancho * cols, alto * filas)
        for i, nv in enumerate(NIVELES):
            sub = render(ancho, alto, nv, con_hoja=False,
                         tiempo=3.0 + i * 0.7, evento_forzado=0.5)
            ox = (i % cols) * ancho
            oy = (i // cols) * alto
            for y in range(alto):
                for x in range(ancho):
                    tira.pix[(oy + y) * tira.ancho + ox + x] = sub.pix[y * ancho + x]
        salida.parent.mkdir(parents=True, exist_ok=True)
        tira.png(salida)
        print(f"{salida}  {tira.ancho}x{tira.alto}  ({len(NIVELES)} eventos)")
        return 0

    if "--presencia" in banderas:
        # Tira de la manifestacion completa: los seis instantes de la campana,
        # para poder juzgar la entrada y la salida sin esperar 71 segundos.
        salida = Path(args[0]) if args else Path("docs/presencia.png")
        indice = 3
        for b in banderas:
            if b.startswith("--nivel"):
                indice = int(b.split("=")[1]) if "=" in b else 3
        nv = NIVELES[indice % len(NIVELES)]

        # Una fila por modo de manifestacion, en su pico y a media entrada.
        pasos = [(1.00, False, MODO_QUIETA), (0.62, False, MODO_QUIETA),
                 (1.00, False, MODO_CORTE), (1.00, False, MODO_SUMERGIDA),
                 (1.00, False, MODO_DOBLE), (0.85, True, MODO_DOBLE)]
        ancho, alto = 420, 236
        tira = Lienzo(ancho * 3, alto * 2)
        for i, (visible, segunda, modo) in enumerate(pasos):
            sub = render(ancho, alto, nv, con_hoja=not desnudo,
                         tiempo=3.0 + i * 0.7,
                         presencia_v=visible, presencia_segunda=segunda,
                         presencia_modo=modo)
            ox = (i % 3) * ancho
            oy = (i // 3) * alto
            for y in range(alto):
                for x in range(ancho):
                    tira.pix[(oy + y) * tira.ancho + ox + x] = sub.pix[y * ancho + x]
        salida.parent.mkdir(parents=True, exist_ok=True)
        tira.png(salida)
        print(f"{salida}  {tira.ancho}x{tira.alto}  presencia sobre {nv.clave}")
        return 0

    ancho = int(args[0]) if len(args) > 0 else 854
    alto = int(args[1]) if len(args) > 1 else 480
    salida = Path(args[2]) if len(args) > 2 else Path("docs/vista_previa.png")
    indice = 0
    visible = 0.0
    for b in banderas:
        if b.startswith("--nivel"):
            indice = int(b.split("=")[1]) if "=" in b else 0
        if b.startswith("--figura"):
            visible = float(b.split("=")[1]) if "=" in b else 1.0

    lz = render(ancho, alto, NIVELES[indice % len(NIVELES)],
                con_hoja=not desnudo, presencia_v=visible)
    salida.parent.mkdir(parents=True, exist_ok=True)
    lz.png(salida)
    print(f"{salida}  {ancho}x{alto}  nivel={NIVELES[indice % len(NIVELES)].clave}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
