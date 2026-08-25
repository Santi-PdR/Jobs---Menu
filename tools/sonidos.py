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

# tools/ al path para poder importar el modulo de muestras reales.
sys.path.insert(0, str(Path(__file__).resolve().parent))

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
# QUINTA GENERACION, Y LA PRIMERA QUE NO SINTETIZA NADA.
#
# Van cuatro descartadas. Conviene dejar escrito por que, porque el error de
# fondo fue siempre el mismo y es facil volver a cometerlo:
#
#   1a. Clics comunes. Sonaban a interfaz de computadora.
#   2a. Clics mejores -sellos, interruptores, ruedas dentadas-. El problema no
#       era la calidad de cada pieza sino la categoria: un clic es un objeto
#       que se manipula, y aca no hay ningun objeto que manipular.
#   3a. Sin transitorio, todo con sumas de senoidales sobre 50 Hz. El concepto
#       era correcto -que suene el edificio, no la interfaz- pero una pila de
#       senoidales es justo lo que el oido reconoce como "sintetizador".
#   4a. Sintesis modal: resonadores inarmonicos excitados con ruido. Es la
#       tecnica con la que se modelan objetos fisicos, y aun asi fallo.
#
# La leccion de la cuarta es la importante: el problema NO era la tecnica. Era
# el metodo. Un golpe real trae cosas que ningun modelo pone porque no se le
# ocurren: el roce de la mano una milesima antes del impacto, la resonancia
# irregular de una pieza que no es perfecta, el aire de la habitacion donde se
# grabo, una cola que no cae en linea recta, y suciedad. Un modelo produce lo
# que el que lo escribio penso en poner; una grabacion trae ademas todo lo que
# no penso. Eso es lo que separa "objeto" de "sintetizador".
#
# Asi que la cadena cambia de raiz:
#
#   ANTES:  ruido -> sintesis -> filtro -> "sonido de boton"
#   AHORA:  grabacion real -> diseno sonoro -> sonido final
#
# La materia prima esta en tools/crudo/: 28 grabaciones reales de objetos
# fisicos golpeados -madera, tablon, metal, chapa, hojalata, vidrio, hormigon,
# material blando-, del paquete Impact Sounds de Kenney, CC0 / dominio publico.
# Detalle de la licencia y de la cadena de edicion en tools/muestras.py.
#
# NADA SE USA CRUDO. Usar una muestra de libreria tal cual se oye enseguida:
# suena a libreria. Cada gesto se construye con una o dos grabaciones que se
# recortan, se bajan de altura, se les quita el brillo que las delata, se
# mezclan por capas, a veces se invierten, y siempre pasan por la misma sala.
#
# El reparto de materiales sigue el criterio de que el oido tiene que poder
# distinguir confirmar de volver sin pensarlo:
#
#   pasar      hormigon lejano, casi solo aire     (roce, no golpe)
#   elegir     madera del tablon, seca             (el dedo sobre la hoja)
#   alternar   vidrio corto, muy amortiguado       (la chincheta girando)
#   confirmar  madera grave + chapa lejana         (el sello sobre la hoja)
#   volver     el mismo cuerpo, mas hueco y suave  (pariente de confirmar)
#   abrir      chapa invertida: el aire entra      (succion, no golpe)
#   cerrar     chapa pesada apagandose             (el aire se va)
#   negado     dos golpes sordos sobre algo firme  (nada resuena)
#
# Reglas duras que sobreviven a las cuatro generaciones porque eran correctas,
# y que ahora se cumplen editando en vez de sintetizando:
#   - Ningun ataque por debajo de 6 ms. Se redondea con suavizar_ataque().
#   - Techo de energia en 5 kHz. Todo lo de arriba suena a plastico.
#   - Componente grave presente en todos: si no hay graves no hay tamano.
#   - Una sola sala comun a los ocho, para que pertenezcan al mismo sitio.
#   - Entre 90 y 700 ms.

from muestras import (  # noqa: E402
    altura,
    apilar,
    cargar,
    cola,
    fundido,
    invertir,
    normalizar as norm_m,
    pasaaltos as pa_m,
    pasabajos as pb_m,
    realzar,
    recortar,
    retrasar,
    sala,
    suavizar_ataque,
)

