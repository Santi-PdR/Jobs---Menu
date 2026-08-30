#!/usr/bin/env python3
"""Materializa y valida los fondos de imagen 10-17 durante la migracion.

10-13 se reconstruyen desde copias base64 corregidas; 14 ya es un PNG valido
en resources; 15-17 se reconstruyen desde los tres fondos nuevos. Cada archivo
se valida por firma, dimensiones, tamano y chunk IEND antes de compilar.
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
DIMENSIONES = {
    10: (256, 144), 11: (256, 144), 12: (256, 144), 13: (256, 144),
    14: (256, 127),
    15: (192, 108), 16: (192, 108), 17: (192, 108),
}
MIN_BYTES = 5_000


def dimensiones_png(datos: bytes) -> tuple[int, int]:
    if len(datos) < 24 or datos[:8] != b"\x89PNG\r\n\x1a\n" or datos[12:16] != b"IHDR":
        raise ValueError("firma/IHDR PNG invalida")
    return struct.unpack(">II", datos[16:24])


def leer_b64(ruta: Path) -> bytes:
    try:
        return base64.b64decode("".join(ruta.read_text(encoding="ascii").split()), validate=True)
    except (ValueError, binascii.Error) as exc:
        raise ValueError(f"payload invalido {ruta.name}: {exc}") from exc


def materializar(indice: int) -> None:
    if indice == 14:
        return
    nombre = f"nivel{indice}.fixed.b64" if indice <= 13 else f"nivel{indice}.b64"
    origen = PAYLOAD / nombre
    if not origen.is_file():
        raise FileNotFoundError(f"falta {origen.relative_to(RAIZ)}")
    DESTINO.mkdir(parents=True, exist_ok=True)
    (DESTINO / f"nivel{indice}.png").write_bytes(leer_b64(origen))


def validar(indice: int) -> None:
    ruta = DESTINO / f"nivel{indice}.png"
    if not ruta.is_file():
        raise FileNotFoundError(f"falta {ruta.relative_to(RAIZ)}")
    datos = ruta.read_bytes()
    if len(datos) < MIN_BYTES:
        raise ValueError(f"nivel {indice}: PNG sospechosamente pequeno ({len(datos)} bytes)")
    ancho, alto = dimensiones_png(datos)
    esperado = DIMENSIONES[indice]
    if (ancho, alto) != esperado:
        raise ValueError(f"nivel {indice}: {ancho}x{alto}; se espera {esperado[0]}x{esperado[1]}")
    if not datos.endswith(b"IEND\xaeB`\x82"):
        raise ValueError(f"nivel {indice}: PNG truncado (falta IEND)")
    print(f"  nivel {indice}: OK {ancho}x{alto}, {len(datos)} bytes")


def main() -> int:
    try:
        for indice in range(10, 18):
            materializar(indice)
        print("Fondos de imagen:")
        for indice in range(10, 18):
            validar(indice)
    except Exception as exc:
        print(f"ERROR fondos: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
