package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Nivel 1 reconstruido: nave industrial de gran escala. */
public final class Nave implements Planta {
    private static final int TRAMOS = 14;
    public int tramos() { return TRAMOS; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        NuevaEscena.base(g, m, n, luz, TRAMOS, 0.60F);
        for (int j = 3; j <= 11; j += 2) {
            float dx = Trazo.profundidad(j, 12);
            if (dx > 5.0F) continue;
            NuevaEscena.columna(g, m, n, dx, -0.78F, 0.040F, n.junta, luz);
            NuevaEscena.columna(g, m, n, dx, 0.78F, 0.040F, n.junta, luz);
            int y = Math.round(m.techoEn(dx) + m.h() * dx * 0.18F);
            int x0 = Math.round(m.enX(dx, -0.78F));
            int x1 = Math.round(m.enX(dx, 0.78F));
            g.fill(x0, y, x1, y + Math.max(2, (int)(m.h()*dx*0.035F)),
                    Paleta.iluminar(Paleta.mezclar(n.techoJunta, Paleta.VANO, 0.18F), luz));
        }
        for (int i = 0; i < 5; i++) {
            int x = (int)(m.ancho() * (0.16F + i * 0.17F));
            int y = (int)(m.alto() * (0.28F + (i%2)*0.05F));
            NuevaEscena.glow(g, x, y, Math.max(8, m.ancho()/55), 0xFF65FF70, 0.48F*luz);
        }
        int cx = Math.round(m.fx());
        int base = Math.round(m.sueloEn(1.0F));
        int w = Math.max(18, Math.round(m.anchoEn(1.0F)*0.24F));
        NuevaEscena.panel(g, cx-w, base-(int)(m.h()*1.05F), cx+w, base,
                Paleta.iluminar(Paleta.mezclar(n.paredBaja, Paleta.VANO, 0.30F), luz*0.55F),
                Paleta.iluminar(n.junta, luz*0.72F), Paleta.iluminar(n.luz, luz), true);
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        NuevaEscena.primerPlanoMarco(g, m, Paleta.VANO, 0.60F, true, true);
    }
}
