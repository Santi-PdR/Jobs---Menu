package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Trazo;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Tratamiento final comun a todos los recintos.
 *
 * No cambia la geometria de ninguna Planta. Agrega profundidad atmosferica,
 * rebote de luz sobre el suelo, humedad y un grano minimo para evitar que las
 * superficies grandes se lean como rectangulos planos.
 */
public final class TratamientoEscena {

    private TratamientoEscena() {
    }

    public static void dibujar(GuiGraphics g, int ancho, int alto,
                               Nivel nivel, float luz, float tiempo,
                               boolean movimiento) {
        profundidad(g, ancho, alto, nivel, luz);
        reboteSuelo(g, ancho, alto, nivel, luz, tiempo);
        humedad(g, ancho, alto, nivel, luz, tiempo, movimiento);
        if (movimiento) {
            grano(g, ancho, alto, nivel, luz, tiempo);
        }
    }

    /** Niebla leve en el tercio lejano: separa fondo, medio y primer plano. */
    private static void profundidad(GuiGraphics g, int ancho, int alto, Nivel nivel, float luz) {
        int centroX = (int) (ancho * nivel.fugaX);
        int centroY = (int) (alto * nivel.fugaY);
        int radioX = Math.max(28, ancho / 5);
        int radioY = Math.max(18, alto / 5);

        for (int i = 6; i >= 1; i--) {
            float t = i / 6.0F;
            int rx = (int) (radioX * t);
            int ry = (int) (radioY * t);
            float alfa = 0.010F * (7 - i) * luz;
            int color = Paleta.conAlfa(nivel.niebla, alfa);
            g.fill(centroX - rx, centroY - ry, centroX + rx, centroY + ry, color);
        }
    }

    /** Rebote vertical muy tenue ligado al material del suelo de cada nivel. */
    private static void reboteSuelo(GuiGraphics g, int ancho, int alto,
                                    Nivel nivel, float luz, float tiempo) {
        float fuerza = (0.018F + nivel.reflejo * 0.050F) * luz;
        float respiracion = 0.92F + 0.08F * (float) Math.sin(tiempo * 0.37F + nivel.clave.hashCode());
        fuerza *= respiracion;

        int inicio = (int) (alto * 0.62F);
        int bandas = 8;
        for (int i = 0; i < bandas; i++) {
            float t = i / (float) bandas;
            int y0 = inicio + (alto - inicio) * i / bandas;
            int y1 = inicio + (alto - inicio) * (i + 1) / bandas;
            float alfa = fuerza * (1.0F - t) * (1.0F - t);
            g.fill(0, y0, ancho, y1, Paleta.conAlfa(nivel.luz, alfa));
        }
    }

    /** Condensacion y reflejo horizontal solo donde la humedad del nivel lo justifica. */
    private static void humedad(GuiGraphics g, int ancho, int alto,
                                Nivel nivel, float luz, float tiempo, boolean movimiento) {
        if (nivel.humedad < 0.35F) {
            return;
        }

        int lineas = 3 + Math.round(nivel.humedad * 4.0F);
        for (int i = 0; i < lineas; i++) {
            float base = Trazo.pseudo(nivel.clave.hashCode() + i * 19);
            float deriva = movimiento ? (float) Math.sin(tiempo * (0.08F + i * 0.011F) + i) * 0.015F : 0.0F;
            int y = (int) (alto * (0.50F + base * 0.40F + deriva));
            int x = (int) (ancho * Trazo.pseudo(nivel.clave.hashCode() + i * 31));
            int largo = Math.max(16, (int) (ancho * (0.06F + 0.12F * base)));
            float alfa = 0.018F * nivel.humedad * luz;
            g.fill(x, y, Math.min(ancho, x + largo), y + 1,
                    Paleta.conAlfa(nivel.luz, alfa));
        }
    }

    /** Textura subpixel muy escasa; no es ruido de TV ni filtro visible. */
    private static void grano(GuiGraphics g, int ancho, int alto,
                              Nivel nivel, float luz, float tiempo) {
        int fase = (int) (tiempo * 4.0F);
        int semilla = nivel.clave.hashCode() ^ (fase * 0x45d9f3b);
        int puntos = Math.max(12, Math.min(40, ancho * alto / 18000));

        for (int i = 0; i < puntos; i++) {
            float px = pseudo(semilla + i * 17);
            float py = pseudo(semilla + i * 29);
            int x = (int) (px * ancho);
            int y = (int) (py * alto);
            float alfa = (0.012F + pseudo(semilla + i * 43) * 0.012F) * luz;
            int color = (i & 1) == 0 ? nivel.luz : Paleta.PAPEL;
            g.fill(x, y, x + 1, y + 1, Paleta.conAlfa(color, alfa));
        }
    }

    private static float pseudo(int n) {
        int x = n;
        x ^= x << 13;
        x ^= x >>> 17;
        x ^= x << 5;
        return (x & 0x7FFFFFFF) / (float) Integer.MAX_VALUE;
    }
}
