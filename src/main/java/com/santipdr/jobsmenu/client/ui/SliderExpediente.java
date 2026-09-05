package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/** Slider administrativo con escala, lectura de valor y respuesta visual precisa. */
public final class SliderExpediente extends AbstractSliderButton {

    private final int minimo;
    private final int maximo;
    private final IntConsumer fijar;
    private final IntFunction<Component> rotulo;
    private final IntFunction<Component> lecturaCorta;
    private int ultimoAplicado;
    private long ultimoSonido;
    private long cambioHasta;
    private float focoSuave;
    private float valorVisual;
    private boolean hoverPrevio;

    public SliderExpediente(int x, int y, int ancho, int alto,
                            int minimo, int maximo, int inicial,
                            IntFunction<Component> rotulo, IntConsumer fijar) {
        this(x, y, ancho, alto, minimo, maximo, inicial, rotulo, fijar,
                v -> Component.literal(Integer.toString(v)));
    }

    public SliderExpediente(int x, int y, int ancho, int alto,
                            int minimo, int maximo, int inicial,
                            IntFunction<Component> rotulo, IntConsumer fijar,
                            IntFunction<Component> lecturaCorta) {
        super(x, y, ancho, alto, Component.empty(), normalizar(inicial, minimo, maximo));
        this.minimo = minimo;
        this.maximo = Math.max(minimo + 1, maximo);
        this.rotulo = rotulo;
        this.fijar = fijar;
        this.lecturaCorta = lecturaCorta != null ? lecturaCorta
                : v -> Component.literal(Integer.toString(v));
        this.ultimoAplicado = valorEntero();
        this.valorVisual = (float) this.value;
        updateMessage();
    }

    private static double normalizar(int v, int min, int max) {
        if (max <= min) return 0.0D;
        return Math.max(0.0D, Math.min(1.0D, (v - min) / (double) (max - min)));
    }

    private int valorEntero() {
        return this.minimo + (int) Math.round(this.value * (this.maximo - this.minimo));
    }

    @Override
    protected void updateMessage() {
        if (this.rotulo != null) this.setMessage(this.rotulo.apply(valorEntero()));
    }

    @Override
    protected void applyValue() {
        int valor = valorEntero();
        if (valor == this.ultimoAplicado) return;
        this.ultimoAplicado = valor;
        if (this.fijar != null) this.fijar.accept(valor);
        this.cambioHasta = System.currentTimeMillis() + 300L;
        PulidoInterfazJobs.confirmarCambio();
        long ahora = System.nanoTime();
        if (ahora - this.ultimoSonido > 110_000_000L) {
            this.ultimoSonido = ahora;
            MezclaAudio.gesto(SonidosNivel.UI_ALTERNAR, 0.30F);
        }
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = this.width;
        int h = this.height;
        boolean raton = this.active && this.isMouseOver(mouseX, mouseY);
        boolean teclado = this.active && this.isFocused() && !raton;
        boolean foco = raton || teclado;
        if (foco && !this.hoverPrevio) MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.18F);
        this.hoverPrevio = foco;

