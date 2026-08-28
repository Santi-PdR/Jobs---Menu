package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Lo que de verdad comparten los cuatro recintos.
 *
 * Un edificio, sea del tipo que sea, tiene un suelo, un cielo, dos costados y
 * una pared al fondo, y los cuatro se proyectan con la misma cuenta. Eso vive
 * aca. Lo que hace que una sala sea una sala y una nave sea una nave -las
 * columnas, las cerchas, las canerias, el agua- vive en su planta y en ningun
 * otro sitio.
 *
 * La regla para decidir si algo va aca: si lo usa una sola planta, no es
 * comun, es de esa planta.
 */
public final class Trazo {

    private Trazo() {
    }

    /** Semilla del ruido. Deletrea JOBS en hexadecimal. */
    public static final int SEMILLA = 0x4A4F4253;

    /** Paso de barrido en pixeles. Dos es el punto donde deja de notarse. */
    public static final int PASO = 2;

    // ----------------------------------------------------------------------
    // Numeros
    // ----------------------------------------------------------------------

    /**
     * Ruido reproducible: la misma entrada da siempre la misma salida.
     *
     * Es lo que permite que cada mancha, cada vano y cada imperfeccion esten
     * siempre en el mismo sitio sin guardar una sola tabla en memoria, y que
     * la vista previa en Python dibuje exactamente la misma escena.
     */
    public static float pseudo(int indice) {
        long h = SEMILLA + indice * 2654435761L;
        h ^= (h >>> 13);
        h *= 1274126177L;
        h ^= (h >>> 16);
        return Math.floorMod(h, 10000L) / 10000.0F;
    }

