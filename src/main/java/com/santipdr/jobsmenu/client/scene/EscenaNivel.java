package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RelojAparicion;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Fondo del menu: un pasillo del nivel, en perspectiva de un punto.
 *
 * Paredes amarillas, cielorraso de placas, alfombra humeda y una hilera de
 * fluorescentes que se pierde al fondo. Al final del pasillo hay un vano
 * oscuro: la salida al nivel siguiente, la que hay que pagar.
 *
 * Todo es procedural, sin una sola textura. La semilla de las manchas es fija
 * a proposito: siempre es el mismo pasillo, siempre las mismas filtraciones.
 * Y aun asi nunca termina de parecer el mismo.
 */
public final class EscenaNivel {

    private EscenaNivel() {
    }

    /** Semilla de las manchas de humedad. */
    private static final long SEMILLA = 0x4A4F4253L;

    /** Cada cuantos milisegundos algo cruza el vano del fondo. */
    private static final long PERIODO_SILUETA_MS = 47_000L;

    /** Cuanto dura el cruce, en milisegundos. */
    private static final long DURACION_SILUETA_MS = 2_600L;

    /** Cuantos tramos de pasillo se dibujan hacia el fondo. */
    private static final int TRAMOS = 11;

    /** Cuantas motas de polvo flotan en el aire. */
    private static final int MOTAS = 70;

    /** Manchas de humedad por pared. */
    private static final int MANCHAS = 14;

    public static void dibujar(GuiGraphics grafico, int ancho, int alto) {
        long ahora = System.currentTimeMillis();
        boolean viva = ConfigTurno.escenaViva();
        boolean movimiento = viva && !ConfigTurno.movimientoReducido();
        boolean destellos = viva && !ConfigTurno.destellosReducidos();

        float tiempo = viva ? (float) (ahora % 600_000L) / 1000.0F : 0.0F;
        float penumbra = RelojAparicion.penumbra();
        float luz = brilloFluorescente(tiempo, destellos) * (1.0F - 0.55F * penumbra);

        // Punto de fuga: algo por encima del centro, como si mirases de pie.
        int fugaX = (int) (ancho * 0.63F);
        int fugaY = (int) (alto * 0.54F);

        superficies(grafico, ancho, alto, fugaX, fugaY, luz);
        humedad(grafico, ancho, alto, fugaX, fugaY, luz);
        fluorescentes(grafico, ancho, alto, fugaX, fugaY, luz, tiempo, destellos);
        vano(grafico, fugaX, fugaY, ancho, alto, ahora, movimiento);

        if (movimiento) {
            polvo(grafico, ancho, alto, tiempo, luz);
        }

        vineta(grafico, ancho, alto, penumbra);
    }