        float destino = foco ? 1.0F : 0.0F;
        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()) {
            this.focoSuave = destino;
            this.valorVisual = (float) this.value;
        } else {
            this.focoSuave += (destino - this.focoSuave) * 0.24F;
            this.valorVisual += ((float) this.value - this.valorVisual) * 0.34F;
        }

        int fondo = Paleta.mezclar(Paleta.papelAviso(), Paleta.UI_PAPEL_FOCO,
                0.16F + 0.58F * this.focoSuave);
        if (!this.active) fondo = Paleta.mezclar(Paleta.VANO, fondo, 0.20F);
        g.fill(x, y, x + w, y + h, fondo);
        int borde = Paleta.conAlfa(Paleta.tintaSecundaria(),
                this.active ? 0.40F + 0.34F * this.focoSuave : 0.18F);
        marco(g, x, y, w, h, borde);
        g.fill(x + 3, y + 3, x + w - 3, y + 4,
                Paleta.conAlfa(Paleta.UI_PAPEL_FOCO, this.active ? 0.28F : 0.10F));

        if (w >= 96) {
            int centro = x + w / 2;
            g.fill(centro - 8, y + 1, centro + 8, y + 2,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.08F + 0.08F * this.focoSuave));
        }

        Font font = Minecraft.getInstance().font;
        String txt = getMessage().getString();
        String lectura = this.lecturaCorta.apply(valorEntero()).getString();
        int badgeW = w >= 118 ? Math.max(31, font.width(lectura) + 10) : 0;
        int max = Math.max(8, w - 28 - badgeW);
        if (font.width(txt) > max) {
            txt = font.plainSubstrByWidth(txt, Math.max(0, max - font.width("..."))) + "...";
        }
        int tw = font.width(txt);
        int textX = badgeW > 0 ? x + 9 : x + (w - tw) / 2;
        int textY = y + 3;
        g.drawString(font, txt, textX, textY,
                this.active ? Paleta.tintaPrincipal() : Paleta.conAlfa(Paleta.tintaSecundaria(), 0.52F), false);

        if (badgeW > 0) {
            int bx = x + w - badgeW - 6;
            int by = y + 3;
            int bh = Math.min(11, h - 8);
            g.fill(bx, by, bx + badgeW, by + bh,
                    Paleta.conAlfa(Paleta.VANO, 0.07F + 0.05F * this.focoSuave));
            g.fill(bx, by + bh - 1, bx + badgeW, by + bh,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.20F + 0.18F * this.focoSuave));
            int pw = font.width(lectura);
            g.drawString(font, lectura, bx + (badgeW - pw) / 2, by + 1,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), this.active ? 0.82F : 0.46F), false);
        }

        int margen = 10;
        int barraY = y + h - 7;
        int barraX0 = x + margen;
        int barraX1 = x + w - margen;
        int largo = Math.max(1, barraX1 - barraX0);
        g.fill(barraX0 - 1, barraY - 1, barraX1 + 1, barraY + 3,
                Paleta.conAlfa(Paleta.VANO, 0.045F));
        g.fill(barraX0, barraY, barraX1, barraY + 1,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.31F));
        g.fill(barraX0, barraY + 1, barraX1, barraY + 2,
                Paleta.conAlfa(Paleta.VANO, 0.08F));

        for (int i = 0; i <= 10; i++) {
            int tx = barraX0 + Math.round(largo * (i / 10.0F));
            boolean mayor = i == 0 || i == 5 || i == 10;
            int th = mayor ? 4 : 2;
            g.fill(tx, barraY - th, tx + 1, barraY,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), mayor ? 0.46F : 0.20F));
            if (mayor && h >= 20) {
                g.fill(tx - 1, barraY + 2, tx + 2, barraY + 3,
                        Paleta.conAlfa(Paleta.tintaSecundaria(), 0.08F));
            }
        }

        int knobReal = barraX0 + (int) Math.round(largo * this.value);
        int knob = barraX0 + Math.round(largo * this.valorVisual);
        g.fill(barraX0, barraY, knob, barraY + 2,
                Paleta.conAlfa(Paleta.UI_ACENTO, this.active ? 0.66F + 0.20F * this.focoSuave : 0.24F));
        g.fill(barraX0, barraY - 1, knob, barraY,
                Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, this.active ? 0.18F + 0.18F * this.focoSuave : 0.08F));

        if (Math.abs(knobReal - knob) >= 2 && this.active) {
            g.fill(knobReal, barraY - 2, knobReal + 1, barraY + 3,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.24F));
        }

        int knobH = Math.min(11, h - 4);
        int ky = barraY - knobH / 2;
        g.fill(knob - 4, ky + 1, knob + 5, ky + knobH + 1,
                Paleta.conAlfa(Paleta.VANO, 0.16F));
        g.fill(knob - 3, ky, knob + 4, ky + knobH,
                Paleta.conAlfa(Paleta.tintaPrincipal(), this.active ? 0.88F : 0.42F));
        g.fill(knob - 2, ky + 1, knob + 3, ky + knobH - 1,
                Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, this.active ? 0.94F : 0.38F));
        g.fill(knob, ky + 2, knob + 1, ky + knobH - 2,
                Paleta.conAlfa(Paleta.tintaPrincipal(), 0.34F));
        g.fill(knob - 1, ky + knobH / 2, knob + 2, ky + knobH / 2 + 1,
                Paleta.conAlfa(Paleta.UI_PAPEL, 0.30F));

        if (w >= 142) {
            String minTxt = Integer.toString(this.minimo);
            String maxTxt = Integer.toString(this.maximo);
            g.drawString(font, minTxt, barraX0, barraY - 12,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.34F), false);
            int mw = font.width(maxTxt);
            g.drawString(font, maxTxt, barraX1 - mw, barraY - 12,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.34F), false);
        }

        if (foco) {
            int c = Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO,
                    teclado ? 0.86F : 0.62F);
            g.fill(x + 3, y + 3, x + 4, y + h - 3, c);
            g.fill(x + w - 4, y + 3, x + w - 3, y + h - 3,
                    Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO,
                            teclado ? 0.72F : 0.34F));
            int guia = barraX0 + Math.max(1, Math.round(largo * this.focoSuave));
            g.fill(barraX0, y + h - 2, guia, y + h - 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.24F));
            if (teclado) {
                g.fill(knobReal - 5, ky - 2, knobReal + 6, ky - 1,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.48F));
                g.fill(knobReal, ky - 4, knobReal + 1, ky - 1,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.36F));
            }
        }

        long restante = this.cambioHasta - System.currentTimeMillis();
        if (restante > 0L && this.active) {
            float a = Math.max(0.0F, Math.min(1.0F, restante / 300.0F));
            int cx = x + w / 2;
            int span = Math.max(5, Math.round((w - 20) * (1.0F - a)));
            g.fill(Math.max(x + 10, cx - span / 2), y + h - 3,
                    Math.min(x + w - 10, cx + span / 2), y + h - 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.48F * a));
        }

        if (!this.active) {
            int cy = y + h / 2;
            g.fill(x + 5, cy, x + 9, cy + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.28F));
            g.fill(x + w - 9, cy, x + w - 5, cy + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.18F));
        }
    }

    private static void marco(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }
}
