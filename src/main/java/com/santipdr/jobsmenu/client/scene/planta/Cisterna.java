package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Pozo hidraulico vertical observado desde una pasarela superior. */
public final class Cisterna implements Planta {
    @Override public float pisoPresencia() { return .74F; }

    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        int piedra = Lienzo.tono(n, n.paredBaja, luz, .52F);
        Lienzo.fondo(g, w, h, n.fondo, piedra, 2400);

        // Boca eliptica del pozo: anillos concentricos descienden y pierden luz.
        int cx = Math.round(w * .52F), cy = Math.round(h * .61F);
        int radio = Math.max(55, Math.round(Math.min(w * .42F, h * .49F)));
        Lienzo.circulo(g, cx, cy, radio, piedra);
        for (int i = 0; i < 6; i++) {
            int rr = Math.round(radio * (1F - i * .115F));
            int desplazamiento = i * Math.max(4, h / 28);
            int material = Lienzo.tono(n, Paleta.mezclar(n.paredAlta, n.fondo, i * .12F),
                    luz, .30F + i * .10F);
            Lienzo.anillo(g, cx, cy + desplazamiento, rr, Math.max(4, radio / 16),
                    material, n.fondo);
        }

        // Contrafuertes verticales convergen hacia el agua muy abajo.
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI * 2.0 / 10.0;
            int x0 = cx + Math.round((float) Math.cos(a) * radio * .92F);
            int y0 = cy + Math.round((float) Math.sin(a) * radio * .53F);
            int x1 = cx + Math.round((float) Math.cos(a) * radio * .34F);
            int y1 = cy + Math.round((float) Math.sin(a) * radio * .18F) + h / 7;
            Lienzo.linea(g, x0, y0, x1, y1, Math.max(4, radio / 18),
                    Lienzo.tono(n, n.junta, luz, .25F + i * .05F));
        }

        // Agua negra al fondo del eje, pequena por la altura del punto de vista.
        int aguaR = Math.max(22, Math.round(radio * .31F));
        Lienzo.circulo(g, cx, cy + h / 6, aguaR,
                Paleta.iluminar(Paleta.mezclar(n.fondo, n.suelo, .27F), luz * .48F));
        for (int i = 0; i < 10; i++) {
            int y = cy + h / 6 - aguaR / 2 + i * Math.max(2, aguaR / 10);
            int ancho = Math.round(aguaR * (.30F + Trazo.pseudo(2490 + i) * .65F));
            int deriva = Math.round((float) Math.sin(tiempo * .29F + i) * 4);
            Lienzo.caja(g, cx - ancho + deriva, y, cx + ancho + deriva, y + 1,
                    Paleta.conAlfa(n.luz, .10F + (9 - i) * .008F));
        }

        // Tuberia de caida y escalera interminable en la pared derecha.
        int tuboX = Math.round(w * .20F);
        Lienzo.linea(g, tuboX, 0, tuboX + w * .10F, h, Math.max(8, w / 46),
                Paleta.iluminar(n.junta, luz * .34F));
        int escX = Math.round(w * .80F);
        Lienzo.linea(g, escX, h * .07F, escX - w * .10F, h * .91F, 2,
                Paleta.conAlfa(n.techoJunta, .78F));
        Lienzo.linea(g, escX + w * .035F, h * .07F, escX - w * .065F, h * .91F, 2,
                Paleta.conAlfa(n.techoJunta, .78F));
        for (int i = 0; i < 15; i++) {
            float t = i / 14F;
            int x = Math.round(escX - w * .10F * t);
            int y = Math.round(h * (.07F + .84F * t));
            Lienzo.linea(g, x, y, x + w * .035F, y, 1, Paleta.conAlfa(n.techoJunta, .72F));
        }

        // Gotas que recorren unicamente el hueco central.
        for (int i = 0; i < 12; i++) {
            float fase = (Trazo.pseudo(2560 + i) + tiempo * (.004F + i * .0003F)) % 1F;
            int x = cx + Math.round((Trazo.pseudo(2580 + i) - .5F) * radio * .68F);
            int y = Math.round(h * (.20F + fase * .58F));
            Lienzo.caja(g, x, y, x + 1, y + 3, Paleta.conAlfa(n.luz, .20F * luz));
        }
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        // Plataforma superior en U: el vacio queda debajo del jugador.
        int metal = Paleta.iluminar(n.junta, luz * .29F);
        Lienzo.quad(g, Math.round(h * .78F), h, -w * .08F, w * .28F,
                -w * .16F, w * .43F, metal);
        Lienzo.quad(g, Math.round(h * .83F), h, w * .79F, w * 1.08F,
                w * .66F, w * 1.16F, metal);
        Lienzo.linea(g, -10, h * .71F, w * .31F, h * .87F, 3,
                Paleta.conAlfa(n.techoJunta, .80F));
        Lienzo.linea(g, w * .77F, h * .83F, w + 10, h * .70F, 3,
                Paleta.conAlfa(n.techoJunta, .80F));
    }
}
