package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.client.sound.GestorAmbiente;
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
 *
 * LA CONTINUIDAD DEL AMBIENTE
 *
 * La visita abarca el aviso y sus pantallas hijas (Opciones, Sonido, Mods...).
 * Antes, al pasar a una hija se detenia el ambiente del recinto y al volver
 * arrancaba de cero: las camas de sonido reiniciaban su bucle cada vez que se
 * tocaba un deslizador, y el pasillo "arrancaba" decenas de veces por sesion.
 * Ahora abrir/cerrar la visita es lo que levanta o detiene las camas, y las
 * pantallas hijas solo pausan los sucesos puntuales, no el sitio.
 */
public final class SesionMenu {

    private static boolean activa;

    /** Permite volver una vez al titulo vanilla despues de abandonar un mundo. */
    private static boolean salidaAlTituloPendiente;

    private SesionMenu() {
    }

    public static void abrir() {
        salidaAlTituloPendiente = false;
        // Al volver de un mundo, Minecraft puede haber retirado todos los
        // canales del SoundEngine sin marcar como detenida nuestra instancia
        // Java. Si se conserva esa referencia, asegurar() cree que el tema
        // sigue sonando y no crea una nueva. Solo una visita realmente nueva
        // invalida la referencia; volver desde Opciones mantiene continuidad.
        if (!activa) {
            GestorMusica.nuevaVisita();
        }
        activa = true;
        // Idempotente: abrir() se vuelve a llamar cada vez que la pantalla se
        // reconstruye (resize, vuelta de una hija) y no debe reiniciar camas.
        GestorAmbiente.abrir();
    }

    public static void cerrar() {
        if (!activa) {
            return;
        }
        activa = false;
        // El tema baja solo hasta detenerse (ver GestorMusica.tick); detenerlo
        // aca lo cortaria en seco y soltar la referencia permitiria crear una
        // copia mientras la anterior todavia se oye. Las camas del recinto, en
        // cambio, se detienen ahora mismo: la visita termino (mundo, apagado
        // del menu o salida al titulo) y no tienen que seguir sonando en la
        // nada.
        GestorAmbiente.cerrar();
    }

    /**
     * Prepara la salida real del mundo. El siguiente TitleScreen debe quedar
     * vanilla aunque el menu propio siga habilitado para la proxima visita.
     */
    public static void prepararSalidaAlTitulo() {
        activa = false;
        salidaAlTituloPendiente = true;
        // Se abandona el mundo: el recinto no debe seguir sonando detras del
        // "Guardando..." ni del titulo vanilla.
        GestorAmbiente.cerrar();
    }

    /** Consume el permiso de mostrar una vez el titulo vanilla. */
    public static boolean consumirSalidaAlTitulo() {
        boolean salida = salidaAlTituloPendiente;
        salidaAlTituloPendiente = false;
        return salida;
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
