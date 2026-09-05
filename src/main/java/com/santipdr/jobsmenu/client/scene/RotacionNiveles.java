package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.config.ConfigTurno;

/**
 * Decide en que nivel esta parado el menu y como se pasa de uno al siguiente.
 *
 * Todo se calcula desde el reloj del sistema. Estado permite que renderer,
 * audio y chrome compartan nivel, luz y transicion sin mezclar instantes.
 */
public final class RotacionNiveles {

    private RotacionNiveles() {
    }

    private static final long SUSPENSION_MS = 22_000L;
    private static final long SUSPENSION_RANURA_MS = 48L * 60_000L;
    private static final long SUSPENSION_INICIO_MS = 22L * 60_000L;
    private static final long SUSPENSION_JITTER_MS = 3L * 60_000L + 30_000L;
    private static final long SUSPENSION_BORDE_MS = 1_500L;
    private static final long TRANSICION_MS = 2_600L;
    private static final float REPARTO_APAGADO = 0.42F;
    private static final long AVISO_MS = 1_400L;

    private static final float[][] AVISO_CHISPAZOS = {
            {0.28F, 0.34F, 0.72F},
            {0.66F, 0.71F, 0.55F},
    };

    private static final float[][] CORTE_CHISPAZOS = {
            {0.35F, 0.44F, 0.25F},
            {0.62F, 0.68F, 0.40F},
    };

    /** Desplazamiento de la rotacion pedido a mano en esta sesion. */
    private static int desplazamiento;

    /** Instante del ultimo salto manual, o Long.MIN_VALUE si todavia no hubo. */
    private static long ultimoSalto = Long.MIN_VALUE;

    /**
     * Renderer, tres camas, musica y chrome pueden pedir el mismo estado dentro
     * del mismo milisegundo. Compartir ese record elimina calculos/asignaciones
     * duplicadas sin introducir una ventana temporal nueva: al cambiar el reloj
     * un solo milisegundo el estado se vuelve a calcular.
     */
    private static long instanteCache = Long.MIN_VALUE;
    private static Estado estadoCache;

    /** Adelanta un nivel conservando la ventana de apagon del salto manual. */
    public static void adelantar() {
        desplazamiento++;
        ultimoSalto = System.currentTimeMillis();
        invalidarCache();
    }

    public record Estado(int indice, Nivel nivel, float luz, long ahora, long dentro,
                          long estancia, boolean rotacion, boolean suspension,
                          float avanceSuspension, long cicloSuspension) {

        public boolean enTransicion() {
            return this.rotacion && this.dentro >= this.estancia;
        }

        public boolean enSuspension() {
            return this.suspension;
        }

        public float avanceSuspension() {
            return this.avanceSuspension;
        }

        public float avanceTransicion() {
            if (!enTransicion()) {
                return 0.0F;
            }
            return Math.max(0.0F, Math.min(1.0F,
                    (this.dentro - this.estancia) / (float) TRANSICION_MS));
        }
    }

    /** Captura nivel, luz y posicion de la transicion con una sola lectura. */
    public static Estado capturar() {
        long ahora = System.currentTimeMillis();
        Estado cache = estadoCache;
        if (cache != null && instanteCache == ahora) {
            return cache;
        }
        Estado calculado = calcular(ahora);
        instanteCache = ahora;
        estadoCache = calculado;
        return calculado;
    }

    private static Estado calcular(long ahora) {
        if (!ConfigTurno.rotarNiveles()) {
            int indice = ConfigTurno.nivelFijo();
            return new Estado(indice, Nivel.porIndice(indice), 1.0F, ahora,
                    0L, 0L, false, false, 0.0F, -1L);
        }

        long estancia = estanciaMs();
        long ciclo = estancia + TRANSICION_MS;
        long total = ciclo * Nivel.cantidad();
        long reloj = Math.floorMod(ahora, total);
        int indice = (int) (reloj / ciclo);
        long dentro = reloj % ciclo;

        if (dentro >= estancia + (long) (TRANSICION_MS * REPARTO_APAGADO)) {
            indice = (indice + 1) % Nivel.cantidad();
        }

        if (desplazamiento != 0) {
            indice = Math.floorMod(indice + desplazamiento, Nivel.cantidad());
            if (ultimoSalto != Long.MIN_VALUE) {
                long desde = ahora - ultimoSalto;
                if (desde >= 0L && desde < TRANSICION_MS) {
                    dentro = estancia + Math.min(TRANSICION_MS - 1L, desde);
                }
            }
        }
        float luz = luzPara(dentro, estancia);
        long cicloSuspension = Math.floorDiv(ahora, SUSPENSION_RANURA_MS);
        long inicioSuspension = inicioSuspension(cicloSuspension);
        long dentroSuspension = ahora - inicioSuspension;
        boolean suspension = ConfigTurno.suspensionRara()
                && dentroSuspension >= 0L && dentroSuspension < SUSPENSION_MS;
        float avanceSuspension = suspension
                ? dentroSuspension / (float) SUSPENSION_MS : 0.0F;
        if (suspension) {
            luz = luzSuspension(avanceSuspension);
        }

        return new Estado(indice, Nivel.porIndice(indice), luz, ahora,
                dentro, estancia, true, suspension, avanceSuspension, cicloSuspension);
    }

