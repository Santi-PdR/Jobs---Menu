package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Planta;
import com.santipdr.jobsmenu.client.scene.planta.PlantaImagen;
import com.santipdr.jobsmenu.client.scene.planta.Trazo;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RelojAparicion;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;

/** Render comun del recinto y sus capas atmosfericas. */
public final class EscenaNivel {

    private EscenaNivel() {
    }

    private static final int MOTAS = 52;

    public static void dibujar(GuiGraphics grafico, int ancho, int alto) {
        dibujar(grafico, ancho, alto, RotacionNiveles.capturar());
    }

    /** Dibuja con un estado capturado al principio del frame. */
    public static void dibujar(GuiGraphics grafico, int ancho, int alto,
                               RotacionNiveles.Estado estado) {
        Nivel nivel = estado.nivel();
        Planta planta = nivel.planta;
        boolean fondoImagen = planta instanceof PlantaImagen;

        boolean viva = ConfigTurno.escenaViva();
        boolean destellos = viva && !fondoImagen && !ConfigTurno.destellosReducidos()
                && !estado.enSuspension();
        // Los PNG 10-17 son una excepcion deliberada desde 0.13.0: se mantienen
        // estaticos aunque el resto de recintos siga usando escena viva.
        boolean movimiento = viva && !fondoImagen && !ConfigTurno.movimientoReducido();
        boolean bajoConsumo = viva && ConfigTurno.bajoConsumo();
        boolean respiracion = movimiento && ConfigTurno.respiracionCamara()
                && !bajoConsumo;
        boolean atmosferaMovimiento = movimiento && !estado.enSuspension();

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

        planta.dibujar(grafico, marco, nivel, luz, tiempo);

        // Las capas siguientes existen para dar vida a los recintos procedurales.
        // Sobre un PNG del usuario alterarian o animarian la imagen, asi que no se
        // aplican a los niveles 10-17.
        if (!fondoImagen) {
            MaterialesEscena.dibujar(grafico, ancho, alto, nivel, luz, tiempo, movimiento);
            TratamientoEscena.dibujar(grafico, ancho, alto, nivel, luz, tiempo, movimiento);
            DireccionArte.dibujar(grafico, ancho, alto, nivel, luz, tiempo);

            planta.primerPlano(grafico, marco, nivel, luz, tiempo);

            if (atmosferaMovimiento) {
                if (!ConfigTurno.destellosReducidos()) {
                    EventosAmbientales.dibujar(grafico, ancho, alto, nivel, luz, estado.ahora());
                }
                if (!bajoConsumo) {
                    Presencia.dibujar(grafico, nivel, marco, luz, planta.pisoPresencia(), estado.ahora());
                    int cantidadMotas = ancho * alto < 300_000 ? 24 : MOTAS;
                    motas(grafico, ancho, alto, tiempo, luz, nivel, cantidadMotas);
                }
            }

            PulidoEscena.dibujar(grafico, ancho, alto, nivel, luz, tiempo, estado,
                    movimiento, bajoConsumo);
        }

        // Vignette y apagones pertenecen a la composicion/transicion del menu,
        // no a una animacion del PNG en si.
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
