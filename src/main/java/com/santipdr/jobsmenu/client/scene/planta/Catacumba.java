package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Catacumba baja e irregular, con nichos y una bifurcacion que no devuelve luz. */
public final class Catacumba implements Planta {
    @Override public float pisoPresencia() { return .93F; }
    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        g.fill(0, 0, m.ancho(), m.alto(), Arquitectura.material(n, n.paredBaja, luz, .30F, .62F));
        int cx = Math.round(m.centro(1)), base = Math.round(m.sueloEn(1));
        int r = Math.max(15, Math.round(m.anchoEn(1) * .28F));
        Arquitectura.arco(g, cx, Math.round(m.techoEn(1)), base, r, Math.max(4, r / 5),
                Arquitectura.material(n, n.paredAlta, luz, 1, .22F), n.fondo);
        // Ramal lateral visible: rompe el eje y hace que el subsuelo continue.
        int bx = Math.round(m.ancho() * .15F), by = Math.round(m.alto() * .56F);
        Arquitectura.arco(g, bx, by - 32, by + 34, 31, 7,
                Arquitectura.material(n, n.paredAlta, luz, .45F, .32F), n.fondo);

        // Nichos no uniformes, algunos vacios y otros con restos apenas legibles.
        for (int s = -1; s <= 1; s += 2) {
            for (int j = 2; j < 17; j += 3) {
                float d = Trazo.profundidad(j, 19);
                int x = Math.round(m.lado(s, d, .86F)), y = Math.round(m.fy() - m.h() * d * .20F);
                int w = Math.max(5, Math.round(d * 4.2F)), h = Math.max(8, Math.round(d * 7F));
                int oscuro = Paleta.conAlfa(n.fondo, .84F);
                g.fill(x - w, y - h, x + w, y + h, oscuro);
                g.fill(x - w - 2, y - h - 2, x + w + 2, y - h, Paleta.conAlfa(n.junta, .60F));
                if ((j + s) % 4 != 0) {
                    Arquitectura.linea(g, x - w + 2, y + h - 4, x + w - 2, y + h - 4, 2,
                            Paleta.conAlfa(0xFFC0B79D, .28F * luz));
                }
            }
        }
        // Piso de piedra irregular y derrumbe cercano.
        for (int j = 1; j < 18; j += 2) {
            float d = Trazo.profundidad(j, 19);
            Arquitectura.linea(g, m.izq(d), m.sueloEn(d), m.der(d), m.sueloEn(d), 1,
                    Arquitectura.material(n, n.sueloJunta, luz, 1 / d, .38F));
        }
        for (int i = 0; i < 18; i++) {
            int x = Math.round(m.ancho() * (.55F + Trazo.pseudo(1900 + i) * .42F));
            int y = Math.round(m.alto() * (.78F + Trazo.pseudo(1910 + i) * .22F));
            int t = 3 + i % 8;
            g.fill(x, y - t, x + t + 3, y, Arquitectura.material(n, n.paredAlta, luz, .10F, .20F));
        }
        // Un unico farol; el resto queda deliberadamente oscuro.
        int lx = Math.round(m.ancho() * .61F), ly = Math.round(m.alto() * .34F);
        Arquitectura.linea(g, lx, 0, lx, ly, 1, n.junta);
        Arquitectura.halo(g, lx, ly, m.alto() / 10, n.luz, .16F * luz);
        g.fill(lx - 3, ly, lx + 3, ly + 8, Paleta.conAlfa(n.luz, .62F * luz));
    }
}
