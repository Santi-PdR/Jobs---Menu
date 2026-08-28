package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.config.ConfigTurno;

/**
 * Decide en que nivel esta parado el menu y como se pasa de uno al siguiente.
 *
 * La rotacion normal se calcula desde el reloj del sistema. Encima de ella hay
 * un modo de inspeccion TEMPORAL, usado solo mientras el menu esta abierto para
 * recorrer los fondos a mano. No toca la configuracion ni se persiste.
 */
public final class RotacionNiveles {

    private RotacionNiveles() {
    }

    private static final long ESTANCIA_MS = 24_000L;
    private static final long TRANSICION_MS = 2_600L;
    private static final float REPARTO_APAGADO = 0.42F;
    private static final long AVISO_MS = 1_400L;
    private static final long CICLO_MS = ESTANCIA_MS + TRANSICION_MS;

    /** -1 = rotacion/config normal; 0..N = nivel temporal elegido a mano. */
    private static int inspeccionNivel = -1;

    private static final float[][] AVISO_CHISPAZOS = {
            {0.28F, 0.34F, 0.72F},
            {0.66F, 0.71F, 0.55F},
    };

    private static final float[][] CORTE_CHISPAZOS = {
            {0.35F, 0.44F, 0.25F},
            {0.62F, 0.68F, 0.40F},
    };

    /** Indice del nivel que se esta mostrando ahora mismo. */
    public static int indiceActual() {
        if (inspeccionNivel >= 0) {
            return Math.floorMod(inspeccionNivel, Nivel.cantidad());
        }
        if (!ConfigTurno.rotarNiveles()) {
            return ConfigTurno.nivelFijo();
        }
        long total = CICLO_MS * Nivel.cantidad();
        long t = Math.floorMod(System.currentTimeMillis(), total);
        int indice = (int) (t / CICLO_MS);
        long dentro = t % CICLO_MS;
        if (dentro >= ESTANCIA_MS + (long) (TRANSICION_MS * REPARTO_APAGADO)) {
            indice++;
        }
        return indice % Nivel.cantidad();
    }

    public static Nivel actual() {
        return Nivel.porIndice(indiceActual());
    }

    /** Activa inspeccion temporal y avanza un recinto. */
    public static void inspeccionarSiguiente() {
        int base = inspeccionNivel >= 0 ? inspeccionNivel : indiceActual();
        inspeccionNivel = Math.floorMod(base + 1, Nivel.cantidad());
    }

    /** Activa inspeccion temporal y retrocede un recinto. */
    public static void inspeccionarAnterior() {
        int base = inspeccionNivel >= 0 ? inspeccionNivel : indiceActual();
        inspeccionNivel = Math.floorMod(base - 1, Nivel.cantidad());
    }

    /** Vuelve al comportamiento configurado sin guardar nada. */
    public static void terminarInspeccion() {
        inspeccionNivel = -1;
    }

    public static boolean inspeccionActiva() {
        return inspeccionNivel >= 0;
    }

    public static float luzDisponible() {
        if (inspeccionActiva()) {
            return 1.0F;
        }
        if (!ConfigTurno.rotarNiveles()) {
            return 1.0F;
        }
        long dentro = posicionEnCiclo();
        if (dentro < ESTANCIA_MS) {
            return preaviso(dentro);
        }

        long transcurrido = dentro - ESTANCIA_MS;
        long apagado = (long) (TRANSICION_MS * REPARTO_APAGADO);

        if (transcurrido < apagado) {
            float t = (float) transcurrido / (float) apagado;
            float caida = 1.0F - t * t;
            if (!ConfigTurno.destellosReducidos()) {
                for (float[] c : CORTE_CHISPAZOS) {
                    if (t > c[0] && t < c[1]) {
                        caida *= c[2];
                    }
                }
            }
            return Math.max(0.0F, caida);
        }

        float t = (float) (transcurrido - apagado) / (float) (TRANSICION_MS - apagado);
        return arranqueTubo(t);
    }

    private static float preaviso(long dentro) {
        if (ConfigTurno.destellosReducidos()) {
            return 1.0F;
        }
        long falta = ESTANCIA_MS - dentro;
        if (falta > AVISO_MS) {
            return 1.0F;
        }
        float t = 1.0F - falta / (float) AVISO_MS;
        for (float[] c : AVISO_CHISPAZOS) {
            if (t > c[0] && t < c[1]) {
                return c[2];
            }
        }
        return 1.0F - 0.06F * t;
    }

    public static int chispazoActual() {
        if (inspeccionActiva() || !ConfigTurno.rotarNiveles() || ConfigTurno.destellosReducidos()) {
            return -1;
        }
        long dentro = posicionEnCiclo();

        if (dentro < ESTANCIA_MS) {
            long falta = ESTANCIA_MS - dentro;
            if (falta > AVISO_MS) {
                return -1;
            }
            float t = 1.0F - falta / (float) AVISO_MS;
            for (int i = 0; i < AVISO_CHISPAZOS.length; i++) {
                float[] c = AVISO_CHISPAZOS[i];
                if (t > c[0] && t < c[1]) {
                    return i;
                }
            }
            return -1;
        }

        long transcurrido = dentro - ESTANCIA_MS;
        long apagado = (long) (TRANSICION_MS * REPARTO_APAGADO);
        if (transcurrido >= apagado) {
            return -1;
        }
        float t = (float) transcurrido / (float) apagado;
        for (int i = 0; i < CORTE_CHISPAZOS.length; i++) {
            float[] c = CORTE_CHISPAZOS[i];
            if (t > c[0] && t < c[1]) {
                return 10 + i;
            }
        }
        return -1;
    }

    public static float pesoChispazo(int indice) {
        if (indice < 0) {
            return 0.0F;
        }
        if (indice >= 10) {
            int i = indice - 10;
            return i < CORTE_CHISPAZOS.length ? 1.0F - CORTE_CHISPAZOS[i][2] : 0.0F;
        }
        return indice < AVISO_CHISPAZOS.length ? 1.0F - AVISO_CHISPAZOS[indice][2] : 0.0F;
    }

    public static float arranqueTubo(float avance) {
        if (avance <= 0.0F) return 0.0F;
        if (avance >= 1.0F) return 1.0F;
        if (ConfigTurno.destellosReducidos()) return avance;
        if (avance < 0.12F) return 0.55F;
        if (avance < 0.20F) return 0.05F;
        if (avance < 0.30F) return 0.80F;
        if (avance < 0.36F) return 0.10F;
        if (avance < 0.46F) return 0.35F;
        return Math.min(1.0F, 0.35F + (avance - 0.46F) / 0.54F * 0.65F);
    }

    public static boolean enTransicion() {
        if (inspeccionActiva() || !ConfigTurno.rotarNiveles()) {
            return false;
        }
        return posicionEnCiclo() >= ESTANCIA_MS;
    }

    public static boolean porTransicionar() {
        if (inspeccionActiva() || !ConfigTurno.rotarNiveles()) {
            return false;
        }
        long dentro = posicionEnCiclo();
        return dentro >= ESTANCIA_MS - AVISO_MS && dentro < ESTANCIA_MS;
    }

    public static float avanceTransicion() {
        if (!enTransicion()) return 0.0F;
        return (posicionEnCiclo() - ESTANCIA_MS) / (float) TRANSICION_MS;
    }

    public static float repartoApagado() {
        return REPARTO_APAGADO;
    }

    private static long posicionEnCiclo() {
        long total = CICLO_MS * Nivel.cantidad();
        return Math.floorMod(System.currentTimeMillis(), total) % CICLO_MS;
    }
}
