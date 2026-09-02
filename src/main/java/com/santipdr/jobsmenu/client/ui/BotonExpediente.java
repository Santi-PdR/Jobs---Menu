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

/** Boton Jobs con jerarquia fisica, foco inequivoco y respuesta contenida. */
public class BotonExpediente extends AbstractButton {

    public enum Tipo { NORMAL, PRINCIPAL, JOBS, TERMINAL }

    private final Runnable accion;
    private final Tipo tipo;
    private boolean hoverPrevio;
    private float focoSuave;
    private long presionadoHasta;
    private long confirmadoHasta;

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
        long ahora = System.currentTimeMillis();
        this.presionadoHasta = ahora + 135L;
        this.confirmadoHasta = ahora + 260L;
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
        else this.focoSuave += (destino - this.focoSuave) * 0.24F;

        long ahora = System.currentTimeMillis();
        boolean pulsado = this.active && ahora < this.presionadoHasta;
        float confirmacion = this.active && ahora < this.confirmadoHasta
                ? Math.max(0.0F, Math.min(1.0F, (this.confirmadoHasta - ahora) / 260.0F)) : 0.0F;

        int x = this.getX();
        int yBase = this.getY();
        int y = yBase + (pulsado ? 1 : 0);
        int w = this.width;
        int h = this.height;

        int papelBase = Paleta.papelAviso();
        float mezclaFoco = this.tipo == Tipo.JOBS ? 0.80F : 0.56F;
        int papelFoco = Paleta.mezclar(papelBase, Paleta.UI_PAPEL_FOCO, mezclaFoco);
        int fondo = Paleta.mezclar(papelBase, papelFoco, this.focoSuave);
        if (this.tipo == Tipo.PRINCIPAL) fondo = Paleta.mezclar(fondo, Paleta.UI_ACENTO, 0.060F);
        if (this.tipo == Tipo.JOBS) fondo = Paleta.mezclar(fondo, Paleta.UI_ACENTO, 0.115F);
        if (this.tipo == Tipo.TERMINAL) fondo = Paleta.mezclar(fondo, Paleta.VANO, 0.035F);
        if (!this.active) fondo = Paleta.mezclar(Paleta.VANO, papelBase, 0.70F);
        if (pulsado) fondo = Paleta.mezclar(fondo, Paleta.VANO, 0.18F);

