package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Trazo;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Lo que a veces esta al fondo del corredor.
 *
 * POR QUE ESTA HECHA ASI
 *
 * La version anterior era un munequito que cruzaba el vano en dos segundos con
 * las piernas alternando. El problema no era el dibujo: era el concepto. Una
 * figura que camina de un lado al otro es un personaje, y un personaje se lee
 * enseguida, se entiende, y a la tercera vez que pasa deja de importar. Ademas
 * atravesaba el centro de la composicion y le robaba la escena al aviso, que es
 * lo que el jugador tendria que estar mirando.
 *
 * Esta version invierte las cuatro decisiones:
 *
 *   1. NO SE MUEVE. Aparece ya estando ahi. No entra, no sale, no camina. Lo
 *      unico que hace es dejar de estar. Que algo quieto aparezca donde no
 *      habia nada inquieta mas que cualquier movimiento.
 *   2. NO TIENE ANATOMIA. Es una columna vertical que se afina hacia arriba,
 *      sin cabeza, sin hombros, sin piernas. Podria ser una persona muy alta o
 *      podria ser un cano. Esa duda es todo el efecto: en cuanto se le dibujan
 *      hombros deja de ser ambigua y pasa a ser un monstruo.
 *   3. ESTA LEJOS. Ocupa la abertura del fondo, no el primer plano. Nunca se
 *      acerca, nunca crece. La distancia es lo que la vuelve un detalle de la
 *      escena y no su protagonista.
 *   4. ENTRA Y SALE LENTO. Varios segundos de aparicion y de retirada, con
 *      alfa bajo. Nada de apariciones subitas: no hay ni va a haber sustos.
 *
 * El recurso mas fuerte no es ninguna de las cuatro, sino la reaparicion: la
 * figura se muestra, se va, y unos segundos despues vuelve corrida unos pixeles
 * hacia un lado. El jugador no llega a estar seguro de que se movio.
 *
 * LO QUE LE FALTABA: MODOS
 *
 * Con todo lo anterior la figura seguia teniendo un defecto de fondo, y no era
 * el dibujo: era que SIEMPRE PASABA LO MISMO. Se asomaba de la misma forma, con
 * la misma silueta y el mismo desvanecido, cada 71 segundos. A la tercera vez
 * el ojo ya sabe que esperar y deja de mirar. Lo que provoca el "que acabo de
 * ver" no es la silueta: es no poder anticipar QUE va a pasar.
 *
 * Por eso cada ciclo elige un modo distinto, sesgado para que lo raro sea raro:
 *
 *   QUIETA     esta ahi y se desvanece. La de siempre, algo mas de la mitad.
 *   CORTE      aparece y desaparece dentro de un parpadeo, apenas unos cuadros.
 *              Da tiempo a registrarla y no a mirarla.
 *   SUMERGIDA  solo el reflejo en el agua; arriba no hay nada parado. Es el que
 *              mas incomoda: hay reflejo de algo que no esta.
 *   DOBLE      dos siluetas a la vez en los dos costados, la segunda mas tenue
 *              y mas baja. No son gemelas: es una, y otra cosa que se le parece.
 *
 * Todo se calcula desde el reloj del sistema, igual que la rotacion de niveles:
 * sin estado mutable y sin temporizadores propios.
 */
public final class Presencia {

    private Presencia() {
    }

    /** Cada cuanto se asoma. Largo a proposito: tiene que sorprender. */
    private static final long PERIODO_MS = 71_000L;

    /** Cuanto dura la primera aparicion, de invisible a invisible. */
    private static final long PRIMERA_MS = 7_000L;

    /** Cuanto se queda sin nada en el medio. */
    private static final long HUECO_MS = 4_500L;

    /** La segunda aparicion, mas corta: solo hace falta que se note el cambio. */
    private static final long SEGUNDA_MS = 4_000L;

    /** Ventana completa de la manifestacion. */
    private static final long TOTAL_MS = PRIMERA_MS + HUECO_MS + SEGUNDA_MS;

    /** Esta ahi y se desvanece: el modo de siempre. */
    public static final int MODO_QUIETA = 0;

