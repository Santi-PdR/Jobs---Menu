#!/usr/bin/env python3
"""Contratos vigentes: UI neutra, robustez y catalogo musical de tres pistas."""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"
RES = ROOT / "src/main/resources/assets/jobsmenu"


def fail(message: str) -> None:
    raise RuntimeError(message)


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def verify_neutral_ui() -> None:
    palette = read(JAVA / "client/ui/Paleta.java")
    required = (
        "UI_PAPEL", "UI_PAPEL_FOCO", "UI_TINTA", "UI_TINTA_TENUE",
        "UI_ACENTO", "UI_ACENTO_FUERTE", "ARCHIVO_FONDO",
        "ARCHIVO_SUPERFICIE", "ARCHIVO_SUPERFICIE_FOCO",
    )
    for name in required:
        if name not in palette:
            fail(f"Paleta.java perdio la constante neutral {name}.")

    files = (
        JAVA / "client/ui/ChromeExpediente.java",
        JAVA / "client/ui/PielVanillaJobs.java",
        JAVA / "client/ui/BotonExpediente.java",
        JAVA / "client/ui/ToggleExpediente.java",
        JAVA / "client/ui/SliderExpediente.java",
        JAVA / "client/ui/PulidoInterfazJobs.java",
        JAVA / "client/ui/TransicionInterfazJobs.java",
        JAVA / "client/screen/PantallaIdiomaJobs.java",
        JAVA / "client/screen/PantallaAjustesAviso.java",
    )
    forbidden = ("Paleta.PARED_ALTA", "Paleta.PARED", "Paleta.FLUOR")
    for path in files:
        text = read(path)
        for token in forbidden:
            if token in text:
                fail(f"{path.relative_to(ROOT)} vuelve a usar color de escena: {token}")

    skin = read(JAVA / "client/ui/PielVanillaJobs.java")
    for token in ("ARCHIVO_SUPERFICIE", "ARCHIVO_SUPERFICIE_FOCO", "esArchivoOscuro"):
        if token not in skin:
            fail(f"PielVanillaJobs perdio el contrato de superficie oscura: {token}")

    polish = read(JAVA / "client/ui/PulidoInterfazJobs.java")
    for token in ("ConfigTurno.movimientoReducido()", "ConfigTurno.bajoConsumo()", "Math.sin"):
        if token not in polish:
            fail(f"PulidoInterfazJobs perdio el pulido accesible: {token}")

    transition = read(JAVA / "client/ui/TransicionInterfazJobs.java")
    for token in ("t * t * (3.0F - 2.0F * t)", "ConfigTurno.bajoConsumo()", "Paleta.UI_PAPEL"):
        if token not in transition:
            fail(f"TransicionInterfazJobs perdio el contrato profesional: {token}")

    for filename in ("BotonExpediente.java", "ToggleExpediente.java", "SliderExpediente.java"):
        widget = read(JAVA / "client/ui" / filename)
        if "ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()" not in widget:
            fail(f"{filename} no respeta Bajo consumo al animar foco.")

    config_ui = read(JAVA / "client/screen/PantallaAjustesAviso.java")
    if "JOBS-0161" in config_ui:
        fail("PantallaAjustesAviso conserva el formulario duro JOBS-0161.")
    if "JOBS-CONFIG" not in config_ui:
        fail("PantallaAjustesAviso perdio el identificador estable JOBS-CONFIG.")


