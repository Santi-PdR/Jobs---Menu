#!/usr/bin/env python3
"""Contrato 0.44: Graficos intocable, sin MODPACK ni captura oculta de OptionsScreen."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, where: str) -> None:
    if token not in text:
        raise RuntimeError(f"{where} perdio contrato 0.44: {token}")


def forbid(text: str, token: str, where: str) -> None:
    if token in text:
        raise RuntimeError(f"{where} recupero una ruta eliminada 0.44: {token}")


def main() -> None:
    options = read(JAVA / "client/screen/PantallaOpcionesJobs.java")
    for token in (
        "public final class PantallaOpcionesJobs extends Screen",
        "CompatGraficos.crearPantallaEmbeddium(this.minecraft, this)",
        "new VideoSettingsScreen(this, this.opciones)",
        "private boolean cerrando;",
        "if (this.cerrando || this.minecraft == null) return;",
        "this.cerrando = true;",
        "if (this.minecraft.screen != this)",
    ):
        require(options, token, "PantallaOpcionesJobs")

    for token in (
        "extends OptionsScreen",
        "super.init();",
        "botonVideoNatural",
        "sincronizarControlesNaturales",
        "coincideRanuraVideo",
        "recordarVideo",
        "integracionNaturalFinalizada",
        "MODPACK",
        "abrirOpcionesModpack",
        "permitirOptionsNaturalUnaVez",
        "widget.visible = false",
    ):
        forbid(options, token, "PantallaOpcionesJobs")

    compat_path = JAVA / "client/CompatGraficos.java"
    if not compat_path.is_file():
        raise RuntimeError("Falta CompatGraficos: no hay puente aislado hacia Embeddium.")
    compat = read(compat_path)
    for token in (
        'private static final String EMBEDDIUM_ID = "embeddium";',
        "getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class)",
        "factory.screenFunction().apply(minecraft, anterior)",
        "pantalla != null && pantalla != anterior",
    ):
        require(compat, token, "CompatGraficos")
    for token in (
        "Class.forName",
        "SodiumOptionsGUI",
        "EmbeddiumVideoOptionsScreen",
        "setScreen(",
    ):
        forbid(compat, token, "CompatGraficos")

    listener = read(JAVA / "client/EscuchaCliente.java")
    for token in (
        "pantalla instanceof VideoSettingsScreen",
        "esPantallaTerceros(pantalla)",
        "boolean flujoExternoActual = flujoExternoActivo || esPantallaTerceros(anterior);",
        "boolean flujoAdministrativo = !flujoExternoActual && (",
        "anterior instanceof PantallaNivel",
        "anterior instanceof PantallaEstancia",
        "anterior instanceof PantallaOpcionesJobs",
    ):
        require(listener, token, "EscuchaCliente")
    for token in (
        "permitirOptionsNaturalUnaVez",
        "optionsNaturalSolicitado",
        "SesionMenu.activa()\n                        || anterior instanceof PantallaNivel",
    ):
        forbid(listener, token, "EscuchaCliente")

    print("OK 0.44: Graficos intocable, sin MODPACK y redirecciones acotadas")


if __name__ == "__main__":
    main()
