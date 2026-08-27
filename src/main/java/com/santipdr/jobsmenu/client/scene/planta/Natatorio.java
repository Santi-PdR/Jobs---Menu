package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Nivel 3 - El natatorio.
 *
 * Un complejo de piletas bajo un mismo techo, sin una sola ventana. El agua
 * esta quieta y tibia, el azulejo llega hasta el cielorraso y todo lo que suena
 * vuelve dos veces.
 *
 * Lo que lo distingue: el suelo NO es suelo. La baldosa de la orilla cubre todo
 * el piso del recinto, y encima de ella se apoya el vaso: un trapecio que
 * arranca lejos, contra la cabecera, y se abre hacia la camara. El agua tiene
 * su propio color, su propio canto y su propio reflejo del techo, y se oscurece
 * a medida que se acerca porque a los pies del que mira es donde mas hondo esta.
 *
 * Esa es la razon de que el vaso no llegue al fondo del cuadro: entre el borde
 * del agua y la pared del fondo queda la cabecera de baldosa, con su doble
 * puerta de vaiven. Sin ese respiro el agua se pega al horizonte y la escena
 * deja de leerse como una pileta para leerse como un pasillo verde.
 */
public final class Natatorio implements Planta {

    /** Tramos en profundidad. */
    private static final int TRAMOS = 14;

    /** A que dy termina la baldosa de la cabecera y empieza el agua. */
    private static final float CABECERA = 1.55F;

    /** Semiancho del vaso, en fraccion del semiancho del recinto. */
    private static final float VASO = 0.82F;

    /** Cuantas calles tiene la pileta. */
    private static final int CALLES = 4;

    @Override
    public int tramos() {
        return TRAMOS;
    }

    /**
     * En el natatorio no se apoya en el suelo: se apoya en el borde.
     *
     * Nada camina sobre el agua. Lo que aparece esta parado en la baldosa que
     * rodea el vaso, y eso lo pone mas cerca de la camara de lo que estaria en
     * cualquier otro recinto.
     */
    @Override
    public float pisoPresencia() {
        return 1.18F;
    }

    @Override
    public void dibujar(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        // El fondo va bien iluminado: aca el testero es azulejo blanco. Pero
        // con el recinto ancho, la fuerza que servia para un vano chico lo lava
        // entero y lo deja como una chapa blanca, asi que va mas contenida.
        Trazo.fondo(grafico, m, nivel, luz,
                Paleta.mezclar(nivel.paredBaja, nivel.techo, 0.70F), 2.15F);
        testero(grafico, m, nivel, luz);

        Trazo.plano(grafico, m, true, nivel.techo,
                Paleta.mezclar(nivel.techo, nivel.niebla, 0.30F), nivel.niebla, luz, 0.44F);
        Trazo.transversales(grafico, m, true, nivel.techoJunta, nivel.niebla, luz, TRAMOS, 0.26F);
        claraboyas(grafico, m, nivel, luz);

        borde(grafico, m, nivel, luz);
        agua(grafico, m, nivel, luz, tiempo);
        calles(grafico, m, nivel, luz, tiempo);

        Trazo.paredes(grafico, m, nivel, luz);
        cenefa(grafico, m, nivel, luz);
        Trazo.juntasVerticales(grafico, m, nivel, luz, TRAMOS, 1.0F, 0.24F);
        Trazo.manchas(grafico, m, nivel, luz, TRAMOS);

        // La escalerilla va despues de las paredes: esta por delante de ellas.
        escalerilla(grafico, m, nivel, luz);
        caustica(grafico, m, nivel, luz, tiempo);

        // El recinto entero se oscurece en los bordes: cuanto mas lejos del
        // centro del vaso, mas se pierde la luz. No es una vineta rectangular
        // comun; es que la humedad y la distancia se llevan la luz de los
        // bordes del recinto, y eso se nota mucho mas en un natatorio que en
        // un pasillo, donde el fondo es una pared y no agua.
        int ancho = m.ancho();
        int alto = m.alto();
        int franja = Math.max(12, ancho / 5);
        for (int x = 0; x < franja; x += 4) {
            float t = 1.0F - x / (float) franja;
            grafico.fill(x, 0, x + 4, alto,
                    Paleta.conAlfa(Paleta.VANO, 0.12F * t * t));
            grafico.fill(ancho - x - 4, 0, ancho - x, alto,
                    Paleta.conAlfa(Paleta.VANO, 0.12F * t * t));
        }
        int franjaV = Math.max(8, alto / 6);
        for (int y = 0; y < franjaV; y += 4) {
            float t = 1.0F - y / (float) franjaV;
            grafico.fill(0, y, ancho, y + 4,
                    Paleta.conAlfa(Paleta.VANO, 0.09F * t * t));
            grafico.fill(0, alto - y - 4, ancho, alto - y,
                    Paleta.conAlfa(Paleta.VANO, 0.09F * t * t));
        }
    }

