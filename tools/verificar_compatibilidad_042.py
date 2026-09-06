#!/usr/bin/env python3
"""Contrato heredado: aislamiento generico de pantallas y subflujos de terceros."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, where: str) -> None:
    if token not in text:
        raise RuntimeError(f"{where} perdio el contrato de aislamiento: {token}")


def forbid(text: str, token: str, where: str) -> None:
    if token in text:
        raise RuntimeError(f"{where} reintrodujo una ruta obsoleta: {token}")


def main() -> None:
    listener = read(JAVA / "client/EscuchaCliente.java")
    for token in (
        "private static boolean flujoExternoActivo;",
        "boolean flujoExternoActual = flujoExternoActivo || esPantallaTerceros(anterior);",
        "boolean flujoAdministrativo = !flujoExternoActual && (",
        "anterior instanceof PantallaNivel",
        "anterior instanceof PantallaEstancia",
        "anterior instanceof PantallaOpcionesJobs",
        "actualizarFlujoExterno(flujoExternoActual, siguiente);",
        "private static boolean esPantallaTerceros(Screen pantalla)",
        "private static boolean esSuperficieAjenaIntocable(Screen pantalla)",
        '!clase.startsWith("net.minecraft.")',
        '!clase.startsWith("net.minecraftforge.")',
        "pantalla instanceof VideoSettingsScreen",
        "|| (flujoExternoActivo && !esPantallaPropia(pantalla))",
        "if (pantalla == null || esSuperficieAjenaIntocable(pantalla)) return;",
        "|| esSuperficieAjenaIntocable(pantalla)) return false;",
        "esSuperficieAjenaIntocable(desde)",
        "esSuperficieAjenaIntocable(hasta)",
        "Minecraft.getInstance().level == null && !esSuperficieAjenaIntocable(siguiente)",
        "siguiente.getClass() == OptionsScreen.class",
        "if (esPantallaTerceros(siguiente) || veniaExterno)",
        "private static void limpiarFlujoExterno()",
    ):
        require(listener, token, "EscuchaCliente")

    for token in (
        "permitirOptionsNaturalUnaVez",
        "optionsNaturalSolicitado",
        "SesionMenu.activa()\n                        || anterior instanceof PantallaNivel",
        "me.jellysquid.mods.sodium",
        "org.embeddedt.embeddium",
        "net.coderbot.iris",
        "net.irisshaders.iris",
    ):
        forbid(listener, token, "EscuchaCliente")

    print("OK aislamiento generico de terceros y redirecciones acotadas")


if __name__ == "__main__":
    main()
