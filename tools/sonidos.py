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
    # Sala de piedra abovedada: grande y calida, cola media, sin el brillo del
    # azulejo -la piedra absorbe los agudos-, mas oscura que el deposito.
    "sala_piedra": impulso(2.30, 0.022, 2_400.0, 1.3),
    # Biblioteca: los libros y la madera absorben casi todo. Cola corta y
    # seca, mate, la mas apagada de todas -por eso el sitio se siente muerto-.
    "biblioteca": impulso(0.85, 0.010, 2_000.0, 0.6),
    # Invernadero: vidrio y volumen de aire. Cola media-larga y brillante -el
    # vidrio refleja los agudos-, pero la vegetacion la amortigua un poco.
    "invernadero": impulso(1.90, 0.018, 6_000.0, 1.0),
    # Catacumbas: tunel de piedra estrecho. Cola media, muy oscura y con muchas
    # reflexiones cercanas -las paredes estan a un brazo-, sin nada de agudos.
    "catacumbas": impulso(1.60, 0.006, 1_600.0, 1.6),
    # Cisterna: aljibe enorme lleno de agua. La cola mas larga de todas, oscura
    # y con muchisimo cuerpo; el agua y la piedra hacen que todo dure segundos.
    "cisterna": impulso(5.20, 0.040, 3_000.0, 1.7),
    # Salon del trono: nave alta de piedra, en ruinas y con boquetes al cielo.
    # Cola larga y grande, algo mas clara que la cisterna -esta seca- y con aire.
    "trono": impulso(3.40, 0.030, 4_000.0, 1.5),
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
    datos = np.asarray(datos, dtype=np.float64)

    # Dos politicas distintas, y la diferencia importa.
    #
    # Para la interfaz el pico es un TECHO: cada gesto se mezcla al nivel que
    # le toca sonar y aca solo se baja si se paso. Si se normalizara siempre,
    # las ocho piezas terminarian al mismo volumen y el balance de la familia
    # -que pasar suene siete decibelios por debajo de confirmar, porque suena
    # treinta veces mas seguido- moriria en la ultima linea de la cadena.
    #
    # Para todo lo demas el pico es un DESTINO, como siempre. Las camas de
    # ambiente se construyen sumando capas de amplitud muy chica y su nivel
    # final no significa nada: sin normalizar, base_nivel2 quedaba cincuenta
    # decibelios por debajo de base_nivel0 y directamente no se oia. Ahi el
    # volumen relativo lo fija MezclaAudio en Java, no el generador.
    # Quitar la componente continua ANTES de normalizar.
    #
    # Varias camas salian con un desplazamiento de hasta 0.135, o sea que la
    # onda entera estaba corrida respecto del cero. Eso no se oye por si mismo
    # -es una frecuencia de cero hercios- pero hace tres cosas malas: se come
    # ese porcentaje del margen antes de que suene una sola nota, produce un
    # golpe seco al arrancar y al parar la pieza, y al sumarse varias capas con
    # desplazamientos distintos el resultado recorta antes de lo que dicen los
    # picos. Viene de filtrar y reverberar ruido de baja frecuencia; se corrige
    # restando la media y con un pasaaltos muy bajo que no toca nada audible.
    if len(datos) > 32:
        datos = datos - float(np.mean(datos))
        datos = pasaaltos(datos, 18.0, 2)

    actual = float(np.max(np.abs(datos)))
    if nombre.startswith("ui/"):
        if actual > pico:
            datos = normalizar(datos, pico)
    elif actual > 0.0:
        datos = normalizar(datos, pico)
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
# SEXTA GENERACION. LA QUINTA ERA OCHO VARIANTES DEL MISMO GESTO.
#
# La quinta ya no sintetizaba nada -eso estaba bien y se conserva- pero fallo
# por otro lado, y el diagnostico salio de medir los ocho archivos juntos en
# vez de escucharlos de a uno:
#
#   pieza        dur    centroide   ataque   energia >4kHz
#   pasar       0.140     649 Hz     19 ms       0.0 %
#   elegir      0.200     374 Hz      9 ms       0.0 %
#   alternar    0.130    1021 Hz     13 ms       0.0 %
#   confirmar   0.460     539 Hz     11 ms       0.0 %
#   volver      0.340     511 Hz     10 ms       0.0 %
#   abrir       0.320     390 Hz    234 ms       0.0 %
#   cerrar      0.400     384 Hz     11 ms       0.0 %
#   negado      0.300     423 Hz      6 ms       0.0 %
#
# Siete de ocho entre 374 y 649 Hz. Cero energia por encima de 4 kHz en LOS
# OCHO. Todos con la misma forma: golpe al principio y cola que cae. Eso es,
# literalmente, un solo sonido con ocho alturas y ocho duraciones, que es
# exactamente lo que se dijo que no habia que hacer.
#
# Tres causas, todas mias:
#
#   1. TODA la materia prima eran impactos. Madera, chapa, vidrio, hojalata:
#      materiales distintos, pero un impacto siempre tiene la misma envolvente
#      -transitorio y decaimiento- y el oido clasifica por envolvente antes que
#      por material. Ocho impactos son ocho impactos.
#   2. _acabado() aplastaba las diferencias que quedaban. Pasabajos a 4 kHz o
#      menos para los ocho, la misma sala para los ocho, normalizado al mismo
#      pico para los ocho. Cada regla era defendible por separado; juntas
#      funcionaban como una prensa.
#   3. "Techo en 5 kHz" era una sobrecorreccion de la primera generacion, que
#      sonaba a plastico por brillo digital. Pero un roce de papel VIVE en los
#      6-10 kHz. Sin esa banda no hay papel: hay un golpe sordo mas.
#
# QUE CAMBIA
#
# La familia ya no se define por el material ni por la altura. Se define por
# el SITIO -una sala comun, en distinta cantidad- y por la MANO -que ningun
# gesto ataque en seco-. Dentro de eso, cada gesto tiene una forma propia:
#
#   gesto      clase de gesto      forma                     banda    dur
#   pasar      roce                sin ataque, corto         media    100
#   elegir     trinquete           dos tiempos: uno y otro   alta     170
#   alternar   pestillo            desliza y engancha        media    210
#   confirmar  posar con peso      presion, apoyo, sala      grave    520
#   volver     roce a la inversa   crece y se detiene        media    300
#   abrir      succion             el aire entra             baja     360
#   cerrar     apoyo y sello       cae y se cierra           baja     420
#   negado     nada resuena        golpe seco, y silencio    media    240
#
# Cinco clases distintas de gesto en vez de una. Un roce no tiene transitorio;
# un trinquete tiene DOS transitorios separados; un pestillo tiene ruido antes
# del enganche; una succion tiene la energia al final. Esas son diferencias que
# el oido no puede confundir aunque las alturas se toquen.
#
# TRAS CINCUENTA REPETICIONES
#
# Es el criterio que se pidio y cambia las decisiones. pasar suena cientos de
# veces por sesion: se le quito el cuerpo entero y quedo en 100 ms de aire con
# grano, mas bajo que todo lo demas. confirmar suena tres o cuatro veces:
# puede permitirse 520 ms y una cola de sala. La regla es que el volumen y la
# duracion de cada gesto son inversamente proporcionales a su frecuencia de
# uso, y por eso hay dieciseis dB entre el gesto mas repetido y el mas raro.
#
# El silencio tambien se diseno: negado termina en seco a proposito, sin cola
# de sala, porque la informacion esta en que el sitio no responde.
#
# MATERIA PRIMA
#
# Sigue sin sintetizarse nada. A las 28 grabaciones de impacto se sumaron 18
# de otras clases -roces, trinquetes, pestillos, objetos que se posan- del
# mismo origen CC0. Eran justamente las clases que faltaban.
#
# Reglas que sobreviven, porque estas si eran correctas:
#   - Ningun ataque instantaneo. Se redondea con suavizar_ataque().
#   - Una sola sala para los ocho. Es lo unico que los emparenta ahora.
#   - Componente grave en los gestos con peso. Sin graves no hay tamano.
# Reglas eliminadas:
#   - El techo unico en 5 kHz. Ahora cada gesto tiene el suyo, de 1.4 a 11 kHz.
#   - El pico unico. Ahora cada gesto se normaliza a lo que le toca sonar.

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
# seca, con un poco de cola. No es un efecto: es el sitio. Es lo unico que
# comparten los ocho gestos, y por eso ahora carga con todo el parentesco.
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


