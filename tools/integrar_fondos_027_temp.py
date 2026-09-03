#!/usr/bin/env python3
from pathlib import Path
import json, zipfile

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / 'src/main/resources/assets/jobsmenu/textures/backgrounds'
ZIP = ROOT / 'src/main/backgrounds/backgrounds_nivel18_31.zip'


def read(path):
    return (ROOT / path).read_text(encoding='utf-8')


def write(path, text):
    p = ROOT / path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(text, encoding='utf-8')


def replace_once(text, old, new, label):
    if old not in text:
        raise RuntimeError(f'anchor missing: {label}')
    return text.replace(old, new, 1)

# 1) Extraer los 14 JPG optimizados (960x540) a recursos reales del mod.
RES.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(ZIP) as z:
    names = sorted(z.namelist())
    expected = [f'nivel{i}.jpg' for i in range(18, 32)]
    if names != expected:
        raise RuntimeError(f'ZIP inesperado: {names}')
    for name in expected:
        data = z.read(name)
        if len(data) < 20_000 or data[:2] != b'\xff\xd8' or data[-2:] != b'\xff\xd9':
            raise RuntimeError(f'JPEG invalido: {name}')
        (RES / name).write_bytes(data)

# 2) Catalogo 18-31.
p = 'src/main/java/com/santipdr/jobsmenu/client/scene/Nivel.java'
s = read(p)
s = s.replace('Los niveles 0-9 conservan plantas procedurales. Los niveles 10-17 usan\n * imagenes suministradas para el proyecto y pasan por PlantaImagen, por lo que\n * participan de luz, apagones, Suspension y tratamiento ambiental.',
'''Los niveles 0-9 conservan plantas procedurales. Los niveles 10-31 usan
 * imagenes suministradas para el proyecto y pasan por PlantaImagen. Los 10-17
 * mantienen su contrato historico estatico; los 18-31 admiten solo atmosfera
 * global sutil, sin deformar ni desplazar la imagen base.''')
