package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Segunda pasada artistica de los recintos.
 *
 * Las plantas siguen siendo las responsables de la arquitectura base. Esta
 * clase agrega lenguaje visual compartido y motivos especificos por nivel para
 * acercar todos los fondos a una misma direccion: monumental, industrial,
 * humeda, con verdes toxicos contenidos y luz ambar puntual. No pretende ser
 * un filtro uniforme: cada recinto recibe elementos distintos.
 */
public final class DireccionArte {

    private DireccionArte() {
    }

    public static void dibujar(GuiGraphics g, int ancho, int alto,
                               Nivel nivel, float luz, float tiempo) {
        profundidad(g, ancho, alto, nivel, luz);

        switch (nivel.clave) {
            case "nivel0" -> administracion(g, ancho, alto, nivel, luz, tiempo);
            case "nivel1" -> deposito(g, ancho, alto, nivel, luz, tiempo);
            case "nivel2" -> servicio(g, ancho, alto, nivel, luz, tiempo);
            case "nivel3" -> natatorio(g, ancho, alto, nivel, luz, tiempo);
            case "nivel4" -> salaPiedra(g, ancho, alto, nivel, luz, tiempo);
            case "nivel5" -> biblioteca(g, ancho, alto, nivel, luz, tiempo);
            case "nivel6" -> invernadero(g, ancho, alto, nivel, luz, tiempo);
            case "nivel7" -> catacumbas(g, ancho, alto, nivel, luz, tiempo);
            case "nivel8" -> cisterna(g, ancho, alto, nivel, luz, tiempo);
            case "nivel9" -> trono(g, ancho, alto, nivel, luz, tiempo);
            default -> { }
        }
    }

    private static void profundidad(GuiGraphics g, int w, int h, Nivel n, float luz) {
        int fx = (int) (w * n.fugaX);
        int fy = (int) (h * n.fugaY);

        // Marco de sombra alrededor del punto de fuga: separa primer plano,
        // medio y fondo sin lavar los colores de la planta.
        for (int i = 0; i < 7; i++) {
            int margenX = Math.max(8, w / 16 + i * w / 34);
            int margenY = Math.max(6, h / 18 + i * h / 38);
            float a = (0.020F + i * 0.006F) * (1.0F - 0.35F * luz);
            int c = Paleta.conAlfa(Paleta.VANO, a);
            g.fill(0, 0, Math.max(0, fx - margenX), h, c);
            g.fill(Math.min(w, fx + margenX), 0, w, h, c);
            g.fill(0, 0, w, Math.max(0, fy - margenY), c);
        }

        // Halo de color lejano. Mantiene la identidad cromatica de cada nivel.
        int haloW = Math.max(20, w / 5);
        int haloH = Math.max(12, h / 7);
        for (int i = 4; i >= 0; i--) {
            float a = (0.012F + i * 0.008F) * luz;
            int x0 = fx - haloW - i * 9;
            int x1 = fx + haloW + i * 9;
            int y0 = fy - haloH - i * 5;
            int y1 = fy + haloH + i * 5;
            g.fill(Math.max(0, x0), Math.max(0, y0), Math.min(w, x1), Math.min(h, y1),
                    Paleta.conAlfa(n.luz, a));
        }
    }

    private static void administracion(GuiGraphics g, int w, int h, Nivel n, float luz, float t) {
        // Totems verdes inspirados en la referencia monumental. Son acentos,
        // no columnas nuevas: se pegan a los laterales y dejan respirar el centro.
        int y0 = (int) (h * 0.43F);
        int y1 = (int) (h * 0.73F);
        torreLuz(g, (int) (w * 0.19F), y0, y1, n.luz, luz, t, 0.0F);
        torreLuz(g, (int) (w * 0.81F), y0, y1, n.luz, luz, t, 1.3F);
        runas(g, (int) (w * 0.12F), (int) (h * 0.48F), n.luz, luz, 5);
        runas(g, (int) (w * 0.88F), (int) (h * 0.48F), n.luz, luz, 5);
    }

