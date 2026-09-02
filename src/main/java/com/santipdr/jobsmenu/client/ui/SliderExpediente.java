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

/** Slider administrativo para valores enteros. */
public final class SliderExpediente extends AbstractSliderButton {

    private final int minimo;
    private final int maximo;
    private final IntConsumer fijar;
    private final IntFunction<Component> rotulo;
    private int ultimoAplicado;
    private long ultimoSonido;
    private float focoSuave;
    private boolean hoverPrevio;

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
        boolean foco = this.active && this.isHoveredOrFocused();
        if (foco && !this.hoverPrevio) MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.18F);
        this.hoverPrevio = foco;
        float destino = foco ? 1.0F : 0.0F;
        if (ConfigTurno.movimientoReducido()) this.focoSuave = destino;
        else this.focoSuave += (destino - this.focoSuave) * 0.24F;

        int fondo = Paleta.mezclar(Paleta.papelAviso(), Paleta.UI_PAPEL_FOCO,
                0.14F + 0.58F * this.focoSuave);
        g.fill(x, y, x + w, y + h, fondo);
        int borde = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.38F + 0.34F * this.focoSuave);
        marco(g, x, y, w, h, borde);

        int margen = 8;
        int barraY = y + h - 6;
        int barraX0 = x + margen;
        int barraX1 = x + w - margen;
        int largo = Math.max(1, barraX1 - barraX0);

        g.fill(barraX0, barraY, barraX1, barraY + 1,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.24F));

        for (int i = 0; i <= 8; i++) {
            int tx = barraX0 + Math.round(largo * (i / 8.0F));
            int th = (i % 4 == 0) ? 3 : 2;
            g.fill(tx, barraY - th, tx + 1, barraY,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), i % 4 == 0 ? 0.34F : 0.18F));
        }

        int knob = barraX0 + (int) Math.round(largo * this.value);
        g.fill(barraX0, barraY, knob, barraY + 2,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.58F + 0.24F * this.focoSuave));

        int knobH = Math.min(8, h - 5);
        int ky = barraY - knobH / 2;
        g.fill(knob - 2, ky, knob + 3, ky + knobH,
                Paleta.conAlfa(Paleta.tintaPrincipal(), 0.82F));
        g.fill(knob - 1, ky + 1, knob + 2, ky + knobH - 1,
                Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.90F));

        if (foco) {
            g.fill(x + 4, y + 3, x + 5, y + h - 3,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.58F));
        }

        Font font = Minecraft.getInstance().font;
        String txt = getMessage().getString();
        int max = Math.max(8, w - 18);
        if (font.width(txt) > max) {
            txt = font.plainSubstrByWidth(txt, Math.max(0, max - font.width("..."))) + "...";
        }
        int tw = font.width(txt);
        g.drawString(font, txt, x + (w - tw) / 2, y + 3, Paleta.tintaPrincipal(), false);
    }

    private static void marco(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }
}