    public static float limitar(float valor, float minimo, float maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    /**
     * dx del tramo numero j de una serie de n.
     *
     * Con j = n el tramo cae justo sobre la pared del fondo; con j = 1 queda
     * muy por fuera de la pantalla. Que las juntas de pared, las del suelo y
     * las del cielo usen esta misma serie es lo que hace que el ojo cierre la
     * figura: los tres planos coinciden en cada tramo.
     */
    public static float profundidad(int j, int tramos) {
        return (float) tramos / (float) Math.max(1, j);
    }

    /**
     * Cuanto se come la niebla a un color segun la distancia.
     *
     * El cuadrado no es capricho: la perdida de contraste con la distancia no
     * es lineal, y con una mezcla lineal el fondo queda plano y de cartulina.
     */
    public static int velar(int color, int niebla, float lejos, float fuerza) {
        return Paleta.mezclar(color, niebla, lejos * lejos * fuerza);
    }

    /** Atenuacion estandar por distancia: lo lejano recibe menos luz. */
    public static float atenuar(float luz, float lejos) {
        return luz * (0.52F + 0.48F * lejos);
    }

    // ----------------------------------------------------------------------
    // Planos
    // ----------------------------------------------------------------------

    /**
     * La pared del fondo: el rectangulo donde muere la perspectiva.
     *
     * No es un agujero, es una pared que esta lejos, y eso hay que dibujarlo.
     * Cuando el fondo se deja en negro el recinto se lee como un tunel abierto
     * a la nada, que es exactamente el error de los fondos genericos. Cada
     * planta le pasa el color de SU testero y despues le apoya encima lo que
     * tenga construido: las puertas de la sala, el porton de la nave, el
     * tablero del pasillo.
     *
     * @param testero color de la pared del fondo; null usa el vano del nivel
     * @param fuerza  cuanta luz le llega. 1.0 si la ultima luminaria apenas la
     *                alcanza; 2.6 en un recinto claro donde el fondo es pared
     *                blanca y esta iluminado como el resto
     */
    public static void fondo(GuiGraphics grafico, Marco m, Nivel nivel, float luz,
                             Integer testero, float fuerza) {
        int x0 = Math.round(m.izq(1.0F));
        int x1 = Math.round(m.der(1.0F));
        int y0 = Math.round(m.techoEn(1.0F));
        int y1 = Math.round(m.sueloEn(1.0F));
        int color = testero == null ? nivel.fondo : testero;

        grafico.fillGradient(x0, y0, x1, y1,
                Paleta.iluminar(Paleta.mezclar(color, nivel.niebla, 0.22F),
                        limitar(luz * 0.52F * fuerza, 0.0F, 1.0F)),
                Paleta.iluminar(color, limitar(luz * 0.30F * fuerza, 0.0F, 1.0F)));
    }

    /**
     * El suelo o el cielo, fila por fila.
     *
     * Se recorre desde la pared del fondo hacia la camara. Cada fila es un
     * corte a profundidad constante, asi que basta un color por fila.
     *
     * @param arriba true para el cielorraso, false para el suelo
     * @param velo   cuanto se lo lleva la niebla en la distancia
     */
    public static void plano(GuiGraphics grafico, Marco m, boolean arriba,
                             int cerca, int lejos, int niebla, float luz, float velo) {
        int desde = arriba ? 0 : Math.round(m.sueloEn(1.0F));
        int hasta = arriba ? Math.round(m.techoEn(1.0F)) : m.alto();

        for (int y = desde; y < hasta; y += PASO) {
            float dy = m.dy(y + PASO * 0.5F);
            if (dy <= 1.0F) {
                continue;
            }
            float lej = limitar(1.0F / dy, 0.0F, 1.0F);
            int color = velar(Paleta.mezclar(cerca, lejos, lej), niebla, lej, velo);
            grafico.fill(0, y, m.ancho(), y + PASO,
                    Paleta.iluminar(color, atenuar(luz, lej)));
        }
    }

    /**
     * Las transversales del suelo o del cielo: las lineas que cruzan y se
     * aprietan hacia la fuga.
     *
     * Se recortan al ancho del recinto a esa profundidad, no a la pantalla:
     * una junta que siguiera de lado a lado delataria al instante que esto es
     * un dibujo plano.
     */
    public static void transversales(GuiGraphics grafico, Marco m, boolean arriba,
                                     int color, int niebla, float luz, int tramos, float alfa) {
        for (int j = 1; j <= tramos; j++) {
            float dy = profundidad(j, tramos);
            float lej = limitar(1.0F / dy, 0.0F, 1.0F);
            float y = arriba ? m.techoEn(dy) : m.sueloEn(dy);
            if (y < -4 || y > m.alto() + 4) {
                continue;
            }
            int grosor = Math.max(1, Math.min((int) (m.h() * 0.09F), (int) (m.h() * dy * 0.010F)));
            int x0 = Math.round(m.izq(dy));
            int x1 = Math.round(m.der(dy));
            grafico.fill(Math.max(0, x0), (int) y, Math.min(m.ancho(), x1), (int) y + grosor,
                    Paleta.conAlfa(Paleta.iluminar(velar(color, niebla, lej, 0.55F),
                            atenuar(luz, lej)), alfa * lej + 0.10F));
        }
    }

    /**
     * Los dos costados del recinto, columna por columna.
     *
     * Cada columna de pantalla que cae fuera de la pared del fondo pertenece a
     * una de las dos paredes laterales, y su profundidad sale de la distancia
     * horizontal a la fuga. El degradado vertical va de la pared alta -donde
     * pega la luz- a la baja, donde ya no llega.
     */
    public static void paredes(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int x = 0; x < m.ancho(); x += PASO) {
            float dx = m.dx(x + PASO * 0.5F);
            if (dx <= 1.0F) {
                continue;
            }
            float lej = limitar(1.0F / dx, 0.0F, 1.0F);
            float y0 = m.techoEn(dx);
            float y1 = m.sueloEn(dx);
            if (y1 < 0 || y0 > m.alto()) {
                continue;
            }
            float at = atenuar(luz, lej);
            grafico.fillGradient(x, (int) y0, x + PASO, (int) y1,
                    Paleta.iluminar(velar(nivel.paredAlta, nivel.niebla, lej, 0.62F), at),
                    Paleta.iluminar(velar(nivel.paredBaja, nivel.niebla, lej, 0.52F), at));
        }
    }

