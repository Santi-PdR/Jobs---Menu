package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RelojAparicion;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;

/**
 * El corredor del nivel, dibujado detras del aviso.
 *
 * GEOMETRIA
 *
 * La abertura del fondo es un rectangulo centrado en el punto de fuga, de
 * semiancho w y semialto h. Las cuatro aristas del corredor son rectas que
 * pasan por la fuga, asi que en pantalla todo se reduce a una sola variable:
 *
 *     dx = |x - fugaX| / w     para columnas
 *     dy = |y - fugaY| / h     para filas
 *
 * En la abertura del fondo, dx = dy = 1. Cuanto mas grande el valor, mas cerca
 * de la camara. La profundidad aparente es lejos = 1/dx, de 0 (encima) a 1 (el
 * fondo).
 *
 * La clave de que esto se lea como un pasillo y no como una escalera de cajas:
 * las juntas de pared, las del suelo y las del cielorraso usan LA MISMA serie
 * de profundidades (ver {@link #profundidadPanel(int)}). Por eso las tres
 * coinciden en cada tramo y el ojo cierra la figura.
 *
 * Su espejo en Python es tools/vista_previa.py. Si se toca una, se toca la otra.
 */
public final class EscenaNivel {

    private EscenaNivel() {
    }

    /** Punto de fuga, en fraccion del ancho. Corrido a la derecha por la hoja. */
    private static final float FUGA_X = 0.545F;

    /** Punto de fuga, en fraccion del alto. */
    private static final float FUGA_Y = 0.520F;

    /** Cuantos tramos de pared se dibujan entre la camara y el fondo. */
    private static final int PANELES = 26;

    /** Motas de polvo suspendidas. */
    private static final int MOTAS = 70;

    /** Semilla del ruido. Deletrea JOBS en hexadecimal. */
    private static final int SEMILLA = 0x4A4F4253;

    // ----------------------------------------------------------------------
    // Entrada
    // ----------------------------------------------------------------------

    /** Dibuja el corredor completo, del fondo hacia la camara. */
    public static void dibujar(GuiGraphics grafico, int ancho, int alto) {
        Nivel nivel = RotacionNiveles.actual();

        boolean viva = ConfigTurno.escenaViva();
        boolean destellos = viva && !ConfigTurno.destellosReducidos();
        boolean movimiento = viva && !ConfigTurno.movimientoReducido();

        float tiempo = viva ? (System.currentTimeMillis() % 600_000L) / 1000.0F : 3.0F;
        float penumbra = RelojAparicion.penumbra();

        float luz = brilloFluorescente(tiempo, destellos)
                * (1.0F - 0.55F * penumbra)
                * RotacionNiveles.luzDisponible();

        // Cuando hay algo al fondo, el pasillo entero baja un punto. Es tan
        // poco que no se ve como un efecto: se siente como que la luz cede.
        if (movimiento) {
            luz *= Presencia.sombra();
        }
        luz = limitar(luz, 0.0F, 1.0F);

        float fx = ancho * FUGA_X;
        float fy = alto * FUGA_Y;
        float w = ancho * nivel.semiancho;
        float h = w * nivel.proporcion;

        fondo(grafico, nivel, ancho, alto, fx, fy, w, h, luz);
        cielorraso(grafico, nivel, ancho, fy, h, luz);
        piso(grafico, nivel, ancho, alto, fy, h, luz);
        transversales(grafico, nivel, ancho, alto, fy, h, luz);
        luminarias(grafico, nivel, alto, fx, fy, w, h, luz);
        paredes(grafico, nivel, ancho, alto, fx, fy, w, h, luz);
        if (nivel.tuberias) {
            canos(grafico, nivel, ancho, fx, fy, w, h, luz);
        }
        if (movimiento) {
            Presencia.dibujar(grafico, nivel, fx, fy, w, h, luz);
            motas(grafico, ancho, alto, tiempo, luz);
        }
        vineta(grafico, ancho, alto, penumbra);
    }

    // ----------------------------------------------------------------------
    // Geometria
    // ----------------------------------------------------------------------

