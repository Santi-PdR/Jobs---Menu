package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.client.screen.PantallaEstancia;
import com.santipdr.jobsmenu.client.screen.PantallaNivel;
import com.santipdr.jobsmenu.client.sound.GestorAmbiente;
import com.santipdr.jobsmenu.client.sound.GestorMusica;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

/**
 * Diagnostico interno oculto del aviso.
 *
 * HERRAMIENTA DE ADMINISTRACION, NO FUNCION DE USUARIO
 *
 * Esta clase no se anuncia en ningun lado: ni en la hoja, ni en los ajustes,
 * ni en la documentacion publica. Solo escribe en el registro del juego un
 * volcado del estado interno cuando se pide con la combinacion reservada. No
 * dibuja nada en pantalla, no abre ninguna interfaz y no cambia ningun valor:
 * es un termometro, no un control.
 *
 * POR QUE NO ES UN KEYMAPPING
 *
 * Igual que el atajo de servicio, esta tecla viaja por los eventos de Screen:
 * en los menus el tick del cliente no consulta los KeyMapping. Se engancha en
 * ScreenEvent.KeyPressed.Pre, solo desde las pantallas propias del mod, y se
 * descarta si hay un cuadro de texto con el foco para no robarle la tecla a
 * quien esta escribiendo.
 */
@Mod.EventBusSubscriber(modid = JobsMenu.MOD_ID, value = Dist.CLIENT)
public final class DiagnosticoOculto {

    private DiagnosticoOculto() {
    }

    @SubscribeEvent
    public static void alApretar(ScreenEvent.KeyPressed.Pre evento) {
        if (evento.getKeyCode() != GLFW.GLFW_KEY_D
                || (evento.getModifiers() & GLFW.GLFW_MOD_CONTROL) == 0) {
            return;
        }
        Screen pantalla = evento.getScreen();
        if (!(pantalla instanceof PantallaNivel) && !(pantalla instanceof PantallaEstancia)) {
            return;
        }
        if (pantalla.getFocused() instanceof EditBox) {
            return;
        }
        evento.setCanceled(true);
        volcar();
    }

    /** Un volcado de una sola linea por dato, en el registro del cliente. */
    private static void volcar() {
        Minecraft cliente = Minecraft.getInstance();
        RotacionNiveles.Estado estado = RotacionNiveles.capturar();

        JobsMenu.LOG.info("=== jobsmenu diagnostico ===");
        JobsMenu.LOG.info("pantalla     : {}", cliente.screen == null
                ? "null" : cliente.screen.getClass().getName());
        JobsMenu.LOG.info("sesion       : activa={}", SesionMenu.activa());
        JobsMenu.LOG.info("nivel        : {} (indice {})", estado.nivel().clave, estado.indice());
        JobsMenu.LOG.info("luz          : {} transicion={} suspension={}",
                String.format(java.util.Locale.ROOT, "%.3f", estado.luz()),
                estado.enTransicion(), estado.enSuspension());
        JobsMenu.LOG.info("musica       : sonando={} reintento={}",
                GestorMusica.sonando(), GestorMusica.reintentoParaDiagnostico());
        JobsMenu.LOG.info("ambiente     : capas={}", GestorAmbiente.capasActivas());
        JobsMenu.LOG.info("volumenes    : aviso={} musica={} ambiente={}",
                ConfigTurno.volumenAvisoPorcentaje(),
                ConfigTurno.volumenMusicaPorcentaje(),
                ConfigTurno.volumenAmbientePorcentaje());
        JobsMenu.LOG.info("opciones     : escena={} rotacion={} fijo={} calma={} "
                        + "bajoConsumo={} perfilAccesible={}",
                ConfigTurno.escenaViva(),
                ConfigTurno.rotarNivelesBruto(),
                ConfigTurno.nivelFijo(),
                ConfigTurno.rotacionCalma(),
                ConfigTurno.bajoConsumo(),
                ConfigTurno.perfilAccesible());
        JobsMenu.LOG.info("ventana      : {}x{} guiScale={}",
                cliente.getWindow().getScreenWidth(),
                cliente.getWindow().getScreenHeight(),
                cliente.getWindow().getGuiScale());
        JobsMenu.LOG.info("=== fin diagnostico ===");
    }
}
