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
    private static final WeakHashMap<AbstractButton, Boolean> HOVER_VANILLA = new WeakHashMap<>();
    private static Screen pantallaHoverVanilla;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void alAbrirPantalla(ScreenEvent.Opening evento) {
        Screen anterior = evento.getCurrentScreen();
        Screen siguiente = evento.getNewScreen();

        ConfigTurno.guardarPendiente();

        boolean destinoMultijugador = siguiente instanceof JoinMultiplayerScreen
                || siguiente != null && siguiente.getClass() == TitleScreen.class;
        boolean destinoRetorno = siguiente != null && (
                siguiente.getClass() == TitleScreen.class
                        || siguiente instanceof JoinMultiplayerScreen
                        || siguiente.getClass().getName().equals(
                                "net.minecraft.client.gui.screens.realms.RealmsMainScreen"));
        boolean flujoAdministrativo = SesionMenu.activa()
                || anterior instanceof PantallaNivel
                || anterior instanceof PantallaEstancia
                || anterior instanceof PantallaOpcionesJobs;

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
                && siguiente != null
                && siguiente.getClass() == TitleScreen.class
                && !(siguiente instanceof PantallaNivel)) {
            LimpiezaRecursosLegados.ejecutar();
            siguiente = new PantallaNivel();
            evento.setNewScreen(siguiente);
        } else if (ConfigTurno.pausaPropia() && esPausaReal(siguiente)) {
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
        // La animacion corta de entrada tambien es una transicion visual. No se
        // registra mientras existe gameplay: pausa y configuracion aparecen ya
        // estabilizadas sobre el mundo, sin barridos ni fundidos de entrada.
        if (Minecraft.getInstance().level == null) {
            PulidoInterfazJobs.notificarApertura(siguiente);
        }
        gesto(anterior, siguiente);
    }

    /**
     * Las pantallas propias se pintan enteras. Los dialogos vanilla auxiliares
     * conservan su logica, pero reciben chrome, controles y campos Jobs. Las
     * pantallas de terceros solo reciben contexto minimo para no romper hooks.
     */
    @SubscribeEvent
    public static void alRenderizarPantalla(ScreenEvent.Render.Post evento) {
        Screen pantalla = evento.getScreen();
        if (pantalla == null || esVideoIntocable(pantalla)) return;

        Minecraft cliente = Minecraft.getInstance();
        String clase = pantalla.getClass().getName();
        boolean propia = esPantallaPropia(pantalla);
        // Inventario, chat y cualquier otra Screen con un mundo cargado son
        // gameplay: ninguna piel, banda ni transicion Jobs puede alcanzarlas.
        if (cliente.level != null && !propia) return;

        actualizarHoverVanilla(pantalla, evento.getMouseX(), evento.getMouseY());

        if (propia) {
            PielVanillaJobs.dibujar(pantalla, evento.getGuiGraphics(),
                    evento.getMouseX(), evento.getMouseY());
            ListasExpediente.renderarBarras(pantalla, evento.getGuiGraphics());
            AtmosferaMenuJobs.dibujar(evento.getGuiGraphics(), pantalla.width, pantalla.height,
                    System.currentTimeMillis());
            // El menu principal ya tiene su propia composicion inferior (nombre y nota
            // del nivel). La instrumentacion generica se reserva para las pantallas
            // secundarias para que los atajos visibles no vuelvan a competir con ella.
            if (!(pantalla instanceof PantallaNivel)) {
                CapaProfesionalJobs.dibujar(pantalla, evento.getGuiGraphics(),
                        evento.getMouseX(), evento.getMouseY(), System.currentTimeMillis());
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
        // Doble compuerta: incluso si una transicion quedara pendiente justo al
        // entrar al mundo, nunca se dibuja sobre pausa/configuracion de gameplay.
        if (cliente.level == null) {
            TransicionInterfazJobs.dibujar(pantalla, evento.getGuiGraphics());
        } else {
            TransicionInterfazJobs.cancelar();
        }
    }

    /**
     * Los controles vanilla que conservamos por compatibilidad no deben volver
     * a introducir el click de fabrica en una superficie Jobs. La musica y el
     * ambiente siguen ligados a SesionMenu; este feedback de UI tambien se
     * permite en pausa/configuracion Jobs dentro de gameplay sin reabrir audio.
     */
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
        ConfigTurno.guardarPendiente();
    }

    @SubscribeEvent
    public static void alEntrarJuego(ClientPlayerNetworkEvent.LoggingIn evento) {
        limpiarRetornoJuego();
        enServidorRemoto = Minecraft.getInstance().getCurrentServer() != null;
        TransicionInterfazJobs.cancelar();
        SesionMenu.cerrar();
    }

    @SubscribeEvent
    public static void alSalirJuego(ClientPlayerNetworkEvent.LoggingOut evento) {
        Minecraft cliente = Minecraft.getInstance();
        retornoDesdeJuego = true;
        // Se usa tambien el estado capturado en ticks jugables por si otro mod
        // limpia currentServer antes de que Forge entregue LoggingOut.
        retornoMultijugadorPendiente = enServidorRemoto || cliente.getCurrentServer() != null;
        enServidorRemoto = false;
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
            // Gameplay es frontera dura. Corta inmediatamente y no ejecuta
            // mantenimiento de audio/transiciones durante el resto de este tick.
            TransicionInterfazJobs.cancelar();
            SesionMenu.cerrar();
            return;
        }
        GestorMusica.atender();
        GestorAmbiente.mantenerCamas();
    }

    /**
     * Video Settings queda fuera de toda piel, marco, transicion y gesto Jobs.
     * Se reconocen tambien las pantallas conocidas de Sodium/Embeddium para no
     * pintar encima si otro mod sustituye la instancia vanilla.
     */
    private static boolean esPantallaPropia(Screen pantalla) {
        return pantalla != null
                && pantalla.getClass().getName().startsWith("com.santipdr.jobsmenu.client.screen.");
    }

    /**
     * Una superficie Jobs puede vivir dentro de gameplay (pausa/configuracion)
     * sin que eso reactive la sesion de musica. Los dialogos vanilla auxiliares
     * solo heredan feedback Jobs mientras la visita de menu sigue activa.
     */
    private static boolean esSuperficieJobsActiva(Screen pantalla) {
        if (pantalla == null || !ConfigTurno.menuPropio() || esVideoIntocable(pantalla)) return false;
        return esPantallaPropia(pantalla) || SesionMenu.activa();
    }

    /**
     * Los widgets propios ya gestionan su hover. Para botones/sliders vanilla
     * conservados por compatibilidad se mantiene estado debil por instancia y
     * se dispara UI_PASAR una sola vez al entrar con raton o foco de teclado.
     */
    private static void actualizarHoverVanilla(Screen pantalla, int mouseX, int mouseY) {
        if (!esSuperficieJobsActiva(pantalla)) return;
        if (pantallaHoverVanilla != pantalla) {
            HOVER_VANILLA.clear();
            pantallaHoverVanilla = pantalla;
        }
        for (var child : pantalla.children()) {
            if (!(child instanceof AbstractButton boton)) continue;
            if (child.getClass().getName().startsWith("com.santipdr.jobsmenu.")) continue;
            boolean foco = boton.visible && boton.active
                    && (boton.isMouseOver(mouseX, mouseY) || boton.isFocused());
            boolean anterior = Boolean.TRUE.equals(HOVER_VANILLA.put(boton, foco));
            if (foco && !anterior) {
                MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.22F);
            }
        }
    }

    /**
     * Las transiciones pertenecen exclusivamente al flujo fuera de gameplay.
     * Aunque la pausa/configuracion sean pantallas Jobs, con un nivel cargado
     * aparecen y desaparecen sin barridos ni fundidos de transicion.
     */
    private static boolean usaTransicionJobs(Screen desde, Screen hasta) {
        if (Minecraft.getInstance().level != null) return false;
        if (hasta == null || esVideoIntocable(desde) || esVideoIntocable(hasta)) return false;
        return esPantallaPropia(desde) || esPantallaPropia(hasta);
    }

    private static boolean esVideoIntocable(Screen pantalla) {
        if (pantalla == null) return false;
        if (pantalla instanceof VideoSettingsScreen) return true;
        String clase = pantalla.getClass().getName().toLowerCase(java.util.Locale.ROOT);
        return (clase.contains("embeddium") || clase.contains("sodium"))
                && clase.contains("video") && clase.contains("screen");
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
