package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/** Nivel 12 - Comedor de turno: cocina, bandejas y ninguna hora de cierre. */
public final class Comedor implements Planta {
    private static final int TRAMOS = 13;

    @Override
    public int tramos() { return TRAMOS; }

    @Override
    public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        Trazo.fondo(g, m, n, luz, Paleta.mezclar(n.paredAlta, n.niebla, 0.30F), 2.0F);
        Trazo.plano(g, m, true, n.techo, n.niebla, n.niebla, luz, 0.48F);
        Trazo.plano(g, m, false, n.suelo, n.sueloLejos, n.niebla, luz, 0.58F);
        Trazo.transversales(g, m, true, n.techoJunta, n.niebla, luz, TRAMOS, 0.38F);
        Trazo.transversales(g, m, false, n.sueloJunta, n.niebla, luz, TRAMOS, 0.52F);
        Trazo.paredes(g, m, n, luz);
        Trazo.juntasVerticales(g, m, n, luz, TRAMOS, 1.0F, 0.36F);
        azulejos(g, m, n, luz);
        mostradores(g, m, n, luz);
        mesas(g, m, n, luz);
        campanas(g, m, n, luz, tiempo);
    }

    private static void azulejos(GuiGraphics g, Marco m, Nivel n, float luz) {
        int y0 = Math.round(m.techoEn(1.0F));
        int y1 = Math.round(m.sueloEn(1.0F));
        for (int k = 1; k < 6; k++) {
            int y = y0 + (y1 - y0) * k / 6;
            g.fill(Math.round(m.izq(1.0F)), y, Math.round(m.der(1.0F)), y + 1,
                    Paleta.conAlfa(Paleta.iluminar(n.junta, luz * 0.62F), 0.45F));
        }
        for (int k = 1; k < 8; k++) {
            int x = Math.round(m.izq(1.0F)) + Math.round((m.der(1.0F) - m.izq(1.0F)) * k / 8.0F);
            g.fill(x, y0, x + 1, y1, Paleta.conAlfa(n.junta, 0.38F));
        }
    }

    private static void mostradores(GuiGraphics g, Marco m, Nivel n, float luz) {
        for (int signo = -1; signo <= 1; signo += 2) {
            for (int y = Math.round(m.sueloEn(1.0F)); y < m.alto(); y += Trazo.PASO) {
                float d = m.dy(y + 1.0F);
                if (d <= 1.0F) continue;
                float cx = m.lado(signo, d * 0.58F);
                float ancho = m.w() * d * 0.28F;
                g.fill(Math.round(cx - ancho), y, Math.round(cx + ancho), y + Trazo.PASO,
                        Paleta.conAlfa(Paleta.iluminar(n.paredBaja, Trazo.atenuar(luz, 1.0F / d)), 0.88F));
            }
        }
    }

    private static void mesas(GuiGraphics g, Marco m, Nivel n, float luz) {
        for (int j = 3; j <= TRAMOS; j += 2) {
            float d = Trazo.profundidad(j, TRAMOS);
            if (d > 5.5F) continue;
            float lejos = 1.0F / d;
            for (int signo = -1; signo <= 1; signo += 2) {
                float cx = m.lado(signo, d * 0.32F);
                float y = m.sueloEn(d * 0.72F);
                int w = Math.max(3, Math.round(m.w() * d * 0.22F));
                int h = Math.max(1, Math.round(m.h() * d * 0.055F));
                g.fill(Math.round(cx) - w, Math.round(y) - h, Math.round(cx) + w, Math.round(y) + h,
                        Paleta.iluminar(n.techoJunta, Trazo.atenuar(luz, lejos) * 0.86F));
                g.fill(Math.round(cx) - 1, Math.round(y) + h, Math.round(cx) + 1, Math.round(m.sueloEn(d)),
                        Paleta.conAlfa(n.junta, 0.72F));
            }
        }
    }

    private static void campanas(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        for (int j = 3; j <= TRAMOS; j += 3) {
            float d = Trazo.profundidad(j, TRAMOS);
            if (d > 5.0F) continue;
            float cx = m.centro(d);
            float y = m.techoEn(d * 0.60F);
            float w = Math.max(3.0F, m.w() * d * 0.18F);
            float h = Math.max(2.0F, m.h() * d * 0.08F);
            g.fill(Math.round(cx - w), Math.round(y), Math.round(cx + w), Math.round(y + h),
                    Paleta.iluminar(n.techoJunta, luz * 0.70F));
            float pulso = 0.72F + 0.18F * (float) Math.sin(tiempo * 1.8F + j);
            g.fill(Math.round(cx - w * 0.72F), Math.round(y + h), Math.round(cx + w * 0.72F), Math.round(y + h + 1),
                    Paleta.conAlfa(n.luz, pulso * luz));
        }
    }

    @Override
    public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int y = Math.round(m.alto() * 0.80F + PrimerPlano.desvio(tiempo, 2.5F, 0.09F));
        g.fillGradient(0, y, m.ancho(), m.alto(),
                Paleta.iluminar(n.paredBaja, 0.48F + 0.30F * luz),
                Paleta.iluminar(n.fondo, 0.20F + 0.12F * luz));
        int borde = Math.max(3, Math.round(m.alto() * 0.025F));
        g.fill(0, y - borde, m.ancho(), y, Paleta.iluminar(n.techo, 0.58F + 0.30F * luz));
        // Bandeja y vaso abandonados: escala cercana, sin texto ni logo.
        int bx = Math.round(m.ancho() * 0.62F);
        g.fill(bx, y - borde - 7, bx + 58, y - borde - 1, Paleta.conAlfa(n.techoJunta, 0.82F));
        g.fill(bx + 42, y - borde - 25, bx + 52, y - borde - 7,
                Paleta.conAlfa(Paleta.iluminar(n.luz, 0.42F + 0.22F * luz), 0.58F));
    }
}
