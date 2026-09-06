#!/usr/bin/env python3
"""Contratos vigentes de UI, navegacion, audio y catalogo Jobs."""
from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src/main/java/com/santipdr/jobsmenu"
RES = ROOT / "src/main/resources/assets/jobsmenu"


def fail(message: str) -> None:
    raise RuntimeError(message)


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(text: str, tokens: tuple[str, ...], where: str) -> None:
    for token in tokens:
        if token not in text:
            fail(f"{where} perdio el contrato: {token}")


def verify_neutral_ui() -> None:
    palette = read(JAVA / "client/ui/Paleta.java")
    require(palette, (
        "UI_PAPEL", "UI_PAPEL_FOCO", "UI_TINTA", "UI_TINTA_TENUE",
        "UI_ACENTO", "UI_ACENTO_FUERTE", "ARCHIVO_FONDO",
        "ARCHIVO_SUPERFICIE", "ARCHIVO_SUPERFICIE_FOCO",
    ), "Paleta")

    files = (
        JAVA / "client/ui/ChromeExpediente.java",
        JAVA / "client/ui/PielVanillaJobs.java",
        JAVA / "client/ui/BotonExpediente.java",
        JAVA / "client/ui/ToggleExpediente.java",
        JAVA / "client/ui/SliderExpediente.java",
        JAVA / "client/ui/PulidoInterfazJobs.java",
        JAVA / "client/ui/TransicionInterfazJobs.java",
        JAVA / "client/screen/PantallaIdiomaJobs.java",
        JAVA / "client/screen/PantallaAjustesAviso.java",
    )
    for path in files:
        text = read(path)
        for token in ("Paleta.PARED_ALTA", "Paleta.PARED", "Paleta.FLUOR"):
            if token in text:
                fail(f"{path.relative_to(ROOT)} vuelve a mezclar paleta de escena en UI: {token}")

    skin = read(JAVA / "client/ui/PielVanillaJobs.java")
    require(skin, ("ARCHIVO_SUPERFICIE", "ARCHIVO_SUPERFICIE_FOCO", "esArchivoOscuro"),
            "PielVanillaJobs")

    polish = read(JAVA / "client/ui/PulidoInterfazJobs.java")
    require(polish, ("ConfigTurno.movimientoReducido()", "ConfigTurno.bajoConsumo()", "Math.sin"),
            "PulidoInterfazJobs")

    transition = read(JAVA / "client/ui/TransicionInterfazJobs.java")
    require(transition, ("t * t * (3.0F - 2.0F * t)", "ConfigTurno.bajoConsumo()", "Paleta.UI_PAPEL"),
            "TransicionInterfazJobs")

    for filename in ("BotonExpediente.java", "ToggleExpediente.java", "SliderExpediente.java"):
        widget = read(JAVA / "client/ui" / filename)
        require(widget, ("ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()",), filename)

    config_ui = read(JAVA / "client/screen/PantallaAjustesAviso.java")
    if "JOBS-0161" in config_ui or "JOBS-CONFIG" not in config_ui:
        fail("PantallaAjustesAviso perdio la identidad estable de configuracion.")