    /**
     * dx de la junta numero j. Con j = PANELES la junta cae exactamente sobre
     * la abertura del fondo; con j = 1 queda muy por fuera de la pantalla.
     */
    private static float profundidadPanel(int j) {
        return (float) PANELES / (float) Math.max(1, j);
    }

    // ----------------------------------------------------------------------
    // Capas
    // ----------------------------------------------------------------------

    /** La abertura del fondo. Lo que hay del otro lado no se ilumina. */
    private static void fondo(GuiGraphics grafico, Nivel nivel, int ancho, int alto,
                              float fx, float fy, float w, float h, float luz) {
        grafico.fill(0, 0, ancho, alto, Paleta.iluminar(nivel.niebla, luz * 0.45F));
        grafico.fillGradient((int) (fx - w), (int) (fy - h), (int) (fx + w), (int) (fy + h),
                Paleta.mezclar(nivel.fondo, nivel.niebla, 0.22F * luz), nivel.fondo);
    }

    /** Placas del cielorraso, fila por fila. Arriba es cerca. */
    private static void cielorraso(GuiGraphics grafico, Nivel nivel, int ancho,
                                   float fy, float h, float luz) {
        int tope = (int) (fy - h);
        for (int y = 0; y < tope; y += 2) {
            float dy = (fy - y) / h;
            float lejos = limitar(1.0F / dy, 0.0F, 1.0F);
            int color = Paleta.mezclar(nivel.techo, nivel.niebla, lejos * lejos * 0.55F);
            grafico.fill(0, y, ancho, y + 2, Paleta.iluminar(color, luz * (0.60F + 0.40F * lejos)));
        }
    }

    /** Suelo, fila por fila. Abajo es cerca. */
    private static void piso(GuiGraphics grafico, Nivel nivel, int ancho, int alto,
                             float fy, float h, float luz) {
        int base = (int) (fy + h);
        for (int y = alto; y > base; y -= 2) {
            float dy = (y - fy) / h;
            float lejos = limitar(1.0F / dy, 0.0F, 1.0F);
            int color = Paleta.mezclar(nivel.suelo, nivel.sueloLejos, lejos);
            color = Paleta.mezclar(color, nivel.niebla, lejos * lejos * 0.45F);
            grafico.fill(0, y - 2, ancho, y, Paleta.iluminar(color, luz * (0.55F + 0.45F * lejos)));
        }
    }

    /** Juntas del suelo y perfileria del cielorraso, a las mismas profundidades. */
    private static void transversales(GuiGraphics grafico, Nivel nivel, int ancho, int alto,
                                      float fy, float h, float luz) {
        for (int j = 1; j <= PANELES; j++) {
            float dx = profundidadPanel(j);
            float lejos = limitar(1.0F / dx, 0.0F, 1.0F);
            int grosor = Math.max(1, Math.min((int) (h * 0.075F), (int) (h * dx * 0.010F)));

            float ys = fy + h * dx;
            if (ys < alto) {
                int color = Paleta.iluminar(
                        Paleta.mezclar(nivel.sueloJunta, nivel.niebla, lejos * 0.5F),
                        luz * (0.55F + 0.45F * lejos));
                grafico.fill(0, (int) ys, ancho, (int) ys + grosor,
                        Paleta.conAlfa(color, 0.60F * lejos + 0.10F));
            }

            float yt = fy - h * dx;
            if (yt > 0) {
                int color = Paleta.iluminar(
                        Paleta.mezclar(nivel.techoJunta, nivel.niebla, lejos * 0.5F),
                        luz * (0.60F + 0.40F * lejos));
                grafico.fill(0, (int) yt - grosor, ancho, (int) yt,
                        Paleta.conAlfa(color, 0.55F * lejos + 0.10F));
            }
        }
    }

