package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.client.sound.GestorAmbiente;
import com.santipdr.jobsmenu.client.sound.GestorMusica;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Ciclo de vida de una visita completa al menu Jobs. */
public final class SesionMenu {

    private static boolean activa;
    private static long inicioVisita;
    private static long numeroVisita;
    private static int pantallasVisitadas;
    private static Screen ultimaPantalla;

    private SesionMenu() {
    }

    public static void abrir() {
        // Una visita nueva reinicia solo la instrumentacion de sesion. Volver
        // desde Options/Mods/etc. conserva el mismo reloj y el mismo audio.
        if (!activa) {
            GestorMusica.nuevaVisita();
            inicioVisita = System.currentTimeMillis();
            numeroVisita++;
            pantallasVisitadas = 0;
            ultimaPantalla = null;
        }
        activa = true;
        GestorAmbiente.abrir();
    }

    public static void cerrar() {
        activa = false;
        ultimaPantalla = null;
        GestorMusica.detenerAhora();
        GestorAmbiente.cerrar();
    }

    /**
     * Registra cambios reales de Screen sin contar frames. Es solo telemetria
     * local de interfaz para el HUD; no se guarda en disco ni sale por red.
     */
    public static void registrarPantalla(Screen pantalla) {
        if (!activa || pantalla == null || pantalla == ultimaPantalla) return;
        ultimaPantalla = pantalla;
        pantallasVisitadas++;
    }

    /** Milisegundos transcurridos desde que empezo la visita actual. */
    public static long duracionVisitaMs() {
        if (!activa || inicioVisita <= 0L) return 0L;
        return Math.max(0L, System.currentTimeMillis() - inicioVisita);
    }

    /** Numero de pantallas distintas abiertas durante la visita actual. */
    public static int pantallasVisitadas() {
        return Math.max(0, pantallasVisitadas);
    }

    /** Contador de visitas de esta ejecucion de Minecraft. */
    public static long numeroVisita() {
        return Math.max(0L, numeroVisita);
    }

    /**
     * La visita solo puede seguir viva fuera de un mundo y con alguna pantalla
     * abierta. El chequeo defensivo cubre cargas, desconexiones y cierres.
     */
    public static boolean activa() {
        Minecraft cliente = Minecraft.getInstance();
        return activa
                && ConfigTurno.menuPropio()
                && cliente.level == null
                && cliente.screen != null;
    }
}