def _gesto(x: np.ndarray, dur: float, mezcla: float, ataque: float,
           pico: float, corte_bajo: float = 0.0, techo: float = 0.0,
           cierre: float = 0.05) -> np.ndarray:
    """Acabado comun, pero sin aplastar.

    La version anterior de esta funcion era la culpable de que los ocho gestos
    se parecieran: imponia el mismo techo de frecuencia y el mismo pico a
    todos. Ahora el techo y el pico son argumentos, y el techo puede no
    aplicarse. Lo unico obligatorio para los ocho es la sala y que el ataque no
    sea instantaneo, que es de donde tiene que venir el parentesco.

    El pico distinto por gesto es deliberado: es el balance de la familia. Un
    gesto que suena trescientas veces por sesion no puede pesar lo mismo que
    uno que suena tres.
    """
    n = muestras(dur)
    if len(x) < n:
        x = np.concatenate([x, np.zeros(n - len(x))])
    x = x[:n]
    x = suavizar_ataque(x, ataque)
    if corte_bajo > 0.0:
        x = pa_m(x, corte_bajo, 2)
    if techo > 0.0:
        x = pb_m(x, techo, 4)
    if mezcla > 0.0:
        x = sala(x, UI, mezcla)[:n]
    return fundido(norm_m(x, pico), 0.004, cierre)


def ui_pasar() -> np.ndarray:
    """Recorrer los renglones del aviso. El gesto mas repetido de todos.

    Es un ROCE, no un golpe: no tiene transitorio. El oido lo distingue de los
    otros siete antes de identificar de que material es, porque la ausencia de
    ataque es una diferencia de categoria, no de grado.

    Concretamente: el canto de la hoja de papel contra el tablon cuando la
    vista baja un renglon. Se hace con dos roces reales cruzados, uno con grano
    y otro suave, filtrados a la banda del papel -1.8 a 9 kHz- que es
    exactamente la banda que la version anterior recortaba. Cien milisegundos,
    y el pico mas bajo de la familia por bastante: 0.34 contra 0.9 de antes.
    Tres veces mas bajo, porque suena treinta veces mas seguido.

    Casi sin sala: un roce de papel a treinta centimetros no reverbera.
    """
    grano = pa_m(cargar("roceCorto_001"), 1_800.0, 2)
    grano = recortar(grano, 0.004, 0.075)
    # Techo del grano bajado de 6.2 a 5.2 kHz. Este es el gesto que suena mas
    # veces por sesion, y era el unico de la familia que se saltaba la regla de
    # "nada por encima de 5 kHz": tenia cerca de un quinto de su energia arriba
    # de esa linea. En un sonido que se dispara trescientas veces, ese brillo es
    # exactamente lo que fatiga el oido a los veinte minutos. Bajarlo no le quita
    # el caracter de papel -el papel se lee de sobra a 5 kHz- y lo vuelve un roce
    # que se puede escuchar toda una sesion sin cansar.
    grano = pb_m(grano, 5_200.0, 4)
    suave = altura(cargar("roceSuave_000"), 1.25)
    suave = pa_m(pb_m(suave, 5_000.0), 900.0, 2)
    suave = recortar(suave, 0.0, 0.09) * 0.55
    # Sin ataque: la envolvente sube y baja sin pico. Es lo que separa un roce
    # de un golpe, y es toda la identidad de este gesto.
    x = apilar((grano, 0.75), (suave, 1.0))
    n = len(x)
    forma = np.sin(np.linspace(0.0, np.pi, n)) ** 0.8
    return _gesto(x * forma, 0.100, 0.06, ataque=12.0, pico=0.21,
                  corte_bajo=700.0, techo=5_400.0, cierre=0.035)


def ui_elegir() -> np.ndarray:
    """Marcar un renglon.

    Un TRINQUETE: dos transitorios separados por veintiocho milisegundos. Esa
    separacion es la identidad del gesto -un diente que sube y cae- y es algo
    que un impacto no puede imitar por mucho que se le cambie la altura.

    El primero es el diente, corto y agudo. El segundo es el asiento, mas
    grave y mas flojo, con una pizca de madera del tablon debajo para que el
    mecanismo este montado en algo y no flotando. Lleva brillo hasta 11 kHz: es
    el gesto mas agudo de los ocho y tiene que serlo, porque es el que confirma
    que el dedo acerto.
    """
    diente = recortar(cargar("trinquete_000"), 0.0, 0.020)
    diente = pa_m(diente, 1_200.0, 2)
    asiento = altura(cargar("trinquete_001"), 0.82)
    asiento = recortar(asiento, 0.010, 0.065)
    asiento = pb_m(asiento, 7_000.0) * 0.62
    tabla = altura(cargar("impactWood_light_000"), 0.9)
    tabla = pb_m(pa_m(tabla, 260.0, 2), 2_400.0) * 0.30
    x = apilar((diente, 1.0),
               (retrasar(asiento, 0.028), 0.85),
               (retrasar(tabla, 0.028), 0.5))
    return _gesto(x, 0.170, 0.14, ataque=2.5, pico=0.52, techo=11_000.0)


def ui_alternar() -> np.ndarray:
    """Pasar de un aviso a otro.

    Un PESTILLO: ruido de deslizamiento primero, enganche despues. La forma es
    al reves que la de un impacto -la energia esta al final, no al principio- y
    eso solo se consigue con material que de verdad se desliza.

    Alternar es un movimiento lateral, y esto lo dice sin metaforas: algo corre
    y se traba. La chincheta que sujetaba la hoja, girando en su agujero, ya no
    hace falta: era una explicacion bonita para un sonido que no la contaba.
    """
    corre = recortar(cargar("pestillo_000"), 0.0, 0.085)
    corre = pa_m(pb_m(corre, 6_500.0), 1_100.0, 2)
    # La rampa es el gesto: el ruido de deslizamiento crece hacia el enganche.
    corre *= np.linspace(0.25, 1.0, len(corre)) ** 1.4
    traba = recortar(cargar("trinquete_002"), 0.0, 0.045)
    traba = pb_m(pa_m(traba, 700.0, 2), 8_000.0)
    cuerpo = altura(cargar("impactWood_light_002"), 0.62)
    cuerpo = pb_m(cuerpo, 1_500.0) * 0.28
    x = apilar((corre, 0.7),
               (retrasar(traba, 0.086), 1.0),
               (retrasar(cuerpo, 0.088), 0.55))
    return _gesto(x, 0.210, 0.16, ataque=4.0, pico=0.62, techo=8_500.0)


def ui_confirmar() -> np.ndarray:
    """Aceptar el turno. El gesto con mas peso, y el que menos veces suena.

    No es un golpe: es POSAR ALGO PESADO. Hay presion antes del apoyo -el peso
    llegando- y sala despues. Tres tiempos: el roce del canto del sello al
    entrar en contacto, el apoyo con todo el cuerpo, y la chapa lejana que
    responde desde el fondo del recinto cuarenta y cinco milisegundos despues.

    Esa tercera capa retrasada es la que dice que el sitio es grande y que la
    decision se oyo en algun lugar que no se ve. Es el unico gesto con reverbe
    generosa -0.40 de mezcla- porque es el unico que se puede permitir ocupar
    medio segundo sin cansar: suena tres o cuatro veces por sesion.
    """
    canto = pa_m(cargar("roceCorto_000"), 2_000.0, 2)
    canto = recortar(canto, 0.0, 0.030) * 0.30
    apoyo = altura(cargar("posar_001"), 0.58)
    apoyo = realzar(apoyo, 150.0, 0.9, 1.7)
    apoyo = realzar(apoyo, 1_100.0, 0.8, 1.25)
    apoyo = pa_m(apoyo, 80.0, 2)
    peso = altura(cargar("impactSoft_heavy_001"), 0.5)
    peso = pb_m(peso, 320.0, 4) * 0.55
    chapa = altura(cargar("impactPlate_medium_001"), 0.40)
    chapa = pb_m(chapa, 1_700.0, 4)
    chapa = retrasar(chapa, 0.045) * 0.30
    grave = _red(0.34, ((1, 0.5), (2, 0.18)), 0.13) * 0.09
    x = apilar((canto, 0.8), (retrasar(apoyo, 0.022), 1.0),
               (retrasar(peso, 0.024), 0.7), (chapa, 0.8), (grave, 0.9))
    return _gesto(x, 0.520, 0.40, ataque=6.0, pico=0.72, techo=5_200.0,
                  cierre=0.14)