anchor = '''            new Nivel("nivel17", new PlantaImagen("nivel17.png", 192, 108, 17),
                    0xFF30446E, 0xFF11182D, 0xFF080B16,
                    0xFF172038, 0xFF0B1020, 0xFF050810,
                    0xFF263452, 0xFF11182A,
                    0xFF17243E, 0xFF7FB8FF, 0xFF02040A,
                    0.500F, 0.505F, 0.235F, 0.235F, 0.165F, 0.145F,
                    0.24F, 0.26F),
'''
new_levels = anchor + '''
            new Nivel("nivel18", new PlantaImagen("nivel18.jpg", 18),
                    0xFF521E28, 0xFF1B0B12, 0xFF09060A,
                    0xFF17131A, 0xFF0B090D, 0xFF050406,
                    0xFF30202A, 0xFF151018,
                    0xFF34111D, 0xFFFF6657, 0xFF020203,
                    0.500F, 0.500F, 0.235F, 0.235F, 0.165F, 0.145F,
                    0.12F, 0.16F),

            new Nivel("nivel19", new PlantaImagen("nivel19.jpg", 19),
                    0xFF46325F, 0xFF191227, 0xFF0A0711,
                    0xFF181425, 0xFF0B0912, 0xFF050309,
                    0xFF302348, 0xFF161024,
                    0xFF251634, 0xFFE9A6FF, 0xFF020106,
                    0.500F, 0.500F, 0.235F, 0.235F, 0.165F, 0.145F,
                    0.20F, 0.10F),

            new Nivel("nivel20", new PlantaImagen("nivel20.jpg", 20),
                    0xFFC8C2BA, 0xFF756E69, 0xFF373231,
                    0xFF908A83, 0xFF514C49, 0xFF252221,
                    0xFFA9A39C, 0xFF625C58,
                    0xFF9D9790, 0xFFFFFFFF, 0xFF111010,
                    0.500F, 0.500F, 0.230F, 0.230F, 0.160F, 0.140F,
                    0.22F, 0.08F),

            new Nivel("nivel21", new PlantaImagen("nivel21.jpg", 21),
                    0xFFD7B58E, 0xFF76563A, 0xFF382619,
                    0xFF8D6B4D, 0xFF4B3525, 0xFF21170F,
                    0xFFB49370, 0xFF62462F,
                    0xFF896949, 0xFFFFE0A8, 0xFF0F0A06,
                    0.500F, 0.500F, 0.230F, 0.230F, 0.165F, 0.145F,
                    0.16F, 0.18F),

            new Nivel("nivel22", new PlantaImagen("nivel22.jpg", 22),
                    0xFF736D64, 0xFF3B3733, 0xFF1A1817,
                    0xFF514C46, 0xFF292623, 0xFF11100F,
                    0xFF5D5751, 0xFF302C29,
                    0xFF403B36, 0xFFD6C7B2, 0xFF070707,
                    0.500F, 0.500F, 0.235F, 0.235F, 0.170F, 0.145F,
                    0.14F, 0.34F),

            new Nivel("nivel23", new PlantaImagen("nivel23.jpg", 23),
                    0xFF82472B, 0xFF452317, 0xFF21100B,
                    0xFF59301F, 0xFF2E1810, 0xFF130A07,
                    0xFF673923, 0xFF351D13,
                    0xFF4D291B, 0xFFFFA45B, 0xFF090402,
                    0.500F, 0.500F, 0.235F, 0.235F, 0.170F, 0.145F,
                    0.12F, 0.28F),

            new Nivel("nivel24", new PlantaImagen("nivel24.jpg", 24),
                    0xFF70191B, 0xFF310A0D, 0xFF160507,
                    0xFF351013, 0xFF190709, 0xFF0A0304,
                    0xFF4C1115, 0xFF21080B,
                    0xFF3B0B0E, 0xFFFF5038, 0xFF040101,
                    0.500F, 0.500F, 0.230F, 0.230F, 0.165F, 0.145F,
                    0.10F, 0.12F),

            new Nivel("nivel25", new PlantaImagen("nivel25.jpg", 25),
                    0xFF293657, 0xFF11172A, 0xFF070A13,
                    0xFF172038, 0xFF0B1020, 0xFF050810,
                    0xFF263653, 0xFF111929,
                    0xFF192641, 0xFFA9C6FF, 0xFF02040A,
                    0.500F, 0.500F, 0.235F, 0.235F, 0.165F, 0.145F,
                    0.24F, 0.20F),

            new Nivel("nivel26", new PlantaImagen("nivel26.jpg", 26),
                    0xFFA8B2C0, 0xFF555F6D, 0xFF252B34,
                    0xFF687484, 0xFF343C48, 0xFF171B21,
                    0xFF8793A4, 0xFF46515F,
                    0xFF657487, 0xFFE9F2FF, 0xFF080B10,
                    0.500F, 0.500F, 0.230F, 0.230F, 0.160F, 0.140F,
                    0.18F, 0.04F),

            new Nivel("nivel27", new PlantaImagen("nivel27.jpg", 27),
                    0xFF5B171B, 0xFF26090C, 0xFF110405,
                    0xFF2C0E11, 0xFF150608, 0xFF080203,
                    0xFF3C1115, 0xFF1C0709,
                    0xFF301014, 0xFFE9544D, 0xFF030101,
                    0.500F, 0.500F, 0.235F, 0.235F, 0.165F, 0.145F,
                    0.12F, 0.08F),

            new Nivel("nivel28", new PlantaImagen("nivel28.jpg", 28),
                    0xFF431318, 0xFF1B080B, 0xFF090304,
                    0xFF211014, 0xFF100608, 0xFF050203,
                    0xFF321218, 0xFF16070A,
                    0xFF291017, 0xFFFF5A4B, 0xFF020101,
                    0.500F, 0.500F, 0.230F, 0.230F, 0.165F, 0.145F,
                    0.08F, 0.06F),

            new Nivel("nivel29", new PlantaImagen("nivel29.jpg", 29),
                    0xFF5A1831, 0xFF250B19, 0xFF10050B,
                    0xFF301020, 0xFF17080F, 0xFF080306,
                    0xFF42162A, 0xFF1D0A14,
                    0xFF351125, 0xFFFF6B72, 0xFF030104,
                    0.500F, 0.500F, 0.235F, 0.235F, 0.165F, 0.145F,
                    0.14F, 0.10F),

            new Nivel("nivel30", new PlantaImagen("nivel30.jpg", 30),
                    0xFF5D1719, 0xFF27090B, 0xFF110405,
                    0xFF301013, 0xFF160608, 0xFF070203,
                    0xFF401216, 0xFF1B0709,
                    0xFF341014, 0xFFFF5945, 0xFF030101,
                    0.500F, 0.500F, 0.230F, 0.230F, 0.165F, 0.145F,
                    0.10F, 0.08F),

            new Nivel("nivel31", new PlantaImagen("nivel31.jpg", 31),
                    0xFF48131A, 0xFF1E080C, 0xFF0C0305,
                    0xFF251015, 0xFF110609, 0xFF060203,
                    0xFF34131A, 0xFF18080C,
                    0xFF2B1018, 0xFFF45B55, 0xFF020102,
                    0.500F, 0.500F, 0.235F, 0.235F, 0.165F, 0.145F,
                    0.12F, 0.08F),
'''
s = replace_once(s, anchor, new_levels, 'Nivel 17 tail')
write(p, s)

