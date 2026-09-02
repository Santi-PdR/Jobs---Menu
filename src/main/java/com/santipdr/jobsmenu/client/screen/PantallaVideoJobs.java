package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.GeometriaExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Ajustes de video vanilla, presentados como expediente Jobs. */
public final class PantallaVideoJobs extends VideoSettingsScreen {

    private GeometriaExpediente.Panel panel;

    public PantallaVideoJobs(Screen anterior, Options opciones) {
        super(anterior, opciones);
    }

    @Override
    protected void init() {
        super.init();
        this.panel = GeometriaExpediente.compacto(this.width, this.height, 430, 318);
        ListasExpediente.estilizar(this, panel.listaArriba(), panel.listaAbajo());
        for (var child : this.children()) {
            if (child instanceof Button b && b.getMessage().equals(CommonComponents.GUI_DONE)) {
                b.visible = false;
                b.active = false;
                break;
            }
        }
        this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - 70, panel.botonY(), 140, 20,
                Component.translatable("jobsmenu.interfaz.volver"),
                BotonExpediente.Tipo.PRINCIPAL, this::onClose));
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        if (panel == null) return;

        ChromeExpediente.panel(g, panel.x(), panel.y(), panel.w(), panel.h());

        int x0 = panel.x() + 12;
        int x1 = panel.x() + panel.w() - 12;
        int y0 = panel.listaArriba() - 7;
        int y1 = panel.listaAbajo() + 5;
        int linea = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.14F);
        int fina = Paleta.conAlfa(Paleta.tintaSecundaria(), 0.07F);
        int acento = Paleta.conAlfa(Paleta.UI_ACENTO, 0.38F);

        // Marco de visor: hace que la lista parezca una ficha de imagen calibrada.
        g.fill(x0, y0, x1, y0 + 1, linea);
        g.fill(x0, y1, x1, y1 + 1, linea);
        g.fill(x0, y0, x0 + 1, y1 + 1, fina);
        g.fill(x1 - 1, y0, x1, y1 + 1, fina);

        // Esquinas de calibracion sin invadir ningun hitbox.
        g.fill(x0, y0, x0 + 8, y0 + 1, acento);
        g.fill(x0, y0, x0 + 1, y0 + 8, acento);
        g.fill(x1 - 8, y0, x1, y0 + 1, acento);
        g.fill(x1 - 1, y0, x1, y0 + 8, acento);
        g.fill(x0, y1, x0 + 8, y1 + 1, acento);
        g.fill(x0, y1 - 7, x0 + 1, y1 + 1, acento);
        g.fill(x1 - 8, y1, x1, y1 + 1, acento);
        g.fill(x1 - 1, y1 - 7, x1, y1 + 1, acento);

        // Regla inferior de escala visual.
        int tramo = Math.max(1, (x1 - x0 - 20) / 6);
        for (int i = 0; i <= 6; i++) {
            int mx = x0 + 10 + i * tramo;
            int alto = i == 0 || i == 3 || i == 6 ? 4 : 2;
            g.fill(mx, y1 - alto - 1, mx + 1, y1 - 1, fina);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        ListasExpediente.renderarBarras(this, g);
        ChromeExpediente.marcoSubpantalla(g, this.font, this.width, this.height,
                panel.x(), panel.y(), panel.w(), panel.h(),
                Component.translatable("jobsmenu.interfaz.video.subtitulo"), "IMG-020");

        // Centro optico: solo una guia tenue, nunca cruza la lista completa.
        int centro = this.width / 2;
        int marca = Paleta.conAlfa(Paleta.UI_ACENTO, 0.16F);
        g.fill(centro - 6, panel.listaArriba() - 4, centro + 7, panel.listaArriba() - 3, marca);
        g.fill(centro, panel.listaArriba() - 6, centro + 1, panel.listaArriba() - 1, marca);
    }
}
