#!/usr/bin/env python3
"""Contrato 0.41.1: Graficos usa el flujo natural de OptionsScreen del modpack."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, where: str) -> None:
    if token not in text:
        raise RuntimeError(f"{where} perdio el contrato grafico natural: {token}")


def forbid(text: str, token: str, where: str) -> None:
    if token in text:
        raise RuntimeError(f"{where} vuelve a saltarse el flujo natural: {token}")


def main() -> None:
    compat = JAVA / "client/CompatGraficos.java"
    if compat.exists():
        raise RuntimeError("CompatGraficos no debe existir: Graficos no puede abrir proveedores por su cuenta.")

    options = read(JAVA / "client/screen/PantallaOpcionesJobs.java")
    for token in (
        "public final class PantallaOpcionesJobs extends OptionsScreen",
        "super(anterior, opciones);",
        "super.init();",
        "private AbstractButton botonVideoNatural;",
        'Component.translatable("options.video").getString()',
        "sincronizarControlesNaturales();",
        "widget.visible = false;",
        "this.botonVideoNatural.onPress();",
        "if (!this.integracionNaturalFinalizada)",
        "child instanceof Renderable renderable",
        'startsWith("com.santipdr.jobsmenu.")',
    ):
        require(options, token, "PantallaOpcionesJobs")

    for token in (
        "CompatGraficos",
        "ConfigScreenHandler",
        "getModContainerById",
        "Class.forName",
        "new VideoSettingsScreen",
        "SodiumOptionsGUI",
        "EmbeddiumVideoOptionsScreen",
        "super.render(g, mouseX, mouseY, partialTick)",
    ):
        forbid(options, token, "PantallaOpcionesJobs")

    listener = read(JAVA / "client/EscuchaCliente.java")
    for token in (
        "pantalla instanceof VideoSettingsScreen",
        'clase.startsWith("me.jellysquid.mods.sodium.client.gui.")',
        'clase.startsWith("org.embeddedt.embeddium.gui.")',
        'clase.startsWith("org.embeddedt.embeddium.impl.gui.")',
        "if (pantalla == null || esVideoIntocable(pantalla)) return;",
        "if (pantalla == null || !ConfigTurno.menuPropio() || esVideoIntocable(pantalla)) return false;",
        "siguiente.getClass() == OptionsScreen.class",
    ):
        require(listener, token, "EscuchaCliente")

    print("OK graficos 0.41.1: OptionsScreen natural + hooks del modpack + aislamiento Jobs")


if __name__ == "__main__":
    main()
