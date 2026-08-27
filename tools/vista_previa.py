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
"""

from __future__ import annotations

import math
import struct
import sys
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
MOTAS = 70


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
    v = 0.90 + 0.035 * math.sin(tiempo * 1.7) + 0.020 * math.sin(tiempo * 5.9 + 1.3)
    if int(tiempo * 3.0) % 97 == 0:
        v *= 0.62
    return limitar(v, 0.45, 1.0)


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
            primer_plano: bool = True) -> None:
    fx = lz.ancho * nivel.fuga_x
    fy = lz.alto * nivel.fuga_y
    m = Marco(lz.ancho, lz.alto, fx, fy,
              lz.ancho * nivel.semi_izq, lz.ancho * nivel.semi_der,
              lz.ancho * nivel.semi_alto, lz.ancho * nivel.semi_bajo)

    luz = brillo_fluorescente(tiempo, destellos) * (1.0 - 0.55 * penumbra) * luz_global
    # La presencia le saca hasta un ocho por ciento a la escena mientras esta,
    # igual que Presencia.sombra(). Nadie lo puede senalar; todo el mundo lo nota.
    luz *= 1.0 - 0.08 * limitar(presencia_v, 0.0, 1.0)
    luz = limitar(luz, 0.0, 1.0)

    PLANTAS[nivel.planta](lz, m, nivel, luz, tiempo)

    # El primer plano va despues del recinto y antes de la presencia: lo que
    # esta cerca tapa lo que esta lejos, y la figura vive dentro del recinto.
    if primer_plano:
        PRIMEROS_PLANOS[nivel.planta](lz, m, nivel, luz, tiempo)

    if presencia_v > 0.0:
        presencia(lz, nivel, m, presencia_v, luz,
                  presencia_segunda, tiempo, PISO_PRESENCIA[nivel.planta],
                  presencia_modo)
    if polvo:
        motas(lz, fx, fy, tiempo, luz)
    vineta(lz, nivel, penumbra, luz)


# --------------------------------------------------------------------------
# Nivel 0 - La sala: espejo de planta/Sala.java
# --------------------------------------------------------------------------
SALA_TRAMOS = 12
SALA_PLACAS = 7


def sala(lz, m, nivel, luz, tiempo) -> None:
    t_fondo(lz, m, nivel, luz, mezclar(nivel.pared_baja, nivel.niebla, 0.20), 1.5)
    sala_puertas(lz, m, nivel, luz)
    t_plano(lz, m, True, nivel.techo, mezclar(nivel.techo, nivel.niebla, 0.35),
            nivel.niebla, luz, 0.50)
    t_plano(lz, m, False, nivel.suelo, nivel.suelo_lejos, nivel.niebla, luz, 0.58)
    sala_alfombra(lz, m, nivel, luz)
    t_transversales(lz, m, False, nivel.suelo_junta, nivel.niebla, luz, SALA_TRAMOS, 0.38)
    t_transversales(lz, m, True, nivel.techo_junta, nivel.niebla, luz, SALA_TRAMOS, 0.46)
    sala_grilla(lz, m, nivel, luz)
    for j in range(2, SALA_TRAMOS + 1):
        dx = profundidad(j, SALA_TRAMOS)
        if dx <= 6.0:
            t_luminaria(lz, m, nivel, dx, 0.90, 0.30, 1.0, luz)
    t_paredes(lz, m, nivel, luz)
    sala_zocalo(lz, m, nivel, luz)
    t_juntas(lz, m, nivel, luz, SALA_TRAMOS, 1.0, 0.45)
    t_manchas(lz, m, nivel, luz, SALA_TRAMOS)
    sala_cuadros(lz, m, nivel, luz)


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
    """La marca mas limpia que dejo el cuadro cuando se lo llevaron."""
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
        for col in range(max(0, x0), min(m.ancho, x1)):
            dxc = m.dx(col + 0.5)
            centro = m.techo_en(1.0 * dxc * 0.30)
            medio = m.h * dxc * 0.22
            lz.fill(col, int(centro - medio), col + 1, int(centro + medio),
                    con_alfa(iluminar(nivel.pared_alta, luz), 0.16 * lej + 0.06))


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
    serv_haz(lz, m, nivel, luz)
    serv_apliques(lz, m, nivel, luz)
    serv_rejillas(lz, m, nivel, luz)


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
    t_plano(lz, m, True, nivel.techo, mezclar(nivel.techo, nivel.niebla, 0.30),
            nivel.niebla, luz, 0.44)
    t_transversales(lz, m, True, nivel.techo_junta, nivel.niebla, luz, NAT_TRAMOS, 0.26)
    nat_claraboyas(lz, m, nivel, luz)
    nat_borde(lz, m, nivel, luz)
    nat_agua(lz, m, nivel, luz, tiempo)
    nat_calles(lz, m, nivel, luz, tiempo)
    nat_reflejo_luces(lz, m, nivel, luz, tiempo)
    t_paredes(lz, m, nivel, luz)
    nat_escalerilla(lz, m, nivel, luz)
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
    t_manchas(lz, m, nivel, luz, CRI_TRAMOS)
    cri_columnas(lz, m, nivel, luz)
    cri_estandartes(lz, m, nivel, luz, tiempo)
    cri_antorchas(lz, m, nivel, luz, tiempo)
    cri_candil(lz, m, nivel, luz, tiempo, pulso)


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
        _linea(lz, round(m.izq(dx)), int(y_pared), cx, int(y_cima), grosor, color)
        _linea(lz, cx, int(y_cima), round(m.der(dx)), int(y_pared), grosor, color)


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
    # Candelabro de mesa.
    velax = int(lz.ancho * 0.30 + balance * 1.4)
    alto = int(lz.alto * 0.10)
    hierro = iluminar(nivel.junta, 0.45 + 0.25 * luz)
    lz.fill(velax - 1, tapa_y - espesor - alto, velax + 2, tapa_y - espesor, con_alfa(hierro, 0.92))
    lz.fill(velax - int(lz.ancho * 0.03), tapa_y - espesor - int(alto * 0.55),
            velax + int(lz.ancho * 0.03), tapa_y - espesor - int(alto * 0.55) + 2, con_alfa(hierro, 0.92))
    for s in (-1, 1):
        vx = velax + s * int(lz.ancho * 0.03)
        vy = tapa_y - espesor - int(alto * 0.55)
        ll = 1.0 + 0.10 * math.sin(tiempo * 13.0 + s)
        for k in range(3, 0, -1):
            t = k / 3.0
            e = lz.ancho * 0.010 * (1.0 + t * 2.2)
            lz.fill(int(vx - e), int(vy - e), int(vx + e), int(vy + e * 0.6),
                    con_alfa(nivel.luz, 0.07 * luz * ll * (1.0 - t * 0.5)))
        lz.fill(vx - 1, vy - int(alto * 0.22), vx + 1, vy,
                con_alfa(iluminar(nivel.pared_alta, 0.7 * luz), 0.9))
        lz.fill(vx - 1, vy - int(alto * 0.30), vx + 1, vy - int(alto * 0.22),
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


PRIMEROS_PLANOS = {
    "sala": pp_sala,
    "nave": pp_nave,
    "servicio": pp_servicio,
    "natatorio": pp_natatorio,
    "cripta": pp_cripta,
}


PLANTAS = {"sala": sala, "nave": nave, "servicio": servicio, "natatorio": natatorio,
           "cripta": cripta}
PISO_PRESENCIA = {"sala": 0.94, "nave": 1.30, "servicio": 0.98, "natatorio": 1.18,
                  "cripta": 0.98}


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


def motas(lz, fx, fy, tiempo, luz) -> None:
    for i in range(MOTAS):
        base_x = pseudo(i * 7)
        base_y = pseudo(i * 7 + 1)
        velocidad = 0.10 + pseudo(i * 7 + 2) * 0.30
        deriva = math.sin(tiempo * (0.25 + pseudo(i * 7 + 3) * 0.4) + i) * 0.012
        y = (base_y + tiempo * velocidad * 0.045) % 1.0
        x = (base_x + deriva) % 1.0
        px = int(x * lz.ancho)
        py = int(y * lz.alto)
        tam = 1 if pseudo(i * 7 + 4) < 0.75 else 2
        a = (0.10 + pseudo(i * 7 + 5) * 0.22) * luz
        lz.fill(px, py, px + tam, py + tam, con_alfa(0xFFFFF7D2, a))


def vineta(lz, nivel, penumbra, luz) -> None:
    franja = max(8, lz.ancho // 6)
    intensidad = 0.38 + 0.42 * penumbra
    paso = 4
    x = 0
    while x < franja:
        t = 1.0 - x / franja
        a = intensidad * t * t
        lz.fill(x, 0, x + paso, lz.alto, con_alfa(VANO, a))
        lz.fill(lz.ancho - x - paso, 0, lz.ancho - x, lz.alto, con_alfa(VANO, a))
        x += paso
    franja_v = max(6, lz.alto // 7)
    y = 0
    while y < franja_v:
        t = 1.0 - y / franja_v
        a = intensidad * 0.75 * t * t
        lz.fill(0, y, lz.ancho, y + paso, con_alfa(VANO, a))
        lz.fill(0, lz.alto - y - paso, lz.ancho, lz.alto - y, con_alfa(VANO, a))
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

    # El atajo de servicio (Ctrl+S) ya no se dibuja: es una herramienta oculta.
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
