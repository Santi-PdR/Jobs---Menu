package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Incidentes visuales raros y propios de cada recinto.
 *
 * No mantiene particulas permanentes. Cada nivel tiene una ventana breve en
 * un periodo largo y una silueta diferente; fuera de esa ventana el coste es
 * una division y una comparacion. El audio ambiental conserva su planificador
 * independiente: este pulso solo acompana el espacio, no dispara sonidos.
 */
public final class PulsoLugar {
    private PulsoLugar() { }

    public static void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        int numero = n.numero();
        float fase = faseRara(tiempo, 53 + numero * 19, 137 + numero * 11, 2.8F + numero * .09F);
        if (fase <= 0F) return;
        int w = m.ancho(), h = m.alto();
        float alfa = (1F - Math.abs(fase * 2F - 1F)) * luz;

        switch (numero) {
            case 0 -> {
                // Sombra de una puerta que se mueve detras de la mampara.
                int x = Math.round(w * (.72F + fase * .09F));
                Lienzo.quad(g, Math.round(h * .27F), Math.round(h * .55F),
                        x, x + w * .035F, x - w * .02F, x + w * .055F,
                        Paleta.conAlfa(n.fondo, .16F * alfa));
            }
            case 1 -> {
                // Chispas que caen una sola vez del carro del puente grua.
                for (int i = 0; i < 7; i++) {
                    int x = Math.round(w * .61F + (Trazo.pseudo(3000 + i) - .5F) * w * .07F);
                    int y = Math.round(h * (.22F + fase * (.22F + i * .018F)));
                    Lienzo.linea(g, x, y, x - 2, y + 5 + i % 4, 1,
                            Paleta.conAlfa(0xFFFFB45A, .48F * alfa));
                }
            }
            case 2 -> {
                // Descarga de presion lateral, concentrada en una junta.
                for (int i = 0; i < 11; i++) {
                    int x = Math.round(w * (.42F - fase * (.04F + i * .008F)));
                    int y = Math.round(h * (.48F + (Trazo.pseudo(3050 + i) - .5F) * .12F));
                    Lienzo.caja(g, x, y, x + 5 + i, y + 2,
                            Paleta.conAlfa(n.niebla, .16F * alfa * (1F - i / 13F)));
                }
            }
            case 3 -> {
                // Onda aislada bajo la torre de salto.
                int r = Math.max(4, Math.round(fase * w * .10F));
                Lienzo.anillo(g, Math.round(w * .78F), Math.round(h * .66F), r,
                        1, Paleta.conAlfa(n.luz, .22F * alfa),
                        Paleta.conAlfa(n.fondo, 0F));
            }
            case 4 -> {
                // Una vela cede y el humo asciende sobre el relicario.
                int x = Math.round(w * .51F);
                for (int i = 0; i < 6; i++) {
                    int y = Math.round(h * (.69F - fase * .23F - i * .018F));
                    Lienzo.circulo(g, x + (i % 2 == 0 ? i : -i), y, 2 + i / 2,
                            Paleta.conAlfa(n.niebla, .09F * alfa));
                }
            }
            case 5 -> {
                // Hoja desprendida que cae dentro del pozo de lectura.
                int x = Math.round(w * (.40F + (float) Math.sin(fase * 8F) * .07F));
                int y = Math.round(h * (.24F + fase * .56F));
                Lienzo.quad(g, y, y + Math.max(3, h / 45), x - 5, x + 6,
                        x - 2, x + 8, Paleta.conAlfa(Paleta.PAPEL, .32F * alfa));
            }
            case 6 -> {
                // Gota pesada en el pano roto y breve reflejo de vidrio.
                int x = Math.round(w * .65F), y = Math.round(h * (.08F + fase * .52F));
                Lienzo.linea(g, x, y - 7, x, y + 2, 1, Paleta.conAlfa(n.luz, .35F * alfa));
                Lienzo.linea(g, w * .51F, h * .10F, w * .80F, h * .24F, 1,
                        Paleta.conAlfa(n.luz, .14F * alfa));
            }
            case 7 -> {
                // Sombra que cruza unicamente el segundo rellano.
                int x = Math.round(w * (.38F + fase * .27F));
                Lienzo.quad(g, Math.round(h * .47F), Math.round(h * .72F),
                        x - w * .04F, x + w * .04F, x - w * .09F, x + w * .08F,
                        Paleta.conAlfa(n.fondo, .22F * alfa));
            }
            case 8 -> {
                // Gota vertical y anillo diminuto en el agua del fondo.
                int x = Math.round(w * .52F), y = Math.round(h * (.30F + fase * .46F));
                Lienzo.linea(g, x, y - 5, x, y + 2, 1, Paleta.conAlfa(n.luz, .30F * alfa));
                if (fase > .78F) {
                    int r = 2 + Math.round((fase - .78F) * w * .12F);
                    Lienzo.anillo(g, x, Math.round(h * .77F), r, 1,
                            Paleta.conAlfa(n.luz, .18F * alfa), Paleta.conAlfa(n.fondo, 0F));
                }
            }
            case 9 -> {
                // Polvo desprendido del oculo, visible solo dentro del haz.
                for (int i = 0; i < 13; i++) {
                    int x = Math.round(w * .54F + (Trazo.pseudo(3200 + i) - .5F) * w * .18F * fase);
                    int y = Math.round(h * (.27F + fase * (.35F + i * .012F)));
                    Lienzo.caja(g, x, y, x + 1 + i % 2, y + 1 + i % 2,
                            Paleta.conAlfa(n.luz, .18F * alfa));
                }
            }
            default -> { }
        }
    }

    /** Devuelve 0 fuera del evento y 0..1 dentro de su ventana. */
    private static float faseRara(float tiempo, int semilla, int periodo, float duracion) {
        float dentro = (tiempo + semilla) % periodo;
        return dentro >= 0F && dentro < duracion ? dentro / duracion : 0F;
    }
}
