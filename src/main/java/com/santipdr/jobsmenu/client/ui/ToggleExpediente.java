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

/** Interruptor Jobs con lectura fisica de estado, foco y confirmacion. */
public final class ToggleExpediente extends AbstractButton {

    private final Component etiqueta;
    private final BooleanSupplier leer;
    private final Consumer<Boolean> fijar;
    private final Function<Boolean, Component> textoValor;
    private boolean hoverPrevio;
    private boolean ultimoValor;
    private boolean tieneUltimoValor;
    private float focoSuave;
    private float estadoSuave;
    private long presionadoHasta;
    private long cambioHasta;

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
        this.estadoSuave = valor() ? 1.0F : 0.0F;
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
        long ahora = System.currentTimeMillis();
        this.presionadoHasta = ahora + 130L;
        this.cambioHasta = ahora + 320L;
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
        boolean v = valor();
        float destinoEstado = v ? 1.0F : 0.0F;
        if (ConfigTurno.movimientoReducido() || ConfigTurno.bajoConsumo()) {
            this.focoSuave = destino;
            this.estadoSuave = destinoEstado;
        } else {
            this.focoSuave += (destino - this.focoSuave) * 0.24F;
            this.estadoSuave += (destinoEstado - this.estadoSuave) * 0.28F;
        }

        int x = getX();
        int y = getY();
        int w = this.width;
        int h = this.height;
        long ahora = System.currentTimeMillis();
        boolean pulsado = this.active && ahora < this.presionadoHasta;
        float cambio = this.active && ahora < this.cambioHasta
                ? Math.max(0.0F, Math.min(1.0F, (this.cambioHasta - ahora) / 320.0F)) : 0.0F;

        int fondo = Paleta.mezclar(Paleta.papelAviso(), Paleta.UI_PAPEL_FOCO,
                0.14F + 0.62F * this.focoSuave);
        if (v) fondo = Paleta.mezclar(fondo, Paleta.UI_ACENTO, 0.025F + 0.035F * this.estadoSuave);
        if (!this.active) fondo = Paleta.mezclar(Paleta.VANO, fondo, 0.18F);
        g.fill(x, y, x + w, y + h, fondo);