    /**
     * Las verticales que separan un pano de pared del siguiente.
     *
     * @param lateral a que fraccion del semiancho estan; 1.0 es la pared misma
     */
    public static void juntasVerticales(GuiGraphics grafico, Marco m, Nivel nivel,
                                        float luz, int tramos, float lateral, float alfa) {
        for (int j = 1; j <= tramos; j++) {
            float dx = profundidad(j, tramos);
            float lej = limitar(1.0F / dx, 0.0F, 1.0F);
            float at = atenuar(luz, lej);
            int grosor = Math.max(1, Math.min((int) (m.w() * 0.10F), (int) (m.w() * dx * 0.009F)));
            int color = Paleta.conAlfa(
                    Paleta.iluminar(velar(nivel.junta, nivel.niebla, lej, 0.55F), at), alfa * lej + 0.12F);
            int y0 = (int) m.techoEn(dx);
            int y1 = (int) m.sueloEn(dx);

            for (int signo = -1; signo <= 1; signo += 2) {
                float x = m.lado(signo, dx * lateral);
                if (x >= -grosor && x <= m.ancho() + grosor) {
                    grafico.fill((int) x, y0, (int) x + grosor, y1, color);
                }
            }
        }
    }

    /**
     * Filtraciones que cuelgan de lo alto de las paredes y se abren hacia abajo.
     *
     * Es la unica suciedad que comparten los cuatro recintos, porque los cuatro
     * estan bajo el mismo techo que gotea.
     */
    public static void manchas(GuiGraphics grafico, Marco m, Nivel nivel, float luz, int tramos) {
        int total = (int) (16 * nivel.humedad);
        for (int i = 0; i < total; i++) {
            float dx = 1.15F + pseudo(i * 3) * (tramos * 0.42F);
            int signo = pseudo(i * 3 + 1) < 0.5F ? -1 : 1;
            float x = m.lado(signo, dx);
            if (x < -40 || x > m.ancho() + 40) {
                continue;
            }
            float lej = limitar(1.0F / dx, 0.0F, 1.0F);
            float y0 = m.techoEn(dx);
            float altura = m.h() * dx * (0.25F + pseudo(i * 3 + 2) * 0.55F);
            float ancho = Math.max(2.0F, m.w() * dx * (0.05F + pseudo(i * 5) * 0.10F));

            final int pasos = 5;
            for (int k = 0; k < pasos; k++) {
                float t = k / (float) pasos;
                float a = 0.30F * (1.0F - t) * (0.35F + 0.65F * lej) * nivel.humedad;
                float am = ancho * (0.6F + 0.9F * t);
                grafico.fill((int) (x - am), (int) (y0 + altura * t),
                        (int) (x + am), (int) (y0 + altura * (t + 1.0F / pasos)),
                        Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz), a));
            }
        }
    }

    /**
     * El interior de un vano abierto: lo que se ve del otro lado.
     *
     * QUE ESTABA MAL
     *
     * Los vanos eran un degradado plano hasta negro. Leen como un agujero
     * recortado en la pared, no como un sitio que continua, y por dos razones
     * concretas:
     *
     *   - un espacio real nunca es uniforme. Hay algo de suelo iluminado justo
     *     en el umbral -la luz de ESTA sala entra un metro para adentro- y la
     *     oscuridad empieza despues, no en el marco;
     *   - un pasillo que sigue tiene una PARED LATERAL, y esa pared recibe luz
     *     rasante desde aca, asi que se ve un poco mas clara de un lado que
     *     del otro segun de que lado del eje este el vano.
     *
     * Es la diferencia entre "hay un rectangulo negro" y "hay un pasillo del
     * que solo veo el primer metro". Cuesta cuatro rectangulos mas.
     *
     * @param lado -1 si el vano esta a la izquierda del eje, +1 si a la derecha
     */
    public static void interiorVano(GuiGraphics grafico, Nivel nivel, int x0, int y0,
                                    int x1, int y1, int lado, float luz) {
        int ancho = x1 - x0;
        int alto = y1 - y0;
        if (ancho <= 2 || alto <= 2) {
            return;
        }

        // El fondo del hueco: negro casi entero, pero no del todo. El negro
        // absoluto es lo que delata el recorte.
        grafico.fill(x0, y0, x1, y1,
                Paleta.conAlfa(Paleta.mezclar(Paleta.VANO, nivel.niebla, 0.10F), 0.97F));

        // LA PARED LATERAL del otro lado, la que se ve en escorzo. Solo se ve
        // una de las dos, y es la contraria al lado donde esta el vano: si el
        // vano esta a la izquierda del eje, la cara que mira a la camara es la
        // de la derecha del hueco.
        int fuga = Math.max(1, Math.round(ancho * 0.34F));
        int desde = lado < 0 ? x1 - fuga : x0;
        int hasta = lado < 0 ? x1 : x0 + fuga;
        for (int k = 0; k < fuga; k++) {
            // Cuanto mas adentro, mas oscura: es la misma pared alejandose.
            float t = lado < 0 ? 1.0F - k / (float) fuga : k / (float) fuga;
            int px = lado < 0 ? desde + k : hasta - 1 - k;
            grafico.fill(px, y0 + Math.round(alto * 0.06F * (1.0F - t)), px + 1, y1,
                    Paleta.conAlfa(Paleta.iluminar(nivel.paredBaja, luz * 0.30F * t * t), 0.85F));
        }

        // EL UMBRAL. La luz de esta sala entra un poco: el primer tramo de
        // suelo del otro lado se ve, y es lo que convierte el agujero en un
        // sitio con piso.
        int umbral = Math.max(1, Math.round(alto * 0.10F));
        grafico.fillGradient(x0 + 1, y1 - umbral, x1 - 1, y1,
                Paleta.conAlfa(Paleta.VANO, 0.0F),
                Paleta.conAlfa(Paleta.iluminar(nivel.sueloLejos, luz * 0.55F), 0.62F));

        // El canto vivo del marco por dentro, del lado que da al pasillo. Un
        // filo claro de un pixel es lo que le da espesor a la pared.
        int filo = lado < 0 ? x0 : x1 - 1;
        grafico.fill(filo, y0, filo + 1, y1,
                Paleta.conAlfa(Paleta.iluminar(nivel.paredAlta, luz * 0.60F), 0.45F));
    }

    /**
     * Una luminaria empotrada en el cielorraso, centrada en el recinto.
     *
     * QUE ESTABA MAL
     *
     * Era una barra clara con un halo alrededor: una pegatina. Le faltaba lo
     * que hace que una luminaria se lea como un objeto montado en un techo y
     * no como una mancha pintada encima:
     *
     *   - un ARTEFACTO. Una luz real esta dentro de algo, y ese algo tiene
     *     chapa, y la chapa recibe menos luz que el tubo porque esta al
     *     costado de la fuente, no delante;
     *   - una SOMBRA. Un objeto que sobresale del techo tapa la luz de los
     *     que estan detras, y por lo tanto proyecta;
     *   - un DIFUSOR con espesor. El tubo no toca el aire: hay un plastico
     *     delante, y el plastico tiene borde;
     *   - ASIMETRIA. Todas eran identicas. Un pasillo con doce luminarias
     *     exactamente iguales es un patron, y un patron se lee como textura
     *     generada, no como sitio.
     *
     * Ahora se dibuja de atras hacia adelante -derrame, cajon, sombra del
     * cajon, difusor, tubo, reflejo en el techo- y cada una recibe un ligero
     * desvio propio a partir de su posicion, asi que ninguna es igual a la de
     * al lado.
     *
     * @param altura  a que fraccion del semialto cuelga, medida desde la fuga
     * @param largo   largo del tubo en fracciones del semiancho a esa distancia
     * @param derrame cuanto ilumina alrededor
     */
    public static void luminaria(GuiGraphics grafico, Marco m, Nivel nivel,
                                 float dx, float altura, float largo, float derrame, float luz) {
        float lej = limitar(1.0F / dx, 0.0F, 1.0F);
        float y = m.techoEn(dx * altura);
        float cx = m.centro(dx);
        float medio = Math.max(1.0F, Math.abs(m.anchoEn(dx)) * 0.5F * largo);
        float grueso = Math.max(1.0F, m.h() * dx * 0.026F);

        // Cada luminaria envejece por su cuenta. Un tubo fluorescente viejo no
        // da la misma luz que el de al lado: unos amarillean, otros estan por
        // irse. Sin esto la fila es un patron repetido y se nota enseguida.
        float desvio = pseudo((int) (dx * 977.0F) + 31);
        float cansancio = 0.80F + 0.32F * desvio;
        float fuerza = luz * (0.45F + 0.55F * lej) * cansancio;

        if (derrame > 0.0F) {
            int pasos = 4;
            for (int k = pasos; k >= 1; k--) {
                float t = k / (float) pasos;
                // El derrame es mas ancho que alto: la luz cae sobre el techo
                // y se abre a los costados, no baja en circulo.
                float ex = medio * (1.0F + t * 2.1F);
                float ey = grueso * (1.0F + t * 5.5F);
                grafico.fill((int) (cx - ex), (int) (y - ey), (int) (cx + ex), (int) (y + ey),
                        Paleta.conAlfa(nivel.luz, 0.050F * derrame * fuerza * (1.0F - t * 0.55F)));
            }
        }

        // EL ARTEFACTO. La chapa del cajon, un poco mas larga y mas alta que
        // el tubo, en el color del techo pero apagada: esta al costado de la
        // fuente, asi que recibe rebote, no luz directa.
        float cajonX = medio * 1.14F;
        float cajonY = grueso * 1.9F;
        if (cajonY >= 1.0F) {
            grafico.fill((int) (cx - cajonX), (int) (y - cajonY),
                    (int) (cx + cajonX), (int) (y + cajonY),
                    Paleta.conAlfa(Paleta.iluminar(nivel.techoJunta, 0.62F + 0.34F * fuerza), 0.90F));
            // El canto inferior del cajon, donde la chapa dobla: la unica
            // parte del artefacto que si ve el tubo de refilon.
            grafico.fill((int) (cx - cajonX), (int) (y + cajonY - Math.max(1.0F, grueso * 0.35F)),
                    (int) (cx + cajonX), (int) (y + cajonY),
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, fuerza * 0.75F), 0.40F));
        }

        // EL DIFUSOR. Plastico lechoso delante del tubo: mas ancho que el tubo
        // y bastante mas apagado. Es lo que evita que la luz sea una linea.
        grafico.fill((int) (cx - medio * 1.04F), (int) (y - grueso * 1.35F),
                (int) (cx + medio * 1.04F), (int) (y + grueso * 1.35F),
                Paleta.conAlfa(Paleta.iluminar(nivel.luz, Math.min(1.0F, fuerza * 0.85F)), 0.55F));

        // EL TUBO. Lo unico que de verdad brilla, y ocupa la menor superficie
        // de las tres capas. Que la parte mas clara sea la mas chica es lo que
        // da la sensacion de que hay una fuente y no una superficie pintada.
        grafico.fill((int) (cx - medio), (int) (y - grueso),
                (int) (cx + medio), (int) (y + grueso),
                Paleta.conAlfa(Paleta.iluminar(nivel.luz, Math.min(1.0F, fuerza * 1.25F)), 0.92F));

        // El extremo del tubo esta siempre mas oscuro: son los electrodos. Un
        // fluorescente nunca ilumina parejo de punta a punta.
        float tapa = Math.max(1.0F, medio * 0.13F);
        int oscuro = Paleta.conAlfa(nivel.techoJunta, 0.45F);
        grafico.fill((int) (cx - medio), (int) (y - grueso), (int) (cx - medio + tapa),
                (int) (y + grueso), oscuro);
        grafico.fill((int) (cx + medio - tapa), (int) (y - grueso), (int) (cx + medio),
                (int) (y + grueso), oscuro);
    }
}