    /**
     * Paredes, techo y alfombra, construidos como cuatro abanicos de trapecios
     * que convergen en el punto de fuga.
     */
    private static void superficies(GuiGraphics grafico, int ancho, int alto, int fugaX, int fugaY, float luz) {
        // Fondo base: el pasillo mas lejano ya esta casi a oscuras.
        grafico.fill(0, 0, ancho, alto, Paleta.iluminar(Paleta.PARED_BAJA, luz * 0.45F));

        for (int i = TRAMOS; i >= 1; i--) {
            float lejos = (float) i / (float) TRAMOS;      // 1.0 = el mas cercano
            float cerca = (float) (i - 1) / (float) TRAMOS;

            // Cuanto se ha cerrado el pasillo hacia el fondo.
            float aperturaLejos = escala(lejos);
            float aperturaCerca = escala(cerca);

            // La luz cae con la distancia.
            float luzTramo = luz * (0.42F + 0.58F * lejos);

            int izqL = interpolar(fugaX, 0, aperturaLejos);
            int derL = interpolar(fugaX, ancho, aperturaLejos);
            int supL = interpolar(fugaY, 0, aperturaLejos);
            int infL = interpolar(fugaY, alto, aperturaLejos);

            int izqC = interpolar(fugaX, 0, aperturaCerca);
            int derC = interpolar(fugaX, ancho, aperturaCerca);
            int supC = interpolar(fugaY, 0, aperturaCerca);
            int infC = interpolar(fugaY, alto, aperturaCerca);

            // Techo: banda entre el borde superior de dos marcos consecutivos.
            grafico.fillGradient(izqL, supL, derL, supC,
                    Paleta.iluminar(Paleta.TECHO, luzTramo * 0.85F),
                    Paleta.iluminar(Paleta.TECHO, luzTramo));

            // Alfombra.
            grafico.fillGradient(izqL, infC, derL, infL,
                    Paleta.iluminar(Paleta.ALFOMBRA_OSCURA, luzTramo * 0.75F),
                    Paleta.iluminar(Paleta.ALFOMBRA, luzTramo * 0.95F));

            // Pared izquierda: del marco lejano al cercano.
            grafico.fillGradient(izqC, supC, izqL, infC,
                    Paleta.iluminar(Paleta.PARED_ALTA, luzTramo),
                    Paleta.iluminar(Paleta.PARED_BAJA, luzTramo * 0.85F));

            // Pared derecha.
            grafico.fillGradient(derL, supC, derC, infC,
                    Paleta.iluminar(Paleta.PARED_ALTA, luzTramo),
                    Paleta.iluminar(Paleta.PARED_BAJA, luzTramo * 0.85F));

            // Zocalo y junta techo-pared: las lineas que dan la perspectiva.
            int junta = Paleta.iluminar(Paleta.MOHO, luzTramo);
            grafico.fill(izqL, supL, derL, supL + 1, Paleta.conAlfa(junta, 0.45F));
            grafico.fill(izqL, infL - 1, derL, infL, Paleta.conAlfa(junta, 0.55F));
            grafico.fill(izqL, supL, izqL + 1, infL, Paleta.conAlfa(junta, 0.35F));
            grafico.fill(derL - 1, supL, derL, infL, Paleta.conAlfa(junta, 0.35F));
        }
    }

    /**
     * Curva de cierre del pasillo. No es lineal: los tramos cercanos ocupan
     * mucho mas que los lejanos, como en una perspectiva real.
     */
    private static float escala(float t) {
        return t * t;
    }

    private static int interpolar(int desde, int hasta, float t) {
        return (int) (desde + (hasta - desde) * t);
    }

    /** Manchas de filtracion en las paredes, deterministas. */
    private static void humedad(GuiGraphics grafico, int ancho, int alto, int fugaX, int fugaY, float luz) {
        for (int i = 0; i < MANCHAS; i++) {
            float a = pseudo(i * 7 + 1);
            float b = pseudo(i * 13 + 5);
            float c = pseudo(i * 29 + 9);

            boolean izquierda = (i % 2) == 0;
            float profundidad = 0.25F + a * 0.70F;
            float apertura = escala(profundidad);

            int borde = izquierda
                    ? interpolar(fugaX, 0, apertura)
                    : interpolar(fugaX, ancho, apertura);
            int sup = interpolar(fugaY, 0, apertura);
            int inf = interpolar(fugaY, alto, apertura);

            int altoPared = Math.max(1, inf - sup);
            int y = sup + (int) (b * altoPared * 0.75F);
            int altoMancha = Math.max(2, (int) (altoPared * (0.06F + c * 0.16F)));
            int anchoMancha = Math.max(2, (int) (Math.abs(borde - fugaX) * 0.05F * (0.5F + c)));

            int x0 = izquierda ? borde : borde - anchoMancha;
            int x1 = izquierda ? borde + anchoMancha : borde;

            float luzMancha = luz * (0.30F + 0.70F * profundidad);
            grafico.fill(x0, y, x1, y + altoMancha,
                    Paleta.conAlfa(Paleta.iluminar(Paleta.MOHO, luzMancha), 0.18F + c * 0.22F));
        }
    }

