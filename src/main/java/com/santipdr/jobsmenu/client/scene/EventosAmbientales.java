package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Trazo;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Sucesos visuales raros y propios de cada recinto.
 *
 * Una ventana de cinco segundos solo se habilita en uno de cada cuatro ciclos
 * de 97 segundos. El resto del tiempo no calcula ni dibuja nada.
 */
public final class EventosAmbientales {
    private static final long CICLO_MS = 97_000L;
    private static final long VENTANA_MS = 5_000L;

    private EventosAmbientales() { }

    public static void dibujar(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo) {
        long ahora = System.currentTimeMillis();
        long ciclo = Math.floorDiv(ahora, CICLO_MS);
        int nivel = n.numero();
        if (Math.floorMod(ciclo + nivel * 3L, 4L) != 0L) return;
        long dentro = Math.floorMod(ahora, CICLO_MS);
        if (dentro < CICLO_MS - VENTANA_MS) return;
        float t = (dentro - (CICLO_MS - VENTANA_MS)) / (float) VENTANA_MS;
        float campana = (float) Math.sin(Math.PI * t);
        if (campana <= .01F) return;

        switch (nivel) {
            case 0 -> fluorescente(g, m, n, luz, campana);
            case 1 -> gancho(g, m, n, luz, tiempo, campana);
            case 2 -> presion(g, m, n, luz, tiempo, campana);
            case 3 -> onda(g, m, n, luz, tiempo, campana);
            case 4 -> brasas(g, m, n, luz, tiempo, campana);
            case 5 -> papel(g, m, n, luz, t, campana);
            case 6 -> hoja(g, m, n, luz, t, campana);
            case 7 -> sombra(g, m, n, campana);
            case 8 -> circulos(g, m, n, luz, t, campana);
            case 9 -> estandarte(g, m, n, luz, tiempo, campana);
            default -> { }
        }
    }

    private static void fluorescente(GuiGraphics g, Marco m, Nivel n, float luz, float a) {
        int y = Math.round(m.alto() * .20F);
        g.fill(Math.round(m.ancho() * .38F), y, Math.round(m.ancho() * .62F), y + 2,
                Paleta.conAlfa(n.luz, .28F * a * luz));
    }

    private static void gancho(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo, float a) {
        int x = Math.round(m.ancho() * .64F + (float) Math.sin(tiempo * .8F) * 7);
        int y = Math.round(m.alto() * .43F);
        g.fill(x, 0, x + 1, y, Paleta.conAlfa(n.junta, .55F * a));
        g.fill(x - 2, y, x + 5, y + 7, Paleta.conAlfa(n.junta, .70F * a * luz));
    }

    private static void presion(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo, float a) {
        for (int i = 0; i < 10; i++) {
            int x = Math.round(m.ancho() * .57F + (Trazo.pseudo(5100 + i) - .5F) * 80);
            int y = Math.round(m.alto() * (.68F - ((i / 10F + tiempo * .05F) % 1F) * .35F));
            g.fill(x, y, x + 6 + i % 5, y + 2, Paleta.conAlfa(n.niebla, .12F * a * luz));
        }
    }

    private static void onda(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo, float a) {
        int y = Math.round(m.alto() * (.66F + .02F * (float) Math.sin(tiempo)));
        int cx = Math.round(m.ancho() * .66F);
        for (int r = 8; r < 65; r += 12)
            g.fill(cx - r, y + r / 6, cx + r, y + r / 6 + 1, Paleta.conAlfa(n.luz, .10F * a * luz));
    }

    private static void brasas(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo, float a) {
        for (int i = 0; i < 9; i++) {
            int x = Math.round(m.ancho() * (.18F + Trazo.pseudo(5200 + i) * .64F));
            int y = Math.round(m.alto() * (.72F - ((tiempo * .04F + i * .13F) % 1F) * .35F));
            g.fill(x, y, x + 1, y + 2, Paleta.conAlfa(n.luz, .45F * a * luz));
        }
    }

    private static void papel(GuiGraphics g, Marco m, Nivel n, float luz, float t, float a) {
        int x = Math.round(m.ancho() * (.52F + t * .18F));
        int y = Math.round(m.alto() * (.35F + t * .42F + (float) Math.sin(t * 12) * .03F));
        g.fill(x, y, x + 9, y + 6, Paleta.conAlfa(Paleta.PAPEL, .35F * a * luz));
    }

    private static void hoja(GuiGraphics g, Marco m, Nivel n, float luz, float t, float a) {
        int x = Math.round(m.ancho() * (.76F - t * .32F + (float) Math.sin(t * 9) * .04F));
        int y = Math.round(m.alto() * (.18F + t * .58F));
        g.fill(x, y, x + 5, y + 3, Paleta.conAlfa(0xFF314523, .65F * a * luz));
    }

    private static void sombra(GuiGraphics g, Marco m, Nivel n, float a) {
        int x = Math.round(m.ancho() * .15F);
        g.fill(x, Math.round(m.alto() * .35F), x + Math.round(m.ancho() * .08F),
                Math.round(m.alto() * .82F), Paleta.conAlfa(n.fondo, .22F * a));
    }

    private static void circulos(GuiGraphics g, Marco m, Nivel n, float luz, float t, float a) {
        int cx = Math.round(m.ancho() * .57F), cy = Math.round(m.alto() * .70F);
        for (int i = 0; i < 4; i++) {
            int r = Math.round((t + i * .18F) % 1F * m.ancho() * .16F);
            g.fill(cx - r, cy + r / 5, cx + r, cy + r / 5 + 1,
                    Paleta.conAlfa(n.luz, .12F * a * luz));
        }
    }

    private static void estandarte(GuiGraphics g, Marco m, Nivel n, float luz, float tiempo, float a) {
        int x = Math.round(m.ancho() * .81F);
        int deriva = Math.round((float) Math.sin(tiempo * .55F) * 7 * a);
        g.fill(x, Math.round(m.alto() * .20F), x + 2, Math.round(m.alto() * .55F), Paleta.conAlfa(n.junta, .72F));
        g.fill(x + 2, Math.round(m.alto() * .25F), x + 25 + deriva, Math.round(m.alto() * .47F),
                Paleta.conAlfa(0xFF29233F, .45F * a * luz));
    }
}
