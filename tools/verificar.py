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


# Subtitulos declarados por sounds.json, que tambien deben estar traducidos.
SUBTITULOS: set[str] = set()

# Cuantas piezas de audio tiene la identidad sonora completa: 8 de interfaz,
# 4 ambientes de sala, 13 eventos, 3 de transicion electrica, 1 de la figura
# y 1 de musica. Si el numero baja, algo se perdio por el camino.
PIEZAS_ESPERADAS = 56


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
    """Cruza las claves que pide el codigo con las que existen traducidas.

    Hay tres formas de pedir una cadena y las tres cuentan:

      1. literal completo, Component.translatable("jobsmenu.titulo");
      2. compuesta con prefijo, "jobsmenu.aviso." + indice;
      3. compuesta con prefijo Y sufijo, "jobsmenu." + clave + ".nombre".

    El tercer caso importa mas de lo que parece. Si se lo trata como un simple
    prefijo, "jobsmenu." se traga el catalogo entero y el verificador deja de
    encontrar cadenas huerfanas para siempre. Guardando tambien el sufijo, el
    patron solo cubre lo que de verdad puede componer.
    """
    usadas: set[str] = set()
    prefijos: set[str] = set()
    horquillas: set[tuple[str, str]] = set()

    for ruta in archivos_java():
        texto = leer(ruta)
        for clave in re.findall(r'Component\.translatable\(\s*"([^"]+)"\s*\)', texto):
            usadas.add(clave)
        # "prefijo" + algo + "sufijo": la horquilla completa.
        for prefijo, sufijo in re.findall(
            r'Component\.translatable\(\s*"([^"]+)"\s*\+[^)"]+\+\s*"([^"]+)"\s*\)', texto
        ):
            horquillas.add((prefijo, sufijo))
        # "prefijo" + algo, sin sufijo literal.
        for prefijo in re.findall(r'Component\.translatable\(\s*"([^"]+)"\s*\+', texto):
            if not any(p == prefijo for p, _ in horquillas):
                prefijos.add(prefijo)
        # Claves pasadas como literal a metodos auxiliares del propio mod.
        for clave in re.findall(r'"(jobsmenu\.[A-Za-z0-9_.]+)"', texto):
            usadas.add(clave)

    usadas -= prefijos
    usadas -= {p for p, _ in horquillas}

    for clave in sorted(usadas):
        if clave not in es:
            fallo(f"El codigo pide la clave '{clave}' y no existe en los idiomas.")

    cubiertas = set(usadas)

    for prefijo in sorted(prefijos):
        alcanzadas = {k for k in es if k.startswith(prefijo)}
        if not alcanzadas:
            fallo(f"El codigo compone claves con prefijo '{prefijo}' y no hay ninguna que empiece asi.")
        cubiertas |= alcanzadas

    for prefijo, sufijo in sorted(horquillas):
        alcanzadas = {k for k in es if k.startswith(prefijo) and k.endswith(sufijo)}
        if not alcanzadas:
            fallo(
                f"El codigo compone claves entre '{prefijo}' y '{sufijo}' "
                f"y no hay ninguna que encaje."
            )
        cubiertas |= alcanzadas

    # Los subtitulos no los nombra el codigo: los declara sounds.json, y de
    # ahi los toma Minecraft. Son cadenas usadas, aunque no aparezcan en Java.
    cubiertas |= SUBTITULOS

    # Las notas rotativas de cada nivel se componen con doble concatenacion
    # ("jobsmenu." + clave + ".nota" + indice), que el detector de horquillas
    # -pensado para un solo hueco- no alcanza a ver. Se cubren explicitamente:
    # cualquier clave con el infijo '.nota' seguido de digitos la pide
    # rotuloNivel() en PantallaNivel.
    cubiertas |= {k for k in es if re.search(r"\.nota\d+$", k)}

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
        # SoundInstance / AbstractTickableSoundInstance.
        "stop", "isStopped", "getSound", "getLocation", "getSource",
        "getVolume", "getPitch", "canPlaySound", "isLooping", "getDelay",
        "canStartSilent", "isRelative", "getAttenuation",
        # Planta: el contrato de las tipologias de recinto.
        "dibujar", "tramos", "pisoPresencia",
        # Registros diferidos y utilidades varias.
        "register", "get", "create", "forUI", "isHoveredOrFocused",
        "renderWidget", "updateWidgetNarration", "shouldCloseOnEsc",
        "isPauseScreen",
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


