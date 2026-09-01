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

/** Microinteracciones compartidas que no cambian hitboxes ni logica de Minecraft. */
public final class PulidoInterfazJobs {

    private static final long ENTRADA_MS = 360L;
    private static final long AVISO_MS = 1500L;
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
        margenSeguro(g, pantalla.width, pantalla.height);
        foco(g, pantalla, mouseX, mouseY);
        entrada(g, pantalla);
        aviso(g, pantalla);
    }

    private static void margenSeguro(GuiGraphics g, int w, int h) {
        if (w < 120 || h < 90) return;
        int m = Math.max(5, Math.min(12, Math.min(w, h) / 28));
        int l = Math.max(5, Math.min(12, m));
        int c = Paleta.conAlfa(Paleta.PARED_ALTA, 0.18F);
        esquina(g, m, m, l, 1, 1, c);
        esquina(g, w - m, m, l, -1, 1, c);
        esquina(g, m, h - m, l, 1, -1, c);
        esquina(g, w - m, h - m, l, -1, -1, c);
    }

    private static void esquina(GuiGraphics g, int x, int y, int l, int dx, int dy, int c) {
        int x0 = dx > 0 ? x : x - l;
        int y0 = dy > 0 ? y : y - l;
        g.fill(x0, Math.min(y, y + dy), x0 + l, Math.max(y, y + dy), c);
        g.fill(Math.min(x, x + dx), y0, Math.max(x, x + dx), y0 + l, c);
    }

    private static void foco(GuiGraphics g, Screen pantalla, int mouseX, int mouseY) {
        for (var child : pantalla.children()) {
            if (!(child instanceof AbstractWidget w) || !w.visible || !w.active) continue;
            boolean raton = w.isMouseOver(mouseX, mouseY);
            boolean teclado = w.isFocused() && !raton;
            if (!raton && !teclado) continue;
            int x = w.getX();
            int y = w.getY();
            int ancho = w.getWidth();
            int alto = w.getHeight();
            int c = Paleta.conAlfa(teclado ? Paleta.FLUOR : Paleta.PARED_ALTA,
                    teclado ? 0.72F : 0.30F);
            int l = Math.min(7, Math.max(3, alto / 3));
            g.fill(x - 2, y - 2, x + l, y - 1, c);
            g.fill(x - 2, y - 2, x - 1, y + l, c);
            g.fill(x + ancho - l, y + alto + 1, x + ancho + 2, y + alto + 2, c);
            g.fill(x + ancho + 1, y + alto - l, x + ancho + 2, y + alto + 2, c);
            if (teclado && !ConfigTurno.movimientoReducido()) {
                g.fill(x + 3, y + alto + 2, x + ancho - 3, y + alto + 3,
                        Paleta.conAlfa(Paleta.FLUOR, 0.24F));
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
                    Paleta.conAlfa(Paleta.VANO, restante * 0.12F));
            return;
        }
        int borde = Math.max(1, Math.round(restante * Math.min(22, pantalla.width / 12.0F)));
        int c = Paleta.conAlfa(Paleta.VANO, restante * 0.36F);
        g.fill(0, 0, pantalla.width, borde, c);
        g.fill(0, pantalla.height - borde, pantalla.width, pantalla.height, c);
        g.fill(0, borde, Math.max(1, borde / 2), pantalla.height - borde, c);
        g.fill(pantalla.width - Math.max(1, borde / 2), borde,
                pantalla.width, pantalla.height - borde, c);
    }

    private static void aviso(GuiGraphics g, Screen pantalla) {
        long restante = avisoHasta - System.currentTimeMillis();
        if (restante <= 0L) return;
        Font font = Minecraft.getInstance().font;
        Component texto = Component.translatable("jobsmenu.interfaz.cambio_guardado");
        int tw = font.width(texto);
        int w = Math.min(pantalla.width - 16, tw + 22);
        int x = pantalla.width - w - 8;
        int y = 8;
        float a = Math.min(1.0F, restante / 220.0F);
        g.fill(x, y, x + w, y + 22, Paleta.conAlfa(Paleta.VANO, 0.78F * a));
        g.fill(x, y, x + 2, y + 22, Paleta.conAlfa(Paleta.FLUOR, 0.74F * a));
        g.drawString(font, ChromeExpediente.ajustar(font, texto.getString(), w - 12),
                x + 8, y + 7, Paleta.conAlfa(Paleta.PAPEL, 0.92F * a), false);
    }
}
