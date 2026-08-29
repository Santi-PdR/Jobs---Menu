package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Archivo circular de varias plantas alrededor de un pozo de lectura. */
public final class Biblioteca implements Planta {
    @Override public float pisoPresencia() { return .93F; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        int madera = Lienzo.tono(n, n.paredBaja, luz, .40F);
        int laton = Paleta.iluminar(0xFF8B733F, luz * .65F);
        Lienzo.fondo(g, w, h, n.fondo, madera, 1400);

        // Torre curva: bandas de estanterias siguen arcos, no dos paredes rectas.
        int cx = Math.round(w * .48F), centroY = Math.round(h * .56F);
        int rExterior = Math.max(80, Math.round(Math.min(w * .48F, h * .72F)));
        Lienzo.circulo(g, cx, centroY, rExterior, madera);
        Lienzo.circulo(g, cx, centroY, Math.round(rExterior * .36F), n.fondo);
        for (int piso = 0; piso < 3; piso++) {
            int y0 = Math.round(h * (.08F + piso * .20F));
            int y1 = y0 + Math.max(29, h / 6);
            // Celdas no identicas: anchuras y alturas varian en torno al anillo.
            for (int i = 0; i < 11; i++) {
                int x0 = Math.round(w * (-.04F + i * .098F));
                int x1 = x0 + Math.max(21, w / 11);
                int recorte = Math.abs(x0 + (x1 - x0) / 2 - cx) / Math.max(1, w / 45);
                int arriba = y0 + Math.min(h / 16, recorte);
                Lienzo.madera(g, x0, arriba, x1, y1, madera, n.junta,
                        1450 + piso * 150 + i * 13);
                for (int y = arriba + Math.max(9, h / 25); y < y1; y += Math.max(10, h / 22))
                    Lienzo.caja(g, x0 + 2, y, x1 - 2, y + 2, Paleta.conAlfa(n.junta, .72F));
                for (int libro = 0; libro < 5; libro++) {
                    int bx = x0 + 4 + libro * Math.max(3, (x1 - x0 - 8) / 5);
                    int bh = 5 + Math.floorMod(i * 7 + libro * 5 + piso, Math.max(6, h / 24));
                    int color = Paleta.mezclar(0xFF40251E, 0xFF556044,
                            Trazo.pseudo(1500 + piso * 90 + i * 8 + libro));
                    Lienzo.caja(g, bx, y1 - bh - 4, bx + Math.max(2, w / 210), y1 - 3,
                            Paleta.conAlfa(color, .82F));
                }
            }
            int balconY = y1;
            Lienzo.caja(g, 0, balconY, w, balconY + Math.max(4, h / 42), laton);
            for (int x = 7; x < w; x += Math.max(13, w / 28))
                Lienzo.linea(g, x, balconY - h * .05F, x, balconY, 1,
                        Paleta.conAlfa(laton, .72F));
        }

        // Hueco central descendente y pasarela anular en primer plano medio.
        int pozoY = Math.round(h * .71F), pozoR = Math.max(35, w / 8);
        Lienzo.circulo(g, cx, pozoY, pozoR, Paleta.iluminar(n.junta, luz * .38F));
        Lienzo.circulo(g, cx, pozoY, Math.round(pozoR * .72F), n.fondo);
        for (int i = 0; i < 5; i++) {
            int rr = Math.round(pozoR * (.72F - i * .10F));
            if (rr > 2) Lienzo.anillo(g, cx, pozoY + i * Math.max(4, h / 35), rr, 2,
                    Paleta.conAlfa(n.junta, .55F - i * .07F), n.fondo);
        }

        // Escalera helicoidal: segmentos cambian de lado y cota alrededor del vacio.
        for (int i = 0; i < 14; i++) {
            double a = i * .48 + tiempo * .006;
            int x = cx + Math.round((float) Math.cos(a) * pozoR * (.78F + i * .012F));
            int y = pozoY + Math.round((float) Math.sin(a) * pozoR * .28F) - i * Math.max(2, h / 75);
            Lienzo.linea(g, x - w * .035F, y, x + w * .035F, y + 2, 2, laton);
        }

        // Lampara central suspendida sobre el pozo.
        int lamparaY = Math.round(h * .38F);
        Lienzo.linea(g, cx, 0, cx, lamparaY, 1, laton);
        Lienzo.halo(g, cx, lamparaY, Math.max(20, h / 7), 0xFF93B377, .12F * luz);
        Lienzo.caja(g, cx - w / 34, lamparaY, cx + w / 34, lamparaY + Math.max(5, h / 35),
                Paleta.conAlfa(0xFF92B478, .66F * luz));
        Lienzo.motas(g, w, h, tiempo * .23F, n.luz, .22F * luz, 1640, 30);
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        // Atril inclinado y legajo abierto, apenas dentro del cuadro.
        Lienzo.quad(g, Math.round(h * .78F), h, w * .73F, w * 1.04F,
                w * .61F, w * 1.12F, Lienzo.tono(n, n.sueloLejos, luz, .08F));
        Lienzo.quad(g, Math.round(h * .75F), Math.round(h * .86F), w * .73F, w * .94F,
                w * .68F, w * 1.00F, Paleta.conAlfa(Paleta.PAPEL, .58F));
        Lienzo.linea(g, w * .84F, h * .76F, w * .86F, h * .85F, 1,
                Paleta.conAlfa(n.junta, .70F));
    }
}
