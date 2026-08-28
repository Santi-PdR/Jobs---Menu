package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Trazo;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Micro-eventos visuales deterministas de baja frecuencia.
 *
 * No son sustos ni una segunda presencia. Son fallos y movimientos del propio
 * edificio: una luminaria que tarda en estabilizar, vapor que cruza un conducto,
 * un reflejo que aparece donde no deberia. Se derivan del reloj para no mantener
 * timers por pantalla y para que redimensionar la ventana no reinicie nada.
 */
public final class EventosAmbientales {

    private EventosAmbientales() {
    }

    private static final long PERIODO_MS = 173_000L;
    private static final long VENTANA_MS = 5_500L;

    public static void dibujar(GuiGraphics grafico, Nivel nivel, Marco m, float luz) {
        if (!ConfigTurno.escenaViva() || ConfigTurno.movimientoReducido() || luz < 0.08F) {
            return;
        }

        long ahora = System.currentTimeMillis();
        long ciclo = Math.floorDiv(ahora, PERIODO_MS);
        long fase = Math.floorMod(ahora, PERIODO_MS);
        if (fase >= VENTANA_MS) {
            return;
        }

        // Solo dos de cada tres ciclos contienen un evento. El hueco es parte
        // del sistema: si el jugador puede esperarlo, deja de ser ambiente.
        float seleccion = Trazo.pseudo((int) (ciclo * 47L + 19L));
        if (seleccion < 0.34F) {
            return;
        }

        float t = fase / (float) VENTANA_MS;
        float campana = (float) Math.sin(Math.PI * t);
        campana *= campana;
        int tipo = (int) (Trazo.pseudo((int) (ciclo * 71L + 31L)) * 3.0F);

        switch (tipo) {
            case 0 -> reflejoLejano(grafico, nivel, m, luz, campana);
            case 1 -> sombraInstalacion(grafico, nivel, m, luz, campana);
            default -> pulsoHumedad(grafico, nivel, m, luz, campana);
        }
    }

    private static void reflejoLejano(GuiGraphics g, Nivel n, Marco m, float luz, float a) {
        float dx = 1.45F + Trazo.pseudo((int) (System.currentTimeMillis() / PERIODO_MS) + 5) * 1.1F;
        int y = Math.round(m.sueloEn(dx));
        int x0 = Math.round(m.enX(dx, -0.34F));
        int x1 = Math.round(m.enX(dx, 0.28F));
        int alto = Math.max(1, Math.round(m.h() * dx * 0.018F));
        g.fill(Math.max(0, x0), y - alto, Math.min(m.ancho(), x1), y,
                Paleta.conAlfa(Paleta.iluminar(n.luz, luz), 0.10F * a * (0.25F + n.reflejo)));
    }

    private static void sombraInstalacion(GuiGraphics g, Nivel n, Marco m, float luz, float a) {
        int ancho = Math.max(3, m.ancho() / 90);
        int x = (int) (m.ancho() * (0.18F + 0.64F * Trazo.pseudo((int) (System.currentTimeMillis() / PERIODO_MS) + 17)));
        int y0 = Math.round(m.techoEn(1.65F));
        int y1 = Math.round(m.sueloEn(1.65F));
        g.fill(x, y0, x + ancho, y1, Paleta.conAlfa(Paleta.VANO, 0.08F * a * luz));
    }

    private static void pulsoHumedad(GuiGraphics g, Nivel n, Marco m, float luz, float a) {
        if (n.humedad < 0.20F) {
            return;
        }
        int alto = Math.max(2, m.alto() / 80);
        int y = (int) (m.alto() * (0.42F + 0.30F * Trazo.pseudo((int) (System.currentTimeMillis() / PERIODO_MS) + 29)));
        g.fill(0, y, m.ancho(), y + alto,
                Paleta.conAlfa(Paleta.mezclar(n.niebla, n.luz, 0.20F), 0.035F * a * luz * n.humedad));
    }
}
