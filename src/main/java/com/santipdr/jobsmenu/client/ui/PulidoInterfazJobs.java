package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.WeakHashMap;

/** Capa visual compartida de Jobs: feedback visible sin alterar hitboxes. */
public final class PulidoInterfazJobs {

    private static final long ENTRADA_MS = 420L;
    private static final long AVISO_MS = 1900L;
    private static final Map<Screen, Long> APERTURAS = new WeakHashMap<>();
    private static long avisoHasta;

    private PulidoInterfazJobs() {
    }

    public static void notificarApertura(Screen pantalla) {
        if (pantalla != null) APERTURAS.put(pantalla, System.currentTimeMillis());
    }

    public static void confirmarCambio() {
        avisoHasta = System.currentTimeMillis() + AVISO_MS;
    }

    public static void dibujar(Screen pantalla, GuiGraphics g, int mouseX, int mouseY) {
        if (pantalla == null || g == null) return;
        marcoPantalla(g, pantalla.width, pantalla.height);
        guiaJerarquia(g, pantalla);
        foco(g, pantalla, mouseX, mouseY);
        entrada(g, pantalla);
        aviso(g, pantalla);
    }

    private static void marcoPantalla(GuiGraphics g, int w, int h) {
        if (w < 120 || h < 90) return;
        int m = Math.max(5, Math.min(12, Math.min(w, h) / 28));
        int l = Math.max(6, Math.min(14, m + 2));
        int c = Paleta.conAlfa(Paleta.UI_ACENTO, 0.24F);
        esquina(g, m, m, l, 1, 1, c);
        esquina(g, w - m, m, l, -1, 1, c);
        esquina(g, m, h - m, l, 1, -1, c);
        esquina(g, w - m, h - m, l, -1, -1, c);

        int rail = Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.12F);
        if (w >= 260) {
            g.fill(m + l + 6, m, w - m - l - 6, m + 1, rail);
            g.fill(m + l + 18, h - m - 1, w - m - l - 18, h - m, Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.07F));
        }
        if (h >= 180) {
            g.fill(m, m + l + 10, m + 1, h - m - l - 10, Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.055F));
            g.fill(w - m - 1, m + l + 18, w - m, h - m - l - 18, Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.040F));
        }

        if (!ConfigTurno.papelLimpio() && !ConfigTurno.bajoConsumo() && w >= 320 && h >= 180) {
            int marca = Paleta.conAlfa(Paleta.UI_ACENTO, 0.055F);
            for (int x = m + 32; x < w - m - 32; x += 52) {
                g.fill(x, m + 2, x + 1, m + 4, marca);
            }
        }
    }

    private static void guiaJerarquia(GuiGraphics g, Screen pantalla) {
        if (pantalla.width < 220 || pantalla.height < 120) return;
        int m = Math.max(6, Math.min(12, Math.min(pantalla.width, pantalla.height) / 28));
        int centro = pantalla.width / 2;
        int c = Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.12F);
        g.fill(centro - 7, m - 1, centro + 7, m, c);
        g.fill(centro, m - 3, centro + 1, m + 2, Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.18F));

        int activos = 0;
        for (var child : pantalla.children()) {
            if (child instanceof AbstractWidget w && w.visible) activos++;
        }
        int maxPips = Math.min(8, activos);
        int start = pantalla.width - m - maxPips * 4;
        for (int i = 0; i < maxPips; i++) {
            int a = i == 0 ? 0x28 : 0x14;
            g.fill(start + i * 4, pantalla.height - m - 2, start + i * 4 + 2, pantalla.height - m,
                    (a << 24) | (Paleta.UI_ACENTO & 0x00FFFFFF));
        }
    }

    private static void esquina(GuiGraphics g, int x, int y, int l, int dx, int dy, int c) {
        int x0 = dx > 0 ? x : x - l;
        int y0 = dy > 0 ? y : y - l;
        g.fill(x0, Math.min(y, y + dy), x0 + l, Math.max(y, y + dy), c);
        g.fill(Math.min(x, x + dx), y0, Math.max(x, x + dx), y0 + l, c);
    }

    private static void foco(GuiGraphics g, Screen pantalla, int mouseX, int mouseY) {
        long ahora = System.currentTimeMillis();
        for (var child : pantalla.children()) {
            if (!(child instanceof AbstractWidget w) || !w.visible || !w.active) continue;
            boolean raton = w.isMouseOver(mouseX, mouseY);
            boolean teclado = w.isFocused() && !raton;
            if (!raton && !teclado) continue;

            int x = w.getX();
            int y = w.getY();
            int ancho = w.getWidth();
            int alto = w.getHeight();
            float pulso = 1.0F;
            if (teclado && !ConfigTurno.movimientoReducido() && !ConfigTurno.bajoConsumo()) {
                pulso = 0.82F + 0.18F * (float) ((Math.sin(ahora / 280.0D) + 1.0D) * 0.5D);
            }
            int c = Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO,
                    (teclado ? 0.84F : 0.42F) * pulso);
            int l = Math.min(9, Math.max(4, alto / 3));
            g.fill(x - 2, y - 2, x + l, y - 1, c);
            g.fill(x - 2, y - 2, x - 1, y + l, c);
            g.fill(x + ancho - l, y + alto + 1, x + ancho + 2, y + alto + 2, c);
            g.fill(x + ancho + 1, y + alto - l, x + ancho + 2, y + alto + 2, c);

            if (teclado) {
                g.fill(x + 4, y + alto + 2, x + ancho - 4, y + alto + 3,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.30F * pulso));
                g.fill(x - 4, y + alto / 2 - 2, x - 2, y + alto / 2 + 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.54F * pulso));
            } else if (raton && ancho > 28) {
                int marca = Math.min(20, Math.max(8, ancho / 7));
                g.fill(x + 5, y + alto + 1, x + 5 + marca, y + alto + 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO, 0.28F));
                g.fill(x + ancho - 3, y + 4, x + ancho - 2, y + alto - 4,
                        Paleta.conAlfa(Paleta.UI_ACENTO, 0.12F));
            }
        }
    }

    private static void entrada(GuiGraphics g, Screen pantalla) {
        Long abierta = APERTURAS.get(pantalla);
        if (abierta == null) return;
        long dt = System.currentTimeMillis() - abierta;
        if (dt < 0 || dt >= ENTRADA_MS) return;
        float t = dt / (float) ENTRADA_MS;
        float restante = 1.0F - t;
        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()) {
            g.fill(0, 0, pantalla.width, pantalla.height,
                    Paleta.conAlfa(Paleta.VANO, restante * 0.08F));
            return;
        }

        float suave = restante * restante;
        int borde = Math.max(1, Math.round(suave * Math.min(22, pantalla.width / 12.0F)));
        int c = Paleta.conAlfa(Paleta.VANO, suave * 0.30F);
        g.fill(0, 0, pantalla.width, borde, c);
        g.fill(0, pantalla.height - borde, pantalla.width, pantalla.height, c);
        g.fill(0, borde, Math.max(1, borde / 2), pantalla.height - borde, c);
        g.fill(pantalla.width - Math.max(1, borde / 2), borde,
                pantalla.width, pantalla.height - borde, c);
        int linea = Math.max(1, borde / 5);
        g.fill(0, Math.max(0, borde - linea), pantalla.width, borde,
                Paleta.conAlfa(Paleta.UI_ACENTO, suave * 0.08F));
    }

    private static void aviso(GuiGraphics g, Screen pantalla) {
        long restante = avisoHasta - System.currentTimeMillis();
        if (restante <= 0L) return;
        Font font = Minecraft.getInstance().font;
        Component texto = Component.translatable("jobsmenu.interfaz.cambio_guardado");
        int tw = font.width(texto);
        int w = Math.min(pantalla.width - 16, tw + 34);
        int x = pantalla.width - w - 8;
        int y = 8;
        float salida = Math.min(1.0F, restante / 300.0F);
        float entrada = Math.min(1.0F, (AVISO_MS - restante) / 180.0F);
        float a = Math.min(entrada, salida);
        g.fill(x + 2, y + 3, x + w + 2, y + 27, Paleta.conAlfa(Paleta.VANO, 0.22F * a));
        g.fill(x, y, x + w, y + 24, Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.94F * a));
        g.fill(x, y, x + 3, y + 24, Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.90F * a));
        g.fill(x + 7, y + 5, x + 11, y + 9, Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.70F * a));
        g.fill(x + 8, y + 6, x + 10, y + 8, Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.90F * a));
        g.fill(x + 3, y + 23, x + w, y + 24, Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.22F * a));
        g.drawString(font, ChromeExpediente.ajustar(font, texto.getString(), w - 24),
                x + 17, y + 8, Paleta.conAlfa(Paleta.ARCHIVO_TEXTO, 0.97F * a), false);
    }
}