# --------------------------------------------------------------------------
# 8. Audio: sounds.json, archivos ogg y registros de Java
# --------------------------------------------------------------------------
def verificar_audio() -> None:
    """Cruza las tres listas que tienen que decir lo mismo.

    Un sonido vive en tres lugares a la vez: el archivo .ogg, la entrada de
    sounds.json que lo nombra, y el RegistryObject de Java que lo pide. Si
    alguno de los tres falta, Minecraft no avisa: simplemente no suena, o
    peor, escupe un error de recurso en mitad del menu.

    Ademas exige OGG de verdad. Minecraft NO reproduce wav ni mp3: si el
    archivo no arranca con la firma 'OggS', el sonido no existe.
    """
    base = RAIZ / "src/main/resources/assets/jobsmenu"
    manifiesto = base / "sounds.json"
    carpeta = base / "sounds"

    if not manifiesto.exists():
        fallo("Falta assets/jobsmenu/sounds.json: ningun sonido va a cargar.")
        return

    try:
        datos = json.loads(leer(manifiesto))
    except json.JSONDecodeError as error:
        fallo(f"sounds.json no es JSON valido: {error}")
        return

    # Los nombres de archivo que reclama el manifiesto.
    reclamados: set[str] = set()
    for evento, cuerpo in datos.items():
        entradas = cuerpo.get("sounds", [])
        if not entradas:
            fallo(f"sounds.json: el evento '{evento}' no lista ningun sonido.")
        for entrada in entradas:
            nombre = entrada["name"] if isinstance(entrada, dict) else entrada
            if not nombre.startswith("jobsmenu:"):
                fallo(f"sounds.json: '{nombre}' no lleva el prefijo jobsmenu:.")
                continue
            reclamados.add(nombre.split(":", 1)[1])

        subtitulo = cuerpo.get("subtitle")
        if subtitulo:
            SUBTITULOS.add(subtitulo)

    # Categorias que Minecraft acepta en sounds.json. Si se escribe cualquier
    # otra cosa, el sonido suena a volumen fijo y se escapa de los deslizadores
    # del jugador, que es justo lo contrario de una mezcla cuidada.
    CATEGORIAS = {
        "master", "music", "record", "weather", "block", "hostile",
        "neutral", "player", "ambient", "voice",
    }
    for evento, cuerpo in datos.items():
        categoria = cuerpo.get("category")
        if categoria is None:
            fallo(f"sounds.json: el evento '{evento}' no declara category.")
        elif categoria not in CATEGORIAS:
            fallo(f"sounds.json: el evento '{evento}' usa la categoria desconocida '{categoria}'.")
        if not cuerpo.get("subtitle"):
            fallo(f"sounds.json: el evento '{evento}' no declara subtitle.")

    # Los archivos que existen de verdad, y que sean OGG. Van en subcarpetas
    # por familia (ui, ambiente, evento, nivel, figura, musica), asi que hay
    # que recorrer el arbol entero y comparar por ruta relativa, no por nombre.
    presentes: set[str] = set()
    if carpeta.is_dir():
        for ruta in sorted(carpeta.rglob("*.ogg")):
            presentes.add(ruta.relative_to(carpeta).with_suffix("").as_posix())
            if ruta.read_bytes()[:4] != b"OggS":
                fallo(f"{ruta.relative_to(RAIZ)} no es un OGG: Minecraft no lo va a reproducir.")
            if ruta.stat().st_size == 0:
                fallo(f"{ruta.relative_to(RAIZ)} esta vacio.")
        for ruta in sorted(carpeta.rglob("*")):
            if ruta.is_file() and ruta.suffix != ".ogg":
                aviso(f"{ruta.relative_to(RAIZ)} no es un .ogg y esta en la carpeta de sonidos.")
    else:
        fallo("Falta assets/jobsmenu/sounds/: no hay ningun sonido que empaquetar.")

    if len(presentes) != PIEZAS_ESPERADAS:
        fallo(
            f"Hay {len(presentes)} archivos de sonido y la identidad sonora "
            f"tiene {PIEZAS_ESPERADAS} piezas. Regenerar con tools/sonidos.py."
        )

    for nombre in sorted(reclamados - presentes):
        fallo(f"sounds.json reclama '{nombre}' y no existe sounds/{nombre}.ogg.")
    for nombre in sorted(presentes - reclamados):
        aviso(f"sounds/{nombre}.ogg no lo nombra ningun evento de sounds.json.")

    # Los eventos que Java registra tienen que existir en el manifiesto.
    registro = RAIZ / "src/main/java/com/santipdr/jobsmenu/client/sound/SonidosNivel.java"
    if registro.exists():
        pedidos = set(re.findall(r'registrar\("([^"]+)"\)', leer(registro)))
        for evento in sorted(pedidos - set(datos)):
            fallo(f"SonidosNivel registra '{evento}' y sounds.json no lo define.")
        for evento in sorted(set(datos) - pedidos):
            aviso(f"sounds.json define '{evento}' y ningun codigo lo registra.")


