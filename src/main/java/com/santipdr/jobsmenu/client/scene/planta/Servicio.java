package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Nivel 2 reconstruido: corredor tecnico denso, caliente y mecanico. */
public final class Servicio implements Planta {
    private static final int TRAMOS = 20;
    public int tramos() { return TRAMOS; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        NuevaEscena.base(g, m, n, luz, TRAMOS, 0.66F);
        for (int k = 0; k < 5; k++) {
            float frac = -0.82F + k * 0.40F;
            for (int y = 0; y < Math.round(m.techoEn(1.0F)); y += 2) {
                float dy = m.dy(y + 1.0F);
                if (dy <= 1.0F) continue;
                int x = Math.round(m.enX(dy, frac));
                int r = Math.max(1, (int)(m.w()*dy*(0.010F + k*0.002F)));
                int color = Paleta.iluminar(Paleta.mezclar(n.junta, k==2 ? n.luz : n.paredAlta, 0.18F), luz);
                g.fill(x-r, y, x+r+1, y+2, color);
            }
        }
        for (int j=3;j<=16;j+=3) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 5.5F) continue;
            int y = Math.round(m.techoEn(dx) + m.h()*dx*0.12F);
            int x0 = Math.round(m.enX(dx,-0.90F));
            int x1 = Math.round(m.enX(dx,0.90F));
            g.fill(x0,y,x1,y+Math.max(2,(int)(m.h()*dx*0.028F)), Paleta.conAlfa(n.junta,0.78F));
        }
        int px = Math.round(m.der(1.0F) - m.anchoEn(1.0F)*0.20F);
        int py = Math.round(m.sueloEn(1.0F)-m.h()*0.75F);
        NuevaEscena.panel(g, px-14, py-24, px+14, py+24,
                Paleta.iluminar(Paleta.mezclar(n.junta,n.paredBaja,0.30F),luz*0.70F),
                Paleta.iluminar(n.paredAlta,luz*0.55F), Paleta.iluminar(n.luz,luz), true);
        NuevaEscena.glow(g, px+8, py-14, 8, 0xFFFFB65E, 0.80F*luz);
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w=Math.max(12,m.ancho()/16);
        g.fill(0,0,w,m.alto(),Paleta.conAlfa(Paleta.VANO,0.72F));
        g.fill(m.ancho()-w/2,0,m.ancho(),m.alto(),Paleta.conAlfa(Paleta.VANO,0.38F));
    }
}
