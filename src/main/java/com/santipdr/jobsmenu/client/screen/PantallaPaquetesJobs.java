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
    private int listaArriba;
    private int listaAbajo;

    public PantallaPaquetesJobs(PackRepository repo, Consumer<PackRepository> callback,
                                Path directorio, Component titulo) {
        super(repo, callback, directorio, Component.empty());
        this.tituloJobs = titulo;
    }

    @Override
    protected void init() {
        super.init();
        this.listaArriba = 66;
        this.listaAbajo = Math.max(this.listaArriba + 70, this.height - 56);
        ListasExpediente.estilizar(this, this.listaArriba, this.listaAbajo);
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
        int top = Math.max(panelY + 46, this.listaArriba - 5);
        int bottom = Math.min(panelY + panelH - 40, this.listaAbajo + 4);

        // PackSelectionScreen usa dos listas de 200 px logicos. El pase anterior
        // pintaba columnas hasta los bordes y por eso la captura parecia vacia.
        // Ahora el archivador coincide con el ancho real de los widgets vanilla.
        int gap = 8;
        int colW = Math.min(204, Math.max(120, (panelW - 44) / 2));
        int leftX = center - gap / 2 - colW;
        int rightX = center + gap / 2;

        dibujarBandeja(g, leftX, top, colW, bottom);
        dibujarBandeja(g, rightX, top, colW, bottom);
        g.fill(center - 1, top + 5, center + 1, bottom - 5,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.34F));

        // Lineas de archivo muy tenues convierten el espacio libre en una
        // bandeja intencional sin interferir con iconos, textos ni clicks.
        for (int y = top + 38; y < bottom - 8; y += 38) {
            g.fill(leftX + 10, y, leftX + colW - 10, y + 1,
                    Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.065F));
            g.fill(rightX + 10, y, rightX + colW - 10, y + 1,
                    Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.065F));
        }

        RenderSystem.setShaderColor(0.78F, 0.78F, 0.78F, 1.0F);
        super.render(g, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Una sola cabecera frontal; no dejamos texto vanilla duplicado.
        g.pose().pushPose();
        g.pose().translate(0.0F, 0.0F, 450.0F);
        g.fill(panelX + 3, panelY + 1, panelX + panelW - 3, panelY + 36, Paleta.VANO);
        g.pose().popPose();
        ChromeExpediente.reemplazarCabeceraArchivo(g, this.font,
                Component.translatable("jobsmenu.interfaz.recursos.titulo"),
                this.tituloJobs, panelX, panelY, panelW);
        ListasExpediente.renderarBarras(this, g);

        int railY = this.height - 27;
        g.fill(panelX + 12, railY, panelX + panelW - 12, railY + 1,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.25F));
        String sello = "JOBS / RESOURCE ARCHIVE";
        int sw = this.font.width(sello);
        if (sw + 36 < panelW) {
            g.drawString(this.font, sello, panelX + panelW - sw - 16, railY + 6,
                    Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.52F), false);
        }
    }

    private void dibujarBandeja(GuiGraphics g, int x, int top, int w, int bottom) {
        g.fill(x, top, x + w, bottom, Paleta.conAlfa(Paleta.ARCHIVO_FONDO, 0.50F));
        g.fill(x, top, x + w, top + 2, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.36F));
        g.fill(x, bottom - 2, x + w, bottom, Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.18F));
        g.fill(x + 5, top + 5, x + 8, bottom - 5,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.22F));
        int tabW = Math.min(58, Math.max(28, w / 3));
        g.fill(x + 8, top - 4, x + 8 + tabW, top,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.34F));
    }
}
