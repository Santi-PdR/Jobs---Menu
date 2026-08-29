package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Nivel 1 - El deposito.
 *
 * Una nave de galpon: mucho mas ancha que alta, con dos hileras de pilares que
 * se pierden hacia el fondo y la estructura del techo a la vista. Aca no hay
 * cielorraso que tape nada; hay cerchas, y entre cercha y cercha se ve el vacio.
 *
 * Lo que la distingue de las otras tres plantas: los PILARES. Son lo unico en
 * todo el mod que se interpone entre la camara y el fondo, y son lo que hace
 * que el ojo mida la profundidad. Un pasillo vacio puede tener cualquier largo;
 * una nave con dieciseis pares de columnas tiene el largo que se cuenta.
 *
 * La segunda diferencia es la luz: no hay una fila corrida de tubos, hay
 * campanas colgadas cada varios metros y una de cada tres esta quemada. Los
 * huecos oscuros entre campana y campana son el motivo de todo el recinto.
 */
public final class Nave implements Planta {

    /** Tramos en profundidad. Muchos, porque el largo es el tema. */
    private static final int TRAMOS = 16;

    /** A que fraccion del semiancho corre cada hilera de pilares. */
    private static final float HILERA = 0.62F;

    /** Altura del cordon inferior de la cercha, en fracciones del semialto. */
    private static final float CORDON = 0.66F;

    /**
     * A partir de este dx la estructura ya salio del cuadro.
     *
     * Sin este corte los tramos cercanos dibujan barras gigantes que ocupan la
     * pantalla entera y la nave se convierte en un peine de rayas. Es el error
     * clasico de proyectar sin recortar por cercania.
     */
    private static final float LEJOS = 5.5F;

    /** Alturas fijas de los largueros de una estanteria; evita un array por frame. */
    private static final float[] ALTURAS_ESTANTERIA = {0.22F, 0.52F, 0.80F};

    @Override
    public int tramos() {
        return TRAMOS;
    }

    /**
     * Los pilares llegan hasta el suelo real de la nave, mas abajo que el de
     * un pasillo, asi que lo que se aparece tiene que apoyar ahi tambien.
     */
    @Override
    public float pisoPresencia() {
        return 1.30F;
    }

    @Override
    public void dibujar(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        Trazo.fondo(grafico, m, nivel, luz,
                Paleta.mezclar(nivel.paredBaja, nivel.niebla, 0.42F), 1.35F);
        porton(grafico, m, nivel, luz);
        puertaMuelle(grafico, m, nivel, luz);

        Trazo.plano(grafico, m, true, Paleta.mezclar(nivel.techo, Paleta.VANO, 0.30F),
                Paleta.mezclar(nivel.techo, nivel.niebla, 0.50F), nivel.niebla, luz, 0.62F);
        Trazo.plano(grafico, m, false, nivel.suelo, nivel.sueloLejos, nivel.niebla, luz, 0.55F);

        losas(grafico, m, nivel, luz);
        Trazo.transversales(grafico, m, false, nivel.sueloJunta, nivel.niebla, luz, TRAMOS, 0.26F);

        Trazo.paredes(grafico, m, nivel, luz);
        Trazo.juntasVerticales(grafico, m, nivel, luz, TRAMOS, 1.0F, 0.26F);
        Trazo.manchas(grafico, m, nivel, luz, TRAMOS);

        estanteria(grafico, m, nivel, luz);
        cerchas(grafico, m, nivel, luz);
        pilares(grafico, m, nivel, luz);
        campanas(grafico, m, nivel, luz);
    }

