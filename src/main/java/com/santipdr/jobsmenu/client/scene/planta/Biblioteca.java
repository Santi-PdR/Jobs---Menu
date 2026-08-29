package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Nivel 5 - La biblioteca.
 *
 * Una sala de estanterias que no termina. Madera oscura hasta el techo, lamparas
 * de mesa con pantalla verde repartidas entre los anaqueles, polvo suspendido en
 * cada haz de luz. Es el recinto mas quieto de todos: aca el terror no es que
 * pase algo, es que no pasa nada y hay demasiados libros para una sola persona.
 *
 * Lo que la distingue: las DOS HILERAS DE ESTANTERIAS que corren hacia la fuga
 * a los dos lados, cerrando un pasillo central de lectura. No son un adorno de
 * pared: son volumenes que se meten hacia el centro y miden la profundidad, como
 * los pilares de la nave, pero llenos hasta arriba.
 */
public final class Biblioteca implements Planta {

    private static final int TRAMOS = 15;

    /** A que fraccion del semiancho corre el frente de cada hilera de estantes. */
    private static final float HILERA = 0.66F;

    @Override
    public int tramos() {
        return TRAMOS;
    }

    @Override
    public float pisoPresencia() {
        return 0.96F;
    }

    @Override
    public void dibujar(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        Trazo.fondo(grafico, m, nivel, luz,
                Paleta.mezclar(nivel.paredBaja, nivel.paredAlta, 0.30F), 1.7F);
        ventanalFondo(grafico, m, nivel, luz);

        Trazo.plano(grafico, m, true, nivel.techo,
                Paleta.mezclar(nivel.techo, nivel.niebla, 0.40F), nivel.niebla, luz, 0.52F);
        Trazo.transversales(grafico, m, true, nivel.techoJunta, nivel.niebla, luz, TRAMOS, 0.30F);

        Trazo.plano(grafico, m, false, nivel.suelo, nivel.sueloLejos, nivel.niebla, luz, 0.55F);
        Trazo.transversales(grafico, m, false, nivel.sueloJunta, nivel.niebla, luz, TRAMOS, 0.42F);
        alfombraCentral(grafico, m, nivel, luz);

        Trazo.paredes(grafico, m, nivel, luz);
        Trazo.juntasVerticales(grafico, m, nivel, luz, TRAMOS, 1.0F, 0.30F);
        Trazo.manchas(grafico, m, nivel, luz, TRAMOS);

        estanterias(grafico, m, nivel, luz);
        paginasDobladas(grafico, m, nivel, luz);
        polvoEstantes(grafico, m, nivel, luz);
        condensacionVentanal(grafico, m, nivel, luz);
        lamparas(grafico, m, nivel, luz, tiempo);
    }

    /** Un ventanal alto al fondo, con la luz gris de afuera que no ayuda. */
    private static void ventanalFondo(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float suelo = m.sueloEn(1.0F);
        float alto = m.h() * 1.35F;
        int x0 = Math.round(m.izq(0.34F));
        int x1 = Math.round(m.der(0.34F));
        int y0 = Math.round(suelo - alto);
        int y1 = Math.round(suelo - m.h() * 0.25F);
        // El vidrio: un gris frio, la unica cosa no calida del recinto.
        grafico.fillGradient(x0, y0, x1, y1,
                Paleta.iluminar(Paleta.mezclar(nivel.niebla, 0xFFB8C0C8, 0.45F), luz * 0.75F),
                Paleta.iluminar(nivel.niebla, luz * 0.45F));
        // Los parteluces: una reja de plomo en cruz.
        int cx = (x0 + x1) / 2;
        int cy = (y0 + y1) / 2;
        int marco = Paleta.iluminar(nivel.junta, luz * 0.6F);
        for (int k = 1; k < 4; k++) {
            int vx = x0 + (x1 - x0) * k / 4;
            grafico.fill(vx, y0, vx + 1, y1, marco);
        }
        grafico.fill(x0, cy, x1, cy + 1, marco);
        grafico.fill(x0 - 2, y0 - 2, x1 + 2, y0, marco);
    }

    /** La alfombra de lectura por el eje, gastada por el paso. */
    private static void alfombraCentral(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int y = Math.round(m.sueloEn(1.0F)); y < m.alto(); y += Trazo.PASO) {
            float dy = m.dy(y + Trazo.PASO * 0.5F);
            if (dy <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
            float medio = m.w() * dy * 0.30F;
            int color = Paleta.mezclar(nivel.sueloJunta, nivel.luz, 0.10F);
            grafico.fill((int) (m.centro(dy) - medio), y, (int) (m.centro(dy) + medio), y + Trazo.PASO,
                    Paleta.conAlfa(Paleta.iluminar(color, Trazo.atenuar(luz, lej)), 0.20F));
        }
    }

