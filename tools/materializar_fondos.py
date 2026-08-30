#!/usr/bin/env python3
"""Materializa y valida los fondos de imagen 10-17.

El bundle base64 existe solo para transportar binarios de forma fiable durante
esta migracion. Antes de compilar se extraen los ocho PNG y se valida su firma,
dimensiones y tamano para impedir publicar assets truncados.
"""
from __future__ import annotations

import base64
import io
import struct
import sys
import zipfile
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
DESTINO = RAIZ / "src/main/resources/assets/jobsmenu/textures/backgrounds"
BUNDLE = RAIZ / "tools/background_payload/backgrounds.zip.b64"
ESPERADOS = range(10, 18)
ANCHO = 256
ALTO = 144
MIN_BYTES = 7_000


def dimensiones_png(datos: bytes) -> tuple[int, int]:
    if len(datos) < 24 or datos[:8] != b"\x89PNG\r\n\x1a\n" or datos[12:16] != b"IHDR":
        raise ValueError("firma/IHDR PNG invalida")
    return struct.unpack(">II", datos[16:24])


def materializar_bundle() -> None:
    if not BUNDLE.is_file():
        return
    crudo = base64.b64decode("".join(BUNDLE.read_text(encoding="ascii").split()), validate=True)
    DESTINO.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(io.BytesIO(crudo)) as zf:
        nombres = {Path(n).name: n for n in zf.namelist() if not n.endswith("/")}
        for indice in ESPERADOS:
            esperado = f"n{indice}_256_16.png"
            if esperado not in nombres:
                raise FileNotFoundError(f"bundle: falta {esperado}")
            datos = zf.read(nombres[esperado])
            (DESTINO / f"nivel{indice}.png").write_bytes(datos)


def validar(indice: int) -> None:
    ruta = DESTINO / f"nivel{indice}.png"
    if not ruta.is_file():
        raise FileNotFoundError(f"falta {ruta.relative_to(RAIZ)}")
    datos = ruta.read_bytes()
    if len(datos) < MIN_BYTES:
        raise ValueError(f"nivel {indice}: PNG sospechosamente pequeno ({len(datos)} bytes)")
    ancho, alto = dimensiones_png(datos)
    if (ancho, alto) != (ANCHO, ALTO):
        raise ValueError(f"nivel {indice}: {ancho}x{alto}; se espera {ANCHO}x{ALTO}")
    print(f"  nivel {indice}: OK {ancho}x{alto}, {len(datos)} bytes")


def main() -> int:
    try:
        materializar_bundle()
        print("Fondos de imagen:")
        for indice in ESPERADOS:
            validar(indice)
    except Exception as exc:
        print(f"ERROR fondos: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
