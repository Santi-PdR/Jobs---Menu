package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.JobsMenu;
import com.santipdr.jobsmenu.client.scene.EscenaNivel;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

/** Lenguaje visual compartido por las pantallas administrativas de Jobs. */
public final class ChromeExpediente {

    private ChromeExpediente() {
    }

    /** Fondo comun. No abre/cierra audio: solo dibuja el recinto vigente. */
    public static void fondo(GuiGraphics g, int ancho, int alto) {
        g.fill(0, 0, ancho, alto, 0xFF000000);
        EscenaNivel.dibujar(g, ancho, alto);

        g.fill(0, 0, ancho, alto, Paleta.conAlfa(Paleta.VANO,
                ConfigTurno.altoContraste() ? 0.58F : 0.46F));

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

    /**
     * Pie seguro para pantallas estrechas y expresivo en paneles anchos. En
     * anchos normales se divide a los extremos y deja el centro libre para
     * navegacion; en superficies muy anchas puede usar la cadena localizada
     * completa sin acercarse al boton central.
     */
    public static void pie(GuiGraphics g, Font font, int x, int y, int w, int h, String formulario) {
        int nivel = RotacionNiveles.capturar().indice();
        String version = version();
        int ty = y + h - 15;
        int color = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.54F);
        String nivelTexto = String.format(java.util.Locale.ROOT, "%02d", nivel);

        if (w >= 560) {
            Component completo = Component.translatable("jobsmenu.interfaz.formulario",
                    formulario, nivelTexto, version);
            int tw = font.width(completo);
            g.drawString(font, completo, x + w - 13 - tw, ty, color, false);
            return;
        }

        String codigo = formulario + " - N" + nivelTexto;
        String revision = "v" + version;

        int margen = 13;
        int mitad = x + w / 2;
        int reservaCentral = Math.min(94, Math.max(54, w / 5));
        int maxLado = Math.max(0, w / 2 - reservaCentral - margen - 4);

        String codigoVisible = ajustar(font, codigo, maxLado);
        String revisionVisible = ajustar(font, revision, maxLado);

        if (!codigoVisible.isEmpty()) {
            g.drawString(font, codigoVisible, x + margen, ty, color, false);
        }
        if (!revisionVisible.isEmpty()) {
            int rw = font.width(revisionVisible);
            g.drawString(font, revisionVisible, x + w - margen - rw, ty, color, false);
        }

        int marca = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.16F);
        g.fill(mitad - reservaCentral, ty + 3, mitad - reservaCentral + 4, ty + 4, marca);
        g.fill(mitad + reservaCentral - 4, ty + 3, mitad + reservaCentral, ty + 4, marca);
    }

    private static String ajustar(Font font, String texto, int maximo) {
        if (texto == null || maximo <= 8) return "";
        if (font.width(texto) <= maximo) return texto;
        String puntos = "...";
        return font.plainSubstrByWidth(texto, Math.max(0, maximo - font.width(puntos))) + puntos;
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
