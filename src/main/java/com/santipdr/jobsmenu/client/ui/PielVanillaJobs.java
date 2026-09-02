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
 *
 * El estilo se decide por superficie. Un archivo oscuro no puede heredar el
 * papel claro ni, mucho menos, los amarillos ambientales del recinto.
 */
public final class PielVanillaJobs {

    private PielVanillaJobs() {
    }

    public static void dibujar(Screen pantalla, GuiGraphics g, int mouseX, int mouseY) {
        if (pantalla == null || g == null) return;
        Font font = Minecraft.getInstance().font;
        boolean archivo = esArchivoOscuro(pantalla);

        for (var child : pantalla.children()) {
            if (child.getClass().getName().startsWith("com.santipdr.jobsmenu.")) continue;
            if (child instanceof AbstractButton boton && !(child instanceof AbstractSliderButton)) {
                dibujarBoton(g, font, boton, mouseX, mouseY, archivo);
            } else if (child instanceof AbstractSliderButton slider) {
                dibujarSlider(g, slider, mouseX, mouseY, archivo);
            } else if (child instanceof EditBox campo) {
                dibujarCampo(g, campo, archivo);
            }
        }
    }

    private static boolean esArchivoOscuro(Screen pantalla) {
        String clase = pantalla.getClass().getName();
        return clase.endsWith("PantallaMundosJobs")
                || clase.endsWith("PantallaMultijugadorJobs")
                || clase.endsWith("PantallaModsJobs")
                || clase.endsWith("PantallaPaquetesJobs");
    }

    private static void dibujarSlider(GuiGraphics g, AbstractSliderButton slider,
                                      int mouseX, int mouseY, boolean archivo) {
        if (!slider.visible) return;
        int x = slider.getX();
        int y = slider.getY();
        int w = slider.getWidth();
        int h = slider.getHeight();
        boolean foco = slider.active && (slider.isMouseOver(mouseX, mouseY) || slider.isFocused());
        int base = archivo ? Paleta.ARCHIVO_ACENTO : Paleta.UI_TINTA_TENUE;
        int c = Paleta.conAlfa(base, foco ? 0.72F : 0.36F);
        marco(g, x, y, w, h, c);
        int by = y + h - 4;
        g.fill(x + 7, by, x + w - 7, by + 1,
                Paleta.conAlfa(base, 0.34F));
        for (int i = 0; i <= 4; i++) {
            int tx = x + 7 + Math.round((w - 14) * (i / 4.0F));
            g.fill(tx, by - 2, tx + 1, by + 2,
                    Paleta.conAlfa(archivo ? Paleta.ARCHIVO_TEXTO : Paleta.UI_TINTA,
                            foco ? 0.42F : 0.22F));
        }
    }

    private static void dibujarBoton(GuiGraphics g, Font font, AbstractButton boton,
                                     int mouseX, int mouseY, boolean archivo) {
        if (!boton.visible) return;

        int x = boton.getX();
        int y = boton.getY();
        int w = boton.getWidth();
        int h = boton.getHeight();
        if (w <= 2 || h <= 2) return;

        boolean foco = boton.active && (boton.isMouseOver(mouseX, mouseY) || boton.isFocused());
        int fondo;
        int bordeBase;
        int tinta;
        int tintaTenue;

        if (archivo) {
            fondo = foco ? Paleta.ARCHIVO_SUPERFICIE_FOCO : Paleta.ARCHIVO_SUPERFICIE;
            if (!boton.active) fondo = Paleta.mezclar(Paleta.ARCHIVO_FONDO, fondo, 0.52F);
            bordeBase = Paleta.ARCHIVO_ACENTO;
            tinta = Paleta.ARCHIVO_TEXTO;
            tintaTenue = Paleta.ARCHIVO_TEXTO_TENUE;
        } else {
            fondo = Paleta.mezclar(Paleta.papelAviso(), Paleta.UI_PAPEL_FOCO,
                    foco ? 0.76F : 0.16F);
            if (!boton.active) fondo = Paleta.mezclar(Paleta.VANO, Paleta.papelAviso(), 0.72F);
            bordeBase = Paleta.UI_TINTA_TENUE;
            tinta = Paleta.tintaPrincipal();
            tintaTenue = Paleta.tintaSecundaria();
        }

        // Cubre completamente la textura vanilla y vuelve a dibujar su etiqueta.
        g.fill(x, y, x + w, y + h, fondo);
        int borde = Paleta.conAlfa(bordeBase,
                boton.active ? (foco ? 0.80F : 0.44F) : 0.22F);
        marco(g, x, y, w, h, borde);

        g.fill(x + 4, y + 3, x + 5, y + h - 3,
                Paleta.conAlfa(foco ? Paleta.UI_ACENTO_FUERTE : bordeBase,
                        foco ? 0.58F : 0.22F));
        g.fill(x + w - 8, y + h - 3, x + w - 4, y + h - 2,
                Paleta.conAlfa(tintaTenue, 0.26F));

        String texto = boton.getMessage().getString();
        int max = Math.max(8, w - 18);
        if (font.width(texto) > max) {
            texto = font.plainSubstrByWidth(texto,
                    Math.max(0, max - font.width("..."))) + "...";
        }
        int tw = font.width(texto);
        int color = boton.active ? tinta : Paleta.conAlfa(tintaTenue, 0.58F);
        g.drawString(font, texto, x + (w - tw) / 2,
                y + (h - font.lineHeight) / 2, color, false);

        if (foco) {
            int u = Math.max(5, Math.min(tw, w - 18));
            int ux = x + (w - u) / 2;
            int uy = y + (h + font.lineHeight) / 2;
            g.fill(ux, uy, ux + u, uy + 1,
                    Paleta.conAlfa(archivo ? Paleta.ARCHIVO_ACENTO : Paleta.UI_ACENTO, 0.50F));
        }
    }

    private static void dibujarCampo(GuiGraphics g, EditBox campo, boolean archivo) {
        if (!campo.isVisible()) return;
        int x = campo.getX();
        int y = campo.getY();
        int w = campo.getWidth();
        int h = campo.getHeight();
        if (w <= 2 || h <= 2) return;

        int base = archivo ? Paleta.ARCHIVO_ACENTO : Paleta.UI_TINTA_TENUE;
        int borde = Paleta.conAlfa(base, campo.isFocused() ? 0.86F : 0.48F);
        marco(g, x - 1, y - 1, w + 2, h + 2, borde);

        // Esquinas de ficha: no cubren texto, cursor ni seleccion de Minecraft.
        int marca = Paleta.conAlfa(campo.isFocused()
                ? Paleta.UI_ACENTO_FUERTE
                : (archivo ? Paleta.ARCHIVO_TEXTO_TENUE : Paleta.UI_TINTA),
                campo.isFocused() ? 0.58F : 0.26F);
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
