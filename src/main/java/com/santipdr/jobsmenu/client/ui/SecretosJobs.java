package com.santipdr.jobsmenu.client.ui;

import java.time.LocalDateTime;

/** Easter eggs discretos y deterministas por sesion. No cambian gameplay. */
public final class SecretosJobs {

    private static final long SEMILLA = System.nanoTime() ^ System.currentTimeMillis();
    private static final boolean EXPEDIENTE_RARO = Math.floorMod(SEMILLA, 240L) == 0L;

    private SecretosJobs() {
    }

    public static boolean expedienteRaro() {
        return EXPEDIENTE_RARO;
    }

    public static boolean hora333() {
        LocalDateTime ahora = LocalDateTime.now();
        return ahora.getHour() == 3 && ahora.getMinute() == 33;
    }

    public static String codigoExpediente() {
        long valor = Math.floorMod(SEMILLA, 0xFFFFFL);
        return String.format(java.util.Locale.ROOT, "J-%05X", valor);
    }
}
