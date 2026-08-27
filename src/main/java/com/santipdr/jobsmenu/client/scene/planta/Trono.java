package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Nivel 9 - El salon del trono.
 *
 * Una sala de audiencias en ruinas. Columnas altas partidas, un techo que se
 * cayo en parte y deja entrar columnas de luz polvorienta, y al fondo, sobre
 * una tarima de escalones, un trono vacio. Dorado apagado y azul de piedra; el
 * unico rojo posible seria el de los Executores, asi que aca no hay: la realeza
 * es oro y sombra.
 *
 * Lo que lo distingue: la PROFUNDIDAD DEL TRONO. Todo converge a un punto -la
 * tarima al fondo, iluminada por un haz cenital- y ese punto esta vacio. Es la
 * unica escena con un centro de atencion narrativo claro, y lo que se encuentra
 * al mirarlo es una silla sin nadie.
 */
public final class Trono implements Planta {

    private static final int TRAMOS = 15;

    /** A que fraccion del semiancho corren las dos hileras de columnas. */
    private static final float HILERA = 0.72F;

    /** A que dy arranca la tarima del trono. */
    private static final float TARIMA = 1.6F;

    @Override
    public int tramos() {
        return TRAMOS;
    }

    @Override
    public float pisoPresencia() {
        return 0.98F;
    }

    @Override
    public void dibujar(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        Trazo.fondo(grafico, m, nivel, luz,
                Paleta.mezclar(nivel.paredBaja, nivel.junta, 0.30F), 1.15F);

        // El techo, con boquetes por donde entra la luz.
        Trazo.plano(grafico, m, true, Paleta.mezclar(nivel.techo, nivel.paredBaja, 0.30F),
                Paleta.mezclar(nivel.techo, nivel.niebla, 0.45F), nivel.niebla, luz, 0.50F);
        Trazo.transversales(grafico, m, true, nivel.techoJunta, nivel.niebla, luz, TRAMOS, 0.28F);
        boquetes(grafico, m, nivel, luz);

        Trazo.plano(grafico, m, false, nivel.suelo, nivel.sueloLejos, nivel.niebla, luz, 0.52F);
        Trazo.transversales(grafico, m, false, nivel.sueloJunta, nivel.niebla, luz, TRAMOS, 0.40F);
        alfombraRoja(grafico, m, nivel, luz);

        Trazo.paredes(grafico, m, nivel, luz);
        sillares(grafico, m, nivel, luz);
        Trazo.manchas(grafico, m, nivel, luz, TRAMOS);

        // El trono al fondo, antes de las columnas de primer plano.
        trono(grafico, m, nivel, luz, tiempo);

        columnas(grafico, m, nivel, luz);
        estandartes(grafico, m, nivel, luz, tiempo);
        haces(grafico, m, nivel, luz, tiempo);
    }

