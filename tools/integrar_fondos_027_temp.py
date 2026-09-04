from pathlib import Path
import json
import re

root = Path('.')
nivel = root / 'src/main/java/com/santipdr/jobsmenu/client/scene/Nivel.java'
gradle = root / 'build.gradle'
props = root / 'gradle.properties'
planta = root / 'src/main/java/com/santipdr/jobsmenu/client/scene/planta/PlantaImagen.java'
es = root / 'src/main/resources/assets/jobsmenu/lang/es_es.json'
en = root / 'src/main/resources/assets/jobsmenu/lang/en_us.json'
verifier = root / 'tools/verificar_fondos.py'

# Version.
p = props.read_text(encoding='utf-8')
p = re.sub(r'(?m)^mod_version\s*=\s*.*$', 'mod_version=0.27.0', p)
props.write_text(p, encoding='utf-8')

# Gradle: extract the versioned source pack directly into runtime resources.
g = gradle.read_text(encoding='utf-8')
marker = "    // NOTA SOBRE LA MUSICA\n"
block = """    // Fondos 18-31 entregados por el usuario. Se versionan como un unico ZIP
    // para conservar los binarios sin conversiones intermedias del conector.
    // processResources los extrae como imagenes normales dentro del JAR.
    inputs.file('assets/backgrounds/niveles18-31.zip')
    from(zipTree(file('assets/backgrounds/niveles18-31.zip'))) {
        into 'assets/jobsmenu/textures/backgrounds'
    }

"""
if "niveles18-31.zip" not in g:
    if marker not in g:
        raise SystemExit('No se encontro el marcador de processResources')
    g = g.replace(marker, block + marker, 1)
gradle.write_text(g, encoding='utf-8')

# Catalogue 18-31. Java remains ASCII-only.
n = nivel.read_text(encoding='utf-8')
n = n.replace('Los niveles 0-9 conservan plantas procedurales. Los niveles 10-17 usan',
              'Los niveles 0-9 conservan plantas procedurales. Los niveles 10-31 usan')
