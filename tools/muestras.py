#!/usr/bin/env python3
"""Materia prima real: grabaciones y su cadena de diseno sonoro.

QUE ES ESTO Y POR QUE EXISTE

Las cuatro generaciones anteriores de sonidos de interfaz se sintetizaban desde
cero -ruido, osciladores, filtros- y las cuatro fallaron por el mismo motivo de
fondo: por muy bien modelado que este, un sonido sintetizado no tiene detras la
suciedad de un objeto real. Un golpe de verdad tiene el roce de la mano antes
del impacto, la resonancia irregular de la pieza, el aire de la habitacion en
la que se grabo y una cola que no cae en linea recta. Eso no se emula con un
banco de resonadores: se graba.

Asi que la cadena cambia de raiz:

    ANTES:  ruido -> sintesis -> filtro -> "sonido de boton"
    AHORA:  grabacion real -> diseno sonoro -> sonido final

LA MATERIA PRIMA

En tools/crudo/ hay 46 grabaciones reales, en dos tandas.

Las primeras 28 son objetos fisicos siendo golpeados: madera, tablon, metal,
chapa, hojalata, vidrio, hormigon, material blando.

Las 18 restantes se sumaron en 0.6.0 y son de otras CLASES de gesto, no de
otros materiales: roces (roceCorto, roceLargo, roceSuave, roceTela),
trinquetes, pestillos que deslizan y enganchan, y objetos que se posan.

Esa segunda tanda existe porque la familia de sonidos de interfaz habia
fracasado dos veces por la misma razon de fondo: toda la materia prima eran
impactos, y un impacto siempre tiene la misma envolvente -transitorio y
decaimiento- por mucho que cambie el material. El oido clasifica por envolvente
antes que por material, asi que ocho impactos suenan a un solo sonido con ocho
alturas. Faltaban clases de gesto, no mas materiales.

Todas son del mismo origen: paquetes de Kenney (kenney.nl), publicados bajo
**Creative Commons Zero (CC0 1.0)**: dominio publico, uso comercial permitido,
redistribucion permitida, atribucion no obligatoria. El texto de la licencia
esta archivado en tools/crudo/LICENCIA.txt. Se acreditan igual, en el README y
en los creditos del mod, porque corresponde aunque la licencia no lo exija.

Estan versionadas dentro del repo a proposito. Son 2.6 MB y garantizan que
cualquiera pueda reconstruir el audio del mod sin depender de que una URL siga
viva dentro de dos anos.

EL DISENO SONORO

Nada se usa crudo. Cada gesto del menu se construye tomando una o dos
grabaciones y pasandolas por una cadena de edicion real: recorte del silencio,
seleccion del trozo que interesa, cambio de altura, ecualizacion, filtrado del
brillo que delata la muestra de libreria, mezcla de capas, inversion, fundidos
y la sala del mod encima. El objetivo no es que el boton suene literalmente a
madera: es que el oido perciba que hay un objeto fisico detras de la interfaz.
"""

from __future__ import annotations

from pathlib import Path

import numpy as np
import soundfile as sf
from scipy import signal

SR = 44_100
CRUDO = Path(__file__).resolve().parent / "crudo"

# Cache: cada archivo se lee del disco una sola vez por ejecucion.
_CACHE: dict[str, np.ndarray] = {}


def cargar(nombre: str) -> np.ndarray:
    """Lee una grabacion, la pasa a mono y le recorta el silencio de los bordes.

    Las librerias suelen dejar unos milisegundos de aire antes del golpe. Ese
    hueco, multiplicado por ocho gestos, es lo que hace que una interfaz se
    sienta con retardo aunque el codigo dispare el sonido en el momento justo.
    """
    if nombre in _CACHE:
        return _CACHE[nombre].copy()

    ruta = CRUDO / f"{nombre}.wav"
    datos, sr = sf.read(ruta, dtype="float64")
    if datos.ndim > 1:
        datos = datos.mean(axis=1)

    if sr != SR:
        datos = signal.resample_poly(datos, SR, sr)

    umbral = float(np.max(np.abs(datos))) * 0.02
    vivos = np.where(np.abs(datos) > umbral)[0]
    if len(vivos) > 0:
        datos = datos[vivos[0]:vivos[-1] + 1]

    _CACHE[nombre] = datos
    return datos.copy()


def altura(x: np.ndarray, factor: float) -> np.ndarray:
    """Cambia la altura remuestreando: mas grave tambien es mas largo.

    Es el cambio de altura de una cinta, no el de un afinador. Al bajar un
    golpe una octava el objeto se vuelve mas grande y mas lento, que es
    justamente lo que se busca: la misma pieza de madera pasa a ser una viga.
    """
    if abs(factor - 1.0) < 1e-6:
        return x
    n = max(1, int(round(len(x) / factor)))
    return signal.resample(x, n)


