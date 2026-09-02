package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** Transicion corta entre expedientes. Nunca bloquea input ni cambia la Screen. */
public final class TransicionInterfazJobs {

    private static final long DURACION_MS = 360L;
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
            float a = (1.0F - t) * (1.0F - t) * 0.15F;
            g.fill(0, 0, pantalla.width, pantalla.height, Paleta.conAlfa(Paleta.VANO, a));
            return;
        }

        // Smoothstep evita el golpe visual que tenia el barrido cubico anterior.
        float avance = t * t * (3.0F - 2.0F * t);
        int banda = Math.max(20, Math.min(pantalla.width / 5, 92));
        int recorrido = pantalla.width + banda * 4;
        int centro = sentido > 0
                ? (int) (-banda * 2 + recorrido * avance)
                : (int) (pantalla.width + banda * 2 - recorrido * avance);
        int x0 = centro - banda;
        int x1 = centro + banda;

        int sombra0 = sentido > 0 ? x0 - 14 : x1 - 2;
        int sombra1 = sentido > 0 ? x0 + 2 : x1 + 14;
        g.fill(sombra0, 0, sombra1, pantalla.height,
                Paleta.conAlfa(Paleta.VANO, 0.24F * (1.0F - t * 0.35F)));

        // Papel frio/gris: mantiene la identidad Jobs y evita volver al amarillo vanilla.
        g.fill(x0, 0, x1, pantalla.height, Paleta.conAlfa(Paleta.UI_PAPEL, 0.58F));
        g.fill(centro - 2, 0, centro + 2, pantalla.height,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.24F));
        g.fill(centro + sentido * 9 - 1, 0, centro + sentido * 9 + 1,
                pantalla.height, Paleta.conAlfa(Paleta.UI_ACENTO, 0.16F));

        int marca = Paleta.conAlfa(Paleta.UI_TINTA, 0.18F);
        int mx = sentido > 0 ? x0 + 8 : x1 - 9;
        int tramo = Math.max(12, pantalla.height / 6);
        g.fill(mx, 10, mx + 1, Math.min(pantalla.height - 10, 10 + tramo), marca);
        g.fill(mx, Math.max(10, pantalla.height - 10 - tramo), mx + 1,
                pantalla.height - 10, marca);

        // El velo solo existe al principio. Su funcion es esconder un frame de salto de layout,
        // no oscurecer la interfaz nueva una vez que ya esta asentada.
        float velo = t < 0.42F ? (0.42F - t) / 0.42F * 0.12F : 0.0F;
        if (velo > 0.0F) {
            g.fill(0, 0, pantalla.width, pantalla.height, Paleta.conAlfa(Paleta.VANO, velo));
        }

        int rail = Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.14F * (1.0F - t));
        int rx0 = Math.max(0, Math.min(pantalla.width, centro - banda / 2));
        int rx1 = Math.max(0, Math.min(pantalla.width, centro + banda / 2));
        if (rx1 > rx0) {
            g.fill(rx0, 3, rx1, 4, rail);
            g.fill(rx0, pantalla.height - 4, rx1, pantalla.height - 3, rail);
        }
    }
}