        if (this.active && !pulsado) {
            int sombraH = this.tipo == Tipo.JOBS ? 3 : 2;
            g.fill(x + 2, y + h, x + w + 1, y + h + sombraH,
                    Paleta.conAlfa(Paleta.VANO, this.tipo == Tipo.JOBS ? 0.36F : 0.23F));
            g.fill(x + 4, y + h + sombraH, x + w - 3, y + h + sombraH + 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.05F + 0.08F * this.focoSuave));
        }
        g.fill(x, y, x + w, y + h, fondo);

        if (this.active && this.focoSuave > 0.02F) {
            g.fill(x + 2, y + 2, x + w - 2, y + h - 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.025F + 0.045F * this.focoSuave));
        }

        int bordeBase = this.tipo == Tipo.TERMINAL || this.tipo == Tipo.JOBS
                ? Paleta.tintaPrincipal() : Paleta.tintaSecundaria();
        float bordeA = this.active ? 0.40F + this.focoSuave * 0.36F : 0.18F;
        if (this.tipo == Tipo.JOBS) bordeA = this.active ? 0.66F + this.focoSuave * 0.25F : 0.28F;
        if (this.tipo == Tipo.TERMINAL) bordeA += 0.06F;
        marco(g, x, y, w, h, Paleta.conAlfa(bordeBase, Math.min(0.96F, bordeA)));
        marcoInterior(g, x + 2, y + 2, w - 4, h - 4,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, this.active ? 0.07F + 0.09F * this.focoSuave : 0.04F));

        g.fill(x + 4, y + 3, x + w - 4, y + 4,
                Paleta.conAlfa(Paleta.UI_PAPEL_FOCO, this.active ? 0.34F : 0.12F));
        if (w >= 74) {
            int centro = x + w / 2;
            g.fill(centro - 8, y + 1, centro + 8, y + 2,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.09F + 0.11F * this.focoSuave));
        }

        if (this.tipo == Tipo.PRINCIPAL && this.active) {
            g.fill(x + 3, y + 2, x + w - 3, y + 4,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.20F + 0.17F * this.focoSuave));
            g.fill(x + 5, y + h - 3, x + w - 5, y + h - 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.13F + 0.19F * this.focoSuave));
            g.fill(x + 3, y + h / 2 - 2, x + 5, y + h / 2 + 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.20F + 0.28F * this.focoSuave));
        } else if (this.tipo == Tipo.TERMINAL && this.active) {
            g.fill(x + 1, y + 1, x + 4, y + h - 1,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.68F));
            g.fill(x + 6, y + 4, x + 7, y + h - 4,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.26F));
            g.fill(x + w - 6, y + 4, x + w - 5, y + h - 4,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.18F));
        } else if (this.tipo == Tipo.JOBS && this.active) {
            dibujarMarcaJobs(g, x, y, w, h);
        }

        if (foco && this.active) {
            int c = Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO,
                    teclado ? 0.92F : 0.48F);
            g.fill(x - 1, y + 4, x, y + h - 4, c);
            g.fill(x + w, y + 4, x + w + 1, y + h - 4,
                    Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO,
                            teclado ? 0.68F : 0.28F));
            int notch = Math.min(10, Math.max(4, w / 10));
            g.fill(x + 4, y - 1, x + 4 + notch, y, c);
            g.fill(x + w - 4 - notch, y + h, x + w - 4, y + h + 1, c);
            if (teclado && w > 44) {
                int cx = x + w / 2;
                g.fill(cx - 2, y - 2, cx + 3, y - 1,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.58F));
                g.fill(cx, y - 4, cx + 1, y - 1,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.42F));
            }
        }

        if (confirmacion > 0.0F && this.active) {
            int cx = x + w / 2;
            int largo = Math.max(6, Math.round((w - 12) * (1.0F - confirmacion)));
            int x0 = Math.max(x + 6, cx - largo / 2);
            int x1 = Math.min(x + w - 6, cx + largo / 2);
            g.fill(x0, y + h - 2, x1, y + h - 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.56F * confirmacion));
        }

        Font font = Minecraft.getInstance().font;
        String texto = this.getMessage().getString();
        int reserva = this.tipo == Tipo.JOBS ? 42 : 22;
        int max = Math.max(8, w - reserva);
        boolean recortado = font.width(texto) > max;
        if (recortado) texto = font.plainSubstrByWidth(texto,
                Math.max(0, max - font.width("..."))) + "...";
        int tw = font.width(texto);
        int tx = this.tipo == Tipo.JOBS ? x + 30 : x + (w - tw) / 2;
        if (this.tipo == Tipo.JOBS) tx = Math.min(tx, x + w - tw - 9);
        int ty = y + (h - font.lineHeight) / 2;
        int tinta = this.active ? Paleta.tintaPrincipal()
                : Paleta.conAlfa(Paleta.tintaSecundaria(), 0.50F);
        g.drawString(font, texto, tx, ty, tinta, false);

        if (recortado && this.active) {
            int my = ty + font.lineHeight / 2;
            g.fill(x + w - 9, my, x + w - 7, my + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.42F));
            g.fill(x + w - 6, my, x + w - 5, my + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.25F));
        }

        if (this.focoSuave > 0.06F && this.active) {
            int uy = ty + font.lineHeight;
            int largo = Math.max(5, Math.round(tw * this.focoSuave));
            g.fill(tx, uy, Math.min(x + w - 7, tx + largo), uy + 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.34F + 0.44F * this.focoSuave));
        }

        if (!this.active) {
            int cy = y + h / 2;
            g.fill(x + 5, cy, x + 10, cy + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.32F));
            g.fill(x + w - 10, cy, x + w - 5, cy + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.20F));
        }
    }

    private void dibujarMarcaJobs(GuiGraphics g, int x, int y, int w, int h) {
        int tinta = Paleta.conAlfa(Paleta.tintaPrincipal(), 0.78F);
        int suave = Paleta.conAlfa(Paleta.UI_ACENTO, 0.27F + 0.25F * this.focoSuave);
        int bx = x + 7;
        int cy = y + h / 2;
        g.fill(bx, y + 4, bx + 1, y + h - 4, tinta);
        g.fill(bx + 4, y + 5, bx + 5, y + h - 5, suave);
        g.fill(bx + 8, cy - 3, bx + 14, cy - 2, tinta);
        g.fill(bx + 8, cy + 2, bx + 14, cy + 3, tinta);
        g.fill(bx + 8, cy - 3, bx + 9, cy + 3, tinta);
        g.fill(bx + 13, cy - 3, bx + 14, cy + 3, tinta);
        g.fill(bx + 10, cy - 1, bx + 12, cy + 1,
                Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.36F + 0.34F * this.focoSuave));
        int avance = Math.max(4, Math.round((w - 38) * this.focoSuave));
        g.fill(x + 28, y + h - 3, Math.min(x + w - 5, x + 28 + avance), y + h - 2, suave);
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
