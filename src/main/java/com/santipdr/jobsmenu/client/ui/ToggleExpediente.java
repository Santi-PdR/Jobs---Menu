package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Interruptor de expediente que conserva un setter real, no estado duplicado. */
public final class ToggleExpediente extends AbstractButton {

    private final Component etiqueta;
    private final BooleanSupplier leer;
    private final Consumer<Boolean> fijar;
    private boolean hoverPrevio;

    public ToggleExpediente(int x, int y, int ancho, int alto, Component etiqueta,
                            BooleanSupplier leer, Consumer<Boolean> fijar) {
        super(x, y, ancho, alto, Component.empty());
        this.etiqueta = etiqueta;
        this.leer = leer;
        this.fijar = fijar;
        actualizarMensaje();
    }

    private boolean valor() {
        try { return this.leer != null && this.leer.getAsBoolean(); }
        catch (Throwable ignored) { return false; }
    }

    private void actualizarMensaje() {
        boolean v = valor();
        this.setMessage(this.etiqueta.copy().append(": ")
                .append(Component.translatable(v ? "options.on" : "options.off")));
    }

    @Override
    public void onPress() {
        boolean nuevo = !valor();
        if (this.fijar != null) this.fijar.accept(nuevo);
        actualizarMensaje();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        MezclaAudio.gesto(SonidosNivel.UI_ALTERNAR, 0.52F);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean hover = this.active && this.isHoveredOrFocused();
        if (hover && !this.hoverPrevio) MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.22F);
        this.hoverPrevio = hover;

        int x = getX();
        int y = getY();
        int w = this.width;
        int h = this.height;
        boolean v = valor();

        int fondo = Paleta.mezclar(Paleta.papelAviso(), Paleta.PARED_ALTA, hover ? 0.15F : 0.04F);
        g.fill(x, y, x + w, y + h, fondo);
        int borde = Paleta.conAlfa(Paleta.tintaSecundaria(), hover ? 0.66F : 0.34F);
        g.fill(x, y, x + w, y + 1, borde);
        g.fill(x, y + h - 1, x + w, y + h, borde);

        int caja = Math.min(10, h - 6);
        int cx = x + 6;
        int cy = y + (h - caja) / 2;
        int tinta = Paleta.conAlfa(Paleta.tintaPrincipal(), v ? 0.86F : 0.42F);
        g.fill(cx, cy, cx + caja, cy + 1, tinta);
        g.fill(cx, cy + caja - 1, cx + caja, cy + caja, tinta);
        g.fill(cx, cy, cx + 1, cy + caja, tinta);
        g.fill(cx + caja - 1, cy, cx + caja, cy + caja, tinta);
        if (v) {
            g.fill(cx + 2, cy + caja / 2, cx + 4, cy + caja - 2, tinta);
            g.fill(cx + 4, cy + caja - 4, cx + caja - 2, cy + caja - 2, tinta);
            g.fill(cx + caja - 3, cy + 2, cx + caja - 1, cy + caja - 2, tinta);
        }

        actualizarMensaje();
        Font font = Minecraft.getInstance().font;
        String txt = getMessage().getString();
        int tx = cx + caja + 7;
        int max = Math.max(8, x + w - 6 - tx);
        if (font.width(txt) > max) {
            txt = font.plainSubstrByWidth(txt, Math.max(0, max - font.width("..."))) + "...";
        }
        g.drawString(font, txt, tx, y + (h - font.lineHeight) / 2,
                this.active ? Paleta.tintaPrincipal() : Paleta.conAlfa(Paleta.tintaSecundaria(), 0.55F), false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
