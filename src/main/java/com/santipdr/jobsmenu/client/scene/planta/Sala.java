package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Administracion abandonada vista desde la esquina del mostrador. */
public final class Sala implements Planta {

    @Override
    public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int muro = Arquitectura.material(n, n.paredBaja, luz, 0.65F, 0.42F);
        int alto = Arquitectura.material(n, n.paredAlta, luz, 0.78F, 0.30F);
        int suelo = Arquitectura.material(n, n.suelo, luz, 0.45F, 0.50F);
        g.fill(0, 0, m.ancho(), m.alto(), muro);
        Arquitectura.trapecio(g, 0, Math.round(m.fy()), 0, m.ancho(), m.izq(1), m.der(1), alto);
        Arquitectura.trapecio(g, Math.round(m.fy()), m.alto(), m.izq(1), m.der(1), 0, m.ancho(), suelo);
        g.fill(Math.round(m.izq(1)), Math.round(m.techoEn(1)), Math.round(m.der(1)),
                Math.round(m.sueloEn(1)), Arquitectura.material(n, n.paredAlta, luz, 1.0F, 0.12F));

        int fy = Math.round(m.fy()), fondoY = Math.round(m.sueloEn(1));
        int puertaW = Math.max(12, Math.round(m.anchoEn(1) * 0.12F));
        for (int s = -1; s <= 1; s += 2) {
            int cx = Math.round(m.centro(1) + s * m.anchoEn(1) * 0.27F);
            g.fill(cx - puertaW / 2, fy - puertaW, cx + puertaW / 2, fondoY, n.fondo);
            g.fill(cx - puertaW / 2 - 2, fy - puertaW - 2, cx + puertaW / 2 + 2, fy - puertaW,
                    Arquitectura.material(n, n.junta, luz, 1.0F, 0.2F));
        }
        int recepY = fondoY - 8;
        g.fill(Math.round(m.centro(1) - m.anchoEn(1) * 0.30F), recepY,
                Math.round(m.centro(1) + m.anchoEn(1) * 0.08F), fondoY + 7,
                Arquitectura.material(n, n.sueloLejos, luz, 0.95F, 0.25F));

        for (int j = 1; j < 9; j++) {
            float d = Trazo.profundidad(j, 10);
            int y = Math.round(m.techoEn(d));
            Arquitectura.linea(g, m.izq(d), y, m.der(d), y, 1,
                    Arquitectura.material(n, n.techoJunta, luz, 1.0F / d, 0.42F));
            if ((j & 1) == 0) {
                for (int k = 0; k < 2; k++) {
                    float lado = k == 0 ? -0.34F : 0.24F;
                    int x = Math.round(m.enX(d, lado));
                    int w = Math.max(5, Math.round(m.anchoEn(d) * 0.055F));
                    Arquitectura.halo(g, x, y + 2, w * 2, n.luz, 0.10F * luz);
                    g.fill(x - w, y, x + w, y + 2, Paleta.conAlfa(n.luz, 0.75F * luz));
                }
            }
        }
        int armX = Math.round(m.der(2.2F) - m.ancho() * 0.10F);
        int armY = Math.round(m.sueloEn(2.2F) - m.alto() * 0.24F);
        g.fill(armX, armY, armX + Math.round(m.ancho() * 0.11F), m.alto(),
                Arquitectura.material(n, n.junta, luz, 0.35F, 0.25F));
        for (int y = armY + 8; y < m.alto() - 5; y += 13) {
            g.fill(armX + 4, y, armX + Math.round(m.ancho() * 0.10F) - 3, y + 1,
                    Paleta.conAlfa(n.techoJunta, 0.45F));
        }
        Arquitectura.polvo(g, m.ancho(), m.alto(), tiempo, n.luz, 0.20F * luz, 100, 22);
    }

    @Override
    public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int y = Math.round(m.alto() * 0.78F);
        Arquitectura.trapecio(g, y, m.alto(), -20, m.ancho() * 0.48F,
                -60, m.ancho() * 0.56F, Arquitectura.material(n, n.sueloLejos, luz, 0.1F, 0.15F));
        g.fill(0, y, Math.round(m.ancho() * 0.50F), y + 5, Paleta.iluminar(n.junta, luz * 0.65F));
    }
}
