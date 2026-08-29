package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Nivel 8 - La cisterna.
 *
 * Un aljibe enorme bajo tierra: filas de columnas que se pierden en la
 * penumbra, naciendo de un agua negra y quieta que lo refleja todo. La luz es
 * escasa y de abajo -unos focos sumergidos que tinen el agua-, y cada gota que
 * cae se oye durante segundos. Combina las dos ideas que mejor funcionaron: el
 * agua del natatorio y los pilares de la nave, pero a oscuras y hacia abajo.
 *
 * Lo que la distingue: el AGUA CUBRE EL PISO y las columnas SE REFLEJAN en ella,
 * invertidas y deshechas por una ondulacion lentisima. La mitad de abajo del
 * cuadro es el reflejo de la mitad de arriba. No hay orilla: se mira desde una
 * pasarela, al ras del agua.
 */
public final class Cisterna implements Planta {

    private static final int TRAMOS = 16;

    /** A que dy esta la linea del agua (el resto hacia abajo es reflejo). */
    private static final float ORILLA = 1.02F;

    /** A que fraccion del semiancho corren las dos hileras de columnas. */
    private static final float HILERA = 0.60F;

    @Override
    public int tramos() {
        return TRAMOS;
    }

    @Override
    public float pisoPresencia() {
        return 1.00F;
    }

    @Override
    public void dibujar(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        Trazo.fondo(grafico, m, nivel, luz,
                Paleta.mezclar(nivel.fondo, nivel.paredBaja, 0.20F), 1.10F);

        // Boveda de ladrillo baja.
        Trazo.plano(grafico, m, true, Paleta.mezclar(nivel.techo, nivel.paredBaja, 0.35F),
                Paleta.mezclar(nivel.techo, nivel.niebla, 0.55F), nivel.niebla, luz, 0.55F);
        Trazo.transversales(grafico, m, true, nivel.techoJunta, nivel.niebla, luz, TRAMOS, 0.26F);

        // El agua ocupa todo lo que hay por debajo de la orilla.
        agua(grafico, m, nivel, luz, tiempo);

        Trazo.paredes(grafico, m, nivel, luz);
        compuertaInspeccion(grafico, m, nivel, luz);
        Trazo.manchas(grafico, m, nivel, luz, TRAMOS);
        tuberiaEntrada(grafico, m, nivel, luz, tiempo);

        // Columnas y su reflejo. Se dibujan de fondo a cerca.
        columnas(grafico, m, nivel, luz, tiempo);
        marcasNivelColumna(grafico, m, nivel, luz);

        focos(grafico, m, nivel, luz, tiempo);
        gotasSuperficie(grafico, m, nivel, luz, tiempo);
    }

