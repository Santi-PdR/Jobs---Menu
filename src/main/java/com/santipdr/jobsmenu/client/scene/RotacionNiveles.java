package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.config.ConfigTurno;

/**
 * Decide en que nivel esta parado el menu y como se pasa de uno al siguiente.
 *
 * El cambio no es un fundido de postal. Aca la transicion es lo que le pasa a
 * cualquiera que se mueva entre niveles: los tubos se apagan, hay un momento en
 * que no hay nada, y cuando la luz vuelve el pasillo ya no es el mismo. Nadie
 * lo anuncia y nadie lo comenta.
 *
 * Todo se calcula desde el reloj del sistema, sin estado mutable: dos clientes
 * abiertos al mismo tiempo ven el mismo nivel.
 */
public final class RotacionNiveles {

    private RotacionNiveles() {
    }

    /** Cuanto se queda quieto cada nivel antes de empezar a irse. */
    private static final long ESTANCIA_MS = 24_000L;

    /** Cuanto dura el apagon completo, de la primera falla a la luz firme. */
    private static final long TRANSICION_MS = 2_600L;

    /** Parte de la transicion que se va en apagar el nivel viejo. */
    private static final float REPARTO_APAGADO = 0.42F;

    /** Cuanto antes del corte empieza el pasillo a dar senales. */
    private static final long AVISO_MS = 1_400L;

    /** Un ciclo entero: un nivel quieto mas su salida. */
    private static final long CICLO_MS = ESTANCIA_MS + TRANSICION_MS;

    /** Indice del nivel que se esta mostrando ahora mismo. */
    public static int indiceActual() {
        if (!ConfigTurno.rotarNiveles()) {
            return ConfigTurno.nivelFijo();
        }
        long total = CICLO_MS * Nivel.cantidad();
        long t = Math.floorMod(System.currentTimeMillis(), total);
        int indice = (int) (t / CICLO_MS);

        // Pasada la mitad de la transicion ya estamos del otro lado.
        long dentro = t % CICLO_MS;
        if (dentro >= ESTANCIA_MS + (long) (TRANSICION_MS * REPARTO_APAGADO)) {
            indice++;
        }
        return indice % Nivel.cantidad();
    }

    /** El nivel que hay que dibujar en este fotograma. */
    public static Nivel actual() {
        return Nivel.porIndice(indiceActual());
    }

    /**
     * Cuanta luz hay disponible ahora mismo, de 0.0 a 1.0.
     *
     * Vale 1.0 durante casi todo el ciclo. Cae a cero al final de la estancia y
     * vuelve a subir a los tirones, como un tubo frio que le cuesta prender.
     */
    public static float luzDisponible() {
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
            // Se va yendo: primero titila, despues se rinde.
            float t = (float) transcurrido / (float) apagado;
            float caida = 1.0F - t * t;
            if (t > 0.35F && t < 0.44F) {
                caida *= 0.25F;
            }
            if (t > 0.62F && t < 0.68F) {
                caida *= 0.40F;
            }
            return Math.max(0.0F, caida);
        }

        float t = (float) (transcurrido - apagado) / (float) (TRANSICION_MS - apagado);
        return arranqueTubo(t);
    }

    /**
     * El pasillo dando senales antes del corte.
     *
     * Durante la ultima parte de la estancia la luz no esta del todo firme: dos
     * caidas muy breves, de las que se ven de reojo. Es el primer paso del
     * flujo completo de la transicion (alteracion, titileo, corte, negro,
     * arranque) y sin el, el apagon aparece de la nada.
     *
     * Con destellos reducidos, la luz se queda quieta y el aviso desaparece.
     */
    private static float preaviso(long dentro) {
        if (ConfigTurno.destellosReducidos()) {
            return 1.0F;
        }
        long falta = ESTANCIA_MS - dentro;
        if (falta > AVISO_MS) {
            return 1.0F;
        }
        float t = 1.0F - falta / (float) AVISO_MS;
        if (t > 0.28F && t < 0.34F) {
            return 0.72F;
        }
        if (t > 0.66F && t < 0.71F) {
            return 0.55F;
        }
        // Entre chispazo y chispazo la luz baja apenas, sin que se note.
        return 1.0F - 0.06F * t;
    }

    /**
     * Encendido de un fluorescente frio: dos chispazos, una duda, y recien ahi
     * se queda prendido. Con destellos reducidos sube derecho.
     */
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

    /** Si en este momento el pasillo esta cambiando de nivel. */
    public static boolean enTransicion() {
        if (!ConfigTurno.rotarNiveles()) {
            return false;
        }
        return posicionEnCiclo() >= ESTANCIA_MS;
    }

    /**
     * Si falta poco para el cambio y conviene ir avisando.
     *
     * La ventana empieza AVISO_MS antes del corte. Sirve para que el titileo
     * electrico se escuche mientras la luz todavia esta firme: primero se
     * sospecha que algo va a pasar y despues pasa. Al reves no funciona.
     */
    public static boolean porTransicionar() {
        if (!ConfigTurno.rotarNiveles()) {
            return false;
        }
        long dentro = posicionEnCiclo();
        return dentro >= ESTANCIA_MS - AVISO_MS && dentro < ESTANCIA_MS;
    }

    /** Cuanto de la transicion actual ya paso, de 0 a 1. Fuera de ella, 0. */
    public static float avanceTransicion() {
        if (!enTransicion()) {
            return 0.0F;
        }
        return (posicionEnCiclo() - ESTANCIA_MS) / (float) TRANSICION_MS;
    }

    /** Que fraccion de la transicion se va en apagar el nivel viejo. */
    public static float repartoApagado() {
        return REPARTO_APAGADO;
    }

    /** Milisegundos transcurridos dentro del ciclo del nivel actual. */
    private static long posicionEnCiclo() {
        long total = CICLO_MS * Nivel.cantidad();
        return Math.floorMod(System.currentTimeMillis(), total) % CICLO_MS;
    }
}