def ui_volver() -> np.ndarray:
    """Retroceder.

    Un ROCE AL REVES: crece y se detiene, en vez de golpear y caer. Es el unico
    gesto de la familia cuya envolvente va hacia arriba, y por eso se lee de
    inmediato como "hacia atras" sin necesidad de ser mas grave que confirmar
    ni de imitarlo.

    Aca estaba el peor error de la version anterior: volver era literalmente
    confirmar una quinta mas abajo, con el mismo archivo de origen. El
    parentesco era tan estrecho que sonaba a error de programacion. Ahora son
    gestos opuestos que comparten unicamente la sala.

    La hoja de papel despegandose del tablon y volviendo a apoyarse. Termina en
    un apoyo blando, sin cola: retroceder no resuena.
    """
    roce = altura(cargar("roceLargo_000"), 0.78)
    roce = pa_m(pb_m(roce, 5_500.0), 600.0, 2)
    roce = recortar(roce, 0.0, 0.175)
    # Envolvente creciente: la que hace que el gesto vaya hacia atras.
    roce *= np.linspace(0.10, 1.0, len(roce)) ** 1.25
    apoyo = altura(cargar("posar_000"), 0.72)
    apoyo = pb_m(pa_m(apoyo, 180.0, 2), 2_600.0)
    hueco = altura(cargar("impactSoft_medium_000"), 0.85)
    hueco = pb_m(hueco, 900.0) * 0.32
    x = apilar((roce, 0.62), (retrasar(apoyo, 0.176), 1.0),
               (retrasar(hueco, 0.178), 0.6))
    return _gesto(x, 0.300, 0.20, ataque=8.0, pico=0.46, techo=6_000.0,
                  cierre=0.09)


def ui_abrir() -> np.ndarray:
    """Abrir el aviso.

    Una SUCCION: la energia esta al final. Al invertir una grabacion, la cola
    de resonancia pasa a ir hacia adelante y el golpe queda al final, asi que
    el sonido entra en vez de salir. Es exactamente la sensacion de un recinto
    que se abre y se llena de aire.

    Esta idea ya estaba en la version anterior y era la unica de las ocho que
    tenia forma propia -234 ms de ataque frente a 6-19 del resto-, asi que se
    conserva y se le agrega lo que le faltaba: aire con grano en la banda alta,
    que es lo que convierte una chapa invertida en una puerta que se abre.
    El golpe final se recorta casi entero para que no haya impacto, solo la
    llegada.
    """
    chapa = invertir(cargar("impactPlate_light_002"))
    chapa = altura(chapa, 0.48)
    chapa = pb_m(chapa, 2_400.0, 4)
    chapa = recortar(chapa, 0.0, 0.32)
    aire = invertir(cola(cargar("footstep_concrete_002"), 0.010))
    aire = altura(aire, 0.5) * 0.40
    # El grano invertido en la banda alta: el aire que entra por el hueco.
    grano = invertir(pa_m(cargar("roceLargo_000"), 2_500.0, 2))
    grano = pb_m(grano, 9_500.0)
    grano = recortar(grano, 0.0, 0.30) * 0.22
    x = apilar((chapa, 1.0), (aire, 0.6), (grano, 0.8))
    return _gesto(x, 0.360, 0.34, ataque=16.0, pico=0.47, techo=9_500.0,
                  cierre=0.10)


def ui_cerrar() -> np.ndarray:
    """Cerrar el aviso. El reverso de abrir: el aire se va.

    Dos tiempos, en el orden contrario al de abrir: primero la chapa pesada
    que cae, despues el sello de aire que la sigue. Termina mas grave de lo
    que empieza -es el unico gesto que baja de altura mientras suena- y eso es
    lo que lo cierra.
    """
    chapa = altura(cargar("impactPlate_heavy_000"), 0.44)
    chapa = pb_m(chapa, 1_800.0, 4)
    chapa = realzar(chapa, 190.0, 0.9, 1.35)
    sello = pb_m(pa_m(cargar("roceSuave_000"), 800.0, 2), 4_200.0)
    sello = recortar(sello, 0.0, 0.11)
    sello *= np.linspace(1.0, 0.15, len(sello)) ** 1.1
    fondo = altura(cargar("impactSoft_heavy_001"), 0.46)
    fondo = pb_m(fondo, 260.0, 4) * 0.42
    x = apilar((chapa, 1.0), (retrasar(sello, 0.055), 0.42),
               (retrasar(fondo, 0.030), 0.7))
    return _gesto(x, 0.420, 0.30, ataque=9.0, pico=0.56, corte_bajo=70.0,
                  techo=4_400.0, cierre=0.13)


def ui_negado() -> np.ndarray:
    """Accion invalida.

    Sin pitido, sin nota triste, sin nada que parezca un error de sistema
    operativo. Un golpe sordo sobre algo que no cede, y despues nada.

    Dos decisiones sobre el silencio, que es lo que hace funcionar este gesto:

    La primera es que se paso de dos golpes a uno. Dos golpes son un patron, y
    un patron se vuelve un tic despues de la quinta vez que se pulsa algo
    bloqueado. Uno solo, seguido de nada, dice lo mismo y no se gasta.

    La segunda es que es el UNICO de los ocho sin sala: mezcla cero. Los otros
    siete devuelven el recinto; este no devuelve nada. La informacion esta ahi,
    en que el sitio no responde. Que rompa la regla de familia es el sentido de
    la regla: el gesto que dice que no es el que no pertenece.
    """
    seco = altura(cargar("impactoSordo_000"), 0.74)
    seco = pa_m(pb_m(seco, 1_000.0, 4), 105.0, 2)
    seco = recortar(seco, 0.0, 0.115)
    # Un roce mate encima: "no" tiene que oirse tambien en un portatil, y para
    # eso hace falta algo por encima de 500 Hz aunque sea sin ningun brillo.
    mate = pb_m(pa_m(altura(cargar("roceTela_000"), 0.7), 520.0, 2), 2_100.0)
    mate = recortar(mate, 0.0, 0.06)
    x = apilar((seco, 1.0), (mate, 0.42))
    # Sala cero y cierre corto: despues del golpe no queda nada.
    return _gesto(x, 0.240, 0.0, ataque=7.0, pico=0.40, techo=1_900.0,
                  cierre=0.06)


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


def base_nivel2(dur: float = 23.0) -> np.ndarray:
    """Nivel 2, pasillos de servicio.

    Estrecho y caliente. Aca si hay maquinas: agua corriendo dentro de las
    canerias, vapor escapandose en algun lado y el retumbe de una caldera que
    no se ve. Las resonancias son de tubo, agudas y con Q alto.
    """
    t = tiempo(dur)

    # La caldera. El ruido marron ya cae 6 dB por octava por si mismo, asi que
    # filtrarlo a 90 Hz y multiplicarlo por seis dejaba una cama con el CIEN
    # POR CIENTO de su energia por debajo de 60 Hz: en unos auriculares
    # normales el nivel 2 no se oia, y lo poco que se oia era el retumbe. Se
    # le sube el corte y se le quita la ganancia bruta.
    caldera = pasabajos(marron(dur), 150.0, 3) * 1.6 * deriva(dur, 0.023, 0.35)
    # Y se le quita el subgrave que solo come margen: por debajo de 35 Hz no
    # hay informacion, hay desplazamiento de cono.
    caldera = pasaaltos(caldera, 35.0, 2)

    # Agua dentro del cano: ruido de banda estrecha con resonancias afinadas.
    agua = pasabanda(rosa(dur), 260.0, 1_100.0, 2)
    agua = resonar(agua, 320.0, 26.0, 0.8)
    agua = resonar(agua, 487.0, 30.0, 0.6)
    agua *= 0.5 * deriva(dur, 0.05, 0.45)

    # Vapor: constante, pero con techo bajo para que no raspe.
    vapor = pasabanda(rosa(dur), 2_200.0, 5_500.0, 2) * 0.28 * deriva(dur, 0.09, 0.50)

    # El reparto tambien estaba mal: el agua y el vapor son lo que hace que
    # esto suene a pasillo de servicio, y estaban veinte decibelios por debajo
    # de una caldera que no aportaba mas que retumbe.
    x = caldera * 0.30 + agua * 0.55 + vapor * 0.32
    return reverberar(x, SALAS["servicio"], 0.30)


def base_nivel3(dur: float = 31.0) -> np.ndarray:
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


def base_nivel4(dur: float = 26.0) -> np.ndarray:
    """Nivel 4, la sala de piedra.

    La otra cara del mod: aca no hay instalacion electrica, hay FUEGO. La cama
    base es el aire caliente de una sala grande de piedra -un tiro suave, como
    el de una chimenea- mas el modo grave de la boveda. No hay zumbido de 50 Hz
    de ningun tubo: lo que sostiene el sitio es el volumen de aire tibio y la
    piedra devolviendolo todo con una cola media y oscura.
    """
    t = tiempo(dur)

    # Tiro de aire caliente: ruido marron ancho, modulado lento como una
    # corriente que sube por la nave. Es el "cuarto tono" de la sala.
    tiro = pasabajos(marron(dur), 200.0, 3) * 4.2 * deriva(dur, 0.021, 0.30)

    # Modo propio de la boveda: una nave de piedra tiene su nota, mas alta que
    # la del deposito de hormigon porque es mas corta.
    modo = np.sin(2 * np.pi * 57.0 * t) * 0.09 * deriva(dur, 0.013, 0.55)
    modo += np.sin(2 * np.pi * 86.0 * t + 0.6) * 0.045 * deriva(dur, 0.019, 0.60)

    # Un siseo grave y lejano: las brasas respirando, sin transitorios todavia
    # (los chasquidos van en la capa de caracter). Banda baja-media.
    brasa = pasabanda(rosa(dur), 240.0, 1_100.0, 2) * 0.20 * deriva(dur, 0.047, 0.45)

    x = tiro * 0.11 + modo * 0.9 + brasa * 0.06
    return reverberar(x, SALAS["sala_piedra"], 0.38)


