package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** Transicion corta entre expedientes. Nunca bloquea input ni cambia la Screen. */
public final class TransicionInterfazJobs {

    private static final long DURACION_MS = 430L;
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
        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()) {
            float a = (1.0F - t) * (1.0F - t) * 0.14F;
            g.fill(0, 0, pantalla.width, pantalla.height, Paleta.conAlfa(Paleta.VANO, a));
            int cx = pantalla.width / 2;
            g.fill(cx - 12, 2, cx + 12, 3,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.08F * (1.0F - t)));
            return;
        }

        float avance = t * t * (3.0F - 2.0F * t);
        int banda = Math.max(22, Math.min(pantalla.width / 5, 96));
        int recorrido = pantalla.width + banda * 4;
        int centro = sentido > 0
                ? (int) (-banda * 2 + recorrido * avance)
                : (int) (pantalla.width + banda * 2 - recorrido * avance);
        int x0 = centro - banda;
        int x1 = centro + banda;

        int cola0 = sentido > 0 ? x0 - 30 : x1 + 5;
        int cola1 = sentido > 0 ? x0 - 5 : x1 + 30;
        g.fill(Math.min(cola0, cola1), 0, Math.max(cola0, cola1), pantalla.height,
                Paleta.conAlfa(Paleta.VANO, 0.12F * (1.0F - t)));

        int sombra0 = sentido > 0 ? x0 - 17 : x1 - 2;
        int sombra1 = sentido > 0 ? x0 + 2 : x1 + 17;
        g.fill(Math.min(sombra0, sombra1), 0, Math.max(sombra0, sombra1), pantalla.height,
                Paleta.conAlfa(Paleta.VANO, 0.25F * (1.0F - t * 0.35F)));

        g.fill(x0, 0, x1, pantalla.height, Paleta.conAlfa(Paleta.UI_PAPEL, 0.60F));
        g.fill(x0 + 4, 0, x0 + 5, pantalla.height,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.12F));
        g.fill(x1 - 5, 0, x1 - 4, pantalla.height,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.08F));
        g.fill(centro - 2, 0, centro + 2, pantalla.height,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.25F));
        g.fill(centro + sentido * 9 - 1, 0, centro + sentido * 9 + 1,
                pantalla.height, Paleta.conAlfa(Paleta.UI_ACENTO, 0.17F));

        int marca = Paleta.conAlfa(Paleta.UI_TINTA, 0.18F);
        int mx = sentido > 0 ? x0 + 9 : x1 - 10;
        int tramo = Math.max(12, pantalla.height / 6);
        g.fill(mx, 10, mx + 1, Math.min(pantalla.height - 10, 10 + tramo), marca);
        g.fill(mx, Math.max(10, pantalla.height - 10 - tramo), mx + 1,
                pantalla.height - 10, marca);

        if (pantalla.height > 100) {
            for (int y = 22; y < pantalla.height - 18; y += 42) {
                g.fill(centro - 1, y, centro + 1, y + 2,
                        Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.13F));
            }
        }

        int registro = Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.14F * (1.0F - t * 0.45F));
        g.fill(centro - 12, 3, centro + 12, 4, registro);
        g.fill(centro, 1, centro + 1, 7, registro);
        g.fill(centro - 8, pantalla.height - 4, centro + 8, pantalla.height - 3,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.12F * (1.0F - t)));

        if (pantalla.width > 280) {
            int labelY = Math.max(10, pantalla.height / 2 - 18);
            int labelX = sentido > 0 ? x0 + 12 : x1 - 42;
            g.fill(labelX, labelY, labelX + 30, labelY + 1,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.10F * (1.0F - t)));
            g.fill(labelX, labelY + 5, labelX + 20, labelY + 6,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.075F * (1.0F - t)));
        }

        float velo = t < 0.40F ? (0.40F - t) / 0.40F * 0.11F : 0.0F;
        if (velo > 0.0F) {
            g.fill(0, 0, pantalla.width, pantalla.height, Paleta.conAlfa(Paleta.VANO, velo));
        }

        int rail = Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.15F * (1.0F - t));
        int rx0 = Math.max(0, Math.min(pantalla.width, centro - banda / 2));
        int rx1 = Math.max(0, Math.min(pantalla.width, centro + banda / 2));
        if (rx1 > rx0) {
            g.fill(rx0, 3, rx1, 4, rail);
            g.fill(rx0, pantalla.height - 4, rx1, pantalla.height - 3, rail);
            int tercio = rx0 + Math.max(1, (rx1 - rx0) / 3);
            g.fill(tercio, 5, tercio + 1, 9,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.10F * (1.0F - t)));
        }
    }
}
