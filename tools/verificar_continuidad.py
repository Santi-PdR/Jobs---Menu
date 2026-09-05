#!/usr/bin/env python3
"""Contratos pequeños que deben sobrevivir a futuras refactorizaciones del flujo Jobs."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, where: str) -> None:
    if token not in text:
        raise RuntimeError(f"{where} perdio el contrato: {token}")


def main() -> None:
    multiplayer = read(JAVA / "client/screen/PantallaMultijugadorJobs.java")
    for token in (
        "private final String servidorPreferido;",
        "restaurarSeleccionPreferida();",
        "this.serverSelectionList.children()",
        "this.serverSelectionList.setSelected(entrada);",
        "this.onSelectedChange();",
        "String seleccion = ipSeleccionada();",
        "this.cerrando = true;",
        "new PantallaMultijugadorJobs(padreDestino(), seleccion)",
        'Component.translatable("selectServer.refresh")',
        "if (this.minecraft != null && !this.cerrando)",
        "MezclaAudio.gesto(SonidosNivel.UI_ALTERNAR, 0.34F);",
    ):
        require(multiplayer, token, "PantallaMultijugadorJobs")

    if '"F5  //  " + (seleccionOficial ? "JOBS" : "SERVER")' in multiplayer:
        raise RuntimeError("Multiplayer recupero el indicador duro JOBS/SERVER.")

    version = None
    for line in read(ROOT / "gradle.properties").splitlines():
        if line.startswith("mod_version="):
            version = line.split("=", 1)[1].strip()
            break
    if not version:
        raise RuntimeError("No se pudo resolver mod_version.")

    for relative in ("README.md", "CONTEXTO.md", "KNOWN_ISSUES.md", "docs/checklist-manual.md", "docs/compatibilidad.md"):
        text = read(ROOT / relative)
        if version not in text:
            raise RuntimeError(f"{relative} no menciona la version vigente {version}.")

    docs_index = ROOT / "docs/README.md"
    if not docs_index.is_file():
        raise RuntimeError("Falta docs/README.md como indice de documentacion vigente/historica.")
    index = read(docs_index)
    for token in ("Documentación vigente", "Histórico", "AUDITORIA_0.37.0_CONTINUIDAD_MULTIPLAYER_Y_DOCS.md"):
        require(index, token, "docs/README.md")

    changelog = read(ROOT / "CHANGELOG.md")
    for release in ("0.35.0", "0.36.0", "0.37.0"):
        require(changelog, f"## {release}", "CHANGELOG.md")

    print(f"OK continuidad Jobs + documentacion {version}")


if __name__ == "__main__":
    main()
