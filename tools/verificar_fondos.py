#!/usr/bin/env python3
"""Valida los fondos PNG 10-17 y JPEG 18-31 versionados como recursos directos."""
from __future__ import annotations

import binascii
import struct
import sys
import zlib
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
DIR = RAIZ / 'src/main/resources/assets/jobsmenu/textures/backgrounds'
DIM_PNG = {
    10: (192, 108), 11: (256, 144), 12: (192, 108), 13: (192, 108),
    14: (256, 127), 15: (192, 108), 16: (192, 108), 17: (192, 108),
}
DIM_JPEG = {i: (1920, 1080) for i in range(18, 32)}
CANALES = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}


def validar_png(i: int) -> None:
    p = DIR / f'nivel{i}.png'
    if not p.is_file():
        raise RuntimeError(f'falta {p.relative_to(RAIZ)}')

    b = p.read_bytes()
    if len(b) < 4_000:
        raise RuntimeError(f'nivel {i}: PNG demasiado pequeno ({len(b)} bytes)')
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
            raise RuntimeError(f'nivel {i}: CRC invalido en {tipo.decode("ascii", "replace")}')

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
        raise RuntimeError(f'nivel {i}: estructura PNG incompleta')

    ancho, alto, bits, color, compresion, filtro, entrelazado = ihdr
    if (ancho, alto) != DIM_PNG[i]:
        raise RuntimeError(f'nivel {i}: dimensiones {(ancho, alto)}, esperado {DIM_PNG[i]}')
    if compresion != 0 or filtro != 0 or entrelazado != 0 or color not in CANALES:
        raise RuntimeError(f'nivel {i}: formato PNG inesperado')

    try:
        bruto = zlib.decompress(b''.join(idat))
    except zlib.error as exc:
        raise RuntimeError(f'nivel {i}: IDAT corrupto: {exc}') from exc

    bytes_fila = (ancho * CANALES[color] * bits + 7) // 8
    esperado = (bytes_fila + 1) * alto
    if len(bruto) != esperado:
        raise RuntimeError(f'nivel {i}: pixels PNG incompletos ({len(bruto)} != {esperado})')
    if pos != len(b):
        raise RuntimeError(f'nivel {i}: datos extra despues de IEND')

    print(f'nivel {i}: PNG OK {ancho}x{alto} ({len(b)} bytes)')


def dimensiones_jpeg(data: bytes) -> tuple[int, int]:
    if len(data) < 4 or data[:2] != b'\xff\xd8' or data[-2:] != b'\xff\xd9':
        raise RuntimeError('firma JPEG invalida')

    pos = 2
    sof = {0xC0, 0xC1, 0xC2, 0xC3, 0xC5, 0xC6, 0xC7,
           0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF}

    while pos + 4 <= len(data):
        if data[pos] != 0xFF:
            pos += 1
            continue
        while pos < len(data) and data[pos] == 0xFF:
            pos += 1
        if pos >= len(data):
            break

        marker = data[pos]
        pos += 1
        if marker in (0xD8, 0xD9) or 0xD0 <= marker <= 0xD7:
            continue
        if marker == 0x01:
            continue
        if pos + 2 > len(data):
            break

        largo = int.from_bytes(data[pos:pos + 2], 'big')
        if largo < 2 or pos + largo > len(data):
            raise RuntimeError('segmento JPEG truncado')
        if marker in sof:
            if largo < 7:
                raise RuntimeError('SOF JPEG invalido')
            alto = int.from_bytes(data[pos + 3:pos + 5], 'big')
            ancho = int.from_bytes(data[pos + 5:pos + 7], 'big')
            return ancho, alto
        if marker == 0xDA:
            break
        pos += largo

    raise RuntimeError('JPEG sin marcador SOF valido')


def validar_jpeg(i: int) -> None:
    p = DIR / f'nivel{i}.jpg'
    if not p.is_file():
        raise RuntimeError(f'falta {p.relative_to(RAIZ)}')

    data = p.read_bytes()
    if len(data) < 50_000:
        raise RuntimeError(f'nivel {i}: JPEG demasiado pequeno ({len(data)} bytes)')

    dim = dimensiones_jpeg(data)
    if dim != DIM_JPEG[i]:
        raise RuntimeError(f'nivel {i}: dimensiones {dim}, esperado {DIM_JPEG[i]}')

    print(f'nivel {i}: JPEG OK {dim[0]}x{dim[1]} ({len(data)} bytes)')


def main() -> int:
    try:
        for i in range(10, 18):
            validar_png(i)
        for i in range(18, 32):
            validar_jpeg(i)
    except Exception as exc:
        print(f'ERROR fondos: {exc}', file=sys.stderr)
        return 1

    print('Fondos 10-31: recursos directos OK')
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
