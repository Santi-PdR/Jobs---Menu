#!/usr/bin/env python3
"""Migracion idempotente de la evolucion de 18 niveles.

Se usa una sola vez desde GitHub Actions para convertir los payloads de imagen
en recursos reales y sincronizar codigo/config/idiomas. Si vuelve a ejecutarse,
no debe producir cambios adicionales.
"""
from __future__ import annotations

import json
import subprocess
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent


def leer(rel: str) -> str:
    return (RAIZ / rel).read_text(encoding="utf-8")


def escribir(rel: str, texto: str) -> None:
    ruta = RAIZ / rel
    ruta.write_text(texto, encoding="utf-8")


def reemplazar(rel: str, viejo: str, nuevo: str, obligatorio: bool = True) -> None:
    texto = leer(rel)
    if nuevo in texto:
        return
    if viejo not in texto:
        if obligatorio:
            raise RuntimeError(f"{rel}: no se encontro patron esperado: {viejo!r}")
        return
    escribir(rel, texto.replace(viejo, nuevo))


def actualizar_config() -> None:
    reemplazar(
        "src/main/java/com/santipdr/jobsmenu/config/ConfigTurno.java",
        '.defineInRange("nivel_fijo", 0, 0, 14);',
        '.defineInRange("nivel_fijo", 0, 0, 17);',
    )
    reemplazar(
        "src/main/java/com/santipdr/jobsmenu/config/ConfigTurno.java",
        'Math.max(0, Math.min(9, nivel))',
        'Math.max(0, Math.min(17, nivel))',
    )
    ruta = "src/main/java/com/santipdr/jobsmenu/client/screen/PantallaAjustesAviso.java"
    texto = leer(ruta)
    texto = texto.replace("rango 0-9", "rango 0-17")
    texto = texto.replace("new OptionInstance.IntRange(0, 9)", "new OptionInstance.IntRange(0, 17)")
    texto = texto.replace("Math.max(0, Math.min(9, valor))", "Math.max(0, Math.min(17, valor))")
    escribir(ruta, texto)


