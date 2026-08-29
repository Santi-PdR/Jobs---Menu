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
        // Al volver de un mundo, Minecraft puede haber retirado todos los
        // canales del SoundEngine sin marcar como detenida nuestra instancia
        // Java. Si se conserva esa referencia, asegurar() cree que REQUIEM
        // sigue sonando y no crea una nueva. Solo una visita realmente nueva
        // invalida la referencia; volver desde Opciones mantiene continuidad.
        if (!activa) {
            GestorMusica.nuevaVisita();
        }
        activa = true;
    }

    public static void cerrar() {
        activa = false;
        // La instancia del tema sigue recibiendo ticks y baja sola hasta
        // detenerse (ver GestorMusica.tick): detenerla aca la cortaria en seco,
        // y soltar la referencia permitiria crear una copia mientras la
        // anterior todavia se oye. No hay nada que hacer, y eso es lo que hay
        // que decir.
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