def verify_robustness() -> None:
    language = read(JAVA / "client/screen/PantallaIdiomaJobs.java")
    require(language, ("reloadResourcePacks().whenComplete", "this.aplicando = false",
                       "GLFW.GLFW_KEY_KP_ENTER"), "PantallaIdiomaJobs")

    multiplayer = read(JAVA / "client/screen/PantallaMultijugadorJobs.java")
    require(multiplayer, ("int util = Math.max(1, this.panelW - margen * 2)",
                          "ChromeExpediente.ajustar", "this.panelW < 300"),
            "PantallaMultijugadorJobs")
    if "Math.max(240, this.panelW - margen * 2)" in multiplayer:
        fail("Multiplayer recupero el ancho minimo que desborda ventanas angostas.")

    listener = read(JAVA / "client/EscuchaCliente.java")
    require(listener, ("SesionMenu.cerrar();\n            return;", "PlaySoundEvent",
                       "MezclaAudio.reemplazoClickVanilla()", "evento.setSound"),
            "EscuchaCliente")

    mix = read(JAVA / "client/sound/MezclaAudio.java")
    if "SoundEvents.UI_BUTTON_CLICK" in mix:
        fail("MezclaAudio volvio a usar el click vanilla como fallback.")
    require(mix, ("resolverPersonalizado", "tonoGesto", "reemplazoClickVanilla"), "MezclaAudio")

    settings = read(JAVA / "client/screen/PantallaAjustesAviso.java")
    require(settings, ('"jobsmenu.ajustes.nivelfijo.detalle", 0, 31',
                       '"jobsmenu.ajustes.pista.detalle", 0, 3', "toggleCuatro",
                       "PantallaAjustesAviso::fijarSonidoBotones",
                       'v -> Component.literal(v + "/31")',
                       'v -> Component.literal(v + "/3")'), "PantallaAjustesAviso")

    slider = read(JAVA / "client/ui/SliderExpediente.java")
    require(slider, ("lecturaCorta", "this.lecturaCorta.apply(valorEntero())"), "SliderExpediente")
    if 'Math.round(this.value * 100.0D) + "%"' in slider:
        fail("SliderExpediente recupero porcentajes falsos para todos los controles.")

    options = read(JAVA / "client/screen/PantallaOpcionesJobs.java")
    require(options, ("public final class PantallaOpcionesJobs extends Screen",
                      "CompatGraficos.crearPantallaEmbeddium(this.minecraft, this)",
                      "new VideoSettingsScreen(this, this.opciones)",
                      "private boolean cerrando;",
                      "if (this.cerrando || this.minecraft == null) return;",
                      "this.minecraft.screen instanceof PantallaPaquetesJobs",
                      "anchoUtil, bh,\n                \"options.online.title\""),
            "PantallaOpcionesJobs")
    for token in ("extends OptionsScreen", "super.init();", "botonVideoNatural",
                  "sincronizarControlesNaturales", "coincideRanuraVideo", "recordarVideo",
                  "integracionNaturalFinalizada", "MODPACK", "abrirOpcionesModpack",
                  "permitirOptionsNaturalUnaVez"):
        if token in options:
            fail(f"PantallaOpcionesJobs recupero el flujo eliminado: {token}")

    if (JAVA / "client/screen/PantallaVideoJobs.java").exists():
        fail("PantallaVideoJobs no debe existir: la GUI grafica no se tematiza.")

    compat = read(JAVA / "client/CompatGraficos.java")
    require(compat, ("ConfigScreenHandler.ConfigScreenFactory.class",
                     "getModContainerById(EMBEDDIUM_ID)",
                     "factory.screenFunction().apply(minecraft, anterior)",
                     "pantalla != null && pantalla != anterior"), "CompatGraficos")
    for token in ("Class.forName", "SodiumOptionsGUI", "EmbeddiumVideoOptionsScreen"):
        if token in compat:
            fail(f"CompatGraficos enlaza una clase interna del proveedor: {token}")

    require(listener, ("esPantallaTerceros", "esSuperficieAjenaIntocable",
                       "pantalla instanceof VideoSettingsScreen",
                       '!clase.startsWith("net.minecraft.")',
                       '!clase.startsWith("net.minecraftforge.")',
                       "esSuperficieAjenaIntocable(pantalla)",
                       "private static boolean flujoExternoActivo;",
                       "boolean flujoExternoActual = flujoExternoActivo || esPantallaTerceros(anterior);"),
            "EscuchaCliente")
    for token in ("permitirOptionsNaturalUnaVez", "optionsNaturalSolicitado",
                  "me.jellysquid.mods.sodium", "org.embeddedt.embeddium",
                  "net.coderbot.iris", "net.irisshaders.iris"):
        if token in listener:
            fail(f"EscuchaCliente recupero una dependencia/ruta grafica obsoleta: {token}")

    require(multiplayer, ("conectarSeleccionado", "ConnectScreen.startConnecting(this, this.minecraft",
                          "ServerAddress.parseString(servidor.ip)", "private final Screen pantallaPadre;",
                          "cerrarAlPadre()", "this.minecraft.setScreen(padreDestino())",
                          "private Screen padreDestino()", "refrescarLista()",
                          "if (this.cerrando) return;", "this.cerrando = false;"),
            "PantallaMultijugadorJobs")
    for token in ("super.onClose();", "this.realRefresh.onPress()", "pulsar(this.realRefresh)",
                  "anteriorJobs", "volverAlMenu()", "GLFW.GLFW_KEY_ESCAPE"):
        if token in multiplayer:
            fail(f"Multiplayer recupero una ruta fragil: {token}")

    transition = read(JAVA / "client/ui/TransicionInterfazJobs.java")
    require(listener, ("usaTransicionJobs", "TransicionInterfazJobs.cancelar()",
                       "if (Minecraft.getInstance().level != null && !propia) return;",
                       "if (Minecraft.getInstance().level != null) return false;",
                       "Minecraft.getInstance().level == null && !esSuperficieAjenaIntocable(siguiente)",
                       "esSuperficieAjenaIntocable(desde)", "esSuperficieAjenaIntocable(hasta)",
                       "if (cliente.level == null) {\n            TransicionInterfazJobs.dibujar(pantalla, evento.getGuiGraphics());"),
            "EscuchaCliente")
    require(transition, ("public static void cancelar()",), "TransicionInterfazJobs")