def base_nivel5(dur: float = 22.0) -> np.ndarray:
    """Nivel 5, la biblioteca.

    El sitio mas quieto. No hay maquinas ni fuego: hay una sala grande llena de
    papel que se traga el sonido. La base es un room tone muy bajo -el silencio
    tiene su propio color- con el rumor apenas audible de una calle o un patio
    detras del ventanal, muy filtrado, que nunca termina de estar.
    """
    t = tiempo(dur)

    # Room tone: ruido rosa muy filtrado y bajo. El silencio de una sala real
    # no es cero, es esto.
    cuarto = pasabajos(rosa(dur), 400.0, 2) * 0.9 * deriva(dur, 0.017, 0.20)

    # La calle detras del ventanal: banda media-baja, lejanisima y sorda, como
    # a traves de un vidrio grueso. Sube y baja como el trafico que no se ve.
    calle = pasabanda(rosa(dur), 120.0, 800.0, 2) * 0.6
    calle *= 0.4 + 0.6 * np.clip(np.sin(2 * np.pi * 0.02 * t) * 0.5 + 0.5, 0.0, None)
    calle = pasabajos(calle, 600.0, 2)

    x = cuarto * 0.09 + calle * 0.05
    return reverberar(x, SALAS["biblioteca"], 0.18)


def base_nivel6(dur: float = 25.0) -> np.ndarray:
    """Nivel 6, el invernadero.

    Una nave grande de vidrio. La base es el aire moviendose bajo la cristalera
    -una corriente ancha y suave- y el ping ocasional del vidrio dilatandose con
    la temperatura. Nada electrico: es un sitio de aire, agua y luz.
    """
    t = tiempo(dur)

    # Corriente de aire bajo el vidrio: ruido de banda ancha, medio, respirando.
    aire = pasabanda(rosa(dur), 200.0, 2_400.0, 2) * 0.7 * deriva(dur, 0.023, 0.45)

    # Volumen del recinto: un grave suave, no tan hondo como el deposito.
    volumen = pasabajos(marron(dur), 160.0, 3) * 2.6 * deriva(dur, 0.017, 0.30)

    x = aire * 0.06 + volumen * 0.06
    return reverberar(x, SALAS["invernadero"], 0.34)


def base_nivel7(dur: float = 24.0) -> np.ndarray:
    """Nivel 7, las catacumbas.

    Piedra fria y aire quieto bajo tierra. La base es un grave muy hondo -la
    masa de tierra alrededor- y un soplo minimo de aire que recorre el tunel. No
    hay nada calido ni electrico: es el sonido de estar debajo de todo.
    """
    t = tiempo(dur)

    # La masa de tierra: subgrave apenas por encima del infrasonido, estable.
    masa = pasabajos(marron(dur), 90.0, 3) * 4.0 * deriva(dur, 0.011, 0.30)
    masa = pasaaltos(masa, 32.0, 2)

    # Corriente que recorre el tunel: banda estrecha baja-media, muy sorda.
    corriente = pasabanda(rosa(dur), 150.0, 700.0, 2) * 0.4 * deriva(dur, 0.03, 0.5)

    x = masa * 0.09 + corriente * 0.05
    return reverberar(x, SALAS["catacumbas"], 0.30)


def base_nivel8(dur: float = 27.0) -> np.ndarray:
    """Nivel 8, la cisterna.

    Un volumen de aire enorme sobre agua quieta. La base es el grave hondo del
    espacio -mas grande que el deposito- y la masa de agua moviendose apenas
    contra las columnas. Todo pasado por la cola larguisima del aljibe: el sitio
    suena tan grande que un solo sonido tarda en morir.
    """
    t = tiempo(dur)

    # El volumen del aljibe: grave profundo y estable.
    volumen = pasabajos(marron(dur), 120.0, 3) * 4.4 * deriva(dur, 0.013, 0.25)
    volumen = pasaaltos(volumen, 30.0, 2)

    # La masa de agua contra la piedra: ondulacion lentisima, grave.
    ola = 0.6 + 0.4 * (np.sin(2 * np.pi * 0.045 * t) * 0.6 + np.sin(2 * np.pi * 0.031 * t + 1.5) * 0.4)
    agua = pasabanda(rosa(dur), 90.0, 500.0, 2) * np.clip(ola, 0.0, None) * 0.7

    x = volumen * 0.08 + agua * 0.06
    return reverberar(x, SALAS["cisterna"], 0.50)


def base_nivel9(dur: float = 25.0) -> np.ndarray:
    """Nivel 9, el salon del trono.

    Una nave alta y en ruinas, abierta al cielo por los boquetes del techo. La
    base es el viento entrando por esos huecos -una corriente ancha, con silbido
    grave- sobre el volumen de aire de la sala. Es un sitio grande y muerto que
    respira por sus heridas.
    """
    t = tiempo(dur)

    # Viento por los boquetes: banda media-baja modulada, con un silbido tenue.
    viento = pasabanda(rosa(dur), 180.0, 1_600.0, 2) * 0.6
    viento *= 0.45 + 0.55 * np.clip(np.sin(2 * np.pi * 0.035 * t) * 0.6
                                    + np.sin(2 * np.pi * 0.021 * t + 1.0) * 0.4 + 0.3, 0.0, None)

    # Volumen de la sala: grave estable.
    volumen = pasabajos(marron(dur), 150.0, 3) * 3.0 * deriva(dur, 0.015, 0.28)

    x = viento * 0.055 + volumen * 0.06
    return reverberar(x, SALAS["trono"], 0.44)


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


def caracter_nivel2(dur: float = 31.0) -> np.ndarray:
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


def caracter_nivel4(dur: float = 39.0) -> np.ndarray:
    """Nivel 4. El fuego, siempre.

    Es la capa que hace que la sala sea la sala: el crepitar continuo de las
    antorchas y el candil. No es un bucle de fogata cercana -eso cansa-, sino
    fuego repartido y lejano, muchos focos chicos alrededor: chasquidos secos
    de madera y resina sueltos, sin periodo, sobre un lecho de siseo de llama.
    """
    t = tiempo(dur)

    # El lecho de llama: siseo de banda media, modulado rapido y suave, como el
    # temblor del fuego. Nunca del todo estable.
    llama = pasabanda(rosa(dur), 500.0, 3_800.0, 2) * 0.34
    llama *= 0.70 + 0.30 * deriva(dur, 0.9, 1.0)

    # Chasquidos de la madera y la resina: transitorios cortos y secos,
    # repartidos sin periodo. Son la firma del fuego. Cada uno es un golpecito
    # de banda ancha con caida muy rapida.
    chas = np.zeros(len(t))
    for _ in range(70):
        pos = muestras(RNG.uniform(0.2, dur - 0.3))
        largo = muestras(RNG.uniform(0.006, 0.045))
        if pos + largo > len(t):
            continue
        tc = np.arange(largo) / SR
        c = pasabanda(RNG.normal(0, 1, largo), 900.0, 6_500.0, 2)
        c *= np.exp(-tc / RNG.uniform(0.004, 0.016)) * RNG.uniform(0.10, 0.55)
        chas[pos:pos + largo] += c

    # Un chisporroteo mas grave y ocasional: la brasa que se asienta.
    for _ in range(10):
        pos = muestras(RNG.uniform(0.5, dur - 0.6))
        largo = muestras(RNG.uniform(0.05, 0.20))
        if pos + largo > len(t):
            continue
        tc = np.arange(largo) / SR
        b = pasabanda(RNG.normal(0, 1, largo), 200.0, 1_400.0, 2)
        b *= np.exp(-tc / RNG.uniform(0.03, 0.08)) * RNG.uniform(0.10, 0.30)
        chas[pos:pos + largo] += b

    x = llama * 0.070 + chas * 0.42
    return reverberar(x, SALAS["sala_piedra"], 0.44)


