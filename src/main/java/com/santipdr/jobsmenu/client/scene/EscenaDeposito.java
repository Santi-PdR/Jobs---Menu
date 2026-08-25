package com.santipdr.jobsmenu.client.scene;

import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RelojAparicion;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Fondo del menu: el pasillo del deposito, de noche, bajo una lampara de sodio.
 *
 * Todo es procedural, sin una sola textura. La semilla del deterioro es fija a
 * proposito: siempre es el mismo deposito, siempre las mismas manchas.
 */
public final class EscenaDeposito {

    private EscenaDeposito() {
    }

    /** Semilla del deterioro de las placas de hormigon. */
    private static final long SEMILLA = 0x4A4F4253L;

    /** Cada cuantos milisegundos cruza algo el fondo del pasillo. */
    private static final long PERIODO_SILUETA_MS = 47_000L;

    /** Cuanto dura el cruce, en milisegundos. */
    private static final long DURACION_SILUETA_MS = 3_000L;

    private static final int GOTAS = 90;
    private static final int MOTAS = 60;

    public static void dibujar(GuiGraphics grafico, int ancho, int alto, float parcial) {
        long ahora = System.currentTimeMillis();
        boolean viva = ConfigTurno.escenaViva();
        boolean movimiento = viva && !ConfigTurno.movimientoReducido();
        boolean destellos = viva && !ConfigTurno.destellosReducidos();

        float tiempo = viva ? (float) (ahora % 600_000L) / 1000.0F : 0.0F;
        float penumbra = RelojAparicion.penumbra();
        float luz = brilloLampara(tiempo, destellos) * (1.0F - 0.45F * penumbra);

        fondo(grafico, ancho, alto);
        placas(grafico, ancho, alto);
        piso(grafico, ancho, alto);
        lampara(grafico, ancho, alto, luz);

        if (movimiento) {
            lluvia(grafico, ancho, alto, tiempo);
            polvo(grafico, ancho, alto, tiempo, luz);
            silueta(grafico, ancho, alto, ahora);
        }

        vineta(grafico, ancho, alto, penumbra);
    }

    private static void fondo(GuiGraphics grafico, int ancho, int alto) {
        grafico.fillGradient(0, 0, ancho, alto, Paleta.FONDO_ALTO, Paleta.FONDO_PROFUNDO);
    }

    /** Placas de hormigon de la pared del fondo, deterministas. */
    private static void placas(GuiGraphics grafico, int ancho, int alto) {
        int ladoX = 64;
        int ladoY = 40;
        int limiteY = (int) (alto * 0.72F);

        for (int y = 0; y < limiteY; y += ladoY) {
            for (int x = 0; x < ancho; x += ladoX) {
                int indice = (x / ladoX) * 31 + (y / ladoY) * 17;
                float ruido = pseudo(indice);
                float alfa = 0.06F + ruido * 0.10F;
                grafico.fill(x, y, Math.min(x + ladoX - 1, ancho), Math.min(y + ladoY - 1, limiteY),
                        Paleta.conAlfa(Paleta.HORMIGON, alfa));
            }
        }

        for (int y = 0; y < limiteY; y += ladoY) {
            grafico.fill(0, y, ancho, y + 1, Paleta.conAlfa(Paleta.HUMO, 0.16F));
        }
        for (int x = 0; x < ancho; x += ladoX) {
            grafico.fill(x, 0, x + 1, limiteY, Paleta.conAlfa(Paleta.HUMO, 0.12F));
        }
    }

    private static void piso(GuiGraphics grafico, int ancho, int alto) {
        int horizonte = (int) (alto * 0.72F);
        grafico.fill(0, horizonte, ancho, horizonte + 1, Paleta.conAlfa(Paleta.HUMO, 0.40F));
        grafico.fillGradient(0, horizonte + 1, ancho, alto,
                Paleta.conAlfa(Paleta.FONDO_PROFUNDO, 0.85F), Paleta.conAlfa(Paleta.HORMIGON, 0.45F));
    }

    /**
     * Cono y halo de la lampara. El parpadeo es irregular a proposito: dos
     * senos desfasados mas un tropiezo ocasional.
     */
    private static float brilloLampara(float tiempo, boolean destellos) {
        if (!destellos) {
            return 0.82F;
        }
        float base = 0.78F
                + 0.10F * (float) Math.sin(tiempo * 2.3F)
                + 0.06F * (float) Math.sin(tiempo * 7.1F + 1.7F);
        boolean tropiezo = Math.floorMod((long) (tiempo * 3.0F), 53L) == 0L;
        if (tropiezo) {
            base *= 0.45F;
        }
        return Math.max(0.25F, Math.min(1.0F, base));
    }

