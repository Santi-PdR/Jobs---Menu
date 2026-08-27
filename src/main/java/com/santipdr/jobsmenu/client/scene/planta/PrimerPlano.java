package com.santipdr.jobsmenu.client.scene.planta;

import com.santipdr.jobsmenu.client.scene.Marco;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Lo que esta MAS CERCA que la camara.
 *
 * Aca esta la razon de fondo por la que los cuatro niveles se seguian leyendo
 * como el mismo pasillo aunque cada uno ya tuviera su arquitectura, su paleta
 * y su camara descentrada.
 *
 * Las cuatro escenas estaban dibujadas ENTERAS entre la fuga y el borde del
 * cuadro. Todo lo que se veia estaba lejos y se iba haciendo chico hacia el
 * centro. Un encuadre asi solo puede leerse de una manera: como un tubo. Da
 * igual que el tubo tenga pileta, estanterias o caneria vista.
 *
 * Lo que hace que un cuadro se lea como un LUGAR y no como un tunel es que
 * haya algo mas cerca que la camara: un montante que corta el borde, una viga
 * que entra por arriba, el canto de un mueble. Pasan dos cosas de golpe:
 *
 * <ol>
 *   <li>Se rompe el marco. El recinto deja de estar contenido dentro de la
 *       pantalla y pasa a continuar fuera de ella, que es como funciona la
 *       vision real: uno nunca ve una habitacion entera de una vez.</li>
 *   <li>Aparece el paralaje. Si lo cercano se mueve mas que lo lejano, el ojo
 *       deduce profundidad de verdad y no profundidad dibujada.</li>
 * </ol>
 *
 * Cada planta pone lo suyo, y no son adornos: son el elemento que dice DESDE
 * DONDE se esta mirando. En la sala, el canto del mostrador -se mira desde
 * detras del mostrador-. En la nave, una columna cortada -se mira desde detras
 * de una columna-. En el servicio, los canos pasan POR ENCIMA de la camara -se
 * esta metido en el pasillo tecnico-. En el natatorio, el trampolin entra
 * desde arriba -se esta debajo del trampolin, al borde del agua-.
 */
public final class PrimerPlano {

    private PrimerPlano() {
    }

    /**
     * Balanceo lentisimo de la camara.
     *
     * No es viento ni temblor: es que nadie sostiene una camara perfectamente
     * quieta. Amplitud de pocos pixeles y periodo largo. Se nota si se mira
     * fijo diez minutos y no se nota si se mira diez segundos, que es
     * exactamente lo que se busca. Como solo afecta al primer plano y no al
     * fondo, produce paralaje: el recinto parece tener volumen.
     */
    public static float desvio(float tiempo, float amplitud, float velocidad) {
        return (float) Math.sin(tiempo * velocidad) * amplitud;
    }

    // ----------------------------------------------------------------------
    // Nivel 0 - La sala: el canto del mostrador
    // ----------------------------------------------------------------------

    /**
     * El mostrador de recepcion, cruzando el borde inferior.
     *
     * Se mira la sala desde detras del mostrador, que es lo que justifica la
     * altura y el descentrado de la camara. Para que se lea como un mueble y
     * no como una sombra en el piso necesita las tres cosas que tiene
     * cualquier mueble mirado de cerca: una tapa horizontal que recibe la luz
     * cenital, un frente vertical en sombra, y un filo entre las dos.
     */
    public static void sala(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        float balance = desvio(tiempo, 3.0F, 0.09F);
        int tapaY = (int) (m.alto() * 0.80F + balance);
        int x0 = (int) (m.ancho() * 0.30F + balance * 1.6F);

        int frente = Paleta.mezclar(nivel.paredBaja, 0xFF000000, 0.58F);
        int tapa = Paleta.mezclar(nivel.suelo, 0xFF000000, 0.20F);

        // Frente: cae del filo hasta el borde del cuadro y se oscurece abajo.
        grafico.fillGradient(x0, tapaY, m.ancho(), m.alto(),
                Paleta.iluminar(frente, 0.34F + 0.20F * luz),
                Paleta.iluminar(frente, 0.12F + 0.10F * luz));

        // Tapa: la banda horizontal clara. Es lo que dice "esto es un mueble".
        int espesor = Math.max(4, (int) (m.alto() * 0.030F));
        grafico.fillGradient(x0, tapaY - espesor, m.ancho(), tapaY,
                Paleta.iluminar(tapa, 0.62F + 0.34F * luz),
                Paleta.iluminar(tapa, 0.44F + 0.26F * luz));

        // Filo iluminado por los tubos del techo.
        grafico.fill(x0, tapaY - espesor, m.ancho(), tapaY - espesor + 2,
                Paleta.conAlfa(0xFFFFF3D8, 0.16F + 0.24F * luz));

        // Canto lateral: cierra el mueble y deja ver el piso detras.
        grafico.fill(x0, tapaY - espesor, x0 + 3, m.alto(),
                Paleta.conAlfa(Paleta.iluminar(frente, 0.20F + 0.14F * luz), 0.90F));

        // Una carpeta olvidada sobre la tapa, de canto.
        int cx = (int) (m.ancho() * 0.58F + balance * 1.6F);
        int cw = (int) (m.ancho() * 0.11F);
        grafico.fill(cx, tapaY - espesor - 6, cx + cw, tapaY - espesor,
                Paleta.conAlfa(Paleta.iluminar(nivel.paredAlta, 0.66F + 0.30F * luz), 0.88F));
        grafico.fill(cx, tapaY - espesor - 6, cx + cw, tapaY - espesor - 5,
                Paleta.conAlfa(0xFFFFF3D8, 0.20F * luz));
    }

