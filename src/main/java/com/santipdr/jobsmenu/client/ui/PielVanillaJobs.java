package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

/**
 * Piel posterior para controles vanilla que deben conservar su logica real.
 * No reemplaza listeners, hitboxes, drag, foco ni callbacks.
 */
public final class PielVanillaJobs {

    private PielVanillaJobs() {
    }

    public static void dibujar(Screen pantalla, GuiGraphics g, int mouseX, int mouseY) {
        if (pantalla == null || g == null) return;
        Font font = Minecraft.getInstance().font;
        boolean archivo = esArchivoOscuro(pantalla);
        float contraste = ConfigTurno.altoContraste() ? 1.18F : 1.0F;

        for (var child : pantalla.children()) {
            if (child.getClass().getName().startsWith("com.santipdr.jobsmenu.")) continue;
            if (child instanceof AbstractButton boton && !(child instanceof AbstractSliderButton)) {
                dibujarBoton(g, font, boton, mouseX, mouseY, archivo, contraste);
            } else if (child instanceof AbstractSliderButton slider) {
                dibujarSlider(g, slider, mouseX, mouseY, archivo, contraste);
            } else if (child instanceof EditBox campo) {
                dibujarCampo(g, campo, archivo, contraste);
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
                                      int mouseX, int mouseY, boolean archivo, float contraste) {
        if (!slider.visible) return;
        int x = slider.getX();
        int y = slider.getY();
        int w = slider.getWidth();
        int h = slider.getHeight();
        if (w <= 4 || h <= 4) return;

        boolean raton = slider.active && slider.isMouseOver(mouseX, mouseY);
        boolean teclado = slider.active && slider.isFocused() && !raton;
        boolean foco = raton || teclado;
        int base = archivo ? Paleta.ARCHIVO_ACENTO : Paleta.UI_TINTA_TENUE;
        int texto = archivo ? Paleta.ARCHIVO_TEXTO : Paleta.UI_TINTA;

        g.fill(x + 2, y + h, x + w + 1, y + h + 2,
                Paleta.conAlfa(Paleta.VANO, slider.active ? 0.14F : 0.07F));
        marco(g, x, y, w, h,
                Paleta.conAlfa(base, limitar((foco ? 0.74F : 0.38F) * contraste)));
        marco(g, x + 2, y + 2, w - 4, h - 4,
                Paleta.conAlfa(base, foco ? 0.11F : 0.055F));
        g.fill(x + 4, y + 3, x + w - 4, y + 4,
                Paleta.conAlfa(archivo ? Paleta.ARCHIVO_TEXTO_TENUE : Paleta.UI_PAPEL_FOCO,
                        slider.active ? 0.16F : 0.06F));

        int by = y + h - 5;
        g.fill(x + 7, by - 1, x + w - 7, by + 2,
                Paleta.conAlfa(Paleta.VANO, 0.055F));
        g.fill(x + 7, by, x + w - 7, by + 1,
                Paleta.conAlfa(base, slider.active ? 0.35F : 0.16F));

        for (int i = 0; i <= 10; i++) {
            int tx = x + 7 + Math.round((w - 14) * (i / 10.0F));
            boolean mayor = i == 0 || i == 5 || i == 10;
            int th = mayor ? 4 : 2;
            g.fill(tx, by - th, tx + 1, by,
                    Paleta.conAlfa(texto, slider.active
                            ? (mayor ? 0.42F : 0.18F) : (mayor ? 0.18F : 0.08F)));
        }

        if (foco) {
            int c = Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : base,
                    limitar((teclado ? 0.70F : 0.46F) * contraste));
            g.fill(x + 3, y + 3, x + 4, y + h - 3, c);
            g.fill(x + w - 4, y + 4, x + w - 3, y + h - 4,
                    Paleta.conAlfa(teclado ? Paleta.UI_ACENTO_FUERTE : base,
                            teclado ? 0.42F : 0.22F));
            if (teclado && w >= 52) {
                int cx = x + w / 2;
                g.fill(cx - 7, y - 2, cx + 8, y - 1,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.42F));
                g.fill(cx, y - 4, cx + 1, y - 1,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.28F));
            }
        }

