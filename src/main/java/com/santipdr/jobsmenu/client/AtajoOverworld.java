package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.screen.PantallaNivel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

/**
 * Control + S abre la lista de mundos sin pasar por el aviso.
 *
 * POR QUE NO ES UN KEYMAPPING
 *
 * Los KeyMapping de Forge se consultan desde el tick del cliente y ese tick no
 * mira el teclado mientras hay una pantalla abierta: en los menus, las teclas
 * viajan por los eventos de Screen. Registrar el atajo como KeyMapping lo
 * dejaria muerto exactamente donde tiene que funcionar. Por eso se engancha en
 * ScreenEvent.KeyPressed, que es el camino que la tecla recorre de verdad.
 *
 * ANTIRREPETICION
 *
 * GLFW manda un evento al apretar y despues uno por cada repeticion mientras la
 * tecla siga hundida, y Minecraft los pasa todos por keyPressed sin distinguir.
 * Sin control, dejar el dedo apoyado abriria la lista de mundos treinta veces
 * por segundo. El pestillo de abajo solo deja pasar el primero y no se abre de
 * nuevo hasta que la S se suelta.
 *
 * CONFLICTOS
 *
 * El atajo se descarta si hay un cuadro de texto con el foco: cuando alguien
 * esta escribiendo el nombre de un servidor, Control + S es de quien escribe.
 * Y solo actua desde pantallas propias del mod o desde la de titulo, para no
 * pisar atajos de otros mods en sus propias interfaces.
 */
@Mod.EventBusSubscriber(modid = JobsMenu.MOD_ID, value = Dist.CLIENT)
public final class AtajoOverworld {

    private AtajoOverworld() {
    }

    /** Cierto mientras la combinacion siga hundida desde que se acepto. */
    private static boolean hundida;

    @SubscribeEvent
    public static void alApretar(ScreenEvent.KeyPressed.Pre evento) {
        if (evento.getKeyCode() != GLFW.GLFW_KEY_S) {
            return;
        }
        if ((evento.getModifiers() & GLFW.GLFW_MOD_CONTROL) == 0) {
            return;
        }
        if (hundida) {
            // Repeticion por tecla mantenida. Se consume igual, para que la S
            // tampoco llegue a la pantalla de abajo.
            evento.setCanceled(true);
            return;
        }

        Screen pantalla = evento.getScreen();
        if (!admite(pantalla)) {
            return;
        }
        if (pantalla.getFocused() instanceof EditBox) {
            return;
        }

        hundida = true;
        evento.setCanceled(true);

        // El gesto no se dispara aca. Cambiar de pantalla ya pasa por
        // EscuchaCliente, que suena la salida del aviso mirando de donde se
        // viene y adonde se va; hacerlo tambien aca daria dos sonidos pisados
        // para un solo movimiento.
        Minecraft cliente = Minecraft.getInstance();
        cliente.setScreen(new SelectWorldScreen(pantalla));
    }

    @SubscribeEvent
    public static void alSoltar(ScreenEvent.KeyReleased.Pre evento) {
        if (evento.getKeyCode() == GLFW.GLFW_KEY_S) {
            hundida = false;
        }
    }

    /**
     * Solo desde el aviso del nivel o desde la pantalla de titulo vanilla.
     *
     * La segunda hace falta porque el mod se puede apagar por configuracion, y
     * el atajo tiene que seguir estando para quien lo apago.
     */
    private static boolean admite(Screen pantalla) {
        return pantalla instanceof PantallaNivel
                || pantalla instanceof net.minecraft.client.gui.screens.TitleScreen;
    }
}