entries = r'''

            // Nuevos fondos de imagen entregados por el usuario (0.27.0).
            new Nivel("nivel18", new PlantaImagen("nivel18.jpg", 18),
                    0xFF43505A, 0xFF1A2229, 0xFF0B0F13,
                    0xFF202930, 0xFF10161B, 0xFF080B0E,
                    0xFF33414A, 0xFF151D22,
                    0xFF1A252C, 0xFFA6D7D9, 0xFF030608,
                    0.500F, 0.500F, 0.220F, 0.220F, 0.160F, 0.140F,
                    0.20F, 0.24F),

            new Nivel("nivel19", new PlantaImagen("nivel19.jpg", 19),
                    0xFF33435F, 0xFF11182B, 0xFF080C16,
                    0xFF172038, 0xFF0B1020, 0xFF050810,
                    0xFF263552, 0xFF11192A,
                    0xFF17253E, 0xFF9AC6FF, 0xFF02040A,
                    0.500F, 0.505F, 0.235F, 0.235F, 0.165F, 0.145F,
                    0.24F, 0.20F),

            new Nivel("nivel20", new PlantaImagen("nivel20.jpg", 20),
                    0xFF909AA4, 0xFF3B444D, 0xFF171B1F,
                    0xFF323A41, 0xFF181D22, 0xFF0A0D10,
                    0xFF68747D, 0xFF2A3137,
                    0xFF4F5B64, 0xFFDDE8ED, 0xFF040608,
                    0.500F, 0.490F, 0.220F, 0.220F, 0.155F, 0.135F,
                    0.22F, 0.12F),

            new Nivel("nivel21", new PlantaImagen("nivel21.jpg", 21),
                    0xFF315B58, 0xFF12302E, 0xFF071918,
                    0xFF173B38, 0xFF0C211F, 0xFF06110F,
                    0xFF25504C, 0xFF102C29,
                    0xFF153A36, 0xFF78F5D8, 0xFF020807,
                    0.500F, 0.500F, 0.230F, 0.230F, 0.160F, 0.140F,
                    0.30F, 0.18F),

            new Nivel("nivel22", new PlantaImagen("nivel22.jpg", 22),
                    0xFF715740, 0xFF3B2C20, 0xFF1B120C,
                    0xFF463426, 0xFF281B12, 0xFF120B07,
                    0xFF5A4432, 0xFF302217,
                    0xFF382719, 0xFFE6B77B, 0xFF070403,
                    0.500F, 0.505F, 0.235F, 0.235F, 0.170F, 0.145F,
                    0.16F, 0.46F),

            new Nivel("nivel23", new PlantaImagen("nivel23.jpg", 23),
                    0xFF79604A, 0xFF423020, 0xFF20150C,
                    0xFF4B3828, 0xFF2A1E13, 0xFF140D08,
                    0xFF604A35, 0xFF332419,
                    0xFF3E2C1D, 0xFFF0C38A, 0xFF080503,
                    0.500F, 0.500F, 0.235F, 0.235F, 0.170F, 0.145F,
                    0.18F, 0.42F),

            new Nivel("nivel24", new PlantaImagen("nivel24.jpg", 24),
                    0xFF6B7074, 0xFF30363B, 0xFF15191C,
                    0xFF3A4044, 0xFF202428, 0xFF0D1012,
                    0xFF545B60, 0xFF292F33,
                    0xFF353C41, 0xFFCFD6D8, 0xFF040506,
                    0.500F, 0.500F, 0.220F, 0.220F, 0.155F, 0.135F,
                    0.20F, 0.22F),

            new Nivel("nivel25", new PlantaImagen("nivel25.jpg", 25),
                    0xFF403B58, 0xFF191628, 0xFF0B0913,
                    0xFF211D31, 0xFF110E1C, 0xFF07050C,
                    0xFF332E49, 0xFF171426,
                    0xFF211B35, 0xFFB5A8FF, 0xFF020106,
                    0.500F, 0.500F, 0.230F, 0.230F, 0.165F, 0.145F,
                    0.22F, 0.16F),

            new Nivel("nivel26", new PlantaImagen("nivel26.jpg", 26),
                    0xFF415171, 0xFF18233B, 0xFF0A1020,
                    0xFF202D47, 0xFF10192B, 0xFF070C16,
                    0xFF324462, 0xFF16223A,
                    0xFF1D2F50, 0xFFA7C8FF, 0xFF02050C,
                    0.500F, 0.500F, 0.235F, 0.235F, 0.165F, 0.145F,
                    0.26F, 0.18F),

            new Nivel("nivel27", new PlantaImagen("nivel27.jpg", 27),
                    0xFF493E59, 0xFF21182D, 0xFF0D0914,
                    0xFF2A2036, 0xFF15101D, 0xFF08060D,
                    0xFF3B314B, 0xFF1C1628,
                    0xFF28203A, 0xFFC4ACED, 0xFF030207,
                    0.500F, 0.500F, 0.230F, 0.230F, 0.165F, 0.145F,
                    0.20F, 0.24F),

            new Nivel("nivel28", new PlantaImagen("nivel28.jpg", 28),
                    0xFF756B58, 0xFF3B3428, 0xFF1A1711,
                    0xFF453D30, 0xFF262118, 0xFF100D09,
                    0xFF5B5140, 0xFF302A20,
                    0xFF3B3326, 0xFFE5D3A5, 0xFF060504,
                    0.500F, 0.500F, 0.225F, 0.225F, 0.160F, 0.140F,
                    0.16F, 0.30F),

            new Nivel("nivel29", new PlantaImagen("nivel29.jpg", 29),
                    0xFF6E2C2A, 0xFF2C1112, 0xFF150708,
                    0xFF351718, 0xFF1B0B0C, 0xFF0C0506,
                    0xFF552120, 0xFF261012,
                    0xFF371516, 0xFFE78878, 0xFF050202,
                    0.500F, 0.500F, 0.225F, 0.225F, 0.165F, 0.145F,
                    0.14F, 0.18F),

            new Nivel("nivel30", new PlantaImagen("nivel30.jpg", 30),
                    0xFF31585D, 0xFF123037, 0xFF07181C,
                    0xFF183A40, 0xFF0C2025, 0xFF061116,
                    0xFF274D53, 0xFF102B30,
                    0xFF17383E, 0xFF7DE2EE, 0xFF020709,
                    0.500F, 0.500F, 0.230F, 0.230F, 0.160F, 0.140F,
                    0.30F, 0.16F),

            new Nivel("nivel31", new PlantaImagen("nivel31.jpg", 31),
                    0xFF4A4D52, 0xFF202328, 0xFF0D0F12,
                    0xFF292C31, 0xFF15171B, 0xFF080A0C,
                    0xFF3B3E44, 0xFF1C1F23,
                    0xFF282B30, 0xFFC3C9CF, 0xFF030405,
                    0.500F, 0.500F, 0.220F, 0.220F, 0.155F, 0.135F,
                    0.18F, 0.18F),
'''
if 'new Nivel("nivel31"' not in n:
    anchor = '                    0.24F, 0.26F),\n    };'
    if anchor not in n:
        raise SystemExit('No se encontro cierre de nivel17')
    n = n.replace(anchor, '                    0.24F, 0.26F),' + entries + '    };', 1)
nivel.write_text(n, encoding='utf-8')