        if (!slider.active) {
            int cy = y + h / 2;
            g.fill(x + 5, cy, x + 10, cy + 1,
                    Paleta.conAlfa(texto, 0.18F));
            g.fill(x + w - 10, cy, x + w - 5, cy + 1,
                    Paleta.conAlfa(texto, 0.12F));
        }
    }

    private static void dibujarBoton(GuiGraphics g, Font font, AbstractButton boton,
                                     int mouseX, int mouseY, boolean archivo, float contraste) {
        if (!boton.visible) return;
        int x = boton.getX();
        int y = boton.getY();
        int w = boton.getWidth();
        int h = boton.getHeight();
        if (w <= 2 || h <= 2) return;

        boolean raton = boton.active && boton.isMouseOver(mouseX, mouseY);
        boolean teclado = boton.active && boton.isFocused() && !raton;
        boolean foco = raton || teclado;
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
            int papel = Paleta.papelAviso();
            fondo = Paleta.mezclar(papel, Paleta.UI_PAPEL_FOCO, foco ? 0.76F : 0.16F);
            if (!boton.active) fondo = Paleta.mezclar(Paleta.VANO, papel, 0.72F);
            bordeBase = Paleta.UI_TINTA_TENUE;
            tinta = Paleta.tintaPrincipal();
            tintaTenue = Paleta.tintaSecundaria();
        }

        g.fill(x + 2, y + h, x + w + 1, y + h + 2,
                Paleta.conAlfa(Paleta.VANO, boton.active ? 0.16F : 0.07F));
        g.fill(x, y, x + w, y + h, fondo);
        marco(g, x, y, w, h,
                Paleta.conAlfa(bordeBase, limitar((boton.active
                        ? (foco ? 0.82F : 0.44F) : 0.22F) * contraste)));
        marco(g, x + 2, y + 2, w - 4, h - 4,
                Paleta.conAlfa(tintaTenue, boton.active ? (foco ? 0.12F : 0.06F) : 0.035F));

        g.fill(x + 4, y + 3, x + w - 4, y + 4,
                Paleta.conAlfa(archivo ? Paleta.ARCHIVO_TEXTO_TENUE : Paleta.UI_PAPEL_FOCO,
                        boton.active ? 0.17F : 0.06F));
        g.fill(x + 4, y + 3, x + 5, y + h - 3,
                Paleta.conAlfa(foco ? Paleta.UI_ACENTO_FUERTE : bordeBase,
                        foco ? 0.58F : 0.22F));
        g.fill(x + w - 8, y + h - 3, x + w - 4, y + h - 2,
                Paleta.conAlfa(tintaTenue, 0.26F));

        String texto = boton.getMessage().getString();
        int max = Math.max(8, w - 18);
        boolean recortado = font.width(texto) > max;
        if (recortado) {
            texto = font.plainSubstrByWidth(texto,
                    Math.max(0, max - font.width("..."))) + "...";
        }
        int tw = font.width(texto);
        int color = boton.active ? tinta : Paleta.conAlfa(tintaTenue, 0.58F);
        int tx = x + (w - tw) / 2;
        int ty = y + (h - font.lineHeight) / 2;
        g.drawString(font, texto, tx, ty, color, false);

        if (foco) {
            int u = Math.max(5, Math.min(tw, w - 18));
            int ux = x + (w - u) / 2;
            int uy = y + (h + font.lineHeight) / 2;
            g.fill(ux, uy, ux + u, uy + 1,
                    Paleta.conAlfa(archivo ? Paleta.ARCHIVO_ACENTO : Paleta.UI_ACENTO,
                            teclado ? 0.62F : 0.48F));
            if (teclado) {
                int cy = y + h / 2;
                g.fill(x - 3, cy - 2, x - 1, cy + 3,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.68F));
                g.fill(x + w + 1, cy - 1, x + w + 3, cy + 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.36F));
                if (w >= 52) {
                    int cx = x + w / 2;
                    g.fill(cx - 6, y - 2, cx + 7, y - 1,
                            Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.34F));
                }
            }
        }

        if (recortado && boton.active) {
            int my = ty + font.lineHeight / 2;
            g.fill(x + w - 9, my, x + w - 7, my + 1,
                    Paleta.conAlfa(tintaTenue, 0.38F));
            g.fill(x + w - 6, my, x + w - 5, my + 1,
                    Paleta.conAlfa(tintaTenue, 0.22F));
        }

        if (!boton.active) {
            int cy = y + h / 2;
            g.fill(x + 5, cy, x + 10, cy + 1,
                    Paleta.conAlfa(tintaTenue, 0.24F));
            g.fill(x + w - 10, cy, x + w - 5, cy + 1,
                    Paleta.conAlfa(tintaTenue, 0.16F));
            if (w >= 44) {
                g.fill(x + w / 2 - 3, y + h - 3, x + w / 2 + 4, y + h - 2,
                        Paleta.conAlfa(tintaTenue, 0.11F));
            }
        }
    }

    private static void dibujarCampo(GuiGraphics g, EditBox campo,
                                     boolean archivo, float contraste) {
        if (!campo.isVisible()) return;
        int x = campo.getX();
        int y = campo.getY();
        int w = campo.getWidth();
        int h = campo.getHeight();
        if (w <= 2 || h <= 2) return;

        boolean foco = campo.isFocused();
        int base = archivo ? Paleta.ARCHIVO_ACENTO : Paleta.UI_TINTA_TENUE;
        int tinta = archivo ? Paleta.ARCHIVO_TEXTO_TENUE : Paleta.UI_TINTA;
        int borde = Paleta.conAlfa(base,
                limitar((foco ? 0.88F : 0.48F) * contraste));

        marco(g, x - 1, y - 1, w + 2, h + 2, borde);
        marco(g, x + 1, y + 1, w - 2, h - 2,
                Paleta.conAlfa(base, foco ? 0.13F : 0.055F));

        int marca = Paleta.conAlfa(foco ? Paleta.UI_ACENTO_FUERTE : tinta,
                foco ? 0.60F : 0.26F);
        g.fill(x + 2, y + 2, x + 8, y + 3, marca);
        g.fill(x + 2, y + 2, x + 3, y + 7, marca);
        g.fill(x + w - 8, y + h - 3, x + w - 2, y + h - 2, marca);
        g.fill(x + w - 3, y + h - 7, x + w - 2, y + h - 2, marca);

        if (foco) {
            int cx = x + w / 2;
            g.fill(cx - 8, y - 3, cx + 9, y - 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.32F));
            g.fill(cx, y - 5, cx + 1, y - 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.22F));
            g.fill(x + 4, y + h + 1, x + Math.min(w - 4, 28), y + h + 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO, 0.22F));
        } else {
            int cy = y + h / 2;
            g.fill(x + 4, cy, x + 8, cy + 1,
                    Paleta.conAlfa(tinta, 0.12F));
            g.fill(x + w - 8, cy, x + w - 4, cy + 1,
                    Paleta.conAlfa(tinta, 0.08F));
        }
    }

    private static void marco(GuiGraphics g, int x, int y, int w, int h, int c) {
        if (w < 2 || h < 2) return;
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private static float limitar(float valor) {
        return Math.max(0.0F, Math.min(1.0F, valor));
    }
}