    /**
     * Las dos hileras de estanterias, columna por columna.
     *
     * Cada estanteria es un bloque alto con baldas horizontales y el desorden de
     * los lomos: franjas verticales de colores apagados y distintos, deterministas.
     * La cara que da al centro recibe la luz de las lamparas; el canto, sombra.
     */
    private static void estanterias(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int x = 0; x < m.ancho(); x += Trazo.PASO) {
            float dx = m.dx(x + Trazo.PASO * 0.5F);
            if (dx <= 1.05F) {
                continue;
            }
            float centro = x + Trazo.PASO * 0.5F;
            int signo = centro < m.fx() ? -1 : 1;
            // Solo pintamos la columna si cae sobre el frente de la hilera.
            float xHilera = m.lado(signo, dx * HILERA);
            if (Math.abs(centro - xHilera) > m.w() * dx * 0.5F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            if (dx > 6.5F) {
                continue;
            }
            float at = Trazo.atenuar(luz, lej);
            float yTecho = m.techoEn(dx * 0.86F);
            float ySuelo = m.sueloEn(dx);
            if (ySuelo - yTecho < 4) {
                continue;
            }
            // Cuerpo del mueble: madera oscura.
            int madera = Paleta.iluminar(Trazo.velar(nivel.paredBaja, nivel.niebla, lej, 0.45F), at * 0.75F);
            grafico.fill(x, (int) yTecho, x + Trazo.PASO, (int) ySuelo, madera);

            // Los lomos: por balda, una franja de color apagado que cambia por
            // tramo. El color sale del ruido, asi que la estanteria no se repite.
            int baldas = 6;
            for (int b = 0; b < baldas; b++) {
                float f0 = b / (float) baldas;
                float f1 = (b + 1) / (float) baldas;
                int yb0 = (int) (yTecho + (ySuelo - yTecho) * f0) + 1;
                int yb1 = (int) (yTecho + (ySuelo - yTecho) * f1) - 1;
                if (yb1 <= yb0) {
                    continue;
                }
                float semilla = Trazo.pseudo((int) (dx * 53.0F) + b * 17 + (signo + 1) * 91);
                // Lomos: mezcla entre el papel viejo y un tinte segun la semilla.
                int lomo = Paleta.mezclar(Paleta.mezclar(nivel.paredAlta, nivel.junta, 0.35F), nivel.luz, 0.12F + semilla * 0.5F);
                grafico.fill(x, yb0, x + Trazo.PASO, yb1,
                        Paleta.iluminar(Trazo.velar(lomo, nivel.niebla, lej, 0.4F), at * (0.7F + 0.3F * semilla)));
                // La balda de madera que los sostiene.
                grafico.fill(x, yb1, x + Trazo.PASO, yb1 + 1,
                        Paleta.conAlfa(Paleta.iluminar(nivel.junta, at), 0.6F));
            }
        }
    }

