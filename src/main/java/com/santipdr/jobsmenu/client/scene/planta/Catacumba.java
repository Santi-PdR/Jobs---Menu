package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Excavacion funeraria que desciende en zigzag entre nichos irregulares. */
public final class Catacumba implements Planta {
    @Override public float pisoPresencia() { return .88F; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        int piedra = Lienzo.tono(n, n.paredBaja, luz, .44F);
        Lienzo.fondo(g, w, h, n.fondo, piedra, 2200);
        Lienzo.piedra(g, 0, 0, w, h, piedra, n.junta, 2210);

        // Tres rellanos que descienden hacia el fondo derecho. Cada abertura
        // cambia de eje; la profundidad se lee como excavacion, no como tunel.
        int[][] rellanos = {
                {Math.round(w * .06F), Math.round(h * .37F), Math.round(w * .48F), Math.round(h * .70F)},
                {Math.round(w * .34F), Math.round(h * .46F), Math.round(w * .78F), Math.round(h * .75F)},
                {Math.round(w * .63F), Math.round(h * .53F), Math.round(w * .95F), Math.round(h * .79F)}
        };
        for (int i = 0; i < rellanos.length; i++) {
            int[] r = rellanos[i];
            int radio = Math.max(14, (r[2] - r[0]) / 4);
            int cx = (r[0] + r[2]) / 2;
            Lienzo.arco(g, cx, r[1] - radio, r[3], radio, Math.max(5, radio / 4),
                    Lienzo.tono(n, n.paredAlta, luz, .34F + i * .19F), n.fondo);
            if (i < rellanos.length - 1) {
                int nx = (rellanos[i + 1][0] + rellanos[i + 1][2]) / 2;
                int ny = rellanos[i + 1][3];
                for (int paso = 0; paso < 6; paso++) {
                    float t = paso / 5F;
                    int x = Math.round(cx + (nx - cx) * t);
                    int y = Math.round(r[3] + (ny - r[3]) * t + paso * h * .018F);
                    int ancho = Math.max(16, Math.round((r[2] - r[0]) * (.38F - t * .13F)));
                    Lienzo.caja(g, x - ancho, y, x + ancho, y + Math.max(3, h / 42),
                            Lienzo.tono(n, n.sueloLejos, luz, .27F + i * .18F));
                }
            }
        }

        // Nichos escalonados a ambos lados; tapas, huecos y tamanos varian.
        for (int lado = 0; lado < 2; lado++) {
            int inicio = lado == 0 ? 0 : Math.round(w * .73F);
            int fin = lado == 0 ? Math.round(w * .31F) : w;
            for (int fila = 0; fila < 4; fila++) {
                int y0 = Math.round(h * (.12F + fila * .135F));
                int y1 = y0 + Math.max(14, h / 11);
                for (int col = 0; col < 3; col++) {
                    int x0 = inicio + 4 + col * Math.max(15, (fin - inicio - 8) / 3);
                    int x1 = Math.min(fin - 3, x0 + Math.max(12, (fin - inicio - 15) / 3));
                    boolean cerrado = Math.floorMod(fila * 5 + col * 3 + lado, 4) == 0;
                    Lienzo.caja(g, x0, y0, x1, y1,
                            cerrado ? Paleta.iluminar(n.paredAlta, luz * .34F) : n.fondo);
                    Lienzo.caja(g, x0 - 2, y0 - 2, x1 + 2, y0,
                            Paleta.conAlfa(n.junta, .72F));
                    if (!cerrado && (fila + col) % 2 == 0)
                        Lienzo.linea(g, x0 + 3, y1 - 4, x1 - 3, y1 - 4, 2,
                                Paleta.conAlfa(0xFFC0B79D, .22F * luz));
                }
            }
        }

        // Unico farol en el primer rellano; el descenso final queda negro.
        int lx = Math.round(w * .38F), ly = Math.round(h * .42F);
        Lienzo.linea(g, lx, 0, lx, ly, 1, Paleta.conAlfa(n.junta, .74F));
        Lienzo.halo(g, lx, ly, Math.max(18, h / 8), n.luz, .13F * luz);
        Lienzo.caja(g, lx - 3, ly, lx + 4, ly + Math.max(7, h / 27),
                Paleta.conAlfa(n.luz, .62F * luz));
        Lienzo.motas(g, w, h, tiempo * .14F, n.luz, .11F * luz, 2350, 9);
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        // Arco inmediato cortado por camara; obliga a mirar desde dentro.
        int material = Paleta.iluminar(n.paredBaja, luz * .22F);
        Lienzo.linea(g, -w * .04F, 0, w * .10F, h, Math.max(20, w / 18), material);
        Lienzo.linea(g, w * 1.02F, h * .11F, w * .91F, h, Math.max(16, w / 22), material);
    }
}
