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
    public final ForgeConfigSpec.BooleanValue pausaPropia;
    public final ForgeConfigSpec.BooleanValue escenaViva;
    public final ForgeConfigSpec.BooleanValue movimientoReducido;
    public final ForgeConfigSpec.BooleanValue destellosReducidos;
    public final ForgeConfigSpec.BooleanValue interfazMinima;
    public final ForgeConfigSpec.BooleanValue mostrarCuentaRegresiva;
    public final ForgeConfigSpec.BooleanValue avisosRotativos;
    public final ForgeConfigSpec.BooleanValue rotarNiveles;
    public final ForgeConfigSpec.IntValue nivelFijo;
    public final ForgeConfigSpec.BooleanValue sonidoBotones;
    public final ForgeConfigSpec.BooleanValue sonidoAmbiente;
    public final ForgeConfigSpec.IntValue volumenAmbiente;
    public final ForgeConfigSpec.BooleanValue musicaMenu;
    public final ForgeConfigSpec.IntValue volumenMusica;
    public final ForgeConfigSpec.BooleanValue creditoMusica;

    static {
        Pair<ConfigTurno, ForgeConfigSpec> par = new ForgeConfigSpec.Builder().configure(ConfigTurno::new);
        INSTANCE = par.getLeft();
        SPEC = par.getRight();
    }

    private ConfigTurno(ForgeConfigSpec.Builder builder) {
        builder.comment("Aviso a los ocupantes - preferencias de la estancia").push("nivel");

        this.menuPropio = builder
                .comment("Sustituir la pantalla de titulo por el aviso del nivel.")
                .define("menu_propio", true);

        this.pausaPropia = builder
                .comment("Sustituir la pantalla de pausa por el aviso de estancia en suspenso.")
                .define("pausa_propia", true);

        this.escenaViva = builder
                .comment("Animar el recinto del nivel. En false la composicion queda estatica.")
                .define("escena_viva", true);

        this.movimientoReducido = builder
                .comment("Apagar el polvo en suspension y lo que se ve al fondo del recinto.")
                .define("movimiento_reducido", false);

        this.destellosReducidos = builder
                .comment("Congelar el parpadeo de los fluorescentes y el pulso de alerta.")
                .define("destellos_reducidos", false);

        this.interfazMinima = builder
                .comment("Dejar solo la cabecera y los renglones, sin la hoja del aviso.")
                .define("interfaz_minima", false);

        this.mostrarCuentaRegresiva = builder
                .comment("Mostrar el tiempo estimado hasta la proxima ronda.")
                .define("mostrar_cuenta_regresiva", true);

        this.avisosRotativos = builder
                .comment("Mostrar los avisos de la administracion al pie de la hoja.")
                .define("avisos_rotativos", true);

        this.rotarNiveles = builder
                .comment("Ir cambiando de nivel solo, con el apagon entre uno y otro.")
                .define("rotar_niveles", true);

        this.nivelFijo = builder
                .comment("Nivel a mostrar cuando la rotacion esta apagada. 0 es el papel mural.")
                .defineInRange("nivel_fijo", 0, 0, 12);

        this.sonidoBotones = builder
                .comment("Sonar la casilla al recorrer y al marcar los renglones del aviso.")
                .define("sonido_botones", true);

        this.sonidoAmbiente = builder
                .comment("Dejar sonando el ambiente del nivel: el fondo, sus ruidos y la instalacion.")
                .define("sonido_ambiente", true);

        this.volumenAmbiente = builder
                .comment("Volumen del ambiente del nivel, de 0 a 100.")
                .defineInRange("volumen_ambiente", 55, 0, 100);

        this.musicaMenu = builder
                .comment("Dejar sonando el tema del menu por debajo de todo lo demas.")
                .define("musica_menu", true);

        this.volumenMusica = builder
                .comment("Volumen del tema del menu, de 0 a 100.")
                .defineInRange("volumen_musica", 70, 0, 100);

        this.creditoMusica = builder
                .comment("Mostrar el credito de la pista (titulo y autor) al empezar a sonar, arriba a la derecha.")
                .define("credito_musica", true);

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

    public static boolean pausaPropia() {
        return leer(INSTANCE.pausaPropia, true);
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

    public static boolean rotarNiveles() {
        return escenaViva() && leer(INSTANCE.rotarNiveles, true);
    }

    public static int nivelFijo() {
        if (!SPEC.isLoaded()) {
            return 0;
        }
        return INSTANCE.nivelFijo.get();
    }

    public static boolean sonidoBotones() {
        return leer(INSTANCE.sonidoBotones, true);
    }

    public static boolean sonidoAmbiente() {
        return leer(INSTANCE.sonidoAmbiente, true);
    }

    public static boolean musicaMenu() {
        return leer(INSTANCE.musicaMenu, true);
    }

    public static boolean creditoMusica() {
        return leer(INSTANCE.creditoMusica, true);
    }

    /** Volumen del tema del menu, ya convertido a la escala 0.0 - 1.0 del motor. */
    public static float volumenMusica() {
        if (!SPEC.isLoaded()) {
            return 0.70F;
        }
        return INSTANCE.volumenMusica.get() / 100.0F;
    }

    /** Volumen del ambiente ya convertido a la escala 0.0 - 1.0 del motor. */
    public static float volumenAmbiente() {
        if (!SPEC.isLoaded()) {
            return 0.55F;
        }
        return INSTANCE.volumenAmbiente.get() / 100.0F;
    }

    // ----------------------------------------------------------------------
    // Lectura y escritura para la pantalla de opciones
    //
    // La pantalla de "Condiciones de estancia" cambia estos valores en vivo y
    // los deja escritos en el .toml, para que no haya que editarlo a mano. Cada
    // setter escribe Y guarda: un ajuste que no sobrevive al cierre del juego
    // no es un ajuste, es un capricho de la sesion.
    //
    // Los tres getters "en bruto" existen porque sus getters publicos combinan
    // el valor con otro (rotar depende de escena viva; la cuenta y los avisos
    // dependen de interfaz minima). La casilla de la opcion tiene que reflejar
    // lo que el jugador eligio, no el resultado ya combinado, o se veria
    // destildada aunque el jugador la hubiese marcado.
    // ----------------------------------------------------------------------

    private static void fijar(ForgeConfigSpec.BooleanValue destino, boolean valor) {
        if (!SPEC.isLoaded()) {
            return;
        }
        destino.set(valor);
        destino.save();
    }

    private static void fijar(ForgeConfigSpec.IntValue destino, int valor) {
        if (!SPEC.isLoaded()) {
            return;
        }
        destino.set(valor);
        destino.save();
    }

    /** El valor guardado de rotar niveles, sin combinar con escena viva. */
    public static boolean rotarNivelesBruto() {
        return leer(INSTANCE.rotarNiveles, true);
    }

    /** El valor guardado de la cuenta, sin combinar con interfaz minima. */
    public static boolean mostrarCuentaRegresivaBruto() {
        return leer(INSTANCE.mostrarCuentaRegresiva, true);
    }

    /** El valor guardado de los avisos, sin combinar con interfaz minima. */
    public static boolean avisosRotativosBruto() {
        return leer(INSTANCE.avisosRotativos, true);
    }

    /** Volumen de la musica en la escala 0 a 100 que ve el jugador. */
    public static int volumenMusicaPorcentaje() {
        return SPEC.isLoaded() ? INSTANCE.volumenMusica.get() : 70;
    }

    /** Volumen del ambiente en la escala 0 a 100 que ve el jugador. */
    public static int volumenAmbientePorcentaje() {
        return SPEC.isLoaded() ? INSTANCE.volumenAmbiente.get() : 55;
    }

    /** Nivel fijo en la escala que ve el jugador (0 a 12). */
    public static int nivelFijoBruto() {
        return SPEC.isLoaded() ? INSTANCE.nivelFijo.get() : 0;
    }

    public static void fijarMenuPropio(boolean valor) {
        fijar(INSTANCE.menuPropio, valor);
    }

    public static void fijarPausaPropia(boolean valor) {
        fijar(INSTANCE.pausaPropia, valor);
    }

    public static void fijarEscenaViva(boolean valor) {
        fijar(INSTANCE.escenaViva, valor);
    }

    public static void fijarRotarNiveles(boolean valor) {
        fijar(INSTANCE.rotarNiveles, valor);
    }

    public static void fijarMovimientoReducido(boolean valor) {
        fijar(INSTANCE.movimientoReducido, valor);
    }

    public static void fijarDestellosReducidos(boolean valor) {
        fijar(INSTANCE.destellosReducidos, valor);
    }

    public static void fijarInterfazMinima(boolean valor) {
        fijar(INSTANCE.interfazMinima, valor);
    }

    public static void fijarMostrarCuentaRegresiva(boolean valor) {
        fijar(INSTANCE.mostrarCuentaRegresiva, valor);
    }

    public static void fijarAvisosRotativos(boolean valor) {
        fijar(INSTANCE.avisosRotativos, valor);
    }

    public static void fijarSonidoBotones(boolean valor) {
        fijar(INSTANCE.sonidoBotones, valor);
    }

    public static void fijarSonidoAmbiente(boolean valor) {
        fijar(INSTANCE.sonidoAmbiente, valor);
    }

    public static void fijarMusicaMenu(boolean valor) {
        fijar(INSTANCE.musicaMenu, valor);
    }

    public static void fijarCreditoMusica(boolean valor) {
        fijar(INSTANCE.creditoMusica, valor);
    }

    public static void fijarVolumenMusica(int porcentaje) {
        fijar(INSTANCE.volumenMusica, Math.max(0, Math.min(100, porcentaje)));
    }

    public static void fijarVolumenAmbiente(int porcentaje) {
        fijar(INSTANCE.volumenAmbiente, Math.max(0, Math.min(100, porcentaje)));
    }

    public static void fijarNivelFijo(int nivel) {
        fijar(INSTANCE.nivelFijo, Math.max(0, Math.min(12, nivel)));
    }
}
