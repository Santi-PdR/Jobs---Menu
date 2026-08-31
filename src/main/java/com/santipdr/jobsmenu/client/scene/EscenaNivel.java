package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Planta;
import com.santipdr.jobsmenu.client.scene.planta.Trazo;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RelojAparicion;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;

/** Render comun del recinto y sus capas atmosfericas. */
public final class EscenaNivel {

    private EscenaNivel() {
    }

    /** Menos polvo generico; ahora pesa mas la identidad de cada recinto. */
    private static final int MOTAS = 52;

    public static void dibujar(GuiGraphics grafico, int ancho, int alto) {
        dibujar(grafico, ancho, alto, RotacionNiveles.capturar());
    }

    /**
     * Dibuja con un estado capturado al principio del frame.
     *
     * El overload publico conserva a los callers existentes; la pantalla
     * principal usa este camino para que la planta, la luz y la transicion no
     * puedan pertenecer a dos instantes distintos.
     */
    public static void dibujar(GuiGraphics grafico, int ancho, int alto,
                               RotacionNiveles.Estado estado) {
        Nivel nivel = estado.nivel();

        boolean viva = ConfigTurno.escenaViva();
        boolean destellos = viva && !ConfigTurno.destellosReducidos()
                && !estado.enSuspension();
        boolean movimiento = viva && !ConfigTurno.movimientoReducido();
        // Bajo consumo no congela el recinto (eso es movimiento_reducido):
        // quita las capas de aire y la respiracion, que son las que mas
        // rellenan por fotograma en pantallas pequenas o integradas.
        boolean bajoConsumo = viva && ConfigTurno.bajoConsumo();
        boolean respiracion = movimiento && ConfigTurno.respiracionCamara()
                && !bajoConsumo;
        boolean atmosferaMovimiento = movimiento && !estado.enSuspension();

        // Con el movimiento reducido el reloj se congela a proposito: las
        // plantas, los materiales, el tratamiento y la direccion de arte
        // reciben un instante fijo, asi el fuego, el agua, las telas y los
        // haces se quedan quietos de verdad. La luz sigue viva porque es otra
        // opcion (destellos_reducidos). Antes solo se apagaban el polvo, la
        // presencia y los eventos, y el resto del recinto seguia animandose.
        float tiempo = movimiento ? (estado.ahora() % 600_000L) / 1000.0F : 3.0F;
        long restanteRonda = RelojAparicion.restanteMs(estado.ahora());
        float penumbra = RelojAparicion.penumbra(restanteRonda);

        float luz = brilloFluorescente(tiempo, destellos)
                * (1.0F - 0.55F * penumbra)
                * estado.luz();

        if (movimiento) {
            luz *= Presencia.sombra(estado.ahora());
        }
        luz = Trazo.limitar(luz, 0.0F, 1.0F);

        float fx = ancho * nivel.fugaX;
        float fy = alto * nivel.fugaY;

        if (respiracion) {
            fx += (float) Math.sin(tiempo * 0.13F) * ancho * 0.0045F;
            fy += (float) Math.sin(tiempo * 0.087F + 1.3F) * alto * 0.0038F;
        }

        Marco marco = new Marco(ancho, alto, fx, fy,
                ancho * nivel.semiIzq, ancho * nivel.semiDer,
                ancho * nivel.semiAlto, ancho * nivel.semiBajo);

        Planta planta = nivel.planta;
        planta.dibujar(grafico, marco, nivel, luz, tiempo);

        // El detalle de material se pega a la arquitectura base antes de las
        // capas de luz: asi una grieta o un remache recibe la misma atmosfera
        // que el resto y no parece un sticker encima de la escena.
        MaterialesEscena.dibujar(grafico, ancho, alto, nivel, luz, tiempo, movimiento);

        // TratamientoEscena trabaja materiales/profundidad global y
        // DireccionArte agrega el lenguaje propio de cada recinto a partir de
        // referencias visuales. Los fondos 10-17 tambien reciben esta capa
        // global, pero conservan su propio movimiento en PlantaImagen.
        TratamientoEscena.dibujar(grafico, ancho, alto, nivel, luz, tiempo, movimiento);
        DireccionArte.dibujar(grafico, ancho, alto, nivel, luz, tiempo);

        planta.primerPlano(grafico, marco, nivel, luz, tiempo);

        // La Suspension apaga tambien los efectos que se mueven por el aire:
        // no hay motas, presencia ni eventos visuales durante el silencio. La
        // planta conserva su animacion normal si el usuario no pidio reducir
        // movimiento; la luz es la que cuenta la historia del corte.
        if (atmosferaMovimiento) {
            // Los eventos tienen entrada/salida por luminancia; se omiten con
            // destellos reducidos en vez de introducir un flash accidental.
            if (!ConfigTurno.destellosReducidos()) {
                EventosAmbientales.dibujar(grafico, ancho, alto, nivel, luz, estado.ahora());
            }
            if (!bajoConsumo) {
                // La figura y el polvo son las dos capas de aire mas caras:
                // en bajo consumo se saltan, y con ellas el rebote de la
                // sombra de la figura sobre el suelo. El recinto queda igual,
                // solo mas despejado y mas rapido.
                Presencia.dibujar(grafico, nivel, marco, luz, planta.pisoPresencia(), estado.ahora());
                int cantidadMotas = ancho * alto < 300_000 ? 24 : MOTAS;
                motas(grafico, ancho, alto, tiempo, luz, nivel, cantidadMotas);
            }
        }

        // Acabado de camara/instalacion comun a los 18 niveles. Esta capa se
        // dibuja despues del aire para integrar todo el recinto, pero antes de
        // la vineta final para no reducir la legibilidad de los bordes.
        PulidoEscena.dibujar(grafico, ancho, alto, nivel, luz, tiempo, estado,
                movimiento, bajoConsumo);
        vineta(grafico, ancho, alto, penumbra);
    }

