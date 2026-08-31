package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Capa de acabado comun para todos los recintos.
 *
 * No reemplaza la direccion artistica de cada nivel: agrega fenomenos que
 * pertenecen a la camara y a la instalacion (luz residual, barrido de exposicion
 * y cuerpo de la transicion). Todo respeta movimiento reducido y bajo consumo.
 */
public final class PulidoEscena {

    private PulidoEscena() {
    }

    public static void dibujar(GuiGraphics g, int ancho, int alto, Nivel nivel,
                               float luz, float tiempo, RotacionNiveles.Estado estado,
                               boolean movimiento, boolean bajoConsumo) {
        if (!ConfigTurno.escenaViva()) {
            return;
        }

        haloFluorescente(g, ancho, alto, nivel, luz, tiempo, bajoConsumo);

        if (movimiento && !bajoConsumo && !estado.enSuspension()) {
            barridoExposicion(g, ancho, alto, nivel, luz, tiempo);
        }

        if (estado.enTransicion()) {
            transicionFisica(g, ancho, alto, nivel, estado.avanceTransicion());
        }

        if (estado.enSuspension()) {
            suspension(g, ancho, alto, estado.avanceSuspension());
        }
    }

    /** Rebote suave de la luz principal sobre la parte alta del encuadre. */
    private static void haloFluorescente(GuiGraphics g, int ancho, int alto,
                                         Nivel nivel, float luz, float tiempo,
                                         boolean bajoConsumo) {
        float pulso = bajoConsumo ? 0.5F
                : 0.5F + 0.5F * (float) Math.sin(tiempo * 0.19F + nivel.fugaX * 4.0F);
        float alfa = (0.018F + 0.022F * pulso) * luz;
        int altoHalo = Math.max(8, alto / 8);
        int color = Paleta.conAlfa(nivel.luz, alfa);
        g.fill(0, 0, ancho, altoHalo, color);
    }

    /**
     * Una linea de exposicion muy tenue que deriva lentamente. No es un efecto
     * CRT: se lee como fluorescente inestable/camara acomodandose a la escena.
     */
    private static void barridoExposicion(GuiGraphics g, int ancho, int alto,
                                          Nivel nivel, float luz, float tiempo) {
        int recorrido = Math.max(1, alto + 80);
        int y = Math.floorMod((int) (tiempo * 1.35F + nivel.fugaY * 173.0F), recorrido) - 40;
        if (y < 0 || y >= alto) {
            return;
        }
        float alfa = 0.016F * luz;
        g.fill(0, y, ancho, Math.min(alto, y + 1), Paleta.conAlfa(nivel.luz, alfa));
        if (y + 3 < alto) {
            g.fill(0, y + 3, ancho, y + 4, Paleta.conAlfa(Paleta.FLUOR, alfa * 0.35F));
        }
    }

    /**
     * Da masa al apagado. El recinto no cambia con un simple fade: la oscuridad
     * entra desde los bordes y, cuando vuelve la corriente, una banda de luz
     * atraviesa el centro antes de estabilizarse.
     */
    private static void transicionFisica(GuiGraphics g, int ancho, int alto,
                                         Nivel nivel, float avance) {
        float t = limitar(avance, 0.0F, 1.0F);
        float reparto = RotacionNiveles.repartoApagado();

        if (t < reparto) {
            float p = suavizar(t / Math.max(0.001F, reparto));
            int lateral = Math.round(ancho * 0.20F * p);
            int vertical = Math.round(alto * 0.16F * p);
            int sombra = Paleta.conAlfa(Paleta.VANO, 0.16F + 0.34F * p);
            g.fill(0, 0, lateral, alto, sombra);
            g.fill(ancho - lateral, 0, ancho, alto, sombra);
            g.fill(0, 0, ancho, vertical, sombra);
            g.fill(0, alto - vertical, ancho, alto, sombra);
            return;
        }

        float p = suavizar((t - reparto) / Math.max(0.001F, 1.0F - reparto));
        float destello = (float) Math.sin(Math.PI * limitar(p * 1.18F, 0.0F, 1.0F));
        if (destello > 0.01F) {
            int y = Math.round(alto * (0.44F + 0.10F * p));
            int grosor = Math.max(1, Math.round(1.0F + 2.0F * destello));
            g.fill(0, y, ancho, Math.min(alto, y + grosor),
                    Paleta.conAlfa(nivel.luz, 0.10F * destello));
        }
    }

    /** Durante La Suspension queda un borde opresivo, sin parpadeos. */
    private static void suspension(GuiGraphics g, int ancho, int alto, float avance) {
        float centro = 1.0F - Math.abs(2.0F * limitar(avance, 0.0F, 1.0F) - 1.0F);
        float alfa = 0.08F + 0.12F * centro;
        int bordeX = Math.max(10, ancho / 12);
        int bordeY = Math.max(8, alto / 14);
        int sombra = Paleta.conAlfa(Paleta.VANO, alfa);
        g.fill(0, 0, bordeX, alto, sombra);
        g.fill(ancho - bordeX, 0, ancho, alto, sombra);
        g.fill(0, 0, ancho, bordeY, sombra);
        g.fill(0, alto - bordeY, ancho, alto, sombra);
    }

    private static float suavizar(float t) {
        t = limitar(t, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float limitar(float valor, float minimo, float maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}
