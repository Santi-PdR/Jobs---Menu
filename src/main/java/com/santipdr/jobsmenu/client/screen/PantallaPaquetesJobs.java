package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.mojang.blaze3d.systems.RenderSystem;

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
        super(repo, callback, directorio, Component.empty());
    }

    @Override
    protected void init() {
        super.init();
        // Son dos listas independientes; cambiarles el ancho con updateSize las
        // superpone. Solo se retiran dirt y bandas vanilla.
        ListasExpediente.estilizar(this);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panelArchivo(g, 12, 8, this.width - 24, this.height - 16);
    }

    @Override
    public void renderDirtBackground(GuiGraphics g) {
        // PackSelectionScreen llama este camino directamente en 1.20.1. Si no
        // se intercepta, el dirt vanilla vuelve a tapar la hoja Jobs.
        renderBackground(g);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderColor(0.72F, 0.67F, 0.52F, 1.0F);
        super.render(g, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ChromeExpediente.reemplazarRotuloArchivo(g, this.font,
                Component.translatable("jobsmenu.interfaz.recursos.titulo"),
                12, 8, this.width - 24);
    }
}
