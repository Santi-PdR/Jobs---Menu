package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Cripta monumental: nave de arcos pesados, altar y fuego lateral. */
public final class Cripta implements Planta {
    @Override public float pisoPresencia() { return .96F; }
    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int piedra = Arquitectura.material(n, n.paredBaja, luz, .45F, .50F);
        g.fill(0, 0, m.ancho(), m.alto(), piedra);
        int base = Math.round(m.sueloEn(1)), cx = Math.round(m.centro(1));
        int radio = Math.max(18, Math.round(m.anchoEn(1) * .34F));
        Arquitectura.arco(g, cx, Math.round(m.techoEn(1)), base, radio, Math.max(5, radio / 6),
                Arquitectura.material(n, n.paredAlta, luz, 1, .18F), n.fondo);
        // Arcadas laterales sucesivas dan ritmo basilical, no repeticion de pasillo.
        for (int j = 2; j < 12; j += 2) {
            float d = Trazo.profundidad(j, 13);
            int yb = Math.round(m.sueloEn(d)), r = Math.max(8, Math.round(m.anchoEn(d) * .12F));
            int c = Arquitectura.material(n, n.paredAlta, luz, 1 / d, .35F);
            for (int s = -1; s <= 1; s += 2) {
                int x = Math.round(m.lado(s, d, .78F));
                Arquitectura.arco(g, x, yb - r * 2, yb, r, Math.max(2, r / 5), c, Paleta.conAlfa(n.fondo, .88F));
            }
        }
        // Altar bajo el abside y losas quebradas hacia la camara.
        int aw = Math.max(16, radio / 2);
        g.fill(cx - aw, base - 9, cx + aw, base, Arquitectura.material(n, n.sueloLejos, luz, .95F, .18F));
        g.fill(cx - aw - 5, base, cx + aw + 5, base + 4, Arquitectura.material(n, n.junta, luz, .85F, .20F));
        for (int j = 1; j < 12; j++) {
            float d = Trazo.profundidad(j, 13);
            Arquitectura.linea(g, m.izq(d), m.sueloEn(d), m.der(d), m.sueloEn(d), 1,
                    Arquitectura.material(n, n.sueloJunta, luz, 1 / d, .32F));
        }
        // Braseros calidos: fuentes puntuales con rebote, no luz uniforme.
        for (int s = -1; s <= 1; s += 2) {
            int bx = Math.round(m.ancho() * (.5F + s * .29F)), by = Math.round(m.alto() * .62F);
            float llama = .85F + .15F * (float) Math.sin(tiempo * 4.1F + s);
            Arquitectura.halo(g, bx, by, m.alto() / 9, n.luz, .22F * luz);
            g.fill(bx - 4, by, bx + 4, by + 8, Paleta.conAlfa(n.luz, .78F * llama * luz));
            Arquitectura.linea(g, bx, by + 8, bx, m.alto(), 3, Arquitectura.material(n, n.junta, luz, .2F, .15F));
        }
        Arquitectura.polvo(g, m.ancho(), m.alto(), tiempo * .45F, n.luz, .18F * luz, 900, 26);
    }
    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int y = Math.round(m.alto() * .83F);
        g.fill(-20, y, Math.round(m.ancho() * .37F), m.alto(), Arquitectura.material(n, n.paredBaja, luz, .08F, .12F));
        Arquitectura.linea(g, -10, y, m.ancho() * .37F, y - 4, 4, Paleta.conAlfa(n.junta, .88F));
    }
}
