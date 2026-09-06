#!/usr/bin/env python3
"""Contratos 0.40: musica Jobs sin fallback vanilla, catalogo estable y hard-stop directo."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, where: str) -> None:
    if token not in text:
        raise RuntimeError(f"{where} perdio el contrato 0.40: {token}")


def main() -> None:
    music = read(JAVA / "client/sound/GestorMusica.java")
    for token in (
        "private static final Pista[] CATALOGO = new Pista[]",
        "return CATALOGO;",
        "MezclaAudio.resolver(pista.evento(), null)",
        "se omite sin fallback vanilla",
        "Minecraft.getInstance().getSoundManager().stop(this);",
        "for (Pista pista : CATALOGO)",
        "return CATALOGO.length;",
    ):
        require(music, token, "GestorMusica")

    if "SoundEvents.MUSIC_MENU" in music or "minecraft:music.menu" in music:
        raise RuntimeError("GestorMusica recupero fallback a musica vanilla.")
    if "return new Pista[]" in music:
        raise RuntimeError("GestorMusica vuelve a reconstruir el catalogo en cada consulta.")

    changelog = read(ROOT / "CHANGELOG.md")
    for version in ("0.35.0", "0.36.0", "0.37.0", "0.38.0", "0.39.0", "0.40.0"):
        require(changelog, f"## {version}", "CHANGELOG.md")

    docs = read(ROOT / "docs/musica.md")
    require(docs, "sin fallback vanilla", "docs/musica.md")
    require(docs, "0.40.0", "docs/musica.md")

    print("OK identidad musical + catalogo estable + hard-stop 0.40")


if __name__ == "__main__":
    main()
