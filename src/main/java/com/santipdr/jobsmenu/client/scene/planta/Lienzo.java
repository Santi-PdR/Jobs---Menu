package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Taller raster de la segunda direccion visual.
 *
 * No aporta arquitectura ni composiciones prefabricadas. Solo sabe pintar
 * superficies con peso: degradado sucio, piedra aparejada, metal remachado,
 * madera, vidrio, azulejo y agua. Cada recinto decide desde cero donde existen
 * esas superficies y que silueta forman.
 */
final class Lienzo {

    private static final int PASO = 3;

    private Lienzo() {
    }

    static int tono(Nivel nivel, int base, float luz, float distancia) {
        float d = Trazo.limitar(distancia, 0.0F, 1.0F);
        int velado = Paleta.mezclar(base, nivel.niebla, d * 0.38F);
        return Paleta.iluminar(velado, Trazo.limitar(luz * (1.04F - d * 0.34F), 0.04F, 1.0F));
    }

    static void caja(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
        int izq = Math.min(x0, x1), der = Math.max(x0, x1);
        int arriba = Math.min(y0, y1), abajo = Math.max(y0, y1);
        if (der > izq && abajo > arriba) {
            g.fill(izq, arriba, der, abajo, color);
        }
    }

    /** Fondo tonal en bandas anchas: pictorico, no un color plano. */
    static void fondo(GuiGraphics g, int ancho, int alto, int arriba, int abajo, int semilla) {
        int bandas = Math.max(12, Math.min(28, alto / 18));
        for (int i = 0; i < bandas; i++) {
            int y0 = i * alto / bandas;
            int y1 = (i + 1) * alto / bandas + 1;
            float t = i / (float) Math.max(1, bandas - 1);
            float ruido = (Trazo.pseudo(semilla + i * 17) - 0.5F) * 0.075F;
            caja(g, 0, y0, ancho, y1, Paleta.mezclar(arriba, abajo,
                    Trazo.limitar(t + ruido, 0.0F, 1.0F)));
        }
    }

    /** Cuadrilatero scanline sin arrays ni vertices temporales. */
    static void quad(GuiGraphics g, int y0, int y1,
                     float x0i, float x0d, float x1i, float x1d, int color) {
        if (y1 <= y0) return;
        for (int y = y0; y < y1; y += PASO) {
            float t = (y - y0) / (float) (y1 - y0);
            int izq = Math.round(x0i + (x1i - x0i) * t);
            int der = Math.round(x0d + (x1d - x0d) * t);
            caja(g, izq, y, der, Math.min(y1, y + PASO), color);
        }
    }

    static void linea(GuiGraphics g, float x0, float y0, float x1, float y1,
                      int grosor, int color) {
        int pasos = Math.max(1, Math.max(Math.abs(Math.round(x1 - x0)), Math.abs(Math.round(y1 - y0))));
        int radio = Math.max(0, grosor / 2);
        for (int i = 0; i <= pasos; i += PASO) {
            float t = i / (float) pasos;
            int x = Math.round(x0 + (x1 - x0) * t);
            int y = Math.round(y0 + (y1 - y0) * t);
            caja(g, x - radio, y - radio, x + radio + 1, y + radio + 1, color);
        }
    }

    static void circulo(GuiGraphics g, int cx, int cy, int radio, int color) {
        if (radio <= 0) return;
        for (int y = -radio; y <= radio; y += 2) {
            int medio = Math.round((float) Math.sqrt(Math.max(0, radio * radio - y * y)));
            caja(g, cx - medio, cy + y, cx + medio + 1, cy + y + 2, color);
        }
    }

    static void anillo(GuiGraphics g, int cx, int cy, int radio, int espesor,
                       int borde, int centro) {
        circulo(g, cx, cy, radio, borde);
        circulo(g, cx, cy, Math.max(1, radio - espesor), centro);
    }

    static void arco(GuiGraphics g, int cx, int cima, int base, int radio,
                     int espesor, int material, int hueco) {
        int union = cima + radio;
        int interiorR = Math.max(1, radio - espesor);
        for (int y = cima; y < Math.min(base, union); y += 2) {
            float dy = (y - union) / (float) radio;
            int exterior = Math.round(radio * (float) Math.sqrt(Math.max(0.0F, 1.0F - dy * dy)));
            float idy = (y - union) / (float) interiorR;
            int interior = Math.round(interiorR * (float) Math.sqrt(Math.max(0.0F, 1.0F - idy * idy)));
            caja(g, cx - exterior, y, cx + exterior + 1, y + 2, material);
            if (interior > 0) caja(g, cx - interior, y, cx + interior + 1, y + 2, hueco);
        }
        caja(g, cx - radio, union, cx - radio + espesor, base, material);
        caja(g, cx + radio - espesor, union, cx + radio, base, material);
        caja(g, cx - radio + espesor, union, cx + radio - espesor, base, hueco);
    }