def caracter_nivel5(dur: float = 33.0) -> np.ndarray:
    """Nivel 5. La madera y el papel de la biblioteca, respirando.

    No es una fuente continua fuerte: es el mueble viejo que se acomoda -tics de
    madera secos y espaciados- y el zumbido finisimo de un tubo o un filamento
    de las lamparas de mesa. Casi nada, que es el punto: hay que aguzar el oido
    para notar que la sala esta viva, y esa atencion es la incomodidad.
    """
    t = tiempo(dur)

    # Zumbido finisimo de las lamparas: una senoidal muy baja y estable.
    zumbido = np.sin(2 * np.pi * 120.0 * t) * 0.02 * deriva(dur, 0.05, 0.4)
    zumbido += pasabanda(rosa(dur), 3_000.0, 6_000.0, 2) * 0.05 * deriva(dur, 0.2, 0.5)

    # Tics de madera: transitorios secos, cortos, muy espaciados.
    tics = np.zeros(len(t))
    for _ in range(18):
        pos = muestras(RNG.uniform(0.3, dur - 0.3))
        largo = min(muestras(RNG.uniform(0.004, 0.02)), len(t) - pos)
        if largo <= 0:
            continue
        tc = np.arange(largo) / SR
        c = pasabanda(RNG.normal(0, 1, largo), 400.0, 3_000.0, 2)
        c *= np.exp(-tc / RNG.uniform(0.003, 0.010)) * RNG.uniform(0.08, 0.28)
        tics[pos:pos + largo] += c

    x = zumbido * 0.5 + tics * 0.30
    return reverberar(x, SALAS["biblioteca"], 0.22)


def caracter_nivel6(dur: float = 35.0) -> np.ndarray:
    """Nivel 6. El agua y las hojas del invernadero, siempre.

    Lo que se mueve aca es la humedad: condensacion goteando del vidrio a las
    hojas y de las hojas al suelo, y el roce constante y suave del follaje con la
    corriente. Es una cama viva y verde, sin nada metalico ni electrico.
    """
    t = tiempo(dur)

    # Follaje: roce ancho y suave, banda media-alta, modulado como el viento
    # entre las hojas.
    follaje = pasabanda(rosa(dur), 800.0, 4_500.0, 2) * 0.30
    follaje *= 0.55 + 0.45 * deriva(dur, 0.7, 1.0)

    # Goteo de condensacion: gotas sueltas, sin periodo, cada una con su ping.
    goteo = np.zeros(len(t))
    for _ in range(30):
        pos = muestras(RNG.uniform(0.2, dur - 0.3))
        largo = min(muestras(RNG.uniform(0.03, 0.12)), len(t) - pos)
        if largo <= 0:
            continue
        tc = np.arange(largo) / SR
        f = RNG.uniform(900.0, 2_400.0)
        g = np.sin(2 * np.pi * f * tc * (1 + 0.8 * tc / (largo / SR))) * np.exp(-tc / 0.03)
        goteo[pos:pos + largo] += g * RNG.uniform(0.06, 0.20)

    x = follaje * 0.055 + goteo * 0.30
    return reverberar(x, SALAS["invernadero"], 0.40)


def caracter_nivel7(dur: float = 37.0) -> np.ndarray:
    """Nivel 7. El agua de las catacumbas, cayendo en la piedra.

    Lo unico que se mueve aca es el agua: gotas que caen de la boveda a charcos
    en el suelo, con el eco cercano del tunel. Cada gota es un plink hueco, muy
    espaciadas, con una cola corta y oscura. Entre gota y gota, el silencio de
    la piedra.
    """
    t = tiempo(dur)

    # Gotas en charco: plinks huecos, graves, sin periodo.
    gotas = np.zeros(len(t))
    for _ in range(26):
        pos = muestras(RNG.uniform(0.3, dur - 0.4))
        largo = min(muestras(RNG.uniform(0.05, 0.18)), len(t) - pos)
        if largo <= 0:
            continue
        tc = np.arange(largo) / SR
        f = RNG.uniform(400.0, 1_100.0)
        g = np.sin(2 * np.pi * f * tc * (1 + 1.2 * np.exp(-tc / 0.02))) * np.exp(-tc / 0.05)
        gotas[pos:pos + largo] += g * RNG.uniform(0.10, 0.30)

    # Un roce grave de piedra muy de fondo, casi constante.
    roce = pasabanda(rosa(dur), 100.0, 500.0, 2) * 0.10 * deriva(dur, 0.04, 0.6)

    x = gotas * 0.28 + roce * 0.05
    return reverberar(x, SALAS["catacumbas"], 0.42)


def caracter_nivel8(dur: float = 41.0) -> np.ndarray:
    """Nivel 8. El agua de la cisterna, siempre, con su eco larguisimo.

    Gotas que caen de la boveda al agua, cada una con una cola enorme -el
    sonido mas caracteristico de un aljibe-, y el lametazo lento del agua contra
    las columnas. Es la capa que hace el sitio: sin ella es una cueva seca.
    """
    t = tiempo(dur)

    # Gotas al agua, muy espaciadas, cada una con su plink que la sala alarga.
    gotas = np.zeros(len(t))
    for _ in range(22):
        pos = muestras(RNG.uniform(0.3, dur - 0.4))
        largo = min(muestras(RNG.uniform(0.06, 0.20)), len(t) - pos)
        if largo <= 0:
            continue
        tc = np.arange(largo) / SR
        f = RNG.uniform(500.0, 1_300.0)
        g = np.sin(2 * np.pi * f * tc * (1 + 1.5 * np.exp(-tc / 0.02))) * np.exp(-tc / 0.06)
        gotas[pos:pos + largo] += g * RNG.uniform(0.12, 0.32)

    # Lametazo del agua contra la piedra: banda baja, lento.
    lame = pasabanda(rosa(dur), 120.0, 600.0, 2)
    lame *= np.clip(np.sin(2 * np.pi * 0.08 * t) * 0.5 + 0.5, 0.0, None) * 0.5

    x = gotas * 0.30 + lame * 0.06
    return reverberar(x, SALAS["cisterna"], 0.56)


def caracter_nivel9(dur: float = 38.0) -> np.ndarray:
    """Nivel 9. Las ruinas del salon del trono, moviendose con el viento.

    Lo que se oye aca es la tela y la piedra suelta: los estandartes rotos
    ondeando -un flap grave y espaciado- y el tintineo de cascotes menudos que
    el viento mueve. Nada de agua ni fuego: solo el edificio deshaciendose muy
    despacio, con la cola grande de la sala.
    """
    t = tiempo(dur)

    # Ondeo de los estandartes: golpes graves de tela, espaciados, sin periodo.
    flap = np.zeros(len(t))
    for _ in range(16):
        pos = muestras(RNG.uniform(0.3, dur - 0.5))
        largo = min(muestras(RNG.uniform(0.08, 0.25)), len(t) - pos)
        if largo <= 0:
            continue
        tc = np.arange(largo) / SR
        f = pasabanda(RNG.normal(0, 1, largo), 120.0, 900.0, 2)
        f *= np.sin(np.pi * tc / (largo / SR)) ** 2 * RNG.uniform(0.10, 0.28)
        flap[pos:pos + largo] += f

    # Cascotes: tics agudos y menudos, muy espaciados.
    cascotes = np.zeros(len(t))
    for _ in range(20):
        pos = muestras(RNG.uniform(0.2, dur - 0.2))
        largo = min(muestras(RNG.uniform(0.004, 0.016)), len(t) - pos)
        if largo <= 0:
            continue
        tc = np.arange(largo) / SR
        c = pasabanda(RNG.normal(0, 1, largo), 1_500.0, 5_000.0, 2)
        c *= np.exp(-tc / 0.006) * RNG.uniform(0.06, 0.18)
        cascotes[pos:pos + largo] += c

    x = flap * 0.28 + cascotes * 0.22
    return reverberar(x, SALAS["trono"], 0.52)



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


def actividad_nivel2(dur: float = 49.0) -> np.ndarray:
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


def actividad_nivel4(dur: float = 57.0) -> np.ndarray:
    """Nivel 4. La sala de piedra, donde el edificio es antiguo y cruje.

    Casi siempre en silencio, con sucesos lejanos que dicen que la sala es
    grande y vieja: una viga de madera asentandose, el eco de una puerta
    pesada en otra nave, la cadena del candil moviendose sola, un desprendimiento
    de piedra menuda. Todo con la cola de la sala puesta.
    """
    x = np.zeros(muestras(dur))

    # Viga de madera de la boveda asentandose: grave, larga, inquietante.
    viga = _lejos(cargar("impactWood_light_002"), 1.6, 900.0, 130.0)
    for s, g in ((7.3, 0.20), (31.8, 0.24), (50.1, 0.16)):
        _sembrar(x, viga, s, g)

    # Puerta pesada en otra nave: madera y hierro, muy lejos.
    puerta = _lejos(cargar("impactPlank_medium_000"), 2.1, 620.0, 90.0)
    _sembrar(x, puerta, 19.4, 0.22)
    _sembrar(x, puerta, 44.7, 0.18)

    # La cadena del candil, moviendose sola: metal agudo, corto.
    cadena = _lejos(cargar("impactMetal_light_003"), 1.2, 3_200.0, 380.0)
    _sembrar(x, cadena, 13.6, 0.13)
    _sembrar(x, cadena, 38.9, 0.15)

    # Piedra menuda cayendo del techo: chasquido seco y disperso.
    piedra = _lejos(cargar("impactGeneric_light_000"), 1.0, 2_400.0, 300.0)
    _sembrar(x, piedra, 26.2, 0.12)

    return reverberar(x, SALAS["sala_piedra"], 0.70)


