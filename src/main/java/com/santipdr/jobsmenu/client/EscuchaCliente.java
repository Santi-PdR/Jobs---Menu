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

/** La puerta del aviso: lifecycle, redirecciones, sonido y continuidad visual. */
@Mod.EventBusSubscriber(modid = JobsMenu.MOD_ID, value = Dist.CLIENT)
public final class EscuchaCliente {

    private EscuchaCliente() {
    }

    private static boolean presentado;
    private static boolean retornoDesdeJuego;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void alAbrirPantalla(ScreenEvent.Opening evento) {
        Screen anterior = evento.getCurrentScreen();
        Screen siguiente = evento.getNewScreen();

        ConfigTurno.guardarPendiente();

        boolean destinoRetorno = siguiente != null && (
                siguiente.getClass() == TitleScreen.class
                        || siguiente instanceof JoinMultiplayerScreen
                        || siguiente.getClass().getName().equals(
                                "net.minecraft.client.gui.screens.realms.RealmsMainScreen"));
        boolean flujoAdministrativo = SesionMenu.activa()
                || anterior instanceof PantallaNivel
                || anterior instanceof PantallaEstancia
                || anterior instanceof PantallaOpcionesJobs;

        if (ConfigTurno.menuPropio() && retornoDesdeJuego && destinoRetorno) {
            retornoDesdeJuego = false;
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
            retornoDesdeJuego = false;
            SesionMenu.abrir();
        } else if (siguiente == null || !ConfigTurno.menuPropio()) {
            SesionMenu.cerrar();
        }

        TransicionInterfazJobs.notificar(anterior, siguiente);
        PulidoInterfazJobs.notificarApertura(siguiente);
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

        String clase = pantalla.getClass().getName();
        boolean propia = clase.startsWith("com.santipdr.jobsmenu.");
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
                    Minecraft.getInstance().font, pantalla.width, pantalla.height);
        }
        if (propia) {
            PulidoInterfazJobs.dibujar(pantalla, evento.getGuiGraphics(),
                    evento.getMouseX(), evento.getMouseY());
        }
        TransicionInterfazJobs.dibujar(pantalla, evento.getGuiGraphics());
    }

    /**
     * Los controles vanilla que conservamos por compatibilidad no deben volver
     * a introducir el click de fabrica en una interfaz Jobs.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void alReproducirSonido(PlaySoundEvent evento) {
        if (!SesionMenu.activa() || esVideoIntocable(Minecraft.getInstance().screen)) return;
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
        retornoDesdeJuego = false;
        SesionMenu.cerrar();
    }

    @SubscribeEvent
    public static void alSalirJuego(ClientPlayerNetworkEvent.LoggingOut evento) {
        retornoDesdeJuego = true;
        SesionMenu.cerrar();
    }

    @SubscribeEvent
    public static void alTickCliente(TickEvent.ClientTickEvent evento) {
        if (evento.phase != TickEvent.Phase.END) return;
        Minecraft cliente = Minecraft.getInstance();
        if (cliente.level != null || !ConfigTurno.menuPropio()) {
            // Gameplay es frontera dura. Corta inmediatamente y no ejecuta
            // mantenimiento de audio del menu durante el resto de este tick.
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