# 3) Imagenes nuevas: ambiente sutil, sin pan/zoom/deformacion.
p = 'src/main/java/com/santipdr/jobsmenu/client/scene/planta/PlantaImagen.java'
s = read(p)
s = replace_once(s, 'import com.santipdr.jobsmenu.client.ui.Paleta;\n', 'import com.santipdr.jobsmenu.client.ui.Paleta;\nimport com.santipdr.jobsmenu.config.ConfigTurno;\n', 'ConfigTurno import')
s = s.replace('Planta para los PNG entregados por el proyecto (niveles 10-17).', 'Planta para fondos de imagen entregados por el proyecto (niveles 10-31).')
s = s.replace('Desde 0.13.0 estas imagenes son deliberadamente estaticas: no hay zoom,\n * paneo, scanlines, niebla animada, flicker ni desplazamientos de color. El\n * renderer solo hace un recorte cover centrado, una integracion estatica muy\n * leve y los apagones/transiciones que pertenecen al flujo general del menu.',
'''Los fondos 10-17 conservan el contrato estatico historico. Los fondos 18-31
 * tampoco sufren zoom, paneo ni deformacion de la textura; cuando Escena viva
 * esta activa pueden recibir una capa atmosferica global muy tenue. Movimiento
 * reducido y Bajo consumo eliminan esa capa animada.''')
s = replace_once(s, '        integrarEstatico(g, w, h, nivel);\n\n        float oscuridad', '        integrarEstatico(g, w, h, nivel);\n        integrarAtmosferaNueva(g, w, h, nivel, tiempo);\n\n        float oscuridad', 'atmosfera call')
s = s.replace('registrarFallo("PNG no decodificable", error);', 'registrarFallo("imagen no decodificable", error);')
method_anchor = '    private static float limitar(float valor, float minimo, float maximo) {'
method = '''    /** Movimiento solo de overlay: la fotografia nunca cambia sus UV ni geometria. */
    private void integrarAtmosferaNueva(GuiGraphics g, int w, int h, Nivel nivel, float tiempo) {
        if (modo < 18 || !ConfigTurno.escenaViva()
                || ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()) {
            return;
        }
        float fase = 0.5F + 0.5F * (float) Math.sin(tiempo * 0.16F + modo * 0.61F);
        g.fill(0, 0, w, h, Paleta.conAlfa(nivel.niebla, 0.006F + 0.010F * fase));

        int banda = Math.max(20, w / 14);
        int recorrido = w + banda * 2;
        int x = (int) ((tiempo * 5.0F + modo * 43.0F) % recorrido) - banda;
        int x0 = Math.max(0, x);
        int x1 = Math.min(w, x + banda);
        if (x1 > x0) {
            g.fill(x0, 0, x1, h, Paleta.conAlfa(nivel.luz, 0.006F + 0.005F * fase));
        }
    }

'''
s = replace_once(s, method_anchor, method + method_anchor, 'atmosfera method')
write(p, s)

