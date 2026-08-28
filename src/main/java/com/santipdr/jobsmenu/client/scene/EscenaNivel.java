package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Planta;
import com.santipdr.jobsmenu.client.scene.planta.Trazo;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RelojAparicion;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;

/**
 * El recinto del nivel, dibujado detras del aviso.
 *
 * Esta clase ya no dibuja paredes. Se ocupa de todo lo que es comun al
 * catalogo -cuanta luz hay, donde esta el punto de fuga, que polvo flota,
 * como se cierran los bordes- y le pasa el encuadre a la {@link Planta} del
 * nivel, que es la que sabe que recinto concreto se esta mostrando.
 *
 * La division importa. Antes habia una sola geometria parametrizada y los
 * niveles eran esa misma geometria con otros colores: cambiar de nivel no
 * cambiaba de lugar. Ahora el catalogo de recintos crece agregando una clase,
 * no agregando banderas a esta.
 *
 * ENCUADRE
 *
 * Todas las aristas del recinto son rectas que pasan por la fuga, asi que en
 * pantalla todo se reduce a dos variables, dx y dy, que valen 1 sobre la pared
 * del fondo y crecen hacia la camara. La cuenta esta en {@link Marco}; las
 * primitivas comunes, en {@link Trazo}.
 *
 * Cada nivel trae su propia camara y no la comparte con nadie: fuga propia y
 * cuatro semiejes independientes -izquierda, derecha, arriba y abajo-. Esa es
 * la pieza que faltaba. Mientras el marco tuvo un solo semiancho y un solo
 * semialto, las dos paredes laterales estaban obligadas a converger igual y
 * los niveles salian siendo el mismo tunel simetrico por mas plantas distintas
 * que se les dibujaran encima.
 *
 * Su espejo en Python es tools/vista_previa.py. Si se toca una, se toca la otra.
 */
public final class EscenaNivel {

    private EscenaNivel() {
    }

    /** Motas de polvo suspendidas. */
    private static final int MOTAS = 64;

    // ----------------------------------------------------------------------
    // Entrada
    // ----------------------------------------------------------------------

    /** Dibuja el recinto completo, del fondo hacia la camara. */
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

        // Cuando hay algo al fondo, el recinto entero baja un punto. Es tan
        // poco que no se ve como un efecto: se siente como que la luz cede.
        if (movimiento) {
            luz *= Presencia.sombra();
        }
        luz = Trazo.limitar(luz, 0.0F, 1.0F);

        float fx = ancho * nivel.fugaX;
        float fy = alto * nivel.fugaY;

        // Respiracion de camara: la fuga deriva unos pocos pixeles en un vaiven
        // lentisimo, con la horizontal y la vertical a periodos distintos para
        // que el recorrido no se repita a ojo. No es un temblor -eso marearia-,
        // es que el pasillo nunca esta del todo quieto, como si el que mira
        // respirara. Se apaga con movimiento reducido o la escena quieta.
        if (movimiento) {
            fx += (float) Math.sin(tiempo * 0.13F) * ancho * 0.006F;
            fy += (float) Math.sin(tiempo * 0.087F + 1.3F) * alto * 0.005F;
        }

        Marco marco = new Marco(ancho, alto, fx, fy,
                ancho * nivel.semiIzq, ancho * nivel.semiDer,
                ancho * nivel.semiAlto, ancho * nivel.semiBajo);

        Planta planta = nivel.planta;
        planta.dibujar(grafico, marco, nivel, luz, tiempo);
        // Lo que esta mas cerca que la camara va despues del recinto: tapa lo
        // lejano y le da al cuadro la profundidad que un tubo no tiene.
        planta.primerPlano(grafico, marco, nivel, luz, tiempo);

        if (movimiento) {
            // Los eventos globales van antes de Presencia para que una aparicion
            // importante nunca quede escondida detras de un brillo o una sombra
            // ambiental de baja prioridad.
            EventosAmbientales.dibujar(grafico, ancho, alto, nivel, luz);
            Presencia.dibujar(grafico, nivel, marco, luz, planta.pisoPresencia());
            motas(grafico, ancho, alto, tiempo, luz);
        }
        vineta(grafico, ancho, alto, penumbra);
    }

    // ----------------------------------------------------------------------
    // Lo que flota por encima de cualquier recinto
    // ----------------------------------------------------------------------

    /** Polvo suspendido, subiendo muy despacio. */
    private static void motas(GuiGraphics grafico, int ancho, int alto, float tiempo, float luz) {
        for (int i = 0; i < MOTAS; i++) {
            float baseX = Trazo.pseudo(i * 7);
            float baseY = Trazo.pseudo(i * 7 + 1);
            float velocidad = 0.10F + Trazo.pseudo(i * 7 + 2) * 0.30F;
            float deriva = (float) Math.sin(tiempo * (0.25F + Trazo.pseudo(i * 7 + 3) * 0.4F) + i) * 0.012F;

            float y = (baseY + tiempo * velocidad * 0.045F) % 1.0F;
            float x = (baseX + deriva + 1.0F) % 1.0F;
            int px = (int) (x * ancho);
            int py = (int) (y * alto);
            int tam = Trazo.pseudo(i * 7 + 4) < 0.75F ? 1 : 2;
            float a = (0.10F + Trazo.pseudo(i * 7 + 5) * 0.22F) * luz;
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
        return Trazo.limitar(v, 0.45F, 1.0F);
    }
}
