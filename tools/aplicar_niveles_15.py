#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def replace_exact(path: Path, old: str, new: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if new in text:
        return False
    if old not in text:
        raise SystemExit(f"Expected pattern not found in {path}: {old!r}")
    path.write_text(text.replace(old, new), encoding="utf-8")
    return True


def patch_ascii(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    replacements = {
        "á": "a", "é": "e", "í": "i", "ó": "o", "ú": "u",
        "Á": "A", "É": "E", "Í": "I", "Ó": "O", "Ú": "U",
        "ñ": "n", "Ñ": "N", "ü": "u", "Ü": "U",
        "–": "-", "—": "-", "·": "-", "“": '"', "”": '"', "’": "'",
    }
    out = text
    for a, b in replacements.items():
        out = out.replace(a, b)
    if out == text:
        return False
    path.write_text(out, encoding="ascii")
    return True


def patch_lang(path: Path, additions: dict[str, str]) -> bool:
    data = json.loads(path.read_text(encoding="utf-8"))
    changed = False
    for key, value in additions.items():
        if data.get(key) != value:
            data[key] = value
            changed = True
    if changed:
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return changed


ES = {
    "jobsmenu.nivel10.nombre": "NIVEL 10 · Área de contención",
    "jobsmenu.nivel10.nota0": "Las cadenas están tensas. La administración no registra qué sujetan.",
    "jobsmenu.nivel10.nota1": "El rojo indica presencia de Executor. No indica distancia.",
    "jobsmenu.nivel10.nota2": "No toque los eslabones. Tampoco cuando dejen de moverse.",
    "jobsmenu.nivel11.nombre": "NIVEL 11 · El atrio",
    "jobsmenu.nivel11.nota0": "La vegetación crece hacia las luces verdes. Las luces no dan calor.",
    "jobsmenu.nivel11.nota1": "Las lámparas ámbar siguen encendidas. No hay personal asignado.",
    "jobsmenu.nivel11.nota2": "Hay una ruta al fondo. La administración no confirma que regrese.",
    "jobsmenu.nivel12.nombre": "NIVEL 12 · La cámara esmeralda",
    "jobsmenu.nivel12.nota0": "Las balizas verdes marcan una instalación que no figura en planos.",
    "jobsmenu.nivel12.nota1": "La sala responde con eco antes de que usted haga ruido.",
    "jobsmenu.nivel12.nota2": "No permanezca en el centro. El centro ya está ocupado.",
    "jobsmenu.nivel13.nombre": "NIVEL 13 · El salón de guardia",
    "jobsmenu.nivel13.nota0": "El candil sigue encendido. La guardia terminó hace mucho.",
    "jobsmenu.nivel13.nota1": "Los estandartes no identifican a ninguna cuadrilla registrada.",
    "jobsmenu.nivel13.nota2": "El túnel del fondo cuenta como salida sólo después del pago.",
    "jobsmenu.nivel14.nombre": "NIVEL 14 · La puerta de jade",
    "jobsmenu.nivel14.nota0": "La puerta está operativa. Abrir no significa atravesar.",
    "jobsmenu.nivel14.nota1": "Las marcas verdes son de servicio. Nadie recuerda qué servicio.",
    "jobsmenu.nivel14.nota2": "Si las balizas se apagan a la vez, no espere a que vuelvan.",
}

EN = {
    "jobsmenu.nivel10.nombre": "LEVEL 10 · Containment area",
    "jobsmenu.nivel10.nota0": "The chains are under tension. Administration does not record what they hold.",
    "jobsmenu.nivel10.nota1": "Red indicates Executor presence. It does not indicate distance.",
    "jobsmenu.nivel10.nota2": "Do not touch the links. Not even when they stop moving.",
    "jobsmenu.nivel11.nombre": "LEVEL 11 · The atrium",
    "jobsmenu.nivel11.nota0": "The vegetation grows toward the green lights. The lights give no heat.",
    "jobsmenu.nivel11.nota1": "The amber lamps remain lit. No personnel are assigned here.",
    "jobsmenu.nivel11.nota2": "There is a route at the far end. Administration does not confirm that it returns.",
    "jobsmenu.nivel12.nombre": "LEVEL 12 · The emerald chamber",
    "jobsmenu.nivel12.nota0": "The green beacons mark an installation absent from every plan.",
    "jobsmenu.nivel12.nota1": "The room answers with an echo before you make a sound.",
    "jobsmenu.nivel12.nota2": "Do not remain in the centre. The centre is already occupied.",
    "jobsmenu.nivel13.nombre": "LEVEL 13 · The guard hall",
    "jobsmenu.nivel13.nota0": "The chandelier remains lit. The watch ended a long time ago.",
    "jobsmenu.nivel13.nota1": "The banners identify no registered crew.",
    "jobsmenu.nivel13.nota2": "The tunnel at the far end counts as an exit only after payment.",
    "jobsmenu.nivel14.nombre": "LEVEL 14 · The jade gate",
    "jobsmenu.nivel14.nota0": "The gate is operational. Opening does not mean crossing.",
    "jobsmenu.nivel14.nota1": "The green markings are for service. Nobody remembers which service.",
    "jobsmenu.nivel14.nota2": "If every beacon goes dark at once, do not wait for them to return.",
}


def main() -> None:
    changed = False
    changed |= patch_ascii(ROOT / "src/main/java/com/santipdr/jobsmenu/client/scene/Nivel.java")
    changed |= patch_ascii(ROOT / "src/main/java/com/santipdr/jobsmenu/client/scene/planta/PlantaImagen.java")
    changed |= replace_exact(
        ROOT / "src/main/java/com/santipdr/jobsmenu/config/ConfigTurno.java",
        'defineInRange("nivel_fijo", 0, 0, 9)',
        'defineInRange("nivel_fijo", 0, 0, 14)',
    )
    changed |= patch_lang(ROOT / "src/main/resources/assets/jobsmenu/lang/es_es.json", ES)
    changed |= patch_lang(ROOT / "src/main/resources/assets/jobsmenu/lang/en_us.json", EN)
    print("changed" if changed else "already up to date")


if __name__ == "__main__":
    main()