# La sala de la interfaz es la del recinto donde esta clavada la hoja: chica,
# seca, con un poco de cola. No es un efecto: es el sitio.
UI = impulso(0.40, 0.005, 2_800.0, 0.9)

# La red electrica. Ya no construye los gestos: solo los apoya, muy por debajo,
# para que esten enchufados al mismo lugar que los ambientes.
RED = 50.0


def _red(dur: float, armonicos, decaimiento: float) -> np.ndarray:
    """Un pedacito de zumbido de red con caida propia.

    Se conserva para el apoyo grave de algunos gestos y para la transicion.
    """
    n = muestras(dur)
    t = tiempo(dur)
    x = np.zeros(n)
    for multiplo, nivel in armonicos:
        fase = RNG.uniform(0, 2 * np.pi)
        x += nivel * np.sin(2 * np.pi * RED * multiplo * t + fase)
    return x * np.exp(-t / decaimiento)


def _acabado(x: np.ndarray, dur: float, mezcla: float, ataque: float = 7.0,
             techo: float = 4_600.0) -> np.ndarray:
    """Acabado comun a los ocho gestos.

    Recorta a la duracion pedida, redondea el ataque, quita el brillo que
    delata la muestra de libreria, mete la pieza en la sala del mod y cierra
    con fundidos. Que los ocho pasen por aqui es lo que los vuelve una familia.
    """
    n = muestras(dur)
    if len(x) < n:
        x = np.concatenate([x, np.zeros(n - len(x))])
    x = x[:n]
    x = suavizar_ataque(x, ataque)
    x = pb_m(x, techo, 4)
    x = sala(x, UI, mezcla)[:n]
    return fundido(norm_m(x, 0.9), 0.005, 0.05)


def ui_pasar() -> np.ndarray:
    """Recorrer los renglones del aviso.

    Es el gesto que mas veces se oye en una sesion, asi que es el que menos
    tiene que pesar: si tuviera cuerpo, cansaria a los treinta segundos. Se
    parte de una pisada de hormigon, se le quita el impacto entero y se deja
    solo el aire que la pisada movio, cinco octavas de reverberacion mas lejos.
    No es un golpe: es el desplazamiento de aire de un golpe que ocurrio en
    otra parte.
    """
    base = cargar("footstep_concrete_000")
    aire = cola(base, 0.012)              # fuera el impacto: solo lo que queda
    aire = altura(aire, 0.55)             # mas grave y mas largo: mas lejos
    aire = pb_m(aire, 1_400.0, 4) * 0.5
    roce = pa_m(cargar("footstep_carpet_000"), 900.0) * 0.16
    roce = altura(roce, 0.8)
    x = apilar((aire, 1.0), (roce, 0.5))
    return _acabado(x, 0.14, 0.30, ataque=9.0, techo=2_600.0)


def ui_elegir() -> np.ndarray:
    """Marcar un renglon.

    La madera del tablon donde esta clavada la hoja. Seca, corta, con el
    cuerpo justo para que se note que algo solido acaba de recibir un toque.
    Dos capas de la misma familia a alturas distintas: la de abajo pone el
    tamano de la tabla, la de arriba el contacto del dedo.
    """
    tabla = altura(cargar("impactWood_medium_000"), 0.88)
    tabla = realzar(tabla, 240.0, 1.1, 1.3)
    # El contacto del dedo vive entre 900 y 2500 Hz: sin esa banda la madera
    # deja de leerse como madera y pasa a ser un golpe sordo sin material.
    toque = altura(cargar("impactWood_light_000"), 1.15)
    toque = realzar(pa_m(toque, 700.0), 1_500.0, 0.8, 2.2) * 0.85
    # El retumbe por debajo de 120 Hz no es la madera: es el microfono. Se
    # quita para que el golpe suene a tabla y no a bombo.
    tabla = pa_m(tabla, 120.0, 2)
    x = apilar((tabla, 1.0), (toque, 1.15))
    return _acabado(x, 0.20, 0.24, ataque=6.5, techo=4_200.0)


