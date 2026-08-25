#!/usr/bin/env python3
"""Vista previa de la escena del menu, sin Minecraft.

Reimplementa en Python las mismas operaciones de dibujo que usa
`EscenaNivel.java` (fill y fillGradient sobre un lienzo ARGB) para poder mirar
la composicion y la perspectiva antes de compilar. No es un emulador: es un
espejo. Si se cambia la escena en Java, se cambia aqui tambien.

Uso:  python3 tools/vista_previa.py [ancho] [alto] [salida.png]
"""

from __future__ import annotations

import struct
import sys
import zlib
from pathlib import Path

# --------------------------------------------------------------------------
# Paleta: espejo de Paleta.java
# --------------------------------------------------------------------------
PARED = 0xFFD8C24F
PARED_ALTA = 0xFFE6D264
PARED_BAJA = 0xFF9A8630
MOHO = 0xFF5E5222
ALFOMBRA = 0xFF8A7638
ALFOMBRA_OSCURA = 0xFF4C401E
TECHO = 0xFFD5CB9B
FLUOR = 0xFFFFF7D2
PAPEL = 0xFFF0E9CE
TINTA = 0xFF14120C
TINTA_TENUE = 0xFF4A422A
VANO = 0xFF0D0B07
ALERTA = 0xFF8E1B12
ALERTA_BRILLO = 0xFFC42B18

SEMILLA = 0x4A4F4253
TRAMOS = 11
MOTAS = 70
MANCHAS = 14

MASCARA = 0xFFFFFFFFFFFFFFFF


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
        destino = self.pix[indice]
        r = (color >> 16) & 0xFF
        g = (color >> 8) & 0xFF
        b = color & 0xFF
        destino[0] = int(destino[0] * (1.0 - a) + r * a)
        destino[1] = int(destino[1] * (1.0 - a) + g * a)
        destino[2] = int(destino[2] * (1.0 - a) + b * a)

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
        if x1 < x0:
            x0, x1 = x1, x0
        if y1 < y0:
            y0, y1 = y1, y0
        alto = max(1, y1 - y0)
        vx0 = max(0, x0)
        vx1 = min(self.ancho, x1)
        for y in range(max(0, y0), min(self.alto, y1)):
            t = (y - y0) / alto
            color = mezclar(arriba, abajo, t)
            base = y * self.ancho
            for x in range(vx0, vx1):
                self._componer(base + x, color)

    def png(self, ruta: Path) -> None:
        filas = bytearray()
        for y in range(self.alto):
            filas.append(0)
            base = y * self.ancho
            for x in range(self.ancho):
                p = self.pix[base + x]
                filas.extend((p[0], p[1], p[2]))

        def trozo(tipo: bytes, datos: bytes) -> bytes:
            c = struct.pack(">I", len(datos)) + tipo + datos
            return c + struct.pack(">I", zlib.crc32(tipo + datos) & 0xFFFFFFFF)

        cabecera = struct.pack(">IIBBBBB", self.ancho, self.alto, 8, 2, 0, 0, 0)
        ruta.write_bytes(
            b"\x89PNG\r\n\x1a\n"
            + trozo(b"IHDR", cabecera)
            + trozo(b"IDAT", zlib.compress(bytes(filas), 6))
            + trozo(b"IEND", b"")
        )


# --------------------------------------------------------------------------
# Escena: espejo de EscenaNivel.java
# --------------------------------------------------------------------------
def escala(t: float) -> float:
    return t * t


def interpolar(desde: int, hasta: int, t: float) -> int:
    return int(desde + (hasta - desde) * t)


def tropiezo(tiempo: float, indice: int) -> float:
    import math

    desfase = pseudo(indice * 31 + 3) * 6.28
    base = (
        0.88
        + 0.07 * math.sin(tiempo * 2.1 + desfase)
        + 0.05 * math.sin(tiempo * 9.3 + desfase * 2.0)
    )
    if (int(tiempo * 4.0) + indice * 17) % 71 == 0:
        base *= 0.35
    return max(0.30, min(1.0, base))


