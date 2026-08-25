#!/usr/bin/env python3
"""Genera los sonidos del mod en OGG/Vorbis, que es lo unico que reproduce Minecraft.

Todo es sintesis propia: no hay muestras descargadas ni licencias de terceros.
La idea sonora del nivel es que no hay musica. Hay una instalacion electrica
vieja funcionando sola en un pasillo vacio, y algunos golpes secos de oficina.

    zumbido       ambiente en bucle: 50 Hz de red, su armonica y aire muerto
    recorrer      pasar el cursor por un renglon: papel rozando
    marcar        marcar la casilla: sello o clic de biro
    pesado        abrir otra pantalla: interruptor grande
    apagon        el nivel se va: el tubo se rinde
    encendido     el nivel nuevo prende: chispazos y arranque

Requiere numpy y soundfile:  pip install numpy soundfile
Uso:  python3 tools/sonidos.py [carpeta_destino]
"""

from __future__ import annotations

import sys
from pathlib import Path

import numpy as np
import soundfile as sf

FRECUENCIA = 44100
DESTINO = Path("src/main/resources/assets/jobsmenu/sounds")

rng = np.random.default_rng(0x4A4F4253)


def t(duracion: float) -> np.ndarray:
    return np.linspace(0.0, duracion, int(FRECUENCIA * duracion), endpoint=False)


def ruido(n: int) -> np.ndarray:
    return rng.uniform(-1.0, 1.0, n)


def pasabajos(x: np.ndarray, corte: float, veces: int = 1) -> np.ndarray:
    """Filtro de un polo, aplicado tantas veces como haga falta."""
    a = np.exp(-2.0 * np.pi * corte / FRECUENCIA)
    y = x
    for _ in range(veces):
        salida = np.empty_like(y)
        acumulado = 0.0
        for i, v in enumerate(y):
            acumulado = a * acumulado + (1.0 - a) * v
            salida[i] = acumulado
        y = salida
    return y


def pasaaltos(x: np.ndarray, corte: float) -> np.ndarray:
    return x - pasabajos(x, corte)


def campana(n: int, subida: float = 0.01, bajada: float = 0.2) -> np.ndarray:
    """Envolvente simple: sube rapido, cae exponencial."""
    env = np.ones(n)
    ns = max(1, int(FRECUENCIA * subida))
    env[:ns] = np.linspace(0.0, 1.0, ns)
    caida = np.exp(-np.arange(n) / (FRECUENCIA * bajada))
    return env * caida


def normalizar(x: np.ndarray, pico: float = 0.85) -> np.ndarray:
    m = np.max(np.abs(x))
    return x if m < 1e-9 else x * (pico / m)


def bucle_suave(x: np.ndarray, cruce: float = 0.5) -> np.ndarray:
    """Empalma el final con el principio para que el bucle no golpee."""
    n = int(FRECUENCIA * cruce)
    if n * 2 >= len(x):
        return x
    cabeza = x[:n]
    cola = x[-n:]
    rampa = np.linspace(0.0, 1.0, n)
    mezcla = cola * (1.0 - rampa) + cabeza * rampa
    return np.concatenate([mezcla, x[n:-n]])


# --------------------------------------------------------------------------
def zumbido(duracion: float = 12.0) -> np.ndarray:
    """El fluorescente. 50 Hz de red electrica, su armonica y el aire del pasillo."""
    x = t(duracion)
    n = len(x)

    # La red, con una deriva minima para que no suene sintetico.
    deriva = 1.0 + 0.0012 * np.sin(2 * np.pi * 0.07 * x)
    señal = 0.50 * np.sin(2 * np.pi * 50.0 * x * deriva)
    señal += 0.26 * np.sin(2 * np.pi * 100.0 * x * deriva + 0.7)
    señal += 0.10 * np.sin(2 * np.pi * 150.0 * x * deriva + 1.9)
    señal += 0.05 * np.sin(2 * np.pi * 200.0 * x + 0.3)

    # El siseo del balasto, arriba de todo.
    siseo = pasaaltos(pasabajos(ruido(n), 5200.0), 1800.0) * 0.05

    # Aire muerto: el volumen del pasillo, sin nada adentro.
    aire = pasabajos(ruido(n), 180.0, veces=3) * 0.55

    # Cada tanto el tubo carraspea.
    carraspeo = np.zeros(n)
    for centro in (2.4, 6.1, 9.7):
        i = int(centro * FRECUENCIA)
        largo = int(0.05 * FRECUENCIA)
        if i + largo < n:
            carraspeo[i:i + largo] += ruido(largo) * np.linspace(0.18, 0.0, largo)

    total = señal * 0.30 + siseo + aire + carraspeo * 0.4
    total = pasabajos(total, 7000.0)
    return bucle_suave(normalizar(total, 0.55), cruce=0.6)