def ui_alternar() -> np.ndarray:
    """Pasar de un aviso a otro.

    La chincheta que sujeta la hoja, girando en su agujero. Vidrio corto y muy
    amortiguado: se le corta la cola casi entera para que quede el contacto y
    nada mas. Es el gesto mas breve de los ocho, porque alternar es un
    movimiento, no una decision.
    """
    pieza = recortar(cargar("impactGlass_light_001"), 0.0, 0.055)
    pieza = altura(pieza, 0.62)
    pieza = pb_m(pieza, 3_100.0, 4)
    cuerpo = altura(cargar("impactWood_light_002"), 0.55) * 0.30
    x = apilar((pieza, 1.0), (cuerpo, 0.7))
    return _acabado(x, 0.13, 0.20, ataque=6.0, techo=3_400.0)


def ui_confirmar() -> np.ndarray:
    """Aceptar el turno. El gesto con mas peso del menu.

    El sello sobre la hoja: primero la madera grave -el cuerpo del sello contra
    la mesa- y, unos milisegundos despues, una chapa lejana que responde al
    golpe desde el fondo del recinto. Esa segunda capa retrasada es la que da
    la sensacion de que el sitio es grande y de que la decision tuvo
    consecuencias en algun lugar que no se ve.
    """
    sello = altura(cargar("impactWood_medium_003"), 0.66)
    sello = realzar(sello, 140.0, 0.9, 1.6)
    sello = realzar(sello, 1_200.0, 0.7, 1.35)
    chapa = altura(cargar("impactPlate_medium_001"), 0.42)
    chapa = pb_m(chapa, 1_900.0, 4)
    chapa = retrasar(chapa, 0.045) * 0.34
    sello = pa_m(sello, 90.0, 2)
    grave = _red(0.30, ((1, 0.5), (2, 0.2)), 0.11) * 0.10
    x = apilar((sello, 1.0), (chapa, 0.8), (grave, 0.9))
    return _acabado(x, 0.46, 0.34, ataque=7.0, techo=3_600.0)


def ui_volver() -> np.ndarray:
    """Retroceder.

    Pariente directo de confirmar -el mismo cuerpo de madera- pero una quinta
    mas abajo, sin la chapa del fondo y con el ataque mas redondeado. Que se
    reconozca la familia es deliberado: volver es confirmar al reves, y el oido
    tiene que emparentarlos sin confundirlos.
    """
    cuerpo = altura(cargar("impactWood_medium_003"), 0.60)
    cuerpo = realzar(cuerpo, 320.0, 1.0, 1.2)
    # Un poco de tablon aporta la madera que se reconoce; sin el, el gesto se
    # hunde por debajo de donde los parlantes chicos ya no llegan.
    veta = altura(cargar("impactPlank_medium_002"), 0.70)
    veta = pb_m(pa_m(veta, 600.0), 3_000.0) * 0.70
    hueco = altura(cargar("impactSoft_medium_000"), 0.8) * 0.35
    cuerpo = pa_m(cuerpo, 95.0, 2)
    x = apilar((cuerpo, 1.0), (veta, 1.0), (hueco, 0.45))
    return _acabado(x, 0.34, 0.30, ataque=9.0, techo=3_200.0)


def ui_abrir() -> np.ndarray:
    """Abrir el aviso.

    Una chapa invertida. Al dar la vuelta a una grabacion, la cola de
    resonancia pasa a ir hacia adelante y el golpe queda al final: el sonido
    entra en vez de salir. Es exactamente la sensacion de un recinto que se
    abre y se llena de aire. El golpe final se recorta casi del todo para que
    no haya impacto, solo la llegada.
    """
    chapa = invertir(cargar("impactPlate_light_002"))
    chapa = altura(chapa, 0.48)
    chapa = pb_m(chapa, 2_200.0, 4)
    chapa = recortar(chapa, 0.0, 0.30)
    aire = invertir(cola(cargar("footstep_concrete_002"), 0.010))
    aire = altura(aire, 0.5) * 0.4
    x = apilar((chapa, 1.0), (aire, 0.6))
    return _acabado(x, 0.32, 0.36, ataque=14.0, techo=2_800.0)


