package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Invernadero de hierro y vidrio, invadido por vegetacion y condensacion. */
public final class Invernadero implements Planta {
    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        g.fill(0, 0, m.ancho(), m.alto(), Arquitectura.material(n, n.niebla, luz, .55F, .20F));
        int sendero = Math.round(m.alto() * .54F);
        Arquitectura.trapecio(g, sendero, m.alto(), m.ancho() * .44F, m.ancho() * .56F,
                m.ancho() * .27F, m.ancho() * .73F, Arquitectura.material(n, n.suelo, luz, .30F, .38F));

        // Cubierta a dos aguas y panos de vidrio translucido.
        int cumX = Math.round(m.ancho() * .50F), cumY = Math.round(m.alto() * .02F);
        int aleroY = Math.round(m.alto() * .39F);
        Arquitectura.trapecio(g, cumY, aleroY, cumX - 2, cumX + 2, 0, cumX,
                Paleta.conAlfa(Paleta.mezclar(n.techo, n.luz, .35F), .48F * luz));
        Arquitectura.trapecio(g, cumY, aleroY, cumX - 2, cumX + 2, cumX, m.ancho(),
                Paleta.conAlfa(Paleta.mezclar(n.techo, n.luz, .28F), .42F * luz));
        Arquitectura.linea(g, cumX, cumY, 0, aleroY, 3, n.junta);
        Arquitectura.linea(g, cumX, cumY, m.ancho(), aleroY, 3, n.junta);
        for (int j = 1; j < 13; j += 2) {
            float d = Trazo.profundidad(j, 14); int y = Math.round(m.techoEn(d));
            Arquitectura.linea(g, m.izq(d), y, m.der(d), y, Math.max(1, Math.round(d * .55F)),
                    Arquitectura.material(n, n.junta, luz, 1 / d, .28F));
        }

        // Bancales elevados: masas verdes a ambos lados, sendero limpio al centro.
        for (int s = -1; s <= 1; s += 2) {
            int bx0 = s < 0 ? 0 : Math.round(m.ancho() * .58F);
            int bx1 = s < 0 ? Math.round(m.ancho() * .42F) : m.ancho();
            int by = Math.round(m.alto() * .64F);
            g.fill(bx0, by, bx1, m.alto(), Arquitectura.material(n, n.sueloLejos, luz, .18F, .20F));
            for (int i = 0; i < 28; i++) {
                int x = bx0 + Math.floorMod(i * 47 + s * 13, Math.max(1, bx1 - bx0));
                int h = 12 + Math.floorMod(i * 19, Math.max(13, m.alto() / 4));
                int verde = Paleta.mezclar(n.paredAlta, 0xFF243718, Trazo.pseudo(1500 + i + s));
                Arquitectura.linea(g, x, by + 5, x + (i % 3 - 1) * 5, by - h, 2, Paleta.conAlfa(verde, .82F));
                Arquitectura.circulo(g, x + (i % 3 - 1) * 5, by - h, 3 + i % 5, Paleta.conAlfa(verde, .72F));
            }
        }
        // Condensacion: gotas que se deslizan por panos concretos, no polvo generico.
        for (int i = 0; i < 24; i++) {
            int x = Math.round(Trazo.pseudo(1700 + i * 7) * m.ancho());
            int y = Math.round(((Trazo.pseudo(1701 + i * 7) + tiempo * (.002F + i % 4 * .0007F)) % 1F) * aleroY);
            int largo = 2 + i % 7;
            g.fill(x, y, x + 1, Math.min(aleroY, y + largo), Paleta.conAlfa(n.luz, .18F * luz));
        }
    }
    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        for (int i = 0; i < 9; i++) {
            int x = Math.round(m.ancho() * (.03F + i * .025F));
            Arquitectura.linea(g, x, m.alto(), x + 12 + i * 2, m.alto() * (.52F - i * .012F),
                    3, Paleta.conAlfa(0xFF17240F, .92F));
        }
    }
}