    // ----------------------------------------------------------------------
    // Nivel 1 - La nave: una columna cortada por el borde
    // ----------------------------------------------------------------------

    /**
     * Una columna de hormigon cortada por el borde izquierdo.
     *
     * La nave es el sitio mas grande de los cuatro y el tamano solo se percibe
     * por comparacion: sin nada cerca, una nave de treinta metros y un pasillo
     * de tres se dibujan igual. Una columna que convence tiene que ser ANCHA
     * -la nave ya esta llena de pilares verticales a media distancia, y uno de
     * ancho parecido se confunde con ellos-, tiene que estar CORTADA por el
     * borde, y tiene que tener una cara iluminada y otra en sombra, porque es
     * un prisma y no una linea.
     */
    public static void nave(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        float balance = desvio(tiempo, 4.5F, 0.07F);
        int ancho = (int) (m.ancho() * 0.235F);
        int x0 = (int) (-ancho * 0.16F + balance);
        int x1 = x0 + ancho;
        // El quiebre esta a dos tercios y no en el medio: la columna no se ve
        // de frente, se ve de canto.
        int quiebre = (int) (x0 + ancho * 0.62F);

        // La columna cae dentro de la franja donde la vineta oscurece el
        // cuadro, asi que con los valores "realistas" del hormigon en sombra
        // desaparece. Se pinta clara a proposito: esta a un metro de la camara
        // y a un metro las luminarias del techo pegan de lleno.
        int hormigon = Paleta.mezclar(nivel.paredBaja, 0xFFFFFFFF, 0.22F);

        // Cara en sombra: media, no negra.
        grafico.fillGradient(x0, 0, quiebre, m.alto(),
                Paleta.iluminar(Paleta.mezclar(hormigon, 0xFF000000, 0.34F), 0.62F + 0.26F * luz),
                Paleta.iluminar(Paleta.mezclar(hormigon, 0xFF000000, 0.52F), 0.42F + 0.20F * luz));

        // Cara iluminada: la mas clara del cuadro entero.
        grafico.fillGradient(quiebre, 0, x1, m.alto(),
                Paleta.iluminar(Paleta.mezclar(hormigon, 0xFFFFFFFF, 0.10F), 0.86F + 0.14F * luz),
                Paleta.iluminar(hormigon, 0.60F + 0.24F * luz));

        // Arista viva entre las dos caras.
        grafico.fill(quiebre, 0, quiebre + 2, m.alto(),
                Paleta.conAlfa(0xFFD9E4EC, 0.10F + 0.16F * luz));

        // Sombra proyectada sobre lo que hay detras. Sin esto la columna
        // flota; con esto se apoya en el espacio.
        grafico.fill(x1, 0, x1 + Math.max(3, (int) (m.ancho() * 0.010F)), m.alto(),
                Paleta.conAlfa(0xFF000000, 0.34F));
        grafico.fill(x1 - 1, 0, x1, m.alto(), Paleta.conAlfa(0xFF000000, 0.50F));

        // Junta de encofrado: da escala y dice "esto es hormigon".
        int paso = Math.max(10, (int) (m.alto() * 0.17F));
        for (int y = paso / 2; y < m.alto(); y += paso) {
            grafico.fill(x0, y, x1, y + 1, Paleta.conAlfa(0xFF000000, 0.20F));
            grafico.fill(x0, y + 1, x1, y + 2,
                    Paleta.conAlfa(0xFFD9E4EC, 0.05F + 0.05F * luz));
        }

        // Numero de columna estarcido: ilegible, pero reconocible como marca.
        int cy = (int) (m.alto() * 0.30F);
        grafico.fill(quiebre + 6, cy, x1 - 5, cy + (int) (m.alto() * 0.09F),
                Paleta.conAlfa(Paleta.iluminar(nivel.junta, 0.60F + 0.34F * luz), 0.55F));

        // Humedad subiendo desde el pie: la nave esta abandonada.
        int hy = (int) (m.alto() * 0.72F);
        grafico.fillGradient(x0, hy, x1, m.alto(),
                Paleta.conAlfa(0xFF000000, 0.0F), Paleta.conAlfa(0xFF000000, 0.34F));
    }

    // ----------------------------------------------------------------------
    // Nivel 2 - El servicio: los canos por encima de la camara
    // ----------------------------------------------------------------------