def ui_cerrar() -> np.ndarray:
    """Cerrar el aviso. El reverso de abrir: el aire se va.

    La misma chapa, ahora en su sentido natural, bajada y con la cola alargada
    hacia el silencio. Termina mas abajo de donde empezo.
    """
    chapa = altura(cargar("impactPlate_heavy_000"), 0.40)
    chapa = pb_m(chapa, 1_700.0, 4)
    fondo = altura(cargar("impactSoft_heavy_001"), 0.85) * 0.45
    x = apilar((chapa, 1.0), (fondo, 0.7))
    return _acabado(x, 0.40, 0.32, ataque=8.0, techo=2_200.0)


def ui_negado() -> np.ndarray:
    """Accion invalida.

    Sin pitido, sin nota triste, sin nada que parezca un error de sistema
    operativo. Dos golpes sordos sobre algo que no cede: el segundo mas flojo
    que el primero, y ninguno de los dos resuena. La informacion esta en que no
    pasa nada. El material se elige muerto a proposito -blando, sin cola-
    porque es la unica forma de que "no" se lea como "no".
    """
    seco = altura(cargar("impactSoft_medium_000"), 0.78)
    # Se le suma un roce mate: "no" tiene que oirse tambien en un portatil, y
    # para eso hace falta algo por encima de los 500 Hz aunque sea sin brillo.
    mate = pb_m(pa_m(altura(cargar("footstep_carpet_000"), 0.7), 500.0), 2_000.0)
    seco = apilar((pa_m(pb_m(seco, 1_100.0, 4), 110.0, 2), 1.0), (mate, 0.55))
    seco = recortar(seco, 0.0, 0.10)
    x = apilar((seco, 1.0), (retrasar(seco, 0.082), 0.52))
    return _acabado(x, 0.30, 0.18, ataque=7.0, techo=1_600.0)


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
# 2c. Capa de actividad: lo que pasa cada tanto, y no cuando uno lo espera
# ==========================================================================
# Las dos camas de arriba resuelven que HAYA sonido siempre. No resuelven el
# otro problema, que es mas dificil: que el sitio siga pareciendo habitado
# despues de diez minutos. Una cama continua, por bien hecha que este, se
# vuelve mobiliario -el oido la archiva como "silencio de esta escena" y deja
# de contarla-. Lo que impide eso no es mas ruido de fondo: son SUCESOS.
#
# Los eventos del bloque 3 ya existen, pero los dispara el programador con un
# temporizador, y eso tiene un techo: se oyen en primer plano, uno por vez, y
# cada uno es un archivo que empieza y termina. Esta tercera capa hace lo
# contrario. Es un bucle largo -de 47 a 61 segundos- que esta casi todo el
# tiempo en silencio y donde, cada tanto, ocurre algo LEJOS: al fondo del
# edificio, dos plantas mas abajo, del otro lado del vidrio.
#
# Por que en bucle y no como evento suelto:
#   - se solapa con los otros sucesos en vez de esperar turno, que es lo que
#     hace un edificio de verdad;
#   - al ser primo con las otras dos camas (24/37/53, 28/41/59, 22/29/47,
#     30/43/61) las tres nunca vuelven a alinearse dentro de una sesion;
#   - no puede terminar y dejar silencio, porque no termina.
#
# El material es grabacion real, la misma que la interfaz. Aca se la trata al
# reves que en un gesto de UI: en vez de acercarla se la manda lejos -octavas
# abajo, sin agudos, con mucha mas sala que sonido directo-. Un golpe de chapa
# bajado dos octavas y metido al fondo de un deposito ya no es un golpe de
# chapa: es algo que se cayo en otra parte del edificio.


def _lejos(x: np.ndarray, octavas: float, techo: float, cuerpo: float = 0.0) -> np.ndarray:
    """Manda una grabacion al fondo del edificio.

    La distancia no es bajar el volumen: es perder agudos, ganar tamano y
    perder el transitorio. Un objeto lejano llega sin el filo del ataque
    porque el aire y las paredes ya se lo comieron por el camino.
    """
    y = altura(x, 2.0 ** -octavas)
    y = pb_m(y, techo, 4)
    y = suavizar_ataque(y, 18.0)
    if cuerpo > 0.0:
        y = realzar(y, cuerpo, 1.2, 1.6)
    # Por debajo de 45 Hz no hay informacion de material, solo retumbe de
    # microfono, y en una cama que se escucha una hora ese retumbe se acumula.
    return pa_m(y, 45.0, 2)


