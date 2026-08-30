#!/usr/bin/env python3
"""Materializa y valida los fondos de imagen 10-17.

Durante esta migracion los PNG se transportan como base64, a veces dividido en
partes para evitar truncados del conector. El build reconstruye todos los
fondos antes de verificarlos, de forma que un asset corrupto nunca llegue al
JAR publicado.
"""
from __future__ import annotations

import base64
import binascii
import struct
import sys
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
DESTINO = RAIZ / "src/main/resources/assets/jobsmenu/textures/backgrounds"
PAYLOAD = RAIZ / "tools/background_payload"
ESPERADOS = range(10, 18)
ANCHO = 256
ALTO = 144
MIN_BYTES = 8_000


def dimensiones_png(datos: bytes) -> tuple[int, int]:
    if len(datos) < 24 or datos[:8] != b"\x89PNG\r\n\x1a\n" or datos[12:16] != b"IHDR":
        raise ValueError("firma/IHDR PNG invalida")
    return struct.unpack(">II", datos[16:24])


def leer_payload(indice: int) -> str:
    unico = PAYLOAD / f"nivel{indice}.b64"
    if unico.is_file():
        return "".join(unico.read_text(encoding="ascii").split())
    partes = sorted(PAYLOAD.glob(f"nivel{indice}.part*"))
    if not partes:
        raise FileNotFoundError(f"falta payload del nivel {indice}")
    return "".join("".join(p.read_text(encoding="ascii").split()) for p in partes)


def materializar(indice: int) -> None:
    try:
        datos = base64.b64decode(leer_payload(indice), validate=True)
    except (ValueError, binascii.Error) as exc:
        raise ValueError(f"payload invalido nivel {indice}: {exc}") from exc
    DESTINO.mkdir(parents=True, exist_ok=True)
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
        for indice in ESPERADOS:
            materializar(indice)
        print("Fondos de imagen:")
        for indice in ESPERADOS:
            validar(indice)
    except Exception as exc:
        print(f"ERROR fondos: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
