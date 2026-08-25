#!/usr/bin/env python3
"""Genera la identidad sonora completa de Jobs - Aviso a los ocupantes.

Todo lo que suena en el mod nace aca. No hay una sola muestra de terceros:
cada pieza se sintetiza desde ruido y osciladores, se le da cuerpo con filtros
y se la mete en la sala que le corresponde con una reverberacion convolutiva
construida a mano. Eso resuelve de paso la cuestion de licencias: el mod puede
repartirse sin arrastrar derechos ajenos.

El criterio de diseno, en una linea: nada tiene que llamar la atencion. Un
menu que se escucha una hora seguida no puede tener nada agudo, nada corto y
seco, nada que se repita de forma reconocible.

    python3 tools/sonidos.py [carpeta_destino]

Requiere numpy, scipy y soundfile.
"""

from __future__ import annotations

import sys
from pathlib import Path

import numpy as np
import soundfile as sf
from scipy import signal

SR = 44_100
RAIZ = Path(__file__).resolve().parent.parent
DESTINO = RAIZ / "src/main/resources/assets/jobsmenu/sounds"

# Semilla fija: el mismo pasillo suena igual en cada compilacion.
RNG = np.random.default_rng(0x4A4F4253)


# ==========================================================================
# Utilidades basicas
# ==========================================================================

def muestras(segundos: float) -> int:
    return int(round(segundos * SR))


def tiempo(segundos: float) -> np.ndarray:
    return np.arange(muestras(segundos), dtype=np.float64) / SR


def blanco(segundos: float) -> np.ndarray:
    return RNG.normal(0.0, 1.0, muestras(segundos))


def rosa(segundos: float) -> np.ndarray:
    """Ruido rosa por conformado espectral: -3 dB por octava.

    Es el ruido de fondo de casi cualquier sala real. El blanco puro suena a
    television sin senal y cansa a los treinta segundos.
    """
    n = muestras(segundos)
    espectro = np.fft.rfft(RNG.normal(0.0, 1.0, n))
    frec = np.fft.rfftfreq(n, 1.0 / SR)
    frec[0] = frec[1] if len(frec) > 1 else 1.0
    return np.fft.irfft(espectro / np.sqrt(frec), n)


def marron(segundos: float) -> np.ndarray:
    """Ruido marron: -6 dB por octava. El retumbe de un edificio grande."""
    n = muestras(segundos)
    espectro = np.fft.rfft(RNG.normal(0.0, 1.0, n))
    frec = np.fft.rfftfreq(n, 1.0 / SR)
    frec[0] = frec[1] if len(frec) > 1 else 1.0
    return np.fft.irfft(espectro / frec, n)


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


def pasabanda(x: np.ndarray, bajo: float, alto: float, orden: int = 4) -> np.ndarray:
    return signal.sosfiltfilt(_sos("bandpass", [bajo, alto], orden), x)


def resonar(x: np.ndarray, frecuencia: float, q: float = 30.0, ganancia: float = 1.0) -> np.ndarray:
    """Realza una frecuencia concreta. Sirve para 'afinar' una tuberia o un tubo."""
    b, a = signal.iirpeak(frecuencia / (SR / 2.0), q)
    return x + ganancia * (signal.filtfilt(b, a, x) - x) * 2.0


def normalizar(x: np.ndarray, pico: float = 0.9) -> np.ndarray:
    maximo = float(np.max(np.abs(x)))
    if maximo < 1e-12:
        return x
    return x * (pico / maximo)


def saturar(x: np.ndarray, cantidad: float = 1.5) -> np.ndarray:
    """Saturacion suave. Redondea los picos en vez de recortarlos."""
    return np.tanh(x * cantidad) / np.tanh(cantidad)


def envolvente(n: int, ataque: float, caida: float, curva: float = 2.0) -> np.ndarray:
    """Ataque suave y caida exponencial, en segundos."""
    t = np.arange(n) / SR
    a = muestras(ataque)
    env = np.ones(n)
    if a > 0:
        subida = np.linspace(0.0, 1.0, min(a, n))
        env[:len(subida)] = subida ** 1.4
    env *= np.exp(-curva * t / max(caida, 1e-6))
    return env


