#!/usr/bin/env python3
"""Regla de entrega: Jobs Menu siempre se publica con version en el nombre."""
from __future__ import annotations

import re
import sys
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
PROPS = RAIZ / "gradle.properties"
WORKFLOW = RAIZ / ".github/workflows/build.yml"


def error(mensaje: str) -> None:
    raise RuntimeError(mensaje)


def main() -> int:
    try:
        props = PROPS.read_text(encoding="utf-8")
        match = re.search(r"(?m)^mod_version=([^\r\n]+)$", props)
        if not match:
            error("falta mod_version en gradle.properties")
        version = match.group(1).strip()
        if not re.fullmatch(r"\d+\.\d+\.\d+(?:[-+][0-9A-Za-z.-]+)?", version):
            error(f"mod_version no parece SemVer: {version}")

        workflow = WORKFLOW.read_text(encoding="utf-8")
        if "jobsmenu-latest.jar" in workflow:
            error("el workflow vuelve a publicar jobsmenu-latest.jar sin version")
        if "MOD_JAR=jobsmenu-$VERSION.jar" not in workflow:
            error("el workflow no construye un nombre de JAR versionado")
        if "artifacts: ${{ env.MOD_JAR }}" not in workflow:
            error("la release no publica el JAR versionado")

        print(f"Version: {version}")
        print(f"Artefacto exigido: jobsmenu-{version}.jar")
        print("Politica de versionado: OK")
        return 0
    except Exception as exc:
        print(f"ERROR versionado: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
