package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Terminal de carga subterranea: canon de contenedores y puente grua. */
public final class Nave implements Planta {
    @Override public float pisoPresencia() { return .98F; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        int hormigon = Lienzo.tono(n, n.paredBaja, luz, .52F);
        int acero = Lienzo.tono(n, n.junta, luz, .38F);
        Lienzo.fondo(g, w, h, n.fondo, hormigon, 400);

        // Muro lejano gigantesco y cinco darsenas oscuras.
        Lienzo.revoque(g, Math.round(w * .16F), Math.round(h * .18F),
                Math.round(w * .91F), Math.round(h * .67F),
                Lienzo.tono(n, n.paredAlta, luz, .78F), n.junta, 420);
        for (int i = 0; i < 5; i++) {
            int x0 = Math.round(w * (.20F + i * .137F));
            int x1 = Math.round(w * (.30F + i * .137F));
            Lienzo.caja(g, x0, Math.round(h * .34F), x1, Math.round(h * .67F), n.fondo);
            for (int y = Math.round(h * .36F); y < h * .66F; y += Math.max(6, h / 25))
                Lienzo.caja(g, x0, y, x1, y + 1, Paleta.conAlfa(acero, .62F));
        }

        // Patio y vias diagonales: volumen abierto, no corredor central.
        Lienzo.quad(g, Math.round(h * .62F), h, w * .12F, w * .93F,
                -w * .20F, w * 1.12F, Lienzo.tono(n, n.suelo, luz, .28F));
        for (int i = 0; i < 4; i++)
            Lienzo.linea(g, w * (.38F + i * .10F), h * .63F,
                    w * (-.04F + i * .34F), h, Math.max(2, w / 180),
                    Paleta.iluminar(n.sueloJunta, luz * .48F));

        // Torres de contenedores desiguales forman un canon de almacenamiento.
        for (int lado = 0; lado < 2; lado++) {
            int x0 = lado == 0 ? 0 : Math.round(w * .72F);
            int x1 = lado == 0 ? Math.round(w * .31F) : w;
            int base = Math.round(h * (lado == 0 ? .79F : .87F));
            for (int piso = 0; piso < (lado == 0 ? 4 : 3); piso++) {
                int y1 = base - piso * Math.max(22, h / 7);
                int y0 = y1 - Math.max(19, h / 9);
                int deriva = piso * (lado == 0 ? -w / 80 : w / 90);
                int color = Paleta.mezclar(n.paredBaja,
                        piso % 3 == 0 ? 0xFF4E5B50 : 0xFF58606A, .48F);
                Lienzo.metal(g, x0 + deriva, y0, x1 + deriva, y1,
                        Lienzo.tono(n, color, luz, .20F + piso * .08F), acero,
                        0xFF70401F, 470 + lado * 90 + piso * 13);
            }
        }

        int vigaY = Math.round(h * .16F), vigaH = Math.max(9, h / 24);
        Lienzo.metal(g, -10, vigaY, w + 10, vigaY + vigaH, acero,
                n.paredAlta, 0xFF653919, 600);
        for (int x = 18; x < w - 16; x += Math.max(25, w / 11))
            Lienzo.quad(g, vigaY + 3, vigaY + vigaH - 2, x, x + 8, x + 5, x + 13, n.fondo);
        int carroX = Math.round(w * .61F);
        Lienzo.metal(g, carroX - w / 28, vigaY + vigaH, carroX + w / 28,
                vigaY + vigaH + Math.max(13, h / 20), n.paredBaja, acero, 0xFF6A351D, 650);
        Lienzo.linea(g, carroX, vigaY + vigaH + h / 20F, carroX - w * .03F, h * .57F,
                1, Paleta.conAlfa(n.junta, .78F));
        Lienzo.caja(g, carroX - w / 25, Math.round(h * .56F), carroX + w / 45,
                Math.round(h * .59F), Paleta.conAlfa(n.junta, .85F));

        Lienzo.quad(g, vigaY + vigaH, Math.round(h * .72F), w * .35F, w * .39F,
                w * .24F, w * .50F, Paleta.conAlfa(n.luz, .035F * luz));
        Lienzo.quad(g, vigaY + vigaH, Math.round(h * .69F), w * .76F, w * .80F,
                w * .66F, w * .91F, Paleta.conAlfa(n.luz, .028F * luz));
        Lienzo.motas(g, w, h, tiempo, n.luz, .18F * luz, 710, 24);
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        Lienzo.madera(g, -12, Math.round(h * .87F), Math.round(w * .30F), h,
                Paleta.iluminar(0xFF644425, luz * .38F), n.junta, 760);
        Lienzo.linea(g, w * .77F, h, w * .95F, h * .73F, Math.max(5, w / 70),
                Paleta.iluminar(n.junta, luz * .34F));
        Lienzo.linea(g, w * .84F, h, w * 1.02F, h * .73F, Math.max(5, w / 70),
                Paleta.iluminar(n.junta, luz * .28F));
    }
}
