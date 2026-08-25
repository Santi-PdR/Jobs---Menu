package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Nivel 2 - Servicio.
 *
 * El unico de los cuatro que sigue siendo un pasillo, y lo es a proposito: es
 * el contraste que necesitan los otros tres. Estrecho, mas alto que ancho,
 * caliente, con el haz de canerias corriendo por el techo y el codo de una
 * bifurcacion a mitad de camino.
 *
 * Lo que lo distingue: el pasillo DOBLA. La pared del fondo no es el final,
 * es la que tapa la vuelta, y por el costado se abre el tramo que sigue hacia
 * un sitio que no se ve. Un pasillo recto termina; uno que dobla continua.
 */
public final class Servicio implements Planta {

    /** Tramos en profundidad. Muchos y cortos: el paso se cuenta. */
    private static final int TRAMOS = 22;

    /** En que tramo se abre la bifurcacion lateral. */
    private static final int CODO = 7;

    @Override
    public int tramos() {
        return TRAMOS;
    }

    @Override
    public void dibujar(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        Trazo.fondo(grafico, m, nivel, luz,
                Paleta.mezclar(nivel.paredBaja, nivel.niebla, 0.20F), 1.5F);
        tableroFondo(grafico, m, nivel, luz);

        Trazo.plano(grafico, m, true, nivel.techo,
                Paleta.mezclar(nivel.techo, nivel.niebla, 0.42F), nivel.niebla, luz, 0.58F);
        Trazo.plano(grafico, m, false, nivel.suelo, nivel.sueloLejos, nivel.niebla, luz, 0.62F);
        Trazo.transversales(grafico, m, false, nivel.sueloJunta, nivel.niebla, luz, TRAMOS, 0.34F);

        Trazo.paredes(grafico, m, nivel, luz);
        Trazo.juntasVerticales(grafico, m, nivel, luz, TRAMOS, 1.0F, 0.45F);
        Trazo.manchas(grafico, m, nivel, luz, TRAMOS);

        bifurcacion(grafico, m, nivel, luz);
        haz(grafico, m, nivel, luz, tiempo);
        apliques(grafico, m, nivel, luz);
        rejillas(grafico, m, nivel, luz);
    }

    /**
     * El tablero electrico del fondo: la chapa cerrada con su piloto.
     *
     * El piloto es el unico punto de color saturado de toda la escena y esta
     * en el punto de fuga. El ojo va ahi solo, y lo que encuentra es una luz
     * que no ilumina nada.
     */
    private static void tableroFondo(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        int x0 = Math.round(m.izq(0.46F));
        int x1 = Math.round(m.der(0.46F));
        float suelo = m.sueloEn(1.0F);
        int y0 = Math.round(suelo - m.h() * 1.22F);
        int y1 = Math.round(suelo - m.h() * 0.42F);

        grafico.fill(x0, y0, x1, y1,
                Paleta.iluminar(Paleta.mezclar(nivel.junta, nivel.paredBaja, 0.40F), luz * 0.52F));
        grafico.fill(x0, y0, x1, y0 + 1,
                Paleta.iluminar(nivel.paredAlta, luz * 0.40F));
        // La bisagra vertical del medio: son dos puertas, no una chapa.
        grafico.fill((x0 + x1) / 2, y0, (x0 + x1) / 2 + 1, y1, Paleta.conAlfa(Paleta.VANO, 0.45F));
        // El piloto.
        int px = x1 - Math.max(3, (x1 - x0) / 8);
        int py = y0 + Math.max(3, (y1 - y0) / 6);
        grafico.fill(px, py, px + 2, py + 2, Paleta.conAlfa(Paleta.ALERTA_BRILLO, 0.85F * luz + 0.15F));
    }