    /**
     * El porton de chapa acanalada del fondo, cerrado.
     *
     * La rendija de luz al pie es lo unico que dice que del otro lado hay algo.
     * No se abre nunca y no hace falta que se abra.
     */
    private static void porton(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float suelo = m.sueloEn(1.0F);
        float alto = m.ha() * 1.05F;
        int x0 = Math.round(m.izq(0.62F));
        int x1 = Math.round(m.der(0.62F));
        int y0 = Math.round(suelo - alto);
        int y1 = Math.round(suelo);

        // La chapa devuelve algo de luz. Si se va a negro, la nave termina en
        // un agujero y se pierde toda la profundidad que arman las cerchas.
        grafico.fillGradient(x0, y0, x1, y1,
                Paleta.iluminar(Paleta.mezclar(nivel.paredBaja, nivel.junta, 0.35F), luz * 0.95F),
                Paleta.iluminar(Paleta.mezclar(nivel.junta, nivel.paredBaja, 0.40F), luz * 0.62F));

        // El acanalado: los nervios verticales son lo que lo vuelve chapa.
        int paso = Math.max(2, (x1 - x0) / 18);
        for (int x = x0 + paso; x < x1; x += paso) {
            grafico.fill(x, y0, x + 1, y1, Paleta.conAlfa(Paleta.VANO, 0.16F));
        }
        for (int k = 1; k < 5; k++) {
            int y = y0 + (y1 - y0) * k / 5;
            grafico.fill(x0, y, x1, y + 1, Paleta.conAlfa(Paleta.VANO, 0.20F));
        }
        grafico.fill(x0 - 1, y0 - 1, x1 + 1, y0 + 1,
                Paleta.iluminar(Paleta.mezclar(nivel.junta, nivel.paredAlta, 0.25F), luz * 0.60F));
        grafico.fill(x0, y1 - 2, x1, y1, Paleta.conAlfa(nivel.luz, 0.12F * luz));
    }