def actualizar_audio() -> None:
    rel = "src/main/java/com/santipdr/jobsmenu/client/sound/GestorAmbiente.java"
    texto = leer(rel)

    # Tres camas: cada fondo nuevo combina materiales distintos y deja de caer
    # al default del nivel 0.
    texto = texto.replace(
        """            case 14:\n                return SonidosNivel.AMBIENTE_NIVEL8;\n            default:\n""",
        """            case 14:\n                return SonidosNivel.AMBIENTE_NIVEL8;\n            case 15:\n                return SonidosNivel.AMBIENTE_NIVEL7;\n            case 16:\n                return SonidosNivel.AMBIENTE_NIVEL1;\n            case 17:\n                return SonidosNivel.AMBIENTE_NIVEL8;\n            default:\n""",
    )
    texto = texto.replace(
        """            case 14:\n                return SonidosNivel.CARACTER_NIVEL6;\n            default:\n""",
        """            case 14:\n                return SonidosNivel.CARACTER_NIVEL6;\n            case 15:\n                return SonidosNivel.CARACTER_NIVEL0;\n            case 16:\n                return SonidosNivel.CARACTER_NIVEL5;\n            case 17:\n                return SonidosNivel.CARACTER_NIVEL7;\n            default:\n""",
    )
    texto = texto.replace(
        """            case 14:\n                return SonidosNivel.ACTIVIDAD_NIVEL9;\n            default:\n""",
        """            case 14:\n                return SonidosNivel.ACTIVIDAD_NIVEL9;\n            case 15:\n                return SonidosNivel.ACTIVIDAD_NIVEL1;\n            case 16:\n                return SonidosNivel.ACTIVIDAD_NIVEL7;\n            case 17:\n                return SonidosNivel.ACTIVIDAD_NIVEL1;\n            default:\n""",
    )

    texto = texto.replace(
        """            case 14 -> REPERTORIOS[9];  // ruina, estandartes y puerta lejana\n            default -> REPERTORIOS[Math.floorMod(nivel, REPERTORIOS.length)];\n""",
        """            case 14 -> REPERTORIOS[9];  // ruina, estandartes y puerta lejana\n            case 15 -> REPERTORIOS[1];  // interferencia: estructura distante, poco literal\n            case 16 -> REPERTORIOS[7];  // prisma: piedra, aire y silencios largos\n            case 17 -> REPERTORIOS[8];  // galeria azul: eco profundo y actividad remota\n            default -> REPERTORIOS[Math.floorMod(nivel, REPERTORIOS.length)];\n""",
    )

    if "factorEsperaNivel" not in texto:
        texto = texto.replace(
            """        long espera = repertorio.esperaMin() + (long) (sesgo * ventana);\n\n        if (AZAR.nextInt(5) == 0) {\n""",
            """        long espera = repertorio.esperaMin() + (long) (sesgo * ventana);\n        espera = (long) (espera * factorEsperaNivel(nivel));\n\n        if (AZAR.nextInt(5) == 0) {\n""",
        )
        texto = texto.replace(
            """    private static float mezclar(float minimo, float maximo) {\n""",
            """    private static float factorEsperaNivel(int nivel) {\n        return switch (nivel) {\n            case 10 -> 0.90F;\n            case 11 -> 1.12F;\n            case 12 -> 0.95F;\n            case 13 -> 1.18F;\n            case 14 -> 1.05F;\n            case 15 -> 1.35F;\n            case 16 -> 1.75F;\n            case 17 -> 1.40F;\n            default -> 1.0F;\n        };\n    }\n\n    private static float mezclar(float minimo, float maximo) {\n""",
        )
    escribir(rel, texto)

    rel = "src/main/java/com/santipdr/jobsmenu/client/sound/CapaAmbiente.java"
    texto = leer(rel)
    texto = texto.replace(
        "this.pitch = 0.975F + 0.004F * nivel;",
        "this.pitch = 0.975F + 0.004F * Math.min(nivel, 9);",
    )
    if "matizNivel(this.nivel" not in texto:
        texto = texto.replace(
            """            objetivo = ConfigTurno.volumenAmbiente() * MezclaAudio.AMBIENTE\n                    * this.papel.peso * ConfigTurno.volumenAviso();\n""",
            """            objetivo = ConfigTurno.volumenAmbiente() * MezclaAudio.AMBIENTE\n                    * this.papel.peso * ConfigTurno.volumenAviso();\n            objetivo *= matizNivel(this.nivel, this.papel);\n""",
        )
        texto = texto.replace(
            """    @Override\n    public boolean canStartSilent() {\n""",
            """    private static float matizNivel(int nivel, Papel papel) {\n        if (nivel < 10) {\n            return 1.0F;\n        }\n        return switch (nivel) {\n            case 10 -> papel == Papel.ACTIVIDAD ? 1.12F : (papel == Papel.BASE ? 0.88F : 0.82F);\n            case 11 -> papel == Papel.ACTIVIDAD ? 0.82F : (papel == Papel.BASE ? 0.78F : 0.86F);\n            case 12 -> papel == Papel.ACTIVIDAD ? 1.05F : (papel == Papel.BASE ? 0.86F : 0.95F);\n            case 13 -> papel == Papel.ACTIVIDAD ? 0.92F : (papel == Papel.BASE ? 0.82F : 0.72F);\n            case 14 -> papel == Papel.ACTIVIDAD ? 0.90F : (papel == Papel.BASE ? 0.88F : 0.90F);\n            case 15 -> papel == Papel.ACTIVIDAD ? 1.18F : (papel == Papel.BASE ? 0.68F : 0.58F);\n            case 16 -> papel == Papel.ACTIVIDAD ? 0.70F : (papel == Papel.BASE ? 0.58F : 0.52F);\n            case 17 -> papel == Papel.ACTIVIDAD ? 0.95F : (papel == Papel.BASE ? 0.72F : 0.64F);\n            default -> 1.0F;\n        };\n    }\n\n    @Override\n    public boolean canStartSilent() {\n""",
        )
    escribir(rel, texto)


