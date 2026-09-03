package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** Transicion corta entre expedientes. Nunca bloquea input ni cambia la Screen. */
public final class TransicionInterfazJobs {

    private static final long DURACION_MS = 470L;
    private static long inicio;
    private static int sentido = 1;

    private TransicionInterfazJobs() {
    }

    public static void notificar(Screen desde, Screen hasta) {
        if (hasta == null || desde == hasta) return;
        inicio = System.currentTimeMillis();
        String a = desde == null ? "" : desde.getClass().getName();
        String b = hasta.getClass().getName();
        sentido = a.compareTo(b) <= 0 ? 1 : -1;
    }

    public static void dibujar(Screen pantalla, GuiGraphics g) {
        if (pantalla == null || inicio <= 0L) return;
        long transcurrido = System.currentTimeMillis() - inicio;
        if (transcurrido < 0L || transcurrido >= DURACION_MS) return;

        float t = transcurrido / (float) DURACION_MS;
        float restante = 1.0F - t;
        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()) {
            float a = restante * restante * 0.14F;
            g.fill(0, 0, pantalla.width, pantalla.height, Paleta.conAlfa(Paleta.VANO, a));
            int cx = pantalla.width / 2;
            g.fill(cx - 16, 2, cx + 16, 3,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.09F * restante));
            g.fill(cx, 0, cx + 1, 6,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.08F * restante));
            g.fill(cx - 8, pantalla.height - 3, cx + 9, pantalla.height - 2,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.065F * restante));
            return;
        }

        float avance = t * t * (3.0F - 2.0F * t);
        int banda = Math.max(24, Math.min(pantalla.width / 5, 104));
        int recorrido = pantalla.width + banda * 4;
        int centro = sentido > 0
                ? (int) (-banda * 2 + recorrido * avance)
                : (int) (pantalla.width + banda * 2 - recorrido * avance);
        int x0 = centro - banda;
        int x1 = centro + banda;

        int cola0 = sentido > 0 ? x0 - 34 : x1 + 5;
        int cola1 = sentido > 0 ? x0 - 5 : x1 + 34;
        g.fill(Math.min(cola0, cola1), 0, Math.max(cola0, cola1), pantalla.height,
                Paleta.conAlfa(Paleta.VANO, 0.13F * restante));

        int sombra0 = sentido > 0 ? x0 - 19 : x1 - 2;
        int sombra1 = sentido > 0 ? x0 + 3 : x1 + 19;
        g.fill(Math.min(sombra0, sombra1), 0, Math.max(sombra0, sombra1), pantalla.height,
                Paleta.conAlfa(Paleta.VANO, 0.28F * (1.0F - t * 0.38F)));

        // Hoja principal y doble borde fisico.
        g.fill(x0, 0, x1, pantalla.height, Paleta.conAlfa(Paleta.UI_PAPEL, 0.64F));
        g.fill(x0 + 3, 0, x0 + 4, pantalla.height,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.14F));
        g.fill(x0 + 7, 0, x0 + 8, pantalla.height,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.055F));
        g.fill(x1 - 5, 0, x1 - 4, pantalla.height,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.09F));
        g.fill(x1 - 9, 0, x1 - 8, pantalla.height,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.040F));
        g.fill(centro - 2, 0, centro + 2, pantalla.height,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.26F));
        g.fill(centro + sentido * 10 - 1, 0, centro + sentido * 10 + 1,
                pantalla.height, Paleta.conAlfa(Paleta.UI_ACENTO, 0.18F));

        // Registro vertical dividido en tramos.
        int marca = Paleta.conAlfa(Paleta.UI_TINTA, 0.19F);
        int mx = sentido > 0 ? x0 + 10 : x1 - 11;
        int tramo = Math.max(14, pantalla.height / 7);
        g.fill(mx, 10, mx + 1, Math.min(pantalla.height - 10, 10 + tramo), marca);
        g.fill(mx, Math.max(10, pantalla.height / 2 - tramo / 3), mx + 1,
                Math.min(pantalla.height - 10, pantalla.height / 2 + tramo / 3),
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.11F));
        g.fill(mx, Math.max(10, pantalla.height - 10 - tramo), mx + 1,
                pantalla.height - 10, marca);

        // Perforaciones/segmentos de archivador sobre el eje de la hoja.
        if (pantalla.height > 100) {
            for (int y = 20; y < pantalla.height - 18; y += 38) {
                int tam = (y / 38) % 2 == 0 ? 2 : 1;
                g.fill(centro - tam, y, centro + tam, y + 2,
                        Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.14F));
                g.fill(centro + sentido * 10 - 1, y + 7,
                        centro + sentido * 10 + 1, y + 9,
                        Paleta.conAlfa(Paleta.UI_ACENTO, 0.07F));
            }
        }

        // Marcas superior e inferior de alineacion.
        int registro = Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.15F * (1.0F - t * 0.42F));
        g.fill(centro - 15, 3, centro + 15, 4, registro);
        g.fill(centro, 1, centro + 1, 8, registro);
        g.fill(centro - 9, pantalla.height - 4, centro + 10, pantalla.height - 3,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.13F * restante));
        g.fill(centro, pantalla.height - 7, centro + 1, pantalla.height - 2,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.08F * restante));

        // Ficha de expediente sugerida dentro de la hoja en pantallas amplias.
        if (pantalla.width > 300 && pantalla.height > 150) {
            int labelY = Math.max(12, pantalla.height / 2 - 24);
            int labelX = sentido > 0 ? x0 + 13 : x1 - 50;
            g.fill(labelX, labelY, labelX + 36, labelY + 1,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.11F * restante));
            g.fill(labelX, labelY + 5, labelX + 25, labelY + 6,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.080F * restante));
            g.fill(labelX, labelY + 10, labelX + 18, labelY + 11,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.060F * restante));
            g.fill(labelX + 31, labelY + 4, labelX + 32, labelY + 13,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.055F * restante));
        }

        // Velo inicial: oculta el cambio de geometria sin hacer un flash fuerte.
        float velo = t < 0.42F ? (0.42F - t) / 0.42F * 0.12F : 0.0F;
        if (velo > 0.0F) {
            g.fill(0, 0, pantalla.width, pantalla.height, Paleta.conAlfa(Paleta.VANO, velo));
        }

        // Rails que acompanan al expediente y dan direccion al movimiento.
        int rail = Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.16F * restante);
        int rx0 = Math.max(0, Math.min(pantalla.width, centro - banda / 2));
        int rx1 = Math.max(0, Math.min(pantalla.width, centro + banda / 2));
        if (rx1 > rx0) {
            g.fill(rx0, 3, rx1, 4, rail);
            g.fill(rx0, pantalla.height - 4, rx1, pantalla.height - 3, rail);
            int tercio = rx0 + Math.max(1, (rx1 - rx0) / 3);
            int dosTercios = rx0 + Math.max(2, (rx1 - rx0) * 2 / 3);
            g.fill(tercio, 5, tercio + 1, 10,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.11F * restante));
            g.fill(dosTercios, pantalla.height - 10, dosTercios + 1, pantalla.height - 5,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.08F * restante));
        }

        // Segunda sombra de salida para que el papel no desaparezca de golpe.
        if (t > 0.62F) {
            float salida = (t - 0.62F) / 0.38F;
            int borde = Math.max(1, Math.round(9.0F * salida));
            int alpha = Paleta.conAlfa(Paleta.VANO, 0.055F * salida);
            g.fill(0, 0, pantalla.width, borde, alpha);
            g.fill(0, pantalla.height - borde, pantalla.width, pantalla.height, alpha);
        }
    }
}
