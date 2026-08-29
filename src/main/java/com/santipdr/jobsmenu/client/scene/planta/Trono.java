package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Camara ceremonial fracturada: estrado suspendido bajo un oculo inmenso. */
public final class Trono implements Planta {
    @Override public float pisoPresencia() { return .90F; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        int piedra = Lienzo.tono(n, n.paredBaja, luz, .51F);
        int oro = Paleta.iluminar(0xFF9A8245, luz * .72F);
        Lienzo.fondo(g, w, h, n.fondo, piedra, 2700);
        Lienzo.piedra(g, 0, 0, w, h, piedra, n.junta, 2710);

        // Oculo monumental: foco arquitectonico y unica entrada de luz.
        int cx = Math.round(w * .54F), oculoY = Math.round(h * .22F);
        int oculoR = Math.max(36, Math.round(Math.min(w * .19F, h * .25F)));
        Lienzo.halo(g, cx, oculoY, oculoR * 2, n.luz, .11F * luz);
        Lienzo.anillo(g, cx, oculoY, oculoR, Math.max(7, oculoR / 4),
                Lienzo.tono(n, n.paredAlta, luz, .61F), n.fondo);
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0;
            Lienzo.linea(g,
                    cx + Math.round((float) Math.cos(a) * oculoR * .72F),
                    oculoY + Math.round((float) Math.sin(a) * oculoR * .72F),
                    cx + Math.round((float) Math.cos(a) * oculoR),
                    oculoY + Math.round((float) Math.sin(a) * oculoR),
                    2, oro);
        }
        Lienzo.quad(g, oculoY + oculoR / 2, Math.round(h * .80F),
                cx - oculoR * .35F, cx + oculoR * .35F,
                cx - w * .20F, cx + w * .17F, Paleta.conAlfa(n.luz, .055F * luz));

        // Muros ceremoniales escalonados y gigantescos contrafuertes.
        for (int lado = 0; lado < 2; lado++) {
            int x0 = lado == 0 ? 0 : Math.round(w * .73F);
            int x1 = lado == 0 ? Math.round(w * .31F) : w;
            Lienzo.piedra(g, x0, Math.round(h * .10F), x1, Math.round(h * .76F),
                    Lienzo.tono(n, n.paredAlta, luz, .22F + lado * .12F), n.junta,
                    2770 + lado * 70);
            for (int i = 0; i < 3; i++) {
                int x = lado == 0 ? x0 + i * Math.max(18, (x1 - x0) / 3)
                        : x1 - i * Math.max(18, (x1 - x0) / 3);
                Lienzo.linea(g, x, h * .10F, x + (lado == 0 ? w * .06F : -w * .06F),
                        h * .76F, Math.max(7, w / 45),
                        Paleta.iluminar(n.junta, luz * (.24F + i * .08F)));
            }
        }

        // Un abismo separa la camara del estrado; dos puentes rotos casi llegan.
        int abismoY = Math.round(h * .59F);
        Lienzo.quad(g, abismoY, h, w * .13F, w * .90F,
                -w * .08F, w * 1.10F, n.fondo);
        Lienzo.quad(g, Math.round(h * .70F), h, 0, w * .25F,
                -w * .10F, w * .38F, Lienzo.tono(n, n.suelo, luz, .15F));
        Lienzo.quad(g, Math.round(h * .68F), h, w * .79F, w,
                w * .67F, w * 1.10F, Lienzo.tono(n, n.suelo, luz, .12F));

        // Estrado suspendido y trono recortado contra el vacio.
        int estradoY = Math.round(h * .62F), estradoW = Math.max(55, w / 6);
        Lienzo.quad(g, estradoY, estradoY + Math.max(18, h / 12),
                cx - estradoW * .68F, cx + estradoW * .68F,
                cx - estradoW, cx + estradoW,
                Lienzo.tono(n, n.sueloLejos, luz, .67F));
        for (int i = 0; i < 4; i++) {
            int y = estradoY + i * Math.max(4, h / 38);
            int ancho = Math.round(estradoW * (.63F + i * .11F));
            Lienzo.caja(g, cx - ancho, y, cx + ancho, y + Math.max(3, h / 45),
                    Paleta.iluminar(n.junta, luz * (.55F - i * .06F)));
        }
        int tronoY = estradoY - Math.max(38, h / 5), tronoW = Math.max(13, w / 32);
        Lienzo.caja(g, cx - tronoW, tronoY, cx + tronoW, estradoY - 4, oro);
        Lienzo.caja(g, cx - tronoW + 4, tronoY + 7, cx + tronoW - 4, estradoY - 7, n.fondo);
        Lienzo.linea(g, cx - tronoW, tronoY, cx, tronoY - h * .07F, 3, oro);
        Lienzo.linea(g, cx + tronoW, tronoY, cx, tronoY - h * .07F, 3, oro);
        Lienzo.caja(g, cx - tronoW - w / 45, estradoY - h / 13,
                cx - tronoW + 3, estradoY - 4, oro);
        Lienzo.caja(g, cx + tronoW - 3, estradoY - h / 13,
                cx + tronoW + w / 45, estradoY - 4, oro);

        // Estandartes muy altos, rotos en siluetas distintas.
        for (int lado = -1; lado <= 1; lado += 2) {
            int x = Math.round(w * (.54F + lado * .29F));
            Lienzo.linea(g, x, 0, x, h * .17F, 2, oro);
            Lienzo.quad(g, Math.round(h * .16F), Math.round(h * .52F),
                    x - w * .035F, x + w * .035F,
                    x - w * (.025F - lado * .012F), x + w * (.018F + lado * .008F),
                    Paleta.conAlfa(0xFF29233F, .78F));
        }
        Lienzo.motas(g, w, h, tiempo * .16F, n.luz, .20F * luz, 2890, 26);
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        // Columnas quebradas y cadenas cruzan los bordes, nunca el foco central.
        int c = Paleta.iluminar(n.paredBaja, luz * .19F);
        Lienzo.linea(g, -w * .03F, h * .28F, w * .08F, h, Math.max(23, w / 17), c);
        Lienzo.linea(g, w * 1.03F, h * .19F, w * .94F, h, Math.max(21, w / 19), c);
        for (int i = 0; i < 10; i++) {
            int x = Math.round(w * (.02F + i * .026F));
            int y = Math.round(h * (.08F + i * .075F));
            Lienzo.anillo(g, x, y, Math.max(3, h / 55), 2,
                    Paleta.conAlfa(n.junta, .74F), Paleta.conAlfa(n.fondo, .45F));
        }
    }
}