# 4) EscenaNivel da tiempo real solo a los nuevos fondos; no habilita capas procedurales.
p = 'src/main/java/com/santipdr/jobsmenu/client/scene/EscenaNivel.java'
s = read(p)
s = replace_once(s,
'''        // Los PNG 10-17 actuales, y cualquier PNG alto futuro, son material
        // fotografico estatico: escena viva nunca mueve ni pulsa su contenido.
        boolean movimiento = viva && !fondoImagen && !ConfigTurno.movimientoReducido();
        boolean bajoConsumo = viva && ConfigTurno.bajoConsumo();''',
'''        // Los 10-17 siguen completamente estaticos. Los 18-31 pueden animar
        // un overlay dentro de PlantaImagen, nunca la geometria de la fotografia.
        boolean movimiento = viva && !fondoImagen && !ConfigTurno.movimientoReducido();
        boolean bajoConsumo = viva && ConfigTurno.bajoConsumo();
        boolean atmosferaImagen = viva && fondoImagen && nivel.numero() >= 18
                && !ConfigTurno.movimientoReducido() && !bajoConsumo;''', 'Escena movimiento')
s = replace_once(s,
'        float tiempo = movimiento ? (estado.ahora() % 600_000L) / 1000.0F : 3.0F;',
'        float tiempo = (movimiento || atmosferaImagen) ? (estado.ahora() % 600_000L) / 1000.0F : 3.0F;', 'Escena tiempo')
s = s.replace('// Un PNG no recibe materiales, tratamiento, foreground, motas, presencia,\n        // eventos, respiracion, flicker ni pulido animado.', '// Un fondo de imagen no recibe materiales, foreground, motas, presencia,\n        // respiracion de camara ni flicker. 18-31 solo usan su overlay interno.')
write(p, s)

# 5) Ambientes existentes reutilizados segun cada escena nueva.
p = 'src/main/java/com/santipdr/jobsmenu/client/sound/GestorAmbiente.java'
s = read(p)
s = replace_once(s, '''            case 17:
                return SonidosNivel.AMBIENTE_NIVEL8;
            default:''', '''            case 17:
                return SonidosNivel.AMBIENTE_NIVEL8;
            case 18, 24, 28, 30:
                return SonidosNivel.AMBIENTE_NIVEL2;
            case 19, 25:
                return SonidosNivel.AMBIENTE_NIVEL8;
            case 20:
                return SonidosNivel.AMBIENTE_NIVEL5;
            case 21:
                return SonidosNivel.AMBIENTE_NIVEL6;
            case 22, 23:
                return SonidosNivel.AMBIENTE_NIVEL7;
            case 26, 27, 29, 31:
                return SonidosNivel.AMBIENTE_NIVEL9;
            default:''', 'base nuevos')
s = replace_once(s, '''            case 17:
                return SonidosNivel.CARACTER_NIVEL7;
            default:''', '''            case 17:
                return SonidosNivel.CARACTER_NIVEL7;
            case 18, 24, 28, 30, 31:
                return SonidosNivel.CARACTER_NIVEL0;
            case 19, 22, 25, 26, 29:
                return SonidosNivel.CARACTER_NIVEL7;
            case 20, 21:
                return SonidosNivel.CARACTER_NIVEL6;
            case 23:
                return SonidosNivel.CARACTER_NIVEL4;
            case 27:
                return SonidosNivel.CARACTER_NIVEL9;
            default:''', 'caracter nuevos')
