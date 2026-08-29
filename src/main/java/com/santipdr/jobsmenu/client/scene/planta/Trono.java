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

    /** A que dy arranca la tarima del trono. Mas cerca = mas presente. */
    private static final float TARIMA = 1.42F;

    /** Remate del respaldo; constante para no crear una matriz en cada frame. */
    private static final float[][] PICOS_CORONA = {
            {-0.5F, 0.06F}, {0.0F, 0.13F}, {0.5F, 0.06F}
    };

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

        // El abside que enmarca el trono, contra el testero del fondo.
        abside(grafico, m, nivel, luz);

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
        dintelRoto(grafico, m, nivel, luz);
        humedadAbside(grafico, m, nivel, luz);
        Trazo.manchas(grafico, m, nivel, luz, TRAMOS);

        // El haz cenital cae ANTES del trono, para que este quede recortado
        // encima de la luz y no la luz encima del trono.
        hazMayor(grafico, m, nivel, luz, tiempo);
        // El trono al fondo, antes de las columnas de primer plano.
        trono(grafico, m, nivel, luz, tiempo);

        columnas(grafico, m, nivel, luz);
        estandartes(grafico, m, nivel, luz, tiempo);
        haces(grafico, m, nivel, luz, tiempo);
    }

    /**
     * El abside: un nicho de piedra al fondo que le da al trono una pared propia.
     *
     * En vez de dejar el testero plano y negro -donde el trono flotaba en un
     * vacio-, se recorta un arco elevado detras de la tarima, mas claro por
     * dentro y con el reborde dorado. Enmarca el trono y lo ancla al mundo.
     */
    private static void abside(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float dx = 1.0F;
        float cx = m.centro(dx);
        float ancho = m.anchoEn(dx) * 0.30F;
        float ySuelo = m.sueloEn(dx);
        float yTecho = m.techoEn(dx);
        float yArco = yTecho + (ySuelo - yTecho) * 0.20F;
        float hombro = yTecho + (ySuelo - yTecho) * 0.34F;
        int interior = Paleta.iluminar(Paleta.mezclar(nivel.paredBaja, nivel.niebla, 0.35F), luz * 0.55F);

        grafico.fill((int) (cx - ancho), (int) hombro, (int) (cx + ancho), (int) ySuelo, interior);

        int pasos = 7;
        int paso = Math.max(1, (int) ((hombro - yArco) / pasos) + 1);
        for (int k = 0; k < pasos; k++) {
            float f = k / (float) (pasos - 1);
            float w = ancho * (1.0F - (1.0F - f) * (1.0F - f));
            int yk = (int) (hombro + (yArco - hombro) * f);
            int y0 = (k == pasos - 1) ? (int) yArco : yk;
            grafico.fill((int) (cx - w), y0, (int) (cx + w), yk + paso, interior);
        }

        int borde = Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz * 0.7F), 0.5F);
        for (int k = 0; k < pasos; k++) {
            float f = k / (float) (pasos - 1);
            float w = ancho * (1.0F - (1.0F - f) * (1.0F - f));
            int yk = (int) (hombro + (yArco - hombro) * f);
            grafico.fill((int) (cx - w) - 1, yk, (int) (cx - w) + 1, yk + 3, borde);
            grafico.fill((int) (cx + w) - 1, yk, (int) (cx + w) + 1, yk + 3, borde);
        }
    }

    /**
     * Dintel de piedra que perdio dos bloques y quedo suspendido sobre el
     * abside. El hueco irregular evita que el arco se lea como un sticker
     * perfecto, y el cambio de altura ancla el remate a las juntas del muro.
     */
    private static void dintelRoto(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float dx = 1.0F;
        float techo = m.techoEn(dx);
        float suelo = m.sueloEn(dx);
        float y = techo + (suelo - techo) * 0.105F;
        float ancho = m.anchoEn(dx) * 0.43F;
        float bloque = ancho / 6.0F;
        int piedra = Paleta.iluminar(Trazo.velar(nivel.junta, nivel.niebla, 1.0F, 0.35F), luz * 0.68F);
        int canto = Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz * 0.62F), 0.38F);

        for (int i = 0; i < 6; i++) {
            // Dos faltantes consecutivos dejan ver el oscuro del abside.
            if (i == 2 || i == 3) {
                continue;
            }
            float x0 = m.centro(dx) - ancho * 0.5F + bloque * i;
            int y0 = Math.round(y + (i % 2 == 0 ? 0.0F : 2.0F));
            int y1 = y0 + Math.max(3, Math.round(m.h() * dx * 0.055F));
            grafico.fill(Math.round(x0), y0, Math.round(x0 + bloque + 1.0F), y1, piedra);
            grafico.fill(Math.round(x0), y0, Math.round(x0 + bloque + 1.0F), y0 + 1, canto);
        }
    }

    /**
     * Humedad localizada en la piedra baja del abside. No es un velo global:
     * nace donde el muro toca el suelo y se corta antes de invadir el trono.
     */
    private static void humedadAbside(GuiGraphics grafico, Marco m, Nivel nivel, float luz) {
        float dx = 1.0F;
        float y = m.sueloEn(dx) - m.h() * 0.10F;
        int color = Paleta.mezclar(nivel.niebla, nivel.paredBaja, 0.45F);
        for (int lado = -1; lado <= 1; lado += 2) {
            float x = m.lado(lado, dx * 0.24F);
            float ancho = m.anchoEn(dx) * 0.055F;
            int alfa = Math.round(42.0F * nivel.humedad * luz);
            grafico.fill(Math.round(x - ancho), Math.round(y - m.h() * 0.08F),
                    Math.round(x + ancho), Math.round(y + m.h() * 0.02F),
                    Paleta.conAlfa(Paleta.iluminar(color, luz * 0.65F), alfa / 255.0F));
            grafico.fill(Math.round(x - ancho * 0.45F), Math.round(y - m.h() * 0.18F),
                    Math.round(x + ancho * 0.45F), Math.round(y - m.h() * 0.06F),
                    Paleta.conAlfa(Paleta.iluminar(color, luz * 0.52F), alfa * 0.65F / 255.0F));
        }
    }

    /**
     * El haz cenital que cae sobre el trono: el gesto central de la sala.
     *
     * Baja desde el techo hasta la tarima, ancho y luminoso, ensanchandose al
     * caer, con un charco de luz al pie y motas de polvo suspendidas dentro. Es
     * lo que dirige la mirada al fondo, donde espera un asiento vacio.
     */
    private static void hazMayor(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        float dx = TARIMA;
        float cx = m.centro(dx);
        float yTop = m.techoEn(dx * 0.30F);
        float yBot = m.sueloEn(dx);
        float ancho = m.anchoEn(dx) * 0.13F;
        float parpadeo = Trazo.pulsoLuz(0.9F, 0.1F, tiempo, 0.5F, 0.0F);
        int pasos = 26;
        int alturaPaso = (int) ((yBot - yTop) / pasos) + 1;
        for (int k = 0; k < pasos; k++) {
            float t = k / (float) (pasos - 1);
            float w = ancho * (0.45F + t * 0.75F);
            int y = (int) (yTop + (yBot - yTop) * t);
            float a = 0.13F * luz * parpadeo * (0.35F + 0.65F * t);
            grafico.fill((int) (cx - w), y, (int) (cx + w), y + alturaPaso,
                    Paleta.conAlfa(Paleta.iluminar(0xFFFFF0C0, luz), a));
        }
        float w = ancho * 1.35F;
        grafico.fill((int) (cx - w), (int) yBot - 3, (int) (cx + w), (int) yBot + 4,
                Paleta.conAlfa(Paleta.iluminar(0xFFFFF0C0, luz), 0.16F * luz));
        for (int i = 0; i < 18; i++) {
            float px = cx + (Trazo.pseudo(700 + i) - 0.5F) * ancho * 1.6F;
            float py = yTop + ((Trazo.pseudo(720 + i) + tiempo * 0.02F * (0.5F + Trazo.pseudo(740 + i))) % 1.0F)
                    * (yBot - yTop);
            int s = Trazo.pseudo(760 + i) < 0.7F ? 1 : 2;
            grafico.fill((int) px, (int) py, (int) px + s, (int) py + s,
                    Paleta.conAlfa(Paleta.iluminar(0xFFFFF6D8, luz), 0.35F * luz));
        }
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
            float cx = m.enX(dx, signo * 0.34F);
            // El hueco pertenece al plano del techo: usar su misma profundidad
            // evita los "cuadrados flotantes" que aparecian al proyectarlo con
            // dx * 0.5. Es pequeno y asimetrico para que lea como ruina.
            float cy = m.techoEn(dx);
            float w = Math.max(3.0F, m.w() * dx * 0.095F);
            float h = Math.max(2.0F, m.h() * dx * 0.045F);
            int cielo = Paleta.conAlfa(Paleta.iluminar(Paleta.mezclar(nivel.niebla, 0xFF8090A0, 0.4F),
                    luz * 0.7F), 0.82F);
            grafico.fill((int) (cx - w), (int) (cy - h * 0.35F), (int) (cx + w * 0.72F), (int) (cy + h), cielo);
            grafico.fill((int) (cx + w * 0.72F), (int) (cy - h * 0.10F), (int) (cx + w), (int) (cy + h * 0.65F), cielo);
            // Una junta rota abajo lo integra en la placa, en vez de dibujar
            // un rectangulo aislado sobre la escena.
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
        float anchoBase = m.anchoEn(dx) * 0.32F;
        float altoEsc = m.h() * dx * 0.055F;
        int oro = Paleta.iluminar(Trazo.velar(nivel.luz, nivel.niebla, lej, 0.18F), Math.min(1.0F, at * 1.15F));
        int oroVivo = Paleta.iluminar(nivel.luz, Math.min(1.0F, at + 0.25F));
        int sombra = Paleta.iluminar(Trazo.velar(nivel.paredBaja, nivel.niebla, lej, 0.45F), at * 0.48F);

        // La tarima: cinco escalones anchos que suben al trono, cada uno con su
        // canto iluminado. Se lee como un estrado, no como un cajon.
        int escalones = 5;
        for (int e = 0; e < escalones; e++) {
            float w = anchoBase * (1.0F - e * 0.11F);
            float yTop = suelo - altoEsc * (e + 1);
            int col = Paleta.iluminar(Trazo.velar(nivel.paredAlta, nivel.niebla, lej, 0.4F),
                    at * (0.62F + e * 0.07F));
            grafico.fill((int) (cx - w), (int) yTop, (int) (cx + w), (int) (suelo - altoEsc * e), col);
            grafico.fill((int) (cx - w), (int) yTop, (int) (cx + w), (int) yTop + 2,
                    Paleta.conAlfa(oroVivo, 0.45F));
        }
        cantosGastados(grafico, cx, suelo, anchoBase, altoEsc, escalones, nivel, at);

        float base = suelo - altoEsc * escalones;
        float atW = anchoBase * 0.52F;
        float respaldo = m.h() * dx * 0.62F;
        float asientoH = m.h() * dx * 0.16F;
        float brazoH = m.h() * dx * 0.20F;
        int mont = Math.max(2, (int) (atW * 0.10F));

        // Sombra proyectada del trono sobre el abside.
        grafico.fill((int) (cx - atW * 0.5F) - 2, (int) (base - respaldo),
                (int) (cx + atW * 0.5F) + 2, (int) base, Paleta.conAlfa(0xFF000000, 0.28F));

        // Respaldo alto, interior en sombra.
        grafico.fill((int) (cx - atW * 0.5F), (int) (base - respaldo),
                (int) (cx + atW * 0.5F), (int) base, sombra);
        // Montantes dorados gruesos a los lados.
        grafico.fill((int) (cx - atW * 0.5F), (int) (base - respaldo),
                (int) (cx - atW * 0.5F) + mont, (int) base, oro);
        grafico.fill((int) (cx + atW * 0.5F) - mont, (int) (base - respaldo),
                (int) (cx + atW * 0.5F), (int) base, oro);
        // Nervaduras verticales del respaldo, tenues.
        for (int r = 1; r < 4; r++) {
            float rx = cx - atW * 0.5F + atW * r / 4.0F;
            grafico.fill((int) rx, (int) (base - respaldo * 0.92F), (int) rx + 1, (int) (base - asientoH),
                    Paleta.conAlfa(oro, 0.35F));
        }
        // Remate coronado: tres picos.
        int picoY = (int) (base - respaldo);
        for (float[] p : PICOS_CORONA) {
            float px = cx + atW * p[0];
            float hPico = m.h() * dx * p[1];
            grafico.fill((int) px - mont / 2, (int) (picoY - hPico),
                    (int) px + mont / 2 + 1, picoY + 1, oro);
        }
        // El hueco de la corona ausente, en el pico central.
        int hx = (int) cx;
        int hy = (int) (picoY - m.h() * dx * 0.10F);
        int hw = Math.max(2, (int) (atW * 0.12F));
        grafico.fill(hx - hw, hy, hx + hw, hy + hw * 2, Paleta.conAlfa(0xFF000000, 0.55F));
        // Asiento.
        grafico.fill((int) (cx - atW * 0.5F), (int) (base - asientoH - brazoH),
                (int) (cx + atW * 0.5F), (int) (base - brazoH), sombra);
        grafico.fill((int) (cx - atW * 0.5F), (int) (base - asientoH - brazoH),
                (int) (cx + atW * 0.5F), (int) (base - asientoH - brazoH) + 2, oro);
        // Brazos: dos bloques dorados a los lados del asiento.
        int brazoW = Math.max(2, (int) (atW * 0.14F));
        grafico.fill((int) (cx - atW * 0.5F), (int) (base - asientoH - brazoH),
                (int) (cx - atW * 0.5F) + brazoW, (int) (base - brazoH * 0.2F), oro);
        grafico.fill((int) (cx + atW * 0.5F) - brazoW, (int) (base - asientoH - brazoH),
                (int) (cx + atW * 0.5F), (int) (base - brazoH * 0.2F), oro);
        // Cojin del asiento: un toque del color de la alfombra, gastado.
        int coj = Paleta.iluminar(Trazo.velar(Paleta.mezclar(nivel.sueloJunta, nivel.luz, 0.3F),
                nivel.niebla, lej, 0.4F), at * 0.7F);
        grafico.fill((int) (cx - atW * 0.5F) + brazoW, (int) (base - asientoH - brazoH * 0.55F),
                (int) (cx + atW * 0.5F) - brazoW, (int) (base - brazoH * 0.55F), coj);
    }

    /**
     * Desgaste localizado en los cantos de la tarima: el paso borra el oro en
     * segmentos cortos, en vez de ensuciar todos los escalones por igual.
     */
    private static void cantosGastados(GuiGraphics grafico, float cx, float suelo,
                                       float anchoBase, float altoEsc, int escalones,
                                       Nivel nivel, float at) {
        int desgaste = Paleta.iluminar(Trazo.velar(nivel.sueloJunta, nivel.niebla, 0.70F, 0.45F), at * 0.80F);
        int brillo = Paleta.conAlfa(Paleta.iluminar(nivel.luz, at), 0.30F);
        for (int e = 0; e < escalones; e++) {
            float w = anchoBase * (1.0F - e * 0.11F);
            int y = (int) (suelo - altoEsc * (e + 1));
            int largo = Math.max(2, (int) (w * (0.10F + Trazo.pseudo(870 + e) * 0.16F)));
            int x = (int) (cx - w + w * (0.16F + Trazo.pseudo(880 + e) * 0.28F));
            grafico.fill(x, y, x + largo, y + 2, Paleta.conAlfa(desgaste, 0.50F));
            if (e == 2) {
                // Un solo canto devuelve luz: el metal gastado no es un halo.
                grafico.fill((int) (cx + w * 0.30F), y, (int) (cx + w * 0.30F) + Math.max(2, largo / 2), y + 1, brillo);
            }
        }
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
            // La cuerda del techo al asta: el estandarte no cuelga de la nada.
            float yTecho = m.techoEn(dx * 0.95F);
            grafico.fill((int) x, (int) yTecho, (int) x + 1, (int) yTop,
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, at * 0.55F), 0.50F));
            // Asta horizontal.
            grafico.fill((int) (x - ancho * 0.5F), (int) yTop - 1, (int) (x + ancho * 0.5F), (int) yTop,
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, at * 0.70F), 0.80F));
            // Tela: panio oscuro, no una banda de oro. El oro es solo el galon
            // y el emblema, que es lo que dice "estandarte" en una sola mirada.
            int tela = Paleta.iluminar(Trazo.velar(Paleta.mezclar(nivel.paredBaja, nivel.junta, 0.35F),
                    nivel.niebla, lej, 0.4F), at * 0.85F);
            // La tela, rota al final (los ultimos jirones se afinan).
            for (int k = 0; k < 8; k++) {
                float f = k / 8.0F;
                float w = ancho * (1.0F - f * 0.5F);
                float ox = onda * f;
                grafico.fill((int) (x - w * 0.5F + ox), (int) (yTop + alto * f), (int) (x + w * 0.5F + ox), (int) (yTop + alto * (f + 0.14F)),
                        Paleta.conAlfa(tela, 0.85F * (1.0F - f * 0.3F)));
            }
            // Galon superior dorado.
            grafico.fill((int) (x - ancho * 0.5F), (int) yTop, (int) (x + ancho * 0.5F), (int) (yTop + Math.max(1, alto * 0.06F)),
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, at), 0.55F));
            // Emblema: un rombo tenue en el centro del panio.
            float ey = yTop + alto * 0.40F;
            grafico.fill((int) (x - ancho * 0.18F), (int) ey, (int) (x + ancho * 0.18F), (int) (ey + alto * 0.14F),
                    Paleta.conAlfa(Paleta.iluminar(nivel.luz, at * 0.8F), 0.30F));
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
            float parpadeo = Trazo.pulsoLuz(0.8F, 0.2F, tiempo, 0.4F, i);
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