def actividad_nivel5(dur: float = 51.0) -> np.ndarray:
    """Nivel 5. La biblioteca, donde lo unico que pasa es que algo cae.

    Sucesos raros y sordos, casi todos de papel y madera: un libro que se cae
    de una balda en otro pasillo, una silla que se corre sola, el crujido de una
    escalera de mano. La sala se traga la cola, asi que cada suceso es seco y se
    acaba enseguida -y por eso inquieta: no hay eco que lo explique-.
    """
    x = np.zeros(muestras(dur))

    # Un libro cayendo al piso: golpe sordo de papel y tapa.
    libro = _lejos(cargar("impactSoft_medium_000"), 0.6, 1_400.0, 210.0)
    for s, g in ((9.1, 0.22), (30.7, 0.26), (46.3, 0.18)):
        _sembrar(x, libro, s, g)

    # Una silla que se corre: madera arrastrada, corta.
    silla = _lejos(cargar("impactWood_light_000"), 1.0, 1_100.0, 160.0)
    _sembrar(x, silla, 18.5, 0.20)
    _sembrar(x, silla, 40.2, 0.16)

    # La escalera de mano de los estantes crujiendo.
    escalera = _lejos(cargar("impactPlank_medium_000"), 1.3, 900.0, 140.0)
    _sembrar(x, escalera, 24.8, 0.15)

    return reverberar(x, SALAS["biblioteca"], 0.40)


def actividad_nivel6(dur: float = 53.0) -> np.ndarray:
    """Nivel 6. El invernadero, donde el vidrio y las plantas se mueven solos.

    Sucesos verdes y de vidrio: un panel que cruje al dilatarse, una maceta de
    barro asentandose, algo cayendo entre el follaje. La sala brillante alarga la
    cola de todo, asi que suena mas grande de lo que uno esperaria de un jardin.
    """
    x = np.zeros(muestras(dur))

    # Panel de vidrio dilatandose: un cruji-ping agudo con cola.
    panel = _lejos(cargar("impactGlass_light_003"), 0.7, 5_000.0, 520.0)
    for s, g in ((8.4, 0.18), (28.9, 0.22), (48.1, 0.15)):
        _sembrar(x, panel, s, g)

    # Maceta de barro asentandose: golpe sordo, terroso.
    maceta = _lejos(cargar("impactSoft_heavy_001"), 1.1, 900.0, 150.0)
    _sembrar(x, maceta, 17.6, 0.20)
    _sembrar(x, maceta, 39.3, 0.16)

    # Algo cayendo en el follaje: hojas y una rama.
    follaje = _lejos(cargar("impactWood_light_002"), 1.4, 1_600.0, 240.0)
    _sembrar(x, follaje, 23.7, 0.14)

    return reverberar(x, SALAS["invernadero"], 0.62)


def actividad_nivel7(dur: float = 55.0) -> np.ndarray:
    """Nivel 7. Las catacumbas, donde la piedra se mueve muy de vez en cuando.

    Sucesos sordos y graves: un bloque de mamposteria asentandose, algo pesado
    cayendo en un nicho lejano, el eco de una piedra rodando en otro corredor.
    La sala estrecha devuelve todo con reflexiones cercanas y una cola oscura.
    """
    x = np.zeros(muestras(dur))

    bloque = _lejos(cargar("impactoSordo_000"), 0.9, 700.0, 110.0)
    for s, g in ((10.2, 0.22), (33.4, 0.26), (49.6, 0.18)):
        _sembrar(x, bloque, s, g)

    caida = _lejos(cargar("impactMetal_heavy_000"), 1.8, 500.0, 80.0)
    _sembrar(x, caida, 21.1, 0.18)
    _sembrar(x, caida, 43.0, 0.14)

    rueda = _lejos(cargar("impactGeneric_light_000"), 1.2, 1_400.0, 200.0)
    _sembrar(x, rueda, 28.5, 0.13)

    return reverberar(x, SALAS["catacumbas"], 0.68)


def actividad_nivel8(dur: float = 59.0) -> np.ndarray:
    """Nivel 8. La cisterna, donde cada suceso tarda cinco segundos en morir.

    Casi vacio y con la cola mas larga de todas: algo cayendo al agua en otra
    nave, una columna asentandose, el chapoteo de algo que se movio bajo el
    agua. La sala hace todo el trabajo; se pone MENOS de lo que uno pondria.
    """
    x = np.zeros(muestras(dur))

    # Algo entrando al agua, lejos: un chof grave y ancho.
    chof = _lejos(cargar("impactSoft_heavy_001"), 0.9, 700.0, 120.0)
    for s, g in ((11.7, 0.20), (37.2, 0.24), (54.0, 0.16)):
        _sembrar(x, chof, s, g)

    # Una columna o un sillar bajo el agua asentandose: grave, con cola.
    sillar = _lejos(cargar("impactoSordo_000"), 1.1, 500.0, 80.0)
    _sembrar(x, sillar, 22.4, 0.18)
    _sembrar(x, sillar, 46.8, 0.14)

    # Chapoteo metalico lejano: la pasarela, quiza.
    metal = _lejos(cargar("impactMetal_light_000"), 1.5, 1_800.0, 240.0)
    _sembrar(x, metal, 30.1, 0.12)

    return reverberar(x, SALAS["cisterna"], 0.80)


def actividad_nivel9(dur: float = 56.0) -> np.ndarray:
    """Nivel 9. El salon del trono, donde la ruina se derrumba de a poco.

    Sucesos de piedra grande y madera vieja: un cascote que cae de lo alto, una
    viga del techo que cede, el eco de una puerta enorme en otra ala. La sala
    alta y seca alarga todo casi como la cisterna, pero con mas aire.
    """
    x = np.zeros(muestras(dur))

    cascote = _lejos(cargar("impactPlate_heavy_000"), 1.0, 900.0, 140.0)
    for s, g in ((9.8, 0.22), (34.1, 0.26), (52.0, 0.16)):
        _sembrar(x, cascote, s, g)

    viga = _lejos(cargar("impactPlank_medium_000"), 1.6, 600.0, 90.0)
    _sembrar(x, viga, 20.6, 0.20)
    _sembrar(x, viga, 45.3, 0.15)

    puerta = _lejos(cargar("impactMetal_heavy_000"), 2.0, 500.0, 70.0)
    _sembrar(x, puerta, 28.9, 0.14)

    return reverberar(x, SALAS["trono"], 0.74)


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


def ev_nivel4_antorcha() -> np.ndarray:
    """Una antorcha prendiendo con fuerza: la resina que se enciende de golpe.

    Un 'fum' grave de aire encendiendose, seguido de un crepitar mas vivo que
    se calma. Es el suceso mas caracteristico de la sala de fuego.
    """
    dur = 2.6
    n = muestras(dur)
    t = tiempo(dur)
    # El golpe de aire al prender: soplo grave con ataque suave.
    fum = pasabanda(RNG.normal(0, 1, n), 90.0, 700.0, 2) * envolvente(n, 0.02, 0.5, 2.5)
    # El crepitar que sigue: chasquidos rapidos que se apagan.
    crepita = np.zeros(n)
    for _ in range(26):
        pos = muestras(RNG.uniform(0.05, 1.8))
        largo = min(muestras(RNG.uniform(0.006, 0.03)), n - pos)
        if largo <= 0:
            continue
        tc = np.arange(largo) / SR
        c = pasabanda(RNG.normal(0, 1, largo), 1_200.0, 6_000.0, 2)
        c *= np.exp(-tc / 0.010) * RNG.uniform(0.15, 0.5) * (1.0 - pos / n)
        crepita[pos:pos + largo] += c
    x = (fum * 0.5 + crepita * 0.5) * 0.34
    return rampa(reverberar(x, SALAS["sala_piedra"], 0.42), 0.02, 0.55)


