#!/usr/bin/env python3
"""Protege optimizaciones de caminos calientes sin intentar medir FPS en CI."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, where: str) -> None:
    if token not in text:
        raise RuntimeError(f"{where} perdio el contrato de optimizacion: {token}")


def forbid(text: str, token: str, where: str) -> None:
    if token in text:
        raise RuntimeError(f"{where} recupero trabajo repetitivo prohibido: {token}")


def main() -> None:
    listas = read(JAVA / "client/ui/ListasExpediente.java")
    for token in (
        "CAMPOS_LISTA_POR_CLASE",
        "pantallaCache",
        "listasCache",
        "comenzarFrame(Screen pantalla)",
        "barrasDibujadasFrame",
        "liberar(Screen pantalla)",
        "List.copyOf(resultado)",
    ):
        require(listas, token, "ListasExpediente")
    forbid(listas, "new IdentityHashMap", "ListasExpediente")

    escucha = read(JAVA / "client/EscuchaCliente.java")
    for token in (
        "ScreenEvent.Render.Pre",
        "Screen pantalla = evento.getScreen();",
        "if (pantalla == null || esSuperficieAjenaIntocable(pantalla)) return;",
        "ListasExpediente.comenzarFrame(pantalla)",
        "if (!esSuperficieAjenaIntocable(pantalla)) {\n            ListasExpediente.liberar(pantalla);",
        "long ahora = System.currentTimeMillis();",
        "Collections.newSetFromMap(new WeakHashMap<>())",
    ):
        require(escucha, token, "EscuchaCliente")

    imagen = read(JAVA / "client/scene/planta/PlantaImagen.java")
    for token in (
        "AbstractTexture texturaFiltrada",
        "if (actual != this.texturaFiltrada)",
        "this.texturaFiltrada = actual;",
    ):
        require(imagen, token, "PlantaImagen")
    forbid(imagen, "getTexture(textura).setFilter", "PlantaImagen")

    avisos = read(JAVA / "client/ui/NotaAviso.java")
    for token in (
        "minutoEspecialCache",
        "claveTextoCache",
        "textoActual(long ahora)",
        "textoActual(ahora)",
    ):
        require(avisos, token, "NotaAviso")

    rotacion = read(JAVA / "client/scene/RotacionNiveles.java")
    for token in (
        "instanteCache",
        "estadoCache",
        "instanteCache == ahora",
        "private static Estado calcular(long ahora)",
    ):
        require(rotacion, token, "RotacionNiveles")

    pulido = read(JAVA / "client/ui/PulidoInterfazJobs.java")
    for token in (
        "CAMBIO_GUARDADO",
        "long ahora = System.currentTimeMillis();",
        "widgets(g, pantalla, mouseX, mouseY, ahora)",
        "entrada(g, pantalla, ahora)",
        "aviso(g, pantalla, ahora)",
    ):
        require(pulido, token, "PulidoInterfazJobs")

    piel = read(JAVA / "client/ui/PielVanillaJobs.java")
    token_contraste = "float contraste = ConfigTurno.altoContraste() ? 1.18F : 1.0F;"
    require(piel, token_contraste, "PielVanillaJobs")
    if piel.count(token_contraste) != 1:
        raise RuntimeError("PielVanillaJobs debe resolver alto contraste una sola vez por pasada.")

    escena = read(JAVA / "client/scene/EscenaNivel.java")
    tratamiento = read(JAVA / "client/scene/TratamientoEscena.java")
    require(escena, "int paso = ahorro ? 6 : 3;", "EscenaNivel")
    require(tratamiento, "int capas = ahorro ? 3 : 6;", "TratamientoEscena")
    require(tratamiento, "int bandas = ahorro ? 4 : 8;", "TratamientoEscena")

    multi = read(JAVA / "client/screen/PantallaMultijugadorJobs.java")
    for token in (
        "TOOLTIP_PROTEGIDO",
        "tooltipInicializado",
        "prepararRotulos();",
        "this.ayudaF5",
    ):
        require(multi, token, "PantallaMultijugadorJobs")

    gradle = read(ROOT / "build.gradle")
    require(gradle, "preserveFileTimestamps = false", "build.gradle")
    require(gradle, "reproducibleFileOrder = true", "build.gradle")
    forbid(gradle, "Implementation-Timestamp", "build.gradle")

    print("OK contratos de optimizacion Jobs")


if __name__ == "__main__":
    main()