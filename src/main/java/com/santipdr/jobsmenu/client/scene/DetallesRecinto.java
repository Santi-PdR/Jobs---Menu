package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Trazo;
import com.santipdr.jobsmenu.client.ui.Paleta;
import net.minecraft.client.gui.GuiGraphics;

/** Capa de microdetalle propia de cada recinto, barata y sin texturas. */
public final class DetallesRecinto {
    private DetallesRecinto() {}

    public static void dibujar(GuiGraphics g, Nivel n, Marco m, float luz, float tiempo) {
        switch (RotacionNiveles.indiceActual()) {
            case 0 -> administracion(g, n, m, luz);
            case 1 -> deposito(g, n, m, luz);
            case 2 -> servicio(g, n, m, luz, tiempo);
            case 3 -> natatorio(g, n, m, luz, tiempo);
            default -> { }
        }
    }

    private static void administracion(GuiGraphics g, Nivel n, Marco m, float luz) {
        // Marcas de cinta y papel retirado: burocracia vieja, no decoracion.
        for (int i = 0; i < 5; i++) {
            float dx = 1.25F + i * 0.48F;
            int x = Math.round(m.izq(dx) + m.w() * dx * 0.12F);
            int y = Math.round(m.techoEn(dx * 0.18F));
            int w = Math.max(2, Math.round(m.w() * dx * 0.045F));
            int h = Math.max(2, Math.round(m.h() * dx * 0.11F));
            g.fill(x, y, x + w, y + h,
                    Paleta.conAlfa(Paleta.iluminar(n.paredAlta, luz), 0.07F));
            g.fill(x, y, x + w, y + 1, Paleta.conAlfa(n.junta, 0.18F));
        }
    }

    private static void deposito(GuiGraphics g, Nivel n, Marco m, float luz) {
        // Rayas de ruedas y arrastre en el hormigon, convergiendo a la fuga.
        for (int lado = -1; lado <= 1; lado += 2) {
            for (int y = Math.round(m.sueloEn(1.15F)); y < m.alto(); y += 7) {
                float dy = m.dy(y + 0.5F);
                if (dy <= 1.0F) continue;
                int x = Math.round(m.enX(dy, lado * 0.28F));
                int w = Math.max(1, Math.round(m.w() * dy * 0.012F));
                g.fill(x, y, x + w, y + 2,
                        Paleta.conAlfa(Paleta.iluminar(n.sueloJunta, luz), 0.16F));
            }
        }
    }

    private static void servicio(GuiGraphics g, Nivel n, Marco m, float luz, float tiempo) {
        // Condensacion que recorre una union y cae. Es lenta y discreta.
        float fase = (tiempo * 0.055F) % 1.0F;
        float dx = 2.15F;
        int x = Math.round(m.der(dx * 0.72F));
        int y0 = Math.round(m.techoEn(dx * 0.50F));
        int y1 = Math.round(m.sueloEn(dx));
        int y = Math.round(y0 + (y1 - y0) * fase);
        g.fill(x, y, x + 1, y + Math.max(1, m.alto() / 180),
                Paleta.conAlfa(Paleta.iluminar(n.luz, luz), 0.28F));
    }

    private static void natatorio(GuiGraphics g, Nivel n, Marco m, float luz, float tiempo) {
        // Ondas independientes del reflejo principal: pocas, amplias y lentas.
        // Rompen el aspecto de superficie pintada sin convertir el vaso en mar.
        for (int i = 0; i < 4; i++) {
            float fase = (tiempo * (0.035F + i * 0.006F) + i * 0.23F) % 1.0F;
            float dy = 1.55F + fase * 3.6F;
            int y = Math.round(m.sueloEn(dy));
            int cx = Math.round(m.enX(dy, -0.10F + i * 0.07F));
            int medio = Math.max(3, Math.round(m.w() * dy * (0.07F + fase * 0.05F)));
            int alfa = (int) (255.0F * 0.11F * (1.0F - fase) * luz);
            if (alfa <= 2) continue;
            int color = (alfa << 24) | (Paleta.iluminar(n.techo, luz) & 0x00FFFFFF);
            g.fill(cx - medio, y, cx + medio, y + 1, color);
        }

        // Junta humeda en la cabecera: una linea irregular muy tenue que fija
        // el nivel del agua y da material al borde lejano.
        int baseY = Math.round(m.sueloEn(1.55F));
        for (int x = Math.max(0, Math.round(m.izq(1.55F)));
             x < Math.min(m.ancho(), Math.round(m.der(1.55F))); x += 5) {
            float a = 0.08F + Trazo.pseudo(x * 13 + 91) * 0.10F;
            g.fill(x, baseY - 1, Math.min(x + 4, m.ancho()), baseY,
                    Paleta.conAlfa(Paleta.iluminar(n.junta, luz), a));
        }
    }
}
