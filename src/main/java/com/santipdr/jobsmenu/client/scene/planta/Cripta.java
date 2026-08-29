package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Rotonda funeraria radial, organizada alrededor de un relicario vacio. */
public final class Cripta implements Planta {
    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        int piedra = Lienzo.tono(n, n.paredBaja, luz, .48F);
        int junta = Lienzo.tono(n, n.junta, luz, .35F);
        Lienzo.fondo(g, w, h, n.fondo, piedra, 1200);
        Lienzo.piedra(g, 0, 0, w, h, piedra, n.junta, 1210);

        // Tambor circular y casquete de la rotonda, vistos desde un lateral.
        int cx = Math.round(w * .53F), cy = Math.round(h * .34F);
        int radio = Math.max(48, Math.round(Math.min(w * .38F, h * .47F)));
        Lienzo.circulo(g, cx, cy, radio, Lienzo.tono(n, n.paredAlta, luz, .64F));
        Lienzo.circulo(g, cx, cy, radio - Math.max(10, radio / 8),
                Paleta.iluminar(n.fondo, .76F));
        // Nervios radiales y oculo superior.
        for (int i = 0; i < 12; i++) {
            double a = Math.PI + i * Math.PI / 11.0;
            Lienzo.linea(g, cx, cy,
                    cx + Math.round((float) Math.cos(a) * radio),
                    cy + Math.round((float) Math.sin(a) * radio),
                    Math.max(2, radio / 34), junta);
        }
        Lienzo.anillo(g, cx, cy - radio / 3, Math.max(10, radio / 6),
                Math.max(3, radio / 24), junta, n.fondo);

        // Siete capillas en torno al anillo: profundidad radial, no una nave.
        for (int i = 0; i < 7; i++) {
            float f = i / 6F;
            int ax = Math.round(w * (.13F + f * .75F));
            int ay = Math.round(h * (.40F + Math.abs(.5F - f) * .11F));
            int ar = Math.max(14, Math.round(h * (.13F - Math.abs(.5F - f) * .035F)));
            Lienzo.arco(g, ax, ay - ar, Math.round(h * .72F), ar,
                    Math.max(4, ar / 5), Lienzo.tono(n, n.paredAlta, luz, .45F + .25F * f), n.fondo);
            if (i == 1 || i == 5) {
                Lienzo.caja(g, ax - ar / 3, Math.round(h * .61F), ax + ar / 3,
                        Math.round(h * .68F), Paleta.conAlfa(Paleta.PAPEL, .20F));
            }
        }

        // Pavimento concentrico y relicario poligonal central.
        Lienzo.quad(g, Math.round(h * .68F), h, w * .08F, w * .94F,
                -w * .12F, w * 1.10F, Lienzo.tono(n, n.suelo, luz, .27F));
        for (int i = 0; i < 5; i++) {
            int y = Math.round(h * (.71F + i * .055F));
            int ancho = Math.round(w * (.13F + i * .13F));
            Lienzo.caja(g, cx - ancho, y, cx + ancho, y + 1,
                    Paleta.conAlfa(n.sueloJunta, .48F));
        }
        int base = Math.round(h * .78F), rw = Math.max(19, w / 17);
        Lienzo.quad(g, base - h / 14, base, cx - rw * .72F, cx + rw * .72F,
                cx - rw, cx + rw, Lienzo.tono(n, n.sueloLejos, luz, .50F));
        Lienzo.caja(g, cx - rw, base, cx + rw, base + Math.max(6, h / 30),
                Paleta.iluminar(n.junta, luz * .55F));
        Lienzo.caja(g, cx - rw / 2, base - h / 7, cx + rw / 2, base - h / 14,
                Paleta.iluminar(n.fondo, .82F));

        // Cuatro velas: puntos calidos pequenos y asimetricos.
        for (int i = 0; i < 4; i++) {
            int x = cx + Math.round((i - 1.5F) * rw * .52F);
            int y = base - h / 14 - (i % 2) * 3;
            float llama = .82F + .18F * (float) Math.sin(tiempo * 3.7F + i * 1.9F);
            Lienzo.halo(g, x, y, Math.max(8, h / 16), n.luz, .11F * luz);
            Lienzo.caja(g, x - 1, y, x + 2, y + Math.max(5, h / 38),
                    Paleta.conAlfa(Paleta.PAPEL, .58F));
            Lienzo.caja(g, x, y - 4, x + 2, y, Paleta.conAlfa(n.luz, llama * .76F * luz));
        }
        Lienzo.motas(g, w, h, tiempo * .35F, n.luz, .15F * luz, 1320, 14);
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        // Fragmento de columna abatido, cerca del ojo y fuera del eje.
        Lienzo.linea(g, -w * .08F, h * .70F, w * .29F, h * 1.03F,
                Math.max(18, w / 25), Paleta.iluminar(n.paredBaja, luz * .24F));
        Lienzo.caja(g, -8, Math.round(h * .67F), Math.round(w * .12F),
                Math.round(h * .75F), Paleta.iluminar(n.junta, luz * .30F));
    }
}
