package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Camara de calderas: recipiente de presion, pasarela y laberinto de tubos. */
public final class Servicio implements Planta {
    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        int hierro = Lienzo.tono(n, n.junta, luz, .30F);
        int cobre = Paleta.iluminar(0xFF8B5529, luz * .72F);
        Lienzo.fondo(g, w, h, n.fondo, Lienzo.tono(n, n.paredBaja, luz, .52F), 800);
        Lienzo.metal(g, 0, 0, w, h, Lienzo.tono(n, n.paredBaja, luz, .48F),
                n.paredAlta, 0xFF6A351A, 810);

        // Gran caldera desplazada: el objeto que define el recinto.
        int cx = Math.round(w * .67F), cy = Math.round(h * .50F);
        int radio = Math.max(34, Math.round(Math.min(w * .24F, h * .42F)));
        Lienzo.halo(g, cx, cy, radio + h / 8, n.luz, .055F * luz);
        Lienzo.circulo(g, cx, cy, radio, Paleta.iluminar(n.junta, luz * .45F));
        Lienzo.circulo(g, cx, cy, radio - Math.max(5, radio / 10),
                Paleta.iluminar(Paleta.mezclar(n.paredBaja, 0xFF6C3C20, .42F), luz * .62F));
        for (int x = cx - radio + 8; x < cx + radio - 6; x += Math.max(11, radio / 4)) {
            int dy = Math.round((float) Math.sqrt(Math.max(0,
                    radio * radio - (x - cx) * (x - cx))));
            Lienzo.linea(g, x, cy - dy + 8, x, cy + dy - 8, 1,
                    Paleta.conAlfa(n.paredAlta, .18F));
        }
        Lienzo.anillo(g, cx, cy, Math.max(13, radio / 4), Math.max(3, radio / 18),
                hierro, n.fondo);
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI * 2.0 / 12.0;
            Lienzo.circulo(g, cx + Math.round((float) Math.cos(a) * radio * .80F),
                    cy + Math.round((float) Math.sin(a) * radio * .80F),
                    Math.max(1, radio / 34), n.paredAlta);
        }

        // Colectores a cotas y diametros distintos.
        for (int i = 0; i < 5; i++) {
            int y = Math.round(h * (.12F + i * .105F));
            int grosor = Math.max(2, h / (i % 2 == 0 ? 65 : 85));
            int color = i == 2 ? cobre : hierro;
            float codoX = w * (.29F + i * .035F);
            Lienzo.linea(g, -8, y, codoX, y, grosor, color);
            Lienzo.linea(g, codoX, y, codoX, h * (.34F + i * .09F), grosor, color);
            Lienzo.linea(g, codoX, h * (.34F + i * .09F), cx - radio * .82F,
                    cy - radio * .38F + i * radio * .18F, grosor, color);
            for (int x = 10 + i * 5; x < w * .28F; x += Math.max(23, w / 15))
                Lienzo.caja(g, x, y - grosor - 2, x + 2, y + grosor + 3,
                        Paleta.conAlfa(n.techoJunta, .74F));
        }

        // Pasarela de rejilla que cruza el recipiente.
        int py = Math.round(h * .69F);
        Lienzo.caja(g, 0, py, w, py + Math.max(8, h / 25), hierro);
        for (int x = 0; x < w; x += Math.max(8, w / 38))
            Lienzo.caja(g, x, py + 2, x + 2, py + Math.max(7, h / 27), n.fondo);
        Lienzo.linea(g, 0, py - h * .12F, w, py - h * .12F, 2, hierro);
        for (int x = 0; x < w; x += Math.max(35, w / 9))
            Lienzo.linea(g, x, py - h * .12F, x, py, 2, hierro);

        // Manometros y valvula de seis radios.
        for (int i = 0; i < 2; i++) {
            int mx = Math.round(w * (.14F + i * .12F)), my = Math.round(h * .55F);
            Lienzo.anillo(g, mx, my, Math.max(8, h / 24), 2, cobre,
                    Paleta.iluminar(Paleta.PAPEL, luz * .50F));
            Lienzo.linea(g, mx, my, mx + (i == 0 ? -4 : 5), my - h * .025F, 1, n.fondo);
        }
        int vx = Math.round(w * .12F), vy = Math.round(h * .81F), vr = Math.max(12, h / 12);
        Lienzo.anillo(g, vx, vy, vr, Math.max(3, vr / 5), hierro, n.fondo);
        for (int i = 0; i < 6; i++) {
            double a = i * Math.PI / 3.0;
            Lienzo.linea(g, vx, vy,
                    vx + Math.round((float) Math.cos(a) * vr),
                    vy + Math.round((float) Math.sin(a) * vr), 2, hierro);
        }

        float pulso = .55F + .45F * (float) Math.sin(tiempo * .21F);
        for (int i = 0; i < 9; i++) {
            float fase = (Trazo.pseudo(930 + i) + tiempo * (.008F + i * .0005F)) % 1F;
            int x = Math.round(w * .45F + (Trazo.pseudo(950 + i) - .5F) * w * .11F);
            int y = Math.round(h * (.64F - fase * .30F));
            Lienzo.caja(g, x, y, x + 3 + i % 5, y + 2,
                    Paleta.conAlfa(n.niebla, (.025F + pulso * .045F) * luz));
        }
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        int color = Paleta.iluminar(n.junta, luz * .27F);
        Lienzo.linea(g, -20, h * .94F, w * .39F, h * .78F, Math.max(9, h / 18), color);
        Lienzo.linea(g, w * .82F, -15, w * .90F, h * .28F, Math.max(8, h / 22), color);
    }
}
