package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Natatorio amplio construido alrededor del agua, con ventanales y graderia. */
public final class Natatorio implements Planta {
    @Override public float pisoPresencia() { return .80F; }
    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        g.fill(0, 0, m.ancho(), m.alto(), Arquitectura.material(n, n.paredAlta, luz, .55F, .35F));
        int borde = Math.round(m.alto() * .47F);
        // Muro de azulejo con junta y ventanales altos que reflejan sobre el vaso.
        for (int y = 0; y < borde; y += 10) g.fill(0, y, m.ancho(), y + 1, Paleta.conAlfa(n.junta, .26F));
        for (int i = 0; i < 5; i++) {
            int wx = Math.round(m.ancho() * (.38F + i * .11F));
            int ww = Math.max(10, m.ancho() / 22);
            g.fill(wx, Math.round(m.alto() * .09F), wx + ww, Math.round(m.alto() * .34F),
                    Paleta.conAlfa(n.fondo, .78F));
            g.fill(wx + 2, Math.round(m.alto() * .11F), wx + ww - 2, Math.round(m.alto() * .32F),
                    Paleta.conAlfa(n.luz, .18F * luz));
        }
        // Graderia lateral escalonada: masa seca contra el agua.
        for (int s = 0; s < 5; s++) {
            int y = borde - s * 11;
            g.fill(0, y, Math.round(m.ancho() * (.25F - s * .025F)), y + 10,
                    Arquitectura.material(n, n.techo, luz, .45F, .30F));
        }
        // Vaso trapezoidal ocupa el cuadro inferior; las juntas y calles convergen.
        int agua = Paleta.mezclar(n.suelo, n.fondo, .34F);
        Arquitectura.trapecio(g, borde, m.alto(), m.ancho() * .26F, m.ancho() * .96F,
                m.ancho() * .08F, m.ancho() * 1.08F, Paleta.iluminar(agua, luz * .78F));
        for (int c = 0; c < 5; c++) {
            float f = c / 4F;
            float xt = m.ancho() * (.30F + .62F * f);
            float xb = m.ancho() * (.12F + .90F * f);
            Arquitectura.linea(g, xt, borde, xb, m.alto(), 1, Paleta.conAlfa(n.luz, .40F * luz));
        }
        for (int y = borde + 8; y < m.alto(); y += 14) {
            int deriva = Math.round((float) Math.sin(tiempo * .45F + y * .11F) * 6);
            g.fill(Math.round(m.ancho() * .27F) + deriva, y, Math.round(m.ancho() * .94F) + deriva, y + 1,
                    Paleta.conAlfa(n.luz, .12F * luz));
        }
        for (int i = 0; i < 5; i++) {
            int wx = Math.round(m.ancho() * (.38F + i * .11F));
            Arquitectura.reflejo(g, wx + m.ancho() / 44, borde, m.alto(), m.ancho() / 40,
                    n.luz, .22F * luz, tiempo, i);
        }
        // Escalerilla cromada en primer plano y condensacion lenta.
        int ex = Math.round(m.ancho() * .78F);
        Arquitectura.linea(g, ex, borde - 12, ex - 8, Math.round(m.alto() * .82F), 3, Paleta.conAlfa(0xFFD9E8E6, .72F));
        Arquitectura.linea(g, ex + 18, borde - 12, ex + 9, Math.round(m.alto() * .82F), 3, Paleta.conAlfa(0xFFD9E8E6, .72F));
        for (int y = borde; y < m.alto() * .78F; y += 12) g.fill(ex - 5, y, ex + 14, y + 2, Paleta.conAlfa(0xFFD9E8E6, .52F));
        Arquitectura.polvo(g, m.ancho(), borde, tiempo * .3F, n.luz, .13F * luz, 600, 18);
    }
}