def recortar(x: np.ndarray, desde: float = 0.0, hasta: float | None = None) -> np.ndarray:
    """Se queda con un trozo, en segundos. El bisturi del diseno sonoro."""
    a = int(desde * SR)
    b = len(x) if hasta is None else min(len(x), int(hasta * SR))
    return x[a:b]


def cola(x: np.ndarray, desde: float) -> np.ndarray:
    """Descarta el ataque y deja solo la resonancia.

    Sirve para quitar el transitorio duro sin perder el cuerpo del material:
    el objeto sigue siendo el mismo, pero ya no se oye el momento del golpe.
    """
    return x[int(desde * SR):]


def invertir(x: np.ndarray) -> np.ndarray:
    """Del reves. Un golpe invertido es una succion: entra en vez de salir."""
    return x[::-1].copy()


def _sos(tipo: str, corte, orden: int):
    nyq = SR / 2.0
    if isinstance(corte, (list, tuple)):
        wn = [max(1e-4, min(0.999, c / nyq)) for c in corte]
    else:
        wn = max(1e-4, min(0.999, corte / nyq))
    return signal.butter(orden, wn, btype=tipo, output="sos")


def pasabajos(x: np.ndarray, corte: float, orden: int = 4) -> np.ndarray:
    return signal.sosfiltfilt(_sos("lowpass", corte, orden), x)


def pasaaltos(x: np.ndarray, corte: float, orden: int = 4) -> np.ndarray:
    return signal.sosfiltfilt(_sos("highpass", corte, orden), x)


def realzar(x: np.ndarray, frecuencia: float, q: float, ganancia: float) -> np.ndarray:
    """Campana de ecualizacion. Para sacarle o darle cuerpo a una zona."""
    b, a = signal.iirpeak(frecuencia / (SR / 2.0), q)
    return x + (ganancia - 1.0) * signal.filtfilt(b, a, x)


def normalizar(x: np.ndarray, pico: float = 0.9) -> np.ndarray:
    maximo = float(np.max(np.abs(x)))
    if maximo < 1e-12:
        return x
    return x * (pico / maximo)


def fundido(x: np.ndarray, entrada: float = 0.004, salida: float = 0.03) -> np.ndarray:
    """Fundidos de entrada y salida. Sin esto hay chasquido en los bordes."""
    y = x.copy()
    a = min(int(entrada * SR), len(y) // 2)
    b = min(int(salida * SR), len(y) // 2)
    if a > 0:
        y[:a] *= np.linspace(0.0, 1.0, a) ** 0.6
    if b > 0:
        y[-b:] *= np.linspace(1.0, 0.0, b) ** 1.4
    return y


def suavizar_ataque(x: np.ndarray, milis: float) -> np.ndarray:
    """Redondea el arranque.

    Un transitorio instantaneo es exactamente lo que el oido lee como "clic de
    computadora". Redondeando los primeros milisegundos el mismo golpe pasa de
    sonar a interfaz a sonar a objeto tocado por una mano.
    """
    n = min(int(milis / 1000.0 * SR), len(x))
    if n <= 0:
        return x
    y = x.copy()
    y[:n] *= np.linspace(0.0, 1.0, n) ** 1.8
    return y


def apilar(*capas: tuple[np.ndarray, float]) -> np.ndarray:
    """Mezcla capas de distinta longitud alineadas por el principio."""
    largo = max(len(c) for c, _ in capas)
    salida = np.zeros(largo)
    for capa, peso in capas:
        salida[:len(capa)] += capa * peso
    return salida


def retrasar(x: np.ndarray, segundos: float) -> np.ndarray:
    """Mete silencio delante. Para separar dos golpes de un mismo gesto."""
    return np.concatenate([np.zeros(int(segundos * SR)), x])


def sala(x: np.ndarray, ir: np.ndarray, mezcla: float) -> np.ndarray:
    """Mete la pieza en el recinto del mod.

    Es lo que hace que los ocho gestos pertenezcan al mismo sitio aunque cada
    uno venga de un objeto distinto: distinta cantidad de sala, misma sala.
    """
    humedo = signal.fftconvolve(x, ir)[:len(x) + len(ir) - 1]
    seco = np.concatenate([x, np.zeros(len(humedo) - len(x))])
    humedo = normalizar(humedo, float(np.max(np.abs(seco))) or 1.0)
    return (1.0 - mezcla) * seco + mezcla * humedo