    static void halo(GuiGraphics g, int cx, int cy, int radio, int color, float alfa) {
        for (int r = radio; r > 2; r -= Math.max(3, radio / 10)) {
            float centro = 1.0F - r / (float) radio;
            circulo(g, cx, cy, r, Paleta.conAlfa(color, alfa * (0.05F + centro * 0.13F)));
        }
    }

    /** Revoque con manchas verticales, descascarado y variacion gruesa. */
    static void revoque(GuiGraphics g, int x0, int y0, int x1, int y1,
                         int base, int sombra, int semilla) {
        caja(g, x0, y0, x1, y1, base);
        int ancho = Math.max(1, x1 - x0), alto = Math.max(1, y1 - y0);
        int manchas = Math.max(8, Math.min(34, ancho * alto / 6200));
        for (int i = 0; i < manchas; i++) {
            int x = x0 + Math.round(Trazo.pseudo(semilla + i * 7) * ancho);
            int y = y0 + Math.round(Trazo.pseudo(semilla + i * 7 + 1) * alto);
            int w = 2 + Math.round(Trazo.pseudo(semilla + i * 7 + 2) * Math.max(4, ancho * 0.10F));
            int h = 2 + Math.round(Trazo.pseudo(semilla + i * 7 + 3) * Math.max(3, alto * 0.09F));
            caja(g, x, y, Math.min(x1, x + w), Math.min(y1, y + h),
                    Paleta.conAlfa(sombra, 0.08F + Trazo.pseudo(semilla + i * 7 + 4) * 0.16F));
        }
        for (int i = 0; i < 5; i++) {
            int x = x0 + Math.round(Trazo.pseudo(semilla + 500 + i) * ancho);
            linea(g, x, y0, x + (i % 2 == 0 ? 5 : -4), y1, 1,
                    Paleta.conAlfa(sombra, 0.10F));
        }
    }

    /** Bloques escalonados: juntas no perfectas y borde humedo. */
    static void piedra(GuiGraphics g, int x0, int y0, int x1, int y1,
                       int base, int junta, int semilla) {
        caja(g, x0, y0, x1, y1, base);
        int altoFila = Math.max(8, Math.min(18, Math.max(1, y1 - y0) / 7));
        int anchoBloque = Math.max(14, Math.min(34, Math.max(1, x1 - x0) / 5));
        int fila = 0;
        for (int y = y0; y < y1; y += altoFila, fila++) {
            int deriva = ((fila & 1) == 0 ? 0 : anchoBloque / 2)
                    + Math.round((Trazo.pseudo(semilla + fila) - 0.5F) * 4);
            caja(g, x0, y, x1, Math.min(y1, y + 1), Paleta.conAlfa(junta, 0.55F));
            for (int x = x0 - anchoBloque + deriva; x < x1; x += anchoBloque) {
                caja(g, x, y, x + 1, Math.min(y1, y + altoFila), Paleta.conAlfa(junta, 0.42F));
            }
        }
        caja(g, x0, y1 - 3, x1, y1, Paleta.conAlfa(junta, 0.28F));
    }

    /** Chapa pintada: pliegues, borde claro, oxido y remaches. */
    static void metal(GuiGraphics g, int x0, int y0, int x1, int y1,
                      int base, int borde, int oxido, int semilla) {
        caja(g, x0, y0, x1, y1, base);
        int ancho = Math.max(1, x1 - x0), alto = Math.max(1, y1 - y0);
        for (int x = x0 + 5; x < x1; x += Math.max(8, ancho / 7)) {
            caja(g, x, y0, x + 1, y1, Paleta.conAlfa(borde, 0.24F));
            caja(g, x + 1, y0, x + 3, y1, Paleta.conAlfa(oxido, 0.10F));
        }
        for (int i = 0; i < 7; i++) {
            int x = x0 + 2 + Math.round(Trazo.pseudo(semilla + i * 3) * Math.max(1, ancho - 5));
            int y = y0 + 2 + Math.round(Trazo.pseudo(semilla + i * 3 + 1) * Math.max(1, alto - 5));
            caja(g, x, y, x + 2, y + 2, Paleta.conAlfa(oxido, 0.38F));
        }
        caja(g, x0, y0, x1, y0 + 2, Paleta.conAlfa(borde, 0.44F));
    }

    /** Tablones con veta longitudinal y separaciones profundas. */
    static void madera(GuiGraphics g, int x0, int y0, int x1, int y1,
                       int base, int veta, int semilla) {
        caja(g, x0, y0, x1, y1, base);
        int tabla = Math.max(6, Math.min(14, Math.max(1, x1 - x0) / 8));
        for (int x = x0; x < x1; x += tabla) {
            caja(g, x, y0, x + 1, y1, Paleta.conAlfa(veta, 0.62F));
            int curva = Math.round((Trazo.pseudo(semilla + x) - 0.5F) * 4);
            linea(g, x + tabla / 2, y0 + 3, x + tabla / 2 + curva, y1 - 3, 1,
                    Paleta.conAlfa(veta, 0.18F));
        }
    }

