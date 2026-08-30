package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Nivel 0 - La sala.
 *
 * Un ambiente ancho y bajo, de los que se cruzan en diagonal sin pensarlo. El
 * cielorraso es una grilla de placas con las luminarias empotradas en fila, la
 * alfombra esta gastada por el paso y en la pared del fondo hay tres vanos de
 * puerta que dan a otra sala igual.
 *
 * Lo que la distingue de todo lo demas: es ANCHA. El semiancho del fondo es
 * grande y la altura corta, asi que las paredes laterales quedan en los bordes
 * de la pantalla y el peso de la imagen se lo lleva el cielorraso. No se
 * camina por una sala: se esta adentro.
 */
public final class Sala implements Planta {

    /** Tramos en profundidad. Pocos y largos: la sala es corta y ancha. */
    private static final int TRAMOS = 12;

    /** Cuantas placas de cielorraso a lo ancho. */
    private static final int PLACAS = 7;

    @Override
    public int tramos() {
        return TRAMOS;
    }

    @Override
    public void dibujar(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        Trazo.fondo(grafico, m, nivel, luz,
                Paleta.mezclar(nivel.paredBaja, nivel.techo, 0.45F), 1.5F);
        puertasFondo(grafico, m, nivel, luz);
        dintelPrincipal(grafico, m, nivel, luz);

        Trazo.plano(grafico, m, true, nivel.techo,
                Paleta.mezclar(nivel.techo, nivel.niebla, 0.35F), nivel.niebla, luz, 0.50F);
        Trazo.plano(grafico, m, false, nivel.suelo, nivel.sueloLejos, nivel.niebla, luz, 0.58F);

        alfombra(grafico, m, nivel, luz);
        Trazo.transversales(grafico, m, false, nivel.sueloJunta, nivel.niebla, luz, TRAMOS, 0.38F);
        Trazo.transversales(grafico, m, true, nivel.techoJunta, nivel.niebla, luz, TRAMOS, 0.46F);
        grillaCielorraso(grafico, m, nivel, luz);
        filaLuminarias(grafico, m, nivel, luz);

        Trazo.paredes(grafico, m, nivel, luz);
        zocalo(grafico, m, nivel, luz);
        Trazo.juntasVerticales(grafico, m, nivel, luz, TRAMOS, 1.0F, 0.45F);
        Trazo.manchas(grafico, m, nivel, luz, TRAMOS);
        cuadros(grafico, m, nivel, luz);
        placaAdministracion(grafico, m, nivel, luz);
        aberturaMantenimiento(grafico, m, nivel, luz);
    }