def verify_robustness() -> None:
    language = read(JAVA / "client/screen/PantallaIdiomaJobs.java")
    for token in ("reloadResourcePacks().whenComplete", "this.aplicando = false", "GLFW.GLFW_KEY_KP_ENTER"):
        if token not in language:
            fail(f"PantallaIdiomaJobs perdio robustez 0.19: {token}")

    multiplayer = read(JAVA / "client/screen/PantallaMultijugadorJobs.java")
    for token in ("int util = Math.max(1, this.panelW - margen * 2)", "ChromeExpediente.ajustar", "this.panelW < 300"):
        if token not in multiplayer:
            fail(f"PantallaMultijugadorJobs perdio responsividad 0.19: {token}")
    if "Math.max(240, this.panelW - margen * 2)" in multiplayer:
        fail("Multijugador volvio al ancho minimo que desborda ventanas angostas.")

    lifecycle = read(JAVA / "client/EscuchaCliente.java")
    fragment = "SesionMenu.cerrar();\n            return;"
    if fragment not in lifecycle:
        fail("EscuchaCliente no corta mantenimiento tras cerrar la sesion en gameplay.")
    for token in ("PlaySoundEvent", "MezclaAudio.reemplazoClickVanilla()", "evento.setSound"):
        if token not in lifecycle:
            fail(f"EscuchaCliente perdio sustitucion de click vanilla: {token}")

    mix = read(JAVA / "client/sound/MezclaAudio.java")
    if "SoundEvents.UI_BUTTON_CLICK" in mix:
        fail("MezclaAudio volvio a usar el click vanilla como fallback de interfaz.")
    for token in ("resolverPersonalizado", "tonoGesto", "reemplazoClickVanilla"):
        if token not in mix:
            fail(f"MezclaAudio perdio identidad de gestos Jobs: {token}")

    settings = read(JAVA / "client/screen/PantallaAjustesAviso.java")
    for token in ('"jobsmenu.ajustes.nivelfijo.detalle", 0, 31',
                  '"jobsmenu.ajustes.pista.detalle", 0, 3', "toggleCuatro",
                  "PantallaAjustesAviso::fijarSonidoBotones",
                  'v -> Component.literal(v + "/31")',
                  'v -> Component.literal(v + "/3")'):
        if token not in settings:
            fail(f"Ajustes perdio selector completo: {token}")

    slider = read(JAVA / "client/ui/SliderExpediente.java")
    for token in ("lecturaCorta", "this.lecturaCorta.apply(valorEntero())"):
        if token not in slider:
            fail(f"SliderExpediente perdio su lectura semantica: {token}")
    if 'Math.round(this.value * 100.0D) + "%"' in slider:
        fail("SliderExpediente volvio a mostrar porcentajes falsos en todos los controles.")

    multiplayer = read(JAVA / "client/screen/PantallaMultijugadorJobs.java")
    for token in ("conectarSeleccionado", "ConnectScreen.startConnecting(this, this.minecraft",
                  "ServerAddress.parseString(servidor.ip)"):
        if token not in multiplayer:
            fail(f"Multijugador perdio retorno contextual de conexion: {token}")



def verify_main_overlay_layout() -> None:
    main_screen = read(JAVA / "client/screen/PantallaNivel.java")
    required = (
        "ALTO_ESTADO_RESERVADO",
        "ANCHO_CREDITO_MINIMO",
        "ALTO_CREDITO_MINIMO",
        "this.compacta || this.width < ANCHO_CREDITO_MINIMO",
        "autor.getString().isBlank()",
        "int reservaEstado = ConfigTurno.mostrarEstadoInstalacion()",
        "this.height - MARGEN_ROTULO - reservaEstado - altoBloque",
    )
    for token in required:
        if token not in main_screen:
            fail(f"PantallaNivel perdio el contrato de composicion adaptativa: {token}")


