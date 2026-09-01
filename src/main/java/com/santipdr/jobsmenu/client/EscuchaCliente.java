package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.screen.PantallaEstancia;
import com.santipdr.jobsmenu.client.screen.PantallaMultijugadorJobs;
import com.santipdr.jobsmenu.client.screen.PantallaNivel;
import com.santipdr.jobsmenu.client.screen.PantallaOpcionesJobs;
import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.GestorAmbiente;
import com.santipdr.jobsmenu.client.sound.GestorMusica;
import com.santipdr.jobsmenu.client.sound.MusicaPropia;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.TransicionInterfazJobs;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
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

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void alAbrirPantalla(ScreenEvent.Opening evento) {
        Screen anterior = evento.getCurrentScreen();
        Screen siguiente = evento.getNewScreen();

        ConfigTurno.guardarPendiente();

        boolean salidaAlTitulo = siguiente != null
                && siguiente.getClass() == TitleScreen.class
                && SesionMenu.consumirSalidaAlTitulo();
        boolean flujoAdministrativo = SesionMenu.activa()
                || anterior instanceof PantallaNivel
                || anterior instanceof PantallaEstancia
                || anterior instanceof PantallaOpcionesJobs;

        if (ConfigTurno.menuPropio()
                && siguiente != null
                && siguiente.getClass() == TitleScreen.class
                && !salidaAlTitulo
                && !(siguiente instanceof PantallaNivel)) {
            MusicaPropia.preparar();
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
        }

        if (siguiente instanceof PantallaNivel) {
            SesionMenu.abrir();
        } else if (salidaAlTitulo || siguiente == null || !ConfigTurno.menuPropio()) {
            SesionMenu.cerrar();
        }

        TransicionInterfazJobs.notificar(anterior, siguiente);
        gesto(anterior, siguiente);
    }

    /**
     * Varias pantallas vanilla crean su boton Done antes de que el wrapper Jobs
     * pueda reemplazarlo. Ocultamos todos los duplicados en pantallas propias;
     * asi ninguno puede quedar solapado o capturando clicks por debajo.
     */
    @SubscribeEvent
    public static void alInicializarPantalla(ScreenEvent.Init.Post evento) {
        Screen pantalla = evento.getScreen();
        if (pantalla == null
                || !pantalla.getClass().getName().startsWith("com.santipdr.jobsmenu.")) {
            return;
        }

        for (var child : pantalla.children()) {
            if (child instanceof Button boton
                    && boton.getMessage().equals(CommonComponents.GUI_DONE)) {
                boton.visible = false;
                boton.active = false;
            }
        }
    }

    /**
     * Las pantallas propias se pintan enteras. Una pantalla externa o vanilla
     * que aparezca durante la visita conserva su render, pero recibe la banda
     * de expediente para que no parezca un salto fuera de Jobs.
     */
    @SubscribeEvent
    public static void alRenderizarPantalla(ScreenEvent.Render.Post evento) {
        Screen pantalla = evento.getScreen();
        if (pantalla == null) return;

        boolean propia = pantalla.getClass().getName().startsWith("com.santipdr.jobsmenu.");
        if (propia) {
            // Se dibuja despues de vanilla para cubrir visualmente su scrollbar
            // sin tocar el comportamiento de rueda, click o drag.
            ListasExpediente.renderarBarras(pantalla, evento.getGuiGraphics());
        } else if (SesionMenu.activa()) {
            ChromeExpediente.bandaContextual(evento.getGuiGraphics(),
                    Minecraft.getInstance().font, pantalla.width, pantalla.height);
        }
        TransicionInterfazJobs.dibujar(pantalla, evento.getGuiGraphics());
    }

    @SubscribeEvent
    public static void alCerrarPantalla(ScreenEvent.Closing evento) {
        ConfigTurno.guardarPendiente();
    }

    @SubscribeEvent
    public static void alTickCliente(TickEvent.ClientTickEvent evento) {
        if (evento.phase != TickEvent.Phase.END) return;
        Minecraft cliente = Minecraft.getInstance();
        if (cliente.level != null || !ConfigTurno.menuPropio()) {
            SesionMenu.cerrar();
        }
        GestorMusica.atender();
        GestorAmbiente.mantenerCamas();
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