def _sembrar(destino: np.ndarray, pieza: np.ndarray, segundo: float,
             ganancia: float) -> None:
    """Coloca una pieza en el bucle, envolviendo por el final.

    Lo que se pasa del final vuelve a entrar por el principio. Sin esto, la
    ultima cola quedaria cortada en seco justo en la junta del bucle, que es
    el unico lugar donde no se puede tener un corte.
    """
    n = len(destino)
    pos = int(segundo * SR) % n
    for i in range(0, len(pieza), n):
        trozo = pieza[i:i + n] * ganancia
        fin = pos + len(trozo)
        if fin <= n:
            destino[pos:fin] += trozo
        else:
            corte = n - pos
            destino[pos:] += trozo[:corte]
            destino[:len(trozo) - corte] += trozo[corte:]


def actividad_nivel0(dur: float = 53.0) -> np.ndarray:
    """Nivel 0. El edificio administrativo trabajando sin nadie adentro.

    Una oficina vacia no esta quieta: el cielorraso se mueve con la presion
    del aire acondicionado, alguna puerta cortafuego se asienta, y muy de vez
    en cuando algo cae en otra planta. Nada de esto se oye claro: la placa de
    yeso y la alfombra se comen todo lo que no sea grave.
    """
    x = np.zeros(muestras(dur))

    # Placas del cielorraso asentandose con el aire. Es el sonido que uno
    # escucha mil veces en una oficina y no registra jamas.
    placa = _lejos(cargar("impactPlate_light_000"), 1.15, 900.0, 190.0)
    for s, g in ((3.4, 0.22), (17.9, 0.15), (31.2, 0.26), (44.6, 0.18)):
        _sembrar(x, placa, s, g)

    # Madera de un marco de puerta trabajando. Muy separadas entre si.
    marco = _lejos(cargar("footstep_wood_001"), 1.6, 700.0, 130.0)
    for s, g in ((9.8, 0.30), (26.5, 0.21), (48.1, 0.27)):
        _sembrar(x, marco, s, g)

    # Algo en otra planta. Dos veces en casi un minuto, y sin cuerpo: llega
    # solo la parte grave, que es lo unico que atraviesa un forjado.
    lejano = _lejos(cargar("impactGeneric_light_000"), 2.3, 320.0)
    _sembrar(x, lejano, 21.7, 0.34)
    _sembrar(x, lejano, 39.4, 0.25)

    return reverberar(x, SALAS["oficina"], 0.62)


def actividad_nivel1(dur: float = 59.0) -> np.ndarray:
    """Nivel 1. La nave dilatandose.

    Un galpon con techo de chapa cambia de forma todo el dia. No hay nadie y
    sin embargo suena: la estructura se mueve, y en un volumen de ese tamano
    cada movimiento vuelve tres o cuatro veces desde paredes distintas.
    """
    x = np.zeros(muestras(dur))

    # Chapa del techo. El clac de dilatacion, bajado dos octavas para que la
    # plancha sea de diez metros y no de treinta centimetros.
    chapa = _lejos(cargar("impactMetal_heavy_000"), 2.0, 1_800.0, 95.0)
    for s, g in ((6.2, 0.26), (23.8, 0.34), (41.1, 0.20), (54.7, 0.29)):
        _sembrar(x, chapa, s, g)

    # Tirante de madera del entrepiso, crujiendo bajo su propio peso.
    tirante = _lejos(cargar("impactPlank_medium_000"), 1.7, 1_100.0, 110.0)
    for s, g in ((14.3, 0.24), (35.6, 0.31), (49.2, 0.17)):
        _sembrar(x, tirante, s, g)

    # Un perfil metalico cediendo un milimetro. Invertido: no es un golpe, es
    # una tension que se acumula y se suelta.
    tension = invertir(_lejos(cargar("impactMetal_medium_001"), 1.4, 1_400.0))
    _sembrar(x, tension, 29.9, 0.22)
    _sembrar(x, tension, 57.3, 0.19)

    return reverberar(x, SALAS["deposito"], 0.70)


