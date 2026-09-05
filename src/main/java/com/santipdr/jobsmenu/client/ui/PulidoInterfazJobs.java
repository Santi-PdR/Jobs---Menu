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

    private static final long ENTRADA_MS = 460L;
    private static final long AVISO_MS = 2100L;
    private static final Component CAMBIO_GUARDADO =
            Component.translatable("jobsmenu.interfaz.cambio_guardado");
    private static final Map<Screen, Long> APERTURAS = new WeakHashMap<>();
    private static long avisoHasta;

    private PulidoInterfazJobs() {
    }

    public static void notificarApertura(Screen pantalla) {
        if (Minecraft.getInstance().level != null) return;
        if (pantalla != null) APERTURAS.put(pantalla, System.currentTimeMillis());
    }

    public static void confirmarCambio() {
        avisoHasta = System.currentTimeMillis() + AVISO_MS;
    }

    public static void dibujar(Screen pantalla, GuiGraphics g, int mouseX, int mouseY) {
        if (pantalla == null || g == null) return;
        long ahora = System.currentTimeMillis();
        marcoPantalla(g, pantalla.width, pantalla.height);
        widgets(g, pantalla, mouseX, mouseY, ahora);
        entrada(g, pantalla, ahora);
        aviso(g, pantalla, ahora);
    }

    private static void marcoPantalla(GuiGraphics g, int w, int h) {
        if (w < 120 || h < 90) return;
        int m = Math.max(5, Math.min(12, Math.min(w, h) / 28));
        int l = Math.max(6, Math.min(14, m + 2));
        int c = Paleta.conAlfa(Paleta.UI_ACENTO, 0.25F);
        esquina(g, m, m, l, 1, 1, c);
        esquina(g, w - m, m, l, -1, 1, c);
        esquina(g, m, h - m, l, 1, -1, c);
        esquina(g, w - m, h - m, l, -1, -1, c);

        int rail = Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.13F);
        if (w >= 260) {
            g.fill(m + l + 6, m, w - m - l - 6, m + 1, rail);
            g.fill(m + l + 18, h - m - 1, w - m - l - 18, h - m,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.08F));
            int cx = w / 2;
            g.fill(cx - 11, m - 1, cx + 11, m, Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.14F));
            g.fill(cx, m - 4, cx + 1, m + 2, Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.19F));
            g.fill(cx - 3, h - m, cx + 4, h - m + 1, Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.11F));
        }
        if (h >= 180) {
            g.fill(m, m + l + 10, m + 1, h - m - l - 10,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.060F));
            g.fill(w - m - 1, m + l + 18, w - m, h - m - l - 18,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.045F));
            int cy = h / 2;
            g.fill(m - 1, cy - 5, m + 1, cy + 5, Paleta.conAlfa(Paleta.UI_ACENTO, 0.10F));
            g.fill(w - m - 1, cy - 3, w - m + 1, cy + 3, Paleta.conAlfa(Paleta.UI_ACENTO, 0.07F));
        }

        if (!ConfigTurno.papelLimpio() && !ConfigTurno.bajoConsumo() && w >= 320 && h >= 180) {
            int marca = Paleta.conAlfa(Paleta.UI_ACENTO, 0.055F);
            for (int x = m + 32; x < w - m - 32; x += 52) {
                g.fill(x, m + 2, x + 1, m + 4, marca);
            }
            for (int y = m + 34; y < h - m - 34; y += 58) {
                g.fill(m + 2, y, m + 4, y + 1, Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.040F));
            }
        }
    }

    /** Cuenta jerarquia y pinta foco en un unico recorrido de children(). */
    private static void widgets(GuiGraphics g, Screen pantalla, int mouseX, int mouseY, long ahora) {
        int activos = 0;
        int visibles = 0;
        boolean animarFoco = !ConfigTurno.movimientoReducido() && !ConfigTurno.bajoConsumo();

        for (var child : pantalla.children()) {
            if (!(child instanceof AbstractWidget w) || !w.visible) continue;
            visibles++;
            if (w.active) activos++;
            if (!w.active) continue;

            boolean raton = w.isMouseOver(mouseX, mouseY);
            boolean teclado = w.isFocused() && !raton;
            if (!raton && !teclado) continue;

            int x = w.getX();
            int y = w.getY();
            int ancho = w.getWidth();
            int alto = w.getHeight();
            float pulso = 1.0F;
            if (teclado && animarFoco) {
                pulso = 0.84F + 0.16F * (float) ((Math.sin(ahora / 300.0D) + 1.0D) * 0.5D);
            }
            int c = Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO,
                    (teclado ? 0.86F : 0.44F) * pulso);
            int l = Math.min(10, Math.max(4, alto / 3));
            g.fill(x - 2, y - 2, x + l, y - 1, c);
            g.fill(x - 2, y - 2, x - 1, y + l, c);
            g.fill(x + ancho - l, y + alto + 1, x + ancho + 2, y + alto + 2, c);
            g.fill(x + ancho + 1, y + alto - l, x + ancho + 2, y + alto + 2, c);

            if (ancho > 36) {
                int cx = x + ancho / 2;
                g.fill(cx - 5, y - 1, cx + 5, y,
                        Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO,
                                (teclado ? 0.42F : 0.18F) * pulso));
            }

            if (teclado) {
                g.fill(x + 4, y + alto + 2, x + ancho - 4, y + alto + 3,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.32F * pulso));
                g.fill(x - 4, y + alto / 2 - 2, x - 2, y + alto / 2 + 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.56F * pulso));
                g.fill(x + ancho + 2, y + alto / 2 - 1, x + ancho + 4, y + alto / 2 + 1,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.30F * pulso));
            } else if (raton && ancho > 28) {
                int marca = Math.min(22, Math.max(8, ancho / 7));
                g.fill(x + 5, y + alto + 1, x + 5 + marca, y + alto + 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO, 0.30F));
                g.fill(x + ancho - 3, y + 4, x + ancho - 2, y + alto - 4,
                        Paleta.conAlfa(Paleta.UI_ACENTO, 0.13F));
            }
        }

        dibujarJerarquia(g, pantalla, visibles, activos);
    }

    private static void dibujarJerarquia(GuiGraphics g, Screen pantalla, int visibles, int activos) {
        if (pantalla.width < 220 || pantalla.height < 120) return;
        int m = Math.max(6, Math.min(12, Math.min(pantalla.width, pantalla.height) / 28));
        int maxPips = Math.min(9, visibles);
        int start = pantalla.width - m - maxPips * 4;
        for (int i = 0; i < maxPips; i++) {
            boolean activo = i < Math.min(activos, maxPips);
            int a = activo ? (i == 0 ? 0x2E : 0x1A) : 0x0D;
            int base = activo ? Paleta.UI_ACENTO : Paleta.UI_TINTA_TENUE;
            g.fill(start + i * 4, pantalla.height - m - 2,
                    start + i * 4 + 2, pantalla.height - m,
                    (a << 24) | (base & 0x00FFFFFF));
        }

        if (pantalla.width >= 360) {
            int x = m + 18;
            int y = pantalla.height - m - 2;
            g.fill(x, y, x + 22, y + 1, Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.10F));
            g.fill(x, y - 2, x + Math.min(22, 4 + activos * 2), y - 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.12F));
        }
    }

    private static void esquina(GuiGraphics g, int x, int y, int l, int dx, int dy, int c) {
        int x0 = dx > 0 ? x : x - l;
        int y0 = dy > 0 ? y : y - l;
        g.fill(x0, Math.min(y, y + dy), x0 + l, Math.max(y, y + dy), c);
        g.fill(Math.min(x, x + dx), y0, Math.max(x, x + dx), y0 + l, c);
        int px = dx > 0 ? x + 2 : x - 3;
        int py = dy > 0 ? y + 2 : y - 3;
        g.fill(px, py, px + 1, py + 1, Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.30F));
    }

    private static void entrada(GuiGraphics g, Screen pantalla, long ahora) {
        if (Minecraft.getInstance().level != null) {
            APERTURAS.remove(pantalla);
            return;
        }
        Long abierta = APERTURAS.get(pantalla);
        if (abierta == null) return;
        long dt = ahora - abierta;
        if (dt < 0 || dt >= ENTRADA_MS) return;
        float t = dt / (float) ENTRADA_MS;
        float restante = 1.0F - t;
        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()) {
            g.fill(0, 0, pantalla.width, pantalla.height,
                    Paleta.conAlfa(Paleta.VANO, restante * 0.075F));
            return;
        }

        float suave = restante * restante;
        int borde = Math.max(1, Math.round(suave * Math.min(24, pantalla.width / 11.0F)));
        int c = Paleta.conAlfa(Paleta.VANO, suave * 0.30F);
        g.fill(0, 0, pantalla.width, borde, c);
        g.fill(0, pantalla.height - borde, pantalla.width, pantalla.height, c);
        g.fill(0, borde, Math.max(1, borde / 2), pantalla.height - borde, c);
        g.fill(pantalla.width - Math.max(1, borde / 2), borde,
                pantalla.width, pantalla.height - borde, c);

        int linea = Math.max(1, borde / 5);
        g.fill(0, Math.max(0, borde - linea), pantalla.width, borde,
                Paleta.conAlfa(Paleta.UI_ACENTO, suave * 0.09F));
        if (pantalla.width > 240) {
            int cx = pantalla.width / 2;
            int span = Math.max(8, Math.round((pantalla.width / 3.0F) * suave));
            g.fill(cx - span, Math.max(0, borde - 1), cx + span, borde,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, suave * 0.06F));
        }
    }

    private static void aviso(GuiGraphics g, Screen pantalla, long ahora) {
        long restante = avisoHasta - ahora;
        if (restante <= 0L) return;
        Font font = Minecraft.getInstance().font;
        int tw = font.width(CAMBIO_GUARDADO);
        int w = Math.min(pantalla.width - 16, tw + 38);
        int x = pantalla.width - w - 8;
        int y = 8;
        float salida = Math.min(1.0F, restante / 330.0F);
        float entrada = Math.min(1.0F, (AVISO_MS - restante) / 190.0F);
        float a = Math.min(entrada, salida);
        float progreso = Math.max(0.0F, Math.min(1.0F, restante / (float) AVISO_MS));

        g.fill(x + 2, y + 3, x + w + 2, y + 28, Paleta.conAlfa(Paleta.VANO, 0.23F * a));
        g.fill(x, y, x + w, y + 25, Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.95F * a));
        g.fill(x, y, x + 3, y + 25, Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.92F * a));
        g.fill(x + 7, y + 5, x + 12, y + 10, Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.72F * a));
        g.fill(x + 8, y + 6, x + 11, y + 9, Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.92F * a));
        g.fill(x + 4, y + 23, x + w - 4, y + 24, Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.18F * a));
        g.fill(x + 4, y + 23, x + 4 + Math.max(1, Math.round((w - 8) * progreso)), y + 24,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.34F * a));
        g.drawString(font, ChromeExpediente.ajustar(font, CAMBIO_GUARDADO.getString(), w - 27),
                x + 18, y + 8, Paleta.conAlfa(Paleta.ARCHIVO_TEXTO, 0.98F * a), false);
    }
}
