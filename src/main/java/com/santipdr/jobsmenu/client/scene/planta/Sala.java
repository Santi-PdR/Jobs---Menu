package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Nivel 0 reconstruido: vestibulo administrativo monumental. */
public final class Sala implements Planta {
    private static final int TRAMOS = 10;
    public int tramos() { return TRAMOS; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        NuevaEscena.base(g, m, n, luz, TRAMOS, 0.48F);
        int base = Math.round(m.sueloEn(1.0F));
        int cx = Math.round(m.centro(1.0F));
        int anchoArco = Math.max(18, Math.round(m.anchoEn(1.0F) * 0.31F));
        int altoArco = Math.max(22, Math.round(m.h() * 1.22F));
        NuevaEscena.arco(g, cx, base, anchoArco, altoArco, 3,
                Paleta.iluminar(Paleta.mezclar(n.junta, n.paredAlta, 0.28F), luz));

        for (int lado : new int[]{-1, 1}) {
            float frac = lado * 0.68F;
            NuevaEscena.columna(g, m, n, 1.08F, frac, 0.055F,
                    Paleta.mezclar(n.paredBaja, Paleta.VANO, 0.20F), luz);
            int x = Math.round(m.enX(1.08F, frac));
            int y = base - Math.max(12, (int)(m.h() * 0.70F));
            NuevaEscena.glow(g, x, y, Math.max(8, m.ancho()/45), 0xFF65FF6A, 0.88F * luz);
            g.fill(x - 2, y + 6, x + 3, base - 4, Paleta.conAlfa(0xFF57D860, 0.28F * luz));
        }

        for (int j = 2; j <= 7; j++) {
            float dx = Trazo.profundidad(j, 8);
            if (dx > 4.5F) continue;
            int y = Math.round(m.sueloEn(dx) - m.h() * dx * 0.08F);
            int w = Math.max(3, (int)(m.w() * dx * 0.055F));
            int x = Math.round(m.centro(dx));
            g.fill(x - w, y, x + w, y + 2, Paleta.conAlfa(n.luz, 0.20F * luz));
        }
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int h = Math.max(10, m.alto()/16);
        g.fillGradient(0, m.alto()-h, m.ancho(), m.alto(),
                Paleta.conAlfa(Paleta.mezclar(n.suelo, n.junta, 0.30F), 0.82F),
                Paleta.conAlfa(Paleta.VANO, 0.94F));
    }
}
