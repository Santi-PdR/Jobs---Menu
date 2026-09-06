package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.screen.PantallaEstancia;
import com.santipdr.jobsmenu.client.screen.PantallaModsJobs;
import com.santipdr.jobsmenu.client.screen.PantallaMultijugadorJobs;
import com.santipdr.jobsmenu.client.screen.PantallaMundosJobs;
import com.santipdr.jobsmenu.client.screen.PantallaNivel;
import com.santipdr.jobsmenu.client.screen.PantallaOpcionesJobs;
import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.GestorAmbiente;
import com.santipdr.jobsmenu.client.sound.GestorMusica;
import com.santipdr.jobsmenu.client.sound.LimpiezaRecursosLegados;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.client.ui.AtmosferaMenuJobs;
import com.santipdr.jobsmenu.client.ui.CapaProfesionalJobs;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.PielVanillaJobs;
import com.santipdr.jobsmenu.client.ui.PulidoInterfazJobs;
import com.santipdr.jobsmenu.client.ui.TransicionInterfazJobs;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/** La puerta del aviso: lifecycle, redirecciones, sonido y continuidad visual. */
@Mod.EventBusSubscriber(modid = JobsMenu.MOD_ID, value = Dist.CLIENT)
public final class EscuchaCliente {

    private EscuchaCliente() {
    }

    private static boolean presentado;
    private static boolean retornoDesdeJuego;
    private static boolean retornoMultijugadorPendiente;
    private static boolean enServidorRemoto;
    private static boolean flujoExternoActivo;
    private static boolean permitirOptionsNaturalUnaVez;
    private static final Set<AbstractButton> HOVER_VANILLA =
            Collections.newSetFromMap(new WeakHashMap<>());
    private static final List<AbstractButton> BOTONES_HOVER_VANILLA = new ArrayList<>();
    private static Screen pantallaHoverVanilla;
    private static int hijosHoverVistos = -1;

