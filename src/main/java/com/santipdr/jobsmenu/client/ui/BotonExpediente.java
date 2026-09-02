package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

/** Boton de expediente: papel frio, tinta y feedback del edificio, sin skin vanilla. */
public class BotonExpediente extends AbstractButton {

    public enum Tipo { NORMAL, PRINCIPAL, JOBS, TERMINAL }

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
        this.presionadoHasta = System.currentTimeMillis() + 105L;
        if (this.accion != null) this.accion.run();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        MezclaAudio.gesto(this.tipo == Tipo.TERMINAL
                ? SonidosNivel.UI_CONFIRMAR : SonidosNivel.UI_ELEGIR,
                this.tipo == Tipo.JOBS ? 0.82F : (this.tipo == Tipo.PRINCIPAL ? 0.76F : 0.62F));
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean foco = this.active && this.isHoveredOrFocused();
        if (foco && !this.hoverPrevio) {
            MezclaAudio.gesto(SonidosNivel.UI_PASAR, this.tipo == Tipo.JOBS ? 0.31F : 0.25F);
        }
        this.hoverPrevio = foco;

        float destino = foco ? 1.0F : 0.0F;
        if (ConfigTurno.movimientoReducido()) {
            this.focoSuave = destino;
        } else {
            this.focoSuave += (destino - this.focoSuave) * 0.24F;
        }

        boolean pulsado = this.active && System.currentTimeMillis() < this.presionadoHasta;
        int x = this.getX();
        int y = this.getY() + (pulsado ? 1 : 0);
        int w = this.width;
        int h = this.height;

        int papelBase = Paleta.papelAviso();
        float mezclaFoco = this.tipo == Tipo.JOBS ? 0.72F : 0.48F;
        int papelFoco = Paleta.mezclar(papelBase, Paleta.UI_PAPEL_FOCO, mezclaFoco);
        int fondo = Paleta.mezclar(papelBase, papelFoco, this.focoSuave);
        if (this.tipo == Tipo.JOBS) fondo = Paleta.mezclar(fondo, Paleta.UI_ACENTO, 0.08F);
        if (!this.active) fondo = Paleta.mezclar(Paleta.VANO, papelBase, 0.68F);
        if (pulsado) fondo = Paleta.mezclar(fondo, Paleta.VANO, 0.12F);

        if (this.active && !pulsado) {
            int sombraH = this.tipo == Tipo.JOBS ? 3 : 2;
            g.fill(x + 2, y + h, x + w + 1, y + h + sombraH,
                    Paleta.conAlfa(Paleta.VANO, this.tipo == Tipo.JOBS ? 0.30F : 0.20F));
        }
        g.fill(x, y, x + w, y + h, fondo);

        int borde = this.tipo == Tipo.TERMINAL || this.tipo == Tipo.JOBS
                ? Paleta.tintaPrincipal() : Paleta.tintaSecundaria();
        float bordeA = this.active ? 0.42F + this.focoSuave * 0.34F : 0.20F;
        if (this.tipo == Tipo.JOBS) bordeA = this.active ? 0.64F + this.focoSuave * 0.22F : 0.28F;
        marco(g, x, y, w, h, Paleta.conAlfa(borde, bordeA));

        if (this.tipo == Tipo.PRINCIPAL && this.active) {
            g.fill(x + 3, y + 3, x + w - 3, y + 4,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.18F + 0.14F * this.focoSuave));
        } else if (this.tipo == Tipo.TERMINAL && this.active) {
            g.fill(x + 1, y + 1, x + 4, y + h - 1,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.64F));
        } else if (this.tipo == Tipo.JOBS && this.active) {
            dibujarMarcaJobs(g, x, y, w, h);
        }

        if (this.isFocused() && !this.isMouseOver(mouseX, mouseY) && this.active) {
            int c = Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.86F);
            marco(g, x - 1, y - 1, w + 2, h + 2, c);
        }

        Font font = Minecraft.getInstance().font;
        String texto = this.getMessage().getString();
        int reserva = this.tipo == Tipo.JOBS ? 34 : 18;
        int max = Math.max(8, w - reserva);
        if (font.width(texto) > max) {
            String puntos = "...";
            texto = font.plainSubstrByWidth(texto, Math.max(0, max - font.width(puntos))) + puntos;
        }
        int tw = font.width(texto);
        int tx = this.tipo == Tipo.JOBS ? x + 27 : x + (w - tw) / 2;
        if (this.tipo == Tipo.JOBS) tx = Math.min(tx, x + w - tw - 8);
        int ty = y + (h - font.lineHeight) / 2;
        int tinta = this.active ? Paleta.tintaPrincipal()
                : Paleta.conAlfa(Paleta.tintaSecundaria(), 0.55F);
        g.drawString(font, texto, tx, ty, tinta, false);

        if (this.focoSuave > 0.08F && this.active) {
            int uy = ty + font.lineHeight;
            int largo = Math.max(4, Math.round(tw * this.focoSuave));
            g.fill(tx, uy, tx + largo, uy + 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.30F + 0.42F * this.focoSuave));
        }
    }

    private void dibujarMarcaJobs(GuiGraphics g, int x, int y, int w, int h) {
        int tinta = Paleta.conAlfa(Paleta.tintaPrincipal(), 0.72F);
        int suave = Paleta.conAlfa(Paleta.UI_ACENTO, 0.24F + 0.22F * this.focoSuave);
        int bx = x + 7;
        int cy = y + h / 2;

        g.fill(bx, y + 4, bx + 1, y + h - 4, tinta);
        g.fill(bx + 4, y + 5, bx + 5, y + h - 5, suave);
        g.fill(bx + 8, cy - 3, bx + 13, cy - 2, tinta);
        g.fill(bx + 8, cy + 2, bx + 13, cy + 3, tinta);
        g.fill(bx + 8, cy - 3, bx + 9, cy + 3, tinta);
        g.fill(bx + 12, cy - 3, bx + 13, cy + 3, tinta);

        int avance = Math.max(4, Math.round((w - 34) * this.focoSuave));
        g.fill(x + 25, y + h - 3, Math.min(x + w - 5, x + 25 + avance), y + h - 2, suave);
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
