package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Nivel 1 - Deposito, segunda composicion.
 *
 * Busca una lectura mas clara de almacen industrial: calle central de carga,
 * dos portones al fondo, racks laterales, pallets, vigas, luminarias y un
 * montacargas abandonado. La Nave original se conserva para rollback.
 */
public final class DepositoNuevo implements Planta {

    private static final int TRAMOS = 13;
    private static final float[] PROFUNDIDADES = {
            1.18F, 1.42F, 1.72F, 2.08F, 2.52F, 3.04F, 3.68F
    };

    @Override
    public int tramos() {
        return TRAMOS;
    }

    @Override
    public float pisoPresencia() {
        return 1.18F;
    }

    @Override
    public void dibujar(GuiGraphics g, Marco m, Nivel nivel, float luz, float tiempo) {
        int testero = Paleta.mezclar(nivel.paredBaja, Paleta.VANO, 0.24F);
        Trazo.fondo(g, m, nivel, luz, testero, 1.22F);

        Trazo.plano(g, m, true,
                Paleta.mezclar(nivel.techo, Paleta.VANO, 0.20F),
                Paleta.mezclar(nivel.techoJunta, nivel.niebla, 0.34F),
                nivel.niebla, luz, 0.50F);
        Trazo.plano(g, m, false,
                Paleta.mezclar(nivel.suelo, nivel.paredBaja, 0.12F),
                nivel.sueloLejos, nivel.niebla, luz, 0.46F);
        Trazo.paredes(g, m, nivel, luz);
        Trazo.transversales(g, m, false, nivel.sueloJunta, nivel.niebla,
                luz, TRAMOS, 0.20F);
        Trazo.transversales(g, m, true, nivel.techoJunta, nivel.niebla,
                luz, 9, 0.11F);
        Trazo.juntasVerticales(g, m, nivel, luz, 9, 1.0F, 0.15F);
        Trazo.manchas(g, m, nivel, luz, 8);

        portones(g, m, nivel, luz);
        calleCarga(g, m, nivel, luz);
        vigas(g, m, nivel, luz);
        racks(g, m, nivel, luz);
        pallets(g, m, nivel, luz);
        luminarias(g, m, nivel, luz);
        montacargas(g, m, nivel, luz);
    }

