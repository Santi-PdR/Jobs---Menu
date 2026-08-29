package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/** Primitivas raster sin texturas para arquitectura, materiales y luz. */
final class Arquitectura {

    private Arquitectura() {
    }

    static int material(Nivel n, int color, float luz, float lejos, float niebla) {
        return Paleta.iluminar(Trazo.velar(color, n.niebla, lejos, niebla),
                Trazo.atenuar(luz, lejos));
    }

    /** Trapecio vertical, sin listas ni objetos temporales por fotograma. */
    static void trapecio(GuiGraphics g, int y0, int y1,
                          float x0i, float x0d, float x1i, float x1d, int color) {
        if (y1 <= y0) {
            return;
        }
        int paso = Math.max(1, Trazo.PASO);
        for (int y = y0; y < y1; y += paso) {
            float t = (y - y0) / (float) (y1 - y0);
            int izq = Math.round(x0i + (x1i - x0i) * t);
            int der = Math.round(x0d + (x1d - x0d) * t);
            if (der > izq) {
                g.fill(izq, y, der, Math.min(y1, y + paso), color);
            }
        }
    }

    /** Segmento grueso para vigas, cables, grietas y reflejos. */
    static void linea(GuiGraphics g, float x0, float y0, float x1, float y1,
                      int grosor, int color) {
        int pasos = Math.max(1, Math.max(Math.abs(Math.round(x1 - x0)), Math.abs(Math.round(y1 - y0))));
        int paso = Math.max(1, Trazo.PASO);
        for (int i = 0; i <= pasos; i += paso) {
            float t = i / (float) pasos;
            int x = Math.round(x0 + (x1 - x0) * t);
            int y = Math.round(y0 + (y1 - y0) * t);
            g.fill(x - grosor / 2, y - grosor / 2,
                    x + Math.max(1, grosor), y + Math.max(1, grosor), color);
        }
    }

    /** Arco de mamposteria con hueco real, no un rectangulo oscuro. */
    static void arco(GuiGraphics g, int cx, int cima, int base, int radio,
                     int espesor, int piedra, int hueco) {
        int union = cima + radio;
        for (int y = cima; y < Math.min(base, union); y += Trazo.PASO) {
            float dy = (y - union) / (float) radio;
            int exterior = Math.round(radio * (float) Math.sqrt(Math.max(0.0F, 1.0F - dy * dy)));
            int interiorR = Math.max(1, radio - espesor);
            int interior = Math.round(interiorR * (float) Math.sqrt(Math.max(0.0F,
                    1.0F - (y - union) * (y - union) / (float) (interiorR * interiorR))));
            g.fill(cx - exterior, y, cx + exterior, y + Trazo.PASO, piedra);
            if (interior > 0) {
                g.fill(cx - interior, y, cx + interior, y + Trazo.PASO, hueco);
            }
        }
        g.fill(cx - radio, union, cx - radio + espesor, base, piedra);
        g.fill(cx + radio - espesor, union, cx + radio, base, piedra);
        g.fill(cx - radio + espesor, union, cx + radio - espesor, base, hueco);
    }

    static void circulo(GuiGraphics g, int cx, int cy, int radio, int color) {
        for (int y = -radio; y <= radio; y += Trazo.PASO) {
            int medio = Math.round((float) Math.sqrt(Math.max(0, radio * radio - y * y)));
            g.fill(cx - medio, cy + y, cx + medio, cy + y + Trazo.PASO, color);
        }
    }

    static void halo(GuiGraphics g, int cx, int cy, int radio, int color, float alfa) {
        for (int r = radio; r > 0; r -= 3) {
            float a = alfa * (1.0F - r / (float) radio) * 0.40F;
            circulo(g, cx, cy, r, Paleta.conAlfa(color, a));
        }
    }

    static void reflejo(GuiGraphics g, int x, int y0, int y1, int ancho,
                        int color, float alfa, float tiempo, int semilla) {
        for (int y = y0; y < y1; y += 3) {
            float t = (y - y0) / (float) Math.max(1, y1 - y0);
            int deriva = Math.round((float) Math.sin(tiempo * 0.35F + y * 0.09F + semilla) * (2.0F + 7.0F * t));
            int medio = Math.max(1, Math.round(ancho * (1.0F - t * 0.70F)));
            g.fill(x - medio + deriva, y, x + medio + deriva, y + 1,
                    Paleta.conAlfa(color, alfa * (1.0F - t) * 0.65F));
        }
    }

    static void polvo(GuiGraphics g, int ancho, int alto, float tiempo,
                      int color, float alfa, int semilla, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            float px = Trazo.pseudo(semilla + i * 11);
            float py = (Trazo.pseudo(semilla + i * 11 + 1) + tiempo * 0.003F
                    * (1.0F + Trazo.pseudo(semilla + i * 11 + 2))) % 1.0F;
            int x = Math.round(px * ancho);
            int y = Math.round(py * alto);
            g.fill(x, y, x + 1, y + 1, Paleta.conAlfa(color,
                    alfa * (0.35F + 0.65F * Trazo.pseudo(semilla + i * 11 + 3))));
        }
    }
}