    /**
     * Permite abrir una unica instancia de OptionsScreen sin que Jobs la
     * sustituya. Se usa para exponer el Options natural completo del modpack,
     * incluidas inyecciones que Jobs no conoce ni debe reconstruir.
     */
    public static void permitirOptionsNaturalUnaVez() {
        permitirOptionsNaturalUnaVez = true;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void alAbrirPantalla(ScreenEvent.Opening evento) {
        Screen anterior = evento.getCurrentScreen();
        Screen siguiente = evento.getNewScreen();

        ConfigTurno.guardarPendiente();

        boolean optionsNaturalSolicitado = permitirOptionsNaturalUnaVez
                && siguiente != null && siguiente.getClass() == OptionsScreen.class;
        if (optionsNaturalSolicitado) {
            permitirOptionsNaturalUnaVez = false;
            flujoExternoActivo = true;
        } else if (permitirOptionsNaturalUnaVez) {
            // El permiso es de un solo uso y no debe contaminar una apertura
            // inesperada si otro mod cambia de Screen antes de Options.
            permitirOptionsNaturalUnaVez = false;
        }

        boolean flujoExternoActual = flujoExternoActivo
                || esPantallaTerceros(anterior)
                || optionsNaturalSolicitado;
        boolean destinoMultijugador = siguiente instanceof JoinMultiplayerScreen
                || siguiente != null && siguiente.getClass() == TitleScreen.class;
        boolean destinoRetorno = siguiente != null && (
                siguiente.getClass() == TitleScreen.class
                        || siguiente instanceof JoinMultiplayerScreen
                        || siguiente.getClass().getName().equals(
                                "net.minecraft.client.gui.screens.realms.RealmsMainScreen"));
        boolean flujoAdministrativo = !flujoExternoActual && (
                SesionMenu.activa()
                        || anterior instanceof PantallaNivel
                        || anterior instanceof PantallaEstancia
                        || anterior instanceof PantallaOpcionesJobs);

        if (ConfigTurno.menuPropio() && retornoDesdeJuego
                && retornoMultijugadorPendiente && destinoMultijugador) {
            limpiarRetornoJuego();
            siguiente = new PantallaMultijugadorJobs(new PantallaNivel());
            evento.setNewScreen(siguiente);
        } else if (ConfigTurno.menuPropio() && retornoDesdeJuego && destinoRetorno) {
            limpiarRetornoJuego();
            siguiente = new PantallaNivel();
            evento.setNewScreen(siguiente);
        } else if (ConfigTurno.menuPropio()
                && !flujoExternoActual
                && siguiente != null
                && siguiente.getClass() == TitleScreen.class
                && !(siguiente instanceof PantallaNivel)) {
            LimpiezaRecursosLegados.ejecutar();
            siguiente = new PantallaNivel();
            evento.setNewScreen(siguiente);
        } else if (!flujoExternoActual && ConfigTurno.pausaPropia() && esPausaReal(siguiente)) {
            siguiente = new PantallaEstancia();
            evento.setNewScreen(siguiente);
        } else if (ConfigTurno.menuPropio() && flujoAdministrativo
                && siguiente != null && siguiente.getClass() == OptionsScreen.class) {
            siguiente = new PantallaOpcionesJobs(anterior, Minecraft.getInstance().options);
            evento.setNewScreen(siguiente);
        } else if (ConfigTurno.menuPropio() && flujoAdministrativo
                && siguiente != null && siguiente.getClass() == JoinMultiplayerScreen.class) {
            siguiente = new PantallaMultijugadorJobs(anterior);
            evento.setNewScreen(siguiente);
        } else if (ConfigTurno.menuPropio() && flujoAdministrativo
                && siguiente != null && siguiente.getClass() == SelectWorldScreen.class) {
            siguiente = new PantallaMundosJobs(anterior);
            evento.setNewScreen(siguiente);
        } else if (ConfigTurno.menuPropio() && flujoAdministrativo
                && siguiente != null && siguiente.getClass() == ModListScreen.class) {
            siguiente = new PantallaModsJobs(anterior);
            evento.setNewScreen(siguiente);
        }

        actualizarFlujoExterno(flujoExternoActual, siguiente, optionsNaturalSolicitado);

        if (siguiente instanceof PantallaNivel) {
            limpiarRetornoJuego();
        }
        if (ConfigTurno.menuPropio() && esPantallaPropia(siguiente)
                && Minecraft.getInstance().level == null) {
            SesionMenu.abrir();
        } else if (siguiente == null || !ConfigTurno.menuPropio()) {
            SesionMenu.cerrar();
        }

        if (usaTransicionJobs(anterior, siguiente)) {
            TransicionInterfazJobs.notificar(anterior, siguiente);
        } else {
            TransicionInterfazJobs.cancelar();
        }
        if (Minecraft.getInstance().level == null && !esSuperficieAjenaIntocable(siguiente)) {
            PulidoInterfazJobs.notificarApertura(siguiente);
        }
        gesto(anterior, siguiente);
    }

    @SubscribeEvent
    public static void alInicializarPantalla(ScreenEvent.Init.Post evento) {
        invalidarHoverVanilla(evento.getScreen());
    }

    @SubscribeEvent
    public static void alEmpezarRenderPantalla(ScreenEvent.Render.Pre evento) {
        Screen pantalla = evento.getScreen();
        if (pantalla == null || esSuperficieAjenaIntocable(pantalla)) return;
        ListasExpediente.comenzarFrame(pantalla);
    }

    @SubscribeEvent
    public static void alRenderizarPantalla(ScreenEvent.Render.Post evento) {
        Screen pantalla = evento.getScreen();
        if (pantalla == null || esSuperficieAjenaIntocable(pantalla)) return;

        Minecraft cliente = Minecraft.getInstance();
        String clase = pantalla.getClass().getName();
        boolean propia = esPantallaPropia(pantalla);
        if (Minecraft.getInstance().level != null && !propia) return;

        actualizarHoverVanilla(pantalla, evento.getMouseX(), evento.getMouseY());

        long ahora = System.currentTimeMillis();
        if (propia) {
            PielVanillaJobs.dibujar(pantalla, evento.getGuiGraphics(),
                    evento.getMouseX(), evento.getMouseY());
            ListasExpediente.renderarBarras(pantalla, evento.getGuiGraphics());
            AtmosferaMenuJobs.dibujar(evento.getGuiGraphics(), pantalla.width, pantalla.height, ahora);
            if (!(pantalla instanceof PantallaNivel)) {
                CapaProfesionalJobs.dibujar(pantalla, evento.getGuiGraphics(),
                        evento.getMouseX(), evento.getMouseY(), ahora);
            }
        } else if (SesionMenu.activa()) {
            if (clase.startsWith("net.minecraft.")) {
                PielVanillaJobs.dibujar(pantalla, evento.getGuiGraphics(),
                        evento.getMouseX(), evento.getMouseY());
            }
            ChromeExpediente.bandaContextual(evento.getGuiGraphics(),
                    cliente.font, pantalla.width, pantalla.height);
        }
        if (propia) {
            PulidoInterfazJobs.dibujar(pantalla, evento.getGuiGraphics(),
                    evento.getMouseX(), evento.getMouseY());
        }
        if (cliente.level == null) {
            TransicionInterfazJobs.dibujar(pantalla, evento.getGuiGraphics());
        } else {
            TransicionInterfazJobs.cancelar();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void alReproducirSonido(PlaySoundEvent evento) {
        Screen pantalla = Minecraft.getInstance().screen;
        if (!esSuperficieJobsActiva(pantalla)) return;
        if (!evento.getOriginalSound().getLocation()
                .equals(SoundEvents.UI_BUTTON_CLICK.value().getLocation())) return;
        evento.setSound(ConfigTurno.sonidoBotones()
                ? MezclaAudio.reemplazoClickVanilla() : null);
    }

    @SubscribeEvent
    public static void alCerrarPantalla(ScreenEvent.Closing evento) {
        Screen pantalla = evento.getScreen();
        ConfigTurno.guardarPendiente();
        if (!esSuperficieAjenaIntocable(pantalla)) {
            ListasExpediente.liberar(pantalla);
        }
        invalidarHoverVanilla(pantalla);
    }

    @SubscribeEvent
    public static void alEntrarJuego(ClientPlayerNetworkEvent.LoggingIn evento) {
        limpiarRetornoJuego();
        limpiarFlujoExterno();
        enServidorRemoto = Minecraft.getInstance().getCurrentServer() != null;
        TransicionInterfazJobs.cancelar();
        SesionMenu.cerrar();
    }

    @SubscribeEvent
    public static void alSalirJuego(ClientPlayerNetworkEvent.LoggingOut evento) {
        Minecraft cliente = Minecraft.getInstance();
        retornoDesdeJuego = true;
        retornoMultijugadorPendiente = enServidorRemoto || cliente.getCurrentServer() != null;
        enServidorRemoto = false;
        limpiarFlujoExterno();
        TransicionInterfazJobs.cancelar();
        SesionMenu.cerrar();
    }

    @SubscribeEvent
    public static void alTickCliente(TickEvent.ClientTickEvent evento) {
        if (evento.phase != TickEvent.Phase.END) return;
        Minecraft cliente = Minecraft.getInstance();
        if (cliente.level != null || !ConfigTurno.menuPropio()) {
            if (cliente.level != null) {
                enServidorRemoto = cliente.getCurrentServer() != null;
            }
            limpiarFlujoExterno();
            TransicionInterfazJobs.cancelar();
            SesionMenu.cerrar();
            return;
        }
        GestorMusica.atender();
        GestorAmbiente.mantenerCamas();
    }

    private static boolean esPantallaPropia(Screen pantalla) {
        return pantalla != null
                && pantalla.getClass().getName().startsWith("com.santipdr.jobsmenu.client.screen.");
    }

    /**
     * Una Screen suministrada por otro mod es propiedad de ese mod. Jobs no
     * intenta conocer sus paquetes concretos ni decidir si es una GUI grafica,
     * de config o cualquier otra superficie: si no pertenece a Minecraft,
     * Forge o Jobs, queda completamente fuera del chrome/input Jobs.
     */
    private static boolean esPantallaTerceros(Screen pantalla) {
        if (pantalla == null || esPantallaPropia(pantalla)) return false;
        String clase = pantalla.getClass().getName();
        return !clase.startsWith("net.minecraft.")
                && !clase.startsWith("net.minecraftforge.");
    }

    /**
     * Video vanilla, pantallas de terceros y sus subflujos vanilla se respetan
     * sin capas Jobs. El marcador externo desaparece al regresar a una Screen
     * propia o al abandonar el menu.
     */
    private static boolean esSuperficieAjenaIntocable(Screen pantalla) {
        return pantalla instanceof VideoSettingsScreen
                || esPantallaTerceros(pantalla)
                || (flujoExternoActivo && !esPantallaPropia(pantalla));
    }

    private static boolean esSuperficieJobsActiva(Screen pantalla) {
        if (pantalla == null || !ConfigTurno.menuPropio()
                || esSuperficieAjenaIntocable(pantalla)) return false;
        return esPantallaPropia(pantalla) || SesionMenu.activa();
    }

    private static void actualizarFlujoExterno(boolean veniaExterno, Screen siguiente,
                                                boolean optionsNaturalSolicitado) {
        if (siguiente == null || esPantallaPropia(siguiente)) {
            flujoExternoActivo = false;
            return;
        }
        if (optionsNaturalSolicitado || esPantallaTerceros(siguiente) || veniaExterno) {
            flujoExternoActivo = true;
        }
    }

    private static void limpiarFlujoExterno() {
        flujoExternoActivo = false;
        permitirOptionsNaturalUnaVez = false;
    }

    private static void actualizarHoverVanilla(Screen pantalla, int mouseX, int mouseY) {
        if (!esSuperficieJobsActiva(pantalla)) return;
        int hijos = pantalla.children().size();
        if (pantallaHoverVanilla != pantalla || hijosHoverVistos != hijos) {
            reconstruirHoverVanilla(pantalla, hijos);
        }
        for (AbstractButton boton : BOTONES_HOVER_VANILLA) {
            boolean foco = boton.visible && boton.active
                    && (boton.isMouseOver(mouseX, mouseY) || boton.isFocused());
            if (foco) {
                if (HOVER_VANILLA.add(boton)) {
                    MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.22F);
                }
            } else {
                HOVER_VANILLA.remove(boton);
            }
        }
    }

    private static void reconstruirHoverVanilla(Screen pantalla, int hijos) {
        HOVER_VANILLA.clear();
        BOTONES_HOVER_VANILLA.clear();
        pantallaHoverVanilla = pantalla;
        hijosHoverVistos = hijos;
        for (var child : pantalla.children()) {
            if (!(child instanceof AbstractButton boton)) continue;
            if (child.getClass().getName().startsWith("com.santipdr.jobsmenu.")) continue;
            BOTONES_HOVER_VANILLA.add(boton);
        }
    }

    private static void invalidarHoverVanilla(Screen pantalla) {
        if (pantallaHoverVanilla != null && pantallaHoverVanilla != pantalla) return;
        HOVER_VANILLA.clear();
        BOTONES_HOVER_VANILLA.clear();
        pantallaHoverVanilla = null;
        hijosHoverVistos = -1;
    }

    private static boolean usaTransicionJobs(Screen desde, Screen hasta) {
        if (Minecraft.getInstance().level != null) return false;
        if (hasta == null || esSuperficieAjenaIntocable(desde)
                || esSuperficieAjenaIntocable(hasta)) return false;
        return esPantallaPropia(desde) || esPantallaPropia(hasta);
    }

    private static boolean esPausaReal(Screen siguiente) {
        if (siguiente == null || siguiente.getClass() != PauseScreen.class) return false;
        Component titulo = siguiente.getTitle();
        return titulo != null && Component.translatable("menu.game").equals(titulo);
    }

    private static void limpiarRetornoJuego() {
        retornoDesdeJuego = false;
        retornoMultijugadorPendiente = false;
    }

    private static void gesto(Screen anterior, Screen siguiente) {
        boolean veniaDelAviso = anterior instanceof PantallaNivel;
        boolean vaAlAviso = siguiente instanceof PantallaNivel;

        if (vaAlAviso && !veniaDelAviso) {
            if (anterior == null && !presentado) {
                presentado = true;
                MezclaAudio.gesto(SonidosNivel.UI_ABRIR, 0.80F);
            } else if (anterior != null) {
                MezclaAudio.gesto(SonidosNivel.UI_VOLVER, 0.70F);
            }
            return;
        }

        if (veniaDelAviso && !vaAlAviso && siguiente != null) {
            MezclaAudio.gesto(SonidosNivel.UI_CERRAR, 0.55F);
        }
    }
}
