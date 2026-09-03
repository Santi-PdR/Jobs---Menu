package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.SesionMenu;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;
import com.santipdr.jobsmenu.config.PerfilesJobs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Elementos contextuales del menu principal. No mueve ni altera el fondo. */
public final class HudPrincipalJobs {

    private HudPrincipalJobs() {
    }

    public static void dibujar(GuiGraphics g, int ancho, int alto,
                               RotacionNiveles.Estado estado) {
        if (g == null || estado == null || ConfigTurno.interfazMinima()) return;
        if (ancho < 420 || alto < 230) return;

        float luz = Math.max(0.10F, estado.luz());
        float contraste = ConfigTurno.altoContraste() ? 1.25F : 1.0F;
        int m = 12;
        int panelW = Math.min(178, Math.max(136, ancho / 5));
        int x = ancho - panelW - m;
        int h = 124;
        int y = Math.max(44, alto / 2 - h / 2);
        Font font = Minecraft.getInstance().font;

        // Profundidad de placa, borde lateral y registros superior/inferior.
        g.fill(x + 4, y + 5, x + panelW + 4, y + h + 5,
                Paleta.conAlfa(Paleta.VANO, 0.24F));
        g.fill(x + 2, y + 3, x + panelW + 2, y + h + 3,
                Paleta.conAlfa(Paleta.VANO, 0.12F));
        g.fill(x, y, x + panelW, y + h,
                Paleta.conAlfa(Paleta.VANO, 0.42F));
        g.fill(x, y, x + 2, y + h,
                Paleta.conAlfa(Paleta.papelAviso(), limitar(0.24F * luz * contraste)));
        g.fill(x + 7, y + 17, x + panelW - 7, y + 18,
                Paleta.conAlfa(Paleta.papelAviso(), 0.11F * luz));
        g.fill(x + 7, y + h - 17, x + panelW - 7, y + h - 16,
                Paleta.conAlfa(Paleta.papelAviso(), 0.065F * luz));
        g.fill(x + panelW - 2, y + 10, x + panelW - 1, y + h - 10,
                Paleta.conAlfa(Paleta.papelAviso(), 0.045F * luz));

        String titulo = "JOBS / SHIFT CONTROL";
        String nivel = Component.translatable("jobsmenu.nivel.actual", estado.indice()).getString();
        String estadoTxt;
        if (estado.enSuspension()) estadoTxt = Component.translatable("jobsmenu.estado.suspension").getString();
        else if (estado.enTransicion()) estadoTxt = Component.translatable("jobsmenu.estado.transicion").getString();
        else estadoTxt = Component.translatable("jobsmenu.estado.normal").getString();

        g.drawString(font, ChromeExpediente.ajustar(font, titulo, panelW - 18),
                x + 9, y + 6,
                Paleta.conAlfa(Paleta.papelAviso(), limitar(0.74F * luz * contraste)), false);
        g.drawString(font, ChromeExpediente.ajustar(font, nivel, panelW - 18),
                x + 9, y + 24,
                Paleta.conAlfa(Paleta.papelAviso(), limitar(0.94F * luz * contraste)), false);
        g.drawString(font, ChromeExpediente.ajustar(font, estadoTxt, panelW - 18),
                x + 9, y + 36,
                Paleta.conAlfa(Paleta.papelAviso(), limitar(0.52F * luz * contraste)), false);

        int indicadorY = y + 49;
        estadoLed(g, font, x + 9, indicadorY, "R", ConfigTurno.rotarNiveles(), luz, contraste);
        estadoLed(g, font, x + 26, indicadorY, "A", ConfigTurno.sonidoAmbiente(), luz, contraste);
        estadoLed(g, font, x + 43, indicadorY, "M", ConfigTurno.musicaMenu(), luz, contraste);
        estadoLed(g, font, x + 60, indicadorY, "U", ConfigTurno.sonidoBotones(), luz, contraste);

        PerfilesJobs.Perfil perfil = PerfilesJobs.actual();
        String p = perfil == null ? "CUSTOM" : codigoPerfil(perfil);
        int pw = font.width(p);
        g.drawString(font, p, x + panelW - pw - 9, indicadorY + 1,
                Paleta.conAlfa(Paleta.papelAviso(), limitar(0.35F * luz * contraste)), false);

        // Progreso real de estancia y tiempo aproximado al siguiente traslado.
        int barY = y + 65;
        int barX0 = x + 9;
        int barX1 = x + panelW - 9;
        int largo = Math.max(1, barX1 - barX0);
        g.fill(barX0, barY, barX1, barY + 2,
                Paleta.conAlfa(Paleta.papelAviso(), 0.085F * luz));
        float avance = avanceEstancia(estado);
        int fin = barX0 + Math.max(1, Math.round(largo * avance));
        g.fill(barX0, barY, fin, barY + 2,
                Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.34F * luz * contraste)));
        g.fill(Math.max(barX0, fin - 1), barY - 1, Math.min(barX1, fin + 1), barY + 3,
                Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, limitar(0.20F * luz * contraste)));
        for (int i = 0; i <= 4; i++) {
            int tx = barX0 + Math.round(largo * (i / 4.0F));
            g.fill(tx, barY - 2, tx + 1, barY,
                    Paleta.conAlfa(Paleta.papelAviso(), i == 0 || i == 4 ? 0.18F : 0.09F));
        }

        String restante = textoRestante(estado);
        g.drawString(font, restante, barX0, barY + 5,
                Paleta.conAlfa(Paleta.papelAviso(), limitar(0.31F * luz * contraste)), false);
        String sesion = textoSesion();
        int sw = font.width(sesion);
        g.drawString(font, sesion, barX1 - sw, barY + 5,
                Paleta.conAlfa(Paleta.papelAviso(), limitar(0.27F * luz * contraste)), false);

        // Estado del volumen maestro Jobs con barra propia. MUTE se reconoce
        // aunque musica y ambiente individuales sigan habilitados.
        int volY = y + 82;
        int vol = ConfigTurno.volumenAvisoPorcentaje();
        String volTxt = vol <= 0 ? "MUTE" : String.format(Locale.ROOT, "VOL %03d", vol);
        g.drawString(font, volTxt, x + 9, volY,
                Paleta.conAlfa(Paleta.papelAviso(), limitar(0.34F * luz * contraste)), false);
        int vx0 = x + panelW - 52;
        int vx1 = x + panelW - 9;
        g.fill(vx0, volY + 3, vx1, volY + 4,
                Paleta.conAlfa(Paleta.papelAviso(), 0.07F * luz));
        int vfin = vx0 + Math.round((vx1 - vx0) * (vol / 100.0F));
        if (vfin > vx0) {
            g.fill(vx0, volY + 3, vfin, volY + 4,
                    Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.26F * luz * contraste)));
        }
        for (int i = 0; i <= 5; i++) {
            int tx = vx0 + Math.round((vx1 - vx0) * (i / 5.0F));
            g.fill(tx, volY + 1, tx + 1, volY + 5,
                    Paleta.conAlfa(Paleta.papelAviso(), 0.08F * luz));
        }

        int by = y + 95;
        dibujarTecla(g, font, x + 9, by, "1-4", luz, contraste);
        dibujarTecla(g, font, x + 37, by, "F", luz, contraste);
        dibujarTecla(g, font, x + 55, by, "M", luz, contraste);
        dibujarTecla(g, font, x + 73, by, "TAB", luz, contraste);
        dibujarTecla(g, font, x + 103, by, "ENTER", luz, contraste);

        int barras = 6;
        for (int i = 0; i < barras; i++) {
            int bx = x + panelW - 9 - i * 5;
            float a = 0.06F + i * 0.020F;
            int bh = 2 + (i % 3);
            g.fill(bx, y + h - 9 - bh, bx + 2, y + h - 9,
                    Paleta.conAlfa(Paleta.papelAviso(), a * luz));
        }

        int registro = x + panelW / 2;
        g.fill(registro - 11, y + h - 4, registro + 12, y + h - 3,
                Paleta.conAlfa(Paleta.papelAviso(), 0.065F * luz));
        g.fill(registro, y + h - 6, registro + 1, y + h - 1,
                Paleta.conAlfa(Paleta.UI_ACENTO, 0.075F * luz));
    }

    private static String textoRestante(RotacionNiveles.Estado estado) {
        if (!estado.rotacion() || estado.estancia() <= 0L) return "NXT HOLD";
        if (estado.enTransicion()) return "NXT MOVE";
        long ms = Math.max(0L, estado.estancia() - estado.dentro());
        long s = ms / 1000L;
        return String.format(Locale.ROOT, "NXT %02d:%02d", (s / 60L) % 100L, s % 60L);
    }

    private static String textoSesion() {
        long s = SesionMenu.duracionVisitaMs() / 1000L;
        return String.format(Locale.ROOT, "T+%02d:%02d", (s / 60L) % 100L, s % 60L);
    }

    private static float avanceEstancia(RotacionNiveles.Estado estado) {
        if (!estado.rotacion() || estado.estancia() <= 0L) return 1.0F;
        if (estado.enTransicion()) return 1.0F;
        return Math.max(0.0F, Math.min(1.0F, estado.dentro() / (float) estado.estancia()));
    }

    private static void estadoLed(GuiGraphics g, Font font, int x, int y, String etiqueta,
                                  boolean activo, float luz, float contraste) {
        int base = activo ? Paleta.UI_ACENTO_FUERTE : Paleta.papelAviso();
        float a = activo ? 0.48F : 0.12F;
        g.fill(x, y, x + 5, y + 5, Paleta.conAlfa(Paleta.VANO, 0.16F));
        g.fill(x + 1, y + 1, x + 4, y + 4,
                Paleta.conAlfa(base, limitar(a * luz * contraste)));
        g.drawString(font, etiqueta, x + 7, y - 2,
                Paleta.conAlfa(Paleta.papelAviso(), limitar(0.25F * luz * contraste)), false);
    }

    private static void dibujarTecla(GuiGraphics g, Font font, int x, int y, String txt,
                                     float luz, float contraste) {
        int tw = font.width(txt);
        int w = tw + 7;
        g.fill(x, y, x + w, y + 11, Paleta.conAlfa(Paleta.VANO, 0.13F));
        g.fill(x, y + 10, x + w, y + 11,
                Paleta.conAlfa(Paleta.papelAviso(), limitar(0.13F * luz * contraste)));
        g.fill(x + 2, y + 2, x + 3, y + 8,
                Paleta.conAlfa(Paleta.UI_ACENTO, limitar(0.09F * luz * contraste)));
        g.drawString(font, txt, x + 3, y + 1,
                Paleta.conAlfa(Paleta.papelAviso(), limitar(0.44F * luz * contraste)), false);
    }

    private static String codigoPerfil(PerfilesJobs.Perfil perfil) {
        return switch (perfil) {
            case EQUILIBRADO -> "EQ";
            case INMERSIVO -> "IMM";
            case RENDIMIENTO -> "PERF";
            case ACCESIBLE -> "ACC";
            case MINIMO -> "MIN";
        };
    }

    private static float limitar(float valor) {
        return Math.max(0.0F, Math.min(1.0F, valor));
    }
}
