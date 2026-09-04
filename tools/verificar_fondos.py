#!/usr/bin/env python3
"""Valida fondos PNG 10-17 y el pack JPEG 18-31."""
from __future__ import annotations

import binascii
import struct
import sys
import zipfile
import zlib
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
DIR = RAIZ / 'src/main/resources/assets/jobsmenu/textures/backgrounds'
PACK = RAIZ / 'assets/backgrounds/niveles18-31.zip'
DIM_PNG = {
    10: (192, 108), 11: (256, 144), 12: (192, 108), 13: (192, 108),
    14: (256, 127), 15: (192, 108), 16: (192, 108), 17: (192, 108),
}
CANALES = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}


def validar_png(i: int) -> None:
    p = DIR / f'nivel{i}.png'
    if not p.is_file():
        raise RuntimeError(f'falta {p.relative_to(RAIZ)}')
    b = p.read_bytes()
    if len(b) < 4000 or b[:8] != b'\x89PNG\r\n\x1a\n':
        raise RuntimeError(f'nivel {i}: PNG ausente/invalido')
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
        if crc_fin > len(b): raise RuntimeError(f'nivel {i}: chunk truncado')
        datos = b[inicio:fin]
        crc_guardado = struct.unpack('>I', b[fin:crc_fin])[0]
        crc_real = binascii.crc32(datos, binascii.crc32(tipo)) & 0xFFFFFFFF
        if crc_real != crc_guardado: raise RuntimeError(f'nivel {i}: CRC invalido')
        if tipo == b'IHDR': ihdr = struct.unpack('>IIBBBBB', datos)
        elif tipo == b'IDAT': idat.append(datos)
        elif tipo == b'IEND':
            vio_iend = True
            pos = crc_fin
            break
        pos = crc_fin
    if ihdr is None or not idat or not vio_iend: raise RuntimeError(f'nivel {i}: estructura PNG incompleta')
    ancho, alto, bits, color, compresion, filtro, entrelazado = ihdr
    if (ancho, alto) != DIM_PNG[i]: raise RuntimeError(f'nivel {i}: dimensiones {(ancho, alto)}, esperado {DIM_PNG[i]}')
    if compresion != 0 or filtro != 0 or entrelazado != 0 or color not in CANALES: raise RuntimeError(f'nivel {i}: formato PNG inesperado')
    bruto = zlib.decompress(b''.join(idat))
    bytes_fila = (ancho * CANALES[color] * bits + 7) // 8
    if len(bruto) != (bytes_fila + 1) * alto: raise RuntimeError(f'nivel {i}: pixels PNG incompletos')
    if pos != len(b): raise RuntimeError(f'nivel {i}: datos extra tras IEND')
    print(f'nivel {i}: PNG OK {ancho}x{alto}')


def jpeg_dim(data: bytes) -> tuple[int, int]:
    if len(data) < 4 or data[:2] != b'\xff\xd8' or data[-2:] != b'\xff\xd9': raise RuntimeError('firma JPEG invalida')
    pos = 2
    sof = {0xC0, 0xC1, 0xC2, 0xC3, 0xC5, 0xC6, 0xC7, 0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF}
    while pos + 4 <= len(data):
        if data[pos] != 0xFF:
            pos += 1
            continue
        while pos < len(data) and data[pos] == 0xFF: pos += 1
        if pos >= len(data): break
        marker = data[pos]
        pos += 1
        if marker in (0xD8, 0xD9) or 0xD0 <= marker <= 0xD7: continue
        if pos + 2 > len(data): break
        length = int.from_bytes(data[pos:pos+2], 'big')
        if length < 2 or pos + length > len(data): raise RuntimeError('segmento JPEG truncado')
        if marker in sof:
            if length < 7: raise RuntimeError('SOF JPEG invalido')
            h = int.from_bytes(data[pos+3:pos+5], 'big')
            w = int.from_bytes(data[pos+5:pos+7], 'big')
            return w, h
        pos += length
    raise RuntimeError('JPEG sin SOF')


def validar_pack() -> None:
    if not PACK.is_file(): raise RuntimeError('falta assets/backgrounds/niveles18-31.zip')
    with zipfile.ZipFile(PACK) as z:
        error = z.testzip()
        if error: raise RuntimeError(f'ZIP corrupto en {error}')
        esperados = [f'nivel{i}.jpg' for i in range(18, 32)]
        if sorted(z.namelist()) != sorted(esperados): raise RuntimeError(f'contenido ZIP inesperado: {z.namelist()}')
        for i in range(18, 32):
            name = f'nivel{i}.jpg'
            data = z.read(name)
            if len(data) < 4000: raise RuntimeError(f'{name}: demasiado pequeno')
            dim = jpeg_dim(data)
            if dim != (960, 540): raise RuntimeError(f'{name}: dimensiones {dim}, esperado (960, 540)')
            print(f'nivel {i}: JPEG OK {dim[0]}x{dim[1]} ({len(data)} bytes)')


def main() -> int:
    try:
        for i in range(10, 18): validar_png(i)
        validar_pack()
    except Exception as exc:
        print(f'ERROR fondos: {exc}', file=sys.stderr)
        return 1
    print('Fondos 10-31: PNG 10-17 + JPEG 18-31 OK')
    return 0

if __name__ == '__main__':
    raise SystemExit(main())
