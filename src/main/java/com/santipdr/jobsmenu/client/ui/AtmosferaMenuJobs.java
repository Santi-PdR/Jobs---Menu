package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;

/** Capas globales discretas del menu. Nunca mueve ni deforma los PNG de fondo. */
public final class AtmosferaMenuJobs {

    private AtmosferaMenuJobs() {
    }

    public static void dibujar(GuiGraphics g, int ancho, int alto, long ahora) {
        if (g == null || ancho < 80 || alto < 60) return;
        int margen = Math.max(6, Math.min(14, Math.min(ancho, alto) / 24));
        float contraste = ConfigTurno.altoContraste() ? 1.20F : 1.0F;

        int rail = Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.07F * contraste));
        g.fill(margen, margen + 16, margen + 1, alto - margen - 16, rail);
        g.fill(ancho - margen - 1, margen + 28, ancho - margen, alto - margen - 28,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.045F * contraste)));

        // Esquinas de registro: hacen que todas las pantallas parezcan parte del mismo instrumento.
        int cEsquina = Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.080F * contraste));
        esquina(g, margen, margen, 11, 1, 1, cEsquina);
        esquina(g, ancho - margen, margen, 11, -1, 1, cEsquina);
        esquina(g, margen, alto - margen, 11, 1, -1, cEsquina);
        esquina(g, ancho - margen, alto - margen, 11, -1, -1, cEsquina);

        if (ancho >= 320) {
            int cx = ancho / 2;
            g.fill(cx - 22, margen, cx + 22, margen + 1,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.060F * contraste)));
            g.fill(cx, margen - 2, cx + 1, margen + 4,
                    Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.090F * contraste)));
            g.fill(cx - 11, alto - margen - 1, cx + 12, alto - margen,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.050F * contraste)));
            g.fill(cx - 2, alto - margen - 3, cx + 3, alto - margen - 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.050F * contraste)));
        }

        if (alto >= 180) {
            int cy = alto / 2;
            g.fill(margen - 2, cy - 6, margen + 2, cy - 5,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.060F * contraste)));
            g.fill(ancho - margen - 2, cy + 5, ancho - margen + 2, cy + 6,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.045F * contraste)));
            g.fill(margen + 2, cy + 11, margen + 5, cy + 12,
                    Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.045F * contraste)));
        }

        // Pequenas marcas de calibracion, estaticas y fuera del contenido principal.
        if (!ConfigTurno.papelLimpio() && ancho >= 360 && alto >= 200) {
            int tick = Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.035F * contraste));
            for (int x = margen + 42; x < ancho - margen - 42; x += 58) {
                g.fill(x, margen + 5, x + 1, margen + 8, tick);
            }
            for (int y = margen + 46; y < alto - margen - 46; y += 64) {
                g.fill(margen + 4, y, margen + 7, y + 1, tick);
                g.fill(ancho - margen - 7, y + 17, ancho - margen - 4, y + 18,
                        Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.026F * contraste)));
            }
        }

        boolean quieto = ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo();
        if (quieto) {
            // Sustituto estatico: conserva identidad sin animacion continua.
            if (ancho >= 330) {
                int cx = ancho / 2;
                g.fill(cx - 16, margen + 2, cx + 17, margen + 3,
                        Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.040F * contraste)));
                g.fill(cx - 8, alto - margen - 3, cx + 9, alto - margen - 2,
                        Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.032F * contraste)));
            }
            return;
        }

        float ciclo = (ahora % 7600L) / 7600.0F;
        int largo = Math.max(18, Math.min(70, ancho / 8));
        int recorrido = Math.max(1, ancho - margen * 2 - largo);
        int x = margen + Math.round(recorrido * ciclo);
        g.fill(x, margen + 2, x + largo, margen + 3,
                Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.048F * contraste)));
        g.fill(x + largo / 2, margen + 1, x + largo / 2 + 1, margen + 5,
                Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, limitar(0.034F * contraste)));

        int y = alto - margen - 3;
        int x2 = ancho - margen - largo - Math.round(recorrido * ciclo);
        g.fill(x2, y, x2 + largo, y + 1,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.040F * contraste)));
        g.fill(x2 + largo / 3, y - 2, x2 + largo / 3 + 1, y + 2,
                Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.026F * contraste)));

        if (ancho >= 420 && alto >= 220) {
            float cicloLento = (ahora % 11800L) / 11800.0F;
            int recorridoY = Math.max(1, alto - margen * 2 - 68);
            int y0 = margen + 34 + Math.round(recorridoY * cicloLento);
            g.fill(margen + 3, y0, margen + 4, y0 + 26,
                    Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.030F * contraste)));
            g.fill(margen + 2, y0 + 12, margen + 6, y0 + 13,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, limitar(0.026F * contraste)));

            int y1 = alto - margen - 60 - Math.round(recorridoY * cicloLento);
            g.fill(ancho - margen - 4, y1, ancho - margen - 3, y1 + 20,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.025F * contraste)));
            g.fill(ancho - margen - 6, y1 + 8, ancho - margen - 2, y1 + 9,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, limitar(0.022F * contraste)));
        }

        // Pulso de sistema en el borde, no sobre el fondo. Muy lento y de baja opacidad.
        if (ancho >= 500 && alto >= 260 && !ConfigTurno.interfazMinima()) {
            float pulso = 0.5F + 0.5F * (float) Math.sin(ahora / 1800.0D);
            int px = ancho / 2;
            int span = 18 + Math.round(12.0F * pulso);
            g.fill(px - span, alto - margen - 1, px + span, alto - margen,
                    Paleta.conAlfa(Paleta.UI_ACENTO, limitar((0.022F + 0.018F * pulso) * contraste)));
        }
    }

    private static void esquina(GuiGraphics g, int x, int y, int largo,
                                int dx, int dy, int color) {
        int x0 = dx > 0 ? x : x - largo;
        int y0 = dy > 0 ? y : y - largo;
        g.fill(x0, Math.min(y, y + dy), x0 + largo, Math.max(y, y + dy), color);
        g.fill(Math.min(x, x + dx), y0, Math.max(x, x + dx), y0 + largo, color);
    }

    private static float limitar(float valor) {
        return Math.max(0.0F, Math.min(1.0F, valor));
    }
}
