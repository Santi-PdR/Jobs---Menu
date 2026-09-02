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

/** Slider administrativo con lectura de escala, valor y foco mas clara. */
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
        boolean raton = this.active && this.isMouseOver(mouseX, mouseY);
        boolean teclado = this.active && this.isFocused() && !raton;
        boolean foco = raton || teclado;
        if (foco && !this.hoverPrevio) MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.18F);
        this.hoverPrevio = foco;
        float destino = foco ? 1.0F : 0.0F;
        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()) this.focoSuave = destino;
        else this.focoSuave += (destino - this.focoSuave) * 0.26F;

        int fondo = Paleta.mezclar(Paleta.papelAviso(), Paleta.UI_PAPEL_FOCO,
                0.16F + 0.58F * this.focoSuave);
        g.fill(x, y, x + w, y + h, fondo);
        int borde = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.40F + 0.34F * this.focoSuave);
        marco(g, x, y, w, h, borde);
        g.fill(x + 3, y + 3, x + w - 3, y + 4, Paleta.conAlfa(Paleta.UI_PAPEL_FOCO, 0.28F));

        int margen = 9;
        int barraY = y + h - 6;
        int barraX0 = x + margen;
        int barraX1 = x + w - margen;
        int largo = Math.max(1, barraX1 - barraX0);
        g.fill(barraX0, barraY, barraX1, barraY + 1,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.30F));
        g.fill(barraX0, barraY + 1, barraX1, barraY + 2,
                Paleta.conAlfa(Paleta.VANO, 0.08F));

        for (int i = 0; i <= 10; i++) {
            int tx = barraX0 + Math.round(largo * (i / 10.0F));
            boolean mayor = i == 0 || i == 5 || i == 10;
            int th = mayor ? 4 : 2;
            g.fill(tx, barraY - th, tx + 1, barraY,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), mayor ? 0.44F : 0.20F));
        }

        int knob = barraX0 + (int) Math.round(largo * this.value);
        g.fill(barraX0, barraY, knob, barraY + 2,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.64F + 0.22F * this.focoSuave));
        g.fill(barraX0, barraY - 1, knob, barraY,
                Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.18F + 0.18F * this.focoSuave));

        int knobH = Math.min(10, h - 4);
        int ky = barraY - knobH / 2;
        g.fill(knob - 3, ky, knob + 4, ky + knobH,
                Paleta.conAlfa(Paleta.tintaPrincipal(), 0.88F));
        g.fill(knob - 2, ky + 1, knob + 3, ky + knobH - 1,
                Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.94F));
        g.fill(knob, ky + 2, knob + 1, ky + knobH - 2,
                Paleta.conAlfa(Paleta.tintaPrincipal(), 0.34F));

        if (foco) {
            int c = Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO,
                    teclado ? 0.82F : 0.60F);
            g.fill(x + 3, y + 3, x + 4, y + h - 3, c);
            g.fill(x + w - 4, y + 3, x + w - 3, y + h - 3,
                    Paleta.conAlfa(c, teclado ? 0.70F : 0.34F));
            int guia = barraX0 + Math.max(1, Math.round(largo * this.focoSuave));
            g.fill(barraX0, y + h - 2, guia, y + h - 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.22F));
        }

        Font font = Minecraft.getInstance().font;
        String txt = getMessage().getString();
        int max = Math.max(8, w - 34);
        if (font.width(txt) > max) txt = font.plainSubstrByWidth(txt, Math.max(0, max - font.width("..."))) + "...";
        int tw = font.width(txt);
        g.drawString(font, txt, x + (w - tw) / 2, y + 3, Paleta.tintaPrincipal(), false);

        String porcentaje = Math.round(this.value * 100.0D) + "%";
        int pw = font.width(porcentaje);
        if (w >= 110) {
            g.drawString(font, porcentaje, x + w - pw - 7, y + 3,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.54F), false);
        }
    }

    private static void marco(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }
}
