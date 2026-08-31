package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/** Slider administrativo para valores enteros. */
public final class SliderExpediente extends AbstractSliderButton {

    private final int minimo;
    private final int maximo;
    private final IntConsumer fijar;
    private final IntFunction<Component> rotulo;
    private int ultimoAplicado;
    private long ultimoSonido;

    public SliderExpediente(int x, int y, int ancho, int alto,
                            int minimo, int maximo, int inicial,
                            IntFunction<Component> rotulo, IntConsumer fijar) {
        super(x, y, ancho, alto, Component.empty(), normalizar(inicial, minimo, maximo));
        this.minimo = minimo;
        this.maximo = Math.max(minimo + 1, maximo);
        this.rotulo = rotulo;
        this.fijar = fijar;
        this.ultimoAplicado = valorEntero();
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
        long ahora = System.nanoTime();
        if (ahora - this.ultimoSonido > 110_000_000L) {
            this.ultimoSonido = ahora;
            MezclaAudio.gesto(SonidosNivel.UI_ALTERNAR, 0.30F);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = this.width;
        int h = this.height;
        boolean foco = this.active && this.isHoveredOrFocused();

        g.fill(x, y, x + w, y + h, Paleta.papelAviso());
        int borde = Paleta.conAlfa(Paleta.tintaSecundaria(), foco ? 0.72F : 0.40F);
        g.fill(x, y, x + w, y + 1, borde);
        g.fill(x, y + h - 1, x + w, y + h, borde);
        g.fill(x, y, x + 1, y + h, borde);
        g.fill(x + w - 1, y, x + w, y + h, borde);

        int margen = 7;
        int barraY = y + h - 5;
        int barraX0 = x + margen;
        int barraX1 = x + w - margen;
        g.fill(barraX0, barraY, barraX1, barraY + 1,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.24F));
        int knob = barraX0 + (int) Math.round((barraX1 - barraX0) * this.value);
        g.fill(barraX0, barraY, knob, barraY + 2,
                Paleta.conAlfa(Paleta.tintaPrincipal(), foco ? 0.74F : 0.52F));
        g.fill(knob - 1, barraY - 2, knob + 2, barraY + 4,
                Paleta.conAlfa(Paleta.tintaPrincipal(), 0.82F));

        Font font = Minecraft.getInstance().font;
        String txt = getMessage().getString();
        int max = Math.max(8, w - 16);
        if (font.width(txt) > max) {
            txt = font.plainSubstrByWidth(txt, Math.max(0, max - font.width("..."))) + "...";
        }
        int tw = font.width(txt);
        g.drawString(font, txt, x + (w - tw) / 2, y + 3, Paleta.tintaPrincipal(), false);
    }
}