# PlantaImagen can decode JPEG through NativeImage/STB too.
pi = planta.read_text(encoding='utf-8')
pi = pi.replace('Planta para los PNG entregados por el proyecto (niveles 10-17).',
                'Planta para las imagenes entregadas por el proyecto (niveles 10-31).')
pi = pi.replace('la fuente de verdad sigue siendo el PNG real.', 'la fuente de verdad sigue siendo la imagen real.')
pi = pi.replace('Valida una vez usando el mismo decodificador PNG que usa Minecraft.',
                'Valida una vez usando el mismo decodificador de imagen que usa Minecraft.')
pi = pi.replace('PNG no decodificable', 'imagen no decodificable')
pi = pi.replace('sin animacion del PNG.', 'sin animacion de la imagen.')
planta.write_text(pi, encoding='utf-8')

es_data = json.loads(es.read_text(encoding='utf-8'))
en_data = json.loads(en.read_text(encoding='utf-8'))
names_es = [
    'NIVEL 18 · Interferencia nula', 'NIVEL 19 · Luna oscura',
    'NIVEL 20 · Integridad celeste', 'NIVEL 21 · Circuito recreativo',
    'NIVEL 22 · Cámara primitiva', 'NIVEL 23 · Pasaje de piedra',
    'NIVEL 24 · Sala inclinada', 'NIVEL 25 · Noche nula',
    'NIVEL 26 · Observatorio lunar', 'NIVEL 27 · Castillo del vacío',
    'NIVEL 28 · Depósito de pan', 'NIVEL 29 · Cámara escarlata',
    'NIVEL 30 · Circuito superior', 'NIVEL 31 · Sala de dominio'
]
names_en = [
    'LEVEL 18 · Null interference', 'LEVEL 19 · Dark moon',
    'LEVEL 20 · Heavenly integrity', 'LEVEL 21 · Circuit frolic',
    'LEVEL 22 · Primitive chamber', 'LEVEL 23 · Stone passage',
    'LEVEL 24 · Tilted room', 'LEVEL 25 · Null night',
    'LEVEL 26 · Moon observatory', 'LEVEL 27 · Void castle',
    'LEVEL 28 · Bread storage', 'LEVEL 29 · Scarlet chamber',
    'LEVEL 30 · Super circuit', 'LEVEL 31 · Dominion room'
]
notes_es = [
    ('La señal visual de este sector presenta interferencias persistentes.', 'No ajuste el monitor para compensarlas.', 'Registre cualquier cambio que continúe después de abandonar el nivel.'),
    ('La iluminación exterior permanece por debajo del valor previsto.', 'Mantenga despejado el corredor de observación.', 'No confunda sombras de la estructura con personal en tránsito.'),
    ('El sector permanece estable mientras los indicadores conserven su brillo.', 'No retire piezas de la estructura visible.', 'Informe cualquier variación de geometría al cambio de turno.'),
    ('Los conductos de señal de esta planta siguen activos.', 'No desconecte nodos sin una orden registrada.', 'Las rutas de mantenimiento deben permanecer libres.'),
    ('El inventario de piedra de este sector está cerrado.', 'No mueva objetos sin registrar origen y destino.', 'La inspección de paredes se realiza desde el pasillo marcado.'),
    ('El paso central es la única ruta autorizada durante mantenimiento.', 'Conserve libres las zonas de tránsito.', 'Informe desprendimientos antes de continuar.'),
    ('Mantenga una postura estable al recorrer esta sala.', 'No apoye equipo sobre superficies inclinadas.', 'La salida debe permanecer visible desde el punto de control.'),
    ('La visibilidad de esta planta es deliberadamente reducida.', 'Utilice las referencias del corredor y no marcas improvisadas.', 'Registre luces ausentes antes de abandonar el sector.'),
    ('El observatorio permanece en modo de baja actividad.', 'No modifique la orientación de los equipos.', 'Las lecturas externas se archivan al cierre del turno.'),
    ('El acceso a las estructuras superiores no está autorizado.', 'Mantenga cerradas las rutas laterales.', 'Toda inspección del fondo debe realizarse desde la zona asignada.'),
    ('El material almacenado en esta planta forma parte del inventario.', 'No retire unidades sin autorización.', 'Mantenga las zonas de carga despejadas.'),
    ('Este sector opera bajo protocolo de contención reforzada.', 'No cruce marcas de seguridad sin una orden activa.', 'Informe cualquier señal roja fuera de las zonas designadas.'),
    ('Los paneles del circuito están bajo mantenimiento programado.', 'No reinicie módulos que ya estén en diagnóstico.', 'Mantenga libres las rutas de cableado.'),
    ('Toda actividad de esta sala queda registrada.', 'No altere mapas, insignias ni material de planificación.', 'El cierre del sector requiere confirmación del responsable de turno.')
]
notes_en = [
    ('Visual signal in this sector shows persistent interference.', 'Do not adjust the monitor to compensate for it.', 'Log any change that continues after leaving the level.'),
    ('Exterior lighting remains below the expected value.', 'Keep the observation corridor clear.', 'Do not confuse structural shadows with personnel in transit.'),
    ('The sector remains stable while its indicators keep their current brightness.', 'Do not remove pieces from the visible structure.', 'Report any geometry change at shift handover.'),
    ('Signal conduits on this floor remain active.', 'Do not disconnect nodes without a recorded order.', 'Maintenance routes must remain clear.'),
    ('The stone inventory in this sector is closed.', 'Do not move objects without recording origin and destination.', 'Wall inspection is performed from the marked corridor.'),
    ('The central passage is the only authorized route during maintenance.', 'Keep transit areas clear.', 'Report falling debris before proceeding.'),
    ('Keep a stable posture while crossing this room.', 'Do not place equipment on tilted surfaces.', 'The exit must remain visible from the control point.'),
    ('Visibility on this floor is deliberately reduced.', 'Use corridor references rather than improvised marks.', 'Log missing lights before leaving the sector.'),
    ('The observatory remains in low-activity mode.', 'Do not alter equipment orientation.', 'External readings are archived at the end of the shift.'),
    ('Access to upper structures is not authorized.', 'Keep lateral routes closed.', 'Any inspection of the far end must be performed from the assigned zone.'),
    ('Material stored on this floor is part of inventory.', 'Do not remove units without authorization.', 'Keep loading areas clear.'),
    ('This sector operates under reinforced containment procedure.', 'Do not cross safety markings without an active order.', 'Report any red signal outside designated areas.'),
    ('Circuit panels are under scheduled maintenance.', 'Do not restart modules already in diagnostics.', 'Keep cable routes clear.'),
    ('All activity in this room is logged.', 'Do not alter maps, insignia, or planning material.', 'Closing the sector requires confirmation from the shift supervisor.')
]
for offset, i in enumerate(range(18, 32)):
    es_data[f'jobsmenu.nivel{i}.nombre'] = names_es[offset]
    en_data[f'jobsmenu.nivel{i}.nombre'] = names_en[offset]
    for j in range(3):
        es_data[f'jobsmenu.nivel{i}.nota{j}'] = notes_es[offset][j]
        en_data[f'jobsmenu.nivel{i}.nota{j}'] = notes_en[offset][j]