    /** Vidrio con cuerpo, perfileria, reflejo diagonal y gotas. */
    static void cristal(GuiGraphics g, int x0, int y0, int x1, int y1,
                        int vidrio, int marco, int luz, int semilla, float tiempo) {
        caja(g, x0, y0, x1, y1, Paleta.conAlfa(vidrio, 0.58F));
        caja(g, x0, y0, x1, y0 + 2, marco);
        caja(g, x0, y1 - 2, x1, y1, marco);
        caja(g, x0, y0, x0 + 2, y1, marco);
        caja(g, x1 - 2, y0, x1, y1, marco);
        linea(g, x0 + 3, y1 - 4, x1 - 4, y0 + 3, 1, Paleta.conAlfa(luz, 0.16F));
        for (int i = 0; i < 7; i++) {
            int x = x0 + 3 + Math.round(Trazo.pseudo(semilla + i * 5) * Math.max(1, x1 - x0 - 7));
            float fase = (Trazo.pseudo(semilla + i * 5 + 1) + tiempo * (0.002F + i * 0.0002F)) % 1.0F;
            int y = y0 + 3 + Math.round(fase * Math.max(1, y1 - y0 - 7));
            caja(g, x, y, x + 1, Math.min(y1 - 2, y + 2 + i % 4), Paleta.conAlfa(luz, 0.22F));
        }
    }

    static void azulejo(GuiGraphics g, int x0, int y0, int x1, int y1,
                        int base, int junta, int tamano) {
        caja(g, x0, y0, x1, y1, base);
        int paso = Math.max(6, tamano);
        for (int y = y0; y < y1; y += paso) {
            caja(g, x0, y, x1, y + 1, Paleta.conAlfa(junta, 0.42F));
        }
        for (int x = x0; x < x1; x += paso) {
            caja(g, x, y0, x + 1, y1, Paleta.conAlfa(junta, 0.34F));
        }
    }

    /** Agua estratificada: masa oscura, luces rotas y ondas no sincronizadas. */
    static void agua(GuiGraphics g, int x0, int y0, int x1, int y1,
                     int profundo, int superficie, int reflejo, float tiempo, int semilla) {
        int alto = Math.max(1, y1 - y0);
        for (int y = y0; y < y1; y += 5) {
            float t = (y - y0) / (float) alto;
            caja(g, x0, y, x1, Math.min(y1, y + 5), Paleta.mezclar(superficie, profundo, t));
        }
        for (int i = 0; i < 18; i++) {
            float f = i / 18F;
            int y = y0 + 3 + Math.round(f * (alto - 5));
            int deriva = Math.round((float) Math.sin(tiempo * (0.22F + i * 0.007F) + semilla + i) * (4 + f * 10));
            int inicio = x0 + Math.round(Trazo.pseudo(semilla + i * 9) * Math.max(1, x1 - x0 - 18));
            int largo = 8 + Math.round(Trazo.pseudo(semilla + i * 9 + 1) * Math.max(10, (x1 - x0) * 0.23F));
            caja(g, inicio + deriva, y, Math.min(x1, inicio + deriva + largo), y + 1,
                    Paleta.conAlfa(reflejo, 0.06F + (1.0F - f) * 0.13F));
        }
    }

    static void reflejo(GuiGraphics g, int x, int y0, int y1, int ancho,
                        int color, float alfa, float tiempo, int semilla) {
        for (int y = y0; y < y1; y += 4) {
            float t = (y - y0) / (float) Math.max(1, y1 - y0);
            int deriva = Math.round((float) Math.sin(tiempo * 0.31F + y * 0.11F + semilla) * (2 + 8 * t));
            int medio = Math.max(1, Math.round(ancho * (1.0F - t * 0.72F)));
            caja(g, x - medio + deriva, y, x + medio + deriva, y + 1,
                    Paleta.conAlfa(color, alfa * (1.0F - t)));
        }
    }

    static void motas(GuiGraphics g, int ancho, int alto, float tiempo,
                      int color, float alfa, int semilla, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            float x = Trazo.pseudo(semilla + i * 11);
            float y = (Trazo.pseudo(semilla + i * 11 + 1)
                    + tiempo * (0.0015F + Trazo.pseudo(semilla + i * 11 + 2) * 0.002F)) % 1.0F;
            int px = Math.round(x * ancho), py = Math.round(y * alto);
            caja(g, px, py, px + 1, py + 1,
                    Paleta.conAlfa(color, alfa * (0.35F + Trazo.pseudo(semilla + i * 11 + 3) * 0.65F)));
        }
    }
}