    /** Existe solo en el pico de la campana, unos pocos cuadros. */
    public static final int MODO_CORTE = 1;

    /** Solo el reflejo: arriba no hay nada parado. */
    public static final int MODO_SUMERGIDA = 2;

    /** Dos siluetas a la vez, una a cada lado del vano. */
    public static final int MODO_DOBLE = 3;

    /** Lo mas opaca que llega a estar. Apenas mas oscura que el vano. */
    private static final float ALFA_MAXIMO = 0.52F;

    /** Cuantos segmentos verticales componen el cuerpo. */
    private static final int SEGMENTOS = 14;

    /**
     * Ancho del cuerpo en la base, en fraccion del semiancho de la abertura.
     *
     * La proporcion es la unica decision de diseno que importa aca. Con 1:14
     * la figura se lee como una grieta en la pared del fondo y no como una
     * presencia; con 1:5 se lee como algo que podria estar parado ahi. La
     * ambiguedad que se busca vive en esa relacion, no en el dibujo.
     */
    private static final float ANCHO = 0.26F;

    /** Alto del cuerpo, en fraccion del semialto de la abertura. */
    private static final float ALTURA = 1.35F;

    /**
     * Cuanto de la manifestacion ya paso, de 0 a 1, o -1 si ahora no hay nada.
     * Sirve para que el audio y la iluminacion sigan a la figura sin repetir
     * la cuenta del reloj en tres lugares distintos.
     */
    public static float avance() {
        if (!ConfigTurno.escenaViva() || ConfigTurno.movimientoReducido()) {
            return -1.0F;
        }
        long fase = Math.floorMod(System.currentTimeMillis(), PERIODO_MS);
        if (fase >= TOTAL_MS) {
            return -1.0F;
        }
        return fase / (float) TOTAL_MS;
    }

    /** Si en este instante hay algo al fondo. */
    public static boolean presente() {
        return visibilidad() > 0.01F;
    }

    /**
     * Cuanto se la ve ahora mismo, de 0 a 1.
     *
     * Dos campanas suaves separadas por un hueco. La curva es seno elevado, no
     * lineal: el arranque y el final son casi planos, asi que no hay un momento
     * en que se pueda decir "ahora aparecio".
     */
    public static float visibilidad() {
        if (!ConfigTurno.escenaViva() || ConfigTurno.movimientoReducido()) {
            return 0.0F;
        }
        long fase = Math.floorMod(System.currentTimeMillis(), PERIODO_MS);

        if (fase < PRIMERA_MS) {
            return campana(fase / (float) PRIMERA_MS);
        }
        long segunda = fase - PRIMERA_MS - HUECO_MS;
        if (segunda >= 0 && segunda < SEGUNDA_MS) {
            return campana(segunda / (float) SEGUNDA_MS) * 0.85F;
        }
        return 0.0F;
    }

    /**
     * Que modo le toca a la manifestacion en curso.
     *
     * Se deriva del numero de ciclo, no de un sorteo por cuadro: durante los
     * 71 segundos que dura el ciclo la respuesta no cambia, asi que la figura
     * no muta a mitad de aparicion. El sesgo esta puesto a mano -mas de la
     * mitad de las veces es la manifestacion comun- porque un modo raro deja
     * de ser raro si sale una de cada cuatro veces.
     */
    public static int modo() {
        long ciclo = Math.floorDiv(System.currentTimeMillis(), PERIODO_MS);
        float r = Trazo.pseudo((int) (ciclo * 31L + 7L));
        if (r < 0.52F) {
            return MODO_QUIETA;
        }
        if (r < 0.74F) {
            return MODO_CORTE;
        }
        if (r < 0.90F) {
            return MODO_SUMERGIDA;
        }
        return MODO_DOBLE;
    }

    /** Si estamos en la segunda aparicion, la que esta corrida de lugar. */
    private static boolean esSegunda() {
        long fase = Math.floorMod(System.currentTimeMillis(), PERIODO_MS);
        return fase >= PRIMERA_MS + HUECO_MS;
    }