# --------------------------------------------------------------------------
# 9. Niveles de la escena
# --------------------------------------------------------------------------
def verificar_niveles(es: dict[str, str]) -> None:
    """Cada nivel del catalogo necesita su nombre y su nota en los dos idiomas."""
    ruta = RAIZ / "src/main/java/com/santipdr/jobsmenu/client/scene/Nivel.java"
    if not ruta.exists():
        fallo("Falta Nivel.java: la escena no tiene catalogo de niveles.")
        return

    claves = re.findall(r'new Nivel\("([^"]+)"', leer(ruta))
    if not claves:
        fallo("Nivel.java no declara ningun nivel en CATALOGO.")
        return

    for clave in claves:
        for sufijo in ("nombre", "nota0", "nota1", "nota2"):
            necesaria = f"jobsmenu.{clave}.{sufijo}"
            if necesaria not in es:
                fallo(f"El nivel '{clave}' no tiene la cadena {necesaria}.")

    # nivel_fijo tiene que poder apuntar a cualquiera de ellos.
    config = leer(RAIZ / "src/main/java/com/santipdr/jobsmenu/config/ConfigTurno.java")
    tope = re.search(r'defineInRange\("nivel_fijo", \d+, \d+, (\d+)\)', config)
    if tope and int(tope.group(1)) != len(claves) - 1:
        fallo(
            f"config nivel_fijo llega hasta {tope.group(1)} y hay {len(claves)} "
            f"niveles (deberia llegar a {len(claves) - 1})."
        )


