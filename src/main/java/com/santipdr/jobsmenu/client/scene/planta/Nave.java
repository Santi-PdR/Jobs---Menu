package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Nave de carga monumental con cerchas, puente grua y almacen vertical. */
public final class Nave implements Planta {
    @Override public float pisoPresencia() { return 0.98F; }

    @Override
    public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        g.fill(0, 0, m.ancho(), m.alto(), Arquitectura.material(n, n.paredBaja, luz, 0.4F, 0.6F));
        int horizonte = Math.round(m.fy());
        Arquitectura.trapecio(g, horizonte, m.alto(), m.izq(1), m.der(1), 0, m.ancho(),
                Arquitectura.material(n, n.suelo, luz, 0.38F, 0.55F));
        g.fill(Math.round(m.izq(1)), Math.round(m.techoEn(1)), Math.round(m.der(1)),
                Math.round(m.sueloEn(1)), Arquitectura.material(n, n.paredAlta, luz, 1, 0.18F));

        int cx = Math.round(m.centro(1)), pw = Math.round(m.anchoEn(1) * 0.42F);
        int py0 = Math.round(m.fy() - m.h() * 0.52F), py1 = Math.round(m.sueloEn(1));
        g.fill(cx - pw, py0, cx + pw, py1, n.fondo);
        for (int y = py0 + 4; y < py1; y += 7) g.fill(cx - pw, y, cx + pw, y + 1, Paleta.conAlfa(n.junta, .55F));

        for (int j = 1; j < 15; j += 2) {
            float d = Trazo.profundidad(j, 18);
            float izq = m.izq(d), der = m.der(d), alero = m.techoEn(d) + m.h() * d * .23F;
            float cumbrera = m.techoEn(d) - m.h() * d * .10F;
            int c = Arquitectura.material(n, n.junta, luz, 1 / d, .38F);
            Arquitectura.linea(g, izq, alero, m.centro(d), cumbrera, Math.max(1, Math.round(d * .8F)), c);
            Arquitectura.linea(g, m.centro(d), cumbrera, der, alero, Math.max(1, Math.round(d * .8F)), c);
            Arquitectura.linea(g, izq, alero, der, alero, 1, c);
        }
        for (int j = 2; j < 16; j += 3) {
            float d = Trazo.profundidad(j, 18);
            int w = Math.max(2, Math.round(d * 2.2F));
            int y0 = Math.round(m.techoEn(d) + m.h() * d * .16F), y1 = Math.round(m.sueloEn(d));
            for (int s = -1; s <= 1; s += 2) {
                int x = Math.round(m.lado(s, d, .93F));
                g.fill(x - w, y0, x + w, y1, Arquitectura.material(n, n.junta, luz, 1 / d, .36F));
            }
        }
        int gy = Math.round(m.alto() * .18F);
        g.fill(0, gy, m.ancho(), gy + 8, Arquitectura.material(n, n.junta, luz, .22F, .20F));
        int carro = Math.round(m.ancho() * .63F);
        g.fill(carro - 12, gy + 8, carro + 12, gy + 20, Arquitectura.material(n, n.paredBaja, luz, .2F, .15F));
        Arquitectura.linea(g, carro, gy + 20, carro, Math.round(m.alto() * .52F), 1, Paleta.conAlfa(n.junta, .75F));
        Arquitectura.polvo(g, m.ancho(), m.alto(), tiempo, n.luz, .16F * luz, 300, 30);
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int c = Arquitectura.material(n, n.junta, luz, .08F, .15F);
        g.fill(-8, 0, Math.round(m.ancho() * .055F), m.alto(), c);
        g.fill(Math.round(m.ancho() * .91F), Math.round(m.alto() * .55F), m.ancho() + 8, m.alto(), c);
    }
}
