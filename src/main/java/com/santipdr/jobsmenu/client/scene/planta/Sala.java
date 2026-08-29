package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Vestibulo administrativo brutalista, abandonado a mitad de una jornada. */
public final class Sala implements Planta {
    @Override public void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        int muroClaro = Lienzo.tono(n, n.paredAlta, luz, .30F);
        int muroOscuro = Lienzo.tono(n, n.paredBaja, luz, .48F);
        int hierro = Lienzo.tono(n, n.junta, luz, .34F);
        Lienzo.fondo(g, w, h, muroClaro, muroOscuro, 100);
        Lienzo.revoque(g, 0, 0, w, Math.round(h * .69F), muroClaro, n.junta, 110);

        // Techo suspendido roto; la perdida irregular descubre la losa humeda.
        Lienzo.caja(g, 0, 0, w, Math.round(h * .18F), Lienzo.tono(n, n.techo, luz, .22F));
        Lienzo.quad(g, 0, Math.round(h * .24F), 0, w * .33F, w * .19F, w * .42F,
                Paleta.iluminar(n.fondo, .65F));
        for (int x = Math.round(w * .19F); x < w; x += Math.max(24, w / 9))
            Lienzo.linea(g, x, 0, x + w * .05F, h * .18F, 1, Paleta.conAlfa(n.techoJunta, .55F));
        for (int y = Math.max(8, h / 18); y < h * .18F; y += Math.max(10, h / 12))
            Lienzo.linea(g, w * .18F, y, w, y, 1, Paleta.conAlfa(n.techoJunta, .42F));

        // Banda institucional desconchada y senaletica humana.
        int bandaY = Math.round(h * .35F);
        Lienzo.caja(g, 0, bandaY, w, bandaY + Math.max(9, h / 17),
                Paleta.conAlfa(Paleta.mezclar(n.junta, 0xFF44502D, .48F), .72F));
        for (int i = 0; i < 8; i++) {
            int x = Math.round(Trazo.pseudo(150 + i * 9) * w);
            Lienzo.caja(g, x, bandaY, x + 4 + i % 8, bandaY + Math.max(9, h / 17),
                    Paleta.conAlfa(muroOscuro, .42F));
        }

        int puertaX = Math.round(w * .15F), puertaY = Math.round(h * .24F);
        Lienzo.metal(g, puertaX, puertaY, Math.round(w * .34F), Math.round(h * .68F),
                Lienzo.tono(n, n.sueloLejos, luz, .62F), hierro, 0xFF6E3D1E, 170);
        Lienzo.caja(g, puertaX + 5, puertaY + 7, Math.round(w * .34F) - 5,
                puertaY + Math.max(16, h / 15), Paleta.conAlfa(Paleta.PAPEL, .45F));
        int relojX = Math.round(w * .43F), relojY = Math.round(h * .29F);
        Lienzo.anillo(g, relojX, relojY, Math.max(7, h / 24), 2, hierro,
                Paleta.iluminar(Paleta.PAPEL, luz * .52F));
        Lienzo.linea(g, relojX, relojY, relojX - 2, relojY - h * .025F, 1, n.fondo);
        Lienzo.linea(g, relojX, relojY, relojX + h * .022F, relojY + 1, 1, n.fondo);

        // Mampara y mostrador son la masa dominante del plano medio.
        int mostradorX = Math.round(w * .49F), mostradorY = Math.round(h * .22F);
        Lienzo.caja(g, mostradorX, mostradorY, w, Math.round(h * .67F), n.fondo);
        int mod = Math.max(34, (w - mostradorX) / 4);
        for (int x = mostradorX; x < w; x += mod)
            Lienzo.cristal(g, x + 2, mostradorY + 2, Math.min(w, x + mod - 2),
                    Math.round(h * .55F), 0xFF556554, hierro, n.luz, 220 + x, tiempo);
        Lienzo.madera(g, mostradorX - 8, Math.round(h * .55F), w, Math.round(h * .71F),
                Lienzo.tono(n, n.sueloLejos, luz, .25F), n.junta, 260);
        Lienzo.caja(g, mostradorX - 12, Math.round(h * .54F), w, Math.round(h * .57F), hierro);

        // Terrazo y postes de fila convergen hacia la atencion.
        Lienzo.quad(g, Math.round(h * .67F), h, 0, w, w * .44F, w * .83F,
                Lienzo.tono(n, n.suelo, luz, .25F));
        for (int i = 0; i < 6; i++) {
            float t = i / 5F;
            int y = Math.round(h * (.70F + t * .24F));
            int x = Math.round(w * (.47F - t * .30F));
            Lienzo.linea(g, x, y - h * (.07F + t * .03F), x, y,
                    Math.max(2, Math.round(2 + t * 3)), hierro);
            if (i > 0) {
                float p = (i - 1) / 5F;
                int px = Math.round(w * (.47F - p * .30F));
                int py = Math.round(h * (.70F + p * .24F));
                Lienzo.linea(g, px, py - h * .055F, x, y - h * (.07F + t * .03F), 1,
                        Paleta.conAlfa(n.junta, .75F));
            }
        }
        Lienzo.motas(g, w, h, tiempo, n.luz, .17F * luz, 300, 16);
    }

    @Override public void primerPlano(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int w = m.ancho(), h = m.alto();
        Lienzo.quad(g, Math.round(h * .83F), h, -w * .08F, w * .42F,
                -w * .18F, w * .52F, Lienzo.tono(n, n.sueloLejos, luz, .06F));
        Lienzo.caja(g, 0, Math.round(h * .82F), Math.round(w * .43F), Math.round(h * .85F),
                Paleta.iluminar(n.junta, luz * .46F));
        Lienzo.quad(g, Math.round(h * .80F), Math.round(h * .86F), w * .10F, w * .31F,
                w * .08F, w * .35F, Paleta.conAlfa(Paleta.PAPEL, .62F));
    }
}
