package com.santipdr.jobsmenu.client.ui;

import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Elementos contextuales del menu principal. No mueve ni altera el fondo. */
public final class HudPrincipalJobs {

    private HudPrincipalJobs() {
    }

    public static void dibujar(GuiGraphics g, int ancho, int alto,
                               RotacionNiveles.Estado estado) {
        if (g == null || estado == null || ConfigTurno.interfazMinima()) return;
        if (ancho < 420 || alto < 230) return;

        float luz = Math.max(0.10F, estado.luz());
        int m = 12;
        int panelW = Math.min(154, Math.max(118, ancho / 5));
        int x = ancho - panelW - m;
        int y = Math.max(54, alto / 2 - 42);
        int h = 84;

        g.fill(x + 2, y + 3, x + panelW + 2, y + h + 3,
                Paleta.conAlfa(Paleta.VANO, 0.22F));
        g.fill(x, y, x + panelW, y + h,
                Paleta.conAlfa(Paleta.VANO, 0.40F));
        g.fill(x, y, x + 2, y + h,
                Paleta.conAlfa(Paleta.papelAviso(), 0.22F * luz));
        g.fill(x + 8, y + 17, x + panelW - 8, y + 18,
                Paleta.conAlfa(Paleta.papelAviso(), 0.10F * luz));

        Minecraft mc = Minecraft.getInstance();
        String titulo = Component.translatable("jobsmenu.hud.turno").getString();
        String nivel = Component.translatable("jobsmenu.hud.nivel", estado.indice()).getString();
        String estadoTxt = estado.enTransicion()
                ? Component.translatable("jobsmenu.estado.transicion").getString()
                : Component.translatable("jobsmenu.estado.normal").getString();
        String ayuda = Component.translatable("jobsmenu.hud.atajos").getString();

        g.drawString(mc.font, ChromeExpediente.ajustar(mc.font, titulo, panelW - 20),
                x + 9, y + 6, Paleta.conAlfa(Paleta.papelAviso(), 0.72F * luz), false);
        g.drawString(mc.font, ChromeExpediente.ajustar(mc.font, nivel, panelW - 20),
                x + 9, y + 24, Paleta.conAlfa(Paleta.papelAviso(), 0.92F * luz), false);
        g.drawString(mc.font, ChromeExpediente.ajustar(mc.font, estadoTxt, panelW - 20),
                x + 9, y + 36, Paleta.conAlfa(Paleta.papelAviso(), 0.48F * luz), false);

        int by = y + 55;
        g.fill(x + 9, by, x + panelW - 9, by + 1,
                Paleta.conAlfa(Paleta.papelAviso(), 0.08F * luz));
        g.drawString(mc.font, ChromeExpediente.ajustar(mc.font, ayuda, panelW - 20),
                x + 9, by + 7, Paleta.conAlfa(Paleta.papelAviso(), 0.34F * luz), false);

        int barras = 5;
        for (int i = 0; i < barras; i++) {
            int bx = x + panelW - 9 - i * 5;
            float a = 0.07F + i * 0.025F;
            g.fill(bx, y + h - 8, bx + 2, y + h - 5,
                    Paleta.conAlfa(Paleta.papelAviso(), a * luz));
        }
    }
}
