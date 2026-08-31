package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;

/**
 * La hoja fotocopiada pegada a la pared: papel, sombra proyectada, borde
 * humedecido y un trozo de cinta arriba.
 */
public final class HojaPapel {

    private HojaPapel() {
    }

    public static void dibujar(GuiGraphics grafico, int x0, int y0, int x1, int y1, boolean conCinta) {
        dibujar(grafico, x0, y0, x1, y1, conCinta, RotacionNiveles.luzDisponible());
    }

    /** Variante para pantallas que no pertenecen al apagon del menu. */
    public static void dibujar(GuiGraphics grafico, int x0, int y0, int x1, int y1,
                               boolean conCinta, float luz) {
        luz = Math.max(0.0F, Math.min(1.0F, luz));
        int papel = Paleta.iluminar(Paleta.papelAviso(), 0.22F + 0.78F * luz);
        int papelBajo = Paleta.iluminar(Paleta.papelAviso(), 0.18F + 0.72F * luz);
        boolean limpio = ConfigTurno.papelLimpio();

        if (!limpio) {
            // Dos sombras con distinta distancia dan espesor sin convertir la
            // hoja en una tarjeta flotante. La lejana es mas suave.
            grafico.fill(x0 + 5, y0 + 6, x1 + 5, y1 + 6,
                    Paleta.conAlfa(Paleta.VANO, 0.13F));
            grafico.fill(x0 + 2, y0 + 3, x1 + 2, y1 + 3,
                    Paleta.conAlfa(Paleta.VANO, 0.24F));
        }

        // El papel no es un rectangulo de color plano: la parte baja recibe
        // menos fluorescente y queda apenas mas sucia.
        grafico.fillGradient(x0, y0, x1, y1,
                Paleta.conAlfa(papel, 0.96F), Paleta.conAlfa(papelBajo, 0.96F));

        int borde = Paleta.tintaSecundaria();
        grafico.fill(x0, y0, x1, y0 + 1, Paleta.conAlfa(borde, limpio ? 0.22F : 0.35F));
        grafico.fill(x0, y1 - 1, x1, y1, Paleta.conAlfa(borde, limpio ? 0.28F : 0.45F));
        grafico.fill(x0, y0, x0 + 1, y1, Paleta.conAlfa(borde, limpio ? 0.22F : 0.35F));
        grafico.fill(x1 - 1, y0, x1, y1, Paleta.conAlfa(borde, limpio ? 0.22F : 0.35F));

        if (!limpio) {
            fotocopia(grafico, x0, y0, x1, y1, luz);
        }

        if (conCinta && !limpio) {
            int cinta = Math.max(16, Math.min(22, (x1 - x0) / 5));
            int centro = (x0 + x1) / 2;
            int cintaColor = Paleta.iluminar(Paleta.papelAviso(), 0.30F + 0.62F * luz);
            grafico.fill(centro - cinta, y0 - 4, centro + cinta, y0 + 4,
                    Paleta.conAlfa(cintaColor, 0.48F));
            // Los dos bordes de la cinta son casi invisibles, pero hacen que no
            // se confunda con otro rectangulo de papel.
            grafico.fill(centro - cinta, y0 - 4, centro + cinta, y0 - 3,
                    Paleta.conAlfa(Paleta.FLUOR, 0.10F * luz));
            grafico.fill(centro - cinta, y0 + 3, centro + cinta, y0 + 4,
                    Paleta.conAlfa(Paleta.VANO, 0.10F));
        }
    }

    /** Marcas deterministas de fotocopia y roce: baratas y sin ruido aleatorio. */
    private static void fotocopia(GuiGraphics g, int x0, int y0, int x1, int y1, float luz) {
        int w = Math.max(1, x1 - x0);
        int h = Math.max(1, y1 - y0);
        int tinta = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.030F + 0.020F * (1.0F - luz));

        int yA = y0 + Math.max(5, h / 5);
        int yB = y0 + Math.max(9, h * 3 / 5);
        g.fill(x0 + Math.max(4, w / 14), yA,
                x0 + Math.max(8, w / 4), yA + 1, tinta);
        g.fill(x1 - Math.max(12, w / 5), yB,
                x1 - Math.max(4, w / 18), yB + 1, tinta);

        // Desgaste minusculo de borde. No invade el area de lectura.
        int roce = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.075F);
        g.fill(x0, y0 + h / 3, x0 + 2, y0 + h / 3 + 1, roce);
        g.fill(x1 - 2, y0 + h * 4 / 5, x1, y0 + h * 4 / 5 + 1, roce);
    }

    public static float tinta() {
        return 0.10F + 0.90F * RotacionNiveles.luzDisponible();
    }
}