def dibujar(lz: Lienzo, tiempo: float = 3.0, penumbra: float = 0.0, silueta_t: float | None = 0.5) -> None:
    import math

    ancho, alto = lz.ancho, lz.alto
    luz = 0.90 * (1.0 - 0.55 * penumbra)

    fuga_x = int(ancho * 0.63)
    fuga_y = int(alto * 0.54)

    # --- superficies ---
    lz.fill(0, 0, ancho, alto, iluminar(PARED_BAJA, luz * 0.45))

    for i in range(TRAMOS, 0, -1):
        lejos = i / TRAMOS
        cerca = (i - 1) / TRAMOS
        ap_l = escala(lejos)
        ap_c = escala(cerca)
        luz_t = luz * (0.42 + 0.58 * lejos)

        izq_l = interpolar(fuga_x, 0, ap_l)
        der_l = interpolar(fuga_x, ancho, ap_l)
        sup_l = interpolar(fuga_y, 0, ap_l)
        inf_l = interpolar(fuga_y, alto, ap_l)

        izq_c = interpolar(fuga_x, 0, ap_c)
        der_c = interpolar(fuga_x, ancho, ap_c)
        sup_c = interpolar(fuga_y, 0, ap_c)
        inf_c = interpolar(fuga_y, alto, ap_c)

        lz.fill_gradient(izq_l, sup_l, der_l, sup_c, iluminar(TECHO, luz_t * 0.85), iluminar(TECHO, luz_t))
        lz.fill_gradient(izq_l, inf_c, der_l, inf_l, iluminar(ALFOMBRA_OSCURA, luz_t * 0.75), iluminar(ALFOMBRA, luz_t * 0.95))
        lz.fill_gradient(izq_c, sup_c, izq_l, inf_c, iluminar(PARED_ALTA, luz_t), iluminar(PARED_BAJA, luz_t * 0.85))
        lz.fill_gradient(der_l, sup_c, der_c, inf_c, iluminar(PARED_ALTA, luz_t), iluminar(PARED_BAJA, luz_t * 0.85))

        junta = iluminar(MOHO, luz_t)
        lz.fill(izq_l, sup_l, der_l, sup_l + 1, con_alfa(junta, 0.45))
        lz.fill(izq_l, inf_l - 1, der_l, inf_l, con_alfa(junta, 0.55))
        lz.fill(izq_l, sup_l, izq_l + 1, inf_l, con_alfa(junta, 0.35))
        lz.fill(der_l - 1, sup_l, der_l, inf_l, con_alfa(junta, 0.35))

    # --- humedad ---
    for i in range(MANCHAS):
        a = pseudo(i * 7 + 1)
        b = pseudo(i * 13 + 5)
        c = pseudo(i * 29 + 9)
        izquierda = (i % 2) == 0
        prof = 0.25 + a * 0.70
        ap = escala(prof)

        borde = interpolar(fuga_x, 0, ap) if izquierda else interpolar(fuga_x, ancho, ap)
        sup = interpolar(fuga_y, 0, ap)
        inf = interpolar(fuga_y, alto, ap)

        alto_pared = max(1, inf - sup)
        y = sup + int(b * alto_pared * 0.75)
        alto_m = max(2, int(alto_pared * (0.06 + c * 0.16)))
        ancho_m = max(2, int(abs(borde - fuga_x) * 0.05 * (0.5 + c)))

        x0 = borde if izquierda else borde - ancho_m
        x1 = borde + ancho_m if izquierda else borde
        luz_m = luz * (0.30 + 0.70 * prof)
        lz.fill(x0, y, x1, y + alto_m, con_alfa(iluminar(MOHO, luz_m), 0.18 + c * 0.22))

    # --- fluorescentes ---
    for i in range(TRAMOS, 0, -1):
        lejos = i / TRAMOS
        ap = escala(lejos)
        izq = interpolar(fuga_x, 0, ap)
        der = interpolar(fuga_x, ancho, ap)
        sup = interpolar(fuga_y, 0, ap)

        centro = (izq + der) // 2
        medio = max(2, (der - izq) // 14)
        grosor = max(1, int((alto * 0.007) * lejos))
        propio = tropiezo(tiempo, i)
        brillo = luz * (0.35 + 0.65 * lejos) * propio
        y = sup + max(1, int(alto * 0.015 * lejos))

        halo_a = medio * 2
        halo_h = max(2, grosor * 4)
        lz.fill_gradient(centro - halo_a, y - halo_h, centro + halo_a, y + halo_h,
                         con_alfa(FLUOR, 0.0), con_alfa(FLUOR, 0.16 * brillo))
        lz.fill(centro - medio, y, centro + medio, y + grosor, con_alfa(FLUOR, 0.35 + 0.60 * brillo))

        inf_t = interpolar(fuga_y, alto, ap)
        refl = max(2, int(alto * 0.05 * lejos))
        lz.fill_gradient(centro - medio, inf_t - refl, centro + medio, inf_t,
                         con_alfa(FLUOR, 0.10 * brillo), con_alfa(FLUOR, 0.0))

    # --- vano ---
    ancho_v = max(10, int(ancho * 0.055))
    alto_v = max(16, int(alto * 0.16))
    x0 = fuga_x - ancho_v // 2
    x1 = fuga_x + ancho_v // 2
    y0 = fuga_y - alto_v // 3
    y1 = fuga_y + (alto_v * 2) // 3
    lz.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, con_alfa(MOHO, 0.75))
    lz.fill(x0, y0, x1, y1, VANO)

    if silueta_t is not None:
        t = silueta_t
        ancho_s = max(3, ancho_v // 3)
        recorrido = ancho_v + ancho_s * 2
        x = x0 - ancho_s + int(t * recorrido)
        vi = max(x0, x)
        vd = min(x1, x + ancho_s)
        if vd > vi:
            alfa = 0.85 * math.sin(t * math.pi)
            cuerpo_y = y0 + alto_v // 6
            lz.fill(vi, cuerpo_y, vd, y1, con_alfa(ALERTA, alfa * 0.30))
            lz.fill(vi, cuerpo_y, vd, y1, con_alfa(VANO, alfa * 0.65))

    # --- polvo ---
    for i in range(MOTAS):
        a = pseudo(i * 13 + 5)
        b = pseudo(i * 29 + 11)
        x = int((a * ancho * 1.7 + tiempo * (3.0 + a * 5.0)) % max(1, ancho))
        y = int((b * alto + tiempo * (4.0 + b * 7.0)) % max(1, alto))
        cerca = 1.0 - abs(y / max(1.0, alto) - 0.45) * 2.0
        brillo = (0.06 + 0.20 * luz) * max(0.0, cerca)
        lz.fill(x, y, x + 1, y + 1, con_alfa(FLUOR, brillo))

    # --- vineta ---
    franja = max(24, ancho // 6)
    intensidad = 0.38 + 0.42 * penumbra
    for x in range(0, min(franja, ancho)):
        t = x / max(1, franja)
        lz.fill(x, 0, x + 1, alto, con_alfa(VANO, intensidad * (1.0 - t)))
    for x in range(max(0, ancho - franja), ancho):
        t = (x - (ancho - franja)) / max(1, franja)
        lz.fill(x, 0, x + 1, alto, con_alfa(VANO, intensidad * t))
    lz.fill_gradient(0, 0, ancho, franja // 2, con_alfa(VANO, intensidad * 0.55), con_alfa(VANO, 0.0))
    lz.fill_gradient(0, alto - franja // 2, ancho, alto, con_alfa(VANO, 0.0), con_alfa(VANO, intensidad * 0.9))

    if penumbra > 0.0:
        lz.fill(0, 0, ancho, alto, con_alfa(VANO, 0.35 * penumbra))


# --------------------------------------------------------------------------
# La hoja de avisos, en bloques (sin tipografia: solo la mancha del texto)
# --------------------------------------------------------------------------
def hoja(lz: Lienzo) -> None:
    ancho, alto = lz.ancho, lz.alto
    hx = max(14, int(ancho * 0.07))
    hy = max(16, int(alto * 0.13))
    ha = min(alto - hy - 16, 208)
    hw = 214

    lz.fill(hx + 3, hy + 4, hx + hw + 3, hy + ha + 4, con_alfa(VANO, 0.30))
    lz.fill(hx, hy, hx + hw, hy + ha, con_alfa(PAPEL, 0.94))
    lz.fill(hx, hy, hx + hw, hy + 1, con_alfa(MOHO, 0.35))
    lz.fill(hx, hy + ha - 1, hx + hw, hy + ha, con_alfa(MOHO, 0.45))
    lz.fill(hx, hy, hx + 1, hy + ha, con_alfa(MOHO, 0.35))
    lz.fill(hx + hw - 1, hy, hx + hw, hy + ha, con_alfa(MOHO, 0.35))

    centro = hx + hw // 2
    lz.fill(centro - 22, hy - 4, centro + 22, hy + 4, con_alfa(PAPEL, 0.45))

    x = hx + 12
    y = hy + 12
    lz.fill(x, y, x + 66, y + 16, con_alfa(TINTA, 0.85))          # JOBS
    lz.fill(x, y + 20, x + 150, y + 27, con_alfa(TINTA_TENUE, 0.75))
    lz.fill(x, y + 33, x + hw - 24, y + 34, con_alfa(TINTA_TENUE, 0.45))
    lz.fill(x, y + 42, x + 130, y + 49, con_alfa(TINTA_TENUE, 0.70))
    lz.fill(x, y + 54, x + 165, y + 61, con_alfa(TINTA, 0.80))

    ry = hy + 88
    for i in range(4):
        yy = ry + i * 23
        if i == 0:
            lz.fill(x - 3, yy, x + hw - 24 + 3, yy + 20, con_alfa(TINTA_TENUE, 0.14))
        cy = yy + (20 - 7) // 2
        for (a, b, c, d) in ((x, cy, x + 7, cy + 1), (x, cy + 6, x + 7, cy + 7),
                             (x, cy, x + 1, cy + 7), (x + 6, cy, x + 7, cy + 7)):
            lz.fill(a, b, c, d, con_alfa(TINTA_TENUE, 0.70))
        if i == 0:
            lz.fill(x + 2, cy + 2, x + 6, cy + 6, con_alfa(TINTA, 0.95))
        lz.fill(x + 14, yy + 6, x + 26, yy + 13, con_alfa(TINTA_TENUE, 0.75))
        largo = (90, 118, 126, 108)[i]
        lz.fill(x + 32, yy + 6, x + 32 + largo, yy + 13, con_alfa(TINTA, 0.80 if i == 0 else 0.60))
        for px in range(x + 32 + largo + 4, x + hw - 26, 3):
            lz.fill(px, yy + 12, px + 1, yy + 13, con_alfa(TINTA_TENUE, 0.30))

    ay = hy + ha - 26
    lz.fill(x, ay, x + hw - 30, ay + 7, con_alfa(TINTA_TENUE, 0.55))
    lz.fill(x, ay + 10, x + 96, ay + 17, con_alfa(TINTA_TENUE, 0.55))

    # reloj de ronda, arriba a la derecha
    m = 12
    lz.fill(ancho - m - 96 - 8, m - 6, ancho - m + 6, m + 26, con_alfa(VANO, 0.45))
    lz.fill(ancho - m - 70, m, ancho - m, m + 7, con_alfa(PAPEL, 0.80))
    lz.fill(ancho - m - 30, m + 13, ancho - m, m + 20, con_alfa(TINTA_TENUE, 0.0) | (ALERTA & 0x00FFFFFF) | 0xC0000000)

    # sello
    lz.fill(ancho - m - 60, alto - 14, ancho - m, alto - 8, con_alfa(PAPEL, 0.45))


def main() -> int:
    ancho = int(sys.argv[1]) if len(sys.argv) > 1 else 427
    alto = int(sys.argv[2]) if len(sys.argv) > 2 else 240
    salida = Path(sys.argv[3]) if len(sys.argv) > 3 else Path("vista_previa.png")

    lz = Lienzo(ancho, alto)
    dibujar(lz)
    hoja(lz)
    lz.png(salida)
    print(f"Escrito {salida} ({ancho}x{alto})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