    private static void motas(GuiGraphics grafico, int ancho, int alto,
                              float tiempo, float luz, Nivel nivel, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            float baseX = Trazo.pseudo(i * 7);
            float baseY = Trazo.pseudo(i * 7 + 1);
            float velocidad = 0.08F + Trazo.pseudo(i * 7 + 2) * 0.25F;
            float deriva = (float) Math.sin(
                    tiempo * (0.20F + Trazo.pseudo(i * 7 + 3) * 0.35F) + i) * 0.010F;

            float y = (baseY + tiempo * velocidad * 0.040F) % 1.0F;
            float x = (baseX + deriva + 1.0F) % 1.0F;
            int px = (int) (x * ancho);
            int py = (int) (y * alto);
            int tam = Trazo.pseudo(i * 7 + 4) < 0.82F ? 1 : 2;
            float a = (0.07F + Trazo.pseudo(i * 7 + 5) * 0.17F) * luz;
            int color = (i % 4 == 0) ? nivel.luz : Paleta.FLUOR;
            grafico.fill(px, py, px + tam, py + tam, Paleta.conAlfa(color, a));
        }
    }

    private static void vineta(GuiGraphics grafico, int ancho, int alto, float penumbra) {
        int franja = Math.max(8, ancho / 6);
        float intensidad = 0.36F + 0.43F * penumbra;
        final int paso = 3;

        for (int x = 0; x < franja; x += paso) {
            float t = 1.0F - x / (float) franja;
            int color = Paleta.conAlfa(Paleta.VANO, intensidad * t * t);
            grafico.fill(x, 0, Math.min(franja, x + paso), alto, color);
            grafico.fill(Math.max(0, ancho - x - paso), 0, ancho - x, alto, color);
        }

        int franjaV = Math.max(6, alto / 7);
        for (int y = 0; y < franjaV; y += paso) {
            float t = 1.0F - y / (float) franjaV;
            int color = Paleta.conAlfa(Paleta.VANO, intensidad * 0.72F * t * t);
            grafico.fill(0, y, ancho, Math.min(franjaV, y + paso), color);
            grafico.fill(0, Math.max(0, alto - y - paso), ancho, alto - y, color);
        }
    }

    public static float brilloFluorescente(float tiempo, boolean destellos) {
        if (!destellos) {
            return 0.90F;
        }
        float v = 0.90F
                + 0.030F * (float) Math.sin(tiempo * 1.7F)
                + 0.015F * (float) Math.sin(tiempo * 5.9F + 1.3F)
                + 0.008F * (float) Math.sin(tiempo * 12.7F + 0.4F);
        if (Math.floorMod((long) (tiempo * 3.0F), 97L) == 0L) {
            v *= 0.68F;
        }
        return Trazo.limitar(v, 0.48F, 1.0F);
    }
}