    /** Paginas dobladas que sobresalen solo de los estantes cercanos. */
    private static void paginasDobladas(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float[] profundidades = {1.35F, 1.75F};
        for (int i = 0; i < profundidades.length; i++) {
            float dx = profundidades[i];
            int signo = i == 0 ? -1 : 1;
            float x = m.lado(signo, dx * (HILERA - 0.04F));
            float y = m.techoEn(dx * 0.86F) + m.h() * dx * (0.30F + i * 0.18F);
            int ancho = Math.max(4, Math.round(m.w() * dx * 0.055F));
            int alto = Math.max(5, Math.round(m.h() * dx * 0.11F));
            int papel = Paleta.iluminar(Paleta.mezclar(nivel.paredAlta, nivel.techo, 0.35F), luz * 0.72F);
            int x0 = Math.round(x - signo * ancho * 0.30F);
            int x1 = Math.round(x + signo * ancho * 0.70F);
            int y0 = Math.round(y);
            int y1 = y0 + alto;
            grafico.fill(Math.min(x0, x1), y0, Math.max(x0, x1), y1, papel);
            grafico.fill(Math.min(x0, x1), y0, Math.max(x0, x1), y0 + 1,
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz), 0.30F));
            grafico.fill(Math.min(x0, x1) + ancho / 3, y0 + alto / 2,
                    Math.min(x0, x1) + ancho / 3 + 1, y1,
                    Paleta.conAlfa(nivel.junta, 0.45F));
        }
    }

    /** Polvo pegado en algunos recovecos de balda, no una capa uniforme. */
    private static void polvoEstantes(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int i = 0; i < 5; i++) {
            float dx = 1.35F + i * 0.42F;
            int signo = i % 2 == 0 ? -1 : 1;
            int x = Math.round(m.lado(signo, dx * (HILERA + 0.02F)));
            int y = Math.round(m.techoEn(dx * 0.86F) + m.h() * dx * (0.62F + (i % 3) * 0.10F));
            int ancho = Math.max(3, Math.round(m.w() * dx * 0.06F));
            grafico.fill(x - ancho / 2, y, x + ancho, y + Math.max(1, Math.round(m.h() * dx * 0.012F)),
                    Paleta.conAlfa(Paleta.mezclar(nivel.junta, nivel.paredAlta, 0.45F), 0.28F * luz));
        }
    }

    /** Condensacion minima en el ventanal, fuera de los libros. */
    private static void condensacionVentanal(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float suelo = m.sueloEn(1.0F);
        float alto = m.h() * 1.35F;
        int x0 = Math.round(m.izq(0.34F));
        int y0 = Math.round(suelo - alto);
        int y1 = Math.round(suelo - m.h() * 0.25F);
        int ancho = Math.max(2, (Math.round(m.der(0.34F)) - x0) / 4);
        for (int i = 0; i < 4; i++) {
            int x = x0 + ancho * (i + 1);
            int inicio = y0 + (y1 - y0) * (i + 1) / 7;
            int largo = Math.max(3, (y1 - y0) / (8 + i));
            grafico.fill(x, inicio, x + Math.max(1, ancho / 16), inicio + largo,
                    Paleta.conAlfa(Paleta.iluminar(nivel.techo, luz), 0.16F));
            grafico.fill(x - 1, inicio + largo, x + Math.max(2, ancho / 12), inicio + largo + 1,
                    Paleta.conAlfa(nivel.luz, 0.20F));
        }
    }

    /**
     * Las lamparas de mesa de pantalla verde, entre los estantes.
     *
     * Van a media altura, alternando de lado, y son la unica luz calida del
     * recinto: un nucleo brillante y un derrame corto sobre la madera. Titilan
     * apenas -filamento viejo-, cada una con su fase.
     */
    private static void lamparas(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        for (int j = 3; j <= TRAMOS; j += 2) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 6.0F) {
                continue;
            }
            int signo = (j % 4 == 1) ? -1 : 1;
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float x = m.lado(signo, dx * (HILERA - 0.10F));
            if (x < -8 || x > m.ancho() + 8) {
                continue;
            }
            float y = m.sueloEn(dx * 0.60F);
            float titil = Trazo.pulsoLuz(0.9F, 0.1F, tiempo, 5.0F, j * 1.3F);
            float at = Trazo.atenuar(luz, lej) * titil;
            float medio = Math.max(1.5F, m.w() * dx * 0.028F);

            // El derrame corto sobre la madera de alrededor.
            for (int k = 4; k >= 1; k--) {
                float t = k / 4.0F;
                float e = medio * (1.0F + t * 3.2F);
                grafico.fill((int) (x - e), (int) (y - e), (int) (x + e), (int) (y + e),
                        Paleta.conAlfa(nivel.luz, 0.06F * at * (1.0F - t * 0.5F)));
            }
            // La pantalla verde de la lampara.
            int verde = Paleta.mezclar(nivel.luz, 0xFF2E5A3A, 0.55F);
            grafico.fill((int) (x - medio), (int) (y - medio * 1.4F), (int) (x + medio), (int) (y - medio * 0.4F),
                    Paleta.iluminar(verde, Math.min(1.0F, at * 1.1F)));
            // El nucleo caliente por debajo de la pantalla.
            grafico.fill((int) (x - medio * 0.5F), (int) (y - medio * 0.4F), (int) (x + medio * 0.5F), (int) (y + medio * 0.3F),
                    Paleta.conAlfa(Paleta.iluminar(0xFFFFF0C0, Math.min(1.0F, at * 1.3F)), 0.9F));
            // El pie.
            grafico.fill((int) (x - 1), (int) (y + medio * 0.3F), (int) (x + 1), (int) (y + medio * 1.2F),
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, at), 0.8F));
        }
    }

    @Override
    public void primerPlano(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        PrimerPlano.biblioteca(grafico, m, nivel, luz, tiempo);
    }
}
