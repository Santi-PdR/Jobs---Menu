package com.santipdr.jobsmenu.client.ui;

import java.time.LocalDateTime;

/** Easter eggs discretos y deterministas por sesion. No cambian gameplay. */
public final class SecretosJobs {

    private static final long SEMILLA = System.nanoTime() ^ System.currentTimeMillis();
    private static final boolean EXPEDIENTE_RARO = Math.floorMod(SEMILLA, 240L) == 0L;
    private static final boolean ARCHIVO_NEGRO = Math.floorMod(SEMILLA >>> 7, 511L) == 0L;
    private static final boolean TURNO_FANTASMA = Math.floorMod(SEMILLA >>> 11, 173L) == 0L;

    private SecretosJobs() {
    }

    public static boolean expedienteRaro() {
        return EXPEDIENTE_RARO;
    }

    public static boolean archivoNegro() {
        return ARCHIVO_NEGRO;
    }

    public static boolean turnoFantasma() {
        return TURNO_FANTASMA;
    }

    public static boolean hora333() {
        LocalDateTime ahora = LocalDateTime.now();
        return ahora.getHour() == 3 && ahora.getMinute() == 33;
    }

    public static boolean minuto13() {
        LocalDateTime ahora = LocalDateTime.now();
        return ahora.getMinute() == 13 && Math.floorMod(SEMILLA, 7L) == 0L;
    }

    public static String codigoExpediente() {
        long valor = Math.floorMod(SEMILLA, 0xFFFFFL);
        return String.format(java.util.Locale.ROOT, "J-%05X", valor);
    }

    public static String codigoArchivoNegro() {
        long valor = Math.floorMod(SEMILLA >>> 3, 0xFFFL);
        return String.format(java.util.Locale.ROOT, "ARCH-%03X", valor);
    }
}
