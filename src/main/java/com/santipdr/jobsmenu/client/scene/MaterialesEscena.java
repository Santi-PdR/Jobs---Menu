package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Trazo;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Microdetalle de material por familia de recinto.
 *
 * Mantiene el renderer sin texturas y rompe superficies demasiado limpias con
 * señales físicas: juntas, vetas, remaches, grietas, condensación y depósitos.
 * Todo es determinista por nivel para que no parezca ruido de pantalla.
 */
public final class MaterialesEscena {

    private MaterialesEscena() {
    }

    public static void dibujar(GuiGraphics g, int w, int h, Nivel n,
                               float luz, float tiempo, boolean movimiento) {
        switch (n.clave) {
            case "nivel0" -> papelMural(g, w, h, n, luz);
            case "nivel1" -> metalHormigon(g, w, h, n, luz, false);
            case "nivel2" -> metalHormigon(g, w, h, n, luz, true);
            case "nivel3" -> azulejo(g, w, h, n, luz);
            case "nivel4" -> piedra(g, w, h, n, luz, 18);
            case "nivel5" -> madera(g, w, h, n, luz);
            case "nivel6" -> vidrioHumedo(g, w, h, n, luz, tiempo, movimiento);
            case "nivel7" -> piedra(g, w, h, n, luz, 26);
            case "nivel8" -> metalHormigon(g, w, h, n, luz, true);
            case "nivel9" -> piedra(g, w, h, n, luz, 22);
            default -> { }
        }
    }

    private static void papelMural(GuiGraphics g, int w, int h, Nivel n, float luz) {
        int seed = n.clave.hashCode();
        for (int i = 0; i < 28; i++) {
            float px = Trazo.pseudo(seed + i * 17);
            float py = Trazo.pseudo(seed + i * 31);
            int x = (int) (px * w);
            int y = (int) (py * h * 0.72F);
            int largo = 3 + (int) (Trazo.pseudo(seed + i * 47) * 14);
            int color = (i % 3 == 0) ? n.junta : n.paredBaja;
            g.fill(x, y, Math.min(w, x + largo), y + 1,
                    Paleta.conAlfa(color, 0.028F * luz));
        }
    }

    private static void metalHormigon(GuiGraphics g, int w, int h, Nivel n,
                                      float luz, boolean industrial) {
        int panel = Math.max(44, w / 9);
        for (int x = panel; x < w; x += panel) {
            g.fill(x, (int) (h * 0.16F), x + 1, (int) (h * 0.78F),
                    Paleta.conAlfa(n.junta, 0.10F * luz));
            if (industrial) {
                for (int y = (int) (h * 0.24F); y < h * 0.73F; y += Math.max(18, h / 8)) {
                    remache(g, x - 1, y, n.luz, luz);
                }
            }
        }
        int seed = n.clave.hashCode();
        for (int i = 0; i < 18; i++) {
            int x = (int) (Trazo.pseudo(seed + i * 13) * w);
            int y = (int) ((0.22F + Trazo.pseudo(seed + i * 23) * 0.55F) * h);
            int largo = 5 + (int) (Trazo.pseudo(seed + i * 41) * 18);
            g.fill(x, y, Math.min(w, x + largo), y + 1,
                    Paleta.conAlfa(Paleta.VANO, 0.035F));
        }
    }

    private static void azulejo(GuiGraphics g, int w, int h, Nivel n, float luz) {
        int pasoX = Math.max(22, w / 25);
        int pasoY = Math.max(14, h / 18);
        int yFin = (int) (h * 0.64F);
        for (int y = (int) (h * 0.18F); y < yFin; y += pasoY) {
            g.fill(0, y, w, y + 1, Paleta.conAlfa(n.junta, 0.055F * luz));
            int desfase = ((y / pasoY) & 1) == 0 ? 0 : pasoX / 2;
            for (int x = desfase; x < w; x += pasoX) {
                g.fill(x, y, x + 1, Math.min(yFin, y + pasoY),
                        Paleta.conAlfa(n.junta, 0.040F * luz));
            }
        }
    }

    private static void piedra(GuiGraphics g, int w, int h, Nivel n,
                               float luz, int grietas) {
        int juntaY = Math.max(18, h / 12);
        for (int y = (int) (h * 0.16F); y < h * 0.78F; y += juntaY) {
            g.fill(0, y, w, y + 1, Paleta.conAlfa(n.junta, 0.060F * luz));
        }

        int seed = n.clave.hashCode() * 7;
        for (int i = 0; i < grietas; i++) {
            int x = (int) (Trazo.pseudo(seed + i * 11) * w);
            int y = (int) ((0.20F + Trazo.pseudo(seed + i * 19) * 0.58F) * h);
            int dx = 4 + (int) (Trazo.pseudo(seed + i * 29) * 14);
            int dy = (Trazo.pseudo(seed + i * 37) > 0.5F ? 1 : -1)
                    * (2 + (int) (Trazo.pseudo(seed + i * 43) * 8));
            grieta(g, x, y, dx, dy, n.junta, luz);
        }
    }

    private static void madera(GuiGraphics g, int w, int h, Nivel n, float luz) {
        int seed = n.clave.hashCode();
        for (int lado = 0; lado < 2; lado++) {
            int x0 = lado == 0 ? 0 : (int) (w * 0.69F);
            int x1 = lado == 0 ? (int) (w * 0.31F) : w;
            for (int i = 0; i < 24; i++) {
                int y = (int) ((0.18F + Trazo.pseudo(seed + lado * 101 + i * 7) * 0.60F) * h);
                int x = x0 + (int) (Trazo.pseudo(seed + lado * 211 + i * 13) * Math.max(1, x1 - x0));
                int largo = 8 + (int) (Trazo.pseudo(seed + i * 23) * 30);
                g.fill(x, y, Math.min(x1, x + largo), y + 1,
                        Paleta.conAlfa(n.paredAlta, 0.025F * luz));
            }
        }
    }

    private static void vidrioHumedo(GuiGraphics g, int w, int h, Nivel n,
                                     float luz, float tiempo, boolean movimiento) {
        int seed = n.clave.hashCode();
        for (int i = 0; i < 20; i++) {
            int x = (int) (Trazo.pseudo(seed + i * 17) * w);
            float base = Trazo.pseudo(seed + i * 31);
            float deriva = movimiento ? ((tiempo * (0.6F + base)) % 18.0F) : 0.0F;
            int y = (int) (h * (0.08F + base * 0.50F)) + (int) deriva;
            int largo = 3 + (int) (base * 13);
            g.fill(x, y, x + 1, Math.min(h, y + largo),
                    Paleta.conAlfa(n.luz, 0.040F * luz * n.humedad));
        }
    }

    private static void remache(GuiGraphics g, int x, int y, int luzColor, float luz) {
        g.fill(x - 1, y - 1, x + 2, y + 2, Paleta.conAlfa(Paleta.VANO, 0.42F));
        g.fill(x, y, x + 1, y + 1, Paleta.conAlfa(luzColor, 0.30F * luz));
    }

    private static void grieta(GuiGraphics g, int x, int y, int dx, int dy,
                               int color, float luz) {
        int pasos = Math.max(3, Math.abs(dx));
        for (int i = 0; i < pasos; i++) {
            float t = i / (float) pasos;
            int px = x + (int) (dx * t);
            int py = y + (int) (dy * t) + ((i & 3) == 0 ? 1 : 0);
            g.fill(px, py, px + 1, py + 1, Paleta.conAlfa(color, 0.16F * luz));
        }
    }
}
