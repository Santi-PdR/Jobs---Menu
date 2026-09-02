package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** Transicion corta entre expedientes. Nunca bloquea input ni cambia la Screen. */
public final class TransicionInterfazJobs {

    private static final long DURACION_MS = 320L;
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
        if (ConfigTurno.movimientoReducido()) {
            float a = (1.0F - t) * 0.20F;
            g.fill(0, 0, pantalla.width, pantalla.height, Paleta.conAlfa(Paleta.VANO, a));
            return;
        }

        float avance = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
        int banda = Math.max(22, pantalla.width / 6);
        int centro = sentido > 0
                ? (int) (-banda * 2 + (pantalla.width + banda * 4) * avance)
                : (int) (pantalla.width + banda * 2 - (pantalla.width + banda * 4) * avance);
        int x0 = centro - banda;
        int x1 = centro + banda;

        int sx0 = sentido > 0 ? x0 - 12 : x1 - 2;
        int sx1 = sentido > 0 ? x0 + 2 : x1 + 12;
        g.fill(sx0, 0, sx1, pantalla.height, Paleta.conAlfa(Paleta.VANO, 0.30F));

        // La carpeta que cruza es interfaz: papel frio y gris, no el papel
        // amarillento que existe fisicamente dentro de algunos niveles.
        g.fill(x0, 0, x1, pantalla.height, Paleta.conAlfa(Paleta.UI_PAPEL, 0.64F));
        g.fill(centro - 3, 0, centro + 3, pantalla.height,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.26F));
        g.fill(centro + sentido * 8 - 1, 0, centro + sentido * 8 + 1, pantalla.height,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.18F));

        int marca = Paleta.conAlfa(Paleta.UI_TINTA, 0.22F);
        int mx = sentido > 0 ? x0 + 7 : x1 - 8;
        g.fill(mx, 10, mx + 1, Math.max(11, pantalla.height / 5), marca);
        g.fill(mx, pantalla.height - Math.max(11, pantalla.height / 5), mx + 1,
                pantalla.height - 10, marca);

        float velo = t < 0.52F ? (0.52F - t) / 0.52F * 0.16F : 0.0F;
        if (velo > 0.0F) {
            g.fill(0, 0, pantalla.width, pantalla.height, Paleta.conAlfa(Paleta.VANO, velo));
        }

        int rail = Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.18F * (1.0F - t));
        int rx0 = Math.max(0, Math.min(pantalla.width, centro - banda / 2));
        int rx1 = Math.max(0, Math.min(pantalla.width, centro + banda / 2));
        if (rx1 > rx0) {
            g.fill(rx0, 3, rx1, 4, rail);
            g.fill(rx0, pantalla.height - 4, rx1, pantalla.height - 3, rail);
        }
    }
}
