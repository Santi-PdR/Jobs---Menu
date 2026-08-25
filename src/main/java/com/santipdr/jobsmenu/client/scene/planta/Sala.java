package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Nivel 0 - La sala.
 *
 * Un ambiente ancho y bajo, de los que se cruzan en diagonal sin pensarlo. El
 * cielorraso es una grilla de placas con las luminarias empotradas en fila, la
 * alfombra esta gastada por el paso y en la pared del fondo hay tres vanos de
 * puerta que dan a otra sala igual.
 *
 * Lo que la distingue de todo lo demas: es ANCHA. El semiancho del fondo es
 * grande y la altura corta, asi que las paredes laterales quedan en los bordes
 * de la pantalla y el peso de la imagen se lo lleva el cielorraso. No se
 * camina por una sala: se esta adentro.
 */
public final class Sala implements Planta {

    /** Tramos en profundidad. Pocos y largos: la sala es corta y ancha. */
    private static final int TRAMOS = 12;

    /** Cuantas placas de cielorraso a lo ancho. */
    private static final int PLACAS = 7;

    @Override
    public int tramos() {
        return TRAMOS;
    }

    @Override
    public void dibujar(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        Trazo.fondo(grafico, m, nivel, luz,
                Paleta.mezclar(nivel.paredBaja, nivel.techo, 0.45F), 1.5F);
        puertasFondo(grafico, m, nivel, luz);

        Trazo.plano(grafico, m, true, nivel.techo,
                Paleta.mezclar(nivel.techo, nivel.niebla, 0.35F), nivel.niebla, luz, 0.50F);
        Trazo.plano(grafico, m, false, nivel.suelo, nivel.sueloLejos, nivel.niebla, luz, 0.58F);

        alfombra(grafico, m, nivel, luz);
        Trazo.transversales(grafico, m, false, nivel.sueloJunta, nivel.niebla, luz, TRAMOS, 0.38F);
        Trazo.transversales(grafico, m, true, nivel.techoJunta, nivel.niebla, luz, TRAMOS, 0.46F);
        grillaCielorraso(grafico, m, nivel, luz);
        filaLuminarias(grafico, m, nivel, luz);

        Trazo.paredes(grafico, m, nivel, luz);
        zocalo(grafico, m, nivel, luz);
        Trazo.juntasVerticales(grafico, m, nivel, luz, TRAMOS, 1.0F, 0.45F);
        Trazo.manchas(grafico, m, nivel, luz, TRAMOS);
        cuadros(grafico, m, nivel, luz);
    }

    /**
     * Las tres puertas del fondo.
     *
     * La del medio esta cerrada y las de los costados abiertas. Es un detalle
     * de dos rectangulos que cambia por completo la lectura del sitio: sin
     * ellas la pared del fondo es el limite del mundo; con ellas, la sala
     * sigue del otro lado y esta es solo una de muchas.
     */
    private static void puertasFondo(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float sueloFondo = m.sueloEn(1.0F);
        float alto = m.h() * 1.30F;

        for (int i = 0; i < 3; i++) {
            float centro = m.fx()
                    + (i > 1 ? m.der(1.0F) - m.fx() : m.fx() - m.izq(1.0F))
                    * (i - 1) * 0.56F;
            float medio = m.w() * 0.13F;
            boolean abierta = i != 1;

            int x0 = Math.round(centro - medio);
            int x1 = Math.round(centro + medio);
            int y0 = Math.round(sueloFondo - alto);
            int y1 = Math.round(sueloFondo);

            if (abierta) {
                grafico.fillGradient(x0, y0, x1, y1,
                        Paleta.conAlfa(Paleta.iluminar(nivel.fondo, luz * 0.30F), 0.95F),
                        Paleta.conAlfa(Paleta.VANO, 0.96F));
            } else {
                grafico.fill(x0, y0, x1, y1,
                        Paleta.iluminar(Paleta.mezclar(nivel.paredBaja, nivel.junta, 0.35F), luz * 0.62F));
                // El picaporte: un pixel que confirma que eso es una puerta.
                grafico.fill(x1 - 4, (y0 + y1) / 2, x1 - 2, (y0 + y1) / 2 + 2,
                        Paleta.iluminar(nivel.luz, luz * 0.55F));
            }

            // Marco. Un poco mas claro que la pared, como todo herraje viejo.
            int marco = Paleta.iluminar(Paleta.mezclar(nivel.junta, nivel.paredAlta, 0.30F), luz * 0.70F);
            grafico.fill(x0 - 1, y0 - 1, x1 + 1, y0 + 1, marco);
            grafico.fill(x0 - 1, y0, x0 + 1, y1, marco);
            grafico.fill(x1 - 1, y0, x1 + 1, y1, marco);
        }
    }