    /** Compuerta de inspeccion en el muro, con perimetro y bisagras. */
    private static void compuertaInspeccion(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float dx = 1.34F;
        float centro = m.lado(1.0F, dx * 0.72F);
        float ancho = m.w() * dx * 0.16F;
        float alto = m.h() * dx * 0.30F;
        int x0 = Math.round(centro - ancho);
        int x1 = Math.round(centro + ancho);
        int y1 = Math.round(m.sueloEn(dx * 0.72F) - m.h() * dx * 0.18F);
        int y0 = Math.round(y1 - alto);
        int marco = Paleta.iluminar(Paleta.mezclar(nivel.junta, nivel.paredAlta, 0.28F), luz * 0.64F);
        grafico.fill(x0 - 2, y0 - 2, x1 + 2, y1 + 2, marco);
        grafico.fill(x0, y0, x1, y1, Paleta.conAlfa(Paleta.VANO, 0.72F));
        grafico.fill(x0 + 3, y0 + 3, x1 - 3, y1 - 3,
                Paleta.conAlfa(Paleta.mezclar(nivel.paredBaja, Paleta.VANO, 0.35F), 0.72F));
        grafico.fill(x0, y0, x1, y0 + 2,
                Paleta.conAlfa(Paleta.iluminar(nivel.techo, luz), 0.52F));
        grafico.fill(x0 - 2, y1 - 2, x1 + 2, y1 + 1,
                Paleta.iluminar(nivel.junta, luz * 0.70F));
        int bisagra = Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz * 0.68F), 0.78F);
        grafico.fill(x1 - 2, y0 + 4, x1 + 1, y0 + 7, bisagra);
        grafico.fill(x1 - 2, y1 - 8, x1 + 1, y1 - 5, bisagra);
        int tiradorY = y0 + Math.round(alto / 2.0F);
        grafico.fill(x0 + 4, tiradorY, x0 + 8, tiradorY + 2,
                Paleta.conAlfa(nivel.luz, 0.54F));
    }

    /** Tuberia de entrada que acaba sobre el agua y deja una gota pendiente. */
    private static void tuberiaEntrada(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        float dx = 2.05F;
        float x = m.lado(-1.0F, dx * 0.70F);
        int grosor = Math.max(2, Math.round(m.w() * dx * 0.024F));
        int y0 = Math.round(m.techoEn(dx * 0.38F));
        int y1 = Math.round(m.sueloEn(dx * 0.88F));
        int metal = Paleta.iluminar(Paleta.mezclar(nivel.junta, nivel.paredAlta, 0.22F), luz * 0.66F);
        grafico.fill(Math.round(x - grosor), y0, Math.round(x + grosor), y1, metal);
        grafico.fill(Math.round(x - grosor), y0 - grosor, Math.round(x + grosor * 3), y0 + grosor, metal);
        grafico.fill(Math.round(x - grosor), y1 - grosor, Math.round(x + grosor * 2), y1 + grosor, metal);
        int gotaY = y1 + Math.round(m.h() * dx * 0.05F);
        float fase = (tiempo * 0.25F) % 1.0F;
        int caida = gotaY + Math.round(m.h() * dx * 0.09F * fase);
        grafico.fill(Math.round(x), caida, Math.round(x + grosor), caida + Math.max(2, grosor),
                Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz), 0.54F));
    }

    /** Marcas de nivel discretas, ancladas a una columna sumergida. */
    private static void marcasNivelColumna(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float dx = 2.25F;
        float x = m.lado(1.0F, dx * HILERA);
        float y0 = m.techoEn(dx * 0.92F);
        float y1 = m.sueloEn(dx);
        int ancho = Math.max(6, Math.round(m.w() * dx * 0.13F));
        int color = Paleta.conAlfa(Paleta.iluminar(nivel.techo, luz * 0.62F), 0.58F);
        for (int i = 1; i <= 3; i++) {
            int y = Math.round(y0 + (y1 - y0) * (i / 4.0F));
            grafico.fill(Math.round(x - ancho), y, Math.round(x + ancho * 0.20F), y + 2, color);
            grafico.fill(Math.round(x - ancho), y + 2, Math.round(x - ancho + 3), y + 4,
                    Paleta.conAlfa(nivel.junta, 0.48F));
        }
    }

    /**
     * El agua negra: cubre desde la linea de orilla hasta abajo. Muy oscura,
     * con un leve brillo de los focos y el color del nivel. El reflejo de las
     * columnas lo agregan las columnas mismas.
     */
    private static void agua(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        int desde = Math.round(m.sueloEn(ORILLA));
        for (int y = desde; y < m.alto(); y += Trazo.PASO) {
            float dy = m.dy(y + Trazo.PASO * 0.5F);
            float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
            // El agua es mas negra hacia la camara (mas honda) y toma algo de
            // color del nivel hacia el fondo.
            int aguaBase = Trazo.velar(Paleta.mezclar(nivel.suelo, nivel.fondo, 0.55F),
                    nivel.niebla, lej, 0.30F);
            grafico.fill(0, y, m.ancho(), y + Trazo.PASO,
                    Paleta.iluminar(aguaBase, Trazo.atenuar(luz, lej) * 0.7F));
        }
        // El filo de la orilla: una linea de luz donde el agua toca la pared.
        int fy = desde;
        if (fy >= 0 && fy < m.alto()) {
            grafico.fill(0, fy, m.ancho(), fy + 1,
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz * 0.5F), 0.35F));
        }
    }

    /**
     * Las dos hileras de columnas, con su reflejo invertido en el agua.
     *
     * Cada columna nace en la orilla y sube hasta la boveda; su reflejo baja
     * desde la orilla, invertido, mas tenue y partido por la ondulacion del
     * agua. El reflejo es la mitad del efecto: sin el, es una nave seca.
     */
    private static void columnas(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        for (int j = 2; j <= TRAMOS; j += 2) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 7.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            float ancho = Math.max(2.0F, m.w() * dx * 0.05F);
            float yTecho = m.techoEn(dx * 0.92F);
            // La columna entra al agua a su propia profundidad; el fuste llega
            // hasta esa linea y de ahi hacia abajo empieza el reflejo.
            float yBase = m.sueloEn(Math.max(ORILLA, dx));

            for (int signo = -1; signo <= 1; signo += 2) {
                float x = m.lado(signo, dx * HILERA);
                if (x < -ancho * 2 || x > m.ancho() + ancho * 2) {
                    continue;
                }
                int frente = Paleta.iluminar(
                        Trazo.velar(nivel.paredAlta, nivel.niebla, lej, 0.45F), at * 0.9F);
                int costado = Paleta.iluminar(
                        Trazo.velar(nivel.paredBaja, nivel.niebla, lej, 0.50F), at * 0.55F);
                float corte = ancho * 0.42F * (signo < 0 ? 1 : -1);

                // El fuste, del techo a la orilla.
                grafico.fill((int) (x - ancho), (int) yTecho, (int) (x + corte), (int) yBase,
                        signo < 0 ? costado : frente);
                grafico.fill((int) (x + corte), (int) yTecho, (int) (x + ancho), (int) yBase,
                        signo < 0 ? frente : costado);

                // Capitel, un ensanche arriba.
                float capA = m.h() * dx * 0.05F;
                grafico.fill((int) (x - ancho * 1.3F), (int) yTecho, (int) (x + ancho * 1.3F), (int) (yTecho + capA),
                        Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, lej, 0.4F), at * 0.8F));

                // EL REFLEJO: baja desde la orilla, invertido y deshecho.
                float largoReflejo = (yBase - yTecho) * 0.8F;
                int pasos = 12;
                for (int k = 0; k < pasos; k++) {
                    float t = k / (float) pasos;
                    int ry0 = (int) (yBase + largoReflejo * t);
                    int ry1 = (int) (yBase + largoReflejo * (k + 1) / pasos);
                    if (ry0 >= m.alto()) {
                        break;
                    }
                    // La ondulacion parte el reflejo lateralmente.
                    float onda = (float) Math.sin(tiempo * 0.5F + t * 6.0F + j) * ancho * 0.4F;
                    float desvanece = (1.0F - t) * (1.0F - t) * 0.5F;
                    grafico.fill((int) (x - ancho + onda), ry0, (int) (x + ancho + onda), Math.max(ry0 + 1, ry1),
                            Paleta.conAlfa(Paleta.iluminar(Trazo.velar(nivel.paredAlta, nivel.niebla, lej, 0.5F),
                                    at * 0.6F), desvanece * luz));
                }
            }
        }
    }

    /**
     * Los focos sumergidos: unos pocos puntos de luz bajo el agua que la tinen
     * desde abajo. Es la unica fuente, y da esa luz teatral de aljibe turistico.
     */
    private static void focos(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        for (int j = 3; j <= TRAMOS; j += 4) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 6.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float x = m.centro(dx);
            float y = m.sueloEn(Math.max(ORILLA, dx));
            float titil = Trazo.pulsoLuz(0.85F, 0.15F, tiempo, 1.5F, j);
            float at = Trazo.atenuar(luz, lej) * titil;
            float medio = Math.max(2.0F, m.w() * dx * 0.06F);
            // Un resplandor difuso subiendo desde el agua.
            for (int k = 5; k >= 1; k--) {
                float t = k / 5.0F;
                float ex = medio * (1.0F + t * 3.0F);
                float ey = medio * (1.0F + t * 5.0F);
                grafico.fill((int) (x - ex), (int) (y - ey), (int) (x + ex), (int) (y + ey * 0.3F),
                        Paleta.conAlfa(nivel.luz, 0.05F * at * (1.0F - t * 0.5F)));
            }
            // El nucleo, apenas bajo la superficie.
            grafico.fill((int) (x - medio * 0.5F), (int) (y - 1), (int) (x + medio * 0.5F), (int) (y + 2),
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, Math.min(1.0F, at * 1.2F)), 0.7F));
        }
    }

    /**
     * Las gotas que caen de la boveda al agua: un anillo que se abre en la
     * superficie, deterministas en posicion, con un ciclo lento cada una.
     */
    private static void gotasSuperficie(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        int desde = Math.round(m.sueloEn(ORILLA));
        for (int i = 0; i < 8; i++) {
            float dx = 1.4F + Trazo.pseudo(i * 13) * (TRAMOS * 0.4F);
            float frac = (Trazo.pseudo(i * 13 + 1) - 0.5F) * 1.6F;
            float x = m.enX(dx, frac);
            float y = m.sueloEn(Math.max(ORILLA, dx));
            if (x < 0 || x > m.ancho() || y < desde) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float ciclo = (tiempo * 0.4F + Trazo.pseudo(i * 13 + 2)) % 1.0F;
            // El anillo se abre en el primer tercio del ciclo y se desvanece.
            if (ciclo > 0.4F) {
                continue;
            }
            float r = ciclo / 0.4F;
            float radio = m.w() * dx * 0.06F * r;
            float a = (1.0F - r) * 0.5F * Trazo.atenuar(luz, lej);
            int col = Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz), a);
            grafico.fill((int) (x - radio), (int) y, (int) (x + radio), (int) y + 1, col);
            grafico.fill((int) (x - radio * 0.6F), (int) y + 2, (int) (x + radio * 0.6F), (int) y + 3, col);
        }
    }

    @Override
    public void primerPlano(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        PrimerPlano.cisterna(grafico, m, nivel, luz, tiempo);
    }
}
