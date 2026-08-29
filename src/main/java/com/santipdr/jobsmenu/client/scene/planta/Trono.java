package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Salon ceremonial final: eje monumental, abside, graderio, trono y ruina. */
public final class Trono implements Planta {
    @Override public float pisoPresencia() { return .90F; }
    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        g.fill(0, 0, m.ancho(), m.alto(), Arquitectura.material(n, n.paredBaja, luz, .32F, .60F));
        Trazo.plano(g, m, false, n.suelo, n.sueloLejos, n.niebla, luz, .48F);
        int cx = Math.round(m.centro(1)), base = Math.round(m.sueloEn(1));
        int radio = Math.max(22, Math.round(m.anchoEn(1) * .42F));
        Arquitectura.arco(g, cx, Math.round(m.techoEn(1)), base, radio, Math.max(7, radio / 7),
                Arquitectura.material(n, n.paredAlta, luz, 1, .16F), Paleta.conAlfa(n.fondo, .92F));

        // Columnata monumental, mas espaciada y masiva que la cripta.
        for (int j = 2; j < 18; j += 4) {
            float d = Trazo.profundidad(j, 20);
            for (int s = -1; s <= 1; s += 2) {
                int x = Math.round(m.lado(s, d, .82F));
                int w = Math.max(4, Math.round(d * 4.3F));
                int y0 = Math.round(m.techoEn(d) + m.h() * d * .04F), y1 = Math.round(m.sueloEn(d));
                int c = Arquitectura.material(n, n.paredAlta, luz, 1 / d, .34F);
                g.fill(x - w, y0, x + w, y1, c);
                g.fill(x - w - 4, y0, x + w + 4, y0 + Math.max(3, w / 2), Paleta.conAlfa(n.junta, .82F));
                g.fill(x - w - 5, y1 - Math.max(4, w / 2), x + w + 5, y1, Paleta.conAlfa(n.junta, .74F));
            }
        }
        // Alfombra axial y cinco escalones hacen converger la mirada al trono.
        Arquitectura.trapecio(g, base, m.alto(), cx - radio * .22F, cx + radio * .22F,
                m.ancho() * .34F, m.ancho() * .66F, Paleta.conAlfa(0xFF25203A, .82F));
        for (int i = 0; i < 5; i++) {
            int y = base - 3 + i * 5, w = radio / 2 + i * 7;
            g.fill(cx - w, y, cx + w, y + 4, Arquitectura.material(n, n.sueloLejos, luz, .86F, .20F));
            g.fill(cx - w, y, cx + w, y + 1, Paleta.conAlfa(n.luz, .28F * luz));
        }
        // Trono con respaldo coronado, brazos y vacio central fuerte.
        int ty = base - 44, tw = Math.max(12, radio / 4);
        g.fill(cx - tw, ty, cx + tw, base - 8, Paleta.iluminar(n.junta, luz * .72F));
        g.fill(cx - tw + 4, ty + 6, cx + tw - 4, base - 10, Paleta.conAlfa(n.fondo, .92F));
        g.fill(cx - tw - 7, base - 25, cx - tw + 3, base - 8, Paleta.iluminar(n.luz, luz * .52F));
        g.fill(cx + tw - 3, base - 25, cx + tw + 7, base - 8, Paleta.iluminar(n.luz, luz * .52F));
        for (int p = -2; p <= 2; p++) {
            int px = cx + p * (tw / 2 + 2);
            Arquitectura.linea(g, px, ty, px + p * 2, ty - 10 - Math.abs(p) * 2, 2, Paleta.conAlfa(n.luz, .78F * luz));
        }
        // Haz cenital unico y estandartes laterales: foco controlado.
        Arquitectura.trapecio(g, 0, base, cx - 8, cx + 8, cx - radio * .55F, cx + radio * .55F,
                Paleta.conAlfa(n.luz, .075F * luz));
        for (int s = -1; s <= 1; s += 2) {
            int x = Math.round(m.ancho() * (.5F + s * .31F));
            Arquitectura.linea(g, x, 0, x, m.alto() * .18F, 2, Paleta.conAlfa(n.junta, .80F));
            Arquitectura.trapecio(g, Math.round(m.alto() * .18F), Math.round(m.alto() * .50F),
                    x - 16, x + 16, x - 12 + s * 4, x + 12 + s * 4, Paleta.conAlfa(0xFF29233F, .76F));
        }
        Arquitectura.polvo(g, m.ancho(), m.alto(), tiempo * .18F, n.luz, .24F * luz, 2200, 38);
    }
    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        // Fragmentos de columnas cortados por el encuadre: escala y ruina.
        int c = Arquitectura.material(n, n.paredBaja, luz, .05F, .12F);
        g.fill(-18, Math.round(m.alto() * .38F), Math.round(m.ancho() * .07F), m.alto(), c);
        g.fill(Math.round(m.ancho() * .94F), Math.round(m.alto() * .22F), m.ancho() + 20, m.alto(), c);
    }
}