def recorrer() -> np.ndarray:
    """Cursor sobre un renglon: la yema pasando por el papel. Casi nada."""
    d = 0.055
    x = t(d)
    n = len(x)
    roce = pasaaltos(pasabajos(ruido(n), 4200.0), 900.0)
    return normalizar(roce * campana(n, 0.004, 0.022), 0.30)


def marcar() -> np.ndarray:
    """Marcar la casilla: madera del sello contra la mesa y la tinta apretada."""
    d = 0.16
    x = t(d)
    n = len(x)

    golpe = ruido(n) * campana(n, 0.0006, 0.012)
    golpe = pasabajos(golpe, 2600.0)

    cuerpo = np.sin(2 * np.pi * 190.0 * x) * campana(n, 0.001, 0.045) * 0.55
    cuerpo += np.sin(2 * np.pi * 380.0 * x) * campana(n, 0.001, 0.028) * 0.25

    clic = np.zeros(n)
    i = int(0.012 * FRECUENCIA)
    largo = int(0.008 * FRECUENCIA)
    clic[i:i + largo] = ruido(largo) * np.linspace(1.0, 0.0, largo) * 0.6
    clic = pasaaltos(clic, 2200.0)

    return normalizar(golpe + cuerpo + clic, 0.80)


def pesado() -> np.ndarray:
    """Cambiar de pantalla: interruptor de pared, de los grandes."""
    d = 0.30
    x = t(d)
    n = len(x)

    palanca = np.zeros(n)
    i = int(0.004 * FRECUENCIA)
    largo = int(0.010 * FRECUENCIA)
    palanca[i:i + largo] = ruido(largo) * np.linspace(1.0, 0.0, largo)
    palanca = pasabajos(pasaaltos(palanca, 900.0), 6000.0)

    cuerpo = np.sin(2 * np.pi * 96.0 * x) * campana(n, 0.001, 0.070) * 0.7
    cuerpo += np.sin(2 * np.pi * 143.0 * x + 0.5) * campana(n, 0.002, 0.045) * 0.35

    cola = pasabajos(ruido(n), 420.0, veces=2) * campana(n, 0.02, 0.11) * 0.30
    return normalizar(palanca * 0.9 + cuerpo + cola, 0.82)


def apagon() -> np.ndarray:
    """El tubo se rinde: el zumbido cae de golpe y queda el eco del pasillo."""
    d = 1.1
    x = t(d)
    n = len(x)

    caida = np.exp(-x * 7.0)
    tono = np.sin(2 * np.pi * 100.0 * x * (1.0 - 0.25 * x)) * caida * 0.5
    tono += np.sin(2 * np.pi * 50.0 * x * (1.0 - 0.25 * x)) * caida * 0.35

    chasquido = np.zeros(n)
    i = int(0.002 * FRECUENCIA)
    largo = int(0.014 * FRECUENCIA)
    chasquido[i:i + largo] = ruido(largo) * np.linspace(1.0, 0.0, largo)
    chasquido = pasaaltos(chasquido, 1500.0) * 0.8

    resto = pasabajos(ruido(n), 260.0, veces=2) * np.exp(-x * 2.2) * 0.22
    return normalizar(tono + chasquido + resto, 0.78)


def encendido() -> np.ndarray:
    """El nivel nuevo prende: dos chispazos, una duda, y el zumbido se instala."""
    d = 1.6
    x = t(d)
    n = len(x)
    salida = np.zeros(n)

    # Los chispazos del arranque, en los mismos tiempos que la animacion.
    for centro, fuerza in ((0.02, 1.0), (0.22, 0.85), (0.46, 0.6)):
        i = int(centro * FRECUENCIA)
        largo = int(0.055 * FRECUENCIA)
        if i + largo >= n:
            continue
        chispa = ruido(largo) * np.exp(-np.linspace(0, 6, largo)) * fuerza
        salida[i:i + largo] += pasaaltos(chispa, 1200.0)

    # El zumbido entra recien sobre el final y se queda.
    entrada = np.clip((x - 0.55) / 0.7, 0.0, 1.0)
    red = np.sin(2 * np.pi * 100.0 * x) * 0.30 + np.sin(2 * np.pi * 50.0 * x) * 0.22
    salida += red * entrada

    siseo = pasaaltos(pasabajos(ruido(n), 5000.0), 2000.0) * entrada * 0.06
    return normalizar(salida + siseo, 0.75)


PIEZAS = {
    "zumbido": zumbido,
    "recorrer": recorrer,
    "marcar": marcar,
    "pesado": pesado,
    "apagon": apagon,
    "encendido": encendido,
}


def main() -> int:
    destino = Path(sys.argv[1]) if len(sys.argv) > 1 else DESTINO
    destino.mkdir(parents=True, exist_ok=True)

    for nombre, generar in PIEZAS.items():
        audio = generar().astype(np.float32)
        ruta = destino / f"{nombre}.ogg"
        sf.write(ruta, audio, FRECUENCIA, format="OGG", subtype="VORBIS")
        print(f"{ruta}  {len(audio) / FRECUENCIA:.2f}s  {ruta.stat().st_size / 1024:.1f} kB")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
