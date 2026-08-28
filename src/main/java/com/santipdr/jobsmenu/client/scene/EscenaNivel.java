package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.scene.planta.Planta;
import com.santipdr.jobsmenu.client.scene.planta.Trazo;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RelojAparicion;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;

/** Recinto vivo del nivel. La geometria concreta pertenece a cada Planta. */
public final class EscenaNivel {

    private EscenaNivel() {
    }

    /* 52 motas conservan profundidad sin pagar 70 fills por cuadro. */
    private static final int MOTAS = 52;

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

        if (movimiento) {
            luz *= Presencia.sombra();
        }
        luz = Trazo.limitar(luz, 0.0F, 1.0F);

        float fx = ancho * nivel.fugaX;
        float fy = alto * nivel.fugaY;
        Marco marco = new Marco(ancho, alto, fx, fy,
                ancho * nivel.semiIzq, ancho * nivel.semiDer,
                ancho * nivel.semiAlto, ancho * nivel.semiBajo);

        Planta planta = nivel.planta;
        planta.dibujar(grafico, marco, nivel, luz, tiempo);
        planta.primerPlano(grafico, marco, nivel, luz, tiempo);

        if (movimiento) {
            EventosAmbientales.dibujar(grafico, nivel, marco, luz);
            Presencia.dibujar(grafico, nivel, marco, luz, planta.pisoPresencia());
            motas(grafico, ancho, alto, tiempo, luz);
        }
        vineta(grafico, ancho, alto, penumbra);
    }

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
            int tam = Trazo.pseudo(i * 7 + 4) < 0.80F ? 1 : 2;
            float a = (0.10F + Trazo.pseudo(i * 7 + 5) * 0.22F) * luz;
            grafico.fill(px, py, px + tam, py + tam, Paleta.conAlfa(Paleta.FLUOR, a));
        }
    }

    private static void vineta(GuiGraphics grafico, int ancho, int alto, float penumbra) {
        int franja = Math.max(8, ancho / 6);
        float intensidad = 0.38F + 0.42F * penumbra;
        final int paso = 5;
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