        if (pulsado) {
            g.fill(x + 2, y + 2, x + w - 2, y + h - 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.18F));
        }
        if (cambio > 0.0F) {
            int avance = Math.max(4, Math.round((w - 8) * (1.0F - cambio)));
            g.fill(x + 4, y + h - 2, Math.min(x + w - 4, x + 4 + avance), y + h - 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.44F * cambio));
        }

        int borde = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.36F + 0.35F * this.focoSuave);
        g.fill(x, y, x + w, y + 1, borde);
        g.fill(x, y + h - 1, x + w, y + h, borde);
        g.fill(x, y, x + 1, y + h, Paleta.conAlfa(Paleta.tintaSecundaria(), 0.24F));
        g.fill(x + w - 1, y, x + w, y + h, Paleta.conAlfa(Paleta.tintaSecundaria(), 0.24F));
        g.fill(x + 3, y + 3, x + w - 3, y + 4,
                Paleta.conAlfa(Paleta.UI_PAPEL_FOCO, 0.28F));

        if (w >= 90) {
            int centro = x + w / 2;
            g.fill(centro - 7, y + 1, centro + 7, y + 2,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.08F + 0.08F * this.focoSuave));
        }

        int caja = Math.min(12, h - 6);
        int cx = x + 7;
        int cy = y + (h - caja) / 2;
        int tinta = Paleta.conAlfa(Paleta.tintaPrincipal(), 0.46F + 0.46F * this.estadoSuave);
        g.fill(cx, cy, cx + caja, cy + 1, tinta);
        g.fill(cx, cy + caja - 1, cx + caja, cy + caja, tinta);
        g.fill(cx, cy, cx + 1, cy + caja, tinta);
        g.fill(cx + caja - 1, cy, cx + caja, cy + caja, tinta);
        g.fill(cx + 2, cy + 2, cx + caja - 2, cy + caja - 2,
                Paleta.conAlfa(Paleta.mezclar(Paleta.VANO, Paleta.UI_ACENTO, this.estadoSuave),
                        0.06F + 0.17F * this.estadoSuave));

        if (this.estadoSuave > 0.08F) {
            int lleno = Math.max(1, Math.round((caja - 4) * this.estadoSuave));
            g.fill(cx + 2, cy + caja - 2 - lleno, cx + 4, cy + caja - 2,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.58F + 0.30F * this.estadoSuave));
            g.fill(cx + 4, cy + caja - 4, cx + caja - 2, cy + caja - 2,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.70F + 0.20F * this.estadoSuave));
            g.fill(cx + caja - 3, cy + 2, cx + caja - 1, cy + caja - 2,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.68F + 0.22F * this.estadoSuave));
        } else {
            g.fill(cx + 3, cy + caja / 2, cx + caja - 3, cy + caja / 2 + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.42F));
        }

        sincronizarMensaje(false);
        Font font = Minecraft.getInstance().font;
        String etiquetaTxt = this.etiqueta.getString();
        String valorTxt = this.textoValor.apply(v).getString();

        int pillPad = 7;
        int pillW = Math.min(Math.max(36, font.width(valorTxt) + pillPad * 2), Math.max(36, w / 3));
        int pillX = x + w - pillW - 6;
        int pillY = y + 4;
        int pillH = Math.max(10, h - 8);
        int pillBg = Paleta.conAlfa(Paleta.mezclar(Paleta.VANO, Paleta.UI_ACENTO, this.estadoSuave),
                0.09F + 0.20F * this.estadoSuave + 0.07F * this.focoSuave);
        g.fill(pillX, pillY, pillX + pillW, pillY + pillH, pillBg);
        g.fill(pillX, pillY, pillX + pillW, pillY + 1,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.32F));
        g.fill(pillX, pillY + pillH - 1, pillX + pillW, pillY + pillH,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.32F));
        g.fill(pillX - 4, y + 5, pillX - 3, y + h - 5,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.14F));

        int indicadorX = pillX + 4;
        int indicadorY = pillY + pillH / 2 - 1;
        g.fill(indicadorX, indicadorY, indicadorX + 4, indicadorY + 2,
                Paleta.conAlfa(v ? Paleta.UI_ACENTO_FUERTE : Paleta.tintaSecundaria(),
                        v ? 0.72F : 0.24F));

        int tx = cx + caja + 8;
        int maxEtiqueta = Math.max(8, pillX - tx - 7);
        if (font.width(etiquetaTxt) > maxEtiqueta) {
            etiquetaTxt = font.plainSubstrByWidth(etiquetaTxt,
                    Math.max(0, maxEtiqueta - font.width("..."))) + "...";
        }
        g.drawString(font, etiquetaTxt, tx, y + (h - font.lineHeight) / 2,
                this.active ? Paleta.tintaPrincipal() : Paleta.conAlfa(Paleta.tintaSecundaria(), 0.50F), false);

        int maxValor = Math.max(8, pillW - pillPad * 2 - 5);
        if (font.width(valorTxt) > maxValor) {
            valorTxt = font.plainSubstrByWidth(valorTxt,
                    Math.max(0, maxValor - font.width("..."))) + "...";
        }
        int vw = font.width(valorTxt);
        g.drawString(font, valorTxt, pillX + (pillW - vw) / 2 + 2,
                y + (h - font.lineHeight) / 2,
                Paleta.conAlfa(Paleta.tintaPrincipal(), 0.68F + 0.26F * this.estadoSuave), false);

        int railX0 = tx;
        int railX1 = Math.max(railX0 + 1, pillX - 8);
        if (railX1 > railX0 + 4) {
            g.fill(railX0, y + h - 4, railX1, y + h - 3,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.08F));
            int progreso = railX0 + Math.round((railX1 - railX0) * this.estadoSuave);
            g.fill(railX0, y + h - 4, progreso, y + h - 3,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.30F));
        }

        if (hover) {
            int c = Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : Paleta.UI_ACENTO,
                    teclado ? 0.86F : 0.58F);
            g.fill(x + 2, y + 2, x + 3, y + h - 2, c);
            g.fill(x + 5, y + h - 2,
                    x + Math.min(w - 6, 18 + Math.round((w - 24) * this.focoSuave)), y + h - 1,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.30F));
            if (teclado) {
                g.fill(x - 2, y + h / 2 - 2, x, y + h / 2 + 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.70F));
                g.fill(x + w, y + h / 2 - 2, x + w + 2, y + h / 2 + 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.42F));
            }
        }

        if (!this.active) {
            int medio = y + h / 2;
            g.fill(x + 4, medio, x + 8, medio + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.28F));
            g.fill(x + w - 10, medio, x + w - 6, medio + 1,
                    Paleta.conAlfa(Paleta.tintaSecundaria(), 0.18F));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