    private static void lampara(GuiGraphics grafico, int ancho, int alto, float luz) {
        int foco = (int) (ancho * 0.74F);
        int horizonte = (int) (alto * 0.72F);

        int capas = 7;
        for (int i = capas; i > 0; i--) {
            float t = (float) i / (float) capas;
            int extension = (int) (ancho * 0.10F * i);
            float alfa = 0.055F * luz * (1.0F - t) + 0.010F;
            grafico.fillGradient(foco - extension, 0, foco + extension, horizonte,
                    Paleta.conAlfa(Paleta.SODIO, alfa),
                    Paleta.conAlfa(Paleta.SODIO_TENUE, alfa * 0.25F));
        }

        int nucleo = 6;
        grafico.fill(foco - nucleo, 6, foco + nucleo, 9, Paleta.conAlfa(Paleta.SODIO, 0.30F + 0.55F * luz));
        grafico.fill(foco - 1, 0, foco + 1, 6, Paleta.conAlfa(Paleta.HUMO, 0.70F));
    }

    private static void lluvia(GuiGraphics grafico, int ancho, int alto, float tiempo) {
        for (int i = 0; i < GOTAS; i++) {
            float base = pseudo(i * 7 + 3);
            float velocidad = 60.0F + base * 90.0F;
            int x = (int) ((base * ancho * 3.0F + tiempo * 12.0F) % Math.max(1, ancho + 40)) - 20;
            int y = (int) ((base * alto + tiempo * velocidad) % Math.max(1, alto));
            int largo = 4 + (int) (base * 6.0F);
            grafico.fill(x, y, x + 1, y + largo, Paleta.conAlfa(Paleta.HUMO, 0.10F + base * 0.14F));
        }
    }

    private static void polvo(GuiGraphics grafico, int ancho, int alto, float tiempo, float luz) {
        int foco = (int) (ancho * 0.74F);
        int radio = (int) (ancho * 0.22F);
        int horizonte = (int) (alto * 0.72F);

        for (int i = 0; i < MOTAS; i++) {
            float a = pseudo(i * 13 + 5);
            float b = pseudo(i * 29 + 11);
            int x = foco - radio + (int) (a * radio * 2.0F);
            int y = (int) ((b * horizonte + tiempo * (6.0F + a * 10.0F)) % Math.max(1, horizonte));
            float brillo = 0.10F + 0.22F * luz * (1.0F - (float) y / Math.max(1.0F, horizonte));
            grafico.fill(x, y, x + 1, y + 1, Paleta.conAlfa(Paleta.SODIO, brillo));
        }
    }

    /** Algo cruza el fondo del pasillo. Tres segundos. Sin ruido. */
    private static void silueta(GuiGraphics grafico, int ancho, int alto, long ahora) {
        long fase = Math.floorMod(ahora, PERIODO_SILUETA_MS);
        if (fase > DURACION_SILUETA_MS) {
            return;
        }

        float t = (float) fase / (float) DURACION_SILUETA_MS;
        int horizonte = (int) (alto * 0.72F);
        int altura = (int) (alto * 0.30F);
        int anchoSilueta = Math.max(6, altura / 5);
        int x = (int) (ancho * 1.05F - t * (ancho * 1.25F));
        int y = horizonte - altura;

        float alfa = 0.55F * (float) Math.sin(t * Math.PI);

        grafico.fill(x, y, x + anchoSilueta, horizonte, Paleta.conAlfa(Paleta.FONDO_PROFUNDO, alfa));
        int cabeza = Math.max(3, anchoSilueta / 2);
        grafico.fill(x + (anchoSilueta - cabeza) / 2, y - cabeza, x + (anchoSilueta + cabeza) / 2, y,
                Paleta.conAlfa(Paleta.FONDO_PROFUNDO, alfa));
    }

    private static void vineta(GuiGraphics grafico, int ancho, int alto, float penumbra) {
        int franja = Math.max(24, ancho / 8);
        float intensidad = 0.55F + 0.35F * penumbra;

        grafico.fillGradient(0, 0, franja, alto,
                Paleta.conAlfa(Paleta.FONDO_PROFUNDO, intensidad), Paleta.conAlfa(Paleta.FONDO_PROFUNDO, 0.0F));
        grafico.fillGradient(ancho - franja, 0, ancho, alto,
                Paleta.conAlfa(Paleta.FONDO_PROFUNDO, 0.0F), Paleta.conAlfa(Paleta.FONDO_PROFUNDO, intensidad));
        grafico.fillGradient(0, 0, ancho, franja / 2,
                Paleta.conAlfa(Paleta.FONDO_PROFUNDO, intensidad), Paleta.conAlfa(Paleta.FONDO_PROFUNDO, 0.0F));
        grafico.fillGradient(0, alto - franja / 2, ancho, alto,
                Paleta.conAlfa(Paleta.FONDO_PROFUNDO, 0.0F), Paleta.conAlfa(Paleta.FONDO_PROFUNDO, intensidad));

        if (penumbra > 0.0F) {
            grafico.fill(0, 0, ancho, alto, Paleta.conAlfa(Paleta.FONDO_PROFUNDO, 0.30F * penumbra));
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
