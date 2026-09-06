#!/usr/bin/env python3
"""Contrato 0.41: Embeddium real cuando existe, vanilla solo como fallback."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, where: str) -> None:
    if token not in text:
        raise RuntimeError(f"{where} perdio el contrato grafico: {token}")


def main() -> None:
    compat = read(JAVA / "client/CompatGraficos.java")
    for token in (
        'private static final String EMBEDDIUM_ID = "embeddium";',
        "getModContainerById(EMBEDDIUM_ID)",
        "ConfigScreenHandler.ConfigScreenFactory.class",
        "factory.screenFunction().apply(minecraft, anterior)",
        "aperturasEmbeddium++",
        "fallbacksVanilla++",
    ):
        require(compat, token, "CompatGraficos")
    if "Class.forName" in compat:
        raise RuntimeError("CompatGraficos no debe depender de reflection ni clases internas de Embeddium.")

    options = read(JAVA / "client/screen/PantallaOpcionesJobs.java")
    for token in (
        "CompatGraficos.crearPantallaEmbeddium(this.minecraft, this)",
        "embeddium != null",
        "new VideoSettingsScreen(this, this.opciones)",
    ):
        require(options, token, "PantallaOpcionesJobs")

    listener = read(JAVA / "client/EscuchaCliente.java")
    for token in (
        "pantalla instanceof VideoSettingsScreen",
        'clase.startsWith("me.jellysquid.mods.sodium.client.gui.")',
        'clase.startsWith("org.embeddedt.embeddium.gui.")',
        'clase.startsWith("org.embeddedt.embeddium.impl.gui.")',
        "if (pantalla == null || esVideoIntocable(pantalla)) return;",
        "if (pantalla == null || !ConfigTurno.menuPropio() || esVideoIntocable(pantalla)) return false;",
    ):
        require(listener, token, "EscuchaCliente")

    print("OK graficos 0.41: Embeddium factory + fallback vanilla + aislamiento Jobs")


if __name__ == "__main__":
    main()
