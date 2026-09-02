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

        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()) return;

        float ciclo = (ahora % 7000L) / 7000.0F;
        int largo = Math.max(18, Math.min(64, ancho / 8));
        int x = margen + Math.round((ancho - margen * 2 - largo) * ciclo);
        g.fill(x, margen + 2, x + largo, margen + 3,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.055F));

        int y = alto - margen - 3;
        int x2 = ancho - margen - largo - Math.round((ancho - margen * 2 - largo) * ciclo);
        g.fill(x2, y, x2 + largo, y + 1,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.045F));
    }
}
