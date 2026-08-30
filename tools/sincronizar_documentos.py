#!/usr/bin/env python3
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

DEPLOY_SECTION = r'''## Compilacion y despliegue

La entrega normal ya no se compila en la PC del usuario. GitHub Actions usa Java 17, ejecuta `tools/verificar.py`, compila con Forge/Gradle y, solo si todo termina correctamente, actualiza la release rodante `dev-latest` con `jobsmenu-latest.jar`.

La **unica instancia de prueba y despliegue** es:

```text
C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods
```

No se mantienen rutas alternativas para `jobs-2`, `Test2.0` ni otras instancias. El procedimiento completo y el PowerShell canonico estan en [`docs/DESPLIEGUE.md`](docs/DESPLIEGUE.md).

Para una prueba normal, el usuario solo abre PowerShell y pega el bloque de despliegue: descarga el ultimo JAR que ya paso CI, valida su cabecera antes de tocar la instalacion actual y reemplaza solamente los JAR de Jobs Menu dentro de `test-1\\mods`.

El build local queda disponible solo para desarrollo o diagnostico:

```powershell
.\gradlew.bat clean build --no-daemon
```

Debe ejecutarse con JDK 17 y terminar en `BUILD SUCCESSFUL`. El artefacto versionado local es `build\\libs\\jobsmenu-0.10.0.jar`.
'''

CONTEXT_SECTION = r'''## Regla vigente de build y despliegue

Esta regla reemplaza cualquier procedimiento historico que aparezca mas abajo en este documento.

- `main` es la rama de entrega. Los cambios estructurales se preparan en una rama de trabajo y solo se integran despues de pasar CI.
- El CI de GitHub es quien verifica y compila la entrega de desarrollo con Java 17.
- El build pasa primero por `tools/verificar.py` y luego por Gradle/Forge.
- El JAR estable para pruebas se publica como `jobsmenu-latest.jar` en la release rodante `dev-latest`.
- El usuario no necesita compilar localmente para una prueba normal: solo ejecuta el PowerShell de `docs/DESPLIEGUE.md`.
- El **unico destino local permitido** es `C:\Users\santi\AppData\Roaming\.sklauncher\instances\test-1\mods`.
- `jobs-2`, `Test2.0` y cualquier otra instancia no forman parte del flujo vigente.
- Nunca se elimina el JAR instalado antes de descargar y validar el reemplazo.

La escena vigente contiene quince niveles: diez plantas procedurales (0-9) y cinco fondos suministrados integrados como niveles 10-14. Ver `docs/NIVELES_10_14.md`.
'''


def update_readme() -> bool:
    path = ROOT / "README.md"
    text = path.read_text(encoding="utf-8")
    original = text

    text = text.replace("accesibilidad y la legibilidad de sus diez recintos.",
                        "accesibilidad y la legibilidad de sus quince recintos.")
    text = text.replace(
        "auditoría de fondos) y rediseñó el Trono desde cero. Build con Java 17 y\nprueba en Minecraft pendientes: ver [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md).",
        "auditoría de fondos) y rediseñó el Trono desde cero. El build automatizado con Java 17 está activo;\nla prueba final dentro de Minecraft sigue siendo manual: ver [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md).",
    )

    if "## Compilacion y despliegue" not in text:
        start = text.find("## Compilar\n")
        if start < 0:
            start = text.find("## Compilar\r\n")
        if start < 0:
            raise SystemExit("README.md: no se encontro la seccion '## Compilar'.")
        end = text.find("\n## Herramientas sin JDK", start)
        if end < 0:
            raise SystemExit("README.md: no se encontro '## Herramientas sin JDK' despues de Compilar.")
        text = text[:start] + DEPLOY_SECTION.rstrip() + "\n" + text[end:]

    marker = "[`docs/NIVELES_10_14.md`](docs/NIVELES_10_14.md)"
    if marker not in text:
        needle = "El fondo va cambiando de nivel solo. Entre uno y otro se corta la luz."
        addition = (needle + "\n\nLa rotacion actual tiene **15 niveles**: diez recintos procedurales y cinco "
                    "fondos suministrados integrados con luz, ambiente y frases propias. "
                    "Ver [`docs/NIVELES_10_14.md`](docs/NIVELES_10_14.md).")
        if needle not in text:
            raise SystemExit("README.md: no se encontro el parrafo de rotacion.")
        text = text.replace(needle, addition, 1)

    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def update_context() -> bool:
    path = ROOT / "CONTEXTO.md"
    text = path.read_text(encoding="utf-8")
    original = text
    heading = "## Regla vigente de build y despliegue"

    if heading in text:
        pattern = re.compile(r"## Regla vigente de build y despliegue\n.*?(?=\n## |\Z)", re.S)
        text, count = pattern.subn(lambda _m: CONTEXT_SECTION.rstrip(), text, count=1)
        if count != 1:
            raise SystemExit("CONTEXTO.md: no se pudo actualizar la regla vigente.")
    else:
        first_break = text.find("\n")
        if first_break < 0:
            raise SystemExit("CONTEXTO.md: formato inesperado.")
        text = text[:first_break + 1] + "\n" + CONTEXT_SECTION.rstrip() + "\n" + text[first_break + 1:]

    text = re.sub(r"\| Rama de trabajo \| `[^`]+` \|", "| Rama de entrega | `main` |", text, count=1)

    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def main() -> None:
    changed = update_readme() | update_context()
    print("documentation changed" if changed else "documentation already current")


if __name__ == "__main__":
    main()
