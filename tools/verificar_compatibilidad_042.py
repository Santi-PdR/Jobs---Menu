#!/usr/bin/env python3
"""Contrato 0.42: flujo natural y aislamiento generico de pantallas de terceros."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, where: str) -> None:
    if token not in text:
        raise RuntimeError(f"{where} perdio el contrato 0.42: {token}")


def forbid(text: str, token: str, where: str) -> None:
    if token in text:
        raise RuntimeError(f"{where} reintrodujo una dependencia fragil: {token}")


def main() -> None:
    if (JAVA / "client/CompatGraficos.java").exists():
        raise RuntimeError("CompatGraficos no debe existir.")

    options = read(JAVA / "client/screen/PantallaOpcionesJobs.java")
    for token in (
        "public final class PantallaOpcionesJobs extends OptionsScreen",
        "super(anterior, opciones);",
        "super.init();",
        "private AbstractButton botonVideoNatural;",
        'Component.translatable("options.video").getString()',
        "ranuraVideoConocida",
        "recordarVideo",
        "coincideRanuraVideo",
        "this.botonVideoNatural.onPress();",
        "widget.visible = false;",
        "if (!this.integracionNaturalFinalizada)",
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
    ):
        forbid(options, token, "PantallaOpcionesJobs")

    listener = read(JAVA / "client/EscuchaCliente.java")
    for token in (
        "private static boolean esPantallaTerceros(Screen pantalla)",
        "private static boolean esSuperficieAjenaIntocable(Screen pantalla)",
        '!clase.startsWith("net.minecraft.")',
        '!clase.startsWith("net.minecraftforge.")',
        "pantalla instanceof VideoSettingsScreen || esPantallaTerceros(pantalla)",
        "boolean flujoAdministrativo = !esPantallaTerceros(anterior) && (",
        "if (pantalla == null || esSuperficieAjenaIntocable(pantalla)) return;",
        "|| esSuperficieAjenaIntocable(pantalla)) return false;",
        "esSuperficieAjenaIntocable(desde)",
        "esSuperficieAjenaIntocable(hasta)",
        "Minecraft.getInstance().level == null && !esSuperficieAjenaIntocable(siguiente)",
        "siguiente.getClass() == OptionsScreen.class",
    ):
        require(listener, token, "EscuchaCliente")
    for token in (
        "me.jellysquid.mods.sodium",
        "org.embeddedt.embeddium",
        "net.coderbot.iris",
        "net.irisshaders.iris",
    ):
        forbid(listener, token, "EscuchaCliente")

    print("OK compatibilidad 0.42: flujo natural + terceros intocables + navegacion aislada")


if __name__ == "__main__":
    main()
