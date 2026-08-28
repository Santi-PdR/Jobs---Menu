package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Primitivas nuevas para la reconstruccion total de los diez fondos. */
public final class NuevaEscena {
    private NuevaEscena() {}

    public static void base(GuiGraphics g, Marco m, Nivel n, float luz, int tramos, float velo) {
        Trazo.fondo(g, m, n, luz, Paleta.mezclar(n.paredBaja, n.niebla, 0.18F), 1.25F);
        Trazo.plano(g, m, true, n.techo, Paleta.mezclar(n.techo, n.niebla, 0.35F), n.niebla, luz, velo);
        Trazo.plano(g, m, false, n.suelo, n.sueloLejos, n.niebla, luz, velo + 0.05F);
        Trazo.paredes(g, m, n, luz);
        Trazo.transversales(g, m, false, n.sueloJunta, n.niebla, luz, tramos, 0.32F);
        Trazo.transversales(g, m, true, n.techoJunta, n.niebla, luz, tramos, 0.26F);
    }

    public static void glow(GuiGraphics g, int cx, int cy, int radio, int color, float alfa) {
        for (int i = 6; i >= 1; i--) {
            int r = Math.max(1, radio * i / 6);
            float a = alfa * (7 - i) / 6.0F;
            g.fill(cx - r, cy - r, cx + r, cy + r, Paleta.conAlfa(color, a * 0.18F));
        }
        g.fill(cx - 1, cy - 1, cx + 2, cy + 2, Paleta.conAlfa(color, alfa));
    }

    public static void columna(GuiGraphics g, Marco m, Nivel n, float dx, float frac,
                               float anchoFrac, int color, float luz) {
        float cx = m.enX(dx, frac);
        float w = Math.max(2.0F, m.w() * dx * anchoFrac);
        int y0 = Math.round(m.techoEn(dx));
        int y1 = Math.round(m.sueloEn(dx));
        int x0 = Math.round(cx - w);
        int x1 = Math.round(cx + w);
        g.fillGradient(x0, y0, x1, y1,
                Paleta.iluminar(Paleta.mezclar(color, n.luz, 0.12F), luz),
                Paleta.iluminar(Paleta.mezclar(color, Paleta.VANO, 0.50F), luz * 0.72F));
        g.fill(x0, y0, x0 + Math.max(1, (int)(w * 0.20F)), y1,
                Paleta.conAlfa(Paleta.VANO, 0.38F));
        g.fill(x1 - 1, y0, x1, y1, Paleta.conAlfa(n.luz, 0.13F * luz));
    }

    public static void arco(GuiGraphics g, int cx, int baseY, int ancho, int alto,
                            int grosor, int color) {
        int pasos = 18;
        for (int i = 0; i <= pasos; i++) {
            double a = Math.PI * i / pasos;
            int x = cx + (int)(Math.cos(a) * ancho);
            int y = baseY - alto - (int)(Math.sin(a) * alto * 0.72);
            g.fill(x - grosor, y - grosor, x + grosor + 1, y + grosor + 1, color);
        }
        g.fill(cx - ancho - grosor, baseY - alto, cx - ancho + grosor, baseY, color);
        g.fill(cx + ancho - grosor, baseY - alto, cx + ancho + grosor, baseY, color);
    }

    public static void cadena(GuiGraphics g, int x, int y0, int y1, int color, int escala) {
        int paso = Math.max(5, 8 * escala);
        for (int y = y0; y < y1; y += paso) {
            int s = Math.max(1, escala);
            g.fill(x - s, y, x + s + 1, y + s * 3, color);
            g.fill(x - s + 1, y + 1, x + s, y + s * 3 - 1, Paleta.conAlfa(Paleta.VANO, 0.72F));
        }
    }

    public static void panel(GuiGraphics g, int x0, int y0, int x1, int y1,
                             int base, int borde, int luz, boolean remaches) {
        g.fillGradient(x0, y0, x1, y1, base, Paleta.mezclar(base, Paleta.VANO, 0.42F));
        g.fill(x0, y0, x1, y0 + 1, borde);
        g.fill(x0, y0, x0 + 1, y1, borde);
        g.fill(x1 - 1, y0, x1, y1, Paleta.conAlfa(Paleta.VANO, 0.55F));
        if (remaches) {
            int r = Math.max(2, (x1 - x0) / 10);
            g.fill(x0 + r, y0 + r, x0 + r + 1, y0 + r + 1, luz);
            g.fill(x1 - r - 1, y0 + r, x1 - r, y0 + r + 1, luz);
            g.fill(x0 + r, y1 - r - 1, x0 + r + 1, y1 - r, luz);
            g.fill(x1 - r - 1, y1 - r - 1, x1 - r, y1 - r, luz);
        }
    }

    public static void agua(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo,
                            float inicioFrac, int lineas) {
        int y0 = (int)(m.alto() * inicioFrac);
        g.fillGradient(0, y0, m.ancho(), m.alto(),
                Paleta.conAlfa(Paleta.mezclar(n.sueloLejos, n.luz, 0.16F), 0.54F),
                Paleta.conAlfa(Paleta.mezclar(n.suelo, Paleta.VANO, 0.58F), 0.90F));
        for (int i = 0; i < lineas; i++) {
            float fase = tiempo * (0.55F + i * 0.06F) + i * 1.7F;
            int y = y0 + (int)((i + 1) * (m.alto() - y0) / (float)(lineas + 1));
            int desplazamiento = (int)(Math.sin(fase) * m.ancho() * 0.025F);
            int largo = Math.max(14, m.ancho() / (5 + i % 3));
            int x = Math.floorMod((int)(Trazo.pseudo(700 + i) * m.ancho()) + desplazamiento, Math.max(1, m.ancho()));
            g.fill(x, y, Math.min(m.ancho(), x + largo), y + 1,
                    Paleta.conAlfa(n.luz, (0.08F + n.reflejo * 0.14F) * luz));
        }
    }

    public static void primerPlanoMarco(GuiGraphics g, Marco m, int color, float alfa,
                                        boolean izquierda, boolean derecha) {
        int w = Math.max(10, m.ancho() / 18);
        if (izquierda) g.fill(0, 0, w, m.alto(), Paleta.conAlfa(color, alfa));
        if (derecha) g.fill(m.ancho() - w, 0, m.ancho(), m.alto(), Paleta.conAlfa(color, alfa));
    }
}
