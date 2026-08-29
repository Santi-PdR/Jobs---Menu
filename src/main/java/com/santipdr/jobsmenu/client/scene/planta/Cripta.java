package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Nivel 4 - La sala.
 *
 * El primer recinto del mod que no es un backroom: una sala de piedra excavada,
 * calida, iluminada por fuego. Es el guino al lobby del server -boveda de roca,
 * antorchas, estandartes, una rueda de carro con velas colgada del techo- y
 * convive con los cuatro niveles anteriores en la rotacion. Donde los backrooms
 * son fluorescente que no se apaga, este es la otra cara: fuego que titila,
 * sombra caliente, piedra en vez de placa.
 *
 * Lo que la distingue:
 *
 *  - la LUZ ES FUEGO. No hay tubos: hay antorchas en las paredes y un candil de
 *    rueda en el centro, y todo lo que ilumina parpadea con vida propia. El
 *    parpadeo no es el del fluorescente agonizando; es la llama respirando.
 *  - la piedra NO es lisa. La boveda y los muros se dibujan con sillares
 *    irregulares y vetas, nunca un plano parejo.
 *  - hay CALOR. La paleta entra en ambar y ocre; el fondo del vano es la boca
 *    de un tunel sin luz, mas negro todavia por contraste con el fuego.
 *
 * Su primer plano es el borde de una mesa larga de banquete, vista desde la
 * cabecera: se mira la sala desde donde se sienta quien preside.
 */
public final class Cripta implements Planta {

    /** Tramos en profundidad. La sala es honda, de nave abovedada. */
    private static final int TRAMOS = 13;

    /** Cuantas dovelas tiene el arco de la boveda a lo ancho. */
    private static final int DOVELAS = 9;

    @Override
    public int tramos() {
        return TRAMOS;
    }

    /**
     * El fuego apoya en el suelo de piedra, como en una sala normal, pero un
     * poco mas adelante: el piso esta seco y firme.
     */
    @Override
    public float pisoPresencia() {
        return 0.98F;
    }

    @Override
    public void dibujar(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        // El pulso del fuego: una respiracion calida y comun a toda la sala,
        // sobre la que cada antorcha pone ademas su propio titileo. Sin esto la
        // luz de fuego se lee como luz electrica quieta.
        float pulso = fuego(tiempo, 0.0F);

        Trazo.fondo(grafico, m, nivel, luz,
                Paleta.mezclar(nivel.fondo, nivel.paredBaja, 0.18F), 1.20F);
        tunelFondo(grafico, m, nivel, luz);

        // Boveda de piedra en vez de cielorraso de placas.
        boveda(grafico, m, nivel, luz);
        Trazo.transversales(grafico, m, true, nivel.techoJunta, nivel.niebla, luz, TRAMOS, 0.30F);

        // Suelo de losas.
        Trazo.plano(grafico, m, false, nivel.suelo, nivel.sueloLejos, nivel.niebla, luz, 0.55F);
        Trazo.transversales(grafico, m, false, nivel.sueloJunta, nivel.niebla, luz, TRAMOS, 0.40F);
        runas(grafico, m, nivel, luz, tiempo);

        Trazo.paredes(grafico, m, nivel, luz);
        sillares(grafico, m, nivel, luz);
        Trazo.manchas(grafico, m, nivel, luz, TRAMOS);
        columnas(grafico, m, nivel, luz);
        estandartes(grafico, m, nivel, luz, tiempo);

        // El fuego va al final: ilumina por encima de todo lo construido.
        antorchas(grafico, m, nivel, luz, tiempo);
        candil(grafico, m, nivel, luz, tiempo, pulso);
    }

