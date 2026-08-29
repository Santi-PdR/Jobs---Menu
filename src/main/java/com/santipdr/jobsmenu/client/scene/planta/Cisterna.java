package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Cisterna vertical: bosque de columnas, pasarela y agua negra sin fondo visible. */
public final class Cisterna implements Planta {
    @Override public float pisoPresencia() { return .76F; }
    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        g.fill(0, 0, m.ancho(), m.alto(), n.fondo);
        int aguaY = Math.round(m.alto() * .57F);
        g.fill(0, aguaY, m.ancho(), m.alto(), Paleta.iluminar(Paleta.mezclar(n.fondo, n.suelo, .24F), luz * .55F));

        // Columnas nacen fuera de cuadro y desaparecen dentro del agua.
        for (int j = 1; j < 15; j += 2) {
            float d = Trazo.profundidad(j, 17);
            for (int s = -1; s <= 1; s += 2) {
                int x = Math.round(m.lado(s, d, .72F));
                int w = Math.max(3, Math.round(d * 3.5F));
                int y0 = Math.round(m.techoEn(d)), y1 = Math.round(m.sueloEn(d) + m.h() * d * .25F);
                int piedra = Arquitectura.material(n, n.paredAlta, luz, 1 / d, .42F);
                g.fill(x - w, y0, x + w, y1, piedra);
                g.fill(x - w - 2, y0, x + w + 2, y0 + Math.max(2, w / 2), Paleta.iluminar(n.junta, luz * .42F));
                if (y1 > aguaY) Arquitectura.reflejo(g, x, Math.max(aguaY, y1 - 5), m.alto(), w,
                        n.paredAlta, .18F * luz, tiempo, j + s);
            }
        }
        // Grandes arcos transversales en altura dan escala vertical.
        for (int j = 2; j < 12; j += 3) {
            float d = Trazo.profundidad(j, 17);
            int y = Math.round(m.techoEn(d) + m.h() * d * .12F);
            Arquitectura.linea(g, m.izq(d), y, m.centro(d), y - m.h() * d * .20F,
                    Math.max(1, Math.round(d)), Arquitectura.material(n, n.junta, luz, 1 / d, .40F));
            Arquitectura.linea(g, m.centro(d), y - m.h() * d * .20F, m.der(d), y,
                    Math.max(1, Math.round(d)), Arquitectura.material(n, n.junta, luz, 1 / d, .40F));
        }
        // Pasarela lateral con baranda, parcialmente fuera de pantalla.
        int py = Math.round(m.alto() * .68F);
        Arquitectura.trapecio(g, py, m.alto(), 0, m.ancho() * .21F, -30, m.ancho() * .34F,
                Arquitectura.material(n, n.sueloLejos, luz, .10F, .15F));
        Arquitectura.linea(g, 0, py - 25, m.ancho() * .25F, m.alto() * .79F, 2, Paleta.conAlfa(n.junta, .82F));
        for (int i = 0; i < 7; i++) {
            float t = i / 6F; int x = Math.round(m.ancho() * .25F * t), y = Math.round(py - 25 + m.alto() * .11F * t);
            Arquitectura.linea(g, x, y, x, y + 25, 2, Paleta.conAlfa(n.junta, .72F));
        }
        // Focos sumergidos y reflejos breves: el agua sigue siendo negra.
        for (int i = 0; i < 4; i++) {
            int x = Math.round(m.ancho() * (.37F + i * .15F)), y = aguaY + 9 + i % 2 * 7;
            Arquitectura.halo(g, x, y, m.alto() / 14, n.luz, .10F * luz);
            g.fill(x - 3, y, x + 3, y + 2, Paleta.conAlfa(n.luz, .72F * luz));
            Arquitectura.reflejo(g, x, y, m.alto(), 10, n.luz, .25F * luz, tiempo, i + 30);
        }
    }
}
