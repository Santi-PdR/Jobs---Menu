package com.santipdr.jobsmenu.client.ui;

/**
 * Cuenta regresiva a la proxima ronda de los Executores.
 *
 * Ciclo fijo anclado al reloj del sistema: no depende de la partida ni del
 * servidor. Es deliberadamente inutil, el jugador no puede hacer nada al
 * respecto. Solo esta ahi para recordarle que el tiempo corre y que la
 * tarifa del nivel siguiente no se paga sola.
 */
public final class RelojAparicion {

    private RelojAparicion() {
    }

    /** Duracion del ciclo, en milisegundos. Trece minutos. */
    public static final long CICLO_MS = 13L * 60L * 1000L;

    /** Tiempo, en milisegundos, que el rotulo se queda en "ronda en curso". */
    private static final long VENTANA_RONDA_MS = 4000L;

    private static final long UMBRAL_ALERTA_MS = 60_000L;
    private static final long UMBRAL_INMINENTE_MS = 8_000L;

    /** Milisegundos que faltan para el proximo cambio de ciclo. */
    public static long restanteMs() {
        long ahora = System.currentTimeMillis();
        return CICLO_MS - Math.floorMod(ahora, CICLO_MS);
    }

    /** Cierto durante los primeros segundos posteriores al cambio de ciclo. */
    public static boolean enRonda() {
        return (CICLO_MS - restanteMs()) < VENTANA_RONDA_MS;
    }

    public static boolean enAlerta() {
        return restanteMs() <= UMBRAL_ALERTA_MS;
    }

    public static boolean inminente() {
        return restanteMs() <= UMBRAL_INMINENTE_MS;
    }

    /** Formato MM:SS del tiempo restante. */
    public static String formatoRestante() {
        long total = Math.max(0L, restanteMs()) / 1000L;
        long minutos = total / 60L;
        long segundos = total % 60L;
        return String.format("%02d:%02d", minutos, segundos);
    }

    /**
     * Color del rotulo segun el estado del ciclo. Con destellos reducidos el
     * pulso desaparece y el color queda fijo.
     */
    public static int color(boolean destellosReducidos) {
        if (enRonda()) {
            return destellosReducidos ? Paleta.ALERTA : Paleta.ALERTA_BRILLO;
        }
        if (inminente()) {
            if (destellosReducidos) {
                return Paleta.ALERTA;
            }
            boolean encendido = (System.currentTimeMillis() / 250L) % 2L == 0L;
            return encendido ? Paleta.ALERTA_BRILLO : Paleta.ALERTA;
        }
        if (enAlerta()) {
            return Paleta.ALERTA;
        }
        return Paleta.TINTA_TENUE;
    }

    /**
     * Cuanto se apaga el fluorescente por cercania de la ronda, de 0.0 a 1.0.
     * La luz cae justo despues del cambio de ciclo y se recupera de a poco.
     */
    public static float penumbra() {
        if (enRonda()) {
            long transcurrido = CICLO_MS - restanteMs();
            return 1.0F - (float) transcurrido / (float) VENTANA_RONDA_MS;
        }
        if (inminente()) {
            return 0.35F;
        }
        return 0.0F;
    }
}

