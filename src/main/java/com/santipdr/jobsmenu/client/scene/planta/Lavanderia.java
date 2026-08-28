package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/** Nivel 10 - Lavanderia industrial interminable. */
public final class Lavanderia implements Planta {
    private static final int TRAMOS = 16;

    @Override
    public int tramos() {
        return TRAMOS;
    }

    @Override
    public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        Trazo.fondo(g, m, n, luz, Paleta.mezclar(n.paredBaja, n.techo, 0.34F), 1.8F);
        Trazo.plano(g, m, true, n.techo, Paleta.mezclar(n.techo, n.niebla, 0.35F), n.niebla, luz, 0.48F);
        Trazo.plano(g, m, false, n.suelo, n.sueloLejos, n.niebla, luz, 0.62F);
        Trazo.transversales(g, m, true, n.techoJunta, n.niebla, luz, TRAMOS, 0.38F);
        Trazo.transversales(g, m, false, n.sueloJunta, n.niebla, luz, TRAMOS, 0.56F);
        Trazo.paredes(g, m, n, luz);
        Trazo.juntasVerticales(g, m, n, luz, TRAMOS, 1.0F, 0.40F);
        Trazo.manchas(g, m, n, luz, TRAMOS);
        maquinas(g, m, n, luz, tiempo);
        luminarias(g, m, n, luz);
        vapor(g, m, n, luz, tiempo);
    }

    private static void luminarias(GuiGraphics g, Marco m, Nivel n, float luz) {
        for (int j = 3; j <= TRAMOS; j += 2) {
            float d = Trazo.profundidad(j, TRAMOS);
            if (d <= 6.0F) {
                Trazo.luminaria(g, m, n, d, 0.86F, 0.34F, 0.9F, luz);
            }
        }
    }

    private static void maquinas(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        for (int j = 3; j <= TRAMOS; j++) {
            float d = Trazo.profundidad(j, TRAMOS);
            if (d > 6.2F) {
                continue;
            }
            float lejos = Trazo.limitar(1.0F / d, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lejos);
            for (int signo = -1; signo <= 1; signo += 2) {
                float cx = m.lado(signo, d * 0.74F);
                float base = m.sueloEn(d);
                float ancho = Math.max(4.0F, m.w() * d * 0.30F);
                float alto = Math.max(7.0F, m.h() * d * 0.62F);
                int x0 = Math.round(cx - ancho * 0.5F);
                int x1 = Math.round(cx + ancho * 0.5F);
                int y0 = Math.round(base - alto);
                int y1 = Math.round(base);
                int metal = Paleta.iluminar(Trazo.velar(n.paredAlta, n.niebla, lejos, 0.42F), at * 0.82F);
                g.fill(x0, y0, x1, y1, metal);
                g.fill(x0, y0, x1, y0 + Math.max(1, Math.round(alto * 0.15F)),
                        Paleta.iluminar(n.techo, at * 0.72F));

                // Puerta circular sugerida por tres marcos cuadrados cada vez
                // mas pequenos; a esta escala lee como ojo de buey.
                int lado = Math.max(3, Math.round(Math.min(ancho, alto) * 0.58F));
                int px0 = Math.round(cx) - lado / 2;
                int py0 = Math.round(base - alto * 0.47F) - lado / 2;
                g.fill(px0, py0, px0 + lado, py0 + lado,
                        Paleta.conAlfa(Paleta.iluminar(n.junta, at * 0.72F), 0.92F));
                int borde = Math.max(1, lado / 6);
                g.fill(px0 + borde, py0 + borde, px0 + lado - borde, py0 + lado - borde,
                        Paleta.iluminar(Paleta.mezclar(n.fondo, n.luz, 0.12F), at * 0.58F));
                int reflejo = Math.max(1, lado / 8);
                g.fill(px0 + borde + 1, py0 + borde + 1,
                        Math.min(px0 + lado - borde, px0 + borde + 1 + reflejo),
                        Math.min(py0 + lado - borde, py0 + borde + 1 + reflejo),
                        Paleta.conAlfa(n.luz, 0.34F * at));

                // Una maquina por hilera mantiene encendido el piloto ambar.
                if ((j + signo) % 4 == 0) {
                    int piloto = Math.max(1, Math.round(ancho * 0.06F));
                    g.fill(x1 - piloto * 2, y0 + piloto, x1 - piloto, y0 + piloto * 2,
                            Paleta.conAlfa(n.luz, 0.70F + 0.20F * (float) Math.sin(tiempo * 2.0F + j)));
                }
            }
        }
    }

    private static void vapor(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        for (int i = 0; i < 12; i++) {
            float x = (Trazo.pseudo(700 + i * 3) + tiempo * 0.002F * (i % 2 == 0 ? 1 : -1) + 1.0F) % 1.0F;
            float y = (Trazo.pseudo(701 + i * 3) + tiempo * 0.006F) % 1.0F;
            int px = Math.round(x * m.ancho());
            int py = Math.round((0.35F + y * 0.58F) * m.alto());
            int w = 8 + Math.round(Trazo.pseudo(702 + i * 3) * 24.0F);
            g.fill(px - w, py, px + w, py + 2, Paleta.conAlfa(n.niebla, 0.025F * luz));
        }
    }

    @Override
    public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        float vaiven = PrimerPlano.desvio(tiempo, 3.0F, 0.08F);
        int x0 = Math.round(m.ancho() * 0.72F + vaiven);
        int y0 = Math.round(m.alto() * 0.78F);
        g.fillGradient(x0, y0, m.ancho(), m.alto(),
                Paleta.iluminar(n.paredBaja, 0.48F + 0.28F * luz),
                Paleta.iluminar(n.fondo, 0.28F + 0.12F * luz));
        // Canasto de ropa que corta el borde: trama y toalla olvidada.
        for (int x = x0 + 5; x < m.ancho(); x += 12) {
            g.fill(x, y0 + 5, x + 2, m.alto(), Paleta.conAlfa(n.junta, 0.55F));
        }
        g.fill(x0 - 8, y0 - 8, x0 + 34, y0 + 7,
                Paleta.conAlfa(Paleta.iluminar(n.techo, 0.62F + 0.26F * luz), 0.88F));
    }
}