    private static void invalidarCache() {
        instanteCache = Long.MIN_VALUE;
        estadoCache = null;
    }

    public static int indiceActual() {
        return capturar().indice();
    }

    public static Nivel actual() {
        return capturar().nivel();
    }

    public static float luzDisponible() {
        return capturar().luz();
    }

    private static float luzPara(long dentro, long estancia) {
        float luz;
        if (dentro < estancia) {
            luz = preaviso(dentro, estancia);
        } else {
            long transcurrido = dentro - estancia;
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
                luz = Math.max(0.0F, caida);
            } else {
                float t = (float) (transcurrido - apagado) / (float) (TRANSICION_MS - apagado);
                luz = arranqueTubo(t);
            }
        }
        return Math.max(0.0F, Math.min(1.0F, luz));
    }

    private static long inicioSuspension(long ciclo) {
        long mezcla = ciclo * 0x9E3779B97F4A7C15L + 0xD1B54A32D192ED03L;
        mezcla ^= mezcla >>> 30;
        mezcla *= 0xBF58476D1CE4E5B9L;
        mezcla ^= mezcla >>> 27;
        mezcla *= 0x94D049BB133111EBL;
        mezcla ^= mezcla >>> 31;
        long jitter = Math.floorMod(mezcla, SUSPENSION_JITTER_MS);
        return ciclo * SUSPENSION_RANURA_MS + SUSPENSION_INICIO_MS + jitter;
    }

    private static float luzSuspension(float avance) {
        if (avance <= 0.0F) {
            return 1.0F;
        }
        if (avance >= 1.0F) {
            return 0.04F;
        }
        float entrada = Math.min(1.0F, avance * SUSPENSION_MS / SUSPENSION_BORDE_MS);
        float salida = Math.min(1.0F,
                (1.0F - avance) * SUSPENSION_MS / SUSPENSION_BORDE_MS);
        if (entrada < 1.0F) {
            float suave = entrada * entrada * (3.0F - 2.0F * entrada);
            return 1.0F - 0.96F * suave;
        }
        if (salida < 1.0F) {
            float suave = salida * salida * (3.0F - 2.0F * salida);
            return 0.04F + 0.96F * suave;
        }
        return 0.04F;
    }

    private static float preaviso(long dentro, long estancia) {
        if (ConfigTurno.destellosReducidos()) {
            return 1.0F;
        }
        long falta = estancia - dentro;
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
        return chispazoActual(capturar());
    }

    public static int chispazoActual(Estado estado) {
        if (!estado.rotacion() || estado.enSuspension() || ConfigTurno.destellosReducidos()) {
            return -1;
        }
        long dentro = estado.dentro();
        long estancia = estado.estancia();

        if (dentro < estancia) {
            long falta = estancia - dentro;
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

        long transcurrido = dentro - estancia;
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
        if (avance <= 0.0F) {
            return 0.0F;
        }
        if (avance >= 1.0F) {
            return 1.0F;
        }
        if (ConfigTurno.destellosReducidos()) {
            return avance;
        }
        if (avance < 0.12F) {
            return 0.55F;
        }
        if (avance < 0.20F) {
            return 0.05F;
        }
        if (avance < 0.30F) {
            return 0.80F;
        }
        if (avance < 0.36F) {
            return 0.10F;
        }
        if (avance < 0.46F) {
            return 0.35F;
        }
        return Math.min(1.0F, 0.35F + (avance - 0.46F) / 0.54F * 0.65F);
    }

    public static boolean enTransicion() {
        return capturar().enTransicion();
    }

    public static boolean porTransicionar() {
        Estado estado = capturar();
        return esRotacionActiva()
                && estado.dentro() >= estanciaMs() - AVISO_MS
                && estado.dentro() < estanciaMs();
    }

    public static float avanceTransicion() {
        return capturar().avanceTransicion();
    }

    public static float repartoApagado() {
        return REPARTO_APAGADO;
    }

    private static boolean esRotacionActiva() {
        return ConfigTurno.rotarNiveles();
    }

    private static long estanciaMs() {
        long base = ConfigTurno.duracionEstancia() * 1_000L;
        return ConfigTurno.rotacionCalma() ? base * 2L : base;
    }
}