s = replace_once(s, '''            case 17:
                return SonidosNivel.ACTIVIDAD_NIVEL1;
            default:''', '''            case 17:
                return SonidosNivel.ACTIVIDAD_NIVEL1;
            case 18, 24:
                return SonidosNivel.ACTIVIDAD_NIVEL1;
            case 19, 25, 26:
                return SonidosNivel.ACTIVIDAD_NIVEL1;
            case 20, 22, 23:
                return SonidosNivel.ACTIVIDAD_NIVEL7;
            case 21:
                return SonidosNivel.ACTIVIDAD_NIVEL6;
            case 27, 29, 31:
                return SonidosNivel.ACTIVIDAD_NIVEL9;
            case 28, 30:
                return SonidosNivel.ACTIVIDAD_NIVEL2;
            default:''', 'actividad nuevos')
s = replace_once(s, '''            case 17 -> REPERTORIOS[8];  // galeria azul: eco profundo y actividad remota
            default ->''', '''            case 17 -> REPERTORIOS[8];  // galeria azul: eco profundo y actividad remota
            case 18, 24, 28, 30 -> REPERTORIOS[2];
            case 19, 25 -> REPERTORIOS[8];
            case 20 -> REPERTORIOS[5];
            case 21 -> REPERTORIOS[6];
            case 22 -> REPERTORIOS[7];
            case 23 -> REPERTORIOS[4];
            case 26, 27, 29, 31 -> REPERTORIOS[9];
            default ->''', 'repertorio nuevos')
s = replace_once(s, '''            case 17 -> 1.40F;
            default ->''', '''            case 17 -> 1.40F;
            case 18, 24, 28, 30 -> 1.25F;
            case 19, 25, 26 -> 1.55F;
            case 20, 21, 22, 23 -> 1.35F;
            case 27, 29, 31 -> 1.50F;
            default ->''', 'espera nuevos')
write(p, s)