    /**
     * Los canos que pasan por encima de la camara.
     *
     * En el pasillo tecnico la camara no mira el pasillo: esta DENTRO. Dos
     * canos cruzan el borde superior de lado a lado, muy cerca y casi en
     * silueta. Son los mismos canos que se ven alejarse al fondo, y esa
     * continuidad -lo cercano y lo lejano son la misma instalacion- es la que
     * mete al que mira dentro del recinto.
     */
    public static void servicio(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        float balance = desvio(tiempo, 2.0F, 0.11F);
        float[] alturas = {0.045F, 0.125F};
        float[] grosores = {0.052F, 0.034F};
        float[] tonos = {0.30F, 0.16F};

        for (int i = 0; i < alturas.length; i++) {
            int y0 = (int) (m.alto() * alturas[i] + balance * (1.0F + i * 0.4F));
            int y1 = y0 + (int) (m.alto() * grosores[i]);
            int cuerpo = Paleta.mezclar(nivel.junta, 0xFF000000, 0.55F + tonos[i] * 0.4F);

            grafico.fillGradient(0, y0, m.ancho(), y1,
                    Paleta.iluminar(cuerpo, 0.42F + 0.26F * luz),
                    Paleta.iluminar(cuerpo, 0.16F + 0.12F * luz));

            // Reflejo especular corrido: el cano es redondo, no una franja.
            grafico.fill(0, y0 + 1, m.ancho(), y0 + 2,
                    Paleta.conAlfa(0xFFE8E2CE, 0.06F + 0.12F * luz));

            // Abrazaderas cada tanto.
            int paso = (int) (m.ancho() * 0.27F);
            for (int x = (int) (m.ancho() * 0.08F); x < m.ancho(); x += paso) {
                grafico.fill(x, y0 - 2, x + 6, y1 + 2,
                        Paleta.conAlfa(Paleta.iluminar(cuerpo, 0.30F + 0.20F * luz), 0.95F));
            }
        }
    }

    // ----------------------------------------------------------------------
    // Nivel 3 - El natatorio: el trampolin
    // ----------------------------------------------------------------------

    /**
     * El trampolin, entrando desde el borde superior izquierdo.
     *
     * Es el elemento que convierte "un pasillo con agua" en "una pileta". Un
     * trampolin se reconoce por cuatro cosas: que entra desde fuera del cuadro,
     * que esta ARRIBA del agua y no del piso, que tiene el canto en sombra y la
     * cara superior clara, y que termina en el aire. Va en diagonal, no
     * centrado: la diagonal es la que rompe la caja.
     */
    public static void natatorio(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        float balance = desvio(tiempo, 2.6F, 0.06F);
        float xIni = -m.ancho() * 0.04F;
        float yIni = m.alto() * 0.10F + balance;
        float xFin = m.ancho() * 0.46F;
        float yFin = m.alto() * 0.36F + balance * 0.4F;

        int pasos = 20;
        float espesorIni = m.alto() * 0.055F;
        float espesorFin = m.alto() * 0.022F;
        float anchoPaso = (xFin - xIni) / pasos + 2.0F;

        for (int i = 0; i < pasos; i++) {
            float t = i / (float) (pasos - 1);
            float x = xIni + (xFin - xIni) * t;
            float y = yIni + (yFin - yIni) * t;
            float esp = espesorIni + (espesorFin - espesorIni) * t;

            // Cara superior: chapa clara que se apaga con la distancia.
            int cara = Paleta.mezclar(nivel.paredAlta, 0xFF000000, 0.10F + 0.30F * t);
            grafico.fill((int) x, (int) y, (int) (x + anchoPaso), (int) (y + esp * 0.42F),
                    Paleta.iluminar(cara, 0.70F + 0.26F * luz * (1.0F - t * 0.4F)));

            // Canto en sombra: es lo que le da espesor de plancha.
            int canto = Paleta.mezclar(nivel.paredBaja, 0xFF000000, 0.58F + 0.20F * t);
            grafico.fill((int) x, (int) (y + esp * 0.42F), (int) (x + anchoPaso), (int) (y + esp),
                    Paleta.iluminar(canto, 0.30F + 0.20F * luz));
        }

        // Punta: remate mas oscuro, colgando sobre el agua.
        grafico.fill((int) (xFin - 4), (int) yFin, (int) (xFin + 3), (int) (yFin + espesorFin),
                Paleta.conAlfa(Paleta.mezclar(nivel.paredBaja, 0xFF000000, 0.70F), 0.90F));

        // Baranda: dos tubos que arrancan del borde y mueren a media plancha.
        float[] alturasBaranda = {0.16F, 0.30F};
        int mitad = pasos / 2;
        for (float alt : alturasBaranda) {
            for (int i = 0; i < mitad; i++) {
                float t = i / (float) (mitad - 1);
                float x = xIni + (xFin - xIni) * t * 0.55F;
                float y = yIni + (yFin - yIni) * t * 0.55F
                        - m.alto() * alt * (1.0F - t * 0.35F);
                grafico.fill((int) x, (int) y, (int) (x + anchoPaso), (int) (y + 2),
                        Paleta.conAlfa(Paleta.iluminar(nivel.junta, 0.60F + 0.30F * luz), 0.75F));
            }
        }

        // Montantes verticales de la baranda.
        float[] montantes = {0.06F, 0.30F, 0.54F};
        for (float t : montantes) {
            float x = xIni + (xFin - xIni) * t * 0.55F;
            float y = yIni + (yFin - yIni) * t * 0.55F;
            grafico.fill((int) x, (int) (y - m.alto() * 0.30F), (int) (x + 2), (int) y,
                    Paleta.conAlfa(Paleta.iluminar(nivel.junta, 0.55F + 0.28F * luz), 0.70F));
        }

        // Sombra de la plancha sobre el agua: lo que la ancla al lugar.
        int sy = (int) (m.alto() * 0.74F);
        grafico.fill((int) xIni, sy, (int) (xFin * 0.92F), sy + (int) (m.alto() * 0.05F),
                Paleta.conAlfa(0xFF000000, 0.16F + 0.06F * luz));
    }

