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

    /**
     * Los parpadeos del aviso: {desde, hasta, cuanta luz queda}.
     *
     * Es la unica definicion de cuando titila la luz antes del corte. La lee
     * el dibujo, para bajar la luz, y la lee el audio, para sonar en el mismo
     * fotograma. Tocar un numero de aca mueve la imagen y el sonido a la vez,
     * que es la unica forma de que no se separen con el tiempo.
     */
    private static final float[][] AVISO_CHISPAZOS = {
            {0.28F, 0.34F, 0.72F},
            {0.66F, 0.71F, 0.55F},
    };

    /** Los parpadeos de la caida, en fraccion de lo que ya cayo la luz. */
    private static final float[][] CORTE_CHISPAZOS = {
            {0.35F, 0.44F, 0.25F},
            {0.62F, 0.68F, 0.40F},
    };

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
            if (!ConfigTurno.destellosReducidos()) {
                for (int i = 0; i < CORTE_CHISPAZOS.length; i++) {
                    float[] c = CORTE_CHISPAZOS[i];
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
        for (int i = 0; i < AVISO_CHISPAZOS.length; i++) {
            float[] c = AVISO_CHISPAZOS[i];
            if (t > c[0] && t < c[1]) {
                return c[2];
            }
        }
        // Entre chispazo y chispazo la luz baja apenas, sin que se note.
        return 1.0F - 0.06F * t;
    }

    /**
     * Cual de los parpadeos se esta viendo ahora mismo, o -1 si ninguno.
     *
     * Existe para que el audio pueda mirar exactamente lo mismo que la imagen.
     * Antes el sonido electrico se disparaba una sola vez por transicion, con
     * su propio criterio, mientras la luz pegaba cuatro bajones en momentos
     * calculados aparte: se veian cuatro parpadeos y se oia uno, y encima
     * ninguno de los dos caia donde el otro. Ahora los dos leen esta tabla, y
     * si algo se ve, se oye.
     *
     * Los indices no se reutilizan entre fases -los del aviso van del 0 al 1 y
     * los del corte del 10 en adelante- para que quien los siga pueda
     * distinguir un parpadeo nuevo de otro sin llevar la cuenta de la fase.
     */
    public static int chispazoActual() {
        if (!ConfigTurno.rotarNiveles() || ConfigTurno.destellosReducidos()) {
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

    /**
     * Cuanto pesa el parpadeo que se esta viendo, de 0 a 1.
     *
     * Un bajon del 28 por ciento no puede sonar igual que uno que deja el
     * recinto casi a oscuras. El audio usa esto para el volumen del chasquido.
     */
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

