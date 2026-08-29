package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Matematica y barridos compartidos por los diez recintos.
 *
 * Solo contiene infraestructura transversal. La arquitectura y los objetos
 * viven en cada Planta; las superficies procedurales, en {@link Lienzo}.
 */
public final class Trazo {
    private Trazo() { }

    public static final int SEMILLA = 0x4A4F4253;
    public static final int PASO = 2;

    public static float pseudo(int indice) {
        long h = SEMILLA + indice * 2654435761L;
        h ^= h >>> 13;
        h *= 1274126177L;
        h ^= h >>> 16;
        return Math.floorMod(h, 10000L) / 10000.0F;
    }

    public static float limitar(float valor, float minimo, float maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }

    public static float profundidad(int j, int tramos) {
        return tramos / (float) Math.max(1, j);
    }

    public static int velar(int color, int niebla, float lejos, float fuerza) {
        return Paleta.mezclar(color, niebla, lejos * lejos * fuerza);
    }

    public static float atenuar(float luz, float lejos) {
        return luz * (0.52F + 0.48F * lejos);
    }

    /** Suelo o techo resuelto por filas de profundidad constante. */
    public static void plano(GuiGraphics g, Marco m, boolean arriba,
                             int cerca, int lejos, int niebla, float luz, float velo) {
        int desde = arriba ? 0 : Math.round(m.sueloEn(1.0F));
        int hasta = arriba ? Math.round(m.techoEn(1.0F)) : m.alto();
        for (int y = desde; y < hasta; y += PASO) {
            float d = m.dy(y + PASO * 0.5F);
            if (d <= 1.0F) continue;
            float lej = limitar(1.0F / d, 0.0F, 1.0F);
            int color = velar(Paleta.mezclar(cerca, lejos, lej), niebla, lej, velo);
            g.fill(0, y, m.ancho(), Math.min(hasta, y + PASO),
                    Paleta.iluminar(color, atenuar(luz, lej)));
        }
    }

    /** Laterales con gradiente material y perspectiva real de Marco. */
    public static void paredes(GuiGraphics g, Marco m, Nivel n, float luz) {
        for (int x = 0; x < m.ancho(); x += PASO) {
            float d = m.dx(x + PASO * 0.5F);
            if (d <= 1.0F) continue;
            float lej = limitar(1.0F / d, 0.0F, 1.0F);
            float y0 = m.techoEn(d), y1 = m.sueloEn(d);
            if (y1 < 0 || y0 > m.alto()) continue;
            float at = atenuar(luz, lej);
            g.fillGradient(x, (int) y0, x + PASO, (int) y1,
                    Paleta.iluminar(velar(n.paredAlta, n.niebla, lej, .62F), at),
                    Paleta.iluminar(velar(n.paredBaja, n.niebla, lej, .52F), at));
        }
    }
}