    /**
     * Respiracion de una llama, de 0 a 1 aproximado alrededor de 1.
     *
     * Tres senos rapidos e inconmensurables mas un chispazo ocasional. El
     * desfase por antorcha hace que no titilen todas a la vez, que es lo que
     * delataria que es un solo generador.
     */
    private static float fuego(float tiempo, float desfase) {
        float t = tiempo + desfase;
        float v = 1.0F
                + 0.06F * (float) Math.sin(t * 11.0F)
                + 0.04F * (float) Math.sin(t * 17.3F + 1.7F)
                + 0.03F * (float) Math.sin(t * 6.1F + 0.4F);
        // Cada tanto la llama pega un tiron mas fuerte.
        float chispa = (float) Math.sin(t * 3.7F + desfase * 2.0F);
        if (chispa > 0.985F) {
            v *= 0.86F;
        }
        return Trazo.limitar(v, 0.80F, 1.15F);
    }

    /**
     * El tunel del fondo: la boca oscura por donde se sigue.
     *
     * No es una pared plana: es un vano en arco de medio punto, mas negro que
     * el resto porque de ahi no viene luz. El contraste con el fuego de la sala
     * es lo que lo vuelve inquietante en vez de decorativo.
     */
    private static void tunelFondo(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float suelo = m.sueloEn(1.0F);
        float alto = m.h() * 1.5F;
        int x0 = Math.round(m.izq(0.42F));
        int x1 = Math.round(m.der(0.42F));
        int y0 = Math.round(suelo - alto);
        int y1 = Math.round(suelo);
        int cx = (x0 + x1) / 2;
        int radio = (x1 - x0) / 2;

        // El interior del tunel, casi negro.
        Trazo.interiorVano(grafico, nivel, x0, y0 + radio / 2, x1, y1, 0, luz);

        // El arco de medio punto por arriba: dovelas en semicirculo.
        for (int i = 0; i <= 16; i++) {
            double ang = Math.PI * i / 16.0;
            int ax = cx - (int) (Math.cos(ang) * radio);
            int ay = (y0 + radio / 2) - (int) (Math.sin(ang) * radio * 0.5);
            int borde = Math.max(1, radio / 8);
            grafico.fill(ax - borde / 2, ay - borde / 2, ax + borde / 2 + 1, ay + borde / 2 + 1,
                    Paleta.iluminar(Paleta.mezclar(nivel.junta, nivel.paredAlta, 0.30F), luz * 0.62F));
        }
    }

