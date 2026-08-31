package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** Transicion corta entre expedientes. Nunca bloquea input ni cambia la Screen. */
public final class TransicionInterfazJobs {

    private static final long DURACION_MS = 280L;
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
            float a = (1.0F - t) * 0.24F;
            g.fill(0, 0, pantalla.width, pantalla.height, Paleta.conAlfa(Paleta.VANO, a));
            return;
        }

        // Una hoja pasa por delante de la camara y deja ver el expediente nuevo.
        // Sin blanco puro ni flash: el gesto pertenece al papel, no a una HUD.
        float avance = 1.0F - (1.0F - t) * (1.0F - t);
        int banda = Math.max(16, pantalla.width / 5);
        int centro = sentido > 0
                ? (int) (-banda + (pantalla.width + 2 * banda) * avance)
                : (int) (pantalla.width + banda - (pantalla.width + 2 * banda) * avance);
        int x0 = centro - banda;
        int x1 = centro + banda;
        g.fill(x0, 0, x1, pantalla.height, Paleta.conAlfa(Paleta.PAPEL, 0.56F));
        g.fill(centro - 2, 0, centro + 2, pantalla.height,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.28F));

        // La primera mitad conserva una sombra del expediente anterior.
        if (t < 0.55F) {
            float a = (0.55F - t) / 0.55F * 0.18F;
            g.fill(0, 0, pantalla.width, pantalla.height, Paleta.conAlfa(Paleta.VANO, a));
        }
    }
}
