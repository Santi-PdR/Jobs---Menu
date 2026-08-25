#!/usr/bin/env python3
"""Sello de verificacion estatica del mod Jobs - Menu de Turno.

No sustituye a `gradlew build`, pero atrapa las erratas que cuestan una vuelta
entera de compilacion: versiones desincronizadas, claves de idioma faltantes,
JSON invalido, acentos colados en el codigo y llaves sin cerrar.

Uso:  python3 tools/verificar.py
Sale con codigo 0 si todo esta en orden, 1 si hay algun fallo.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent

FALLOS: list[str] = []
AVISOS: list[str] = []


def fallo(mensaje: str) -> None:
    FALLOS.append(mensaje)


def aviso(mensaje: str) -> None:
    AVISOS.append(mensaje)


def leer(ruta: Path) -> str:
    return ruta.read_text(encoding="utf-8")


def propiedades() -> dict[str, str]:
    datos: dict[str, str] = {}
    for linea in leer(RAIZ / "gradle.properties").splitlines():
        linea = linea.strip()
        if not linea or linea.startswith("#") or "=" not in linea:
            continue
        clave, valor = linea.split("=", 1)
        datos[clave.strip()] = valor.strip()
    return datos


# --------------------------------------------------------------------------
# 1. Versiones sincronizadas
# --------------------------------------------------------------------------
def verificar_versiones(props: dict[str, str]) -> None:
    version = props.get("mod_version")
    if not version:
        fallo("gradle.properties no define mod_version.")
        return

    contexto = leer(RAIZ / "CONTEXTO.md")
    if f"**{version}**" not in contexto:
        fallo(f"CONTEXTO.md no declara la version {version}.")

    readme = leer(RAIZ / "README.md")
    if f"**{version}**" not in readme:
        fallo(f"README.md no declara la version {version}.")

    java = leer(RAIZ / "src/main/java/com/santipdr/jobsmenu/JobsMenu.java")
    if f'VERSION = "{version}"' not in java:
        fallo(f"JobsMenu.VERSION no coincide con {version}.")

    jar = f"jobsmenu-{version}.jar"
    if jar not in readme:
        aviso(f"README.md no menciona el artefacto {jar}.")


# --------------------------------------------------------------------------
# 2. Sustituciones de mods.toml
# --------------------------------------------------------------------------
def verificar_mods_toml(props: dict[str, str]) -> None:
    ruta = RAIZ / "src/main/resources/META-INF/mods.toml"
    texto = leer(ruta)
    for clave in sorted(set(re.findall(r"\$\{(\w+)\}", texto))):
        if clave not in props:
            fallo(f"mods.toml usa ${{{clave}}} y gradle.properties no lo define.")

    mod_id = props.get("mod_id", "")
    if mod_id and f'modId="{mod_id}"' not in texto and "${mod_id}" not in texto:
        fallo("mods.toml no declara el modId del proyecto.")


# --------------------------------------------------------------------------
# 3. Archivos de idioma
# --------------------------------------------------------------------------
def cargar_lang(nombre: str) -> dict[str, str]:
    ruta = RAIZ / "src/main/resources/assets/jobsmenu/lang" / nombre
    try:
        return json.loads(leer(ruta))
    except json.JSONDecodeError as error:
        fallo(f"{nombre} no es JSON valido: {error}")
        return {}


def verificar_idiomas() -> dict[str, str]:
    es = cargar_lang("es_es.json")
    en = cargar_lang("en_us.json")

    for clave in sorted(set(es) - set(en)):
        fallo(f"La clave '{clave}' esta en es_es.json pero falta en en_us.json.")
    for clave in sorted(set(en) - set(es)):
        fallo(f"La clave '{clave}' esta en en_us.json pero falta en es_es.json.")

    for clave, valor in es.items():
        if not valor.strip():
            fallo(f"La clave '{clave}' tiene texto vacio en es_es.json.")

    prohibidas = ("build", "commit", "config file", "render", "bug", "debug", "widget")
    for clave, valor in es.items():
        bajo = valor.lower()
        for palabra in prohibidas:
            if palabra in bajo:
                aviso(f"'{clave}' contiene jerga de taller ('{palabra}'): {valor}")

    return es


# --------------------------------------------------------------------------
# 4. Claves usadas en el codigo
# --------------------------------------------------------------------------
def archivos_java() -> list[Path]:
    return sorted((RAIZ / "src/main/java").rglob("*.java"))


def verificar_claves(es: dict[str, str]) -> None:
    usadas: set[str] = set()
    dinamicas: set[str] = set()

    for ruta in archivos_java():
        texto = leer(ruta)
        for clave in re.findall(r'Component\.translatable\(\s*"([^"]+)"\s*\)', texto):
            usadas.add(clave)
        for prefijo in re.findall(r'Component\.translatable\(\s*"([^"]+)"\s*\+', texto):
            dinamicas.add(prefijo)
        # Claves pasadas como literal a metodos auxiliares del propio mod.
        for clave in re.findall(r'"(jobsmenu\.[A-Za-z0-9_.]+)"', texto):
            usadas.add(clave)

    usadas -= dinamicas

    for clave in sorted(usadas):
        if clave not in es:
            fallo(f"El codigo pide la clave '{clave}' y no existe en los idiomas.")

    for prefijo in sorted(dinamicas):
        if not any(k.startswith(prefijo) for k in es):
            fallo(f"El codigo compone claves con prefijo '{prefijo}' y no hay ninguna que empiece asi.")

    cubiertas = set(usadas)
    for prefijo in dinamicas:
        cubiertas.update(k for k in es if k.startswith(prefijo))

    for clave in sorted(set(es) - cubiertas):
        aviso(f"La clave '{clave}' no la usa nadie en el codigo.")


# --------------------------------------------------------------------------
# 5. Higiene del codigo Java
# --------------------------------------------------------------------------
def verificar_java() -> None:
    for ruta in archivos_java():
        relativa = ruta.relative_to(RAIZ)
        crudo = ruta.read_bytes()

        try:
            crudo.decode("ascii")
        except UnicodeDecodeError as error:
            fragmento = crudo[max(0, error.start - 30):error.start + 30]
            fallo(
                f"{relativa} tiene caracteres no ASCII (byte {error.start}). "
                f"Los acentos van en los archivos de idioma. Contexto: {fragmento!r}"
            )

        texto = crudo.decode("utf-8", errors="replace")
        limpio = despojar(texto)

        for abre, cierra, nombre in (("{", "}", "llaves"), ("(", ")", "parentesis"), ("[", "]", "corchetes")):
            if limpio.count(abre) != limpio.count(cierra):
                fallo(
                    f"{relativa}: {nombre} desbalanceados "
                    f"({limpio.count(abre)} '{abre}' vs {limpio.count(cierra)} '{cierra}')."
                )

        if texto.count('"') % 2 != 0:
            fallo(f"{relativa}: numero impar de comillas dobles.")

        if not re.search(r"^package\s+com\.santipdr\.jobsmenu", texto, re.MULTILINE):
            fallo(f"{relativa}: falta la declaracion de paquete esperada.")

        esperado = ruta.stem
        if not re.search(rf"\b(class|interface|enum|record)\s+{re.escape(esperado)}\b", texto):
            fallo(f"{relativa}: no declara un tipo llamado {esperado}.")


def despojar(texto: str) -> str:
    """Quita cadenas, caracteres y comentarios para poder contar delimitadores."""
    salida: list[str] = []
    i = 0
    largo = len(texto)
    while i < largo:
        c = texto[i]
        if c == "/" and i + 1 < largo and texto[i + 1] == "/":
            while i < largo and texto[i] != "\n":
                i += 1
        elif c == "/" and i + 1 < largo and texto[i + 1] == "*":
            i += 2
            while i + 1 < largo and not (texto[i] == "*" and texto[i + 1] == "/"):
                i += 1
            i += 2
        elif c == '"':
            i += 1
            while i < largo and texto[i] != '"':
                i += 2 if texto[i] == "\\" else 1
            i += 1
        elif c == "'":
            i += 1
            while i < largo and texto[i] != "'":
                i += 2 if texto[i] == "\\" else 1
            i += 1
        else:
            salida.append(c)
            i += 1
    return "".join(salida)


# --------------------------------------------------------------------------
# 6. Recursos varios
# --------------------------------------------------------------------------
def verificar_recursos() -> None:
    ruta = RAIZ / "src/main/resources/pack.mcmeta"
    try:
        datos = json.loads(leer(ruta))
    except json.JSONDecodeError as error:
        fallo(f"pack.mcmeta no es JSON valido: {error}")
        return

    formato = datos.get("pack", {}).get("pack_format")
    if formato != 15:
        fallo(f"pack.mcmeta declara pack_format {formato}; 1.20.1 espera 15.")

    for necesario in (
        "build.gradle",
        "settings.gradle",
        "gradle.properties",
        "gradlew",
        "gradlew.bat",
        "gradle/wrapper/gradle-wrapper.properties",
        ".gitignore",
    ):
        if not (RAIZ / necesario).exists():
            fallo(f"Falta el archivo {necesario}.")

    if not (RAIZ / "gradle/wrapper/gradle-wrapper.jar").exists():
        aviso("gradle/wrapper/gradle-wrapper.jar no esta presente; el despliegue lo descarga.")


def main() -> int:
    props = propiedades()
    verificar_versiones(props)
    verificar_mods_toml(props)
    es = verificar_idiomas()
    verificar_claves(es)
    verificar_java()
    verificar_recursos()

    for mensaje in AVISOS:
        print(f"  aviso  | {mensaje}")
    for mensaje in FALLOS:
        print(f"  FALLO  | {mensaje}")

    print()
    if FALLOS:
        print(f"Verificacion con {len(FALLOS)} fallo(s) y {len(AVISOS)} aviso(s).")
        return 1

    print(f"Verificacion superada. {len(AVISOS)} aviso(s), ningun fallo.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
