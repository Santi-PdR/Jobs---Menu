package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Nivel 7 - Las catacumbas.
 *
 * Un tunel angosto y bajo excavado en la roca, con nichos en las dos paredes.
 * Frio, humedo, azul-gris; la unica luz es la de un farol que alguien dejo
 * colgado y unas pocas velas votivas en los nichos. Es el pariente oscuro de la
 * sala de piedra: alli habia fuego y banquete, aca hay piedra fria y huecos en
 * la pared.
 *
 * Lo que lo distingue: los NICHOS. Huecos rectangulares excavados en fila a los
 * dos lados, algunos con una vela, todos negros por dentro. Son lo que dice que
 * esto no es un pasillo cualquiera: es un lugar donde se guarda algo. El tunel
 * es estrecho y arqueado, y baja apenas hacia la fuga.
 */
public final class Catacumba implements Planta {

    private static final int TRAMOS = 20;

    /** Cada cuantos tramos hay un nicho. */
    private static final int PASO_NICHO = 3;

    @Override
    public int tramos() {
        return TRAMOS;
    }

    @Override
    public float pisoPresencia() {
        return 0.97F;
    }

    @Override
    public void dibujar(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        Trazo.fondo(grafico, m, nivel, luz,
                Paleta.mezclar(nivel.fondo, nivel.paredBaja, 0.15F), 1.10F);
        arcoFondo(grafico, m, nivel, luz);

        // Boveda de canon baja.
        Trazo.plano(grafico, m, true, Paleta.mezclar(nivel.techo, nivel.paredBaja, 0.30F),
                Paleta.mezclar(nivel.techo, nivel.niebla, 0.50F), nivel.niebla, luz, 0.55F);
        Trazo.transversales(grafico, m, true, nivel.techoJunta, nivel.niebla, luz, TRAMOS, 0.30F);
        arcosBoveda(grafico, m, nivel, luz);

        Trazo.plano(grafico, m, false, nivel.suelo, nivel.sueloLejos, nivel.niebla, luz, 0.60F);
        Trazo.transversales(grafico, m, false, nivel.sueloJunta, nivel.niebla, luz, TRAMOS, 0.42F);

        Trazo.paredes(grafico, m, nivel, luz);
        sillaresIrregulares(grafico, m, nivel, luz);
        Trazo.manchas(grafico, m, nivel, luz, TRAMOS);

        nichos(grafico, m, nivel, luz, tiempo);
        farol(grafico, m, nivel, luz, tiempo);
        goteras(grafico, m, nivel, luz, tiempo);
    }

    /** El arco del fondo: el tunel dobla y sigue hacia lo negro. */
    private static void arcoFondo(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float suelo = m.sueloEn(1.0F);
        float alto = m.h() * 1.55F;
        int x0 = Math.round(m.izq(0.55F));
        int x1 = Math.round(m.der(0.55F));
        int y0 = Math.round(suelo - alto);
        int y1 = Math.round(suelo);
        int cx = (x0 + x1) / 2;
        int radio = (x1 - x0) / 2;
        Trazo.interiorVano(grafico, nivel, x0, y0 + radio / 2, x1, y1, 0, luz);
        for (int i = 0; i <= 14; i++) {
            double ang = Math.PI * i / 14.0;
            int ax = cx - (int) (Math.cos(ang) * radio);
            int ay = (y0 + radio / 2) - (int) (Math.sin(ang) * radio * 0.55);
            int b = Math.max(1, radio / 7);
            grafico.fill(ax - b / 2, ay - b / 2, ax + b / 2 + 1, ay + b / 2 + 1,
                    Paleta.iluminar(nivel.junta, luz * 0.55F));
        }
    }