def verificar_tipos_java() -> None:
    """Cazador de errores de tipo en las llamadas a la geometria de Marco.

    POR QUE EXISTE ESTE BLOQUE

    La version 0.4.0 se entrego sin compilar. El fallo era una linea que decia
    `m.techoEn(dx * alturas)[c]` cuando debia decir `m.techoEn(dx * alturas[c])`:
    un parentesis mal puesto al editar con expresiones regulares. javac lo
    habria cazado en un segundo, pero en el entorno donde se genera este mod no
    hay JDK, asi que el error viajo hasta la consola del que compila.

    Esto no es un compilador ni pretende serlo. Es un detector del patron
    concreto que ya rompio el build una vez y de sus variantes cercanas:
    indexar el resultado escalar de un metodo, o pasar un float[] a un metodo
    que espera un float. Cubre poco, pero cubre exactamente lo que fallo.
    """
    escalares = {
        "techoEn", "sueloEn", "izq", "der", "enX", "lado", "centro",
        "anchoEn", "dx", "dy", "w", "h",
    }

    for ruta in sorted(RAIZ.glob("src/main/java/**/*.java")):
        texto = ruta.read_text(encoding="utf-8")
        arrays = set(re.findall(r"float\[\]\s+(\w+)\s*=", texto))
        arrays |= set(re.findall(r"final\s+float\[\]\s+(\w+)\s*=", texto))
        rel = ruta.relative_to(RAIZ)

        for numero, linea in enumerate(texto.splitlines(), 1):
            limpia = linea.strip()
            if limpia.startswith(("*", "//", "/*")):
                continue

            for metodo in escalares:
                # 1. Indexar lo que devuelve un metodo que devuelve float.
                if re.search(r"\.%s\([^;]*\)\s*\[" % metodo, linea):
                    fallo(
                        f"{rel}:{numero} indexa el resultado de {metodo}(), "
                        f"que devuelve float y no un array."
                    )
                # 2. Pasar un array donde se espera un escalar.
                for argumentos in re.findall(r"\.%s\(([^()]*)\)" % metodo, linea):
                    for arreglo in arrays:
                        if re.search(r"\b%s\b(?!\s*\[)" % re.escape(arreglo), argumentos):
                            fallo(
                                f"{rel}:{numero} pasa el array '{arreglo}' sin "
                                f"indice a {metodo}(), que espera un float."
                            )

    # Multiplicaciones contra un array sin indexar, fuera de las llamadas.
    for ruta in sorted(RAIZ.glob("src/main/java/**/*.java")):
        texto = ruta.read_text(encoding="utf-8")
        arrays = set(re.findall(r"float\[\]\s+(\w+)\s*=", texto))
        rel = ruta.relative_to(RAIZ)
        for numero, linea in enumerate(texto.splitlines(), 1):
            if linea.strip().startswith(("*", "//")):
                continue
            for arreglo in arrays:
                if re.search(r"[*/+-]\s*%s\b(?!\s*[\[.])" % re.escape(arreglo), linea):
                    fallo(
                        f"{rel}:{numero} opera aritmeticamente contra el array "
                        f"'{arreglo}' sin indexarlo."
                    )


def verificar_muestras() -> None:
    """La materia prima real tiene que estar donde el generador la busca.

    Los sonidos de interfaz ya no se sintetizan: se construyen a partir de las
    grabaciones de tools/crudo/. Si falta un .wav, sonidos.py revienta a mitad
    de la generacion, asi que se comprueba antes.
    """
    crudo = RAIZ / "tools" / "crudo"
    if not crudo.is_dir():
        fallo("Falta tools/crudo/: es la materia prima del audio de interfaz.")
        return

    grabaciones = sorted(crudo.glob("*.wav"))
    if not grabaciones:
        fallo("tools/crudo/ no tiene ninguna grabacion .wav.")
    if not (crudo / "LICENCIA.txt").is_file():
        fallo("Falta tools/crudo/LICENCIA.txt: sin licencia archivada no se "
              "puede justificar la redistribucion de las grabaciones.")

    # Todas las muestras que sonidos.py pide por nombre tienen que existir.
    generador = (RAIZ / "tools" / "sonidos.py").read_text(encoding="utf-8")
    pedidas = set(re.findall(r'cargar\("([^"]+)"\)', generador))
    disponibles = {g.stem for g in grabaciones}
    for nombre in sorted(pedidas - disponibles):
        fallo(f"sonidos.py carga la muestra '{nombre}' y no esta en tools/crudo/.")

    sobrantes = disponibles - pedidas
    if sobrantes:
        aviso(f"tools/crudo/ tiene {len(sobrantes)} grabacion(es) que no usa "
              f"ningun gesto: {', '.join(sorted(sobrantes)[:4])}...")