    /**
     * La hilera de tubos del cielorraso, uno por tramo, encogiendo hacia el
     * fondo. El parpadeo es irregular a proposito y cada tubo tropieza en un
     * momento distinto: nunca parpadean todos a la vez.
     */
    private static void fluorescentes(GuiGraphics grafico, int ancho, int alto, int fugaX, int fugaY,
                                      float luz, float tiempo, boolean destellos) {
        for (int i = TRAMOS; i >= 1; i--) {
            float lejos = (float) i / (float) TRAMOS;
            float apertura = escala(lejos);

            int izq = interpolar(fugaX, 0, apertura);
            int der = interpolar(fugaX, ancho, apertura);
            int sup = interpolar(fugaY, 0, apertura);

            int centro = (izq + der) / 2;
            int medio = Math.max(2, (der - izq) / 14);
            int grosor = Math.max(1, (int) ((alto * 0.007F) * lejos));

            float propio = destellos ? tropiezo(tiempo, i) : 1.0F;
            float brillo = luz * (0.35F + 0.65F * lejos) * propio;

            int y = sup + Math.max(1, (int) (alto * 0.015F * lejos));

            // Halo del tubo sobre las placas del techo.
            int haloAncho = medio * 2;
            int haloAlto = Math.max(2, grosor * 4);
            grafico.fillGradient(centro - haloAncho, y - haloAlto, centro + haloAncho, y + haloAlto,
                    Paleta.conAlfa(Paleta.FLUOR, 0.0F),
                    Paleta.conAlfa(Paleta.FLUOR, 0.16F * brillo));

            // El tubo.
            grafico.fill(centro - medio, y, centro + medio, y + grosor,
                    Paleta.conAlfa(Paleta.FLUOR, 0.35F + 0.60F * brillo));

            // Reflejo palido en la alfombra, justo debajo.
            int infTramo = interpolar(fugaY, alto, apertura);
            int reflejoAlto = Math.max(2, (int) (alto * 0.05F * lejos));
            grafico.fillGradient(centro - medio, infTramo - reflejoAlto, centro + medio, infTramo,
                    Paleta.conAlfa(Paleta.FLUOR, 0.10F * brillo),
                    Paleta.conAlfa(Paleta.FLUOR, 0.0F));
        }
    }

    /** Parpadeo propio de cada tubo. Devuelve un factor de 0.3 a 1.0. */
    private static float tropiezo(float tiempo, int indice) {
        float desfase = pseudo(indice * 31 + 3) * 6.28F;
        float base = 0.88F
                + 0.07F * (float) Math.sin(tiempo * 2.1F + desfase)
                + 0.05F * (float) Math.sin(tiempo * 9.3F + desfase * 2.0F);
        boolean falla = Math.floorMod((long) (tiempo * 4.0F) + indice * 17L, 71L) == 0L;
        if (falla) {
            base *= 0.35F;
        }
        return Math.max(0.30F, Math.min(1.0F, base));
    }

    /**
     * El vano del fondo: la salida al nivel siguiente. Nunca se ilumina, y de
     * vez en cuando algo lo cruza sin apuro.
     */
    private static void vano(GuiGraphics grafico, int fugaX, int fugaY, int ancho, int alto,
                             long ahora, boolean movimiento) {
        int anchoVano = Math.max(10, (int) (ancho * 0.055F));
        int altoVano = Math.max(16, (int) (alto * 0.16F));

        int x0 = fugaX - anchoVano / 2;
        int x1 = fugaX + anchoVano / 2;
        int y0 = fugaY - altoVano / 3;
        int y1 = fugaY + (altoVano * 2) / 3;

        // Marco.
        grafico.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, Paleta.conAlfa(Paleta.MOHO, 0.75F));
        // El hueco.
        grafico.fill(x0, y0, x1, y1, Paleta.VANO);

        if (!movimiento) {
            return;
        }

