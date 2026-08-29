package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Conservatorio colapsado alrededor de un arbol que atraveso la cubierta. */
public final class Invernadero implements Planta {
    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        int hierro = Lienzo.tono(n, n.junta, luz, .38F);
        Lienzo.fondo(g, w, h, n.fondo, Lienzo.tono(n, n.niebla, luz, .55F), 1800);

        // Cupula acristalada asimetrica. Faltan panos completos y entra luz
        // natural por una rotura; no es el antiguo techo a dos aguas.
        int cx = Math.round(w * .43F), base = Math.round(h * .61F);
        int radio = Math.max(65, Math.round(Math.min(w * .49F, h * .67F)));
        Lienzo.circulo(g, cx, base, radio, Paleta.conAlfa(n.techo, .48F));
        Lienzo.circulo(g, cx, base, radio - Math.max(5, radio / 18),
                Paleta.conAlfa(0xFF496154, .32F));
        for (int i = 0; i < 11; i++) {
            double a = Math.PI + i * Math.PI / 10.0;
            Lienzo.linea(g, cx, base,
                    cx + Math.round((float) Math.cos(a) * radio),
                    base + Math.round((float) Math.sin(a) * radio),
                    Math.max(2, radio / 45), hierro);
        }
        for (int i = 1; i < 5; i++) {
            int rr = Math.round(radio * i / 5F);
            Lienzo.anillo(g, cx, base, rr, Math.max(1, radio / 90),
                    Paleta.conAlfa(hierro, .68F), Paleta.conAlfa(0xFF496154, .08F));
        }
        // Boquete de la cubierta y su haz de luz filtrada.
        Lienzo.quad(g, 0, Math.round(h * .42F), w * .57F, w * .76F,
                w * .45F, w * .78F, Paleta.conAlfa(n.luz, .055F * luz));
        Lienzo.quad(g, Math.round(h * .02F), Math.round(h * .18F), w * .56F, w * .74F,
                w * .50F, w * .80F, n.fondo);
        for (int i = 0; i < 9; i++) {
            int x = Math.round(w * (.51F + Trazo.pseudo(1840 + i) * .29F));
            int y = Math.round(h * (.02F + Trazo.pseudo(1860 + i) * .18F));
            Lienzo.linea(g, x, y, x + (i % 3 - 1) * w * .035F, y + h * .08F,
                    1, Paleta.conAlfa(n.luz, .28F));
        }

        // Arbol central: tronco ramificado y raices que levantan el pavimento.
        int troncoX = Math.round(w * .45F), troncoY = Math.round(h * .74F);
        int corteza = Paleta.iluminar(0xFF3A2E1C, luz * .56F);
        Lienzo.linea(g, troncoX, troncoY, troncoX + w * .03F, h * .23F,
                Math.max(12, w / 32), corteza);
        Lienzo.linea(g, troncoX + w * .02F, h * .49F, w * .27F, h * .30F,
                Math.max(7, w / 55), corteza);
        Lienzo.linea(g, troncoX + w * .04F, h * .42F, w * .69F, h * .25F,
                Math.max(6, w / 62), corteza);
        Lienzo.linea(g, troncoX + w * .02F, h * .56F, w * .78F, h * .48F,
                Math.max(5, w / 75), corteza);
        for (int i = 0; i < 42; i++) {
            int x = Math.round(w * (.18F + Trazo.pseudo(1900 + i * 5) * .65F));
            int y = Math.round(h * (.16F + Trazo.pseudo(1901 + i * 5) * .41F));
            int r = 3 + i % Math.max(4, h / 38);
            int verde = Paleta.mezclar(0xFF172611, n.paredAlta,
                    Trazo.pseudo(1902 + i * 5) * .65F);
            Lienzo.circulo(g, x, y, r, Paleta.conAlfa(verde, .72F));
        }

        // Pavimento radial quebrado y bancales absorbidos por las raices.
        Lienzo.quad(g, Math.round(h * .60F), h, 0, w, w * .12F, w * .86F,
                Lienzo.tono(n, n.suelo, luz, .27F));
        for (int i = 0; i < 10; i++) {
            int x = Math.round(w * (.05F + i * .095F));
            Lienzo.linea(g, cx, base, x, h, 1, Paleta.conAlfa(n.sueloJunta, .38F));
        }
        for (int i = 0; i < 7; i++) {
            int x = troncoX + Math.round((Trazo.pseudo(2050 + i) - .5F) * w * .58F);
            Lienzo.linea(g, troncoX, troncoY, x, h,
                    Math.max(3, w / (70 + i * 7)), Paleta.conAlfa(corteza, .90F));
        }
        for (int lado = 0; lado < 2; lado++) {
            int x0 = lado == 0 ? 0 : Math.round(w * .73F);
            int x1 = lado == 0 ? Math.round(w * .26F) : w;
            Lienzo.madera(g, x0, Math.round(h * .67F), x1, Math.round(h * .84F),
                    Paleta.iluminar(n.sueloLejos, luz * .38F), n.junta, 2100 + lado * 40);
        }

        // Condensacion sobre la cupula y hojas lentas cerca del haz.
        for (int i = 0; i < 20; i++) {
            float fase = (Trazo.pseudo(2140 + i) + tiempo * (.0015F + i % 4 * .0004F)) % 1F;
            int x = Math.round(w * (.08F + Trazo.pseudo(2160 + i) * .78F));
            int y = Math.round(h * (.04F + fase * .42F));
            Lienzo.caja(g, x, y, x + 1, y + 2 + i % 6,
                    Paleta.conAlfa(n.luz, .16F * luz));
        }
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        // Hojas fuera de foco recortan ambos extremos sin cerrar el centro.
        for (int i = 0; i < 7; i++) {
            int y = Math.round(h * (.55F + i * .07F));
            Lienzo.linea(g, -8, h, w * (.08F + i * .018F), y,
                    Math.max(4, h / 45), Paleta.iluminar(0xFF10200E, luz * .28F));
            Lienzo.circulo(g, Math.round(w * (.08F + i * .018F)), y,
                    Math.max(4, h / 24), Paleta.iluminar(0xFF183018, luz * .34F));
        }
    }
}