def ev_nivel4_cadena() -> np.ndarray:
    """El candil de rueda meciendose: la cadena cede un eslabon.

    Un tintineo metalico grave con dos o tres golpes de eslabon y una cola de
    metal grande resonando. Dice que algo movio el candil.
    """
    dur = 3.2
    n = muestras(dur)
    t = tiempo(dur)
    x = np.zeros(n)
    fundamental = RNG.uniform(150.0, 210.0)
    for golpe, g in ((0.0, 1.0), (0.14, 0.6), (0.33, 0.35)):
        pos = muestras(golpe)
        if pos >= n:
            continue
        tc = np.arange(n - pos) / SR
        eslabon = pasabanda(RNG.normal(0, 1, n - pos), 800.0, 5_000.0, 2)
        eslabon *= np.exp(-tc / 0.05) * 0.4 * g
        for k, a in ((1, 1.0), (2.7, 0.4), (5.1, 0.2)):
            eslabon += np.sin(2 * np.pi * fundamental * k * tc) * np.exp(-tc / (0.6 / k)) * 0.12 * g
        x[pos:] += eslabon
    x = pasabajos(x, 5_500.0, 2) * 0.34
    return rampa(reverberar(x, SALAS["sala_piedra"], 0.55), 0.002, 0.75)


def ev_nivel4_piedra() -> np.ndarray:
    """Un bloque de piedra asentandose en la boveda. Grave, seco, con eco.

    La sala es vieja y pesada. Cada tanto algo de mamposteria se acomoda: un
    crujido grave de roca contra roca, y la nave entera devolviendolo.
    """
    dur = 4.2
    n = muestras(dur)
    t = tiempo(dur)
    roce = pasabanda(RNG.normal(0, 1, n), 120.0, 900.0, 2) * envolvente(n, 0.008, 0.30, 3.0)
    cuerpo = np.sin(2 * np.pi * 46.0 * t) * np.exp(-t / 0.5) * 0.4
    cuerpo += np.sin(2 * np.pi * 71.0 * t + 0.5) * np.exp(-t / 0.35) * 0.2
    x = pasabajos(roce * 0.6 + cuerpo, 1_600.0, 2) * 0.32
    return rampa(reverberar(x, SALAS["sala_piedra"], 0.72), 0.01, 0.95)


def ev_nivel5_libro() -> np.ndarray:
    """Un libro que se cae de un estante. Golpe de tapa y aleteo de paginas."""
    dur = 1.8
    n = muestras(dur)
    tapa = pasabanda(RNG.normal(0, 1, n), 200.0, 1_600.0, 2) * envolvente(n, 0.002, 0.08, 4.0)
    # Aleteo: dos o tres golpecitos de papel al abrirse en el aire.
    aleteo = np.zeros(n)
    for pos_s in (0.03, 0.11, 0.19):
        pos = muestras(pos_s)
        largo = min(muestras(0.05), n - pos)
        if largo <= 0:
            continue
        tc = np.arange(largo) / SR
        a = pasabanda(RNG.normal(0, 1, largo), 1_500.0, 5_000.0, 2)
        a *= np.exp(-tc / 0.02) * RNG.uniform(0.15, 0.3)
        aleteo[pos:pos + largo] += a
    x = (tapa * 0.6 + aleteo * 0.5) * 0.32
    return rampa(reverberar(x, SALAS["biblioteca"], 0.30), 0.002, 0.40)


def ev_nivel5_susurro() -> np.ndarray:
    """Un roce largo entre estantes: como paginas pasando, o algo que pasa.

    El suceso mas inquietante del nivel: podria ser el aire moviendo un libro
    abierto, o podria no serlo. Banda alta y suave, sin transitorio.
    """
    dur = 3.4
    n = muestras(dur)
    t = tiempo(dur)
    roce = pasabanda(rosa(dur), 2_000.0, 6_500.0, 2)
    sobre = np.sin(np.pi * np.clip(t / dur, 0, 1)) ** 2
    # Modulacion como de paginas sucesivas.
    roce *= 0.5 + 0.5 * np.clip(np.sin(2 * np.pi * 2.5 * t), 0, None)
    x = roce * sobre * 0.16
    return rampa(reverberar(x, SALAS["biblioteca"], 0.28), 0.05, 0.60)


def ev_nivel5_reloj() -> np.ndarray:
    """El pendulo de un reloj de pared, unos pocos golpes y para.

    Grave, de madera, con una cola muy corta. Da una cadencia que arranca y se
    interrumpe: un reloj al que se le acaba la cuerda.
    """
    dur = 3.6
    n = muestras(dur)
    x = np.zeros(n)
    for k, g in enumerate((1.0, 0.9, 0.8, 0.55)):
        pos = muestras(0.05 + k * 0.85)
        if pos >= n:
            break
        largo = min(muestras(0.12), n - pos)
        tc = np.arange(largo) / SR
        tic = pasabanda(RNG.normal(0, 1, largo), 300.0, 1_800.0, 2)
        tic *= np.exp(-tc / 0.03) * 0.4 * g
        tic += np.sin(2 * np.pi * 90.0 * tc) * np.exp(-tc / 0.05) * 0.15 * g
        x[pos:pos + largo] += tic
    x = pasabajos(x, 2_500.0, 2) * 0.34
    return rampa(reverberar(x, SALAS["biblioteca"], 0.30), 0.002, 0.55)


def ev_nivel6_vidrio() -> np.ndarray:
    """Un panel de vidrio de la cristalera crujiendo y asentandose.

    Un chirrido agudo de vidrio contra el marco de hierro, con una cola larga y
    brillante que la sala de cristal alarga.
    """
    dur = 3.0
    n = muestras(dur)
    t = tiempo(dur)
    chirrido = pasabanda(RNG.normal(0, 1, n), 2_000.0, 6_500.0, 2) * envolvente(n, 0.02, 0.4, 2.0)
    # Un par de resonancias vidriosas.
    for frec in (1_840.0, 2_730.0, 4_100.0):
        chirrido += np.sin(2 * np.pi * frec * t) * np.exp(-t / RNG.uniform(0.2, 0.5)) * 0.10
    x = pasaaltos(chirrido, 900.0, 2) * 0.30
    return rampa(reverberar(x, SALAS["invernadero"], 0.55), 0.02, 0.85)


def ev_nivel6_gota() -> np.ndarray:
    """Una gota grande de condensacion cayendo del vidrio a un charco."""
    dur = 2.2
    n = muestras(dur)
    t = tiempo(dur)
    # El impacto en el agua: pitch que sube rapido (la burbuja cerrando).
    f = 700.0
    plop = np.sin(2 * np.pi * f * t * (1 + 3.0 * np.exp(-t / 0.02))) * np.exp(-t / 0.05)
    salpica = pasabanda(RNG.normal(0, 1, n), 1_500.0, 5_000.0, 2) * envolvente(n, 0.001, 0.03, 6.0)
    x = (plop * 0.6 + salpica * 0.3) * 0.30
    return rampa(reverberar(x, SALAS["invernadero"], 0.60), 0.002, 0.75)


def ev_nivel6_hojas() -> np.ndarray:
    """Un movimiento largo entre las hojas: algo se abre paso, o el viento.

    Roce ancho de follaje que crece y se apaga. Como el susurro de la
    biblioteca, deja la duda de si fue la corriente o algo mas.
    """
    dur = 3.6
    n = muestras(dur)
    t = tiempo(dur)
    roce = pasabanda(rosa(dur), 700.0, 4_000.0, 2)
    sobre = np.sin(np.pi * np.clip(t / dur, 0, 1)) ** 2
    roce *= 0.4 + 0.6 * np.clip(np.sin(2 * np.pi * 1.3 * t + np.sin(2 * np.pi * 0.4 * t)), 0, None)
    x = roce * sobre * 0.17
    return rampa(reverberar(x, SALAS["invernadero"], 0.45), 0.05, 0.70)


def ev_nivel7_gota() -> np.ndarray:
    """Una gota grande cayendo en un charco del tunel. Plink hueco y grave."""
    dur = 2.4
    n = muestras(dur)
    t = tiempo(dur)
    f = 520.0
    plink = np.sin(2 * np.pi * f * t * (1 + 2.0 * np.exp(-t / 0.03))) * np.exp(-t / 0.06)
    x = plink * 0.32
    return rampa(reverberar(x, SALAS["catacumbas"], 0.55), 0.002, 0.80)


def ev_nivel7_piedra() -> np.ndarray:
    """Un bloque de piedra rodando y asentandose en otro corredor. Grave, seco."""
    dur = 3.8
    n = muestras(dur)
    t = tiempo(dur)
    roce = pasabanda(RNG.normal(0, 1, n), 90.0, 700.0, 2) * envolvente(n, 0.01, 0.35, 3.0)
    cuerpo = np.sin(2 * np.pi * 40.0 * t) * np.exp(-t / 0.5) * 0.4
    x = pasabajos(roce * 0.6 + cuerpo, 1_000.0, 2) * 0.30
    return rampa(reverberar(x, SALAS["catacumbas"], 0.72), 0.01, 0.95)


