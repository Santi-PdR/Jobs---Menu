package com.santipdr.jobsmenu.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

/**
 * Capa visual para dialogos vanilla que deben conservar su logica original.
 * Se dibuja despues de Minecraft: no reemplaza hitboxes, listeners ni estado.
 */
public final class PielVanillaJobs {

    private PielVanillaJobs() {
    }

    public static void dibujar(Screen pantalla, GuiGraphics g, int mouseX, int mouseY) {
        if (pantalla == null || g == null) return;
        Font font = Minecraft.getInstance().font;

        for (var child : pantalla.children()) {
            if (child.getClass().getName().startsWith("com.santipdr.jobsmenu.")) continue;
            if (child instanceof AbstractButton boton && !(child instanceof AbstractSliderButton)) {
                dibujarBoton(g, font, boton, mouseX, mouseY);
            } else if (child instanceof AbstractSliderButton slider) {
                dibujarSlider(g, slider, mouseX, mouseY);
            } else if (child instanceof EditBox campo) {
                dibujarCampo(g, campo);
            }
        }
    }

    private static void dibujarSlider(GuiGraphics g, AbstractSliderButton slider,
                                      int mouseX, int mouseY) {
        if (!slider.visible) return;
        int x = slider.getX();
        int y = slider.getY();
        int w = slider.getWidth();
        int h = slider.getHeight();
        boolean foco = slider.active && (slider.isMouseOver(mouseX, mouseY) || slider.isFocused());
        int c = Paleta.conAlfa(Paleta.tintaSecundaria(), foco ? 0.72F : 0.36F);
        marco(g, x, y, w, h, c);
        int by = y + h - 4;
        g.fill(x + 7, by, x + w - 7, by + 1,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.34F));
        for (int i = 0; i <= 4; i++) {
            int tx = x + 7 + Math.round((w - 14) * (i / 4.0F));
            g.fill(tx, by - 2, tx + 1, by + 2,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), foco ? 0.42F : 0.22F));
        }
    }

    private static void dibujarBoton(GuiGraphics g, Font font, AbstractButton boton,
                                     int mouseX, int mouseY) {
        if (!boton.visible) return;

        int x = boton.getX();
        int y = boton.getY();
        int w = boton.getWidth();
        int h = boton.getHeight();
        if (w <= 2 || h <= 2) return;

        boolean foco = boton.active && (boton.isMouseOver(mouseX, mouseY) || boton.isFocused());
        int papel = Paleta.mezclar(Paleta.papelAviso(), Paleta.PARED_ALTA, foco ? 0.18F : 0.04F);
        if (!boton.active) papel = Paleta.mezclar(Paleta.VANO, Paleta.papelAviso(), 0.70F);

        // Cubre completamente la textura vanilla sin tocar el widget real.
        g.fill(x, y, x + w, y + h, papel);
        int borde = Paleta.conAlfa(Paleta.tintaSecundaria(),
                boton.active ? (foco ? 0.74F : 0.42F) : 0.20F);
        marco(g, x, y, w, h, borde);

        g.fill(x + 4, y + 3, x + 5, y + h - 3,
                Paleta.conAlfa(Paleta.tintaPrincipal(), foco ? 0.52F : 0.18F));
        g.fill(x + w - 8, y + h - 3, x + w - 4, y + h - 2,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.24F));

        String texto = boton.getMessage().getString();
        int max = Math.max(8, w - 18);
        if (font.width(texto) > max) {
            texto = font.plainSubstrByWidth(texto,
                    Math.max(0, max - font.width("..."))) + "...";
        }
        int tw = font.width(texto);
        int tinta = boton.active ? Paleta.tintaPrincipal()
                : Paleta.conAlfa(Paleta.tintaSecundaria(), 0.56F);
        g.drawString(font, texto, x + (w - tw) / 2,
                y + (h - font.lineHeight) / 2, tinta, false);

        if (foco) {
            int u = Math.max(5, Math.min(tw, w - 18));
            int ux = x + (w - u) / 2;
            int uy = y + (h + font.lineHeight) / 2;
            g.fill(ux, uy, ux + u, uy + 1,
                    Paleta.conAlfa(Paleta.tintaPrincipal(), 0.42F));
        }
    }

    private static void dibujarCampo(GuiGraphics g, EditBox campo) {
        if (!campo.isVisible()) return;
        int x = campo.getX();
        int y = campo.getY();
        int w = campo.getWidth();
        int h = campo.getHeight();
        if (w <= 2 || h <= 2) return;

        int borde = Paleta.conAlfa(Paleta.tintaSecundaria(), campo.isFocused() ? 0.82F : 0.46F);
        marco(g, x - 1, y - 1, w + 2, h + 2, borde);

        // Esquinas de ficha: no cubren el texto ni el cursor de Minecraft.
        int marca = Paleta.conAlfa(Paleta.tintaPrincipal(), campo.isFocused() ? 0.52F : 0.24F);
        g.fill(x + 2, y + 2, x + 8, y + 3, marca);
        g.fill(x + 2, y + 2, x + 3, y + 7, marca);
        g.fill(x + w - 8, y + h - 3, x + w - 2, y + h - 2, marca);
        g.fill(x + w - 3, y + h - 7, x + w - 2, y + h - 2, marca);
    }

    private static void marco(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }
}
