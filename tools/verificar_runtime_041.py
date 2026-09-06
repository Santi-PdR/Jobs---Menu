#!/usr/bin/env python3
"""Contratos 0.41: runtime liviano, audio puntual y continuidad de lista."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, token: str, where: str) -> None:
    if token not in text:
        raise RuntimeError(f"{where} perdio el contrato 0.41: {token}")


def forbid(text: str, token: str, where: str) -> None:
    if token in text:
        raise RuntimeError(f"{where} recupero una regresion 0.41: {token}")


def main() -> None:
    tracker_path = JAVA / "client/sound/RastreadorAudioJobs.java"
    if not tracker_path.is_file():
        raise RuntimeError("Falta RastreadorAudioJobs: los FX puntuales volverian a quedar huerfanos.")
    tracker = read(tracker_path)
    for token in (
        "private static final List<SoundInstance> PUNTUALES",
        "purgarFinalizados();",
        "sonidos.isActive(instancia)",
        "sonidos.stop(instancia);",
        "public static void recursosRecargados()",
        "public static int cantidad()",
    ):
        require(tracker, token, "RastreadorAudioJobs")

    mezcla = read(JAVA / "client/sound/MezclaAudio.java")
    for token in (
        "public static SoundInstance ambiental",
        "SoundEvent sonido = resolver(evento, null);",
        "RastreadorAudioJobs.registrar(",
        "public static void recursosRecargados()",
    ):
        require(mezcla, token, "MezclaAudio")
    forbid(mezcla, "SoundEvents.AMBIENT_CAVE", "MezclaAudio")

    sesion = read(JAVA / "client/SesionMenu.java")
    for token in (
        "boolean necesitaCierre = activa",
        "GestorMusica.sonando()",
        "GestorAmbiente.capasActivas() > 0",
        "RastreadorAudioJobs.cantidad() > 0",
        "if (!necesitaCierre) return;",
        "RastreadorAudioJobs.detenerTodo();",
        "cierresEfectivosParaDiagnostico()",
    ):
        require(sesion, token, "SesionMenu")

    reload = read(JAVA / "client/RecargaRecursosCliente.java")
    for token in (
        "RastreadorAudioJobs.recursosRecargados();",
        "MezclaAudio.recursosRecargados();",
        "AtomicLong GENERACION",
    ):
        require(reload, token, "RecargaRecursosCliente")

    config = read(JAVA / "config/ConfigTurno.java")
    for token in (
        "if (destino.get() == valor)",
        "cambiosOmitidos++;",
        "cambiosAplicados++;",
        "guardadosRealizados++;",
        "guardadoPendienteParaDiagnostico()",
        "ultimoGuardadoHaceMsParaDiagnostico()",
        "if (!cambio)",
    ):
        require(config, token, "ConfigTurno")

    escucha = read(JAVA / "client/EscuchaCliente.java")
    for token in (
        "BOTONES_HOVER_VANILLA",
        "ScreenEvent.Init.Post",
        "reconstruirHoverVanilla(pantalla, hijos);",
        "for (AbstractButton boton : BOTONES_HOVER_VANILLA)",
        "invalidarHoverVanilla(pantalla);",
    ):
        require(escucha, token, "EscuchaCliente")

    multi = read(JAVA / "client/screen/PantallaMultijugadorJobs.java")
    for token in (
        "private final double scrollPreferido;",
        "restaurarScrollPreferido();",
        "setScrollAmount(this.scrollPreferido)",
        "getScrollAmount()",
        "boolean cambiado = false;",
        "if (cambiado) {",
        "padreDestino(), seleccion, scroll",
    ):
        require(multi, token, "PantallaMultijugadorJobs")

    diag = read(JAVA / "client/DiagnosticoOculto.java")
    for token in (
        "RastreadorAudioJobs.purgadosParaDiagnostico()",
        "ConfigTurno.cambiosOmitidosParaDiagnostico()",
        "SesionMenu.cierresEfectivosParaDiagnostico()",
    ):
        require(diag, token, "DiagnosticoOculto")

    print("OK runtime/audio/config/Multiplayer 0.41")


if __name__ == "__main__":
    main()
