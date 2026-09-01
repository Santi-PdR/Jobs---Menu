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

/** Boton de expediente: papel, tinta y feedback del edificio, sin skin vanilla. */
public class BotonExpediente extends AbstractButton {

    public enum Tipo { NORMAL, PRINCIPAL, TERMINAL }

    private final Runnable accion;
    private final Tipo tipo;
    private boolean hoverPrevio;
    private float focoSuave;
    private long presionadoHasta;

    public BotonExpediente(int x, int y, int ancho, int alto, Component texto,
                           Tipo tipo, Runnable accion) {
        super(x, y, ancho, alto, texto);
        this.tipo = tipo == null ? Tipo.NORMAL : tipo;
        this.accion = accion;
    }

    public BotonExpediente(int x, int y, int ancho, int alto, Component texto, Runnable accion) {
        this(x, y, ancho, alto, texto, Tipo.NORMAL, accion);
    }

    @Override
    public void onPress() {
        this.presionadoHasta = System.currentTimeMillis() + 110L;
        if (this.accion != null) this.accion.run();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        MezclaAudio.gesto(this.tipo == Tipo.TERMINAL
                ? SonidosNivel.UI_CONFIRMAR : SonidosNivel.UI_ELEGIR,
                this.tipo == Tipo.PRINCIPAL ? 0.78F : 0.64F);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean foco = this.active && this.isHoveredOrFocused();
        if (foco && !this.hoverPrevio) {
            MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.27F);
        }
        this.hoverPrevio = foco;
        this.focoSuave += ((foco ? 1.0F : 0.0F) - this.focoSuave) * 0.22F;

        boolean pulsado = this.active && System.currentTimeMillis() < this.presionadoHasta;
        int x = this.getX();
        int y = this.getY() + (pulsado ? 1 : 0);
        int w = this.width;
        int h = this.height;

        int papelBase = Paleta.papelAviso();
        int papelFoco = Paleta.mezclar(papelBase, Paleta.PARED_ALTA, 0.18F);
        int fondo = Paleta.mezclar(papelBase, papelFoco, this.focoSuave);
        if (!this.active) fondo = Paleta.mezclar(Paleta.VANO, papelBase, 0.68F);
        if (pulsado) fondo = Paleta.mezclar(fondo, Paleta.VANO, 0.12F);

        if (this.active && !pulsado) {
            g.fill(x + 2, y + h, x + w + 1, y + h + 2,
                    Paleta.conAlfa(Paleta.VANO, 0.22F));
        }
        g.fill(x, y, x + w, y + h, fondo);

        int borde = this.tipo == Tipo.TERMINAL
                ? Paleta.tintaPrincipal() : Paleta.tintaSecundaria();
        float bordeA = this.active ? 0.42F + this.focoSuave * 0.34F : 0.20F;
        marco(g, x, y, w, h, Paleta.conAlfa(borde, bordeA));

        // Linea de formulario: el principal lleva doble regla; terminal queda
        // oscuro, nunca rojo (el rojo del mod pertenece a los Executores).
        if (this.tipo == Tipo.PRINCIPAL && this.active) {
            g.fill(x + 3, y + 3, x + w - 3, y + 4,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.18F + 0.12F * this.focoSuave));
        } else if (this.tipo == Tipo.TERMINAL && this.active) {
            g.fill(x + 1, y + 1, x + 4, y + h - 1,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.64F));
        }

        if (this.isFocused() && !this.isMouseOver(mouseX, mouseY) && this.active) {
            int c = Paleta.conAlfa(Paleta.PARED_ALTA, 0.80F);
            marco(g, x - 1, y - 1, w + 2, h + 2, c);
        }

        Font font = Minecraft.getInstance().font;
        String texto = this.getMessage().getString();
        int max = Math.max(8, w - 18);
        if (font.width(texto) > max) {
            String puntos = "...";
            texto = font.plainSubstrByWidth(texto, Math.max(0, max - font.width(puntos))) + puntos;
        }
        int tw = font.width(texto);
        int tx = x + (w - tw) / 2;
        int ty = y + (h - font.lineHeight) / 2;
        int tinta = this.active ? Paleta.tintaPrincipal()
                : Paleta.conAlfa(Paleta.tintaSecundaria(), 0.55F);
        g.drawString(font, texto, tx, ty, tinta, false);

        if (this.focoSuave > 0.08F && this.active) {
            int uy = ty + font.lineHeight;
            int largo = Math.max(4, Math.round(tw * this.focoSuave));
            g.fill(tx, uy, tx + largo, uy + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.28F + 0.40F * this.focoSuave));
        }
    }

    private static void marco(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