    /**
     * El tramo que se abre de costado, a mitad de pasillo.
     *
     * Se dibuja como un hueco oscuro entre dos juntas consecutivas, con el
     * borde iluminado del lado que da al pasillo. Es lo que convierte esto de
     * un tubo cerrado en una red.
     */
    private static void bifurcacion(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float dxA = Trazo.profundidad(CODO, TRAMOS);
        float dxB = Trazo.profundidad(CODO + 2, TRAMOS);
        float lej = Trazo.limitar(1.0F / dxA, 0.0F, 1.0F);

        float xa = m.izq(dxA);
        float xb = m.izq(dxB);
        int x0 = (int) Math.min(xa, xb);
        int x1 = (int) Math.max(xa, xb);
        if (x1 <= 0 || x0 >= m.ancho()) {
            return;
        }

        for (int col = Math.max(0, x0); col < Math.min(m.ancho(), x1); col++) {
            float dxc = m.dx(col + 0.5F);
            float ys = m.sueloEn(dxc);
            float altura = 2.0F * m.h() * dxc * 0.82F;
            // El hueco se aclara apenas hacia el fondo: por ahi entra algo de
            // luz del tramo siguiente, que no se ve.
            float t = (col - x0) / (float) Math.max(1, x1 - x0);
            grafico.fillGradient(col, (int) (ys - altura), col + 1, (int) ys,
                    Paleta.conAlfa(Paleta.iluminar(nivel.fondo, luz * 0.14F), 0.95F),
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz * 0.10F * (1.0F - t)), 0.92F));
        }

        // El canto vivo del vano, del lado del pasillo.
        if (x1 >= 0 && x1 < m.ancho()) {
            float dxc = m.dx(x1 + 0.5F);
            grafico.fill(x1, (int) (m.sueloEn(dxc) - 2.0F * m.h() * dxc * 0.82F), x1 + 2, (int) m.sueloEn(dxc),
                    Paleta.conAlfa(Paleta.iluminar(nivel.paredAlta, Trazo.atenuar(luz, lej)), 0.75F));
        }
    }

    /**
     * El haz de canerias bajo el techo.
     *
     * Cinco corridas de distinto diametro, a distinta altura y de distinto
     * material. La mas gruesa lleva aislante -por eso es mas clara y mate-, y
     * una de las finas tiene una junta que gotea cada tanto.
     */
    private static void haz(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        final float[] alturas = {0.86F, 0.78F, 0.70F, 0.62F, 0.56F};
        final float[] radios = {0.070F, 0.038F, 0.054F, 0.028F, 0.022F};
        final float[] tonos = {0.45F, 0.10F, 0.28F, 0.05F, 0.18F};

        for (int c = 0; c < alturas.length; c++) {
            for (int x = 0; x < m.ancho(); x += Trazo.PASO) {
                float dx = m.dx(x + Trazo.PASO * 0.5F);
                if (dx <= 1.0F) {
                    continue;
                }
                float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
                float at = Trazo.atenuar(luz, lej);

                // Cada caneria cuelga a su propia distancia del techo. Se
                // evalua el techo a una profundidad menor que la real: como el
                // techo baja hacia la fuga, eso deja el cano por debajo del
                // cielorraso sin tener que calcular la altura a mano.
                float eje = m.techoEn(dx * alturas[c]);
                float radio = Math.max(1.0F, m.h() * dx * radios[c]);

                int base = Paleta.mezclar(nivel.junta, nivel.paredAlta, 0.20F + tonos[c]);
                grafico.fillGradient(x, (int) (eje - radio),
                        x + Trazo.PASO, (int) (eje + radio),
                        Paleta.iluminar(Paleta.mezclar(base, nivel.luz, 0.26F), at),
                        Paleta.iluminar(Paleta.mezclar(base, Paleta.VANO, 0.40F), at));
            }
        }

        // Las abrazaderas que sujetan el haz al techo, una por tramo.
        for (int j = 2; j <= TRAMOS; j += 3) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 8.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej) * 0.85F;
            for (int signo = -1; signo <= 1; signo += 2) {
                float x = m.lado(signo, dx * 0.80F);
                if (x < 0 || x > m.ancho()) {
                    continue;
                }
                float y0 = m.techoEn(dx * 0.90F);
                float y1 = m.techoEn(dx * 0.54F);
                int grosor = Math.max(1, (int) (m.w() * dx * 0.012F));
                grafico.fill((int) x, (int) y0, (int) x + grosor, (int) y1,
                        Paleta.conAlfa(Paleta.iluminar(nivel.junta, at), 0.70F));
            }
        }
    }

    /**
     * Los apliques de pared: no cuelgan del techo porque el techo esta ocupado.
     *
     * Van pegados a la pared derecha, uno cada tres tramos, con la reja de
     * proteccion por delante. La luz que dan es lateral y rasante, que es la
     * que peor le queda a un pasillo.
     */
    private static void apliques(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 2; j <= TRAMOS; j += 3) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 7.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            float x = m.der(dx * 0.98F);
            if (x < 0 || x > m.ancho()) {
                continue;
            }
            float y = m.techoEn(dx * 0.48F);
            float alto = Math.max(1.5F, m.h() * dx * 0.070F);
            float ancho = Math.max(1.5F, m.w() * dx * 0.030F);

            grafico.fill((int) (x - ancho), (int) (y - alto), (int) x, (int) (y + alto),
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, Math.min(1.0F, at * 1.6F)), 0.95F));
            // La reja: dos barrotes horizontales delante del vidrio.
            grafico.fill((int) (x - ancho), (int) (y - alto * 0.35F), (int) x, (int) (y - alto * 0.35F) + 1,
                    Paleta.conAlfa(Paleta.VANO, 0.45F));
            grafico.fill((int) (x - ancho), (int) (y + alto * 0.35F), (int) x, (int) (y + alto * 0.35F) + 1,
                    Paleta.conAlfa(Paleta.VANO, 0.45F));
            // El derrame sobre la pared, corto y sucio.
            for (int k = 3; k >= 1; k--) {
                float t = k / 3.0F;
                grafico.fill((int) (x - ancho * (1.0F + t * 3.5F)), (int) (y - alto * (1.0F + t * 2.2F)),
                        (int) x, (int) (y + alto * (1.0F + t * 2.2F)),
                        Paleta.conAlfa(nivel.luz, 0.075F * at * (1.0F - t * 0.45F)));
            }
        }
    }

    /** Las rejillas de extraccion al pie de la pared izquierda. */
    private static void rejillas(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 5; j <= TRAMOS; j += 6) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 6.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej) * 0.70F;
            float x = m.izq(dx);
            if (x < -20 || x > m.ancho()) {
                continue;
            }
            float ySuelo = m.sueloEn(dx);
            float alto = m.h() * dx * 0.16F;
            float ancho = Math.max(2.0F, m.w() * dx * 0.10F);

            grafico.fill((int) x, (int) (ySuelo - alto), (int) (x + ancho), (int) ySuelo,
                    Paleta.iluminar(Paleta.mezclar(nivel.junta, Paleta.VANO, 0.35F), at));
            int lamas = Math.max(2, (int) (alto / 3.0F));
            for (int k = 1; k < lamas; k++) {
                float y = ySuelo - alto + alto * k / lamas;
                grafico.fill((int) x, (int) y, (int) (x + ancho), (int) y + 1,
                        Paleta.conAlfa(Paleta.iluminar(nivel.paredAlta, at), 0.35F));
            }
        }
    }

    /** El primer plano de este recinto: los canos que pasan por encima de la camara. */
    @Override
    public void primerPlano(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        PrimerPlano.servicio(grafico, m, nivel, luz, tiempo);
    }
}
