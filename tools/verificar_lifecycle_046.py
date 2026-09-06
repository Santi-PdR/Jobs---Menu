#!/usr/bin/env python3
"""Contratos 0.46: idioma/Unicode transaccional y buscador robusto."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, tokens: tuple[str, ...], where: str) -> None:
    for token in tokens:
        if token not in text:
            raise RuntimeError(f"{where} perdio contrato 0.46: {token}")


def forbid(text: str, tokens: tuple[str, ...], where: str) -> None:
    for token in tokens:
        if token in text:
            raise RuntimeError(f"{where} recupero ruta fragil 0.46: {token}")


def verify_language_transaction() -> None:
    language = read(JAVA / "client/screen/PantallaIdiomaJobs.java")
    require(language, (
        "private Boolean unicodeAplicado;",
        "private boolean unicodePendiente;",
        "private boolean cerrando;",
        "this.unicodeAplicado = this.opciones.forceUnicodeFont().get();",
        "() -> this.unicodePendiente",
        "this.unicodePendiente = v;",
        "boolean cambiaUnicode = this.unicodePendiente != unicodeAnterior;",
        "this.opciones.forceUnicodeFont().set(this.unicodePendiente);",
        "this.opciones.forceUnicodeFont().set(unicodeAnterior);",
        "this.minecraft.reloadResourcePacks().whenComplete",
        "finalizarAplicacion(",
        "this.minecraft.screen == this",
        "private void cerrarSinRecarga()",
        "if (this.aplicando || this.cerrando || this.minecraft == null) return;",
    ), "PantallaIdiomaJobs")
    forbid(language, (
        "() -> this.opciones.forceUnicodeFont().get(), v -> {\n                    this.opciones.forceUnicodeFont().set(v);\n                    this.opciones.save();",
    ), "PantallaIdiomaJobs")


def verify_search_navigation() -> None:
    search = read(JAVA / "client/screen/PantallaBuscarAjustesJobs.java")
    require(search, (
        "private String contadorResultados = \"00\";",
        "private boolean cerrando;",
        "if (this.cerrando || this.minecraft == null) return;",
        "this.anterior.abrirCategoriaDesdeBusqueda(ajuste.categoria());",
        "new EntradaResultado(ajuste, titulo, detalle, categoria)",
        "EntradaResultado(Ajuste ajuste, String titulo, String detalle, String categoria)",
        "this.contadorResultados = String.format(Locale.ROOT, \"%02d\", cantidad);",
        "return Component.literal(this.titulo);",
    ), "PantallaBuscarAjustesJobs")
    forbid(search, (
        "this.anterior.keyPressed(GLFW.GLFW_KEY_1 + ajuste.categoria(), 0, 0);",
        "Component.translatable(this.ajuste.clave()).getString()",
        "Component.translatable(this.ajuste.detalle()).getString()",
    ), "PantallaBuscarAjustesJobs")

    settings = read(JAVA / "client/screen/PantallaAjustesAviso.java")
    require(settings, (
        "void abrirCategoriaDesdeBusqueda(int indice)",
        "Categoria[] categorias = Categoria.values();",
        "if (indice < 0 || indice >= categorias.length) return;",
        "if (nueva == this.categoria)",
        "this.minecraft.setScreen(this);",
        "this.minecraft.setScreen(new PantallaAjustesAviso(this.anterior, this.opciones, nueva));",
    ), "PantallaAjustesAviso")


def main() -> None:
    verify_language_transaction()
    verify_search_navigation()
    print("OK idioma/Unicode transaccional + buscador robusto 0.46")


if __name__ == "__main__":
    main()
