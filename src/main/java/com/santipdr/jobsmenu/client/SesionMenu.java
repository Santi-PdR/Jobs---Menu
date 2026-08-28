package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.client.sound.GestorMusica;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;

/**
 * Ciclo de vida de una visita al menu Jobs.
 *
 * La pantalla principal deja de ser la pantalla activa al abrir Opciones,
 * Sonido, Video, Controles, Mods, Recursos, Un jugador o Multijugador. Esas
 * pantallas siguen perteneciendo a la misma visita aunque ya no sean una
 * PantallaNivel. Mantener este estado separado de una clase de Screen evita
 * cortar y recrear la musica en cada salto y permite cerrarla con certeza al
 * entrar a un mundo, desactivar el menu o terminar la sesion.
 */
public final class SesionMenu {

    private static boolean activa;

    private SesionMenu() {
    }

    public static void abrir() {
        activa = true;
    }

    public static void cerrar() {
        activa = false;
        GestorMusica.soltar();
    }

    /**
     * La visita solo puede seguir viva fuera de un mundo y con alguna pantalla
     * abierta. El chequeo defensivo cubre cargas de mundo, desconexiones,
     * cierres rapidos y configuraciones cambiadas desde el archivo.
     */
    public static boolean activa() {
        Minecraft cliente = Minecraft.getInstance();
        return activa
                && ConfigTurno.menuPropio()
                && cliente.level == null
                && cliente.screen != null;
    }
}