    private static void deposito(GuiGraphics g, int w, int h, Nivel n, float luz, float t) {
        // Verticalidad industrial: pilares negros, puntos verdes de mantenimiento
        // y charcos de luz muy bajos para que el espacio parezca enorme.
        for (int i = 0; i < 4; i++) {
            int x = (int) (w * (0.16F + i * 0.23F));
            g.fill(x, (int) (h * 0.24F), x + Math.max(3, w / 180), (int) (h * 0.78F),
                    Paleta.conAlfa(Paleta.VANO, 0.28F));
            int cy = (int) (h * (0.48F + (i % 2) * 0.06F));
            pulso(g, x - 2, cy, n.luz, luz, t, i * 1.1F);
        }
    }

    private static void servicio(GuiGraphics g, int w, int h, Nivel n, float luz, float t) {
        // Haz de tuberias y abrazaderas en perspectiva. Se dibujan altos para no
        // ensuciar la zona de lectura del aviso.
        int y = (int) (h * 0.19F);
        for (int i = 0; i < 5; i++) {
            int yy = y + i * Math.max(3, h / 70);
            int c = Paleta.mezclar(n.paredBaja, n.luz, 0.14F + i * 0.035F);
            g.fill(0, yy, w, yy + Math.max(1, h / 180), Paleta.conAlfa(c, 0.25F * luz));
        }
        int calor = (int) ((Math.sin(t * 0.7F) * 0.5F + 0.5F) * 18);
        g.fill((int) (w * 0.70F), (int) (h * 0.52F), (int) (w * 0.94F), (int) (h * 0.54F),
                Paleta.conAlfa(n.luz, (0.025F + calor / 900.0F) * luz));
    }

    private static void natatorio(GuiGraphics g, int w, int h, Nivel n, float luz, float t) {
        causticas(g, w, h, n, luz, t, 0.64F, 8);
        // Reflejo vertical de luminarias sobre agua, roto en segmentos.
        for (int i = 0; i < 4; i++) {
            int x = (int) (w * (0.28F + i * 0.15F));
            for (int s = 0; s < 5; s++) {
                int yy = (int) (h * 0.60F) + s * Math.max(3, h / 45);
                int drift = (int) (Math.sin(t * 1.3F + i + s) * 4.0F);
                g.fill(x + drift, yy, x + drift + Math.max(2, w / 90), yy + 1,
                        Paleta.conAlfa(n.luz, 0.055F * luz * (1.0F - s * 0.12F)));
            }
        }
    }

    private static void salaPiedra(GuiGraphics g, int w, int h, Nivel n, float luz, float t) {
        // Cadenas y charcos ambar como en las referencias de lobby de piedra.
        cadena(g, (int) (w * 0.18F), 0, (int) (h * 0.31F), n.junta, luz, 8);
        cadena(g, (int) (w * 0.82F), 0, (int) (h * 0.25F), n.junta, luz, 7);
        antorcha(g, (int) (w * 0.14F), (int) (h * 0.54F), n.luz, luz, t, 0.0F);
        antorcha(g, (int) (w * 0.86F), (int) (h * 0.50F), n.luz, luz, t, 1.8F);
    }

    private static void biblioteca(GuiGraphics g, int w, int h, Nivel n, float luz, float t) {
        // Brillos de lamparas verdes muy bajos y lineas de lomos en profundidad.
        int verde = 0xFF8FAE68;
        for (int lado : new int[] { -1, 1 }) {
            int x = lado < 0 ? (int) (w * 0.24F) : (int) (w * 0.76F);
            pulso(g, x, (int) (h * 0.44F), verde, luz * 0.75F, t, lado);
        }
        for (int i = 0; i < 6; i++) {
            int y = (int) (h * (0.28F + i * 0.065F));
            g.fill((int) (w * 0.08F), y, (int) (w * 0.28F), y + 1,
                    Paleta.conAlfa(n.luz, 0.025F * luz));
            g.fill((int) (w * 0.72F), y, (int) (w * 0.92F), y + 1,
                    Paleta.conAlfa(n.luz, 0.025F * luz));
        }
    }

    private static void invernadero(GuiGraphics g, int w, int h, Nivel n, float luz, float t) {
        // Rayos altos de luz natural y siluetas vegetales en bordes.
        for (int i = 0; i < 4; i++) {
            int x = (int) (w * (0.30F + i * 0.13F));
            int dx = (int) (Math.sin(t * 0.09F + i) * 5.0F);
            g.fill(x + dx, 0, x + dx + Math.max(2, w / 130), (int) (h * 0.62F),
                    Paleta.conAlfa(n.luz, 0.028F * luz));
        }
        hojas(g, 0, h, w, n.paredBaja, luz, t, false);
        hojas(g, w, h, w, n.paredBaja, luz, t, true);
    }