    // ----------------------------------------------------------------------
    // Nivel 4 - La sala: el borde de la mesa de banquete
    // ----------------------------------------------------------------------

    /**
     * El canto de una mesa larga de madera, cruzando el borde inferior.
     *
     * Se mira la sala desde la cabecera de la mesa -desde donde preside quien
     * convoca-. Como el mostrador de la sala administrativa, necesita tapa
     * horizontal iluminada por el fuego de arriba, frente vertical en sombra y
     * un filo entre ambos; pero la madera es calida y el reflejo del fuego sobre
     * la tapa titila apenas, cosa que una fotocopiadora no hace.
     */
    public static void cripta(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        float balance = desvio(tiempo, 2.4F, 0.08F);
        // El titileo del fuego sobre la madera: comun a todo el primer plano.
        float llama = 1.0F + 0.05F * (float) Math.sin(tiempo * 12.0F)
                + 0.03F * (float) Math.sin(tiempo * 7.1F + 1.0F);
        int tapaY = (int) (m.alto() * 0.82F + balance);
        int x0 = (int) (m.ancho() * 0.14F + balance * 1.4F);
        int x1 = (int) (m.ancho() * 0.92F + balance * 1.4F);

        int frente = Paleta.mezclar(nivel.paredBaja, 0xFF000000, 0.55F);
        int tapa = Paleta.mezclar(nivel.suelo, nivel.paredAlta, 0.30F);

        // Frente de la mesa: cae del filo al borde del cuadro, mas oscuro abajo.
        grafico.fillGradient(x0, tapaY, x1, m.alto(),
                Paleta.iluminar(frente, (0.30F + 0.20F * luz) * llama),
                Paleta.iluminar(frente, 0.10F + 0.08F * luz));

        // Tapa: la banda horizontal de madera, lamida por el fuego de arriba.
        int espesor = Math.max(5, (int) (m.alto() * 0.035F));
        grafico.fillGradient(x0, tapaY - espesor, x1, tapaY,
                Paleta.iluminar(tapa, Math.min(1.0F, (0.60F + 0.34F * luz) * llama)),
                Paleta.iluminar(tapa, 0.42F + 0.24F * luz));

        // Filo iluminado por el candil y las antorchas.
        grafico.fill(x0, tapaY - espesor, x1, tapaY - espesor + 2,
                Paleta.conAlfa(Paleta.iluminar(nivel.luz, Math.min(1.0F, luz * llama)), 0.20F + 0.24F * luz));

        // Vetas de la madera en la tapa: unas pocas lineas longitudinales.
        for (int k = 1; k <= 3; k++) {
            int vy = tapaY - espesor + k * espesor / 4;
            grafico.fill(x0, vy, x1, vy + 1, Paleta.conAlfa(0xFF000000, 0.10F));
        }

        // Un candelabro bajo sobre la mesa, de canto: un pie y dos velas que
        // titilan. Es lo que dice que en esta mesa se sienta alguien.
        int velaX = (int) (m.ancho() * 0.30F + balance * 1.4F);
        candelabroMesa(grafico, nivel, velaX, tapaY - espesor, m, luz, tiempo);

        // Una jarra de canto, mas a la derecha: silueta simple.
        int jx = (int) (m.ancho() * 0.66F + balance * 1.4F);
        int jw = (int) (m.ancho() * 0.05F);
        int jh = (int) (m.alto() * 0.07F);
        grafico.fill(jx, tapaY - espesor - jh, jx + jw, tapaY - espesor,
                Paleta.conAlfa(Paleta.iluminar(nivel.junta, 0.50F + 0.30F * luz), 0.90F));
        grafico.fill(jx + jw, tapaY - espesor - (int) (jh * 0.6F), jx + jw + (int) (jw * 0.3F),
                tapaY - espesor - (int) (jh * 0.25F),
                Paleta.conAlfa(Paleta.iluminar(nivel.junta, 0.50F + 0.30F * luz), 0.90F));
        // Brillo del fuego en el hombro de la jarra.
        grafico.fill(jx, tapaY - espesor - jh, jx + jw, tapaY - espesor - jh + 2,
                Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz * llama), 0.30F));
    }

    /** Un candelabro bajo sobre la mesa, visto de canto, con dos velas vivas. */
    private static void candelabroMesa(GuiGraphics grafico, Nivel nivel, int x, int base,
                                       Marco m, float luz, float tiempo) {
        int alto = (int) (m.alto() * 0.10F);
        int hierro = Paleta.iluminar(nivel.junta, 0.45F + 0.25F * luz);
        // Pie y brazo.
        grafico.fill(x - 1, base - alto, x + 2, base, Paleta.conAlfa(hierro, 0.92F));
        grafico.fill(x - (int) (m.ancho() * 0.03F), base - (int) (alto * 0.55F),
                x + (int) (m.ancho() * 0.03F), base - (int) (alto * 0.55F) + 2,
                Paleta.conAlfa(hierro, 0.92F));
        // Dos velas.
        for (int s = -1; s <= 1; s += 2) {
            int vx = x + s * (int) (m.ancho() * 0.03F);
            int vy = base - (int) (alto * 0.55F);
            float llama = 1.0F + 0.10F * (float) Math.sin(tiempo * 13.0F + s);
            // Derrame: chico y contenido, no un halo enorme.
            for (int k = 3; k >= 1; k--) {
                float t = k / 3.0F;
                float e = m.ancho() * 0.010F * (1.0F + t * 2.2F);
                grafico.fill((int) (vx - e), (int) (vy - e), (int) (vx + e), (int) (vy + e * 0.6F),
                        Paleta.conAlfa(nivel.luz, 0.07F * luz * llama * (1.0F - t * 0.5F)));
            }
            // Cuerpo de vela y nucleo.
            grafico.fill(vx - 1, vy - (int) (alto * 0.22F), vx + 1, vy,
                    Paleta.conAlfa(Paleta.iluminar(nivel.paredAlta, 0.7F * luz), 0.9F));
            grafico.fill(vx - 1, vy - (int) (alto * 0.30F), vx + 1, vy - (int) (alto * 0.22F),
                    Paleta.conAlfa(Paleta.iluminar(0xFFFFF3D8, Math.min(1.0F, luz * llama * 1.4F)), 0.95F));
        }
    }

    // ----------------------------------------------------------------------
    // Nivel 5 - La biblioteca: el borde de la mesa de lectura con un libro
    // ----------------------------------------------------------------------

    /**
     * El canto de una mesa de lectura, con un libro abierto y una lampara.
     *
     * Se mira la biblioteca desde un pupitre de la sala central. La tapa de
     * madera recibe la luz verde de la lampara; el libro abierto es dos paginas
     * claras en V, lo unico que dice que alguien estaba aca hace un momento.
     */
    public static void biblioteca(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        float balance = desvio(tiempo, 2.0F, 0.07F);
        int tapaY = (int) (m.alto() * 0.84F + balance);
        int x0 = (int) (m.ancho() * 0.10F + balance * 1.3F);
        int x1 = (int) (m.ancho() * 0.90F + balance * 1.3F);

        int frente = Paleta.mezclar(nivel.paredBaja, 0xFF000000, 0.55F);
        int tapa = Paleta.mezclar(nivel.suelo, nivel.junta, 0.30F);

        grafico.fillGradient(x0, tapaY, x1, m.alto(),
                Paleta.iluminar(frente, 0.28F + 0.18F * luz),
                Paleta.iluminar(frente, 0.10F + 0.08F * luz));
        int espesor = Math.max(5, (int) (m.alto() * 0.032F));
        grafico.fillGradient(x0, tapaY - espesor, x1, tapaY,
                Paleta.iluminar(tapa, 0.55F + 0.30F * luz),
                Paleta.iluminar(tapa, 0.40F + 0.22F * luz));
        grafico.fill(x0, tapaY - espesor, x1, tapaY - espesor + 2,
                Paleta.conAlfa(0xFF2E5A3A, 0.10F + 0.16F * luz));

        // El libro abierto, de canto: dos paginas claras en V y el lomo al medio.
        int lx = (int) (m.ancho() * 0.40F + balance * 1.3F);
        int lw = (int) (m.ancho() * 0.20F);
        int lh = Math.max(4, (int) (m.alto() * 0.03F));
        int pagina = Paleta.iluminar(Paleta.mezclar(nivel.paredAlta, 0xFFFFFFFF, 0.25F), 0.5F + 0.4F * luz);
        grafico.fill(lx, tapaY - espesor - lh, lx + lw / 2, tapaY - espesor, pagina);
        grafico.fill(lx + lw / 2, tapaY - espesor - lh, lx + lw, tapaY - espesor, pagina);
        // El lomo levantado del medio.
        grafico.fill(lx + lw / 2 - 1, tapaY - espesor - lh - 2, lx + lw / 2 + 1, tapaY - espesor,
                Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz), 0.8F));

        // Una lampara de mesa a la derecha, pantalla verde, tibia.
        int px = (int) (m.ancho() * 0.70F + balance * 1.3F);
        int py = tapaY - espesor;
        int ph = (int) (m.alto() * 0.12F);
        float titil = 0.9F + 0.1F * (float) Math.sin(tiempo * 5.0F);
        for (int k = 4; k >= 1; k--) {
            float t = k / 4.0F;
            float e = m.ancho() * 0.02F * (1.0F + t * 2.5F);
            grafico.fill((int) (px - e), (int) (py - ph * 0.6F - e * 0.5F), (int) (px + e), py,
                    Paleta.conAlfa(nivel.luz, 0.06F * luz * titil * (1.0F - t * 0.5F)));
        }
        grafico.fill(px - 1, py - ph, px + 1, py, Paleta.conAlfa(Paleta.iluminar(nivel.junta, luz), 0.85F));
        int verde = Paleta.mezclar(nivel.luz, 0xFF2E5A3A, 0.55F);
        grafico.fill(px - (int) (m.ancho() * 0.03F), py - ph - 3,
                px + (int) (m.ancho() * 0.03F), py - ph + 4,
                Paleta.iluminar(verde, Math.min(1.0F, luz * titil * 1.1F)));
    }

    // ----------------------------------------------------------------------
    // Nivel 6 - El invernadero: hojas colgando desde el borde superior
    // ----------------------------------------------------------------------

    /**
     * Frondas grandes que cuelgan del entramado y entran por las esquinas de
     * arriba, muy cerca de la camara. Es la vegetacion que se comio el techo, y
     * lo que dice que se mira el invernadero desde debajo de una planta trepadora.
     * Se mecen lentisimo con la corriente.
     */
    public static void invernadero(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        // Una fronda desde la esquina superior izquierda y otra, mas chica,
        // desde la derecha. En sombra: estan a contraluz del vidrio.
        frondaColgante(grafico, m, tiempo, -m.ancho() * 0.02F, 0.0F, m.ancho() * 0.34F,
                m.alto() * 0.40F, 0xFF223C18, luz, 1.0F);
        frondaColgante(grafico, m, tiempo + 3.0F, m.ancho() * 1.02F, 0.0F, m.ancho() * 0.72F,
                m.alto() * 0.30F, 0xFF1C3414, luz, -1.0F);

        // Una maceta colgante con enredadera cayendo, mas al centro-derecha.
        int cx = (int) (m.ancho() * 0.80F);
        int cy = 0;
        int largo = (int) (m.alto() * 0.34F);
        for (int i = 0; i < largo; i += 4) {
            float t = i / (float) largo;
            float sway = (float) Math.sin(tiempo * 0.5F + t * 3.0F) * m.ancho() * 0.01F;
            int x = (int) (cx + sway);
            grafico.fill(x - 1, cy + i, x + 2, cy + i + 3,
                    Paleta.conAlfa(Paleta.iluminar(0xFF2E4A1E, 0.4F + 0.3F * luz), 0.85F));
            if (i % 16 == 0) {
                grafico.fill(x - 4, cy + i, x + 5, cy + i + 4,
                        Paleta.conAlfa(Paleta.iluminar(0xFF3E5A28, 0.4F + 0.3F * luz), 0.75F));
            }
        }
    }

    /** Una hoja/fronda grande en abanico, anclada a una esquina de arriba. */
    private static void frondaColgante(GuiGraphics grafico, Marco m, float tiempo,
                                       float bx, float by, float tx, float ty,
                                       int color, float luz, float dir) {
        int nervios = 9;
        float mece = (float) Math.sin(tiempo * 0.35F) * m.ancho() * 0.012F;
        for (int k = 0; k < nervios; k++) {
            float a = k / (float) (nervios - 1);
            // Cada nervio abre en abanico desde el anclaje.
            float ex = bx + (tx - bx) * (0.6F + 0.6F * a) + dir * (a - 0.5F) * m.ancho() * 0.10F + mece;
            float ey = by + (ty - by) * (0.5F + 0.9F * a);
            int pasos = 10;
            for (int p = 0; p <= pasos; p++) {
                float t = p / (float) pasos;
                int x = (int) (bx + (ex - bx) * t + mece * t);
                int y = (int) (by + (ey - by) * t);
                int ancho = Math.max(2, (int) (m.ancho() * 0.014F * (1.0F - t * 0.5F)));
                grafico.fill(x - ancho, y - 1, x + ancho, y + 2,
                        Paleta.conAlfa(Paleta.iluminar(color, 0.35F + 0.30F * luz), 0.88F));
            }
        }
    }

    // ----------------------------------------------------------------------
    // Nivel 7 - Las catacumbas: el arco de piedra que cruza la camara
    // ----------------------------------------------------------------------

    /**
     * Un arco de piedra en primer plano: se pasa por debajo de un dintel.
     *
     * Dos jambas gruesas a los lados y un arco que las une por arriba, todo muy
     * cerca y en sombra, enmarcando la escena. Es lo que mete al que mira DENTRO
     * del tunel, y da la sensacion de techo bajo. En una de las jambas, una vela
     * consumida.
     */
    public static void catacumba(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        int piedra = Paleta.mezclar(nivel.paredBaja, 0xFF000000, 0.45F);
        int piedraClara = Paleta.iluminar(Paleta.mezclar(nivel.paredAlta, 0xFF000000, 0.30F), 0.5F + 0.3F * luz);
        int w = m.ancho();
        int h = m.alto();
        int jamba = (int) (w * 0.14F);

        // Jamba izquierda y derecha (prismas verticales que cruzan todo el alto).
        grafico.fillGradient(0, 0, jamba, h,
                Paleta.iluminar(piedra, 0.5F + 0.2F * luz), Paleta.iluminar(piedra, 0.25F + 0.12F * luz));
        grafico.fillGradient(w - jamba, 0, w, h,
                Paleta.iluminar(piedra, 0.5F + 0.2F * luz), Paleta.iluminar(piedra, 0.25F + 0.12F * luz));
        // Filo interior iluminado de cada jamba.
        grafico.fill(jamba, 0, jamba + 2, h, Paleta.conAlfa(piedraClara, 0.5F));
        grafico.fill(w - jamba - 2, 0, w - jamba, h, Paleta.conAlfa(piedraClara, 0.5F));

        // El arco superior: baja desde las dos jambas y se curva por el techo.
        int arcoAlto = (int) (h * 0.22F);
        int cx = w / 2;
        for (int x = jamba; x <= w - jamba; x += Trazo.PASO) {
            float t = (x - jamba) / (float) Math.max(1, (w - 2 * jamba));
            // Curva de arco: seno, mas bajo en los extremos, sube al centro... no:
            // el dintel cuelga MAS en el centro no, cuelga en los bordes. Un arco
            // de medio punto: mas alto (menos cuelgue) en el centro.
            float caida = (float) Math.sin(Math.PI * t);
            int borde = (int) (arcoAlto * (1.0F - 0.6F * caida));
            grafico.fillGradient(x, 0, x + Trazo.PASO, borde,
                    Paleta.iluminar(piedra, 0.45F + 0.2F * luz), Paleta.iluminar(piedra, 0.20F + 0.1F * luz));
            // El canto inferior del arco, iluminado.
            grafico.fill(x, borde, x + Trazo.PASO, borde + 2, Paleta.conAlfa(piedraClara, 0.45F));
        }

        // Una vela consumida sobre la jamba derecha, cerca de la camara.
        int vx = w - jamba / 2;
        int vy = (int) (h * 0.55F);
        float titil = 0.85F + 0.15F * (float) Math.sin(tiempo * 6.5F);
        for (int k = 4; k >= 1; k--) {
            float t = k / 4.0F;
            float e = w * 0.03F * (1.0F + t * 2.2F);
            grafico.fill((int) (vx - e), (int) (vy - e), (int) (vx + e), (int) (vy + e * 0.6F),
                    Paleta.conAlfa(nivel.luz, 0.08F * luz * titil * (1.0F - t * 0.5F)));
        }
        grafico.fill(vx - 2, vy - (int) (h * 0.05F), vx + 2, vy,
                Paleta.conAlfa(Paleta.iluminar(nivel.paredAlta, 0.6F * luz), 0.9F));
        grafico.fill(vx - 1, vy - (int) (h * 0.065F), vx + 1, vy - (int) (h * 0.05F),
                Paleta.conAlfa(Paleta.iluminar(0xFFFFE0A0, Math.min(1.0F, luz * titil * 1.4F)), 0.95F));
    }

    // ----------------------------------------------------------------------
    // Nivel 8 - La cisterna: la baranda de la pasarela al ras del agua
    // ----------------------------------------------------------------------

    /**
     * La baranda de hierro de la pasarela, cruzando el borde inferior.
     *
     * Se mira la cisterna desde una pasarela metalica al ras del agua: una
     * baranda horizontal con montantes verticales, en silueta contra el agua
     * apenas luminosa, muy cerca. Es lo que pone al que mira sobre el agua y no
     * dentro de ella. El pasamanos capta un brillo de los focos.
     */
    public static void cisterna(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        float balance = desvio(tiempo, 1.6F, 0.09F);
        int w = m.ancho();
        int h = m.alto();
        int hierro = Paleta.mezclar(nivel.junta, 0xFF000000, 0.35F);

        // El pasamanos superior.
        int pasY = (int) (h * 0.80F + balance);
        int grosor = Math.max(3, (int) (h * 0.018F));
        grafico.fillGradient(0, pasY, w, pasY + grosor,
                Paleta.iluminar(hierro, 0.40F + 0.20F * luz), Paleta.iluminar(hierro, 0.20F + 0.10F * luz));
        // Brillo de los focos en el canto de arriba del pasamanos.
        grafico.fill(0, pasY, w, pasY + 1, Paleta.conAlfa(Paleta.iluminar(nivel.luz, luz), 0.16F));

        // El larguero inferior.
        int bajoY = pasY + (int) (h * 0.10F);
        grafico.fill(0, bajoY, w, bajoY + Math.max(2, grosor / 2), Paleta.iluminar(hierro, 0.28F + 0.14F * luz));

        // Montantes verticales cada tanto, desde el pasamanos hasta el borde.
        int paso = Math.max(24, (int) (w * 0.11F));
        for (int x = (int) (paso * 0.5F + balance * 2.0F); x < w; x += paso) {
            grafico.fillGradient(x, pasY, x + Math.max(2, grosor / 2), h,
                    Paleta.iluminar(hierro, 0.34F + 0.16F * luz), Paleta.iluminar(hierro, 0.12F + 0.08F * luz));
        }

        // El frente sombrio de la pasarela por debajo del larguero.
        grafico.fillGradient(0, bajoY + grosor / 2, w, h,
                Paleta.conAlfa(0xFF000000, 0.30F), Paleta.conAlfa(0xFF000000, 0.62F));

        // Un farol de mano posado en la baranda, a la izquierda, con su reflejo
        // temblando en el agua justo debajo.
        int fx = (int) (w * 0.24F + balance * 2.0F);
        int fy = pasY;
        int fh = (int) (h * 0.09F);
        float titil = 0.85F + 0.15F * (float) Math.sin(tiempo * 6.0F);
        for (int k = 4; k >= 1; k--) {
            float t = k / 4.0F;
            float e = w * 0.03F * (1.0F + t * 2.4F);
            grafico.fill((int) (fx - e), (int) (fy - fh - e * 0.5F), (int) (fx + e), fy,
                    Paleta.conAlfa(nivel.luz, 0.07F * luz * titil * (1.0F - t * 0.5F)));
        }
        grafico.fill(fx - 3, fy - fh, fx + 3, fy, Paleta.conAlfa(Paleta.iluminar(hierro, luz), 0.92F));
        grafico.fill(fx - 2, fy - fh + 2, fx + 2, fy - 2,
                Paleta.conAlfa(Paleta.iluminar(0xFFFFE0A0, Math.min(1.0F, luz * titil * 1.3F)), 0.9F));
    }

    // ----------------------------------------------------------------------
    // Nivel 9 - El salon del trono: un tambor de columna caida en el suelo
    // ----------------------------------------------------------------------

    /**
     * Un tambor de columna derribada, atravesado en el primer plano.
     *
     * Un cilindro de piedra enorme, caido de lado, cruzando el borde inferior en
     * diagonal, muy cerca. Es lo que dice que el salon esta en ruinas y pone al
     * que mira detras de un escombro, agachado. La cara de arriba recibe la luz
     * cenital; el resto, sombra. Al lado, cascotes menores.
     */
    public static void trono(GuiGraphics grafico, Marco m, Nivel nivel, float luz, float tiempo) {
        int w = m.ancho();
        int h = m.alto();
        int piedra = Paleta.mezclar(nivel.paredBaja, 0xFF000000, 0.35F);
        int piedraLuz = Paleta.iluminar(Paleta.mezclar(nivel.paredAlta, 0xFF000000, 0.10F), 0.55F + 0.30F * luz);
        int piedraSombra = Paleta.iluminar(Paleta.mezclar(piedra, 0xFF000000, 0.4F), 0.30F + 0.15F * luz);

        // El tambor: una banda gruesa en diagonal suave por el borde inferior.
        int yIzq = (int) (h * 0.74F);
        int yDer = (int) (h * 0.84F);
        int alto = (int) (h * 0.28F);
        for (int x = 0; x < w; x += Trazo.PASO) {
            float t = x / (float) w;
            int yTop = (int) (yIzq + (yDer - yIzq) * t);
            // Cara superior iluminada (una franja fina arriba).
            grafico.fill(x, yTop, x + Trazo.PASO, yTop + Math.max(2, alto / 8),
                    piedraLuz);
            // Cuerpo del tambor.
            grafico.fillGradient(x, yTop + Math.max(2, alto / 8), x + Trazo.PASO, h,
                    Paleta.iluminar(piedra, 0.40F + 0.18F * luz), piedraSombra);
        }
        // Las molduras circulares de los extremos del tambor (anillos concentricos
        // sugeridos con lineas horizontales cerca de los bordes).
        for (int r = 1; r <= 3; r++) {
            int yr = yIzq + r * alto / 10;
            grafico.fill(0, yr, (int) (w * 0.10F), yr + 1, Paleta.conAlfa(piedraSombra, 0.7F));
            int yrd = yDer + r * alto / 10;
            grafico.fill((int) (w * 0.90F), yrd, w, yrd + 1, Paleta.conAlfa(piedraSombra, 0.7F));
        }
        // El filo iluminado del canto superior, a lo largo.
        for (int x = 0; x < w; x += Trazo.PASO) {
            float t = x / (float) w;
            int yTop = (int) (yIzq + (yDer - yIzq) * t);
            grafico.fill(x, yTop, x + Trazo.PASO, yTop + 1,
                    Paleta.conAlfa(Paleta.iluminar(0xFFFFF0C0, luz), 0.14F));
        }

        // Unos cascotes sueltos delante, silueta.
        for (int i = 0; i < 4; i++) {
            int cx = (int) (w * (0.20F + i * 0.22F));
            int cw = (int) (w * (0.04F + Trazo.pseudo(i * 9) * 0.05F));
            int cy = (int) (h * 0.72F) - (int) (Trazo.pseudo(i * 9 + 1) * h * 0.04F);
            int ch = (int) (h * 0.06F);
            grafico.fill(cx, cy, cx + cw, cy + ch,
                    Paleta.iluminar(piedra, 0.32F + 0.16F * luz));
            grafico.fill(cx, cy, cx + cw, cy + 1, Paleta.conAlfa(piedraLuz, 0.5F));
        }
    }
}