    /**
     * Puerta lateral de muelle, embutida en la pared derecha del deposito.
     *
     * El vano esta en el plano de la pared, no pegado al centro del cuadro:
     * jambas, dintel y umbral hacen que el almacen tenga una segunda salida
     * legible detras de la carga.
     */
    private static void puertaMuelle(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float dx = 1.35F;
        float pared = m.lado(1.0F, dx);
        float ancho = Math.max(5.0F, m.w() * dx * 0.13F);
        float suelo = m.sueloEn(dx);
        float alto = Math.max(10.0F, m.h() * dx * 0.62F);
        float y0 = suelo - alto;
        int x0 = Math.round(pared - ancho);
        int x1 = Math.round(pared);
        int y1 = Math.round(suelo);
        int hueco = Paleta.conAlfa(Paleta.mezclar(Paleta.VANO, nivel.niebla, 0.15F), 0.94F);
        grafico.fill(x0, Math.round(y0), x1, y1, hueco);
        int marco = Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, 0.72F, 0.50F), luz * 0.72F);
        int dintel = Math.max(2, Math.round(m.h() * dx * 0.035F));
        grafico.fill(x0 - 2, Math.round(y0) - dintel, x1 + 2, Math.round(y0), marco);
        grafico.fill(x0 - 2, Math.round(y0), x0 + 1, y1, marco);
        grafico.fill(x1 - 1, Math.round(y0), x1 + 2, y1, marco);
        grafico.fill(x0 - 2, y1 - Math.max(2, dintel / 2), x1 + 2, y1, marco);
        // Umbral iluminado y dos bisagras: escala de muelle, no rectangulo negro.
        grafico.fill(x0 + 2, y1 - Math.max(2, dintel / 2), x1 - 2, y1,
                Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz * 0.45F), 0.36F));
        int bisagra = Paleta.conAlfa(Paleta.iluminar(nivel.paredAlta, luz), 0.65F);
        grafico.fill(x0 + 2, Math.round(y0 + alto * 0.22F), x0 + 4,
                Math.round(y0 + alto * 0.28F), bisagra);
        grafico.fill(x0 + 2, Math.round(y0 + alto * 0.70F), x0 + 4,
                Math.round(y0 + alto * 0.76F), bisagra);
    }

    /**
     * Dos corridas longitudinales de losa en el piso.
     *
     * Sin ellas el suelo de la nave es una mancha lisa que no da escala. Con
     * ellas el ojo tiene dos rectas de fuga mas con que medir el ancho.
     */
    private static void losas(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int lado = 0; lado < 2; lado++) {
            float frac = lado == 0 ? -0.55F : 0.55F;
            for (int y = Math.round(m.sueloEn(1.0F)); y < m.alto(); y += Trazo.PASO) {
                float dy = m.dy(y + Trazo.PASO * 0.5F);
                if (dy <= 1.0F) {
                    continue;
                }
                float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
                float x = m.enX(dy, frac);
                int grosor = Math.max(1, (int) (m.w() * dy * 0.005F));
                grafico.fill((int) x, y, (int) x + grosor, y + Trazo.PASO,
                        Paleta.conAlfa(Paleta.iluminar(nivel.sueloJunta, Trazo.atenuar(luz, lej)),
                                0.30F * lej + 0.08F));
            }
        }
    }

    /**
     * La triangulacion metalica del techo.
     *
     * Dos cordones horizontales y un zigzag entre ellos, por tramo. El ojo no
     * cuenta las barras: lee "estructura", y con eso ya sabe que el techo esta
     * mucho mas alto de lo que estaria en una sala.
     */
    private static void cerchas(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 1; j <= TRAMOS; j++) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > LEJOS) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej) * 0.78F;
            int color = Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, lej, 0.45F), at);

            float ySup = m.techoEn(dx * 0.98F);
            float yInf = m.techoEn(dx * CORDON);
            if (yInf < -6) {
                continue;
            }
            int x0 = Math.max(0, (int) (m.izq(dx)));
            int x1 = Math.min(m.ancho(), (int) (m.der(dx)));
            if (x1 - x0 < 6) {
                continue;
            }
            int grosor = Math.max(1, (int) (m.h() * dx * 0.020F));
            grafico.fill(x0, (int) ySup, x1, (int) ySup + grosor, color);
            grafico.fill(x0, (int) yInf, x1, (int) yInf + grosor, color);

            int paso = Math.max(5, (x1 - x0) / 10);
            boolean sube = true;
            for (int x = x0; x < x1 - paso; x += paso) {
                float ya = sube ? yInf : ySup;
                float yb = sube ? ySup : yInf;
                for (int k = 0; k < 6; k++) {
                    float t = k / 6.0F;
                    int px = (int) (x + paso * t);
                    int py = (int) (ya + (yb - ya) * t);
                    grafico.fill(px, py, px + Math.max(1, paso / 5), py + grosor,
                            Paleta.conAlfa(color, 0.65F));
                }
                sube = !sube;
            }
        }
    }

    /**
     * Las campanas colgadas del cordon inferior.
     *
     * Una de cada tres esta quemada, siempre las mismas, porque la cuenta sale
     * del ruido reproducible. Los tramos oscuros entre las que si prenden son
     * lo que hace que la nave se sienta abandonada y no simplemente vacia.
     */
    private static void campanas(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 1; j <= TRAMOS; j += 2) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > LEJOS) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            float yTecho = m.techoEn(dx * CORDON);
            float yLampara = m.techoEn(dx) * (CORDON - 0.14F);
            float medio = Math.max(1.5F, m.w() * dx * 0.038F);
            if (yLampara > m.alto()) {
                continue;
            }

            grafico.fill((int) m.fx() - 1, (int) yTecho, (int) m.fx() + 1, (int) yLampara,
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, at), 0.60F));
            grafico.fill((int) (m.fx() - medio), (int) yLampara,
                    (int) (m.fx() + medio), (int) (yLampara + medio * 0.45F),
                    Paleta.iluminar(Paleta.mezclar(nivel.junta, nivel.paredAlta, 0.25F), at * 0.85F));

            if (Trazo.pseudo(910 + j) <= 0.30F) {
                continue;
            }
            float y = yLampara + medio * 0.45F;
            for (int k = 3; k >= 1; k--) {
                float t = k / 3.0F;
                float ex = medio * (1.0F + t * 4.5F);
                float ey = medio * (1.0F + t * 3.5F);
                grafico.fill((int) (m.fx() - ex), (int) (y - ey * 0.25F),
                        (int) (m.fx() + ex), (int) (y + ey),
                        Paleta.conAlfa(nivel.luz, 0.05F * at * (1.0F - t * 0.5F)));
            }
            grafico.fill((int) (m.fx() - medio * 0.6F), (int) y,
                    (int) (m.fx() + medio * 0.6F), (int) (y + Math.max(1.0F, medio * 0.30F)),
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, Math.min(1.0F, at * 1.4F)), 0.95F));
        }
    }

    /**
     * Las dos hileras de pilares.
     *
     * Cada uno se dibuja como prisma, no como rectangulo: la cara que mira a la
     * camara recibe luz y la cara lateral queda en sombra, y el corte entre
     * ambas se invierte segun de que lado de la fuga este el pilar. Ese unico
     * detalle es lo que los hace leer como volumen.
     */
    private static void pilares(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 2; j <= TRAMOS; j += 2) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > LEJOS) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            float ancho = Math.max(1.5F, m.w() * dx * 0.040F);
            float yTecho = m.techoEn(dx * CORDON);
            float ySuelo = m.sueloEn(dx);

            for (int signo = -1; signo <= 1; signo += 2) {
                // Una bahia perdio el pilar derecho: el hueco se lee como
                // espacio de carga y rompe la repeticion mecanica.
                if (j == 8 && signo > 0) {
                    continue;
                }
                float x = m.lado(signo, dx * HILERA);
                if (x < -ancho * 2 || x > m.ancho() + ancho * 2) {
                    continue;
                }
                int frente = Paleta.iluminar(
                        Trazo.velar(nivel.paredAlta, nivel.niebla, lej, 0.55F), at * 0.88F);
                int costado = Paleta.iluminar(
                        Trazo.velar(nivel.paredBaja, nivel.niebla, lej, 0.50F), at * 0.55F);
                float corte = ancho * 0.40F * (signo < 0 ? 1 : -1);

                grafico.fill((int) (x - ancho), (int) yTecho, (int) (x + corte), (int) ySuelo,
                        signo < 0 ? costado : frente);
                grafico.fill((int) (x + corte), (int) yTecho, (int) (x + ancho), (int) ySuelo,
                        signo < 0 ? frente : costado);

                // La base de hormigon. Sin ella el pilar flota sobre el piso.
                float alto = m.h() * dx * 0.07F;
                grafico.fill((int) (x - ancho * 1.25F), (int) (ySuelo - alto),
                        (int) (x + ancho * 1.25F), (int) ySuelo,
                        Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, lej, 0.4F), at * 0.70F));
            }
        }
    }

    /**
     * Tres largueros de estanteria contra la pared izquierda, sin nada encima.
     *
     * Que esten vacios es el punto. Alguien se llevo lo que habia.
     */
    private static void estanteria(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int x = 0; x < m.ancho(); x += Trazo.PASO) {
            float centro = x + Trazo.PASO * 0.5F;
            if (centro > m.fx()) {
                continue;
            }
            float dx = m.dx(centro);
            if (dx <= 1.10F || dx > 4.5F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej) * 0.75F;
            float ySuelo = m.sueloEn(dx);
            int color = Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, lej, 0.5F), at);

            for (float a : ALTURAS_ESTANTERIA) {
                float y = ySuelo - m.h() * dx * a;
                int grosor = Math.max(1, (int) (m.h() * dx * 0.016F));
                grafico.fill(x, (int) y, x + Trazo.PASO, (int) y + grosor,
                        Paleta.conAlfa(color, 0.85F));
            }
        }
    }

    /** El primer plano de este recinto: la columna de hormigon cortada por el borde. */
    @Override
    public void primerPlano(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        PrimerPlano.nave(grafico, m, nivel, luz, tiempo);
    }
}
