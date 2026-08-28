package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Trazo;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Acabado comun de los recintos.
 *
 * Las plantas construyen la arquitectura; esta clase hace el trabajo que una
 * camara real haria despues: separa planos con bruma, deja que la luminaria
 * contamine el aire y agrega una patina minima sobre el lente. No sustituye el
 * detalle propio de cada nivel y, sobre todo, no lo repinta con una textura
 * generica. Su trabajo es evitar que rectangulos correctos se lean planos.
 */
public final class AcabadoEscena {

    private AcabadoEscena() {
    }

    /**
     * Bruma de profundidad y halo de la fuente dominante.
     *
     * Se dibuja despues de la arquitectura: los objetos lejanos pierden un
     * poco de contraste, mientras que el primer plano conserva negros firmes.
     */
    public static void profundidad(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        final int capas = 9;
        for (int i = capas; i >= 1; i--) {
            float d = 1.0F + i * 0.22F;
            int x0 = Math.max(0, Math.round(m.izq(d)));
            int x1 = Math.min(m.ancho(), Math.round(m.der(d)));
            int y0 = Math.max(0, Math.round(m.techoEn(d)));
            int y1 = Math.min(m.alto(), Math.round(m.sueloEn(d)));
            if (x1 <= x0 || y1 <= y0) {
                continue;
            }
            float lejos = 1.0F / d;
            float alfa = (0.010F + 0.020F * lejos * lejos) * luz;
            int color = Paleta.conAlfa(nivel.niebla, alfa);
            int borde = Math.max(1, Math.min(3, Math.round(Math.min(m.w(), m.h()) * d * 0.018F)));
            grafico.fill(x0, y0, x1, Math.min(y1, y0 + borde), color);
            grafico.fill(x0, Math.max(y0, y1 - borde), x1, y1, color);
            grafico.fill(x0, y0, Math.min(x1, x0 + borde), y1, color);
            grafico.fill(Math.max(x0, x1 - borde), y0, x1, y1, color);
        }

        // El aire alrededor de la fuga recoge el color de la luminaria. Son
        // capas casi transparentes: se percibe volumen, no un rectangulo.
        for (int i = 7; i >= 1; i--) {
            float escala = i / 7.0F;
            int rx = Math.max(2, Math.round(m.w() * (0.55F + escala * 2.8F)));
            int ry = Math.max(2, Math.round(m.h() * (0.45F + escala * 2.2F)));
            int x0 = Math.max(0, Math.round(m.fx()) - rx);
            int x1 = Math.min(m.ancho(), Math.round(m.fx()) + rx);
            int y0 = Math.max(0, Math.round(m.fy()) - ry);
            int y1 = Math.min(m.alto(), Math.round(m.fy()) + ry);
            float alfa = (0.006F + (1.0F - escala) * 0.006F) * luz;
            grafico.fill(x0, y0, x1, y1, Paleta.conAlfa(nivel.luz, alfa));
        }
    }

    /**
     * Grano material y marcas de humedad pegadas a los bordes de la vista.
     * Deterministas: no chisporrotean de un fotograma al siguiente.
     */
    public static void patina(GuiGraphics grafico, int ancho, int alto,
                              Nivel nivel, float luz, float tiempo, boolean movimiento) {
        int semillaNivel = nivel.numero() * 997;
        int total = 54 + Math.round(nivel.humedad * 42.0F);
        for (int i = 0; i < total; i++) {
            float nx = Trazo.pseudo(semillaNivel + i * 5);
            float ny = Trazo.pseudo(semillaNivel + i * 5 + 1);
            // La mayoria vive cerca del borde; el centro queda limpio para que
            // la arquitectura y la hoja sigan siendo legibles.
            if (nx > 0.16F && nx < 0.84F && ny > 0.12F && ny < 0.88F
                    && Trazo.pseudo(semillaNivel + i * 5 + 2) < 0.78F) {
                continue;
            }
            int x = Math.round(nx * (ancho - 1));
            int y = Math.round(ny * (alto - 1));
            int largo = Trazo.pseudo(semillaNivel + i * 5 + 3) > 0.86F ? 2 : 1;
            boolean claro = Trazo.pseudo(semillaNivel + i * 5 + 4) > 0.58F;
            int tinte = claro ? nivel.luz : Paleta.VANO;
            float alfa = (claro ? 0.035F : 0.045F) * (0.45F + 0.55F * luz);
            grafico.fill(x, y, Math.min(ancho, x + largo), y + 1, Paleta.conAlfa(tinte, alfa));
        }

        // En los recintos humedos hay rastros verticales muy tenues en el
        // cristal imaginario de la camara. Movimiento reducido los congela.
        if (nivel.humedad < 0.55F) {
            return;
        }
        int rastros = 3 + Math.round(nivel.humedad * 5.0F);
        for (int i = 0; i < rastros; i++) {
            float base = Trazo.pseudo(semillaNivel + 500 + i * 3);
            int x = base < 0.5F
                    ? Math.round(base * ancho * 0.24F)
                    : ancho - Math.round((1.0F - base) * ancho * 0.24F);
            float avance = movimiento ? (tiempo * (0.006F + i * 0.0007F)) % 1.0F : 0.35F;
            int y = Math.round((Trazo.pseudo(semillaNivel + 501 + i * 3) * 0.55F + avance * 0.45F) * alto);
            int largo = Math.max(3, Math.round(alto * (0.018F + Trazo.pseudo(semillaNivel + 502 + i * 3) * 0.045F)));
            grafico.fill(x, y, x + 1, Math.min(alto, y + largo),
                    Paleta.conAlfa(nivel.niebla, 0.028F * nivel.humedad * luz));
        }
    }
}