    @Override
    public void primerPlano(GuiGraphics g, Marco m, Nivel nivel,
                            float luz, float tiempo) {
        int sombra = Paleta.conAlfa(
                Paleta.mezclar(Paleta.VANO, nivel.paredBaja, 0.20F),
                0.74F * luz + 0.12F);
        int borde = Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz * 0.58F), 0.28F);

        int izq = Math.max(8, m.ancho() / 30);
        int der = Math.max(8, m.ancho() / 34);
        int inicio = Math.max(0, m.alto() / 7);
        g.fill(0, inicio, izq, m.alto(), sombra);
        g.fill(izq, inicio + 12, izq + 2, m.alto(), borde);
        g.fill(m.ancho() - der, m.alto() / 5, m.ancho(), m.alto(), sombra);
        g.fill(m.ancho() - der - 2, m.alto() / 5 + 10,
                m.ancho() - der, m.alto(), borde);
    }

    private static void portones(GuiGraphics g, Marco m, Nivel nivel, float luz) {
        float dx = 1.0F;
        int suelo = Math.round(m.sueloEn(dx));
        int techo = Math.round(m.techoEn(dx));
        int alto = Math.max(12, suelo - techo);
        int y0 = techo + Math.max(4, alto / 8);
        int y1 = suelo - 2;

        int x0 = Math.round(m.enX(dx, -0.78F));
        int x1 = Math.round(m.enX(dx, -0.08F));
        int x2 = Math.round(m.enX(dx, 0.08F));
        int x3 = Math.round(m.enX(dx, 0.78F));

        dibujarPorton(g, x0, y0, x1, y1, nivel, luz, false);
        dibujarPorton(g, x2, y0, x3, y1, nivel, luz, true);
    }

    private static void dibujarPorton(GuiGraphics g, int x0, int y0, int x1, int y1,
                                      Nivel nivel, float luz, boolean oscuro) {
        int base = Paleta.iluminar(
                Paleta.mezclar(nivel.paredBaja, nivel.junta, oscuro ? 0.42F : 0.28F),
                luz * (oscuro ? 0.52F : 0.68F));
        int marco = Paleta.iluminar(
                Paleta.mezclar(nivel.techoJunta, nivel.paredAlta, 0.28F), luz * 0.64F);
        g.fill(x0 - 2, y0 - 2, x1 + 2, y1 + 1, marco);
        g.fill(x0, y0, x1, y1, base);

        int paso = Math.max(3, (x1 - x0) / 14);
        for (int x = x0 + paso; x < x1; x += paso) {
            g.fill(x, y0 + 1, x + 1, y1 - 1,
                    Paleta.conAlfa(Paleta.VANO, 0.17F));
        }
        for (int k = 1; k < 5; k++) {
            int y = y0 + (y1 - y0) * k / 5;
            g.fill(x0 + 1, y, x1 - 1, y + 1,
                    Paleta.conAlfa(Paleta.VANO, 0.18F));
        }
        g.fill(x0 + 2, y1 - 2, x1 - 2, y1,
                Paleta.conAlfa(nivel.luz, 0.12F * luz));
    }

    private static void calleCarga(GuiGraphics g, Marco m, Nivel nivel, float luz) {
        int marca = Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz * 0.52F), 0.18F);
        int junta = Paleta.conAlfa(Paleta.iluminar(nivel.sueloJunta, luz * 0.58F), 0.28F);

        for (int signo = -1; signo <= 1; signo += 2) {
            for (int y = Math.round(m.sueloEn(1.0F)); y < m.alto(); y += 3) {
                float dy = m.dy(y + 1.5F);
                if (dy <= 1.0F) continue;
                int x = Math.round(m.enX(dy, signo * 0.30F));
                int grosor = Math.max(1, Math.min(4, Math.round(dy * 0.8F)));
                g.fill(x, y, x + grosor, Math.min(m.alto(), y + 3), marca);
            }
        }

        int y = Math.round(m.sueloEn(1.12F));
        int x0 = Math.round(m.enX(1.12F, -0.55F));
        int x1 = Math.round(m.enX(1.12F, 0.55F));
        for (int x = x0; x < x1; x += 15) {
            g.fill(x, y, Math.min(x + 8, x1), y + 2, junta);
        }
    }

    private static void vigas(GuiGraphics g, Marco m, Nivel nivel, float luz) {
        int viga = Paleta.conAlfa(
                Paleta.iluminar(Paleta.mezclar(nivel.techoJunta, Paleta.VANO, 0.16F),
                        luz * 0.70F), 0.78F);
        int tirante = Paleta.conAlfa(Paleta.iluminar(nivel.techoJunta, luz * 0.55F), 0.42F);

        for (float dx : PROFUNDIDADES) {
            int y = Math.round(m.techoEn(dx));
            if (y < -8 || y > m.alto() / 2) continue;
            int x0 = Math.max(0, Math.round(m.enX(dx, -0.88F)));
            int x1 = Math.min(m.ancho(), Math.round(m.enX(dx, 0.88F)));
            int grosor = Math.max(1, Math.min(5, Math.round(dx * 1.1F)));
            g.fill(x0, y, x1, y + grosor, viga);
            int cx = (x0 + x1) / 2;
            int drop = Math.max(4, Math.round(m.h() * dx * 0.08F));
            g.fill(cx, y, cx + Math.max(1, grosor / 2), y + drop, tirante);
        }
    }

    private static void racks(GuiGraphics g, Marco m, Nivel nivel, float luz) {
        for (int lado = -1; lado <= 1; lado += 2) {
            for (int i = 0; i < PROFUNDIDADES.length - 1; i++) {
                float dx = PROFUNDIDADES[i];
                if (dx > 3.10F) continue;
                float x = m.enX(dx, lado * 0.76F);
                float suelo = m.sueloEn(dx);
                float techo = m.techoEn(dx);
                float alto = Math.max(8.0F, (suelo - techo) * 0.55F);
                float ancho = Math.max(5.0F, m.anchoEn(dx) * 0.12F);
                int x0 = Math.round(lado < 0 ? x : x - ancho);
                int x1 = Math.round(lado < 0 ? x + ancho : x);
                int y1 = Math.round(suelo);
                int y0 = Math.round(suelo - alto);

                int poste = Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz * 0.68F), 0.72F);
                int balda = Paleta.conAlfa(Paleta.iluminar(nivel.paredAlta, luz * 0.62F), 0.54F);
                int caja = Paleta.conAlfa(
                        Paleta.iluminar(Paleta.mezclar(nivel.paredBaja, nivel.suelo, 0.34F),
                                luz * 0.62F), 0.82F);

                int pw = Math.max(1, Math.min(4, Math.round(dx)));
                g.fill(x0, y0, x0 + pw, y1, poste);
                g.fill(x1 - pw, y0, x1, y1, poste);
                for (int k = 1; k <= 3; k++) {
                    int by = y0 + (y1 - y0) * k / 4;
                    g.fill(x0, by, x1, by + Math.max(1, pw), balda);
                    if ((i + k + (lado > 0 ? 1 : 0)) % 3 != 0) {
                        int margen = Math.max(2, (x1 - x0) / 10);
                        int cajaH = Math.max(3, (y1 - y0) / 10);
                        g.fill(x0 + margen, by - cajaH, x1 - margen, by, caja);
                    }
                }
            }
        }
    }

    private static void pallets(GuiGraphics g, Marco m, Nivel nivel, float luz) {
        for (int i = 0; i < 5; i++) {
            float dx = 1.28F + i * 0.42F;
            float frac = i % 2 == 0 ? -0.48F : 0.46F;
            int cx = Math.round(m.enX(dx, frac));
            int suelo = Math.round(m.sueloEn(dx));
            int w = Math.max(6, Math.round(m.anchoEn(dx) * 0.055F));
            int h = Math.max(4, Math.round((m.sueloEn(dx) - m.techoEn(dx)) * 0.10F));
            int madera = Paleta.conAlfa(
                    Paleta.iluminar(Paleta.mezclar(nivel.suelo, nivel.paredBaja, 0.42F),
                            luz * 0.65F), 0.78F);
            int carga = Paleta.conAlfa(
                    Paleta.iluminar(Paleta.mezclar(nivel.paredBaja, Paleta.VANO, 0.18F),
                            luz * 0.60F), 0.86F);
            g.fill(cx - w / 2, suelo - h, cx + w / 2, suelo - 2, carga);
            g.fill(cx - w / 2 - 1, suelo - 2, cx + w / 2 + 1, suelo, madera);
            g.fill(cx - w / 3, suelo, cx - w / 3 + 2, suelo + 2, madera);
            g.fill(cx + w / 3 - 1, suelo, cx + w / 3 + 1, suelo + 2, madera);
        }
    }

    private static void luminarias(GuiGraphics g, Marco m, Nivel nivel, float luz) {
        for (int i = 0; i < PROFUNDIDADES.length - 1; i++) {
            float dx = PROFUNDIDADES[i];
            int x = Math.round(m.enX(dx, i % 2 == 0 ? -0.10F : 0.12F));
            int y = Math.round(m.techoEn(dx) + (m.sueloEn(dx) - m.techoEn(dx)) * 0.08F);
            int w = Math.max(5, Math.round(m.anchoEn(dx) * 0.07F));
            boolean muerta = i == 2 || i == 5;
            int c = muerta
                    ? Paleta.conAlfa(Paleta.iluminar(nivel.techoJunta, luz * 0.42F), 0.42F)
                    : Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz * 0.95F), 0.68F);
            g.fill(x - w / 2, y, x + w / 2, y + Math.max(1, Math.round(dx * 0.8F)), c);
        }
    }

    private static void montacargas(GuiGraphics g, Marco m, Nivel nivel, float luz) {
        float dx = 1.54F;
        int suelo = Math.round(m.sueloEn(dx));
        int cx = Math.round(m.enX(dx, 0.34F));
        int w = Math.max(10, Math.round(m.anchoEn(dx) * 0.075F));
        int h = Math.max(12, Math.round((m.sueloEn(dx) - m.techoEn(dx)) * 0.18F));
        int cuerpo = Paleta.conAlfa(
                Paleta.iluminar(Paleta.mezclar(nivel.paredBaja, nivel.luz, 0.14F),
                        luz * 0.58F), 0.88F);
        int oscuro = Paleta.conAlfa(Paleta.VANO, 0.74F);
        int metal = Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz * 0.62F), 0.76F);

        int x0 = cx - w / 2;
        int x1 = cx + w / 2;
        int y0 = suelo - h;
        g.fill(x0, y0 + h / 3, x1, suelo - 3, cuerpo);
        g.fill(x0 + w / 5, y0, x1 - w / 5, y0 + h / 2, metal);
        g.fill(x0 + w / 4, y0 + 2, x1 - w / 4, y0 + h / 3, oscuro);
        g.fill(x0 - 2, suelo - 4, x0 + w / 4, suelo + 1, oscuro);
        g.fill(x1 - w / 4, suelo - 4, x1 + 2, suelo + 1, oscuro);

        int mastX = x1 - 2;
        int mastTop = y0 - Math.max(4, h / 3);
        g.fill(mastX, mastTop, mastX + 2, suelo - 2, metal);
        g.fill(mastX + 3, mastTop, mastX + 5, suelo - 2, metal);
        g.fill(mastX + 2, suelo - 4, mastX + Math.max(10, w), suelo - 2, metal);
    }
}
