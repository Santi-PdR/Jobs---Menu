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

        int rail = Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.07F);
        g.fill(margen, margen + 16, margen + 1, alto - margen - 16, rail);
        g.fill(ancho - margen - 1, margen + 28, ancho - margen, alto - margen - 28,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.045F));

        if (ancho >= 320) {
            int cx = ancho / 2;
            g.fill(cx - 18, margen, cx + 18, margen + 1,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.055F));
            g.fill(cx, margen - 2, cx + 1, margen + 3,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.08F));
            g.fill(cx - 9, alto - margen - 1, cx + 10, alto - margen,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.045F));
        }

        if (alto >= 180) {
            int cy = alto / 2;
            g.fill(margen - 2, cy - 5, margen + 2, cy - 4,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.055F));
            g.fill(ancho - margen - 2, cy + 4, ancho - margen + 2, cy + 5,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.040F));
        }

        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()) return;

        float ciclo = (ahora % 7600L) / 7600.0F;
        int largo = Math.max(18, Math.min(70, ancho / 8));
        int recorrido = Math.max(1, ancho - margen * 2 - largo);
        int x = margen + Math.round(recorrido * ciclo);
        g.fill(x, margen + 2, x + largo, margen + 3,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.048F));

        int y = alto - margen - 3;
        int x2 = ancho - margen - largo - Math.round(recorrido * ciclo);
        g.fill(x2, y, x2 + largo, y + 1,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.040F));

        if (ancho >= 420 && alto >= 220) {
            float cicloLento = (ahora % 11800L) / 11800.0F;
            int y0 = margen + 34 + Math.round((alto - margen * 2 - 68) * cicloLento);
            g.fill(margen + 3, y0, margen + 4, y0 + 26,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.030F));
            int y1 = alto - margen - 60 - Math.round((alto - margen * 2 - 68) * cicloLento);
            g.fill(ancho - margen - 4, y1, ancho - margen - 3, y1 + 20,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.025F));
        }
    }
}