def rampa(x: np.ndarray, entrada: float = 0.01, salida: float = 0.02) -> np.ndarray:
    """Evita el chasquido de arranque y de corte. Nunca falta."""
    y = x.copy()
    a = min(muestras(entrada), len(y) // 2)
    b = min(muestras(salida), len(y) // 2)
    if a > 0:
        y[:a] *= np.linspace(0.0, 1.0, a) ** 0.5
    if b > 0:
        y[-b:] *= np.linspace(1.0, 0.0, b) ** 0.5
    return y


def deriva(segundos: float, velocidad: float, profundidad: float) -> np.ndarray:
    """Modulacion lenta e irregular. Lo que impide que un bucle suene a bucle.

    Son tres senos de periodos primos entre si: nunca vuelven a coincidir en
    la duracion de la pieza, asi que la modulacion no se repite.
    """
    t = tiempo(segundos)
    m = (np.sin(2 * np.pi * velocidad * t)
         + 0.6 * np.sin(2 * np.pi * velocidad * 0.37 * t + 1.1)
         + 0.4 * np.sin(2 * np.pi * velocidad * 0.13 * t + 2.7))
    return 1.0 + profundidad * m / 2.0


# ==========================================================================
# Reverberacion: cada nivel tiene su sala
# ==========================================================================

def impulso(t60: float, predelay: float = 0.01, brillo: float = 4_000.0,
            densidad: float = 1.0) -> np.ndarray:
    """Respuesta impulsiva sintetica.

    Ruido con caida exponencial, filtrado para que los agudos se apaguen antes
    que los graves (que es lo que hace una sala de verdad), con unas primeras
    reflexiones sueltas al principio para dar tamano.
    """
    n = muestras(t60)
    t = np.arange(n) / SR
    cuerpo = RNG.normal(0.0, 1.0, n) * np.exp(-6.0 * t / t60)

    # Los agudos mueren primero: se separan las bandas y se les da caida propia.
    agudos = pasaaltos(cuerpo, 2_000.0, 2) * np.exp(-4.0 * t / t60)
    medios = pasabanda(cuerpo, 300.0, 2_000.0, 2)
    graves = pasabajos(cuerpo, 300.0, 2) * np.exp(1.2 * t / t60 - 1.2)
    ir = 0.35 * agudos + medios + 0.8 * graves
    ir = pasabajos(ir, brillo, 2)

    # Primeras reflexiones: las paredes cercanas.
    reflejos = np.zeros(n)
    cantidad = int(10 * densidad)
    for _ in range(cantidad):
        pos = int(RNG.uniform(0.004, 0.09) * SR)
        if pos < n:
            reflejos[pos] += RNG.uniform(-1.0, 1.0) * 0.5
    ir = ir + reflejos

    ir[0] += 1.0
    ir = normalizar(ir, 1.0)
    hueco = muestras(predelay)
    return np.concatenate([np.zeros(hueco), ir])


def reverberar(x: np.ndarray, ir: np.ndarray, mezcla: float = 0.35) -> np.ndarray:
    humedo = signal.fftconvolve(x, ir)[:len(x)]
    humedo = normalizar(humedo, float(np.max(np.abs(x))) or 1.0)
    return (1.0 - mezcla) * x + mezcla * humedo


# Salas del mod. Se calculan una sola vez porque son caras.
SALAS = {
    # Oficina con cielorraso de placas: se come todo, casi no devuelve nada.
    "oficina": impulso(0.55, 0.006, 2_600.0, 0.7),
    # Deposito de hormigon: grande, con cola larga y oscura.
    "deposito": impulso(2.60, 0.028, 3_200.0, 1.4),
    # Pasillo de servicio: estrecho, metalico, reflexiones muy cercanas.
    "servicio": impulso(1.10, 0.004, 5_000.0, 1.8),
    # Complejo de piscinas: azulejo, enorme, brillante y con cola larguisima.
    "piscinas": impulso(4.20, 0.035, 7_500.0, 1.6),
}


# ==========================================================================
# Bucles sin costura
# ==========================================================================

def bucle_suave(x: np.ndarray, cruce: float = 3.0) -> np.ndarray:
    """Empalma el final con el principio para que el bucle no tenga junta.

    La cola se funde encima de la cabeza con una curva de potencia constante
    (raiz del coseno), no lineal: una mezcla lineal de dos ruidos hace un bache
    de volumen en el medio, y ese bache es exactamente lo que delata el bucle.
    """
    c = min(muestras(cruce), len(x) // 3)
    cabeza = x[:c]
    cola = x[-c:]
    f = np.linspace(0.0, np.pi / 2, c)
    mezclado = cola * np.cos(f) + cabeza * np.sin(f)
    return np.concatenate([mezclado, x[c:len(x) - c]])


def escribir(nombre: str, datos: np.ndarray, pico: float = 0.85) -> None:
    """Guarda la pieza como OGG Vorbis mono.

    La escritura va por bloques y no de un saque: el codificador Vorbis de
    libsndfile se cae con segmentation fault cuando se le pasa un buffer de
    mas de un minuto en una sola llamada, y el tema del menu dura mas que eso.
    """
    ruta = DESTINO / f"{nombre}.ogg"
    ruta.parent.mkdir(parents=True, exist_ok=True)
    datos = normalizar(np.asarray(datos, dtype=np.float64), pico)
    bloque = SR * 4
    with sf.SoundFile(ruta, "w", samplerate=SR, channels=1,
                      format="OGG", subtype="VORBIS") as archivo:
        for inicio in range(0, len(datos), bloque):
            archivo.write(datos[inicio:inicio + bloque])
    kb = ruta.stat().st_size / 1024.0
    print(f"  {nombre:<26} {len(datos)/SR:6.2f} s  {kb:7.1f} kB")


# ==========================================================================
# 1. Interfaz
# ==========================================================================
# CUARTA GENERACION. Van tres descartadas y conviene dejar escrito por que,
# porque cada error es facil de repetir.
#
#   1a. Clics comunes. Sonaban a interfaz de computadora.
#   2a. Clics mejores: sellos, interruptores, ruedas dentadas. El problema no
#       era la calidad de cada pieza sino la categoria: un clic es un objeto
#       que se manipula, y aca no hay ningun objeto que manipular.
#   3a. Sin transitorio, todo construido con _red(): sumas de senoidales a
#       multiplos de 50 Hz. El concepto era correcto -que suene el edificio y
#       no la interfaz- pero la ejecucion lo arruinaba. Una pila de senoidales
#       es lo que el oido reconoce como "sintetizador", y ocho gestos hechos
#       con el mismo apilado suenan a ocho largos distintos del mismo zumbido.
#
# QUE HACE ESTA GENERACION DISTINTO
#
# Un objeto real no suena con senoidales armonicas: suena con MODOS. Al
# excitarlo, unas cuantas resonancias inarmonicas arrancan juntas y cada una
# se apaga a su propio ritmo -las agudas primero, las graves despues-. Eso es
# lo que hace que el oido diga "chapa", "hormigon", "vidrio" en vez de "tono".
# Aca los gestos se sintetizan filtrando ruido con resonadores afinados en
# proporciones inarmonicas, que es sintesis modal: no hay un solo oscilador
# senoidal en toda la familia.
#
# Y sobre todo: los ocho gestos NO son el mismo material. Un menu necesita
# que el oido distinga confirmar de volver sin pensarlo, y eso no se consigue
# con duraciones distintas del mismo timbre. Cada gesto tiene su cuerpo:
#
#   pasar      aire desplazado, sin cuerpo             (nada resuena)
#   elegir     madera del tablon, seca                 (modos amortiguados)
#   alternar   ceramica del azulejo, corta             (modos altos, breves)
#   confirmar  hormigon, grave, con vacio detras       (modos bajos, largos)
#   volver     el mismo hormigon una quinta abajo      (pariente de confirmar)
#   abrir      el recinto entero llenandose de aire    (sin modos, puro ruido)
#   cerrar     lo de arriba al reves                   (sin modos)
#   negado     dos golpes sordos sobre algo que no cede
#
# Reglas duras que se mantienen de la generacion anterior porque eran buenas:
#   - Sin transitorio duro. Ningun ataque por debajo de 6 ms.
#   - Techo en 5 kHz. Todo lo que pasa de ahi suena a plastico.
#   - Todo por la misma sala, para que pertenezcan al mismo sitio.
#   - Nada dura menos de 90 ms ni mas de 700.
#
# Regla nueva: los modos se afinan sobre la escala del tema del menu (la
# menor). No como melodia -no hay melodia- sino para que cuando un gesto suene
# encima de la musica no choque nunca. Es lo que cose el audio en un sistema.

# La sala de la interfaz es la del recinto donde esta clavada la hoja: chica,
# seca y con un poco de cola. No es una sala de efectos: es el sitio.
UI = impulso(0.40, 0.005, 2_800.0, 0.9)

# La red electrica. Sigue estando, pero ya no construye los gestos: solo los
# apoya, muy por debajo, para que esten enchufados al mismo lugar que los
# ambientes. Es cimiento, no material.
RED = 50.0


def _red(dur: float, armonicos, decaimiento: float) -> np.ndarray:
    """Un pedacito de zumbido de red con caida propia.

    Se conserva para el apoyo grave de algunos gestos y para la transicion.
    Ya no es el material principal de la interfaz.
    """
    n = muestras(dur)
    t = tiempo(dur)
    x = np.zeros(n)
    for multiplo, nivel in armonicos:
        fase = RNG.uniform(0, 2 * np.pi)
        x += nivel * np.sin(2 * np.pi * RED * multiplo * t + fase)
    return x * np.exp(-t / decaimiento)


def modal(dur: float, modos, excitacion: float = 0.004,
          color: float = 3_000.0) -> np.ndarray:
    """Sintesis modal: un objeto golpeado.

    Se excita un ruido muy corto -el golpe- y se lo hace pasar por un banco de
    resonadores muy selectivos. Cada modo es (frecuencia, peso, decaimiento en
    segundos). Como cada uno se apaga a su ritmo, el timbre cambia mientras
    suena, que es exactamente lo que hace un objeto de verdad y lo que ninguna
    suma de senoidales consigue.

    Las frecuencias no son multiplos enteros a proposito: los objetos reales
    son inarmonicos, y esa inarmonicidad es la que da la sensacion de material.
    """
    n = muestras(dur)
    t = np.arange(n) / SR

    # El golpe: ruido cortisimo, filtrado para que no meta agudo de mas.
    golpe = RNG.normal(0.0, 1.0, n)
    golpe *= np.exp(-t / max(excitacion, 1e-4))
    golpe = pasabajos(golpe, color, 2)

    salida = np.zeros(n)
    for frecuencia, peso, caida in modos:
        if frecuencia >= SR / 2.0 * 0.95:
            continue
        # Resonador muy estrecho: el Q sale del decaimiento pedido.
        q = max(2.0, np.pi * frecuencia * caida)
        b, a = signal.iirpeak(frecuencia / (SR / 2.0), q)
        voz = signal.lfilter(b, a, golpe)
        salida += peso * voz * np.exp(-t / caida)

    return salida


def _apoyo(dur: float, corte: float, caida: float, fuerza: float) -> np.ndarray:
    """El grave que sostiene un gesto. Sin el, todo suena a maqueta."""
    n = muestras(dur)
    t = np.arange(n) / SR
    x = pasabajos(marron(dur), corte, 3) * 3.0
    return x * np.exp(-t / caida) * fuerza


def ui_pasar() -> np.ndarray:
    """Pasar por encima de un renglon.

    Es el sonido que mas veces se va a oir en la sesion, asi que casi no es un
    sonido: es aire desplazado, como cuando la mano pasa cerca de una hoja sin
    llegar a tocarla. No resuena nada porque no se toca nada.

    Si al escucharlo suelto parece que falta algo, esta bien. En su sitio, con
    el ambiente debajo, se percibe como que la hoja se entero.
    """
    dur = 0.15
    n = muestras(dur)
    aire = pasabanda(rosa(dur), 260.0, 1_800.0, 2)
    aire *= envolvente(n, 0.028, 0.080, 1.7)
    # Una sola resonancia bajisima: el tablon acusando el paso, casi inaudible.
    roce = modal(dur, ((196.0, 1.0, 0.045),), 0.006, 1_400.0) * 0.10
    x = aire * 0.60 + roce
    return rampa(reverberar(x, UI, 0.18), 0.012, 0.045)


def ui_elegir() -> np.ndarray:
    """Queda marcado un renglon.

    El tablon de madera acusando que algo se apoyo encima. Modos secos y muy
    amortiguados: la madera no canta, responde y se calla. Es el gesto que
    tiene que leerse como "esto quedo seleccionado" sin pedir atencion.

    La afinacion base es un la grave, la misma nota que sostiene el tema.
    """
    dur = 0.26
    # Madera: inarmonica y de decaimiento corto. Los agudos mueren primero.
    modos = (
        (110.0, 1.00, 0.115),
        (178.0, 0.62, 0.080),
        (293.0, 0.40, 0.052),
        (447.0, 0.22, 0.034),
        (694.0, 0.11, 0.020),
    )
    cuerpo = modal(dur, modos, 0.0035, 2_400.0)
    cuerpo = normalizar(cuerpo, 0.9)
    x = cuerpo * 0.82 + _apoyo(dur, 190.0, 0.070, 0.30)
    return rampa(reverberar(saturar(x, 1.10), UI, 0.24), 0.007, 0.055)


def ui_confirmar() -> np.ndarray:
    """Se acepta y se cambia de pantalla.

    El unico gesto con consecuencia, y el unico que tiene peso. Es un golpe
    sobre hormigon: modos graves, largos, que llenan el recinto y despues lo
    dejan vacio. La confirmacion no esta en el golpe sino en el silencio que
    deja detras.
    """
    dur = 0.60
    n = muestras(dur)
    t = tiempo(dur)

    # Hormigon: pocas resonancias, muy graves, colas largas.
    modos = (
        (55.0, 1.00, 0.340),
        (82.5, 0.70, 0.260),
        (131.0, 0.45, 0.185),
        (207.0, 0.24, 0.110),
        (330.0, 0.12, 0.062),
    )
    cuerpo = modal(dur, modos, 0.0055, 1_800.0)
    cuerpo = normalizar(cuerpo, 0.95)

    # El aire que se mueve detras del golpe: es lo que da tamano al recinto.
    conducto = pasabajos(marron(dur), 300.0, 3) * 3.0
    conducto *= envolvente(n, 0.045, 0.250, 1.4) * 0.45

    # La red apenas asoma medio segundo, como si la instalacion lo acusara.
    apunte = _red(dur, ((1, 0.5), (2, 0.30)), 0.14) * 0.10
    apunte *= np.clip(t / 0.05, 0.0, 1.0)

    x = saturar(cuerpo * 0.80 + conducto + apunte, 1.20)
    return rampa(reverberar(x, UI, 0.34), 0.009, 0.130)


def ui_volver() -> np.ndarray:
    """Volver atras.

    El mismo hormigon que confirmar pero una quinta abajo y sin la cola larga:
    el golpe se da y se acaba. Que sean parientes evidentes es deliberado; ir y
    volver tienen que sonar al mismo par de manos sobre la misma pared.
    """
    dur = 0.40
    modos = (
        (36.7, 1.00, 0.190),
        (55.0, 0.66, 0.150),
        (87.3, 0.40, 0.105),
        (138.0, 0.20, 0.066),
        (220.0, 0.09, 0.038),
    )
    cuerpo = modal(dur, modos, 0.0060, 1_500.0)
    cuerpo = normalizar(cuerpo, 0.92)
    x = saturar(cuerpo * 0.80 + _apoyo(dur, 240.0, 0.105, 0.34), 1.12)
    return rampa(reverberar(x, UI, 0.28), 0.011, 0.100)


def ui_alternar() -> np.ndarray:
    """Cambiar el valor de una opcion.

    El gesto mas chico que existe. Ceramica: la una contra un azulejo. Modos
    altos y cortisimos, sin cuerpo grave, para que se distinga al instante de
    elegir y de confirmar sin ser mas fuerte que ellos.

    Es el unico gesto de la familia con material del natatorio, y esta bien:
    es el que se repite cuando el jugador toca opciones, y conviene que sea
    liviano.
    """
    dur = 0.17
    modos = (
        (880.0, 1.00, 0.038),
        (1_318.0, 0.58, 0.026),
        (1_760.0, 0.30, 0.018),
        (2_640.0, 0.14, 0.011),
    )
    cuerpo = modal(dur, modos, 0.0018, 4_200.0)
    cuerpo = normalizar(cuerpo, 0.75)
    # Un apoyo grave minimo: sin el, la ceramica sola suena a campanita.
    x = cuerpo * 0.55 + _apoyo(dur, 260.0, 0.030, 0.26)
    return rampa(reverberar(x, UI, 0.22), 0.006, 0.050)


def ui_abrir() -> np.ndarray:
    """Se abre el menu.

    No hay objeto: hay un recinto que se llena de aire. Una instalacion
    arrancando, el conducto moviendose, la red enganchando. Es el gesto mas
    largo de la familia porque es el que abre el lugar.
    """
    dur = 0.70
    t = tiempo(dur)
    arranque = pasabajos(marron(dur), 380.0, 3) * 3.4
    arranque *= np.clip(t / 0.34, 0.0, 1.0) * np.exp(-np.clip(t - 0.34, 0.0, None) / 0.26)
    # El recinto respondiendo: dos modos muy graves y largos, apenas audibles.
    recinto = modal(dur, ((55.0, 1.0, 0.42), (82.5, 0.5, 0.30)), 0.010, 1_200.0)
    recinto = normalizar(recinto, 0.55) * np.clip(t / 0.20, 0.0, 1.0)
    engancha = _red(dur, ((1, 0.7), (2, 0.8)), 0.34) * 0.30
    engancha *= np.clip(t / 0.22, 0.0, 1.0)
    x = saturar(arranque * 0.52 + recinto * 0.45 + engancha, 1.15)
    return rampa(reverberar(x, UI, 0.36), 0.030, 0.170)


def ui_cerrar() -> np.ndarray:
    """Se cierra el menu. Lo de arriba, al reves y mas rapido."""
    dur = 0.50
    t = tiempo(dur)
    frena = pasabajos(marron(dur), 320.0, 3) * 3.0 * np.exp(-t / 0.15)
    # El recinto vaciandose: el mismo par de modos, apagandose.
    recinto = modal(dur, ((55.0, 1.0, 0.20), (82.5, 0.45, 0.14)), 0.010, 1_100.0)
    recinto = normalizar(recinto, 0.50)
    # La red perdiendo alimentacion: cae de tono mientras se apaga.
    suelta = np.sin(2 * np.pi * RED * 2.0 * (1.0 - 0.10 * np.clip(t / 0.30, 0, 1)) * t)
    suelta *= np.exp(-t / 0.13) * 0.32
    x = saturar(frena * 0.52 + recinto * 0.42 + suelta, 1.15)
    return rampa(reverberar(x, UI, 0.32), 0.014, 0.140)


def ui_negado() -> np.ndarray:
    """Accion invalida.

    Sin pitido, sin nota triste, sin nada que parezca un error de sistema
    operativo. Dos golpes sordos sobre algo que no cede: el segundo mas flojo
    que el primero, y ninguno de los dos resuena. La informacion esta en que no
    pasa nada. El material esta muerto a proposito -decaimientos cortisimos-
    porque es la unica forma de que "no" se lea como "no".
    """
    dur = 0.34
    n = muestras(dur)
    x = np.zeros(n)
    modos = (
        (98.0, 1.00, 0.038),
        (147.0, 0.55, 0.026),
        (233.0, 0.26, 0.017),
    )
    for retardo, fuerza in ((0.0, 1.0), (0.078, 0.52)):
        d = muestras(retardo)
        largo = n - d
        if largo <= 0:
            continue
        golpe = modal(largo / SR, modos, 0.0045, 1_300.0)
        golpe = normalizar(golpe, 0.85)
        tc = np.arange(largo) / SR
        sordo = pasabajos(RNG.normal(0, 1, largo), 620.0, 2) * np.exp(-tc / 0.020) * 0.30
        x[d:d + largo] += (golpe * 0.62 + sordo) * fuerza
    return rampa(reverberar(saturar(x, 1.25), UI, 0.26), 0.009, 0.075)


# ==========================================================================
# 2. Ambientes: un lugar por nivel
# ==========================================================================
# Cada base es un bucle largo (20-30 s) hecho de capas que se mueven a
# velocidades distintas. Lo que decide si un ambiente "es" un lugar no son los
# efectos sino el room tone: la mezcla de graves y el tiempo de reverberacion.

def base_nivel0(dur: float = 24.0) -> np.ndarray:
    """Nivel 0, seccion administrativa.

    Una oficina sin ventanas: el balasto del fluorescente a 100 Hz (el doble
    de la red), el conducto de aire acondicionado como una capa de graves
    filtrados, y nada mas. Es el ambiente mas seco y mas chico de los cuatro,
    a proposito: el techo esta encima de la cabeza.
    """
    t = tiempo(dur)

    # Balasto: red de 50 Hz y sus armonicos pares. El 100 manda.
    balasto = np.zeros(len(t))
    for frec, nivel in ((50.0, 0.22), (100.0, 1.0), (150.0, 0.30), (200.0, 0.42), (300.0, 0.12)):
        fase = RNG.uniform(0, 2 * np.pi)
        balasto += nivel * np.sin(2 * np.pi * frec * t + fase)
    balasto *= deriva(dur, 0.07, 0.10)

    # El conducto de aire: ruido marron muy filtrado, se mueve solo.
    conducto = pasabajos(marron(dur), 220.0, 3) * 4.0 * deriva(dur, 0.031, 0.22)

    # Siseo del tubo, apenas. Filtrado alto pero con techo en 6 kHz.
    siseo = pasabanda(rosa(dur), 1_800.0, 6_000.0, 2) * 0.30 * deriva(dur, 0.13, 0.35)

    x = balasto * 0.16 + conducto * 0.10 + siseo * 0.05
    return reverberar(x, SALAS["oficina"], 0.20)


def base_nivel1(dur: float = 28.0) -> np.ndarray:
    """Nivel 1, deposito.

    Hormigon y demasiado volumen de aire. Casi no hay fuente sonora: lo que se
    escucha es el tamano del lugar. Graves profundos, una resonancia modal muy
    baja (la nota propia de la sala) y una cola de reverberacion larga.
    """
    t = tiempo(dur)

    aire = pasabajos(marron(dur), 130.0, 3) * 5.0 * deriva(dur, 0.019, 0.30)

    # Modo propio de la sala: un ambiente grande siempre tiene una nota.
    modo = np.sin(2 * np.pi * 41.0 * t) * 0.10 * deriva(dur, 0.011, 0.55)
    modo += np.sin(2 * np.pi * 62.0 * t + 0.8) * 0.05 * deriva(dur, 0.017, 0.60)

    # Ventilacion lejanisima, ya casi solo medios apagados.
    lejos = pasabanda(rosa(dur), 300.0, 1_400.0, 2) * 0.22 * deriva(dur, 0.043, 0.40)

    x = aire * 0.11 + modo * 0.9 + lejos * 0.05
    return reverberar(x, SALAS["deposito"], 0.42)


def base_nivel2(dur: float = 22.0) -> np.ndarray:
    """Nivel 2, pasillos de servicio.

    Estrecho y caliente. Aca si hay maquinas: agua corriendo dentro de las
    canerias, vapor escapandose en algun lado y el retumbe de una caldera que
    no se ve. Las resonancias son de tubo, agudas y con Q alto.
    """
    t = tiempo(dur)

    caldera = pasabajos(marron(dur), 90.0, 3) * 6.0 * deriva(dur, 0.023, 0.35)

    # Agua dentro del cano: ruido de banda estrecha con resonancias afinadas.
    agua = pasabanda(rosa(dur), 260.0, 1_100.0, 2)
    agua = resonar(agua, 320.0, 26.0, 0.8)
    agua = resonar(agua, 487.0, 30.0, 0.6)
    agua *= 0.5 * deriva(dur, 0.05, 0.45)

    # Vapor: constante, pero con techo bajo para que no raspe.
    vapor = pasabanda(rosa(dur), 2_200.0, 5_500.0, 2) * 0.28 * deriva(dur, 0.09, 0.50)

    x = caldera * 0.10 + agua * 0.07 + vapor * 0.045
    return reverberar(x, SALAS["servicio"], 0.30)


def base_nivel3(dur: float = 30.0) -> np.ndarray:
    """Nivel 3, las piscinas. El ambiente mas trabajado de los cuatro.

    Tres cosas a la vez, como en un natatorio cerrado de verdad:

      1. La climatizacion, que nunca para y es la capa que sostiene todo.
      2. El agua: no un bucle de chapoteo, sino movimiento muy lento y grave,
         el vaso entero desplazandose apenas.
      3. El azulejo, que devuelve todo con una cola larguisima y brillante.

    Lo que vuelve creible una pileta cubierta no es el sonido del agua: es que
    todo lo demas suene reverberado sobre superficie dura.
    """
    t = tiempo(dur)

    # Climatizacion: la capa base, ancha y estable.
    clima = pasabajos(marron(dur), 170.0, 3) * 4.5 * deriva(dur, 0.017, 0.20)
    clima += pasabanda(rosa(dur), 200.0, 900.0, 2) * 0.45 * deriva(dur, 0.037, 0.30)

    # Rejilla de ventilacion: banda media-alta, la unica cosa "cercana".
    rejilla = pasabanda(rosa(dur), 1_400.0, 4_200.0, 2) * 0.26 * deriva(dur, 0.11, 0.45)

    # Movimiento de agua: ruido muy filtrado modulado por olas lentas, mas
    # unas ondulaciones sueltas que rompen la periodicidad.
    ola = (np.sin(2 * np.pi * 0.11 * t) * 0.5 + np.sin(2 * np.pi * 0.07 * t + 1.9) * 0.3 + 0.6)
    agua = pasabanda(rosa(dur), 120.0, 700.0, 2) * np.clip(ola, 0.0, None) * 0.9
    for _ in range(14):
        pos = muestras(RNG.uniform(0.5, dur - 1.5))
        largo = muestras(RNG.uniform(0.35, 1.1))
        if pos + largo > len(t):
            continue
        tc = np.arange(largo) / SR
        onda = pasabanda(RNG.normal(0, 1, largo), 180.0, 1_300.0, 2)
        onda *= np.sin(np.pi * tc / (largo / SR)) ** 2 * RNG.uniform(0.25, 0.7)
        agua[pos:pos + largo] += onda

    # Zumbido electrico del cuarto de maquinas, lejos.
    electrico = np.sin(2 * np.pi * 100.0 * t) * 0.05 * deriva(dur, 0.029, 0.40)

    x = clima * 0.10 + rejilla * 0.05 + agua * 0.075 + electrico
    return reverberar(x, SALAS["piscinas"], 0.52)


# ==========================================================================
# 2b. Capa de caracter: la segunda cama, tambien continua
# ==========================================================================
# El pedido fue explicito: tiene que haber sonido de fondo A TODA HORA -aire,
# agua- y no solo eventos espaciados. La base de arriba ya es continua, pero
# una sola cama tiene un problema: por muy bien empalmado que este el bucle,
# a los tres o cuatro pases el oido aprende el archivo y lo empieza a
# reconocer. No se oye la junta, se oye el patron.
#
# La solucion no es alargar el archivo, es poner DOS camas de duracion prima
# entre si. Una de 24 s y otra de 37 s vuelven a alinearse cada quince minutos;
# una de 28 y otra de 41, cada diecinueve. Durante toda la sesion el oyente
# escucha combinaciones que no se repiten, con dos archivos chicos.
#
# La division de material no es arbitraria: en la BASE va lo estable -la nota
# del sitio, el zumbido, el volumen de aire- y aca va lo que se mueve -el aire
# corriendo, el agua desplazandose, el goteo lejano-. Por eso esta capa es la
# que se oye "viva" y la que sostiene la sensacion de que el lugar sigue
# funcionando aunque no pase nada.

def caracter_nivel0(dur: float = 37.0) -> np.ndarray:
    """Nivel 0. El aire acondicionado de la seccion administrativa.

    Nunca se apaga y nunca esta del todo estable: el motor tiene una batida
    lenta, de esas que solo se notan cuando uno lleva un rato en la oficina.
    """
    t = tiempo(dur)

    # El conducto. Es lo unico que hay, y por eso tiene que respirar.
    conducto = pasabajos(marron(dur), 300.0, 3) * 4.2
    conducto *= 0.75 + 0.25 * deriva(dur, 0.023, 1.0)

    # La batida del motor: dos frecuencias muy cercanas que van y vienen.
    batida = (np.sin(2 * np.pi * 24.0 * t) + np.sin(2 * np.pi * 24.7 * t)) * 0.5
    batida *= 0.06

    # La reja de retorno, banda media, filtrada por un barrido lentisimo.
    reja = pasabanda(rosa(dur), 700.0, 2_600.0, 2) * 0.20 * deriva(dur, 0.047, 0.55)

    x = conducto * 0.085 + batida + reja * 0.045
    return reverberar(x, SALAS["oficina"], 0.24)


def caracter_nivel1(dur: float = 41.0) -> np.ndarray:
    """Nivel 1. La nave respirando.

    Un galpon de este tamano tiene corrientes de aire propias, y la chapa del
    techo se dilata y se contrae todo el dia. No hay maquinas: hay estructura.
    """
    t = tiempo(dur)

    # Corriente de aire cruzando la nave, muy grave y muy lenta.
    corriente = pasabajos(marron(dur), 90.0, 3) * 5.5
    corriente *= 0.6 + 0.4 * deriva(dur, 0.013, 1.0)

    # Silbido de la corriente colandose por algun hueco alto. Va y viene.
    silbido = pasabanda(rosa(dur), 900.0, 2_200.0, 2)
    ventana = np.clip(np.sin(2 * np.pi * 0.021 * t - 0.7), 0.0, None) ** 2
    silbido *= ventana * 0.30

    # La chapa: crujidos de dilatacion, muy separados y muy suaves. Estan aca y
    # no en los eventos porque no son sucesos, son el estado del edificio.
    chapa = np.zeros(len(t))
    for _ in range(7):
        pos = muestras(RNG.uniform(1.0, dur - 3.0))
        largo = muestras(RNG.uniform(0.6, 1.8))
        if pos + largo > len(t):
            continue
        tc = np.arange(largo) / SR
        cru = pasabanda(RNG.normal(0, 1, largo), 140.0, 800.0, 2)
        cru *= np.sin(np.pi * tc / (largo / SR)) ** 3 * RNG.uniform(0.10, 0.28)
        chapa[pos:pos + largo] += cru

    x = corriente * 0.075 + silbido * 0.035 + chapa * 0.5
    return reverberar(x, SALAS["deposito"], 0.50)


def caracter_nivel2(dur: float = 29.0) -> np.ndarray:
    """Nivel 2. El haz de canerias trabajando.

    El unico nivel donde hay maquinaria de verdad del otro lado de la pared.
    Circulacion de agua caliente dentro del cano, la bomba lejos, y el cano
    mismo transmitiendo todo por contacto.
    """
    t = tiempo(dur)

    # Agua circulando: ruido de banda estrecha resonado en el diametro del cano.
    flujo = pasabanda(rosa(dur), 400.0, 2_000.0, 2) * 0.55
    flujo = resonar(flujo, 620.0, 12.0, 1.6)
    flujo = resonar(flujo, 940.0, 16.0, 1.2)
    flujo *= 0.7 + 0.3 * deriva(dur, 0.061, 1.0)

    # La bomba, dos paredes mas alla: pulso lento y sordo.
    pulso = np.clip(np.sin(2 * np.pi * 1.45 * t), 0.0, None) ** 4
    bomba = pasabajos(marron(dur), 150.0, 3) * 3.0 * (0.55 + 0.45 * pulso)

    # Goteo continuo, sin ser un evento: un ritmo que casi es un ritmo.
    goteo = np.zeros(len(t))
    paso = 2.7
    pos = 0.6
    while pos < dur - 0.4:
        i = muestras(pos)
        largo = muestras(0.16)
        if i + largo < len(t):
            tc = np.arange(largo) / SR
            g = np.sin(2 * np.pi * RNG.uniform(1_500, 2_400) * tc) * np.exp(-tc / 0.012)
            g += pasabajos(RNG.normal(0, 1, largo), 900.0, 2) * np.exp(-tc / 0.020) * 0.5
            goteo[i:i + largo] += g * RNG.uniform(0.20, 0.42)
        pos += paso * RNG.uniform(0.75, 1.35)

    x = flujo * 0.055 + bomba * 0.055 + goteo * 0.22
    return reverberar(x, SALAS["servicio"], 0.40)


def caracter_nivel3(dur: float = 43.0) -> np.ndarray:
    """Nivel 3. El agua del natatorio, siempre.

    Es la capa mas importante de las ocho: es la que el pedido nombraba por su
    nombre. Un complejo de piletas vacio no suena a chapoteo, suena a masa de
    agua quieta en un recinto enorme de azulejo, con el rebalse corriendo por
    la canaleta perimetral y la climatizacion que no para nunca.
    """
    t = tiempo(dur)

    # La canaleta de rebalse: agua corriendo constante, filtrada y lejana. Es
    # el hilo que no se corta jamas, y es lo que hace que el sitio este vivo.
    canaleta = pasabanda(rosa(dur), 700.0, 3_400.0, 2) * 0.42
    canaleta *= 0.72 + 0.28 * deriva(dur, 0.053, 1.0)

    # La masa de agua: olas lentisimas contra el borde del vaso, casi infrasonido.
    masa = pasabajos(marron(dur), 110.0, 3) * 4.6
    ola = 0.62 + 0.38 * (np.sin(2 * np.pi * 0.055 * t) * 0.6
                         + np.sin(2 * np.pi * 0.037 * t + 2.1) * 0.4)
    masa *= ola

    # Lengüetazos del agua en la canaleta, sueltos, sin periodo reconocible.
    lame = np.zeros(len(t))
    for _ in range(22):
        pos = muestras(RNG.uniform(0.4, dur - 1.2))
        largo = muestras(RNG.uniform(0.20, 0.75))
        if pos + largo > len(t):
            continue
        tc = np.arange(largo) / SR
        l = pasabanda(RNG.normal(0, 1, largo), 350.0, 2_600.0, 2)
        l *= np.sin(np.pi * tc / (largo / SR)) ** 2 * RNG.uniform(0.14, 0.40)
        lame[pos:pos + largo] += l

    # Climatizacion del recinto, la unica capa completamente estable.
    clima = pasabajos(marron(dur), 240.0, 3) * 3.6 * (0.85 + 0.15 * deriva(dur, 0.019, 1.0))

    x = canaleta * 0.050 + masa * 0.070 + lame * 0.30 + clima * 0.045
    return reverberar(x, SALAS["piscinas"], 0.56)


# ==========================================================================
# 3. Eventos ambientales
# ==========================================================================
# Los eventos son lo que impide que un ambiente se vuelva mobiliario. Cada
# nivel tiene tres o cuatro, y el programador los dispara con separacion
# aleatoria y volumen variable, asi que la combinacion nunca se repite igual.

def ev_nivel0_tubo() -> np.ndarray:
    """Un tubo que sube de intensidad un segundo y vuelve. Pasa todo el tiempo."""
    dur = 2.6
    t = tiempo(dur)
    sobre = np.exp(-((t - 1.0) ** 2) / 0.22)
    zumbido = (np.sin(2 * np.pi * 100.0 * t) + 0.5 * np.sin(2 * np.pi * 200.0 * t)
               + 0.25 * np.sin(2 * np.pi * 300.0 * t))
    x = zumbido * sobre * 0.30
    x += pasabanda(rosa(dur), 1_500.0, 5_000.0, 2) * sobre * 0.10
    return rampa(reverberar(x, SALAS["oficina"], 0.25), 0.05, 0.30)


def ev_nivel0_placa() -> np.ndarray:
    """Una placa del cielorraso acomodandose. Seco, arriba de la cabeza."""
    dur = 1.1
    n = muestras(dur)
    t = tiempo(dur)
    cruje = pasabanda(RNG.normal(0, 1, n), 400.0, 2_400.0, 2)
    perfil = np.zeros(n)
    for _ in range(5):
        pos = int(RNG.uniform(0.0, 0.35) * SR)
        if pos < n:
            perfil[pos:] += np.exp(-np.arange(n - pos) / SR / 0.03) * RNG.uniform(0.3, 1.0)
    x = cruje * perfil * 0.5
    return rampa(reverberar(x, SALAS["oficina"], 0.35), 0.005, 0.20)


def ev_nivel0_puerta() -> np.ndarray:
    """Una puerta cerrandose lejos, en otro pasillo. Nunca cerca."""
    dur = 1.8
    n = muestras(dur)
    t = tiempo(dur)
    golpe = pasabajos(RNG.normal(0, 1, n), 420.0, 2) * envolvente(n, 0.002, 0.10, 4.0)
    cuerpo = np.sin(2 * np.pi * 58.0 * t) * envolvente(n, 0.004, 0.16, 3.0) * 0.5
    x = (golpe + cuerpo) * 0.35
    x = pasabajos(x, 1_200.0, 2)  # la distancia se come los agudos
    return rampa(reverberar(x, SALAS["oficina"], 0.55), 0.01, 0.40)


def ev_nivel1_metal() -> np.ndarray:
    """Algo metalico cayendo al fondo del deposito. La cola dice el tamano."""
    dur = 4.5
    n = muestras(dur)
    t = tiempo(dur)
    impacto = pasabanda(RNG.normal(0, 1, n), 250.0, 3_500.0, 2) * envolvente(n, 0.001, 0.05, 6.0)
    for frec in (183.0, 291.0, 447.0, 638.0):
        impacto += np.sin(2 * np.pi * frec * t) * np.exp(-t / RNG.uniform(0.15, 0.45)) * 0.18
    x = pasabajos(impacto, 4_000.0, 2) * 0.35
    return rampa(reverberar(x, SALAS["deposito"], 0.72), 0.002, 0.80)


def ev_nivel1_estructura() -> np.ndarray:
    """La estructura asentandose. Muy grave, muy lento, casi infrasonido."""
    dur = 5.0
    t = tiempo(dur)
    sobre = np.exp(-((t - 1.8) ** 2) / 1.4)
    grave = np.sin(2 * np.pi * 33.0 * t + 3.0 * np.sin(2 * np.pi * 0.3 * t))
    grave += 0.5 * np.sin(2 * np.pi * 49.0 * t)
    tension = pasabanda(rosa(dur), 90.0, 500.0, 2) * 0.6
    x = (grave * 0.5 + tension) * sobre * 0.30
    return rampa(reverberar(x, SALAS["deposito"], 0.60), 0.30, 1.00)


def ev_nivel1_lejano() -> np.ndarray:
    """Un golpe unico, muy lejos. No se sabe que fue ni de donde vino."""
    dur = 4.0
    n = muestras(dur)
    golpe = pasabajos(RNG.normal(0, 1, n), 300.0, 2) * envolvente(n, 0.004, 0.09, 5.0)
    x = pasabajos(golpe, 700.0, 2) * 0.28
    return rampa(reverberar(x, SALAS["deposito"], 0.80), 0.01, 0.90)


def ev_nivel2_golpe_cano() -> np.ndarray:
    """Golpe de ariete: la columna de agua frenando de golpe dentro del cano.

    Es el sonido mas caracteristico de una sala de maquinas. Un impacto seco y
    despues el cano entero sonando como un tubo afinado.
    """
    dur = 2.4
    n = muestras(dur)
    t = tiempo(dur)
    impacto = pasabanda(RNG.normal(0, 1, n), 300.0, 4_000.0, 2) * envolvente(n, 0.0008, 0.025, 7.0)
    tubo = np.zeros(n)
    fundamental = RNG.uniform(120.0, 175.0)
    for k, nivel in ((1, 1.0), (2, 0.45), (3, 0.28), (5, 0.12)):
        tubo += nivel * np.sin(2 * np.pi * fundamental * k * t) * np.exp(-t / (0.55 / k))
    x = (impacto * 0.8 + tubo * 0.45) * 0.40
    return rampa(reverberar(x, SALAS["servicio"], 0.45), 0.001, 0.40)


def ev_nivel2_valvula() -> np.ndarray:
    """Una valvula soltando vapor. Sube, se sostiene y la cierran."""
    dur = 3.2
    t = tiempo(dur)
    sobre = np.clip(t / 0.25, 0, 1) * np.clip((dur - 0.6 - t) / 0.5, 0, 1)
    vapor = pasabanda(rosa(dur), 1_600.0, 6_000.0, 2) * sobre
    vapor = resonar(vapor, 2_400.0, 8.0, 0.5)
    x = vapor * 0.22
    return rampa(reverberar(x, SALAS["servicio"], 0.40), 0.10, 0.45)


def ev_nivel2_goteo() -> np.ndarray:
    """Condensacion cayendo sobre metal. Tres gotas, irregulares."""
    dur = 2.8
    n = muestras(dur)
    x = np.zeros(n)
    for _ in range(3):
        pos = muestras(RNG.uniform(0.1, 2.0))
        largo = min(muestras(0.35), n - pos)
        if largo <= 0:
            continue
        tc = np.arange(largo) / SR
        frec = RNG.uniform(900.0, 1_700.0)
        gota = np.sin(2 * np.pi * frec * tc * (1.0 + 2.5 * np.exp(-tc / 0.008)))
        gota *= np.exp(-tc / 0.035) * RNG.uniform(0.4, 1.0)
        x[pos:pos + largo] += gota
    x = pasabajos(x, 6_000.0, 2) * 0.20
    return rampa(reverberar(x, SALAS["servicio"], 0.50), 0.002, 0.40)


def ev_nivel3_gota() -> np.ndarray:
    """Una gota cayendo al agua, en una sala de azulejo enorme.

    La gota dura 40 ms; lo que se escucha durante los cuatro segundos
    siguientes es la sala. Ese desbalance es el efecto.
    """
    dur = 4.5
    n = muestras(dur)
    x = np.zeros(n)
    pos = muestras(0.15)
    largo = muestras(0.30)
    tc = np.arange(largo) / SR
    frec = RNG.uniform(700.0, 1_200.0)
    # El glissando ascendente es lo que hace que se lea como agua y no como palo.
    gota = np.sin(2 * np.pi * frec * tc * (1.0 + 3.0 * np.exp(-tc / 0.010)))
    gota *= np.exp(-tc / 0.030)
    x[pos:pos + largo] = gota
    x = pasabajos(x, 5_000.0, 2) * 0.30
    return rampa(reverberar(x, SALAS["piscinas"], 0.78), 0.002, 1.00)


def ev_nivel3_ondas() -> np.ndarray:
    """El agua moviendose contra el borde. Lento, grave, sin chapoteo."""
    dur = 5.5
    t = tiempo(dur)
    sobre = np.sin(np.pi * np.clip(t / dur, 0, 1)) ** 1.5
    lamido = pasabanda(rosa(dur), 150.0, 1_100.0, 2)
    modulacion = 0.5 + 0.5 * np.sin(2 * np.pi * 0.55 * t + 0.4 * np.sin(2 * np.pi * 0.21 * t))
    x = lamido * modulacion * sobre * 0.32
    return rampa(reverberar(x, SALAS["piscinas"], 0.60), 0.30, 1.20)


def ev_nivel3_ventilacion() -> np.ndarray:
    """El aire acondicionado arrancando un ciclo. Sube y se queda."""
    dur = 5.0
    t = tiempo(dur)
    sobre = np.clip(t / 1.8, 0, 1) * np.clip((dur - t) / 1.5, 0, 1)
    motor = pasabajos(marron(dur), 200.0, 3) * 5.0
    motor += np.sin(2 * np.pi * 47.0 * t) * 0.12
    aspas = pasabanda(rosa(dur), 800.0, 3_000.0, 2) * 0.3
    aspas *= 1.0 + 0.25 * np.sin(2 * np.pi * 11.0 * t)  # la frecuencia de paso de pala
    x = (motor * 0.10 + aspas * 0.06) * sobre
    return rampa(reverberar(x, SALAS["piscinas"], 0.45), 0.40, 1.00)


def ev_nivel3_lejano() -> np.ndarray:
    """Algo entrando al agua en otra sala. Nunca se aclara que fue."""
    dur = 5.0
    n = muestras(dur)
    t = tiempo(dur)
    entrada = pasabanda(RNG.normal(0, 1, n), 200.0, 2_000.0, 2)
    entrada *= envolvente(n, 0.010, 0.22, 3.0)
    burbujas = np.zeros(n)
    for _ in range(9):
        pos = muestras(RNG.uniform(0.05, 0.9))
        largo = min(muestras(0.12), n - pos)
        if largo <= 0:
            continue
        tc = np.arange(largo) / SR
        f = RNG.uniform(300.0, 900.0)
        burbujas[pos:pos + largo] += (np.sin(2 * np.pi * f * tc * (1 + 1.5 * tc / (largo / SR)))
                                      * np.exp(-tc / 0.020) * RNG.uniform(0.2, 0.6))
    x = pasabajos(entrada * 0.5 + burbujas * 0.4, 2_500.0, 2) * 0.22
    return rampa(reverberar(x, SALAS["piscinas"], 0.82), 0.01, 1.20)


# ==========================================================================
# 4. Transicion entre niveles
# ==========================================================================

def tr_titileo() -> np.ndarray:
    """Aviso previo: el tubo duda un instante antes de que se corte todo."""
    dur = 0.9
    t = tiempo(dur)
    zumbido = np.sin(2 * np.pi * 100.0 * t) + 0.4 * np.sin(2 * np.pi * 200.0 * t)
    corte = np.ones(len(t))
    for inicio, fin in ((0.10, 0.16), (0.30, 0.34), (0.55, 0.66)):
        corte[muestras(inicio):muestras(fin)] = 0.12
    chispa = pasabanda(rosa(dur), 2_000.0, 7_000.0, 2) * (1.0 - corte) * 0.35
    x = zumbido * corte * 0.20 + chispa
    return rampa(reverberar(x, SALAS["oficina"], 0.30), 0.01, 0.10)


def tr_apagon() -> np.ndarray:
    """Se corta la alimentacion.

    Tres cosas encadenadas: el contactor abriendo, el zumbido cayendo de tono
    mientras el balasto se descarga, y el silencio entrando de golpe. La caida
    de tono es la parte importante: sin eso parece que bajaron un volumen.
    """
    dur = 1.6
    n = muestras(dur)
    t = tiempo(dur)

    contactor = pasabanda(RNG.normal(0, 1, n), 150.0, 2_200.0, 2) * envolvente(n, 0.0008, 0.035, 6.0)

    # El zumbido pierde frecuencia y amplitud a la vez.
    caida = np.exp(-t / 0.16)
    fase = 2 * np.pi * np.cumsum(100.0 * (0.35 + 0.65 * caida)) / SR
    moribundo = (np.sin(fase) + 0.4 * np.sin(2 * fase)) * caida * 0.6

    # El aire acondicionado tambien se para, pero tarda mas.
    aire = pasabajos(marron(dur), 180.0, 3) * 4.0 * np.exp(-t / 0.55) * 0.10

    x = contactor * 0.7 + moribundo * 0.35 + aire
    return rampa(reverberar(x, SALAS["oficina"], 0.38), 0.001, 0.25)


def tr_encendido() -> np.ndarray:
    """El tubo arranca en frio.

    Los chispazos caen exactamente donde la luz titila en RotacionNiveles
    (0.00, 0.12, 0.20, 0.30, 0.36 del avance), asi que el ojo y el oido dicen
    lo mismo. Esa sincronia es la mitad del efecto.
    """
    dur = 2.2
    n = muestras(dur)
    t = tiempo(dur)

    x = np.zeros(n)
    # El rele de arranque, primero.
    rele = pasabanda(RNG.normal(0, 1, n), 200.0, 1_800.0, 2) * envolvente(n, 0.001, 0.030, 6.0)
    x += rele * 0.55

    # Chispazos del cebador, en los mismos instantes que el parpadeo visual.
    for avance, fuerza in ((0.02, 0.9), (0.14, 0.7), (0.22, 1.0), (0.32, 0.6), (0.40, 0.8)):
        pos = muestras(avance * dur)
        largo = min(muestras(0.11), n - pos)
        if largo <= 0:
            continue
        tc = np.arange(largo) / SR
        chispa = pasabanda(RNG.normal(0, 1, largo), 800.0, 6_500.0, 2) * np.exp(-tc / 0.014)
        golpe = np.sin(2 * np.pi * 100.0 * tc) * np.exp(-tc / 0.030) * 0.5
        x[pos:pos + largo] += (chispa + golpe) * fuerza * 0.6

    # El zumbido estabilizandose: sube de tono hasta los 100 Hz definitivos.
    arranque = np.clip((t - 0.45 * dur) / (0.45 * dur), 0.0, 1.0)
    fase = 2 * np.pi * np.cumsum(100.0 * (0.80 + 0.20 * arranque)) / SR
    zumbido = (np.sin(fase) + 0.45 * np.sin(2 * fase)) * arranque * 0.22
    x += zumbido

    return rampa(reverberar(x, SALAS["oficina"], 0.32), 0.002, 0.30)


# ==========================================================================
# 5. La figura
# ==========================================================================

def figura_presencia() -> np.ndarray:
    """Lo que se escucha cuando la figura cruza el fondo.

    No es un golpe ni un grito: es una caida de presion. Un tono grave que
    aparece por debajo del ambiente y se va sin haber llegado a nada. Si el
    jugador no lo nota conscientemente, esta bien hecho.
    """
    dur = 6.0
    t = tiempo(dur)
    sobre = np.exp(-((t - 2.6) ** 2) / 2.6)

    grave = np.sin(2 * np.pi * 38.0 * t + 1.5 * np.sin(2 * np.pi * 0.13 * t))
    grave += 0.4 * np.sin(2 * np.pi * 57.0 * t + 0.7)

    # Una banda de ruido que se estrecha: da la sensacion de algo acercandose
    # sin que se pueda decir que es.
    tenso = pasabanda(rosa(dur), 120.0, 420.0, 2) * 0.7

    x = (grave * 0.35 + tenso) * sobre * 0.30
    return rampa(reverberar(x, SALAS["deposito"], 0.55), 0.60, 1.50)


# ==========================================================================
# 6. Musica
# ==========================================================================

def nota(midi: int) -> float:
    return 440.0 * (2.0 ** ((midi - 69) / 12.0))


def tema(compas: float = 9.0) -> np.ndarray:
    """Tema del menu: pieza ambiental original, propia del mod.

    Ocho acordes largos sobre un pedal de la, con las voces entrando y saliendo
    a destiempo para que nunca se escuche un cambio de acorde limpio. No hay
    ritmo, no hay melodia que se pueda tararear: es armonia sostenida, que es
    lo unico que aguanta escucharse en bucle sin cansar.

    Se escribe como musica_defecto.ogg. Si el owner consigue una pista con
    licencia, la deja en musica/tema.ogg y el mod la prefiere sin tocar codigo.
    """
    # La menor, con dos giros que no resuelven. El pedal de la sostiene todo.
    progresion = [
        [45, 57, 60, 64, 72],   # Am
        [41, 53, 57, 60, 65],   # F
        [43, 55, 58, 62, 67],   # Gm
        [40, 52, 55, 59, 64],   # Em
        [45, 57, 60, 67, 76],   # Am add
        [46, 53, 58, 62, 65],   # Bb
        [43, 55, 59, 62, 69],   # G
        [45, 57, 64, 69, 72],   # Am
    ]

    dur = compas * len(progresion)
    n = muestras(dur)
    t = tiempo(dur)
    x = np.zeros(n)

    for indice, acorde in enumerate(progresion):
        inicio = indice * compas
        for voz, midi in enumerate(acorde):
            # Cada voz entra y sale con su propio retraso: los acordes se
            # solapan y el cambio nunca cae todo junto.
            desfase = RNG.uniform(-0.9, 0.9) + voz * 0.28
            arranque = max(0.0, inicio + desfase)
            largo = compas * RNG.uniform(1.35, 1.75)
            pos = muestras(arranque)
            cuenta = min(muestras(largo), n - pos)
            if cuenta <= 0:
                continue

            tc = np.arange(cuenta) / SR
            frecuencia = nota(midi)

            # Voz con desafinacion lenta: dos osciladores separados unos cents.
            voz_a = np.sin(2 * np.pi * frecuencia * tc)
            voz_b = np.sin(2 * np.pi * frecuencia * 1.0018 * tc + 0.9)
            armonico = np.sin(2 * np.pi * frecuencia * 2.0 * tc) * 0.16
            tercero = np.sin(2 * np.pi * frecuencia * 3.0 * tc) * 0.07
            onda = (voz_a + voz_b) * 0.5 + armonico + tercero

            # Envolvente de fuelle: entra y sale sin que se note el borde.
            fase = np.clip(tc / (largo), 0.0, 1.0)
            env = np.sin(np.pi * fase) ** 1.6
            env *= 1.0 + 0.06 * np.sin(2 * np.pi * RNG.uniform(0.15, 0.4) * tc)

            peso = 1.0 / (1.0 + voz * 0.55)
            x[pos:pos + cuenta] += onda * env * peso * 0.30

    # Sub: el la que no se va nunca. Es lo que ata la pieza.
    sub = np.sin(2 * np.pi * nota(33) * t) * 0.22
    sub *= 0.75 + 0.25 * np.sin(2 * np.pi * 0.037 * t)
    x += sub

    # Campanas muy sueltas, apenas audibles, sobre notas de la escala.
    for _ in range(11):
        pos = muestras(RNG.uniform(2.0, dur - 6.0))
        largo = min(muestras(RNG.uniform(3.0, 5.0)), n - pos)
        if largo <= 0:
            continue
        tc = np.arange(largo) / SR
        midi = int(RNG.choice([76, 79, 81, 84, 72]))
        f = nota(midi)
        campana = (np.sin(2 * np.pi * f * tc)
                   + 0.35 * np.sin(2 * np.pi * f * 2.76 * tc)
                   + 0.18 * np.sin(2 * np.pi * f * 5.4 * tc))
        campana *= np.exp(-tc / RNG.uniform(0.8, 1.6))
        x[pos:pos + largo] += campana * RNG.uniform(0.03, 0.07)

    # Piso de ruido: una grabacion perfectamente limpia suena artificial.
    x += pasabajos(rosa(dur), 3_000.0, 2) * 0.010

    # El filtro se abre y se cierra muy despacio, como si la sala respirara.
    lento = 0.5 + 0.5 * np.sin(2 * np.pi * 0.012 * t)
    x = pasabajos(x, 1_400.0, 2) * 0.55 + pasabajos(x, 4_500.0, 2) * 0.45 * lento.mean()

    x = reverberar(x, SALAS["deposito"], 0.40)
    x = saturar(x, 1.1)
    return bucle_suave(x, 5.0)


# ==========================================================================
# Catalogo
# ==========================================================================

PIEZAS = {
    # Interfaz
    "ui/pasar": ui_pasar,
    "ui/elegir": ui_elegir,
    "ui/confirmar": ui_confirmar,
    "ui/volver": ui_volver,
    "ui/alternar": ui_alternar,
    "ui/abrir": ui_abrir,
    "ui/cerrar": ui_cerrar,
    "ui/negado": ui_negado,

    # Ambientes base, en bucle
    "ambiente/nivel0": lambda: bucle_suave(base_nivel0(), 4.0),
    "ambiente/nivel1": lambda: bucle_suave(base_nivel1(), 5.0),
    "ambiente/nivel2": lambda: bucle_suave(base_nivel2(), 4.0),
    "ambiente/nivel3": lambda: bucle_suave(base_nivel3(), 6.0),

    # Capa de caracter, tambien en bucle. Duraciones primas con las bases para
    # que las dos camas de cada nivel no vuelvan a alinearse en toda la sesion.
    "caracter/nivel0": lambda: bucle_suave(caracter_nivel0(), 5.0),
    "caracter/nivel1": lambda: bucle_suave(caracter_nivel1(), 6.0),
    "caracter/nivel2": lambda: bucle_suave(caracter_nivel2(), 4.0),
    "caracter/nivel3": lambda: bucle_suave(caracter_nivel3(), 7.0),

    # Eventos
    "evento/nivel0_tubo": ev_nivel0_tubo,
    "evento/nivel0_placa": ev_nivel0_placa,
    "evento/nivel0_puerta": ev_nivel0_puerta,
    "evento/nivel1_metal": ev_nivel1_metal,
    "evento/nivel1_estructura": ev_nivel1_estructura,
    "evento/nivel1_lejano": ev_nivel1_lejano,
    "evento/nivel2_cano": ev_nivel2_golpe_cano,
    "evento/nivel2_valvula": ev_nivel2_valvula,
    "evento/nivel2_goteo": ev_nivel2_goteo,
    "evento/nivel3_gota": ev_nivel3_gota,
    "evento/nivel3_ondas": ev_nivel3_ondas,
    "evento/nivel3_ventilacion": ev_nivel3_ventilacion,
    "evento/nivel3_lejano": ev_nivel3_lejano,

    # Transicion
    "nivel/titileo": tr_titileo,
    "nivel/apagon": tr_apagon,
    "nivel/encendido": tr_encendido,

    # Figura
    "figura/presencia": figura_presencia,

    # Musica propia (pista por defecto)
    "musica/defecto": tema,
}

# Picos por familia: la UI tiene que quedar por debajo del ambiente.
PICOS = {
    "ui/": 0.55,
    "ambiente/": 0.70,
    "caracter/": 0.66,
    "evento/": 0.72,
    "nivel/": 0.80,
    "figura/": 0.60,
    "musica/": 0.78,
}


def pico_de(nombre: str) -> float:
    for prefijo, valor in PICOS.items():
        if nombre.startswith(prefijo):
            return valor
    return 0.85


def main() -> int:
    global DESTINO
    if len(sys.argv) > 1:
        DESTINO = Path(sys.argv[1])

    print(f"Generando {len(PIEZAS)} piezas en {DESTINO}\n")
    total = 0.0
    for nombre, generador in PIEZAS.items():
        datos = generador()
        escribir(nombre, datos, pico_de(nombre))
        total += (DESTINO / f"{nombre}.ogg").stat().st_size

    print(f"\nTotal: {total/1024:.1f} kB en {len(PIEZAS)} archivos.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
