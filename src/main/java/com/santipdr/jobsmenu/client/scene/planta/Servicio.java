package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Galeria tecnica claustrofobica: tuberias, calor, valvulas y un codo ciego. */
public final class Servicio implements Planta {
    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        g.fill(0, 0, m.ancho(), m.alto(), Arquitectura.material(n, n.paredBaja, luz, .35F, .60F));
        Trazo.paredes(g, m, n, luz);
        Trazo.plano(g, m, false, n.suelo, n.sueloLejos, n.niebla, luz, .60F);
        int x0 = Math.round(m.izq(1)), x1 = Math.round(m.der(1));
        int y0 = Math.round(m.techoEn(1)), y1 = Math.round(m.sueloEn(1));
        g.fill(x0, y0, x1, y1, n.fondo);
        // El corredor dobla bruscamente a la izquierda en el fondo.
        g.fill(x0 - Math.round(m.anchoEn(1) * .65F), y0 + 5, Math.round(m.centro(1)), y1, n.fondo);
        g.fill(Math.round(m.centro(1)), y0 + 5, x1, y1,
                Arquitectura.material(n, n.paredAlta, luz, .95F, .22F));

        // Hazes de canerias a distintas cotas, con abrazaderas y bajantes.
        for (int p = 0; p < 5; p++) {
            float frac = .08F + p * .105F;
            int py = Math.round(m.alto() * frac);
            int radio = 3 + p % 2;
            int metal = Arquitectura.material(n, p % 2 == 0 ? n.junta : n.paredAlta, luz, .22F, .25F);
            Arquitectura.linea(g, -10, py, Math.round(m.ancho() * .72F), py + p * 3, radio, metal);
            Arquitectura.linea(g, Math.round(m.ancho() * .72F), py + p * 3,
                    Math.round(m.ancho() * .72F), Math.round(m.alto() * (.42F + p * .05F)), radio, metal);
            for (int x = 24 + p * 11; x < m.ancho() * .68F; x += 54) {
                g.fill(x, py - radio - 2, x + 2, py + radio + 2, Paleta.conAlfa(n.techoJunta, .75F));
            }
        }
        // Tablero de mantenimiento y manometros, cerca del ojo.
        int panelX = Math.round(m.ancho() * .76F), panelY = Math.round(m.alto() * .29F);
        g.fill(panelX, panelY, m.ancho(), Math.round(m.alto() * .75F),
                Arquitectura.material(n, n.sueloLejos, luz, .16F, .18F));
        for (int i = 0; i < 3; i++) {
            int cy = panelY + 22 + i * 31;
            Arquitectura.circulo(g, panelX + 25, cy, 10, Paleta.iluminar(n.techo, luz * .52F));
            Arquitectura.linea(g, panelX + 25, cy, panelX + 21 + i * 3, cy - 6, 1, n.fondo);
        }
        // Valvula frontal: lectura inmediata de sala de servicio.
        int vx = Math.round(m.ancho() * .18F), vy = Math.round(m.alto() * .67F), vr = Math.max(12, m.alto() / 14);
        Arquitectura.circulo(g, vx, vy, vr, Paleta.conAlfa(n.junta, .92F));
        Arquitectura.circulo(g, vx, vy, vr - 4, Paleta.conAlfa(n.paredBaja, .94F));
        Arquitectura.linea(g, vx - vr, vy, vx + vr, vy, 2, n.junta);
        Arquitectura.linea(g, vx, vy - vr, vx, vy + vr, 2, n.junta);

        float vapor = .5F + .5F * (float) Math.sin(tiempo * .19F);
        for (int i = 0; i < 12; i++) {
            int sx = Math.round(m.ancho() * .57F + (Trazo.pseudo(800 + i) - .5F) * m.ancho() * .15F);
            int sy = Math.round(m.alto() * (.62F - (i / 12F + tiempo * .012F) % 1F * .36F));
            g.fill(sx, sy, sx + 3 + i % 4, sy + 2, Paleta.conAlfa(n.niebla, (.03F + .08F * vapor) * luz));
        }
    }
}