    /**
     * Abertura de mantenimiento en el lateral derecho.
     *
     * La placa de administracion habita el lado izquierdo; este hueco baja al
     * lado contrario, con su marco, sus dos bisagras y el interior oscuro que
     * no termina en la pared. Dice que el sitio tiene instalaciones que cuidar,
     * y equilibra la sala sin hacerla simetrica.
     */
    private static void aberturaMantenimiento(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float dx = 1.35F;
        float x = m.lado(1.0F, dx * 0.74F);
        float y0 = m.techoEn(dx * 0.55F) + m.h() * dx * 0.30F;
        int ancho = Math.max(8, Math.round(m.w() * dx * 0.20F));
        int alto = Math.max(12, Math.round(m.h() * dx * 0.26F));
        int x0 = Math.round(x - ancho * 0.5F);
        int y1 = Math.round(y0 + alto);
        int marco = Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz * 0.72F), 0.85F);
        int interior = Paleta.iluminar(Trazo.velar(nivel.paredBaja, nivel.niebla, 0.80F, 0.55F),
                luz * 0.35F);
        int bisagra = Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz * 0.6F), 0.7F);

        grafico.fill(x0, (int) y0, x0 + ancho, y1, interior);
        // Marco: cuatro cantos finos sobre el hueco, como el de la placa.
        grafico.fill(x0, (int) y0, x0 + ancho, (int) y0 + 1, marco);
        grafico.fill(x0, y1 - 1, x0 + ancho, y1, marco);
        grafico.fill(x0, (int) y0, x0 + 1, y1, marco);
        grafico.fill(x0 + ancho - 1, (int) y0, x0 + ancho, y1, marco);
        // Dos bisagras al borde izquierdo: la tapa se abre de ese lado.
        for (int i = 0; i < 2; i++) {
            int by = (int) (y0 + alto * (0.30F + i * 0.34F));
            grafico.fill(x0, by, x0 + 3, by + 3, bisagra);
        }
    }

    /** Placa metalica lateral con remaches anclados a su propio perimetro. */
    private static void placaAdministracion(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float dx = 1.20F;
        float x = m.lado(-1.0F, dx * 0.76F);
        float y = m.techoEn(dx * 0.45F) + m.h() * dx * 0.26F;
        int ancho = Math.max(8, Math.round(m.w() * dx * 0.18F));
        int alto = Math.max(10, Math.round(m.h() * dx * 0.22F));
        int placa = Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, 0.83F, 0.35F), luz * 0.70F);
        int borde = Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz * 0.58F), 0.45F);
        int x0 = Math.round(x - ancho * 0.5F);
        int y0 = Math.round(y);
        grafico.fill(x0, y0, x0 + ancho, y0 + alto, placa);
        grafico.fill(x0, y0, x0 + ancho, y0 + 1, borde);
        grafico.fill(x0, y0 + alto - 1, x0 + ancho, y0 + alto, borde);
        grafico.fill(x0, y0, x0 + 1, y0 + alto, borde);
        grafico.fill(x0 + ancho - 1, y0, x0 + ancho, y0 + alto, borde);
        // Remaches: cada uno queda sobre una esquina del marco, no perdido en
        // la superficie de la placa.
        int remache = Paleta.conAlfa(Paleta.iluminar(nivel.paredAlta, luz), 0.78F);
        int margen = Math.max(2, ancho / 7);
        for (int lado = -1; lado <= 1; lado += 2) {
            int rx = lado < 0 ? x0 + margen : x0 + ancho - margen - 1;
            grafico.fill(rx, y0 + margen, rx + 2, y0 + margen + 2, remache);
            grafico.fill(rx, y0 + alto - margen - 2, rx + 2, y0 + alto - margen, remache);
        }
        // Tres ranuras grabadas: senal de procedimiento, no texto de interfaz.
        for (int i = 0; i < 3; i++) {
            int yy = y0 + alto / 3 + i * Math.max(2, alto / 9);
            grafico.fill(x0 + margen, yy, x0 + ancho - margen, yy + 1,
                    Paleta.conAlfa(nivel.paredBaja, 0.55F));
        }
    }

    /**
     * Las tres puertas del fondo.
     *
     * La del medio esta cerrada y las de los costados abiertas. Es un detalle
     * de dos rectangulos que cambia por completo la lectura del sitio: sin
     * ellas la pared del fondo es el limite del mundo; con ellas, la sala
     * sigue del otro lado y esta es solo una de muchas.
     */
    private static void puertasFondo(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float sueloFondo = m.sueloEn(1.0F);
        float alto = m.h() * 1.30F;

        for (int i = 0; i < 3; i++) {
            float centro = m.fx()
                    + (i > 1 ? m.der(1.0F) - m.fx() : m.fx() - m.izq(1.0F))
                    * (i - 1) * 0.56F;
            float medio = m.w() * 0.13F;
            boolean abierta = i != 1;

            int x0 = Math.round(centro - medio);
            int x1 = Math.round(centro + medio);
            int y0 = Math.round(sueloFondo - alto);
            int y1 = Math.round(sueloFondo);

            if (abierta) {
                // No un rectangulo negro: el primer metro de lo que sigue del
                // otro lado, con su pared en escorzo y su umbral iluminado.
                Trazo.interiorVano(grafico, nivel, x0, y0, x1, y1, i < 1 ? -1 : 1, luz);
            } else {
                grafico.fill(x0, y0, x1, y1,
                        Paleta.iluminar(Paleta.mezclar(nivel.paredBaja, nivel.junta, 0.35F), luz * 0.62F));
                // El picaporte: un pixel que confirma que eso es una puerta.
                grafico.fill(x1 - 4, (y0 + y1) / 2, x1 - 2, (y0 + y1) / 2 + 2,
                        Paleta.iluminar(nivel.luz, luz * 0.55F));
            }

            // Marco. Un poco mas claro que la pared, como todo herraje viejo.
            int marco = Paleta.iluminar(Paleta.mezclar(nivel.junta, nivel.paredAlta, 0.30F), luz * 0.70F);
            grafico.fill(x0 - 1, y0 - 1, x1 + 1, y0 + 1, marco);
            grafico.fill(x0 - 1, y0, x0 + 1, y1, marco);
            grafico.fill(x1 - 1, y0, x1 + 1, y1, marco);
        }
    }

    /**
     * Dintel pesado del vano central: una sola pieza de arquitectura que
     * conecta pared y techo y le da escala institucional a la sala.
     */
    private static void dintelPrincipal(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float dx = 1.0F;
        float techo = m.techoEn(dx);
        float suelo = m.sueloEn(dx);
        float ancho = m.anchoEn(dx) * 0.40F;
        float alto = m.h() * 0.10F;
        float y = techo + (suelo - techo) * 0.22F;
        int piedra = Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, 1.0F, 0.45F), luz * 0.72F);
        int canto = Paleta.conAlfa(Paleta.iluminar(nivel.paredAlta, luz * 0.65F), 0.38F);
        grafico.fill(Math.round(m.centro(dx) - ancho), Math.round(y),
                Math.round(m.centro(dx) + ancho), Math.round(y + alto), piedra);
        grafico.fill(Math.round(m.centro(dx) - ancho), Math.round(y),
                Math.round(m.centro(dx) + ancho), Math.round(y + 2.0F), canto);
        // Tres juntas cortas: el dintel se construyo con bloques, no es una
        // barra continua dibujada sobre el fondo.
        for (int i = 1; i < 4; i++) {
            int x = Math.round(m.centro(dx) - ancho + (2.0F * ancho * i / 4.0F));
            grafico.fill(x, Math.round(y + 2.0F), x + 1, Math.round(y + alto),
                    Paleta.conAlfa(nivel.paredBaja, 0.45F));
        }
    }

    /**
     * La grilla del cielorraso: las longitudinales que faltan.
     *
     * Las transversales ya las puso {@link Trazo}. Estas son las que corren
     * hacia la fuga, y son las que convierten el techo en una grilla de placas
     * en vez de en una escalera de lineas.
     */
    private static void grillaCielorraso(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int i = 1; i < PLACAS; i++) {
            float frac = (i / (float) PLACAS) * 2.0F - 1.0F;
            // Cada longitudinal es una recta que pasa por la fuga: se dibuja
            // fila por fila porque en pantalla no es vertical ni horizontal.
            for (int y = 0; y < m.techoEn(1.0F); y += Trazo.PASO) {
                float dy = m.dy(y + Trazo.PASO * 0.5F);
                if (dy <= 1.0F) {
                    continue;
                }
                float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
                float x = m.enX(dy, frac);
                int grosor = Math.max(1, (int) (m.w() * dy * 0.006F));
                grafico.fill((int) x, y, (int) x + grosor, y + Trazo.PASO,
                        Paleta.conAlfa(Paleta.iluminar(
                                Trazo.velar(nivel.techoJunta, nivel.niebla, lej, 0.5F),
                                Trazo.atenuar(luz, lej)), 0.40F * lej + 0.10F));
            }
        }
    }

    /**
     * Las luminarias empotradas, una por tramo, en el eje de la sala.
     *
     * Van en la grilla del cielorraso y no colgadas, que es lo propio de una
     * oficina; y son largas y anchas, no tubos finos.
     */
    private static void filaLuminarias(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 2; j <= TRAMOS; j++) {
            // El tubo del sexto tramo esta fuera de servicio: una interrupcion
            // puntual rompe la simetria sin convertir el techo en ruido.
            if (j == 6) {
                continue;
            }
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 6.0F) {
                continue;
            }
            Trazo.luminaria(grafico, m, nivel, dx, 0.90F, 0.30F, 1.0F, luz);
        }
    }

    /**
     * La alfombra: una franja mas oscura por el centro, gastada por el paso.
     *
     * No tiene bordes rectos. La suciedad de una alfombra de oficina se
     * concentra donde la gente camina, y eso es una banda difusa por el eje.
     */
    private static void alfombra(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int y = Math.round(m.sueloEn(1.0F)); y < m.alto(); y += Trazo.PASO) {
            float dy = m.dy(y + Trazo.PASO * 0.5F);
            if (dy <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
            float medio = m.w() * dy * 0.42F;
            float a = 0.16F * (0.45F + 0.55F * lej);
            grafico.fill((int) (m.fx() - medio), y, (int) (m.fx() + medio), y + Trazo.PASO,
                    Paleta.conAlfa(Paleta.iluminar(nivel.sueloJunta, luz), a));
        }
    }

    /** El zocalo corrido al pie de las dos paredes. */
    private static void zocalo(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int x = 0; x < m.ancho(); x += Trazo.PASO) {
            float dx = m.dx(x + Trazo.PASO * 0.5F);
            if (dx <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float y1 = m.sueloEn(dx);
            int alto = Math.max(1, (int) (m.h() * dx * 0.055F));
            grafico.fill(x, (int) y1 - alto, x + Trazo.PASO, (int) y1,
                    Paleta.iluminar(nivel.junta, Trazo.atenuar(luz, lej) * 0.85F));
        }
    }

    /**
     * Las marcas donde hubo algo colgado: el empapelado menos desvaido.
     *
     * El papel que el cuadro protegio del sol es MAS OSCURO y rico que el
     * resto; antes la marca se pintaba mas clara y se leia como un rectangulo
     * luminoso flotante. Ademas la alfa se modula por ruido, asi el contorno
     * se quiebra como el papel real y no es una linea recta.
     */
    private static void cuadros(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 3; j < TRAMOS; j++) {
            if (Trazo.pseudo(820 + j) > 0.38F) {
                continue;
            }
            float dxA = Trazo.profundidad(j, TRAMOS);
            float dxB = Trazo.profundidad(j + 1, TRAMOS);
            int signo = Trazo.pseudo(860 + j) < 0.5F ? -1 : 1;
            float lej = Trazo.limitar(1.0F / dxA, 0.0F, 1.0F);

            int x0 = (int) Math.min(m.lado(signo, dxA), m.lado(signo, dxB));
            int x1 = (int) Math.max(m.lado(signo, dxA), m.lado(signo, dxB));
            if (x1 <= 0 || x0 >= m.ancho() || x1 - x0 < 3) {
                continue;
            }

            int tinta = Paleta.mezclar(nivel.paredAlta, 0xFF000000, 0.42F);
            for (int col = Math.max(0, x0); col < Math.min(m.ancho(), x1); col++) {
                float dxc = m.dx(col + 0.5F);
                float centro = m.techoEn(dxc * 0.30F);
                float medio = m.h() * dxc * 0.22F;
                // Alfa por columna: el contorno se quiebra como el papel real.
                float quiebre = 0.70F + 0.60F * Trazo.pseudo(888 + col * 7);
                float alfa = (0.10F * lej + 0.04F) * quiebre;
                grafico.fill(col, (int) (centro - medio), col + 1, (int) (centro + medio),
                        Paleta.conAlfa(Paleta.iluminar(tinta, luz), alfa));
            }
        }
    }

    /** El primer plano de este recinto: el canto del mostrador desde el que se mira la sala. */
    @Override
    public void primerPlano(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        PrimerPlano.sala(grafico, m, nivel, luz, tiempo);
    }
}