    /** Los tubos del cielorraso y el charco de luz que devuelve el suelo. */
    private static void luminarias(GuiGraphics grafico, Nivel nivel, int alto,
                                   float fx, float fy, float w, float h, float luz) {
        for (int j = 2; j <= PANELES; j += 2) {
            float dx = profundidadPanel(j);
            float dxSig = j < PANELES ? profundidadPanel(j + 1) : dx * 0.86F;
            float lejos = limitar(1.0F / dx, 0.0F, 1.0F);

            float y0 = fy - h * dx;
            float y1 = fy - h * dxSig;
            if (y1 < 0 || y0 > alto) {
                continue;
            }
            float semi = w * dx * 0.34F;
            float intensidad = luz * (0.60F + 0.40F * lejos);
            int color = Paleta.iluminar(nivel.luz, intensidad);

            grafico.fill((int) (fx - semi * 1.9F), (int) y0 - 1,
                    (int) (fx + semi * 1.9F), (int) y1 + 1, Paleta.conAlfa(color, 0.18F));
            grafico.fill((int) (fx - semi), (int) y0, (int) (fx + semi), (int) y1,
                    Paleta.conAlfa(color, 0.92F));

            if (nivel.reflejo <= 0.0F) {
                continue;
            }
            float ry0 = fy + h * dx;
            float ry1 = fy + h * dxSig;
            if (ry0 > alto) {
                continue;
            }
            float semiR = semi * 1.25F;
            grafico.fill((int) (fx - semiR), (int) ry1, (int) (fx + semiR), (int) ry0,
                    Paleta.conAlfa(color, nivel.reflejo * 0.55F));
        }
    }

    /**
     * Las dos paredes laterales, columna por columna.
     *
     * Cada columna es un unico degradado vertical, que es justo lo que
     * fillGradient sabe hacer: claro contra el cielorraso, apagado contra el
     * zocalo. La distancia decide cuanto se la come la niebla.
     */
    private static void paredes(GuiGraphics grafico, Nivel nivel, int ancho, int alto,
                                float fx, float fy, float w, float h, float luz) {
        final int paso = 2;
        for (int x = 0; x < ancho; x += paso) {
            float centro = x + paso * 0.5F;
            float dx = Math.abs(centro - fx) / w;
            if (dx <= 1.0F) {
                continue;
            }
            float lejos = limitar(1.0F / dx, 0.0F, 1.0F);
            float y0 = fy - h * dx;
            float y1 = fy + h * dx;
            if (y1 < 0 || y0 > alto) {
                continue;
            }

            float atenuacion = luz * (0.52F + 0.48F * lejos);
            int alta = Paleta.mezclar(nivel.paredAlta, nivel.niebla, lejos * lejos * 0.62F);
            int baja = Paleta.mezclar(nivel.paredBaja, nivel.niebla, lejos * lejos * 0.52F);
            grafico.fillGradient(x, (int) y0, x + paso, (int) y1,
                    Paleta.iluminar(alta, atenuacion), Paleta.iluminar(baja, atenuacion));

            if (nivel.zocalo) {
                int altoZocalo = Math.max(1, (int) (h * dx * 0.055F));
                grafico.fill(x, (int) y1 - altoZocalo, x + paso, (int) y1,
                        Paleta.iluminar(nivel.junta, atenuacion * 0.85F));
            }
        }

        juntasPared(grafico, nivel, ancho, fx, fy, w, h, luz);
        if (nivel.humedad > 0.0F) {
            manchas(grafico, nivel, ancho, fx, fy, w, h, luz);
        }
        if (nivel.marcos) {
            vanos(grafico, nivel, ancho, fx, fy, w, h, luz);
        }
    }

    /** Las verticales que separan panel de panel. Se aprietan hacia la fuga. */
    private static void juntasPared(GuiGraphics grafico, Nivel nivel, int ancho,
                                    float fx, float fy, float w, float h, float luz) {
        for (int j = 1; j <= PANELES; j++) {
            float dx = profundidadPanel(j);
            float lejos = limitar(1.0F / dx, 0.0F, 1.0F);
            float atenuacion = luz * (0.52F + 0.48F * lejos);
            int grosor = Math.max(1, Math.min((int) (w * 0.10F), (int) (w * dx * 0.009F)));
            int color = Paleta.conAlfa(
                    Paleta.iluminar(Paleta.mezclar(nivel.junta, nivel.niebla, lejos * 0.55F), atenuacion),
                    0.45F * lejos + 0.12F);
            int y0 = (int) (fy - h * dx);
            int y1 = (int) (fy + h * dx);

            for (int signo = -1; signo <= 1; signo += 2) {
                float x = fx + signo * w * dx;
                if (x >= -grosor && x <= ancho + grosor) {
                    grafico.fill((int) x, y0, (int) x + grosor, y1, color);
                }
            }
        }
    }

