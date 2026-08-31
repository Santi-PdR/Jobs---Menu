package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.scene.EscenaNivel;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

/**
 * Lenguaje visual compartido por las pantallas administrativas de Jobs.
 *
 * La referencia de GripeVerde enseno una idea util: las pantallas hijas deben
 * sentirse parte del mismo sitio aunque por dentro sigan usando la logica de
 * Minecraft. Aqui esa idea se traduce al lenguaje de Jobs: expediente, papel
 * fotocopiado, tinta seca, luz de instalacion y el recinto vivo detras.
 */
public final class ChromeExpediente {

    private ChromeExpediente() {
    }

    /** Fondo vivo comun. No abre/cierra audio: solo dibuja el recinto vigente. */
    public static void fondo(GuiGraphics g, int ancho, int alto) {
        g.fill(0, 0, ancho, alto, 0xFF000000);
        EscenaNivel.dibujar(g, ancho, alto);

        // Las pantallas de trabajo necesitan mas calma que el aviso principal.
        g.fill(0, 0, ancho, alto, Paleta.conAlfa(Paleta.VANO,
                ConfigTurno.altoContraste() ? 0.58F : 0.46F));

        // Un grano horizontal casi invisible integra widgets vanilla con el
        // recinto. Bajo consumo lo omite por completo.
        if (!ConfigTurno.bajoConsumo()) {
            for (int y = 1; y < alto; y += 5) {
                g.fill(0, y, ancho, y + 1, Paleta.conAlfa(Paleta.FLUOR, 0.012F));
            }
        }
    }

    /** Papel principal del expediente con doble borde y marcas de archivado. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h) {
        HojaPapel.dibujar(g, x, y, x + w, y + h, false, 0.94F);

        int borde = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.42F);
        g.fill(x + 4, y + 4, x + w - 4, y + 5, borde);
        g.fill(x + 4, y + h - 5, x + w - 4, y + h - 4, borde);
        g.fill(x + 4, y + 4, x + 5, y + h - 4, borde);
        g.fill(x + w - 5, y + 4, x + w - 4, y + h - 4, borde);

        // Perforaciones de carpeta: pequenas y deterministas.
        int agujero = Paleta.conAlfa(Paleta.VANO, 0.28F);
        for (int i = 0; i < 3; i++) {
            int py = y + h / 4 + i * h / 4;
            g.fill(x + 7, py - 1, x + 9, py + 1, agujero);
        }
    }

    /** Cabecera de una pantalla propia. */
    public static void cabecera(GuiGraphics g, Font font, Component titulo, Component subtitulo,
                                int panelX, int panelY, int panelW) {
        int centro = panelX + panelW / 2;
        int ty = panelY + 13;
        int tw = font.width(titulo);
        g.drawString(font, titulo, centro - tw / 2, ty, Paleta.tintaPrincipal(), false);

        if (subtitulo != null) {
            int sw = font.width(subtitulo);
            g.drawString(font, subtitulo, centro - sw / 2, ty + 13,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.82F), false);
        }
        divisor(g, panelX + 18, panelX + panelW - 18, panelY + 42);
    }

    /** Decoracion ligera para una subpantalla vanilla que conserva su titulo. */
    public static void marcoSubpantalla(GuiGraphics g, Font font, int ancho, int alto,
                                        int panelX, int panelY, int panelW, int panelH,
                                        Component subtitulo, String formulario) {
        if (subtitulo != null && panelH > 90) {
            int sw = font.width(subtitulo);
            g.drawString(font, subtitulo, ancho / 2 - sw / 2, panelY + 28,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.70F), false);
            divisor(g, panelX + 18, panelX + panelW - 18, panelY + 40);
        }
        esquinas(g, panelX, panelY, panelW, panelH);
        pie(g, font, panelX, panelY, panelW, panelH, formulario);
    }

    public static void divisor(GuiGraphics g, int x0, int x1, int y) {
        if (x1 <= x0) return;
        int medio = (x0 + x1) / 2;
        int fuerte = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.40F);
        int suave = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.08F);
        // fillGradient solo interpola verticalmente; el degradado horizontal se
        // aproxima por cuatro tramos para no repetir el bug historico del mod.
        int q = Math.max(1, (x1 - x0) / 4);
        g.fill(x0, y, x0 + q, y + 1, suave);
        g.fill(x0 + q, y, medio, y + 1, fuerte);
        g.fill(medio, y, x1 - q, y + 1, fuerte);
        g.fill(x1 - q, y, x1, y + 1, suave);
        g.fill(medio - 1, y - 1, medio + 1, y + 2,
                Paleta.conAlfa(Paleta.tintaPrincipal(), 0.38F));
    }

    public static void esquinas(GuiGraphics g, int x, int y, int w, int h) {
        int c = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.36F);
        int m = 8;
        int l = 10;
        g.fill(x + m, y + m, x + m + l, y + m + 1, c);
        g.fill(x + m, y + m, x + m + 1, y + m + l, c);
        g.fill(x + w - m - l, y + m, x + w - m, y + m + 1, c);
        g.fill(x + w - m - 1, y + m, x + w - m, y + m + l, c);
        g.fill(x + m, y + h - m - 1, x + m + l, y + h - m, c);
        g.fill(x + m, y + h - m - l, x + m + 1, y + h - m, c);
        g.fill(x + w - m - l, y + h - m - 1, x + w - m, y + h - m, c);
        g.fill(x + w - m - 1, y + h - m - l, x + w - m, y + h - m, c);
    }

    /** Sello inferior con formulario, nivel actual y version real del mod. */
    public static void pie(GuiGraphics g, Font font, int x, int y, int w, int h, String formulario) {
        int nivel = RotacionNiveles.capturar().indice();
        String version = version();
        Component texto = Component.translatable("jobsmenu.interfaz.formulario",
                formulario, String.format(java.util.Locale.ROOT, "%02d", nivel), version);
        int tx = x + w - font.width(texto) - 13;
        int ty = y + h - 15;
        g.drawString(font, texto, Math.max(x + 12, tx), ty,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.54F), false);
    }

    /** Banda discreta para pantallas menores que siguen siendo de otra clase. */
    public static void bandaContextual(GuiGraphics g, Font font, int ancho, int alto) {
        int altoBanda = 17;
        g.fill(0, 0, ancho, altoBanda, Paleta.conAlfa(Paleta.PAPEL, 0.88F));
        g.fill(0, altoBanda - 1, ancho, altoBanda,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.38F));
        Component rotulo = Component.translatable("jobsmenu.interfaz.banda");
        g.drawString(font, rotulo, 8, 4, Paleta.tintaSecundaria(), false);

        Component estado = Component.translatable("jobsmenu.interfaz.estado");
        int ew = font.width(estado);
        g.drawString(font, estado, Math.max(8, ancho - ew - 8), 4,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.68F), false);

        g.fill(0, alto - 2, ancho, alto,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.18F));
    }

    private static String version() {
        try {
            return ModList.get().getModContainerById(JobsMenu.MOD_ID)
                    .map(c -> c.getModInfo().getVersion().toString())
                    .orElse("?");
        } catch (Throwable ignored) {
            return "?";
        }
    }
}
