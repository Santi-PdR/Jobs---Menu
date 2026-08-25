#!/usr/bin/env python3
"""Sello de verificacion estatica del mod Jobs - Aviso a los ocupantes.

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
# Claves validas de una dependencia en Forge 1.20.1 (rama 47.x).
# OJO: 'type' es sintaxis de NeoForge y de Forge posteriores; aqui NO existe.
# Forge 47 exige 'mandatory' y aborta con "Missing required field mandatory".
DEP_OBLIGATORIAS = {"modId", "mandatory"}
DEP_OPCIONALES = {"versionRange", "ordering", "side", "referralUrl"}
DEP_AJENAS = {"type", "reason"}

ORDENAMIENTOS = {"NONE", "BEFORE", "AFTER"}
LADOS = {"BOTH", "CLIENT", "SERVER"}
PRUEBAS_PANTALLA = {"MATCH_VERSION", "IGNORE_SERVER_VERSION", "IGNORE_ALL_VERSION", "NONE"}


def verificar_mods_toml(props: dict[str, str]) -> None:
    """Valida el mods.toml de verdad: sustituye las variables y lo parsea.

    Buscar cadenas sueltas no alcanzaba. Una vez se colo type="required"
    (sintaxis de NeoForge) donde Forge 47 quiere mandatory=true: compilaba
    perfecto y el juego rechazaba el jar al arrancar.
    """
    ruta = RAIZ / "src/main/resources/META-INF/mods.toml"
    texto = leer(ruta)

    for clave in sorted(set(re.findall(r"\$\{(\w+)\}", texto))):
        if clave not in props:
            fallo(f"mods.toml usa ${{{clave}}} y gradle.properties no lo define.")

    # Sustitucion igual a la que hace Gradle, para poder parsearlo.
    resuelto = re.sub(r"\$\{(\w+)\}", lambda m: props.get(m.group(1), ""), texto)

    try:
        import tomllib
    except ModuleNotFoundError:
        aviso("Sin tomllib (Python < 3.11): no se pudo parsear mods.toml.")
        return

    try:
        datos = tomllib.loads(resuelto)
    except Exception as error:
        fallo(f"mods.toml no es TOML valido una vez sustituido: {error}")
        return

    for clave in ("modLoader", "loaderVersion", "license"):
        if clave not in datos:
            fallo(f"mods.toml no declara '{clave}'.")

    mods = datos.get("mods", [])
    if not mods:
        fallo("mods.toml no declara ningun bloque [[mods]].")
        return

    mod_id = props.get("mod_id", "")
    declarados = {m.get("modId") for m in mods}
    if mod_id and mod_id not in declarados:
        fallo(f"mods.toml no declara el modId '{mod_id}'.")

    for mod in mods:
        prueba = mod.get("displayTest")
        if prueba is not None and prueba not in PRUEBAS_PANTALLA:
            fallo(f"mods.toml: displayTest='{prueba}' no es un valor valido.")

    dependencias = datos.get("dependencies", {})
    for duenio, lista in dependencias.items():
        if duenio not in declarados:
            fallo(f"mods.toml: [[dependencies.{duenio}]] no corresponde a ningun mod declarado.")
        for dep in lista:
            nombre = dep.get("modId", "?")

            for ajena in sorted(DEP_AJENAS & set(dep)):
                fallo(
                    f"mods.toml: la dependencia '{nombre}' usa '{ajena}', que es sintaxis de "
                    f"NeoForge. Forge 1.20.1 exige mandatory=true/false."
                )

            for obligatoria in sorted(DEP_OBLIGATORIAS - set(dep)):
                fallo(
                    f"mods.toml: la dependencia '{nombre}' no declara '{obligatoria}' "
                    f"(Forge aborta con 'Missing required field {obligatoria} in dependency')."
                )

            if "mandatory" in dep and not isinstance(dep["mandatory"], bool):
                fallo(f"mods.toml: 'mandatory' de '{nombre}' debe ser true o false, sin comillas.")

            for sobrante in sorted(set(dep) - DEP_OBLIGATORIAS - DEP_OPCIONALES - DEP_AJENAS):
                aviso(f"mods.toml: la dependencia '{nombre}' declara '{sobrante}', que Forge ignora.")

            if dep.get("ordering", "NONE") not in ORDENAMIENTOS:
                fallo(f"mods.toml: ordering='{dep.get('ordering')}' de '{nombre}' no es valido.")

            if dep.get("side", "BOTH") not in LADOS:
                fallo(f"mods.toml: side='{dep.get('side')}' de '{nombre}' no es valido.")

        for necesaria in ("forge", "minecraft"):
            if not any(d.get("modId") == necesaria for d in lista):
                aviso(f"mods.toml: '{duenio}' no declara dependencia de '{necesaria}'.")


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


# --------------------------------------------------------------------------
# 7. Metodos propios que se llaman y no existen
# --------------------------------------------------------------------------
# Palabras que van seguidas de parentesis y no son llamadas a metodos.
PALABRAS_CLAVE = {
    "if", "for", "while", "switch", "catch", "return", "new", "this", "super",
    "do", "else", "try", "assert", "instanceof", "throw", "synchronized",
    "case", "break", "continue", "yield",
}

DECLARACION = re.compile(
    r"^\s*(?:@\w+\s+)*"
    r"(?:public|private|protected|static|final|abstract|default|synchronized|\s)*"
    r"[\w$<>\[\],.\s?]+?\s+(\w+)\s*\([^;]*?\)\s*(?:throws [\w,\s.]+)?\{",
    re.MULTILINE,
)


def verificar_simbolos() -> None:
    """Atrapa las llamadas sin receptor a metodos que la clase no declara.

    Es el error que `javac` reporta como 'cannot find symbol' y que cuesta una
    vuelta entera de compilacion. Solo mira llamadas simples (sin punto
    delante), que por definicion tienen que estar declaradas en la propia
    clase o heredadas; las heredadas de Screen y compania se declaran en
    HEREDADAS para no gritar de mas.
    """
    heredadas = {
        # Screen / GuiComponent / AbstractWidget de Minecraft.
        "addRenderableWidget", "renderBackground", "renderTransparentBackground",
        "addWidget", "addRenderableOnly", "removeWidget", "clearWidgets",
        "onClose", "minecraft", "setFocused", "getFocused", "isFocused",
        "setTooltip", "setMessage", "getMessage", "visitWidgets", "rebuildWidgets",
        "width", "height", "getX", "getY", "getWidth", "getHeight",
        "isHovered", "isActive", "active", "playDownSound", "setX", "setY",
        "defaultButtonNarrationText", "createNarrationMessage", "getRectangle",
        "font", "children", "init", "tick", "render", "onPress",
    }

    for ruta in archivos_java():
        relativa = ruta.relative_to(RAIZ)
        limpio = despojar(leer(ruta))

        declaradas = set(DECLARACION.findall(limpio))
        llamadas: set[str] = set()

        for coincidencia in re.finditer(r"(?<![.\w$])(\w+)\s*\(", limpio):
            nombre = coincidencia.group(1)
            if nombre in PALABRAS_CLAVE or nombre in heredadas:
                continue
            # Los constructores y los tipos empiezan en mayuscula.
            if nombre[0].isupper():
                continue
            antes = limpio[max(0, coincidencia.start() - 6):coincidencia.start()]
            if re.search(r"\bnew\s+$", antes):
                continue
            llamadas.add(nombre)

        for nombre in sorted(llamadas - declaradas):
            fallo(
                f"{relativa}: se llama a '{nombre}()' y la clase no lo declara "
                f"(javac diria 'cannot find symbol')."
            )


def main() -> int:
    props = propiedades()
    verificar_versiones(props)
    verificar_mods_toml(props)
    es = verificar_idiomas()
    verificar_claves(es)
    verificar_java()
    verificar_simbolos()
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