    /**
     * La boveda: en vez de un plano de placas, un canon de piedra que se cierra
     * en arco hacia los costados. Se dibuja como el plano del techo mas unas
     * nervaduras que bajan siguiendo la curva.
     */
    private static void boveda(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        Trazo.plano(grafico, m, true, Paleta.mezclar(nivel.techo, nivel.paredBaja, 0.30F),
                Paleta.mezclar(nivel.techo, nivel.niebla, 0.45F), nivel.niebla, luz, 0.52F);

        // Nervaduras: arcos transversales de piedra, uno por tramo, que curvan
        // de una pared a la otra pasando por lo alto de la boveda.
        for (int j = 2; j <= TRAMOS; j += 2) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 6.5F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej) * 0.85F;
            int color = Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, lej, 0.5F), at);
            int x0 = Math.round(m.izq(dx));
            int x1 = Math.round(m.der(dx));
            int grosor = Math.max(1, (int) (m.h() * dx * 0.02F));
            // Arco: la nervadura sube hacia el centro. Se aproxima con tres
            // tramos rectos que dan la sensacion de curva.
            float yPared = m.techoEn(dx);
            float yCima = m.techoEn(dx * 0.82F);
            int cx = Math.round(m.centro(dx));
            trazoLinea(grafico, x0, (int) yPared, cx, (int) yCima, grosor, color);
            trazoLinea(grafico, cx, (int) yCima, x1, (int) yPared, grosor, color);
        }
    }

    /** Una linea recta gruesa entre dos puntos, por pasos. Para las nervaduras. */
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

    /**
     * Los sillares: la piedra no es lisa. Hiladas horizontales y juntas
     * verticales alternadas, con un leve desvio de color por bloque para que
     * ninguno sea igual al de al lado.
     */
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
            // Hiladas: cada cierto alto una junta horizontal mas oscura.
            int hiladas = 6;
            for (int k = 1; k < hiladas; k++) {
                float f = k / (float) hiladas;
                int y = (int) (y0 + (y1 - y0) * f);
                float desvio = Trazo.pseudo((int) (dx * 131.0F) + k * 37 + x / 8) * 0.10F - 0.05F;
                grafico.fill(x, y, x + Trazo.PASO, y + 1,
                        Paleta.conAlfa(Paleta.iluminar(nivel.junta, at * (0.9F + desvio)),
                                0.30F * lej + 0.10F));
            }
        }
        // Juntas verticales, desfasadas por hilada (aparejo de ladrillo).
        Trazo.juntasVerticales(grafico, m, nivel, luz, TRAMOS, 1.0F, 0.30F);
    }

    /**
     * Dos columnas de piedra, una a cada lado, adelantadas hacia la camara.
     *
     * Se dibujan como prismas: cara iluminada del lado que da al centro (donde
     * pega el fuego) y cara en sombra del otro. Dan la escala de la sala.
     */
    private static void columnas(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 3; j <= TRAMOS; j += 3) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 5.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            float ancho = Math.max(2.0F, m.w() * dx * 0.055F);
            float yTecho = m.techoEn(dx * 0.92F);
            float ySuelo = m.sueloEn(dx);

            for (int signo = -1; signo <= 1; signo += 2) {
                float x = m.lado(signo, dx * 0.80F);
                if (x < -ancho * 2 || x > m.ancho() + ancho * 2) {
                    continue;
                }
                int frente = Paleta.iluminar(
                        Trazo.velar(nivel.paredAlta, nivel.niebla, lej, 0.45F), at * 0.92F);
                int costado = Paleta.iluminar(
                        Trazo.velar(nivel.paredBaja, nivel.niebla, lej, 0.50F), at * 0.55F);
                // La cara iluminada es la que mira al centro de la sala.
                float corte = ancho * 0.42F * (signo < 0 ? 1 : -1);
                grafico.fill((int) (x - ancho), (int) yTecho, (int) (x + corte), (int) ySuelo,
                        signo < 0 ? frente : costado);
                grafico.fill((int) (x + corte), (int) yTecho, (int) (x + ancho), (int) ySuelo,
                        signo < 0 ? costado : frente);

                // Basa y capitel: dos ensanches en los extremos.
                float alto = m.h() * dx * 0.06F;
                int cap = Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, lej, 0.4F), at * 0.8F);
                grafico.fill((int) (x - ancho * 1.3F), (int) (ySuelo - alto),
                        (int) (x + ancho * 1.3F), (int) ySuelo, cap);
                grafico.fill((int) (x - ancho * 1.3F), (int) yTecho,
                        (int) (x + ancho * 1.3F), (int) (yTecho + alto), cap);
            }
        }
    }

    /**
     * Los estandartes colgados de las paredes: tela larga con un galon claro
     * arriba y un emblema apenas sugerido. Ondean muy despacio.
     */
    private static void estandartes(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        for (int j = 2; j <= TRAMOS; j += 3) {
            if (Trazo.pseudo(700 + j) > 0.6F) {
                continue;
            }
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 5.5F) {
                continue;
            }
            int signo = Trazo.pseudo(710 + j) < 0.5F ? -1 : 1;
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            float x = m.lado(signo, dx * 0.94F);
            if (x < -20 || x > m.ancho() + 20) {
                continue;
            }
            float ancho = Math.max(3.0F, m.w() * dx * 0.05F);
            float yTop = m.techoEn(dx * 0.60F);
            float alto = m.h() * dx * 0.55F;
            // Ondeo lentisimo del pano.
            float onda = (float) Math.sin(tiempo * 0.6F + j) * ancho * 0.15F;

            // La cuerda: del techo al asta. Sin esto la tela parece colgar de
            // la nada, y una tela colgada de la nada es un rectangulo flotante.
            float yTecho = m.techoEn(dx * 0.92F);
            grafico.fill((int) x, (int) yTecho, (int) x + 1, (int) yTop,
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, at * 0.55F), 0.50F));
            // Asta horizontal.
            grafico.fill((int) (x - ancho * 0.5F), (int) yTop - 1, (int) (x + ancho * 0.5F), (int) yTop,
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, at * 0.70F), 0.80F));

            int tela = Paleta.iluminar(Trazo.velar(Paleta.mezclar(nivel.paredBaja, nivel.junta, 0.35F), nivel.niebla, lej, 0.4F), at * 0.9F);
            for (int k = 0; k < 8; k++) {
                float f = k / 8.0F;
                float ox = onda * f;
                float xx = x - ancho * 0.5F + ox;
                grafico.fill((int) xx, (int) (yTop + alto * f), (int) (xx + ancho), (int) (yTop + alto * (f + 0.14F)),
                        Paleta.conAlfa(tela, 0.85F));
            }
            // Galon superior, mas claro.
            grafico.fill((int) (x - ancho * 0.5F), (int) yTop, (int) (x + ancho * 0.5F), (int) (yTop + Math.max(1, alto * 0.06F)),
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, at), 0.55F));
            // Emblema: un rombo tenue en el centro del pano.
            float ey = yTop + alto * 0.4F;
            grafico.fill((int) (x - ancho * 0.18F), (int) ey, (int) (x + ancho * 0.18F), (int) (ey + alto * 0.14F),
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, at * 0.8F), 0.30F));
        }
    }

    /**
     * Las antorchas de pared: la fuente de luz real de la sala.
     *
     * Cada una es un punto brillante con su derrame calido alrededor, montada
     * en un soporte de hierro, y cada una titila con su propia fase. El derrame
     * es amplio y suave: el fuego no recorta como un tubo, lame la piedra.
     */
    private static void antorchas(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        for (int j = 2; j <= TRAMOS; j += 2) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 6.5F) {
                continue;
            }
            int signo = (j % 4 == 0) ? 1 : -1;
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float x = m.lado(signo, dx * 0.90F);
            if (x < -10 || x > m.ancho() + 10) {
                continue;
            }
            float y = m.techoEn(dx * 0.42F);
            float llama = fuego(tiempo, j * 1.7F);
            float at = Trazo.atenuar(luz, lej) * llama;
            float medio = Math.max(1.5F, m.w() * dx * 0.02F);

            // El soporte de hierro.
            grafico.fill((int) (x - medio * 0.3F), (int) y, (int) (x + medio * 0.3F), (int) (y + medio * 2.0F),
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz * 0.5F), 0.85F));

            // El derrame: capas amplias y suaves de luz calida sobre la piedra.
            for (int k = 5; k >= 1; k--) {
                float t = k / 5.0F;
                float ex = medio * (1.0F + t * 5.0F);
                float ey = medio * (1.0F + t * 6.0F);
                grafico.fill((int) (x - ex), (int) (y - ey), (int) (x + ex), (int) (y + ey),
                        Paleta.conAlfa(nivel.luz, 0.06F * at * (1.0F - t * 0.5F)));
            }
            // El nucleo de la llama.
            grafico.fill((int) (x - medio * 0.6F), (int) (y - medio * 0.8F),
                    (int) (x + medio * 0.6F), (int) (y + medio * 0.6F),
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, Math.min(1.0F, at * 1.4F)), 0.95F));
            // Punta clara de la llama.
            grafico.fill((int) (x - medio * 0.25F), (int) (y - medio * 1.4F),
                    (int) (x + medio * 0.25F), (int) (y - medio * 0.6F),
                    Paleta.conAlfa(Paleta.iluminar(0xFFFFF3D8, at), 0.75F));
        }
    }

    /**
     * El candil de rueda: una rueda de carro colgada del centro de la boveda
     * por una cadena, con velas en el aro. Es el objeto que corona la sala.
     *
     * Se dibuja en el eje, a media profundidad, con su cadena subiendo hasta lo
     * alto, el aro de madera, los radios y las velas encendidas titilando.
     */
    private static void candil(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo, float pulso) {
        // El candil cuelga en el eje de la sala, pero NO se posiciona con la
        // proyeccion del techo: la boveda es tan alta que techoEn() cae fuera
        // del cuadro por arriba. Se ancla a una altura de pantalla visible y la
        // cadena sube desde ahi hasta perderse en lo alto. Es el objeto que
        // corona la sala; tiene que verse si o si.
        float dx = 1.7F;
        float cx = m.centro(dx);
        float cy = m.alto() * 0.30F;
        float radio = Math.max(10.0F, m.ancho() * 0.075F);
        float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
        float at = Trazo.atenuar(luz, lej);

        // Un balanceo casi imperceptible, como si algo hubiera pasado cerca.
        float mece = (float) Math.sin(tiempo * 0.5F) * radio * 0.06F;
        cx += mece;

        // La cadena desde lo alto del cuadro hasta el cubo del candil.
        int madera = Paleta.iluminar(Paleta.mezclar(nivel.junta, nivel.paredAlta, 0.30F), at * 0.9F);
        for (float yy = 0; yy < cy - radio * 0.4F; yy += 4) {
            float m2 = (float) Math.sin(tiempo * 0.5F) * radio * 0.06F * yy / Math.max(1.0F, cy);
            grafico.fill((int) (m.centro(dx) + m2 - 1), (int) yy, (int) (m.centro(dx) + m2 + 1), (int) yy + 2,
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, at * 0.7F), 0.8F));
        }

        int grosoAro = Math.max(2, (int) (radio * 0.16F));

        // El aro de madera: un anillo aproximado por segmentos.
        int seg = 24;
        int[] px = new int[seg];
        int[] py = new int[seg];
        for (int i = 0; i < seg; i++) {
            double a = 2 * Math.PI * i / seg;
            px[i] = (int) (cx + Math.cos(a) * radio);
            py[i] = (int) (cy + Math.sin(a) * radio * 0.42F);   // aplastado: se ve en escorzo
        }
        for (int i = 0; i < seg; i++) {
            int n = (i + 1) % seg;
            trazoLinea(grafico, px[i], py[i], px[n], py[n], grosoAro, madera);
        }
        // Aro interior.
        for (int i = 0; i < seg; i++) {
            double a = 2 * Math.PI * i / seg;
            int ix = (int) (cx + Math.cos(a) * radio * 0.55F);
            int iy = (int) (cy + Math.sin(a) * radio * 0.55F * 0.42F);
            int nxp = (int) (cx + Math.cos(2 * Math.PI * ((i + 1) % seg) / seg) * radio * 0.55F);
            int nyp = (int) (cy + Math.sin(2 * Math.PI * ((i + 1) % seg) / seg) * radio * 0.55F * 0.42F);
            trazoLinea(grafico, ix, iy, nxp, nyp, Math.max(1, grosoAro / 2), madera);
        }
        // Los radios.
        for (int i = 0; i < 8; i++) {
            double a = 2 * Math.PI * i / 8;
            int ex = (int) (cx + Math.cos(a) * radio);
            int ey = (int) (cy + Math.sin(a) * radio * 0.42F);
            trazoLinea(grafico, (int) cx, (int) cy, ex, ey, Math.max(1, grosoAro / 2), madera);
        }
        // El cubo central.
        grafico.fill((int) (cx - radio * 0.14F), (int) (cy - radio * 0.14F * 0.42F - 2),
                (int) (cx + radio * 0.14F), (int) (cy + radio * 0.14F * 0.42F + 2),
                Paleta.iluminar(nivel.junta, at));

        // Las velas: puntos de fuego repartidos en el aro, cada una con su
        // titileo y su derrame. Son la segunda fuente de luz de la sala.
        int velas = 8;
        for (int i = 0; i < velas; i++) {
            double a = 2 * Math.PI * i / velas + 0.2;
            int vx = (int) (cx + Math.cos(a) * radio);
            int vyBase = (int) (cy + Math.sin(a) * radio * 0.42F);
            float llama = fuego(tiempo, i * 2.3F + 5.0F);
            float av = at * llama;
            // Palito de vela.
            grafico.fill(vx - 1, vyBase - (int) (radio * 0.18F), vx + 1, vyBase,
                    Paleta.conAlfa(Paleta.iluminar(nivel.paredAlta, at * 0.7F), 0.85F));
            int vy = vyBase - (int) (radio * 0.18F);
            // Derrame.
            for (int k = 3; k >= 1; k--) {
                float t = k / 3.0F;
                float e = radio * 0.10F * (1.0F + t * 3.5F);
                grafico.fill((int) (vx - e), (int) (vy - e), (int) (vx + e), (int) (vy + e),
                        Paleta.conAlfa(nivel.luz, 0.10F * av * (1.0F - t * 0.5F)));
            }
            // Nucleo.
            grafico.fill(vx - 1, vy - 2, vx + 2, vy + 1,
                    Paleta.conAlfa(Paleta.iluminar(0xFFFFF3D8, Math.min(1.0F, av * 1.4F)), 0.95F));
        }

        // El derrame grande del candil entero sobre la boveda, por encima.
        for (int k = 4; k >= 1; k--) {
            float t = k / 4.0F;
            float e = radio * (1.2F + t * 1.8F);
            grafico.fill((int) (cx - e), (int) (cy - e * 0.5F), (int) (cx + e), (int) (cy + e * 0.42F),
                    Paleta.conAlfa(nivel.luz, 0.03F * at * pulso * (1.0F - t * 0.5F)));
        }
    }

    /**
     * Las runas del suelo: lineas grabadas que despiden un brillo tenue, como
     * en la referencia. Forman un patron radial en el centro de la sala y
     * laten muy despacio, desfasadas del fuego.
     */
    private static void runas(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        float dxCentro = 1.9F;
        float cx = m.centro(dxCentro);
        float cy = m.sueloEn(dxCentro);
        float lej = Trazo.limitar(1.0F / dxCentro, 0.0F, 1.0F);
        float latido = 0.5F + 0.5F * (float) Math.sin(tiempo * 0.8F);
        float base = (0.12F + 0.16F * latido) * luz;
        int color = Paleta.mezclar(nivel.luz, nivel.paredAlta, 0.35F);

        // Rayos radiales desde el centro, aplastados en perspectiva.
        int rayos = 12;
        float largo = m.w() * dxCentro * 0.5F;
        for (int i = 0; i < rayos; i++) {
            double a = 2 * Math.PI * i / rayos;
            float ex = cx + (float) Math.cos(a) * largo;
            float ey = cy + (float) Math.sin(a) * largo * 0.32F;
            trazoLinea(grafico, (int) cx, (int) cy, (int) ex, (int) ey, 1,
                    Paleta.conAlfa(Paleta.iluminar(color, luz), base * (0.6F + 0.4F * lej)));
        }
        // Dos anillos concentricos.
        for (float rr : new float[] {0.45F, 0.85F}) {
            int seg = 20;
            for (int i = 0; i < seg; i++) {
                double a0 = 2 * Math.PI * i / seg;
                double a1 = 2 * Math.PI * (i + 1) / seg;
                int x0 = (int) (cx + Math.cos(a0) * largo * rr);
                int y0 = (int) (cy + Math.sin(a0) * largo * rr * 0.32F);
                int x1 = (int) (cx + Math.cos(a1) * largo * rr);
                int y1 = (int) (cy + Math.sin(a1) * largo * rr * 0.32F);
                trazoLinea(grafico, x0, y0, x1, y1, 1,
                        Paleta.conAlfa(Paleta.iluminar(color, luz), base * 0.8F));
            }
        }
    }

    /** El primer plano de este recinto: el borde de la mesa larga. */
    @Override
    public void primerPlano(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        PrimerPlano.cripta(grafico, m, nivel, luz, tiempo);
    }
}
