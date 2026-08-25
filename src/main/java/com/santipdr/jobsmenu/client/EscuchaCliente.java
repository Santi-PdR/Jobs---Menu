package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.screen.PantallaTurno;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Sustituye la pantalla de titulo vanilla por el tablon de turnos.
 *
 * Se hace al abrirse la pantalla y no antes: asi, si el jugador apaga el mod en
 * la configuracion, el menu original vuelve sin reiniciar el juego.
 */
@Mod.EventBusSubscriber(modid = JobsMenu.MOD_ID, value = Dist.CLIENT)
public final class EscuchaCliente {

    private EscuchaCliente() {
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void alAbrirPantalla(ScreenEvent.Opening evento) {
        if (!ConfigTurno.menuPropio()) {
            return;
        }
        if (!(evento.getScreen() instanceof TitleScreen)) {
            return;
        }
        if (evento.getScreen() instanceof PantallaTurno) {
            return;
        }
        evento.setNewScreen(new PantallaTurno());
    }
}
