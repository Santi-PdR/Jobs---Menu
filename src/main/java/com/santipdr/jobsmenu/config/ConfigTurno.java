package com.santipdr.jobsmenu.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Configuracion de cliente. Todo interruptor de esta clase debe dejar un menu
 * usable y legible en cualquiera de sus dos posiciones.
 */
public final class ConfigTurno {

    public static final ForgeConfigSpec SPEC;
    public static final ConfigTurno INSTANCE;

    public final ForgeConfigSpec.BooleanValue menuPropio;
    public final ForgeConfigSpec.BooleanValue escenaViva;
    public final ForgeConfigSpec.BooleanValue movimientoReducido;
    public final ForgeConfigSpec.BooleanValue destellosReducidos;
    public final ForgeConfigSpec.BooleanValue interfazMinima;
    public final ForgeConfigSpec.BooleanValue mostrarCuentaRegresiva;
    public final ForgeConfigSpec.BooleanValue avisosRotativos;

    static {
        Pair<ConfigTurno, ForgeConfigSpec> par = new ForgeConfigSpec.Builder().configure(ConfigTurno::new);
        INSTANCE = par.getLeft();
        SPEC = par.getRight();
    }

    private ConfigTurno(ForgeConfigSpec.Builder builder) {
        builder.comment("Registro de turnos - preferencias del puesto de trabajo").push("turno");

        this.menuPropio = builder
                .comment("Sustituir la pantalla de titulo por el tablon de turnos.")
                .define("menu_propio", true);

        this.escenaViva = builder
                .comment("Animar el fondo del deposito. En false la composicion queda estatica.")
                .define("escena_viva", true);

        this.movimientoReducido = builder
                .comment("Apagar lluvia, polvo y siluetas del fondo.")
                .define("movimiento_reducido", false);

        this.destellosReducidos = builder
                .comment("Congelar el parpadeo de la lampara y el pulso de alerta.")
                .define("destellos_reducidos", false);

        this.interfazMinima = builder
                .comment("Dejar solo cabecera y renglones del tablon.")
                .define("interfaz_minima", false);

        this.mostrarCuentaRegresiva = builder
                .comment("Mostrar el tiempo estimado hasta la proxima aparicion.")
                .define("mostrar_cuenta_regresiva", true);

        this.avisosRotativos = builder
                .comment("Mostrar los avisos de la empresa al pie del tablon.")
                .define("avisos_rotativos", true);

        builder.pop();
    }

    private static boolean leer(ForgeConfigSpec.BooleanValue valor, boolean porDefecto) {
        if (!SPEC.isLoaded()) {
            return porDefecto;
        }
        return valor.get();
    }

    public static boolean menuPropio() {
        return leer(INSTANCE.menuPropio, true);
    }

    public static boolean escenaViva() {
        return leer(INSTANCE.escenaViva, true);
    }

    public static boolean movimientoReducido() {
        return leer(INSTANCE.movimientoReducido, false);
    }

    public static boolean destellosReducidos() {
        return leer(INSTANCE.destellosReducidos, false);
    }

    public static boolean interfazMinima() {
        return leer(INSTANCE.interfazMinima, false);
    }

    public static boolean mostrarCuentaRegresiva() {
        return !interfazMinima() && leer(INSTANCE.mostrarCuentaRegresiva, true);
    }

    public static boolean avisosRotativos() {
        return !interfazMinima() && leer(INSTANCE.avisosRotativos, true);
    }
}
