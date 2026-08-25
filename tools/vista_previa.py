#!/usr/bin/env python3
"""Vista previa de la escena del menu, sin Minecraft.

Reimplementa en Python las mismas operaciones de dibujo que usa
`EscenaNivel.java` (fill y fillGradient sobre un lienzo ARGB) para poder mirar
la composicion y la perspectiva antes de compilar. No es un emulador: es un
espejo. Si se cambia la escena en Java, se cambia aqui tambien.

Geometria del corredor (identica en ambos lados):

    La abertura del fondo es un rectangulo centrado en el punto de fuga, de
    semiancho `w` y semialto `h`. Las cuatro aristas del corredor son rectas
    que pasan por la fuga, asi que en pantalla todo se reduce a una variable:

        dx = |x - fugaX| / w        (columna)
        dy = |y - fugaY| / h        (fila)

    En la abertura del fondo dx = dy = 1. La profundidad de lo que se ve en
    esa columna o fila es z = Z_FONDO / dx, es decir, `lejos = 1/dx` va de 0
    (pegado a la camara) a 1 (la abertura del fondo). Las juntas de pared, las
    del suelo y las del techo se calculan con la MISMA serie de profundidades,
    por eso las tres coinciden y el pasillo se lee como un pasillo.

Uso:  python3 tools/vista_previa.py [ancho] [alto] [salida.png] [--nivel N]
                                    [--figura=0.0..1.0]
      python3 tools/vista_previa.py --contacto docs/contacto.png
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
SEMIANCHO = 0.082   # valor de referencia; cada nivel define el suyo
PANELES = 26
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
    """Un nivel del servidor. Define la piel completa del corredor."""

    def __init__(self, clave, pared_alta, pared_baja, junta, suelo, suelo_lejos,
                 suelo_junta, techo, techo_junta, niebla, luz, fondo,
                 proporcion, semiancho, reflejo, zocalo, humedad, tuberias, marcos):
        self.clave = clave
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
        self.proporcion = proporcion      # alto del corredor / ancho
        self.semiancho = semiancho        # semiancho de la abertura del fondo
        self.reflejo = reflejo            # cuanto devuelve el suelo (0..1)
        self.zocalo = zocalo              # dibujar zocalo corrido
        self.humedad = humedad            # manchas en la pared (0..1)
        self.tuberias = tuberias          # canos corridos bajo el cielorraso
        self.marcos = marcos              # vanos abiertos en las paredes


NIVELES = [
    # Nivel 0 - El papel mural. La postal del servidor.
    Nivel(clave="nivel0",
          pared_alta=0xFFE6D264, pared_baja=0xFF9A8630, junta=0xFF5E5222,
          suelo=0xFF8A7638, suelo_lejos=0xFF6E5C2A, suelo_junta=0xFF4C401E,
          techo=0xFFD5CB9B, techo_junta=0xFF8E8760,
          niebla=0xFFC9B455, luz=0xFFFFF7D2, fondo=0xFF0D0B07,
          proporcion=0.92, semiancho=0.082, reflejo=0.16, zocalo=True, humedad=1.0,
          tuberias=False, marcos=True),

    # Nivel 1 - El deposito. Hormigon, altura, eco.
    Nivel(clave="nivel1",
          pared_alta=0xFFB6BAAE, pared_baja=0xFF74786C, junta=0xFF4A4E43,
          suelo=0xFF80847A, suelo_lejos=0xFF5A5E54, suelo_junta=0xFF3C4036,
          techo=0xFF9EA298, techo_junta=0xFF5C6055,
          niebla=0xFF6E7268, luz=0xFFE8F0FF, fondo=0xFF171B1D,
          proporcion=0.98, semiancho=0.132, reflejo=0.30, zocalo=False, humedad=0.35,
          tuberias=False, marcos=True),

    # Nivel 2 - Servicio. Estrecho, caliente, oxidado.
    Nivel(clave="nivel2",
          pared_alta=0xFF6E4A28, pared_baja=0xFF3E2A17, junta=0xFF241609,
          suelo=0xFF413025, suelo_lejos=0xFF2A1F16, suelo_junta=0xFF1B120C,
          techo=0xFF4A3520, techo_junta=0xFF2A1C0E,
          niebla=0xFF54371C, luz=0xFFFFB65E, fondo=0xFF0B0703,
          proporcion=0.78, semiancho=0.070, reflejo=0.22, zocalo=False, humedad=0.75,
          tuberias=True, marcos=False),

    # Nivel 3 - Las piscinas. Azulejo, agua, silencio con eco.
    Nivel(clave="nivel3",
          pared_alta=0xFFE4EFEC, pared_baja=0xFFA9C6C2, junta=0xFF7EA5A2,
          suelo=0xFF63B6B4, suelo_lejos=0xFF2F7E82, suelo_junta=0xFF3E9A9A,
          techo=0xFFE8F2F0, techo_junta=0xFFB2CCC9,
          niebla=0xFFBEDCD9, luz=0xFFF4FFFD, fondo=0xFF08171A,
          proporcion=1.02, semiancho=0.098, reflejo=0.62, zocalo=False, humedad=0.15,
          tuberias=False, marcos=True),
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
# Escena: espejo de EscenaNivel.java
# --------------------------------------------------------------------------
def profundidad_panel(j: int) -> float:
    """dx de la junta numero j. j=PANELES cae justo en la abertura del fondo."""
    return PANELES / float(max(1, j))


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


def dibujar(lz: Lienzo, nivel: Nivel, tiempo: float = 3.0, penumbra: float = 0.0,
            luz_global: float = 1.0, presencia_v: float = 0.0,
            presencia_segunda: bool = False,
            polvo: bool = True, destellos: bool = True) -> None:
    ancho = lz.ancho
    alto = lz.alto

    fx = ancho * FUGA_X
    fy = alto * FUGA_Y
    w = ancho * nivel.semiancho
    h = w * nivel.proporcion

    luz = brillo_fluorescente(tiempo, destellos) * (1.0 - 0.55 * penumbra) * luz_global
    # La presencia le saca hasta un ocho por ciento a la escena mientras esta,
    # igual que Presencia.sombra(). Nadie lo puede senalar; todo el mundo lo nota.
    luz *= 1.0 - 0.08 * limitar(presencia_v, 0.0, 1.0)
    luz = limitar(luz, 0.0, 1.0)

    fondo(lz, nivel, fx, fy, w, h, luz)
    cielorraso(lz, nivel, fx, fy, w, h, luz)
    piso(lz, nivel, fx, fy, w, h, luz)
    transversales(lz, nivel, fx, fy, w, h, luz)
    luminarias(lz, nivel, fx, fy, w, h, luz)
    paredes(lz, nivel, fx, fy, w, h, luz, tiempo)
    if nivel.tuberias:
        canos(lz, nivel, fx, fy, w, h, luz)
    if presencia_v > 0.0:
        presencia(lz, nivel, fx, fy, w, h, presencia_v, luz,
                  presencia_segunda, tiempo)
    if polvo:
        motas(lz, fx, fy, tiempo, luz)
    vineta(lz, nivel, penumbra, luz)


def fondo(lz, nivel, fx, fy, w, h, luz) -> None:
    """La abertura del fondo. Lo que hay del otro lado no se ilumina."""
    lz.fill(0, 0, lz.ancho, lz.alto, iluminar(nivel.niebla, luz * 0.45))
    x0 = int(fx - w)
    x1 = int(fx + w)
    y0 = int(fy - h)
    y1 = int(fy + h)
    lz.fill_gradient(x0, y0, x1, y1,
                     mezclar(nivel.fondo, nivel.niebla, 0.22 * luz),
                     nivel.fondo)


def cielorraso(lz, nivel, fx, fy, w, h, luz) -> None:
    """Placas del cielorraso, fila por fila. Arriba = cerca."""
    tope = int(fy - h)
    y = 0
    while y < tope:
        dy = (fy - y) / h
        lejos = limitar(1.0 / dy, 0.0, 1.0)
        color = mezclar(nivel.techo, nivel.niebla, lejos * lejos * 0.55)
        color = iluminar(color, luz * (0.60 + 0.40 * lejos))
        lz.fill(0, y, lz.ancho, y + 2, color)
        y += 2


def piso(lz, nivel, fx, fy, w, h, luz) -> None:
    """Suelo, fila por fila. Abajo = cerca."""
    base = int(fy + h)
    y = lz.alto
    while y > base:
        dy = (y - fy) / h
        lejos = limitar(1.0 / dy, 0.0, 1.0)
        color = mezclar(nivel.suelo, nivel.suelo_lejos, lejos)
        color = mezclar(color, nivel.niebla, lejos * lejos * 0.45)
        color = iluminar(color, luz * (0.55 + 0.45 * lejos))
        lz.fill(0, y - 2, lz.ancho, y, color)
        y -= 2


def transversales(lz, nivel, fx, fy, w, h, luz) -> None:
    """Juntas del suelo y perfileria del cielorraso, a las mismas profundidades."""
    for j in range(1, PANELES + 1):
        dx = profundidad_panel(j)
        lejos = limitar(1.0 / dx, 0.0, 1.0)
        grosor = max(1, min(int(h * 0.075), int(h * dx * 0.010)))

        ys = fy + h * dx
        if ys < lz.alto:
            color = iluminar(mezclar(nivel.suelo_junta, nivel.niebla, lejos * 0.5),
                             luz * (0.55 + 0.45 * lejos))
            lz.fill(0, int(ys), lz.ancho, int(ys) + grosor, con_alfa(color, 0.60 * lejos + 0.10))

        yt = fy - h * dx
        if yt > 0:
            color = iluminar(mezclar(nivel.techo_junta, nivel.niebla, lejos * 0.5),
                             luz * (0.52 + 0.48 * lejos))
            lz.fill(0, int(yt) - grosor, lz.ancho, int(yt), con_alfa(color, 0.55 * lejos + 0.10))


def luminarias(lz, nivel, fx, fy, w, h, luz) -> None:
    """Los tubos del cielorraso y el charco que devuelven en el suelo."""
    for j in range(1, PANELES + 1):
        if j % 2 != 0:
            continue
        dx = profundidad_panel(j)
        dx_sig = profundidad_panel(j + 1) if j < PANELES else dx * 0.86
        lejos = limitar(1.0 / dx, 0.0, 1.0)

        y0 = fy - h * dx
        y1 = fy - h * dx_sig
        if y1 < 0 or y0 > lz.alto:
            continue
        semi = w * dx * 0.34
        intensidad = luz * (0.60 + 0.40 * lejos)

        # Halo sobre la placa
        lz.fill(int(fx - semi * 1.9), int(y0) - 1, int(fx + semi * 1.9), int(y1) + 1,
                con_alfa(iluminar(nivel.luz, intensidad), 0.18))
        # El tubo
        lz.fill(int(fx - semi), int(y0), int(fx + semi), int(y1),
                con_alfa(iluminar(nivel.luz, intensidad), 0.92))

        if nivel.reflejo <= 0.0:
            continue
        # Lo que devuelve el suelo, a la misma profundidad
        ry0 = fy + h * dx
        ry1 = fy + h * dx_sig
        if ry0 > lz.alto:
            continue
        semi_r = semi * 1.25
        lz.fill(int(fx - semi_r), int(ry1), int(fx + semi_r), int(ry0),
                con_alfa(iluminar(nivel.luz, intensidad), nivel.reflejo * 0.55))


def paredes(lz, nivel, fx, fy, w, h, luz, tiempo) -> None:
    """Las dos paredes laterales, columna por columna.

    Cada columna es un unico degradado vertical: claro contra el cielorraso,
    apagado contra el zocalo. La distancia decide cuanto se come la niebla.
    """
    paso = 2
    x = 0
    while x < lz.ancho:
        centro = x + paso * 0.5
        dx = abs(centro - fx) / w
        if dx <= 1.0:
            x += paso
            continue

        lejos = limitar(1.0 / dx, 0.0, 1.0)
        y0 = fy - h * dx
        y1 = fy + h * dx
        if y1 < 0 or y0 > lz.alto:
            x += paso
            continue

        atenuacion = luz * (0.52 + 0.48 * lejos)
        alta = mezclar(nivel.pared_alta, nivel.niebla, lejos * lejos * 0.62)
        baja = mezclar(nivel.pared_baja, nivel.niebla, lejos * lejos * 0.52)
        lz.fill_gradient(x, int(y0), x + paso, int(y1),
                         iluminar(alta, atenuacion), iluminar(baja, atenuacion))

        if nivel.zocalo:
            alto_zocalo = max(1, int(h * dx * 0.055))
            lz.fill(x, int(y1) - alto_zocalo, x + paso, int(y1),
                    iluminar(nivel.junta, atenuacion * 0.85))

        x += paso

    juntas_pared(lz, nivel, fx, fy, w, h, luz)
    if nivel.humedad > 0.0:
        manchas(lz, nivel, fx, fy, w, h, luz)
    if nivel.marcos:
        vanos(lz, nivel, fx, fy, w, h, luz)


def juntas_pared(lz, nivel, fx, fy, w, h, luz) -> None:
    """Las verticales que separan panel de panel. Se aprietan hacia la fuga."""
    for j in range(1, PANELES + 1):
        dx = profundidad_panel(j)
        lejos = limitar(1.0 / dx, 0.0, 1.0)
        atenuacion = luz * (0.52 + 0.48 * lejos)
        grosor = max(1, min(int(w * 0.10), int(w * dx * 0.009)))
        color = con_alfa(iluminar(mezclar(nivel.junta, nivel.niebla, lejos * 0.55), atenuacion), 0.45 * lejos + 0.12)
        y0 = int(fy - h * dx)
        y1 = int(fy + h * dx)
        for signo in (-1, 1):
            x = fx + signo * w * dx
            if -grosor <= x <= lz.ancho + grosor:
                lz.fill(int(x), y0, int(x) + grosor, y1, color)


def vanos(lz, nivel, fx, fy, w, h, luz) -> None:
    """Puertas abiertas en las paredes. Se abren entre dos juntas consecutivas."""
    for j in range(4, PANELES - 1):
        if int(pseudo(600 + j) * 5) != 0:
            continue
        dx_a = profundidad_panel(j)
        dx_b = profundidad_panel(j + 1)
        lejos = limitar(1.0 / dx_a, 0.0, 1.0)
        signo = -1 if pseudo(700 + j) < 0.5 else 1
        xa = fx + signo * w * dx_a
        xb = fx + signo * w * dx_b
        x0, x1 = (int(min(xa, xb)), int(max(xa, xb)))
        if x1 <= 0 or x0 >= lz.ancho:
            continue
        y_suelo_a = fy + h * dx_a
        y_suelo_b = fy + h * dx_b
        # El vano llega al 78% de la altura del corredor
        col = x0
        while col < x1:
            t = (col - x0) / max(1, x1 - x0)
            ys = y_suelo_a + (y_suelo_b - y_suelo_a) * t
            dxc = abs(col + 0.5 - fx) / w
            altura = 2.0 * h * dxc * 0.78
            lz.fill(col, int(ys - altura), col + 1, int(ys),
                    con_alfa(iluminar(nivel.fondo, luz * 0.20 * lejos), 0.94))
            col += 1
        # Marco
        lz.fill(x0, int(min(y_suelo_a, y_suelo_b) - 2.0 * h * dx_a * 0.78), x0 + 1, int(y_suelo_a),
                con_alfa(iluminar(nivel.junta, luz * (0.52 + 0.48 * lejos)), 0.8))


def canos(lz, nivel, fx, fy, w, h, luz) -> None:
    """Tuberias corridas bajo el cielorraso, una a cada lado del corredor."""
    paso = 2
    for altura_rel, radio_rel, tono in ((0.74, 0.045, 0.0), (0.62, 0.032, 0.25)):
        x = 0
        while x < lz.ancho:
            centro = x + paso * 0.5
            dx = abs(centro - fx) / w
            if dx <= 1.0:
                x += paso
                continue
            lejos = limitar(1.0 / dx, 0.0, 1.0)
            atenuacion = luz * (0.52 + 0.48 * lejos)
            eje = fy - h * dx * altura_rel
            radio = max(1.0, h * dx * radio_rel)
            color = mezclar(nivel.junta, nivel.pared_alta, 0.30 + tono)
            lz.fill_gradient(x, int(eje - radio), x + paso, int(eje + radio),
                             iluminar(mezclar(color, nivel.luz, 0.22), atenuacion),
                             iluminar(mezclar(color, VANO, 0.35), atenuacion))
            x += paso


def manchas(lz, nivel, fx, fy, w, h, luz) -> None:
    """Filtraciones. Cuelgan del cielorraso y se abren hacia abajo."""
    total = int(16 * nivel.humedad)
    for i in range(total):
        dx = 1.15 + pseudo(i * 3) * (PANELES * 0.42)
        signo = -1 if pseudo(i * 3 + 1) < 0.5 else 1
        x = fx + signo * w * dx
        if x < -40 or x > lz.ancho + 40:
            continue
        lejos = limitar(1.0 / dx, 0.0, 1.0)
        y0 = fy - h * dx
        altura = h * dx * (0.25 + pseudo(i * 3 + 2) * 0.55)
        ancho_mancha = max(2.0, w * dx * (0.05 + pseudo(i * 5) * 0.10))
        pasos = 5
        for k in range(pasos):
            t = k / pasos
            a = 0.30 * (1.0 - t) * (0.35 + 0.65 * lejos) * nivel.humedad
            am = ancho_mancha * (0.6 + 0.9 * t)
            lz.fill(int(x - am), int(y0 + altura * t), int(x + am), int(y0 + altura * (t + 1.0 / pasos)),
                    con_alfa(iluminar(nivel.junta, luz), a))


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


def presencia(lz, nivel, fx, fy, w, h, visible, luz, segunda=False, tiempo=0.0) -> None:
    """Lo que a veces esta al fondo del corredor.

    `visible` va de 0 a 1 y es lo que Presencia.visibilidad() devuelve en el
    juego. `segunda` elige el costado: la reaparicion sale corrida hacia el
    otro lado, que es el recurso central de todo el efecto.
    """
    visible = limitar(visible * luz, 0.0, 1.0)
    if visible <= 0.01:
        return

    lado = -0.34 if segunda else 0.41
    x = fx + w * lado
    base = fy + h * 0.94
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
def render(ancho: int, alto: int, nivel: Nivel, **kw) -> Lienzo:
    lz = Lienzo(ancho, alto)
    dibujar(lz, nivel, **kw)
    hoja(lz)
    return lz


def main() -> int:
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    banderas = [a for a in sys.argv[1:] if a.startswith("--")]

    if "--contacto" in banderas:
        salida = Path(args[0]) if args else Path("docs/vista_previa.png")
        ancho, alto = 480, 270
        tira = Lienzo(ancho * 2, alto * 2)
        for i, nv in enumerate(NIVELES):
            # La presencia se muestra en el nivel 3, que es donde mas se
            # nota por el reflejo, y no en el 0, que es la postal del servidor.
            sub = render(ancho, alto, nv, tiempo=3.0 + i,
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
            sub = render(ancho, alto, nv, tiempo=3.0 + i * 0.7,
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

    lz = render(ancho, alto, NIVELES[indice % len(NIVELES)], presencia_v=visible)
    salida.parent.mkdir(parents=True, exist_ok=True)
    lz.png(salida)
    print(f"{salida}  {ancho}x{alto}  nivel={NIVELES[indice % len(NIVELES)].clave}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
