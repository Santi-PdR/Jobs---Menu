#!/usr/bin/env python3
"""Valida que los fondos 10-17 sean PNG realmente decodificables.

No alcanza con comprobar firma, IHDR e IEND: un PNG puede conservar esos
marcadores y tener el flujo IDAT roto. Eso produce la textura morado/negro de
Minecraft. Este verificador comprueba CRC de chunks y descomprime IDAT completo.
"""
from __future__ import annotations

import binascii
import struct
import sys
import zlib
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
DIR = RAIZ / 'src/main/resources/assets/jobsmenu/textures/backgrounds'
DIM = {
    10: (192, 108),
    11: (256, 144),
    12: (192, 108),
    13: (192, 108),
    14: (256, 127),
    15: (192, 108),
    16: (192, 108),
    17: (192, 108),
}
CANALES = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}


def validar(i: int) -> None:
    p = DIR / f'nivel{i}.png'
    if not p.is_file():
        raise RuntimeError(f'falta {p.relative_to(RAIZ)}')

    b = p.read_bytes()
    if len(b) < 4_000:
        raise RuntimeError(f'nivel {i}: archivo demasiado pequeno ({len(b)} bytes)')
    if b[:8] != b'\x89PNG\r\n\x1a\n':
        raise RuntimeError(f'nivel {i}: firma PNG invalida')

    pos = 8
    ihdr = None
    idat = []
    vio_iend = False

    while pos + 12 <= len(b):
        largo = struct.unpack('>I', b[pos:pos + 4])[0]
        tipo = b[pos + 4:pos + 8]
        inicio = pos + 8
        fin = inicio + largo
        crc_fin = fin + 4
        if crc_fin > len(b):
            raise RuntimeError(f'nivel {i}: chunk {tipo!r} truncado')

        datos = b[inicio:fin]
        crc_guardado = struct.unpack('>I', b[fin:crc_fin])[0]
        crc_real = binascii.crc32(tipo)
        crc_real = binascii.crc32(datos, crc_real) & 0xFFFFFFFF
        if crc_real != crc_guardado:
            raise RuntimeError(f'nivel {i}: CRC invalido en chunk {tipo.decode("ascii", "replace")}')

        if tipo == b'IHDR':
            if largo != 13:
                raise RuntimeError(f'nivel {i}: IHDR invalido')
            ihdr = struct.unpack('>IIBBBBB', datos)
        elif tipo == b'IDAT':
            idat.append(datos)
        elif tipo == b'IEND':
            vio_iend = True
            pos = crc_fin
            break

        pos = crc_fin

    if ihdr is None or not idat or not vio_iend:
        raise RuntimeError(f'nivel {i}: faltan IHDR/IDAT/IEND')

    ancho, alto, bits, color, compresion, filtro, entrelazado = ihdr
    if (ancho, alto) != DIM[i]:
        raise RuntimeError(f'nivel {i}: dimensiones {(ancho, alto)}, se esperaba {DIM[i]}')
    if compresion != 0 or filtro != 0:
        raise RuntimeError(f'nivel {i}: metodo PNG no soportado')
    if entrelazado != 0:
        raise RuntimeError(f'nivel {i}: PNG entrelazado no permitido para estos fondos')
    if color not in CANALES:
        raise RuntimeError(f'nivel {i}: tipo de color PNG desconocido: {color}')

    try:
        bruto = zlib.decompress(b''.join(idat))
    except zlib.error as exc:
        raise RuntimeError(f'nivel {i}: IDAT corrupto/no descomprimible: {exc}') from exc

    bits_pixel = CANALES[color] * bits
    bytes_fila = (ancho * bits_pixel + 7) // 8
    esperado = (bytes_fila + 1) * alto
    if len(bruto) != esperado:
        raise RuntimeError(
            f'nivel {i}: flujo de pixels incompleto ({len(bruto)} bytes, se esperaban {esperado})'
        )

    if pos != len(b):
        # Evita aceptar basura silenciosa despues de IEND.
        raise RuntimeError(f'nivel {i}: datos extra despues de IEND')

    print(f'nivel {i}: OK {ancho}x{alto}, color={color}, bits={bits} ({len(b)} bytes)')


def main() -> int:
    try:
        for i in range(10, 18):
            validar(i)
    except Exception as exc:
        print(f'ERROR fondos: {exc}', file=sys.stderr)
        return 1
    print('Fondos 10-17: PNG/IDAT/CRC OK')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