def actividad_nivel2(dur: float = 47.0) -> np.ndarray:
    """Nivel 2. La instalacion del pasillo de servicio.

    Aca los sucesos son mas seguidos que en los otros niveles y estan mas
    cerca, porque el pasillo es estrecho y uno esta literalmente adentro de la
    instalacion: los canos pasan a un palmo de la cabeza. Es el nivel mas
    inquieto de los cuatro, y tiene que serlo tambien de oido.
    """
    x = np.zeros(muestras(dur))

    # Dilatacion del cano de agua caliente: el tic-tic del metal, en grupos
    # irregulares, nunca a intervalo fijo. Un cano que hace tic cada segundo
    # exacto es un metronomo, y un metronomo delata la maquina.
    tic = _lejos(cargar("impactMetal_light_000"), 0.9, 3_200.0, 320.0)
    for s, g in ((2.1, 0.20), (2.9, 0.14), (3.4, 0.10),
                 (18.6, 0.22), (19.2, 0.16), (20.3, 0.11),
                 (33.8, 0.19), (34.5, 0.13),
                 (44.2, 0.21), (45.1, 0.15)):
        _sembrar(x, tic, s, g)

    # Chapa de un conducto de ventilacion pandeando al cambiar la presion.
    conducto = _lejos(cargar("impactTin_medium_000"), 1.3, 2_200.0, 240.0)
    for s, g in ((11.4, 0.26), (27.7, 0.31), (40.9, 0.23)):
        _sembrar(x, conducto, s, g)

    # Un golpe de ariete lejano: la bomba que arranca en algun sitio y toda la
    # columna de agua acusa el golpe. Es el unico suceso grande del nivel.
    ariete = _lejos(cargar("impactMetal_light_003"), 2.1, 600.0, 80.0)
    _sembrar(x, ariete, 15.2, 0.30)
    _sembrar(x, ariete, 38.5, 0.24)

    return reverberar(x, SALAS["servicio"], 0.66)


def actividad_nivel3(dur: float = 61.0) -> np.ndarray:
    """Nivel 3. El natatorio, donde cada suceso tarda en apagarse.

    Es el nivel donde menos cosas pasan y donde mas se notan, porque el
    azulejo no absorbe nada: un solo golpe se queda dando vueltas cuatro
    segundos. La regla aca fue poner MENOS de lo que pedia el impulso, y
    dejar que la sala haga el trabajo.
    """
    x = np.zeros(muestras(dur))

    # Azulejo suelto en el fondo del vaso, movido por el agua. Vidrioso, corto,
    # y con toda la cola puesta por el recinto.
    azulejo = _lejos(cargar("impactGlass_light_003"), 0.8, 4_200.0, 480.0)
    for s, g in ((8.7, 0.16), (25.3, 0.21), (52.8, 0.13)):
        _sembrar(x, azulejo, s, g)

    # El ventanal. Una lamina de vidrio de tres metros vibrando con el viento
    # de afuera: se oye el vidrio, no el viento.
    ventanal = _lejos(cargar("impactGlass_medium_000"), 1.9, 900.0, 105.0)
    _sembrar(x, ventanal, 16.9, 0.24)
    _sembrar(x, ventanal, 43.6, 0.19)

    # La escalera de mano metalica del borde, resonando bajo el agua. Bajada
    # tres octavas queda en el limite de lo que se puede llamar una nota.
    escalera = _lejos(cargar("impactBell_heavy_002"), 3.0, 420.0)
    _sembrar(x, escalera, 34.1, 0.17)

    # Chapa de la climatizacion, arriba del todo, casi en el techo del recinto.
    alto = _lejos(cargar("impactTin_medium_002"), 1.5, 1_600.0, 210.0)
    _sembrar(x, alto, 5.4, 0.14)
    _sembrar(x, alto, 47.2, 0.18)

    return reverberar(x, SALAS["piscinas"], 0.78)


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

    # Capa de actividad: bucles largos, casi vacios, con sucesos lejanos. El
    # cruce es corto porque casi toda la junta cae sobre silencio.
    "actividad/nivel0": lambda: bucle_suave(actividad_nivel0(), 2.0),
    "actividad/nivel1": lambda: bucle_suave(actividad_nivel1(), 2.0),
    "actividad/nivel2": lambda: bucle_suave(actividad_nivel2(), 2.0),
    "actividad/nivel3": lambda: bucle_suave(actividad_nivel3(), 2.0),

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
    "actividad/": 0.58,
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