def actualizar_idiomas() -> None:
    es_nuevas = {
        "jobsmenu.nivel15.nombre": "NIVEL 15 · Interferencia de Executor",
        "jobsmenu.nivel15.nota0": "El campo rojo no es decorativo. La administración recomienda reducir su silueta.",
        "jobsmenu.nivel15.nota1": "Las líneas se mueven cuando nadie mira. Se mueven más rápido cuando alguien sí.",
        "jobsmenu.nivel15.nota2": "Se registró interferencia de Executor. No se pudo establecer distancia.",
        "jobsmenu.nivel16.nombre": "NIVEL 16 · El archivo del prisma",
        "jobsmenu.nivel16.nota0": "El prisma no tiene número de inventario. Aun así figura en todas las listas.",
        "jobsmenu.nivel16.nota1": "No use los reflejos para contar ocupantes. No se ponen de acuerdo.",
        "jobsmenu.nivel16.nota2": "La geometría blanca no señaliza una salida. La administración lo comprobó dos veces.",
        "jobsmenu.nivel17.nombre": "NIVEL 17 · La galería de sombra",
        "jobsmenu.nivel17.nota0": "La figura estaba allí antes de que se encendiera la sala.",
        "jobsmenu.nivel17.nota1": "No confunda una silueta inmóvil con una silueta vacía.",
        "jobsmenu.nivel17.nota2": "La luz azul alcanza el fondo. Eso no significa que el fondo termine allí.",
    }
    en_nuevas = {
        "jobsmenu.nivel15.nombre": "LEVEL 15 · Executor interference",
        "jobsmenu.nivel15.nota0": "The red field is not decorative. Administration recommends reducing your silhouette.",
        "jobsmenu.nivel15.nota1": "The lines move when nobody watches. They move faster when somebody does.",
        "jobsmenu.nivel15.nota2": "Executor interference was recorded. Distance could not be established.",
        "jobsmenu.nivel16.nombre": "LEVEL 16 · The prism archive",
        "jobsmenu.nivel16.nota0": "The prism has no inventory number. It still appears on every list.",
        "jobsmenu.nivel16.nota1": "Do not use reflections to count occupants. They do not agree.",
        "jobsmenu.nivel16.nota2": "The white geometry does not mark an exit. Administration checked twice.",
        "jobsmenu.nivel17.nombre": "LEVEL 17 · The shadow gallery",
        "jobsmenu.nivel17.nota0": "The figure was there before the room was lit.",
        "jobsmenu.nivel17.nota1": "Do not confuse a still silhouette with an empty silhouette.",
        "jobsmenu.nivel17.nota2": "The blue light reaches the far end. That does not mean the far end stops there.",
    }
    for nombre, nuevas in (("es_es.json", es_nuevas), ("en_us.json", en_nuevas)):
        rel = f"src/main/resources/assets/jobsmenu/lang/{nombre}"
        datos = json.loads(leer(rel))
        datos.update(nuevas)
        escribir(rel, json.dumps(datos, ensure_ascii=False, indent=2) + "\n")


def actualizar_verificador() -> None:
    rel = "tools/verificar.py"
    texto = leer(rel)
    texto = texto.replace("0, 0, 14", "0, 0, 17")
    texto = texto.replace("Math.min(9, nivel)", "Math.min(17, nivel)")
    escribir(rel, texto)


def actualizar_docs() -> None:
    rel = "CHANGELOG.md"
    if (RAIZ / rel).is_file():
        texto = leer(rel)
        marca = "## Unreleased · niveles 15-17"
        if marca not in texto:
            bloque = (
                f"{marca}\n\n"
                "- Reparados los fondos de imagen 10-13 corrigiendo su geometría de textura.\n"
                "- Añadidos Nivel 15 (Interferencia de Executor), Nivel 16 (Archivo del prisma) y Nivel 17 (Galería de sombra).\n"
                "- El selector fijo admite 0-17 y la rotación usa 18 niveles.\n"
                "- Nueva mezcla ambiental por escena: densidad, camas y frecuencia de sucesos ajustadas para 10-17.\n"
                "- El CI materializa y valida los PNG antes de compilar.\n\n"
            )
            escribir(rel, bloque + texto)


def main() -> None:
    subprocess.run(["python3", str(RAIZ / "tools/materializar_fondos.py")], check=True)
    actualizar_config()
    actualizar_audio()
    actualizar_idiomas()
    actualizar_verificador()
    actualizar_docs()
    print("Migracion de 18 niveles aplicada.")


if __name__ == "__main__":
    main()
