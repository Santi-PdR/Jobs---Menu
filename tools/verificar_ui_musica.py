#!/usr/bin/env python3
"""Contratos vigentes de 0.18.0: UI neutra, sesion musical y ranura OGG."""
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
        "UI_PAPEL",
        "UI_PAPEL_FOCO",
        "UI_TINTA",
        "UI_TINTA_TENUE",
        "UI_ACENTO",
        "UI_ACENTO_FUERTE",
        "ARCHIVO_FONDO",
        "ARCHIVO_SUPERFICIE",
        "ARCHIVO_SUPERFICIE_FOCO",
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
            fail(f"PulidoInterfazJobs perdio el pulido accesible 0.18: {token}")

    transition = read(JAVA / "client/ui/TransicionInterfazJobs.java")
    for token in ("t * t * (3.0F - 2.0F * t)", "ConfigTurno.bajoConsumo()", "Paleta.UI_PAPEL"):
        if token not in transition:
            fail(f"TransicionInterfazJobs perdio el contrato 0.18: {token}")

    config_ui = read(JAVA / "client/screen/PantallaAjustesAviso.java")
    if "JOBS-0161" in config_ui:
        fail("PantallaAjustesAviso conserva el formulario duro JOBS-0161.")
    if "JOBS-CONFIG" not in config_ui:
        fail("PantallaAjustesAviso perdio el identificador estable JOBS-CONFIG.")


def verify_music_session() -> None:
    manager = read(JAVA / "client/sound/GestorMusica.java")
    required = (
        'new Pista("absurdism", SonidosNivel.MUSICA_TEMA, "musica/defecto.ogg")',
        "SUAVIZADO_SUBIDA",
        "SUAVIZADO_BAJADA",
        "SUAVIZADO_CROSSFADE",
        "atenderCrossfade()",
        "gananciaObjetivo",
        "canStartSilent()",
        "detenerAhora()",
        "cliente.getMusicManager().stopPlaying()",
    )
    for token in required:
        if token not in manager:
            fail(f"GestorMusica perdio el contrato musical: {token}")

    sounds = json.loads(read(RES / "sounds.json"))
    event = sounds.get("musica.tema")
    if not isinstance(event, dict):
        fail("sounds.json no contiene musica.tema.")
    entries = event.get("sounds", [])
    names = []
    for item in entries:
        if isinstance(item, str):
            names.append(item)
        elif isinstance(item, dict):
            names.append(item.get("name"))
    if "jobsmenu:musica/defecto" not in names:
        fail("musica.tema ya no apunta al OGG usado como Absurdism.")

    ogg = RES / "sounds/musica/defecto.ogg"
    if not ogg.is_file() or ogg.stat().st_size < 64:
        fail("Falta la pista empaquetada sounds/musica/defecto.ogg.")

    music_doc = read(ROOT / "docs/musica.md")
    if "Absurdism" not in music_doc:
        fail("docs/musica.md no identifica la pista incluida como Absurdism.")
    for token in ("upon_the_hill_v2", "music/menu_nueva.ogg", "integrar_ogg_subido.yml"):
        if token not in music_doc:
            fail(f"docs/musica.md perdio el contrato de la segunda pista: {token}")


def verify_upload_slot() -> None:
    workflow = read(ROOT / ".github/workflows/integrar_ogg_subido.yml")
    required = (
        "music/menu_nueva.ogg",
        "tema_nuevo.ogg",
        "loudnorm=I=-18:TP=-1.5:LRA=11",
        "MUSICA_TEMA_NUEVO",
        "upon_the_hill_v2",
        "./gradlew build --stacktrace --no-daemon",
    )
    for token in required:
        if token not in workflow:
            fail(f"El pipeline de OGG perdio el contrato: {token}")

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
        verify_music_session()
        verify_upload_slot()
        print("UI neutra y microinteracciones 0.18: OK")
        print("Reproductor musical de sesion: OK")
        print("Ranura de OGG subido: OK")
        return 0
    except Exception as exc:
        print(f"ERROR 0.18.0: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
