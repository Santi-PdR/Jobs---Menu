package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.scene.RotacionNiveles;

import net.minecraft.client.gui.GuiGraphics;

/**
 * La hoja fotocopiada pegada a la pared: papel, sombra proyectada, borde
 * humedecido y un trozo de cinta arriba.
 *
 * Vive suelta porque ya no la usa una sola pantalla. El aviso del nivel, las
 * condiciones de estancia y la pausa son todas la misma hoja pegada al mismo
 * pasillo, y tienen que envejecer igual: el mismo papel, la misma cinta, el
 * mismo apagarse cuando el fluorescente se corta. Repetir el dibujo en cada
 * pantalla era pedir que tres copias se fueran separando con el tiempo.
 */
public final class HojaPapel {

    private HojaPapel() {
    }

    /**
     * Dibuja una hoja entre las dos esquinas dadas.
     *
     * @param conCinta si lleva el trozo de cinta adhesiva en el borde superior.
     *                 El aviso principal la lleva; una hoja secundaria clavada
     *                 con chinche no.
     */
    public static void dibujar(GuiGraphics grafico, int x0, int y0, int x1, int y1, boolean conCinta) {
        dibujar(grafico, x0, y0, x1, y1, conCinta, RotacionNiveles.luzDisponible());
    }

    /** Variante para pantallas que no pertenecen al apagon del menu. */
    public static void dibujar(GuiGraphics grafico, int x0, int y0, int x1, int y1,
                               boolean conCinta, float luz) {
        // El papel se oscurece con el pasillo. No es tinta: es el blanco de la
        // hoja, que sin fluorescente encima deja de ser blanco.
        luz = Math.max(0.0F, Math.min(1.0F, luz));
        int papel = Paleta.iluminar(Paleta.PAPEL, 0.22F + 0.78F * luz);

        grafico.fill(x0 + 3, y0 + 4, x1 + 3, y1 + 4, Paleta.conAlfa(Paleta.VANO, 0.30F));
        grafico.fill(x0, y0, x1, y1, Paleta.conAlfa(papel, 0.94F));
        grafico.fill(x0, y0, x1, y0 + 1, Paleta.conAlfa(Paleta.MOHO, 0.35F));
        grafico.fill(x0, y1 - 1, x1, y1, Paleta.conAlfa(Paleta.MOHO, 0.45F));
        grafico.fill(x0, y0, x0 + 1, y1, Paleta.conAlfa(Paleta.MOHO, 0.35F));
        grafico.fill(x1 - 1, y0, x1, y1, Paleta.conAlfa(Paleta.MOHO, 0.35F));

        if (conCinta) {
            int cinta = 22;
            int centro = (x0 + x1) / 2;
            grafico.fill(centro - cinta, y0 - 4, centro + cinta, y0 + 4, Paleta.conAlfa(papel, 0.45F));
        }
    }

    /** Cuanta tinta se lee ahora mismo, de 0.10 a 1.0, segun la luz del pasillo. */
    public static float tinta() {
        return 0.10F + 0.90F * RotacionNiveles.luzDisponible();
    }
}
