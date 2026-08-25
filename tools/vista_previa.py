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
                 fondo, proporcion, semiancho, reflejo, humedad):
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
        self.proporcion = proporcion      # alto del recinto / ancho
        self.semiancho = semiancho        # semiancho de la abertura del fondo
        self.reflejo = reflejo            # cuanto devuelve el suelo (0..1)
        self.humedad = humedad            # manchas en la pared (0..1)


NIVELES = [
    # Nivel 0 - El papel mural. Sala ancha y baja, la postal del servidor.
    Nivel(clave="nivel0", planta="sala",
          pared_alta=0xFFE6D264, pared_baja=0xFF9A8630, junta=0xFF5E5222,
          suelo=0xFF8A7638, suelo_lejos=0xFF6E5C2A, suelo_junta=0xFF4C401E,
          techo=0xFFD5CB9B, techo_junta=0xFF8E8760,
          niebla=0xFFC9B455, luz=0xFFFFF7D2, fondo=0xFF0D0B07,
          proporcion=0.62, semiancho=0.150, reflejo=0.16, humedad=1.00),

    # Nivel 1 - El deposito. Nave con pilares, cerchas y campanas.
    Nivel(clave="nivel1", planta="nave",
          pared_alta=0xFFB6BAAE, pared_baja=0xFF74786C, junta=0xFF4A4E43,
          suelo=0xFF80847A, suelo_lejos=0xFF5A5E54, suelo_junta=0xFF3C4036,
          techo=0xFF9EA298, techo_junta=0xFF5C6055,
          niebla=0xFF6E7268, luz=0xFFE8F0FF, fondo=0xFF171B1D,
          proporcion=0.50, semiancho=0.115, reflejo=0.30, humedad=0.35),

    # Nivel 2 - Servicio. El unico que sigue siendo un pasillo, y dobla.
    Nivel(clave="nivel2", planta="servicio",
          pared_alta=0xFF6E4A28, pared_baja=0xFF3E2A17, junta=0xFF241609,
          suelo=0xFF413025, suelo_lejos=0xFF2A1F16, suelo_junta=0xFF1B120C,
          techo=0xFF4A3520, techo_junta=0xFF2A1C0E,
          niebla=0xFF54371C, luz=0xFFFFB65E, fondo=0xFF0B0703,
          proporcion=1.35, semiancho=0.058, reflejo=0.22, humedad=0.75),

    # Nivel 3 - Las piscinas. Abajo no hay suelo: hay agua.
    Nivel(clave="nivel3", planta="natatorio",
          pared_alta=0xFFE4EFEC, pared_baja=0xFFA9C6C2, junta=0xFF7EA5A2,
          suelo=0xFF63B6B4, suelo_lejos=0xFF2F7E82, suelo_junta=0xFF3E9A9A,
          techo=0xFFE8F2F0, techo_junta=0xFFB2CCC9,
          niebla=0xFFBEDCD9, luz=0xFFF4FFFD, fondo=0xFF08171A,
          proporcion=0.62, semiancho=0.118, reflejo=0.62, humedad=0.15),
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
    """Encuadre: donde converge la perspectiva y cuanto mide el fondo."""

    def __init__(self, ancho, alto, fx, fy, w, h):
        self.ancho = ancho
        self.alto = alto
        self.fx = fx
        self.fy = fy
        self.w = w
        self.h = h

    def dx(self, x):
        return abs(x - self.fx) / self.w

    def techo_en(self, dx):
        return self.fy - self.h * dx

    def suelo_en(self, dx):
        return self.fy + self.h * dx


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
    lz.fill_gradient(round(m.fx - m.w), round(m.fy - m.h),
                     round(m.fx + m.w), round(m.fy + m.h),
                     iluminar(mezclar(color, nivel.niebla, 0.22),
                              limitar(luz * 0.52 * fuerza, 0.0, 1.0)),
                     iluminar(color, limitar(luz * 0.30 * fuerza, 0.0, 1.0)))


def t_plano(lz, m, arriba, cerca, lejos_c, niebla, luz, velo) -> None:
    """El suelo o el cielo, fila por fila."""
    desde = 0 if arriba else round(m.fy + m.h)
    hasta = round(m.fy - m.h) if arriba else m.alto
    for y in range(desde, hasta, PASO):
        dy = abs(y + PASO * 0.5 - m.fy) / m.h
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
        y = m.fy - m.h * dy if arriba else m.fy + m.h * dy
        if y < -4 or y > m.alto + 4:
            continue
        grosor = max(1, min(int(m.h * 0.09), int(m.h * dy * 0.010)))
        x0 = round(m.fx - m.w * dy)
        x1 = round(m.fx + m.w * dy)
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
            x = m.fx + signo * m.w * dx * lateral
            if -grosor <= x <= m.ancho + grosor:
                lz.fill(int(x), y0, int(x) + grosor, y1, color)


def t_manchas(lz, m, nivel, luz, tramos) -> None:
    """Filtraciones que cuelgan de lo alto y se abren hacia abajo."""
    total = int(16 * nivel.humedad)
    for i in range(total):
        dx = 1.15 + pseudo(i * 3) * (tramos * 0.42)
        signo = -1 if pseudo(i * 3 + 1) < 0.5 else 1
        x = m.fx + signo * m.w * dx
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
    lej = limitar(1.0 / dx, 0.0, 1.0)
    y = m.fy - m.h * dx * altura
    medio = max(1.0, m.w * dx * largo)
    grueso = max(1.0, m.h * dx * 0.026)
    fuerza = luz * (0.45 + 0.55 * lej)
    if derrame > 0.0:
        for k in (3, 2, 1):
            t = k / 3.0
            ex = medio * (1.0 + t * 1.5)
            ey = grueso * (1.0 + t * 5.0)
            lz.fill(int(m.fx - ex), int(y - ey), int(m.fx + ex), int(y + ey),
                    con_alfa(nivel.luz, 0.055 * derrame * fuerza * (1.0 - t * 0.5)))
    lz.fill(int(m.fx - medio), int(y - grueso), int(m.fx + medio), int(y + grueso),
            con_alfa(iluminar(nivel.luz, min(1.0, fuerza * 1.25)), 0.92))


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
            presencia_segunda: bool = False,
            polvo: bool = True, destellos: bool = True) -> None:
    fx = lz.ancho * FUGA_X
    fy = lz.alto * FUGA_Y
    w = lz.ancho * nivel.semiancho
    h = w * nivel.proporcion
    m = Marco(lz.ancho, lz.alto, fx, fy, w, h)

    luz = brillo_fluorescente(tiempo, destellos) * (1.0 - 0.55 * penumbra) * luz_global
    # La presencia le saca hasta un ocho por ciento a la escena mientras esta,
    # igual que Presencia.sombra(). Nadie lo puede senalar; todo el mundo lo nota.
    luz *= 1.0 - 0.08 * limitar(presencia_v, 0.0, 1.0)
    luz = limitar(luz, 0.0, 1.0)

    PLANTAS[nivel.planta](lz, m, nivel, luz, tiempo)

    if presencia_v > 0.0:
        presencia(lz, nivel, fx, fy, w, h, presencia_v, luz,
                  presencia_segunda, tiempo, PISO_PRESENCIA[nivel.planta])
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
    suelo = m.fy + m.h
    alto = m.h * 1.30
    for i in range(3):
        centro = m.fx + m.w * (i - 1) * 0.56
        medio = m.w * 0.13
        x0, x1 = round(centro - medio), round(centro + medio)
        y0, y1 = round(suelo - alto), round(suelo)
        if i != 1:
            lz.fill_gradient(x0, y0, x1, y1,
                             con_alfa(iluminar(nivel.fondo, luz * 0.30), 0.95),
                             con_alfa(VANO, 0.96))
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
        for y in range(0, max(0, int(m.fy - m.h)), PASO):
            dy = abs(y + PASO * 0.5 - m.fy) / m.h
            if dy <= 1.0:
                continue
            lej = limitar(1.0 / dy, 0.0, 1.0)
            x = m.fx + m.w * dy * frac
            grosor = max(1, int(m.w * dy * 0.006))
            lz.fill(int(x), y, int(x) + grosor, y + PASO,
                    con_alfa(iluminar(velar(nivel.techo_junta, nivel.niebla, lej, 0.5),
                                      atenuar(luz, lej)), 0.40 * lej + 0.10))