    /** Los arcos de la boveda: nervaduras de ladrillo, una por tramo. */
    private static void arcosBoveda(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 1; j <= TRAMOS; j += 2) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 9.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej) * 0.8F;
            int color = Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, lej, 0.5F), at);
            int x0 = Math.round(m.izq(dx));
            int x1 = Math.round(m.der(dx));
            int grosor = Math.max(1, (int) (m.h() * dx * 0.02F));
            float yPared = m.techoEn(dx);
            float yCima = m.techoEn(dx * 0.86F);
            int cx = Math.round(m.centro(dx));
            trazoLinea(grafico, x0, (int) yPared, cx, (int) yCima, grosor, color);
            trazoLinea(grafico, cx, (int) yCima, x1, (int) yPared, grosor, color);
        }
    }

    private static void trazoLinea(GuiGraphics grafico, int x0, int y0, int x1, int y1,
                                   int grosor, int color) {
        int pasos = Math.max(1, Math.abs(x1 - x0) / Trazo.PASO);
        for (int i = 0; i <= pasos; i++) {
            float t = i / (float) pasos;
            int x = (int) (x0 + (x1 - x0) * t);
            int y = (int) (y0 + (y1 - y0) * t);
            grafico.fill(x, y, x + Trazo.PASO, y + grosor, color);
        }
    }

    /** Sillares irregulares de piedra en las paredes, con desvio de color. */
    private static void sillaresIrregulares(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int x = 0; x < m.ancho(); x += Trazo.PASO) {
            float dx = m.dx(x + Trazo.PASO * 0.5F);
            if (dx <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            float y0 = m.techoEn(dx);
            float y1 = m.sueloEn(dx);
            int hiladas = 7;
            for (int k = 1; k < hiladas; k++) {
                float f = (float) k / hiladas;
                int y = (int) (y0 + (y1 - y0) * f);
                float desvio = Trazo.pseudo((int) (dx * 149.0F) + k * 29 + x / 6) * 0.12F - 0.06F;
                grafico.fill(x, y, x + Trazo.PASO, y + 1,
                        Paleta.conAlfa(Paleta.iluminar(nivel.junta, at * (0.9F + desvio)),
                                0.28F * lej + 0.10F));
            }
        }
        Trazo.juntasVerticales(grafico, m, nivel, luz, TRAMOS, 1.0F, 0.26F);
    }

    /**
     * Los nichos excavados en las dos paredes.
     *
     * Cada nicho es un hueco rectangular oscuro con su arco arriba; algunos
     * tienen una vela votiva encendida en el borde, con su derrame corto. El
     * ruido decide cuales estan encendidos, asi que siempre son los mismos.
     */
    private static void nichos(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        for (int j = 2; j <= TRAMOS; j += PASO_NICHO) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 7.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            for (int signo = -1; signo <= 1; signo += 2) {
                float x = m.lado(signo, dx * 0.98F);
                if (x < -m.w() || x > m.ancho() + m.w()) {
                    continue;
                }
                float ancho = Math.max(3.0F, m.w() * dx * 0.16F);
                float centroY = m.techoEn(dx * 0.45F);
                float alto = m.h() * dx * 0.34F;
                int nx0 = (int) (x - ancho * 0.5F);
                int nx1 = (int) (x + ancho * 0.5F);
                int ny0 = (int) (centroY - alto * 0.5F);
                int ny1 = (int) (centroY + alto * 0.5F);
                // El hueco negro.
                grafico.fill(nx0, ny0, nx1, ny1,
                        Paleta.conAlfa(Paleta.mezclar(nivel.fondo, nivel.niebla, 0.10F), 0.95F));
                // Paredes del hueco en sombra (el lado de dentro de la roca).
                int cornisa = Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, lej, 0.4F), at * 0.35F);
                grafico.fill(nx0, ny0, nx1, ny0 + 2, cornisa);
                grafico.fill(nx0, ny0, nx0 + 2, ny1, cornisa);
                grafico.fill(nx1 - 2, ny0, nx1, ny1, cornisa);
                // El hueco se excava en la pared: el borde inferior es el
                // alfeizar, iluminado por la luz del tunel, no un marco.
                int alfeizar = Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, lej, 0.35F), at * 0.60F);
                grafico.fill(nx0 - 2, ny1, nx1 + 2, ny1 + Math.max(2, (int) (alto * 0.10F)), alfeizar);
                // Una vela votiva encendida en algunos, siempre los mismos.
                if (Trazo.pseudo(500 + j * 7 + (signo + 1) * 40) > 0.55F) {
                    float titil = 0.85F + 0.15F * (float) Math.sin(tiempo * 6.0F + j);
                    float av = at * titil;
                    int vx = (nx0 + nx1) / 2;
                    int vy = ny1 - Math.max(2, (int) (alto * 0.16F));
                    for (int k = 3; k >= 1; k--) {
                        float t = k / 3.0F;
                        float e = ancho * 0.28F * (1.0F + t * 2.5F);
                        grafico.fill((int) (vx - e), (int) (vy - e), (int) (vx + e), (int) (vy + e * 0.6F),
                                Paleta.conAlfa(nivel.luz, 0.09F * av * (1.0F - t * 0.5F)));
                    }
                    grafico.fill(vx - 1, vy - 2, vx + 2, vy + 1,
                            Paleta.conAlfa(Paleta.iluminar(0xFFFFE0A0, Math.min(1.0F, av * 1.3F)), 0.95F));
                }
            }
        }
    }

    /** El farol colgado: la luz principal, un poco adelante, meciendose apenas. */
    private static void farol(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        float dx = 2.4F;
        float cx = m.centro(dx) + (float) Math.sin(tiempo * 0.6F) * m.w() * dx * 0.02F;
        float cy = m.techoEn(dx * 0.55F);
        float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
        float titil = 0.88F + 0.12F * (float) Math.sin(tiempo * 7.0F);
        float at = Trazo.atenuar(luz, lej) * titil;
        float medio = Math.max(2.0F, m.w() * dx * 0.03F);

        // El gancho y la cadena corta.
        float yTecho = m.techoEn(dx * 0.90F);
        grafico.fill((int) cx - 1, (int) yTecho, (int) cx + 1, (int) cy,
                Paleta.conAlfa(Paleta.iluminar(nivel.junta, at * 0.7F), 0.8F));
        // El derrame amplio.
        for (int k = 5; k >= 1; k--) {
            float t = k / 5.0F;
            float e = medio * (1.0F + t * 4.5F);
            grafico.fill((int) (cx - e), (int) (cy - e), (int) (cx + e), (int) (cy + e),
                    Paleta.conAlfa(nivel.luz, 0.06F * at * (1.0F - t * 0.5F)));
        }
        // La caja del farol: hierro con vidrio.
        int hierro = Paleta.iluminar(nivel.junta, at * 0.9F);
        grafico.fill((int) (cx - medio), (int) (cy - medio * 1.3F), (int) (cx + medio), (int) (cy + medio * 1.3F), hierro);
        // El nucleo de la llama dentro.
        grafico.fill((int) (cx - medio * 0.5F), (int) (cy - medio * 0.7F), (int) (cx + medio * 0.5F), (int) (cy + medio * 0.6F),
                Paleta.conAlfa(Paleta.iluminar(0xFFFFE0A0, Math.min(1.0F, at * 1.4F)), 0.95F));
    }

    /** Goteras: hilos de agua brillante bajando por la pared, deterministas. */
    private static void goteras(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        for (int i = 0; i < 10; i++) {
            float dx = 1.3F + Trazo.pseudo(i * 11) * (TRAMOS * 0.35F);
            if (dx > 7.0F) {
                continue;
            }
            int signo = Trazo.pseudo(i * 11 + 1) < 0.5F ? -1 : 1;
            float x = m.lado(signo, dx * 0.9F);
            if (x < 0 || x > m.ancho()) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float y0 = m.techoEn(dx);
            float y1 = m.sueloEn(dx);
            // El hilo de humedad, apenas mas claro que la piedra.
            grafico.fill((int) x, (int) y0, (int) x + 1, (int) y1,
                    Paleta.conAlfa(Paleta.iluminar(Paleta.mezclar(nivel.paredAlta, 0xFF88AACC, 0.4F),
                            Trazo.atenuar(luz, lej) * 0.5F), 0.30F));
            // Una gota que cae cada tanto por el hilo.
            float fase = (tiempo * 0.3F + Trazo.pseudo(i * 11 + 2)) % 1.0F;
            int gy = (int) (y0 + (y1 - y0) * fase);
            grafico.fill((int) x - 1, gy, (int) x + 2, gy + 3,
                    Paleta.conAlfa(Paleta.iluminar(0xFFBFE0FF, Trazo.atenuar(luz, lej)), 0.55F));
        }
    }

    @Override
    public void primerPlano(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        PrimerPlano.catacumba(grafico, m, nivel, luz, tiempo);
    }
}