    /**
     * La grilla del cielorraso: las longitudinales que faltan.
     *
     * Las transversales ya las puso {@link Trazo}. Estas son las que corren
     * hacia la fuga, y son las que convierten el techo en una grilla de placas
     * en vez de en una escalera de lineas.
     */
    private static void grillaCielorraso(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int i = 1; i < PLACAS; i++) {
            float frac = (i / (float) PLACAS) * 2.0F - 1.0F;
            // Cada longitudinal es una recta que pasa por la fuga: se dibuja
            // fila por fila porque en pantalla no es vertical ni horizontal.
            for (int y = 0; y < m.techoEn(1.0F); y += Trazo.PASO) {
                float dy = m.dy(y + Trazo.PASO * 0.5F);
                if (dy <= 1.0F) {
                    continue;
                }
                float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
                float x = m.enX(dy, frac);
                int grosor = Math.max(1, (int) (m.w() * dy * 0.006F));
                grafico.fill((int) x, y, (int) x + grosor, y + Trazo.PASO,
                        Paleta.conAlfa(Paleta.iluminar(
                                Trazo.velar(nivel.techoJunta, nivel.niebla, lej, 0.5F),
                                Trazo.atenuar(luz, lej)), 0.40F * lej + 0.10F));
            }
        }
    }

    /**
     * Las luminarias empotradas, una por tramo, en el eje de la sala.
     *
     * Van en la grilla del cielorraso y no colgadas, que es lo propio de una
     * oficina; y son largas y anchas, no tubos finos.
     */
    private static void filaLuminarias(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 2; j <= TRAMOS; j++) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 6.0F) {
                continue;
            }
            Trazo.luminaria(grafico, m, nivel, dx, 0.90F, 0.30F, 1.0F, luz);
        }
    }

    /**
     * La alfombra: una franja mas oscura por el centro, gastada por el paso.
     *
     * No tiene bordes rectos. La suciedad de una alfombra de oficina se
     * concentra donde la gente camina, y eso es una banda difusa por el eje.
     */
    private static void alfombra(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int y = Math.round(m.sueloEn(1.0F)); y < m.alto(); y += Trazo.PASO) {
            float dy = m.dy(y + Trazo.PASO * 0.5F);
            if (dy <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
            float medio = m.w() * dy * 0.42F;
            float a = 0.16F * (0.45F + 0.55F * lej);
            grafico.fill((int) (m.fx() - medio), y, (int) (m.fx() + medio), y + Trazo.PASO,
                    Paleta.conAlfa(Paleta.iluminar(nivel.sueloJunta, luz), a));
        }
    }

    /** El zocalo corrido al pie de las dos paredes. */
    private static void zocalo(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int x = 0; x < m.ancho(); x += Trazo.PASO) {
            float dx = m.dx(x + Trazo.PASO * 0.5F);
            if (dx <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float y1 = m.sueloEn(dx);
            int alto = Math.max(1, (int) (m.h() * dx * 0.055F));
            grafico.fill(x, (int) y1 - alto, x + Trazo.PASO, (int) y1,
                    Paleta.iluminar(nivel.junta, Trazo.atenuar(luz, lej) * 0.85F));
        }
    }

    /**
     * Las marcas rectangulares que quedan donde hubo algo colgado.
     *
     * No hay cuadros: hay el rectangulo mas limpio que dejo el cuadro cuando
     * se lo llevaron. Es el detalle que dice que aca antes trabajaba gente.
     */
    private static void cuadros(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 3; j < TRAMOS; j++) {
            if (Trazo.pseudo(820 + j) > 0.38F) {
                continue;
            }
            float dxA = Trazo.profundidad(j, TRAMOS);
            float dxB = Trazo.profundidad(j + 1, TRAMOS);
            int signo = Trazo.pseudo(860 + j) < 0.5F ? -1 : 1;
            float lej = Trazo.limitar(1.0F / dxA, 0.0F, 1.0F);

            int x0 = (int) Math.min(m.lado(signo, dxA), m.lado(signo, dxB));
            int x1 = (int) Math.max(m.lado(signo, dxA), m.lado(signo, dxB));
            if (x1 <= 0 || x0 >= m.ancho() || x1 - x0 < 3) {
                continue;
            }

            for (int col = Math.max(0, x0); col < Math.min(m.ancho(), x1); col++) {
                float dxc = m.dx(col + 0.5F);
                float centro = m.techoEn(dxc * 0.30F);
                float medio = m.h() * dxc * 0.22F;
                grafico.fill(col, (int) (centro - medio), col + 1, (int) (centro + medio),
                        Paleta.conAlfa(Paleta.iluminar(nivel.paredAlta, luz), 0.16F * lej + 0.06F));
            }
        }
    }
}
