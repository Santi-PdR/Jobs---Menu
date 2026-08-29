package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Eventos visuales breves que rompen la repeticion de los fondos.
 *
 * Cada familia de recinto responde distinto: los backrooms tienen barridos de
 * fluorescente, los espacios humedos respiran sobre el agua, piedra y ruina
 * levantan polvo, y los recintos profundos pueden dejar pasar una silueta.
 * No sustituyen la animacion propia de cada Planta.
 */
public final class EventosAmbientales {

    private EventosAmbientales() {
    }

    private static final long CICLO_MS = 61_000L;
    private static final long VENTANA_MS = 6_200L;

    public static void dibujar(GuiGraphics grafico, int ancho, int alto,
                               Nivel nivel, float luz) {
        long ahora = System.currentTimeMillis();
        long ciclo = Math.floorDiv(ahora, CICLO_MS);
        long dentro = Math.floorMod(ahora, CICLO_MS);
        int semilla = nivel.clave.hashCode() * 31 + (int) (ciclo ^ (ciclo >>> 32));

        // La mitad de los ciclos quedan vacios. Los eventos se ven durante una
        // sesion normal, pero nunca parecen un salvapantallas en bucle.
        if ((semilla & 1) != 0 || dentro >= VENTANA_MS) {
            return;
        }

        float progreso = dentro / (float) VENTANA_MS;
        float pulso = (float) Math.sin(progreso * Math.PI);
        if (pulso <= 0.001F) return;

        int numero = nivel.numero();
        if (numero <= 2) {
            barridoFluorescente(grafico, ancho, alto, nivel, luz, progreso, pulso, semilla);
        } else if (numero == 3 || numero == 6 || numero == 8) {
            humedadViva(grafico, ancho, alto, nivel, luz, progreso, pulso, semilla);
        } else if (numero == 5) {
            polvoEnHaz(grafico, ancho, alto, nivel, luz, progreso, pulso, semilla);
        } else if (numero == 7 || numero == 9) {
            siluetaLejana(grafico, ancho, alto, pulso, semilla, progreso);
        } else {
            polvoEnHaz(grafico, ancho, alto, nivel, luz, progreso, pulso, semilla);
        }
    }

    /** Luz que corre por una junta o luminaria, como un tubo que intenta estabilizarse. */
    private static void barridoFluorescente(GuiGraphics g, int ancho, int alto,
                                             Nivel nivel, float luz, float progreso,
                                             float pulso, int semilla) {
        int y = (int) (alto * (0.18F + 0.18F * pseudo(semilla + 7)));
        int largo = Math.max(26, ancho / 8);
        int recorrido = ancho + largo;
        int x = (int) (progreso * recorrido) - largo;
        float alfa = 0.11F * luz * pulso;
        g.fill(x, y, Math.min(ancho, x + largo), y + 1, Paleta.conAlfa(nivel.luz, alfa));
        if (x > 0) {
            g.fill(Math.max(0, x - 18), y + 1, x, y + 2,
                    Paleta.conAlfa(nivel.luz, alfa * 0.35F));
        }
    }

    /** Ondas y velo de condensacion para piscina, invernadero y cisterna. */
    private static void humedadViva(GuiGraphics g, int ancho, int alto,
                                     Nivel nivel, float luz, float progreso,
                                     float pulso, int semilla) {
        float humedad = Math.max(0.35F, nivel.humedad);
        int baseY = (int) (alto * (0.58F + 0.18F * pseudo(semilla + 13)));
        int centro = (int) (ancho * (0.20F + 0.60F * pseudo(semilla + 17)));
        int radio = Math.max(18, (int) (ancho * (0.03F + progreso * 0.08F)));
        float alfa = 0.08F * humedad * luz * pulso;

        // El rizo horizontal (las dos lineas de onda) no se dibuja en el
        // natatorio: ese nivel ya tiene la red de luz de la planta y el arte,
        // y tres aguas superpuestas se leian como ruido. El velo se queda.
        boolean rizo = nivel.numero() != 3;
        if (rizo) {
            g.fill(Math.max(0, centro - radio), baseY,
                    Math.min(ancho, centro + radio), baseY + 1,
                    Paleta.conAlfa(nivel.luz, alfa));
            g.fill(Math.max(0, centro - radio / 2), baseY + 3,
                    Math.min(ancho, centro + radio / 2), baseY + 4,
                    Paleta.conAlfa(nivel.luz, alfa * 0.50F));
        }

        int veloY = Math.max(0, baseY - 18);
        g.fill(0, veloY, ancho, veloY + 2,
                Paleta.conAlfa(nivel.luz, 0.018F * humedad * pulso));
    }

    /** Particulas iluminadas dentro de un haz, mas visibles que las motas globales. */
    private static void polvoEnHaz(GuiGraphics g, int ancho, int alto,
                                   Nivel nivel, float luz, float progreso,
                                   float pulso, int semilla) {
        int cx = (int) (ancho * (0.38F + 0.24F * pseudo(semilla + 23)));
        int y0 = (int) (alto * 0.18F);
        int y1 = (int) (alto * 0.72F);
        for (int i = 0; i < 14; i++) {
            float p = pseudo(semilla + i * 19);
            int x = cx + (int) ((p - 0.5F) * ancho * 0.16F);
            int y = y0 + (int) (((pseudo(semilla + i * 31 + 3) + progreso * 0.18F) % 1.0F) * (y1 - y0));
            float a = (0.08F + p * 0.10F) * luz * pulso;
            g.fill(x, y, x + (p > 0.82F ? 2 : 1), y + 1, Paleta.conAlfa(nivel.luz, a));
        }
    }

    /**
     * Figura estrecha que cruza lejos, sin rasgos ni jumpscare.
     *
     * El desplazamiento sale del MISMO progreso que abre la ventana y sostiene
     * el pulso: antes la silueta media su propia fraccion de la ventana con su
     * propio reloj, y en el pico del pulso podia saltar de posicion porque las
     * dos cuentas nunca iban exactamente iguales.
     */
    private static void siluetaLejana(GuiGraphics g, int ancho, int alto,
                                      float pulso, int semilla, float progreso) {
        boolean derecha = (semilla & 4) == 0;
        int recorrido = Math.max(24, ancho / 9);
        int baseX = (int) (ancho * (0.43F + 0.12F * pseudo(semilla + 41)));
        int desplazamiento = (int) ((progreso - 0.5F) * recorrido);
        int x = derecha ? baseX + desplazamiento : baseX - desplazamiento;
        int y = (int) (alto * (0.39F + 0.10F * pseudo(semilla + 47)));
        int h = Math.max(12, alto / 12);
        int w = Math.max(3, ancho / 160);
        float a = 0.22F * pulso;
        g.fill(x, y, x + w, y + h, Paleta.conAlfa(Paleta.VANO, a));
        g.fill(x - 1, y + 2, x + w + 1, y + h / 3, Paleta.conAlfa(Paleta.VANO, a * 0.75F));
    }

    private static float pseudo(int n) {
        int x = n;
        x ^= x << 13;
        x ^= x >>> 17;
        x ^= x << 5;
        return (x & 0x7FFFFFFF) / (float) Integer.MAX_VALUE;
    }
}
