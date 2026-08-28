package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Microeventos visuales globales que aparecen muy de vez en cuando.
 *
 * No sustituyen los eventos propios de cada Planta: son una capa breve y
 * discreta que rompe la repeticion sin convertir el fondo en una animacion
 * constante. Todo se deriva del reloj y de la clave del nivel, por lo que no
 * necesita estado, timers ni allocations por frame.
 */
public final class EventosAmbientales {

    private EventosAmbientales() {
    }

    /** Duracion de un ciclo. Solo algunos ciclos contienen un evento. */
    private static final long CICLO_MS = 157_000L;

    /** Ventana visible maxima dentro de un ciclo elegido. */
    private static final long VENTANA_MS = 4_800L;

    public static void dibujar(GuiGraphics grafico, int ancho, int alto,
                               Nivel nivel, float luz) {
        long ahora = System.currentTimeMillis();
        long ciclo = Math.floorDiv(ahora, CICLO_MS);
        long dentro = Math.floorMod(ahora, CICLO_MS);

        int semilla = nivel.clave.hashCode() * 31 + (int) (ciclo ^ (ciclo >>> 32));

        // Aproximadamente dos de cada tres ciclos quedan completamente vacios.
        // Asi no se aprende el ritmo mirando el menu un par de minutos.
        if (Math.floorMod(semilla, 3) != 0 || dentro >= VENTANA_MS) {
            return;
        }

        float progreso = dentro / (float) VENTANA_MS;
        float pulso = (float) Math.sin(progreso * Math.PI);
        if (pulso <= 0.001F) {
            return;
        }

        int tipo = Math.floorMod(semilla / 7, 3);
        switch (tipo) {
            case 0 -> reflejoLejano(grafico, ancho, alto, nivel, luz, pulso, semilla);
            case 1 -> sombraPasajera(grafico, ancho, alto, pulso, semilla);
            default -> respiracionHumedad(grafico, ancho, alto, nivel, luz, pulso, semilla);
        }
    }

    /** Un reflejo fino que aparece lejos y desaparece sin llegar a ser un flash. */
    private static void reflejoLejano(GuiGraphics grafico, int ancho, int alto,
                                      Nivel nivel, float luz, float pulso, int semilla) {
        int x = (int) (ancho * (0.28F + 0.44F * pseudo(semilla + 11)));
        int y = (int) (alto * (0.27F + 0.30F * pseudo(semilla + 17)));
        int largo = Math.max(10, (int) (ancho * (0.025F + 0.035F * pseudo(semilla + 23))));
        float alfa = 0.07F * luz * pulso;
        grafico.fill(x, y, x + largo, y + 1, Paleta.conAlfa(nivel.luz, alfa));
    }

    /** Una sombra muy breve cruza un borde de la escena, fuera del centro de lectura. */
    private static void sombraPasajera(GuiGraphics grafico, int ancho, int alto,
                                       float pulso, int semilla) {
        boolean derecha = (semilla & 1) == 0;
        int anchoSombra = Math.max(8, ancho / 32);
        int x0 = derecha ? ancho - anchoSombra : 0;
        int x1 = derecha ? ancho : anchoSombra;
        int y0 = (int) (alto * (0.20F + 0.46F * pseudo(semilla + 31)));
        int y1 = Math.min(alto, y0 + Math.max(18, alto / 7));
        grafico.fill(x0, y0, x1, y1, Paleta.conAlfa(Paleta.VANO, 0.16F * pulso));
    }

    /** Velo minimo de humedad; gana presencia en niveles ya definidos como humedos. */
    private static void respiracionHumedad(GuiGraphics grafico, int ancho, int alto,
                                           Nivel nivel, float luz, float pulso, int semilla) {
        float humedad = Math.max(0.10F, nivel.humedad);
        int y = (int) (alto * (0.58F + 0.22F * pseudo(semilla + 43)));
        int grosor = Math.max(1, alto / 180);
        float alfa = 0.035F * humedad * luz * pulso;
        grafico.fill(0, y, ancho, Math.min(alto, y + grosor), Paleta.conAlfa(nivel.luz, alfa));
    }

    private static float pseudo(int n) {
        int x = n;
        x ^= x << 13;
        x ^= x >>> 17;
        x ^= x << 5;
        return (x & 0x7FFFFFFF) / (float) Integer.MAX_VALUE;
    }
}