    private static void catacumbas(GuiGraphics g, int w, int h, Nivel n, float luz, float t) {
        cadena(g, (int) (w * 0.48F), 0, (int) (h * 0.28F), n.junta, luz, 9);
        antorcha(g, (int) (w * 0.13F), (int) (h * 0.47F), n.luz, luz * 0.85F, t, 0.7F);
        // Los nichos los dibuja la escena de la planta (Catacumba.nichos),
        // excavados con su alfeizar. Los que habia aca, con dobles bordes
        // claros sobre un fondo plano, se superponian a esos y se leian como
        // cuadros flotantes en la pared: retirados en la revision 0.9.0.
    }

    private static void cisterna(GuiGraphics g, int w, int h, Nivel n, float luz, float t) {
        causticas(g, w, h, n, luz, t, 0.58F, 10);
        // Puntos de mantenimiento verde sumergidos, como eco de la referencia.
        int verde = 0xFF62FF65;
        for (int i = 0; i < 5; i++) {
            int x = (int) (w * (0.16F + i * 0.17F));
            int y = (int) (h * (0.60F + (i % 2) * 0.045F));
            pulso(g, x, y, verde, luz * 0.55F, t, i * 0.8F);
        }
    }

    private static void trono(GuiGraphics g, int w, int h, Nivel n, float luz, float t) {
        // Sala ceremonial en ruinas: cadenas verticales, luz central y antorchas
        // laterales. El rojo de la referencia NO se copia: queda reservado a
        // Executores por identidad del mod.
        cadena(g, (int) (w * 0.22F), 0, (int) (h * 0.34F), n.junta, luz, 10);
        cadena(g, (int) (w * 0.78F), 0, (int) (h * 0.29F), n.junta, luz, 9);
        g.fill((int) (w * 0.47F), 0, (int) (w * 0.53F), (int) (h * 0.70F),
                Paleta.conAlfa(n.luz, 0.035F * luz));
        antorcha(g, (int) (w * 0.18F), (int) (h * 0.55F), n.luz, luz, t, 0.2F);
        antorcha(g, (int) (w * 0.82F), (int) (h * 0.55F), n.luz, luz, t, 2.2F);
    }

    private static void torreLuz(GuiGraphics g, int x, int y0, int y1,
                                 int color, float luz, float t, float fase) {
        int ancho = Math.max(8, (y1 - y0) / 12);
        g.fill(x - ancho, y0, x + ancho, y1, Paleta.conAlfa(Paleta.VANO, 0.48F));
        int paso = Math.max(10, (y1 - y0) / 4);
        for (int y = y0 + 8; y < y1 - 4; y += paso) {
            pulso(g, x, y, color, luz, t, fase + y * 0.01F);
        }
    }

    private static void runas(GuiGraphics g, int x, int y, int color, float luz, int cantidad) {
        int tam = 2;
        for (int i = 0; i < cantidad; i++) {
            int yy = y + i * 7;
            g.fill(x, yy, x + tam, yy + 4, Paleta.conAlfa(color, 0.36F * luz));
            g.fill(x + 3, yy + (i % 2), x + 5, yy + 2 + (i % 3),
                    Paleta.conAlfa(color, 0.22F * luz));
        }
    }

    private static void pulso(GuiGraphics g, int x, int y, int color,
                              float luz, float t, float fase) {
        float p = 0.75F + 0.25F * (float) Math.sin(t * 1.5F + fase);
        int r = 4;
        g.fill(x - r * 2, y - r * 2, x + r * 2, y + r * 2,
                Paleta.conAlfa(color, 0.025F * luz * p));
        g.fill(x - r, y - r, x + r, y + r,
                Paleta.conAlfa(color, 0.11F * luz * p));
        g.fill(x - 2, y - 2, x + 2, y + 2,
                Paleta.conAlfa(color, 0.75F * luz * p));
    }

