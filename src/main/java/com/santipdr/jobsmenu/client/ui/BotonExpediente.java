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

/** Boton de expediente de segunda generacion: sobrio, legible y con estados claros. */
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
        this.presionadoHasta = System.currentTimeMillis() + 120L;
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
        boolean raton = this.active && this.isMouseOver(mouseX, mouseY);
        boolean teclado = this.active && this.isFocused() && !raton;
        boolean foco = raton || teclado;
        if (foco && !this.hoverPrevio) {
            MezclaAudio.gesto(SonidosNivel.UI_PASAR, this.tipo == Tipo.JOBS ? 0.31F : 0.25F);
        }
        this.hoverPrevio = foco;

        float destino = foco ? 1.0F : 0.0F;
        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()) this.focoSuave = destino;
        else this.focoSuave += (destino - this.focoSuave) * 0.26F;

        boolean pulsado = this.active && System.currentTimeMillis() < this.presionadoHasta;
        int x = this.getX();
        int y = this.getY() + (pulsado ? 1 : 0);
        int w = this.width;
        int h = this.height;

        int papelBase = Paleta.papelAviso();
        float mezclaFoco = this.tipo == Tipo.JOBS ? 0.76F : 0.54F;
        int papelFoco = Paleta.mezclar(papelBase, Paleta.UI_PAPEL_FOCO, mezclaFoco);
        int fondo = Paleta.mezclar(papelBase, papelFoco, this.focoSuave);
        if (this.tipo == Tipo.PRINCIPAL) fondo = Paleta.mezclar(fondo, Paleta.UI_ACENTO, 0.055F);
        if (this.tipo == Tipo.JOBS) fondo = Paleta.mezclar(fondo, Paleta.UI_ACENTO, 0.10F);
        if (!this.active) fondo = Paleta.mezclar(Paleta.VANO, papelBase, 0.72F);
        if (pulsado) fondo = Paleta.mezclar(fondo, Paleta.VANO, 0.16F);

        if (this.active && !pulsado) {
            int sombraH = this.tipo == Tipo.JOBS ? 3 : 2;
            g.fill(x + 2, y + h, x + w + 1, y + h + sombraH,
                    Paleta.conAlfa(Paleta.VANO, this.tipo == Tipo.JOBS ? 0.34F : 0.22F));
            if (foco) g.fill(x + 3, y + h + sombraH, x + w - 2, y + h + sombraH + 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.10F));
        }
        g.fill(x, y, x + w, y + h, fondo);

        int bordeBase = this.tipo == Tipo.TERMINAL || this.tipo == Tipo.JOBS
                ? Paleta.tintaPrincipal() : Paleta.tintaSecundaria();
        float bordeA = this.active ? 0.40F + this.focoSuave * 0.36F : 0.18F;
        if (this.tipo == Tipo.JOBS) bordeA = this.active ? 0.66F + this.focoSuave * 0.24F : 0.28F;
        marco(g, x, y, w, h, Paleta.conAlfa(bordeBase, bordeA));
        marcoInterior(g, x + 2, y + 2, w - 4, h - 4,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, this.active ? 0.07F + 0.08F * this.focoSuave : 0.04F));

        g.fill(x + 4, y + 3, x + w - 4, y + 4,
                Paleta.conAlfa(Paleta.UI_PAPEL_FOCO, this.active ? 0.34F : 0.12F));

        if (this.tipo == Tipo.PRINCIPAL && this.active) {
            g.fill(x + 3, y + 2, x + w - 3, y + 4,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.20F + 0.16F * this.focoSuave));
            g.fill(x + 5, y + h - 3, x + w - 5, y + h - 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.12F + 0.18F * this.focoSuave));
        } else if (this.tipo == Tipo.TERMINAL && this.active) {
            g.fill(x + 1, y + 1, x + 4, y + h - 1,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.66F));
            g.fill(x + 6, y + 4, x + 7, y + h - 4,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.24F));
        } else if (this.tipo == Tipo.JOBS && this.active) {
            dibujarMarcaJobs(g, x, y, w, h);
        }

        if (foco && this.active) {
            int c = Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO,
                    teclado ? 0.90F : 0.46F);
            g.fill(x - 1, y + 4, x, y + h - 4, c);
            g.fill(x + w, y + 4, x + w + 1, y + h - 4, Paleta.conAlfa(c, teclado ? 0.70F : 0.32F));
            int notch = Math.min(9, Math.max(4, w / 10));
            g.fill(x + 4, y - 1, x + 4 + notch, y, c);
            g.fill(x + w - 4 - notch, y + h, x + w - 4, y + h + 1, c);
        }

        Font font = Minecraft.getInstance().font;
        String texto = this.getMessage().getString();
        int reserva = this.tipo == Tipo.JOBS ? 38 : 20;
        int max = Math.max(8, w - reserva);
        boolean recortado = font.width(texto) > max;
        if (recortado) texto = font.plainSubstrByWidth(texto, Math.max(0, max - font.width("..."))) + "...";
        int tw = font.width(texto);
        int tx = this.tipo == Tipo.JOBS ? x + 29 : x + (w - tw) / 2;
        if (this.tipo == Tipo.JOBS) tx = Math.min(tx, x + w - tw - 8);
        int ty = y + (h - font.lineHeight) / 2;
        int tinta = this.active ? Paleta.tintaPrincipal()
                : Paleta.conAlfa(Paleta.tintaSecundaria(), 0.50F);
        g.drawString(font, texto, tx, ty, tinta, false);

        if (recortado && this.active) {
            g.fill(x + w - 8, ty + font.lineHeight / 2, x + w - 6, ty + font.lineHeight / 2 + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.38F));
        }

        if (this.focoSuave > 0.06F && this.active) {
            int uy = ty + font.lineHeight;
            int largo = Math.max(5, Math.round(tw * this.focoSuave));
            g.fill(tx, uy, Math.min(x + w - 7, tx + largo), uy + 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.34F + 0.44F * this.focoSuave));
        }

        if (!this.active) {
            g.fill(x + 5, y + h / 2, x + 9, y + h / 2 + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.30F));
        }
    }

    private void dibujarMarcaJobs(GuiGraphics g, int x, int y, int w, int h) {
        int tinta = Paleta.conAlfa(Paleta.tintaPrincipal(), 0.76F);
        int suave = Paleta.conAlfa(Paleta.UI_ACENTO, 0.26F + 0.24F * this.focoSuave);
        int bx = x + 7;
        int cy = y + h / 2;
        g.fill(bx, y + 4, bx + 1, y + h - 4, tinta);
        g.fill(bx + 4, y + 5, bx + 5, y + h - 5, suave);
        g.fill(bx + 8, cy - 3, bx + 14, cy - 2, tinta);
        g.fill(bx + 8, cy + 2, bx + 14, cy + 3, tinta);
        g.fill(bx + 8, cy - 3, bx + 9, cy + 3, tinta);
        g.fill(bx + 13, cy - 3, bx + 14, cy + 3, tinta);
        int avance = Math.max(4, Math.round((w - 36) * this.focoSuave));
        g.fill(x + 27, y + h - 3, Math.min(x + w - 5, x + 27 + avance), y + h - 2, suave);
    }

    private static void marco(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private static void marcoInterior(GuiGraphics g, int x, int y, int w, int h, int c) {
        if (w < 4 || h < 4) return;
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