def sala_alfombra(lz, m, nivel, luz) -> None:
    """La franja gastada del centro. Sin bordes rectos."""
    for y in range(round(m.fy + m.h), m.alto, PASO):
        dy = abs(y + PASO * 0.5 - m.fy) / m.h
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
        x0 = int(min(m.fx + signo * m.w * dxa, m.fx + signo * m.w * dxb))
        x1 = int(max(m.fx + signo * m.w * dxa, m.fx + signo * m.w * dxb))
        if x1 <= 0 or x0 >= m.ancho or x1 - x0 < 3:
            continue
        for col in range(max(0, x0), min(m.ancho, x1)):
            dxc = m.dx(col + 0.5)
            centro = m.fy - m.h * dxc * 0.30
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
    t_fondo(lz, m, nivel, luz, mezclar(nivel.pared_baja, nivel.niebla, 0.30))
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
    suelo = m.fy + m.h
    alto = m.h * 1.15
    x0, x1 = round(m.fx - m.w * 0.55), round(m.fx + m.w * 0.55)
    y0, y1 = round(suelo - alto), round(suelo)
    lz.fill_gradient(x0, y0, x1, y1,
                     iluminar(mezclar(nivel.pared_baja, nivel.junta, 0.45), luz * 0.42),
                     iluminar(mezclar(nivel.junta, VANO, 0.30), luz * 0.24))
    paso = max(2, (x1 - x0) // 14)
    for x in range(x0 + paso, x1, paso):
        lz.fill(x, y0, x + 1, y1, con_alfa(VANO, 0.22))
    for k in range(1, 4):
        y = y0 + (y1 - y0) * k // 4
        lz.fill(x0, y, x1, y + 1, con_alfa(VANO, 0.26))
    lz.fill(x0 - 1, y0 - 1, x1 + 1, y0 + 1,
            iluminar(mezclar(nivel.junta, nivel.pared_alta, 0.25), luz * 0.60))
    lz.fill(x0, y1 - 2, x1, y1, con_alfa(nivel.luz, 0.12 * luz))


def nave_losas(lz, m, nivel, luz) -> None:
    """Dos corridas longitudinales de losa. El suelo deja de ser una mancha."""
    for frac in (-0.55, 0.55):
        for y in range(round(m.fy + m.h), m.alto, PASO):
            dy = abs(y + PASO * 0.5 - m.fy) / m.h
            if dy <= 1.0:
                continue
            lej = limitar(1.0 / dy, 0.0, 1.0)
            x = m.fx + m.w * dy * frac
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
        y_sup = m.fy - m.h * dx * 0.98
        y_inf = m.fy - m.h * dx * NAVE_CORDON
        if y_inf < -6:
            continue
        x0 = max(0, int(m.fx - m.w * dx))
        x1 = min(m.ancho, int(m.fx + m.w * dx))
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
        y_techo = m.fy - m.h * dx * NAVE_CORDON
        y_lampara = m.fy - m.h * dx * (NAVE_CORDON - 0.14)
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
        y_techo = m.fy - m.h * dx * NAVE_CORDON
        y_suelo = m.fy + m.h * dx
        for signo in (-1, 1):
            x = m.fx + signo * m.w * dx * NAVE_HILERA
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
    x0 = round(m.fx - m.w * 0.46)
    x1 = round(m.fx + m.w * 0.46)
    suelo = m.fy + m.h
    y0 = round(suelo - m.h * 1.22)
    y1 = round(suelo - m.h * 0.42)
    lz.fill(x0, y0, x1, y1,
            iluminar(mezclar(nivel.junta, nivel.pared_baja, 0.40), luz * 0.52))
    lz.fill(x0, y0, x1, y0 + 1, iluminar(nivel.pared_alta, luz * 0.40))
    lz.fill((x0 + x1) // 2, y0, (x0 + x1) // 2 + 1, y1, con_alfa(VANO, 0.45))
    px = x1 - max(3, (x1 - x0) // 8)
    py = y0 + max(3, (y1 - y0) // 6)
    lz.fill(px, py, px + 2, py + 2, con_alfa(ALERTA_BRILLO, 0.85 * luz + 0.15))


def serv_bifurcacion(lz, m, nivel, luz) -> None:
    """El tramo que se abre de costado. Esto no es un tubo: es una red."""
    dxa = profundidad(SERV_CODO, SERV_TRAMOS)
    dxb = profundidad(SERV_CODO + 2, SERV_TRAMOS)
    lej = limitar(1.0 / dxa, 0.0, 1.0)
    xa = m.fx - m.w * dxa
    xb = m.fx - m.w * dxb
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
            eje = m.fy - m.h * dx * alturas[c]
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
            x = m.fx + signo * m.w * dx * 0.80
            if x < 0 or x > m.ancho:
                continue
            y0 = m.fy - m.h * dx * 0.90
            y1 = m.fy - m.h * dx * 0.54
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
        x = m.fx + m.w * dx * 0.98
        if x < 0 or x > m.ancho:
            continue
        y = m.fy - m.h * dx * 0.48
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
        x = m.fx - m.w * dx
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
NAT_VASO = 0.74        # semiancho del vaso, en fraccion del recinto
NAT_CALLES = 4


def natatorio(lz, m, nivel, luz, tiempo) -> None:
    t_fondo(lz, m, nivel, luz, mezclar(nivel.pared_baja, nivel.techo, 0.55), 2.6)
    nat_testero(lz, m, nivel, luz)
    t_plano(lz, m, True, nivel.techo, mezclar(nivel.techo, nivel.niebla, 0.30),
            nivel.niebla, luz, 0.44)
    t_transversales(lz, m, True, nivel.techo_junta, nivel.niebla, luz, NAT_TRAMOS, 0.26)
    nat_claraboyas(lz, m, nivel, luz)
    nat_borde(lz, m, nivel, luz)
    nat_agua(lz, m, nivel, luz, tiempo)
    nat_calles(lz, m, nivel, luz, tiempo)
    t_paredes(lz, m, nivel, luz)
    nat_escalerilla(lz, m, nivel, luz)
    nat_azulejo(lz, m, nivel, luz)
    t_juntas(lz, m, nivel, luz, NAT_TRAMOS, 1.0, 0.24)
    t_manchas(lz, m, nivel, luz, NAT_TRAMOS)
    nat_caustica(lz, m, nivel, luz, tiempo)


def nat_testero(lz, m, nivel, luz) -> None:
    """Doble puerta de vaiven al fondo, con su franja de vidrio armado."""
    suelo = m.fy + m.h * NAT_CABECERA
    alto = m.h * 1.05
    x0, x1 = round(m.fx - m.w * 0.30), round(m.fx + m.w * 0.30)
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
    lz.fill(round(m.fx - m.w * NAT_CABECERA), y1 - 2,
            round(m.fx + m.w * NAT_CABECERA), y1,
            con_alfa(iluminar(nivel.junta, luz), 0.40))


def nat_claraboyas(lz, m, nivel, luz) -> None:
    """Lo unico que ilumina esto entra por arriba, y no alcanza."""
    for j in range(2, NAT_TRAMOS + 1):
        dx = profundidad(j, NAT_TRAMOS)
        if dx > 6.0 or pseudo(1200 + j) <= 0.45:
            continue
        t_luminaria(lz, m, nivel, dx, 0.97, 0.34, 1.30, luz)


def nat_borde(lz, m, nivel, luz) -> None:
    """La baldosa de la orilla. Ocupa todo el suelo; el agua se le apoya."""
    for y in range(round(m.fy + m.h), m.alto, PASO):
        dy = abs(y + PASO * 0.5 - m.fy) / m.h
        if dy <= 1.0:
            continue
        lej = limitar(1.0 / dy, 0.0, 1.0)
        color = velar(mezclar(nivel.techo, nivel.pared_baja, 0.30),
                      nivel.niebla, lej, 0.40)
        lz.fill(0, y, m.ancho, y + PASO, iluminar(color, atenuar(luz, lej)))
    t_transversales(lz, m, False, nivel.techo_junta, nivel.niebla, luz, NAT_TRAMOS, 0.20)


def nat_agua(lz, m, nivel, luz, tiempo) -> None:
    """El vaso: un trapecio que arranca lejos y se abre hacia la camara."""
    desde = round(m.fy + m.h * NAT_CABECERA)
    for y in range(desde, m.alto, PASO):
        dy = abs(y + PASO * 0.5 - m.fy) / m.h
        lej = limitar(1.0 / dy, 0.0, 1.0)
        medio = m.w * dy * NAT_VASO
        x0, x1 = int(m.fx - medio), int(m.fx + medio)
        if x1 <= 0 or x0 >= m.ancho:
            continue
        # Cuanto mas cerca, mas hondo se ve el vaso y mas oscuro el agua.
        hondo = limitar((dy - NAT_CABECERA) / (NAT_TRAMOS * 0.35), 0.0, 1.0)
        color = velar(mezclar(nivel.suelo, nivel.suelo_lejos, 0.25 + hondo * 0.70),
                      nivel.niebla, lej, 0.25)
        lz.fill(max(0, x0), y, min(m.ancho, x1), y + PASO,
                iluminar(color, atenuar(luz, lej) * 0.90))
        canto = con_alfa(iluminar(nivel.techo, luz), 0.60)
        lz.fill(max(0, x0 - 2), y, max(0, x0), y + PASO, canto)
        lz.fill(min(m.ancho, x1), y, min(m.ancho, x1 + 2), y + PASO, canto)

    # La cabecera del vaso, al fondo, y el reflejo del techo sobre ella.
    medio = m.w * NAT_CABECERA * NAT_VASO
    lz.fill(int(m.fx - medio), desde - 2, int(m.fx + medio), desde + 1,
            con_alfa(iluminar(nivel.techo, luz), 0.70))
    largo = m.h * 1.4
    for y in range(desde, min(m.alto, desde + int(largo)), PASO):
        tt = (y - desde) / largo
        dy = abs(y - m.fy) / m.h
        ancho = m.w * dy * NAT_VASO * 0.70
        onda = math.sin(tiempo * 0.45 + tt * 7.0) * m.w * 0.012
        lz.fill(int(m.fx - ancho + onda), y, int(m.fx + ancho + onda), y + PASO,
                con_alfa(nivel.techo, 0.22 * (1.0 - tt) * (1.0 - tt) * luz))


def nat_calles(lz, m, nivel, luz, tiempo) -> None:
    """Las lineas del fondo del vaso, quebradas por el agua que las tapa."""
    desde = round(m.fy + m.h * NAT_CABECERA)
    for i in range(1, NAT_CALLES):
        frac = (i / NAT_CALLES) * 2.0 - 1.0
        for y in range(desde, m.alto, PASO):
            dy = abs(y + PASO * 0.5 - m.fy) / m.h
            lej = limitar(1.0 / dy, 0.0, 1.0)
            onda = math.sin(tiempo * 0.5 + dy * 2.2 + i * 1.7) * m.w * 0.010
            x = m.fx + m.w * dy * NAT_VASO * frac + onda
            grosor = max(1, int(m.w * dy * 0.012))
            lz.fill(int(x), y, int(x) + grosor, y + PASO,
                    con_alfa(iluminar(nivel.techo, luz), 0.26 + 0.16 * lej))


def nat_escalerilla(lz, m, nivel, luz) -> None:
    """Dos barandas curvas asomando del agua. La escala se lee con esto."""
    dy = 1.95
    y = m.fy + m.h * dy
    x = m.fx + m.w * dy * NAT_VASO
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
        y = m.fy - m.h * dx * 0.16
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


PLANTAS = {"sala": sala, "nave": nave, "servicio": servicio, "natatorio": natatorio}
PISO_PRESENCIA = {"sala": 0.94, "nave": 1.30, "servicio": 0.98, "natatorio": 1.18}


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


def presencia(lz, nivel, fx, fy, w, h, visible, luz, segunda=False, tiempo=0.0,
              piso=0.94) -> None:
    """Lo que a veces esta al fondo del recinto.

    `visible` va de 0 a 1 y es lo que Presencia.visibilidad() devuelve en el
    juego. `segunda` elige el costado: la reaparicion sale corrida hacia el
    otro lado, que es el recurso central de todo el efecto. `piso` lo dicta la
    planta: en el natatorio los pies quedan en la orilla, no abajo de todo.
    """
    visible = limitar(visible * luz, 0.0, 1.0)
    if visible <= 0.01:
        return

    lado = -0.34 if segunda else 0.41
    x = fx + w * lado
    base = fy + h * piso
    altura = h * ALTURA_PRESENCIA
    vaiven = math.sin(tiempo * 0.55) * (w * 0.012)

    alfa = ALFA_MAXIMO_PRESENCIA * visible
    tinte = color_presencia(nivel)
    cuerpo_presencia(lz, x + vaiven, base, altura, w, alfa, tinte)

    if nivel.reflejo > 0.20:
        reflejo_presencia(lz, x + vaiven, base, altura, w,
                          alfa * nivel.reflejo * 0.85, tiempo, tinte)


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
    ancho_hoja = 214
    x0 = max(14, int(lz.ancho * 0.07))
    y0 = max(16, int(lz.alto * 0.13))
    alto_hoja = min(lz.alto - y0 - 16, 208)
    x1 = x0 + ancho_hoja
    y1 = y0 + alto_hoja

    lz.fill(x0 + 3, y0 + 4, x1 + 3, y1 + 4, con_alfa(VANO, 0.30))
    lz.fill(x0, y0, x1, y1, con_alfa(PAPEL, 0.94))
    for bx, by, bx2, by2 in ((x0, y0, x1, y0 + 1), (x0, y1 - 1, x1, y1),
                             (x0, y0, x0 + 1, y1), (x1 - 1, y0, x1, y1)):
        lz.fill(bx, by, bx2, by2, con_alfa(0xFF5E5222, 0.40))
    centro = (x0 + x1) // 2
    lz.fill(centro - 22, y0 - 4, centro + 22, y0 + 4, con_alfa(PAPEL, 0.45))

    lz.fill(x0 + 12, y0 + 12, x0 + 12 + 58, y0 + 12 + 16, con_alfa(TINTA, 0.92))
    lz.fill(x0 + 12, y0 + 32, x1 - 12, y0 + 33, con_alfa(TINTA_TENUE, 0.45))
    lz.fill(x0 + 12, y0 + 42, x0 + 150, y0 + 50, con_alfa(TINTA_TENUE, 0.55))
    lz.fill(x0 + 12, y0 + 54, x0 + 170, y0 + 62, con_alfa(TINTA, 0.70))
    for i in range(4):
        y = y0 + 88 + i * 23
        lz.fill(x0 + 12, y + 6, x0 + 19, y + 13, con_alfa(TINTA_TENUE, 0.70))
        lz.fill(x0 + 44, y + 6, x0 + 150, y + 14, con_alfa(TINTA_TENUE, 0.75))
    lz.fill(x0 + 12, y1 - 26, x1 - 20, y1 - 18, con_alfa(TINTA_TENUE, 0.55))


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
        tira = Lienzo(ancho * 2, alto * 2)
        for i, nv in enumerate(NIVELES):
            # La presencia se muestra en el nivel 3, que es donde mas se
            # nota por el reflejo, y no en el 0, que es la postal del servidor.
            sub = render(ancho, alto, nv, con_hoja=not desnudo, tiempo=3.0 + i,
                         presencia_v=1.0 if i == 3 else 0.0)
            ox = (i % 2) * ancho
            oy = (i // 2) * alto
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

        pasos = [(0.15, False), (0.55, False), (1.00, False),
                 (0.40, False), (0.00, False), (0.85, True)]
        ancho, alto = 420, 236
        tira = Lienzo(ancho * 3, alto * 2)
        for i, (visible, segunda) in enumerate(pasos):
            sub = render(ancho, alto, nv, con_hoja=not desnudo,
                         tiempo=3.0 + i * 0.7,
                         presencia_v=visible, presencia_segunda=segunda)
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