    /** Puertas abiertas en las paredes, entre dos juntas consecutivas. */
    private static void vanos(GuiGraphics grafico, Nivel nivel, int ancho,
                              float fx, float fy, float w, float h, float luz) {
        for (int j = 4; j < PANELES - 1; j++) {
            if ((int) (pseudo(600 + j) * 5.0F) != 0) {
                continue;
            }
            float dxA = profundidadPanel(j);
            float dxB = profundidadPanel(j + 1);
            float lejos = limitar(1.0F / dxA, 0.0F, 1.0F);
            int signo = pseudo(700 + j) < 0.5F ? -1 : 1;

            float xa = fx + signo * w * dxA;
            float xb = fx + signo * w * dxB;
            int x0 = (int) Math.min(xa, xb);
            int x1 = (int) Math.max(xa, xb);
            if (x1 <= 0 || x0 >= ancho) {
                continue;
            }

            float ysA = fy + h * dxA;
            float ysB = fy + h * dxB;
            int hueco = Paleta.conAlfa(Paleta.iluminar(nivel.fondo, luz * 0.20F * lejos), 0.94F);

            for (int col = Math.max(0, x0); col < Math.min(ancho, x1); col++) {
                float t = (col - x0) / (float) Math.max(1, x1 - x0);
                float ys = ysA + (ysB - ysA) * t;
                float dxc = Math.abs(col + 0.5F - fx) / w;
                float altura = 2.0F * h * dxc * 0.78F;
                grafico.fill(col, (int) (ys - altura), col + 1, (int) ys, hueco);
            }

            if (x0 >= 0 && x0 < ancho) {
                grafico.fill(x0, (int) (Math.min(ysA, ysB) - 2.0F * h * dxA * 0.78F), x0 + 1, (int) ysA,
                        Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz * (0.52F + 0.48F * lejos)), 0.80F));
            }
        }
    }

    /** Tuberias corridas bajo el cielorraso, una a cada lado del corredor. */
    private static void canos(GuiGraphics grafico, Nivel nivel, int ancho,
                              float fx, float fy, float w, float h, float luz) {
        final int paso = 2;
        final float[] alturas = {0.74F, 0.62F};
        final float[] radios = {0.045F, 0.032F};
        final float[] tonos = {0.0F, 0.25F};

        for (int c = 0; c < alturas.length; c++) {
            for (int x = 0; x < ancho; x += paso) {
                float centro = x + paso * 0.5F;
                float dx = Math.abs(centro - fx) / w;
                if (dx <= 1.0F) {
                    continue;
                }
                float lejos = limitar(1.0F / dx, 0.0F, 1.0F);
                float atenuacion = luz * (0.52F + 0.48F * lejos);
                float eje = fy - h * dx * alturas[c];
                float radio = Math.max(1.0F, h * dx * radios[c]);
                int color = Paleta.mezclar(nivel.junta, nivel.paredAlta, 0.30F + tonos[c]);
                grafico.fillGradient(x, (int) (eje - radio), x + paso, (int) (eje + radio),
                        Paleta.iluminar(Paleta.mezclar(color, nivel.luz, 0.22F), atenuacion),
                        Paleta.iluminar(Paleta.mezclar(color, Paleta.VANO, 0.35F), atenuacion));
            }
        }
    }

    /** Filtraciones. Cuelgan del cielorraso y se abren hacia abajo. */
    private static void manchas(GuiGraphics grafico, Nivel nivel, int ancho,
                                float fx, float fy, float w, float h, float luz) {
        int total = (int) (16 * nivel.humedad);
        for (int i = 0; i < total; i++) {
            float dx = 1.15F + pseudo(i * 3) * (PANELES * 0.42F);
            int signo = pseudo(i * 3 + 1) < 0.5F ? -1 : 1;
            float x = fx + signo * w * dx;
            if (x < -40 || x > ancho + 40) {
                continue;
            }
            float lejos = limitar(1.0F / dx, 0.0F, 1.0F);
            float y0 = fy - h * dx;
            float altura = h * dx * (0.25F + pseudo(i * 3 + 2) * 0.55F);
            float anchoMancha = Math.max(2.0F, w * dx * (0.05F + pseudo(i * 5) * 0.10F));

            final int pasos = 5;
            for (int k = 0; k < pasos; k++) {
                float t = k / (float) pasos;
                float a = 0.30F * (1.0F - t) * (0.35F + 0.65F * lejos) * nivel.humedad;
                float am = anchoMancha * (0.6F + 0.9F * t);
                grafico.fill((int) (x - am), (int) (y0 + altura * t),
                        (int) (x + am), (int) (y0 + altura * (t + 1.0F / pasos)),
                        Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz), a));
            }
        }
    }

    /** Polvo suspendido, subiendo muy despacio. */
    private static void motas(GuiGraphics grafico, int ancho, int alto, float tiempo, float luz) {
        for (int i = 0; i < MOTAS; i++) {
            float baseX = pseudo(i * 7);
            float baseY = pseudo(i * 7 + 1);
            float velocidad = 0.10F + pseudo(i * 7 + 2) * 0.30F;
            float deriva = (float) Math.sin(tiempo * (0.25F + pseudo(i * 7 + 3) * 0.4F) + i) * 0.012F;

            float y = (baseY + tiempo * velocidad * 0.045F) % 1.0F;
            float x = (baseX + deriva + 1.0F) % 1.0F;
            int px = (int) (x * ancho);
            int py = (int) (y * alto);
            int tam = pseudo(i * 7 + 4) < 0.75F ? 1 : 2;
            float a = (0.10F + pseudo(i * 7 + 5) * 0.22F) * luz;
            grafico.fill(px, py, px + tam, py + tam, Paleta.conAlfa(Paleta.FLUOR, a));
        }
    }

    /** Los bordes de la pantalla se apagan. Se cierran mas cuando ronda. */
    private static void vineta(GuiGraphics grafico, int ancho, int alto, float penumbra) {
        int franja = Math.max(8, ancho / 6);
        float intensidad = 0.38F + 0.42F * penumbra;
        final int paso = 4;

        for (int x = 0; x < franja; x += paso) {
            float t = 1.0F - x / (float) franja;
            int color = Paleta.conAlfa(Paleta.VANO, intensidad * t * t);
            grafico.fill(x, 0, x + paso, alto, color);
            grafico.fill(ancho - x - paso, 0, ancho - x, alto, color);
        }

        int franjaV = Math.max(6, alto / 7);
        for (int y = 0; y < franjaV; y += paso) {
            float t = 1.0F - y / (float) franjaV;
            int color = Paleta.conAlfa(Paleta.VANO, intensidad * 0.75F * t * t);
            grafico.fill(0, y, ancho, y + paso, color);
            grafico.fill(0, alto - y - paso, ancho, alto - y, color);
        }
    }

    // ----------------------------------------------------------------------
    // Utilidades
    // ----------------------------------------------------------------------

    /** Brillo del fluorescente. Nunca queda del todo quieto. */
    public static float brilloFluorescente(float tiempo, boolean destellos) {
        if (!destellos) {
            return 0.90F;
        }
        float v = 0.90F
                + 0.035F * (float) Math.sin(tiempo * 1.7F)
                + 0.020F * (float) Math.sin(tiempo * 5.9F + 1.3F);
        if (Math.floorMod((long) (tiempo * 3.0F), 97L) == 0L) {
            v *= 0.62F;
        }
        return limitar(v, 0.45F, 1.0F);
    }

    /** Ruido reproducible: la misma entrada da siempre la misma salida. */
    private static float pseudo(int indice) {
        long h = SEMILLA + indice * 2654435761L;
        h ^= (h >>> 13);
        h *= 1274126177L;
        h ^= (h >>> 16);
        return Math.floorMod(h, 10000L) / 10000.0F;
    }

    private static float limitar(float valor, float minimo, float maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}
