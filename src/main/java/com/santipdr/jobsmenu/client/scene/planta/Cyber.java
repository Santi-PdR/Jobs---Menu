package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/** Nivel 11 - Cyber: centro de datos y cabinas que nunca cierran sesion. */
public final class Cyber implements Planta {
    private static final int TRAMOS = 18;

    @Override
    public int tramos() { return TRAMOS; }

    @Override
    public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        Trazo.fondo(g, m, n, luz, Paleta.mezclar(n.fondo, n.paredBaja, 0.28F), 1.4F);
        Trazo.plano(g, m, true, n.techo, n.niebla, n.niebla, luz, 0.72F);
        Trazo.plano(g, m, false, n.suelo, n.sueloLejos, n.niebla, luz, 0.70F);
        Trazo.transversales(g, m, true, n.techoJunta, n.niebla, luz, TRAMOS, 0.30F);
        Trazo.transversales(g, m, false, n.sueloJunta, n.niebla, luz, TRAMOS, 0.48F);
        Trazo.paredes(g, m, n, luz);
        racks(g, m, n, luz, tiempo);
        bandejas(g, m, n, luz);
        terminalFondo(g, m, n, luz, tiempo);
    }

    private static void racks(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        for (int j = 2; j <= TRAMOS; j++) {
            float d = Trazo.profundidad(j, TRAMOS);
            if (d > 6.5F) continue;
            float lejos = Trazo.limitar(1.0F / d, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lejos);
            for (int signo = -1; signo <= 1; signo += 2) {
                float cx = m.lado(signo, d * 0.70F);
                float y1 = m.sueloEn(d);
                float y0 = m.techoEn(d * 0.82F);
                float medio = Math.max(3.0F, m.w() * d * 0.25F);
                int x0 = Math.round(cx - medio);
                int x1 = Math.round(cx + medio);
                int top = Math.round(y0);
                int bottom = Math.round(y1);
                g.fillGradient(x0, top, x1, bottom,
                        Paleta.iluminar(n.paredAlta, at * 0.70F),
                        Paleta.iluminar(n.paredBaja, at * 0.46F));
                int unidades = 9;
                for (int u = 1; u < unidades; u++) {
                    int y = top + (bottom - top) * u / unidades;
                    g.fill(x0 + 1, y, x1 - 1, y + 1, Paleta.conAlfa(n.junta, 0.62F));
                    if ((u + j + signo) % 3 == 0) {
                        int led = Math.max(1, Math.round(medio * 0.08F));
                        float pulso = 0.48F + 0.34F * (float) Math.sin(tiempo * (1.4F + u * 0.05F) + j + u);
                        g.fill(signo < 0 ? x1 - led * 2 : x0 + led,
                                y - led, signo < 0 ? x1 - led : x0 + led * 2, y,
                                Paleta.conAlfa(n.luz, pulso * at));
                    }
                }
                g.fill(x0, top, x0 + 2, bottom, Paleta.conAlfa(n.techoJunta, 0.72F));
                g.fill(x1 - 2, top, x1, bottom, Paleta.conAlfa(n.fondo, 0.80F));
            }
        }
    }

    private static void bandejas(GuiGraphics g, Marco m, Nivel n, float luz) {
        for (int signo = -1; signo <= 1; signo += 2) {
            for (int x = 0; x < m.ancho(); x += 2) {
                float d = m.dx(x + 1.0F);
                if (d <= 1.0F || d > 7.0F) continue;
                float esperado = m.lado(signo, d * 0.38F);
                if (Math.abs(x - esperado) > 2.0F) continue;
                int y = Math.round(m.techoEn(d * 0.58F));
                g.fill(x, y, x + 2, y + 3,
                        Paleta.conAlfa(Paleta.iluminar(n.junta, Trazo.atenuar(luz, 1.0F / d)), 0.86F));
            }
        }
    }

    private static void terminalFondo(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = Math.max(8, Math.round(m.w() * 0.44F));
        int h = Math.max(5, Math.round(m.h() * 0.42F));
        int cx = Math.round(m.fx());
        int cy = Math.round(m.fy());
        g.fill(cx - w, cy - h, cx + w, cy + h, Paleta.iluminar(n.fondo, luz * 0.45F));
        int margen = Math.max(1, w / 8);
        int pantalla = Paleta.mezclar(n.fondo, n.luz, 0.30F);
        g.fill(cx - w + margen, cy - h + margen, cx + w - margen, cy + h - margen,
                Paleta.conAlfa(Paleta.iluminar(pantalla, luz), 0.88F));
        int barrido = cy - h + margen + Math.floorMod((int) (tiempo * 8.0F), Math.max(1, h * 2 - margen * 2));
        g.fill(cx - w + margen, barrido, cx + w - margen, barrido + 1,
                Paleta.conAlfa(n.luz, 0.42F * luz));
    }

    @Override
    public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int x1 = Math.round(m.ancho() * 0.27F + PrimerPlano.desvio(tiempo, 4.0F, 0.07F));
        g.fillGradient(0, 0, x1, m.alto(),
                Paleta.iluminar(n.paredAlta, 0.52F + 0.25F * luz),
                Paleta.iluminar(n.fondo, 0.22F + 0.14F * luz));
        for (int y = 24; y < m.alto(); y += 28) {
            g.fill(x1 - 8, y, x1 - 3, y + 2, Paleta.conAlfa(n.luz, 0.25F * luz));
        }
    }
}
