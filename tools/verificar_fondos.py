#!/usr/bin/env python3
"""Valida que todos los fondos de imagen empaquetados sean PNG completos."""
from __future__ import annotations
import struct, sys
from pathlib import Path
RAIZ=Path(__file__).resolve().parent.parent
DIR=RAIZ/'src/main/resources/assets/jobsmenu/textures/backgrounds'
DIM={10:(256,144),11:(256,144),12:(192,108),13:(256,144),14:(256,127),15:(192,108),16:(192,108),17:(192,108)}

def validar(i:int)->None:
    p=DIR/f'nivel{i}.png'
    if not p.is_file(): raise RuntimeError(f'falta {p.relative_to(RAIZ)}')
    b=p.read_bytes()
    if len(b)<5000: raise RuntimeError(f'nivel {i}: archivo demasiado pequeno ({len(b)} bytes)')
    if len(b)<24 or b[:8]!=b'\x89PNG\r\n\x1a\n' or b[12:16]!=b'IHDR': raise RuntimeError(f'nivel {i}: firma PNG invalida')
    wh=struct.unpack('>II',b[16:24])
    if wh!=DIM[i]: raise RuntimeError(f'nivel {i}: dimensiones {wh}, se esperaba {DIM[i]}')
    if not b.endswith(b'IEND\xaeB`\x82'): raise RuntimeError(f'nivel {i}: PNG truncado')
    print(f'nivel {i}: OK {wh[0]}x{wh[1]} ({len(b)} bytes)')

def main()->int:
    try:
        for i in range(10,18): validar(i)
    except Exception as e:
        print(f'ERROR fondos: {e}',file=sys.stderr); return 1
    print('Fondos 10-17: OK')
    return 0
if __name__=='__main__': raise SystemExit(main())
