package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.screen.PantallaEstancia;
import com.santipdr.jobsmenu.client.screen.PantallaNivel;
import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.MusicaPropia;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * La puerta del aviso: quien entra, quien sale y como suena cada cosa.
 *
 * SUSTITUCION DE LA PANTALLA DE TITULO
 *
 * Se hace al abrirse la pantalla y no antes: asi, si el jugador apaga el mod en
 * la configuracion, el menu original vuelve sin reiniciar el juego.
 *
 * LOS GESTOS DE ENTRADA Y SALIDA
 *
 * Los tres sonidos que enmarcan la pantalla se disparan aca y no dentro de
 * PantallaNivel, por una razon concreta: init() se vuelve a ejecutar cada vez
 * que cambia el tamano de la ventana, y removed() cada vez que se va a otra
 * pantalla, incluso a una que va a volver enseguida. Colgados de ahi, los
 * gestos sonarian al redimensionar el juego. ScreenEvent.Opening, en cambio,
 * solo corre cuando de verdad se cambia de pantalla, y ademas dice de donde se
 * viene, que es justo lo que hace falta para distinguir "abrir" de "volver".
 *
 *   abrir  - el aviso aparece por primera vez, o se llega desde afuera
 *   volver - se regresa al aviso desde una pantalla hija
 *   cerrar - el aviso queda atras y otra pantalla toma su lugar
 */
@Mod.EventBusSubscriber(modid = JobsMenu.MOD_ID, value = Dist.CLIENT)
public final class EscuchaCliente {

    private EscuchaCliente() {
    }

    /** Si el aviso ya se mostro alguna vez en esta sesion del juego. */
    private static boolean presentado;

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void alAbrirPantalla(ScreenEvent.Opening evento) {
        Screen anterior = evento.getCurrentScreen();
        Screen siguiente = evento.getNewScreen();

        if (ConfigTurno.menuPropio()
                && siguiente instanceof TitleScreen
                && !(siguiente instanceof PantallaNivel)) {
            // El hueco para la musica propia se prepara la primera vez que se
            // entra al menu, no al cargar el mod: asi no se toca el disco en
            // el arranque del juego, que es donde mas duele.
            MusicaPropia.preparar();
            siguiente = new PantallaNivel();
            evento.setNewScreen(siguiente);
        } else if (ConfigTurno.pausaPropia() && esPausaReal(siguiente)) {
            // Solo la pausa DE VERDAD, no la superposicion de F3+Esc.
            siguiente = new PantallaEstancia();
            evento.setNewScreen(siguiente);
        }

        gesto(anterior, siguiente);
    }

    /**
     * Si esta pantalla es la pausa de verdad y no la superposicion de F3+Esc.
     *
     * PauseScreen se construye de dos formas: con menu (Escape, muestra los
     * botones, titulo "menu.game") y sin menu (F3+Esc, solo el rotulo "Game
     * Paused", titulo "menu.paused"). La segunda no lleva botones y no hay que
     * tocarla: reemplazarla dejaria una hoja de pausa flotando cuando el
     * jugador solo queria congelar el cuadro. Se distinguen por el titulo, que
     * es lo unico publico que las separa -el campo showPauseMenu es privado-.
     * Ademas se exige la clase exacta para no pisar pausas de otros mods.
     */
    private static boolean esPausaReal(Screen siguiente) {
        if (siguiente == null || siguiente.getClass() != PauseScreen.class) {
            return false;
        }
        Component titulo = siguiente.getTitle();
        return titulo != null && Component.translatable("menu.game").equals(titulo);
    }

    /**
     * El sonido que corresponde al cruce de puerta.
     *
     * Se mira de donde se viene y adonde se va. Salir del aviso hacia otra
     * pantalla y volver de ella no son el mismo movimiento y no suenan igual:
     * uno deja la hoja apoyandose contra la pared, el otro la despega otra vez.
     */
    private static void gesto(Screen anterior, Screen siguiente) {
        boolean veniaDelAviso = anterior instanceof PantallaNivel;
        boolean vaAlAviso = siguiente instanceof PantallaNivel;

        if (vaAlAviso && !veniaDelAviso) {
            if (anterior == null && !presentado) {
                // Primera vez en la sesion: el aviso se despega de la pared.
                presentado = true;
                MezclaAudio.gesto(SonidosNivel.UI_ABRIR, 0.80F);
            } else if (anterior != null) {
                // Se vuelve de una pantalla hija.
                MezclaAudio.gesto(SonidosNivel.UI_VOLVER, 0.70F);
            }
            return;
        }

        if (veniaDelAviso && !vaAlAviso && siguiente != null) {
            // La hoja vuelve a apoyarse. Bajo: llega detras del sello del
            // renglon que se acaba de marcar y tiene que leerse como su cola,
            // no como un segundo golpe.
            MezclaAudio.gesto(SonidosNivel.UI_CERRAR, 0.55F);
        }
    }
}
