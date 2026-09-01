package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.PielVanillaJobs;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;

import java.nio.file.Path;
import java.util.function.Consumer;

/** Selector de paquetes vanilla conservado dentro de un archivador Jobs. */
public final class PantallaPaquetesJobs extends PackSelectionScreen {

    public PantallaPaquetesJobs(PackRepository repo, Consumer<PackRepository> callback,
                                Path directorio, Component titulo) {
        super(repo, callback, directorio, titulo);
    }

    @Override
    protected void init() {
        super.init();
        ListasExpediente.estilizar(this);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, 8, 6, this.width - 16, this.height - 12);
    }

    @Override
    public void renderDirtBackground(GuiGraphics g) {
        // PackSelectionScreen llama este camino directamente en 1.20.1. Si no
        // se intercepta, el dirt vanilla vuelve a tapar la hoja Jobs.
        renderBackground(g);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        PielVanillaJobs.dibujar(this, g, mouseX, mouseY);
        ChromeExpediente.marcoSubpantalla(g, this.font, this.width, this.height,
                8, 6, this.width - 16, this.height - 12,
                Component.translatable("jobsmenu.interfaz.paquetes.subtitulo"), "ARC-014");
    }
}