def ev_nivel7_viento() -> np.ndarray:
    """Una corriente de aire recorriendo el tunel: un lamento grave que sube y baja.

    El suceso mas inquietante del nivel. Banda baja-media, larga, sin ataque, con
    una modulacion de altura que casi parece una voz sin llegar a serlo.
    """
    dur = 4.6
    n = muestras(dur)
    t = tiempo(dur)
    base = pasabanda(rosa(dur), 120.0, 900.0, 2)
    # Modulacion como un lamento: la banda se estrecha y se mueve.
    lam = resonar(base, 220.0 + 80.0 * np.sin(2 * np.pi * 0.25 * t).mean(), 18.0, 1.0)
    sobre = np.sin(np.pi * np.clip(t / dur, 0, 1)) ** 2
    x = lam * sobre * 0.16
    return rampa(reverberar(x, SALAS["catacumbas"], 0.60), 0.10, 1.10)


def ev_nivel8_gota() -> np.ndarray:
    """Una gota grande al agua del aljibe. Plink hueco con una cola larguisima."""
    dur = 4.0
    n = muestras(dur)
    t = tiempo(dur)
    f = 640.0
    plink = np.sin(2 * np.pi * f * t * (1 + 2.2 * np.exp(-t / 0.03))) * np.exp(-t / 0.07)
    x = plink * 0.32
    return rampa(reverberar(x, SALAS["cisterna"], 0.66), 0.002, 1.30)


def ev_nivel8_chapoteo() -> np.ndarray:
    """Algo moviendose bajo el agua, lejos. Un remolino grave que sube y baja."""
    dur = 4.4
    n = muestras(dur)
    t = tiempo(dur)
    agua = pasabanda(rosa(dur), 150.0, 1_100.0, 2)
    sobre = np.sin(np.pi * np.clip(t / dur, 0, 1)) ** 2
    agua *= 0.4 + 0.6 * np.clip(np.sin(2 * np.pi * 0.8 * t), 0, None)
    x = agua * sobre * 0.16
    return rampa(reverberar(x, SALAS["cisterna"], 0.62), 0.05, 1.10)


def ev_nivel8_columna() -> np.ndarray:
    """Una columna asentandose bajo el agua. Grave, sordo, con eco enorme."""
    dur = 5.0
    n = muestras(dur)
    t = tiempo(dur)
    golpe = pasabajos(RNG.normal(0, 1, n), 300.0, 2) * envolvente(n, 0.004, 0.14, 4.0)
    cuerpo = np.sin(2 * np.pi * 38.0 * t) * np.exp(-t / 0.6) * 0.4
    x = pasabajos(golpe * 0.6 + cuerpo, 900.0, 2) * 0.30
    return rampa(reverberar(x, SALAS["cisterna"], 0.78), 0.01, 1.40)


def ev_nivel9_cascote() -> np.ndarray:
    """Un cascote de piedra cayendo de lo alto al suelo del salon. Seco, con eco."""
    dur = 3.4
    n = muestras(dur)
    golpe = pasabanda(RNG.normal(0, 1, n), 200.0, 2_200.0, 2) * envolvente(n, 0.001, 0.06, 5.0)
    x = pasabajos(golpe, 2_500.0, 2) * 0.32
    return rampa(reverberar(x, SALAS["trono"], 0.70), 0.002, 1.10)


def ev_nivel9_estandarte() -> np.ndarray:
    """Un estandarte roto ondeando con una racha. Flap grave de tela, con eco."""
    dur = 3.0
    n = muestras(dur)
    t = tiempo(dur)
    tela = pasabanda(rosa(dur), 130.0, 1_100.0, 2)
    sobre = np.sin(np.pi * np.clip(t / dur, 0, 1)) ** 2
    tela *= 0.4 + 0.6 * np.clip(np.sin(2 * np.pi * 3.0 * t), 0, None)
    x = tela * sobre * 0.17
    return rampa(reverberar(x, SALAS["trono"], 0.55), 0.03, 0.85)


def ev_nivel9_puerta() -> np.ndarray:
    """El eco de una puerta enorme cerrandose en otra ala. Grave, lejano, largo."""
    dur = 4.4
    n = muestras(dur)
    t = tiempo(dur)
    golpe = pasabajos(RNG.normal(0, 1, n), 260.0, 2) * envolvente(n, 0.004, 0.12, 4.0)
    cuerpo = np.sin(2 * np.pi * 44.0 * t) * np.exp(-t / 0.5) * 0.35
    x = pasabajos(golpe * 0.6 + cuerpo, 800.0, 2) * 0.28
    return rampa(reverberar(x, SALAS["trono"], 0.78), 0.01, 1.30)


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
    "ambiente/nivel4": lambda: bucle_suave(base_nivel4(), 5.0),
    "ambiente/nivel5": lambda: bucle_suave(base_nivel5(), 5.0),
    "ambiente/nivel6": lambda: bucle_suave(base_nivel6(), 5.0),
    "ambiente/nivel7": lambda: bucle_suave(base_nivel7(), 5.0),
    "ambiente/nivel8": lambda: bucle_suave(base_nivel8(), 6.0),
    "ambiente/nivel9": lambda: bucle_suave(base_nivel9(), 5.0),

    # Capa de caracter, tambien en bucle. Duraciones primas con las bases para
    # que las dos camas de cada nivel no vuelvan a alinearse en toda la sesion.
    "caracter/nivel0": lambda: bucle_suave(caracter_nivel0(), 5.0),
    "caracter/nivel1": lambda: bucle_suave(caracter_nivel1(), 6.0),
    "caracter/nivel2": lambda: bucle_suave(caracter_nivel2(), 4.0),
    "caracter/nivel3": lambda: bucle_suave(caracter_nivel3(), 7.0),
    "caracter/nivel4": lambda: bucle_suave(caracter_nivel4(), 5.0),
    "caracter/nivel5": lambda: bucle_suave(caracter_nivel5(), 4.0),
    "caracter/nivel6": lambda: bucle_suave(caracter_nivel6(), 5.0),
    "caracter/nivel7": lambda: bucle_suave(caracter_nivel7(), 4.0),
    "caracter/nivel8": lambda: bucle_suave(caracter_nivel8(), 6.0),
    "caracter/nivel9": lambda: bucle_suave(caracter_nivel9(), 5.0),

    # Capa de actividad: bucles largos, casi vacios, con sucesos lejanos. El
    # cruce es corto porque casi toda la junta cae sobre silencio.
    "actividad/nivel0": lambda: bucle_suave(actividad_nivel0(), 2.0),
    "actividad/nivel1": lambda: bucle_suave(actividad_nivel1(), 2.0),
    "actividad/nivel2": lambda: bucle_suave(actividad_nivel2(), 2.0),
    "actividad/nivel3": lambda: bucle_suave(actividad_nivel3(), 2.0),
    "actividad/nivel4": lambda: bucle_suave(actividad_nivel4(), 2.0),
    "actividad/nivel5": lambda: bucle_suave(actividad_nivel5(), 2.0),
    "actividad/nivel6": lambda: bucle_suave(actividad_nivel6(), 2.0),
    "actividad/nivel7": lambda: bucle_suave(actividad_nivel7(), 2.0),
    "actividad/nivel8": lambda: bucle_suave(actividad_nivel8(), 2.0),
    "actividad/nivel9": lambda: bucle_suave(actividad_nivel9(), 2.0),

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
    "evento/nivel4_antorcha": ev_nivel4_antorcha,
    "evento/nivel4_cadena": ev_nivel4_cadena,
    "evento/nivel4_piedra": ev_nivel4_piedra,
    "evento/nivel5_libro": ev_nivel5_libro,
    "evento/nivel5_susurro": ev_nivel5_susurro,
    "evento/nivel5_reloj": ev_nivel5_reloj,
    "evento/nivel6_vidrio": ev_nivel6_vidrio,
    "evento/nivel6_gota": ev_nivel6_gota,
    "evento/nivel6_hojas": ev_nivel6_hojas,
    "evento/nivel7_gota": ev_nivel7_gota,
    "evento/nivel7_piedra": ev_nivel7_piedra,
    "evento/nivel7_viento": ev_nivel7_viento,
    "evento/nivel8_gota": ev_nivel8_gota,
    "evento/nivel8_chapoteo": ev_nivel8_chapoteo,
    "evento/nivel8_columna": ev_nivel8_columna,
    "evento/nivel9_cascote": ev_nivel9_cascote,
    "evento/nivel9_estandarte": ev_nivel9_estandarte,
    "evento/nivel9_puerta": ev_nivel9_puerta,

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