    private static float campana(float t) {
        if (t <= 0.0F || t >= 1.0F) {
            return 0.0F;
        }
        float s = (float) Math.sin(Math.PI * t);
        return s * s;
    }

    /**
     * Cuanta luz le saca a la escena mientras esta.
     *
     * No apaga nada: baja un ocho por ciento como maximo. Es un cambio que casi
     * nadie va a poder senalar y que todo el mundo va a sentir.
     */
    public static float sombra() {
        return 1.0F - 0.08F * visibilidad();
    }

    /**
     * La dibuja, si hay algo que dibujar.
     *
     * @param m    el encuadre del recinto. Se le pide el marco entero y no un
     *             par de semiejes porque la figura tiene que pararse contra una
     *             pared REAL: en un recinto visto de esquina, la pared derecha
     *             esta mucho mas cerca de la fuga que la izquierda, y medir con
     *             un semiancho promedio la dejaria flotando en medio del vano
     * @param luz  luz disponible; con el recinto apagado tampoco se la ve
     * @param piso a que fraccion del semialto apoya, segun el recinto: en el
     *             natatorio apoya en el borde del agua y no en el suelo
     */
    public static void dibujar(GuiGraphics grafico, Nivel nivel,
                               Marco m, float luz, float piso) {
        float visible = visibilidad() * luz;
        if (visible <= 0.01F) {
            return;
        }

        int modo = modo();

        // En el modo corte la campana se estrangula: la figura solo existe en
        // el pico y los flancos se comen todo el resto de la aparicion.
        if (modo == MODO_CORTE) {
            visible = Trazo.limitar((visible - 0.72F) / 0.28F, 0.0F, 1.0F);
            if (visible <= 0.01F) {
                return;
            }
        }

        // Nunca en el centro exacto del vano: siempre corrida hacia un costado,
        // como si estuviera parada contra una de las paredes del fondo.
        float lado = esSegunda() ? -0.34F : 0.41F;
        float w = m.w();

        // Los pies apoyan donde este el piso de este recinto, no en el aire.
        float base = m.fy() + m.hb() * piso;
        float altura = m.h() * ALTURA;

        float t = System.currentTimeMillis() / 1000.0F;
        float alfa = ALFA_MAXIMO * visible;
        int tinte = tinte(nivel);

        // En el modo doble hay dos siluetas; en los demas, una.
        float[][] posiciones = modo == MODO_DOBLE
                ? new float[][] {{lado, 1.0F, 1.0F}, {-lado * 0.86F, 0.62F, 0.88F}}
                : new float[][] {{lado, 1.0F, 1.0F}};

        for (float[] pos : posiciones) {
            float x = m.enX(1.0F, pos[0]);
            float peso = pos[1];
            float escala = pos[2];

            // Respiracion: un pixel largo, muy lento. Basta para que no se lea
            // como un elemento pintado sobre la pared.
            float vaiven = (float) Math.sin(t * 0.55F + pos[0]) * (w * 0.012F);

            // En sumergida no hay cuerpo: solo lo que devuelve el agua.
            if (modo != MODO_SUMERGIDA) {
                dibujarCuerpo(grafico, x + vaiven, base, altura * escala, w,
                        alfa * peso, tinte);
            }

            // El reflejo. En las piscinas es la mitad del efecto: se ve antes
            // el borron en el suelo que la figura misma. Cuando es lo unico que
            // hay, se lo sube: tiene que sostener la escena solo.
            if (nivel.reflejo > 0.20F) {
                float fuerza = modo == MODO_SUMERGIDA ? 1.35F : 0.85F;
                dibujarReflejo(grafico, x + vaiven, base, altura * escala, w,
                        alfa * peso * nivel.reflejo * fuerza, tinte);
            }
        }
    }

