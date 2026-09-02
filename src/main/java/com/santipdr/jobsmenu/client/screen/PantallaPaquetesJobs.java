package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;

import java.nio.file.Path;
import java.util.function.Consumer;

/** Selector de paquetes vanilla conservado dentro de un archivador Jobs. */
public final class PantallaPaquetesJobs extends PackSelectionScreen {

    private final Component tituloJobs;

    public PantallaPaquetesJobs(PackRepository repo, Consumer<PackRepository> callback,
                                Path directorio, Component titulo) {
        super(repo, callback, directorio, Component.empty());
        this.tituloJobs = titulo;
    }

    @Override
    protected void init() {
        super.init();
        ListasExpediente.estilizar(this);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panelArchivo(g, 12, 8, this.width - 24, this.height - 16);
    }

    @Override
    public void renderDirtBackground(GuiGraphics g) {
        renderBackground(g);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int panelX = 12;
        int panelY = 8;
        int panelW = this.width - 24;
        int panelH = this.height - 16;
        int center = this.width / 2;
        int top = panelY + 38;
        int bottom = panelY + panelH - 38;

        int leftX = panelX + 10;
        int rightX = center + 4;
        int leftW = Math.max(40, center - leftX - 6);
        int rightW = Math.max(40, panelX + panelW - rightX - 10);
        g.fill(leftX, top, leftX + leftW, bottom, Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.38F));
        g.fill(rightX, top, rightX + rightW, bottom, Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.38F));
        g.fill(center - 1, top + 3, center + 1, bottom - 3, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.28F));

        for (int x : new int[]{leftX, rightX}) {
            int w = x == leftX ? leftW : rightW;
            g.fill(x, top, x + w, top + 2, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.34F));
            g.fill(x, bottom - 2, x + w, bottom, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.18F));
            g.fill(x + 6, top - 4, x + Math.min(w - 6, 54), top, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.30F));
        }

        RenderSystem.setShaderColor(0.72F, 0.72F, 0.72F, 1.0F);
        super.render(g, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        ChromeExpediente.reemplazarCabeceraArchivo(g, this.font, this.tituloJobs,
                Component.empty(), panelX, panelY, panelW);
        ListasExpediente.renderarBarras(this, g);

        int railY = this.height - 27;
        g.fill(panelX + 12, railY, panelX + panelW - 12, railY + 1,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.25F));
        String sello = "JOBS / RESOURCE ARCHIVE";
        int sw = this.font.width(sello);
        g.drawString(this.font, sello, panelX + panelW - sw - 16, railY + 6,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.52F), false);
    }
}