    /**
     * Antorcha de pared con soporte de hierro.
     *
     * El soporte es lo que evita que la llama se lea como un rectangulo
     * flotando: una mensula vertical anclada a la pared, un brazo que sale
     * hacia la escena y una copa donde descansa el fuego. La llama titila con
     * su propia fase, como en la cripta.
     */
    private static void antorcha(GuiGraphics g, int x, int y, int color,
                                 float luz, float t, float fase) {
        float p = 0.75F + 0.25F * (float) Math.sin(t * 7.0F + fase);
        int hierro = Paleta.mezclar(Paleta.VANO, color, 0.22F);
        // Soporte vertical contra la pared (quien sostiene la mensula).
        g.fill(x - 1, y - 14, x + 2, y + 20, Paleta.conAlfa(hierro, 0.85F));
        // Brazo: sale de la pared y termina debajo de la llama.
        g.fill(x - 1, y + 8, x + 6, y + 12, Paleta.conAlfa(hierro, 0.80F));
        // Copa del fuego.
        g.fill(x, y + 2, x + 4, y + 9, Paleta.conAlfa(hierro, 0.90F));
        // Derrame calido.
        g.fill(x - 20, y - 16, x + 24, y + 18,
                Paleta.conAlfa(color, 0.026F * luz * p));
        g.fill(x - 8, y - 8, x + 12, y + 10,
                Paleta.conAlfa(color, 0.075F * luz * p));
        // Llama: nucleo y punta clara.
        g.fill(x, y - 8, x + 5, y + 3,
                Paleta.conAlfa(color, 0.80F * luz));
        g.fill(x + 1, y - 12, x + 4, y - 6,
                Paleta.conAlfa(Paleta.mezclar(color, 0xFFF3D8, 0.60F), 0.70F * luz * p));
    }

    private static void cadena(GuiGraphics g, int x, int y0, int y1,
                               int color, float luz, int eslabones) {
        int paso = Math.max(5, (y1 - y0) / Math.max(1, eslabones));
        for (int y = y0; y < y1; y += paso) {
            boolean vertical = ((y - y0) / paso) % 2 == 0;
            int rx = vertical ? 3 : 5;
            int ry = vertical ? 5 : 3;
            int c = Paleta.mezclar(Paleta.VANO, color, 0.55F);
            g.fill(x - rx, y, x + rx, y + 1, Paleta.conAlfa(c, 0.70F * luz));
            g.fill(x - rx, y + ry * 2, x + rx, y + ry * 2 + 1, Paleta.conAlfa(c, 0.55F * luz));
            g.fill(x - rx, y, x - rx + 1, y + ry * 2, Paleta.conAlfa(c, 0.62F * luz));
            g.fill(x + rx - 1, y, x + rx, y + ry * 2, Paleta.conAlfa(c, 0.62F * luz));
        }
    }

    private static void causticas(GuiGraphics g, int w, int h, Nivel n,
                                  float luz, float t, float desdeY, int lineas) {
        for (int i = 0; i < lineas; i++) {
            float f = i / (float) Math.max(1, lineas - 1);
            int y = (int) (h * desdeY + f * h * (1.0F - desdeY));
            int x = (int) (w * ((i * 0.173F + 0.11F) % 0.86F));
            int deriva = (int) (Math.sin(t * (0.65F + i * 0.03F) + i) * w * 0.018F);
            int largo = Math.max(10, w / 18 - i * 2);
            g.fill(x + deriva, y, Math.min(w, x + deriva + largo), y + 1,
                    Paleta.conAlfa(n.luz, (0.020F + 0.025F * (1.0F - f)) * luz * n.reflejo));
        }
    }

    private static void hojas(GuiGraphics g, int borde, int h, int w,
                              int color, float luz, float t, boolean derecha) {
        for (int i = 0; i < 9; i++) {
            int y = (int) (h * (0.20F + i * 0.075F));
            int largo = 10 + (i % 4) * 6;
            int deriva = (int) (Math.sin(t * 0.30F + i) * 3.0F);
            int x0 = derecha ? w - largo - deriva : borde + deriva;
            int x1 = derecha ? w : largo + deriva;
            g.fill(Math.max(0, x0), y, Math.min(w, x1), y + 2,
                    Paleta.conAlfa(color, 0.26F * luz));
        }
    }

}
