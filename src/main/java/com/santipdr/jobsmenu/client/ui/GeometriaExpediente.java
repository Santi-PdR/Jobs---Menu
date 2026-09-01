package com.santipdr.jobsmenu.client.ui;

/** Medidas compartidas para que las pantallas no vuelvan a ocupar el monitor entero. */
public final class GeometriaExpediente {

    private GeometriaExpediente() {
    }

    public static Panel compacto(int pantallaW, int pantallaH, int maxW, int maxH) {
        int w = Math.max(1, Math.min(maxW, pantallaW - 16));
        int h = Math.max(1, Math.min(maxH, pantallaH - 16));
        // Las pantallas vanilla dibujan su titulo en Y=20. Mantener el borde
        // superior a ocho pixeles permite compactar sin dejar ese titulo fuera.
        int y = Math.max(4, Math.min(8, (pantallaH - h) / 2));
        return new Panel((pantallaW - w) / 2, y, w, h);
    }

    public record Panel(int x, int y, int w, int h) {
        public int listaArriba() {
            return y + 50;
        }

        public int listaAbajo() {
            return y + h - 42;
        }

        public int botonY() {
            return y + h - 29;
        }
    }
}
