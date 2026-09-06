package com.santipdr.jobsmenu.client;

import com.santipdr.jobsmenu.client.sound.GestorAmbiente;
import com.santipdr.jobsmenu.client.sound.GestorMusica;
import com.santipdr.jobsmenu.client.sound.RastreadorAudioJobs;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Ciclo de vida de una visita completa al menu Jobs. */
public final class SesionMenu {

    private static boolean activa;
    private static long inicioVisita;
    private static long numeroVisita;
    private static long cierresEfectivos;
    private static int pantallasVisitadas;
    private static Screen ultimaPantalla;

    private SesionMenu() {
    }

    public static void abrir() {
        // Navegar entre pantallas Jobs forma parte de la misma visita. Antes se
        // llamaba tambien a GestorAmbiente.abrir() en cada Screen nueva; aunque
        // el gestor es idempotente, eso hacia mantenimiento extra justo durante
        // cada transicion. Una visita ya activa no necesita reinicializacion.
        if (activa) {
            return;
        }

        // Una visita nueva nunca hereda un FX puntual de una visita anterior.
        RastreadorAudioJobs.detenerTodo();
        GestorMusica.nuevaVisita();
        inicioVisita = System.currentTimeMillis();
        numeroVisita++;
        pantallasVisitadas = 0;
        ultimaPantalla = null;
        activa = true;
        GestorAmbiente.abrir();
    }

    public static void cerrar() {
        // alTickCliente llama a cerrar() durante gameplay por defensa. Despues
        // del primer hard-stop no tiene sentido recorrer musica/capas en cada
        // tick. Si aparece audio residual, los contadores vivos vuelven a abrir
        // esta ruta y se ejecuta otro corte completo.
        boolean necesitaCierre = activa
                || GestorMusica.sonando()
                || GestorAmbiente.capasActivas() > 0
                || RastreadorAudioJobs.cantidad() > 0;
        if (!necesitaCierre) return;

        activa = false;
        ultimaPantalla = null;
        GestorMusica.detenerAhora();
        GestorAmbiente.cerrar();
        RastreadorAudioJobs.detenerTodo();
        cierresEfectivos++;
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

    /** Estado bruto antes de aplicar guards de mundo/pantalla/config. */
    public static boolean activaInternaParaDiagnostico() {
        return activa;
    }

    public static long cierresEfectivosParaDiagnostico() {
        return cierresEfectivos;
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