es.write_text(json.dumps(es_data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
en.write_text(json.dumps(en_data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

verifier.write_text(r'''#!/usr/bin/env python3
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
''', encoding='utf-8')

# Docs.
changelog = root / 'CHANGELOG.md'
c = changelog.read_text(encoding='utf-8')
section = """## 0.27.0

- Se agregan 14 fondos nuevos como niveles 18-31 a partir del ZIP entregado por el usuario.
- Los nuevos fondos se conservan como JPEG 960x540 y se extraen al JAR durante `processResources`.
- Se amplian catalogo, traducciones y verificador de fondos; los niveles 10-17 no cambian.

"""
if '## 0.27.0' not in c: c = section + c
changelog.write_text(c, encoding='utf-8')

doc = root / 'docs/FONDOS_18_31.md'
doc.write_text("""# Fondos 18-31 (0.27.0)

Los 14 JPG del ZIP del usuario se integran como niveles nuevos, en el orden original:

18. cool_glitchy_null_by_autumn
19. dark_moon_1
20. heavenlytegrity
21. circuit_frolic
22. caveman
23. caveboy
24. bad_posture
25. a_very_null_night
26. moonboy
27. void_castle
28. tbread
29. scarlet_king
30. new_super_circuit_bros_3d
31. world_domination

Fuente versionada: `assets/backgrounds/niveles18-31.zip`. El build extrae `nivel18.jpg` a `nivel31.jpg` dentro de `assets/jobsmenu/textures/backgrounds/`. Resolucion de runtime: 960x540, 16:9. Los fondos 10-17 permanecen sin cambios.

Los nuevos fondos usan `PlantaImagen` y por ahora conservan la imagen estable; fades, blackout y transiciones globales del menu siguen funcionando por encima.
""", encoding='utf-8')

readme = root / 'README.md'
r = readme.read_text(encoding='utf-8')
if 'fondos 18-31' not in r.lower():
    r += "\n### Fondos 18-31\n\nDesde 0.27.0 el catalogo llega al nivel 31 e incluye 14 fondos adicionales entregados por el usuario. Ver `docs/FONDOS_18_31.md`.\n"
readme.write_text(r, encoding='utf-8')