    /** Los boquetes del techo: parches oscuros de cielo, mas claros que la placa. */
    private static void boquetes(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 2; j <= TRAMOS; j += 3) {
            if (Trazo.pseudo(300 + j) > 0.6F) {
                continue;
            }
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 6.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            int signo = Trazo.pseudo(310 + j) < 0.5F ? -1 : 1;
            float cx = m.enX(dx, signo * 0.4F);
            float cy = m.techoEn(dx * 0.5F);
            float w = m.w() * dx * 0.18F;
            float h = m.h() * dx * 0.10F;
            // El cielo por el boquete: un gris apenas mas claro y frio.
            grafico.fill((int) (cx - w), (int) (cy - h), (int) (cx + w), (int) (cy + h),
                    Paleta.conAlfa(Paleta.iluminar(Paleta.mezclar(nivel.niebla, 0xFF8090A0, 0.4F),
                            luz * 0.7F), 0.8F));
            // El borde roto, dentado.
            grafico.fill((int) (cx - w), (int) (cy + h), (int) (cx + w), (int) (cy + h) + 2,
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, Trazo.atenuar(luz, lej)), 0.6F));
        }
    }

    /** La alfombra que sube por el eje hasta la tarima. Dorada, gastada. */
    private static void alfombraRoja(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int y = Math.round(m.sueloEn(1.0F)); y < m.alto(); y += Trazo.PASO) {
            float dy = m.dy(y + Trazo.PASO * 0.5F);
            if (dy <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
            float medio = m.w() * dy * 0.16F;
            int color = Paleta.mezclar(nivel.sueloJunta, nivel.luz, 0.22F);
            grafico.fill((int) (m.centro(dy) - medio), y, (int) (m.centro(dy) + medio), y + Trazo.PASO,
                    Paleta.conAlfa(Paleta.iluminar(color, Trazo.atenuar(luz, lej)), 0.35F));
            // Los bordes de galon, mas claros.
            grafico.fill((int) (m.centro(dy) - medio), y, (int) (m.centro(dy) - medio + 1), y + Trazo.PASO,
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, Trazo.atenuar(luz, lej)), 0.35F));
            grafico.fill((int) (m.centro(dy) + medio - 1), y, (int) (m.centro(dy) + medio), y + Trazo.PASO,
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, Trazo.atenuar(luz, lej)), 0.35F));
        }
    }

    /**
     * El trono sobre su tarima de escalones, al fondo del eje.
     *
     * Tres escalones que suben, y encima el respaldo alto del trono con un
     * remate. Recibe un haz de luz cenital propio -es el punto de fuga narrativo-
     * y esta vacio. Un tenue brillo dorado lo recorta sin llegar a iluminarlo.
     */
    private static void trono(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        float dx = TARIMA;
        float cx = m.centro(dx);
        float suelo = m.sueloEn(dx);
        float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
        float at = Trazo.atenuar(luz, lej);
        float anchoBase = m.w() * dx * 0.34F;
        float altoEsc = m.h() * dx * 0.06F;

        // Un haz de luz cenital que cae sobre el trono.
        for (int k = 5; k >= 1; k--) {
            float t = k / 5.0F;
            float w = anchoBase * (0.5F + t * 0.6F);
            grafico.fill((int) (cx - w), (int) m.techoEn(dx * 0.2F), (int) (cx + w), (int) suelo,
                    Paleta.conAlfa(Paleta.iluminar(0xFFFFF0C0, luz), 0.03F * (1.0F - t * 0.4F)));
        }

        // Tres escalones de la tarima, cada uno mas angosto arriba.
        for (int e = 0; e < 3; e++) {
            float w = anchoBase * (1.0F - e * 0.18F);
            float yTop = suelo - altoEsc * (e + 1);
            int col = Paleta.iluminar(Trazo.velar(nivel.paredAlta, nivel.niebla, lej, 0.4F), at * (0.7F + e * 0.06F));
            grafico.fill((int) (cx - w), (int) yTop, (int) (cx + w), (int) (suelo - altoEsc * e), col);
            // El canto iluminado del escalon.
            grafico.fill((int) (cx - w), (int) yTop, (int) (cx + w), (int) yTop + 1,
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, at), 0.4F));
        }

        // El trono: un asiento y un respaldo alto sobre el ultimo escalon.
        float baseTrono = suelo - altoEsc * 3;
        float anchoTrono = anchoBase * 0.42F;
        float altoTrono = m.h() * dx * 0.42F;
        int oro = Paleta.iluminar(Trazo.velar(nivel.luz, nivel.niebla, lej, 0.35F), at * 0.85F);
        int sombra = Paleta.iluminar(Trazo.velar(nivel.paredBaja, nivel.niebla, lej, 0.5F), at * 0.5F);
        // El respaldo (en sombra por dentro, oro en los bordes).
        grafico.fill((int) (cx - anchoTrono * 0.5F), (int) (baseTrono - altoTrono),
                (int) (cx + anchoTrono * 0.5F), (int) baseTrono, sombra);
        // Montantes dorados a los lados del respaldo.
        grafico.fill((int) (cx - anchoTrono * 0.5F), (int) (baseTrono - altoTrono),
                (int) (cx - anchoTrono * 0.5F) + 2, (int) baseTrono, oro);
        grafico.fill((int) (cx + anchoTrono * 0.5F) - 2, (int) (baseTrono - altoTrono),
                (int) (cx + anchoTrono * 0.5F), (int) baseTrono, oro);
        // El remate superior del respaldo.
        grafico.fill((int) (cx - anchoTrono * 0.5F) - 1, (int) (baseTrono - altoTrono) - 2,
                (int) (cx + anchoTrono * 0.5F) + 1, (int) (baseTrono - altoTrono) + 1, oro);
        // El asiento.
        grafico.fill((int) (cx - anchoTrono * 0.5F), (int) (baseTrono - altoTrono * 0.4F),
                (int) (cx + anchoTrono * 0.5F), (int) (baseTrono - altoTrono * 0.25F), oro);
    }

    /** Sillares de piedra en las paredes, con desvio de color. */
    private static void sillares(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int x = 0; x < m.ancho(); x += Trazo.PASO) {
            float dx = m.dx(x + Trazo.PASO * 0.5F);
            if (dx <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            float y0 = m.techoEn(dx);
            float y1 = m.sueloEn(dx);
            int hiladas = 6;
            for (int k = 1; k < hiladas; k++) {
                float f = (float) k / hiladas;
                int y = (int) (y0 + (y1 - y0) * f);
                float desvio = Trazo.pseudo((int) (dx * 139.0F) + k * 31 + x / 7) * 0.10F - 0.05F;
                grafico.fill(x, y, x + Trazo.PASO, y + 1,
                        Paleta.conAlfa(Paleta.iluminar(nivel.junta, at * (0.9F + desvio)),
                                0.26F * lej + 0.10F));
            }
        }
        Trazo.juntasVerticales(grafico, m, nivel, luz, TRAMOS, 1.0F, 0.28F);
    }

    /**
     * Las columnas altas, algunas partidas.
     *
     * Una de cada tres esta rota: le falta la parte de arriba y termina en un
     * munon dentado. Es lo que dice que el salon esta en ruinas y no solo vacio.
     */
    private static void columnas(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 2; j <= TRAMOS; j += 2) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 5.5F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            float ancho = Math.max(2.0F, m.w() * dx * 0.05F);
            float yTecho = m.techoEn(dx * 0.95F);
            float ySuelo = m.sueloEn(dx);
            boolean rota = Trazo.pseudo(400 + j) < 0.35F;
            float yTope = rota ? yTecho + (ySuelo - yTecho) * (0.35F + Trazo.pseudo(420 + j) * 0.2F) : yTecho;

            for (int signo = -1; signo <= 1; signo += 2) {
                float x = m.lado(signo, dx * HILERA);
                if (x < -ancho * 2 || x > m.ancho() + ancho * 2) {
                    continue;
                }
                int frente = Paleta.iluminar(Trazo.velar(nivel.paredAlta, nivel.niebla, lej, 0.45F), at * 0.9F);
                int costado = Paleta.iluminar(Trazo.velar(nivel.paredBaja, nivel.niebla, lej, 0.5F), at * 0.55F);
                float corte = ancho * 0.4F * (signo < 0 ? 1 : -1);
                grafico.fill((int) (x - ancho), (int) yTope, (int) (x + corte), (int) ySuelo,
                        signo < 0 ? costado : frente);
                grafico.fill((int) (x + corte), (int) yTope, (int) (x + ancho), (int) ySuelo,
                        signo < 0 ? frente : costado);
                if (rota) {
                    // Munon dentado: dos escalones de piedra en el tope.
                    grafico.fill((int) (x - ancho), (int) yTope, (int) (x + corte), (int) yTope + Math.max(1, (int) (ancho * 0.4F)),
                            Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, lej, 0.4F), at * 0.7F));
                } else {
                    // Capitel de la columna intacta.
                    grafico.fill((int) (x - ancho * 1.3F), (int) yTecho, (int) (x + ancho * 1.3F), (int) (yTecho + m.h() * dx * 0.05F),
                            Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, lej, 0.4F), at * 0.8F));
                }
            }
        }
    }

    /** Estandartes largos y rotos colgando entre las columnas, dorados. */
    private static void estandartes(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        for (int j = 3; j <= TRAMOS; j += 3) {
            if (Trazo.pseudo(600 + j) > 0.55F) {
                continue;
            }
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 5.0F) {
                continue;
            }
            int signo = Trazo.pseudo(610 + j) < 0.5F ? -1 : 1;
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            float x = m.lado(signo, dx * (HILERA - 0.04F));
            if (x < -20 || x > m.ancho() + 20) {
                continue;
            }
            float ancho = Math.max(3.0F, m.w() * dx * 0.05F);
            float yTop = m.techoEn(dx * 0.72F);
            float alto = m.h() * dx * 0.48F;
            float onda = (float) Math.sin(tiempo * 0.5F + j) * ancho * 0.16F;
            int tela = Paleta.iluminar(Trazo.velar(Paleta.mezclar(nivel.luz, nivel.paredBaja, 0.45F),
                    nivel.niebla, lej, 0.4F), at * 0.85F);
            // La tela, rota al final (los ultimos jirones se afinan).
            for (int k = 0; k < 8; k++) {
                float f = k / 8.0F;
                float w = ancho * (1.0F - f * 0.5F);
                float ox = onda * f;
                grafico.fill((int) (x - w * 0.5F + ox), (int) (yTop + alto * f), (int) (x + w * 0.5F + ox), (int) (yTop + alto * (f + 0.14F)),
                        Paleta.conAlfa(tela, 0.85F * (1.0F - f * 0.3F)));
            }
        }
    }

    /** Los haces de luz que bajan por los boquetes del techo. */
    private static void haces(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        for (int i = 0; i < 4; i++) {
            float frac = (Trazo.pseudo(i * 17) - 0.5F) * 1.4F;
            float dxTop = 2.2F + i * 1.4F;
            float xTop = m.enX(dxTop, frac);
            float yTop = m.techoEn(dxTop * 0.4F);
            float yBot = m.sueloEn(dxTop);
            float lej = Trazo.limitar(1.0F / dxTop, 0.0F, 1.0F);
            float parpadeo = 0.8F + 0.2F * (float) Math.sin(tiempo * 0.4F + i);
            float a = 0.05F * luz * (0.5F + 0.5F * lej) * parpadeo;
            int pasos = 12;
            float ancho = Math.max(2.0F, m.w() * dxTop * 0.04F);
            for (int k = 0; k < pasos; k++) {
                float t = k / (float) pasos;
                float x = xTop + (m.enX(dxTop, frac * 0.7F) - xTop) * t;
                float y = yTop + (yBot - yTop) * t;
                grafico.fill((int) (x - ancho * (1.0F + t)), (int) y, (int) (x + ancho * (1.0F + t)), (int) y + Trazo.PASO * 2,
                        Paleta.conAlfa(Paleta.iluminar(0xFFFFF0C0, luz), a * (1.0F - t * 0.5F)));
            }
        }
    }

    @Override
    public void primerPlano(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        PrimerPlano.trono(grafico, m, nivel, luz, tiempo);
    }
}
