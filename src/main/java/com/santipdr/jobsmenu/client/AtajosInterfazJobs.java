package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.screen.PantallaEstancia;
import com.santipdr.jobsmenu.client.screen.PantallaNivel;
import com.santipdr.jobsmenu.client.ui.RenglonTablon;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Atajos visibles del aviso. Nunca actuan mientras el usuario escribe texto. */
@Mod.EventBusSubscriber(modid = JobsMenu.MOD_ID, value = Dist.CLIENT)
public final class AtajosInterfazJobs {

    private AtajosInterfazJobs() {
    }

    @SubscribeEvent
    public static void alApretar(ScreenEvent.KeyPressed.Pre evento) {
        Screen pantalla = evento.getScreen();
        if (!(pantalla instanceof PantallaNivel) && !(pantalla instanceof PantallaEstancia)) return;
        if (pantalla.getFocused() instanceof EditBox) return;
        if (evento.getModifiers() != 0) return;

        int key = evento.getKeyCode();
        int indice = -1;
        if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_4) {
            indice = key - GLFW.GLFW_KEY_1;
        } else if (key >= GLFW.GLFW_KEY_KP_1 && key <= GLFW.GLFW_KEY_KP_4) {
            indice = key - GLFW.GLFW_KEY_KP_1;
        }
        if (indice < 0) return;

        // En pausa solo 1 y 2 son accesos rapidos. El tercero desconecta y se
        // mantiene fuera de los atajos numericos para evitar salidas accidentales.
        if (pantalla instanceof PantallaEstancia && indice > 1) return;

        List<RenglonTablon> renglones = new ArrayList<>();
        for (var child : pantalla.children()) {
            if (child instanceof RenglonTablon renglon && renglon.visible) {
                renglones.add(renglon);
            }
        }
        renglones.sort(Comparator.comparingInt(RenglonTablon::getY));
        if (indice >= renglones.size()) return;

        RenglonTablon objetivo = renglones.get(indice);
        if (!objetivo.active) return;
        evento.setCanceled(true);
        objetivo.onPress();
    }
}