# 6) Nombres y notas localizadas.
es = {
18: ('Interferencia nula', 'La señal de este sector no conserva una forma estable. Mantenga visible el aviso.', 'Los bloques de imagen son parte del registro de cámara; no intente alinearlos.', 'Si la escena cambia de color, espere a que la identificación del Nivel siga siendo legible.'),
19: ('Luna oscura', 'La luz del cielo no corresponde a ningún horario registrado.', 'No hay acceso exterior asociado a esta vista.', 'El punto luminoso permanece fijo aunque cambie el turno.'),
20: ('Integridad celeste', 'La superficie clara está clasificada como zona de tránsito, no como salida.', 'La figura central no pertenece al personal de mantenimiento.', 'No siga las líneas oscuras fuera del área señalizada.'),
21: ('Circuito exterior', 'Los conductos exteriores continúan funcionando aunque no figuren en el plano.', 'La zona abierta sigue bajo normas de instalación.', 'Si pierde de vista el aviso, vuelva al último punto numerado.'),
22: ('Caverna del vigía', 'El marcador rojo identifica una presencia, no una ruta.', 'La roca no tiene numeración de obra ni fecha de excavación.', 'No utilice la silueta como referencia de distancia.'),
23: ('Cueva de torsión', 'Las paredes cambian con la iluminación de emergencia. No use el color como referencia.', 'Las formas del fondo no constan como maquinaria.', 'Mantenga libre el paso central aunque parezca bloqueado.'),
24: ('Corredor carmesí', 'Sector bajo protocolo de contención. Las marcas rojas no autorizan el acceso.', 'No permanezca frente a la figura durante una transición de turno.', 'Los Executores tienen prioridad absoluta en este corredor.'),
25: ('Noche nula', 'Las estrellas son visibles desde un sector sin acceso exterior. El informe sigue abierto.', 'No abandone el sendero por una luz que no figure en el plano.', 'La silueta del fondo no cuenta como personal asignado.'),
26: ('Superficie lunar', 'No hay esclusa registrada para esta superficie.', 'La Tierra visible no confirma una salida al exterior.', 'Conserve el número de Nivel como única referencia de ubicación.'),
27: ('Castillo del vacío', 'La estructura del horizonte no pertenece al inventario de la instalación.', 'No se ha autorizado ninguna ruta hacia las torres.', 'La iluminación roja indica contención, no una vía de evacuación.'),
28: ('Núcleo fragmentado', 'El núcleo registra actividad sin equipo conectado.', 'No toque bloques que aparezcan fuera del marco de mantenimiento.', 'Los pulsos visuales no sustituyen las señales del turno.'),
29: ('Corte escarlata', 'Zona reservada para contención de alta prioridad.', 'La presencia mayor no tiene expediente de visitante.', 'Mantenga distancia de cualquier figura que permanezca fuera de escala.'),
30: ('Circuito de contención', 'Cada tramo repite la misma identificación. Verifique el Nivel antes de avanzar.', 'Las estructuras rojas forman parte del cierre de seguridad.', 'No siga conexiones que desaparezcan durante un corte de luz.'),
31: ('Dominio exterior', 'Los mapas exteriores terminan en este sector.', 'La figura del horizonte no responde a señales de la instalación.', 'Si el cielo cambia, permanezca junto al aviso hasta el siguiente turno.'),
}
en = {
18: ('Null interference', 'The signal in this sector does not keep a stable shape. Keep the notice in sight.', 'The image blocks belong to the camera record; do not try to align them.', 'If the scene changes colour, wait until the Level identification is readable again.'),
19: ('Dark moon', 'The light in the sky does not match any registered schedule.', 'No exterior access is associated with this view.', 'The bright point remains fixed even when the shift changes.'),
20: ('Heavenly integrity', 'The bright surface is classified as a transit area, not an exit.', 'The central figure is not maintenance staff.', 'Do not follow the dark lines beyond the marked area.'),
21: ('Outer circuit', 'The exterior conduits remain active even though they are absent from the plan.', 'The open area is still subject to facility rules.', 'If you lose sight of the notice, return to the last numbered point.'),
22: ('Watcher cavern', 'The red marker identifies a presence, not a route.', 'The rock has no construction number or excavation date.', 'Do not use the silhouette as a distance reference.'),
23: ('Twisted cave', 'The walls change under emergency lighting. Do not use colour as a reference.', 'The shapes in the distance are not registered as machinery.', 'Keep the central passage clear even when it appears blocked.'),
24: ('Crimson corridor', 'This sector is under containment protocol. Red marks do not authorize access.', 'Do not remain in front of the figure during a shift transition.', 'Executors have absolute priority in this corridor.'),
25: ('Null night', 'Stars are visible from a sector with no exterior access. The report remains open.', 'Do not leave the path for a light that is absent from the plan.', 'The distant silhouette does not count as assigned personnel.'),
26: ('Lunar surface', 'No airlock is registered for this surface.', 'A visible Earth does not confirm an exterior exit.', 'Keep the Level number as your only location reference.'),
27: ('Void castle', 'The structure on the horizon is not part of the facility inventory.', 'No route to the towers has been authorized.', 'Red lighting indicates containment, not an evacuation path.'),
28: ('Fragmented core', 'The core reports activity with no equipment connected.', 'Do not touch blocks that appear outside the maintenance frame.', 'Visual pulses do not replace shift signals.'),
29: ('Scarlet court', 'This area is reserved for high-priority containment.', 'The larger presence has no visitor file.', 'Keep your distance from any figure that remains out of scale.'),
30: ('Containment circuit', 'Every section repeats the same identification. Verify the Level before moving.', 'The red structures are part of the security closure.', 'Do not follow connections that disappear during a power cut.'),
31: ('Outer dominion', 'Exterior maps end at this sector.', 'The figure on the horizon does not respond to facility signals.', 'If the sky changes, remain by the notice until the next shift.'),
}
for lang, data in [('es_es', es), ('en_us', en)]:
    lp = ROOT / f'src/main/resources/assets/jobsmenu/lang/{lang}.json'
    obj = json.loads(lp.read_text(encoding='utf-8'))
    prefix = 'NIVEL' if lang == 'es_es' else 'LEVEL'
    for n, vals in data.items():
        obj[f'jobsmenu.nivel{n}.nombre'] = f'{prefix} {n} · {vals[0]}'
        for j in range(3):
            obj[f'jobsmenu.nivel{n}.nota{j}'] = vals[j + 1]
    lp.write_text(json.dumps(obj, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

# 7) Verificador: conserva validacion PNG fuerte y suma JPEG 18-31.
p = 'tools/verificar_fondos.py'
s = read(p)
s = s.replace('Valida que los fondos 10-17 sean PNG realmente decodificables.', 'Valida fondos PNG 10-17 y JPEG 18-31 realmente decodificables.')
insert = '''\n\ndef dimensiones_jpeg(datos: bytes) -> tuple[int, int]:
    if len(datos) < 4 or datos[:2] != b'\\xff\\xd8':
        raise RuntimeError('firma JPEG invalida')
    pos = 2
    sof = {0xC0, 0xC1, 0xC2, 0xC3, 0xC5, 0xC6, 0xC7, 0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF}
    while pos + 4 <= len(datos):
        while pos < len(datos) and datos[pos] != 0xFF:
            pos += 1
        while pos < len(datos) and datos[pos] == 0xFF:
            pos += 1
        if pos >= len(datos):
            break
        marcador = datos[pos]
        pos += 1
        if marcador in {0xD8, 0xD9} or 0xD0 <= marcador <= 0xD7:
            continue
        if pos + 2 > len(datos):
            break
        largo = int.from_bytes(datos[pos:pos + 2], 'big')
        if largo < 2 or pos + largo > len(datos):
            raise RuntimeError('segmento JPEG truncado')
        if marcador in sof:
            if largo < 7:
                raise RuntimeError('SOF JPEG invalido')
            alto = int.from_bytes(datos[pos + 3:pos + 5], 'big')
            ancho = int.from_bytes(datos[pos + 5:pos + 7], 'big')
            return ancho, alto
        pos += largo
    raise RuntimeError('JPEG sin SOF')


def validar_jpeg(i: int) -> None:
    p = DIR / f'nivel{i}.jpg'
    if not p.is_file():
        raise RuntimeError(f'falta {p.relative_to(RAIZ)}')
    datos = p.read_bytes()
    if len(datos) < 20_000 or datos[-2:] != b'\\xff\\xd9':
        raise RuntimeError(f'nivel {i}: JPEG incompleto')
    ancho, alto = dimensiones_jpeg(datos)
    if (ancho, alto) != (960, 540):
        raise RuntimeError(f'nivel {i}: dimensiones {(ancho, alto)}, se esperaba (960, 540)')
    print(f'nivel {i}: OK JPEG {ancho}x{alto} ({len(datos)} bytes)')
'''
s = replace_once(s, '\n\ndef main() -> int:\n', insert + '\n\ndef main() -> int:\n', 'jpeg verifier funcs')
s = replace_once(s, '''        for i in range(10, 18):
            validar(i)''', '''        for i in range(10, 18):
            validar(i)
        for i in range(18, 32):
            validar_jpeg(i)''', 'verifier loops')
s = s.replace("print('Fondos 10-17: PNG/IDAT/CRC OK')", "print('Fondos 10-17 PNG y 18-31 JPEG: OK')")
write(p, s)

# 8) Version y documentacion actual.
p = 'gradle.properties'
s = read(p).replace('mod_version=0.26.0', 'mod_version=0.27.0', 1)
write(p, s)

p = 'README.md'
s = read(p)
s = s.replace('| Versión | **0.26.0** |', '| Versión | **0.27.0** |', 1)
s = s.replace('| Artefacto | **`jobsmenu-0.26.0.jar`** |', '| Artefacto | **`jobsmenu-0.27.0.jar`** |', 1)
s = s.replace('| Niveles | **18 (0–17)** |', '| Niveles | **32 (0–31)** |', 1)
section = '''## 0.27.0 · Ampliación visual 18–31\n\n- 14 fondos enviados para el proyecto pasan a ser niveles reales 18–31.\n- Se empaquetan como JPEG 960×540 optimizados, manteniendo relación 16:9 y suficiente definición para el menú.\n- Los niveles 10–17 conservan su contrato estático. Los 18–31 pueden usar una bruma/pasada de luz global muy sutil con Escena viva; Movimiento reducido y Bajo consumo la desactivan.\n- Cada nivel nuevo tiene nombre, tres notas y una mezcla ambiental reutilizada de forma temática.\n- Ningún fondo nuevo mueve, hace zoom o deforma la fotografía base.\n\n'''
s = replace_once(s, '## 0.26.0 · Catálogo musical real y control de sesión\n', section + '## 0.26.0 · Catálogo musical real y control de sesión\n', 'README section')
write(p, s)

p = 'CONTEXTO.md'
s = read(p)
s = s.replace('## 3. Estado 0.26.0', '## 3. Estado 0.27.0', 1)
s = s.replace('0.26.0 suma un catalogo musical real de tres pistas y conserva como base todo el pase visual 0.24.0.', '0.27.0 amplía el catálogo a 32 niveles. Los niveles 18-31 son fondos de imagen nuevos con nombres/notas propios y ambiente reutilizado de forma temática; 10-17 conservan su contrato estático.', 1)
s = s.replace('## 4. Fondos 10-17', '## 4. Fondos de imagen 10-31', 1)
write(p, s)

p = 'CHANGELOG.md'
s = read(p)
entry = '''## 0.27.0\n\n- Añadidos 14 niveles nuevos (18–31) desde el paquete de backgrounds suministrado.\n- Assets optimizados a JPEG 960×540 para equilibrar calidad y tamaño del JAR.\n- Nombres y tres notas localizadas por nivel en español e inglés.\n- Atmósfera de overlay sutil sólo para 18–31, respetando Movimiento reducido/Bajo consumo.\n- Mezclas ambientales existentes reasignadas temáticamente a los nuevos niveles.\n- Verificador de fondos ampliado a PNG 10–17 + JPEG 18–31.\n\n'''
if s.startswith('#'):
    idx = s.find('\n\n') + 2
    s = s[:idx] + entry + s[idx:]
else:
    s = entry + s
write(p, s)

for p in ['docs/DESPLIEGUE.md', 'docs/checklist-manual.md']:
    s = read(p).replace('0.26.0', '0.27.0').replace('jobsmenu-0.26.0.jar', 'jobsmenu-0.27.0.jar')
    write(p, s)

mapping = ['cool_glitchy_null_by_autumn','dark_moon_1','heavenlytegrity','circuit_frolic','caveman','caveboy','bad_posture','a_very_null_night','moonboy','void_castle','tbread','scarlet_king','new_super_circuit_bros_3d','world_domination']
doc = '# Fondos 0.27.0 · Niveles 18–31\n\nLos 14 archivos suministrados eran 1920×1080. Para el JAR se optimizaron a JPEG 960×540 manteniendo 16:9; no se recorta el arte fuente.\n\n'
for i, name in enumerate(mapping, 18):
    doc += f'- Nivel {i}: `{name}.jpg` → `textures/backgrounds/nivel{i}.jpg`\n'
doc += '\nLos niveles 10–17 siguen completamente estáticos. En 18–31 la única animación permitida es un overlay global tenue; la imagen base nunca recibe pan, zoom, parallax o deformación.\n'
write('docs/FONDOS_0.27.0.md', doc)

# Limpiar archivos de integración temporales; los JPG ya quedan versionados.
for path in [ZIP, ROOT / 'tools/integrar_fondos_027_temp.py', ROOT / '.github/workflows/integrar_fondos_027_temp.yml']:
    try:
        path.unlink()
    except FileNotFoundError:
        pass

print('Integracion fondos 0.27.0 completada')
