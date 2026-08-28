package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.client.screen.PantallaNivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Explorador temporal de recintos.
 *
 * PAGE UP / PAGE DOWN recorre los niveles sin cambiar la configuracion.
 * HOME abandona la inspeccion y devuelve el control a la rotacion normal.
 * Solo existe en la pantalla principal del mod y se anuncia en la propia UI.
 */
@Mod.EventBusSubscriber(modid = JobsMenu.MOD_ID, value = Dist.CLIENT)
public final class NavegacionNiveles {

    private NavegacionNiveles() {
    }

    @SubscribeEvent
    public static void tecla(ScreenEvent.KeyPressed.Pre evento) {
        if (!(evento.getScreen() instanceof PantallaNivel)) {
            return;
        }

        if (evento.getKeyCode() == GLFW.GLFW_KEY_PAGE_DOWN) {
            RotacionNiveles.inspeccionarSiguiente();
            evento.setCanceled(true);
        } else if (evento.getKeyCode() == GLFW.GLFW_KEY_PAGE_UP) {
            RotacionNiveles.inspeccionarAnterior();
            evento.setCanceled(true);
        } else if (evento.getKeyCode() == GLFW.GLFW_KEY_HOME && RotacionNiveles.inspeccionActiva()) {
            RotacionNiveles.terminarInspeccion();
            evento.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void dibujar(ScreenEvent.Render.Post evento) {
        if (!(evento.getScreen() instanceof PantallaNivel) || ConfigTurno.interfazMinima()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        GuiGraphics g = evento.getGuiGraphics();

        Component ayuda = Component.literal("PgUp / PgDn  explorar");
        Component estado = RotacionNiveles.inspeccionActiva()
                ? Component.literal("HOME  volver a rotacion")
                : Component.literal("rotacion automatica");

        int margen = 12;
        int ancho = Math.max(mc.font.width(ayuda), mc.font.width(estado));
        int x = mc.getWindow().getGuiScaledWidth() - margen - ancho;
        int y = mc.getWindow().getGuiScaledHeight() - margen - 19;

        g.fill(x - 7, y - 5, x + ancho + 7, y + 20,
                Paleta.conAlfa(Paleta.VANO, RotacionNiveles.inspeccionActiva() ? 0.62F : 0.34F));
        g.fill(x - 7, y - 5, x - 5, y + 20,
                Paleta.conAlfa(Paleta.PAPEL, RotacionNiveles.inspeccionActiva() ? 0.80F : 0.38F));

        g.drawString(mc.font, ayuda, x, y,
                Paleta.conAlfa(Paleta.PAPEL, 0.78F), false);
        g.drawString(mc.font, estado, x, y + 10,
                Paleta.conAlfa(Paleta.PAPEL, RotacionNiveles.inspeccionActiva() ? 0.92F : 0.48F), false);
    }
}
