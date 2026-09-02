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

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

/** Interruptor Jobs con lectura clara de estado, foco y accion. */
public final class ToggleExpediente extends AbstractButton {

    private final Component etiqueta;
    private final BooleanSupplier leer;
    private final Consumer<Boolean> fijar;
    private final Function<Boolean, Component> textoValor;
    private boolean hoverPrevio;
    private boolean ultimoValor;
    private boolean tieneUltimoValor;
    private float focoSuave;
    private long presionadoHasta;

    public ToggleExpediente(int x, int y, int ancho, int alto, Component etiqueta,
                            BooleanSupplier leer, Consumer<Boolean> fijar) {
        this(x, y, ancho, alto, etiqueta, leer, fijar,
                v -> Component.translatable(v ? "options.on" : "options.off"));
    }

    public ToggleExpediente(int x, int y, int ancho, int alto, Component etiqueta,
                            BooleanSupplier leer, Consumer<Boolean> fijar,
                            Function<Boolean, Component> textoValor) {
        super(x, y, ancho, alto, Component.empty());
        this.etiqueta = etiqueta;
        this.leer = leer;
        this.fijar = fijar;
        this.textoValor = textoValor != null ? textoValor
                : v -> Component.translatable(v ? "options.on" : "options.off");
        sincronizarMensaje(true);
    }

    private boolean valor() {
        try { return this.leer != null && this.leer.getAsBoolean(); }
        catch (Throwable ignored) { return false; }
    }

    private void sincronizarMensaje(boolean forzar) {
        boolean v = valor();
        if (!forzar && this.tieneUltimoValor && v == this.ultimoValor) return;
        this.ultimoValor = v;
        this.tieneUltimoValor = true;
        this.setMessage(this.etiqueta.copy().append(": ").append(this.textoValor.apply(v)));
    }

    @Override
    public void onPress() {
        this.presionadoHasta = System.currentTimeMillis() + 125L;
        boolean nuevo = !valor();
        if (this.fijar != null) this.fijar.accept(nuevo);
        sincronizarMensaje(true);
        PulidoInterfazJobs.confirmarCambio();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        MezclaAudio.gesto(SonidosNivel.UI_ALTERNAR, 0.52F);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean raton = this.active && this.isMouseOver(mouseX, mouseY);
        boolean teclado = this.active && this.isFocused() && !raton;
        boolean hover = raton || teclado;
        if (hover && !this.hoverPrevio) MezclaAudio.gesto(SonidosNivel.UI_PASAR, 0.22F);
        this.hoverPrevio = hover;

        float destino = hover ? 1.0F : 0.0F;
        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()) this.focoSuave = destino;
        else this.focoSuave += (destino - this.focoSuave) * 0.26F;

        int x = getX();
        int y = getY();
        int w = this.width;
        int h = this.height;
        boolean v = valor();
        boolean pulsado = this.active && System.currentTimeMillis() < this.presionadoHasta;