    /**
     * La doble puerta de vaiven de la cabecera, con su franja de vidrio armado.
     *
     * Es lo unico que dice que este complejo tiene mas salas iguales a esta del
     * otro lado. El vidrio esta oscuro: ahi no hay luz encendida.
     */
    private static void testero(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        // Azulejo del testero, antes que la puerta. Con el recinto ancho la
        // pared del fondo es una superficie grande: si queda lisa se lee como
        // una chapa gris pegada en medio del cuadro, no como el fondo de un
        // natatorio. Las hiladas se juntan hacia el zocalo, como en la obra.
        int fx0 = Math.round(m.izq(1.0F));
        int fx1 = Math.round(m.der(1.0F));
        int fy0 = Math.round(m.techoEn(1.0F));
        int fy1 = Math.round(m.sueloEn(1.0F));
        int hiladas = 9;
        for (int k = 1; k < hiladas; k++) {
            float f = (float) Math.pow((float) k / hiladas, 1.25F);
            int y = (int) (fy0 + (fy1 - fy0) * f);
            float desvio = Trazo.pseudo(k * 191 + 37) * 0.10F - 0.05F;
            grafico.fill(fx0, y, fx1, y + 1,
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz * (0.85F + desvio)), 0.28F));
        }
        // Varios juntas verticales de baldosilla, no solo dos: un azulejo real
        // tiene columnas de teja, y un natatorio de verdad los azulejos son
        // cuadrados que se cuentan desde el techo hasta el suelo.
        int columnas = 7;
        for (int c = 1; c < columnas; c++) {
            float f = (float) c / columnas;
            float desvio = Trazo.pseudo(c * 271 + 11) * 0.10F - 0.05F;
            int x = (int) (fx0 + (fx1 - fx0) * f);
            grafico.fill(x, fy0, x + 1, fy1,
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz * (0.85F + desvio)), 0.18F));
        }

        float suelo = m.sueloEn(CABECERA);
        float alto = m.h() * 1.05F;
        int x0 = Math.round(m.izq(0.30F));
        int x1 = Math.round(m.der(0.30F));
        int y0 = Math.round(suelo - alto);
        int y1 = Math.round(suelo);

        grafico.fill(x0 - 2, y0 - 2, x1 + 2, y1,
                Paleta.iluminar(Paleta.mezclar(nivel.junta, nivel.techo, 0.30F), luz * 0.75F));
        grafico.fill(x0, y0, x1, y1,
                Paleta.iluminar(Paleta.mezclar(nivel.paredBaja, Paleta.VANO, 0.45F), luz * 0.55F));
        grafico.fill((x0 + x1) / 2, y0, (x0 + x1) / 2 + 1, y1, Paleta.conAlfa(Paleta.VANO, 0.55F));

        int vy = y0 + (y1 - y0) / 4;
        int altoVidrio = Math.max(3, (y1 - y0) / 5);
        for (int lado = 0; lado < 2; lado++) {
            int vx0 = x0 + 3 + lado * (x1 - x0) / 2;
            int vx1 = vx0 + (x1 - x0) / 2 - 6;
            grafico.fill(vx0, vy, vx1, vy + altoVidrio, Paleta.conAlfa(Paleta.VANO, 0.70F));
        }
        // La junta de baldosa al pie, corrida de pared a pared.
        grafico.fill(Math.round(m.izq(CABECERA)), y1 - 2,
                Math.round(m.der(CABECERA)), y1,
                Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz), 0.40F));
    }

    /** Lo unico que ilumina esto entra por arriba, y no alcanza. */
    private static void claraboyas(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int j = 2; j <= TRAMOS; j++) {
            float dx = Trazo.profundidad(j, TRAMOS);
            if (dx > 6.0F || Trazo.pseudo(1200 + j) <= 0.45F) {
                continue;
            }
            // En el natatorio las luminarias estan mas apagadas: el recinto es
            // grande y humedo, y la luz del techo recorre menos antes de perderse.
            Trazo.luminaria(grafico, m, nivel, dx, 0.97F, 0.30F, 1.10F, luz);
        }
    }

    /** La baldosa de la orilla. Ocupa todo el suelo; el agua se le apoya. */
    private static void borde(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int y = Math.round(m.sueloEn(1.0F)); y < m.alto(); y += Trazo.PASO) {
            float dy = m.dy(y + Trazo.PASO * 0.5F);
            if (dy <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
            // Cada baldosa recibe un leve desvio: un piso real no es monocromatico.
            float desvio = Trazo.pseudo((int) (dy * 337.0F) + 13) * 0.12F - 0.06F;
            int color = Trazo.velar(Paleta.mezclar(nivel.techo, nivel.paredBaja, 0.30F + desvio),
                    nivel.niebla, lej, 0.38F);
            grafico.fill(0, y, m.ancho(), y + Trazo.PASO,
                    Paleta.iluminar(color, Trazo.atenuar(luz, lej)));
        }
        Trazo.transversales(grafico, m, false, nivel.techoJunta, nivel.niebla, luz, TRAMOS, 0.20F);

        // El borde de la orilla: donde la baldosa encuentra el agua, hay un filo
        // de luz que tambien dice donde empieza el vaso. No es una linea negra
        // sino la reflexion del techo en el canto humedo de la baldosa.
        float frontera = m.sueloEn(CABECERA);
        int fyBorde = Math.round(frontera);
        if (fyBorde >= 0 && fyBorde < m.alto()) {
            float dyBorde = m.dy(frontera);
            float lejBorde = Trazo.limitar(1.0F / dyBorde, 0.0F, 1.0F);
            int filo = Paleta.conAlfa(Paleta.iluminar(nivel.techo, luz * 0.80F), 0.55F);
            grafico.fill(0, fyBorde, m.ancho(), fyBorde + 1, filo);
            // La fila de baldosilla inmediatamente delante del agua queda
            // mas clara por reflejo del agua mismo.
            grafico.fill(0, fyBorde - 1, m.ancho(), fyBorde,
                    Paleta.conAlfa(Paleta.iluminar(nivel.suelo, luz * 0.40F), 0.35F));
        }
    }

    /**
     * El vaso y el reflejo del techo sobre el.
     *
     * El agua se oscurece hacia la camara porque hacia alla esta la parte
     * honda. El canto claro a los dos lados es el filo de la baldosa mojada:
     * son dos pixeles y son la mitad de la lectura del efecto.
     */
    private static void agua(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        int desde = Math.round(m.sueloEn(CABECERA));

        for (int y = desde; y < m.alto(); y += Trazo.PASO) {
            float dy = m.dy(y + Trazo.PASO * 0.5F);
            float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
            int x0 = (int) m.enX(dy, -VASO);
            int x1 = (int) m.enX(dy, VASO);
            if (x1 <= 0 || x0 >= m.ancho()) {
                continue;
            }
            float hondo = Trazo.limitar((dy - CABECERA) / (TRAMOS * 0.35F), 0.0F, 1.0F);
            // El agua del natatorio es mas oscura que el suelo: la masa de agua
            // tiene su propio tono, mas teal y mas hondo que las baldosas que la
            // reciben. Cuanto mas hondo, mas se acerca al color del fondo.
            int aguaBase = Trazo.velar(
                    Paleta.mezclar(nivel.suelo, nivel.paredBaja, 0.45F + hondo * 0.35F),
                    nivel.niebla, lej, 0.30F);
            grafico.fill(Math.max(0, x0), y, Math.min(m.ancho(), x1), y + Trazo.PASO,
                    Paleta.iluminar(aguaBase, Trazo.atenuar(luz, lej) * 0.90F));

            int canto = Paleta.conAlfa(Paleta.iluminar(nivel.techo, luz), 0.60F);
            grafico.fill(Math.max(0, x0 - 2), y, Math.max(0, x0), y + Trazo.PASO, canto);
            grafico.fill(Math.min(m.ancho(), x1), y, Math.min(m.ancho(), x1 + 2), y + Trazo.PASO, canto);
        }

        // La cabecera del vaso, al fondo.
        grafico.fill((int) m.enX(CABECERA, -VASO), desde - 2,
                (int) m.enX(CABECERA, VASO), desde + 1,
                Paleta.conAlfa(Paleta.iluminar(nivel.techo, luz), 0.70F));

        // El reflejo del techo, deshecho por una ondulacion lentisima.
        float largo = m.h() * 1.4F;
        int hasta = Math.min(m.alto(), desde + (int) largo);
        for (int y = desde; y < hasta; y += Trazo.PASO) {
            float t = (y - desde) / largo;
            float dy = m.dy(y);
            float onda = (float) Math.sin(tiempo * 0.45F + t * 7.0F) * m.w() * 0.012F;
            grafico.fill((int) (m.enX(dy, -VASO * 0.70F) + onda), y,
                    (int) (m.enX(dy, VASO * 0.70F) + onda), y + Trazo.PASO,
                    Paleta.conAlfa(nivel.techo, 0.22F * (1.0F - t) * (1.0F - t) * luz));
        }

        // Niebla superficial: el aire sobre el agua dentro de un natatorio
        // cerrado siempre esta cargado de humedad. Es mas visible a poca
        // distancia del borde (arriba del agua en pantalla) y se disipa hacia
        // la camara (abajo en pantalla).
        for (int y = desde; y < m.alto(); y += Trazo.PASO) {
            float dy = m.dy(y + Trazo.PASO * 0.5F);
            float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
            int x0 = (int) m.enX(dy, -VASO);
            int x1 = (int) m.enX(dy, VASO);
            if (x1 <= 0 || x0 >= m.ancho()) {
                continue;
            }
            float prof = Trazo.limitar((dy - CABECERA) / (TRAMOS * 0.35F), 0.0F, 1.0F);
            // La niebla es mas densa cerca del fondo del vaso (arriba en pantalla)
            // y menos cerca de la camara (abajo). Atauencia del aire con el agua.
            float humedad = (1.0F - prof) * 0.18F * luz;
            if (humedad <= 0.0F) {
                continue;
            }
            int x0i = Math.max(0, x0);
            int x1i = Math.min(m.ancho(), x1);
            if (x1i <= x0i) {
                continue;
            }
            int niebla = Paleta.conAlfa(Paleta.mezclar(nivel.paredAlta, nivel.paredBaja, 0.50F),
                    humedad);
            grafico.fill(x0i, y, x1i, y + Trazo.PASO, niebla);
        }

        // Burbujas y motas: en un natatorio quieto hay siempre algo flotando.
        // Se dibujan como pocos pixeles brillantes en el agua, cada uno en una
        // posicion fija que da el ruido reproducible.
        int burbujas = 14;
        for (int i = 0; i < burbujas; i++) {
            float seed = Trazo.pseudo(i * 53 + 7);
            int dxB = Math.round(1.1F + seed * (TRAMOS * 0.55F));
            float lejB = Trazo.limitar(1.0F / dxB, 0.0F, 1.0F);
            float xPos = m.enX(dxB, (Trazo.pseudo(i * 53 + 11) - 0.5F) * VASO * 1.6F);
            float yPos = m.sueloEn(dxB);
            if (xPos < 0 || xPos > m.ancho() || yPos < desde || yPos > m.alto()) {
                continue;
            }
            int alt = Math.max(1, (int) (m.h() * dxB * 0.035F));
            int py = (int) yPos - alt;
            if (py < desde || py > m.alto()) {
                continue;
            }
            float brillo = 0.08F + Trazo.pseudo(i * 53 + 19) * 0.14F;
            brillo *= luz;
            grafico.fill((int) xPos, py, (int) xPos + 1, py + alt,
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, brillo), 0.50F));
        }
    }

    /**
     * Las lineas del fondo del vaso.
     *
     * Van quebradas: se las mira a traves de una capa de agua que se mueve, y
     * si salieran rectas el agua dejaria de existir.
     */
    private static void calles(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        int desde = Math.round(m.sueloEn(CABECERA));
        for (int i = 1; i < CALLES; i++) {
            float frac = (i / (float) CALLES) * 2.0F - 1.0F;
            for (int y = desde; y < m.alto(); y += Trazo.PASO) {
                float dy = m.dy(y + Trazo.PASO * 0.5F);
                float lej = Trazo.limitar(1.0F / dy, 0.0F, 1.0F);
                float onda = (float) Math.sin(tiempo * 0.5F + dy * 2.2F + i * 1.7F) * m.w() * 0.010F;
                float x = m.enX(dy, frac * VASO) + onda;
                int grosor = Math.max(1, (int) (m.w() * dy * 0.012F));
                grafico.fill((int) x, y, (int) x + grosor, y + Trazo.PASO,
                        Paleta.conAlfa(Paleta.iluminar(nivel.techo, luz), 0.26F + 0.16F * lej));
            }
        }
    }

    /**
     * Las dos barandas curvas de la escalerilla, asomando del agua.
     *
     * Es el unico objeto de la escena con una medida que todo el mundo conoce
     * de memoria. Es lo que fija la escala de la pileta entera.
     */
    private static void escalerilla(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float dy = 1.95F;
        float y = m.sueloEn(dy);
        float x = m.der(dy * VASO);
        if (x > m.ancho() + 24 || y > m.alto() + 24) {
            return;
        }
        float alto = m.h() * dy * 0.30F;
        float sep = m.w() * dy * 0.055F;
        int color = Paleta.iluminar(Paleta.mezclar(nivel.techo, Paleta.VANO, 0.25F), luz * 0.90F);
        int grosor = Math.max(2, (int) (m.w() * dy * 0.010F));

        for (int signo = -1; signo <= 1; signo += 2) {
            float px = x + sep * signo * 0.5F;
            grafico.fill((int) px, (int) (y - alto), (int) px + grosor, (int) y, color);
        }
        grafico.fill((int) (x - sep * 0.5F), (int) (y - alto),
                (int) (x + sep * 0.5F) + grosor, (int) (y - alto) + grosor, color);
        for (int k = 0; k < 2; k++) {
            float py = y - alto * 0.55F + alto * 0.30F * k;
            grafico.fill((int) (x - sep * 0.5F), (int) py,
                    (int) (x + sep * 0.5F) + grosor, (int) py + grosor,
                    Paleta.conAlfa(color, 0.80F));
        }
    }

    /** La cenefa de azulejo a la altura de los ojos. Sin ella hay revoque. */
    private static void cenefa(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        for (int x = 0; x < m.ancho(); x += Trazo.PASO) {
            float dx = m.dx(x + Trazo.PASO * 0.5F);
            if (dx <= 1.0F) {
                continue;
            }
            float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
            float at = Trazo.atenuar(luz, lej);
            float y = m.techoEn(dx * 0.16F);
            int alto = Math.max(1, (int) (m.h() * dx * 0.075F));
            grafico.fill(x, (int) y, x + Trazo.PASO, (int) y + alto,
                    Paleta.conAlfa(Paleta.iluminar(
                            Trazo.velar(nivel.junta, nivel.niebla, lej, 0.5F), at), 0.55F));
            grafico.fill(x, (int) y + alto, x + Trazo.PASO, (int) y + alto + 1,
                    Paleta.conAlfa(Paleta.iluminar(nivel.techo, at), 0.35F));
        }
    }

    /**
     * La caustica: la red de luz que el agua devuelve a las paredes.
     *
     * Sube lentisima, en siete bandas de velocidad distinta que nunca vuelven a
     * coincidir. Es el unico movimiento continuo de los cuatro recintos y es lo
     * que hace que el natatorio se sienta vivo estando completamente quieto.
     */
    private static void caustica(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        for (int b = 0; b < 7; b++) {
            float fase = tiempo * (0.10F + Trazo.pseudo(1300 + b) * 0.07F) + b * 0.9F;
            float altura = fase - (float) Math.floor(fase);
            float a = 0.11F * (float) Math.sin(Math.PI * altura) * luz;
            if (a <= 0.004F) {
                continue;
            }
            for (int x = 0; x < m.ancho(); x += Trazo.PASO * 2) {
                float dx = m.dx(x + Trazo.PASO);
                if (dx <= 1.0F) {
                    continue;
                }
                float lej = Trazo.limitar(1.0F / dx, 0.0F, 1.0F);
                float ySuelo = m.sueloEn(dx);
                float y = ySuelo - (ySuelo - m.fy()) * altura;
                int grueso = Math.max(1, (int) (m.h() * dx * 0.020F));
                float ondul = (float) Math.sin(x * 0.06F + tiempo * 0.7F + b) * 0.5F + 0.5F;
                grafico.fill(x, (int) y, x + Trazo.PASO * 2, (int) y + grueso,
                        Paleta.conAlfa(nivel.luz,
                                a * (0.35F + 0.65F * ondul) * (0.4F + 0.6F * lej)));
            }
        }
    }

    /** El primer plano de este recinto: el trampolin que entra desde arriba. */
    @Override
    public void primerPlano(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        PrimerPlano.natatorio(grafico, m, nivel, luz, tiempo);
    }
}
