#!/usr/bin/env python3
"""Contratos 0.43: perfiles exactos, busqueda usable y navegacion externa segura."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, where: str) -> None:
    if token not in text:
        raise RuntimeError(f"{where} perdio contrato 0.43: {token}")


def forbid(text: str, token: str, where: str) -> None:
    if token in text:
        raise RuntimeError(f"{where} recupero heuristica obsoleta 0.43: {token}")


def verify_profiles() -> None:
    perfiles = read(JAVA / "config/PerfilesJobs.java")
    for token in (
        "private static boolean baseComunActual(boolean exigirEstadoVisible)",
        "private static boolean coincideEquilibrado()",
        "private static boolean coincideInmersivo()",
        "private static boolean coincideRendimiento()",
        "private static boolean coincideAccesible()",
        "private static boolean coincideMinimo()",
        "ConfigTurno.duracionEstancia() == 24",
        "ConfigTurno.duracionEstancia() == 20",
        "ConfigTurno.duracionEstancia() == 36",
        "ConfigTurno.duracionEstancia() == 38",
        "ConfigTurno.duracionEstancia() == 45",
        "ConfigTurno.volumenAmbientePorcentaje() == 55",
        "ConfigTurno.volumenAmbientePorcentaje() == 68",
        "ConfigTurno.volumenAmbientePorcentaje() == 48",
        "ConfigTurno.volumenAmbientePorcentaje() == 42",
        "ConfigTurno.volumenAmbientePorcentaje() == 40",
    ):
        require(perfiles, token, "PerfilesJobs")

    for token in (
        "if (ConfigTurno.perfilAccesible()) return Perfil.ACCESIBLE;",
        "if (ConfigTurno.interfazMinima() && ConfigTurno.bajoConsumo()) return Perfil.MINIMO;",
        "ConfigTurno.volumenAmbientePorcentaje() >= 64",
    ):
        forbid(perfiles, token, "PerfilesJobs")


def verify_search_and_close() -> None:
    for filename in ("PantallaMundosJobs.java", "PantallaModsJobs.java"):
        text = read(JAVA / "client/screen" / filename)
        for token in (
            "private boolean cerrando;",
            "this.cerrando = false;",
            "private boolean atenderEscapeBusqueda()",
            'this.busqueda.setValue("");',
            "this.busqueda.setFocused(false);",
            "this.setFocused(null);",
            "if (this.cerrando || this.minecraft == null) return;",
            "this.cerrando = true;",
        ):
            require(text, token, filename)


def verify_external_flow() -> None:
    listener = read(JAVA / "client/EscuchaCliente.java")
    require(listener,
            "ConfigTurno.menuPropio()\n                && !flujoExternoActual\n                && siguiente != null\n                && siguiente.getClass() == TitleScreen.class",
            "EscuchaCliente")
    require(listener,
            "!flujoExternoActual && ConfigTurno.pausaPropia() && esPausaReal(siguiente)",
            "EscuchaCliente")


def main() -> None:
    verify_profiles()
    verify_search_and_close()
    verify_external_flow()
    print("OK UX, perfiles exactos y navegacion externa 0.43")


if __name__ == "__main__":
    main()
