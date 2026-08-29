package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Biblioteca de doble altura con balcon, pasillo central y mesa de consulta. */
public final class Biblioteca implements Planta {
    @Override public float pisoPresencia() { return .95F; }
    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        g.fill(0, 0, m.ancho(), m.alto(), Arquitectura.material(n, n.paredBaja, luz, .4F, .52F));
        Trazo.plano(g, m, false, n.suelo, n.sueloLejos, n.niebla, luz, .50F);
        int fondo0 = Math.round(m.techoEn(1)), fondo1 = Math.round(m.sueloEn(1));
        g.fill(Math.round(m.izq(1)), fondo0, Math.round(m.der(1)), fondo1,
                Arquitectura.material(n, n.paredAlta, luz, 1, .15F));
        int cx = Math.round(m.centro(1)), ventW = Math.max(12, Math.round(m.anchoEn(1) * .18F));
        g.fill(cx - ventW, fondo0 + 5, cx + ventW, fondo1 - 5, Paleta.conAlfa(n.fondo, .88F));

        // Estanterias son volumen: laterales oscuros, baldas, libros irregulares.
        for (int s = -1; s <= 1; s += 2) {
            int x0 = s < 0 ? 0 : Math.round(m.ancho() * .70F);
            int x1 = s < 0 ? Math.round(m.ancho() * .30F) : m.ancho();
            g.fill(x0, Math.round(m.alto() * .08F), x1, m.alto(),
                    Arquitectura.material(n, n.paredBaja, luz, .2F, .22F));
            for (int y = Math.round(m.alto() * .14F); y < m.alto() * .88F; y += 24) {
                g.fill(x0, y, x1, y + 3, Paleta.iluminar(n.junta, luz * .50F));
                for (int x = x0 + 4; x < x1 - 4; x += 5 + (x / 11) % 4) {
                    int h = 9 + Math.floorMod(x * 7 + y, 11);
                    g.fill(x, y - h, x + 3, y, Paleta.conAlfa(Paleta.mezclar(n.paredAlta, n.junta,
                            Trazo.pseudo(x + y)), .78F));
                }
            }
        }
        // Balcon intermedio y escalera movil crean segunda planta reconocible.
        int balcon = Math.round(m.alto() * .42F);
        g.fill(0, balcon, m.ancho(), balcon + 4, Arquitectura.material(n, n.junta, luz, .3F, .25F));
        for (int x = 8; x < m.ancho(); x += 17) g.fill(x, balcon - 13, x + 2, balcon, Paleta.conAlfa(n.junta, .65F));
        Arquitectura.linea(g, m.ancho() * .76F, m.alto() * .16F, m.ancho() * .60F, m.alto() * .82F,
                3, Paleta.conAlfa(n.junta, .86F));
        for (int i = 0; i < 9; i++) {
            float t = i / 8F; int x = Math.round(m.ancho() * (.76F - .16F * t)), y = Math.round(m.alto() * (.16F + .66F * t));
            g.fill(x - 7, y, x + 7, y + 2, Paleta.conAlfa(n.junta, .72F));
        }
        // Lamparas verdes puntuales sobre el eje de lectura.
        for (int j = 2; j < 10; j += 3) {
            float d = Trazo.profundidad(j, 15); int x = Math.round(m.centro(d)), y = Math.round(m.techoEn(d) + 10);
            Arquitectura.linea(g, x, m.techoEn(d), x, y, 1, n.junta);
            Arquitectura.halo(g, x, y, Math.max(8, Math.round(d * 7)), 0xFF87A96B, .12F * luz);
            g.fill(x - Math.max(3, Math.round(d * 3)), y, x + Math.max(3, Math.round(d * 3)), y + 3,
                    Paleta.conAlfa(0xFFA6C881, .62F * luz));
        }
        Arquitectura.polvo(g, m.ancho(), m.alto(), tiempo * .25F, n.luz, .24F * luz, 1200, 34);
    }
    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int y = Math.round(m.alto() * .82F), x0 = Math.round(m.ancho() * .24F), x1 = Math.round(m.ancho() * .77F);
        Arquitectura.trapecio(g, y, m.alto(), x0, x1, x0 - 28, x1 + 35,
                Arquitectura.material(n, n.sueloLejos, luz, .08F, .12F));
        g.fill(x0, y, x1, y + 5, Paleta.iluminar(n.junta, luz * .60F));
        for (int i = 0; i < 5; i++) {
            int px = x0 + 18 + i * 29, py = y - 3 - (i % 2) * 4;
            Arquitectura.linea(g, px, py, px + 22, py + 5, 2, Paleta.conAlfa(Paleta.PAPEL, .45F));
        }
    }
}
