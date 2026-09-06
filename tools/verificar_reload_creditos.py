#!/usr/bin/env python3
"""Contratos 0.39: creditos musicales empaquetados y reload sin generaciones perdidas."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"
RES = ROOT / "src/main/resources/assets/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, where: str) -> None:
    if token not in text:
        raise RuntimeError(f"{where} perdio el contrato 0.39: {token}")


def main() -> None:
    marker = RES / "musica_creditada.txt"
    if not marker.is_file():
        raise RuntimeError("Falta musica_creditada.txt: los creditos del catalogo quedarian deshabilitados.")
    marker_text = read(marker)
    for track in ("absurdism", "requiem", "upon_the_hill_v2"):
        require(marker_text, track, "musica_creditada.txt")

    music = read(JAVA / "client/sound/GestorMusica.java")
    require(music, 'new ResourceLocation("jobsmenu", "musica_creditada.txt")', "GestorMusica")
    require(music, "marcadorHorneado()", "GestorMusica")
    require(music, "ConfigTurno.creditoMusica()", "GestorMusica")

    reload = read(JAVA / "client/RecargaRecursosCliente.java")
    for token in (
        "AtomicLong GENERACION",
        "GENERACION.incrementAndGet();",
        "long procesada = GENERACION.get();",
        "if (GENERACION.get() != procesada)",
        "programarSiHaceFalta();",
        "generacionParaDiagnostico()",
    ):
        require(reload, token, "RecargaRecursosCliente")

    session = read(JAVA / "client/SesionMenu.java")
    guard = session.find("if (activa) {\n            return;\n        }")
    new_visit = session.find("GestorMusica.nuevaVisita();")
    if guard < 0 or new_visit < 0 or guard > new_visit:
        raise RuntimeError("SesionMenu debe cortar reaperturas antes de reinicializar la visita.")

    diag = read(JAVA / "client/DiagnosticoOculto.java")
    for token in ("GestorMusica.pistaParaDiagnostico()", "RecargaRecursosCliente.generacionParaDiagnostico()"):
        require(diag, token, "DiagnosticoOculto")

    print("OK creditos musicales + generaciones de reload 0.39")


if __name__ == "__main__":
    main()