    /**
     * De que color es algo que esta contra la abertura del fondo.
     *
     * Pintarla siempre del color del vano parecia lo correcto y no lo es. En
     * los niveles cuya abertura ya es casi negra (el 0 administrativo y el 2
     * de servicio) una figura negra sobre fondo negro no existe: el jugador
     * nunca la ve. Y en los claros, un rectangulo negro se lee como una grieta
     * en la pared.
     *
     * La solucion no es fijar un color sino derivarlo del fondo de cada nivel.
     * Contra un fondo oscuro la presencia queda un punto MAS clara que el,
     * como una silueta a contraluz; contra un fondo claro queda mas oscura. El
     * contraste es parecido en los diez niveles y en ninguno se la ve del
     * todo, que es exactamente lo que hace falta.
     */
    private static int tinte(Nivel nivel) {
        if (luminancia(nivel.fondo) < 0.16F) {
            return Paleta.mezclar(nivel.fondo, nivel.niebla, 0.30F);
        }
        return Paleta.VANO;
    }

    /** Brillo percibido de un color, de 0 a 1. */
    private static float luminancia(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (0.299F * r + 0.587F * g + 0.114F * b) / 255.0F;
    }

    /**
     * El cuerpo: una columna que se afina hacia arriba.
     *
     * Se dibuja por segmentos con un ancho que decrece de abajo hacia arriba
     * siguiendo una potencia, y con una ondulacion horizontal minima que hace
     * que el contorno no sea una recta. Un rectangulo perfecto se lee como
     * mobiliario; un contorno apenas irregular se lee como algo.
     */
    private static void dibujarCuerpo(GuiGraphics grafico, float x, float base,
                                      float altura, float w, float alfa, int tinte) {
        float anchoPie = Math.max(3.0F, w * ANCHO);

        for (int i = 0; i < SEGMENTOS; i++) {
            float desde = i / (float) SEGMENTOS;
            float hasta = (i + 1) / (float) SEGMENTOS;

            float y0 = base - altura * hasta;
            float y1 = base - altura * desde;
            if (y1 - y0 < 1.0F) {
                y1 = y0 + 1.0F;
            }

            // Se afina de a poco y se acelera arriba: eso sugiere unos hombros
            // y una cabeza sin llegar nunca a dibujarlos.
            float estrechez = 1.0F - 0.55F * (float) Math.pow(desde, 1.6F);
            float ancho = Math.max(1.4F, anchoPie * estrechez);

            // Ondulacion del contorno. Va contra la altura y no contra el
            // indice del segmento: con el indice el borde queda en zigzag y la
            // figura parece una grieta; con la altura es una curva larga.
            float torcion = (float) Math.sin(desde * 2.2F + 0.6F) * anchoPie * 0.10F;

            // La parte de arriba es mas tenue: se desvanece contra el fondo en
            // vez de terminar en un borde recto.
            float desvanecido = alfa * (1.0F - 0.42F * (float) Math.pow(desde, 2.2F));

            int color = Paleta.conAlfa(tinte, Math.min(0.95F, desvanecido));
            grafico.fill(
                    Math.round(x - ancho * 0.5F + torcion),
                    Math.round(y0),
                    Math.round(x + ancho * 0.5F + torcion),
                    Math.round(y1),
                    color);
        }
    }

    /** El reflejo en el suelo mojado: estirado, deshecho y mucho mas tenue. */
    private static void dibujarReflejo(GuiGraphics grafico, float x, float base,
                                       float altura, float w, float alfa, int tinte) {
        float largo = altura * 0.70F;
        int tramos = 9;
        float t = System.currentTimeMillis() / 1000.0F;

        for (int i = 0; i < tramos; i++) {
            float desde = i / (float) tramos;
            float y0 = base + largo * desde;
            float y1 = base + largo * (i + 1) / tramos;

            // El agua deshace la imagen a medida que se aleja del objeto.
            float ondulacion = (float) Math.sin(t * 1.3F + i * 0.9F) * w * 0.05F * desde;
            float ancho = Math.max(1.4F, w * ANCHO * (1.0F + desde * 0.8F));
            float desvanecido = alfa * (1.0F - desde) * (1.0F - desde);
            if (desvanecido < 0.01F) {
                continue;
            }

            grafico.fill(
                    Math.round(x - ancho * 0.5F + ondulacion),
                    Math.round(y0),
                    Math.round(x + ancho * 0.5F + ondulacion),
                    Math.round(Math.max(y1, y0 + 1)),
                    Paleta.conAlfa(tinte, desvanecido));
        }
    }
}
