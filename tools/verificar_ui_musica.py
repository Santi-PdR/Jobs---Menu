#!/usr/bin/env python3
"""Contratos de 0.17.0: UI neutra y reproductor musical de sesion."""
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

    # Estas superficies son UI pura. Ninguna puede volver a tomar el amarillo
    # fisico de pared/fluorescente como color de widget o foco.
    files = (
        JAVA / "client/ui/ChromeExpediente.java",
        JAVA / "client/ui/PielVanillaJobs.java",
        JAVA / "client/ui/BotonExpediente.java",
        JAVA / "client/ui/ToggleExpediente.java",
        JAVA / "client/ui/SliderExpediente.java",
        JAVA / "client/ui/PulidoInterfazJobs.java",
        JAVA / "client/ui/TransicionInterfazJobs.java",
        JAVA / "client/screen/PantallaIdiomaJobs.java",
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
    if "t9KaSaGEwvI" not in music_doc:
        fail("docs/musica.md perdio la referencia de la segunda pista solicitada.")


def main() -> int:
    try:
        verify_neutral_ui()
        verify_music_session()
        print("UI neutra: OK")
        print("Reproductor musical de sesion: OK")
        return 0
    except Exception as exc:
        print(f"ERROR 0.17.0: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
