#!/usr/bin/env python3
"""Contratos 0.45: busqueda, continuidad, rollback y navegacion robusta."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, tokens: tuple[str, ...], where: str) -> None:
    for token in tokens:
        if token not in text:
            raise RuntimeError(f"{where} perdio contrato 0.45: {token}")


def forbid(text: str, tokens: tuple[str, ...], where: str) -> None:
    for token in tokens:
        if token in text:
            raise RuntimeError(f"{where} recupero ruta obsoleta 0.45: {token}")


def verify_search() -> None:
    path = JAVA / "client/screen/PantallaBuscarAjustesJobs.java"
    if not path.is_file():
        raise RuntimeError("falta PantallaBuscarAjustesJobs")
    search = read(path)
    require(search, (
        "public final class PantallaBuscarAjustesJobs extends Screen",
        "private static final Ajuste[] AJUSTES",
        "CTRL+F // SEARCH",
        "setHint(Component.literal(\"CTRL+F // SEARCH\"))",
        "public void resize(Minecraft minecraft, int width, int height)",
        "this.scrollConservado = this.lista.getScrollAmount();",
        "this.lista.setScrollAmount(this.scrollConservado);",
        "GLFW.GLFW_KEY_ENTER",
        "GLFW.GLFW_KEY_ESCAPE",
        "this.anterior.keyPressed(GLFW.GLFW_KEY_1 + ajuste.categoria(), 0, 0);",
    ), "PantallaBuscarAjustesJobs")

    shortcuts = read(JAVA / "client/AtajosInterfazJobs.java")
    require(shortcuts, (
        "pantalla instanceof PantallaAjustesAviso ajustes",
        "GLFW.GLFW_MOD_CONTROL",
        "new PantallaBuscarAjustesJobs(ajustes)",
    ), "AtajosInterfazJobs")


def verify_config() -> None:
    config = read(JAVA / "client/screen/PantallaAjustesAviso.java")
    require(config, (
        "private static Categoria ultimaCategoria = Categoria.VISUAL;",
        "this(anterior, opciones, ultimaCategoria);",
        "ultimaCategoria = this.categoria;",
        'String estado = actual == null\n                ? "CUSTOM"',
        'String ayuda = "CTRL+F";',
        "private boolean cerrando;",
        "if (this.cerrando || this.minecraft == null) return;",
    ), "PantallaAjustesAviso")

    for name in ("PantallaPielJobs.java", "PantallaControlesJobs.java"):
        text = read(JAVA / "client/screen" / name)
        require(text, (
            "private boolean cerrando;",
            "this.cerrando = false;",
            "if (this.cerrando || this.minecraft == null) return;",
            "this.cerrando = true;",
        ), name)


def verify_language_transaction() -> None:
    language = read(JAVA / "client/screen/PantallaIdiomaJobs.java")
    require(language, (
        "if (this.aplicado == null)",
        "if (this.pendiente == null)",
        "private String filtroConservado = \"\";",
        "private double scrollConservado;",
        "public void resize(Minecraft minecraft, int width, int height)",
        "this.scrollConservado = this.lista.getScrollAmount();",
        "this.lista.setScrollAmount(this.scrollConservado);",
        "String idiomaAnterior = this.idiomas.getSelected();",
        "this.opciones.languageCode = idiomaAnterior;",
        "this.idiomas.setSelected(idiomaAnterior);",
        "this.falloAplicacion = true;",
        "MezclaAudio.gesto(SonidosNivel.UI_NEGADO, 0.46F);",
    ), "PantallaIdiomaJobs")


def verify_resize_continuity() -> None:
    for name in ("PantallaMundosJobs.java", "PantallaModsJobs.java"):
        text = read(JAVA / "client/screen" / name)
        require(text, (
            "private String filtroPreferido = \"\";",
            "private boolean focoPreferido;",
            "public void resize(Minecraft minecraft, int width, int height)",
            "capturarBusqueda();",
            "restaurarBusqueda();",
            "this.busqueda.setValue(this.filtroPreferido);",
        ), name)


def verify_hot_paths_and_callbacks() -> None:
    sound = read(JAVA / "client/screen/PantallaSonidoJobs.java")
    require(sound, (
        "private static Field campoLista;",
        "private static boolean campoListaResuelto;",
        "private static Field campoLista()",
        "if (campoListaResuelto) return campoLista;",
    ), "PantallaSonidoJobs")

    options = read(JAVA / "client/screen/PantallaOpcionesJobs.java")
    require(options, (
        "if (this.minecraft.screen instanceof PantallaPaquetesJobs)",
        "this.minecraft.options.updateResourcePacks(r);",
    ), "PantallaOpcionesJobs")
    forbid(options, (
        "if (this.minecraft.screen != this)",
    ), "PantallaOpcionesJobs")


def main() -> None:
    verify_search()
    verify_config()
    verify_language_transaction()
    verify_resize_continuity()
    verify_hot_paths_and_callbacks()
    print("OK busqueda, continuidad, rollback y robustez 0.45")


if __name__ == "__main__":
    main()