def verify_music_session() -> None:
    manager = read(JAVA / "client/sound/GestorMusica.java")
    required = (
        'new Pista("absurdism", SonidosNivel.MUSICA_TEMA, "musica/defecto.ogg",',
        'new Pista("requiem", SonidosNivel.MUSICA_REQUIEM, "musica/requiem.ogg",',
        'new Pista("upon_the_hill_v2", SonidosNivel.MUSICA_UPON_HILL,',
        "adelantarPista()", "siguienteIndice(Pista[] pistas)", "tituloPistaActual()",
        "SUAVIZADO_SUBIDA", "SUAVIZADO_BAJADA", "SUAVIZADO_CROSSFADE",
        "atenderCrossfade()", "gananciaObjetivo", "canStartSilent()",
        "detenerAhora()", "cliente.getMusicManager().stopPlaying()",
        "ConfigTurno.pistaMusica()", "sincronizarSeleccion()", "indiceFijado",
        "boolean permiteRotacion",
    )
    for token in required:
        if token not in manager:
            fail(f"GestorMusica perdio el contrato musical: {token}")

    config = read(JAVA / "config/ConfigTurno.java")
    for token in ('defineInRange("pista_musica", 0, 0, 3)', "fijarPistaMusica", "pistaMusica()"):
        if token not in config:
            fail(f"ConfigTurno perdio seleccion persistente de pista: {token}")

    sounds = json.loads(read(RES / "sounds.json"))
    event = sounds.get("musica.tema")
    if not isinstance(event, dict):
        fail("sounds.json no contiene musica.tema.")
    entries = event.get("sounds", [])
    names = []
    for item in entries:
        names.append(item if isinstance(item, str) else item.get("name") if isinstance(item, dict) else None)
    if "jobsmenu:musica/defecto" not in names:
        fail("musica.tema ya no apunta al OGG usado como Absurdism.")

    ogg = RES / "sounds/musica/defecto.ogg"
    if not ogg.is_file() or ogg.stat().st_size < 64:
        fail("Falta la pista empaquetada sounds/musica/defecto.ogg.")

    music_doc = read(ROOT / "docs/musica.md")
    if "Absurdism" not in music_doc:
        fail("docs/musica.md no identifica la pista incluida como Absurdism.")
    for token in ("Absurdism", "REQUIEM", "Upon the Hill V2", "music/upon_the_hill_v2_q4.ogg"):
        if token not in music_doc:
            fail(f"docs/musica.md perdio el catalogo musical: {token}")

    for event_name, resource in (("musica.requiem", "jobsmenu:musica/requiem"),
                                 ("musica.upon_hill", "jobsmenu:musica/upon_the_hill_v2")):
        event = sounds.get(event_name)
        if not isinstance(event, dict):
            fail(f"sounds.json no contiene {event_name}.")
        names = [item if isinstance(item, str) else item.get("name") for item in event.get("sounds", [])]
        if resource not in names:
            fail(f"{event_name} no apunta a {resource}.")

    for filename in ("defecto.ogg", "requiem.ogg", "upon_the_hill_v2.ogg"):
        ogg = RES / "sounds/musica" / filename
        if not ogg.is_file() or ogg.stat().st_size < 64:
            fail(f"Falta pista empaquetada: {filename}")



def verify_ambient_catalog() -> None:
    manager = read(JAVA / "client/sound/GestorAmbiente.java")
    layer = read(JAVA / "client/sound/CapaAmbiente.java")
    for level in range(18, 32):
        if manager.count(f"case {level}:") < 3:
            fail(f"Nivel {level} no tiene las tres camas ambientales explicitas.")
        if manager.count(f"case {level} ->") < 2:
            fail(f"Nivel {level} no tiene repertorio y espera ambiental explicitos.")
        if f"case {level} -> papel" not in layer:
            fail(f"Nivel {level} no tiene balance de capas propio.")
    for token in ("tonoNivel(nivel, papel)", "private static float tonoNivel"):
        if token not in layer:
            fail(f"CapaAmbiente perdio afinacion por fondo: {token}")


def verify_audio_sources() -> None:
    for path in (ROOT / "music/REQUIEM-Forsaken-OST.ogg", ROOT / "music/upon_the_hill_v2_q4.ogg"):
        if not path.is_file() or path.stat().st_size < 64:
            fail(f"Falta fuente OGG autorizada: {path.relative_to(ROOT)}")
    legacy = (
        ROOT / ".github/workflows/integrar_pista_autorizada.yml",
        ROOT / "tools/cobalt_transport.py",
        ROOT / "tools/integrar_pista.trigger",
    )
    for path in legacy:
        if path.exists():
            fail(f"Sigue presente infraestructura de audio obsoleta: {path.relative_to(ROOT)}")


def main() -> int:
    try:
        verify_neutral_ui()
        verify_robustness()
        verify_main_overlay_layout()
        verify_music_session()
        verify_ambient_catalog()
        verify_audio_sources()
        print("UI neutra, bajo consumo y robustez 0.19: OK")
        print("Composicion adaptativa del main: OK")
        print("Reproductor musical de sesion: OK")
        print("Catalogo musical de tres pistas: OK")
        print("Identidad ambiental de 32 niveles: OK")
        print("Lecturas semanticas de sliders: OK")
        return 0
    except Exception as exc:
        print(f"ERROR catalogo musical: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