def verify_main_overlay_layout() -> None:
    main_screen = read(JAVA / "client/screen/PantallaNivel.java")
    require(main_screen, ("ALTO_ESTADO_RESERVADO", "ANCHO_CREDITO_MINIMO", "ALTO_CREDITO_MINIMO",
                          "this.compacta || this.width < ANCHO_CREDITO_MINIMO",
                          "autor.getString().isBlank()",
                          "int reservaEstado = ConfigTurno.mostrarEstadoInstalacion()",
                          "this.height - MARGEN_ROTULO - reservaEstado - altoBloque"),
            "PantallaNivel")


def verify_music_session() -> None:
    manager = read(JAVA / "client/sound/GestorMusica.java")
    require(manager, ('new Pista("absurdism", SonidosNivel.MUSICA_TEMA, "musica/defecto.ogg",',
                      'new Pista("requiem", SonidosNivel.MUSICA_REQUIEM, "musica/requiem.ogg",',
                      'new Pista("upon_the_hill_v2", SonidosNivel.MUSICA_UPON_HILL,',
                      "adelantarPista()", "siguienteIndice(Pista[] pistas)", "tituloPistaActual()",
                      "SUAVIZADO_SUBIDA", "SUAVIZADO_BAJADA", "SUAVIZADO_CROSSFADE",
                      "atenderCrossfade()", "gananciaObjetivo", "canStartSilent()",
                      "detenerAhora()", "cliente.getMusicManager().stopPlaying()",
                      "ConfigTurno.pistaMusica()", "sincronizarSeleccion()", "indiceFijado",
                      "boolean permiteRotacion"), "GestorMusica")

    config = read(JAVA / "config/ConfigTurno.java")
    require(config, ('defineInRange("pista_musica", 0, 0, 3)', "fijarPistaMusica", "pistaMusica()"),
            "ConfigTurno")

    sounds = json.loads(read(RES / "sounds.json"))
    expected = {
        "musica.tema": "jobsmenu:musica/defecto",
        "musica.requiem": "jobsmenu:musica/requiem",
        "musica.upon_hill": "jobsmenu:musica/upon_the_hill_v2",
    }
    for event_name, resource in expected.items():
        event = sounds.get(event_name)
        if not isinstance(event, dict):
            fail(f"sounds.json no contiene {event_name}.")
        names = [item if isinstance(item, str) else item.get("name")
                 for item in event.get("sounds", []) if isinstance(item, (str, dict))]
        if resource not in names:
            fail(f"{event_name} no apunta a {resource}.")

    for filename in ("defecto.ogg", "requiem.ogg", "upon_the_hill_v2.ogg"):
        ogg = RES / "sounds/musica" / filename
        if not ogg.is_file() or ogg.stat().st_size < 64:
            fail(f"Falta pista empaquetada: {filename}")

    music_doc = read(ROOT / "docs/musica.md")
    require(music_doc, ("Absurdism", "REQUIEM", "Upon the Hill V2",
                        "music/upon_the_hill_v2_q4.ogg"), "docs/musica.md")


def verify_ambient_catalog() -> None:
    manager = read(JAVA / "client/sound/GestorAmbiente.java")
    layer = read(JAVA / "client/sound/CapaAmbiente.java")
    for level in range(18, 32):
        if manager.count(f"case {level}:") < 3:
            fail(f"Nivel {level} no tiene tres camas ambientales explicitas.")
        if manager.count(f"case {level} ->") < 2:
            fail(f"Nivel {level} no tiene repertorio/espera ambiental explicitos.")
        if f"case {level} -> papel" not in layer:
            fail(f"Nivel {level} no tiene balance de capas propio.")
    require(layer, ("tonoNivel(nivel, papel)", "private static float tonoNivel"), "CapaAmbiente")


def verify_audio_sources() -> None:
    for path in (ROOT / "music/REQUIEM-Forsaken-OST.ogg", ROOT / "music/upon_the_hill_v2_q4.ogg"):
        if not path.is_file() or path.stat().st_size < 64:
            fail(f"Falta fuente OGG autorizada: {path.relative_to(ROOT)}")
    for path in (ROOT / ".github/workflows/integrar_pista_autorizada.yml",
                 ROOT / "tools/cobalt_transport.py", ROOT / "tools/integrar_pista.trigger"):
        if path.exists():
            fail(f"Sigue presente infraestructura de audio obsoleta: {path.relative_to(ROOT)}")


def main() -> int:
    try:
        verify_neutral_ui()
        verify_robustness()
        verify_main_overlay_layout()
        verify_music_session()
        verify_ambient_catalog()
        verify_audio_sources()
        print("UI, graficos intocables, aislamiento de terceros y audio Jobs: OK")
        print("Composicion adaptativa y catalogo musical/ambiental: OK")
        return 0
    except Exception as exc:
        print(f"ERROR verificacion UI/musica: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