        long fase = Math.floorMod(ahora, PERIODO_SILUETA_MS);
        if (fase > DURACION_SILUETA_MS) {
            return;
        }

        // Algo cruza el vano. No entra, no sale: pasa de largo.
        float t = (float) fase / (float) DURACION_SILUETA_MS;
        int anchoSilueta = Math.max(3, anchoVano / 3);
        int recorrido = anchoVano + anchoSilueta * 2;
        int x = x0 - anchoSilueta + (int) (t * recorrido);

        int visibleIzq = Math.max(x0, x);
        int visibleDer = Math.min(x1, x + anchoSilueta);
        if (visibleDer <= visibleIzq) {
            return;
        }

        float alfa = 0.85F * (float) Math.sin(t * Math.PI);
        int cuerpoY = y0 + altoVano / 6;
        grafico.fill(visibleIzq, cuerpoY, visibleDer, y1, Paleta.conAlfa(Paleta.ALERTA, alfa * 0.30F));
        grafico.fill(visibleIzq, cuerpoY, visibleDer, y1, Paleta.conAlfa(Paleta.VANO, alfa * 0.65F));
    }

    /** Polvo suspendido, mas visible cerca de los tubos. */
    private static void polvo(GuiGraphics grafico, int ancho, int alto, float tiempo, float luz) {
        for (int i = 0; i < MOTAS; i++) {
            float a = pseudo(i * 13 + 5);
            float b = pseudo(i * 29 + 11);

            int x = (int) ((a * ancho * 1.7F + tiempo * (3.0F + a * 5.0F)) % Math.max(1, ancho));
            int y = (int) ((b * alto + tiempo * (4.0F + b * 7.0F)) % Math.max(1, alto));

            float cercaDelCentro = 1.0F - Math.abs((float) y / Math.max(1.0F, alto) - 0.45F) * 2.0F;
            float brillo = (0.06F + 0.20F * luz) * Math.max(0.0F, cercaDelCentro);

            grafico.fill(x, y, x + 1, y + 1, Paleta.conAlfa(Paleta.FLUOR, brillo));
        }
    }

    private static void vineta(GuiGraphics grafico, int ancho, int alto, float penumbra) {
        int franja = Math.max(24, ancho / 6);
        float intensidad = 0.38F + 0.42F * penumbra;

        // fillGradient solo interpola en vertical: los lados se hacen por columnas.
        for (int x = 0; x < franja && x < ancho; x++) {
            float t = (float) x / (float) franja;
            grafico.fill(x, 0, x + 1, alto, Paleta.conAlfa(Paleta.VANO, intensidad * (1.0F - t)));
        }
        for (int x = Math.max(0, ancho - franja); x < ancho; x++) {
            float t = (float) (x - (ancho - franja)) / (float) franja;
            grafico.fill(x, 0, x + 1, alto, Paleta.conAlfa(Paleta.VANO, intensidad * t));
        }
        grafico.fillGradient(0, 0, ancho, franja / 2,
                Paleta.conAlfa(Paleta.VANO, intensidad * 0.55F), Paleta.conAlfa(Paleta.VANO, 0.0F));
        grafico.fillGradient(0, alto - franja / 2, ancho, alto,
                Paleta.conAlfa(Paleta.VANO, 0.0F), Paleta.conAlfa(Paleta.VANO, intensidad * 0.9F));

        if (penumbra > 0.0F) {
            grafico.fill(0, 0, ancho, alto, Paleta.conAlfa(Paleta.VANO, 0.35F * penumbra));
        }
    }

    /** Ruido determinista en el rango 0.0 a 1.0. Sin Random, sin estado. */
    private static float pseudo(int indice) {
        long h = SEMILLA + indice * 2654435761L;
        h ^= (h >>> 13);
        h *= 1274126177L;
        h ^= (h >>> 16);
        return (float) (Math.floorMod(h, 10_000L)) / 10_000.0F;
    }
}