        int fondo = Paleta.mezclar(Paleta.papelAviso(), Paleta.UI_PAPEL_FOCO,
                0.14F + 0.62F * this.focoSuave);
        if (!this.active) fondo = Paleta.mezclar(Paleta.VANO, fondo, 0.18F);
        g.fill(x, y, x + w, y + h, fondo);
        if (pulsado) g.fill(x + 2, y + 2, x + w - 2, y + h - 2,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.17F));

        int borde = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.36F + 0.34F * this.focoSuave);
        g.fill(x, y, x + w, y + 1, borde);
        g.fill(x, y + h - 1, x + w, y + h, borde);
        g.fill(x, y, x + 1, y + h, Paleta.conAlfa(Paleta.tintaSecundaria(), 0.24F));
        g.fill(x + w - 1, y, x + w, y + h, Paleta.conAlfa(Paleta.tintaSecundaria(), 0.24F));
        g.fill(x + 3, y + 3, x + w - 3, y + 4, Paleta.conAlfa(Paleta.UI_PAPEL_FOCO, 0.28F));

        int caja = Math.min(11, h - 6);
        int cx = x + 7;
        int cy = y + (h - caja) / 2;
        int tinta = Paleta.conAlfa(Paleta.tintaPrincipal(), v ? 0.92F : 0.44F);
        g.fill(cx, cy, cx + caja, cy + 1, tinta);
        g.fill(cx, cy + caja - 1, cx + caja, cy + caja, tinta);
        g.fill(cx, cy, cx + 1, cy + caja, tinta);
        g.fill(cx + caja - 1, cy, cx + caja, cy + caja, tinta);
        g.fill(cx + 2, cy + 2, cx + caja - 2, cy + caja - 2,
                Paleta.conAlfa(v ? Paleta.UI_ACENTO : Paleta.VANO, v ? 0.18F : 0.05F));
        if (v) {
            g.fill(cx + 2, cy + caja / 2, cx + 4, cy + caja - 2, tinta);
            g.fill(cx + 4, cy + caja - 4, cx + caja - 2, cy + caja - 2, tinta);
            g.fill(cx + caja - 3, cy + 2, cx + caja - 1, cy + caja - 2, tinta);
        } else {
            g.fill(cx + 3, cy + caja / 2, cx + caja - 3, cy + caja / 2 + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.42F));
        }

        sincronizarMensaje(false);
        Font font = Minecraft.getInstance().font;
        String etiquetaTxt = this.etiqueta.getString();
        String valorTxt = this.textoValor.apply(v).getString();

        int pillPad = 7;
        int pillW = Math.min(Math.max(34, font.width(valorTxt) + pillPad * 2), Math.max(34, w / 3));
        int pillX = x + w - pillW - 6;
        int pillY = y + 4;
        int pillH = Math.max(10, h - 8);
        int pillBg = v
                ? Paleta.conAlfa(Paleta.UI_ACENTO, 0.24F + 0.14F * this.focoSuave)
                : Paleta.conAlfa(Paleta.VANO, 0.09F);
        g.fill(pillX, pillY, pillX + pillW, pillY + pillH, pillBg);
        g.fill(pillX, pillY, pillX + pillW, pillY + 1,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.32F));
        g.fill(pillX, pillY + pillH - 1, pillX + pillW, pillY + pillH,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.32F));
        g.fill(pillX - 4, y + 5, pillX - 3, y + h - 5,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.14F));

        int tx = cx + caja + 8;
        int maxEtiqueta = Math.max(8, pillX - tx - 7);
        if (font.width(etiquetaTxt) > maxEtiqueta) {
            etiquetaTxt = font.plainSubstrByWidth(etiquetaTxt,
                    Math.max(0, maxEtiqueta - font.width("..."))) + "...";
        }
        g.drawString(font, etiquetaTxt, tx, y + (h - font.lineHeight) / 2,
                this.active ? Paleta.tintaPrincipal() : Paleta.conAlfa(Paleta.tintaSecundaria(), 0.50F), false);

        int maxValor = Math.max(8, pillW - pillPad * 2);
        if (font.width(valorTxt) > maxValor) {
            valorTxt = font.plainSubstrByWidth(valorTxt,
                    Math.max(0, maxValor - font.width("..."))) + "...";
        }
        int vw = font.width(valorTxt);
        g.drawString(font, valorTxt, pillX + (pillW - vw) / 2,
                y + (h - font.lineHeight) / 2,
                Paleta.conAlfa(Paleta.tintaPrincipal(), v ? 0.94F : 0.68F), false);

        if (hover) {
            int c = Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO,
                    teclado ? 0.82F : 0.56F);
            g.fill(x + 2, y + 2, x + 3, y + h - 2, c);
            g.fill(x + 5, y + h - 2, x + Math.min(w - 6, 18 + Math.round((w - 24) * this.focoSuave)), y + h - 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.28F));
        }
        if (pulsado) {
            int centro = x + w / 2;
            g.fill(centro - 10, y + h - 3, centro + 10, y + h - 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.72F));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