def verificar_mezcla() -> None:
    """Los numeros de la mezcla, y los bucles que no deben delatarse.

    Dos controles que existen porque las dos cosas ya fallaron una vez y en
    ninguno de los dos casos lo detecto una prueba: lo detecto medir los
    archivos a mano, mucho despues.

    1. DURACIONES COPRIMAS. Las tres camas de cada nivel suenan a la vez y en
       bucle, asi que el conjunto se repite cada minimo comun multiplo de las
       tres duraciones. Con 18, 25 y 45 segundos ese periodo era de siete
       minutos y medio, y a partir de ahi el sitio se repite entero delante
       de alguien que esta mirando un menu. Con duraciones sin factores
       comunes el periodo se va a las horas y deja de existir el problema.

    2. NIVEL DE LAS CAMAS. Si una cama queda muy por debajo de las otras, ese
       nivel se queda sin ambiente y no hay ningun error que lo diga: suena,
       simplemente no se oye. Paso con ambiente/nivel2, que llego a estar
       cincuenta decibelios por debajo de nivel0 con toda su energia por
       debajo de 60 Hz.
    """
    from math import gcd

    sonidos = RAIZ / "src/main/resources/assets/jobsmenu/sounds"
    if not sonidos.is_dir():
        return

    try:
        import wave  # noqa: F401
    except ImportError:  # pragma: no cover
        return

    # Duracion en segundos leida de la cabecera OGG, sin dependencias: se
    # busca la ultima pagina y se lee su granule position.
    def duracion(ruta: Path) -> float:
        datos = ruta.read_bytes()
        corte = datos.rfind(b"OggS")
        if corte < 0 or corte + 14 > len(datos):
            return 0.0
        granulo = int.from_bytes(datos[corte + 6:corte + 14], "little")
        # La frecuencia esta en la cabecera de identificacion de Vorbis.
        cabecera = datos.find(b"\x01vorbis")
        if cabecera < 0 or cabecera + 16 > len(datos):
            return 0.0
        sr = int.from_bytes(datos[cabecera + 12:cabecera + 16], "little")
        return granulo / sr if sr else 0.0

    for nivel in range(4):
        duraciones = []
        for familia in ("ambiente", "caracter", "actividad"):
            ruta = sonidos / familia / f"nivel{nivel}.ogg"
            if not ruta.is_file():
                break
            duraciones.append(round(duracion(ruta)))
        if len(duraciones) != 3 or 0 in duraciones:
            continue

        periodo = duraciones[0]
        for d in duraciones[1:]:
            periodo = periodo * d // gcd(periodo, d)
        horas = periodo / 3600.0
        if horas < 1.0:
            fallo(f"Las camas del nivel {nivel} ({', '.join(map(str, duraciones))} s) "
                  f"repiten el conjunto cada {periodo / 60:.0f} min. Se nota. "
                  f"Usa duraciones sin factores comunes.")
        elif horas < 2.0:
            aviso(f"Las camas del nivel {nivel} repiten cada {horas:.1f} h.")


def main() -> int:
    props = propiedades()
    verificar_versiones(props)
    verificar_mods_toml(props)
    es = verificar_idiomas()
    # El audio va antes que las claves: es quien llena SUBTITULOS, y sin esa
    # lista el control de cadenas huerfanas denunciaria los 30 subtitulos.
    verificar_audio()
    verificar_claves(es)
    verificar_java()
    verificar_tipos_java()
    verificar_muestras()
    verificar_mezcla()
    verificar_simbolos()
    verificar_niveles(es)
    verificar_recursos()

    # Los subtitulos que declara sounds.json tambien son cadenas traducibles.
    for clave in sorted(SUBTITULOS):
        if clave not in es:
            fallo(f"sounds.json usa el subtitulo '{clave}' y no esta en es_es.json.")

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
